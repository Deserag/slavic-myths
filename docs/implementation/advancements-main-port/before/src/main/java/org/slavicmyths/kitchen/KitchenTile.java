package org.slavicmyths.kitchen;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
public final class KitchenTile extends BlockEntity implements MenuProvider {
 public final SimpleContainer inventory=new SimpleContainer(9){@Override public void setChanged(){super.setChanged();changed();}};
 public int progress,total,servings,maxServings,pendingServings;public boolean activePot;public String activeId="";public ItemStack pending=ItemStack.EMPTY,dish=ItemStack.EMPTY;
 private boolean dirty=true,updating;private int lastVisualProgress;
 public KitchenTile(BlockPos p,BlockState s){super(KitchenII.TILE.get(),p,s);}
 public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(1);}
 public boolean busy(){return total>0;}
 public boolean toolLocked(int slot){return slot==1?(servings>0||busy()&&activePot):busy()&&!activePot;}
 public void changed(){setChanged();dirty=true;if(!updating)sync();}
 private void sync(){if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}
 public Component getDisplayName(){return Component.translatable("block.slavicmyths.kitchen_table");}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new KitchenMenu(id,inv,worldPosition,inventory,this);}
 public KitchenInput input(){List<ItemStack>a=new ArrayList<>();for(int i=2;i<6;i++)a.add(inventory.getItem(i));return new KitchenInput(a);}
 private static boolean insert(ItemStack[] slots,ItemStack incoming){ItemStack rest=incoming.copy();for(int i=0;i<slots.length;i++)if(!slots[i].isEmpty()&&ItemStack.isSameItemSameComponents(slots[i],rest)){int n=Math.min(rest.getCount(),slots[i].getMaxStackSize()-slots[i].getCount());slots[i].grow(n);rest.shrink(n);if(rest.isEmpty())return true;}for(int i=0;i<slots.length;i++)if(slots[i].isEmpty()){int n=Math.min(rest.getCount(),rest.getMaxStackSize());slots[i]=rest.copyWithCount(n);rest.shrink(n);if(rest.isEmpty())return true;}return false;}
 private boolean resultFits(ItemStack result){ItemStack s=inventory.getItem(6);return s.isEmpty()||ItemStack.isSameItemSameComponents(s,result)&&s.getCount()+result.getCount()<=s.getMaxStackSize();}
 private ItemStack[] returns(KitchenRecipe recipe){ItemStack[] slots={inventory.getItem(7).copy(),inventory.getItem(8).copy()};for(ItemStack s:recipe.remainders())if(!insert(slots,s))return null;return slots;}
 private void start(){
  dirty=false;KitchenInput input=input();
  for(RecipeHolder<KitchenRecipe> holder:level.getRecipeManager().getRecipesFor(KitchenRecipe.TYPE.get(),input,level)){
   KitchenRecipe r=holder.value();int tool=r.pot()?1:0;
   if(!r.tool().test(inventory.getItem(tool))||r.pot()&&servings>0||!r.pot()&&!resultFits(r.result()))continue;
   ItemStack[] rem=returns(r);if(rem==null)continue;int[]used=r.allocation(input);if(used==null)continue;
   updating=true;for(int i=0;i<4;i++)inventory.removeItem(i+2,used[i]);inventory.setItem(7,rem[0]);inventory.setItem(8,rem[1]);
   pending=r.result().copy();activePot=r.pot();pendingServings=r.servings();activeId=holder.id().toString();total=r.time();progress=0;lastVisualProgress=0;dirty=false;updating=false;setChanged();sync();break;
  }
 }
 public static void tick(Level w,BlockPos p,BlockState s,KitchenTile t){
  if(w.isClientSide)return;
  if(!t.busy()){if(t.dirty)t.start();return;}
  t.progress++;t.setChanged();
  if(t.progress>=t.total){
   if(!t.activePot&&!t.resultFits(t.pending))return; // Keep the saved pending result until actual capacity exists.
   t.updating=true;if(t.activePot){t.dish=t.pending.copyWithCount(1);t.maxServings=t.pendingServings;t.servings=t.maxServings;}else{ItemStack out=t.inventory.getItem(6);if(out.isEmpty())t.inventory.setItem(6,t.pending.copy());else out.grow(t.pending.getCount());}
   t.pending=ItemStack.EMPTY;t.total=0;t.progress=0;t.activeId="";t.updating=false;t.dirty=true;t.sync();w.playSound(null,p,SoundEvents.UI_LOOM_TAKE_RESULT,SoundSource.BLOCKS,.5F,1F);
  }else if(t.progress/10!=t.lastVisualProgress/10){t.lastVisualProgress=t.progress;t.sync();}
 }
 public boolean install(Player p,InteractionHand h,int slot){if(!inventory.getItem(slot).isEmpty())return false;ItemStack held=p.getItemInHand(h);Item expected=KitchenII.item(slot==0?"rolling_pin":"metal_pot");if(!held.is(expected))return false;if(!level.isClientSide){inventory.setItem(slot,held.copyWithCount(1));if(!p.getAbilities().instabuild)held.shrink(1);}return true;}
 public void removeTool(Player p,int slot){if(toolLocked(slot)){p.displayClientMessage(Component.translatable("kitchen.slavicmyths.serve_first"),true);return;}if(!level.isClientSide)give(p,inventory.removeItemNoUpdate(slot));changed();}
 public boolean serve(Player p,ItemStack bowl){if(servings<=0||!bowl.is(Items.BOWL))return false;if(!level.isClientSide){if(!p.getAbilities().instabuild)bowl.shrink(1);ItemStack result=dish.copyWithCount(1);servings--;if(servings==0){dish=ItemStack.EMPTY;maxServings=0;}changed();give(p,result);}return true;}
 public static void give(Player p,ItemStack s){if(!s.isEmpty()&&!p.getInventory().add(s))p.drop(s,false);}
 public void dropInventory(){updating=true;for(int i=0;i<9;i++){ItemStack s=inventory.removeItemNoUpdate(i);if(!s.isEmpty())net.minecraft.world.level.block.Block.popResource(level,worldPosition,s);}updating=false;servings=0;dish=ItemStack.EMPTY;pending=ItemStack.EMPTY;total=0;}
 @Override public void onLoad(){super.onLoad();dirty=true;}
 @Override protected void saveAdditional(CompoundTag n,HolderLookup.Provider p){super.saveAdditional(n,p);NonNullList<ItemStack>st=NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)st.set(i,inventory.getItem(i));ContainerHelper.saveAllItems(n,st,p);n.putInt("Progress",progress);n.putInt("Total",total);n.putInt("Servings",servings);n.putInt("MaxServings",maxServings);n.putInt("PendingServings",pendingServings);n.putBoolean("ActivePot",activePot);n.putString("ActiveRecipe",activeId);if(!pending.isEmpty())n.put("Pending",pending.save(p));if(!dish.isEmpty())n.put("Dish",dish.save(p));}
 @Override protected void loadAdditional(CompoundTag n,HolderLookup.Provider p){super.loadAdditional(n,p);updating=true;NonNullList<ItemStack>st=NonNullList.withSize(9,ItemStack.EMPTY);ContainerHelper.loadAllItems(n,st,p);for(int i=0;i<9;i++)inventory.setItem(i,st.get(i));total=Math.max(0,Math.min(12000,n.getInt("Total")));progress=Math.max(0,Math.min(total,n.getInt("Progress")));maxServings=Math.max(0,Math.min(64,n.getInt("MaxServings")));servings=Math.max(0,Math.min(maxServings,n.getInt("Servings")));pendingServings=Math.max(0,Math.min(64,n.getInt("PendingServings")));activePot=n.getBoolean("ActivePot");activeId=n.getString("ActiveRecipe");pending=ItemStack.parseOptional(p,n.getCompound("Pending"));dish=ItemStack.parseOptional(p,n.getCompound("Dish"));if(pending.isEmpty())total=0;if(dish.isEmpty())servings=0;updating=false;dirty=true;}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider p){return saveWithoutMetadata(p);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
