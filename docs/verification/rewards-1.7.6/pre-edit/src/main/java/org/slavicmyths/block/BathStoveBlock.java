package org.slavicmyths.block;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.slavicmyths.entity.BannikEntity;
public final class BathStoveBlock extends Block {
    public BathStoveBlock(Properties p){super(p);}
    @Override public InteractionResult useWithoutItem(BlockState s,Level w,BlockPos pos,Player p,BlockHitResult hit){return useItemOn(p.getItemInHand(InteractionHand.MAIN_HAND),s,w,pos,p,InteractionHand.MAIN_HAND,hit).result();}
@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,BlockState s,Level w,BlockPos pos,Player p,InteractionHand h,BlockHitResult hit){
        for(BannikEntity b:w.getEntitiesOfClass(BannikEntity.class,new AABB(pos).inflate(8)))if(b.offer(p,h))return net.minecraft.world.ItemInteractionResult.sidedSuccess(w.isClientSide);
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
