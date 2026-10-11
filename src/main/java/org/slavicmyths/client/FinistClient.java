package org.slavicmyths.client;
public final class FinistClient {
 public static void properties(){var bow=(org.slavicmyths.finist.FinistBow)org.slavicmyths.finist.FinistItems.BOW.get();net.minecraft.client.renderer.item.ItemProperties.register(bow,net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull"),(s,l,e,seed)->e==null||e.getUseItem()!=s?0:bow.power(s.getUseDuration(e)-e.getUseItemRemainingTicks()));net.minecraft.client.renderer.item.ItemProperties.register(bow,net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling"),(s,l,e,seed)->e!=null&&e.isUsingItem()&&e.getUseItem()==s?1:0);}
 private FinistClient(){}
}
