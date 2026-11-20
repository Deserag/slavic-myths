package org.slavicmyths.marine;

import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.rpg.Runes;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineLegendaryGameTests {
    @GameTest(template = "empty", timeoutTicks = 60)
    public static void tridentRuneStackRoundTrip(GameTestHelper t) {
        ItemStack weapon = new ItemStack(PharaohkaTrident.ITEM.get());
        t.assertTrue(Runes.capacity(weapon) == 3, "Trident must have exactly three sockets");
        Runes.write(weapon, 3, List.of("tide", "abyss", "storm"));
        ItemStack copy = weapon.copy();
        var saved = (net.minecraft.nbt.CompoundTag) copy.save(t.getLevel().registryAccess());
        var loaded = ItemStack.parseOptional(t.getLevel().registryAccess(), saved);
        t.assertTrue(loaded.is(PharaohkaTrident.ITEM.get()), "Wrong item after save");
        t.assertTrue(Runes.list(loaded).equals(List.of("tide", "abyss", "storm")),
            "Runes lost on save/load");
        t.assertTrue(loaded.getMaxDamage() == 900, "Trident durability changed");
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void tidalWaveIsBoundedAndHasCooldown(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "Pharaohka521"));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PharaohkaTrident.ITEM.get()));
        player.moveTo(t.absolutePos(new BlockPos(2, 2, 2)), 0F, 0F);
        var target = EntityType.ZOMBIE.create(t.getLevel());
        t.assertTrue(target != null, "Missing test target");
        target.moveTo(t.absolutePos(new BlockPos(2, 2, 3)), 0F, 0F);
        t.getLevel().addFreshEntity(target);
        float initial = target.getHealth();
        t.assertTrue(PharaohkaTrident.wave(player, target.position(), null),
            "First wave failed");
        t.assertTrue(target.getHealth() < initial, "Wave did not inflict damage");
        t.assertTrue(!PharaohkaTrident.wave(player, target.position(), null),
            "Cooldown did not reject second wave");
        target.discard();
        player.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void shieldAttributes(GameTestHelper t) {
        var shield = new ItemStack(ZhelvaShield.ITEM.get());
        t.assertTrue(shield.getMaxDamage() == 1200, "Shield durability changed");
        t.assertTrue(shield.getItem() instanceof net.minecraft.world.item.ShieldItem,
            "Shield no longer uses vanilla blocking");
        t.assertTrue(PharaohkaTrident.COOLDOWN == 12 * 20, "Tidal Rift cooldown changed");
        t.assertTrue(ZhelvaShield.DURATION < ZhelvaShield.COOLDOWN,
            "Shell Guard would have permanent uptime");
        t.succeed();
    }
}
