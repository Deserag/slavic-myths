package org.slavicmyths.flight;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.rpg.RpgMenu;
public final class CargoMenu extends Container {
 public static final RegistryObject<ContainerType<CargoMenu>> TYPE=RpgMenu.MENUS.register("mortar_cargo",()->IForgeContainerType.create((id,inv,b)->new CargoMenu(id,inv,b.readVarInt())));
 public static void register(){}
 private final FlyingVessel vessel;private final IInventory cargo;
 private CargoMenu(int id,PlayerInventory inv,int entityId){this(id,inv,inv.player.level.getEntity(entityId));}
 private CargoMenu(int id,PlayerInventory inv,Entity entity){this(id,inv,entity instanceof FlyingVessel?(FlyingVessel)entity:null);}
 public CargoMenu(int id,PlayerInventory inv,FlyingVessel v){super(TYPE.get(),id);vessel=v;cargo=v==null?new Inventory(9):v.cargo;if(v!=null&&!inv.player.level.isClientSide)v.viewers++;
  for(int i=0;i<9;i++)addSlot(new Slot(cargo,i,8+i*18,20){@Override public boolean mayPlace(ItemStack s){return !(s.getItem() instanceof FlightItem);}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,52+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,110));
 }
 @Override public boolean stillValid(PlayerEntity p){return vessel!=null&&vessel.isAlive()&&vessel.mortar&&vessel.owns(p)&&p.isAlive()&&p.distanceToSqr(vessel)<=16;}
 @Override public ItemStack quickMoveStack(PlayerEntity p,int i){Slot slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();if(i<9){if(!moveItemStackTo(s,9,slots.size(),true))return ItemStack.EMPTY;}else{if(s.getItem() instanceof FlightItem||!moveItemStackTo(s,0,9,false))return ItemStack.EMPTY;}if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
 @Override public void removed(PlayerEntity p){super.removed(p);if(vessel!=null&&!p.level.isClientSide)vessel.viewers=Math.max(0,vessel.viewers-1);}
}
