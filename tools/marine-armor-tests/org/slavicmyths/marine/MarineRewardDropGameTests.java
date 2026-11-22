package org.slavicmyths.marine;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Actual boss death events, including the leader-only Pharaohka reward boundary. */
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class MarineRewardDropGameTests {
    private static <T extends LivingEntity> void death(GameTestHelper t,
            EntityType<T> type, Item material, int expectedCount, Item token) {
        var boss = type.create(t.getLevel());
        t.assertTrue(boss != null, "Boss entity was not created");
        if (boss instanceof net.minecraft.world.entity.Mob mob) mob.setNoAi(true);
        boss.moveTo(t.absolutePos(new BlockPos(2, 2, 2)), 0F, 0F);
        t.assertTrue(t.getLevel().addFreshEntity(boss), "Boss could not spawn");
        boss.setHealth(0);
        boss.die(t.getLevel().damageSources().genericKill());
        var pos = boss.position();
        t.runAfterDelay(2, () -> {
            List<ItemEntity> items = t.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(pos, pos).inflate(2.5));
            int total = items.stream().filter(e -> e.getItem().is(material))
                .mapToInt(e -> e.getItem().getCount()).sum();
            long tokenCount = items.stream().filter(e -> e.getItem().is(token))
                .mapToLong(e -> e.getItem().getCount()).sum();
            t.assertTrue(total == expectedCount, "Wrong material count from death of "
                + type + ": expected " + expectedCount + ", got " + total);
            t.assertTrue(tokenCount == (expectedCount == 0 ? 0 : 1),
                "Missing or duplicate trade token after death of " + type);
            items.forEach(ItemEntity::discard);
            boss.discard();
            t.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void pharaohkaLeaderDropsAndOnlyLeader(GameTestHelper t) {
        death(t, PharaohkaContent.LEADER.get(), MarineRewards.PHARAOHKA_MATERIAL.get(),
            3, MarineRewards.PHARAOHKA_TOKEN.get());
    }
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void ordinaryPharaohkaNeverDropsLeaderRewards(GameTestHelper t) {
        death(t, PharaohkaContent.PHARAOHKA.get(), MarineRewards.PHARAOHKA_MATERIAL.get(),
            0, MarineRewards.PHARAOHKA_TOKEN.get());
    }
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void serpentDropsExactlyFiveScalesAndToken(GameTestHelper t) {
        death(t, DeepSeaSerpentContent.BOSS.get(), MarineRewards.DEEP_SERPENT_MATERIAL.get(),
            5, MarineRewards.DEEP_SERPENT_TOKEN.get());
    }
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void oceanZmeyDropsExactlyThreeHeartsAndToken(GameTestHelper t) {
        death(t, OceanZmeyContent.BOSS.get(), MarineRewards.OCEAN_ZMEY_MATERIAL.get(),
            3, MarineRewards.OCEAN_ZMEY_TOKEN.get());
    }
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void stratimDropsExactlyFourFeathersAndToken(GameTestHelper t) {
        death(t, StratimBirdContent.BOSS.get(), MarineRewards.STRATIM_MATERIAL.get(),
            4, MarineRewards.STRATIM_TOKEN.get());
    }
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void tsarPikeDropsExactlyThreeFangsAndToken(GameTestHelper t) {
        death(t, TsarPikeContent.BOSS.get(), MarineRewards.TSAR_PIKE_MATERIAL.get(),
            3, MarineRewards.TSAR_PIKE_TOKEN.get());
    }
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void zhelvaDropsExactlySixPlatesAndToken(GameTestHelper t) {
        death(t, ZhelvaContent.BOSS.get(), MarineRewards.ZHELVA_MATERIAL.get(),
            6, MarineRewards.ZHELVA_TOKEN.get());
    }
}
