package org.slavicmyths.rpg;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.progression.Knowledge;
public final class Runes {
 public static final String[] IDS={"thunder","heat","forest","midday","shadow","protection","life","wind"};
 private static final java.util.Map<String,net.minecraft.tags.TagKey<Item>> TAGS=new java.util.HashMap<>();
 static {for(String key:new String[]{"rune_staff","rune_dagger","rune_spear","rune_heavy","rune_capacity_1","rune_capacity_3","no_reforging"})TAGS.put(key,ItemTags.create(ResourceLocation.fromNamespaceAndPath("slavicmyths",key)));}
 public static boolean tagged(ItemStack s,String tag){return s.is(TAGS.get(tag));}
 public static void init(){}
 public static boolean heavy(ItemStack s){return tagged(s,"rune_heavy")||s.getItem() instanceof AxeItem;}
 public static String category(ItemStack s){
  if(s.isEmpty())return "";
  for(String c:new String[]{"staff","dagger","spear","heavy"})if(tagged(s,"rune_"+c))return c;
  Item i=s.getItem();if(i instanceof BowItem)return "bow";if(i instanceof ShieldItem)return "shield";if(i instanceof AxeItem)return "heavy";if(i instanceof SwordItem)return "sword";return "";
 }
 public static int max(ItemStack s){if(category(s).isEmpty()||tagged(s,"no_reforging"))return 0;if(tagged(s,"rune_capacity_3"))return 3;if(tagged(s,"rune_capacity_1"))return 1;
  if(s.getItem() instanceof TieredItem){Tier t=((TieredItem)s.getItem()).getTier();if(t==Tiers.WOOD||t==Tiers.STONE)return 1;if(t==Tiers.DIAMOND||t==Tiers.NETHERITE)return 3;}return 2;}
 public static int slots(ItemStack s){return Math.max(0,Math.min(max(s),org.slavicmyths.item.ItemState.runes(s).slots()));}
 public static List<String> list(ItemStack s){List<String> out=new ArrayList<>();for(String id:org.slavicmyths.item.ItemState.runes(s).runes())if(out.size()<slots(s)&&Arrays.asList(IDS).contains(id)&&!out.contains(id))out.add(id);return out;}
 public static boolean has(ItemStack s,String rune){return list(s).contains(rune);}
 public static void write(ItemStack s,int slots,List<String> runes){org.slavicmyths.item.ItemState.runes(s,slots,runes);}
 public static int cost(net.minecraft.world.entity.player.Player p,int base){return PathData.has(p,"rune_knowledge")?Math.max(1,(int)Math.ceil(base*.85)):base;}
 public static void operation(ServerPlayer p,Container inv,int action){
  ItemStack weapon=inv.getItem(0),material=inv.getItem(1),catalyst=inv.getItem(2);int slots=slots(weapon);List<String> runes=list(weapon);
  if(weapon.getCount()!=1||max(weapon)==0){PathData.message(p,"unsupported");return;}
  int xp;Item required=null,extra=null;int count=1;
  if(action==0){
   if(slots>=max(weapon)){PathData.message(p,"capacity");return;}
   required=slots==0?Items.IRON_INGOT:slots==1?ModItems.SILVER_INGOT.get():ModItems.PERUNITE.get();count=slots==0?3:slots==1?2:3;
   extra=slots==0?null:slots==1?ModItems.ANCIENT_SIGN.get():ModItems.OVINNIK_CLAW.get();xp=cost(p,new int[]{6,15,28}[slots]);
   if(material.getItem()!=required||material.getCount()<count||extra!=null&&catalyst.getItem()!=extra){PathData.message(p,"forge_materials_"+slots);return;}
  }else if(action==1){
   String rune=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(material.getItem()).toString();if(!rune.startsWith("slavicmyths:rune_")){PathData.message(p,"need_rune");return;}rune=rune.substring("slavicmyths:rune_".length());
   if(!Arrays.asList(IDS).contains(rune)||runes.contains(rune)||runes.size()>=slots){PathData.message(p,"socket_full");return;}
   xp=cost(p,7);runes.add(rune);
  }else {int index=action-2;if(index<0||index>=runes.size()){PathData.message(p,"no_rune");return;}xp=cost(p,2);runes.remove(index);}
  if(!PathData.pay(p,xp))return;
  if(action==0){material.shrink(count);if(extra!=null)catalyst.shrink(1);slots++;if(slots>=2)Knowledge.award(p,"reforged");}
  else if(action==1){material.shrink(1);Knowledge.award(p,"rune_installed");}
  write(weapon,slots,runes);if(runes.size()==3)Knowledge.award(p,"three_signs");inv.setChanged();PathData.message(p,"operation_done");
 }
 private Runes(){}
}
