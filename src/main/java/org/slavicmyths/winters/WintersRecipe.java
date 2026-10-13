package org.slavicmyths.winters;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.*;
/** Exact, unmirrored 3x3 recipes; vanilla ResultSlot owns all ingredient transactions. */
public final class WintersRecipe extends CustomRecipe {
 public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,"slavicmyths");
 public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<WintersRecipe>> SERIALIZER=SERIALIZERS.register("two_winters",()->new SimpleCraftingRecipeSerializer<>(WintersRecipe::new));
 public WintersRecipe(CraftingBookCategory c){super(c);}
 public static CompoundTag tag(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
 public static boolean original(ItemStack s){var n=tag(s);return s.is(WintersContent.AMULET.get())&&n.contains("TwoWintersTemplate",Tag.TAG_BYTE)&&n.getByte("TwoWintersTemplate")==1&&n.hasUUID("TwoWintersReceipt");}
 public static boolean shape(CraftingInput in,boolean copy){
  if(in.width()!=3||in.height()!=3||in.ingredientCount()!=9)return false;
  Item[] expected={Items.IRON_INGOT,Items.LAPIS_LAZULI,Items.IRON_INGOT,org.slavicmyths.registry.ModItems.PERUNITE.get(),copy?WintersContent.AMULET.get():org.slavicmyths.morozko.FrostContent.HEART.get(),org.slavicmyths.registry.ModItems.PERUNITE.get(),Items.IRON_INGOT,copy?Items.DIAMOND:org.slavicmyths.karachun.NightContent.SHARD.get(),Items.IRON_INGOT};
  for(int i=0;i<9;i++)if(!in.getItem(i).is(expected[i]))return false;
  return !copy||original(in.getItem(4));
 }
 private boolean copying(){return category()==CraftingBookCategory.MISC;}
 @Override public boolean matches(CraftingInput in,Level w){if(!shape(in,copying()))return false;return !copying()||w.isClientSide||w instanceof ServerLevel s&&WintersTemplates.get(s).contains(tag(in.getItem(4)).getUUID("TwoWintersReceipt"));}
 @Override public ItemStack assemble(CraftingInput in,HolderLookup.Provider p){
  if(!shape(in,copying()))return ItemStack.EMPTY;
  var out=new ItemStack(WintersContent.AMULET.get());var n=new CompoundTag();n.putBoolean("TwoWintersTemplate",!copying());
  if(!copying())n.putUUID("TwoWintersReceipt",UUID.randomUUID());else n.putLong("TwoWintersReady",tag(in.getItem(4)).getLong("TwoWintersReady"));
  out.set(DataComponents.CUSTOM_DATA,CustomData.of(n));return out;
 }
 @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput in){var out=NonNullList.withSize(in.size(),ItemStack.EMPTY);if(copying()&&shape(in,true))out.set(4,in.getItem(4).copyWithCount(1));return out;}
 @Override public boolean canCraftInDimensions(int w,int h){return w==3&&h==3;}
 @Override public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}
}
