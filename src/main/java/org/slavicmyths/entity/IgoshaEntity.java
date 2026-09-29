package org.slavicmyths.entity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.slavicmyths.registry.ModItems;
public final class IgoshaEntity extends LandSpiritEntity {
    private long depart;
    public IgoshaEntity(EntityType<? extends IgoshaEntity> t,World w){super(t,w);}
    public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,18).add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.ATTACK_DAMAGE,1).add(Attributes.FOLLOW_RANGE,18);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new net.minecraft.entity.ai.goal.SwimGoal(this));}
    @Override public int getAmbientSoundInterval(){return 800;}
    @Override protected ActionResultType mobInteract(PlayerEntity p,Hand h){
        ItemStack s=p.getItemInHand(h);if(s.getItem()!=Items.BREAD && s.getItem()!=ModItems.HONEY_BREAD.get() && s.getItem()!=ModItems.LINEN_CLOTH.get())return super.mobInteract(p,h);
        if(!level.isClientSide && depart==0){if(!p.abilities.instabuild)s.shrink(1);depart=level.getGameTime()+80;setPersistenceRequired();setTarget(null);state(2);particles(ParticleTypes.HAPPY_VILLAGER,5);}
        return ActionResultType.sidedSuccess(level.isClientSide);
    }
    @Override protected void think(){
        if(depart>0){if(level.getGameTime()>=depart){if(random.nextInt(3)==0)spawnAtLocation(ModItems.OLD_BUTTON.get());particles(ParticleTypes.POOF,10);remove();}return;}
        PlayerEntity p=nearby(18);if(p==null)return;
        Vector3d toward=position().subtract(p.position()).normalize();boolean watched=p.getLookAngle().dot(toward)>.6 && canSee(p);
        state(watched?0:1);getNavigation().stop();
        if(!watched && distanceToSqr(p)>16 && isOnGround()){Vector3d v=p.position().subtract(position()).normalize();setDeltaMovement(v.x*.28,.24,v.z*.28);}
        if(getTarget()!=null && level.getGameTime()>nextPower){
            p.addEffect(new EffectInstance(random.nextBoolean()?Effects.WEAKNESS:Effects.BLINDNESS,45));voice("power",.35F);
            Vector3d behind=p.position().subtract(p.getLookAngle().scale(3));BlockPos pos=new BlockPos(behind);
            if(level.hasChunkAt(pos) && Math.abs(behind.y-getY())<2 && level.getFluidState(pos).isEmpty() && level.getBlockState(pos.below()).getMaterial().isSolid()
                && level.clip(new RayTraceContext(position().add(0,.4,0),behind.add(0,.4,0),RayTraceContext.BlockMode.COLLIDER,RayTraceContext.FluidMode.NONE,this)).getType()==RayTraceResult.Type.MISS)randomTeleport(behind.x,behind.y,behind.z,true);
            for(net.minecraft.util.math.BlockPos at:net.minecraft.util.math.BlockPos.betweenClosed(blockPosition().offset(-1,-1,-1),blockPosition().offset(1,1,1))) {
                if(level.getBlockState(at).getBlock() instanceof net.minecraft.block.DoorBlock){level.playSound(null,at,SoundEvents.WOODEN_DOOR_CLOSE,SoundCategory.HOSTILE,.5F,.8F);break;}
            }
            nextPower=level.getGameTime()+400;
        }
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundNBT n){super.addAdditionalSaveData(n);n.putLong("Depart",depart);}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundNBT n){super.readAdditionalSaveData(n);depart=n.getLong("Depart");}
}
