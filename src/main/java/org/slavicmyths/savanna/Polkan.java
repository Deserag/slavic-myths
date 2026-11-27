package org.slavicmyths.savanna;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CreatureHealthBalance;
import org.slavicmyths.registry.ModEntities;
public final class Polkan extends PathfinderMob {
    public static final int HERD_SIZE=8,MAX_HP=1400;
    public static final int IDLE=0,THRUST=1,SWEEP=2,CHARGE=3,BOW=4,VOLLEY=5,RUNE_BOW=6,THROW=7,STOMP=8,KICK=9,COMMAND=10,GUARD=11;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(Polkan.class,EntityDataSerializers.INT),AGE=SynchedEntityData.defineId(Polkan.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(Polkan.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RUN=SynchedEntityData.defineId(Polkan.class,EntityDataSerializers.BOOLEAN),SPEAR_OUT=SynchedEntityData.defineId(Polkan.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar=new ServerBossEvent(Component.translatableWithFallback("entity.slavicmyths.polkan","Полкан"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
    private final Map<UUID,Long> threats=new HashMap<>();
    private final long[] ready=new long[12];
    private final Set<UUID> chargeHits=new HashSet<>();
    private boolean herdCreated;
    private UUID spearId,actionTarget;
    private int throwsDone,spearWait,globalDelay;
    private long nextRoam,commandUntil;
    private Vec3 chargeDirection=Vec3.ZERO;
    public Polkan(EntityType<? extends Polkan> t,Level w){super(t,w);setPersistenceRequired();xpReward=0;}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,MAX_HP).add(Attributes.ARMOR,18).add(Attributes.ARMOR_TOUGHNESS,6).add(Attributes.KNOCKBACK_RESISTANCE,.9).add(Attributes.ATTACK_DAMAGE,26).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,48);}
    @Override public float getMaxHealth(){return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
    @Override public double getAttributeValue(Holder<Attribute> a){return a.equals(Attributes.MAX_HEALTH)?CreatureHealthBalance.uncappedMaximum(getAttribute(a)):super.getAttributeValue(a);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(AGE,0);b.define(PHASE,1);b.define(RUN,false);b.define(SPEAR_OUT,false);}
    public int action(){return entityData.get(ACTION);} public int actionAge(){return entityData.get(AGE);} public int phase(){return entityData.get(PHASE);}
    public boolean running(){return entityData.get(RUN);} public boolean spearOut(){return entityData.get(SPEAR_OUT);}
    @Override public int getMaxSpawnClusterSize(){return 1;}
    @Override public Component getName(){return hasCustomName()?super.getName():Component.translatableWithFallback(getType().getDescriptionId(),"Полкан");}
    public boolean spawnHerd(ServerLevel w,ServerPlayer summoner){
        if(herdCreated||PolkanHerd.get(w).exists(getUUID())||!w.canSeeSky(blockPosition())||!w.noCollision(this))return false;
        if(summoner==null&&!w.getEntitiesOfClass(Polkan.class,getBoundingBox().inflate(128),p->p!=this&&p.isAlive()).isEmpty())return false;
        List<SavannaAnimal> guards=new ArrayList<>();
        for(int i=0;i<HERD_SIZE;i++){
            SavannaAnimal a=ModEntities.POLKAN_TARPAN.get().create(w);if(a==null)return false;boolean found=false;
            for(int attempt=0;attempt<7&&!found;attempt++){
                double angle=i*Math.PI/4+attempt*.2,r=4+attempt*1.5;
                BlockPos col=BlockPos.containing(getX()+Math.cos(angle)*r,getY(),getZ()+Math.sin(angle)*r);
                if(!w.hasChunkAt(col))continue;BlockPos p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,col);
                if(Math.abs(p.getY()-getY())>4||!w.canSeeSky(p)||!w.getFluidState(p).isEmpty()||!w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP)||(summoner!=null&&!w.mayInteract(summoner,p)))continue;
                a.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,getYRot(),0);
                if(!w.noCollision(a)||a.getBoundingBox().intersects(getBoundingBox())||guards.stream().anyMatch(g->g.getBoundingBox().intersects(a.getBoundingBox())))continue;found=true;
            }
            if(!found)return false;a.polkanOwner=getUUID();a.polkanSlot=i;a.setHealth(50);guards.add(a);
        }
        List<SavannaAnimal> added=new ArrayList<>();for(SavannaAnimal a:guards){if(!w.addFreshEntity(a)){added.forEach(Entity::discard);return false;}added.add(a);}
        PolkanHerd.get(w).register(getUUID(),guards);herdCreated=true;setHealth(MAX_HP);return true;
    }
    private boolean valid(LivingEntity x){return x!=null&&x!=this&&x.isAlive()&&x.level()==level()&&!(x instanceof Polkan)&&!(x instanceof SavannaAnimal a&&a.polkanGuard())&&(!(x instanceof Player p)||!p.isCreative()&&!p.isSpectator())&&!isAlliedTo(x)&&canAttack(x);}
    public void provoke(LivingEntity x){if(valid(x)){threats.put(x.getUUID(),level().getGameTime()+600);if(getTarget()==null)setTarget(x);}}
    public boolean hostileTo(LivingEntity x){return valid(x)&&threats.getOrDefault(x.getUUID(),-1L)>=level().getGameTime();}
    @Override public boolean hurt(DamageSource s,float amount){
        if(s.getDirectEntity() instanceof PolkanMissile m&&m.getOwner()==this)return false;
        if(s.getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow)amount*=.85F;
        boolean hit=super.hurt(s,amount);if(hit&&!level().isClientSide&&s.getEntity() instanceof LivingEntity a)provoke(a);return hit;
    }
    private List<LivingEntity> candidates(ServerLevel w){
        threats.entrySet().removeIf(e->e.getValue()<w.getGameTime());List<LivingEntity> xs=new ArrayList<>();
        for(UUID id:threats.keySet())if(w.getEntity(id) instanceof LivingEntity x&&valid(x)&&distanceToSqr(x)<=64*64)xs.add(x);
        xs.sort(Comparator.comparingDouble((LivingEntity x)->distanceToSqr(x)).thenComparing(Entity::getUUID));return xs;
    }
    @Override public void tick(){
        super.tick();if(!(level() instanceof ServerLevel w)||!isAlive())return;
        if(!herdCreated){if(PolkanHerd.get(w).exists(getUUID()))herdCreated=true;else if(!spawnHerd(w,null)){discard();return;}}
        bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));
        if(tickCount%20==0){Set<ServerPlayer> viewers=new HashSet<>();for(ServerPlayer p:w.players())if(!p.isSpectator()&&distanceToSqr(p)<=48*48)viewers.add(p);for(ServerPlayer p:List.copyOf(bar.getPlayers()))if(!viewers.contains(p))bar.removePlayer(p);for(ServerPlayer p:viewers)if(!bar.getPlayers().contains(p))bar.addPlayer(p);}
        int phase=getHealth()<=490||PolkanHerd.get(w).losses(getUUID())>=6?3:getHealth()<=980?2:1;entityData.set(PHASE,phase);
        if(globalDelay>0)globalDelay--;
        List<LivingEntity> xs=candidates(w);LivingEntity target=getTarget();if(!xs.contains(target)){target=xs.isEmpty()?null:xs.get(0);setTarget(target);}
        if(action()!=IDLE){LivingEntity locked=actionTarget!=null&&w.getEntity(actionTarget) instanceof LivingEntity l&&hostileTo(l)?l:target;advance(w,locked,xs);return;}
        if(target==null){entityData.set(RUN,false);if(w.getGameTime()>=nextRoam){nextRoam=w.getGameTime()+160;for(int i=0;i<8;i++){BlockPos p=blockPosition().offset(random.nextInt(31)-15,0,random.nextInt(31)-15);if(!w.hasChunkAt(p))continue;p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p);if(Math.abs(p.getY()-getY())<=4&&w.canSeeSky(p)&&w.getFluidState(p).isEmpty()){getNavigation().moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,1);break;}}}return;}
        double d=distanceTo(target);
        if(globalDelay==0){
            if(phase>=2&&d>8&&d<38&&hasLineOfSight(target)&&available(w,THROW)){begin(w,THROW,target,800);return;}
            if(phase>=2&&xs.stream().filter(x->distanceToSqr(x)<25).count()>=2&&available(w,STOMP)){begin(w,STOMP,target,300);return;}
            if(d<=5&&forward().dot(flat(target.position().subtract(position())))<-.4&&available(w,KICK)){begin(w,KICK,target,120);return;}
            if(d<=5&&hasLineOfSight(target)){int a=available(w,SWEEP)&&random.nextBoolean()?SWEEP:THRUST;if(available(w,a)){begin(w,a,target,a==SWEEP?200:45);return;}}
            if(phase>=2&&d>=7&&d<=22&&onGround()&&hasLineOfSight(target)&&available(w,CHARGE)){begin(w,CHARGE,target,240);return;}
            if(phase>=2&&available(w,COMMAND)&&d<24&&PolkanHerd.get(w).losses(getUUID())<HERD_SIZE){begin(w,COMMAND,target,500);return;}
            if(phase>=2&&d<12&&available(w,GUARD)&&PolkanHerd.get(w).losses(getUUID())<6){begin(w,GUARD,target,400);return;}
            if(d>5&&d<=48&&hasLineOfSight(target)){
                if(available(w,RUNE_BOW)&&phase>=2){begin(w,RUNE_BOW,target,320);return;}
                if(available(w,VOLLEY)&&phase>=2){begin(w,VOLLEY,target,160);return;}
                if(available(w,BOW)){begin(w,BOW,target,55);return;}
            }
        }
        getLookControl().setLookAt(target,30,30);entityData.set(RUN,true);if(tickCount%5==0)getNavigation().moveTo(target,phase==3?2.3:2.0);
    }
    private boolean available(ServerLevel w,int a){return w.getGameTime()>=ready[a];}
    private void begin(ServerLevel w,int a,LivingEntity t,int cooldown){
        ready[a]=w.getGameTime()+(a==THROW?cooldown:phase()==3?(int)(cooldown*.75):cooldown);entityData.set(ACTION,a);entityData.set(AGE,0);actionTarget=t.getUUID();getNavigation().stop();entityData.set(RUN,a==CHARGE);chargeHits.clear();throwsDone=0;spearWait=0;
        if(a!=KICK)face(t);
        if(a==CHARGE)chargeDirection=flat(t.position().subtract(position()));
        playSound(a==COMMAND?SoundEvents.HORSE_ANGRY:SoundEvents.PLAYER_ATTACK_SWEEP,1.2F,.8F);
    }
    private void end(){if(action()==THROW)ready[THROW]=level().getGameTime()+800;entityData.set(ACTION,IDLE);entityData.set(AGE,0);entityData.set(RUN,false);actionTarget=null;getNavigation().stop();globalDelay=12;}
    private Vec3 forward(){double a=Math.toRadians(getYRot());return new Vec3(-Math.sin(a),0,Math.cos(a));}
    private void face(LivingEntity x){Vec3 d=x.position().subtract(position());float y=(float)Math.toDegrees(Math.atan2(-d.x,d.z));setYRot(y);yBodyRot=y;setYHeadRot(y);}
    static Vec3 flat(Vec3 d){return d.multiply(1,0,1).normalize();}
    private void hit(LivingEntity x,float damage,int slow,int weakness,double kb){
        if(!hostileTo(x)||!hasLineOfSight(x))return;
        if(x.hurt(damageSources().mobAttack(this),damage*(phase()==3?1.2F:1F))){
            if(kb>0)x.knockback(kb,getX()-x.getX(),getZ()-x.getZ());
            if(slow>0)x.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,slow,1));
            if(weakness>0)x.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,weakness,0));
        }
    }
    private void area(List<LivingEntity> xs,double r,double minDot,float damage,int slow,int weak,double kb){for(LivingEntity x:xs)if(distanceToSqr(x)<=r*r&&forward().dot(flat(x.position().subtract(position())))>=minDot)hit(x,damage,slow,weak,kb);}
    private void advance(ServerLevel w,LivingEntity t,List<LivingEntity> xs){
        int a=action(),n=actionAge()+1;entityData.set(AGE,n);if(t==null&&a!=THROW){end();return;}
        if(t!=null&&a!=CHARGE&&a!=KICK){face(t);getLookControl().setLookAt(t,45,30);}
        switch(a){
            case THRUST->{if(n==12&&distanceTo(t)<=5)hit(t,26,0,0,.7);if(n>=24)end();}
            case SWEEP->{if(n==16)area(xs,5,0,32,0,0,1.5);if(n>=30)end();}
            case STOMP->{if(n==23){area(xs,5,-1,30,0,100,1);w.sendParticles(ParticleTypes.CLOUD,getX(),getY()+.2,getZ(),45,2,.15,2,.04);}if(n>=36)end();}
            case KICK->{if(n==12)for(LivingEntity x:xs)if(distanceToSqr(x)<=16&&forward().dot(flat(x.position().subtract(position())))<-.25)hit(x,24,0,0,1.7);if(n>=25)end();}
            case CHARGE->{if(n>=12&&n<=35){if(horizontalCollision||!onGround()){end();return;}setDeltaMovement(chargeDirection.x*.76,getDeltaMovement().y,chargeDirection.z*.76);for(LivingEntity x:xs)if(distanceToSqr(x)<16&&chargeDirection.dot(flat(x.position().subtract(position())))>-.1&&chargeHits.add(x.getUUID()))hit(x,40,60,0,1.7);}if(n>=43)end();}
            case BOW,VOLLEY,RUNE_BOW->{if(phase()>=2&&n%5==1&&distanceTo(t)<18){Vec3 away=flat(position().subtract(t.position()));Vec3 side=new Vec3(-away.z,0,away.x);getNavigation().moveTo(getX()+away.x*5+side.x*3,getY(),getZ()+away.z*5+side.z*3,1.45);entityData.set(RUN,true);}if(n==22||(a==VOLLEY&&(n==39||n==56)))launch(w,t,a==RUNE_BOW?24:16,false);if(n>=(a==VOLLEY?68:34))end();}
            case THROW->{if(spearId!=null){if(w.getEntity(spearId) instanceof PolkanMissile m&&m.isAlive()){if(++spearWait>90){m.discard();spearReturned();}return;}if(++spearWait>90)spearReturned();else return;}
                if(throwsDone>=3||t==null){if(n>=12)end();return;}if(n>=16){LivingEntity next=xs.isEmpty()?t:xs.get(throwsDone%xs.size());if(distanceTo(next)<=48&&hasLineOfSight(next)&&launch(w,next,34,true)){throwsDone++;spearWait=0;entityData.set(AGE,0);}else end();}}
            case COMMAND->{if(n==20){commandUntil=w.getGameTime()+160;for(SavannaAnimal g:guards(w)){g.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,160,0));g.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,160,0));}}if(n>=35)end();}
            case GUARD->{if(n==16)addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,120,0));if(n>=30)end();}
            default->end();
        }
    }
    private boolean launch(ServerLevel w,LivingEntity t,float damage,boolean spear){
        if(t==null||!hostileTo(t)||!hasLineOfSight(t))return false;PolkanMissile m=ModEntities.POLKAN_MISSILE.get().create(w);if(m==null)return false;
        Vec3 origin=handPosition();Vec3 delta=t.getBoundingBox().getCenter().subtract(origin);double flight=delta.length()/2.9;Vec3 aim=spear?delta:delta.add(0,.5*.035*flight*flight,0);m.init(this,spear,damage*(phase()==3?1.2F:1F),origin,aim.normalize().scale(spear?2.1:2.9));
        if(!w.addFreshEntity(m))return false;if(spear){spearId=m.getUUID();entityData.set(SPEAR_OUT,true);}playSound(spear?SoundEvents.TRIDENT_THROW:SoundEvents.ARROW_SHOOT,1,.85F);return true;
    }
    Vec3 handPosition(){Vec3 f=forward(),side=new Vec3(-f.z,0,f.x);return position().add(0,2.65,0).add(f.scale(.65)).add(side.scale(.45));}
    boolean ownsSpear(UUID id){return id.equals(spearId)&&action()==THROW;}
    void spearReturned(){spearId=null;entityData.set(SPEAR_OUT,false);spearWait=0;entityData.set(AGE,0);}
    private List<SavannaAnimal> guards(ServerLevel w){return w.getEntitiesOfClass(SavannaAnimal.class,getBoundingBox().inflate(64),g->getUUID().equals(g.polkanOwner)&&g.isAlive());}
    static void guardStep(SavannaAnimal g,ServerLevel w){
        if(g.polkanCooldown>0)g.polkanCooldown--;
        if(g.polkanReleased||g.polkanOwner==null||PolkanHerd.get(w).ended(g.polkanOwner)){if(!g.polkanReleased)g.releasePolkan();if(g.tickCount%80==0)g.getNavigation().moveTo(g.getX()+g.getRandom().nextInt(15)-7,g.getY(),g.getZ()+g.getRandom().nextInt(15)-7,1);return;}
        if(!PolkanHerd.get(w).member(g.polkanOwner,g.getUUID(),g.polkanSlot)){g.releasePolkan();return;}
        if(!(w.getEntity(g.polkanOwner) instanceof Polkan leader)||!leader.isAlive()){g.setTarget(null);g.getNavigation().stop();return;}
        List<LivingEntity> xs=leader.candidates(w);
        if(xs.isEmpty()||leader.action()==GUARD){g.setTarget(null);g.polkanStrike(0,false);if((g.tickCount+g.polkanSlot)%12==0){double angle=g.polkanSlot*Math.PI/4,r=leader.action()==GUARD?3:6;g.getNavigation().moveTo(leader.getX()+Math.cos(angle)*r,leader.getY(),leader.getZ()+Math.sin(angle)*r,1.25);}return;}
        if(g.distanceToSqr(leader)>48*48){g.setTarget(null);g.getNavigation().moveTo(leader,1.65);return;}
        LivingEntity t=xs.get(Math.floorMod(g.polkanSlot,xs.size()));g.setTarget(t);g.getLookControl().setLookAt(t,30,30);
        int ticks=g.strikeTicks();if(ticks>0){boolean charge=g.charging();if(ticks==6&&g.distanceToSqr(t)<=9&&g.hasLineOfSight(t)&&leader.hostileTo(t)){if(t.hurt(g.damageSources().mobAttack(g),14+(w.getGameTime()<leader.commandUntil?3:0)))t.knockback(charge?1.4:.8,g.getX()-t.getX(),g.getZ()-t.getZ());}if(charge&&ticks>6&&g.onGround()&&!g.horizontalCollision){Vec3 d=flat(t.position().subtract(g.position()));g.setDeltaMovement(d.x*.62,g.getDeltaMovement().y,d.z*.62);}g.polkanStrike(ticks-1,charge&&ticks>1);if(ticks==1)g.polkanCooldown=35;return;}
        double d=g.distanceToSqr(t);if(g.polkanCooldown==0&&g.hasLineOfSight(t)&&(d<9||d<49&&(w.getGameTime()+g.polkanSlot*5)%(w.getGameTime()<leader.commandUntil?20:60)==0)){g.getNavigation().stop();g.polkanStrike(12,d>=9);return;}if(g.tickCount%5==0)g.getNavigation().moveTo(t,1.6);
    }
    @Override public void die(DamageSource s){super.die(s);if(level() instanceof ServerLevel w){PolkanHerd.get(w).finish(getUUID());for(SavannaAnimal g:guards(w)){Vec3 away=flat(g.position().subtract(position()));g.releasePolkan();g.getNavigation().moveTo(g.getX()+away.x*10,g.getY(),g.getZ()+away.z*10,1.4);}}bar.removeAllPlayers();}
    @Override public void remove(RemovalReason r){bar.removeAllPlayers();if(level() instanceof ServerLevel w&&r.shouldDestroy())PolkanHerd.get(w).finish(getUUID());super.remove(r);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putBoolean("HerdCreated",herdCreated);n.putLongArray("PolkanReady",ready);CreatureHealthBalance.mark(n);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);herdCreated=n.getBoolean("HerdCreated");long[] a=n.getLongArray("PolkanReady");System.arraycopy(a,0,ready,0,Math.min(a.length,ready.length));entityData.set(ACTION,0);entityData.set(AGE,0);entityData.set(SPEAR_OUT,false);}
}