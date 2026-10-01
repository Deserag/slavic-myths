package org.slavicmyths.bandit;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.registry.*;

/** Player-sized humans. Goals own their attack cadence; no global tactical manager. */
public final class BanditEntity extends MonsterEntity {
    public static final int FIGHTER=0,ARCHER=1,HEAVY=2,SENIOR=3,ATAMAN=4;
    private static final DataParameter<Integer> FACE=EntityDataManager.defineId(BanditEntity.class,DataSerializers.INT);
    private static final DataParameter<Integer> DUTY=EntityDataManager.defineId(BanditEntity.class,DataSerializers.INT);
    private static final DataParameter<Integer> ACTION=EntityDataManager.defineId(BanditEntity.class,DataSerializers.INT);
    public final int role;public UUID camp;public int campMember=-1,campTotal;public BlockPos home;
    public int zone; public LivingEntity suspect; public long suspectSince; private boolean directEngagement;
    public int duty(){return entityData.get(DUTY);}
    public void duty(int value){entityData.set(DUTY,value);}
    public void ambientAction(int value){action(value);}
    public void engage(LivingEntity target){directEngagement=true;setTarget(target);directEngagement=false;}
    private long commandUntil,shieldUntil;private int commandCooldown=100;
    public BanditEntity(EntityType<? extends BanditEntity> type,World world,int role){super(type,world);this.role=role;if(getNavigation() instanceof net.minecraft.pathfinding.GroundPathNavigator)((net.minecraft.pathfinding.GroundPathNavigator)getNavigation()).setCanOpenDoors(true);xpReward=role==ATAMAN?18:5;}
    public static AttributeModifierMap.MutableAttribute attributes(int role){return createMonsterAttributes().add(Attributes.MAX_HEALTH,role==ATAMAN?56:role==HEAVY?30:role==SENIOR?25:20).add(Attributes.MOVEMENT_SPEED,role==HEAVY?.23:role==ATAMAN?.27:.28).add(Attributes.ATTACK_DAMAGE,2).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,role==HEAVY?.35:role==ATAMAN?.3:0);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(DUTY,0);entityData.define(FACE,0);entityData.define(ACTION,0);}
    public int face(){return entityData.get(FACE);}
    public int action(){return entityData.get(ACTION);}
    private void action(int state){entityData.set(ACTION,state);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(0,new OpenDoorGoal(this,true));goalSelector.addGoal(1,new CombatGoal());
        goalSelector.addGoal(2,new StrongholdGoal(this));
        goalSelector.addGoal(3,new MoveTowardsRestrictionGoal(this,.85));goalSelector.addGoal(5,new WaterAvoidingRandomWalkingGoal(this,.65));
        goalSelector.addGoal(6,new LookAtGoal(this,PlayerEntity.class,8));goalSelector.addGoal(7,new LookRandomlyGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,PlayerEntity.class,true));
    }
    @Override public boolean isAlliedTo(Entity other){return other instanceof BanditEntity||super.isAlliedTo(other);}
    @Override public ILivingEntityData finalizeSpawn(IServerWorld world,DifficultyInstance difficulty,SpawnReason reason,ILivingEntityData data,CompoundNBT tag){
        ILivingEntityData result=super.finalizeSpawn(world,difficulty,reason,data,tag);entityData.set(FACE,random.nextInt(4));
        Item[] arms={Items.IRON_SWORD,Items.IRON_AXE,ModItems.WOODEN_MACE.get(),ModItems.STONE_MACE.get(),ModItems.MACE.get()};
        setItemSlot(EquipmentSlotType.MAINHAND,new ItemStack(role==ARCHER?Items.BOW:role==ATAMAN?ModItems.MACE.get():role==HEAVY?(random.nextBoolean()?ModItems.MACE.get():Items.IRON_AXE):role==SENIOR?(random.nextBoolean()?Items.IRON_SWORD:ModItems.MACE.get()):arms[random.nextInt(arms.length)]));
        if(role==ATAMAN||role==HEAVY&&random.nextInt(4)!=0)setItemSlot(EquipmentSlotType.OFFHAND,new ItemStack(Items.SHIELD));
        if(role!=ARCHER)setItemSlot(EquipmentSlotType.CHEST,new ItemStack(role==HEAVY||role==ATAMAN?ModItems.PLATE_CUIRASS.get():role==SENIOR?Items.CHAINMAIL_CHESTPLATE:ModItems.GAMBESON.get()));
        for(EquipmentSlotType slot:EquipmentSlotType.values())setDropChance(slot,.025F);
        return result;
    }
    @Override public boolean removeWhenFarAway(double distance){return camp==null;}
    @Override public boolean hurt(DamageSource source,float amount){
        if(source.getEntity() instanceof BanditEntity)return false;
        if(!level.isClientSide&&campTotal==StrongholdRecords.TOTAL&&source.getEntity() instanceof LivingEntity)engage((LivingEntity)source.getEntity());
        if(isBlocking()&&source.getDirectEntity() instanceof LivingEntity&&((LivingEntity)source.getDirectEntity()).getMainHandItem().getItem() instanceof AxeItem){stopUsingItem();shieldUntil=level.getGameTime()+100;action(0);}
        return super.hurt(source,amount);
    }
    @Override public void setTarget(LivingEntity target){if(target!=null&&!level.isClientSide&&campTotal==StrongholdRecords.TOTAL&&camp!=null&&!directEngagement){StrongholdRecords records=StrongholdRecords.get((ServerWorld)level);if(records.state((ServerWorld)level,camp,zone)!=2){if(!records.record(camp).cleared){if(suspect==null)suspectSince=level.getGameTime();suspect=target;records.suspect(camp,zone);}return;}}boolean alert=getTarget()==null&&target!=null;super.setTarget(target);if(alert&&!level.isClientSide)playSound(ModSounds.BANDIT_ALERT.get(),.7F,.92F+face()*.04F);}
    @Override protected SoundEvent getAmbientSound(){return ModSounds.BANDIT_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource s){return ModSounds.BANDIT_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModSounds.BANDIT_DEATH.get();}
    @Override public int getAmbientSoundInterval(){return 420;}
    @Override public void addAdditionalSaveData(CompoundNBT tag){super.addAdditionalSaveData(tag);tag.putInt("CampZone",zone);tag.putInt("CampDuty",duty());tag.putInt("HumanFace",face());tag.putLong("ShieldReady",shieldUntil);if(camp!=null){tag.putUUID("BanditCamp",camp);tag.putInt("CampMember",campMember);tag.putInt("CampTotal",campTotal);}if(home!=null)tag.putLong("BanditHome",home.asLong());}
    @Override public void readAdditionalSaveData(CompoundNBT tag){super.readAdditionalSaveData(tag);zone=Math.max(0,Math.min(4,tag.getInt("CampZone")));duty(tag.getInt("CampDuty"));entityData.set(FACE,Math.floorMod(tag.getInt("HumanFace"),4));shieldUntil=tag.getLong("ShieldReady");if(tag.hasUUID("BanditCamp")){camp=tag.getUUID("BanditCamp");campMember=tag.getInt("CampMember");campTotal=tag.getInt("CampTotal");}if(tag.contains("BanditHome")){home=BlockPos.of(tag.getLong("BanditHome"));restrictTo(home,32);}}
    @Override public void die(DamageSource source){
        if(!level.isClientSide){
            if(camp!=null){if(campTotal==StrongholdRecords.TOTAL)StrongholdRecords.get((ServerWorld)level).killed(this,source.getEntity());else CampRecords.get((ServerWorld)level).killed(this,source.getEntity());}
            if(role==ATAMAN){if(source.getEntity() instanceof PlayerEntity)org.slavicmyths.progression.Knowledge.award((PlayerEntity)source.getEntity(),"without_ataman");
                for(BanditEntity b:level.getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(24),e->Objects.equals(e.camp,camp)))b.commandUntil=0;}
        }super.die(source);
    }
    private final class CombatGoal extends Goal {
        private int timer,cooldown,repath,shots;private boolean heavy;
        CombatGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        public boolean canUse(){return getTarget()!=null&&getTarget().isAlive();}
        public boolean canContinueToUse(){return canUse()&&(home==null||getTarget().blockPosition().distSqr(home)<48*48);}
        public void start(){timer=0;cooldown=10;}
        public void stop(){action(0);stopUsingItem();getNavigation().stop();setTarget(null);}
        public void tick(){
            LivingEntity target=getTarget();if(target==null)return;getLookControl().setLookAt(target,30,30);
            double distance=distanceToSqr(target);boolean sight=canSee(target);if(cooldown>0)cooldown--;if(repath>0)repath--;
            if(role==ATAMAN&&--commandCooldown<=0){commandCooldown=180;playSound(ModSounds.BANDIT_COMMAND.get(),.8F,1);
                for(BanditEntity b:level.getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(20),e->e!=BanditEntity.this&&Objects.equals(camp,e.camp)&&(campTotal!=StrongholdRecords.TOTAL||e.zone==zone))){b.commandUntil=level.getGameTime()+160;if(b.getTarget()==null)b.setTarget(target);}if(timer==0)action(6);}
            if(role==ARCHER){ranged(target,distance,sight);return;}
            if(timer>0) {
                getNavigation().stop();timer--;
                if(action()==4){if(timer==0){stopUsingItem();action(3);cooldown=8;}return;}
                if(timer==0){stopUsingItem();swing(Hand.MAIN_HAND,true);
                    if(distance<=7.3&&sight){
                        AttributeModifier power=new AttributeModifier(UUID.fromString("548bd780-d3a7-428d-8bb0-ecf053af0800"),"Ataman heavy strike",3,AttributeModifier.Operation.ADDITION);
                        boolean hit;
                        if(heavy)getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(power);
                        try {hit=doHurtTarget(target);} finally {if(heavy)getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(power);}
                        if(target instanceof PlayerEntity&&target.isBlocking()&&getMainHandItem().getItem() instanceof AxeItem)((PlayerEntity)target).disableShield(true);
                        if(hit&&heavy){Vector3d d=target.position().subtract(position());target.knockback(.8F,-d.x,-d.z);}
                    }
                    playSound(ModSounds.BANDIT_ATTACK.get(),.65F,heavy?.8F:1);action(3);cooldown=heavy?30:getMainHandItem().getItem() instanceof SwordItem?15:25;
                }return;
            }
            if(cooldown>0){getNavigation().stop();return;}
            if(distance<7.3&&sight){getNavigation().stop();
                if(getOffhandItem().getItem() instanceof ShieldItem&&level.getGameTime()>shieldUntil&&random.nextInt(3)==0){startUsingItem(Hand.OFF_HAND);action(4);timer=22;return;}
                heavy=role==ATAMAN&&random.nextInt(3)==0;timer=heavy?24:getMainHandItem().getItem() instanceof SwordItem?7:15;action(heavy?7:1);
                if(heavy)playSound(ModSounds.BANDIT_HEAVY.get(),.8F,.9F);
            }else if(repath==0){repath=commandUntil>level.getGameTime()?12:20;Vector3d d=position().subtract(target.position()).normalize();double side=(getId()%3-1)*.9;
                getNavigation().moveTo(target.getX()+d.x*1.6+d.z*side,target.getY(),target.getZ()+d.z*1.6-d.x*side,1);action(0);}
        }
        private void ranged(LivingEntity target,double distance,boolean sight){
            if(distance<64){stopUsingItem();shots=0;action(0);if(repath==0){repath=20;Vector3d away=position().subtract(target.position()).normalize();getNavigation().moveTo(getX()+away.x*5,getY(),getZ()+away.z*5,1.15);}return;}
            if(distance>196||!sight){stopUsingItem();shots=0;action(0);if(repath==0){repath=20;getNavigation().moveTo(target,.9);}return;}
            getNavigation().stop();if(cooldown>0)return;if(!isUsingItem()){startUsingItem(Hand.MAIN_HAND);action(5);shots=0;}
            if(++shots>=24){ArrowEntity arrow=new ArrowEntity(level,BanditEntity.this);double x=target.getX()-getX(),z=target.getZ()-getZ();arrow.setBaseDamage(2);arrow.shoot(x,target.getY(.5)-arrow.getY()+Math.sqrt(x*x+z*z)*.18,z,1.6F,6);level.addFreshEntity(arrow);playSound(SoundEvents.SKELETON_SHOOT,.65F,1);stopUsingItem();action(3);cooldown=35;shots=0;}
        }
    }
}
