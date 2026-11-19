package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import java.util.List;

/** Tier VII airborne ocean boss. Server alone moves, targets and applies combat damage. */
public final class StratimEntity extends Monster {
    public static final int PATROL=0,CHASE=1,PECK=2,TALONS=3,DIVE=4,GUST=5,STORM=6,RETURN=7;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(StratimEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START=SynchedEntityData.defineId(StratimEntity.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new ServerBossEvent(Component.translatableWithFallback("entity.slavicmyths.stratim","Стратим-птица"),
        BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
    private int peckCd=35,talonCd=100,diveCd=220,gustCd=160,stormCd=450,actionEnd;
    private int lostTicks,patrolTicks,stormCharge;
    private Vec3 home;
    private boolean inflicted;
    public StratimEntity(EntityType<? extends StratimEntity> type,Level level) {
        super(type,level);
        noPhysics=false;
        setNoGravity(true);
        xpReward=110;
    }
    public static AttributeSupplier.Builder attributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,1150)
            .add(Attributes.ARMOR,8).add(Attributes.ARMOR_TOUGHNESS,3)
            .add(Attributes.ATTACK_DAMAGE,22).add(Attributes.MOVEMENT_SPEED,.35)
            .add(Attributes.FLYING_SPEED,.65).add(Attributes.FOLLOW_RANGE,64)
            .add(Attributes.KNOCKBACK_RESISTANCE,.75);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){
        super.defineSynchedData(b);
        b.define(ACTION,PATROL);b.define(START,0);
    }
    public int action(){return entityData.get(ACTION);}
    public int actionTicks(){return Math.max(0,tickCount-entityData.get(START));}
    private void begin(int state,int ticks){
        entityData.set(ACTION,state);entityData.set(START,tickCount);actionEnd=tickCount+ticks;inflicted=false;
    }
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override protected boolean shouldDespawnInPeaceful(){return true;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public void travel(Vec3 movement){move(net.minecraft.world.entity.MoverType.SELF,getDeltaMovement());}
    private boolean clear(Vec3 destination) {
        BlockPos p=BlockPos.containing(destination);
        for(int dx=-2;dx<=2;dx+=2)for(int dz=-2;dz<=2;dz+=2)
            for(int dy=-1;dy<=2;dy+=2){
                BlockPos q=p.offset(dx,dy,dz);
                if(!level().hasChunkAt(q)||!level().getBlockState(q).getCollisionShape(level(),q).isEmpty())return false;
            }
        return true;
    }
    private void steer(Vec3 destination,double speed){
        Vec3 diff=destination.subtract(position());
        if(diff.lengthSqr()<.09){setDeltaMovement(getDeltaMovement().scale(.7));return;}
        Vec3 desired=diff.normalize().scale(speed);
        if(!clear(position().add(desired.scale(5)))){
            desired=new Vec3(desired.z*.5,Math.max(.24,desired.y+.36),-desired.x*.5);
            if(!clear(position().add(desired.scale(4))))desired=new Vec3(0,.22,0);
        }
        setDeltaMovement(getDeltaMovement().scale(.76).add(desired.scale(.24)));
        if(getDeltaMovement().horizontalDistanceSqr()>.001){
            float yaw=(float)(Mth.atan2(-getDeltaMovement().x,getDeltaMovement().z)*Mth.RAD_TO_DEG);
            setYRot(Mth.rotLerp(.13F,getYRot(),yaw));setYBodyRot(getYRot());
        }
    }
    private LivingEntity findPrey(){
        List<LivingEntity> nearby=level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(64,48,64),e->e!=this && e.isAlive() && !e.isSpectator()
                && (e instanceof Player || e.getType().getCategory()==MobCategory.WATER_CREATURE));
        LivingEntity closest=null;double best=64*64;
        for(LivingEntity e:nearby){
            if(e instanceof Player p && (p.isCreative()||p.isSpectator()))continue;
            double d=distanceToSqr(e);
            if(d<best && hasLineOfSight(e)){best=d;closest=e;}
        }
        return closest;
    }
    private void damageCone(LivingEntity target,float amount,double reach,double spread){
        Vec3 facing=target.position().subtract(position()).normalize();
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(reach),
            x->x!=this && x.isAlive() && !x.isSpectator())){
            if(e instanceof Player p && (p.isCreative()||p.isSpectator()))continue;
            Vec3 ray=e.position().subtract(position());
            if(ray.lengthSqr()>reach*reach || ray.lengthSqr()<.01 || ray.normalize().dot(facing)<spread || !hasLineOfSight(e))continue;
            if(e.hurt(damageSources().mobAttack(this),amount))e.push(facing.x*.9,.35,facing.z*.9);
        }
    }
    @Override public void aiStep(){
        super.aiStep();
        if(level().isClientSide)return;
        setNoGravity(true);
        if(home==null)home=position();
        bar.setProgress(getHealth()/Math.max(1,getMaxHealth()));
        if(peckCd>0)peckCd--;if(talonCd>0)talonCd--;if(diveCd>0)diveCd--;
        if(gustCd>0)gustCd--;if(stormCd>0)stormCd--;
        LivingEntity target=getTarget();
        if(target==null || !target.isAlive() || distanceToSqr(target)>96*96){
            if(++lostTicks%30==0)target=findPrey();
            setTarget(target);
        }else lostTicks=0;
        if(action()==PECK||action()==TALONS||action()==DIVE||action()==GUST||action()==STORM){
            if(target!=null){
                if(action()==DIVE && actionTicks()<25)steer(target.position().add(0,1.2,0),1.25);
                else if(action()==DIVE)steer(target.position().add(0,9,0),.65);
                else if(action()==STORM)steer(target.position().add(0,17,0),.35);
                else steer(target.position().add(0,4,0),.45);
                if(!inflicted){
                    int t=actionTicks();
                    if(action()==PECK&&t>=12){damageCone(target,22,5,.35);inflicted=true;}
                    if(action()==TALONS&&t>=16){damageCone(target,25,6,.2);inflicted=true;}
                    if(action()==DIVE&&t>=20){damageCone(target,33,7,.15);inflicted=true;}
                    if(action()==GUST&&t>=20){damageCone(target,17,18,.3);inflicted=true;}
                    if(action()==STORM&&t>=32){
                        damageCone(target,18,22,-.1);inflicted=true;
                        if(level() instanceof ServerLevel server)server.sendParticles(ParticleTypes.CLOUD,
                            getX(),getY(),getZ(),70,10,4,10,.12);
                    }
                }
            }
            if(tickCount>=actionEnd)begin(target==null?RETURN:CHASE,0);
            return;
        }
        if(target==null){
            if(action()!=PATROL&&action()!=RETURN)begin(RETURN,0);
            if(distanceToSqr(home)>30*30)steer(home.add(0,8,0),.55);
            else {
                if(action()!=PATROL)begin(PATROL,0);
                patrolTicks++;
                double a=(patrolTicks%1200)*Math.PI/300;
                steer(home.add(Math.cos(a)*22,8+Math.sin(a*.5)*6,Math.sin(a)*22),.38);
            }
            return;
        }
        if(action()!=CHASE)begin(CHASE,0);
        double d=distanceToSqr(target);
        if(stormCd==0 && d<32*32){stormCd=600;begin(STORM,58);return;}
        if(diveCd==0&&d>12*12&&d<40*40){diveCd=300;begin(DIVE,42);return;}
        if(gustCd==0&&d<19*19){gustCd=240;begin(GUST,36);return;}
        if(talonCd==0&&d<7*7){talonCd=140;begin(TALONS,28);return;}
        if(peckCd==0&&d<5*5){peckCd=48;begin(PECK,22);return;}
        steer(target.position().add(0,d<64?5:13,0),.65);
    }
}
