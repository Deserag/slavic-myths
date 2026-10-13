package org.slavicmyths.winters;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;
/** One artifact; all player state belongs to the existing persisted PathData. */
public final class WintersContent {
 public static final DeferredHolder<Item,Item> AMULET=org.slavicmyths.registry.ModItems.ITEMS.register("amulet_two_winters",WintersItem::new);
 public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"slavicmyths");
 public static final DeferredHolder<MobEffect,MobEffect> AURA=EFFECTS.register("nether_aura",()->new Aura());
 private static final class Aura extends MobEffect {
  Aura(){super(MobEffectCategory.HARMFUL,0x9e1b28);addAttributeModifier(Attributes.ATTACK_DAMAGE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","nether_aura"),-4,AttributeModifier.Operation.ADD_VALUE);}
 }
 public static void init(IEventBus bus){AMULET.getId();EFFECTS.register(bus);WintersRecipe.SERIALIZERS.register(bus);}
 private WintersContent(){}
}
