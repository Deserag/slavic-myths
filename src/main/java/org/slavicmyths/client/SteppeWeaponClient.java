package org.slavicmyths.client;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import org.slavicmyths.savanna.SteppeWeapons;
public final class SteppeWeaponClient {
 public static void properties(){
  ItemProperties.register(SteppeWeapons.item(SteppeWeapons.Kind.SKARAPEYA_BLADE),ResourceLocation.fromNamespaceAndPath("slavicmyths","steppe_split"),(s,l,e,seed)->SteppeWeapons.split(s)?1:0);
  for(var k:SteppeWeapons.Kind.values())if(k.bow()||k==SteppeWeapons.Kind.STEPPE_CROSSBOW){var item=SteppeWeapons.item(k);ItemProperties.register(item,ResourceLocation.withDefaultNamespace("pulling"),(s,l,e,seed)->e!=null&&e.isUsingItem()&&e.getUseItem()==s?1:0);ItemProperties.register(item,ResourceLocation.withDefaultNamespace("pull"),(s,l,e,seed)->e==null||e.getUseItem()!=s?0:(s.getUseDuration(e)-e.getUseItemRemainingTicks())/(float)(item instanceof CrossbowItem?CrossbowItem.getChargeDuration(s,e):20));if(item instanceof CrossbowItem)ItemProperties.register(item,ResourceLocation.withDefaultNamespace("charged"),(s,l,e,seed)->CrossbowItem.isCharged(s)?1:0);}
 }
 private SteppeWeaponClient(){}
}
