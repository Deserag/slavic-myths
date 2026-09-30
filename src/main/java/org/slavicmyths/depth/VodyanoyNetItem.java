package org.slavicmyths.depth;
import net.minecraft.item.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
public final class VodyanoyNetItem extends Item {
 public VodyanoyNetItem(Properties p){super(p.durability(64));}
 public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){ItemStack s=p.getItemInHand(h);if(p.getCooldowns().isOnCooldown(this))return ActionResult.fail(s);if(!w.isClientSide){ThrownNet net=new ThrownNet(w,p);net.shootFromRotation(p,p.xRot,p.yRot,0,1.2F,1);if(w.addFreshEntity(net)){p.getCooldowns().addCooldown(this,160);s.hurtAndBreak(1,p,e->e.broadcastBreakEvent(h));w.playSound(null,p.blockPosition(),SoundEvents.FISHING_BOBBER_THROW,SoundCategory.PLAYERS,.6F,.7F);}}return ActionResult.consume(s);}
}
