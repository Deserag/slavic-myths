package org.slavicmyths.item;
import net.minecraft.item.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.entity.Entity;
import java.util.List;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
public final class SevenLeagueBoots extends ArmorItem {
 public SevenLeagueBoots(Properties p){super(ArmorMaterial.LEATHER,EquipmentSlotType.FEET,p);}
 @Override public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlotType slot,String type){return "slavicmyths:textures/armor/seven_league.png";}
 @Override public void appendHoverText(ItemStack stack,World world,List<ITextComponent> lines,ITooltipFlag flag){lines.add(new TranslationTextComponent(getDescriptionId()+".effect").withStyle(TextFormatting.GRAY));}
}
