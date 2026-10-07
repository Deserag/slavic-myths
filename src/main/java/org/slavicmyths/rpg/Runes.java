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
 public static int cost(net.minecraft.world.entity.player.Player p,int base){return p.isCreative()?0:PathData.has(p,"rune_knowledge")?Math.max(1,(int)Math.ceil(base*.85)):base;}
 public static String rune(ItemStack stack){var key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());String id=key.getNamespace().equals("slavicmyths")&&key.getPath().startsWith("rune_")?key.getPath().substring(5):"";return Arrays.asList(IDS).contains(id)?id:"";}
 public static Item forgeMaterial(ItemStack weapon){return slots(weapon)==0?Items.IRON_INGOT:slots(weapon)==1?ModItems.SILVER_INGOT.get():ModItems.PERUNITE.get();}
 public static int forgeCount(ItemStack weapon){return slots(weapon)==1?2:3;}
 public static Item extra(ItemStack weapon){return slots(weapon)==0?null:slots(weapon)==1?ModItems.ANCIENT_SIGN.get():ModItems.OVINNIK_CLAW.get();}
 public static int inventoryCount(net.minecraft.world.entity.player.Player p,Item item){int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(item))n+=p.getInventory().getItem(i).getCount();return n;}
 public static int operationCost(net.minecraft.world.entity.player.Player p,ItemStack weapon,int action){return cost(p,action==0?new int[]{6,15,28}[Math.min(2,slots(weapon))]:action==1?7:2);}
 public static net.minecraft.network.chat.Component blocker(net.minecraft.world.entity.player.Player p,Container inv,int action){
  ItemStack weapon=inv.getItem(0),material=inv.getItem(1);int sockets=slots(weapon);var installed=list(weapon);
  if(weapon.getCount()!=1||max(weapon)==0)return text("unsupported");
  if(action==0){if(sockets>=max(weapon))return text("capacity");if(!material.is(forgeMaterial(weapon))||material.getCount()<forgeCount(weapon))return text("missing_material",forgeMaterial(weapon).getDescription(),forgeCount(weapon));if(extra(weapon)!=null&&inventoryCount(p,extra(weapon))<1)return text("missing_material",extra(weapon).getDescription(),1);}
  else if(action==1){String id=rune(material);if(id.isEmpty())return text("need_rune");if(!RuneDefinition.ALL.get(id).categories().contains(category(weapon)))return text("incompatible_rune").copy().withStyle(net.minecraft.ChatFormatting.GRAY);if(installed.contains(id))return text("duplicate_rune");if(installed.size()>=sockets)return text("socket_full");}
  else if(action<2||action>4||action-2>=installed.size())return text("no_rune");
  int xp=operationCost(p,weapon,action);if(!p.isCreative()&&p.experienceLevel<xp)return text("need_xp",xp);return null;
 }
 private static net.minecraft.network.chat.Component text(String key,Object... args){return net.minecraft.network.chat.Component.translatable("rpg.slavicmyths."+key,args);}
 /** A copy for presentation only. Never inserted into a menu slot or player inventory. */
 public static ItemStack preview(Container inv,int action){ItemStack result=inv.getItem(0).copy();if(max(result)==0)return ItemStack.EMPTY;int sockets=slots(result);var installed=list(result);
  if(action==0){if(sockets>=max(result))return ItemStack.EMPTY;sockets++;}
  else if(action==1){String id=rune(inv.getItem(1));if(id.isEmpty()||installed.contains(id)||installed.size()>=sockets)return ItemStack.EMPTY;installed.add(id);}
  else {int i=action-2;if(i<0||i>=installed.size())return ItemStack.EMPTY;installed.remove(i);}
  write(result,sockets,installed);return result;
 }
 public static void operation(ServerPlayer p,Container inv,int action){
  var reason=blocker(p,inv,action);if(reason!=null){p.displayClientMessage(reason,true);return;}
  ItemStack weapon=inv.getItem(0),material=inv.getItem(1),result=preview(inv,action);if(result.isEmpty()||!PathData.pay(p,operationCost(p,weapon,action)))return;
  if(action==0){Item extra=extra(weapon);material.shrink(forgeCount(weapon));if(extra!=null){for(int i=0;i<p.getInventory().getContainerSize();i++){ItemStack stack=p.getInventory().getItem(i);if(stack.is(extra)){stack.shrink(1);p.getInventory().setChanged();break;}}}if(slots(result)>=2)Knowledge.award(p,"reforged");}
  else if(action==1){material.shrink(1);Knowledge.award(p,"rune_installed");}
  write(weapon,slots(result),list(result));if(list(result).size()==3)Knowledge.award(p,"three_signs");inv.setChanged();PathData.message(p,"operation_done");
 }
 private Runes(){}
}
