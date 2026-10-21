package org.slavicmyths.poloz;

import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.sounds.*;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import org.slavicmyths.rpg.Runes;

/** Server-authoritative bounded reward effects, triggered by equipment and accepted hits. */
@EventBusSubscriber(modid="slavicmyths")
public final class PolozRewardEvents {
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getMainHandItem().is(PolozRewards.WHIP.get())){p.getPersistentData().putLong("PolozPrimaryAt",p.server.overworld().getGameTime());p.getPersistentData().putBoolean("PolozPrimaryCharged",p.getAttackStrengthScale(.5f)>.85f);}}
 @SubscribeEvent public static void tooltip(ItemTooltipEvent e){String setKey=org.slavicmyths.item.ArmorSetHealth.tooltip(e.getItemStack());if(setKey!=null)e.getToolTip().add(Component.translatable(setKey));if(PolozRewards.upgrade(e.getItemStack())!=null)e.getToolTip().add(Component.translatable("poloz.upgrade_help"));}
 public static boolean secondary(ServerPlayer p){return EFFECT.contains(p.getUUID());}
 private static final Set<UUID> EFFECT=new HashSet<>();
 public static void refresh(LivingEntity e){if(e.level().isClientSide)return;org.slavicmyths.item.ArmorSetHealth.refresh(e);apply(e,Attributes.KNOCKBACK_RESISTANCE,"poloz_set_knockback",PolozRewards.knockback(e));if(e.getHealth()>e.getMaxHealth())e.setHealth(e.getMaxHealth());}
 private static void apply(LivingEntity e,net.minecraft.core.Holder<Attribute> attr,String name,double value){var a=e.getAttribute(attr);if(a==null)return;var id=PolozContent.id(name);a.removeModifier(id);if(value>0)a.addTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_VALUE));}
 @SubscribeEvent public static void equip(LivingEquipmentChangeEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getEntity() instanceof LivingEntity l)refresh(l);}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){refresh(e.getEntity());}
 @SubscribeEvent public static void mitigate(LivingDamageEvent.Pre e){if(!e.getEntity().level().isClientSide&&!e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))e.setNewDamage((float)(e.getNewDamage()*(1-PolozRewards.reduction(e.getEntity()))));}
 /** Equivalent smithing-table operation: sneak-use with netherite piece in hand, two scales in inventory. */
 @SubscribeEvent public static void smith(PlayerInteractEvent.RightClickBlock e){var p=e.getEntity();var s=e.getItemStack();if(!p.isShiftKeyDown()||!p.level().getBlockState(e.getPos()).is(Blocks.SMITHING_TABLE)||PolozRewards.upgrade(s)==null)return;
  e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);if(!(p instanceof ServerPlayer server))return;
  if(!p.mayBuild()||!p.mayInteract(p.level(),e.getPos())||s.getCount()!=1)return;
  if(Runes.inventoryCount(p,PolozContent.SCALE.get())<2){p.displayClientMessage(Component.translatable("poloz.upgrade_need"),true);return;}
  var result=PolozRewards.upgradeCopy(s);int left=2;for(int i=0;i<p.getInventory().getContainerSize()&&left>0;i++){var material=p.getInventory().getItem(i);if(material.is(PolozContent.SCALE.get())){int n=Math.min(left,material.getCount());material.shrink(n);left-=n;}}
  p.setItemInHand(e.getHand(),result);p.getInventory().setChanged();p.level().playSound(null,e.getPos(),SoundEvents.SMITHING_TABLE_USE,SoundSource.BLOCKS,1,1);server.containerMenu.broadcastChanges();
 }
 public static boolean eligible(ServerPlayer p,LivingEntity t){return t!=p&&t.isAlive()&&!t.isAlliedTo(p)&&!p.getUUID().equals(org.slavicmyths.kalin.KalinParty.owner(t))&&(!(t instanceof TamableAnimal pet)||!p.getUUID().equals(pet.getOwnerUUID()))&&(p.serverLevel().getServer().isPvpAllowed()||!(t instanceof net.minecraft.world.entity.player.Player));}
 private static boolean sight(ServerPlayer p,LivingEntity t){return p.hasLineOfSight(t);}
 @SubscribeEvent public static void hit(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getSource().getDirectEntity()!=p||!e.getSource().is(DamageTypes.PLAYER_ATTACK)||EFFECT.contains(p.getUUID()))return;
  var target=e.getEntity();var s=p.getMainHandItem();long now=p.server.overworld().getGameTime();var data=p.getPersistentData();
  // Secondary lash hits and fissures cannot feed themselves or multiply rune counters.
  if(target==p||target.isAlliedTo(p)||p.getUUID().equals(org.slavicmyths.kalin.KalinParty.owner(target)))return;
  if(s.is(PolozRewards.WHIP.get()))s.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
  if(s.is(PolozRewards.WHIP.get())&&data.getLong("PolozPrimaryAt")==now&&data.getBoolean("PolozPrimaryCharged")&&now>=data.getLong("PolozSweepAt")){data.putLong("PolozSweepAt",now+20);var look=p.getLookAngle().multiply(1,0,1).normalize();EFFECT.add(p.getUUID());try{int hits=0;for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(6,2,6),t->t!=target&&eligible(p,t))){var delta=t.position().subtract(p.position()).multiply(1,0,1);if(delta.lengthSqr()>36||delta.normalize().dot(look)<.25||!sight(p,t))continue;if(hits++>=3)break;t.hurt(p.damageSources().playerAttack(p),13);}}finally{EFFECT.remove(p.getUUID());}p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,.8f,.65f);}
  if(!org.slavicmyths.rpg.AdvancedRunes.active(s,"earth_lord_iv"))return;
  // Only one accepted primary strike per tick, including multipart or broad attacks.
  if(data.getLong("PolozRuneHitAt")==now&&data.contains("PolozRuneHitAt"))return;data.putLong("PolozRuneHitAt",now);
  int count=(data.getInt("PolozRuneHits")+1)%4;data.putInt("PolozRuneHits",count);if(count!=0||now<data.getLong("PolozRuneReady"))return;data.putLong("PolozRuneReady",now+400);
  EFFECT.add(p.getUUID());try{Vec3 impact=target.position();if(target instanceof GreatPoloz boss){double distance=boss.getBoundingBox().distanceToSqr(p.getEyePosition());for(var part:boss.parts){double d=part.getBoundingBox().distanceToSqr(p.getEyePosition());if(d<distance){distance=d;impact=part.position();}}boss.rewardFissure(p,6);}fissure(p,impact);final Vec3 center=impact;int n=target instanceof GreatPoloz?1:0;for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(impact,impact).inflate(2),t->eligible(p,t)&&!(t instanceof GreatPoloz)&&t.position().distanceToSqr(center)<=4)){if(n++>=4)break;t.invulnerableTime=0;t.hurt(p.damageSources().playerAttack(p),6);}}finally{EFFECT.remove(p.getUUID());}
 }
 public static void fissure(ServerPlayer p,Vec3 at){var w=p.serverLevel();var dust=new DustParticleOptions(new org.joml.Vector3f(.75f,.09f,.12f),.9f);for(int i=0;i<12;i++)w.sendParticles(dust,at.x+(i-6)*.18,at.y+.08,at.z+Math.sin(i*2)*.12,1,0,0,0,0);w.playSound(null,BlockPos.containing(at),SoundEvents.DEEPSLATE_BREAK,SoundSource.PLAYERS,.8f,.6f);}
 public static void spikes(ServerPlayer p){var w=p.serverLevel();var direction=p.getLookAngle().multiply(1,0,1).normalize();if(direction.lengthSqr()<.5)return;Set<UUID> struck=new HashSet<>();EFFECT.add(p.getUUID());try{for(int i=1;i<=3;i++){Vec3 at=p.position().add(direction.scale(i*1.8));var hit=w.clip(new net.minecraft.world.level.ClipContext(p.position().add(0,.5,0),at.add(0,.5,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.MISS)break;var ground=w.clip(new net.minecraft.world.level.ClipContext(at.add(0,1,0),at.add(0,-2,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));if(ground.getType()==HitResult.Type.MISS||!w.hasChunkAt(BlockPos.containing(ground.getLocation())))continue;at=ground.getLocation();var spike=PolozContent.SPIKE.get().create(w);if(spike!=null){spike.setPos(at);w.addFreshEntity(spike);}fissure(p,at);for(int y=0;y<8;y++)w.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.DEEPSLATE.defaultBlockState()),at.x,at.y+y*.18,at.z,5,.12,.05,.12,.015);for(var t:w.getEntitiesOfClass(LivingEntity.class,new AABB(at.x-.8,at.y,at.z-.8,at.x+.8,at.y+2,at.z+.8),t->eligible(p,t)))if(struck.add(t.getUUID()))t.hurt(p.damageSources().playerAttack(p),10);}}finally{EFFECT.remove(p.getUUID());}}
 private PolozRewardEvents(){}
}
