package org.slavicmyths.bogatyr;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.bogatyr.BogatyrRules.State;

/** Entity-local encounter, marked vanilla minions, locked headings and bounded hit budgets. */
public final class IdolishcheEntity extends CombatEntity {
    private static final EntityDataAccessor<Boolean> ACTIVE=SynchedEntityData.defineId(IdolishcheEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> ORIGIN=SynchedEntityData.defineId(IdolishcheEntity.class,EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> RAGE=SynchedEntityData.defineId(IdolishcheEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> HEADING=SynchedEntityData.defineId(IdolishcheEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> VICTIM=SynchedEntityData.defineId(IdolishcheEntity.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.idolishche_poganoe"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
    public UUID encounter=UUID.randomUUID(),initiator;
    public Vec3 origin=Vec3.ZERO;
    public boolean active,debug=true,intro,recorded;
    public int feeds,hunger;
    private long startTick,lastPlayerHit,lastDanger,tauntUntil,tauntImmune,comboUntil,lastValidTick,introStart;
    private final Map<UUID,Long> poisonAt=new HashMap<>();
    private final Map<UUID,Integer> poisonTicks=new HashMap<>();
    private UUID victim,minion,taunted,contestHero;
    private final Map<UUID,Long> playerHits=new HashMap<>();
    private record Pressure(long tick,UUID target,float damage){}
    private final Deque<Pressure> pressure=new ArrayDeque<>();
    public boolean ownerActive(UUID owner){return owner!=null&&level().getGameTime()-playerHits.getOrDefault(owner,-10000L)<=240;}
    public void outgoingHit(LivingEntity target,float damage){long now=level().getGameTime();while(!pressure.isEmpty()&&now-pressure.peekFirst().tick>300)pressure.removeFirst();pressure.addLast(new Pressure(now,target.getUUID(),damage));}
    private boolean staticTank(){float total=0,hero=0;long now=level().getGameTime();for(var e:pressure)if(now-e.tick<=300){total+=e.damage;if(level() instanceof ServerLevel w&&w.getEntity(e.target) instanceof IlyaEntity)hero+=e.damage;}return total>=40&&hero/total>.7F;}
    private Vec3 locked=new Vec3(0,0,1);
    private float interruptMeter,heroInterrupt;
    private final Map<UUID,Long> contributions=new HashMap<>(),grabImmunity=new HashMap<>();
    private final Set<UUID> alliesAtStart=new HashSet<>(),minions=new HashSet<>(),hit=new HashSet<>();
    public String lastDamageReport="No measured player hit";
    public final IdolishcheHand rightHand=new IdolishcheHand(this,true);
    public final IdolishcheHand leftHand=new IdolishcheHand(this,false);
    private final net.neoforged.neoforge.entity.PartEntity<?>[] parts={rightHand,leftHand};
    public boolean handHit;private boolean resetOnLoad;
    public boolean encounterActive(){return entityData.get(ACTIVE);}
    public Vec3 arenaOrigin(){return entityData.get(ORIGIN).getBottomCenter();}
    @Override public boolean isMultipartEntity(){return true;}
    @Override public net.neoforged.neoforge.entity.PartEntity<?>[] getParts(){return parts;}
    @Override public void setId(int id){super.setId(id);if(rightHand!=null)rightHand.setId(id+1);if(leftHand!=null)leftHand.setId(id+2);}
    @Override public boolean canBeCollidedWith(){return isAlive();}
    public IdolishcheEntity(EntityType<? extends IdolishcheEntity> t,Level w){super(t,w);xpReward=0;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTIVE,false);b.define(ORIGIN,BlockPos.ZERO);b.define(RAGE,false);b.define(HEADING,0F);b.define(VICTIM,-1);}
    public boolean rage(){return entityData.get(RAGE);}
    public float lockedYaw(){return entityData.get(HEADING);}
    public int victimId(){return entityData.get(VICTIM);}
    public boolean grabbed(LivingEntity e){return e!=null&&e.getUUID().equals(victim);}
    public boolean dangerous(){return switch(state()){case FIST_WINDUP,STOMP_TELEGRAPH,GRAB_START,BREATH_WINDUP,CHARGE_WINDUP->true;default->false;};}
    public void start(ServerPlayer p,boolean debug){this.debug=debug;initiator=p.getUUID();origin=position();active=true;entityData.set(ACTIVE,true);entityData.set(ORIGIN,BlockPos.containing(origin));startTick=level().getGameTime();lastValidTick=startTick;lastPlayerHit=startTick;encounter=UUID.randomUUID();if(!debug)for(var h:level().getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(40),h->h.isAlive()&&h.owner!=null)){h.bind(this);alliesAtStart.add(h.owner);}cue("roar_01");}
    public void contribution(ServerPlayer p){if(active&&!p.isCreative()&&!p.isSpectator())contributions.put(p.getUUID(),level().getGameTime());}
    private boolean valid(LivingEntity t){if(t==null||!t.isAlive()||t.level()!=level()||t.position().distanceToSqr(origin)>72*72)return false;if(t instanceof ServerPlayer p)return !p.isSpectator()&&!p.isCreative();if(debug&&org.slavicmyths.bogatyr.HeroThreats.availableHero(t))return true;return t instanceof IlyaEntity h&&!h.recovering()&&encounter.equals(h.encounter);}
    private List<LivingEntity> targets(double r){return level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(r),this::valid);}
    public void taunt(IlyaEntity h,boolean crisis){long now=level().getGameTime();if(now<tauntImmune&&!crisis)return;taunted=h.getUUID();tauntUntil=now+(now<tauntImmune?60:120);tauntImmune=tauntUntil+260;setTarget(h);}
    public void heroMace(IlyaEntity h){if(state()==State.FEED_CHEW&&elapsed()>=12&&elapsed()<=38){heroInterrupt+=40;if(interruptMeter>=8)interruptFeed();}if(victim!=null&&level() instanceof ServerLevel w&&w.getEntity(victim) instanceof ServerPlayer&&h.position().distanceToSqr(hand())<=20.25){release(true);begin(State.STUNNED,16);}}
    public static DamageSource source(LivingEntity owner,String attack){var type=ResourceKey.create(Registries.DAMAGE_TYPE,BogatyrContent.id("idolishche_"+attack));return new DamageSource(owner.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type),owner);}
    @Override public void begin(State s,int d){super.begin(s,d);if(s==State.STOMP_TELEGRAPH||s==State.CHARGE_WINDUP||s==State.BREATH_WINDUP){hit.clear();locked=heading();entityData.set(HEADING,getYRot());}if(s==State.FEED_CHEW){interruptMeter=0;heroInterrupt=0;}}
    @Override public void tick(){super.tick();var hand=hand();rightHand.setPos(hand.x,hand.y-.7,hand.z);var leftWristPosition=position().add(BogatyrMotion.leftWrist(state(),age(0),tickCount,getYRot()));leftHand.setPos(leftWristPosition.x,leftWristPosition.y-.7,leftWristPosition.z);if(!(level() instanceof ServerLevel w)||!isAlive())return;bar.setProgress(Math.clamp(getHealth()/900,0,1));bar.setName(Component.translatable("entity.slavicmyths.idolishche_poganoe").append("  "+(int)getHealth()+" / 900"));
        if(resetOnLoad){resetOnLoad=false;reset();}
        if(!active){if(tickCount%10==0){if(debug){var h=org.slavicmyths.bogatyr.HeroThreats.debugOpponent(this,24);if(h!=null&&org.slavicmyths.bogatyr.HeroThreats.availableHero(h)){var owner=org.slavicmyths.kalin.KalinParty.owner(h);start(w.getServer().getPlayerList().getPlayer(owner),true);return;}}var p=w.getNearestPlayer(this,24);if(p instanceof ServerPlayer sp&&!sp.isCreative()&&!sp.isSpectator())start(sp,debug);}return;}
        if(tickCount%10==0){boolean any=false;for(var p:w.getEntitiesOfClass(ServerPlayer.class,new AABB(origin,origin).inflate(72),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator())){double d=p.position().multiply(1,0,1).distanceToSqr(origin.multiply(1,0,1));if(d<=26*26){any=true;bar.addPlayer(p);}else{bar.removePlayer(p);if(BogatyrConfig.HINTS.get())p.displayClientMessage(Component.translatable("gui.slavicmyths.171.leave_arena"),true);}}if(debug&&org.slavicmyths.bogatyr.HeroThreats.debugOpponent(this,26)!=null)any=true;if(any)lastValidTick=w.getGameTime();if(w.getGameTime()-lastValidTick>=100){reset();return;}
            if(!intro){for(var h:w.getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(24),h->encounter.equals(h.encounter))){intro=true;introStart=w.getGameTime();for(var p:bar.getPlayers()){p.sendSystemMessage(Component.translatable("dialogue.slavicmyths.171.ilya"));}h.cue("taunt_nonverbal_01");break;}}
        }
        if(introStart>0&&w.getGameTime()-introStart>=56){for(var p:bar.getPlayers())p.sendSystemMessage(Component.translatable("dialogue.slavicmyths.171.idol"));introStart=0;}
        for(var id:new ArrayList<>(poisonAt.keySet()))if(w.getGameTime()>=poisonAt.get(id)){if(w.getEntity(id) instanceof LivingEntity e&&valid(e))e.hurt(w.damageSources().indirectMagic(this,this),1);int left=poisonTicks.getOrDefault(id,0)-1;if(left<=0){poisonAt.remove(id);poisonTicks.remove(id);}else{poisonTicks.put(id,left);poisonAt.put(id,w.getGameTime()+40);}}
        if(!rage()&&BogatyrRules.rage(getHealth())&&victim==null){entityData.set(RAGE,true);getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(.24*1.18);begin(State.RAGE_TRANSITION,65);}
        if(victim!=null)holdVictim(w);
        step(w);if(duration()>0&&elapsed()>=duration())finish(w);
        if(duration()>0)return;
        if(tickCount%8==0){LivingEntity t=null;if(taunted!=null&&w.getGameTime()<tauntUntil&&w.getEntity(taunted) instanceof LivingEntity h&&valid(h))t=h;
            if(staticTank())t=targets(36).stream().filter(e->e instanceof ServerPlayer&&hasLineOfSight(e)).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(t);
            if(t==null)t=targets(36).stream().filter(this::hasLineOfSight).min(Comparator.comparingDouble(e->distanceToSqr(e)+(e instanceof IlyaEntity?35:0))).orElse(null);setTarget(t);if(t!=null){face(t);choose(w,t);}else if(tickCount%16==0)getNavigation().stop();}
        if(tickCount%16==0&&valid(getTarget())&&duration()==0){var t=getTarget();if(edge(t)>3.6&&position().distanceToSqr(origin)<23*23)getNavigation().moveTo(t,1);else getNavigation().stop();}
        if(getDeltaMovement().horizontalDistanceSqr()>.0005&&tickCount%18==9)cue("step_heavy_01");if(tickCount%900==0)cue(random.nextBoolean()?"idle_growl_01":"idle_growl_02");
        if((w.getGameTime()-startTick)%1200==0&&w.getGameTime()-lastPlayerHit>=240)hunger=Math.min(2,hunger+1);
    }
    private void choose(ServerLevel w,LivingEntity t){float wanted=(float)Math.toDegrees(Math.atan2(t.getZ()-getZ(),t.getX()-getX()))-90;if(Math.abs(net.minecraft.util.Mth.wrapDegrees(wanted-getYRot()))>25){begin(State.ACQUIRE_TARGET,18);return;}long age=w.getGameTime()-startTick;if(w.getGameTime()-lastDanger<20)return;
        var food=ownMinions(w).stream().filter(z->distanceToSqr(z)<=64).findFirst().orElse(null);
        if(getHealth()<765&&feeds<2&&food!=null&&ready("feed")){minion=food.getUUID();cooldown("feed",800);begin(State.FEED_GRAB,24);return;}
        if(feeds<2&&age>=240&&ready("summon")&&ownMinions(w).size()<5&&(food==null||hunger==2)){cooldown("summon",840);begin(State.FEED_SUMMON,50);return;}
        if(distanceToSqr(t)>=81&&distanceToSqr(t)<=529&&ready("charge")){cooldown("charge",340);begin(State.CHARGE_WINDUP,28);return;}
        if(age>=80&&distanceToSqr(t)<225&&ready("stomp")){cooldown("stomp",300);begin(State.STOMP_TELEGRAPH,32);return;}
        if(edge(t)<6&&ready("grab")&&w.getGameTime()>=grabImmunity.getOrDefault(t.getUUID(),0L)){cooldown("grab",360);begin(State.GRAB_START,25);return;}
        if(edge(t)<11&&ready("breath")){cooldown("breath",420);begin(State.BREATH_WINDUP,25);return;}
        if(edge(t)<4.8&&ready("fist")){begin(State.FIST_WINDUP,22);}
    }
    private void step(ServerLevel w){int t=elapsed();switch(state()){
        case ACQUIRE_TARGET->{if(valid(getTarget()))face(getTarget());}
        case FIST_SLAM->{if(t==8){cue("punch_impact_01");for(var e:targets(5))if(cone(e,4.8,95,heading())&&e.getY()<getY()+3&&hasLineOfSight(e))damage(e,"fist",20, .8);dust(25);lastDanger=w.getGameTime();}}
        case STOMP_PULSE->{if(t==4||t==10||t==16){int ring=(t-4)/6;cue(switch(ring){case 0->"stomp_near_01";case 1->"stomp_mid_01";default->"stomp_outer_01";});for(var e:targets(16)){double r=e.position().multiply(1,0,1).distanceTo(originContact().multiply(1,0,1));int zone=r<=5?0:r<=10?1:r<=15?2:-1;if(zone!=ring||hit.contains(e.getUUID())||!groundVisible(e))continue;hit.add(e.getUUID());float raw=BogatyrRules.stompRaw(ring);if(e.getY()>getY()+.55&&!e.onGround())raw*=.45F;damage(e,"stomp",raw,.65);}lastDanger=w.getGameTime();dust(40);}}
        case GRAB_HOLD->{if(t==8&&victim==null){var target=getTarget();if(valid(target)&&cone(target,6,62,heading())&&hasLineOfSight(target)){victim=target.getUUID();entityData.set(VICTIM,target.getId());damage(target,"grasp",18,0);cue("grasp_01");}}}
        case GRAB_THROW->{if(t==12){cue("throw_01");release(false);lastDanger=w.getGameTime();}}
        case FEED_SUMMON->{if(t==29){summon(w);cue("minions_summon_01");}}
        case FEED_GRAB->{if(minion!=null&&w.getEntity(minion) instanceof Zombie z){z.getNavigation().stop();z.setNoAi(true);z.setDeltaMovement(Vec3.ZERO);if(t>=14)z.teleportTo(hand().x,hand().y,hand().z);}else begin(State.IDLE,0);}
        case FEED_CHEW->{if(minion!=null&&w.getEntity(minion) instanceof Zombie z){var mouth=position().add(heading().scale(1.65)).add(0,7.45,0);var at=hand().lerp(mouth,Math.clamp(t/14.0,0,1));z.teleportTo(at.x,at.y,at.z);if(t==18)cue("eat_01");if(t==40){if(feeds<2){heal(45);feeds++;}z.discard();minions.remove(minion);minion=null;hunger=0;}}else if(t<40)begin(State.IDLE,0);}
        case BREATH_ACTIVE->{if(t==1)cue("poison_breath_01");if(t%10==0){for(var e:targets(13))if(cone(e,11,75,locked)&&hasLineOfSight(e)&&e.getY()<getY()+4&&!hit.contains(e.getUUID())){hit.add(e.getUUID());damage(e,"breath",20,0);e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60));e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80));e.addEffect(new MobEffectInstance(MobEffects.CONFUSION,50));poisonAt.put(e.getUUID(),w.getGameTime()+40);poisonTicks.put(e.getUUID(),2);}w.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(net.minecraft.core.particles.ParticleTypes.ENTITY_EFFECT,0xFF879F43),getX()+locked.x*4,getY()+2,getZ()+locked.z*4,14,2,1,2,.01);}lastDanger=w.getGameTime();}
        case CHARGE_RUNNING->{if(t==1)cue("rush_charge_01");setYRot(lockedYaw());yBodyRot=getYRot();Vec3 next=position().add(locked.scale(.31));if(!w.hasChunkAt(BlockPos.containing(next))||!w.getWorldBorder().isWithinBounds(BlockPos.containing(next))){begin(State.SHIELD_RECOVER,15);return;}setDeltaMovement(locked.scale(.31).add(0,getDeltaMovement().y,0));for(var e:targets(.8))if(!hit.contains(e.getUUID())&&getBoundingBox().inflate(.4).intersects(e.getBoundingBox())){hit.add(e.getUUID());if(e instanceof IlyaEntity h&&h.shield()&&h.ready("contest")&&h.heading().dot(locked)<-.42){h.cooldown("contest",280);h.hurt(source(this,"charge"),32);setDeltaMovement(Vec3.ZERO);contestHero=h.getUUID();begin(State.CONTEST,24);return;}damage(e,"charge",22,1.1);lastDanger=w.getGameTime();}if(horizontalCollision&&thickWall(w)){setDeltaMovement(Vec3.ZERO);begin(State.STUNNED,50);}dust(14);}
        case CONTEST->{setDeltaMovement(Vec3.ZERO);if(t==6&&contestHero!=null&&w.getEntity(contestHero) instanceof IlyaEntity h){var delta=locked.scale(.35);if(w.noCollision(h,h.getBoundingBox().move(delta)))h.setPos(h.position().add(delta));}if(t==12&&BogatyrConfig.HINTS.get())for(var p:bar.getPlayers())p.displayClientMessage(Component.translatable("gui.slavicmyths.171.combo_hint"),true);}
        case RAGE_TRANSITION->{if(t==30){cue("rage_01");for(var p:bar.getPlayers())p.displayClientMessage(Component.translatable("gui.slavicmyths.171.boss.rage"),true);}}
        default->{}
    }}
    private void finish(ServerLevel w){switch(state()){
        case FIST_WINDUP->begin(State.FIST_SLAM,17);
        case FIST_SLAM->{cooldown("fist",rage()?61:72);begin(State.SHIELD_RECOVER,16);}
        case STOMP_TELEGRAPH->begin(State.STOMP_PULSE,22);
        case GRAB_START->begin(State.GRAB_HOLD,18);
        case GRAB_HOLD->begin(State.GRAB_THROW,24);
        case FEED_GRAB->begin(State.FEED_CHEW,52);
        case BREATH_WINDUP->begin(State.BREATH_ACTIVE,60);
        case CHARGE_WINDUP->begin(State.CHARGE_RUNNING,44);
        case CHARGE_RUNNING->begin(State.SHIELD_RECOVER,15);
        case CONTEST->begin(State.STUNNED,16);
        default->begin(State.CHASE,0);
    }}
        @Override public void face(LivingEntity t){getLookControl().setLookAt(t,20,20);float wanted=(float)Math.toDegrees(Math.atan2(t.getZ()-getZ(),t.getX()-getX()))-90;setYRot(net.minecraft.util.Mth.approachDegrees(getYRot(),wanted,8));yBodyRot=getYRot();}
    public Vec3 originContact(){return position().add(heading().scale(1));}
    public Vec3 hand(){return position().add(BogatyrMotion.wrist(state(),age(0),tickCount,getYRot()));}
    private void holdVictim(ServerLevel w){if(w.getEntity(victim) instanceof LivingEntity e&&valid(e)){var h=hand();e.setDeltaMovement(Vec3.ZERO);e.fallDistance=0;e.teleportTo(h.x,h.y,h.z);}else release(true);}
    public void release(boolean rescued){if(victim!=null&&level() instanceof ServerLevel w&&w.getEntity(victim) instanceof LivingEntity e){grabImmunity.put(victim,w.getGameTime()+240);if(rescued){var at=position().add(heading().scale(4.8));e.teleportTo(at.x,getY()+.35,at.z);e.fallDistance=0;}else{e.setDeltaMovement(heading().scale(.9).add(0,.48,0));e.hurtMarked=true;e.getPersistentData().putLong("IdolThrowUntil",w.getGameTime()+80);e.getPersistentData().putUUID("IdolThrowBoss",getUUID());}if(e instanceof IlyaEntity h&&rescued)h.begin(State.SHIELD_RECOVER,10);}victim=null;entityData.set(VICTIM,-1);interruptMeter=0;}
    public void struggle(ServerPlayer p){if(grabbed(p)&&ready("struggle")&&(state()==State.GRAB_HOLD&&elapsed()>=8||state()==State.GRAB_THROW&&elapsed()<12)){cooldown("struggle",3);interruptMeter+=5;if(interruptMeter>=30){release(true);begin(State.STUNNED,16);}}}
    private boolean thickWall(ServerLevel w){var p=BlockPos.containing(position().add(locked.scale(2.3)).add(0,1,0));var q=BlockPos.containing(position().add(locked.scale(3.3)).add(0,1,0));return w.hasChunkAt(p)&&w.hasChunkAt(q)&&w.getBlockState(p).isCollisionShapeFullBlock(w,p)&&w.getBlockState(q).isCollisionShapeFullBlock(w,q);}
    private void damage(LivingEntity e,String attack,float raw,double knock){if(rage()&&!attack.equals("breath"))raw*=1.1F;if(e.hurt(source(this,attack),raw)&&knock>0)e.knockback(knock,getX()-e.getX(),getZ()-e.getZ());}
    private List<Zombie> ownMinions(ServerLevel w){var result=new ArrayList<Zombie>();minions.removeIf(id->!(w.getEntity(id) instanceof Zombie z)||!z.isAlive());for(var id:minions)if(w.getEntity(id) instanceof Zombie z)result.add(z);return result;}
    private void summon(ServerLevel w){int remaining=Math.min(5-ownMinions(w).size(),rage()?4:2+random.nextInt(2));for(int i=0;i<12&&remaining>0;i++){double a=i*Math.PI/6;var at=BogatyrLedger.safe(w,position().add(Math.cos(a)*6,0,Math.sin(a)*6),.6,1.95,3);if(at==null||at.distanceToSqr(origin)>26*26||!w.getEntitiesOfClass(ServerPlayer.class,new AABB(at,at).inflate(3)).isEmpty())continue;var z=EntityType.ZOMBIE.create(w);if(z==null)continue;z.moveTo(at.x,at.y,at.z,0,0);z.setPersistenceRequired();z.getPersistentData().putUUID("IdolOwner",getUUID());z.getPersistentData().putUUID("IdolEncounter",encounter);z.getPersistentData().putBoolean("feedEligible",true);z.setCanPickUpLoot(false);if(getTarget()!=null)z.setTarget(getTarget());if(w.addFreshEntity(z)){minions.add(z.getUUID());remaining--;}}}
    private boolean groundVisible(LivingEntity e){
        Vec3 from=position().add(0,.5,0),to=e.position().add(0,.5,0);
        for(int i=0;i<=16;i++)if(!level().hasChunkAt(BlockPos.containing(from.lerp(to,i/16.0))))return false;
        return level().clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;
    }
    public void registeredHit(DamageSource s,float raw,float actual){if(actual<=0)return;if(s.getEntity() instanceof ServerPlayer p){contribution(p);playerHits.put(p.getUUID(),level().getGameTime());lastPlayerHit=level().getGameTime();if(state()==State.FEED_CHEW&&elapsed()>=12&&elapsed()<=38){interruptMeter+=raw;if(interruptMeter>=32||heroInterrupt>=40&&interruptMeter>=8)interruptFeed();}if(victim!=null&&handHit){interruptMeter+=raw;if(interruptMeter>=25){release(true);begin(State.STUNNED,16);}}if(state()==State.STUNNED&&duration()==50&&ready("combo")){var h=level().getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(4),h1->h1.isAlive()&&!h1.recovering()&&h1.edge(this)<=3.3&&hasLineOfSight(h1)&&encounter.equals(h1.encounter)).stream().findFirst().orElse(null);if(h!=null){cooldown("combo",700);comboUntil=level().getGameTime()+24;h.jointStrikeBoss=getUUID();h.jointStrikeUntil=comboUntil;h.setTarget(this);h.face(this);h.begin(State.MACE_WINDUP,31);h.cooldown("mace",42);p.getPersistentData().putLong("IdolComboUntil",comboUntil);p.getPersistentData().putUUID("IdolComboBoss",getUUID());}}}if(ready("hurt_sound")){cooldown("hurt_sound",32);cue(random.nextBoolean()?"hurt_01":"hurt_02");}}
    private void interruptFeed(){if(level() instanceof ServerLevel w&&minion!=null&&w.getEntity(minion) instanceof Zombie z){z.setNoAi(false);var at=position().add(heading().scale(5));z.teleportTo(at.x,getY(),at.z);}minion=null;hunger=0;begin(State.STUNNED,16);}
    private void clean(ServerLevel w){release(true);for(var id:minions)if(w.getEntity(id) instanceof Zombie z)z.discard();minions.clear();for(var h:w.getEntitiesOfClass(IlyaEntity.class,getBoundingBox().inflate(80),h->encounter.equals(h.encounter)))h.endEncounter();bar.removeAllPlayers();poisonAt.clear();poisonTicks.clear();}
    public void reset(){if(level() instanceof ServerLevel w){clean(w);active=false;entityData.set(ACTIVE,false);feeds=0;hunger=0;contributions.clear();playerHits.clear();pressure.clear();alliesAtStart.clear();intro=false;setTarget(null);entityData.set(RAGE,false);setHealth(900);getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(.24);cooldowns.clear();begin(State.IDLE,0);}}
    @Override public void die(DamageSource s){boolean advancementWasDead=dead;org.slavicmyths.progression.AdvancementMilestones.beginDeath(this);if(level() instanceof ServerLevel w){if(!debug&&!recorded&&!s.is(DamageTypeTags.BYPASSES_INVULNERABILITY)){recorded=true;for(var e:contributions.entrySet()){var p=w.getServer().getPlayerList().getPlayer(e.getKey());if(p!=null&&!p.isCreative()&&!p.isSpectator()&&p.level()==w&&p.position().distanceToSqr(origin)<=72*72&&w.getGameTime()-e.getValue()<=900)BogatyrLedger.get(w).victory(p,this,alliesAtStart.contains(p.getUUID()));}}clean(w);active=false;entityData.set(ACTIVE,false);}super.die(s);begin(State.DEATH,110);cue("death_01");org.slavicmyths.progression.AdvancementMilestones.endDeath(this,!advancementWasDead&&dead,s);}
    @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}
    @Override protected void tickDeath(){deathTime++;if(deathTime==65)dust(40);if(deathTime>=110&&!level().isClientSide)remove(RemovalReason.KILLED);}
    @Override public void onRemovedFromLevel(){bar.setVisible(false);bar.removeAllPlayers();super.onRemovedFromLevel();}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putUUID("Encounter",encounter);if(initiator!=null)n.putUUID("Initiator",initiator);n.putDouble("OriginX",origin.x);n.putDouble("OriginY",origin.y);n.putDouble("OriginZ",origin.z);n.putBoolean("Debug",debug);n.putBoolean("Recorded",recorded);n.putBoolean("Rage",rage());n.putInt("Feeds",feeds);n.putBoolean("WasActive",active);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);encounter=n.hasUUID("Encounter")?n.getUUID("Encounter"):UUID.randomUUID();initiator=n.hasUUID("Initiator")?n.getUUID("Initiator"):null;origin=new Vec3(n.getDouble("OriginX"),n.getDouble("OriginY"),n.getDouble("OriginZ"));debug=n.getBoolean("Debug");recorded=n.getBoolean("Recorded");feeds=n.getInt("Feeds");entityData.set(RAGE,n.getBoolean("Rage"));active=false;resetOnLoad=n.getBoolean("WasActive");entityData.set(ORIGIN,BlockPos.containing(origin));/* Restarts abort the encounter. Own orphan minions reconcile on entity join. */}
}

