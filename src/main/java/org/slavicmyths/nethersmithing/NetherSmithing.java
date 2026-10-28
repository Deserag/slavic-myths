package org.slavicmyths.nethersmithing;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.combat.*;

/** #326/#330 available material chain and six existing diamond-form upgrades only. */
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class NetherSmithing {
 public static final List<String> MATERIALS=List.of("zhara_plate","four_faces_crown_core","infernal_armor_heart","navya_armor_weave","blazing_step_seal","pekelnik_ember","navya_fork_core","ashen_blade_core","shadow_knife_core","volcanic_flail_core","infernal_bow_core");
 public static final List<String> FORMS=List.of("sword","axe","spear","sulitsa","throwing_knife","flail");
 static {
  ModItems.ITEMS.register("nether_hardening_template",()->new SmithingTemplateItem(Component.translatable("nethersmithing.applies"),Component.translatable("nethersmithing.ingredients"),Component.translatable("nethersmithing.upgrade"),Component.translatable("nethersmithing.base"),Component.translatable("nethersmithing.addition"),List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_sword")),List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"))));
  for(String id:MATERIALS)ModItems.ITEMS.register(id,()->new Item(new Item.Properties()){
   @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> text,TooltipFlag flag){text.add(Component.translatable("nethersmithing.material."+id));}
  });
  for(String form:FORMS)ModItems.ITEMS.register("zhara_"+form,()->switch(form){
   case "sword"->new SwordItem(Tiers.DIAMOND,new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND,3,-2.4F)));
   case "axe"->new AxeItem(Tiers.DIAMOND,new Item.Properties().attributes(AxeItem.createAttributes(Tiers.DIAMOND,5,-3)));
   case "spear"->new org.slavicmyths.item.SpearItem(Tiers.DIAMOND,new Item.Properties());
   case "sulitsa","throwing_knife"->new ThrowingWeaponItem(Tiers.DIAMOND,form.equals("throwing_knife"),new Item.Properties(),1.15F);
   default->new FlailItem(Tiers.DIAMOND,new Item.Properties(),1.15F);
  });
 }
 public static void init(){}
 public static boolean ordinary(ItemStack stack){var id=BuiltInRegistries.ITEM.getKey(stack.getItem());return id.getNamespace().equals("slavicmyths")&&id.getPath().startsWith("zhara_")&&FORMS.contains(id.getPath().substring(6));}
 public static Item base(String form){return form.equals("sword")?Items.DIAMOND_SWORD:form.equals("axe")?Items.DIAMOND_AXE:WeaponCatalog.item("diamond_"+form);}
 @SubscribeEvent public static void defaults(ModifyDefaultComponentsEvent event){
  for(String form:FORMS){Item source=base(form),target=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","zhara_"+form));var original=source.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
   var attributes=NetherSmithingRules.attributes(original);
   int durability=NetherSmithingRules.durability(source.components().getOrDefault(DataComponents.MAX_DAMAGE,1561));
   event.modify(target,patch->{patch.set(DataComponents.ATTRIBUTE_MODIFIERS,attributes);patch.set(DataComponents.MAX_DAMAGE,durability);});
  }
 }
 /** Vanilla smithing already transfers every component; only normalize wear, never repair. */
 public static void normalizeWear(ItemStack base,ItemStack result){NetherSmithingRules.normalizeWear(base,result);}
 private NetherSmithing(){}
}
