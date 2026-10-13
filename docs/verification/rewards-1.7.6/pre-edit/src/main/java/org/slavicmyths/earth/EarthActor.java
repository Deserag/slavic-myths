package org.slavicmyths.earth;

import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

/** Bounded temporary actors: real projectiles, rings, spikes, hand prop and animated gate.
 * Sweeps sample at <=.2 blocks along velocity and <=.4 along the whole 5.5-block pillar. */
public final class EarthActor extends Entity {
    public static final int STONE=0,PILLAR=1,WARNING=2,COUNTER=3,TREMOR=4,LINE_WARN=5,SPIKES=6,HELD=7,GATE=8,DEBRIS=9,COLLAPSE_BLOCK=10;
    private static final EntityDataAccessor<Integer> KIND=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.INT),LIFE=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.INT),OWNER=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.INT),VARIANT=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.FLOAT),DX=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.FLOAT),DZ=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(EarthActor.class,EntityDataSerializers.LONG);
    private UUID owner,arena;private Vec3 axis=new Vec3(0,0,1),origin=Vec3.ZERO;private final Set<UUID> hit=new HashSet<>();
    public EarthActor(EntityType<?> t,Level w){super(t,w);noPhysics=true;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,0);b.define(LIFE,80);b.define(OWNER,-1);b.define(VARIANT,0);b.define(RADIUS,1F);b.define(DX,0F);b.define(DZ,1F);b.define(START,0L);}
    public float age(float partial){return (float)(level().getGameTime()-entityData.get(START))+partial;}
    public int kind(){return entityData.get(KIND);}public int life(){return entityData.get(LIFE);}public int ownerId(){return entityData.get(OWNER);}public int variant(){return entityData.get(VARIANT);}public float radius(){return entityData.get(RADIUS);}public Vec3 direction(){return new Vec3(entityData.get(DX),0,entityData.get(DZ));}public UUID owner(){return owner;}
    private static EarthActor create(ServerLevel w,EarthSpirit boss,int kind,Vec3 at,int life,double radius){var a=EarthContent.ACTOR.get().create(w);if(a==null)throw new IllegalStateException("earth actor factory");a.entityData.set(START,w.getGameTime());a.entityData.set(KIND,kind);a.entityData.set(LIFE,life);a.entityData.set(RADIUS,(float)radius);a.entityData.set(VARIANT,w.random.nextInt(3));a.setPos(at);a.origin=at;if(boss!=null){a.owner=boss.getUUID();a.arena=boss.arenaId();a.entityData.set(OWNER,boss.getId());}return a;}
    public static void marker(ServerLevel w,EarthSpirit b,Vec3 at,double radius,int ticks){w.addFreshEntity(create(w,b,WARNING,at,ticks,radius));}
    public static void wave(ServerLevel w,EarthSpirit b,double radius,int ticks,boolean tremor){w.addFreshEntity(create(w,b,tremor?TREMOR:COUNTER,b.position(),ticks+28,radius));}
    public static void line(ServerLevel w,EarthSpirit b,Vec3 direction,boolean warning){var a=create(w,b,warning?LINE_WARN:SPIKES,b.position(),warning?24:22,12);a.axis=direction.multiply(1,0,1).normalize();a.entityData.set(DX,(float)a.axis.x);a.entityData.set(DZ,(float)a.axis.z);w.addFreshEntity(a);}
    public static void held(ServerLevel w,EarthSpirit b,Vec3 at){w.addFreshEntity(create(w,b,HELD,at,26,5.5));}
    public static void projectile(ServerLevel w,EarthSpirit b,Vec3 at,Vec3 target,boolean pillar){var a=create(w,b,pillar?PILLAR:STONE,at,80,pillar?5.5:.6);a.axis=target.subtract(at).normalize();double speed=pillar?1.15:1.65;double flight=target.distanceTo(at)/speed;a.setDeltaMovement(a.axis.scale(speed).add(0,.012*flight/2,0));a.setYRot((float)Math.toDegrees(Math.atan2(-a.axis.x,a.axis.z)));a.setXRot((float)-Math.toDegrees(Math.asin(a.axis.y)));w.addFreshEntity(a);if(pillar)marker(w,b,new Vec3(target.x,b.home().getY()+.04,target.z),3.5,24);}
    public static void gate(ServerLevel w,EarthArena.Entry e){var a=create(w,null,GATE,e.at(-14,0,0).getCenter().add(0,-.5,0),Integer.MAX_VALUE,5);a.arena=e.id;a.entityData.set(VARIANT,e.direction);w.addFreshEntity(a);}
    public static void debris(ServerLevel w,Vec3 at,EarthSpirit boss,int count){for(int i=0;i<count;i++){var a=create(w,boss,DEBRIS,at,24+w.random.nextInt(12),.2+w.random.nextDouble()*.4);a.setDeltaMovement((w.random.nextDouble()-.5)*.3,.08+w.random.nextDouble()*.15,(w.random.nextDouble()-.5)*.3);w.addFreshEntity(a);}}
    /** Cosmetic only: no ItemEntity, block placement, loot, owner damage or pickup. */
    public static void collapse(ServerLevel w,Vec3 at,int count,double spread){var blocks=List.of(net.minecraft.world.level.block.Blocks.COBBLESTONE,net.minecraft.world.level.block.Blocks.MOSSY_COBBLESTONE,net.minecraft.world.level.block.Blocks.STONE,net.minecraft.world.level.block.Blocks.ANDESITE,net.minecraft.world.level.block.Blocks.DIORITE,net.minecraft.world.level.block.Blocks.GRANITE,net.minecraft.world.level.block.Blocks.COBBLED_DEEPSLATE,net.minecraft.world.level.block.Blocks.DIRT,net.minecraft.world.level.block.Blocks.COARSE_DIRT);for(int i=0;i<count;i++){double angle=i*Math.PI*2/count;var a=create(w,null,COLLAPSE_BLOCK,at.add(Math.cos(angle)*spread,(w.random.nextDouble()-.5)*.6,Math.sin(angle)*spread),64+w.random.nextInt(17),.35+w.random.nextDouble()*.35);a.entityData.set(VARIANT,net.minecraft.world.level.block.Block.getId(blocks.get(w.random.nextInt(blocks.size())).defaultBlockState()));a.noPhysics=false;a.setDeltaMovement(Math.cos(angle)*(.05+w.random.nextDouble()*.12),.05+w.random.nextDouble()*.13,Math.sin(angle)*(.05+w.random.nextDouble()*.12));w.addFreshEntity(a);}}
    @Override public boolean shouldBeSaved(){return false;}
    @Override protected void readAdditionalSaveData(CompoundTag n){}@Override protected void addAdditionalSaveData(CompoundTag n){}
    private EarthSpirit boss(){return owner!=null&&level() instanceof ServerLevel w&&w.getEntity(owner) instanceof EarthSpirit b?b:null;}
    @Override public void tick(){super.tick();if(kind()==COLLAPSE_BLOCK){noPhysics=false;move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().multiply(onGround()?.5:.96,onGround()?0:.98,onGround()?.5:.96).add(0,onGround()?0:-.04,0));if(!level().isClientSide&&tickCount>=life())discard();return;}if(level().isClientSide){if(kind()==STONE||kind()==PILLAR||kind()==DEBRIS){setPos(position().add(getDeltaMovement()));setDeltaMovement(getDeltaMovement().add(0,kind()==DEBRIS?-.025:-.012,0));}return;}var w=(ServerLevel)level();var b=boss();if(tickCount>life()||owner!=null&&(b==null||!b.isAlive()||b.action()==EarthRules.Action.DORMANT)){discard();return;}
        switch(kind()){
            case STONE,PILLAR->projectileTick(w,b);
            case COUNTER,TREMOR->waveTick(w,b);
            case SPIKES->spikeTick(w,b);
            case HELD->{if(b==null||b.action()!=EarthRules.Action.PILLAR||b.age(0)>=46){discard();return;}double t=Math.clamp((b.age(0)-20)/7,0,1);Vec3 hand=b.hand();setPos(origin.lerp(hand,t));setYRot(b.getYRot());setXRot((float)(Math.clamp((b.age(0)-27)/12,0,1)*-65));}
            case GATE->{var e=arena==null?null:EarthArena.get(w).entries.get(arena);if(e==null||e.gateClosed||e.state==EarthArena.State.DORMANT||e.state==EarthArena.State.DEFEATED)discard();}
            case DEBRIS->{setPos(position().add(getDeltaMovement()));setDeltaMovement(getDeltaMovement().multiply(.96,.96,.96).add(0,-.025,0));}
            default->{}
        }
    }
    private static boolean sight(ServerLevel w,Vec3 from,Vec3 to,Entity context){return w.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,context)).getType()==HitResult.Type.MISS;}
    private boolean damage(ServerLevel w,EarthSpirit b,Player p,float raw,double force,Vec3 center){boolean landed=hit.add(p.getUUID())&&p.hurt(b.attackSource(this),raw);if(landed&&force>0){var v=p.position().subtract(center).multiply(1,0,1).normalize();p.knockback(force,-v.x,-v.z);p.hurtMarked=true;}return landed;}
    private void waveTick(ServerLevel w,EarthSpirit b){if(b==null)return;int travel=kind()==TREMOR?16:9;if(tickCount>travel)return;double previous=1.5+(radius()-1.5)*(tickCount-1)/travel,current=1.5+(radius()-1.5)*tickCount/travel;
        for(var p:w.getEntitiesOfClass(Player.class,new AABB(origin,origin).inflate(radius()+1,2,radius()+1),b::valid)){double d=p.position().subtract(origin).horizontalDistance();double feet=p.getY()-origin.y;double half=p.getBbWidth()/2;if(d+half<previous-.55||d-half>current+.55||feet<-.4||feet>(kind()==TREMOR?.9:1.5)||hit.contains(p.getUUID()))continue;if(sight(w,origin.add(0,.3,0),p.position().add(0,.3,0),this)){boolean landed=damage(w,b,p,EarthRules.WAVE_RAW,kind()==TREMOR?.35:1.5,origin);if(landed&&kind()==TREMOR){p.addEffect(new net.minecraft.world.effect.MobEffectInstance(EarthContent.IMBALANCE,13,0,false,false,true));}}}
    }
    private void spikeTick(ServerLevel w,EarthSpirit b){if(b==null)return;for(int i=0;i<3;i++)if(tickCount==1+i*2){double start=1+i*4,end=start+4;for(var p:w.getEntitiesOfClass(Player.class,new AABB(origin,origin.add(axis.scale(13))).inflate(2,1.4,2),b::valid)){Vec3 v=p.position().subtract(origin);double along=v.dot(axis),side=Math.abs(v.x*axis.z-v.z*axis.x);if(along>=start&&along<=end&&side<=1+p.getBbWidth()/2&&Math.abs(v.y)<1.2&&sight(w,origin.add(0,.3,0),p.position().add(0,.3,0),this))damage(w,b,p,EarthRules.SPIKE_RAW,.25,origin);}}}
    private boolean solid(ServerLevel w,Vec3 sample,double r){var box=new AABB(sample,sample).inflate(r);for(var at:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){if(!w.hasChunkAt(at))return true;var shape=w.getBlockState(at).getCollisionShape(w,at);for(var part:shape.toAabbs())if(part.move(at).intersects(box))return true;}return false;}
    private void projectileTick(ServerLevel w,EarthSpirit b){if(b==null){discard();return;}Vec3 velocity=getDeltaMovement(),from=position();int steps=Math.max(1,(int)Math.ceil(velocity.length()/.2));double length=kind()==PILLAR?5.5:0,r=kind()==PILLAR?.78:.25;
        for(int s=1;s<=steps;s++){Vec3 center=from.add(velocity.scale(s/(double)steps));boolean wall=false;Vec3 impact=center;for(double a=-length/2;a<=length/2+.001;a+=length==0?1:.35){Vec3 sample=center.add(axis.scale(a));if(solid(w,sample,r)){wall=true;impact=sample;break;}}if(wall){impact(w,b,impact);return;}
            var candidates=w.getEntitiesOfClass(Player.class,new AABB(center,center).inflate(length/2+r,2+r,length/2+r),b::valid);
            for(var p:candidates){Vec3 lo=center.subtract(axis.scale(length/2)),hi=center.add(axis.scale(length/2));if(p.getBoundingBox().inflate(r).contains(lo)||p.getBoundingBox().inflate(r).clip(lo,hi).isPresent()){long until=p.getPersistentData().getLong("EarthVolleyHitUntil");if(kind()!=STONE||w.getGameTime()>=until){damage(w,b,p,kind()==PILLAR?EarthRules.PILLAR_RAW:EarthRules.STONE_RAW,kind()==PILLAR?.9:.35,center);if(kind()==STONE)p.getPersistentData().putLong("EarthVolleyHitUntil",w.getGameTime()+8);}impact(w,b,center);return;}}
        }
        setPos(from.add(velocity));setDeltaMovement(velocity.add(0,-.012,0));
    }
    private void impact(ServerLevel w,EarthSpirit b,Vec3 at){EarthArena.cue(w,BlockPos.containing(at),kind()==PILLAR?"pillar_impact":"volley_impact",kind()==PILLAR?1:.6F);if(kind()==PILLAR)for(var p:w.getEntitiesOfClass(Player.class,new AABB(at,at).inflate(3.5,1.5,3.5),b::valid))if(!hit.contains(p.getUUID())&&p.position().distanceToSqr(at)<3.5*3.5&&sight(w,at.add(0,.1,0),p.position().add(0,.6,0),this))damage(w,b,p,EarthRules.SPLASH_RAW,.6,at);
        debris(w,at,b,kind()==PILLAR?7:4);w.sendParticles(ParticleTypes.POOF,at.x,at.y,at.z,kind()==PILLAR?20:5,.5,.3,.5,.02);discard();
    }
}
