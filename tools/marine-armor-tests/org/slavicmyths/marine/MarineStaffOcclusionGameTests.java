package org.slavicmyths.marine;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Real server-side line-of-sight regression. */
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineStaffOcclusionGameTests {
    @GameTest(template = "empty", timeoutTicks = 60)
    public static void abyssPulseCannotHitThroughSolidBlocks(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "OceanOcclusion"));
        player.moveTo(t.absolutePos(new BlockPos(2, 2, 2)), 0F, 0F);
        player.setItemInHand(InteractionHand.MAIN_HAND,
            new ItemStack(MarineWeapons.STAFF.get()));
        var target = EntityType.ZOMBIE.create(t.getLevel());
        t.assertTrue(target != null, "Missing target");
        target.moveTo(t.absolutePos(new BlockPos(2, 2, 4)), 0F, 0F);
        t.getLevel().addFreshEntity(target);
        t.setBlock(new BlockPos(2, 3, 3), Blocks.STONE);
        float initial = target.getHealth();
        long now = t.getLevel().getGameTime();
        MarineWeaponAbilities.staffShot(player);
        for (int i = 0; i < 6; i++)
            MarineWeaponAbilities.tick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        t.assertTrue(target.getHealth() == initial, "Abyss staff hit through a solid wall");
        t.assertTrue(player.getPersistentData().getLong(MarineWeaponAbilities.PREFIX + "staffShot") == now + 40,
            "Blocked travelling pulse should still consume the 40-tick cooldown");
        t.assertTrue(player.getPersistentData().getInt(MarineWeaponAbilities.PREFIX + "shotTicks") <= 0,
            "Staff projectile did not stop at the wall");
        target.discard();
        player.discard();
        t.succeed();
    }
}
