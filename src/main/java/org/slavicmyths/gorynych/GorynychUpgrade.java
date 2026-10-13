package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.*;
public final class GorynychUpgrade extends CustomRecipe {
 public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,"slavicmyths");
 public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<GorynychUpgrade>> SERIALIZER=SERIALIZERS.register("gorynych_upgrade",()->new SimpleCraftingRecipeSerializer<>(GorynychUpgrade::new));
 public GorynychUpgrade(CraftingBookCategory category){super(category);}
 public static Item target(ItemStack base){if(base.is(GorynychContent.item("serpent_crossbow")))return GorynychContent.item("three_breath_crossbow");for(var slot:GorynychContent.ARMOR.keySet()){Item source=switch(slot){case HEAD->org.slavicmyths.registry.ModItems.PERUNITE_HELMET.get();case CHEST->org.slavicmyths.registry.ModItems.PERUNITE_CHESTPLATE.get();case LEGS->org.slavicmyths.registry.ModItems.PERUNITE_LEGGINGS.get();default->org.slavicmyths.registry.ModItems.PERUNITE_BOOTS.get();};if(base.is(source))return GorynychContent.ARMOR.get(slot).get();}return null;}
 private static ItemStack base(CraftingInput input){ItemStack found=ItemStack.EMPTY;for(var s:input.items())if(!s.isEmpty()&&target(s)!=null){if(!found.isEmpty())return ItemStack.EMPTY;found=s;}return found;}
 private static boolean ward(CraftingInput input){if(input.width()<3||input.ingredientCount()!=3)return false;Set<Item> expected=new HashSet<>(List.of(org.slavicmyths.guardians.GuardianContent.FIRE_FRAGMENT.get(),org.slavicmyths.guardians.GuardianContent.WATER_FRAGMENT.get(),org.slavicmyths.poloz.PolozContent.EYE.get()));for(var stack:input.items())if(!stack.isEmpty()&&!expected.remove(stack.getItem()))return false;return expected.isEmpty();}
 @Override public boolean matches(CraftingInput input,Level level){if(ward(input))return category()==CraftingBookCategory.MISC;if(category()!=CraftingBookCategory.EQUIPMENT)return false;var base=base(input);if(base.isEmpty()||input.width()<3||input.height()<2||input.size()!=6&&input.size()!=9||input.ingredientCount()!=6)return false;Map<Item,Integer> required=new HashMap<>();required.put(base.getItem(),1);required.put(GorynychContent.item("gorynych_scale"),1);if(base.getItem() instanceof CrossbowItem){required.put(GorynychContent.item("ancient_serpent_steel"),1);for(int type=0;type<3;type++)required.put(GorynychContent.bottle(type),1);}else{required.put(org.slavicmyths.registry.ModItems.PERUNITE.get(),2);required.put(Items.DIAMOND,2);}for(var s:input.items())if(!s.isEmpty()){int remain=required.getOrDefault(s.getItem(),0);if(remain<=0)return false;required.put(s.getItem(),remain-1);}return required.values().stream().allMatch(v->v==0);}
 @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider lookup){if(ward(input)){if(category()!=CraftingBookCategory.MISC)return ItemStack.EMPTY;var result=new ItemStack(org.slavicmyths.quest185.Quest185Content.WARD.get());var tag=new net.minecraft.nbt.CompoundTag();tag.putBoolean("GuardianWard196",true);result.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));return result;}if(category()!=CraftingBookCategory.EQUIPMENT)return ItemStack.EMPTY;var base=base(input);var target=target(base);if(target==null)return ItemStack.EMPTY;return copy(base,target);}
 public static ItemStack copy(ItemStack base,Item target){var result=base.transmuteCopy(target,1);if(base.isDamageableItem())result.setDamageValue((int)((double)base.getDamageValue()/base.getMaxDamage()*result.getMaxDamage()));return result;}
 @Override public boolean canCraftInDimensions(int width,int height){return width>=3&&height>=(category()==CraftingBookCategory.MISC?1:2);}
 @Override public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}
}
