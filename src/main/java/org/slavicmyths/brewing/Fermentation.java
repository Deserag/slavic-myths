package org.slavicmyths.brewing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.slavicmyths.kitchen.KitchenTile;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
/** Per-vessel state; no manager, unloaded blocks cannot age. Legacy NBT is unchanged. */
public final class Fermentation {
 public String beverage="";public int age,servings;
 public boolean active(){return servings>0;}
 public void save(CompoundTag n){if(active()){CompoundTag b=new CompoundTag();b.putString("Beverage",beverage);b.putInt("Age",age);b.putInt("Servings",servings);n.put("Fermentation",b);}}
 public void load(CompoundTag n){load(n,Brewing.BATCHES.values());}
 void load(CompoundTag n,java.util.Collection<String> beverages){CompoundTag b=n.getCompound("Fermentation");beverage=b.getString("Beverage");age=Math.max(0,Math.min(12000,b.getInt("Age")));servings=Math.max(0,Math.min(8,b.getInt("Servings")));if(!beverages.contains(beverage)){servings=0;beverage="";}}
 public boolean interact(BlockEntity tile,Player p,InteractionHand hand,boolean allowStart){ItemStack held=p.getItemInHand(hand);String batch=Brewing.BATCHES.get(Brewing.key(held));
  if(!active()){if(!allowStart||batch==null||!((Container)tile).isEmpty())return false;if(!p.level().isClientSide){beverage=batch;age=0;servings=8;held.consume(1,p);tile.setChanged();}return true;}
  if(!p.level().isClientSide){String container=Brewing.key(held),q=BrewRules.quality(age),b=q.equals("spoiled")?"spoiled_brew":beverage;int n=BrewRules.pourCount(container,BeverageItem.pitcherAllowed(beverage),BeverageItem.mugAllowed(beverage),servings);
   if(n>0){ItemStack out=BeverageItem.filled(b,q,container,n);held.shrink(1);servings-=n;if(servings==0)beverage="";tile.setChanged();BeverageItem.obtained(p,out);KitchenTile.give(p,out);}else p.displayClientMessage(Component.translatable("brewing.slavicmyths.status",Component.translatable("brewing.slavicmyths."+q),servings),true);
  }return true;
 }
 /** Preserve every remaining serving when either vessel is broken. Clear before spawning. */
 public void dropAll(BlockEntity tile){String b=BrewRules.quality(age).equals("spoiled")?"spoiled_brew":beverage,q=BrewRules.quality(age);int count=servings;servings=0;beverage="";age=0;for(int i=0;i<count;i++)Block.popResource(tile.getLevel(),tile.getBlockPos(),BeverageItem.filled(b,q,BeverageItem.mugAllowed(b)?"wooden_mug":"dark_bottle",1));}
}
