package org.slavicmyths.storage;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.network.chat.Component;
import org.slavicmyths.storage.Household.Kind;
import org.slavicmyths.kitchen.KitchenII;
import org.slavicmyths.kitchen.KitchenTile;
public class StorageTile extends BlockEntity implements Container,MenuProvider {
 public final org.slavicmyths.brewing.Fermentation fermentation=new org.slavicmyths.brewing.Fermentation();public final Kind kind;public final NonNullList<ItemStack> items;public final ItemStack[] results={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};public final int[] progress=new int[4],time=new int[4];public final String[] recipes={"","","",""};public int hayCount=1;private int viewers;private boolean loading;
 public StorageTile(BlockPos p,BlockState s){super(Household.TYPE.get(),p,s);kind=((StorageBlock)s.getBlock()).kind;items=NonNullList.withSize(kind.capacity,ItemStack.EMPTY);}
 public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(kind.multi()?1.1:.1);}
 public int getContainerSize(){return items.size();}public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}public ItemStack getItem(int i){return items.get(i);}public int getMaxStackSize(){return kind.display()?16:kind==Kind.DRY?1:99;}
 public ItemStack removeItem(int i,int n){ItemStack s=ContainerHelper.removeItem(items,i,n);if(!s.isEmpty())setChanged();return s;}public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
 public void setItem(int i,ItemStack s){items.set(i,s);s.limitSize(getMaxStackSize(s));setChanged();}public void clearContent(){items.clear();setChanged();}
 public boolean accepted(ItemStack s){return !s.isEmpty()&&(kind.tag.isEmpty()||s.is(KitchenII.tag("storage/"+kind.tag)));}
 public boolean canPlaceItem(int i,ItemStack s){if(fermentation.active()||kind!=Kind.CHEST&&kind!=Kind.BARREL)return false;return accepted(s)&&(kind!=Kind.BARREL||items.stream().filter(v->!v.isEmpty()).allMatch(v->v.is(s.getItem())));}
 public boolean canTakeItem(Container c,int i,ItemStack s){return !fermentation.active()&&(kind==Kind.CHEST||kind==Kind.BARREL);}
 public boolean stillValid(Player p){return !isRemoved()&&p.isAlive()&&p.level().getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getCenter())<=64;}
 public Component getDisplayName(){return Component.translatable("block.slavicmyths."+kind.id);}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return kind==Kind.CHEST?ChestMenu.threeRows(id,inv,this):new BarrelMenu(id,inv,this);}
 public static class BarrelMenu extends ChestMenu {public BarrelMenu(int id,Inventory inv,StorageTile tile){super(MenuType.GENERIC_9x1,id,inv,tile,1);for(int i=0;i<9;i++){Slot old=slots.get(i);Slot s=new Slot(tile,i,old.x,old.y){public boolean mayPlace(ItemStack stack){return tile.canPlaceItem(getContainerSlot(),stack);}};s.index=i;slots.set(i,s);}}}
 public void startOpen(Player p){if(kind==Kind.CHEST&&!p.isSpectator()){viewers++;setChanged();}}public void stopOpen(Player p){if(kind==Kind.CHEST&&!p.isSpectator()){viewers=Math.max(0,viewers-1);setChanged();}}
 @Override public void setChanged(){super.setChanged();if(!loading&&level!=null&&!level.isClientSide){derived();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}}
 public void derived(){if(level==null||level.isClientSide)return;StorageBlock b=(StorageBlock)getBlockState().getBlock();BlockState s=level.getBlockState(worldPosition);if(!s.is(b))return;
  if(kind==Kind.SACK){int count=items.stream().mapToInt(ItemStack::getCount).sum(),max=items.stream().filter(v->!v.isEmpty()).findFirst().map(ItemStack::getMaxStackSize).orElse(64)*4;int fill=count==0?0:Math.min(4,(count*4+max-1)/max);s=s.setValue(StorageBlock.FILL,fill);}
  if(kind==Kind.BARREL)s=s.setValue(StorageBlock.BREWING,fermentation.active());
  if(kind==Kind.CHEST)s=s.setValue(StorageBlock.OPEN,viewers>0);
  if(kind==Kind.HAY){s=s.setValue(StorageBlock.LEVEL,Math.max(1,hayCount));for(int part:b.parts()){BlockPos p=b.position(s,worldPosition,part);BlockState st=level.getBlockState(p);if(st.is(b)&&st.getValue(StorageBlock.LEVEL)!=Math.max(1,hayCount))level.setBlock(p,st.setValue(StorageBlock.LEVEL,Math.max(1,hayCount)),2);}}
  if(!level.getBlockState(worldPosition).equals(s))level.setBlock(worldPosition,s,2);
 }
 @Override public void onLoad(){super.onLoad();viewers=0;derived();}
 public int stage(int i){if(items.get(i).isEmpty())return 0;if(time[i]==0)return 4;int percent=progress[i]*100/time[i];return percent<40?1:percent<80?2:3;}
 public static void tick(Level w,BlockPos p,BlockState s,StorageTile t){if(w.isClientSide)return;if(t.kind==Kind.BARREL){if(t.fermentation.active()&&t.fermentation.age<12000){String before=org.slavicmyths.brewing.BrewRules.quality(t.fermentation.age);t.fermentation.age++;t.superChanged();if(!before.equals(org.slavicmyths.brewing.BrewRules.quality(t.fermentation.age)))t.setChanged();}return;}if(t.kind!=Kind.DRY)return;boolean changed=false;for(int i=0;i<4;i++){if(t.items.get(i).isEmpty()||t.time[i]==0)continue;int before=t.stage(i);t.progress[i]++;t.superChanged();if(t.progress[i]>=t.time[i]){t.items.set(i,t.results[i].copy());t.results[i]=ItemStack.EMPTY;t.time[i]=0;t.progress[i]=0;changed=true;}else if(before!=t.stage(i))changed=true;}if(changed)t.setChanged();}
 private void superChanged(){super.setChanged();}
 private int targeted(BlockHitResult hit){double x=hit.getLocation().x-worldPosition.getX(),z=hit.getLocation().z-worldPosition.getZ();Direction f=getBlockState().getValue(StorageBlock.FACING);double horizontal=f==Direction.NORTH?x:f==Direction.SOUTH?1-x:f==Direction.EAST?z:1-z;if(kind==Kind.DRY)return Math.max(0,Math.min(3,(int)(horizontal*2)));int col=Math.max(0,Math.min(2,(int)(horizontal*3)));return col+(kind==Kind.SHELF&&hit.getLocation().y-worldPosition.getY()>=1?3:0);}
 public void interact(Player p,InteractionHand hand,BlockHitResult hit){if(level.isClientSide||p.isSpectator())return;ItemStack held=p.getItemInHand(hand);
  if(kind==Kind.HAY){if(held.is(Items.HAY_BLOCK)&&hayCount<4){if(!p.getAbilities().instabuild)held.shrink(1);hayCount++;setChanged();}else if(held.isEmpty()&&p.isShiftKeyDown()){if(hayCount<=0)return;hayCount--;KitchenTile.give(p,new ItemStack(Items.HAY_BLOCK));if(hayCount==0)level.removeBlock(worldPosition,false);else setChanged();}return;}
  if(kind==Kind.DRY){int i=targeted(hit);if(held.isEmpty()){KitchenTile.give(p,items.get(i));items.set(i,ItemStack.EMPTY);results[i]=ItemStack.EMPTY;progress[i]=time[i]=0;recipes[i]="";setChanged();}else if(items.get(i).isEmpty()){var match=level.getRecipeManager().getRecipeFor(DryingRecipe.TYPE.get(),new SingleRecipeInput(held),level);if(match.isPresent()){var r=match.get();items.set(i,held.copyWithCount(1));if(!p.getAbilities().instabuild)held.shrink(1);results[i]=r.value().result().copy();time[i]=r.value().dryingTime();progress[i]=0;recipes[i]=r.id().toString();setChanged();}}return;}
  if(held.isEmpty()){int i=kind.display()?targeted(hit):-1;if(i<0)for(int j=items.size()-1;j>=0;j--)if(!items.get(j).isEmpty()){i=j;break;}if(i>=0)KitchenTile.give(p,removeItem(i,p.isShiftKeyDown()?items.get(i).getCount():1));return;}
  if(!accepted(held))return;if(kind==Kind.SACK&&items.stream().filter(v->!v.isEmpty()).anyMatch(v->!v.is(held.getItem())))return;
  int n=p.isShiftKeyDown()?held.getCount():1;int first=kind.display()?targeted(hit):0,last=kind.display()?first+1:items.size();
  for(int i=first;i<last&&n>0;i++){ItemStack old=items.get(i);if(!old.isEmpty()&&!ItemStack.isSameItemSameComponents(old,held))continue;int put=Math.min(n,getMaxStackSize(held)-old.getCount());if(put<=0)continue;if(old.isEmpty())items.set(i,held.copyWithCount(put));else old.grow(put);if(!p.getAbilities().instabuild)held.shrink(put);n-=put;}setChanged();
 }
 public void dropAll(){if(kind==Kind.HAY){int count=hayCount;hayCount=0;if(count>0)Block.popResource(level,worldPosition,new ItemStack(Items.HAY_BLOCK,count));return;}for(int i=0;i<items.size();i++){ItemStack s=items.get(i);items.set(i,ItemStack.EMPTY);if(!s.isEmpty())Block.popResource(level,worldPosition,s);}}
 protected void saveAdditional(CompoundTag n,HolderLookup.Provider p){super.saveAdditional(n,p);n.putInt("DataVersion",1);if(kind==Kind.BARREL)fermentation.save(n);if(kind==Kind.HAY)n.putInt("HayCount",hayCount);else ContainerHelper.saveAllItems(n,items,p);if(kind==Kind.DRY){n.putIntArray("DryProgress",progress);n.putIntArray("DryTime",time);for(int i=0;i<4;i++){n.putString("DryRecipe"+i,recipes[i]);if(!results[i].isEmpty())n.put("DryResult"+i,results[i].save(p));}}}
 protected void loadAdditional(CompoundTag n,HolderLookup.Provider p){super.loadAdditional(n,p);loading=true;if(kind==Kind.BARREL)fermentation.load(n);items.clear();if(kind==Kind.HAY)hayCount=Math.max(1,Math.min(4,n.getInt("HayCount")));else ContainerHelper.loadAllItems(n,items,p);for(ItemStack s:items)s.limitSize(getMaxStackSize(s));if(kind==Kind.DRY){int[]pr=n.getIntArray("DryProgress"),ti=n.getIntArray("DryTime");for(int i=0;i<4;i++){time[i]=i<ti.length?Math.max(0,Math.min(120000,ti[i])):0;progress[i]=i<pr.length?Math.max(0,Math.min(time[i],pr[i])):0;results[i]=ItemStack.parseOptional(p,n.getCompound("DryResult"+i));recipes[i]=n.getString("DryRecipe"+i);if(items.get(i).isEmpty()||results[i].isEmpty())time[i]=0;}}loading=false;}
 public CompoundTag getUpdateTag(HolderLookup.Provider p){return saveWithoutMetadata(p);}public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
