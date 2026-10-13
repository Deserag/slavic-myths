package org.slavicmyths.mor;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.slavicmyths.entity.*;
import org.slavicmyths.hunt.ElementHuntMob;
import org.slavicmyths.registry.ModEntities;

/** One controller per spirit. No world registry polling or chunk tickets. */
public final class MorController {
    private static final ResourceLocation HEALTH=id("mor_health"),DAMAGE=id("mor_damage"),SPEED=id("mor_speed"),FLIGHT=id("mor_flight"),FREEZE=id("mor_freeze");
    final LandSpiritEntity mob;
    final int[] cooldown=new int[10];
    boolean pendingNatural,clonesSummoned;
    private UUID parent,focus;
    public final HigherMor higher;
    private final List<UUID> children=new ArrayList<>(2);
    private int createdCount,cloneAge,parentMissing,castTick,meleeWait,unseen,retry;
    private boolean retried,cleaned,hitSoundPlayed;
    private final Map<UUID,BlockPos> childPositions=new HashMap<>();
    private final Map<UUID,Integer> childMissing=new HashMap<>();
    Vec3 aim=Vec3.ZERO,mark=Vec3.ZERO,origin=Vec3.ZERO;
    boolean diveHit;
    public MorController(LandSpiritEntity mob){this.mob=mob;this.higher=new HigherMor(mob);}
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    public int kind(){return mob instanceof LeshyEntity?1:mob instanceof PoludnitsaEntity?2:mob instanceof KikimoraEntity?3:mob.getType()==ModEntities.FIRE_SERPENT.get()?4:mob.getType()==ModEntities.LIKHO_ONE_EYED.get()?5:mob instanceof OvinnikEntity?6:0;}
    public boolean protectedMob(){return mob.hasCustomName()||mob.morHasFriend()||mob instanceof org.slavicmyths.hunt.HuntMob h&&h.owner!=null||mob.getPassengers().stream().anyMatch(e->e instanceof Player)||mob.getVehicle() instanceof Player||mob.getRootVehicle().getPassengers().stream().anyMatch(e->e instanceof Player)||mob.morClone();}
    public boolean apply(boolean natural){
        if(mob.level().isClientSide||!MorRules.canApply(kind(),mob.morState(),mob.isAlive(),protectedMob()))return false;
        mob.setInvisible(false);mob.getNavigation().stop();mob.setTarget(null);mob.setDeltaMovement(Vec3.ZERO);
        mob.morSync(natural?2:1,natural?0:50,0);mob.refreshDimensions();if(natural)attributes(true);else {freeze(true);MorAudio.transform(mob,kind(),false);}return true;
    }
    public void naturalCandidate(){pendingNatural=true;}
    public boolean pendingNatural(){return pendingNatural;}
    public void finalizedSpawn(){if(pendingNatural){pendingNatural=false;apply(true);}}
    private void modifier(net.minecraft.core.Holder<Attribute> a,ResourceLocation id,double amount){var value=mob.getAttribute(a);if(value!=null&&!value.hasModifier(id))value.addPermanentModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
    private void attributes(boolean natural){float old=mob.getMaxHealth(),hp=mob.getHealth();int k=kind();modifier(Attributes.MAX_HEALTH,HEALTH,k>=5?1:k==1||k==4?1.5:1);modifier(Attributes.ATTACK_DAMAGE,DAMAGE,k==1||k==4?.2:k==2?.12:.05);modifier(Attributes.MOVEMENT_SPEED,SPEED,k==1?-.05:k==2?.15:k==3?.2:.12);if(k==4)modifier(Attributes.FLYING_SPEED,FLIGHT,.12);mob.setHealth(natural?mob.getMaxHealth():MorRules.health(hp,old,mob.getMaxHealth()));}
    private void freeze(boolean on){var a=mob.getAttribute(Attributes.MOVEMENT_SPEED);if(on){if(!a.hasModifier(FREEZE))a.addTransientModifier(new AttributeModifier(FREEZE,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}else a.removeModifier(FREEZE);}
    public boolean tick(){
        finalizedSpawn();
        if(mob.morState()==0)return false;
        if(!mob.isAlive()){cleanup();return true;}
        if(mob.morState()==1){mob.getNavigation().stop();mob.setTarget(null);mob.setDeltaMovement(Vec3.ZERO);int remaining=MorRules.transformTick(mob.morRemaining());mob.morSync(remaining==0?2:1,remaining,0);if(remaining==0){freeze(false);attributes(false);MorAudio.transform(mob,kind(),true);burst(mob.position().add(0,.5,0),8,.3);}return true;}
        for(int i=1;i<10;i++)cooldown[i]=Math.max(0,cooldown[i]-1);meleeWait=Math.max(0,meleeWait-1);
        if(kind()>=5){higher.tick();return true;}
        if(mob.morClone()&&!cloneTick())return true;
        if(kind()==4&&!mob.morClone())refreshChildren();if(kind()==4)mob.morConcealed(hidesBar());
        if(mob.tickCount%40==0&&!mob.morClone())for(Player p:mob.level().getEntitiesOfClass(Player.class,mob.getBoundingBox().inflate(16),p->opponent(p)&&mob.distanceToSqr(p)<=256))if(p instanceof ServerPlayer s)org.slavicmyths.tales.TaleService.promote(s,"slavicmyths:mor_first_signs",2,false);
        if(mob.tickCount%240==0)MorAudio.idle(mob,kind());
        if(retry>0&&--retry==0)spawnClones();
        if(mob.morSkill()!=0){cast();return true;}
        if(mob.tickCount%5!=0)return true;
        LivingEntity target=mob.getTarget();double range=kind()==4?24:kind()==3?14:kind()==2?18:16;
        if(!opponent(target)||mob.distanceToSqr(target)>(kind()==4?32*32:16*16)){mob.setTarget(null);target=null;}
        if(target!=null){unseen=sight(target)?0:unseen+5;if(unseen>=200){mob.setTarget(null);target=null;}}
        if(target==null&&opponent(mob.getLastHurtByMob()))target=mob.getLastHurtByMob();
        if(target==null){target=mob.level().getNearestPlayer(mob.getX(),mob.getY(),mob.getZ(),range,p->p instanceof Player player&&opponent(player)&&sight(player));mob.setTarget(target);unseen=0;}
        if(target==null)return true;
        mob.getLookControl().setLookAt(target,30,30);double d=mob.distanceTo(target);boolean sight=sight(target);int skill=switch(kind()){case 1->MorLeshy.choose(this,target,d,sight);case 2->MorPoludnitsa.choose(this,target,d,sight);case 3->MorKikimora.choose(this,target,d,sight);case 4->MorSerpent.choose(this,target,d,sight);default->0;};
        if(skill!=0){begin(skill,target);return true;}
        if(d<=2.5&&sight&&meleeWait==0){mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);mob.doHurtTarget(target);meleeWait=30;}else if(kind()==4)safeVelocity(target.position().add(0,2,0).subtract(mob.position()).normalize().scale(mob.getAttributeValue(Attributes.FLYING_SPEED)));else mob.getNavigation().moveTo(target,1);
        return true;
    }
    private void begin(int skill,LivingEntity t){focus=t.getUUID();mark=t.position();origin=mob.position();aim=t.position().add(0,.5,0).subtract(mob.position()).normalize();castTick=0;diveHit=false;hitSoundPlayed=false;mob.getNavigation().stop();mob.setDeltaMovement(Vec3.ZERO);mob.morSync(2,0,skill);if(skill==9)clonesSummoned=true;MorAudio.start(mob,skill);}
    private void cast(){int s=mob.morSkill(),t=++castTick;LivingEntity target=((ServerLevel)mob.level()).getEntity(focus) instanceof LivingEntity e?e:null;
        if(!opponent(target)||mob.distanceToSqr(target)> (kind()==4?32*32:16*16)){finish(s,40);return;}
        MorAudio.tick(mob,s,t);mob.getNavigation().stop();if(t<=MorRules.windup(s))mob.setDeltaMovement(Vec3.ZERO);
        switch(kind()){case 1->MorLeshy.tick(this,target,t,s);case 2->MorPoludnitsa.tick(this,target,t,s);case 3->MorKikimora.tick(this,target,t,s);case 4->MorSerpent.tick(this,target,t,s);default->{}}
        if(t>=MorRules.duration(s))finish(s,MorRules.cooldown(s));
    }
    public void shuffle(){MorAudio.split(mob);safeVelocity(new Vec3(.16,0,.16));}
    private void finish(int skill,int cd){cooldown[skill]=cooldown[skill]>0?cooldown[skill]:cd;mob.morSync(2,0,0);castTick=0;focus=null;mob.setDeltaMovement(Vec3.ZERO);}
    void swing(LivingEntity target,float damage,double reach,float cone,boolean follow){if(mob.distanceTo(target)>reach||!sight(target)||aim.dot(target.position().subtract(mob.position()).multiply(1,0,1).normalize())<cone)return;if(follow&&target.getLastHurtByMob()==mob)target.invulnerableTime=Math.min(target.invulnerableTime,10);damage(target,damage);}
    void projectile(LivingEntity target,boolean thread,float damage){MorBolt bolt=new MorBolt(MorContent.BOLT.get(),mob.level());bolt.configure(mob,thread,damage);bolt.setPos(mob.getX(),mob.getEyeY()-.2,mob.getZ());Vec3 v=target.getBoundingBox().getCenter().subtract(bolt.position());bolt.shoot(v.x,v.y,v.z,thread?.65F:.7F,0);mob.level().addFreshEntity(bolt);}
    public static boolean valid(LivingEntity e){return e instanceof Player p&&p.isAlive()&&!p.isCreative()&&!p.isSpectator();}
    private boolean opponent(LivingEntity e){return (valid(e)||org.slavicmyths.bogatyr.HeroThreats.availableHero(e)&&mob.getLastHurtByMob()==e||org.slavicmyths.poloz.PolozMinions.enemy(mob,e))&&!mob.isAlliedTo(e);}
    List<Player> players(double radius){return mob.level().getEntitiesOfClass(Player.class,mob.getBoundingBox().inflate(radius),p->opponent(p)&&mob.distanceToSqr(p)<=radius*radius&&sight(p));}
    boolean damage(LivingEntity target,float amount){if(!opponent(target))return false;boolean hit=target.hurt(mob.damageSources().mobAttack(mob),amount);if(hit&&!hitSoundPlayed){sound(SoundEvents.PLAYER_ATTACK_STRONG,.5F,.75F);hitSoundPlayed=true;}return hit;}
    public static void hold(LivingEntity e,int ticks){if(!valid(e))return;e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,ticks,4));e.setDeltaMovement(e.getDeltaMovement().multiply(0,1,0));e.hurtMarked=true;}
    boolean ground(Vec3 p){BlockPos b=BlockPos.containing(p);return mob.level().hasChunkAt(b)&&mob.level().getFluidState(b).isEmpty()&&mob.level().getBlockState(b.below()).isSolid()&&mob.level().getBlockState(b).getCollisionShape(mob.level(),b).isEmpty();}
    void safeStep(Vec3 step,boolean jump){Vec3 v=step.add(0,jump?.35:0,0);if(!ground(mob.position().add(step)))return;if(clear(mob.position(),mob.position().add(v))&&loaded(mob.getBoundingBox().move(v))&&mob.level().noCollision(mob,mob.getBoundingBox().move(v)))mob.setDeltaMovement(step.scale(.18).add(0,jump?Math.min(.28,Math.sqrt(Math.max(0,.16*(.65-(mob.getY()-origin.y))))):0,0));mob.hasImpulse=true;}
    void safeVelocity(Vec3 v){Vec3 next=mob.position().add(v);if(mob.level().hasChunkAt(BlockPos.containing(next))&&mob.level().getWorldBorder().isWithinBounds(mob.getBoundingBox().move(v))&&loaded(mob.getBoundingBox().move(v))&&mob.level().noCollision(mob,mob.getBoundingBox().move(v))&&clear(mob.position(),next)){mob.setDeltaMovement(v);mob.hasImpulse=true;}else mob.setDeltaMovement(Vec3.ZERO);}
    public boolean clearForZone(Vec3 a,Vec3 b){return clear(a,b);}
    boolean sight(LivingEntity e){return e!=null&&clear(mob.getEyePosition(),e.getEyePosition());}
    private boolean loaded(AABB box){for(int x=net.minecraft.util.Mth.floor(box.minX)>>4;x<=net.minecraft.util.Mth.floor(box.maxX)>>4;x++)for(int z=net.minecraft.util.Mth.floor(box.minZ)>>4;z<=net.minecraft.util.Mth.floor(box.maxZ)>>4;z++)if(!mob.level().hasChunk(x,z))return false;return true;}
    private boolean clear(Vec3 a,Vec3 b){return loaded(new AABB(a,b).inflate(.05))&&mob.level().clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mob)).getType()==HitResult.Type.MISS;}
    void rootCue(boolean raised){for(int i=-1;i<=1;i++)if(ground(mark.add(i,0,i))){burst(mark.add(i,raised?.6:.05,i),2,raised?.15:.4);if(raised){MorBolt root=new MorBolt(MorContent.BOLT.get(),mob.level());root.configureRoot(mob);root.setPos(mark.add(i,0,i));mob.level().addFreshEntity(root);}}}
    void burst(Vec3 p,int count,double radius){((ServerLevel)mob.level()).sendParticles(MorContent.SPARK.get(),p.x,p.y,p.z,count,radius,.15,radius,0);}
    private void sound(net.minecraft.sounds.SoundEvent event,float volume,float pitch){mob.playSound(event,volume,pitch);}
    public boolean hidesBar(){return mob.morClone()||!children.isEmpty();}
    List<ElementHuntMob> localClones(){return mob.level().getEntitiesOfClass(ElementHuntMob.class,mob.getBoundingBox().inflate(96,32,96),e->e.morClone()&&mob.getUUID().equals(e.mor.parent));}
    void spawnClones(){if(mob.morClone()||!mob.isAlive())return;ServerLevel w=(ServerLevel)mob.level();for(var existing:localClones())if(!children.contains(existing.getUUID())&&children.size()<2){children.add(existing.getUUID());childPositions.put(existing.getUUID(),existing.blockPosition());}createdCount=Math.max(createdCount,children.size());for(int slot=createdCount;slot<2;slot++){ElementHuntMob clone=ModEntities.FIRE_SERPENT.get().create(w);if(clone==null)break;boolean placed=false;for(int i=0;i<10;i++){double angle=(i+slot*5)*Math.PI/5;Vec3 p=mob.position().add(Math.cos(angle)*(2+i%3),1+i%2,Math.sin(angle)*(2+i%3));clone.setPos(p);if(w.hasChunkAt(clone.blockPosition())&&w.getWorldBorder().isWithinBounds(clone.getBoundingBox())&&loaded(clone.getBoundingBox())&&w.noCollision(clone)){placed=true;break;}}if(!placed)for(int sign:new int[]{-1,1}){clone.setPos(mob.position().add(sign*1.5,.5,sign*.5));if(loaded(clone.getBoundingBox())&&w.getWorldBorder().isWithinBounds(clone.getBoundingBox())&&w.noCollision(clone)){placed=true;break;}}if(!placed)break;
            clone.mor.parent=mob.getUUID();clone.mor.clonesSummoned=true;clone.morSync(2,0,0);clone.morSetClone(true);clone.morConcealed(true);clone.mor.attributes(true);clone.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH);clone.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30);clone.setHealth(30);clone.setTarget(mob.getTarget());if(w.addFreshEntity(clone)){createdCount++;children.add(clone.getUUID());childPositions.put(clone.getUUID(),clone.blockPosition());}else break;}
        if(createdCount<2){if(!retried){retried=true;retry=20;}else com.mojang.logging.LogUtils.getLogger().warn("Mor clone safe placement failed for {}",mob.getUUID());}
    }
    private boolean cloneTick(){cloneAge++;if(cloneAge>=1800){notifyParent();mob.discard();return false;}if(mob.tickCount%20==0){Entity p=parent==null?null:((ServerLevel)mob.level()).getEntity(parent);if(p instanceof LandSpiritEntity original&&original.isAlive()&&mob.distanceToSqr(original)<=96*96){parentMissing=0;}else {parentMissing+=20;if(p!=null&&!p.isAlive()||parentMissing>=200){notifyParent();mob.discard();return false;}}}return true;}
    private void notifyParent(){if(parent!=null&&((ServerLevel)mob.level()).getEntity(parent) instanceof LandSpiritEntity p){p.mor.children.remove(mob.getUUID());p.mor.childPositions.remove(mob.getUUID());p.morConcealed(p.mor.hidesBar());}}
    public void cleanup(){if(mob.level().isClientSide||cleaned)return;cleaned=true;if(kind()>=5)for(var zone:mob.level().getEntitiesOfClass(MorZone.class,mob.getBoundingBox().inflate(32),z->mob.getUUID().equals(z.owner())))zone.discard();if(mob.morClone()){notifyParent();return;}if(kind()==4)for(var clone:localClones())clone.discard();}
    public void refreshChildren(){if(mob.tickCount%20!=0||children.isEmpty())return;ServerLevel w=(ServerLevel)mob.level();children.removeIf(id->{Entity e=w.getEntity(id);if(e!=null){childPositions.put(id,e.blockPosition());childMissing.remove(id);return !e.isAlive();}BlockPos at=childPositions.get(id);if(at!=null&&w.hasChunkAt(at))return childMissing.merge(id,20,Integer::sum)>=200;return false;});}
    public void save(CompoundTag tag){if(kind()==0)return;if(kind()>=5){higher.save(tag);}new MorSnapshot(mob.morState(),mob.morRemaining(),mob.morClone(),clonesSummoned,cloneAge,parent,cooldown,children,childPositions,mob.morSkill(),castTick,focus,aim,mark,origin,retry,retried,createdCount,diveHit,hitSoundPlayed).write(tag);}
    public void load(CompoundTag tag){
        if(kind()==0)return;MorSnapshot s=MorSnapshot.read(tag);
        mob.morSync(s.state(),s.remaining(),s.state()==2&&s.focus()!=null?s.skill():0);mob.morSetClone(s.isClone());
        clonesSummoned=s.summoned();cloneAge=s.age();parent=s.parent();children.clear();children.addAll(s.children());childPositions.clear();childPositions.putAll(s.positions());
        int[] saved=s.cooldowns();for(int i=1;i<10;i++)cooldown[i]=Math.max(MorRules.cooldown(i),i<saved.length?Math.clamp(saved[i],0,400):0);
        castTick=s.castTick();mob.morSetSkillElapsed(castTick);focus=s.focus();aim=s.aim();mark=s.mark();origin=s.origin();retry=s.retry();retried=s.retried();createdCount=s.createdCount();diveHit=s.diveHit();hitSoundPlayed=s.hitSoundPlayed();
        if(kind()>=5){higher.load(tag);}
        mob.refreshDimensions();mob.morConcealed(s.isClone()||!children.isEmpty());freeze(s.state()==1);if(s.state()==2&&!mob.morClone())attributes(false);
    }
}
