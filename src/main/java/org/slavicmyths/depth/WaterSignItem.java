package org.slavicmyths.depth;
import net.minecraft.item.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
public final class WaterSignItem extends Item {
 public WaterSignItem(Properties p){super(p);}
 public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){if(!w.isClientSide&&!p.getCooldowns().isOnCooldown(this)){p.getCooldowns().addCooldown(this,100);org.slavicmyths.progression.Knowledge.award(p,"depth_clue");BlockPos nearest=PoolIndex.get((ServerWorld)w).nearest(p.blockPosition());String direction="unknown";if(nearest!=null){double dx=nearest.getX()-p.getX(),dz=nearest.getZ()-p.getZ();direction=dx*dx+dz*dz<32*32?"near":Math.abs(dx)>Math.abs(dz)?dx>0?"east":"west":dz>0?"south":"north";}p.displayClientMessage(new TranslationTextComponent("depth.slavicmyths.hint."+direction),false);}return ActionResult.sidedSuccess(p.getItemInHand(h),w.isClientSide);}
}
