package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.effect.*;
import net.minecraft.sounds.*;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.combat.*;
import org.slavicmyths.savanna.SteppeWeapons.Kind;
import org.slavicmyths.combat.WeaponInputHandler.Action;
@EventBusSubscriber(modid="slavicmyths")
public final class SteppeWeaponSkills {
 private static final String[] POISONS={"Яд","Иссушение","Замедление","Слабость","Ослепление","Окаменение","Чёрная кровь"};
 private static final String POISON="SteppeProjectilePoison",CURSE="SteppeProjectileCurse";
 private static final Map<ServerPlayer,List<Task>> TASKS=new WeakHashMap<>();
 private record Task(long due,ServerLevel world,ItemStack held,java.util.function.Consumer<ServerPlayer> run){}
 static String poisonName(int n){return POISONS[Math.floorMod(n,7)];}
 private static boolean legal(ServerPlayer p,LivingEntity x,boolean area){return x!=null&&x!=p&&x.isAlive()&&x.level()==p.level()&&CombatTargeting.canAttack(p,x,area?CombatTargeting.Context.AREA:CombatTargeting.Context.DIRECT)&&(!area||CombatTargeting.isEnemy(p,x,CombatTargeting.Context.ACQUIRE)||x==p.getLastHurtMob()||x==p.getLastHurtByMob())&&p.hasLineOfSight(x);}
 private static String key(Kind k,Action a){return "SteppeCD_"+k.id+"_"+a.id();}
 private static boolean ready(ServerPlayer p,Kind k,Action a){return p.getPersistentData().getLong(key(k,a))<=p.serverLevel().getGameTime();}
 private static void commit(ServerPlayer p,ItemStack s,Kind k,Action a,int cd,int wear){p.getPersistentData().putLong(key(k,a),p.serverLevel().getGameTime()+cd);if(wear>0&&!p.hasInfiniteMaterials())s.hurtAndBreak(wear,p,EquipmentSlot.MAINHAND);p.swing(InteractionHand.MAIN_HAND,true);}
 private static void schedule(ServerPlayer p,ItemStack s,int delay,java.util.function.Consumer<ServerPlayer> r){var q=TASKS.computeIfAbsent(p,x->new ArrayList<>());if(q.size()<64)q.add(new Task(p.serverLevel().getGameTime()+delay,p.serverLevel(),s,r));}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var q=TASKS.get(p);if(q==null)return;if(!p.isAlive()){TASKS.remove(p);return;}var due=new ArrayList<Task>();q.removeIf(t->{if(t.world()!=p.serverLevel()||t.held()!=null&&(t.held().isEmpty()||p.getMainHandItem()!=t.held()))return true;if(t.due()<=p.serverLevel().getGameTime()){due.add(t);return true;}return false;});for(var t:due)t.run().accept(p);if(q.isEmpty())TASKS.remove(p);}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)TASKS.remove(p);}
 private static Vec3 end(ServerPlayer p,double range){Vec3 from=p.getEyePosition(),to=from.add(p.getLookAngle().scale(range));var b=p.serverLevel().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));return b.getType()==HitResult.Type.MISS?to:b.getLocation();}
 private static LivingEntity aim(ServerPlayer p,double range){Vec3 from=p.getEyePosition(),to=end(p,range);return p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(from,to).inflate(1),x->legal(p,x,false)&&x.getBoundingBox().inflate(.3).clip(from,to).isPresent()).stream().min(Comparator.comparingDouble(x->x.distanceToSqr(p))).orElse(null);}
 private static void line(ServerPlayer p,Vec3 start,Vec3 end,boolean cursed){int n=Math.min(90,Math.max(1,(int)(start.distanceTo(end)*4)));for(int i=0;i<=n;i++){var v=start.lerp(end,(double)i/n);p.serverLevel().sendParticles(cursed?ParticleTypes.WITCH:ParticleTypes.CRIT,v.x,v.y,v.z,1,.02,.02,.02,0);}}
 private static void ring(ServerPlayer p,Vec3 at,double r,boolean cursed){for(int i=0;i<36;i++){double a=i*Math.PI/18;p.serverLevel().sendParticles(cursed?ParticleTypes.WITCH:ParticleTypes.CRIT,at.x+Math.cos(a)*r,at.y+.2,at.z+Math.sin(a)*r,1,0,0,0,0);}}
 private static void poison(ServerPlayer p,LivingEntity x,int mode){if(!legal(p,x,false)||!CombatTargeting.canControl(p,x)||CombatTargeting.controlResistant(x))return;switch(mode){case 0->x.addEffect(new MobEffectInstance(MobEffects.POISON,100,0));case 1->x.addEffect(new MobEffectInstance(MobEffects.WITHER,80,0));case 2->x.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1));case 3->x.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,0));case 4->x.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,40,0));case 5,6->SkarapeyaPoison.apply(x,mode);}}
 private static void curse(ServerPlayer p,LivingEntity x,boolean auto){if(!legal(p,x,false))return;int n=DivCurse.add(x,p.getUUID());p.serverLevel().sendParticles(ParticleTypes.ENCHANT,x.getX(),x.getY()+x.getBbHeight()+.4,x.getZ(),n*4,.3,.2,.3,.05);if(auto&&n==4)doom(p,x);}
 private static void doom(ServerPlayer p,LivingEntity x){if(!DivCurse.owned(x,p.getUUID())||DivCurse.active(x).length<4)return;DivCurse.clear(x);Vec3 at=x.position();ring(p,at,2.5,true);schedule(p,null,20,owner->{ring(owner,at,2.5,true);line(owner,at.add(0,5,0),at,true);for(var t:owner.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(2.5),v->legal(owner,v,true)||v==x&&legal(owner,v,false)))if(t.position().distanceToSqr(at)<=6.25)t.hurt(MythDamageSources.caused("steppe_weapon_skill",owner),20);});}
 private static void hit(ServerPlayer p,LivingEntity x,float damage,int venom,boolean mark,boolean auto){if(!legal(p,x,false))return;if(x.hurt(MythDamageSources.caused("steppe_weapon_skill",p),damage)){if(venom>=0)poison(p,x,venom);if(mark)curse(p,x,auto);}}
 private static void area(ServerPlayer p,Vec3 at,double r,double angle,float d,int venom,boolean mark){Vec3 f=p.getLookAngle().multiply(1,0,1).normalize();for(var x:p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(r),t->legal(p,t,true)&&t.distanceToSqr(at)<=r*r)){Vec3 delta=x.position().subtract(at).multiply(1,0,1).normalize();if(angle>=180||f.dot(delta)>=Math.cos(Math.toRadians(angle)))hit(p,x,d,venom,mark,true);}ring(p,at,r,venom>=0||mark);}
 private static void wave(ServerPlayer p,double range,float d,int venom,boolean mark,boolean auto,int pierce){Vec3 from=p.getEyePosition(),to=end(p,range);line(p,from,to,venom>=0||mark);p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(from,to).inflate(1),x->legal(p,x,false)&&x.getBoundingBox().inflate(.35).clip(from,to).isPresent()).stream().sorted(Comparator.comparingDouble(x->x.distanceToSqr(p))).limit(pierce).forEach(x->hit(p,x,d,venom,mark,auto));}
 private static boolean dash(ServerPlayer p,LivingEntity target,double distance){if(target==null)return false;Vec3 delta=target.position().subtract(p.position()).multiply(1,0,1);if(delta.length()<2)return true;Vec3 step=delta.normalize().scale(Math.min(distance,delta.length()-1.6));for(double f=1;f>.1;f-=.1){Vec3 v=step.scale(f);if(p.serverLevel().noCollision(p,p.getBoundingBox().move(v))&&p.serverLevel().clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(v),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS){p.teleportTo(p.getX()+v.x,p.getY(),p.getZ()+v.z);return true;}}return false;}
 private static boolean returning(ServerPlayer p,ItemStack s,float dmg,float yaw){var w=new WeaponProjectile(p.serverLevel(),p,s.copyWithCount(1),true,true);w.configurePairThrow(dmg);w.shootFromRotation(p,p.getXRot(),p.getYRot()+yaw,0,2.3F,0);return p.serverLevel().addFreshEntity(w);}
 private static ItemStack ammo(ServerPlayer p){if(p.getOffhandItem().is(Items.ARROW))return p.getOffhandItem();for(var s:p.getInventory().items)if(s.is(Items.ARROW))return s;return ItemStack.EMPTY;}
 private static boolean shot(ServerPlayer p,ItemStack weapon,float damage,float yaw,int venom,boolean mark,int pierce,boolean free){
  ItemStack ammo=ammo(p);if(!free&&!p.hasInfiniteMaterials()&&ammo.isEmpty())return false;
  var arrow=new Arrow(p.serverLevel(),p,new ItemStack(Items.ARROW),weapon.copyWithCount(1));arrow.setBaseDamage(damage/2.5F);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;arrow.setPierceLevel((byte)pierce);
  arrow.getPersistentData().putBoolean("SteppeProjectile",true);arrow.getPersistentData().putInt(POISON,venom);arrow.getPersistentData().putBoolean(CURSE,mark);arrow.shootFromRotation(p,p.getXRot(),p.getYRot()+yaw,0,2.5F,0);
  if(!p.serverLevel().addFreshEntity(arrow))return false;if(!free&&!p.hasInfiniteMaterials())ammo.shrink(1);return true;
 }
 @SubscribeEvent public static void projectileDamage(LivingIncomingDamageEvent e){if(e.getSource().getDirectEntity() instanceof Arrow a&&a.getPersistentData().getBoolean("SteppeProjectile")&&(!(a.getOwner() instanceof ServerPlayer p)||!CombatTargeting.canAttack(p,e.getEntity(),CombatTargeting.Context.PROJECTILE)))e.setCanceled(true);}
 @SubscribeEvent public static void projectileHit(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p))return;if(e.getSource().getDirectEntity() instanceof Arrow a&&a.getPersistentData().getBoolean("SteppeProjectile")){int v=a.getPersistentData().getInt(POISON);if(v>=0)poison(p,e.getEntity(),v);if(a.getPersistentData().getBoolean(CURSE))curse(p,e.getEntity(),true);}else if(e.getSource().getDirectEntity() instanceof WeaponProjectile a&&SteppeWeapons.kind(a.getItem())==Kind.SKARAPEYA_BLADE)poison(p,e.getEntity(),SteppeWeapons.poison(a.getItem()));}
 static void meleePassive(ServerPlayer p,LivingEntity t,ItemStack s,Kind k){if(!legal(p,t,false))return;if(k==Kind.DIV_CLAWS)curse(p,t,true);if(k==Kind.SKARAPEYA_BLADE||k==Kind.SKARAPEYA_WHIP)poison(p,t,SteppeWeapons.poison(s));if(k==Kind.HYENA_KNUCKLES)bleed(p,t);}
 private static void bleed(ServerPlayer p,LivingEntity t){if(t.getPersistentData().getLong("SteppeBleedUntil")>p.serverLevel().getGameTime())return;t.getPersistentData().putLong("SteppeBleedUntil",p.serverLevel().getGameTime()+80);for(int i=1;i<=4;i++)schedule(p,null,i*20,x->{if(legal(x,t,false)&&t.distanceToSqr(x)<=256)t.hurt(MythDamageSources.periodic("steppe_weapon_skill",x),2);});}
 static void use(ServerPlayer p,ItemStack s,Kind k,Action a){
  if(!p.isAlive()||p.isSpectator()||p.getMainHandItem()!=s||SteppeWeapons.kind(s)!=k||((a==Action.SHIFT_LEFT_CLICK||a==Action.SHIFT_RIGHT_CLICK)!=p.isShiftKeyDown())||!ready(p,k,a))return;
  if((k.pair()||k==Kind.SKARAPEYA_BLADE)&&!p.getOffhandItem().isEmpty())return;
  int venom=SteppeWeapons.poison(s);
  if(a==Action.SHIFT_RIGHT_CLICK&&(k==Kind.SKARAPEYA_GUSLI||k==Kind.SKARAPEYA_WHIP||k==Kind.SKARAPEYA_NEEDLE)){SteppeWeapons.poison(s,venom+1);p.displayClientMessage(Component.literal(poisonName(venom+1)),true);commit(p,s,k,a,8,0);return;}
  if(k==Kind.SKARAPEYA_BLADE&&a==Action.SHIFT_RIGHT_CLICK){if(!WeaponProjectile.pairedInFlight(p).isEmpty())return;SteppeWeapons.mode(s,!SteppeWeapons.split(s));commit(p,s,k,a,10,0);return;}
  if(k==Kind.SKARAPEYA_NEEDLE){int n=a==Action.SHIFT_LEFT_CLICK?3:1,done=0;for(int i=0;i<n;i++){if(!p.hasInfiniteMaterials()&&s.getMaxDamage()-s.getDamageValue()<=done)break;if(shot(p,s,12,(i-(n-1)*.5F)*3,venom,false,0,true))done++;}if(done>0){commit(p,s,k,a,n==1?8:60,done);p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,.6F,1.5F);}return;}
  if(k.bow()){boolean fan=a==Action.SHIFT_LEFT_CLICK;int n=fan?3:1,done=0;for(int i=0;i<n;i++)if(shot(p,s,fan?13:24,(i-(n-1)*.5F)*8,-1,k==Kind.DIV_BOW,fan?0:3,false))done++;if(done>0)commit(p,s,k,a,fan?100:160,1);return;}
  if(k==Kind.POLKAN_SPEAR||k==Kind.STEPPE_SPEAR){
   if(a==Action.SHIFT_LEFT_CLICK){area(p,p.position(),5,100,25,-1,false);commit(p,s,k,a,100,1);return;}
   if(!WeaponProjectile.pairedInFlight(p).isEmpty()){WeaponProjectile.pairedInFlight(p).forEach(WeaponProjectile::recallPair);return;}
   if(returning(p,s,k==Kind.POLKAN_SPEAR?30:16,0)){commit(p,s,k,a,a==Action.SHIFT_RIGHT_CLICK?800:80,1);if(a==Action.SHIFT_RIGHT_CLICK)for(int i=1;i<3;i++)schedule(p,s,i*14,x->returning(x,s,30,0));}return;}
  if(k==Kind.SKARAPEYA_GUSLI){if(a==Action.LEFT_CLICK){wave(p,16,12,venom,false,true,2);commit(p,s,k,a,12,1);}else if(a==Action.RIGHT_CLICK){commit(p,s,k,a,220,3);for(int i=0;i<5;i++)schedule(p,s,i*12+1,x->wave(x,20,13,venom,false,true,4));}else{var target=aim(p,24);Vec3 at=target==null?p.position().add(p.getLookAngle().scale(8)):target.position();ring(p,at,4,true);commit(p,s,k,a,260,3);for(int i=0;i<5;i++)schedule(p,s,20+i*20,x->area(x,at,4,180,8,venom,false));}return;}
  if(k==Kind.DIV_STAFF){if(a==Action.SHIFT_RIGHT_CLICK){var t=aim(p,24);if(t!=null&&DivCurse.owned(t,p.getUUID())&&DivCurse.active(t).length==4){doom(p,t);commit(p,s,k,a,120,1);}}else if(a==Action.SHIFT_LEFT_CLICK){area(p,p.position(),7,65,13,-1,true);commit(p,s,k,a,120,1);}else{wave(p,24,14,-1,true,false,1);commit(p,s,k,a,20,1);}return;}
  if(k==Kind.SKARAPEYA_BLADE){if(SteppeWeapons.split(s)){if(a==Action.RIGHT_CLICK){if(!WeaponProjectile.pairedInFlight(p).isEmpty()){WeaponProjectile.pairedInFlight(p).forEach(WeaponProjectile::recallPair);return;}if(returning(p,s,23,-5)){returning(p,s,23,5);commit(p,s,k,a,120,2);}}else{commit(p,s,k,a,150,2);for(int i=0;i<3;i++)schedule(p,s,i*6+1,x->{var t=aim(x,5);if(t!=null)hit(x,t,19,venom,false,true);});}}else if(a==Action.RIGHT_CLICK){wave(p,18,30,venom,false,true,5);commit(p,s,k,a,120,1);}else{area(p,p.position(),5.5,180,25,venom,false);commit(p,s,k,a,160,1);}return;}
  if(k==Kind.SKARAPEYA_WHIP){if(a==Action.RIGHT_CLICK)wave(p,12,22,venom,false,true,3);else area(p,p.position(),5,180,18,venom,false);commit(p,s,k,a,100,1);return;}
  if(k==Kind.DIV_CLAWS||k==Kind.JACKAL_DAGGERS||k==Kind.HYENA_KNUCKLES){LivingEntity t=aim(p,7);if(t==null)return;
   if(a==Action.SHIFT_LEFT_CLICK){if(t.distanceToSqr(p)>25)return;commit(p,s,k,a,180,2);int n=k==Kind.DIV_CLAWS?4:3;for(int i=0;i<n;i++)schedule(p,s,1+i*10,x->{if(legal(x,t,false)&&t.distanceToSqr(x)<=25){hit(x,t,k==Kind.DIV_CLAWS?12:9,-1,k==Kind.DIV_CLAWS,true);if(k==Kind.HYENA_KNUCKLES)bleed(x,t);}});}
   else if(a==Action.SHIFT_RIGHT_CLICK){if(!p.onGround())return;commit(p,s,k,a,220,2);p.setDeltaMovement(p.getDeltaMovement().add(0,.65,0));p.hurtMarked=true;schedule(p,s,14,x->{if(dash(x,t,6)){area(x,t.position(),3,180,24,-1,true);x.fallDistance=0;}});}
   else if(dash(p,t,5)){hit(p,t,k==Kind.HYENA_KNUCKLES&&t.getHealth()<t.getMaxHealth()*.5?24:18,-1,k==Kind.DIV_CLAWS,true);if(k==Kind.HYENA_KNUCKLES)bleed(p,t);commit(p,s,k,a,100,1);}return;}
  if(k==Kind.POLKAN_AXE&&a==Action.RIGHT_CLICK){var t=aim(p,7);if(t==null||!dash(p,t,5))return;hit(p,t,32,-1,false,true);commit(p,s,k,a,140,1);return;}
  if(k==Kind.STEPPE_AXE){var t=aim(p,4.5);if(t==null)return;hit(p,t,22,-1,false,true);}else if(k==Kind.STEPPE_HALBERD&&a==Action.SHIFT_RIGHT_CLICK)wave(p,7,18,-1,false,true,2);else area(p,p.position(),k==Kind.STEPPE_SWORD?4:4.5,k==Kind.STEPPE_SWORD?65:180,k==Kind.POLKAN_AXE?25:k.damage+2,-1,false);commit(p,s,k,a,100,1);
 }
 private SteppeWeaponSkills(){}
}
