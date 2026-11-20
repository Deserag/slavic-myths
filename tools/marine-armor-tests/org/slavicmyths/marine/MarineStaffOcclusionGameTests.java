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
        player.getMainHandItem().use(t.getLevel(), player, InteractionHand.MAIN_HAND);
        t.assertTrue(target.getHealth() == initial, "Abyss staff hit through a solid wall");
        t.assertTrue(player.getCooldowns().isOnCooldown(MarineWeapons.STAFF.get()),
            "Blocked pulse should still consume cooldown");
        target.discard();
        player.discard();
        t.succeed();
    }
}
