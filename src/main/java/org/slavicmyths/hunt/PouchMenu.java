package org.slavicmyths.hunt;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.RpgMenu;
/** The held stack is the storage identity. Every mutation writes NBT immediately, not only on close. */
public final class PouchMenu extends Container {
 public static final RegistryObject<ContainerType<PouchMenu>> TYPE=RpgMenu.MENUS.register("trophy_pouch",()->IForgeContainerType.create((id,inv,b)->new PouchMenu(id,inv,Hand.values()[b.readVarInt()])));
 private static final net.minecraftforge.common.Tags.IOptionalNamedTag<Item> TROPHIES=ItemTags.createOptional(new ResourceLocation("slavicmyths","hunt_trophies"));
 public static void register(){}
 public static boolean allowed(ItemStack s){return !s.isEmpty()&&s.getItem()!=ModItems.MESHOCHEK_TROFEEV.get()&&!(s.getItem() instanceof BlockItem&&((BlockItem)s.getItem()).getBlock() instanceof net.minecraft.block.ShulkerBoxBlock)&&s.getItem().is(TROPHIES);}
 private final ItemStack pouch;private final Hand hand;private final int selected;private final PlayerInventory playerInventory;private final Inventory storage;
 public static void open(ServerPlayerEntity p,Hand hand){net.minecraftforge.fml.network.NetworkHooks.openGui(p,new net.minecraft.inventory.container.SimpleNamedContainerProvider((id,inv,player)->new PouchMenu(id,inv,hand),new TranslationTextComponent("container.slavicmyths.trophy_pouch")),b->b.writeVarInt(hand.ordinal()));p.level.playSound(null,p.blockPosition(),SoundEvents.ARMOR_EQUIP_LEATHER,SoundCategory.PLAYERS,.4F,.8F);}
 public PouchMenu(int id,PlayerInventory inv,Hand hand){super(TYPE.get(),id);this.hand=hand;playerInventory=inv;selected=inv.selected;pouch=inv.player.getItemInHand(hand);storage=new Inventory(9){@Override public void setChanged(){super.setChanged();if(!inv.player.level.isClientSide){save();for(int i=0;i<9;i++)if(allowed(getItem(i))){org.slavicmyths.item.FolkAccessoryItem.award(inv.player,"trophy_keeper");break;}}}};
  net.minecraft.nbt.ListNBT contents=pouch.getOrCreateTag().getList("HuntTrophies",10);for(net.minecraft.nbt.INBT raw:contents){net.minecraft.nbt.CompoundNBT n=(net.minecraft.nbt.CompoundNBT)raw;int slot=n.getByte("Slot")&255;if(slot<9)storage.setItem(slot,ItemStack.of(n));}
  for(int i=0;i<9;i++)addSlot(new Slot(storage,i,8+i*18,20){@Override public boolean mayPlace(ItemStack s){return allowed(s);}});
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,8+col*18,52+row*18));for(int col=0;col<9;col++){final boolean locked=hand==Hand.MAIN_HAND&&col==selected;addSlot(new Slot(inv,col,8+col*18,110){@Override public boolean mayPickup(PlayerEntity p){return !locked;}@Override public boolean mayPlace(ItemStack s){return !locked;}});}
 }
 private void save(){if(storage==null)return;net.minecraft.nbt.ListNBT items=new net.minecraft.nbt.ListNBT();for(int i=0;i<9;i++)if(!storage.getItem(i).isEmpty()){net.minecraft.nbt.CompoundNBT n=storage.getItem(i).save(new net.minecraft.nbt.CompoundNBT());n.putByte("Slot",(byte)i);items.add(n);}pouch.getOrCreateTag().put("HuntTrophies",items);playerInventory.setChanged();}
 @Override public boolean stillValid(PlayerEntity p){return p.isAlive()&&p.getItemInHand(hand)==pouch&&pouch.getItem()==ModItems.MESHOCHEK_TROFEEV.get()&&(hand==Hand.OFF_HAND||p.inventory.selected==selected);}
 @Override public ItemStack clicked(int slot,int button,ClickType type,PlayerEntity player){if(!stillValid(player))return ItemStack.EMPTY;if(type==ClickType.SWAP&&(button==40||hand==Hand.MAIN_HAND&&button==selected))return ItemStack.EMPTY;ItemStack result=super.clicked(slot,button,type,player);if(!player.level.isClientSide)save();return result;}
 @Override public ItemStack quickMoveStack(PlayerEntity p,int i){if(!stillValid(p)||i<0||i>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();if(i<9){if(!moveItemStackTo(s,9,slots.size(),true))return ItemStack.EMPTY;}else{if(!allowed(s)||!moveItemStackTo(s,0,9,false))return ItemStack.EMPTY;}if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);save();return copy;}
 @Override public void removed(PlayerEntity p){super.removed(p);if(!p.level.isClientSide){save();p.level.playSound(null,p.blockPosition(),SoundEvents.ARMOR_EQUIP_LEATHER,SoundCategory.PLAYERS,.25F,.6F);}}
}
