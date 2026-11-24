package org.slavicmyths.sisters;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class GluheyaGameTests {
    @GameTest(template="empty",timeoutTicks=80)
    public static void registrationAndPhaseTransition(GameTestHelper t){
        for(int x=1;x<=5;x++)for(int z=1;z<=5;z++)
            t.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        var boss=SistersContent.GLUHEYA.get().create(t.getLevel());
        t.assertTrue(boss!=null,"Gluheya entity missing");
        boss.moveTo(t.absoluteVec(new Vec3(3,2,3)));
        t.getLevel().addFreshEntity(boss);
        t.assertTrue(SistersContent.GLUHEYA_EGG.get() instanceof SpawnEggItem,"Gluheya spawn egg missing");
        t.assertTrue(Math.abs(boss.getBbHeight()-3.75F)<.02F,"Gluheya hitbox changed");
        t.assertTrue(boss.getMaxHealth()==1600F&&!boss.secondPhase(),"Gluheya initial phase or health changed");
        boss.setHealth(801F);
        boss.tick();
        t.assertTrue(!boss.secondPhase()&&boss.action()!=GluheyaBoss.SHIFT,"Premature phase change");
        boss.setHealth(800F);
        boss.tick();
        t.assertTrue(boss.action()==GluheyaBoss.SHIFT,"Phase transition not started");
        boss.tick();
        t.assertTrue(boss.secondPhase(),"Second phase not enabled");
        t.assertTrue(Math.abs(boss.getAttributeValue(Attributes.ARMOR)-10D)<.01D,"Second phase armor changed");
        t.assertTrue(Math.abs(boss.getAttributeValue(Attributes.MOVEMENT_SPEED)-.37D)<.01D,"Second phase movement speed changed");
        boss.discard();
        t.succeed();
    }

    @GameTest(template="empty")
    public static void silenceRingOnlyWhenTargetWithinItsRange(GameTestHelper t){
        t.assertTrue(GluheyaBoss.phaseOneAttack(2,81D)==GluheyaBoss.SILENCE_RING,"Ring unavailable in range");
        t.assertTrue(GluheyaBoss.phaseOneAttack(2,121D)==GluheyaBoss.FAST_WAVE,"Ring chosen outside its range");
        t.assertTrue(GluheyaBoss.phaseOneAttack(1,121D)==GluheyaBoss.LONG_BELL,"Long bell selection changed");
        t.assertTrue(GluheyaBoss.phaseOneAttack(3,121D)==GluheyaBoss.BELL_STORM,"Bell storm selection changed");
        t.succeed();
    }
}
