package org.slavicmyths.winters;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.crafting.*;
public final class WintersChecks {
 private static int checks;
 private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 private static void register(String key,Item item){Registry.register(BuiltInRegistries.ITEM,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",key),item);}
 private static CraftingInput grid(boolean copy,ItemStack original){
  return CraftingInput.of(3,3,new ArrayList<>(List.of(new ItemStack(Items.IRON_INGOT),new ItemStack(Items.LAPIS_LAZULI),new ItemStack(Items.IRON_INGOT),new ItemStack(org.slavicmyths.registry.ModItems.PERUNITE.get()),copy?original:new ItemStack(org.slavicmyths.morozko.FrostContent.HEART.get()),new ItemStack(org.slavicmyths.registry.ModItems.PERUNITE.get()),new ItemStack(Items.IRON_INGOT),new ItemStack(copy?Items.DIAMOND:org.slavicmyths.karachun.NightContent.SHARD.get()),new ItemStack(Items.IRON_INGOT))));
 }
 public static void main(String[] args)throws Exception{
  for(int t=0;t<=4000;t++){int expected=t<600?0:t<1200?1:t<1800?2:3;check(WintersRules.stage(t)==expected,"exact 30/60/90 second boundaries");}
  for(float reserve:new float[]{0,1,5,10})for(float damage:new float[]{0,1,4,10,11,25,10000}){float out=WintersRules.shieldDamage(damage,reserve),left=WintersRules.shieldLeft(damage,reserve);check(out>=0&&out<=damage&&left>=0&&left<=reserve,"bounded reserve");check(Math.abs((damage-out)-(reserve-left))<.001,"actual absorbed damage consumes reserve once");}
  float reserve=10,absorbed=0;for(int i=0;i<100;i++){float damage=1.2f;absorbed+=damage-WintersRules.shieldDamage(damage,reserve);reserve=WintersRules.shieldLeft(damage,reserve);}check(Math.abs(absorbed-10)<.001&&reserve==0,"100 hits cannot create infinite absorption");
  check(WintersRules.COOLDOWN==2400&&WintersRules.SHIELD_TICKS==160,"common two-minute deadline and eight-second shield");
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  // Isolated offline fixture only: Bootstrap freezes vanilla registries before a mod loader exists.
  // Reopen the item registry to bind four test fixtures; no production or world code uses this.
  var frozen=net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");frozen.setAccessible(true);frozen.setBoolean(BuiltInRegistries.ITEM,false);
  var intrusive=net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");intrusive.setAccessible(true);intrusive.set(BuiltInRegistries.ITEM,new IdentityHashMap<>());
  register("amulet_two_winters",new WintersItem());register("perunite",new Item(new Item.Properties()));register("heart_of_eternal_cold",new Item(new Item.Properties()));register("shard_of_last_night",new Item(new Item.Properties()));
  var lookup=RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
  var first=new WintersRecipe(CraftingBookCategory.EQUIPMENT);var copy=new WintersRecipe(CraftingBookCategory.MISC);var input=grid(false,ItemStack.EMPTY);
  check(WintersRecipe.shape(input,false),"actual rewards in exact first-recipe positions");check(!WintersRecipe.shape(input,true),"first and copying recipes disjoint");
  var original=first.assemble(input,lookup);check(WintersRecipe.original(original)&&original.getMaxStackSize()==1&&!original.isDamageableItem(),"one issued-form original without durability");
  var ledger=new WintersTemplates();UUID id=WintersRecipe.tag(original).getUUID("TwoWintersReceipt");check(!ledger.contains(id),"unissued fake flag/token rejected");ledger.issue(id);var restored=WintersTemplates.load(ledger.save(new CompoundTag(),lookup),lookup);check(restored.contains(id)&&!restored.contains(UUID.randomUUID()),"server receipt survives NBT roundtrip; unknown token rejected");
  original.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Personal winter"));
  var tag=WintersRecipe.tag(original);tag.putString("Rune","unchanged");tag.putLong("TwoWintersReady",987654321L);original.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
  for(int i=0;i<128;i++){
   var in=grid(true,original);check(WintersRecipe.shape(in,true),"original accepted for repeat paid craft");
   var result=copy.assemble(in,lookup);check(!WintersRecipe.original(result)&&!WintersRecipe.shape(grid(true,result),true),"copy never becomes template");
   check(WintersRecipe.tag(result).getLong("TwoWintersReady")==987654321L,"copying cannot bypass transferred artifact deadline");
   var remaining=copy.getRemainingItems(in);check(remaining.stream().filter(s->!s.isEmpty()).count()==1&&remaining.get(4).getCount()==1,"exactly one returned original in original slot");
   check(ItemStack.isSameItemSameComponents(original,remaining.get(4))&&remaining.get(4)!=original,"native remaining-items deep copy preserves all metadata");
   check(ItemStack.isSameItemSameComponents(original,in.getItem(4)),"preview/cancel does not mutate template");
  }
  for(int slot=0;slot<9;slot++){
   var list=new ArrayList<>(input.items());list.set(slot,ItemStack.EMPTY);check(!WintersRecipe.shape(CraftingInput.of(3,3,list),false),"no missing-material free first craft");
   list=new ArrayList<>(grid(true,original).items());list.set(slot,new ItemStack(Items.DIRT));check(!WintersRecipe.shape(CraftingInput.of(3,3,list),true),"no wrong-material copies");
  }
  var fake=new ItemStack(WintersContent.AMULET.get());var n=new CompoundTag();n.putBoolean("TwoWintersTemplate",true);fake.set(DataComponents.CUSTOM_DATA,CustomData.of(n));check(!WintersRecipe.original(fake),"bare fabricated flag rejected");
  n.putUUID("TwoWintersReceipt",UUID.randomUUID());n.putInt("TwoWintersTemplate",1);fake.set(DataComponents.CUSTOM_DATA,CustomData.of(n));check(!WintersRecipe.original(fake),"wrong flag type rejected");
  check(!first.canCraftInDimensions(2,2)&&!copy.canCraftInDimensions(3,2),"requires full 3x3 crafting table");
  System.out.println("WINTERS_OFFLINE_PASS checks="+checks+"; recipe/remaining-items/native metadata, receipt NBT, shield arithmetic and aura boundaries; no client/server launch or live inventory test");
 }
}
