package org.slavicmyths.yaga;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
/** Finite, shared catalogue: the server, menu and JEI all read these same entries. */
public final class YagaServices {
 public static final class Need{public final String id;public final int count;public Need(String id,int n){this.id=id.contains(":")?id:"slavicmyths:"+id;count=n;}public Item item(){Item i=BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));if(i==null||i==Items.AIR)throw new IllegalStateException("Missing Yaga ingredient "+id);return i;}public ItemStack stack(){return new ItemStack(item(),count);}}
 public static final class Entry{public final String id,result;public final int amount,stage;public final Need[] inputs;Entry(String id,int stage,String result,int amount,Need...needs){this.id=id;this.stage=stage;this.result=result;this.amount=amount;inputs=needs;}public ItemStack output(){return result.isEmpty()?ItemStack.EMPTY:new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",result)),amount);}}
 private static Need n(String id,int count){return new Need(id,count);}
 public static final Entry[] MAIN={
  new Entry("not_empty_handed",0,"svyazka_trav_yagi",2,n("ovinnaya_zola",2),n("klyk_volkolaka",1),n("pylayuschaya_cheshuya",1),n("uzel_podveya",1)),
  new Entry("heat_and_wind",1,"putevodny_klubok",1,n("sosud_ognennogo_dyhaniya",1),n("sosud_podveya",1)),
  new Entry("prove_strength",2,"yagin_nastoy_stoykosti",2)};
 public static final Entry[] CONTRACTS={
  new Entry("contract_ovinnik",1,"svyazka_trav_yagi",2,n("ovinnaya_zola",3)),new Entry("contract_volkolak",1,"svyazka_trav_yagi",2,n("klyk_volkolaka",2)),
  new Entry("contract_serpent",1,"svyazka_trav_yagi",3,n("pylayuschaya_cheshuya",2)),new Entry("contract_podvey",1,"svyazka_trav_yagi",3,n("vihrevaya_nit",2)),
  new Entry("contract_likho",3,"otvar_ochishcheniya",2,n("nit_durnoy_doli",2)),new Entry("contract_tugarin",3,"yagin_nastoy_stoykosti",2,n("tugarinova_kozha",2))};
 public static final Entry[] EXCHANGES={
  new Entry("herbs",1,"svyazka_trav_yagi",2,n("ovinnaya_zola",4)),new Entry("linen",1,"flax",4,n("klyk_volkolaka",2)),
  new Entry("cleansing",2,"otvar_ochishcheniya",1,n("nit_durnoy_doli",3)),new Entry("salve",2,"letuchaya_maz",1,n("ognennoe_pero",1)),
  new Entry("steadfast",3,"yagin_nastoy_stoykosti",1,n("tugarinova_kozha",3)),new Entry("restoring",3,"lesnoy_nastoy_vosstanovleniya",2,n("kost_likha",1))};
 public static final Entry[] BREWS={
  new Entry("otvar_ochishcheniya",2,"otvar_ochishcheniya",1,n("svyazka_trav_yagi",1),n("wormwood",2),n("st_johns_wort",2),n("minecraft:glass_bottle",1)),
  new Entry("letuchaya_maz",2,"letuchaya_maz",1,n("uzel_podveya",1),n("minecraft:honey_bottle",1),n("flax",2),n("minecraft:bowl",1)),
  new Entry("otvar_lesnoy_zorkosti",2,"otvar_lesnoy_zorkosti",1,n("fireweed",2),n("juniper_berries",2),n("minecraft:brown_mushroom",1),n("minecraft:glass_bottle",1)),
  new Entry("yagin_nastoy_stoykosti",3,"yagin_nastoy_stoykosti",1,n("nettle",2),n("ovinnaya_zola",1),n("minecraft:iron_nugget",2),n("minecraft:glass_bottle",1)),
  new Entry("otvar_bodrosti",2,"otvar_bodrosti",1,n("juniper_berries",2),n("minecraft:honey_bottle",1),n("nettle",2),n("minecraft:glass_bottle",1)),
  new Entry("lesnoy_nastoy_vosstanovleniya",3,"lesnoy_nastoy_vosstanovleniya",1,n("st_johns_wort",2),n("minecraft:glistering_melon_slice",1),n("fireweed",2),n("minecraft:glass_bottle",1))};
 public static final int[] POOL={0,1,2,3,0,1,2,3,4,0,1,2,3,5};
 public static int nextContract(int stage,int current){int next=current;do{next=(next+1)%POOL.length;}while(stage<3&&POOL[next]>=4);return next;}
 public static boolean has(Container inv,Need[] needs){Map<Item,Integer> total=new HashMap<>();for(Need n:needs)total.merge(n.item(),n.count,Integer::sum);for(Map.Entry<Item,Integer> e:total.entrySet()){int count=0;for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).getItem()==e.getKey())count+=inv.getItem(i).getCount();if(count<e.getValue())return false;}return true;}
 public static void consume(Container inv,Need[] needs){if(!has(inv,needs))throw new IllegalStateException("Unvalidated Yaga transaction");for(Need n:needs){int remaining=n.count;for(int i=0;i<inv.getContainerSize()&&remaining>0;i++){ItemStack s=inv.getItem(i);if(s.getItem()!=n.item())continue;int take=Math.min(remaining,s.getCount());s.shrink(take);remaining-=take;}}inv.setChanged();}
 public static boolean killed(ServerPlayer p,String id){net.minecraft.advancements.AdvancementHolder a=p.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));return a!=null&&p.getAdvancements().getOrStartProgress(a).isDone();}
 public static void give(ServerPlayer p,ItemStack reward){if(!reward.isEmpty()&&!p.getInventory().add(reward))p.drop(reward,false);}
 public static void award(ServerPlayer p,String id){org.slavicmyths.item.FolkAccessoryItem.award(p,id);}
 public static boolean fail(ServerPlayer p,String key){p.displayClientMessage(Component.translatable("yaga.fail."+key),true);return false;}
 private YagaServices(){}
}
