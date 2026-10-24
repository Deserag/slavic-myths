package org.slavicmyths.industry;
import java.util.Optional;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
/** Append missing specialist offers, including merchants loaded with an old saved offer list. */
public final class ChudToolTrades {
 public static void ensure(MerchantOffers offers,int role){
  if(role==1){add(offers,ChudTools.AXE.get(),12,Items.DIAMOND,6,2);add(offers,ChudTools.LIFT.get(),8,Items.EMERALD,16,2);}
  if(role==2){add(offers,ChudTools.LANTERN.get(),4,Items.AMETHYST_SHARD,8,4);add(offers,ChudTools.PICKAXE.get(),16,Items.DIAMOND,8,2);}
 }
 private static void add(MerchantOffers offers,Item out,int iron,Item second,int count,int stock){
  if(offers.stream().anyMatch(o->o.getResult().is(out)))return;
  offers.add(new MerchantOffer(new ItemCost(Industry.IRON.get(),iron),Optional.of(new ItemCost(second,count)),new ItemStack(out),stock,1,0));
 }
 private ChudToolTrades(){}
}
