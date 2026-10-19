package org.slavicmyths.bandit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.chat.Component;

import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.*;
import org.slavicmyths.registry.ModSounds;
import org.slavicmyths.progression.Knowledge;

/** Finite encounter controller. Attacks lock their direction before release. */
public final class NightingaleEntity extends PathfinderMob implements net.minecraft.world.entity.monster.Enemy {
    public static final int IDLE=0,NOTICE=1,QUICK=2,HEAVY=3,SHOVE=4,RETREAT=5,INHALE=6,DEEP_INHALE=7,RADIAL=8,EXHALE=9,DEEP_EXHALE=10,DYING=11,QUICK_RELEASE=12,HEAVY_RELEASE=13,SHOVE_RELEASE=14;
    public enum Phase { PHASE_ONE, TRANSFORMING, PHASE_TWO }
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.INT);
    public Phase phase(){return Phase.values()[Math.max(0,Math.min(2,entityData.get(PHASE)))];}
    public static final int COMBO=15,LEAP_ATTACK=16,WHIRLWIND=17,DASH=18,WAVE=19,ONSLAUGHT=20,TRANSFORM=21;
    public static final int BREAK_AWAY=22,AIR_DIVE=23;
    private int closePressure,meleeChoices;
    private Vec3 divePoint=Vec3.ZERO,airAim=Vec3.ZERO;
    private boolean diveReleased;
    private double hoverHeight=4.5;
    private final Map<Integer,Integer> attackCooldowns=new HashMap<>();
    private record Threat(float damage,int time) {}
    private final Map<UUID,Threat> threats=new LinkedHashMap<>();
    private int actionTick,targetLock;
    private Vec3 actionOrigin=Vec3.ZERO;
    private boolean leapImpactReleased,dashImpactReleased;
    private double leapDistance,dashDistance;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DRAWN=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> IMPACT=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> START=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.nightingale"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> participants=new HashSet<>();
    public UUID camp;public BlockPos home;
    private int remaining,recovery,deathTicks;
    private boolean engaged,lootReleased;private DamageSource fatal;
    private Vec3 aim=new Vec3(0,0,1);
    public NightingaleEntity(EntityType<? extends NightingaleEntity> type,Level world){super(type,world);fatal=damageSources().generic();setPersistenceRequired();xpReward=50;getNavigation().setCanFloat(true);}
    @Override protected EntityDimensions getDefaultDimensions(Pose pose){return EntityDimensions.scalable(.95F,1.85F).withEyeHeight(1.25F);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,350).add(Attributes.MOVEMENT_SPEED,.265).add(Attributes.ATTACK_DAMAGE,10).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.65);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(PHASE,0);builder.define(ACTION,0);builder.define(START,0);builder.define(DRAWN,false);builder.define(IMPACT,0F);}
    public boolean drawn(){return entityData.get(DRAWN);}
    public float impact(){return entityData.get(IMPACT);}
    public int action(){return entityData.get(ACTION);}
    public float elapsed(float partial){return (int)level().getGameTime()-entityData.get(START)+partial;}
    private void begin(int action,int ticks){entityData.set(ACTION,action);entityData.set(START,(int)level().getGameTime());remaining=ticks;getNavigation().stop();
        LivingEntity t=getTarget();if(t!=null){Vec3 d=t.position().subtract(position());aim=new Vec3(d.x,0,d.z).normalize();setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));yBodyRot=getYRot();yHeadRot=getYRot();}
        if(action==INHALE||action==RADIAL)playSound(ModSounds.boss("nightingale_phase1_whistle_charge"),1,.95F);
        if(action==DEEP_INHALE)playSound(ModSounds.NIGHTINGALE_DEEP_INHALE.get(),1.4F,1);
        if(action==HEAVY)playSound(ModSounds.NIGHTINGALE_BREATH.get(),.8F,.9F);
    }
    private boolean valid(LivingEntity e){return e!=null&&e!=this&&e.isAlive()&&!e.isRemoved()&&e.level()==level()&&!isAlliedTo(e)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()))&&distanceToSqr(e)<48*48&&(home==null||e.blockPosition().distSqr(home)<48*48);}
    private double threatScore(LivingEntity e){Threat t=threats.get(e.getUUID());return (e instanceof Player?300:8)+(t==null?0:t.damage()*2*Math.max(0,1-(tickCount-t.time())/200.0))+Math.max(0,12-distanceTo(e));}
    private void selectTarget(){
        if(tickCount%10!=0&&(getTarget()==null||valid(getTarget())))return;
        threats.values().removeIf(t->tickCount-t.time()>=200);
        LivingEntity current=getTarget(),best=valid(current)?current:null;
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(32),e->valid(e)&&(BossTargets.automatic(e)||threats.containsKey(e.getUUID()))))
            if(hasLineOfSight(e)&&(best==null||threatScore(e)>threatScore(best)))best=e;
        if(best!=current&&(!valid(current)||tickCount>=targetLock)) {setTarget(best);targetLock=tickCount+30;}
    }
    private void startAttack(int a,int duration,int cooldown){begin(a,duration);if(a==DASH)playSound(ModSounds.boss("nightingale_phase2_dash"),1,1);actionTick=0;leapImpactReleased=false;dashImpactReleased=false;actionOrigin=position();leapDistance=getTarget()==null?0:Math.min(8,distanceTo(getTarget()));dashDistance=getTarget()==null?0:Math.min(10,distanceTo(getTarget())+1.5);attackCooldowns.put(a,cooldown);}
    private void transformIfNeeded(){
        if(phase()==Phase.PHASE_ONE&&getHealth()<=getMaxHealth()*.5F){
            entityData.set(PHASE,Phase.TRANSFORMING.ordinal());startAttack(TRANSFORM,40,0);
            playSound(ModSounds.boss("nightingale_phase1_whistle_charge"),1,1.2F);
        }
    }
    @Override public void aiStep(){super.aiStep();if(level().isClientSide||!isAlive())return;if(home==null)home=blockPosition();
        attackCooldowns.replaceAll((a,t)->Math.max(0,t-1));selectTarget();transformIfNeeded();
        boolean second=phase()==Phase.PHASE_TWO;
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.265*(second?1.2:1));getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(second?12:10);
        if(tickCount%20==0){
            bar.setName(Component.translatable(second?"entity.slavicmyths.nightingale.enraged":"entity.slavicmyths.nightingale"));
            bar.setProgress(Math.max(0,getHealth()/getMaxHealth()));
            for(ServerPlayer p:new ArrayList<>(bar.getPlayers()))if(!valid(p)||distanceToSqr(p)>36*36)bar.removePlayer(p);
            if(getTarget()!=null)for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(36),this::valid)){
                participants.add(p.getUUID());Knowledge.award(p,"meet_nightingale");if(p instanceof ServerPlayer sp)bar.addPlayer(sp);
            }
        }
        if(phase()==Phase.TRANSFORMING){getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);actionTick++;
            if(actionTick%8==0)WindAttack.particles((ServerLevel)level(),position().add(0,1,0),aim,actionTick/8.0,true);
            if(actionTick>=40){entityData.set(PHASE,Phase.PHASE_TWO.ordinal());begin(IDLE,0);remaining=0;recovery=12;playSound(ModSounds.boss("nightingale_phase2_laugh"),1,1);}
            return;
        }
        LivingEntity target=getTarget();
        boolean close=valid(target)&&target instanceof Player&&position().subtract(target.position()).multiply(1,0,1).lengthSqr()<=9&&Math.abs(getY()-target.getY())<=2.5;
        closePressure=close?Math.min(100,closePressure+1):0;
        if(!second&&closePressure>=100&&ready(BREAK_AWAY)){closePressure=0;startAttack(BREAK_AWAY,24,120);}
        updateFlight(target);
        if(remaining>0){getNavigation().stop();actionTick++;attackTick(target);if(--remaining==0){begin(IDLE,0);recovery=second?10:16;}return;}
        if(recovery>0){recovery--;return;}
        if(!valid(target)){entityData.set(DRAWN,false);engaged=false;bar.removeAllPlayers();if(tickCount%20==0&&distanceToSqr(Vec3.atCenterOf(home))>16)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.8);return;}
        if(!engaged){engaged=true;entityData.set(DRAWN,true);playSound(ModSounds.boss("nightingale_phase1_aggro"),1,1);if(camp!=null)for(BanditEntity b:level().getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(22),b->camp.equals(b.camp)))b.engage(target);}
        // Preserve descent from the camp's raised platform, without teleporting or scanning structures.
        if(!second&&getY()>target.getY()+3&&getY()>=home.getY()-1&&Math.abs(getX()-home.getX())<9&&Math.abs(getZ()-home.getZ())<9){
            if(tickCount%10==0)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+4.5,1);
            if(onGround()&&getNavigation().isDone())setDeltaMovement(0,getDeltaMovement().y,.15);return;
        }
        getLookControl().setLookAt(target,25,25);double distance=distanceTo(target);
        if(!hasLineOfSight(target)){if(tickCount%10==0)getNavigation().moveTo(target,1);return;}
        if(second){
            getNavigation().stop();
            if(getY()<target.getY()+Math.min(3.5,hoverHeight-.3))return; // Finish takeoff, or use the room beneath a ceiling.
            if(distance<=12&&ready(WAVE)){startAttack(WAVE,30,100);airAim=target.getBoundingBox().getCenter().subtract(getEyePosition()).normalize();return;}
            if(hoverHeight>=2.5&&distance<=12&&ready(AIR_DIVE)){startAttack(AIR_DIVE,52,140);diveReleased=false;divePoint=target.position().add(0,.1,0);return;}
            if(hoverHeight<2.5&&distance<=6&&ready(WHIRLWIND)){startAttack(WHIRLWIND,30,160);return;}
            return;
        }else{
            if(distance>=5&&distance<=8&&ready(LEAP_ATTACK)&&onGround()){startAttack(LEAP_ATTACK,32,220);playSound(ModSounds.boss("nightingale_phase1_leap"),1,1);return;}
            if(distance<=10&&ready(INHALE)){startAttack(INHALE,28,140);return;}
            if(distance<=2.35&&ready(COMBO)&&++meleeChoices%3==0){startAttack(COMBO,36,180);return;}
        }
        if(distance<=2.2&&ready(QUICK)){startAttack(QUICK,20,70);return;}
        if(tickCount%10==0)getNavigation().moveTo(target,second?1.15:1);
    }
    private boolean ready(int action){return attackCooldowns.getOrDefault(action,0)==0;}
    private void updateFlight(LivingEntity target){
        boolean flying=phase()==Phase.PHASE_TWO&&valid(target);
        setNoGravity(flying);if(!flying)return;
        getNavigation().stop();fallDistance=0;
        if(tickCount%10==0){
            Vec3 from=position().add(0,getBbHeight()+.05,0),to=new Vec3(getX(),Math.max(from.y,target.getY()+6.5),getZ());
            var ceiling=level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
            hoverHeight=ceiling.getType()==HitResult.Type.BLOCK?Mth.clamp(ceiling.getLocation().y-target.getY()-getBbHeight()-.15,0,4.5):4.5;
        }
        if(action()==AIR_DIVE&&actionTick>=10&&actionTick<36&&!diveReleased)return;
        Vec3 offset=position().subtract(target.position()).multiply(1,0,1);
        Vec3 radial=offset.lengthSqr()<.01?new Vec3(1,0,0):offset.normalize();
        Vec3 wanted=target.position().add(radial.scale(4)).add(0,hoverHeight,0).subtract(position());
        Vec3 velocity=new Vec3(Mth.clamp(wanted.x*.12,-.32,.32),Mth.clamp(wanted.y*.22,-.4,.45),Mth.clamp(wanted.z*.12,-.32,.32));
        // Native movement clips against blocks; do not force chunks or teleport through ceilings.
        if(!level().noCollision(this,getBoundingBox().move(velocity)))velocity=new Vec3(0,velocity.y>0?0:velocity.y,0);
        setDeltaMovement(velocity);hasImpulse=true;
    }
    private void airWhistle(){
        ServerLevel w=(ServerLevel)level();Vec3 mouth=getEyePosition();
        for(LivingEntity victim:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(12),this::valid)){
            Vec3 delta=victim.getBoundingBox().getCenter().subtract(mouth);
            if(delta.lengthSqr()<=144&&airAim.dot(delta.normalize())>=.78&&hasLineOfSight(victim)){
                victim.hurt(damageSources().mobAttack(this),14);
                WindImpulse.push(victim,victim.position().subtract(position()),1.2F);
            }
        }
        for(int i=1;i<=10;i++){Vec3 at=mouth.add(airAim.scale(i));w.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,at.x,at.y,at.z,2,.12,.12,.12,.01);}
    }
    private boolean melee(LivingEntity target,float damage,double range){
        if(valid(target)&&distanceTo(target)<=range&&hasLineOfSight(target)&&WindAttack.inCone(target.position().subtract(position()),aim,range,false)){
            if(action()!=QUICK&&hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS))damage=Math.max(0,damage-4*(getEffect(net.minecraft.world.effect.MobEffects.WEAKNESS).getAmplifier()+1));
            boolean hit=target.hurt(damageSources().mobAttack(this),damage);if(hit){target.knockback(.35,getX()-target.getX(),getZ()-target.getZ());target.hurtMarked=true;}return hit;
        }return false;
    }
    private void wave(float damage,double range,boolean radial){
        WindAttack.release(this,aim,range,damage,2.2F,radial,false);for(int radius=1;radius<=range;radius+=2)WindAttack.particles((ServerLevel)level(),position().add(0,1,0),aim,radius,radial);
    }
    private void attackTick(LivingEntity target){int a=action(),t=actionTick;boolean second=phase()==Phase.PHASE_TWO;
        if((a==INHALE||a==WAVE||a==WHIRLWIND)&&t<16&&t%4==0)WindAttack.particles((ServerLevel)level(),getEyePosition(),aim,.5,false);
        if(a==QUICK&&t==10){melee(target,(float)getAttributeValue(Attributes.ATTACK_DAMAGE),2.2);playSound(ModSounds.boss(second?"nightingale_phase2_slash":"nightingale_phase1_slash"),.9F,second?1.3F:1);}
        if(a==COMBO&&(t==8||t==20||t==32)){melee(target,t==32?7:4,2.35);playSound(ModSounds.boss("nightingale_phase1_slash"),.9F,1.2F);}
        if(a==INHALE&&t==16){wave(14,10,false);playSound(ModSounds.boss("nightingale_phase1_whistle_release"),1.2F,1);}
        if(a==WHIRLWIND&&t==16){wave(12,6,true);playSound(ModSounds.boss("nightingale_phase2_whirlwind"),1.2F,1);}
        if(a==WAVE&&t==16){if(second)airWhistle();else wave(14,12,false);playSound(ModSounds.boss("nightingale_phase2_wave"),1.2F,1);}
        if(a==BREAK_AWAY){
            if(t==8){WindAttack.release(this,aim,5,6,3.8F,true,false);playSound(ModSounds.boss("nightingale_phase1_whistle_release"),1.2F,1.3F);}
            if(t>=8&&t<=18){Vec3 retreat=aim.scale(-.6);if(level().noCollision(this,getBoundingBox().move(retreat))){setDeltaMovement(retreat.x,getDeltaMovement().y,retreat.z);hasImpulse=true;}}
        }
        if(a==AIR_DIVE){
            if(t==10){if(valid(target))divePoint=target.position().add(0,.1,0);playSound(ModSounds.boss("nightingale_phase2_dash"),1,1.2F);}
            if(t>=10&&t<36&&!diveReleased){
                Vec3 delta=divePoint.subtract(position());double length=delta.length();
                setDeltaMovement(length<.01?Vec3.ZERO:delta.scale(Math.min(.85,length)/length));hasImpulse=true;
                if(t>=12&&(position().distanceToSqr(divePoint)<1.6||onGround()||horizontalCollision)){
                    diveReleased=true;fallDistance=0;
                    for(LivingEntity victim:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2.2),this::valid))if(distanceTo(victim)<=2.2&&Math.abs(getY()-victim.getY())<1.8&&hasLineOfSight(victim))victim.hurt(damageSources().mobAttack(this),16);
                    playSound(ModSounds.boss("nightingale_phase2_slash"),1,1);setDeltaMovement(0,.45,0);
                }
            }
            if(t>=36)diveReleased=true;
        }
        if(a==LEAP_ATTACK){
            if(t==10){setDeltaMovement(aim.scale(.5).add(0,.55,0));hasImpulse=true;}
            if(t>10&&!onGround()){
                boolean forward=t<=30&&!horizontalCollision&&position().subtract(actionOrigin).multiply(1,0,1).lengthSqr()<leapDistance*leapDistance;
                setDeltaMovement(forward?aim.x*.45:0,getDeltaMovement().y,forward?aim.z*.45:0);hasImpulse=true;
            }
            if(t>=12&&onGround()&&!leapImpactReleased){
                leapImpactReleased=true;playSound(ModSounds.boss("nightingale.phase1.land"),.8F,1);
                for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2),e->valid(e)&&(BossTargets.automatic(e)||threats.containsKey(e.getUUID()))))if(distanceTo(e)<=2&&hasLineOfSight(e))e.hurt(damageSources().mobAttack(this),12);
            }
            fallDistance=0;
        }
        if(a==DASH||a==ONSLAUGHT){
            if(t>=8&&t<=18){
                boolean forward=!horizontalCollision&&position().subtract(actionOrigin).multiply(1,0,1).lengthSqr()<dashDistance*dashDistance;
                setDeltaMovement(forward?aim.x*.8:0,getDeltaMovement().y,forward?aim.z*.8:0);hasImpulse=true;
                if(a==DASH&&!dashImpactReleased&&melee(target,12,3))dashImpactReleased=true;
            }
            if(t==19)setDeltaMovement(0,getDeltaMovement().y,0);
            if(a==ONSLAUGHT&&t==20&&valid(target)){
                aim=target.position().subtract(position()).multiply(1,0,1).normalize();
                setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));yBodyRot=getYRot();yHeadRot=getYRot();
            }
            if(a==ONSLAUGHT&&(t==22||t==34))melee(target,4,3.2);
            if(a==ONSLAUGHT&&t==46){wave(10,10,false);playSound(ModSounds.boss("nightingale_phase2_wave"),1.1F,1.1F);}
        }
    }
    @Override public boolean hurt(DamageSource source,float amount){
        if(phase()==Phase.TRANSFORMING)amount*=.25F;
        boolean hit=super.hurt(source,amount);
        if(hit&&!level().isClientSide){entityData.set(IMPACT,Math.min(1,amount/12));
            if(isAlive()&&source.getEntity() instanceof LivingEntity attacker&&valid(attacker)){
                Threat old=threats.get(attacker.getUUID());threats.put(attacker.getUUID(),new Threat(Math.min(100,amount+(old==null?0:old.damage())),tickCount));
                if(threats.size()>32)threats.remove(threats.keySet().iterator().next());if(!valid(getTarget())){setTarget(attacker);targetLock=tickCount+30;}
                if(attacker instanceof Player)participants.add(attacker.getUUID());
            }
            if(isAlive())transformIfNeeded();
        }return hit;
    }
    @Override public boolean isAlliedTo(Entity other){return other instanceof BanditEntity||other instanceof NightingaleEntity||super.isAlliedTo(other);}
    @Override public boolean canChangeDimensions(net.minecraft.world.level.Level from,net.minecraft.world.level.Level to){return false;}
    @Override public int getMaxFallDistance(){return 9;}
    @Override public boolean causeFallDamage(float distance,float multiplier,net.minecraft.world.damagesource.DamageSource source){return phase()==Phase.PHASE_TWO?false:super.causeFallDamage(Math.max(0,distance-7),multiplier,source);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public int getAmbientSoundInterval(){return 600;}
    @Override protected SoundEvent getAmbientSound(){return action()!=IDLE||getTarget()!=null?null:phase()==Phase.PHASE_TWO?ModSounds.boss("nightingale_phase2_laugh"):ModSounds.boss("nightingale_phase1_idle_whistle");}
    @Override protected SoundEvent getHurtSound(DamageSource s){return ModSounds.boss(phase()==Phase.PHASE_TWO?"nightingale_phase2_hurt":"nightingale_phase1_hurt");}
    @Override protected SoundEvent getDeathSound(){return ModSounds.boss(phase()==Phase.PHASE_TWO?"nightingale_phase2_death":"nightingale_phase1_death");}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void remove(Entity.RemovalReason reason){bar.removeAllPlayers();super.remove(reason);}
    @Override public void onRemovedFromLevel(){bar.removeAllPlayers();super.onRemovedFromLevel();}
    @Override public void die(DamageSource source){
        boolean wasDead=dead;super.die(source);
        // Forge death cancellation must not resolve the camp or award a victory.
        if(!level().isClientSide&&!wasDead&&dead){
            fatal=source;if(source.getEntity() instanceof Player)participants.add(source.getEntity().getUUID());
            setNoGravity(false);fallDistance=0;begin(DYING,0);bar.removeAllPlayers();
            if(camp!=null)StrongholdRecords.get((ServerLevel)level()).nightingaleDied(camp);
            for(UUID id:participants){ServerPlayer p=((ServerLevel)level()).getServer().getPlayerList().getPlayer(id);if(p!=null&&p.level()==level()&&distanceToSqr(p)<64*64){Knowledge.award(p,"meet_nightingale");Knowledge.award(p,"defeat_nightingale");}}
        }
    }
    @Override protected void dropAllDeathLoot(ServerLevel world,DamageSource source){fatal=source;} // deferred until the collapse
    @Override protected void tickDeath(){deathTicks++;deathTime=deathTicks;
        if(!level().isClientSide&&deathTicks==32)WindAttack.particles((ServerLevel)level(),position().add(0,.5,0),aim,2,true);
        if(!level().isClientSide&&deathTicks>=55){if(!lootReleased){lootReleased=true;super.dropAllDeathLoot((ServerLevel)level(),fatal);}discard();}
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);org.slavicmyths.combat.CreatureHealthBalance.mark(n);if(camp!=null)n.putUUID("NightingaleCamp",camp);if(home!=null)n.putLong("NightingaleHome",home.asLong());n.putString("NightingalePhase",phase().name());CompoundTag cds=new CompoundTag();attackCooldowns.forEach((a,t)->cds.putInt(Integer.toString(a),t));n.put("NightingaleCooldowns",cds);n.putBoolean("NightingaleLoot",lootReleased);n.putInt("CollapseTicks",deathTicks);ListTag list=new ListTag();for(UUID id:participants)list.add(StringTag.valueOf(id.toString()));n.put("Witnesses",list);}
    @Override public void readAdditionalSaveData(CompoundTag n){n=org.slavicmyths.combat.CreatureHealthBalance.prepare(n,350,250,false);super.readAdditionalSaveData(n);camp=n.hasUUID("NightingaleCamp")?n.getUUID("NightingaleCamp"):null;home=n.contains("NightingaleHome")?BlockPos.of(n.getLong("NightingaleHome")):null;lootReleased=n.getBoolean("NightingaleLoot");deathTicks=n.getInt("CollapseTicks");for(Tag v:n.getList("Witnesses",8))try{participants.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}entityData.set(PHASE,getHealth()<=getMaxHealth()*.5F||n.getString("NightingalePhase").equals("PHASE_TWO")||n.getString("NightingalePhase").equals("TRANSFORMING")?2:0);attackCooldowns.clear();CompoundTag cds=n.getCompound("NightingaleCooldowns");for(String key:cds.getAllKeys())try{attackCooldowns.put(Integer.parseInt(key),Math.max(0,Math.min(440,cds.getInt(key))));}catch(NumberFormatException ignored){}begin(getHealth()<=0?DYING:IDLE,0);if(getHealth()<=0)entityData.set(START,(int)level().getGameTime()-deathTicks);recovery=40;}
}
