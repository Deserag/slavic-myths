package org.slavicmyths.kitchen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.rpg.RpgMenu;
import org.slavicmyths.registry.ModBlocks;
public final class KitchenMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>,MenuType<KitchenMenu>> TYPE=RpgMenu.MENUS.register("kitchen",()->IMenuTypeExtension.create((id,inv,b)->new KitchenMenu(id,inv,b.readBlockPos())));
 public static void register(){}
 public final BlockPos pos;public final Container inventory;private final KitchenTile tile;public final ContainerData data;
 public KitchenMenu(int id,Inventory inv,BlockPos p){this(id,inv,p,new SimpleContainer(9),null);}
 public KitchenMenu(int id,Inventory inv,BlockPos p,Container c,KitchenTile tile){super(TYPE.get(),id);pos=p;inventory=c;this.tile=tile;
  data=tile==null?new SimpleContainerData(4):new ContainerData(){public int get(int i){return switch(i){case 0->tile.progress;case 1->tile.total;case 2->tile.servings;default->tile.maxServings;};}public void set(int i,int v){}public int getCount(){return 4;}};addDataSlots(data);
  addSlot(new Slot(c,0,20,24){public boolean mayPlace(ItemStack s){return s.is(KitchenII.item("rolling_pin"));}public boolean mayPickup(Player p){return tile==null||!tile.toolLocked(0);}});
  addSlot(new Slot(c,1,218,24){public boolean mayPlace(ItemStack s){return s.is(KitchenII.item("metal_pot"));}public boolean mayPickup(Player p){return tile==null||!tile.toolLocked(1);}});
  for(int i=0;i<4;i++)addSlot(new Slot(c,i+2,83+i%2*20,43+i/2*20));
  for(int i=0;i<3;i++)addSlot(new Slot(c,i+6,181+(i==2?20:0),i==0?44:76){public boolean mayPlace(ItemStack s){return false;}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,47+col*18,130+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,47+col*18,188));
 }

 @Override public boolean stillValid(Player p){BlockState s=p.level().getBlockState(pos);return p.isAlive()&&p.distanceToSqr(pos.getCenter())<=64&&s.is(ModBlocks.KITCHEN_TABLE.get());}
 @Override public ItemStack quickMoveStack(Player p,int i){Slot slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();boolean ok;if(i<9)ok=moveItemStackTo(s,9,slots.size(),true);else if(s.is(KitchenII.item("rolling_pin")))ok=moveItemStackTo(s,0,1,false);else if(s.is(KitchenII.item("metal_pot")))ok=moveItemStackTo(s,1,2,false);else ok=moveItemStackTo(s,2,6,false);if(!ok)return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
 /** One serving button: bowl on cursor or in inventory; server owns the transaction. */
 @Override public boolean clickMenuButton(Player p,int id){if(id!=0||tile==null||p.level().isClientSide||!stillValid(p))return false;if(getCarried().is(Items.BOWL))return tile.serve(p,getCarried());for(int i=0;i<p.getInventory().getContainerSize();i++){ItemStack s=p.getInventory().getItem(i);if(s.is(Items.BOWL)&&tile.serve(p,s)){p.getInventory().setChanged();return true;}}return false;}
}
