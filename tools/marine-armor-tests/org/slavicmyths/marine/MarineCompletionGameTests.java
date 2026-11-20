package org.slavicmyths.marine;

import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineCompletionGameTests {
    @GameTest(template = "empty", timeoutTicks = 60)
    public static void allTwelveRewardsAreRegistered(GameTestHelper t) {
        for (String id : List.of("storm_anchor", "abyss_staff", "storm_harpoon",
                "tide_ring", "abyss_necklace", "storm_bracelet", "tailwind_feather",
                "tsar_pike_belt", "zhelva_amulet",
                "marine_compass", "depths_lantern", "breathing_bowl")) {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths", id));
            t.assertTrue(item != Items.AIR, "Missing marine item: " + id);
        }
        t.assertTrue(MarineWeapons.ANCHOR.get() instanceof FishingRodItem,
            "Anchor must use the vanilla bobber");
        t.assertTrue(MarineWeapons.HARPOON.get() instanceof TridentItem,
            "Harpoon must preserve the vanilla thrown trident");
        var tridentTag = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            ResourceLocation.withDefaultNamespace("enchantable/trident"));
        var fishingTag = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            ResourceLocation.withDefaultNamespace("enchantable/fishing"));
        var durabilityTag = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            ResourceLocation.withDefaultNamespace("enchantable/durability"));
        t.assertTrue(new ItemStack(MarineWeapons.HARPOON.get()).is(tridentTag),
            "Loyalty / Riptide / Impaling / Channeling cannot enchant harpoon");
        t.assertTrue(new ItemStack(MarineWeapons.ANCHOR.get()).is(fishingTag),
            "Lure and Luck of the Sea cannot enchant the storm anchor");
        t.assertTrue(new ItemStack(MarineWeapons.HARPOON.get()).is(durabilityTag),
            "Mending and Unbreaking must accept storm harpoon");
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 70)
    public static void allSixCuriosOnlyWorkInTheirOwnSlots(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "OceanWorn523"));
        var curios = CuriosApi.getCuriosInventory(player).orElseThrow();
        java.util.Map<String, String> slots = java.util.Map.of(
            "tide_ring", "ring",
            "abyss_necklace", "necklace",
            "storm_bracelet", "charm",
            "tailwind_feather", "charm",
            "tsar_pike_belt", "belt",
            "zhelva_amulet", "charm"
        );
        for (var entry : slots.entrySet()) {
            String id = entry.getKey();
            var slot = curios.getCurios().get(entry.getValue());
            t.assertTrue(slot != null && slot.getStacks().getSlots() > 0,
                "Missing slot for " + id);
            var item = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("slavicmyths", id));
            var stack = new ItemStack(item);
            player.getInventory().add(stack.copy());
            t.assertTrue(!MarineAccessories.wears(player, id),
                "Inventory-only accessory was treated as worn: " + id);
            slot.getStacks().setStackInSlot(0, stack);
            t.assertTrue(MarineAccessories.wears(player, id),
                "Accessory not detected in equipped slot: " + id);
            slot.getStacks().setStackInSlot(0, ItemStack.EMPTY);
            t.assertTrue(!MarineAccessories.wears(player, id),
                "Accessory stayed equipped after removal: " + id);
        }
        player.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 70)
    public static void anchorUsesVanillaBobberWithRetrievalCooldown(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "StormAnchor522"));
        player.moveTo(t.absolutePos(new BlockPos(2, 3, 2)), 0F, 0F);
        player.setItemInHand(InteractionHand.MAIN_HAND,
            new ItemStack(MarineWeapons.ANCHOR.get()));
        t.assertTrue(player.fishing == null, "Unexpected existing bobber");
        player.getMainHandItem().use(t.getLevel(), player, InteractionHand.MAIN_HAND);
        t.assertTrue(player.fishing != null, "Anchor did not cast a vanilla fishing hook");
        player.getMainHandItem().use(t.getLevel(), player, InteractionHand.MAIN_HAND);
        t.assertTrue(player.getCooldowns().isOnCooldown(MarineWeapons.ANCHOR.get()),
            "Anchor retrieval did not trigger the four-second cooldown");
        if (player.fishing != null) player.fishing.discard();
        player.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void abyssStaffPulseAndCooldown(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "AbyssStaff522"));
        player.moveTo(t.absolutePos(new BlockPos(2, 2, 2)), 0F, 0F);
        player.setItemInHand(InteractionHand.MAIN_HAND,
            new ItemStack(MarineWeapons.STAFF.get()));
        var zombie = EntityType.ZOMBIE.create(t.getLevel());
        t.assertTrue(zombie != null, "Target unavailable");
        zombie.moveTo(t.absolutePos(new BlockPos(2, 2, 4)), 0F, 0F);
        t.getLevel().addFreshEntity(zombie);
        float health = zombie.getHealth();
        player.getMainHandItem().use(t.getLevel(), player, InteractionHand.MAIN_HAND);
        t.assertTrue(zombie.getHealth() < health, "Abyss pulse did not damage the target");
        t.assertTrue(player.getCooldowns().isOnCooldown(MarineWeapons.STAFF.get()),
            "Staff cooldown missing");
        t.assertTrue(player.getMainHandItem().getDamageValue() == 1,
            "Staff durability was not spent");
        zombie.discard();
        player.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void accessoriesRequireCuriosEquipment(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "OceanCurios523"));
        player.getInventory().add(new ItemStack(MarineAccessories.ZHELVA_AMULET.get()));
        var armor = player.getAttribute(Attributes.ARMOR);
        var key = ResourceLocation.fromNamespaceAndPath("slavicmyths", "zhelva_amulet_armor");
        t.assertTrue(armor != null, "No armor attribute");
        MarineAccessories.apply(player);
        t.assertTrue(armor.getModifier(key) == null,
            "Inventory-only amulet illegally gave armor");
        var inv = CuriosApi.getCuriosInventory(player).orElseThrow();
        var charm = inv.getCurios().get("charm");
        t.assertTrue(charm != null && charm.getStacks().getSlots() > 0,
            "Missing Curios charm slot");
        charm.getStacks().setStackInSlot(0,
            new ItemStack(MarineAccessories.ZHELVA_AMULET.get()));
        MarineAccessories.apply(player);
        t.assertTrue(armor.getModifier(key) != null
                && armor.getModifier(key).amount() == 2D,
            "Equipped amulet must grant precisely two armor points");
        charm.getStacks().setStackInSlot(0, ItemStack.EMPTY);
        MarineAccessories.apply(player);
        t.assertTrue(armor.getModifier(key) == null, "Unequipped amulet stayed active");
        player.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void bowlBreathingConsumesDurabilityAndCooldown(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "OceanBowl523"));
        player.setItemInHand(InteractionHand.MAIN_HAND,
            new ItemStack(MarineArtifacts.BOWL.get()));
        player.getMainHandItem().use(t.getLevel(), player, InteractionHand.MAIN_HAND);
        t.assertTrue(player.hasEffect(MobEffects.WATER_BREATHING),
            "Bowl did not grant water breathing");
        t.assertTrue(player.getMainHandItem().getDamageValue() == 1,
            "Bowl was not consumed");
        t.assertTrue(player.getCooldowns().isOnCooldown(MarineArtifacts.BOWL.get()),
            "Bowl cooldown missing");
        player.getMainHandItem().use(t.getLevel(), player, InteractionHand.MAIN_HAND);
        t.assertTrue(player.getMainHandItem().getDamageValue() == 1,
            "Bowl cooldown did not reject a second activation");
        player.discard();
        t.succeed();
    }
}
