package org.slavicmyths.entity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
public final class KikimoraEntity extends LandSpiritEntity {
    private long nextNoticeSound;private int attention;private double fleeX,fleeZ;
    public KikimoraEntity(EntityType<? extends KikimoraEntity> t,World w){super(t,w);}
    public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,28).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,20);}
    @Override protected void think(){
        PlayerEntity p=nearby(18);
        if(getTarget()!=null){
            state(3);
            if(getHealth()<9){state(4);Vector3d v=position().subtract(getTarget().position()).normalize();fleeX=getX()+v.x*8;fleeZ=getZ()+v.z*8;getNavigation().moveTo(fleeX,getY(),fleeZ,1.36);setTarget(null);angryUntil=level.getGameTime()+100;return;}
            if(level.getGameTime()>nextPower && distanceToSqr(getTarget())<64 && canSee(getTarget())){getTarget().addEffect(new EffectInstance(Effects.BLINDNESS,45));nextPower=level.getGameTime()+400;voice("power",.5F);}
            return;
        }
        if(state()==4 && level.getGameTime()<angryUntil){getNavigation().moveTo(fleeX,getY(),fleeZ,1.36);return;}
        if(p==null){attention=0;state(0);wander();return;}
        double d=distanceToSqr(p);getLookControl().setLookAt(p,30,30);
        if(d<49)attention+=org.slavicmyths.rpg.PathData.has(p,"quiet_step")?15:20;else attention=Math.max(0,attention-20);
        if(d<4 || attention>200 || (!level.isDay() && d<64 && random.nextInt(40)==0)){provoke(p);return;}
        if(canSee(p) && (level.isDay() || random.nextInt(3)==0)){if(state()!=1 && level.getGameTime()>nextNoticeSound){voice("notice",.25F);nextNoticeSound=level.getGameTime()+500;}state(1);getNavigation().stop();}
        else {state(2);Vector3d side=p.getLookAngle();getNavigation().moveTo(p.getX()+side.z*6,p.getY(),p.getZ()-side.x*6,.85);}
    }
    @Override public int getAmbientSoundInterval(){return 650;}
    @Override public void playAmbientSound(){if(!level.isClientSide){if(tickCount%3==0)voice("laugh",.22F);else voice("ambient",.25F);}}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState block){if(!level.isClientSide && tickCount%2==0)voice("step",.1F);}
    @Override public boolean doHurtTarget(net.minecraft.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level.isClientSide)voice("attack",.4F);return hit;}
}
