package org.slavicmyths.flight;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.slavicmyths.registry.ModEntities;
public final class FlightItem extends Item {
 private final boolean mortar;
 public FlightItem(Properties p,boolean mortar){super(p.stacksTo(1));this.mortar=mortar;}
 @Override public ActionResultType useOn(ItemUseContext c){if(c.getPlayer()==null)return ActionResultType.PASS;
  if(!org.slavicmyths.yaga.FlightRecovery.ready(c.getPlayer()))return ActionResultType.FAIL;
  BlockPos p=c.getClickedPos().relative(c.getClickedFace());if(!c.getPlayer().mayUseItemAt(p,c.getClickedFace(),c.getItemInHand()))return ActionResultType.FAIL;
  FlyingVessel v=(mortar?ModEntities.FLYING_MORTAR.get():ModEntities.FLYING_BROOM.get()).create(c.getLevel());if(v==null)return ActionResultType.FAIL;v.setPos(p.getX()+.5,p.getY()+.1,p.getZ()+.5);v.yRot=c.getPlayer().yRot;
  if(!c.getLevel().noCollision(v,v.getBoundingBox()))return ActionResultType.FAIL;
  if(!c.getLevel().isClientSide){v.fromItem(c.getItemInHand(),c.getPlayer().getUUID());if(c.getLevel().addFreshEntity(v)){c.getItemInHand().shrink(1);return ActionResultType.SUCCESS;}return ActionResultType.FAIL;}
  return ActionResultType.SUCCESS;
 }
 @Override public int getEnchantmentValue(){return 12;}
}
