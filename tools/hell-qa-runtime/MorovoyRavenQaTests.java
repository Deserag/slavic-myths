package org.slavicmyths.hellqa;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.gametest.framework.GameTest;
import org.slavicmyths.morovoy.MorovoyRaven;
import org.slavicmyths.morovoy.MorovoyRavenContent;

/** Real gameTestServer tests against a production JAR. Not a GUI/MP/audio acceptance gate. */
@GameTestHolder("slavicmyths_hellqa") @PrefixGameTestTemplate(false)
public final class MorovoyRavenQaTests {
    private static void check(boolean value, String message) {
        if (!value) throw new GameTestAssertException(message);
    }

    @GameTest(template="empty", timeoutTicks=360)
    public static void realRavenWorldTickAndPersistence(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos anchor = test.absolutePos(new BlockPos(3,2,3));
        for (int x=-8;x<=8;x++) for (int z=-8;z<=8;z++) world.setBlock(anchor.offset(x,-1,z), Blocks.BLACKSTONE.defaultBlockState(), 2);
        MorovoyRaven boss = MorovoyRavenContent.BOSS.get().create(world);
        check(boss != null, "Production raven entity was not registered");
        boss.moveTo(anchor.getX()+.5, anchor.getY()+10, anchor.getZ()+.5, 0, 0);
        boss.setArenaAnchor(anchor);
        world.addFreshEntity(boss);
        check(boss.isAlive() && boss.getMaxHealth()==1600F, "Raven max health incorrect");
        check(boss.getBbHeight() >= 4.9F, "Voxel boss collider shorter than approved art");
        Vec3 initial = boss.position();
        test.runAfterDelay(160, () -> {
            check(boss.isAlive() && !boss.isRemoved(), "Raven crashed or despawned on actual server ticks");
            check(boss.tickCount >= 155, "Raven did not receive actual Minecraft world ticks");
            check(boss.pose() >= MorovoyRaven.CIRCLE && boss.pose() <= MorovoyRaven.TAKEOFF, "Raven phase escaped registered state machine");
            CompoundTag saved = new CompoundTag();
            boss.addAdditionalSaveData(saved);
            check(saved.getBoolean("HasHome"), "Raven lost arena anchor during native server ticks");
            check(saved.getInt("HomeX")==anchor.getX() && saved.getInt("HomeY")==anchor.getY() && saved.getInt("HomeZ")==anchor.getZ(), "Raven anchor coordinates changed");
            MorovoyRaven restored = MorovoyRavenContent.BOSS.get().create(world);
            check(restored != null, "Restored boss type not registered");
            restored.readAdditionalSaveData(saved);
            check(restored.pose()==boss.pose() && restored.getBbHeight()>=4.9F, "Saved pose/geometry not restored");
            check(boss.position().distanceToSqr(initial)>0.1, "Raven stayed frozen instead of performing server-authoritative movement");
            System.out.println("RAVEN_GAME_TEST_PASS world_ticks="+boss.tickCount+" phase="+boss.pose()+" displacement="+boss.position().distanceTo(initial)+" seed="+world.getSeed());
            boss.discard();
            test.succeed();
        });
    }

    @GameTest(template="empty", timeoutTicks=80)
    public static void realRavenMinionOwnershipAndBounds(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos anchor=test.absolutePos(new BlockPos(2,2,2));
        var boss=MorovoyRavenContent.BOSS.get().create(world);
        var minion=MorovoyRavenContent.MINION.get().create(world);
        check(boss!=null && minion!=null, "Production boss/minion registry absent");
        boss.moveTo(test.absoluteVec(new Vec3(2,12,2)));
        boss.setArenaAnchor(anchor);
        world.addFreshEntity(boss);
        minion.moveTo(boss.position().add(3,1,0));
        minion.attach(boss,0);
        world.addFreshEntity(minion);
        check(minion.minion() && boss.getUUID().equals(minion.parent()), "Minion is not linked to its Raven parent");
        check(minion.getMaxHealth()==28F && minion.getBbHeight()>=1.3F, "Minion HP/geometry scaling is wrong");
        CompoundTag tag=new CompoundTag();
        minion.addAdditionalSaveData(tag);
        check(tag.hasUUID("Parent"), "Minion parent UUID was not persisted");
        minion.discard();
        boss.discard();
        System.out.println("RAVEN_GAME_TEST_PASS minion ownership, HP, parent NBT, scaled collider");
        test.succeed();
    }
}
