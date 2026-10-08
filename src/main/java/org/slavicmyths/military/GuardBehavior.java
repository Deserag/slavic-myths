package org.slavicmyths.military;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.*;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.village.VillageRoles;
/** A bounded CORE behavior alongside native schedule/POI/sleep/raid packages. */
public final class GuardBehavior extends Behavior<Villager> {
 private LivingEntity enemy;private long search,attack,path,patrol;private BlockPos anchor;private WalkTarget ownWalk;
 public GuardBehavior(){super(Map.of(),Integer.MAX_VALUE);}
 public static Item weapon(int kind){return switch(kind){case 0->ModItems.SPEAR.get();case 1->Items.IRON_SWORD;case 2->ModItems.BATTLE_AXE.get();case 3->Items.BOW;default->ModItems.RESTORED_SPEAR.get();};}
 public static int archetype(UUID id){int roll=Math.floorMod(id.hashCode(),100);int total=0;for(int i=0;i<Military.ARCHETYPE_WEIGHTS.length;i++){total+=Military.ARCHETYPE_WEIGHTS[i];if(roll<total)return i;}return 4;}
 public static boolean shield(int kind){return kind>=0&&kind<3;}
 public static boolean hostile(Villager guard,LivingEntity e){if(e==guard||!e.isAlive()||e instanceof Villager||e instanceof net.minecraft.world.entity.animal.IronGolem||e instanceof WanderingTrader||e instanceof net.minecraft.world.entity.TamableAnimal||e instanceof net.minecraft.world.entity.monster.Creeper)return false;
  if(e instanceof Player p)return !p.isCreative()&&!p.isSpectator()&&(guard.getLastHurtByMob()==p||guard.getPlayerReputation(p)<=-100||guard.getPersistentData().getLong("GuardAggressorUntil")>guard.level().getGameTime()&&guard.getPersistentData().hasUUID("GuardAggressor")&&guard.getPersistentData().getUUID("GuardAggressor").equals(p.getUUID()));return e.getType().is(Military.HOSTILES)||e.getType().is(Military.BANDITS)||guard.getPersistentData().getLong("GuardAggressorUntil")>guard.level().getGameTime()&&guard.getPersistentData().hasUUID("GuardAggressor")&&guard.getPersistentData().getUUID("GuardAggressor").equals(e.getUUID());}
 protected boolean checkExtraStartConditions(ServerLevel l,Villager v){return Military.guard(v);}
 protected boolean canStillUse(ServerLevel l,Villager v,long t){return Military.guard(v);}
 protected void start(ServerLevel l,Villager v,long t){v.getPersistentData().remove("GuardAllowDemotion");if(v.getData(Military.ARCHETYPE)<0)v.setData(Military.ARCHETYPE,archetype(v.getUUID()));var hp=v.getAttribute(Attributes.MAX_HEALTH);var id=VillageRoles.id("guard_health");if(hp!=null&&!hp.hasModifier(id)){hp.addPermanentModifier(new AttributeModifier(id,10,AttributeModifier.Operation.ADD_VALUE));v.setHealth(Math.min(v.getMaxHealth(),v.getHealth()+10));}var armor=v.getAttribute(Attributes.ARMOR);if(armor!=null&&!armor.hasModifier(VillageRoles.id("guard_armor")))armor.addPermanentModifier(new AttributeModifier(VillageRoles.id("guard_armor"),4,AttributeModifier.Operation.ADD_VALUE));}
 protected void tick(ServerLevel l,Villager v,long now){
  if(v.isTrading()||v.isBaby())return;var data=v.getPersistentData();var job=v.getBrain().getMemory(MemoryModuleType.JOB_SITE);if(job.isPresent()&&job.get().dimension().equals(l.dimension())){anchor=job.get().pos();data.putLong("GuardAnchor",anchor.asLong());}
  if(anchor==null&&data.contains("GuardAnchor"))anchor=BlockPos.of(data.getLong("GuardAnchor"));if(anchor==null)return;
  if(l.hasChunkAt(anchor)&&!l.getBlockState(anchor).is(Military.TABLE.get())){if(!data.contains("GuardLostSince"))data.putLong("GuardLostSince",now);if(now-data.getLong("GuardLostSince")>=Military.LOST_TABLE_GRACE){data.putBoolean("GuardAllowDemotion",true);v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.NONE));v.refreshBrain(l);cleanup(v);return;}}else if(l.hasChunkAt(anchor))data.remove("GuardLostSince");
  if(enemy!=null&&(!hostile(v,enemy)||enemy.blockPosition().distSqr(anchor)>Military.RESPONSE_RADIUS*Military.RESPONSE_RADIUS)){enemy=null;if(v.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null)==ownWalk)v.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);}
  if(now>=search){search=now+40;var hurt=v.getLastHurtByMob();if(hurt!=null&&hostile(v,hurt)&&hurt.blockPosition().distSqr(anchor)<=48*48)enemy=hurt;else if(enemy==null)enemy=l.getEntitiesOfClass(LivingEntity.class,new AABB(anchor).inflate(48),e->hostile(v,e)&&v.hasLineOfSight(e)).stream().min(Comparator.comparingDouble(v::distanceToSqr)).orElse(null);}
  if(enemy!=null){if(v.isSleeping())v.stopSleeping();v.getLookControl().setLookAt(enemy,30,30);data.putString("GuardReason","HOSTILE");data.putUUID("GuardTarget",enemy.getUUID());int kind=v.getData(Military.ARCHETYPE);double d=v.distanceToSqr(enemy),reach=kind==0?10.24:kind==4?16:6.25;
   boolean blocking=shield(kind)&&d<36&&now%100<25;if(blocking)data.putLong("GuardBlockUntil",now+2);int action=blocking?2:1;if(v.getData(Military.ACTION)!=action)v.setData(Military.ACTION,action);
   if(kind==3){if(d<36){if(now>=path){Vec3 away=v.position().subtract(enemy.position()).normalize();walk(v,v.blockPosition().offset((int)(away.x*5),0,(int)(away.z*5)),now);}}else if(d>225||!v.hasLineOfSight(enemy)){if(now>=path)walk(v,enemy.blockPosition(),now);}else if(now>=attack&&clearShot(l,v,enemy)){attack=now+50;Arrow arrow=new Arrow(l,v,new ItemStack(Items.ARROW),new ItemStack(Items.BOW));arrow.setBaseDamage(2);Vec3 aim=enemy.getEyePosition().subtract(arrow.position());arrow.shoot(aim.x,aim.y+Math.sqrt(d)*.06,aim.z,1.6F,5);l.addFreshEntity(arrow);v.swing(InteractionHand.MAIN_HAND);}}
   else if(d<=reach&&v.hasLineOfSight(enemy)){if(now>=attack){attack=now+(kind==2?40:kind==4?32:24);v.swing(InteractionHand.MAIN_HAND);enemy.hurt(l.damageSources().mobAttack(v),kind==2?7:kind==4?6:5);}}else if(now>=path)walk(v,enemy.blockPosition(),now);
  }else{if(v.getData(Military.ACTION)!=0)v.setData(Military.ACTION,0);data.remove("GuardTarget");data.remove("GuardBlockUntil");data.putString("GuardReason","PATROL");if(v.isSleeping()||!v.getBrain().isActive(Activity.WORK)&&!v.getBrain().isActive(Activity.IDLE))return;if(v.blockPosition().distSqr(anchor)>32*32){if(now>=path)walk(v,anchor,now);}else if(now>=patrol&&!v.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)){patrol=now+300;var p=net.minecraft.world.entity.ai.util.DefaultRandomPos.getPos(v,12,3);if(p!=null&&p.distanceToSqr(anchor.getCenter())<=32*32)walk(v,BlockPos.containing(p),now);}}
 }
 private static boolean clearShot(ServerLevel l,Villager v,LivingEntity target){Vec3 a=v.getEyePosition(),b=target.getEyePosition();for(LivingEntity e:l.getEntitiesOfClass(LivingEntity.class,new AABB(a,b).inflate(1),e->e!=v&&e!=target))if(e.getBoundingBox().inflate(.3).clip(a,b).isPresent())return false;return v.hasLineOfSight(target);}
 private void walk(Villager v,BlockPos p,long now){path=now+40;var route=v.getNavigation().createPath(p,1);if(route==null||!route.canReach()||route.getEndNode()==null)return;ownWalk=new WalkTarget(new BlockPosTracker(route.getEndNode().asBlockPos()),.65F,0);v.getBrain().setMemory(MemoryModuleType.WALK_TARGET,ownWalk);}
 public static void cleanup(Villager v){for(var a:List.of(Attributes.MAX_HEALTH,Attributes.ARMOR)){var inst=v.getAttribute(a);if(inst!=null){inst.removeModifier(VillageRoles.id("guard_health"));inst.removeModifier(VillageRoles.id("guard_armor"));}}v.setHealth(Math.min(v.getHealth(),v.getMaxHealth()));v.getPersistentData().remove("GuardBlockUntil");v.getPersistentData().remove("GuardTarget");}
 protected void stop(ServerLevel l,Villager v,long now){if(!Military.guard(v))cleanup(v);if(v.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null)==ownWalk)v.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);enemy=null;}
}
