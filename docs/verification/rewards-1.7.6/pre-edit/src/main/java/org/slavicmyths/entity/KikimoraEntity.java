package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
public final class KikimoraEntity extends LandSpiritEntity {
    private long nextNoticeSound;private int attention;private double fleeX,fleeZ;
    public enum Behavior { IDLE, STALK, HIDE, PEEK, AMBUSH, SCRATCH_COMBO, FLEE_REPOSITION, COOLDOWN }
    private final AttackTimeline attack=new AttackTimeline();private Behavior behavior=Behavior.IDLE;private boolean firstStrike,secondStrike;
    public Behavior behavior(){return behavior;}
    public KikimoraEntity(EntityType<? extends KikimoraEntity> t,Level w){super(t,w);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,36).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.ARMOR,2).add(Attributes.FOLLOW_RANGE,28);}
    @Override protected boolean meleeReady(){return false;}
    @Override protected AttackTimeline attackTimeline(){return attack;}
    @Override protected void abilityTick(){
        if(getTarget()==null){attack.cancel();return;}
        if(Math.abs(getTarget().getY()-getY())>=3){behavior=Behavior.FLEE_REPOSITION;reposition();attack.cancel();return;}
        if(attack.ready()){
            double d=distanceToSqr(getTarget());
            if(d>=16&&d<=49&&hasLineOfSight(getTarget())){behavior=Behavior.AMBUSH;attack.start(14,28,24,40);getNavigation().stop();voice("attack",.5F);firstStrike=secondStrike=false;}
            else if(d<16&&hasLineOfSight(getTarget())){behavior=Behavior.SCRATCH_COMBO;attack.start(12,28,24,40);getNavigation().stop();voice("attack",.5F);firstStrike=secondStrike=false;}
            else{behavior=Behavior.STALK;chase(.85);return;}
        }
        if(attack.phase()==AttackTimeline.Phase.TELEGRAPH){faceTarget();getNavigation().stop();state(3);}
        boolean active=attack.tick();
        if(active){behavior=Behavior.SCRATCH_COMBO;getNavigation().moveTo(getTarget(),1.6);}
        if(attack.phase()==AttackTimeline.Phase.ACTIVE){
            if(attack.elapsed()<=10&&!firstStrike&&strikeTarget(6,2.3,.15F)){firstStrike=true;getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30));}
            if(attack.elapsed()>=22&&!secondStrike){faceTarget();if(strikeTarget(4,2.3,.1F))secondStrike=true;}
        }
        else if(attack.phase()==AttackTimeline.Phase.RECOVERY){behavior=Behavior.FLEE_REPOSITION;state(4);if(attack.elapsed()%8==0)reposition();}
        else if(attack.phase()==AttackTimeline.Phase.COOLDOWN){behavior=Behavior.COOLDOWN;state(1);}
    }
    private void reposition(){if(getTarget()==null)return;var away=position().subtract(getTarget().position()).multiply(1,0,1).normalize();getNavigation().moveTo(getX()+away.x*6,getY(),getZ()+away.z*6,1.3);}
    @Override protected void think(){
        Player p=nearby(18);
        if(getTarget()!=null){
            return;
        }
        if(state()==4 && level().getGameTime()<angryUntil){getNavigation().moveTo(fleeX,getY(),fleeZ,1.36);return;}
        if(p==null){attention=0;state(0);behavior=Behavior.IDLE;wander();return;}
        double d=distanceToSqr(p);getLookControl().setLookAt(p,30,30);
        if(d<49)attention+=org.slavicmyths.rpg.PathData.has(p,"quiet_step")?15:20;else attention=Math.max(0,attention-20);
        if(d<4 || attention>200 || (!level().isDay() && d<64 && random.nextInt(40)==0)){provoke(p);return;}
        if(hasLineOfSight(p) && (level().isDay() || random.nextInt(3)==0)){if(state()!=1 && level().getGameTime()>nextNoticeSound){voice("notice",.25F);nextNoticeSound=level().getGameTime()+500;}state(1);behavior=Behavior.PEEK;getNavigation().stop();}
        else {state(2);behavior=hasLineOfSight(p)?Behavior.STALK:Behavior.HIDE;Vec3 side=p.getLookAngle();getNavigation().moveTo(p.getX()+side.z*6,p.getY(),p.getZ()-side.x*6,.85);}
    }
    @Override public int getAmbientSoundInterval(){return 650;}
    @Override public void playAmbientSound(){if(!level().isClientSide){if(tickCount%3==0)voice("laugh",.22F);else voice("ambient",.25F);}}
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(!level().isClientSide && tickCount%2==0)voice("step",.1F);}
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level().isClientSide)voice("attack",.4F);return hit;}
}
