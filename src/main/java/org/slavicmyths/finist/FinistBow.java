package org.slavicmyths.finist;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.network.chat.Component;
import org.slavicmyths.military.GreyWolfStory;
public final class FinistBow extends BowItem {
 public FinistBow(Properties p){super(p);}
 public float power(int ticks){float f=Math.max(0,ticks)/30F;return Math.min(1,(f*f+2*f)/3);}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){var s=p.getItemInHand(hand);boolean ammo=!p.getProjectile(s).isEmpty()||FinistQuiver.charges(FinistQuiver.equipped(p))>0;var hook=net.neoforged.neoforge.event.EventHooks.onArrowNock(s,level,p,hand,ammo);if(hook!=null)return hook;if(!ammo&&!p.hasInfiniteMaterials())return InteractionResultHolder.fail(s);p.startUsingItem(hand);return InteractionResultHolder.consume(s);}
 @Override public void releaseUsing(ItemStack s,Level l,LivingEntity user,int left){if(!(user instanceof Player p))return;var quiver=FinistQuiver.equipped(p);boolean magic=FinistQuiver.charges(quiver)>0;var ammo=p.getProjectile(s);int held=net.neoforged.neoforge.event.EventHooks.onArrowLoose(s,l,p,getUseDuration(s,user)-left,magic||!ammo.isEmpty());if(held<0||power(held)<.1F||!magic&&ammo.isEmpty())return;float power=power(held);
  if(l instanceof ServerLevel w){if(magic){var arrow=new FinistArrow(user,w,s);arrow.setCritArrow(power==1);arrow.setBaseDamage(2.8);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;arrow.getPersistentData().putBoolean("FinistPair",true);arrow.getPersistentData().putBoolean("FinistManual",p.isShiftKeyDown());if(p instanceof ServerPlayer player&&!p.isShiftKeyDown()){var target=FinistMarks.target(player);if(target!=null&&player.hasLineOfSight(target))arrow.guide(target.getUUID());}arrow.shootFromRotation(p,p.getXRot(),p.getYRot(),0,power*3.6F,.7F);
   if(!w.addFreshEntity(arrow))return;if(!FinistQuiver.spend(quiver)){arrow.discard();return;}s.hurtAndBreak(1,p,LivingEntity.getSlotForHand(p.getUsedItemHand()));if(p instanceof ServerPlayer player)GreyPackRing.sound(player,"finist_magic_shot",.65F);
  }else{var shots=draw(s,ammo,p);if(shots.isEmpty())return;shoot(w,p,p.getUsedItemHand(),s,shots,power*3.6F,.7F,power==1,null);w.playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.ARROW_SHOOT,net.minecraft.sounds.SoundSource.PLAYERS,.8F,.85F);}p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));}
 }
 @Override protected void shootProjectile(LivingEntity shooter,Projectile projectile,int index,float velocity,float inaccuracy,float angle,LivingEntity target){if(projectile instanceof AbstractArrow arrow){arrow.setBaseDamage(arrow.getBaseDamage()*1.4);arrow.getPersistentData().putBoolean("FinistPair",!FinistQuiver.equipped(shooter).isEmpty());arrow.getPersistentData().putBoolean("FinistManual",shooter.isShiftKeyDown());}super.shootProjectile(shooter,projectile,index,velocity,inaccuracy,angle,target);}
 @Override public boolean isValidRepairItem(ItemStack s,ItemStack ingredient){return ingredient.is(Items.PHANTOM_MEMBRANE)||super.isValidRepairItem(s,ingredient);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){for(String key:List.of("bow.damage","bow.compatibility","bow.pair","bow.lock","bow.manual"))lines.add(GreyWolfStory.text(key).withStyle(net.minecraft.ChatFormatting.GRAY));}
}
