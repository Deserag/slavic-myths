package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Isolated server GameTests; no client geometry tests are mislabeled as Minecraft inspection. */
@GameTestHolder("slavicmyths_pharaohka")
@PrefixGameTestTemplate(false)
public final class ZhelvaGameTests {
    private static ZhelvaEntity spawn(GameTestHelper test) {
        for (int x=1; x<19; x++)
            for (int z=1; z<19; z++) {
                test.setBlock(new BlockPos(x, 1, z), Blocks.PRISMARINE);
                for (int y=2; y<=8; y++)
                    test.setBlock(new BlockPos(x, y, z), Blocks.WATER);
            }
        ZhelvaEntity boss = ZhelvaContent.BOSS.get().create(test.getLevel());
        test.assertTrue(boss != null, "Zhelva entity is not registered");
        boss.moveTo(test.absoluteVec(new Vec3(9.5,4.5,9.5)));
        boss.setYRot(0);
        boss.setYHeadRot(0);
        boss.setYBodyRot(0);
        test.assertTrue(test.getLevel().addFreshEntity(boss), "Zhelva failed to spawn");
        return boss;
    }
    @GameTest(template="empty", timeoutTicks=60)
    public static void registrationBalanceAndParts(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        test.assertTrue(boss.getMaxHealth()==540F, "540 HP budget missing");
        test.assertTrue(boss.getMaxHealth() < OceanZmeyEntity.attributes().build()
            .getValue(Attributes.MAX_HEALTH), "Must remain below Ocean Zmey");
        test.assertTrue(boss.getMaxHealth() < 1000F, "Must remain below Deep Sea Serpent");
        test.assertTrue(boss.getAttributeValue(Attributes.ATTACK_DAMAGE)==14F, "Turtle damage too high");
        test.assertTrue(boss.getAttributeValue(Attributes.ARMOR)==14F, "Carapace armor missing");
        test.assertTrue(boss.getParts().length==6, "Head, flippers and tail hitboxes absent");
        test.assertTrue(ZhelvaContent.EGG.get().getDefaultInstance().getCount()==1, "Spawn egg absent");
        test.assertTrue(ZhelvaContent.HEAD_ITEM.get().getDefaultInstance().getCount()==1, "Trophy item absent");
        test.assertTrue(ZhelvaContent.STONE_CHARM.get().getDefaultInstance().getCount()==1, "Reward absent");
        boss.discard();test.succeed();
    }
    @GameTest(template="empty", timeoutTicks=55)
    public static void phasesChangeAtHealthThresholds(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        test.assertTrue(ZhelvaEntity.phaseAt(1F)==1, "Initial phase incorrect");
        test.assertTrue(ZhelvaEntity.phaseAt(.6F)==2, "Second phase incorrect");
        test.assertTrue(ZhelvaEntity.phaseAt(.2F)==3, "Third phase incorrect");
        boss.setHealth(320F);
        test.runAfterDelay(3, ()->{
            test.assertTrue(boss.battlePhase()==2, "Phase II is not synced");
            boss.setHealth(120F);
            test.runAfterDelay(3, ()->{
                test.assertTrue(boss.battlePhase()==3, "Phase III is not synced");
                boss.discard();test.succeed();
            });
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void biteTelegraphMatchesDamageTick(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null, "Missing victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(9.5,4.5,13.2)));
        test.assertTrue(test.getLevel().addFreshEntity(victim), "Victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.BITE,34,null);
        test.runAfterDelay(13,()->{
            test.assertTrue(victim.getHealth()==before, "Damage before jaw animation strike");
            test.assertTrue(boss.exposedHead(), "Neck must open during attack");
            test.runAfterDelay(16,()->{
                test.assertTrue(victim.getHealth()<before || !victim.isAlive(),
                    "Bite lacked real server damage at tick 21");
                victim.discard();boss.discard();test.succeed();
            });
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void biteMissesTargetBehindShell(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing rear victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(9.5,4.5,6.2)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Rear victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.BITE,34,null);
        test.runAfterDelay(28,()->{
            test.assertTrue(victim.getHealth()==before,
                "Front bite damaged a target behind the shell");
            victim.discard();boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void pressureRingLeavesInnerSafeZone(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing inner-zone victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(12.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Inner-zone victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.PRESSURE_RING,48,null);
        test.runAfterDelay(38,()->{
            test.assertTrue(victim.getHealth()==before,
                "Pressure ring hit its warned inner safe zone");
            victim.discard();boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=75)
    public static void pressureRingDamagesOnlyAfterWarning(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing wave victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(17.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Wave victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.PRESSURE_RING,48,null);
        test.runAfterDelay(20,()->{
            test.assertTrue(victim.getHealth()==before,"Pressure ring hit before impact");
            test.runAfterDelay(17,()->{
                test.assertTrue(victim.getHealth()<before || !victim.isAlive(),
                    "Pressure ring missed a target inside its warned band");
                victim.discard();boss.discard();test.succeed();
            });
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void leftFlipperDoesNotHitRightSide(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing right-side victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(5.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Right-side victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.SWEEP_LEFT,38,null);
        test.runAfterDelay(31,()->{
            test.assertTrue(victim.getHealth()==before,
                "Left flipper damaged the opposite side");
            victim.discard();boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void leftFlipperHitsWarnedSide(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing left-side victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(13.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Left-side victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.SWEEP_LEFT,38,null);
        test.runAfterDelay(31,()->{
            test.assertTrue(victim.getHealth()<before || !victim.isAlive(),
                "Left flipper missed a target on the warned side");
            victim.discard();boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void rightFlipperHitsOnlyWarnedSide(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var right=EntityType.ZOMBIE.create(test.getLevel());
        var left=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(right!=null && left!=null,"Missing flipper victims");
        right.setNoAi(true);left.setNoAi(true);
        right.moveTo(test.absoluteVec(new Vec3(5.5,4.5,9.5)));
        left.moveTo(test.absoluteVec(new Vec3(13.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(right),"Right victim spawn failed");
        test.assertTrue(test.getLevel().addFreshEntity(left),"Left victim spawn failed");
        float rightBefore=right.getHealth(), leftBefore=left.getHealth();
        boss.begin(ZhelvaEntity.SWEEP_RIGHT,38,null);
        test.runAfterDelay(31,()->{
            test.assertTrue(right.getHealth()<rightBefore || !right.isAlive(),
                "Right flipper missed the warned side");
            test.assertTrue(left.getHealth()==leftBefore,
                "Right flipper damaged the opposite side");
            right.discard();left.discard();boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void pressureLanceCanBeDodged(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing lance victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(12.5,4.5,12.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Lance victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.PRESSURE_LANCE,44,
            test.absoluteVec(new Vec3(12.5,4.5,12.5)));
        test.runAfterDelay(17,()->{
            test.assertTrue(victim.getHealth()==before,"Lance hit before warning ended");
            victim.moveTo(test.absoluteVec(new Vec3(16.5,4.5,12.5)));
            test.runAfterDelay(19,()->{
                test.assertTrue(victim.getHealth()==before,
                    "Lance hit after target left the warned area");
                victim.discard();boss.discard();test.succeed();
            });
        });
    }
    @GameTest(template="empty", timeoutTicks=70)
    public static void pressureLanceHitsLockedAirborneArea(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing airborne lance victim");
        victim.setNoAi(true);victim.setNoGravity(true);
        victim.moveTo(test.absoluteVec(new Vec3(12.5,12.5,12.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Airborne victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.PRESSURE_LANCE,44,
            test.absoluteVec(new Vec3(12.5,12.5,12.5)));
        test.runAfterDelay(17,()->{
            test.assertTrue(victim.getHealth()==before,"Lance hit before its warning");
            test.runAfterDelay(19,()->{
                test.assertTrue(victim.getHealth()<before || !victim.isAlive(),
                    "Lance missed the locked airborne area");
                victim.discard();boss.discard();test.succeed();
            });
        });
    }
    @GameTest(template="empty", timeoutTicks=80)
    public static void shellSlamHitsAfterPhaseThreeWarning(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        boss.setHealth(120F);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing shell victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(12.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Shell victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.SHELL_SLAM,56,null);
        test.runAfterDelay(27,()->{
            test.assertTrue(victim.getHealth()==before,"Shell slam hit before impact");
            test.runAfterDelay(20,()->{
                test.assertTrue(victim.getHealth()<before || !victim.isAlive(),
                    "Phase III shell slam missed a nearby target");
                victim.discard();boss.discard();test.succeed();
            });
        });
    }
    @GameTest(template="empty", timeoutTicks=75)
    public static void siltBurstHitsLockedAreaAfterWarning(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        boss.setHealth(300F);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null,"Missing silt victim");
        victim.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(12.5,4.5,12.5)));
        test.assertTrue(test.getLevel().addFreshEntity(victim),"Silt victim spawn failed");
        float before=victim.getHealth();
        boss.begin(ZhelvaEntity.SILT,53,test.absoluteVec(new Vec3(12.5,4.5,12.5)));
        test.runAfterDelay(24,()->{
            test.assertTrue(victim.getHealth()==before,"Silt hit before warning ended");
            test.runAfterDelay(18,()->{
                test.assertTrue(victim.getHealth()<before || !victim.isAlive(),
                    "Silt missed the locked warned area");
                victim.discard();boss.discard();test.succeed();
            });
        });
    }
    @GameTest(template="empty", timeoutTicks=125)
    public static void idleBossesStayInArenaWithoutPlayers(GameTestHelper test) {
        ZhelvaEntity first=spawn(test);
        ZhelvaEntity second=ZhelvaContent.BOSS.get().create(test.getLevel());
        test.assertTrue(second!=null,"Second Zhelva registration missing");
        second.moveTo(test.absoluteVec(new Vec3(13.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(second),"Second Zhelva spawn failed");
        test.runAfterDelay(95,()->{
            test.assertTrue(first.isAlive() && second.isAlive(),
                "Idle bosses died or stalled during simultaneous server ticks");
            test.assertTrue(first.distanceToSqr(test.absoluteVec(new Vec3(9.5,4.5,9.5)))<20*20
                && second.distanceToSqr(test.absoluteVec(new Vec3(13.5,4.5,9.5)))<20*20,
                "Idle Zhelva left its arena without a target");
            first.discard();second.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=45)
    public static void guardAndVulnerableHeadWindows(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        boss.begin(ZhelvaEntity.GUARD,42,null);
        test.assertTrue(!boss.exposedHead(), "Guard should hide head");
        boss.begin(ZhelvaEntity.BITE,34,null);
        test.runAfterDelay(14,()->{
            test.assertTrue(boss.exposedHead(), "Bite must expose head");
            boss.begin(ZhelvaEntity.IDLE,0,null);
            test.assertTrue(!boss.exposedHead(), "Neutral head incorrectly exposed");
            boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=55)
    public static void saveReloadKeepsPhaseAndCancelsUnsafeStrike(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        boss.setHealth(125F);
        boss.begin(ZhelvaEntity.SHELL_SLAM,55,null);
        CompoundTag tag=new CompoundTag();
        boss.addAdditionalSaveData(tag);
        ZhelvaEntity restored=ZhelvaContent.BOSS.get().create(test.getLevel());
        test.assertTrue(restored!=null,"Missing reloaded boss");
        restored.readAdditionalSaveData(tag);
        test.assertTrue(restored.action()==ZhelvaEntity.IDLE, "Unsafe old attack replayed on load");
        test.assertTrue(restored.battlePhase()==3, "Phase lost when restoring HP");
        test.assertTrue(restored.getParts().length==6, "Reload lost multipart collision");
        boss.discard();restored.discard();test.succeed();
    }
    @GameTest(template="empty", timeoutTicks=65)
    public static void unreachableHighTargetTriggersRangedAttack(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var highTarget=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(highTarget!=null,"Missing airborne target fixture");
        highTarget.setNoAi(true);
        highTarget.setNoGravity(true);
        highTarget.moveTo(test.absoluteVec(new Vec3(9.5,16.5,16)));
        test.assertTrue(test.getLevel().addFreshEntity(highTarget),"Could not spawn elevated target");
        boss.setTarget(highTarget);
        test.runAfterDelay(34,()->{
            test.assertTrue(boss.action()==ZhelvaEntity.PRESSURE_LANCE,
                "Elevated target caused endless path chase instead of ranged attack");
            highTarget.discard();boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=55)
    public static void targetBeyondPressureLanceRangeIsReleased(GameTestHelper test) {
        ZhelvaEntity boss=spawn(test);
        var distant=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(distant!=null,"Missing distant target fixture");
        distant.setNoAi(true);
        distant.setNoGravity(true);
        distant.moveTo(test.absoluteVec(new Vec3(9.5,32.5,30.5)));
        test.assertTrue(test.getLevel().addFreshEntity(distant),"Could not spawn distant target");
        boss.setTarget(distant);
        test.runAfterDelay(12,()->{
            test.assertTrue(boss.getTarget()==null,
                "Zhelva kept pursuing a target beyond pressure lance range");
            distant.discard();boss.discard();test.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=42)
    public static void inWorldTrophyUsesBlockEntity(GameTestHelper test) {
        BlockPos pos=new BlockPos(8,2,8);
        var state=ZhelvaContent.HEAD.get().defaultBlockState()
            .setValue(ZhelvaTrophyBlock.FACING,Direction.NORTH);
        test.setBlock(pos,state);
        test.assertTrue(test.getLevel().getBlockEntity(test.absolutePos(pos))
            instanceof ZhelvaTrophyBlock.Tile, "3D trophy block-entity missing");
        test.succeed();
    }
    @GameTest(template="empty", timeoutTicks=45)
    public static void neckMeshNeverBakesZeroThicknessCuboids(GameTestHelper test) {
        // Same section and sampling laws as the actual client ModelPart geometry.
        // In the old model the first x=-radius was 0 high and broke resource reload.
        int baked=0, skipped=0;
        for (float z=-59F; z<=-31F; z+=4.2F) {
            float radius=12.5F+3.5F*((z+59F)/28F);
            test.assertTrue(ZhelvaGeometryProfile.neckThickness(-radius,radius)==0F,
                "An ellipse boundary must not become a cube");
            for (float x=-radius+2.2F; x<radius; x+=4.4F) {
                float thickness=ZhelvaGeometryProfile.neckThickness(x,radius);
                if (thickness<=0F) { skipped++; continue; }
                test.assertTrue(Float.isFinite(thickness) && thickness*1.9F>0.01F,
                    "Client mesh would contain empty or invalid neck cuboid");
                baked++;
            }
        }
        test.assertTrue(baked>=35 && baked<=65,
            "Head/neck silhouette or cuboid budget unexpectedly changed: "+baked);
        test.assertTrue(skipped==0,
            "Neck samples should be in the ellipse interior, not on its boundary");
        test.succeed();
    }
    @GameTest(template="empty", timeoutTicks=55)
    public static void pressureCentersRemainSeparate(GameTestHelper test) {
        Vec3 center=new Vec3(9,4,9);
        for(int i=0;i<3;i++)for(int j=i+1;j<3;j++)
            test.assertTrue(ZhelvaEntity.vortexCenter(center,i).distanceTo(
                ZhelvaEntity.vortexCenter(center,j)) > 10.0,
                "Whirlpools overlap: telegraphed safe spaces are lost");
        test.succeed();
    }
}
