package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModItems;
public final class IgoshaEntity extends LandSpiritEntity {
    private long depart;
    public enum Behavior { WATCH, FEINT_APPROACH, VANISH, NUISANCE_HIT, FLEE, COOLDOWN }
    private final AttackTimeline attack=new AttackTimeline();private Behavior behavior=Behavior.WATCH;private int vanished;
    public Behavior behavior(){return behavior;}
    public IgoshaEntity(EntityType<? extends IgoshaEntity> t,Level w){super(t,w);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.MOVEMENT_SPEED,.32).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,24);}
    @Override protected boolean meleeReady(){return false;}
    @Override protected AttackTimeline attackTimeline(){return attack;}
    @Override protected void abilityTick(){
        if(vanished>0){if(--vanished==0)setInvisible(false);return;}
        if(getTarget()==null||depart>0){attack.cancel();return;}
        if(attack.ready()){
            if(attackable(getTarget(),2.2)){attack.start(12,1,24,40);behavior=Behavior.NUISANCE_HIT;voice("power",.35F);}
            else{behavior=Behavior.FEINT_APPROACH;chase(1.3);return;}
        }
        if(attack.phase()==AttackTimeline.Phase.TELEGRAPH){faceTarget();getNavigation().stop();}
        if(attack.tick()){strikeTarget(4,2.2,0);particles(ParticleTypes.POOF,8);setInvisible(true);vanished=12;behavior=Behavior.VANISH;
            var away=position().subtract(getTarget().position()).multiply(1,0,1).normalize();
            for(int i=0;i<6;i++){var p=blockPosition().offset((int)(away.x*(4+i)),0,(int)(away.z*(4+i)));if(level().hasChunkAt(p)&&level().getFluidState(p).isEmpty()&&level().getBlockState(p.below()).isSolid()&&randomTeleport(p.getX()+.5,p.getY(),p.getZ()+.5,true))break;}
        }
        if(attack.phase()==AttackTimeline.Phase.RECOVERY){behavior=Behavior.FLEE;var away=position().subtract(getTarget().position()).multiply(1,0,1).normalize();if(tickCount%8==0)getNavigation().moveTo(getX()+away.x*6,getY(),getZ()+away.z*6,1.3);}
        if(attack.phase()==AttackTimeline.Phase.COOLDOWN)behavior=Behavior.COOLDOWN;
    }
    @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(this));}
    @Override public int getAmbientSoundInterval(){return 800;}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand h){
        ItemStack s=p.getItemInHand(h);if(s.getItem()!=Items.BREAD && s.getItem()!=ModItems.HONEY_BREAD.get() && s.getItem()!=ModItems.LINEN_CLOTH.get())return super.mobInteract(p,h);
        if(!level().isClientSide && depart==0){if(!p.getAbilities().instabuild)s.shrink(1);depart=level().getGameTime()+80;setPersistenceRequired();setTarget(null);state(2);particles(ParticleTypes.HAPPY_VILLAGER,5);}
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override protected void think(){
        if(depart>0){if(level().getGameTime()>=depart){if(random.nextInt(3)==0)spawnAtLocation(ModItems.OLD_BUTTON.get());particles(ParticleTypes.POOF,10);discard();}return;}
        if(getTarget()!=null)return;
        Player p=nearby(18);if(p==null)return;
        Vec3 toward=position().subtract(p.position()).normalize();boolean watched=p.getLookAngle().dot(toward)>.6 && hasLineOfSight(p);
        state(watched?0:1);getNavigation().stop();
        if(!watched && distanceToSqr(p)>16 && onGround()){Vec3 v=p.position().subtract(position()).normalize();setDeltaMovement(v.x*.28,.24,v.z*.28);}
        if(getTarget()!=null && level().getGameTime()>nextPower){
            p.addEffect(new MobEffectInstance(random.nextBoolean()?MobEffects.WEAKNESS:MobEffects.BLINDNESS,45));voice("power",.35F);
            Vec3 behind=p.position().subtract(p.getLookAngle().scale(3));BlockPos pos=BlockPos.containing(behind);
            if(level().hasChunkAt(pos) && Math.abs(behind.y-getY())<2 && level().getFluidState(pos).isEmpty() && level().getBlockState(pos.below()).isSolid()
                && level().clip(new ClipContext(position().add(0,.4,0),behind.add(0,.4,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS)randomTeleport(behind.x,behind.y,behind.z,true);
            for(net.minecraft.core.BlockPos at:net.minecraft.core.BlockPos.betweenClosed(blockPosition().offset(-1,-1,-1),blockPosition().offset(1,1,1))) {
                if(level().getBlockState(at).getBlock() instanceof net.minecraft.world.level.block.DoorBlock){level().playSound(null,at,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.HOSTILE,.5F,.8F);break;}
            }
            nextPower=level().getGameTime()+400;
        }
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag n){super.addAdditionalSaveData(n);n.putLong("Depart",depart);}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag n){super.readAdditionalSaveData(n);depart=n.getLong("Depart");attack.cancel();vanished=0;setInvisible(false);}
}
