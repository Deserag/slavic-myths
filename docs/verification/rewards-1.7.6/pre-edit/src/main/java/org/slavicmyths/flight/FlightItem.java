package org.slavicmyths.flight;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import org.slavicmyths.registry.ModEntities;
public final class FlightItem extends Item {
 private final boolean mortar;
 public FlightItem(Properties p,boolean mortar){super(p.stacksTo(1));this.mortar=mortar;}
 @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer()==null)return InteractionResult.PASS;
  if(!org.slavicmyths.yaga.FlightRecovery.ready(c.getPlayer()))return InteractionResult.FAIL;
  BlockPos p=c.getClickedPos().relative(c.getClickedFace());if(!c.getPlayer().mayUseItemAt(p,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;
  FlyingVessel v=(mortar?ModEntities.FLYING_MORTAR.get():ModEntities.FLYING_BROOM.get()).create(c.getLevel());if(v==null)return InteractionResult.FAIL;v.setPos(p.getX()+.5,p.getY()+.1,p.getZ()+.5);v.setYRot(c.getPlayer().getYRot());
  if(!c.getLevel().noCollision(v,v.getBoundingBox()))return InteractionResult.FAIL;
  if(!c.getLevel().isClientSide){v.fromItem(c.getItemInHand(),c.getPlayer().getUUID());if(c.getLevel().addFreshEntity(v)){c.getItemInHand().shrink(1);return InteractionResult.SUCCESS;}return InteractionResult.FAIL;}
  return InteractionResult.SUCCESS;
 }
 @Override public int getEnchantmentValue(){return 12;}
}
