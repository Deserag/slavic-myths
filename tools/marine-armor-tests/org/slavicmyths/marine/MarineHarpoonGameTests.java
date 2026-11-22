package org.slavicmyths.marine;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Exercise the real vanilla trident release path, not just TridentItem inheritance. */
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineHarpoonGameTests {
    @GameTest(template = "empty", timeoutTicks = 75)
    public static void throwingCreatesVanillaTridentWithOriginalItem(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "StormHarpoon522"));
        player.moveTo(t.absolutePos(new BlockPos(2, 3, 2)), 0F, 0F);
        var harpoon = new ItemStack(MarineHarpoons.AMMO.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, harpoon);
        int useTicks = 24;
        MarineHarpoons.AMMO.get().releaseUsing(harpoon, t.getLevel(),
            player, harpoon.getUseDuration(player) - useTicks);
        var thrown = t.getLevel().getEntitiesOfClass(ThrownTrident.class,
            player.getBoundingBox().inflate(5D));
        t.assertTrue(thrown.size() == 1,
            "Harpoon release did not create one vanilla ThrownTrident");
        var projectile = thrown.getFirst();
        t.assertTrue(projectile.getOwner() == player,
            "Harpoon projectile lost its owner");
        t.assertTrue(projectile.getWeaponItem() != null
            && projectile.getWeaponItem().is(MarineHarpoons.AMMO.get()),
            "Thrown trident lost the original harpoon ItemStack");
        projectile.discard();
        player.discard();
        t.succeed();
    }
}
