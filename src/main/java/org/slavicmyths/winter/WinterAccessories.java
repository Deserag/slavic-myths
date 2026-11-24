package org.slavicmyths.winter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.FolkAccessoryItem;
import org.slavicmyths.morozko.FrostLedger;
import org.slavicmyths.combat.CombatTargeting;

@EventBusSubscriber(modid="slavicmyths")
public final class WinterAccessories {
 private static final String COLD="WinterFrostbite";
 private static final TagKey<EntityType<?>> FIRE=TagKey.create(Registries.ENTITY_TYPE,id("trait_fire_affinity"));
 private static final TagKey<Item> WINTER=TagKey.create(Registries.ITEM,id("winter_weapons"));
 private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("slavicmyths",s);}
 private static boolean wears(LivingEntity p,Item item){return !FolkAccessoryItem.equipped(p,item).isEmpty();}
 private static void mod(LivingEntity p,net.minecraft.core.Holder<Attribute> a,String key,double amount,AttributeModifier.Operation op){
  var instance=p.getAttribute(a);if(instance==null)return;var name=id(key);var old=instance.getModifier(name);
  if(old!=null&&old.amount()==amount&&old.operation()==op)return;
  instance.removeModifier(name);if(amount!=0)instance.addTransientModifier(new AttributeModifier(name,amount,op));
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;
  boolean live=p.isAlive()&&!p.isSpectator(),fang=live&&wears(p,ModItems.NORTHERN_WOLVERINE_FANG.get()),belt=live&&wears(p,ModItems.ICE_CAPTAIN_BELT.get()),heart=live&&wears(p,ModItems.SNOW_QUEEN_HEART.get());
  var add=AttributeModifier.Operation.ADD_VALUE;
  mod(p,Attributes.ATTACK_SPEED,"winter_fang_speed",fang?.15:0,AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
  mod(p,Attributes.ARMOR,"winter_belt_armor",belt?3:0,add);
  mod(p,Attributes.KNOCKBACK_RESISTANCE,"winter_belt_knockback",belt?.1:0,add);
  mod(p,Attributes.MAX_HEALTH,"winter_accessory_health",(belt?4:0)+(heart?6:0),add);
  if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
 }
 public static boolean winterItem(ItemStack stack){return stack.is(WINTER);}
 private static boolean winterHit(net.minecraft.world.damagesource.DamageSource source,ServerPlayer p){
  if(source.getDirectEntity() instanceof org.slavicmyths.combat.WeaponProjectile projectile)return projectile.getItem().is(WINTER);
  if(source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow)return arrow.getPersistentData().getBoolean(WinterRanged.SHOT);
  if(source.getDirectEntity() instanceof org.slavicmyths.morozko.FrostShard shard)return shard.winterWeapon();
  if(source.getDirectEntity() instanceof org.slavicmyths.morozko.FrostHazard hazard)return hazard.winterWeapon();
  if(source.getDirectEntity() instanceof org.slavicmyths.icequeen.QueenMinion minion)return minion.playerBound();
  return p.getMainHandItem().is(WINTER);
 }
 private static boolean weaponHit(net.minecraft.world.damagesource.DamageSource source,ServerPlayer p){
  return source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile || source.getDirectEntity() instanceof org.slavicmyths.morozko.FrostHazard h&&h.winterWeapon() || source.getDirectEntity() instanceof org.slavicmyths.icequeen.QueenMinion m&&m.playerBound() || source.getDirectEntity()==p&&!source.getMsgId().equals("winter_frostbite");
 }
 @SubscribeEvent public static void damage(LivingDamageEvent.Pre e){
  if(e.getEntity().level().isClientSide||e.getNewDamage()<=0)return;
  float n=e.getNewDamage();
  if(e.getEntity() instanceof ServerPlayer p){
   if(e.getSource().is(DamageTypes.FALL)&&wears(p,ModItems.WINTER_OWL_FEATHER.get()))n*=.3f;
   if((e.getSource().is(DamageTypes.MAGIC)||e.getSource().is(DamageTypes.INDIRECT_MAGIC))&&wears(p,ModItems.SNOW_QUEEN_HEART.get()))n*=.8f;
  }
  if(e.getSource().getEntity() instanceof ServerPlayer p&&weaponHit(e.getSource(),p)){
   if(e.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile&&wears(p,ModItems.WINTER_OWL_FEATHER.get()))n*=1.15f;
   if(p.getHealth()<p.getMaxHealth()*.3f&&wears(p,ModItems.NORTHERN_WOLVERINE_FANG.get()))n*=1.2f;
   if(stacks(e.getEntity())>=5&&wears(p,ModItems.SILENCE_RING.get()))n*=1.25f;
   if(winterHit(e.getSource(),p)&&e.getEntity().getType().is(FIRE))n*=.8f;
  }
  if(n!=e.getNewDamage())e.setNewDamage(n);
 }
 @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
  if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||!weaponHit(e.getSource(),p))return;
  boolean ring=wears(p,ModItems.FROST_PERMAFROST_RING.get()),winter=winterHit(e.getSource(),p);
  if(ring||winter)apply(p,e.getEntity(),ring&&winter?160:120);
 }
 public static int stacks(LivingEntity t){
  var n=t.getPersistentData().getCompound(COLD);return t.level().getGameTime()<n.getLong("Until")?n.getInt("Stacks"):0;
 }
 public static void apply(ServerPlayer p,LivingEntity t,int duration){
  if(SnowArmor.full(t)||!CombatTargeting.canAttack(p,t)||t instanceof org.slavicmyths.morozko.PlayerDouble)return;
  long now=t.level().getGameTime();var n=t.getPersistentData().getCompound(COLD);int count=stacks(t);
  if(count==0||now>=n.getLong("AddAt")){count=Math.min(5,count+1);n.putLong("AddAt",now+10);}
  n.putInt("Stacks",count);n.putLong("Until",now+duration);n.putUUID("Owner",p.getUUID());
  if(!n.contains("Next")||n.getLong("Next")<now)n.putLong("Next",now+20);
  t.getPersistentData().put(COLD,n);
 }
 @SubscribeEvent public static void coldTick(EntityTickEvent.Post e){
  if(!(e.getEntity() instanceof LivingEntity t)||!(t.level() instanceof ServerLevel w)||!t.getPersistentData().contains(COLD))return;
  int count=SnowArmor.full(t)?0:stacks(t);var n=t.getPersistentData().getCompound(COLD);
  double slow=CombatTargeting.controlResistant(t)?Math.min(.2,count*.1):count*.1;
  mod(t,Attributes.MOVEMENT_SPEED,"winter_frostbite_slow",-slow,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
  if(count==0){t.getPersistentData().remove(COLD);return;}
  long now=w.getGameTime();if(count>=3&&now>=n.getLong("Next")){
   n.putLong("Next",now+20);var p=n.hasUUID("Owner")?w.getServer().getPlayerList().getPlayer(n.getUUID("Owner")):null;
   if(p!=null&&p.level()==w&&CombatTargeting.canAttack(p,t))t.hurt(org.slavicmyths.combat.MythDamageSources.periodic("winter_frostbite",p),(count-2)*(t.getType().is(FIRE)?.8f:1f));
  }
 }
 public static void queenVictory(ServerPlayer p){
  var ledger=FrostLedger.get(p.serverLevel());var data=ledger.player(p.getUUID());boolean changed=false;
  if(!data.getBoolean("WinterQueenAccessories")){
   data.putBoolean("WinterQueenAccessories",true);ledger.enqueue(p,new ItemStack(ModItems.SNOW_QUEEN_HEART.get()));ledger.enqueue(p,new ItemStack(ModItems.ICE_CROWN.get()));changed=true;
  }
  if(!data.getBoolean("WinterQueenWeapon")){
   data.putBoolean("WinterQueenWeapon",true);ledger.enqueue(p,new ItemStack(ModItems.ICE_QUEEN_SPEAR_STAFF.get()));changed=true;
  }
  if(changed){ledger.setDirty();ledger.collect(p);}
 }
 private WinterAccessories(){}
}
