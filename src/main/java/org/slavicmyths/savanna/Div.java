package org.slavicmyths.savanna;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import org.slavicmyths.combat.CreatureHealthBalance;

public final class Div extends PathfinderMob {
    public static final int MAX_HP=1600, IDLE=0, CLAW=1, DOUBLE=2, WING=3, GRAB=4, TAIL=5, CURSED_CLAW=6, FEATHER=7, FAN=8, SCREAM=9, GAZE=10, GROUND=11, DIVE=12, FATE=13;
    private static final int[] COOLDOWN={0,60,140,180,500,160,240,80,200,320,400,480,440,0};
    private static final int[] DURATION={0,26,40,34,72,30,30,30,38,44,48,36,90,54};
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(Div.class,EntityDataSerializers.INT), AGE=SynchedEntityData.defineId(Div.class,EntityDataSerializers.INT), PHASE=SynchedEntityData.defineId(Div.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLYING=SynchedEntityData.defineId(Div.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar=new ServerBossEvent(Component.translatableWithFallback("entity.slavicmyths.div","Див"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    private final long[] ready=new long[14];
    private final List<Shot> shots=new ArrayList<>();
    private final List<Zone> zones=new ArrayList<>();
    private record Doom(long due,Vec3 at){}
    private final Map<UUID,Doom> doomed=new HashMap<>();
    private UUID locked,grabbed;
    private Vec3 aim=Vec3.ZERO,flightOrigin=Vec3.ZERO;
    private int delay,airTicks;
    private long roamAt;
    private record Shot(Vec3 pos,Vec3 velocity,float damage,int life,boolean curse){}
    private record Zone(Vec3 pos,long start,long end){}
    public Div(EntityType<? extends Div> type,Level world){super(type,world);setPersistenceRequired();xpReward=0;}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,MAX_HP).add(Attributes.ARMOR,16).add(Attributes.ARMOR_TOUGHNESS,6).add(Attributes.KNOCKBACK_RESISTANCE,.9).add(Attributes.ATTACK_DAMAGE,32).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.FOLLOW_RANGE,64);}
    @Override public float getMaxHealth(){return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
    @Override public double getAttributeValue(net.minecraft.core.Holder<Attribute> a){return a.equals(Attributes.MAX_HEALTH)?CreatureHealthBalance.uncappedMaximum(getAttribute(a)):super.getAttributeValue(a);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(AGE,0);b.define(PHASE,1);b.define(FLYING,false);}
    public int action(){return entityData.get(ACTION);} public int actionAge(){return entityData.get(AGE);} public int phase(){return entityData.get(PHASE);} public boolean flying(){return entityData.get(FLYING);}
    public static int duration(int a){return a>=0&&a<DURATION.length?DURATION[a]:1;}
    @Override public int getMaxSpawnClusterSize(){return 1;}
    @Override public Component getName(){return hasCustomName()?super.getName():Component.translatableWithFallback(getType().getDescriptionId(),"Див");}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor w,DifficultyInstance d,MobSpawnType reason,SpawnGroupData data){SpawnGroupData result=super.finalizeSpawn(w,d,reason,data);setHealth(MAX_HP);return result;}
    private boolean valid(LivingEntity x){return x!=null&&x!=this&&x.isAlive()&&x.level()==level()&&!(x instanceof Div)&&(!(x instanceof Player p)||!p.isCreative()&&!p.isSpectator())&&!isAlliedTo(x)&&canAttack(x);}
    private List<LivingEntity> candidates(ServerLevel w){return w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(64),x->valid(x)&&(x instanceof Player||x==getTarget())&&distanceToSqr(x)<=64*64);}
    private LivingEntity lookup(ServerLevel w,UUID id){return id!=null&&w.getEntity(id) instanceof LivingEntity l&&valid(l)?l:null;}
    @Override public boolean hurt(DamageSource s,float amount){boolean hit=super.hurt(s,amount);if(hit&&!level().isClientSide&&s.getEntity() instanceof LivingEntity x&&valid(x))setTarget(x);return hit;}
    private Vec3 front(){double r=Math.toRadians(getYRot());return new Vec3(-Math.sin(r),0,Math.cos(r));}
    private static Vec3 flat(Vec3 v){return v.multiply(1,0,1).normalize();}
    private void face(Vec3 p){Vec3 d=p.subtract(position());float y=(float)Math.toDegrees(Math.atan2(-d.x,d.z));setYRot(y);yBodyRot=y;setYHeadRot(y);}
    private Vec3 mouth(){return position().add(0,7.6,0).add(front().scale(1.7));}
    private boolean inRange(LivingEntity x,double r){return x!=null&&x.getBoundingBox().getCenter().distanceToSqr(position().add(0,2.1,0))<=r*r;}
    private float damage(float v){return v*(phase()==3?1.2F:1F);}
    private boolean hit(LivingEntity x,float amount,boolean curse,double kb){if(!valid(x)||!hasLineOfSight(x))return false;if(!x.hurt(damageSources().mobAttack(this),damage(amount)))return false;if(kb>0)x.knockback(kb,getX()-x.getX(),getZ()-x.getZ());if(curse)mark(x);return true;}
    private void mark(LivingEntity x){if(doomed.containsKey(x.getUUID()))return;int n=DivCurse.add(x,getUUID());if(n==4)doomed.put(x.getUUID(),new Doom(level().getGameTime()+40,x.position()));}
    private void area(List<LivingEntity> xs,double r,double min,double max,float dmg,boolean curse,double kb){for(LivingEntity x:xs){double dot=front().dot(flat(x.position().subtract(position())));if(inRange(x,r)&&dot>=min&&dot<=max)hit(x,dmg,curse,kb);}}
    private void start(int a,LivingEntity t){entityData.set(ACTION,a);entityData.set(AGE,0);locked=t.getUUID();aim=t.position();flightOrigin=position();if(a!=TAIL)face(aim);getNavigation().stop();ready[a]=level().getGameTime()+(phase()==3?(int)(COOLDOWN[a]*.75):COOLDOWN[a]);playSound(SoundEvents.PHANTOM_AMBIENT,1.8F,.65F);}
    private void finish(){grabbed=null;locked=null;entityData.set(ACTION,IDLE);entityData.set(AGE,0);delay=10;setNoGravity(false);entityData.set(FLYING,false);}
    private void fly(Vec3 target,double speed){Vec3 d=target.subtract(position());Vec3 v=d.length()<speed?d:d.normalize().scale(speed);if(level().hasChunkAt(BlockPos.containing(position().add(v)))&&level().noCollision(this,getBoundingBox().move(v))){setNoGravity(true);entityData.set(FLYING,true);setDeltaMovement(v);}else{setDeltaMovement(Vec3.ZERO);setNoGravity(false);entityData.set(FLYING,false);}}
    private void particleLine(ServerLevel w,Vec3 a,Vec3 b){int count=Math.min(48,Math.max(1,(int)(a.distanceTo(b)*2)));for(int i=0;i<=count;i++){Vec3 v=a.lerp(b,i/(double)count);w.sendParticles(ParticleTypes.WITCH,v.x,v.y,v.z,1,0,0,0,0);}}
    private void ring(ServerLevel w,Vec3 pos,double r){for(int i=0;i<32;i++){double a=i*Math.PI/16;w.sendParticles(ParticleTypes.WITCH,pos.x+r*Math.cos(a),pos.y+.15,pos.z+r*Math.sin(a),1,0,0,0,0);}}
    private void shoot(LivingEntity t,int count,float dmg,boolean curse){Vec3 origin=mouth(),v=t.getBoundingBox().getCenter().subtract(origin).normalize();for(int i=0;i<count;i++){double a=(i-(count-1)*.5)*.12;shots.add(new Shot(origin,v.yRot((float)a).scale(1.6),damage(dmg),45,curse));}}
    private void tickShots(ServerLevel w){List<Shot> next=new ArrayList<>();for(Shot shot:shots){Vec3 from=shot.pos,to=from.add(shot.velocity);if(!w.hasChunkAt(BlockPos.containing(to)))continue;HitResult block=w.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));Vec3 limit=block.getType()==HitResult.Type.MISS?to:block.getLocation();LivingEntity target=null;double best=from.distanceToSqr(limit);
        for(LivingEntity x:w.getEntitiesOfClass(LivingEntity.class,new AABB(from,limit).inflate(.5),this::valid)){var p=x.getBoundingBox().inflate(.2).clip(from,limit);if(p.isPresent()&&from.distanceToSqr(p.get())<best){best=from.distanceToSqr(p.get());target=x;}}
        particleLine(w,from,limit);if(target!=null){if(target.hurt(damageSources().mobAttack(this),shot.damage)&&shot.curse)mark(target);}else if(block.getType()==HitResult.Type.MISS&&shot.life>1)next.add(new Shot(to,shot.velocity,shot.damage,shot.life-1,shot.curse));}shots.clear();shots.addAll(next);}
    private void tickZones(ServerLevel w,List<LivingEntity> xs){long now=w.getGameTime();zones.removeIf(z->now>=z.end);for(Zone z:zones){if(now%10==0)ring(w,z.pos,4);if(now>=z.start&&(now-z.start)%20==0)for(LivingEntity x:xs)if(x.position().distanceToSqr(z.pos)<=16)hit(x,4,false,0);}}
    private void tickDoom(ServerLevel w){long now=w.getGameTime();var it=doomed.entrySet().iterator();while(it.hasNext()){var row=it.next();LivingEntity x=lookup(w,row.getKey());if(x==null||!DivCurse.owned(x,getUUID())){it.remove();continue;}if(now>=row.getValue().due){DivCurse.clear(x);it.remove();Vec3 at=row.getValue().at;ring(w,at,3);particleLine(w,at.add(0,8,0),at);if(x.position().distanceToSqr(at)<=9&&inRange(x,42))hit(x,60,false,1.2);}else if(now%5==0)ring(w,row.getValue().at,3);}}
    private int choose(ServerLevel w,LivingEntity t){double d=distanceTo(t);int[] choices=d<=8?(phase()==1?new int[]{TAIL,CLAW,DOUBLE,WING,CURSED_CLAW}:new int[]{TAIL,GRAB,CURSED_CLAW,DOUBLE,WING,CLAW}):(phase()==1?new int[]{FEATHER,SCREAM,GAZE}:new int[]{DIVE,GROUND,FAN,GAZE,SCREAM,FEATHER});for(int a:choices){if(w.getGameTime()<ready[a])continue;if(a==TAIL&&front().dot(flat(t.position().subtract(position())))>-.2)continue;if(a==GRAB&&(!onGround()||d>6))continue;if(a==DIVE&&(d<9||d>36))continue;if(d>44)continue;if(hasLineOfSight(t))return a;}return IDLE;}
    @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w)||!isAlive())return;if(w.getDifficulty()==Difficulty.PEACEFUL){discard();return;}entityData.set(PHASE,getHealth()<=560?3:getHealth()<=1120?2:1);bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));
        if(tickCount%20==0){Set<ServerPlayer> viewers=new HashSet<>();for(ServerPlayer p:w.players())if(!p.isSpectator()&&distanceToSqr(p)<=64*64)viewers.add(p);for(ServerPlayer p:List.copyOf(bar.getPlayers()))if(!viewers.contains(p))bar.removePlayer(p);for(ServerPlayer p:viewers)if(!bar.getPlayers().contains(p))bar.addPlayer(p);}
        var xs=candidates(w);tickShots(w);tickZones(w,xs);tickDoom(w);
        LivingEntity t=getTarget();if(!valid(t)||distanceToSqr(t)>4096){t=xs.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);setTarget(t);}
        if(action()!=IDLE){advance(w,lookup(w,locked),xs);return;}
        if(delay>0)delay--;
        if(t==null){if(w.getGameTime()>=roamAt){roamAt=w.getGameTime()+180;BlockPos p=blockPosition().offset(random.nextInt(31)-15,0,random.nextInt(31)-15);if(w.hasChunkAt(p)){p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p);getNavigation().moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,1);}}return;}
        if(delay==0){int a=choose(w,t);if(a!=IDLE){start(a,t);return;}}
        getLookControl().setLookAt(t,30,30);if(tickCount%8==0)getNavigation().moveTo(t,1.35);
    }
    private void advance(ServerLevel w,LivingEntity t,List<LivingEntity> xs){int a=action(),n=actionAge()+1;entityData.set(AGE,n);if(t==null){finish();return;}if(n<=8&&a!=TAIL){aim=t.position();face(aim);}switch(a){
        case CLAW->{if(n==13)area(xs,7,.2,1,32,false,.8);}
        case DOUBLE->{if(n==13||n==26)area(xs,7,.1,1,22,false,.4);}
        case WING->{if(n==18)area(xs,10,-.3,1,28,false,2);}
        case TAIL->{if(n==15)area(xs,9,-1,-.15,30,false,1.5);}
        case CURSED_CLAW->{if(n==15)area(xs,7,.1,1,36,true,1);}
        case FEATHER->{if(n==15&&hasLineOfSight(t))shoot(t,1,20,true);}
        case FAN->{if(n==19&&hasLineOfSight(t))shoot(t,7,14,false);}
        case SCREAM->{if(n>=18&&n<=32&&n%4==2)ring(w,position().add(0,2,0),(n-14)*1.25);if(n==24)area(xs,24,.25,1,34,true,1);}
        case GAZE->{if(n>=12&&n<28&&n%4==0)particleLine(w,mouth(),aim.add(0,1,0));if(n==28){particleLine(w,mouth(),aim.add(0,1,0));for(LivingEntity x:xs)if(x.position().distanceToSqr(aim)<9&&inRange(x,42))hit(x,40,false,0);}}
        case GROUND->{if(n==18){zones.add(new Zone(aim,w.getGameTime()+20,w.getGameTime()+180));ring(w,aim,4);}}
        case GRAB->{if(n==20&&inRange(t,6)&&hasLineOfSight(t))grabbed=t.getUUID();if(grabbed!=null&&n>=20&&n<48){fly(flightOrigin.add(0,3,0),.35);Vec3 p=position().add(front().scale(2.4)).add(0,3.8,0);if(w.noCollision(t,t.getBoundingBox().move(p.subtract(t.position())))){t.teleportTo(p.x,p.y,p.z);t.setDeltaMovement(Vec3.ZERO);t.fallDistance=0;}else grabbed=null;}if(n==48&&grabbed!=null){hit(t,48,false,0);t.setDeltaMovement(front().scale(1.15).add(0,.35,0));t.hurtMarked=true;grabbed=null;}if(n>=48)fly(flightOrigin,.45);}
        case DIVE->{if(n<=28)fly(flightOrigin.add(0,7,0),.55);if(n==28){aim=t.position();face(aim);}if(n>=29&&n<=55){fly(aim,.95);if(n==55||position().distanceToSqr(aim)<2.25){area(xs,8,-1,1,50,false,1.5);ring(w,position(),5);entityData.set(AGE,56);}}if(n>=56){BlockPos p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,blockPosition());fly(Vec3.atBottomCenterOf(p),.5);}}
        default->{}
    }if(n>=duration(a))finish();}
    @Override public void die(DamageSource s){super.die(s);bar.removeAllPlayers();shots.clear();zones.clear();doomed.clear();grabbed=null;}
    @Override public void remove(RemovalReason r){bar.removeAllPlayers();super.remove(r);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putLongArray("DivReady",ready);CreatureHealthBalance.mark(n);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);long[] a=n.getLongArray("DivReady");System.arraycopy(a,0,ready,0,Math.min(a.length,ready.length));finish();}
}
