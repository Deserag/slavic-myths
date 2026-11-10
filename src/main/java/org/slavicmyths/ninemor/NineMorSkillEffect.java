package org.slavicmyths.ninemor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Server-driven battle choreography; synchronized entities keep the real 3D weapons visible for their full attack. */
public final class NineMorSkillEffect extends ThrowableItemProjectile {
    public static final int SWEEP=1,RIFT=2,EXECUTION=3,DOME=4;
    private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(NineMorSkillEffect.class,EntityDataSerializers.INT);
    private UUID markedTarget;
    private final Set<UUID> hitTargets=new HashSet<>();
    private boolean executionResolved;

    public NineMorSkillEffect(EntityType<? extends NineMorSkillEffect> type,Level level){
        super(type,level);
        setNoGravity(true);
        noPhysics=true;
    }
    @Override protected Item getDefaultItem(){return NineMorContent.SPEAR.get();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);
        builder.define(MODE,0);
    }
    public int mode(){return entityData.get(MODE);}
    public static NineMorSkillEffect spawn(ServerPlayer player,int mode,LivingEntity target) {
        if(mode<SWEEP||mode>DOME)return null;
        NineMorSkillEffect fx=new NineMorSkillEffect(NineMorContent.SKILL.get(),player.level());
        fx.entityData.set(MODE,mode);
        fx.setOwner(player);
        fx.setYRot(player.getYRot());
        if(target!=null)fx.markedTarget=target.getUUID();
        Vec3 dir=Vec3.directionFromRotation(0F,player.getYRot()).multiply(1D,0D,1D).normalize();
        Vec3 from=mode==EXECUTION&&target!=null?target.position().add(0,target.getBbHeight()*.6D,0):
            player.position().add(0,mode==RIFT?.12D:1.25D,0).add(mode==RIFT?dir.scale(1.1D):Vec3.ZERO);
        fx.setPos(from.x,from.y,from.z);
        player.serverLevel().addFreshEntity(fx);
        return fx;
    }
    @Override protected boolean canHitEntity(Entity entity){return false;}
    @Override protected void onHitEntity(EntityHitResult hit){}
    @Override protected void onHitBlock(BlockHitResult hit){}
    private boolean valid(ServerPlayer player,LivingEntity victim){
        return victim.level()==level()&&NineMorRewardAbilities.eligible(player,victim);
    }
    @Override public void tick(){
        if(level() instanceof ServerLevel world) {
            if(!(getOwner() instanceof ServerPlayer player)||!player.isAlive()||player.level()!=level()){discard();return;}
            int age=tickCount+1;
            switch(mode()){
                case SWEEP -> {
                    setPos(player.getX(),player.getY()+1.25D,player.getZ());
                    if(age==8){
                        int hit=0;
                        for(LivingEntity enemy:world.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(4.3D),
                                e->valid(player,e)&&e.distanceToSqr(player)<=20D)){
                            if(enemy.hurt(player.damageSources().playerAttack(player),16F)){
                                NineMorSerpent.applyElement(enemy,hit%9);
                                enemy.addEffect(new MobEffectInstance(MobEffects.WITHER,65,0));
                                Vec3 delta=enemy.position().subtract(player.position());
                                if(delta.horizontalDistanceSqr()>.01D)enemy.knockback(.7D,-delta.x,-delta.z);
                                if(++hit>=12)break;
                            }
                        }
                    }
                }
                case RIFT -> {
                    if(age<=16){
                        Vec3 dir=Vec3.directionFromRotation(0F,getYRot()).multiply(1D,0D,1D).normalize();
                        Vec3 next=position().add(dir.scale(.79D));
                        if(!world.hasChunkAt(BlockPos.containing(next))){discard();return;}
                        setPos(next.x,next.y,next.z);
                        if(hitTargets.size()<12){
                            for(LivingEntity enemy:world.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2.15D,2D,2.15D),
                                    e->valid(player,e)&&!hitTargets.contains(e.getUUID()))){
                                Vec3 delta=enemy.position().subtract(position());
                                if(delta.x*delta.x+delta.z*delta.z>4.62D)continue;
                                hitTargets.add(enemy.getUUID());
                                if(enemy.hurt(player.damageSources().playerAttack(player),20F)){
                                    enemy.addEffect(new MobEffectInstance(MobEffects.WITHER,90,1));
                                    enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,1));
                                }
                                if(hitTargets.size()>=12)break;
                            }
                        }
                    }
                }
                case EXECUTION -> {
                    Entity found=markedTarget==null?null:world.getEntity(markedTarget);
                    if(!(found instanceof LivingEntity victim)||!valid(player,victim)){discard();return;}
                    setPos(victim.getX(),victim.getY()+victim.getBbHeight()*.6D,victim.getZ());
                    if(age==17&&!executionResolved){
                        executionResolved=true;
                        if(victim.getHealth()<=victim.getMaxHealth()*.35F&&player.hasLineOfSight(victim)
                                &&victim.distanceToSqr(player)<=100D){
                            float damage=Math.min(100F,Math.max(40F,victim.getMaxHealth()*.38F));
                            if(victim.hurt(player.damageSources().playerAttack(player),damage)){
                                victim.addEffect(new MobEffectInstance(MobEffects.WITHER,125,2));
                                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,75,2));
                            }
                        }
                    }
                }
                case DOME -> setPos(player.getX(),player.getY()+1.25D,player.getZ());
                default -> {discard();return;}
            }
            if(age>=(mode()==DOME?120:mode()==EXECUTION?26:mode()==RIFT?19:18)){discard();return;}
        }
        setDeltaMovement(Vec3.ZERO);
        super.tick();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        tag.putInt("MorSkillMode",mode());
        if(markedTarget!=null)tag.putUUID("MorSkillTarget",markedTarget);
        tag.putBoolean("MorSkillExecutionResolved",executionResolved);
        tag.putInt("MorSkillHitCount",hitTargets.size());
        int i=0;for(UUID hit:hitTargets)tag.putUUID("MorSkillHit"+(i++),hit);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        entityData.set(MODE,tag.getInt("MorSkillMode"));
        markedTarget=tag.hasUUID("MorSkillTarget")?tag.getUUID("MorSkillTarget"):null;
        executionResolved=tag.getBoolean("MorSkillExecutionResolved");
        int count=Math.min(12,tag.getInt("MorSkillHitCount"));
        for(int i=0;i<count;i++)if(tag.hasUUID("MorSkillHit"+i))hitTargets.add(tag.getUUID("MorSkillHit"+i));
    }
}
