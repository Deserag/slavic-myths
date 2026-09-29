package org.slavicmyths.entity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerBossInfo;
public final class OvinnikEntity extends LandSpiritEntity {
    private final ServerBossInfo bar=new ServerBossInfo(new net.minecraft.util.text.TranslationTextComponent("entity.slavicmyths.ovinnik"),BossInfo.Color.RED,BossInfo.Overlay.PROGRESS);
    private int warnings,chargeTicks,recovery;private long nextHeat,nextAsh;private Vector3d dash=Vector3d.ZERO;private boolean enraged;
    public OvinnikEntity(EntityType<? extends OvinnikEntity> t,World w){super(t,w);bar.setVisible(false);}
    public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,140).add(Attributes.MOVEMENT_SPEED,.29).add(Attributes.ATTACK_DAMAGE,9).add(Attributes.ARMOR,7).add(Attributes.KNOCKBACK_RESISTANCE,.8).add(Attributes.FOLLOW_RANGE,24);}
    @Override public void startSeenByPlayer(ServerPlayerEntity p){super.startSeenByPlayer(p);bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayerEntity p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override protected boolean meleeReady(){return windup==0 && chargeTicks==0 && recovery==0;}
    @Override protected void think(){
        if(home==null){home=blockPosition();setPersistenceRequired();}
        if(getTarget()==null){bar.setVisible(false);state(0);PlayerEntity p=nearby(9);if(p==null){warnings=0;return;}getLookControl().setLookAt(p,30,30);warnings+=20;if(warnings%100==0){voice("angry",.7F);p.displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("spirit.slavicmyths.ovinnik_warning"),true);}if(warnings>=400)provoke(p);return;}
        bar.setVisible(true);bar.setPercent(getHealth()/getMaxHealth());
        if(!enraged && getHealth()<70){enraged=true;getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.33);voice("power",1);particles(ParticleTypes.FLAME,15);}
        long now=level.getGameTime();
        if(enraged)particles(ParticleTypes.SMOKE,3);
        if(windup>0 || chargeTicks>0 || recovery>0)return;
        if(enraged && now>nextAsh){particles(ParticleTypes.SMOKE,55);voice("power",.9F);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(6),p->!p.isCreative() && !p.isSpectator() && canSee(p)))p.addEffect(new EffectInstance(Effects.BLINDNESS,35));nextAsh=now+400;recovery=20;return;}
        if(now>nextHeat){particles(ParticleTypes.FLAME,25);voice("power",.7F);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(4),p->!p.isCreative() && !p.isSpectator() && canSee(p)))p.hurt(DamageSource.mobAttack(this),3);nextHeat=now+340;return;}
        if(now>nextPower){getNavigation().stop();state(distanceToSqr(getTarget())<16?4:5);windup=18;dash=getTarget().position().subtract(position()).normalize();nextPower=now+(enraged?140:220);voice("power",.75F);}
    }
    @Override protected void abilityTick(){
        bar.setPercent(getHealth()/getMaxHealth());bar.setVisible(getTarget()!=null && isAlive());
        if(recovery>0){windup=1;getNavigation().stop();if(--recovery==0){windup=0;state(3);}return;}
        if(windup>0){getNavigation().stop();if(--windup==0){if(getTarget()==null){state(0);return;}if(state()==4){if(distanceToSqr(getTarget())<12 && canSee(getTarget())){getTarget().hurt(DamageSource.mobAttack(this),13);getTarget().knockback(1.1F,-dash.x,-dash.z);}recovery=15;state(6);}else{chargeTicks=12;state(5);}}}
        if(chargeTicks>0){getNavigation().stop();setDeltaMovement(dash.x*.65,getDeltaMovement().y,dash.z*.65);chargeTicks--;if(getTarget()!=null && distanceToSqr(getTarget())<5 && canSee(getTarget())){getTarget().hurt(DamageSource.mobAttack(this),10);getTarget().knockback(.8F,-dash.x,-dash.z);chargeTicks=0;}if(chargeTicks==0){recovery=25;state(6);}}
    }
    @Override public void die(DamageSource source){bar.removeAllPlayers();particles(ParticleTypes.SMOKE,40);super.die(source);}
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundNBT n){super.addAdditionalSaveData(n);n.putLong("NextHeat",nextHeat);n.putLong("NextAsh",nextAsh);n.putBoolean("Enraged",enraged);}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundNBT n){super.readAdditionalSaveData(n);nextHeat=n.getLong("NextHeat");nextAsh=n.getLong("NextAsh");enraged=n.getBoolean("Enraged");if(enraged)getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.33);}
}
