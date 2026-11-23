package org.slavicmyths.sisters;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CreatureHealthBalance;

/**
 * Gluheya: optional high-level winter encounter.
 * Bell control (phase 1) -> short daggers and inescapable four-blade pull (phase 2).
 * Not registered for natural spawn or world generation; test spawn egg only.
 */
public final class GluheyaBoss extends Monster {
    public static final int IDLE=0, FAST_WAVE=1, LONG_BELL=2, SILENCE_RING=3, SILENCE_FIELD=4,
        BELL_STORM=5, SHIFT=6, COMBO=7, DAGGER_THROW=8, BACKSTAB=9, HUNT=10, PULL=11;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(GluheyaBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRAME=SynchedEntityData.defineId(GluheyaBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SECOND=SynchedEntityData.defineId(GluheyaBoss.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ORBIT=SynchedEntityData.defineId(GluheyaBoss.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new org.slavicmyths.combat.CreatureBossBar(this,
        Component.literal("Глухея"),org.slavicmyths.combat.BossBarPalette.colorFor(getType(),BossEvent.BossBarColor.BLUE),
        BossEvent.BossBarOverlay.PROGRESS);
    private final Map<UUID,ArrayDeque<Long>> lodged=new HashMap<>();
    private final List<Shot> activeShots=new ArrayList<>();
    private int cooldown=80,sequence,teleportCooldown=180,huntCooldown=200,fieldCooldown=100;
    private int fieldTicks=0,comboHits=0;
    private Vec3 fieldCenter=Vec3.ZERO,stormCenter=Vec3.ZERO,lockedAim=Vec3.ZERO;
    private UUID pulledPlayer;
    private boolean summonedField;
    private static final class Shot {
        private Vec3 pos;
        private final Vec3 direction;
        private final UUID target;
        private int remaining=18;
        Shot(Vec3 pos,Vec3 direction,UUID target){this.pos=pos;this.direction=direction;this.target=target;}
    }

    public GluheyaBoss(EntityType<? extends GluheyaBoss> type,Level level) {
        super(type,level);
        setPersistenceRequired();
        setCustomName(Component.literal("Глухея"));
        setCustomNameVisible(false);
        setHealth(getMaxHealth());
        xpReward=0;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,1600)
            .add(Attributes.ATTACK_DAMAGE,16).add(Attributes.MOVEMENT_SPEED,.28)
            .add(Attributes.ARMOR,8).add(Attributes.FOLLOW_RANGE,50)
            .add(Attributes.KNOCKBACK_RESISTANCE,.50);
    }
    @Override public float getMaxHealth(){return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){
        super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(FRAME,0);b.define(SECOND,false);b.define(ORBIT,0);
    }
    public int action(){return entityData.get(ACTION);}
    public int actionFrame(){return entityData.get(FRAME);}
    public boolean secondPhase(){return entityData.get(SECOND);}
    public int orbitBlades(){return entityData.get(ORBIT);}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void onRemovedFromLevel(){bar.removeAllPlayers();super.onRemovedFromLevel();}

    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new Pursue());
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,32));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private boolean valid(LivingEntity p){
        return p!=null&&p.isAlive()&&org.slavicmyths.combat.CombatTargeting.canAttack(
            this,p,org.slavicmyths.combat.CombatTargeting.Context.ACQUIRE);
    }
    private boolean damage(LivingEntity p,float amount){
        return valid(p)&&p.hurt(damageSources().mobAttack(this),amount);
    }
    private void debuff(LivingEntity p,int ticks,boolean dark){
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,ticks,0));
        if(dark)p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,ticks,0));
    }
    private void nausea(LivingEntity p){
        var previous=p.getEffect(MobEffects.CONFUSION);
        int level=previous==null?0:1;
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,level==0?160:200,level));
    }
    private void start(int action,LivingEntity p){
        entityData.set(ACTION,action);entityData.set(FRAME,0);
        getNavigation().stop();
        if(p!=null)lockedAim=p.position();
        if(action==HUNT){entityData.set(ORBIT,7);lodged.clear();activeShots.clear();}
        if(action==BELL_STORM)stormCenter=lockedAim;
        if(action==COMBO)comboHits=0;
        playSound(SoundEvents.AMETHYST_BLOCK_CHIME,1.0F,.65F);
    }
    private void finish(){
        entityData.set(ACTION,IDLE);entityData.set(FRAME,0);entityData.set(ORBIT,0);
        cooldown=secondPhase()?42+random.nextInt(20):140+random.nextInt(60);
        pulledPlayer=null;
    }
    private int length(int action){return switch(action){
        case FAST_WAVE->35;case LONG_BELL->72;case SILENCE_RING->38;
        case SILENCE_FIELD->36;case BELL_STORM->220;case SHIFT->65;
        case COMBO->46;case DAGGER_THROW->34;case BACKSTAB->50;
        case HUNT->245;case PULL->55;default->0;
    };}
    private void wave(ServerLevel world,LivingEntity p,float amount,double width,double reach,boolean strong){
        Vec3 origin=position().add(0,1.8,0);
        Vec3 forward=lockedAim.add(0,1.0,0).subtract(origin);
        if(forward.lengthSqr()<.001)return;
        forward=forward.normalize();
        for(int j=0;j<=25;j++){
            double d=reach*j/25D;
            Vec3 pos=origin.add(forward.scale(d));
            world.sendParticles(ParticleTypes.SONIC_BOOM,pos.x,pos.y,pos.z,1,0,0,0,0);
        }
        if(p==null||!valid(p))return;
        Vec3 delta=p.position().add(0,.9,0).subtract(origin);
        double projected=delta.dot(forward);
        if(projected<0||projected>reach||delta.subtract(forward.scale(projected)).length()>width)return;
        if(damage(p,amount)){
            if(strong){
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200,0));
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,200,0));
                debuff(p,100,false);
            }else nausea(p);
        }
    }
    private void ring(ServerLevel world,Vec3 center,double radius,float damage){
        for(int i=0;i<32;i++){
            double angle=i*Math.PI/16D;
            world.sendParticles(ParticleTypes.SONIC_BOOM,center.x+radius*Math.cos(angle),
                center.y+1,center.z+radius*Math.sin(angle),1,0,0,0,0);
        }
        for(Player p:world.getEntitiesOfClass(Player.class,new AABB(center,center).inflate(radius+2,4,radius+2))){
            if(!valid(p)||p.position().distanceToSqr(center)>radius*radius)continue;
            if(damage(p,damage)){debuff(p,100,true);p.push(p.getX()-center.x,.3,p.getZ()-center.z);}
        }
    }
    private Vec3 bellLocation(int i){
        int count=i<10?10:18;int local=i<10?i:i-10;
        double r=i<10?16:30,angle=2*Math.PI*local/count+(i<10?0:.14);
        return stormCenter.add(Math.cos(angle)*r,0,Math.sin(angle)*r);
    }
    private void storm(ServerLevel world,int frame){
        if(frame==1)playSound(SoundEvents.BELL_BLOCK,1.2F,.65F);
        for(int i=0;i<28;i++){
            Vec3 center=bellLocation(i);
            if(!world.hasChunkAt(BlockPos.containing(center)))continue;
            int wave=i%4;
            if(frame<50||((frame-50)/15)%4!=wave){
                if(frame%10==0)world.sendParticles(ParticleTypes.END_ROD,center.x,center.y+1.8,center.z,2,.2,.3,.2,.01);
                continue;
            }
            if(frame%15!=0)continue;
            world.sendParticles(ParticleTypes.SONIC_BOOM,center.x,center.y+1.5,center.z,1,0,0,0,0);
            for(Player p:world.getEntitiesOfClass(Player.class,new AABB(center,center).inflate(3,3,3))){
                if(!valid(p)||p.position().distanceToSqr(center)>9)continue;
                if(damage(p,9)){p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,100,0));nausea(p);}
            }
        }
    }
    private boolean teleportNear(ServerLevel world,LivingEntity p,boolean back){
        if(!valid(p))return false;
        double angle=back?Math.toRadians(p.getYRot())+Math.PI:random.nextDouble()*Math.PI*2;
        for(int k=0;k<8;k++){
            double a=angle+k*Math.PI/4,dist=back?2.9:6.0+random.nextDouble()*6.0;
            double x=p.getX()+Math.sin(a)*dist,z=p.getZ()-Math.cos(a)*dist,y=p.getY();
            if(!world.hasChunkAt(BlockPos.containing(x,y,z)))continue;
            Vec3 old=position();
            teleportTo(x,y,z);
            if(world.noCollision(this)){world.sendParticles(ParticleTypes.PORTAL,x,y+1.5,z,20,.5,1,.5,.05);return true;}
            teleportTo(old.x,old.y,old.z);
        }
        return false;
    }
    private void launchBlade(ServerLevel world,LivingEntity target){
        if(!valid(target))return;
        Vec3 from=position().add(0,2.5,0);
        Vec3 aim=target.getEyePosition().subtract(from);
        if(aim.lengthSqr()<.001)return;
        activeShots.add(new Shot(from,aim.normalize(),target.getUUID()));
        world.sendParticles(ParticleTypes.SNOWFLAKE,from.x,from.y,from.z,9,.25,.25,.25,.08);
    }
    private void updateShots(ServerLevel world){
        Iterator<Shot> it=activeShots.iterator();
        while(it.hasNext()){
            Shot s=it.next();
            Vec3 next=s.pos.add(s.direction.scale(2.5));
            HitResult block=world.clip(new ClipContext(s.pos,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
            if(block.getType()!=HitResult.Type.MISS||--s.remaining<=0){it.remove();continue;}
            world.sendParticles(ParticleTypes.SNOWFLAKE,next.x,next.y,next.z,4,.12,.12,.12,.02);
            var entity=world.getEntity(s.target);
            if(entity instanceof LivingEntity p&&valid(p)&&p.getBoundingBox().inflate(.42).clip(s.pos,next).isPresent()){
                boolean hit=damage(p,5);
                if(hit&&action()==HUNT){
                    long now=world.getGameTime();
                    var hits=lodged.computeIfAbsent(p.getUUID(),key->new ArrayDeque<>());
                    while(!hits.isEmpty()&&now-hits.peekFirst()>240)hits.removeFirst();
                    hits.addLast(now);
                    if(hits.size()>=4){
                        pulledPlayer=p.getUUID();
                        entityData.set(ACTION,PULL);
                        entityData.set(FRAME,0);
                        entityData.set(ORBIT,0);
                        activeShots.clear();
                        huntCooldown=800;
                        break;
                    }
                }
                it.remove();
            }else s.pos=next;
        }
    }
    private void actionStep(ServerLevel world,LivingEntity p){
        int act=action(),f=actionFrame()+1;entityData.set(FRAME,f);
        switch(act){
            case FAST_WAVE->{if(f==17)wave(world,p,8,1.8,25,false);}
            case LONG_BELL->{if(f==1)playSound(SoundEvents.BELL_BLOCK,1.4F,.55F);
                if(f==40)wave(world,p,20,2.1,28,true);}
            case SILENCE_RING->{if(f==24)ring(world,position(),10,14);}
            case SILENCE_FIELD->{if(f==24){fieldCenter=position();fieldTicks=240;summonedField=true;}}
            case BELL_STORM->{storm(world,f);if(f==1){setInvisible(true);}if(f==210)setInvisible(false);}
            case SHIFT->{if(f==1){entityData.set(SECOND,true);var a=getAttribute(Attributes.ARMOR);
                if(a!=null)a.setBaseValue(10);var speed=getAttribute(Attributes.MOVEMENT_SPEED);
                if(speed!=null)speed.setBaseValue(.37);}
                if(f==30)playSound(SoundEvents.WITHER_SPAWN,1.4F,1.2F);}
            case COMBO->{if(f==16||f==26||f==38){
                if(p!=null&&distanceToSqr(p)<16&&damage(p,f==38?9:5))comboHits++;
                if(f==38&&comboHits==3&&p!=null){p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,120,0));
                    p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,120,0));}
            }}
            case DAGGER_THROW->{if(f==16||f==25)launchBlade(world,p);}
            case BACKSTAB->{if(f==16)teleportNear(world,p,true);
                if(f==31&&p!=null&&distanceToSqr(p)<21)damage(p,14);}
            case HUNT->{if(f>=20&&(f-20)%30==0&&f<=200&&p!=null){
                    launchBlade(world,p);entityData.set(ORBIT,Math.max(0,7-(f-20)/30-1));}}
            case PULL->{if(f==22&&pulledPlayer!=null){
                    var victim=world.getEntity(pulledPlayer);
                    if(victim instanceof LivingEntity v&&valid(v)){
                        // Once four blades lodge, there is no interruptible escape step.
                        Vec3 dest=position().add(getLookAngle().multiply(2.4,0,2.4));
                        if(world.hasChunkAt(BlockPos.containing(dest)))v.teleportTo(dest.x,v.getY(),dest.z);
                    }}
                if(f==36&&pulledPlayer!=null){
                    var victim=world.getEntity(pulledPlayer);
                    if(victim instanceof LivingEntity v&&valid(v)&&distanceToSqr(v)<36){
                        damage(v,32);
                        lodged.remove(v.getUUID());
                        world.sendParticles(ParticleTypes.SONIC_BOOM,v.getX(),v.getY()+1,v.getZ(),1,0,0,0,0);
                    }}}
            default->{}
        }
        if(f>=length(act))finish();
    }
    private void field(ServerLevel world){
        if(fieldTicks<=0)return;
        fieldTicks--;
        if(fieldTicks==0){summonedField=false;return;}
        if(fieldTicks%20!=0)return;
        for(int i=0;i<24;i++){
            double a=i*Math.PI/12D;
            world.sendParticles(ParticleTypes.SNOWFLAKE,fieldCenter.x+Math.cos(a)*50,fieldCenter.y+.7,
                fieldCenter.z+Math.sin(a)*50,1,0,.2,0,0);
        }
        int passed=240-fieldTicks;
        for(Player p:world.getEntitiesOfClass(Player.class,new AABB(fieldCenter,fieldCenter).inflate(50,8,50))){
            if(valid(p)&&p.position().distanceToSqr(fieldCenter)<=2500&&passed>=100){
                p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,45,0));
            }
        }
    }
    @Override public boolean hurt(DamageSource source,float amount){
        boolean hit=super.hurt(source,amount);
        if(hit&&source.getEntity() instanceof LivingEntity attacker&&valid(attacker)&&!secondPhase()){
            setTarget(attacker);
            if(distanceToSqr(attacker)>100&&teleportCooldown>60)teleportCooldown=60;
        }
        return hit;
    }
    @Override protected void customServerAiStep(){
        super.customServerAiStep();
        if(!(level() instanceof ServerLevel world)||!isAlive())return;
        bar.setProgress(Math.clamp(getHealth()/Math.max(1F,getMaxHealth()),0F,1F));
        updateShots(world);field(world);
        if(teleportCooldown>0)teleportCooldown--;
        if(huntCooldown>0)huntCooldown--;
        if(fieldCooldown>0)fieldCooldown--;
        LivingEntity p=valid(getTarget())?getTarget():null;
        if(!secondPhase()&&getHealth()<=getMaxHealth()*.5F&&action()!=SHIFT){
            setInvisible(false);start(SHIFT,p);return;
        }
        if(action()!=IDLE){
            if(p!=null)getLookControl().setLookAt(p,30,30);
            actionStep(world,p);return;
        }
        if(p==null)return;
        getLookControl().setLookAt(p,30,30);
        if(teleportCooldown<=0){
            teleportNear(world,p,secondPhase());
            teleportCooldown=secondPhase()?80+random.nextInt(40):200+random.nextInt(80);
        }
        if(cooldown>0){cooldown--;return;}
        if(!secondPhase()){
            int choice=sequence++%5;
            if(fieldCooldown<=0){start(SILENCE_FIELD,p);fieldCooldown=360;return;}
            start(switch(choice){case 0->FAST_WAVE;case 1->LONG_BELL;
                case 2->SILENCE_RING;case 3->BELL_STORM;default->FAST_WAVE;},p);
        }else if(huntCooldown<=0){start(HUNT,p);huntCooldown=800;}
        else{start(switch(sequence++%4){case 0->COMBO;case 1->DAGGER_THROW;
            case 2->BACKSTAB;default->COMBO;},p);}
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        tag.putBoolean("GluheyaSecond",secondPhase());
        tag.putInt("GluheyaHuntCooldown",huntCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        boolean second=tag.getBoolean("GluheyaSecond");
        entityData.set(SECOND,second);
        var armor=getAttribute(Attributes.ARMOR);
        if(armor!=null)armor.setBaseValue(second?10:8);
        var speed=getAttribute(Attributes.MOVEMENT_SPEED);
        if(speed!=null)speed.setBaseValue(second?.37:.28);
        huntCooldown=Math.max(0,tag.getInt("GluheyaHuntCooldown"));
        setInvisible(false);entityData.set(ACTION,IDLE);entityData.set(FRAME,0);entityData.set(ORBIT,0);
    }
    private final class Pursue extends Goal {
        Pursue(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return valid(getTarget());}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            var target=getTarget();if(!valid(target))return;
            if(action()!=IDLE){getNavigation().stop();return;}
            double d=distanceToSqr(target);
            if(secondPhase()){if(d>9)getNavigation().moveTo(target,1.23);}
            else if(d<100)getNavigation().stop();
            else if(d>625)getNavigation().moveTo(target,.9);
        }
    }
}
