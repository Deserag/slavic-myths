package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
public final class BreathCloud extends Entity {
 private static final EntityDataAccessor<Integer> TYPE=SynchedEntityData.defineId(BreathCloud.class,EntityDataSerializers.INT),USES=SynchedEntityData.defineId(BreathCloud.class,EntityDataSerializers.INT);
 private UUID owner;private int life=GorynychRules.CLOUD_LIFE;private boolean collectable=true;private double radius=3;
 public BreathCloud(EntityType<? extends BreathCloud> t,Level w){super(t,w);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(TYPE,0);b.define(USES,3);}
 public void configure(LivingEntity e,int type,Vec3 p,int uses){owner=e.getUUID();entityData.set(TYPE,type);entityData.set(USES,uses);setPos(p);collectable=e instanceof Gorynych;life=collectable?240:80;radius=collectable?3:2;}
 public int type(){return entityData.get(TYPE);}public int uses(){return entityData.get(USES);}
 public static ParticleOptions particle(int type){return new DustParticleOptions(new org.joml.Vector3f(type==0?.64f:type==1?1f:.55f,type==0?.12f:type==1?.38f:.72f,type==0?.9f:type==1?.04f:.84f),1.2f);}
 @Override public boolean isPickable(){return isAlive();}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();var entity=owner==null?null:w.getEntity(owner);if(!(entity instanceof LivingEntity living)||!living.isAlive()||entity instanceof Gorynych boss&&!boss.fighting()||--life<=0){discard();return;}
  if(tickCount%4==0)for(int i=0;i<uses()*3;i++){double a=i*Math.PI*2/Math.max(1,uses()*3)+tickCount*.04;w.sendParticles(particle(type()),getX()+Math.cos(a)*radius,getY()+(type()==2?.6:.2),getZ()+Math.sin(a)*radius,1,.1,.08,.1,0);}
  if(tickCount%20==0)for(var target:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(0,.3,0),t->t!=living&&t.isAlive()&&t.position().distanceToSqr(position())<radius*radius&&allowed(living,t))){if(w.clip(new net.minecraft.world.level.ClipContext(position().add(0,.6,0),target.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)continue;target.hurt(org.slavicmyths.combat.MythDamageSources.periodic(type()==0?"gorynych_mor":type()==1?"gorynych_fire":"gorynych_impact",living),type()==2?2:3);if(type()==0)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,80));if(type()==1)target.igniteForSeconds(2);if(type()==2){Vec3 d=target.position().subtract(position()).normalize().scale(.4);target.setDeltaMovement(target.getDeltaMovement().add(d.x,.18,d.z));target.hurtMarked=true;}if(living instanceof Gorynych g){UUID player=org.slavicmyths.karachun.NightOwners.owner(target);if(player!=null)GorynychLedger.get(w).participant(g.instance,player);}}
 }
 public static boolean allowed(LivingEntity owner,LivingEntity target){if(owner instanceof Gorynych g)return g.candidate(target);return target instanceof Enemy&&!target.isAlliedTo(owner)&&!owner.isAlliedTo(target)&&!(target instanceof Gorynych g&&g.state()>=GorynychRules.EXHAUSTED)&&!(target instanceof org.slavicmyths.guardians.GuardianEntity g&&g.state()>=org.slavicmyths.guardians.GuardianRules.PURIFY);}
 @Override public InteractionResult interact(Player player,InteractionHand hand){if(level().isClientSide)return InteractionResult.SUCCESS;if(!collectable||uses()<=0||!(player instanceof ServerPlayer p)||p.isSpectator()||!p.isAlive()||distanceToSqr(p)>64)return InteractionResult.PASS;var stack=p.getItemInHand(hand);if(!stack.is(Items.GLASS_BOTTLE))return InteractionResult.PASS;stack.shrink(1);var bottle=new ItemStack(GorynychContent.bottle(type()));if(!p.getInventory().add(bottle))p.drop(bottle,false);entityData.set(USES,uses()-1);GorynychContent.sound(level(),blockPosition(),"bottle_collect");if(uses()==0)discard();return InteractionResult.SUCCESS;}
 @Override protected void readAdditionalSaveData(CompoundTag n){}
 @Override protected void addAdditionalSaveData(CompoundTag n){}
}
