package org.slavicmyths.depth;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
public final class WaterSignItem extends Item {
 public WaterSignItem(Properties p){super(p);}
 public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){if(!w.isClientSide&&!p.getCooldowns().isOnCooldown(this)){p.getCooldowns().addCooldown(this,100);org.slavicmyths.progression.Knowledge.award(p,"depth_clue");BlockPos nearest=PoolIndex.get((ServerLevel)w).nearest(p.blockPosition());String direction="unknown";if(nearest!=null){double dx=nearest.getX()-p.getX(),dz=nearest.getZ()-p.getZ();direction=dx*dx+dz*dz<32*32?"near":Math.abs(dx)>Math.abs(dz)?dx>0?"east":"west":dz>0?"south":"north";}p.displayClientMessage(Component.translatable("depth.slavicmyths.hint."+direction),false);}return InteractionResultHolder.sidedSuccess(p.getItemInHand(h),w.isClientSide);}
}
