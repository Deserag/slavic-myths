package org.slavicmyths.farming;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.phys.HitResult;
import org.slavicmyths.item.ItemState;

public final class WateringCanItem extends Item {
    public WateringCanItem(Properties properties) { super(properties.stacksTo(1).component(ItemState.WATER_CHARGES.get(), 0)); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.slavicmyths.watering_can.water", stack.getOrDefault(ItemState.WATER_CHARGES.get(), 0)));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || stack.getOrDefault(ItemState.WATER_CHARGES.get(), 0) == 8) return InteractionResultHolder.pass(stack);
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.mayInteract(player, hit.getBlockPos()) || !player.mayUseItemAt(hit.getBlockPos(), hit.getDirection(), stack)) return InteractionResultHolder.pass(stack);
        var fluid = level.getFluidState(hit.getBlockPos());
        if (!fluid.is(FluidTags.WATER) || !fluid.isSource()) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            stack.set(ItemState.WATER_CHARGES.get(), 8);
            level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var player = context.getPlayer(); var stack = context.getItemInHand();
        if (player == null || player.isShiftKeyDown() || stack.getOrDefault(ItemState.WATER_CHARGES.get(), 0) == 0) return InteractionResult.PASS;
        BlockPos center = context.getClickedPos();
        if (level.getBlockState(center).getBlock() instanceof CropBlock) center = center.below();
        if (!level.getBlockState(center).is(Blocks.FARMLAND)) return InteractionResult.PASS;
        int count = 0;
        for (int x=-1; x<=1; x++) for (int z=-1; z<=1; z++) {
            var pos = center.offset(x,0,z);
            if (!level.hasChunkAt(pos) || !level.mayInteract(player,pos) || !player.mayUseItemAt(pos,context.getClickedFace(),stack)) continue;
            var state = level.getBlockState(pos);
            if (!state.is(Blocks.FARMLAND)) continue;
            count++;
            if (level instanceof ServerLevel server) {
                server.setBlock(pos, state.setValue(FarmBlock.MOISTURE,7), 2);
                server.sendParticles(ParticleTypes.SPLASH,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,3,.25,.05,.25,.01);
            }
        }
        if (count == 0) return InteractionResult.PASS;
        if (!level.isClientSide) {
            stack.set(ItemState.WATER_CHARGES.get(), stack.getOrDefault(ItemState.WATER_CHARGES.get(), 0)-1);
            level.playSound(null, center, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS,.6F,1.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
