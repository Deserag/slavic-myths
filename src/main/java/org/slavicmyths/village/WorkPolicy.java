package org.slavicmyths.village;
import net.minecraft.core.registries.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.*;

/** Opt-in logistics, then a profession sell-output deny tag. Never copies MerchantOffer stock. */
public final class WorkPolicy {
    public static String role(Villager v){return BuiltInRegistries.VILLAGER_PROFESSION.getKey(v.getVillagerData().getProfession()).getPath();}
    public static TagKey<Item> tag(String group,String role){return TagKey.create(Registries.ITEM,VillageRoles.id("villager_"+group+"/"+role));}
    public static boolean input(Villager v,ItemStack s){return !s.isEmpty()&&s.is(tag("inputs",role(v)));}
    public static int wanted(Villager v,ItemStack s){if(!input(v,s))return 0;int have=v.getInventory().countItem(s.getItem());int limit=s.getMaxStackSize()==1?1:8;return Math.min(4,Math.max(0,limit-have));}
    private static final String HARVEST="SlavicMythsWorkHarvest";
    public static ItemStack harvest(ItemStack s){var data=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();data.putBoolean(HARVEST,true);s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));return s;}
    public static ItemStack clean(ItemStack s){var data=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();data.remove(HARVEST);if(data.isEmpty())s.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);else s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));return s;}
    public static boolean deposit(Villager v,ItemStack s){String role=role(v);boolean actualHarvest=(role.equals("shepherd")&&s.is(net.minecraft.tags.ItemTags.WOOL)||role.equals("fisherman")&&s.is(tag("outputs",role)))&&s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean(HARVEST);
        return !s.isEmpty()&&s.is(tag("outputs",role))&&(actualHarvest||!s.is(tag("trade_outputs",role)));}
    private WorkPolicy(){}
}
