package org.slavicmyths.marine;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.rpg.PathData;
import org.slavicmyths.sisters.GneteyaGreatHammer;
import org.slavicmyths.sisters.SistersContent;

/** Verify the real server hammer mechanics and independent cooldowns. */
@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class GneteyaSkillsGameTests {
    @GameTest(template = "empty", timeoutTicks = 60)
    public static void boneFractureCombosIntoSentenceWithoutBreakingTerrain(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "GneteyaQA"));
        player.moveTo(t.absoluteVec(new Vec3(3, 3, 3)));
        player.setGameMode(GameType.SURVIVAL);
        player.setYRot(0F);
        player.setXRot(8F);
        ItemStack hammer = new ItemStack(SistersContent.GNETEYA_GREAT_HAMMER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
        var target = EntityType.COW.create(t.getLevel());
        t.assertTrue(target != null, "Missing hammer target");
        target.setNoAi(true);
        target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100D);
        target.setHealth(100F);
        target.moveTo(t.absoluteVec(new Vec3(3, 3, 5)));
        t.getLevel().addFreshEntity(target);
        t.setBlock(new BlockPos(4, 2, 4), Blocks.STONE);
        long now = t.getLevel().getGameTime();
        player.setShiftKeyDown(true);
        GneteyaGreatHammer.boneFracture(player);
        t.assertTrue(Math.abs(target.getHealth() - 66F) < .1F,
            "Bone Fracture must deal 34 HP: " + target.getHealth());
        t.assertTrue(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                && target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 1,
            "Bone Fracture did not apply Slowness II");
        t.assertTrue(PathData.data(player).getLong("GneteyaGreatHammerFractureReady") == now + 280,
            "Bone Fracture must independently recharge for 14 seconds");

        // Sentence is available even during Bone Fracture's cooldown.
        target.invulnerableTime = 0;
        player.setShiftKeyDown(false);
        hammer.use(t.getLevel(), player, InteractionHand.MAIN_HAND);
        hammer.getItem().releaseUsing(hammer, t.getLevel(), player, 72000 - 14);
        t.assertTrue(Math.abs(target.getHealth() - 26F) < .1F,
            "Sentence did not add the 6 HP hammer-debuff bonus: " + target.getHealth());
        t.assertTrue(PathData.data(player).getLong("GneteyaGreatHammerSentenceReady") == now + 360,
            "Sentence must independently recharge for 18 seconds");
        t.assertTrue(t.getLevel().getBlockState(t.absolutePos(new BlockPos(4, 2, 4))).is(Blocks.STONE),
            "Bone Fracture changed terrain");
        target.discard();
        player.discard();
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void boneFractureRequiresEmptyOffhand(GameTestHelper t) {
        var player = FakePlayerFactory.get(t.getLevel(),
            new GameProfile(UUID.randomUUID(), "GneteyaOffhand"));
        player.moveTo(t.absoluteVec(new Vec3(3, 3, 3)));
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND,
            new ItemStack(SistersContent.GNETEYA_GREAT_HAMMER.get()));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
        var target = EntityType.COW.create(t.getLevel());
        t.assertTrue(target != null, "Missing offhand target");
        target.setNoAi(true);
        target.moveTo(t.absoluteVec(new Vec3(3, 3, 5)));
        t.getLevel().addFreshEntity(target);
        float health = target.getHealth();
        GneteyaGreatHammer.boneFracture(player);
        t.assertTrue(target.getHealth() == health, "Offhand hammer skill damaged the target");
        t.assertTrue(PathData.data(player).getLong("GneteyaGreatHammerFractureReady") == 0,
            "Blocked hammer skill consumed cooldown");
        target.discard();
        player.discard();
        t.succeed();
    }
}
