package org.slavicmyths.flight;
import net.minecraft.enchantment.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.RegistryObject;
public final class Tailwind extends Enchantment {
 public static final DeferredRegister<Enchantment> ENCHANTMENTS=DeferredRegister.create(ForgeRegistries.ENCHANTMENTS,"slavicmyths");
 private static final EnchantmentType FLIGHT=EnchantmentType.create("slavic_flight",i->i instanceof FlightItem);
 public static final RegistryObject<Enchantment> TAILWIND=ENCHANTMENTS.register("tailwind",Tailwind::new);
 public Tailwind(){super(Rarity.RARE,FLIGHT,new EquipmentSlotType[]{EquipmentSlotType.MAINHAND});}
 @Override public int getMaxLevel(){return 3;}
 @Override public int getMinCost(int level){return 15+(level-1)*15;}
 @Override public int getMaxCost(int level){return getMinCost(level)+20;}
 @Override public boolean canEnchant(ItemStack s){return s.getItem() instanceof FlightItem;}
 @Override public boolean isTreasureOnly(){return true;}
}
