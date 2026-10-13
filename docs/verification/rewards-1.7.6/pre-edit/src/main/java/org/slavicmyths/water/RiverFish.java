package org.slavicmyths.water;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModItems;
public final class RiverFish extends AbstractFish {
 public final int kind;
 public RiverFish(EntityType<? extends RiverFish> t,Level w,int kind){super(t,w);this.kind=kind;}
 public ItemStack getBucketItemStack(){return new ItemStack(Items.WATER_BUCKET);}
 // These larger river animals are caught by fishing/net, not converted into vanilla fish buckets.
 protected InteractionResult mobInteract(Player p,InteractionHand h){return InteractionResult.PASS;}
 protected SoundEvent getFlopSound(){return SoundEvents.COD_FLOP;}
 protected SoundEvent getHurtSound(DamageSource s){return SoundEvents.COD_HURT;}
 protected SoundEvent getDeathSound(){return SoundEvents.COD_DEATH;}
 public void aiStep(){super.aiStep();if(level().isClientSide||!isInWater())return;
  if(kind==2){if(tickCount%20==0)getNavigation().moveTo(getX(),getY()-1,getZ(),.35);setDeltaMovement(getDeltaMovement().add(0,-.012,0));}
  if(tickCount%40==0){Player p=level().getNearestPlayer(this,4);if(p!=null&&!p.isCreative()&&!p.isSpectator()){Vec3 away=position().subtract(p.position()).normalize().scale(kind==0?.2:kind==1?.1:.14);setDeltaMovement(getDeltaMovement().add(away));}}
  if(kind==0&&tickCount%120==0){java.util.List<AbstractFish> prey=level().getEntitiesOfClass(AbstractFish.class,getBoundingBox().inflate(5),f->f!=this&&f.getBbWidth()<.6F);if(!prey.isEmpty())getNavigation().moveTo(prey.get(0),1.25);}
 }
 protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath("slavicmyths","entities/"+(kind==0?"pike":kind==1?"carp":"crayfish")));}
}
