package org.slavicmyths.firebird;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class FirebirdEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(FirebirdEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE=SynchedEntityData.defineId(FirebirdEntity.class,EntityDataSerializers.INT);
    public UUID visit;public BlockPos anchor=BlockPos.ZERO;
    private Vec3 waypoint;private LivingEntity aggressor;private int cooldown,ambient=400,call=1400,hurtCue,flightTicks;private BlockPos rewardSite;
    public FirebirdEntity(EntityType<? extends FirebirdEntity> t,Level w){super(t,w);setNoGravity(true);setPersistenceRequired();setHealth(3000);}
    // Vanilla 1.21.1 caps MAX_HEALTH at 1024. AT removes final only; other species retain vanilla behavior.
    @Override public float getMaxHealth(){return 3000;}
    @Override public double getAttributeValue(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute){return attribute.equals(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)?3000:super.getAttributeValue(attribute);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,0);b.define(AGE,0);}
    @Override protected void registerGoals(){}
    public int state(){return entityData.get(STATE);}public int age(){return entityData.get(AGE);}
    private void state(int s){entityData.set(STATE,s);entityData.set(AGE,0);}
    public void cue(String s,float volume){playSound(FirebirdContent.sound(s),volume,.94F+random.nextFloat()*.12F);}
    public void depart(){if(state()!=7){state(7);cue("depart",.75F);var p=level().getNearestPlayer(this,128);Vec3 away=p==null?new Vec3(1,0,1):position().subtract(p.position());waypoint=position().add(away.normalize().scale(60)).add(0,30,0);}}
    @Override public void travel(Vec3 input){move(MoverType.SELF,getDeltaMovement());}
    @Override public boolean causeFallDamage(float f,float m,DamageSource s){return false;}
    @Override public boolean canBeLeashed(){return false;}
    @Override public void tick(){super.tick();if(level().isClientSide){if(isAlive()&&tickCount%10==0)level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,getX(),getY()+2.4,getZ(),0,.008,0);if(isAlive()&&tickCount%7==0)level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,getX(),getY()+1.3,getZ()+1.5,0,-.008,0);return;}if(!isAlive())return;var w=(ServerLevel)level();var d=FirebirdEncounter.get(w);entityData.set(AGE,age()+1);if(cooldown>0)cooldown--;if(hurtCue>0)hurtCue--;
        if(visit!=null&&!d.owns(this)){discard();return;}
        if(d.owns(this)){d.lastSeen=w.getGameTime();d.setDirty();if(!FirebirdRules.night(w.getDayTime())||Integer.bitCount(d.issued)>=d.planned)depart();}
        if(--ambient<=0){cue("ambient",.4F);ambient=360+random.nextInt(341);}if(--call<=0){cue("call",.65F);call=1000+random.nextInt(801);}
        if(state()==1)flightTicks=0;else if(state()==2||state()==9)flightTicks++;
        if(state()!=1&&tickCount%35==15)cue("wing_flap",.35F);
        if(state()==7){if(waypoint==null)waypoint=position().add(45,30,45);navigate(w,.35);if(age()>=100&&(w.getNearestPlayer(this,48)==null||age()>=300)){if(d.owns(this))d.close(w);discard();}return;}
        if(state()==5&&age()>=11){state(6);return;}
        if(state()==6){if(age()==8){cue("retaliation",.8F);if(aggressor!=null&&aggressor.isAlive()&&distanceToSqr(aggressor)<=9&&hasLineOfSight(aggressor)){var key=ResourceKey.create(Registries.DAMAGE_TYPE,FirebirdContent.id("firebird_retaliation"));aggressor.hurt(new DamageSource(w.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),this),20);}}if(age()>=17){state(2);cooldown=60;waypoint=position().add(0,14,10);}return;}
        if(state()==4){if(age()==6&&d.owns(this)&&prepareDrop(w))issue(w,d);if(age()>=14){state(2);if(d.owns(this)&&Integer.bitCount(d.issued)>=d.planned)depart();}return;}
        if(d.owns(this)&&Integer.bitCount(d.issued)<d.planned){if(d.remaining>0){d.remaining--;d.setDirty();}else {
            if(prepareDrop(w)){state(4);rewardSite=null;return;}
            if(tickCount%100==0){var observer=w.getNearestPlayer(this,160);if(observer!=null)for(int i=0;i<16;i++){double a=random.nextDouble()*Math.PI*2,r=8+random.nextDouble()*10;var p=FirebirdEncounter.surface(w,(int)Math.floor(observer.getX()+Math.cos(a)*r),(int)Math.floor(observer.getZ()+Math.sin(a)*r));if(p!=null){rewardSite=p;break;}}}
            if(rewardSite!=null){waypoint=Vec3.atBottomCenterOf(rewardSite).add(0,6,0);if(state()==1||state()==0)state(2);navigate(w,.18);return;}
        }}
        if(anchor.equals(BlockPos.ZERO)&&visit==null)anchor=blockPosition();
        if(state()==8){waypoint=Vec3.atBottomCenterOf(anchor);navigate(w,.08);if(age()>=23&&position().distanceToSqr(waypoint)<.01){setDeltaMovement(Vec3.ZERO);state(1);cue("perch",.35F);}else if(age()>120)state(2);}
        else if(state()==9){navigate(w,.16);if(age()>=32)state(2);}
        else if(state()==0){waypoint=Vec3.atBottomCenterOf(anchor);navigate(w,.14);if(position().distanceToSqr(waypoint)<4)state(8);else if(age()>240)state(2);}
        else if(state()==1){setDeltaMovement(Vec3.ZERO);if(age()>120+Math.floorMod(getUUID().hashCode(),281)){state(3);cue("takeoff",.75F);}}
        else if(state()==3){setDeltaMovement(getDeltaMovement().lerp(new Vec3(0,.14,0),.15));if(age()>=19){state(2);waypoint=null;}}
        else {if(waypoint==null||position().distanceToSqr(waypoint)<9||age()%160==0){double angle=tickCount*.009;double radius=35+random.nextDouble()*20;var p=FirebirdEncounter.surface(w,anchor.getX()+(int)(Math.cos(angle)*radius),anchor.getZ()+(int)(Math.sin(angle)*radius));if(p!=null)waypoint=Vec3.atBottomCenterOf(p).add(0,12+random.nextInt(15),0);else waypoint=Vec3.atBottomCenterOf(anchor).add(0,16,0);}navigate(w,.18);if(flightTicks>800&&tickCount%100==0){var p=FirebirdEncounter.surface(w,getBlockX(),getBlockZ());if(p!=null){anchor=p;state(0);}}else if(flightTicks%320==0){state(9);cue("glide",.23F);}}
    }
    private void navigate(ServerLevel w,double speed){if(waypoint==null)return;var desired=waypoint.subtract(position()).normalize().scale(speed);var next=getBoundingBox().move(desired.scale(8));if(!w.hasChunkAt(BlockPos.containing(position().add(desired.scale(8))))||!w.noCollision(this,next)){desired=new Vec3(0,.14,0);waypoint=position().add(0,5,0);}setDeltaMovement(getDeltaMovement().lerp(desired,.08));if(getDeltaMovement().horizontalDistanceSqr()>.0001){float y=(float)(Math.atan2(-getDeltaMovement().x,getDeltaMovement().z)*180/Math.PI);setYRot(net.minecraft.util.Mth.rotLerp(.08F,getYRot(),y));yBodyRot=getYRot();}}
    private boolean prepareDrop(ServerLevel w){var p=FirebirdEncounter.surface(w,getBlockX(),getBlockZ());if(p==null)return false;var observer=w.getNearestPlayer(this,48);if(observer==null)return false;double dist=observer.position().distanceToSqr(Vec3.atBottomCenterOf(p));return dist>=64&&dist<=324;}
    private void issue(ServerLevel w,FirebirdEncounter d){int slot=0;while(slot<d.planned&&(d.issued&(1<<slot))!=0)slot++;if(slot>=d.planned)return;final int current=slot;
        var existing=w.getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(48),i->{var n=i.getPersistentData();return n.hasUUID("FirebirdVisit")&&n.getUUID("FirebirdVisit").equals(visit)&&n.getInt("FirebirdSlot")==current;});
        if(!existing.isEmpty()){d.issued|=1<<slot;d.remaining=FirebirdRules.delay(false,random);d.setDirty();return;}
        if(d.outcomes[slot]<0){d.outcomes[slot]=FirebirdRules.resource(random.nextInt(100));d.setDirty();}
        var direction=Vec3.directionFromRotation(0,getYRot());var drop=new ItemEntity(w,getX()-direction.x*.9,getY()+1.3,getZ()-direction.z*.9,new ItemStack(FirebirdContent.ITEMS.get(d.outcomes[slot]).get()));drop.setDeltaMovement(direction.scale(.025).add(0,-.025,0));drop.setDefaultPickUpDelay();drop.getPersistentData().putUUID("FirebirdVisit",visit);drop.getPersistentData().putInt("FirebirdSlot",slot);
        if(w.addFreshEntity(drop)){d.issued|=1<<slot;d.remaining=FirebirdRules.delay(false,random);d.setDirty();cue("drop",.5F);w.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW,drop.getX(),drop.getY(),drop.getZ(),7,.15,.15,.15,.02);}
    }
    @Override public boolean hurt(DamageSource s,float amount){boolean result=super.hurt(s,amount);if(result&&!level().isClientSide){if(hurtCue==0){cue("hurt",.55F);hurtCue=12;}if(s.getEntity() instanceof LivingEntity a&&a.isAlive()&&!(a instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator()))&&cooldown==0&&state()!=5&&state()!=6&&state()!=7){aggressor=a;state(5);cue("warning",.85F);}}return result;}
    @Override public void die(DamageSource s){if(!level().isClientSide){cue("death",.85F);var d=FirebirdEncounter.get((ServerLevel)level());if(d.owns(this))d.close((ServerLevel)level());}super.die(s);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(visit!=null)n.putUUID("Visit",visit);n.putLong("Anchor",anchor.asLong());n.putInt("FirebirdState",state());n.putInt("FirebirdAge",age());}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);visit=n.hasUUID("Visit")?n.getUUID("Visit"):null;anchor=BlockPos.of(n.getLong("Anchor"));int s=n.getInt("FirebirdState");state(s==7?7:s==1?1:2);entityData.set(AGE,Math.clamp(n.getInt("FirebirdAge"),0,1000));setNoGravity(true);}
}
