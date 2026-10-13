package org.slavicmyths.bogatyr;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.bogatyr.BogatyrRules.State;

/** A persistent, finite defensive ally; healing does not reset encounter budgets. */
public final class IlyaEntity extends CombatEntity {
    private static final EntityDataAccessor<String> OWNER=SynchedEntityData.defineId(IlyaEntity.class,EntityDataSerializers.STRING);
    public UUID owner,encounter,bossId;
    public boolean unyieldingUsed,returned,interceptUsed;
    public long unyieldingUntil,recoveryEnd,jointStrikeUntil;public UUID jointStrikeBoss;
    private Vec3 refuge;
    private int shieldHits;
    public IlyaEntity(EntityType<? extends IlyaEntity> t,Level w){super(t,w);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(OWNER,"");}
    public UUID syncedOwner(){try{return UUID.fromString(entityData.get(OWNER));}catch(IllegalArgumentException e){return null;}}
    public boolean recovering(){return BogatyrRules.recovering(state());}
    public boolean shield(){return state()==State.SHIELD_HOLD;}
    public IdolishcheEntity boss(){return level() instanceof ServerLevel w&&bossId!=null&&w.getEntity(bossId) instanceof IdolishcheEntity b&&b.isAlive()&&b.active?b:null;}
    public void bind(IdolishcheEntity b){if(b.encounter.equals(encounter))return;encounter=b.encounter;bossId=b.getUUID();unyieldingUsed=false;returned=false;interceptUsed=false;shieldHits=0;setTarget(b);}
    public void endEncounter(){bossId=null;encounter=null;setTarget(null);if(!recovering())begin(State.FOLLOW,0);}
    @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w)||!isAlive())return;
        if(owner==null)return;entityData.set(OWNER,owner.toString());var ledger=BogatyrLedger.get(w);var canonical=ledger.heroes.get(owner);
        if(canonical!=null&&!canonical.equals(getUUID())){discard();return;}if(canonical==null){ledger.heroes.put(owner,getUUID());ledger.setDirty();}
        var p=w.getServer().getPlayerList().getPlayer(owner);var b=boss();if(bossId!=null&&b==null)endEncounter();
        if(recovering()){recovery(w,b,p);return;}
        if(b!=null&&BogatyrRules.retreat(getHealth())&&!b.grabbed(this)){startRetreat(b);return;}
        if(b!=null&&!unyieldingUsed&&BogatyrRules.unyielding(getHealth())&&!b.grabbed(this)){unyieldingUsed=true;unyieldingUntil=w.getGameTime()+200;begin(State.UNYIELDING,30);cue("unyielding_nonverbal_01");}
        step(w,b);if(duration()>0&&elapsed()>=duration())finish();
        if(duration()>0)return;
        if(p!=null&&p.level()==level()&&tickCount%5==0&&distanceToSqr(p)<=25&&ready("shield")){
            for(var shot:w.getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,getBoundingBox().inflate(14),shot->shot.getOwner()==null||!isAlliedTo(shot.getOwner()))){var velocity=shot.getDeltaMovement();if(velocity.lengthSqr()<.01||p.getBoundingBox().inflate(.4).clip(shot.position(),shot.position().add(velocity.scale(18))).isEmpty())continue;var source=shot.getOwner();if(source instanceof LivingEntity enemy)face(enemy);cooldown("shield",280);shieldHits=0;begin(State.SHIELD_RAISE,12);return;}
        }
        if(b!=null&&tickCount%5==0){face(b);
            if((b.state()==State.FIST_WINDUP||b.state()==State.CHARGE_WINDUP)&&edge(b)<5&&ready("shield")){shieldHits=0;cooldown("shield",280);begin(State.SHIELD_RAISE,12);return;}
            if(p!=null&&b.getTarget()==p&&b.state()==State.FIST_WINDUP&&distanceToSqr(p)<25&&ready("intercept")&&elapsed()<18){cooldown("intercept",340);interceptUsed=false;begin(State.PROTECTOR_DASH,14);getNavigation().moveTo(p.getX()+b.heading().x*1.5,p.getY(),p.getZ()+b.heading().z*1.5,.37/.28);return;}
            if(p!=null&&p.getHealth()<p.getMaxHealth()*.6&&b.getTarget()==p&&ready("taunt")&&distanceToSqr(b)<144&&hasLineOfSight(b)){begin(State.TAUNT_CAST,28);return;}
            if(b.ownerActive(owner)&&ready("shockwave")&&edge(b)<4.5&&(b.state()==State.CHARGE_WINDUP||w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(4.5),m->m.isAlive()&&!isAlliedTo(m)).size()>2)){cooldown("shockwave",500);begin(State.SHOCKWAVE_CAST,46);return;}
            if(b.ownerActive(owner)&&edge(b)<=3.3&&ready("mace")){cooldown("mace",42);begin(State.MACE_WINDUP,31);return;}
            if(tickCount%10==0){setTarget(b);begin(State.PURSUE,0);Vec3 at=b.position().subtract(b.heading().scale(3.4));getNavigation().moveTo(at.x,at.y,at.z,1);}
        }else if(b==null&&p!=null&&p.level()==level()&&tickCount%10==0){
            LivingEntity enemy=w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(10),m->m.isAlive()&&!isAlliedTo(m)&&!(m instanceof org.slavicmyths.earth.EarthSpirit)&&m.getTarget()==p).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if(enemy!=null){setTarget(enemy);face(enemy);if(p.getHealth()<p.getMaxHealth()*.6&&ready("taunt")&&distanceToSqr(enemy)<=144&&hasLineOfSight(enemy)){begin(State.TAUNT_CAST,28);return;}if(ready("shockwave")&&w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(4.5),m->m.isAlive()&&!isAlliedTo(m)&&distanceToSqr(m)<=20.25).size()>2){cooldown("shockwave",500);begin(State.SHOCKWAVE_CAST,46);return;}if(edge(enemy)<3.3&&ready("mace")){cooldown("mace",42);begin(State.MACE_WINDUP,31);}else getNavigation().moveTo(enemy,1);}
            else{setTarget(null);begin(State.FOLLOW,0);if(distanceToSqr(p)>64)getNavigation().moveTo(p,1);if(distanceToSqr(p)>784&&getNavigation().isDone()){var at=BogatyrLedger.safe(w,p.position(),.93,2.45,6);if(at!=null)teleportTo(at.x,at.y,at.z);}}
        }
        if(getDeltaMovement().horizontalDistanceSqr()>.0004&&tickCount%11==5)cue("step_heavy_01");if(tickCount%400==0&&random.nextInt(30)==0)cue("ambient_breath_01");
    }
    private void step(ServerLevel w,IdolishcheEntity b){int t=elapsed();switch(state()){
        case TAUNT_CAST->{if(t==15){taunt(w,b,false);cooldown("taunt",400);}}
        case UNYIELDING->{if(t==16)taunt(w,b,true);}
        case SHIELD_RAISE->{if(t==8)cue("shield_raise_01");}
        case PROTECTOR_DASH->{if(b!=null)face(b);}
        case MACE_WINDUP->{if(t==13)cue("mace_swing_01");if(t==19){var target=getTarget();if(target!=null&&target.isAlive()&&cone(target,3.3,120,heading())&&hasLineOfSight(target)){target.hurt(damageSources().mobAttack(this),target instanceof IdolishcheEntity&&target.getUUID().equals(jointStrikeBoss)&&w.getGameTime()<=jointStrikeUntil?15:target instanceof IdolishcheEntity?10:12);if(target instanceof IdolishcheEntity giant)giant.heroMace(this);else target.knockback(.55,getX()-target.getX(),getZ()-target.getZ());cue("mace_impact_01");jointStrikeBoss=null;}}}
        case SHOCKWAVE_CAST->{if(t==29){for(var enemy:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(4.5),e->e.isAlive()&&e!=this&&!isAlliedTo(e)&&(e instanceof Monster||e instanceof IdolishcheEntity)))if(distanceToSqr(enemy)<20.25&&hasLineOfSight(enemy)){enemy.hurt(damageSources().mobAttack(this),enemy instanceof IdolishcheEntity?5:8);if(enemy instanceof IdolishcheEntity giant&&giant.state()==State.CHARGE_WINDUP)giant.begin(State.STUNNED,10);else if(!(enemy instanceof IdolishcheEntity))enemy.knockback(.45,getX()-enemy.getX(),getZ()-enemy.getZ());}dust(25);cue("mace_impact_01");}}
        default->{}
    }}
    private void finish(){switch(state()){case SHIELD_RAISE,PROTECTOR_DASH->begin(State.SHIELD_HOLD,64);case SHIELD_HOLD->begin(State.SHIELD_RECOVER,10);default->begin(State.GUARD,0);}}
    private void taunt(ServerLevel w,IdolishcheEntity b,boolean crisis){cue("taunt_nonverbal_01");if(b!=null&&distanceToSqr(b)<=144&&hasLineOfSight(b))b.taunt(this,crisis);int count=0;for(var m:w.getEntitiesOfClass(Monster.class,getBoundingBox().inflate(12),m->m.isAlive()&&!isAlliedTo(m)&&distanceToSqr(m)<=144&&hasLineOfSight(m))){if(count++>=5)break;m.setTarget(this);m.getPersistentData().putLong("BogatyrTauntUntil",w.getGameTime()+120);m.getPersistentData().putUUID("BogatyrTauntHero",getUUID());}dust(14);}
    public float mitigation(net.minecraft.world.damagesource.DamageSource source,float before){if(source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)||source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)||source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)||source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)||source.is(net.minecraft.tags.DamageTypeTags.IS_DROWNING)||source.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING)||source.is(net.minecraft.world.damagesource.DamageTypes.STARVE)||source.getMsgId().contains("breath"))return 1;float scale=.75F;if(level().getGameTime()<unyieldingUntil)scale*=.75F;var at=source.getSourcePosition();if(shield()&&at!=null&&at.subtract(position()).multiply(1,0,1).normalize().dot(heading())>=Math.cos(Math.toRadians(65))){boolean massive=source.getMsgId().contains("stomp")||source.getMsgId().contains("grasp");scale*=massive?.8F:source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)?.15F:.4F;if(ready("block_sound")){cue(random.nextBoolean()?"shield_block_01":"shield_block_02");cooldown("block_sound",6);}if(before>=6&&++shieldHits>=3)begin(State.SHIELD_RECOVER,24);}return scale;}
    public boolean canIntercept(ServerPlayer p,net.minecraft.world.damagesource.DamageSource source){if(!shield()||interceptUsed||!p.getUUID().equals(owner)||distanceToSqr(p)>6.25||source.getSourcePosition()==null)return false;var toward=source.getSourcePosition().subtract(position()).multiply(1,0,1).normalize();var behind=p.position().subtract(position()).multiply(1,0,1).normalize();return toward.dot(heading())>=Math.cos(Math.toRadians(65))&&behind.dot(heading())<=-Math.cos(Math.toRadians(22.5))&&!source.getMsgId().contains("stomp")&&!source.getMsgId().contains("breath")&&!source.getMsgId().contains("grasp");}
    private void startRetreat(IdolishcheEntity b){getNavigation().stop();setTarget(null);begin(State.RETREAT_START,28);cue("retreat_breath_01");refuge=null;var w=(ServerLevel)level();Vec3 away=position().subtract(b.origin).multiply(1,0,1).normalize();if(away.lengthSqr()<.1)away=new Vec3(1,0,0);for(int r:new int[]{30,34,38}){var at=BogatyrLedger.safe(w,b.origin.add(away.scale(r)),.93,2.45,6);if(at!=null&&at.distanceToSqr(b.origin)>=24*24){var path=getNavigation().createPath(net.minecraft.core.BlockPos.containing(at),0);if(path!=null&&path.canReach()){refuge=at;break;}}}recoveryEnd=0;}
    private void recovery(ServerLevel w,IdolishcheEntity b,ServerPlayer p){int t=elapsed();switch(state()){
        case RETREAT_START->{if(t>=28){begin(State.RETREAT_MOVING,60);if(refuge!=null)getNavigation().moveTo(refuge.x,refuge.y,refuge.z,1.2);}}
        case RETREAT_MOVING->{if(refuge!=null&&distanceToSqr(refuge)<2){begin(State.HEAL_DRINK,54);recoveryEnd=w.getGameTime()+54+BogatyrRules.RECOVERY_TICKS;}else if(t>=60){if(refuge==null&&p!=null)refuge=BogatyrLedger.safe(w,b==null?p.position():b.origin.add(34,0,0),.93,2.45,6);if(refuge!=null&&(b==null||refuge.distanceToSqr(b.origin)>=576)){dust(14);teleportTo(refuge.x,refuge.y,refuge.z);begin(State.HEAL_DRINK,54);recoveryEnd=w.getGameTime()+54+BogatyrRules.RECOVERY_TICKS;}else begin(State.DOWN_FOR_ENCOUNTER,0);}}
        case HEAL_DRINK->{if(t==14)cue("herbal_drink_01");if(t>=54)begin(State.RECOVERY,BogatyrRules.RECOVERY_TICKS);}
        case RECOVERY->{if(t%20==0)heal(2);if(t%240==0)cue("recover_breath_01");if(w.getGameTime()>=recoveryEnd&&getHealth()>=140){if(b==null||!returned){returned=true;begin(State.RETURNING,35);cue("return_nonverbal_01");}else begin(State.DOWN_FOR_ENCOUNTER,0);}}
        case DOWN_FOR_ENCOUNTER->{if(b==null){begin(State.RECOVERY,1100);recoveryEnd=w.getGameTime()+1100;}}
        default->{}
    }}
    @Override public boolean isAlliedTo(Entity e){return e instanceof IlyaEntity||owner!=null&&(owner.equals(e.getUUID())||e instanceof net.minecraft.world.entity.TamableAnimal a&&owner.equals(a.getOwnerUUID()))||super.isAlliedTo(e);}
    @Override public void die(net.minecraft.world.damagesource.DamageSource source){if(level() instanceof ServerLevel w&&owner!=null){var d=BogatyrLedger.get(w);if(getUUID().equals(d.heroes.get(owner))){d.heroes.remove(owner);d.setDirty();}}super.die(source);}
    @Override protected void dropAllDeathLoot(ServerLevel w,net.minecraft.world.damagesource.DamageSource s){}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("Owner",owner);if(encounter!=null)n.putUUID("Encounter",encounter);if(bossId!=null)n.putUUID("Boss",bossId);n.putBoolean("UnyieldingUsed",unyieldingUsed);n.putBoolean("Returned",returned);n.putLong("UnyieldingUntil",unyieldingUntil);n.putLong("RecoveryEnd",recoveryEnd);if(refuge!=null){n.putDouble("RefugeX",refuge.x);n.putDouble("RefugeY",refuge.y);n.putDouble("RefugeZ",refuge.z);}}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;encounter=n.hasUUID("Encounter")?n.getUUID("Encounter"):null;bossId=n.hasUUID("Boss")?n.getUUID("Boss"):null;unyieldingUsed=n.getBoolean("UnyieldingUsed");returned=n.getBoolean("Returned");unyieldingUntil=n.getLong("UnyieldingUntil");recoveryEnd=n.getLong("RecoveryEnd");if(n.contains("RefugeX"))refuge=new Vec3(n.getDouble("RefugeX"),n.getDouble("RefugeY"),n.getDouble("RefugeZ"));try{var s=State.valueOf(n.getString("CombatState"));if(BogatyrRules.recovering(s))resume(s,Math.max(1,n.getInt("CombatDuration")),n.getInt("CombatElapsed"));}catch(IllegalArgumentException ignored){}}
}
