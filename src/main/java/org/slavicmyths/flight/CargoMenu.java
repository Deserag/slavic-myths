package org.slavicmyths.flight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.rpg.RpgMenu;
public final class CargoMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>,MenuType<CargoMenu>> TYPE=RpgMenu.MENUS.register("mortar_cargo",()->IMenuTypeExtension.create((id,inv,b)->new CargoMenu(id,inv,b.readVarInt())));
 public static void register(){}
 private final FlyingVessel vessel;private final Container cargo;
 private CargoMenu(int id,Inventory inv,int entityId){this(id,inv,inv.player.level().getEntity(entityId));}
 private CargoMenu(int id,Inventory inv,Entity entity){this(id,inv,entity instanceof FlyingVessel?(FlyingVessel)entity:null);}
 public CargoMenu(int id,Inventory inv,FlyingVessel v){super(TYPE.get(),id);vessel=v;cargo=v==null?new SimpleContainer(9):v.cargo;if(v!=null&&!inv.player.level().isClientSide)v.viewers++;
  for(int i=0;i<9;i++)addSlot(new Slot(cargo,i,8+i*18,20){@Override public boolean mayPlace(ItemStack s){return !(s.getItem() instanceof FlightItem);}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,52+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,110));
 }
 @Override public boolean stillValid(Player p){return vessel!=null&&vessel.isAlive()&&vessel.mortar&&vessel.owns(p)&&p.isAlive()&&p.distanceToSqr(vessel)<=16;}
 @Override public ItemStack quickMoveStack(Player p,int i){Slot slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();if(i<9){if(!moveItemStackTo(s,9,slots.size(),true))return ItemStack.EMPTY;}else{if(s.getItem() instanceof FlightItem||!moveItemStackTo(s,0,9,false))return ItemStack.EMPTY;}if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
 @Override public void removed(Player p){super.removed(p);if(vessel!=null&&!p.level().isClientSide)vessel.viewers=Math.max(0,vessel.viewers-1);}
}
