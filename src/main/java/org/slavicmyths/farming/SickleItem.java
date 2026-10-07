package org.slavicmyths.farming;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public final class SickleItem extends Item {
    public SickleItem(Properties properties) { super(properties.durability(250)); }
    @Override public int getEnchantmentValue() { return 14; }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos(); var player = context.getPlayer();
        var state = level.getBlockState(pos);
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()) || !level.mayInteract(player, pos)
                || !state.is(Farming.HARVESTABLE) || !(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            var drops = Block.getDrops(state, server, pos, null, player, context.getItemInHand());
            if (!server.setBlock(pos, crop.getStateForAge(1), 3)) return InteractionResult.FAIL;
            drops.forEach(stack -> Block.popResource(server, pos, stack));
            context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            server.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, .7F, 1.1F);
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX()+.5, pos.getY()+.4, pos.getZ()+.5, 6, .2,.2,.2,.02);
            server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
