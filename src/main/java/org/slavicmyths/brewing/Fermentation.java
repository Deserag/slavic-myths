package org.slavicmyths.brewing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.slavicmyths.kitchen.KitchenTile;
import org.slavicmyths.storage.StorageTile;
/** Per-barrel state; no manager, unloaded blocks cannot age. */
public final class Fermentation {
 public String beverage="";public int age,servings;
 public boolean active(){return servings>0;}
 public void save(CompoundTag n){if(active()){CompoundTag b=new CompoundTag();b.putString("Beverage",beverage);b.putInt("Age",age);b.putInt("Servings",servings);n.put("Fermentation",b);}}
 public void load(CompoundTag n){CompoundTag b=n.getCompound("Fermentation");beverage=b.getString("Beverage");age=Math.max(0,Math.min(12000,b.getInt("Age")));servings=Math.max(0,Math.min(8,b.getInt("Servings")));if(!Brewing.BATCHES.containsValue(beverage)){servings=0;beverage="";}}
 public boolean interact(StorageTile tile,Player p,InteractionHand hand){ItemStack held=p.getItemInHand(hand);String batch=Brewing.BATCHES.get(Brewing.key(held));
  if(!active()){if(batch==null||!tile.isEmpty())return false;if(!p.level().isClientSide){beverage=batch;age=0;servings=8;held.consume(1,p);tile.setChanged();}return true;}
  if(!p.level().isClientSide){String container=Brewing.key(held),q=BrewRules.quality(age),b=q.equals("spoiled")?"spoiled_brew":beverage;int n=container.equals("ceramic_pitcher")&&BeverageItem.pitcherAllowed(beverage)?Math.min(4,servings):container.equals("dark_bottle")||container.equals("wooden_mug")&&BeverageItem.mugAllowed(beverage)?1:0;
   if(n>0){ItemStack out=BeverageItem.filled(b,q,container,n);held.shrink(1);servings-=n;if(servings==0)beverage="";tile.setChanged();BeverageItem.obtained(p,out);KitchenTile.give(p,out);}else p.displayClientMessage(Component.translatable("brewing.slavicmyths.status",Component.translatable("brewing.slavicmyths."+q),servings),true);
  }return true;
 }
}
