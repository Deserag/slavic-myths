package org.slavicmyths.bandit;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.*;

/** Player-sized humans. Goals own their attack cadence; no global tactical manager. */
public final class BanditEntity extends Monster {
    public static final int FIGHTER=0,ARCHER=1,HEAVY=2,SENIOR=3,ATAMAN=4;
    private static final EntityDataAccessor<Integer> FACE=SynchedEntityData.defineId(BanditEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DUTY=SynchedEntityData.defineId(BanditEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(BanditEntity.class,EntityDataSerializers.INT);
    public final int role;public UUID camp;public int campMember=-1,campTotal;public BlockPos home;
    public int zone; public LivingEntity suspect; public long suspectSince; private boolean directEngagement;
    public int duty(){return entityData.get(DUTY);}
    public void duty(int value){entityData.set(DUTY,value);}
    public void ambientAction(int value){action(value);}
    public void engage(LivingEntity target){directEngagement=true;setTarget(target);directEngagement=false;}
    private long commandUntil,shieldUntil;private int commandCooldown=100;
    public BanditEntity(EntityType<? extends BanditEntity> type,Level world,int role){super(type,world);this.role=role;if(getNavigation() instanceof net.minecraft.world.entity.ai.navigation.GroundPathNavigation)((net.minecraft.world.entity.ai.navigation.GroundPathNavigation)getNavigation()).setCanOpenDoors(true);xpReward=role==ATAMAN?18:5;}
    public static AttributeSupplier.Builder attributes(int role){return createMonsterAttributes().add(Attributes.MAX_HEALTH,role==ATAMAN?56:role==HEAVY?30:role==SENIOR?25:20).add(Attributes.MOVEMENT_SPEED,role==HEAVY?.23:role==ATAMAN?.27:.28).add(Attributes.ATTACK_DAMAGE,2).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,role==HEAVY?.35:role==ATAMAN?.3:0);}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(DUTY,0);builder.define(FACE,0);builder.define(ACTION,0);}
    public int face(){return entityData.get(FACE);}
    public int action(){return entityData.get(ACTION);}
    private void action(int state){entityData.set(ACTION,state);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(0,new OpenDoorGoal(this,true));goalSelector.addGoal(1,new CombatGoal());
        goalSelector.addGoal(2,new StrongholdGoal(this));
        goalSelector.addGoal(3,new MoveTowardsRestrictionGoal(this,.85));goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.65));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public boolean isAlliedTo(Entity other){return other instanceof BanditEntity||other instanceof NightingaleEntity||super.isAlliedTo(other);}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){
        SpawnGroupData result=super.finalizeSpawn(world,difficulty,reason,data);entityData.set(FACE,random.nextInt(4));
        Item[] arms={Items.IRON_SWORD,Items.IRON_AXE,ModItems.WOODEN_MACE.get(),ModItems.STONE_MACE.get(),ModItems.MACE.get()};
        setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(role==ARCHER?Items.BOW:role==ATAMAN?ModItems.MACE.get():role==HEAVY?(random.nextBoolean()?ModItems.MACE.get():Items.IRON_AXE):role==SENIOR?(random.nextBoolean()?Items.IRON_SWORD:ModItems.MACE.get()):arms[random.nextInt(arms.length)]));
        if(role==ATAMAN||role==HEAVY&&random.nextInt(4)!=0)setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
        if(role!=ARCHER)setItemSlot(EquipmentSlot.CHEST,new ItemStack(role==HEAVY||role==ATAMAN?ModItems.PLATE_CUIRASS.get():role==SENIOR?Items.CHAINMAIL_CHESTPLATE:ModItems.GAMBESON.get()));
        for(EquipmentSlot slot:EquipmentSlot.values())setDropChance(slot,.025F);
        return result;
    }
    @Override public boolean removeWhenFarAway(double distance){return camp==null;}
    @Override public boolean hurt(DamageSource source,float amount){
        if(source.getEntity() instanceof BanditEntity)return false;
        if(!level().isClientSide&&campTotal==StrongholdRecords.TOTAL&&source.getEntity() instanceof LivingEntity)engage((LivingEntity)source.getEntity());
        if(isBlocking()&&source.getDirectEntity() instanceof LivingEntity&&((LivingEntity)source.getDirectEntity()).getMainHandItem().getItem() instanceof AxeItem){stopUsingItem();shieldUntil=level().getGameTime()+100;action(0);}
        return super.hurt(source,amount);
    }
    @Override public void setTarget(LivingEntity target){if(target!=null&&!level().isClientSide&&campTotal==StrongholdRecords.TOTAL&&camp!=null&&!directEngagement){StrongholdRecords records=StrongholdRecords.get((ServerLevel)level());if(records.state((ServerLevel)level(),camp,zone)!=2){if(!records.record(camp).cleared){if(suspect==null)suspectSince=level().getGameTime();suspect=target;records.suspect(camp,zone);}return;}}boolean alert=getTarget()==null&&target!=null;super.setTarget(target);if(alert&&!level().isClientSide)playSound(ModSounds.BANDIT_ALERT.get(),.7F,.92F+face()*.04F);}
    @Override protected SoundEvent getAmbientSound(){return ModSounds.BANDIT_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource s){return ModSounds.BANDIT_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModSounds.BANDIT_DEATH.get();}
    @Override public int getAmbientSoundInterval(){return 420;}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("CampZone",zone);tag.putInt("CampDuty",duty());tag.putInt("HumanFace",face());tag.putLong("ShieldReady",shieldUntil);if(camp!=null){tag.putUUID("BanditCamp",camp);tag.putInt("CampMember",campMember);tag.putInt("CampTotal",campTotal);}if(home!=null)tag.putLong("BanditHome",home.asLong());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);zone=Math.max(0,Math.min(4,tag.getInt("CampZone")));duty(tag.getInt("CampDuty"));entityData.set(FACE,Math.floorMod(tag.getInt("HumanFace"),4));shieldUntil=tag.getLong("ShieldReady");if(tag.hasUUID("BanditCamp")){camp=tag.getUUID("BanditCamp");campMember=tag.getInt("CampMember");campTotal=tag.getInt("CampTotal");}if(tag.contains("BanditHome")){home=BlockPos.of(tag.getLong("BanditHome"));restrictTo(home,32);}}
    @Override public void die(DamageSource source){
        if(!level().isClientSide){
            if(camp!=null){if(campTotal==StrongholdRecords.TOTAL)StrongholdRecords.get((ServerLevel)level()).killed(this,source.getEntity());else CampRecords.get((ServerLevel)level()).killed(this,source.getEntity());}
            if(role==ATAMAN){if(source.getEntity() instanceof Player)org.slavicmyths.progression.Knowledge.award((Player)source.getEntity(),"without_ataman");
                for(BanditEntity b:level().getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(24),e->Objects.equals(e.camp,camp)))b.commandUntil=0;}
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
            double distance=distanceToSqr(target);boolean sight=hasLineOfSight(target);if(cooldown>0)cooldown--;if(repath>0)repath--;
            if(role==ATAMAN&&--commandCooldown<=0){commandCooldown=180;playSound(ModSounds.BANDIT_COMMAND.get(),.8F,1);
                for(BanditEntity b:level().getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(20),e->e!=BanditEntity.this&&Objects.equals(camp,e.camp)&&(campTotal!=StrongholdRecords.TOTAL||e.zone==zone))){b.commandUntil=level().getGameTime()+160;if(b.getTarget()==null)b.setTarget(target);}if(timer==0)action(6);}
            if(role==ARCHER){ranged(target,distance,sight);return;}
            if(timer>0) {
                getNavigation().stop();timer--;
                if(action()==4){if(timer==0){stopUsingItem();action(3);cooldown=8;}return;}
                if(timer==0){stopUsingItem();swing(InteractionHand.MAIN_HAND,true);
                    if(distance<=7.3&&sight){
                        AttributeModifier power=new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","548bd780-d3a7-428d-8bb0-ecf053af0800"),3,AttributeModifier.Operation.ADD_VALUE);
                        boolean hit;
                        if(heavy)getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(power);
                        try {hit=doHurtTarget(target);} finally {if(heavy)getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(power);}
                        if(target instanceof Player&&target.isBlocking()&&getMainHandItem().getItem() instanceof AxeItem)((Player)target).disableShield();
                        if(hit&&heavy){Vec3 d=target.position().subtract(position());target.knockback(.8F,-d.x,-d.z);}
                    }
                    playSound(ModSounds.BANDIT_ATTACK.get(),.65F,heavy?.8F:1);action(3);cooldown=heavy?30:getMainHandItem().getItem() instanceof SwordItem?15:25;
                }return;
            }
            if(cooldown>0){getNavigation().stop();return;}
            if(distance<7.3&&sight){getNavigation().stop();
                if(getOffhandItem().getItem() instanceof ShieldItem&&level().getGameTime()>shieldUntil&&random.nextInt(3)==0){startUsingItem(InteractionHand.OFF_HAND);action(4);timer=22;return;}
                heavy=role==ATAMAN&&random.nextInt(3)==0;timer=heavy?24:getMainHandItem().getItem() instanceof SwordItem?7:15;action(heavy?7:1);
                if(heavy)playSound(ModSounds.BANDIT_HEAVY.get(),.8F,.9F);
            }else if(repath==0){repath=commandUntil>level().getGameTime()?12:20;Vec3 d=position().subtract(target.position()).normalize();double side=(getId()%3-1)*.9;
                getNavigation().moveTo(target.getX()+d.x*1.6+d.z*side,target.getY(),target.getZ()+d.z*1.6-d.x*side,1);action(0);}
        }
        private void ranged(LivingEntity target,double distance,boolean sight){
            if(distance<64){stopUsingItem();shots=0;action(0);if(repath==0){repath=20;Vec3 away=position().subtract(target.position()).normalize();getNavigation().moveTo(getX()+away.x*5,getY(),getZ()+away.z*5,1.15);}return;}
            if(distance>196||!sight){stopUsingItem();shots=0;action(0);if(repath==0){repath=20;getNavigation().moveTo(target,.9);}return;}
            getNavigation().stop();if(cooldown>0)return;if(!isUsingItem()){startUsingItem(InteractionHand.MAIN_HAND);action(5);shots=0;}
            if(++shots>=24){Arrow arrow=new Arrow(level(),BanditEntity.this,new ItemStack(Items.ARROW),getMainHandItem());double x=target.getX()-getX(),z=target.getZ()-getZ();arrow.setBaseDamage(2);arrow.shoot(x,target.getY(.5)-arrow.getY()+Math.sqrt(x*x+z*z)*.18,z,1.6F,6);level().addFreshEntity(arrow);playSound(SoundEvents.SKELETON_SHOOT,.65F,1);stopUsingItem();action(3);cooldown=35;shots=0;}
        }
    }
}
