package org.slavicmyths.hunt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.RpgMenu;
/** The held stack is the storage identity. Every mutation writes components immediately, not only on close. */
public final class PouchMenu extends AbstractContainerMenu {
 public static final DeferredHolder<MenuType<?>, MenuType<PouchMenu>> TYPE=RpgMenu.MENUS.register("trophy_pouch",()->IMenuTypeExtension.create((id,inv,b)->new PouchMenu(id,inv,InteractionHand.values()[b.readVarInt()])));
 private static final TagKey<Item> TROPHIES=ItemTags.create(ResourceLocation.fromNamespaceAndPath("slavicmyths","hunt_trophies"));
 public static void register(){}
 public static boolean allowed(ItemStack s){return !s.isEmpty()&&s.getItem()!=ModItems.MESHOCHEK_TROFEEV.get()&&!(s.getItem() instanceof BlockItem&&((BlockItem)s.getItem()).getBlock() instanceof net.minecraft.world.level.block.ShulkerBoxBlock)&&s.is(TROPHIES);}
 private final ItemStack pouch;private final InteractionHand hand;private final int selected;private final Inventory playerInventory;private final SimpleContainer storage;private boolean loading=true;
 public static void open(ServerPlayer p,InteractionHand hand){p.openMenu(new SimpleMenuProvider((id,inv,player)->new PouchMenu(id,inv,hand),Component.translatable("container.slavicmyths.trophy_pouch")),b->b.writeVarInt(hand.ordinal()));p.level().playSound(null,p.blockPosition(),SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.PLAYERS,.4F,.8F);}
 public PouchMenu(int id,Inventory inv,InteractionHand hand){super(TYPE.get(),id);this.hand=hand;playerInventory=inv;selected=inv.selected;pouch=inv.player.getItemInHand(hand);storage=new SimpleContainer(9){@Override public void setChanged(){super.setChanged();if(!loading&&!inv.player.level().isClientSide){save();for(int i=0;i<9;i++)if(allowed(getItem(i))){org.slavicmyths.item.FolkAccessoryItem.award(inv.player,"trophy_keeper");break;}}}};
  var contents=org.slavicmyths.item.ItemState.inventory(pouch,false,inv.player.registryAccess());
  for(int i=0;i<9;i++)storage.setItem(i,contents.get(i));loading=false;
  for(int i=0;i<9;i++)addSlot(new Slot(storage,i,8+i*18,20){@Override public boolean mayPlace(ItemStack s){return allowed(s);}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,52+row*18));for(int col=0;col<9;col++){final boolean locked=hand==InteractionHand.MAIN_HAND&&col==selected;addSlot(new Slot(inv,col,8+col*18,110){@Override public boolean mayPickup(Player p){return !locked;}@Override public boolean mayPlace(ItemStack s){return !locked;}});}
 }
 private void save(){if(storage==null||loading)return;var items=net.minecraft.core.NonNullList.withSize(9,ItemStack.EMPTY);for(int i=0;i<9;i++)items.set(i,storage.getItem(i).copy());org.slavicmyths.item.ItemState.inventory(pouch,false,items);playerInventory.setChanged();}
 @Override public boolean stillValid(Player p){return p.isAlive()&&p.getItemInHand(hand)==pouch&&pouch.getItem()==ModItems.MESHOCHEK_TROFEEV.get()&&(hand==InteractionHand.OFF_HAND||p.getInventory().selected==selected);}
 @Override public void clicked(int slot,int button,ClickType type,Player player){if(!stillValid(player))return;if(type==ClickType.SWAP&&(button==40||hand==InteractionHand.MAIN_HAND&&button==selected))return;super.clicked(slot,button,type,player);if(!player.level().isClientSide)save();}
 @Override public ItemStack quickMoveStack(Player p,int i){if(!stillValid(p)||i<0||i>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();if(i<9){if(!moveItemStackTo(s,9,slots.size(),true))return ItemStack.EMPTY;}else{if(!allowed(s)||!moveItemStackTo(s,0,9,false))return ItemStack.EMPTY;}if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);save();return copy;}
 @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide){save();p.level().playSound(null,p.blockPosition(),SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.PLAYERS,.25F,.6F);}}
}
