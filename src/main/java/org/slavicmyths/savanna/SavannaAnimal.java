package org.slavicmyths.savanna;

import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public final class SavannaAnimal extends PathfinderMob {
    public enum Kind {
        AUROCHS("aurochs",48,0.25,7,6),
        SAIGA("saiga",22,0.34,0,14),
        TARPAN("tarpan",32,0.31,0,9),
        JAYRAN("jayran",20,0.36,0,7),
        JACKAL("jackal",19,0.32,4,5),
        STRIPED_HYENA("striped_hyena",29,0.28,5,3),
        PERSIAN_LEOPARD("persian_leopard",34,0.34,7,1),
        PHEASANT("pheasant",8,0.25,0,4),
        STEPPE_EAGLE("steppe_eagle",18,0.28,4,2);
        public final String id;
        final double health,speed,damage;
        final int maxGroup;
        Kind(String id,double health,double speed,double damage,int maxGroup){this.id=id;this.health=health;this.speed=speed;this.damage=damage;this.maxGroup=maxGroup;}
        public boolean bird(){return this==PHEASANT||this==STEPPE_EAGLE;}
        public boolean predator(){return this==JACKAL||this==STRIPED_HYENA||this==PERSIAN_LEOPARD||this==STEPPE_EAGLE;}
        public boolean herd(){return this==AUROCHS||this==SAIGA||this==TARPAN||this==JAYRAN;}
        public boolean flock(){return maxGroup>1&&this!=STEPPE_EAGLE;}
    }
    private static final EntityDataAccessor<Integer> STRIKE=SynchedEntityData.defineId(SavannaAnimal.class,EntityDataSerializers.INT);
    private final Kind kind;
    private UUID groupId;
    private LivingEntity danger;
    private int fear,anger,cooldown;
    private static final class Group implements SpawnGroupData {
        final UUID id;
        Group(UUID id){this.id=id;}
    }
    public SavannaAnimal(EntityType<? extends SavannaAnimal> type,Level world,Kind kind){
        super(type,world);this.kind=kind;
        if(kind==Kind.STEPPE_EAGLE){
            moveControl=new FlyingMoveControl(this,20,true);
            navigation=new FlyingPathNavigation(this,world);
        }
        installGoals();
    }
    public Kind kind(){return kind;}
    public int strikeTicks(){return entityData.get(STRIKE);}
    public int strikeLength(){return kind==Kind.AUROCHS?18:12;}
    public static AttributeSupplier.Builder attributes(Kind kind){
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,kind.health)
            .add(Attributes.MOVEMENT_SPEED,kind.speed).add(Attributes.FLYING_SPEED,.42)
            .add(Attributes.ATTACK_DAMAGE,kind.damage).add(Attributes.FOLLOW_RANGE,24)
            .add(Attributes.ARMOR,kind==Kind.AUROCHS?3:0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STRIKE,0);}
    @Override protected void registerGoals(){}
    private void installGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        if(kind.damage>0)goalSelector.addGoal(1,new StrikeGoal());
        goalSelector.addGoal(2,new EscapeGoal());
        if(kind.flock())goalSelector.addGoal(3,new GroupGoal());
        if(kind==Kind.STEPPE_EAGLE)goalSelector.addGoal(5,new WaterAvoidingRandomFlyingGoal(this,1.0));
        else goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.85));
        goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,7));
        goalSelector.addGoal(8,new RandomLookAroundGoal(this));
    }
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){
        super.finalizeSpawn(world,difficulty,reason,data);
        Group group=data instanceof Group g?g:new Group(getUUID());
        groupId=group.id;return group;
    }
    @Override public int getMaxSpawnClusterSize(){return kind.maxGroup;}
    private boolean sameGroup(SavannaAnimal other){return groupId!=null&&groupId.equals(other.groupId)&&kind==other.kind;}
    private boolean valid(LivingEntity e){return e!=null&&e.isAlive()&&e.level()==level()&&(!(e instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
    private boolean prey(LivingEntity e){
        if(!valid(e)||!(e instanceof SavannaAnimal a)||a.kind.predator())return false;
        return switch(kind){
            case STEPPE_EAGLE,JACKAL -> a.kind==Kind.PHEASANT;
            case STRIPED_HYENA -> a.kind==Kind.PHEASANT||a.kind==Kind.JAYRAN;
            case PERSIAN_LEOPARD -> a.kind==Kind.PHEASANT||a.kind==Kind.JAYRAN||a.kind==Kind.SAIGA;
            default -> false;
        };
    }
    private void alert(LivingEntity attacker){
        if(!valid(attacker))return;
        if(kind==Kind.AUROCHS||kind.predator()){setTarget(attacker);anger=240;}
        else{danger=attacker;fear=120;setTarget(null);}
    }
    @Override public boolean hurt(DamageSource source,float amount){
        boolean hit=super.hurt(source,amount);
        if(hit&&!level().isClientSide&&source.getEntity() instanceof LivingEntity attacker&&attacker!=this&&valid(attacker)){
            alert(attacker);
            if(kind.flock())for(SavannaAnimal a:level().getEntitiesOfClass(SavannaAnimal.class,getBoundingBox().inflate(14),x->x!=this&&sameGroup(x)&&x.isAlive()))a.alert(attacker);
        }
        return hit;
    }
    @Override public void aiStep(){
        super.aiStep();
        if(level().isClientSide)return;
        if(cooldown>0)cooldown--;
        if(fear>0)fear--;
        if(fear==0||!valid(danger))danger=null;
        LivingEntity target=getTarget();
        if(target!=null&&(!valid(target)||distanceToSqr(target)>32*32||anger<=0)){setTarget(null);entityData.set(STRIKE,0);}
        if(anger>0)anger--;
        if((tickCount+getId())%60==0&&getTarget()==null&&fear==0){
            if(kind.predator()&&random.nextInt(3)==0){
                SavannaAnimal nearest=null;double best=18*18;
                for(SavannaAnimal a:level().getEntitiesOfClass(SavannaAnimal.class,getBoundingBox().inflate(18),this::prey)){
                    double d=distanceToSqr(a);if(d<best&&hasLineOfSight(a)){nearest=a;best=d;}
                }
                if(nearest!=null){setTarget(nearest);anger=180;}
            }else if(!kind.predator()&&kind!=Kind.AUROCHS){
                for(SavannaAnimal a:level().getEntitiesOfClass(SavannaAnimal.class,getBoundingBox().inflate(8),x->x.kind.predator()&&x.isAlive())){
                    if(a.getTarget()==this||hasLineOfSight(a)){danger=a;fear=80;break;}
                }
            }
        }
        if(kind==Kind.STEPPE_EAGLE&&getTarget()==null&&getNavigation().isDone()&&onGround()&&tickCount%40==0){
            getNavigation().moveTo(getX()+random.nextInt(13)-6,getY()+4,getZ()+random.nextInt(13)-6,1);
        }
    }
    private final class StrikeGoal extends Goal{
        StrikeGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        public boolean canUse(){return valid(getTarget());}
        public boolean requiresUpdateEveryTick(){return true;}
        public void stop(){entityData.set(STRIKE,0);getNavigation().stop();}
        public void tick(){
            LivingEntity target=getTarget();if(!valid(target))return;
            getLookControl().setLookAt(target,30,30);
            double reach=(getBbWidth()+target.getBbWidth())*.5+1.0;
            int t=strikeTicks();
            if(t>0){
                getNavigation().stop();
                if(t==strikeLength()/2&&distanceToSqr(target)<reach*reach&&hasLineOfSight(target))doHurtTarget(target);
                entityData.set(STRIKE,t-1);
                if(t==1)cooldown=kind==Kind.AUROCHS?35:25;
                return;
            }
            if(distanceToSqr(target)<reach*reach&&cooldown==0&&hasLineOfSight(target)){
                entityData.set(STRIKE,strikeLength());getNavigation().stop();return;
            }
            if(tickCount%5==0)getNavigation().moveTo(target,1.3);
        }
    }
    private final class EscapeGoal extends Goal{
        EscapeGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        public boolean canUse(){return fear>0&&valid(danger)&&getTarget()==null;}
        public boolean requiresUpdateEveryTick(){return true;}
        public void stop(){getNavigation().stop();}
        public void tick(){
            if(!valid(danger)||tickCount%8!=0)return;
            Vec3 away=position().subtract(danger.position()).multiply(1,0,1).normalize();
            getNavigation().moveTo(getX()+away.x*8,getY(),getZ()+away.z*8,1.45);
            if(kind==Kind.PHEASANT&&onGround())setDeltaMovement(away.scale(.35).add(0,.55,0));
        }
    }
    private final class GroupGoal extends Goal{
        private SavannaAnimal leader;
        GroupGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        public boolean canUse(){
            if(groupId==null||fear>0||getTarget()!=null||(tickCount+getId())%35!=0)return false;
            leader=null;
            for(SavannaAnimal a:level().getEntitiesOfClass(SavannaAnimal.class,getBoundingBox().inflate(24),x->x!=SavannaAnimal.this&&sameGroup(x)&&x.isAlive())){
                if(a.getUUID().compareTo(getUUID())>=0)continue;
                if(leader==null||a.getUUID().compareTo(leader.getUUID())<0)leader=a;
            }
            return leader!=null&&distanceToSqr(leader)>36;
        }
        public boolean canContinueToUse(){return leader!=null&&leader.isAlive()&&fear==0&&getTarget()==null&&distanceToSqr(leader)>16&&distanceToSqr(leader)<900;}
        public void start(){getNavigation().moveTo(leader,1.0);}
        public void tick(){if(tickCount%10==0)getNavigation().moveTo(leader,1.0);}
        public void stop(){leader=null;getNavigation().stop();}
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(groupId!=null)n.putUUID("SavannaGroup",groupId);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);groupId=n.hasUUID("SavannaGroup")?n.getUUID("SavannaGroup"):null;entityData.set(STRIKE,0);}
}
