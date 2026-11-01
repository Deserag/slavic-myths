package org.slavicmyths.posad.boss;

import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.Runes;

/** 2.2 #389 isolated GameTests, never shipped in the production JAR. */
@GameTestHolder("slavicmyths_boss389")
@PrefixGameTestTemplate(false)
public final class Boss389GameTests {
    private static PosadBoss make(GameTestHelper helper, boolean bauk) {
        PosadBoss boss = (bauk ? ModEntities.BAUK : ModEntities.CURSED_MYTAR).get().create(helper.getLevel());
        if (boss == null) throw new AssertionError("Boss factory returned null");
        boss.moveTo(helper.absoluteVec(new Vec3(3D, 3D, 3D)), 0F, 0F);
        helper.getLevel().addFreshEntity(boss);
        return boss;
    }

    @GameTest(template = "empty")
    public static void registeredBossesHaveRealPhysicalStats(GameTestHelper helper) {
        PosadBoss mytar = make(helper, false);
        PosadBoss bauk = make(helper, true);
        bauk.moveTo(bauk.position().add(10D, 0D, 0D));
        helper.assertTrue(mytar.getMaxHealth() == 1350F && mytar.getBbHeight() == 4F, "Mytar must have 1350 HP and 4-block body");
        helper.assertTrue(bauk.getMaxHealth() == 1800F && bauk.getBbWidth() == 3.5F && bauk.getBbHeight() == 3.5F, "Bauk HP/hitbox changed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void healthThresholdsTriggerExactlyOnePhaseTransition(GameTestHelper helper) {
        PosadBoss mytar = make(helper, false);
        mytar.setHealth(607.5F);
        mytar.customServerAiStep();
        helper.assertTrue(mytar.phase() == 2, "Mytar phase two must trigger at 607.5 HP");
        PosadBoss bauk = make(helper, true);
        bauk.setHealth(630F);
        bauk.customServerAiStep();
        helper.assertTrue(bauk.enraged(), "Bauk rage must trigger at 630 HP");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reloadKeepsPhaseRageAndHealth(GameTestHelper helper) {
        PosadBoss bauk = make(helper, true);
        bauk.setHealth(630F);
        bauk.customServerAiStep();
        CompoundTag tag = new CompoundTag();
        bauk.addAdditionalSaveData(tag);
        PosadBoss loaded = ModEntities.BAUK.get().create(helper.getLevel());
        if (loaded == null) throw new AssertionError("Reload factory returned null");
        loaded.readAdditionalSaveData(tag);
        helper.assertTrue(loaded.enraged() && Math.abs(loaded.getHealth() - 630F) < .1F, "Bauk rage/HP must persist");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shackledSickleHasExactlyThreePersistentRuneSockets(GameTestHelper helper) {
        ItemStack sickle = new ItemStack(ModItems.OKOVNY_SICKLE.get());
        helper.assertTrue(Runes.capacity(sickle) == 3, "The actual Rune Anvil must allow exactly 3 slots");
        helper.assertTrue(Runes.slots(sickle) == 0, "New weapon starts at 0/3");
        Runes.write(sickle, 3, List.of("strength", "wind", "blood"));
        helper.assertTrue(Runes.slots(sickle) == 3 && Runes.occupied(sickle) == 3, "Installed rune data must persist on the item");
        String reason = Runes.installationReason(1, Runes.capacity(sickle), Runes.slots(sickle), Runes.list(sickle), "heat", true);
        helper.assertTrue("socket_full".equals(reason), "A fourth rune MUST be rejected");
        helper.succeed();
    }
}
