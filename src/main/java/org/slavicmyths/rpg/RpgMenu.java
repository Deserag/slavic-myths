package org.slavicmyths.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.ModBlocks;
public final class RpgMenu extends AbstractContainerMenu {
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"slavicmyths");
 public static final DeferredHolder<MenuType<?>,MenuType<RpgMenu>> TYPE=MENUS.register("rpg",()->IMenuTypeExtension.create((id,inv,b)->new RpgMenu(id,inv,b.readBlockPos(),b.readBoolean())));
 public final BlockPos pos;public final boolean anvil;public final SimpleContainer input=new SimpleContainer(2);
 public int mode,recipeIndex;
 public final SimpleContainer crafting=new SimpleContainer(4){@Override public void setChanged(){super.setChanged();updateResult();}};
 public final SimpleContainer result=new SimpleContainer(1);
 private final Player owner;private boolean committed,updating;
 public RpgMenu(int id,Inventory inv,BlockPos pos,boolean anvil){
  super(TYPE.get(),id);this.pos=pos;this.anvil=anvil;this.owner=inv.player;mode=anvil?3:0;
  addDataSlot(new DataSlot(){public int get(){return mode;}public void set(int n){mode=n;}});
  addDataSlot(new DataSlot(){public int get(){return recipeIndex;}public void set(int n){recipeIndex=n;}});
  if(anvil){
   addSlot(new Slot(input,0,30,54){@Override public boolean isActive(){return mode!=3;}@Override public int getMaxStackSize(){return 1;}@Override public boolean mayPlace(ItemStack s){return mode!=3&&Runes.max(s)>0;}});
   addSlot(new Slot(input,1,70,54){@Override public boolean isActive(){return mode<2;}@Override public boolean mayPlace(ItemStack s){return mode==0&&(s.is(net.minecraft.world.item.Items.IRON_INGOT)||s.is(org.slavicmyths.registry.ModItems.SILVER_INGOT.get())||s.is(org.slavicmyths.registry.ModItems.PERUNITE.get()))||mode==1&&!Runes.rune(s).isEmpty();}});
   for(int n=0;n<4;n++){final int index=n;addSlot(new Slot(crafting,n,20+n*32,54){@Override public boolean isActive(){var r=selectedRecipe();return mode==3&&(!crafting.getItem(index).isEmpty()||r!=null&&(index==3?r.chisel():index<r.inputs().size()));}@Override public boolean mayPlace(ItemStack s){if(mode!=3)return false;if(index==3)return s.is(RuneFoundation.item("rune_chisel"));var r=selectedRecipe();return r!=null&&index<r.inputs().size()&&r.inputs().get(index).test(s);}});}
   addSlot(new Slot(result,0,158,54){@Override public boolean isActive(){return mode==3;}@Override public boolean mayPlace(ItemStack s){return false;}
    @Override public boolean mayPickup(Player p){return mode==3&&stillValid(p)&&(committed||canCraft());}
    @Override public ItemStack remove(int n){if(n<=0||!mayPickup(owner))return ItemStack.EMPTY;if(!committed&&!commitCraft())return ItemStack.EMPTY;return super.remove(n);}
    @Override public void onTake(Player p,ItemStack stack){if(result.isEmpty()){committed=false;updateResult();}super.onTake(p,stack);}
   });
   for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,20+col*18,132+row*18));
   for(int col=0;col<9;col++)addSlot(new Slot(inv,col,20+col*18,190));}

 }
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockState(pos).getBlock()==(anvil?ModBlocks.RUNIC_ANVIL.get():ModBlocks.PATH_STONE.get());}
 @Override public ItemStack quickMoveStack(Player p,int i){
  if(!anvil||!stillValid(p)||i<0||i>=slots.size())return ItemStack.EMPTY;if(i==6)updateResult();Slot slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;
  ItemStack stack=slot.getItem(),copy=stack.copy();
  if(i==6){if(!slot.mayPickup(p)||!fitsOutput(stack))return ItemStack.EMPTY;ItemStack taken=slot.remove(stack.getCount());if(taken.isEmpty())return ItemStack.EMPTY;moveItemStackTo(taken,7,slots.size(),true);slot.onTake(p,copy);return copy;}
  if(i<6){if(!slot.isActive()||!moveItemStackTo(stack,7,slots.size(),true))return ItemStack.EMPTY;}
  else if(mode==3){boolean moved=false;for(int n=2;n<6;n++)if(slots.get(n).mayPlace(stack)&&moveItemStackTo(stack,n,n+1,false)){moved=true;break;}if(!moved)return ItemStack.EMPTY;}
  else if(Runes.max(stack)>0){if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;}
  else if(!slots.get(1).mayPlace(stack)||!moveItemStackTo(stack,1,2,false))return ItemStack.EMPTY;
  if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;
 }
 @Override public void clicked(int slot,int button,ClickType type,Player player){
  // Output is transactional. Hotbar swaps and pickup-all must not bypass Slot.remove.
  if(anvil&&slot==6&&type!=ClickType.PICKUP&&type!=ClickType.QUICK_MOVE&&type!=ClickType.THROW)return;
  super.clicked(slot,button,type,player);
 }
 @Override public boolean canTakeItemForPickAll(ItemStack stack,Slot slot){return slot.index!=6&&super.canTakeItemForPickAll(stack,slot);}
 @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide){updating=true;clearContainer(p,input);clearContainer(p,crafting);if(committed)clearContainer(p,result);result.clearContent();}}
 @Override public boolean clickMenuButton(Player p,int action){
  if(!(p instanceof ServerPlayer)||!stillValid(p))return false;
  if(anvil){if(action>=100&&action<=103){if(committed&&!result.isEmpty())return false;mode=action-100;updateResult();}else if(action>=200){int index=action-200;if(mode!=3||index>=recipes().size()||committed)return false;recipeIndex=index;updateResult();}else if(action>=0&&action<=4&&mode!=3)Runes.operation((ServerPlayer)p,input,action);else return false;}
  else if(action>=0&&action<4)PathData.choose((ServerPlayer)p,action);
  else if(action>=10&&action<34)PathData.buy((ServerPlayer)p,action-10);
  else if(action>=50&&action<74&&PathData.has(p,PathData.SKILLS[action-50])&&Abilities.active(action-50))PathData.data(p).putInt("Active",action-50+1);
  broadcastChanges();RpgNetwork.sync((ServerPlayer)p);return true;
 }
 public java.util.List<net.minecraft.world.item.crafting.RecipeHolder<RuneCraftRecipe>> recipes(){return owner.level().getRecipeManager().getAllRecipesFor(RuneCraftRecipe.TYPE.get()).stream().sorted(java.util.Comparator.comparing(r->r.id().toString())).toList();}
 public RuneCraftRecipe selectedRecipe(){var all=recipes();return recipeIndex>=0&&recipeIndex<all.size()?all.get(recipeIndex).value():null;}
 private RuneCraftRecipe.Input craftInput(){return new RuneCraftRecipe.Input(java.util.stream.IntStream.range(0,4).mapToObj(crafting::getItem).toList());}
 public boolean canCraft(){var r=selectedRecipe();return mode==3&&r!=null&&r.matches(craftInput(),owner.level());}
 private void updateResult(){if(owner==null||owner.level().isClientSide||updating||committed||!anvil)return;updating=true;var r=selectedRecipe();result.setItem(0,canCraft()?r.getResultItem(owner.registryAccess()):ItemStack.EMPTY);updating=false;}
 private boolean commitCraft(){if(owner.level().isClientSide||!canCraft()||!stillValid(owner))return false;var r=selectedRecipe();committed=true;updating=true;
  result.setItem(0,r.assemble(craftInput(),owner.registryAccess()));for(int n=0;n<r.inputs().size();n++)crafting.removeItem(n,r.inputs().get(n).count());
  if(r.chisel()){ItemStack tool=crafting.getItem(3);int damage=tool.getDamageValue()+1;if(damage>=tool.getMaxDamage())crafting.setItem(3,ItemStack.EMPTY);else tool.setDamageValue(damage);}
  crafting.setChanged();updating=false;
  owner.level().playSound(null,pos,r.chisel()?net.minecraft.sounds.SoundEvents.ANVIL_USE:net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,net.minecraft.sounds.SoundSource.BLOCKS,.35F,1.2F);
  if(owner.level() instanceof net.minecraft.server.level.ServerLevel world&&!r.chisel())world.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,4,.15,.1,.15,0);
  return true;
 }
 private boolean fitsOutput(ItemStack out){int remaining=out.getCount();for(int n=7;n<slots.size();n++){ItemStack s=slots.get(n).getItem();if(s.isEmpty())remaining-=Math.min(out.getMaxStackSize(),slots.get(n).getMaxStackSize(out));else if(ItemStack.isSameItemSameComponents(s,out))remaining-=Math.max(0,Math.min(s.getMaxStackSize(),slots.get(n).getMaxStackSize(out))-s.getCount());if(remaining<=0)return true;}return false;}
}
