package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.kurgan.KurganFighter.*;

/** One exclusive combat goal; six fixed move sets with server-authoritative releases. */
public final class KurganCreature extends MonsterEntity {
    private static final DataParameter<Integer> ACTION=EntityDataManager.defineId(KurganCreature.class,DataSerializers.INT),START=EntityDataManager.defineId(KurganCreature.class,DataSerializers.INT),PHASE=EntityDataManager.defineId(KurganCreature.class,DataSerializers.INT),CLONES=EntityDataManager.defineId(KurganCreature.class,DataSerializers.INT);
    private static final DataParameter<Integer> PHASE_START=EntityDataManager.defineId(KurganCreature.class,DataSerializers.INT);
    private static final DataParameter<Boolean> GUARD=EntityDataManager.defineId(KurganCreature.class,DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> DECOY1=EntityDataManager.defineId(KurganCreature.class,DataSerializers.OPTIONAL_BLOCK_POS),DECOY2=EntityDataManager.defineId(KurganCreature.class,DataSerializers.OPTIONAL_BLOCK_POS);
    public final Kind kind; public UUID kurgan;public int room=-1;public BlockPos home;
    private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
    private int actionTick,basicWait,blocks,memory,cloneTime;private boolean summoned,riposte,dashHit;
    private Vector3d aim=Vector3d.ZERO;private BlockPos lastSeen,sealTarget;
    private final ServerBossInfo bar=new ServerBossInfo(new net.minecraft.util.text.StringTextComponent(""),BossInfo.Color.RED,BossInfo.Overlay.PROGRESS);
    public KurganCreature(EntityType<? extends KurganCreature> type,World world,Kind kind){super(type,world);this.kind=kind;xpReward=kind.boss()?50:8;bar.setName(getDisplayName());}
    public static AttributeModifierMap.MutableAttribute attributes(Kind k){return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.FOLLOW_RANGE,k.range).add(Attributes.ARMOR,k.armor).add(Attributes.KNOCKBACK_RESISTANCE,k.resistance);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(ACTION,0);entityData.define(START,0);entityData.define(PHASE,0);entityData.define(PHASE_START,-100);entityData.define(GUARD,false);entityData.define(CLONES,0);entityData.define(DECOY1,Optional.empty());entityData.define(DECOY2,Optional.empty());}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(5,new LookAtGoal(this,PlayerEntity.class,8));goalSelector.addGoal(6,new LookRandomlyGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<PlayerEntity>(this,PlayerEntity.class,true).setUnseenMemoryTicks(120));}
    @Override public CreatureAttribute getMobType(){return CreatureAttribute.UNDEAD;}
    public Move action(){return Move.values()[entityData.get(ACTION)];}public int phase(){return entityData.get(PHASE);}public boolean guarding(){return entityData.get(GUARD);}
    public float elapsed(float partial){return Math.max(0,(int)level.getGameTime()-entityData.get(START)+partial);}
    public float phaseElapsed(float partial){return Math.max(0,(int)level.getGameTime()-entityData.get(PHASE_START)+partial);}
    public boolean clones(){return entityData.get(CLONES)>(int)level.getGameTime();}public Optional<BlockPos> decoy(int i){return entityData.get(i==0?DECOY1:DECOY2);}
    private SoundEvent sound(String cue){return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("slavicmyths",kind.id+"_"+cue));}
    private void cue(String name){SoundEvent s=sound(name);if(s!=null)playSound(s,.8F,.9F+random.nextFloat()*.1F);}
    @Override protected SoundEvent getAmbientSound(){return sound("ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource s){return sound("hurt");}
    @Override protected SoundEvent getDeathSound(){return sound("death");}
    @Override protected void playStepSound(BlockPos p,BlockState s){SoundEvent sound=sound("step");if(sound!=null)playSound(sound,.3F,.9F);}
    @Override public int getAmbientSoundInterval(){return kind==Kind.NAV?180:240;}
    @Override public boolean removeWhenFarAway(double distance){return kurgan==null&&!kind.boss();}
    @Override public void startSeenByPlayer(ServerPlayerEntity p){super.startSeenByPlayer(p);if(kind.boss())bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayerEntity p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void onRemovedFromWorld(){bar.removeAllPlayers();super.onRemovedFromWorld();}
    @Override public void tick(){Vector3d before=position();super.tick();if(!level.isClientSide){if(kind==Kind.PRINCE&&kurgan!=null&&!allowed(position())&&allowed(before)){teleportTo(before.x,before.y,before.z);setDeltaMovement(Vector3d.ZERO);navigation.stop();}if(kind.boss())bar.setPercent(getHealth()/getMaxHealth());}}
    @Override public boolean hurt(DamageSource source,float damage){
        Vector3d point=source.getSourcePosition();boolean frontal=point!=null&&getLookAngle().multiply(1,0,1).normalize().dot(point.subtract(position()).multiply(1,0,1).normalize())>.25;
        if(!level.isClientSide&&guarding()&&frontal&&!source.isBypassInvul()&&!source.isBypassArmor()){
            damage*=kind==Kind.DRUZHINNIK?.4F:.3F;blocks++;if(blocks>=3){blocks=0;riposte=true;}
            cue(kind==Kind.VOEVODA?"shield_impact":"shield_block");
        }return super.hurt(source,damage);
    }
    private boolean ready(Move m){return cooldowns.getOrDefault(m,0)<=0;}
    private void begin(Move move,LivingEntity target){entityData.set(ACTION,move.ordinal());entityData.set(START,(int)level.getGameTime());actionTick=0;dashHit=false;entityData.set(GUARD,false);navigation.stop();aim=target.position().subtract(position()).multiply(1,0,1).normalize();sealTarget=target.blockPosition();cooldowns.put(move,move.cooldown);
        String cue=kind==Kind.UPYR?(move==Move.LEAP?"leap":move==Move.BITE?"bite":"attack"):
            kind==Kind.NAV?(move==Move.SHIFT?"shift":"attack"):
            kind==Kind.DRUZHINNIK?(move==Move.BASH?"shield_bash":"sword_attack"):
            kind==Kind.VOEVODA?(move==Move.CHARGE?"shield_charge":move==Move.SWEEP?"axe_attack":"axe_attack"):
            kind==Kind.VOLKHV?(move==Move.BOLT?"cast_basic":move==Move.CLONES?"cast_clones":move==Move.SEAL?"cast_ground_seal":"summon"):
            move==Move.SUMMON?"summon":move==Move.GRAB?"grab":move==Move.HEAVY?"heavy_strike":"combo";cue(cue);
    }
    private Move choose(double distance,boolean sight){
        if(kind==Kind.VOLKHV){if(!summoned&&getHealth()<=getMaxHealth()*.5)return Move.SUMMON;if(ready(Move.CLONES)&&distance<10)return Move.CLONES;if(ready(Move.SEAL)&&sight&&distance<14)return Move.SEAL;if(ready(Move.BOLT)&&sight&&distance<=20)return Move.BOLT;return Move.IDLE;}
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
    private boolean allowed(Vector3d pos){if(kurgan==null)return true;KurganInstance i=BurialRecords.get((ServerWorld)level).instances.get(kurgan);if(i==null)return true;BlockPos p=new BlockPos(pos);return kind==Kind.PRINCE?i.plan.rooms.get(i.plan.finalRoom).box().contains(p.getX()-i.origin.getX(),p.getY()-i.origin.getY(),p.getZ()-i.origin.getZ()):i.contains(p);}
    private void dash(){Vector3d next=position().add(aim.scale(.75));if(allowed(next)){setDeltaMovement(aim.scale(kind==Kind.UPYR?.72:.65).add(0,getDeltaMovement().y,0));hasImpulse=true;}}
    private boolean strike(LivingEntity target,float damage,double reach,double cone,boolean bash){if(!target.isAlive()||distanceTo(target)>reach||!getSensing().canSee(target))return false;Vector3d direction=target.position().subtract(position()).multiply(1,0,1).normalize();if(aim.dot(direction)<cone)return false;
        if(bash&&target instanceof PlayerEntity&&target.isBlocking()){((PlayerEntity)target).getCooldowns().addCooldown(Items.SHIELD,60);target.stopUsingItem();}
        boolean hit=target.hurt(DamageSource.mobAttack(this),damage);if(bash)target.knockback(.9F, -aim.x,-aim.z);return hit;
    }
    private void release(LivingEntity target){Move m=action();float damage=(float)kind.damage;basicWait=basicInterval(kind,phase());
        if(m==Move.CLAW||m==Move.SWORD||m==Move.AXE||m==Move.TOUCH)basicWait=Math.max(0,basicWait-m.windup-1);
        switch(m){
            case CLAW:case SWORD:case AXE:case COMBO:strike(target,damage,kind.boss()?3.3:2.7,-.1,false);break;
            case TOUCH:if(strike(target,damage,2.5,-.2,false))target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,40,0));break;
            case BITE:if(strike(target,damage+2,2.3,.4,false)){heal(4);target.getPersistentData().putLong("SlavicUpyrBiteUntil",level.getGameTime()+100);}break;
            case SHIFT:reposition(target,false);cue("spectral_appear");break;
            case CLONES:reposition(target,true);break;
            case BOLT:KurganBolt bolt=new KurganBolt(ModEntities.KURGAN_BOLT.get(),level);bolt.setOwner(this);bolt.setPos(getX(),getEyeY()-.25,getZ());Vector3d delta=target.position().add(0,target.getBbHeight()*.55,0).subtract(bolt.position());bolt.shoot(delta.x,delta.y,delta.z,1F,.5F);level.addFreshEntity(bolt);break;
            case SEAL:areaSeal();break;
            case SUMMON:summoned=true;KurganEncounters.summon(this,kind==Kind.PRINCE?3:2+random.nextInt(2));break;
            case SWEEP:for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(3.5)))strike(p,damage+2,3.5,-.35,true);break;
            case HEAVY:if(strike(target,damage+4,3.8,.65,true))cue("heavy_strike");break;
            case GRAB:if(strike(target,8,3,.5,true)){heal(12);target.setDeltaMovement(position().subtract(target.position()).normalize().scale(.6).add(0,.1,0));target.hurtMarked=true;}break;
            default:break;
        }
    }
    private void reposition(LivingEntity target,boolean clones){Vector3d old=position(),side=new Vector3d(-aim.z,0,aim.x);if(random.nextBoolean())side=side.scale(-1);BlockPos p=KurganEncounters.safePosition(this,old.add(side.scale(3)).subtract(aim.scale(1.5)),true);
        if(p==null)return;Vector3d newPos=new Vector3d(p.getX()+.5,p.getY(),p.getZ()+.5);if(!clearLine(old.add(0,1,0),newPos.add(0,1,0)))return;
        if(clones){BlockPos other=KurganEncounters.safePosition(this,old.subtract(side.scale(3)),true);if(other==null||other.distSqr(new BlockPos(old))<2||other.distSqr(p)<2||!clearLine(old.add(0,1,0),new Vector3d(other.getX()+.5,other.getY()+1,other.getZ()+.5)))return;entityData.set(DECOY1,Optional.of(new BlockPos(old)));entityData.set(DECOY2,Optional.of(other));entityData.set(CLONES,(int)level.getGameTime()+60);}
        teleportTo(newPos.x,newPos.y,newPos.z);
    }
    public boolean clearLine(Vector3d a,Vector3d b){return level.clip(new RayTraceContext(a,b,RayTraceContext.BlockMode.COLLIDER,RayTraceContext.FluidMode.NONE,this)).getType()==RayTraceResult.Type.MISS;}
    private void sealParticles(){if(sealTarget==null)return;ServerWorld world=(ServerWorld)level;for(int i=0;i<12;i++){double a=i*Math.PI*2/12;world.sendParticles(ParticleTypes.SOUL,sealTarget.getX()+.5+Math.cos(a)*2.3,sealTarget.getY()+.1,sealTarget.getZ()+.5+Math.sin(a)*2.3,1,0,.02,0,0);}}
    private void areaSeal(){if(sealTarget==null)return;Vector3d center=new Vector3d(sealTarget.getX()+.5,sealTarget.getY()+.2,sealTarget.getZ()+.5);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,new AxisAlignedBB(sealTarget).inflate(2.5,2,2.5)))if(p.isAlive()&&p.position().distanceTo(center)<3&&clearLine(center.add(0,.5,0),p.position().add(0,1,0))&&p.hurt(DamageSource.mobAttack(this),8))p.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,50,2));}
    private final class Fight extends Goal {
        Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){LivingEntity t=getTarget();return t!=null&&t.isAlive();}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void stop(){navigation.stop();entityData.set(ACTION,0);entityData.set(GUARD,false);}
        @Override public void tick(){LivingEntity target=getTarget();if(target==null)return;for(Move m:new ArrayList<>(cooldowns.keySet()))cooldowns.put(m,Math.max(0,cooldowns.get(m)-1));basicWait=Math.max(0,basicWait-1);
            int nextPhase=Math.max(phase(),KurganFighter.phase(kind,getHealth()/getMaxHealth()));if(nextPhase!=phase()){entityData.set(PHASE,nextPhase);entityData.set(PHASE_START,(int)level.getGameTime());getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind.speed+(kind==Kind.VOEVODA?.02:nextPhase*.025));cue("phase_shift");}
            boolean sight=getSensing().canSee(target);if(sight){lastSeen=target.blockPosition();memory=120;}else if(--memory<=0){setTarget(null);return;}
            if(action()!=Move.IDLE){entityData.set(GUARD,false);actionTick++;Move move=action();if(move==Move.SEAL&&actionTick<move.windup&&actionTick%4==0)sealParticles();
                if(actionTick==move.windup)release(target);
                if((move==Move.LEAP||move==Move.BASH||move==Move.CHARGE||move==Move.RUSH||move==Move.RIPOSTE)&&actionTick>=move.windup&&actionTick<move.windup+6){dash();if(!dashHit&&distanceTo(target)<2.7&&getSensing().canSee(target)){strike(target,(float)(move==Move.BASH||move==Move.CHARGE?kind.damage*.6:kind.damage),2.7,.45,true);dashHit=true;}}
                int spacing=10-phase()*2;
                if(move==Move.COMBO&&(actionTick==move.windup+spacing||actionTick==move.windup+spacing*2))strike(target,(float)kind.damage,3.3,.2,false);
                if(actionTick>=move.windup+move.recovery-(move==Move.COMBO?phase()*4:0)){entityData.set(ACTION,0);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind.speed+(kind==Kind.VOEVODA&&phase()>0?.02:kind==Kind.PRINCE?phase()*.025:0));}
                return;
            }
            double distance=distanceTo(target);Move selected=choose(distance,sight);if(selected!=Move.IDLE){begin(selected,target);return;}
            lookControl.setLookAt(target,35,35);
            boolean shield=(kind==Kind.DRUZHINNIK||kind==Kind.VOEVODA||kind==Kind.PRINCE)&&distance<6&&(tickCount%36<(phase()>0?6:12));entityData.set(GUARD,shield);
            if(tickCount%8==0){Vector3d destination=lastSeen==null?target.position():new Vector3d(lastSeen.getX()+.5,lastSeen.getY(),lastSeen.getZ()+.5);
                if(kind==Kind.VOLKHV&&distance<5){Vector3d away=position().subtract(target.position()).multiply(1,0,1).normalize();BlockPos retreat=KurganEncounters.safePosition(KurganCreature.this,position().add(away.scale(3)),true);if(retreat!=null)destination=new Vector3d(retreat.getX()+.5,retreat.getY(),retreat.getZ()+.5);}
                else if(kind==Kind.NAV&&distance<6)destination=destination.add(Math.sin(tickCount*.12)*1.5,0,Math.cos(tickCount*.12)*1.5);
                if(allowed(destination))navigation.moveTo(destination.x,destination.y,destination.z,phase()>0?1.12:1.0);else if(home!=null)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);
            }
        }
    }
    @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);if(kurgan!=null)n.putUUID("Kurgan",kurgan);if(home!=null)n.putLong("KurganHome",home.asLong());n.putInt("KurganRoom",room);n.putBoolean("LimitedSummon",summoned);n.putInt("CombatPhase",phase());CompoundNBT cd=new CompoundNBT();for(Map.Entry<Move,Integer> e:cooldowns.entrySet())cd.putInt(e.getKey().name(),e.getValue());n.put("KurganCooldowns",cd);}
    @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);kurgan=n.hasUUID("Kurgan")?n.getUUID("Kurgan"):null;home=n.contains("KurganHome")?BlockPos.of(n.getLong("KurganHome")):null;room=n.contains("KurganRoom")?n.getInt("KurganRoom"):-1;summoned=n.getBoolean("LimitedSummon");entityData.set(PHASE,Math.max(0,Math.min(2,n.getInt("CombatPhase"))));CompoundNBT cd=n.getCompound("KurganCooldowns");for(Move m:Move.values())cooldowns.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));}
    @Override public void die(DamageSource source){boolean wasDead=dead;super.die(source);if(!wasDead&&dead&&!level.isClientSide)KurganEncounters.died(this);}
}
