package org.slavicmyths.kurgan;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.chat.Component;

import java.util.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
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
import net.minecraft.core.registries.BuiltInRegistries;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.kurgan.KurganFighter.*;

/** One exclusive combat goal; six fixed move sets with server-authoritative releases. */
public final class KurganCreature extends Monster {
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.INT),CLONES=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE_START=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> GUARD=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> DECOY1=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.OPTIONAL_BLOCK_POS),DECOY2=SynchedEntityData.defineId(KurganCreature.class,EntityDataSerializers.OPTIONAL_BLOCK_POS);
    public final Kind kind; public UUID kurgan;public int room=-1;public BlockPos home;
    private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
    private int actionTick,basicWait,blocks,memory,cloneTime,alignWait;private boolean summoned,riposte,dashHit;
    private Vec3 aim=Vec3.ZERO;private BlockPos lastSeen,sealTarget;
    private final ServerBossEvent bar=new ServerBossEvent(Component.literal(""),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
    public KurganCreature(EntityType<? extends KurganCreature> type,Level world,Kind kind){super(type,world);this.kind=kind;xpReward=kind.boss()?50:8;bar.setName(getDisplayName());}
    public static AttributeSupplier.Builder attributes(Kind k){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.FOLLOW_RANGE,k.range).add(Attributes.ARMOR,k.armor).add(Attributes.KNOCKBACK_RESISTANCE,k.resistance);}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ACTION,0);builder.define(START,0);builder.define(PHASE,0);builder.define(PHASE_START,-100);builder.define(GUARD,false);builder.define(CLONES,0);builder.define(DECOY1,Optional.empty());builder.define(DECOY2,Optional.empty());}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(6,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<Player>(this,Player.class,true).setUnseenMemoryTicks(120));}
    public Move action(){return Move.values()[entityData.get(ACTION)];}public int phase(){return entityData.get(PHASE);}public boolean guarding(){return entityData.get(GUARD);}
    public float elapsed(float partial){return Math.max(0,(int)level().getGameTime()-entityData.get(START)+partial);}
    public float phaseElapsed(float partial){return Math.max(0,(int)level().getGameTime()-entityData.get(PHASE_START)+partial);}
    public boolean clones(){return entityData.get(CLONES)>(int)level().getGameTime();}public Optional<BlockPos> decoy(int i){return entityData.get(i==0?DECOY1:DECOY2);}
    private SoundEvent sound(String cue){return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",kind.id+"_"+cue));}
    private void cue(String name){SoundEvent s=sound(name);if(s!=null)playSound(s,.8F,.9F+random.nextFloat()*.1F);}
    @Override protected SoundEvent getAmbientSound(){return sound("ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource s){return sound("hurt");}
    @Override protected SoundEvent getDeathSound(){return sound("death");}
    @Override protected void playStepSound(BlockPos p,BlockState s){SoundEvent sound=sound("step");if(sound!=null)playSound(sound,.3F,.9F);}
    @Override public int getAmbientSoundInterval(){return kind==Kind.NAV?180:240;}
    @Override public boolean removeWhenFarAway(double distance){return kurgan==null&&!kind.boss();}
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);if(kind.boss())bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void onRemovedFromLevel(){bar.removeAllPlayers();super.onRemovedFromLevel();}
    @Override public void tick(){Vec3 before=position();super.tick();if(!level().isClientSide){if(kind==Kind.PRINCE&&kurgan!=null&&!allowed(position())&&allowed(before)){teleportTo(before.x,before.y,before.z);setDeltaMovement(Vec3.ZERO);navigation.stop();}if(kind.boss())bar.setProgress(getHealth()/getMaxHealth());}}
    @Override public boolean hurt(DamageSource source,float damage){
        Vec3 point=source.getSourcePosition();boolean frontal=point!=null&&getLookAngle().multiply(1,0,1).normalize().dot(point.subtract(position()).multiply(1,0,1).normalize())>.25;
        if(!level().isClientSide&&guarding()&&frontal&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)){
            damage*=kind==Kind.DRUZHINNIK?.4F:.3F;blocks++;if(blocks>=3){blocks=0;riposte=true;}
            cue(kind==Kind.VOEVODA?"shield_impact":"shield_block");
        }return super.hurt(source,damage);
    }
    private boolean ready(Move m){return cooldowns.getOrDefault(m,0)<=0;}
    private void begin(Move move,LivingEntity target){entityData.set(ACTION,move.ordinal());entityData.set(START,(int)level().getGameTime());actionTick=0;alignWait=0;dashHit=false;entityData.set(GUARD,false);navigation.stop();aim=target.position().subtract(position()).multiply(1,0,1).normalize();sealTarget=target.blockPosition();cooldowns.put(move,move.cooldown);
        String cue=kind==Kind.UPYR?(move==Move.LEAP?"leap":move==Move.BITE?"bite":"attack"):
            kind==Kind.NAV?(move==Move.SHIFT?"shift":"attack"):
            kind==Kind.DRUZHINNIK?(move==Move.BASH?"shield_bash":"sword_attack"):
            kind==Kind.VOEVODA?(move==Move.CHARGE?"shield_charge":move==Move.SWEEP?"axe_attack":"axe_attack"):
            kind==Kind.VOLKHV?(move==Move.BOLT?"cast_basic":move==Move.CLONES?"cast_clones":move==Move.SEAL?"cast_ground_seal":"summon"):
            move==Move.SUMMON?"summon":move==Move.GRAB?"grab":move==Move.HEAVY?"heavy_strike":"combo";cue(cue);
    }
    private Move choose(double distance,boolean sight){
        if(kind==Kind.VOLKHV){if(!summoned&&sight&&getHealth()<=getMaxHealth()*.5)return Move.SUMMON;if(ready(Move.CLONES)&&sight&&distance<10)return Move.CLONES;if(ready(Move.SEAL)&&sight&&distance<14)return Move.SEAL;if(ready(Move.BOLT)&&sight&&distance<=20)return Move.BOLT;return Move.IDLE;}
        if(!sight)return Move.IDLE;
        if(kind==Kind.UPYR){if(distance>3&&distance<9&&ready(Move.LEAP))return Move.LEAP;if(distance<2.2&&ready(Move.BITE))return Move.BITE;return distance<2.3&&basicWait<=0?Move.CLAW:Move.IDLE;}
        if(kind==Kind.NAV){if(distance<7&&ready(Move.SHIFT))return Move.SHIFT;return distance<2.3&&basicWait<=0?Move.TOUCH:Move.IDLE;}
        if(kind==Kind.DRUZHINNIK){if(riposte&&distance<3){riposte=false;return Move.RIPOSTE;}if(distance<8&&distance>2&&ready(Move.BASH))return Move.BASH;return distance<2.7&&basicWait<=0?Move.SWORD:Move.IDLE;}
        if(kind==Kind.VOEVODA){if(distance>3&&distance<11&&ready(Move.CHARGE))return Move.CHARGE;if(distance<3.5&&ready(Move.SWEEP))return Move.SWEEP;return distance<3&&basicWait<=0?Move.AXE:Move.IDLE;}
        if(phase()==0&&!summoned)return Move.SUMMON;
        if(phase()==2&&distance<4&&ready(Move.GRAB))return Move.GRAB;
        if(distance>3&&distance<12&&ready(Move.RUSH))return Move.RUSH;
        if(phase()>0&&distance<4&&ready(Move.HEAVY))return Move.HEAVY;
        return distance<3.3&&basicWait<=0?Move.COMBO:Move.IDLE;
    }
    private boolean allowed(Vec3 pos){if(kurgan==null)return true;KurganInstance i=BurialRecords.get((ServerLevel)level()).instances.get(kurgan);if(i==null)return true;BlockPos p=BlockPos.containing(pos);return kind==Kind.PRINCE?i.plan.rooms.get(i.plan.finalRoom).box().contains(p.getX()-i.origin.getX(),p.getY()-i.origin.getY(),p.getZ()-i.origin.getZ()):i.contains(p);}
    private void dash(){Vec3 next=position().add(aim.scale(.75));if(allowed(next)){setDeltaMovement(aim.scale(kind==Kind.UPYR?.72:.65).add(0,getDeltaMovement().y,0));hasImpulse=true;}}
    private boolean strike(LivingEntity target,float damage,double reach,double cone,boolean bash){if(!target.isAlive()||distanceTo(target)>reach||!getSensing().hasLineOfSight(target))return false;Vec3 direction=target.position().subtract(position()).multiply(1,0,1).normalize();if(aim.dot(direction)<cone)return false;
        if(bash&&target instanceof Player&&target.isBlocking()){((Player)target).getCooldowns().addCooldown(Items.SHIELD,60);target.stopUsingItem();}
        boolean hit=target.hurt(damageSources().mobAttack(this),damage);if(bash)target.knockback(.9F, -aim.x,-aim.z);return hit;
    }
    private void release(LivingEntity target){Move m=action();float damage=(float)kind.damage;basicWait=basicInterval(kind,phase());
        if(m==Move.CLAW||m==Move.SWORD||m==Move.AXE||m==Move.TOUCH)basicWait=Math.max(0,basicWait-m.windup-1);
        switch(m){
            case CLAW:case SWORD:case AXE:case COMBO:strike(target,damage,kind.boss()?3.3:2.7,-.1,false);break;
            case TOUCH:if(strike(target,damage,2.5,-.2,false))target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0));break;
            case BITE:if(strike(target,damage+2,2.3,.4,false)){heal(4);target.getPersistentData().putLong("SlavicUpyrBiteUntil",level().getGameTime()+100);}break;
            case SHIFT:reposition(target,false);cue("spectral_appear");break;
            case CLONES:reposition(target,true);break;
            case BOLT:KurganBolt bolt=new KurganBolt(ModEntities.KURGAN_BOLT.get(),level());bolt.setOwner(this);bolt.setPos(getX(),getEyeY()-.25,getZ());Vec3 delta=target.position().add(0,target.getBbHeight()*.55,0).subtract(bolt.position());bolt.shoot(delta.x,delta.y,delta.z,1F,.5F);level().addFreshEntity(bolt);break;
            case SEAL:areaSeal();break;
            case SUMMON:summoned=true;KurganEncounters.summon(this,kind==Kind.PRINCE?3:2+random.nextInt(2));break;
            case SWEEP:for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(3.5)))strike(p,damage+2,3.5,-.35,true);break;
            case HEAVY:if(strike(target,damage+4,3.8,.65,true))cue("heavy_strike");break;
            case GRAB:if(strike(target,8,3,.5,true)){heal(12);target.setDeltaMovement(position().subtract(target.position()).normalize().scale(.6).add(0,.1,0));target.hurtMarked=true;}break;
            default:break;
        }
    }
    private void reposition(LivingEntity target,boolean clones){Vec3 old=position(),side=new Vec3(-aim.z,0,aim.x);if(random.nextBoolean())side=side.scale(-1);BlockPos p=KurganEncounters.safePosition(this,old.add(side.scale(3)).subtract(aim.scale(1.5)),true);
        if(p==null)return;Vec3 newPos=new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5);if(!clearLine(old.add(0,1,0),newPos.add(0,1,0)))return;
        if(clones){BlockPos other=KurganEncounters.safePosition(this,old.subtract(side.scale(3)),true);if(other==null||other.distSqr(BlockPos.containing(old))<2||other.distSqr(p)<2||!clearLine(old.add(0,1,0),new Vec3(other.getX()+.5,other.getY()+1,other.getZ()+.5)))return;entityData.set(DECOY1,Optional.of(BlockPos.containing(old)));entityData.set(DECOY2,Optional.of(other));entityData.set(CLONES,(int)level().getGameTime()+60);}
        teleportTo(newPos.x,newPos.y,newPos.z);
    }
    public boolean clearLine(Vec3 a,Vec3 b){return level().clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    private void sealParticles(){if(sealTarget==null)return;ServerLevel world=(ServerLevel)level();for(int i=0;i<12;i++){double a=i*Math.PI*2/12;world.sendParticles(ParticleTypes.SOUL,sealTarget.getX()+.5+Math.cos(a)*2.3,sealTarget.getY()+.1,sealTarget.getZ()+.5+Math.sin(a)*2.3,1,0,.02,0,0);}}
    private void areaSeal(){if(sealTarget==null)return;Vec3 center=new Vec3(sealTarget.getX()+.5,sealTarget.getY()+.2,sealTarget.getZ()+.5);for(Player p:level().getEntitiesOfClass(Player.class,new AABB(sealTarget).inflate(2.5,2,2.5)))if(p.isAlive()&&p.position().distanceTo(center)<3&&clearLine(center.add(0,.5,0),p.position().add(0,1,0))&&p.hurt(damageSources().mobAttack(this),8))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50,2));}
    private final class Fight extends Goal {
        Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public boolean canUse(){LivingEntity t=getTarget();return t!=null&&t.isAlive();}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void stop(){navigation.stop();entityData.set(ACTION,0);entityData.set(GUARD,false);}
        @Override public void tick(){LivingEntity target=getTarget();if(target==null)return;for(Move m:new ArrayList<>(cooldowns.keySet()))cooldowns.put(m,Math.max(0,cooldowns.get(m)-1));basicWait=Math.max(0,basicWait-1);
            int nextPhase=Math.max(phase(),KurganFighter.phase(kind,getHealth()/getMaxHealth()));if(nextPhase!=phase()){entityData.set(PHASE,nextPhase);entityData.set(PHASE_START,(int)level().getGameTime());getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind.speed+(kind==Kind.VOEVODA?.02:nextPhase*.025));cue("phase_shift");}
            boolean sight=getSensing().hasLineOfSight(target);if(sight){lastSeen=target.blockPosition();memory=120;}else if(--memory<=0){setTarget(null);return;}
            if(action()!=Move.IDLE){entityData.set(GUARD,false);actionTick++;Move move=action();if(move==Move.SEAL&&actionTick<move.windup&&actionTick%4==0)sealParticles();
                if(actionTick<=move.windup){
                    boolean facing=org.slavicmyths.hunt.DashFacing.turn(KurganCreature.this,aim,18);
                    if(actionTick==move.windup&&!facing){if(++alignWait<=20){actionTick--;return;}entityData.set(ACTION,0);basicWait=Math.max(basicWait,12);return;}
                    if(actionTick==move.windup)org.slavicmyths.hunt.DashFacing.lock(KurganCreature.this,aim);
                }
                if(actionTick==move.windup)release(target);
                if((move==Move.LEAP||move==Move.BASH||move==Move.CHARGE||move==Move.RUSH||move==Move.RIPOSTE)&&actionTick>=move.windup&&actionTick<move.windup+6){dash();if(!dashHit&&distanceTo(target)<2.7&&getSensing().hasLineOfSight(target)){strike(target,(float)(move==Move.BASH||move==Move.CHARGE?kind.damage*.6:kind.damage),2.7,.45,true);dashHit=true;}}
                int spacing=10-phase()*2;
                if(move==Move.COMBO&&(actionTick==move.windup+spacing||actionTick==move.windup+spacing*2))strike(target,(float)kind.damage,3.3,.2,false);
                if(actionTick>=move.windup+move.recovery-(move==Move.COMBO?phase()*4:0)){entityData.set(ACTION,0);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind.speed+(kind==Kind.VOEVODA&&phase()>0?.02:kind==Kind.PRINCE?phase()*.025:0));}
                return;
            }
            double distance=distanceTo(target);Move selected=choose(distance,sight);if(selected!=Move.IDLE){begin(selected,target);return;}
            lookControl.setLookAt(target,35,35);
            boolean shield=(kind==Kind.DRUZHINNIK||kind==Kind.VOEVODA||kind==Kind.PRINCE)&&distance<6&&(tickCount%36<(phase()>0?6:12));entityData.set(GUARD,shield);
            if(tickCount%8==0){Vec3 destination=lastSeen==null?target.position():new Vec3(lastSeen.getX()+.5,lastSeen.getY(),lastSeen.getZ()+.5);
                if(kind==Kind.VOLKHV&&distance<5){Vec3 away=position().subtract(target.position()).multiply(1,0,1).normalize();BlockPos retreat=KurganEncounters.safePosition(KurganCreature.this,position().add(away.scale(3)),true);if(retreat!=null)destination=new Vec3(retreat.getX()+.5,retreat.getY(),retreat.getZ()+.5);}
                else if(kind==Kind.NAV&&distance<6)destination=destination.add(Math.sin(tickCount*.12)*1.5,0,Math.cos(tickCount*.12)*1.5);
                if(kind==Kind.DRUZHINNIK&&distance>3){
                    Vec3 forward=target.position().subtract(position()).multiply(1,0,1).normalize();
                    double offset=(Math.floorMod(getId(),3)-1)*.9;
                    BlockPos spaced=KurganEncounters.safePosition(KurganCreature.this,destination.add(-forward.z*offset,0,forward.x*offset),false);
                    if(spaced!=null)destination=new Vec3(spaced.getX()+.5,spaced.getY(),spaced.getZ()+.5);
                }
                if(allowed(destination))navigation.moveTo(destination.x,destination.y,destination.z,phase()>0?1.12:1.0);else if(home!=null)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);
            }
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(kurgan!=null)n.putUUID("Kurgan",kurgan);if(home!=null)n.putLong("KurganHome",home.asLong());n.putInt("KurganRoom",room);n.putBoolean("LimitedSummon",summoned);n.putInt("CombatPhase",phase());CompoundTag cd=new CompoundTag();for(Map.Entry<Move,Integer> e:cooldowns.entrySet())cd.putInt(e.getKey().name(),e.getValue());n.put("KurganCooldowns",cd);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);kurgan=n.hasUUID("Kurgan")?n.getUUID("Kurgan"):null;home=n.contains("KurganHome")?BlockPos.of(n.getLong("KurganHome")):null;room=n.contains("KurganRoom")?n.getInt("KurganRoom"):-1;summoned=n.getBoolean("LimitedSummon");entityData.set(PHASE,Math.max(0,Math.min(2,n.getInt("CombatPhase"))));CompoundTag cd=n.getCompound("KurganCooldowns");for(Move m:Move.values())cooldowns.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));}
    @Override public void die(DamageSource source){boolean wasDead=dead;super.die(source);if(!wasDead&&dead&&!level().isClientSide)KurganEncounters.died(this);}
}
