package org.slavicmyths.brewing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
public final class VatMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>,MenuType<VatMenu>> TYPE=org.slavicmyths.rpg.RpgMenu.MENUS.register("vat",()->IMenuTypeExtension.create((id,inv,b)->new VatMenu(id,inv,b.readBlockPos(),new SimpleContainer(9),null)));
 public final BlockPos pos;public final ContainerData data;private final BrewTile tile;
 public VatMenu(int id,Inventory inv,BlockPos pos,Container container,BrewTile tile){super(TYPE.get(),id);this.pos=pos;this.tile=tile;data=tile==null?new SimpleContainerData(3):new ContainerData(){public int get(int i){return i==0?tile.progress:i==1?tile.total:switch(tile.mode){case "MALTING"->1;case "INFUSION"->2;case "WORT"->3;case "MUST"->4;default->0;};}public void set(int i,int v){}public int getCount(){return 3;}};addDataSlots(data);
  for(int i=0;i<9;i++){int x=i<4?16+i*20:i<6?112+(i-4)*20:i<8?176+(i-6)*20:112,y=i==8?76:42;addSlot(new Slot(container,i,x,y){public boolean mayPlace(ItemStack s){return tile==null?(getContainerSlot()<4||getContainerSlot()==8):tile.canPlaceItem(getContainerSlot(),s);}public boolean mayPickup(Player p){return tile==null||tile.canTakeItem(container,getContainerSlot(),getItem());}});}
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,32+col*18,114+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,32+col*18,172));
 }
 public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getCenter())<=64&&p.level().getBlockState(pos).is(Brewing.BLOCKS.get("fermentation_vat").get());}
 public ItemStack quickMoveStack(Player p,int i){Slot slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();boolean ok;if(i<9)ok=moveItemStackTo(s,9,slots.size(),true);else if(java.util.Set.of("veles_staff","rune_forest","thunder_crystal_powder").contains(Brewing.key(s)))ok=moveItemStackTo(s,8,9,false);else ok=moveItemStackTo(s,0,4,false);if(!ok)return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
