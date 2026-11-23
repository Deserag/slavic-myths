package org.slavicmyths.wolverine;

import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.trophy.TrophyHeads;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class WolverineGameTests {
    private static NorthernWolverine boss(GameTestHelper helper) {
        for (int x=-4;x<15;x++) for (int z=-4;z<15;z++) {
            helper.setBlock(new BlockPos(x,1,z),Blocks.STONE);
            for (int y=2;y<=7;y++) helper.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
        NorthernWolverine boss=WolverineContent.BOSS.get().create(helper.getLevel());
        if (boss==null) throw new IllegalStateException("Wolverine creation failed");
        boss.moveTo(helper.absoluteVec(new Vec3(5,2,5)));
        boss.setNoGravity(true);
        helper.getLevel().addFreshEntity(boss);
        return boss;
    }

    @SuppressWarnings("unchecked")
    private static void pose(NorthernWolverine boss,int move,int ticks) throws Exception {
        Field action=NorthernWolverine.class.getDeclaredField("ACTION");
        Field clock=NorthernWolverine.class.getDeclaredField("CLOCK");
        action.setAccessible(true);clock.setAccessible(true);
        boss.getEntityData().set((EntityDataAccessor<Integer>)action.get(null),move);
        boss.getEntityData().set((EntityDataAccessor<Integer>)clock.get(null),
            (int)boss.level().getGameTime()-ticks);
    }

    @GameTest(template="empty")
    public static void registrationAndBossStats(GameTestHelper helper) {
        NorthernWolverine boss=boss(helper);
        helper.assertTrue(boss.getMaxHealth()==800F,"Boss must have 800 HP");
        helper.assertTrue(!boss.phaseTwo(),"New boss already in second phase");
        helper.assertTrue(Math.abs(boss.getBbHeight()-2.8F)<.02F,"Boss hitbox is not 2.8 blocks");
        helper.assertTrue(WolverineContent.EGG.get() instanceof SpawnEggItem,"Missing wolverine egg");
        helper.assertTrue(TrophyHeads.ITEMS.get(TrophyHeads.Kind.NORTH_WOLVERINE).get()!=null,"Missing trophy item");
        helper.assertTrue(TrophyHeads.BLOCKS.get(TrophyHeads.Kind.NORTH_WOLVERINE).get()!=null,"Missing trophy block");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void twoPhaseThreshold(GameTestHelper helper) {
        NorthernWolverine boss=boss(helper);
        boss.setHealth(401F);boss.tick();
        helper.assertTrue(!boss.phaseTwo(),"Second phase activated above 50% HP");
        boss.setHealth(400F);boss.tick();
        helper.assertTrue(boss.phaseTwo(),"Second phase failed at 400 HP");
        boss.setHealth(700F);boss.tick();
        helper.assertTrue(boss.phaseTwo(),"Second phase should not reset after healing");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void roarBuffAndRecovery(GameTestHelper helper) throws Exception {
        NorthernWolverine boss=boss(helper);
        boss.setHealth(400F);boss.tick();
        pose(boss,NorthernWolverine.ROAR,30);
        boss.tick();
        helper.assertTrue(boss.empowered(),"Roar did not grant temporary buff");
        Field cooldown=NorthernWolverine.class.getDeclaredField("roarCooldown");
        cooldown.setAccessible(true);
        helper.assertTrue(cooldown.getInt(boss)==700,"Roar cooldown is not 35 seconds");
        Field buff=NorthernWolverine.class.getDeclaredField("buffTicks");
        buff.setAccessible(true);
        buff.setInt(boss,1);
        pose(boss,NorthernWolverine.IDLE,0);
        boss.tick();
        helper.assertTrue(!boss.empowered(),"Roar buff never expires");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void encounterStateSurvivesSave(GameTestHelper helper) throws Exception {
        NorthernWolverine boss=boss(helper);
        boss.setHealth(320F);boss.tick();
        pose(boss,NorthernWolverine.ROAR,30);
        boss.tick();
        CompoundTag saved=new CompoundTag();
        boss.saveWithoutId(saved);
        NorthernWolverine loaded=WolverineContent.BOSS.get().create(helper.getLevel());
        if(loaded==null)throw new IllegalStateException("Failed to recreate boss");
        loaded.load(saved);
        helper.assertTrue(loaded.phaseTwo(),"Second phase lost during save/load");
        helper.assertTrue(loaded.empowered(),"Roar buff lost during save/load");
        helper.assertTrue(Math.abs(loaded.getHealth()-320F)<.01F,"Health lost during save/load");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void burrowExitsAtSafeOffset(GameTestHelper helper) throws Exception {
        NorthernWolverine boss=boss(helper);
        Field destination=NorthernWolverine.class.getDeclaredField("burrowDestination");
        destination.setAccessible(true);
        Vec3 target=helper.absoluteVec(new Vec3(7,2,7));
        destination.set(boss,target);
        pose(boss,NorthernWolverine.BURROW,24);
        boss.tick();
        double distance=boss.position().multiply(1,0,1).distanceTo(target.multiply(1,0,1));
        helper.assertTrue(distance>2.5&&distance<4.0,"Burrow did not emerge at a safe offset");
        helper.assertTrue(helper.getLevel().noCollision(boss),"Burrow left boss inside blocks or entities");
        helper.succeed();
    }
    private WolverineGameTests() {}
}
