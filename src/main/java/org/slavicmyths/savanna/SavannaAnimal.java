package org.slavicmyths.savanna;

import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
    private final boolean packBoss;
    public enum HyenaRole { NONE, LEADER, GUARD, RETINUE }
    private final HyenaRole hyenaRole;
    private static final EntityDataAccessor<Integer> ROAR=SynchedEntityData.defineId(SavannaAnimal.class,EntityDataSerializers.INT);
    private boolean heavyStrike, chargeHit;
    private static final EntityDataAccessor<Boolean> CHARGE=SynchedEntityData.defineId(SavannaAnimal.class,EntityDataSerializers.BOOLEAN);
    UUID packId;
    int packSlot=-1;
    private UUID groupId;
    private LivingEntity danger;
    private int fear,anger,cooldown;
    private static final class Group implements SpawnGroupData {
        final UUID id;
        Group(UUID id){this.id=id;}
    }
    public SavannaAnimal(EntityType<? extends SavannaAnimal> type,Level world,Kind kind){
        this(type,world,kind,false);
    }
    public SavannaAnimal(EntityType<? extends SavannaAnimal> type,Level world,Kind kind,boolean packBoss){
        this(type,world,kind,packBoss,HyenaRole.NONE);
    }
    public SavannaAnimal(EntityType<? extends SavannaAnimal> type,Level world,Kind kind,HyenaRole role){
        this(type,world,kind,false,role);
    }
    private SavannaAnimal(EntityType<? extends SavannaAnimal> type,Level world,Kind kind,boolean packBoss,HyenaRole role){
        super(type,world);this.kind=kind;this.packBoss=packBoss;this.hyenaRole=role;
        if(isEncounterMember()){setPersistenceRequired();xpReward=0;}
        if(kind==Kind.STEPPE_EAGLE){
            moveControl=new FlyingMoveControl(this,20,true);
            navigation=new FlyingPathNavigation(this,world);
        }
        installGoals();
    }
    public Kind kind(){return kind;}
    public boolean isPackBoss(){return packBoss;}
    public HyenaRole hyenaRole(){return hyenaRole;}
    public boolean isHyenaPack(){return hyenaRole!=null&&hyenaRole!=HyenaRole.NONE;}
    public boolean isEncounterMember(){return packBoss||isHyenaPack();}
    public int roarTicks(){return entityData.get(ROAR);}
    public boolean charging(){return entityData.get(CHARGE);}
    public float encounterScale(){return packBoss?1.5F:hyenaRole==HyenaRole.LEADER?2.5F:hyenaRole==HyenaRole.GUARD?2F:1F;}
    public static AttributeSupplier.Builder hyenaAttributes(HyenaRole role){
        return attributes(Kind.STRIPED_HYENA).add(Attributes.MAX_HEALTH,role==HyenaRole.LEADER?400:role==HyenaRole.GUARD?150:29)
            .add(Attributes.ATTACK_DAMAGE,role==HyenaRole.LEADER?18:role==HyenaRole.GUARD?12:5)
            .add(Attributes.ARMOR,role==HyenaRole.LEADER?8:role==HyenaRole.GUARD?4:0).add(Attributes.FOLLOW_RANGE,36);
    }
    public static AttributeSupplier.Builder packAttributes(){return attributes(Kind.JACKAL).add(Attributes.MAX_HEALTH,JackalPacks.MEMBER_HP).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.FOLLOW_RANGE,36).add(Attributes.MOVEMENT_SPEED,.34);}
    @Override public net.minecraft.network.chat.Component getName(){
        if(hasCustomName())return super.getName();
        if(packBoss)return net.minecraft.network.chat.Component.translatableWithFallback("entity.slavicmyths.steppe_pack_jackal","Степной шакал стаи");
        if(isHyenaPack())return net.minecraft.network.chat.Component.translatableWithFallback(getType().getDescriptionId(),switch(hyenaRole){case LEADER->"Чёрная гиена";case GUARD->"Телохранитель Чёрной гиены";default->"Гиена свиты";});
        return super.getName();
    }
    public int strikeTicks(){return entityData.get(STRIKE);}
    public int strikeLength(){return kind==Kind.AUROCHS?18:12;}
    public static AttributeSupplier.Builder attributes(Kind kind){
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,kind.health)
            .add(Attributes.MOVEMENT_SPEED,kind.speed).add(Attributes.FLYING_SPEED,.42)
            .add(Attributes.ATTACK_DAMAGE,kind.damage).add(Attributes.FOLLOW_RANGE,24)
            .add(Attributes.ARMOR,kind==Kind.AUROCHS?3:0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STRIKE,0);b.define(ROAR,0);b.define(CHARGE,false);}
    @Override protected void registerGoals(){}
    private void installGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        if(kind.damage>0)goalSelector.addGoal(1,new StrikeGoal());
        goalSelector.addGoal(2,new EscapeGoal());
        if(kind.flock()&&!isEncounterMember())goalSelector.addGoal(3,new GroupGoal());
        if(isEncounterMember())goalSelector.addGoal(3,new PackRoamGoal());
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
    @Override public int getMaxSpawnClusterSize(){return isEncounterMember()?1:kind.maxGroup;}
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
            if(isHyenaPack()){
                if(level() instanceof ServerLevel w&&packId!=null&&packTargetAllowed(attacker)){
                    HyenaPacks.get(w).attacked(this,attacker);
                    if(strikeTicks()==0)setTarget(attacker);
                }
                return hit;
            }
            if(packBoss){if(level() instanceof ServerLevel w&&packId!=null&&canAttack(attacker)&&!(attacker instanceof SavannaAnimal a&&a.isPackBoss())){JackalPacks.get(w).attacked(packId,attacker);setTarget(attacker);}return hit;}
            alert(attacker);
            if(kind.flock())for(SavannaAnimal a:level().getEntitiesOfClass(SavannaAnimal.class,getBoundingBox().inflate(14),x->x!=this&&sameGroup(x)&&x.isAlive()))a.alert(attacker);
        }
        return hit;
    }
    @Override public void aiStep(){
        super.aiStep();
        if(level().isClientSide)return;
        if(cooldown>0)cooldown--;
        if(isEncounterMember()){if(level() instanceof ServerLevel w&&isAlive())packStep(w);return;}
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
    private void packStep(ServerLevel w){
        if(isHyenaPack()){hyenaStep(w);return;}
        JackalPacks data=JackalPacks.get(w);
        if(packId==null&&!data.spawn(this,w)){discard();return;}
        if(!data.validMember(this)){discard();return;}
        data.update(this,w);
        LivingEntity old=getTarget();
        if(old!=null&&(!valid(old)||!canAttack(old)||distanceToSqr(old)>36*36)){setTarget(null);entityData.set(STRIKE,0);}
        if((tickCount+getId())%10==0&&strikeTicks()==0){
            LivingEntity next=data.target(this,w);
            if(next!=getTarget()){setTarget(next);getNavigation().stop();}
        }
    }
    private void hyenaStep(ServerLevel w){
        HyenaPacks data=HyenaPacks.get(w);
        if(packId==null&&(hyenaRole!=HyenaRole.LEADER||!data.spawn(this,w))){discard();return;}
        if(!data.validMember(this)){discard();return;}
        data.update(this,w);
        LivingEntity old=getTarget();
        if(old!=null&&(!packTargetAllowed(old)||distanceToSqr(old)>36*36)){setTarget(null);entityData.set(STRIKE,0);}
        if(roarTicks()>0){entityData.set(ROAR,roarTicks()-1);getNavigation().stop();return;}
        if(hyenaRole==HyenaRole.LEADER&&strikeTicks()==0&&data.tryRoar(this,w)){
            entityData.set(ROAR,20);getNavigation().stop();
            playSound(net.minecraft.sounds.SoundEvents.WOLF_GROWL,1.8F,.65F);return;
        }
        if((tickCount+getId())%10==0&&strikeTicks()==0){
            LivingEntity next=data.target(this,w);
            if(next!=getTarget()){setTarget(next);getNavigation().stop();}
        }
    }
    boolean packTargetAllowed(LivingEntity x){return x!=this&&valid(x)&&canAttack(x)&&!isAlliedTo(x)
        &&!(x instanceof SavannaAnimal a&&((packId!=null&&packId.equals(a.packId))||a.kind()==kind));}
    private boolean hyenaBoosted(){return isHyenaPack()&&level() instanceof ServerLevel w&&
        (hyenaRole==HyenaRole.LEADER?(HyenaPacks.get(w).enraged(packId)||getHealth()<getMaxHealth()*.25F):HyenaPacks.get(w).buffed(packId,w.getGameTime()));}
    boolean packEnraged(){return packBoss&&level() instanceof ServerLevel w&&JackalPacks.get(w).enraged(packId);}
    boolean packPrey(LivingEntity x){if(isHyenaPack())return packTargetAllowed(x)&&x.getMaxHealth()<getMaxHealth();return x!=this&&valid(x)&&canAttack(x)&&!isAlliedTo(x)
        &&!(x instanceof SavannaAnimal a&&(a.isPackBoss()||a.kind()==Kind.JACKAL))
        &&x.getMaxHealth()<getMaxHealth();}
    private final class PackRoamGoal extends Goal{
        PackRoamGoal(){setFlags(EnumSet.of(Flag.MOVE));}
        public boolean canUse(){return packId!=null&&getTarget()==null&&level() instanceof ServerLevel w&&(!isHyenaPack()||!HyenaPacks.get(w).leaderDead(packId));}
        public boolean requiresUpdateEveryTick(){return true;}
        public void tick(){
            if((tickCount+getId())%20!=0||!(level() instanceof ServerLevel w))return;
            Vec3 p=isHyenaPack()?HyenaPacks.get(w).roamPoint(SavannaAnimal.this,w):JackalPacks.get(w).roamPoint(SavannaAnimal.this,w);
            if(p!=null)getNavigation().moveTo(p.x,p.y,p.z,1.05);
        }
        public void stop(){getNavigation().stop();}
    }
    @Override public void die(DamageSource source){
        super.die(source);
        if(isEncounterMember()&&level() instanceof ServerLevel w&&packId!=null&&isDeadOrDying()){if(isHyenaPack())HyenaPacks.get(w).died(this,w);else JackalPacks.get(w).died(this,w);}
    }
    @Override public void remove(RemovalReason reason){
        if(isEncounterMember()&&level() instanceof ServerLevel w&&packId!=null){if(isHyenaPack())HyenaPacks.get(w).removed(this,w);else JackalPacks.get(w).removed(this,w);}
        super.remove(reason);
    }
    private final class StrikeGoal extends Goal{
        StrikeGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        public boolean canUse(){return valid(getTarget());}
        public boolean requiresUpdateEveryTick(){return true;}
        public void stop(){entityData.set(STRIKE,0);entityData.set(CHARGE,false);heavyStrike=false;chargeHit=false;getNavigation().stop();}
        public void tick(){
            LivingEntity target=getTarget();if(!valid(target)||isHyenaPack()&&!packTargetAllowed(target))return;
            if(isHyenaPack()&&roarTicks()>0){getNavigation().stop();return;}
            getLookControl().setLookAt(target,30,30);
            double reach=(getBbWidth()+target.getBbWidth())*.5+1.0;
            int t=strikeTicks();
            if(t>0){
                getNavigation().stop();
                if(charging()){
                    if(t>3&&!chargeHit){
                        double distance=distanceToSqr(target);
                        if(distance<reach*reach&&hasLineOfSight(target)){
                            chargeHit=true;
                            if(doHurtTarget(target))target.knockback(1.5,getX()-target.getX(),getZ()-target.getZ());
                        }else if(onGround()&&!horizontalCollision&&hasLineOfSight(target)){
                            Vec3 direction=target.position().subtract(position()).multiply(1,0,1).normalize();
                            setDeltaMovement(direction.x*.7,getDeltaMovement().y,direction.z*.7);
                        }
                    }
                }else if(t==strikeLength()/2&&distanceToSqr(target)<reach*reach&&hasLineOfSight(target)){
                    boolean hit=doHurtTarget(target);
                    if(hit&&isHyenaPack()&&heavyStrike)target.knockback(hyenaRole==HyenaRole.LEADER?1.5:0.85,getX()-target.getX(),getZ()-target.getZ());
                }
                entityData.set(STRIKE,t-1);
                if(t==1){cooldown=isHyenaPack()?(hyenaBoosted()?12:28):packBoss?(packEnraged()?10:25):kind==Kind.AUROCHS?35:25;heavyStrike=false;entityData.set(CHARGE,false);chargeHit=false;}
                return;
            }
            if(distanceToSqr(target)<reach*reach&&cooldown==0&&hasLineOfSight(target)){
                heavyStrike=isHyenaPack()&&hyenaRole!=HyenaRole.RETINUE&&random.nextInt(hyenaBoosted()?2:4)==0;
                entityData.set(STRIKE,strikeLength());getNavigation().stop();return;
            }
            if(isEncounterMember()){
                double dist=distanceToSqr(target);
                double lungeRange=isHyenaPack()&&hyenaRole==HyenaRole.LEADER?49:16;
                if(cooldown==0&&onGround()&&dist>reach*reach&&dist<lungeRange&&hasLineOfSight(target)&&random.nextInt(isHyenaPack()&&hyenaBoosted()?3:5)==0){
                    Vec3 forward=target.position().subtract(position()).multiply(1,0,1).normalize();
                    heavyStrike=isHyenaPack()&&hyenaRole!=HyenaRole.RETINUE;
                    if(hyenaRole==HyenaRole.LEADER){entityData.set(CHARGE,true);chargeHit=false;}
                    setDeltaMovement(forward.scale(hyenaRole==HyenaRole.LEADER?.85:hyenaRole==HyenaRole.GUARD?.60:.48).add(0,.32,0));entityData.set(STRIKE,strikeLength());getNavigation().stop();return;
                }
                if(tickCount%5==0){
                    double chase=(isHyenaPack()?hyenaBoosted():packEnraged())?1.55:1.3;
                    if(dist<reach*reach){getNavigation().stop();}
                    else if(dist>25||tickCount%20==0)getNavigation().moveTo(target,chase);
                    else{
                        double angle=packSlot*2*Math.PI/(isHyenaPack()?HyenaPacks.SIZE:JackalPacks.SIZE);
                        double offset=isHyenaPack()?Math.max(1.6,getBbWidth()):1.6;
                        getNavigation().moveTo(target.getX()+Math.cos(angle)*offset,target.getY(),target.getZ()+Math.sin(angle)*offset,chase);
                    }
                }
            }else if(tickCount%5==0)getNavigation().moveTo(target,1.3);
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
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(groupId!=null)n.putUUID("SavannaGroup",groupId);if(packBoss&&packId!=null){n.putUUID("JackalPack",packId);n.putInt("JackalSlot",packSlot);}if(isHyenaPack()&&packId!=null){n.putUUID("HyenaPack",packId);n.putInt("HyenaSlot",packSlot);}}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);groupId=n.hasUUID("SavannaGroup")?n.getUUID("SavannaGroup"):null;entityData.set(STRIKE,0);if(packBoss&&n.hasUUID("JackalPack")){packId=n.getUUID("JackalPack");packSlot=n.getInt("JackalSlot");}if(isHyenaPack()&&n.hasUUID("HyenaPack")){packId=n.getUUID("HyenaPack");packSlot=n.getInt("HyenaSlot");}entityData.set(ROAR,0);entityData.set(CHARGE,false);}
}
