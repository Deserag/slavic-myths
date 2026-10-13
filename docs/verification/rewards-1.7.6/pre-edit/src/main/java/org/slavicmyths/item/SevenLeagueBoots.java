package org.slavicmyths.item;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import java.util.List;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
public final class SevenLeagueBoots extends ArmorItem {
 public SevenLeagueBoots(Properties p){super(ArmorMaterials.LEATHER,ArmorItem.Type.BOOTS,p.durability(65));}
 @Override public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean innerModel){return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/armor/seven_league.png");}
 @Override public void appendHoverText(ItemStack stack,Item.TooltipContext world,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));}
}
