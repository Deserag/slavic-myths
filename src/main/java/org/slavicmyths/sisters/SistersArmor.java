package org.slavicmyths.sisters;

import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.item.ModGear;
import org.slavicmyths.registry.ModItems;

/** An upgrade path for the four level-7 sisters, reusing existing equipment bases. */
public final class SistersArmor {
    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> MATERIAL = ModGear.ARMOR_MATERIALS.register("sisters_25", () -> new ArmorMaterial(
        Map.of(ArmorItem.Type.HELMET,5,ArmorItem.Type.CHESTPLATE,10,ArmorItem.Type.LEGGINGS,7,ArmorItem.Type.BOOTS,5),
        20,SoundEvents.ARMOR_EQUIP_NETHERITE,()->Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","alatyr_ingot"))),
        List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_25"))),2F,.10F));

    public static final DeferredHolder<Item,Item> TRYASEYA_BOOTS = ModItems.ITEMS.register("tryaseya_boots", () -> new Piece(ArmorItem.Type.BOOTS,"tryaseya_boots",850,4));
    public static final DeferredHolder<Item,Item> GNETEYA_CHESTPLATE = ModItems.ITEMS.register("gneteya_chestplate", () -> new Piece(ArmorItem.Type.CHESTPLATE,"gneteya_chestplate",1100,8));
    public static final DeferredHolder<Item,Item> LEDEYA_HELMET = ModItems.ITEMS.register("ledeya_helmet", () -> new Piece(ArmorItem.Type.HELMET,"ledeya_helmet",820,4));
    public static final DeferredHolder<Item,Item> AGNEYA_LEGGINGS = ModItems.ITEMS.register("agneya_leggings", () -> new Piece(ArmorItem.Type.LEGGINGS,"agneya_leggings",1000,4));

    public static void init() {}

    public static boolean wears(LivingEntity entity, EquipmentSlot slot) {
        ItemStack stack=entity.getItemBySlot(slot);
        return stack.getItem() instanceof Piece;
    }

    public static boolean full(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).is(TRYASEYA_BOOTS.get())
            && entity.getItemBySlot(EquipmentSlot.CHEST).is(GNETEYA_CHESTPLATE.get())
            && entity.getItemBySlot(EquipmentSlot.HEAD).is(LEDEYA_HELMET.get())
            && entity.getItemBySlot(EquipmentSlot.LEGS).is(AGNEYA_LEGGINGS.get());
    }

    public static final class Piece extends ArmorItem {
        private final String id;
        private final int bonusHealth;
        private Piece(Type type,String id,int durability,int bonusHealth) {
            super(MATERIAL,type,new Item.Properties().durability(durability).rarity(Rarity.EPIC).fireResistant());
            this.id=id;this.bonusHealth=bonusHealth;
        }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(switch(id) {
                case "tryaseya_boots"->"Сапоги Трясеи";
                case "gneteya_chestplate"->"Нагрудник Гнетеи";
                case "ledeya_helmet"->"Шлем Ледеи";
                default->"Поножи Агнеи";
            });
        }
        @Override public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
            ItemAttributeModifiers mods=super.getDefaultAttributeModifiers(stack);
            EquipmentSlotGroup group=EquipmentSlotGroup.bySlot(getEquipmentSlot());
            mods=mods.withModifierAdded(Attributes.MAX_HEALTH,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths",id+"_hp"),bonusHealth,AttributeModifier.Operation.ADD_VALUE),group);
            if(getType()==Type.BOOTS) {
                mods=mods.withModifierAdded(Attributes.MOVEMENT_SPEED,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","wind_boots_speed"),org.slavicmyths.equipment.UniqueEquipmentRules.SPEED,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),group);
                mods=mods.withModifierAdded(Attributes.JUMP_STRENGTH,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","wind_boots_jump"),org.slavicmyths.equipment.UniqueEquipmentRules.JUMP,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),group);
            }
            return mods;
        }
        @Override public ResourceLocation getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner) {
            return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/armor/sisters/"+id+".png");
        }
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flags) {
            super.appendHoverText(stack,context,lines,flags);
            lines.add(Component.literal("✦ НАСЛЕДИЕ СЕСТЁР").withStyle(ChatFormatting.DARK_PURPLE));
            switch(id) {
                case "tryaseya_boots" -> lines.add(Component.literal("Ветер: +40% скорости и усиленный прыжок").withStyle(ChatFormatting.AQUA));
                case "gneteya_chestplate" -> lines.add(Component.literal("Очищение: 30% шанс предотвратить негативный эффект").withStyle(ChatFormatting.AQUA));
                case "ledeya_helmet" -> lines.add(Component.literal("Точный удар: 25% шанс увеличить урон в 1,5 раза").withStyle(ChatFormatting.AQUA));
                case "agneya_leggings" -> lines.add(Component.literal("Сытость: +1 ед. голода каждые 1,75 сек. при необходимости").withStyle(ChatFormatting.AQUA));
                default -> {}
            }
            lines.add(Component.literal("Комплект 4/4: +10 сердец • полёт • Скорость III").withStyle(ChatFormatting.LIGHT_PURPLE));
            if(getType()==Type.CHESTPLATE)
                lines.add(Component.literal("Аура: Shift+ПКМ пустой рукой • 20 сек. / 60 сек.").withStyle(ChatFormatting.GRAY));
        }
    }
    private SistersArmor() {}
}
