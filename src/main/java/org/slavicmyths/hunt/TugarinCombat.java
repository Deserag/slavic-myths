package org.slavicmyths.hunt;

import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModSounds;
import static org.slavicmyths.hunt.BossRules.Move;
import static org.slavicmyths.hunt.WorldBoss.TugarinState;

/** Per-boss bounded scheduler. No world scans, terrain changes, flight capability or entity replacement. */
final class TugarinCombat {
    private final WorldBoss boss;
    private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
    private record Threat(float damage,int time) {}
    private final Map<UUID,Threat> threats=new LinkedHashMap<>();
    private final Set<UUID> hits=new HashSet<>();
    private Vec3 heading=Vec3.ZERO,origin=Vec3.ZERO;
    private int time,rest,targetLockedUntil,nextDecision;
    private Move lastSpecial=Move.IDLE;
    TugarinCombat(WorldBoss boss){this.boss=boss;}
    private ServerLevel level(){return (ServerLevel)boss.level();}
    void threat(LivingEntity attacker,float damage){
        if(!valid(attacker))return;
        if(attacker==boss.getTarget()&&targetLockedUntil==0)targetLockedUntil=boss.tickCount+30;
        Threat old=threats.get(attacker.getUUID());
        threats.put(attacker.getUUID(),new Threat(Math.min(100,(old==null?0:old.damage)+damage),boss.tickCount));
        if(threats.size()>32)threats.remove(threats.keySet().iterator().next());
        if(!valid(boss.getTarget()))selectTarget(true);
    }
    private boolean valid(LivingEntity e){return e!=null&&e!=boss&&e.isAlive()&&!e.isRemoved()&&e.level()==boss.level()&&!boss.isAlliedTo(e)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator()))&&boss.distanceToSqr(e)<=48*48;}
    private double score(LivingEntity e){Threat t=threats.get(e.getUUID());double recent=t==null?0:t.damage*(1-(boss.tickCount-t.time)/200.0);return (e instanceof Player?300:8)+recent*2+Math.max(0,12-boss.distanceTo(e));}
    private void selectTarget(boolean urgent){
        int now=boss.tickCount;
        if(!urgent&&now%10!=0)return;
        threats.values().removeIf(t->now-t.time>=200);
        LivingEntity current=boss.getTarget(),best=valid(current)?current:null;
        for(UUID id:threats.keySet()){var e=level().getEntity(id);if(e instanceof LivingEntity candidate&&valid(candidate)&&(best==null||score(candidate)>score(best)))best=candidate;}
        // A local, bounded player query every ten ticks; no scans for arbitrary monsters.
        for(LivingEntity p:level().getEntitiesOfClass(LivingEntity.class,boss.getBoundingBox().inflate(32),e->valid(e)&&org.slavicmyths.bandit.BossTargets.automatic(e)))if(boss.getSensing().hasLineOfSight(p)&&(best==null||score(p)>score(best)))best=p;
        if(best!=current&&(!valid(current)||now>=targetLockedUntil||best!=null&&score(best)>score(current)*2+8)){
            boss.setTarget(best);targetLockedUntil=now+30;if(current==null&&best!=null)boss.playSound(ModSounds.boss("tugarin_phase1_aggro"),1,.7F);
        }
    }
    void tick(){
        cooldowns.replaceAll((m,v)->Math.max(0,v-1));
        selectTarget(boss.getTarget()!=null&&!valid(boss.getTarget()));
        startTransformationIfNeeded();
        if(boss.tugarinState()==TugarinState.TRANSFORMING){transform();return;}
        boolean second=boss.tugarinState()==TugarinState.PHASE_TWO;
        boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(BossKind.TUGARIN.speed*(second?1.23:1));
        boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12);
        boss.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(second?.65:.78);
        LivingEntity target=boss.getTarget();
        if(!valid(target)){if(rest>0){if(--rest==0)boss.tugarinMove(Move.IDLE);}else if(boss.move()!=Move.IDLE)recover(10);return;}
        if(boss.home!=null&&boss.distanceToSqr(Vec3.atCenterOf(boss.home))>96*96){boss.setTarget(null);recover(20);return;}
        if(rest>0){boss.getNavigation().stop();if(--rest==0)boss.tugarinMove(Move.IDLE);return;}
        if(boss.move()==Move.IDLE){
            boss.getLookControl().setLookAt(target,25,25);
            if(boss.tickCount>=nextDecision&&boss.getSensing().hasLineOfSight(target)){
                Move next=choose(boss.distanceTo(target),second);
                nextDecision=boss.tickCount+6;
                if(next!=Move.IDLE){begin(next,target);return;}
            }
            if(boss.tickCount%10==0)boss.getNavigation().moveTo(target,1);
            return;
        }
        boss.getNavigation().stop();time++;
        Move move=boss.move();int windup=windup(move);
        if(time<=windup)DashFacing.turn(boss,heading,25);else DashFacing.lock(boss,heading);
        if(move==Move.SPIN){
            // Three revolutions, one damage pulse per revolution; native hurt immunity remains intact.
            if(time==16||time==28||time==40){
                hits.clear();area(16,5.2,-1,.8F,0,true);
                boss.playSound(ModSounds.boss(time==16?"tugarin_spin_blade_1":time==28?"tugarin_spin_blade_2":"tugarin_spin_blade_3"),.8F,1);
                for(int i=0;i<24;i++){
                    double a=i*Math.PI/12;Vec3 point=boss.position().add(5.2*Math.cos(a),1.8,5.2*Math.sin(a));
                    if(level().hasChunkAt(net.minecraft.core.BlockPos.containing(point)))level().sendParticles(ParticleTypes.CLOUD,point.x,point.y,point.z,1,.12,.1,.12,.01);
                }
            }
            if(time>=52){boss.playSound(SoundEvents.RAVAGER_STEP,1,.65F);recover(24);}return;
        }
        if(move==Move.BLADE_COMBO){
            if(time==10||time==22||time==34){hits.clear();hit(target,time==34?8:5,3.5,.4,.3F,0);boss.playSound(ModSounds.boss("tugarin_phase1_attack"),.8F,1);}
            if(time>=40)recover(18);return;
        }
        if(move==Move.CHARGE){
            if(time>windup){
                Vec3 delta=heading.scale(.8).add(0,boss.getDeltaMovement().y,0);
                boss.setDeltaMovement(delta);boss.hasImpulse=true;
                area(14,2.1,.2,.8F,14,true);
                if(boss.horizontalCollision||boss.position().distanceToSqr(origin)>=81||time>=windup+12)recover(24);
            }
            return;
        }
        if(move==Move.WING_DIVE){
            // Eight-tick takeoff, twelve-tick low glide, fixed-heading dive, bounded landing recovery.
            if(time==windup){boss.setDeltaMovement(heading.scale(.28).add(0,.55,0));boss.hasImpulse=true;boss.playSound(ModSounds.boss("tugarin_phase2_wing"),.7F,1.25F);}
            if(time>windup&&time<=20){boss.setDeltaMovement(heading.scale(.45).add(0,Math.min(.08,boss.getDeltaMovement().y),0));boss.hasImpulse=true;}
            if(time>20){boss.setDeltaMovement(heading.scale(.75).add(0,-.45,0));boss.hasImpulse=true;area(16,2.4,.1,.8F,0,true);}
            if((time>20&&boss.onGround())||boss.horizontalCollision||time>=36||boss.position().distanceToSqr(origin)>144){particles(ParticleTypes.CLOUD,16,1,.1);recover(25);}
            boss.fallDistance=0;return;
        }
        if(move==Move.BREATH){
            if(time<windup&&time%5==0){Vec3 mouth=boss.getEyePosition().add(heading);level().sendParticles(ParticleTypes.SMOKE,mouth.x,mouth.y-.2,mouth.z,3,.15,.1,.15,.01);}
            if(time==windup)boss.playSound(ModSounds.boss("tugarin_phase2_fire_release"),.8F,.8F);
            if(time>=windup&&time<=windup+24){
                if(time%4==0){Vec3 mouth=boss.getEyePosition().add(heading);level().sendParticles(ParticleTypes.FLAME,mouth.x,mouth.y-.2,mouth.z,8,.15,.1,.15,.025);
                    for(int distance=2;distance<=8;distance+=2)for(int lane=-2;lane<=2;lane++){
                        double angle=lane*Math.PI/12;Vec3 ray=new Vec3(heading.x*Math.cos(angle)-heading.z*Math.sin(angle),0,heading.x*Math.sin(angle)+heading.z*Math.cos(angle));Vec3 point=mouth.add(ray.scale(distance-1)).add(0,-.2,0);
                        if(level().hasChunkAt(net.minecraft.core.BlockPos.containing(point)))level().sendParticles(ParticleTypes.FLAME,point.x,point.y,point.z,1,.06,.12,.06,.015);
                    }
                }
                if(time==windup||time==windup+12||time==windup+24)area(6,8,Math.cos(Math.toRadians(30)),0,0,false);
            }
            if(time>=windup+24)recover(18);return;
        }
        if(move==Move.SERPENT_LUNGE&&time>=windup&&time<windup+5){boss.setDeltaMovement(heading.scale(.48).add(0,boss.getDeltaMovement().y,0));boss.hasImpulse=true;area(14,5,.55,.3F,45,true);}
        if(time==windup){
            switch(move){
                case PUNCH -> hit(target,(float)boss.getAttributeValue(Attributes.ATTACK_DAMAGE),3.5,.4,.65F,0);
                case SWEEP -> {area(16,4,.42,.65F,0,true);boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP,.9F,.6F);}
                case SMOKE_BURST -> {for(int i=0;i<24;i++){double angle=i*Math.PI/12;Vec3 point=boss.position().add(5*Math.cos(angle),.8,5*Math.sin(angle));if(level().hasChunkAt(net.minecraft.core.BlockPos.containing(point)))level().sendParticles(ParticleTypes.CLOUD,point.x,point.y,point.z,2,.12,.25,.12,.05);}particles(ParticleTypes.CLOUD,18,1.4,.3);particles(ParticleTypes.FLAME,12,1,.2);area(8,5,-1,.55F,35,true);boss.playSound(SoundEvents.FIRECHARGE_USE,.9F,.7F);}
                case STONE -> {BossStone stone=new BossStone(ModEntities.TUGARIN_STONE.get(),boss.level());stone.setOwner(boss);stone.setPos(boss.getX(),boss.getEyeY()-.2,boss.getZ());Vec3 d=target.getEyePosition().subtract(stone.position());stone.shoot(d.x,d.y+d.length()*.10,d.z,1.1F,.5F);level().addFreshEntity(stone);boss.playSound(SoundEvents.SNOWBALL_THROW,.7F,.5F);}
                default -> {}
            }
        }
        if(time>=windup+10)recover(move==Move.PUNCH?(second?4:10):16);
    }
    private Move choose(double range,boolean second){
        if(second){
            if(range>=5&&range<=12&&boss.onGround()&&ready(Move.WING_DIVE)&&lastSpecial!=Move.WING_DIVE)return Move.WING_DIVE;
            if(range>=3&&range<=8&&ready(Move.BREATH)&&lastSpecial!=Move.BREATH)return Move.BREATH;
            if(range<=4.5&&ready(Move.SMOKE_BURST)&&lastSpecial!=Move.SMOKE_BURST)return Move.SMOKE_BURST;
            if(range<=5&&ready(Move.SERPENT_LUNGE)&&lastSpecial!=Move.SERPENT_LUNGE)return Move.SERPENT_LUNGE;
        }else{
            if(range<=5.2&&ready(Move.SPIN)&&lastSpecial!=Move.SPIN)return Move.SPIN;
            if(range<=4&&ready(Move.SWEEP)&&lastSpecial!=Move.SWEEP)return Move.SWEEP;
            if(range>=5&&range<=10&&boss.onGround()&&ready(Move.CHARGE)&&lastSpecial!=Move.CHARGE)return Move.CHARGE;
            if(range<=3.5&&ready(Move.BLADE_COMBO)&&lastSpecial!=Move.BLADE_COMBO)return Move.BLADE_COMBO;
        }
        if(range<=3.5&&ready(Move.PUNCH))return Move.PUNCH;
        return Move.IDLE;
    }
    private boolean ready(Move move){return cooldowns.getOrDefault(move,0)==0;}
    private static int windup(Move move){return switch(move){case SPIN -> 16;case PUNCH -> 10;case CHARGE -> 10;case BREATH -> 18;case STONE -> 18;case SERPENT_LUNGE,WING_DIVE -> 8;default -> 14;};}
    private static int cooldown(Move move){return switch(move){case SPIN -> 180;case PUNCH -> 26;case BLADE_COMBO -> 260;case SWEEP -> 200;case CHARGE -> 240;case STONE,WING_DIVE -> 280;case BREATH -> 240;case SERPENT_LUNGE -> 150;case SMOKE_BURST -> 340;default -> 0;};}
    private void begin(Move move,LivingEntity target){
        boss.tugarinMove(move);time=0;hits.clear();origin=boss.position();heading=target.position().subtract(origin).multiply(1,0,1).normalize();
        cooldowns.put(move,move==Move.PUNCH&&boss.tugarinState()==TugarinState.PHASE_TWO?20:cooldown(move));
        if(move!=Move.PUNCH)lastSpecial=move;
        boss.getNavigation().stop();
        if(move==Move.SPIN)boss.playSound(SoundEvents.RAVAGER_ROAR,.9F,.6F);
        boss.playSound(switch(move){case BREATH,SMOKE_BURST -> ModSounds.boss("tugarin_phase2_fire_charge");case WING_DIVE -> ModSounds.boss("tugarin_phase2_dive");case SERPENT_LUNGE -> ModSounds.boss("tugarin_phase2_attack");case SWEEP -> ModSounds.boss("tugarin_phase1_sweep");case CHARGE -> ModSounds.boss("tugarin_phase1_charge");default -> ModSounds.boss(boss.tugarinState()==TugarinState.PHASE_TWO?"tugarin_phase2_attack":"tugarin_phase1_attack");},.7F,move==Move.BREATH?.7F:.8F);
    }
    private boolean hit(LivingEntity target,float damage,double radius,double cone,float kb,int slow){
        if(!valid(target)||boss.distanceTo(target)>radius||!boss.getSensing().hasLineOfSight(target)||heading.dot(target.position().subtract(boss.position()).multiply(1,0,1).normalize())<cone)return false;
        // Native weakness/strength and difficulty/armor pipelines remain effective.
        if(boss.move()!=Move.PUNCH&&boss.hasEffect(MobEffects.WEAKNESS))damage=Math.max(0,damage-4*(boss.getEffect(MobEffects.WEAKNESS).getAmplifier()+1));
        if(damage<=0||!target.hurt(boss.damageSources().mobAttack(boss),damage))return false;
        if(kb>0){Vec3 away=target.position().subtract(boss.position()).multiply(1,0,1).normalize();target.knockback(target.isBlocking()?kb*.35:kb,-away.x,-away.z);}
        if(slow>0)target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,slow));
        if(boss.move()==Move.BREATH||boss.move()==Move.SMOKE_BURST)target.igniteForSeconds(2);
        return true;
    }
    private void area(float damage,double radius,double cone,float kb,int slow,boolean once){
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,boss.getBoundingBox().inflate(radius),e->valid(e)&&(e==boss.getTarget()||org.slavicmyths.bandit.BossTargets.automatic(e)||threats.containsKey(e.getUUID())))){
            if(once&&hits.contains(e.getUUID()))continue;
            if(hit(e,damage,radius,cone,kb,slow)&&once)hits.add(e.getUUID());
        }
    }
    private void recover(int ticks){boss.tugarinMove(Move.RECOVERY);rest=ticks;time=0;boss.getNavigation().stop();boss.setDeltaMovement(0,boss.getDeltaMovement().y,0);}
    private void particles(net.minecraft.core.particles.SimpleParticleType type,int count,double spread,double speed){level().sendParticles(type,boss.getX(),boss.getY()+1,boss.getZ(),count,spread,.6,spread,speed);}
    void startTransformationIfNeeded(){
        if(boss.tugarinState()==TugarinState.PHASE_ONE&&boss.getHealth()<=boss.getMaxHealth()*.5F){
            boss.tugarinState(TugarinState.TRANSFORMING);boss.tugarinMove(Move.TRANSITION);time=0;
            boss.getNavigation().stop();boss.setDeltaMovement(0,boss.getDeltaMovement().y,0);
            boss.playSound(ModSounds.boss("tugarin_transform_start"),1,.6F);
        }
    }
    private void transform(){
        boss.getNavigation().stop();boss.setDeltaMovement(0,boss.getDeltaMovement().y,0);time++;
        if(time%10==0)particles(ParticleTypes.SMOKE,10,.6,.015);
        if(time==20){boss.playSound(ModSounds.boss("tugarin_phase2_idle"),.8F,.8F);boss.playSound(ModSounds.boss("tugarin_transform_body"),.6F,.7F);}
        if(time==40){particles(ParticleTypes.FLAME,16,.7,.035);boss.playSound(ModSounds.boss("tugarin_transform_wings"),.9F,.9F);for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,boss.getBoundingBox().inflate(4),this::valid))if(boss.getSensing().hasLineOfSight(e)){Vec3 d=e.position().subtract(boss.position()).normalize();e.knockback(.5,-d.x,-d.z);}}
        if(time>=60){boss.tugarinState(TugarinState.PHASE_TWO);boss.playSound(ModSounds.boss("tugarin_transform_roar"),1,1.1F);recover(12);}
    }
    void save(CompoundTag n){n.putString("TugarinState",boss.tugarinState().name());n.putInt("TugarinTransformTicks",time);CompoundTag cd=new CompoundTag();cooldowns.forEach((m,v)->cd.putInt(m.name(),v));n.put("TugarinCooldowns",cd);}
    void load(CompoundTag n){
        // Interrupted transformation resolves to the true form instead of replaying it on chunk load.
        boolean second=boss.getHealth()<=boss.getMaxHealth()*.5F||n.getString("TugarinState").equals("PHASE_TWO")||n.getString("TugarinState").equals("TRANSFORMING");
        boss.tugarinState(second?TugarinState.PHASE_TWO:TugarinState.PHASE_ONE);
        CompoundTag cd=n.getCompound("TugarinCooldowns");for(Move m:Move.values())cooldowns.put(m,Math.max(0,Math.min(cooldown(m),cd.getInt(m.name()))));
        boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(BossKind.TUGARIN.speed*(second?1.23:1));boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12);
        recover(20);
    }
}
