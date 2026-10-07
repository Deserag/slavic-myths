package org.slavicmyths.farming;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.CropBlock;

public final class FertilizerItem extends Item {
    public FertilizerItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var player = context.getPlayer(); var center = context.getClickedPos();
        if (player == null) return InteractionResult.PASS;
        var clicked=level.getBlockState(center);
        if(clicked.getBlock() instanceof org.slavicmyths.garden.PerennialBush && clicked.getValue(org.slavicmyths.garden.PerennialBush.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)center=center.below();
        if(!level.getBlockState(center).is(Farming.HARVESTABLE) && !(level.getBlockState(center).getBlock() instanceof org.slavicmyths.garden.PerennialBush))return InteractionResult.PASS;
        int count = 0;
        for (int x=-1;x<=1;x++) for (int z=-1;z<=1;z++) {
            var pos = center.offset(x,0,z);
            if (!level.hasChunkAt(pos) || !level.mayInteract(player,pos) || !player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand())) continue;
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof org.slavicmyths.garden.PerennialBush bush) {
                if(state.getValue(org.slavicmyths.garden.PerennialBush.HALF)!=net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER || state.getValue(org.slavicmyths.garden.PerennialBush.PHASE)>=4)continue;
                if(level instanceof ServerLevel server){
                    if(!bush.growTo(server,pos,state.getValue(org.slavicmyths.garden.PerennialBush.PHASE)+1))continue;
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,3,.2,.2,.2,.01);
                }
                count++;continue;
            }
            if (!state.is(Farming.HARVESTABLE) || !(state.getBlock() instanceof CropBlock crop) || crop.isMaxAge(state)) continue;
            if (level instanceof ServerLevel server) {
                if (!server.setBlock(pos,crop.getStateForAge(crop.getAge(state)+1),2)) continue;
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,3,.2,.2,.2,.01);
            }
            count++;
        }
        if (count == 0) return InteractionResult.PASS;
        if (!level.isClientSide) {
            context.getItemInHand().consume(1,player);
            level.playSound(null,center,SoundEvents.BONE_MEAL_USE,SoundSource.BLOCKS,.7F,1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
