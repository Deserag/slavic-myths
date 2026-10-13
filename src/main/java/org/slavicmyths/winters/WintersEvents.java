package org.slavicmyths.winters;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.tags.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
/** Event-driven combat; only one ordinary-inventory scan per 20 player ticks. */
@EventBusSubscriber(modid="slavicmyths")
public final class WintersEvents {
 public static final ResourceLocation KNOCK=ResourceLocation.fromNamespaceAndPath("slavicmyths","two_winters_knockback");
 private static final TagKey<EntityType<?>> NETHER_ENEMIES=TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","two_winters_nether_enemies"));
 private static final TagKey<EntityType<?>> BOSSES=TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","two_winters_bosses"));
 private static final ThreadLocal<Boolean> WAVE=ThreadLocal.withInitial(()->false);
 public static CompoundTag data(Player p){return org.slavicmyths.gorynych.GorynychLedger.part(org.slavicmyths.rpg.PathData.data(p),"TwoWinters197");}
 public static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
 public static boolean dark(Player p){return data(p).getBoolean("Dark");}
 public static boolean present(ServerPlayer p){return p.getPersistentData().getBoolean("TwoWintersPresent");}
 public static boolean inventory(Player p){for(var s:p.getInventory().items)if(s.is(WintersContent.AMULET.get()))return true;for(var s:p.getInventory().offhand)if(s.is(WintersContent.AMULET.get()))return true;return false;}
 public static boolean boss(LivingEntity t){return t.getType().is(BOSSES);}
 public static boolean enemy(LivingEntity t,ServerPlayer p){
  if(!t.isAlive()||t==p||t instanceof Player||t.isAlliedTo(p)||p.isAlliedTo(t)||t.isInvulnerable())return false;
  // Any owned companion is ineligible, including other players' pets.
  if(org.slavicmyths.karachun.NightOwners.owner(t)!=null||t instanceof net.minecraft.world.entity.TamableAnimal pet&&pet.isTame())return false;
  if(t instanceof org.slavicmyths.gorynych.Gorynych g&&g.state()>=org.slavicmyths.gorynych.GorynychRules.EXHAUSTED)return false;
  if(t instanceof org.slavicmyths.guardians.GuardianEntity g&&g.state()>=org.slavicmyths.guardians.GuardianRules.PURIFY)return false;
  if(t instanceof NeutralMob)return t instanceof Mob m&&m.getTarget()==p;
  return t instanceof Enemy||t instanceof Mob m&&m.getTarget()==p;
 }
 private static void message(ServerPlayer p,String key,Object... args){p.displayClientMessage(Component.translatable("winters.slavicmyths."+key,args),true);}
 private static void sound(ServerPlayer p,SoundEvent s,float pitch){p.serverLevel().playSound(null,p.blockPosition(),s,SoundSource.PLAYERS,.35f,pitch);}
 private static void clearShield(ServerPlayer p,boolean fx){var n=data(p);boolean existed=n.getFloat("Shield")>0;n.remove("Shield");n.remove("ShieldUntil");if(existed&&fx){sound(p,SoundEvents.GLASS_BREAK,1.4f);p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE,p.getX(),p.getY()+1,p.getZ(),6,.4,.6,.4,.01);}}
 public static void refresh(ServerPlayer p){
  boolean has=p.isAlive()&&inventory(p);p.getPersistentData().putBoolean("TwoWintersPresent",has);
  var kb=p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);boolean defensive=has&&!dark(p);
  if(kb!=null){if(defensive&&!kb.hasModifier(KNOCK))kb.addTransientModifier(new AttributeModifier(KNOCK,.2,AttributeModifier.Operation.ADD_VALUE));else if(!defensive)kb.removeModifier(KNOCK);}
  if(!has||dark(p)||p.level().dimension()!=Level.NETHER||!p.isAlive()||now(p)>=data(p).getLong("ShieldUntil"))clearShield(p,true);
 }
 public static void use(ServerPlayer p,ItemStack stack){
  if(!p.isAlive()||p.isSpectator()||!p.getMainHandItem().is(WintersContent.AMULET.get()))return;
  refresh(p);var n=data(p);
  if(p.isShiftKeyDown()){n.putBoolean("Dark",!dark(p));refresh(p);sound(p,dark(p)?SoundEvents.SOUL_ESCAPE.value():SoundEvents.AMETHYST_BLOCK_CHIME,dark(p)?.65f:1.4f);p.serverLevel().sendParticles(dark(p)?ParticleTypes.SOUL:ParticleTypes.SNOWFLAKE,p.getX(),p.getY()+1,p.getZ(),5,.3,.4,.3,.02);message(p,dark(p)?"karachun":"morozko");org.slavicmyths.rpg.RpgNetwork.sync(p);return;}
  if(p.level().dimension()!=Level.NETHER){message(p,"nether_only");return;}
  long time=now(p),ready=Math.max(n.getLong("Ready"),WintersRecipe.tag(stack).getLong("TwoWintersReady"));
  if(time<ready){message(p,"cooldown",(ready-time+19)/20);return;}
  n.putLong("Ready",time+WintersRules.COOLDOWN);
  var item=WintersRecipe.tag(stack);item.putLong("TwoWintersReady",time+WintersRules.COOLDOWN);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(item));
  if(!dark(p)){p.clearFire();n.putFloat("Shield",10);n.putLong("ShieldUntil",time+WintersRules.SHIELD_TICKS);sound(p,SoundEvents.AMETHYST_BLOCK_RESONATE,1.6f);message(p,"shield");}
  else{
   WAVE.set(true);try{
    for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(5),t->enemy(t,p)&&t.distanceToSqr(p)<=25&&p.hasLineOfSight(t))){
     boolean b=boss(t);if(t.hurt(p.damageSources().playerAttack(p),b?3:6)&&!b){t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,0),p);t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80,0),p);}
    }
   }finally{WAVE.set(false);}
   n.putLong("WaveUntil",time+10);sound(p,SoundEvents.SOUL_ESCAPE.value(),.5f);message(p,"wave");
  }
  // Persist the common deadline with the same player saving hook used by existing boss receipts.
  ((org.slavicmyths.equipment.mixin.FrostPlayerSaveInvoker)p.server.getPlayerList()).slavicmyths$saveFrost(p);
  org.slavicmyths.rpg.RpgNetwork.sync(p);
 }
 @SubscribeEvent public static void player(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p))return;
  // Animation only, no target search or damage.
  long time=now(p);var n=data(p);long wave=n.getLong("WaveUntil");
  if(wave>time){double r=(11-(wave-time))*.5;for(int i=0;i<12;i++){double a=i*Math.PI/6;p.serverLevel().sendParticles(new DustParticleOptions(new org.joml.Vector3f(.34f,.14f,.44f),.8f),p.getX()+Math.cos(a)*r,p.getY()+.2,p.getZ()+Math.sin(a)*r,1,0,0,0,0);}}
  if(p.tickCount%20!=0)return;
  refresh(p);
  if(n.getFloat("Shield")>0)for(int i=0;i<6;i++){double a=i*Math.PI/3;p.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE,p.getX()+Math.cos(a)*.7,p.getY()+.6+(i%2)*.6,p.getZ()+Math.sin(a)*.7,1,0,0,0,0);}
  aura(p);
 }
 public static void aura(ServerPlayer p){
  var n=data(p);
  if(!p.isAlive()||p.level().dimension()!=Level.NETHER||present(p)||p.isCreative()||p.isSpectator()||p.getAbilities().invulnerable){n.remove("AuraStart");n.remove("AuraStage");n.remove("AuraHit");p.removeEffect(WintersContent.AURA);return;}
  long time=now(p);if(!n.contains("AuraStart")){n.putLong("AuraStart",time);n.putInt("AuraStage",-1);}
  long elapsed=time-n.getLong("AuraStart");int stage=WintersRules.stage(elapsed);
  if(stage!=n.getInt("AuraStage")){n.putInt("AuraStage",stage);n.putLong("AuraHit",time);message(p,"aura_"+stage);sound(p,SoundEvents.SOUL_SAND_STEP,.6f);}
  if(stage>0)p.addEffect(new MobEffectInstance(WintersContent.AURA,40,stage==1?0:1,false,false,true));
  if(time%100<20)p.serverLevel().sendParticles(new DustParticleOptions(new org.joml.Vector3f(.62f,.1f,.15f),.7f),p.getX(),p.getY()+.8,p.getZ(),2,.35,.6,.35,0);
  int interval=WintersRules.interval(stage);if(interval>0&&time-n.getLong("AuraHit")>=interval){n.putLong("AuraHit",time);p.hurt(p.damageSources().magic(),stage==2?1:2);}
 }
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent.Pre e){
  if(e.getEntity().level().isClientSide||e.getNewDamage()<=0)return;
  if(!WAVE.get()&&e.getSource().getEntity() instanceof ServerPlayer p&&present(p)&&dark(p)&&enemy(e.getEntity(),p)&&!boss(e.getEntity())&&e.getEntity().getType().is(NETHER_ENEMIES))
   e.setNewDamage(e.getNewDamage()*1.15f);
  if(e.getEntity() instanceof ServerPlayer p&&present(p)&&!dark(p)&&!e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
   if(e.getSource().getDirectEntity()==e.getSource().getEntity()&&e.getSource().getEntity() instanceof LivingEntity attacker&&attacker.distanceToSqr(p)<=9&&enemy(attacker,p)&&p.getRandom().nextFloat()<.2f)attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0),p);
   float damage=e.getNewDamage();if(e.getSource().is(DamageTypeTags.IS_FIRE))damage*=.6f;
   var n=data(p);if(p.level().dimension()==Level.NETHER&&now(p)<n.getLong("ShieldUntil")&&n.getFloat("Shield")>0){float reserve=n.getFloat("Shield");n.putFloat("Shield",WintersRules.shieldLeft(damage,reserve));damage=WintersRules.shieldDamage(damage,reserve);if(n.getFloat("Shield")==0){n.putFloat("Shield",reserve);clearShield(p,true);}}
   e.setNewDamage(damage);
  }
 }
 @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
  if(e.getEntity().level().isClientSide||e.getNewDamage()<=0||WAVE.get())return;
  if(e.getSource().getDirectEntity()!=null&&e.getSource().getEntity() instanceof ServerPlayer p&&present(p)&&dark(p)&&enemy(e.getEntity(),p)&&!boss(e.getEntity())&&p.getRandom().nextFloat()<.15f)e.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0),p);
 }
 @SubscribeEvent public static void effect(MobEffectEvent.Added e){var effect=e.getEffectInstance();if(e.getEntity() instanceof ServerPlayer p&&present(p)&&dark(p)&&effect.is(MobEffects.WITHER)&&!effect.isInfiniteDuration())((org.slavicmyths.equipment.mixin.GorynychEffectDuration)effect).slavicmyths$gorynychDuration(Math.max(1,(int)Math.ceil(effect.getDuration()*.65)));}
 @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent e){
  if(!(e.getEntity() instanceof ServerPlayer p)||!WintersRecipe.original(e.getCrafting()))return;
  // Only a real first-recipe grid can authenticate an original.
  var grid=e.getInventory();if(grid.getContainerSize()!=9)return;var list=new java.util.ArrayList<ItemStack>();for(int i=0;i<9;i++)list.add(grid.getItem(i));
  if(WintersRecipe.shape(net.minecraft.world.item.crafting.CraftingInput.of(3,3,list),false)){WintersTemplates.get(p.serverLevel()).issue(WintersRecipe.tag(e.getCrafting()).getUUID("TwoWintersReceipt"));p.server.overworld().getDataStorage().save();}
 }
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){clearShield(p,false);data(p).remove("AuraStart");data(p).remove("AuraStage");data(p).remove("AuraHit");data(p).remove("WaveUntil");p.removeEffect(WintersContent.AURA);}}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void clone(PlayerEvent.Clone e){if(e.getEntity() instanceof ServerPlayer p){var n=data(e.getOriginal()).copy();n.remove("Shield");n.remove("ShieldUntil");n.remove("AuraStart");n.remove("AuraStage");n.remove("AuraHit");n.remove("WaveUntil");org.slavicmyths.rpg.PathData.data(p).put("TwoWinters197",n);}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){data(p).remove("Shield");data(p).remove("ShieldUntil");data(p).remove("AuraStart");data(p).remove("AuraStage");data(p).remove("AuraHit");refresh(p);org.slavicmyths.rpg.RpgNetwork.sync(p);}}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){clearShield(p,false);data(p).remove("AuraStart");data(p).remove("AuraStage");data(p).remove("AuraHit");data(p).remove("WaveUntil");}}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){refresh(p);aura(p);}}
 private WintersEvents(){}
}

