package org.slavicmyths.water;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.slavicmyths.registry.ModItems;
public final class RiverFish extends AbstractFishEntity {
 public final int kind;
 public RiverFish(EntityType<? extends RiverFish> t,World w,int kind){super(t,w);this.kind=kind;}
 protected ItemStack getBucketItemStack(){return new ItemStack(Items.WATER_BUCKET);}
 // These larger river animals are caught by fishing/net, not converted into vanilla fish buckets.
 protected ActionResultType mobInteract(PlayerEntity p,Hand h){return ActionResultType.PASS;}
 protected SoundEvent getFlopSound(){return SoundEvents.COD_FLOP;}
 protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.COD_HURT;}
 protected SoundEvent getDeathSound(){return SoundEvents.COD_DEATH;}
 public void aiStep(){super.aiStep();if(level.isClientSide||!isInWater())return;
  if(kind==2){if(tickCount%20==0)getNavigation().moveTo(getX(),getY()-1,getZ(),.35);setDeltaMovement(getDeltaMovement().add(0,-.012,0));}
  if(tickCount%40==0){PlayerEntity p=level.getNearestPlayer(this,4);if(p!=null&&!p.isCreative()&&!p.isSpectator()){Vector3d away=position().subtract(p.position()).normalize().scale(kind==0?.2:kind==1?.1:.14);setDeltaMovement(getDeltaMovement().add(away));}}
  if(kind==0&&tickCount%120==0){java.util.List<AbstractFishEntity> prey=level.getEntitiesOfClass(AbstractFishEntity.class,getBoundingBox().inflate(5),f->f!=this&&f.getBbWidth()<.6F);if(!prey.isEmpty())getNavigation().moveTo(prey.get(0),1.25);}
 }
 protected ResourceLocation getDefaultLootTable(){return new ResourceLocation("slavicmyths","entities/"+(kind==0?"pike":kind==1?"carp":"crayfish"));}
}
