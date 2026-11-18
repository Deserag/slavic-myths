package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.combat.*;
import org.slavicmyths.nethersmithing.NetherSmithingRules;
public final class Nether337SmithingOffline {
 static double damage(ItemAttributeModifiers a){double raw=1;for(var e:a.modifiers())if(e.attribute().equals(Attributes.ATTACK_DAMAGE))raw+=e.modifier().amount();return raw;}
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args){
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  List<ItemAttributeModifiers> sources=List.of(Items.DIAMOND_SWORD.components().get(DataComponents.ATTRIBUTE_MODIFIERS),Items.DIAMOND_AXE.components().get(DataComponents.ATTRIBUTE_MODIFIERS),org.slavicmyths.item.SpearItem.attributes(Tiers.DIAMOND,4+Tiers.DIAMOND.getAttackDamageBonus()),ThrowingWeaponItem.attributes(Tiers.DIAMOND,false),ThrowingWeaponItem.attributes(Tiers.DIAMOND,true),org.slavicmyths.combat.MaceItem.attributes(6+Tiers.DIAMOND.getAttackDamageBonus(),.7F));
  for(var a:sources){var upgraded=NetherSmithingRules.attributes(a);check(Math.abs(damage(upgraded)-damage(a)*1.15)<.00001,"direct scaling");for(var e:a.modifiers())if(!e.attribute().equals(Attributes.ATTACK_DAMAGE))check(upgraded.modifiers().contains(e),"speed/reach changed");check(NetherSmithingRules.durability(Tiers.DIAMOND.getUses())==1874,"durability scaling");}
  check(Math.abs(ThrowingWeaponItem.thrownDamage(Tiers.DIAMOND,true,1.15F)-ThrowingWeaponItem.thrownDamage(Tiers.DIAMOND,true,1)*1.15)<.00001,"knife throw multiplier");
  check(Math.abs(FlailItem.chargedDamage(Tiers.DIAMOND,1.15F)-FlailItem.chargedDamage(Tiers.DIAMOND,1)*1.15)<.00001,"charged flail multiplier");
  ItemStack base=new ItemStack(Items.DIAMOND_SWORD);base.setDamageValue(780);base.set(DataComponents.CUSTOM_NAME,Component.literal("Preserved name"));CompoundTag tag=new CompoundTag();tag.putString("UnrelatedCustomState","retained");base.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
  ItemStack result=base.transmuteCopy(Items.NETHERITE_SWORD,1);NetherSmithingRules.normalizeWear(base,result);check(result.getDamageValue()==(int)Math.ceil(780./1561*2031),"wear ratio");check(result.get(DataComponents.CUSTOM_NAME).equals(base.get(DataComponents.CUSTOM_NAME)),"custom name lost");check(result.get(DataComponents.CUSTOM_DATA).equals(base.get(DataComponents.CUSTOM_DATA)),"custom data lost");
  check(base.getDamageValue()==780,"preview mutated input");
  System.out.println("PASS six source-form native attribute transformations +15% direct/+20% durability; knife projectile/flail charge; smithing wear and arbitrary components. Actual new item registry/default component event/mixin/UI/runes/MP NOT_RUN.");
 }
}
