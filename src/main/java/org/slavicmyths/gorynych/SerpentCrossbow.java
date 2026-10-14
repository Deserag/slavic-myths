package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
public final class SerpentCrossbow extends CrossbowItem {
 public final boolean special;
 public SerpentCrossbow(boolean special){super(new Item.Properties().durability(special?680:520).rarity(special?Rarity.EPIC:Rarity.RARE));this.special=special;}
 public static CompoundTag state(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("Gorynych196");}
 static void write(ItemStack s,CompoundTag state){var n=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();n.put("Gorynych196",state);s.set(DataComponents.CUSTOM_DATA,CustomData.of(n));}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return super.getUseDuration(s,e)+10;}
 @Override public void onUseTick(Level w,LivingEntity e,ItemStack s,int left){super.onUseTick(w,e,s,left+10);}
 @Override public void releaseUsing(ItemStack s,Level w,LivingEntity e,int left){super.releaseUsing(s,w,e,left+10);}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(p instanceof net.minecraft.server.level.ServerPlayer serverPlayer&&CrossbowVolley.active(serverPlayer))return InteractionResultHolder.fail(s);if(special&&p.isShiftKeyDown()){if(!w.isClientSide){var n=state(s);int mode=(Math.floorMod(n.getInt("Mode"),3)+1)%3;n.putInt("Mode",mode);write(s,n);p.displayClientMessage(GorynychContent.text("mode_"+mode,0),true);GorynychContent.sound(w,p.blockPosition(),"crossbow_charge");}return InteractionResultHolder.sidedSuccess(s,w.isClientSide);}return super.use(w,p,hand);}
 @Override public void performShooting(Level w,LivingEntity e,InteractionHand hand,ItemStack s,float speed,float spread,LivingEntity target){
  if(!special){super.performShooting(w,e,hand,s,speed,spread,target);return;}
  if(!(w instanceof net.minecraft.server.level.ServerLevel server)||s.isEmpty()||!isCharged(s)||e instanceof net.minecraft.server.level.ServerPlayer p&&CrossbowVolley.active(p))return;
  if(e instanceof Player p&&net.neoforged.neoforge.event.EventHooks.onArrowLoose(s,w,p,1,true)<0)return;
  var ammo=s.set(DataComponents.CHARGED_PROJECTILES,net.minecraft.world.item.component.ChargedProjectiles.EMPTY).getItems().getFirst();
  int mode=Math.floorMod(state(s).getInt("Mode"),3);
  boolean burst=state(s).getBoolean("Burst")&&e instanceof net.minecraft.server.level.ServerPlayer;
  for(int i=0;i<3;i++){
   var round=ammo.copyWithCount(1);
   if(i>0)round.set(DataComponents.INTANGIBLE_PROJECTILE,net.minecraft.util.Unit.INSTANCE);
   if(burst&&i>0)CrossbowVolley.enqueue((net.minecraft.server.level.ServerPlayer)e,s,hand,round,mode,speed,i*4,i);
   else fireRound(server,e,hand,s,round,mode,speed,burst?0:i==1?-10:i==2?10:0,target,i);
   if(s.isEmpty())break;
  }
  if(e instanceof net.minecraft.server.level.ServerPlayer p){net.minecraft.advancements.CriteriaTriggers.SHOT_CROSSBOW.trigger(p,s);p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));}
 }
 void fireRound(net.minecraft.server.level.ServerLevel w,LivingEntity owner,InteractionHand hand,ItemStack weapon,ItemStack ammo,int mode,float speed,float angle,LivingEntity target,int lane){
  var shot=createProjectile(w,owner,weapon,ammo,owner instanceof Player);
  var eye=owner.getEyePosition();var forward=owner.getLookAngle();var right=forward.cross(owner.getUpVector(1)).normalize();
  double handed=((hand==InteractionHand.MAIN_HAND)==(owner.getMainArm()==HumanoidArm.LEFT))?-1:1;
  var muzzle=eye.add(forward.scale(.45)).add(right.scale(handed*.20+(lane==1?.24:lane==2?-.24:0))).add(owner.getUpVector(1).scale(-.25));
  var obstruction=w.clip(new net.minecraft.world.level.ClipContext(eye,muzzle,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,owner));
  if(obstruction.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)muzzle=eye;
  shot.setPos(muzzle.x,muzzle.y,muzzle.z);
  super.shootProjectile(owner,shot,0,speed,0,angle,target);
  if(owner instanceof Player){
   var end=eye.add(forward.scale(32));var block=w.clip(new net.minecraft.world.level.ClipContext(eye,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,owner));
   if(block.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)end=block.getLocation();
   var hit=ProjectileUtil.getEntityHitResult(w,owner,eye,end,new net.minecraft.world.phys.AABB(eye,end).inflate(1),e->e instanceof LivingEntity l&&l.isAlive()&&org.slavicmyths.combat.CombatTargeting.canAttack(owner,l),.05f);
   if(hit!=null)end=hit.getEntity().getBoundingBox().inflate(.05).clip(eye,end).orElse(end);
   var up=owner.getUpVector(1);var direction=end.subtract(muzzle).normalize().toVector3f().rotate(new org.joml.Quaternionf().setAngleAxis(Math.toRadians(angle),up.x,up.y,up.z));
   shot.shoot(direction.x,direction.y,direction.z,speed,0);
  }
  shot.getPersistentData().putInt("GorynychMuzzleLane",lane);
  if(shot instanceof AbstractArrow arrow){arrow.setBaseDamage(arrow.getBaseDamage()*1.18);if(ammo.has(DataComponents.INTANGIBLE_PROJECTILE)||owner instanceof Player p&&p.hasInfiniteMaterials())arrow.pickup=AbstractArrow.Pickup.CREATIVE_ONLY;}
  shot.getPersistentData().putInt("GorynychSpecial",mode+1);
  w.addFreshEntity(shot);weapon.hurtAndBreak(getDurabilityUse(ammo),owner,LivingEntity.getSlotForHand(hand));
  GorynychContent.sound(w,owner.blockPosition(),mode==0?"crossbow_special_mor":mode==1?"crossbow_special_fire":"crossbow_special_storm");
 }
 @Override protected void shootProjectile(LivingEntity owner,Projectile shot,int index,float velocity,float inaccuracy,float angle,LivingEntity target){super.shootProjectile(owner,shot,index,velocity,inaccuracy,angle,target);if(!owner.level().isClientSide&&shot instanceof AbstractArrow arrow)arrow.setBaseDamage(arrow.getBaseDamage()*1.18);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){super.appendHoverText(s,c,lines,flag);lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".desc"));if(special){var n=state(s);lines.add(GorynychContent.text("mode_"+Math.floorMod(n.getInt("Mode"),3),0));lines.add(GorynychContent.text(n.getBoolean("Burst")?"volley_burst":"volley_fan"));}}
}
