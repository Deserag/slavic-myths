package org.slavicmyths.marine;

import java.lang.reflect.Field;
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

/** Opt-in real NeoForge GameTests, kept outside the production mod JAR. */
@GameTestHolder("slavicmyths_pharaohka")
@PrefixGameTestTemplate(false)
public final class TsarPikeGameTests {
    private static void tank(GameTestHelper test) {
        for (int x=1;x<=19;x++) for (int z=1;z<=19;z++) {
            test.setBlock(new BlockPos(x,1,z),Blocks.PRISMARINE);
            for (int y=2;y<=8;y++)
                test.setBlock(new BlockPos(x,y,z),
                    x==1||z==1||x==19||z==19?Blocks.PRISMARINE:Blocks.WATER);
        }
    }
    private static TsarPikeEntity spawn(GameTestHelper test,int x,int y,int z) {
        TsarPikeEntity pike=TsarPikeContent.BOSS.get().create(test.getLevel());
        test.assertTrue(pike!=null,"Tsar Pike registration failed");
        pike.moveTo(test.absoluteVec(new Vec3(x+.5,y+.2,z+.5)));
        test.assertTrue(test.getLevel().addFreshEntity(pike),"Unable to spawn Tsar Pike");
        return pike;
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void registeredAndTierFive(GameTestHelper test) {
        tank(test);
        TsarPikeEntity boss=spawn(test,8,3,8);
        test.assertTrue(boss.getMaxHealth()==600,"Tsar Pike proposed 600 HP not applied");
        test.assertTrue(boss.getAttributeValue(Attributes.ATTACK_DAMAGE)==18,
            "Approved tier V base damage not applied");
        test.assertTrue(boss.getAttributeValue(Attributes.ARMOR)==6,"Boss armor missing");
        test.assertTrue(boss.getAttributeValue(Attributes.FOLLOW_RANGE)==32,"Follow distance incorrect");
        test.assertTrue(TsarPikeContent.EGG.get().getDefaultInstance().getCount()==1,"Spawn egg missing");
        test.assertTrue(TsarPikeContent.HEAD_ITEM.get().getDefaultInstance().getCount()==1,
            "Head trophy item missing");
        test.assertTrue(TsarPikeEntity.BITE_DAMAGE>11 && TsarPikeEntity.TAIL_DAMAGE>12,
            "Tier V attack coefficients were not applied");
        CompoundTag tag=new CompoundTag();
        boss.addAdditionalSaveData(tag);
        TsarPikeEntity restored=TsarPikeContent.BOSS.get().create(test.getLevel());
        restored.readAdditionalSaveData(tag);
        test.assertTrue(restored.getMaxHealth()==600 && restored.action()==TsarPikeEntity.IDLE,
            "Saved boss state was not restored safely");
        boss.discard();restored.discard();test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=90)
    public static void attacksNonPlayerFish(GameTestHelper test){
        tank(test);
        TsarPikeEntity pike=spawn(test,8,3,8);
        var cod=EntityType.COD.create(test.getLevel());
        test.assertTrue(cod!=null,"Cannot create non-player target");
        cod.setNoAi(true);
        cod.moveTo(test.absoluteVec(new Vec3(8.5,3.5,12.5)));
        test.assertTrue(test.getLevel().addFreshEntity(cod),"Could not spawn fish prey");
        test.runAfterDelay(36,()->{
            test.assertTrue(pike.getTarget()==cod || !cod.isAlive(),
                "Tsar Pike did not discover and pursue non-player aquatic prey");
            pike.discard();cod.discard();test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=45)
    public static void trophyRemovesBothCells(GameTestHelper test){
        BlockPos anchor=new BlockPos(8,2,8);
        var base=TsarPikeContent.HEAD.get().defaultBlockState()
            .setValue(TsarPikeHeadBlock.FACING,Direction.NORTH)
            .setValue(TsarPikeHeadBlock.FRONT,false);
        var nose=base.setValue(TsarPikeHeadBlock.FRONT,true);
        test.setBlock(anchor,base);
        test.setBlock(anchor.north(),nose);
        test.assertTrue(test.getBlockState(anchor.north()).is(TsarPikeContent.HEAD.get()),
            "The 2-block head did not occupy its front cell");
        test.getLevel().destroyBlock(test.absolutePos(anchor),true);
        test.runAfterDelay(3,()->{
            test.assertTrue(test.getBlockState(anchor).isAir()
                && test.getBlockState(anchor.north()).isAir(),
                "The two trophy blocks did not clean up together");
            test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=80)
    public static void trophyReactsWithoutGrantingUnactivatedAura(GameTestHelper test){
        BlockPos anchor=new BlockPos(8,2,8);
        var block=TsarPikeContent.HEAD.get().defaultBlockState()
            .setValue(TsarPikeHeadBlock.FACING,Direction.NORTH)
            .setValue(TsarPikeHeadBlock.FRONT,false);
        test.setBlock(anchor,block);
        var tile=test.getLevel().getBlockEntity(test.absolutePos(anchor));
        test.assertTrue(tile instanceof TsarPikeHeadBlock.Tile,"Missing animated trophy tile");
        var head=(TsarPikeHeadBlock.Tile)tile;
        var cod=EntityType.COD.create(test.getLevel());
        test.assertTrue(cod!=null,"Missing proximity prey fixture");
        cod.setNoAi(true);
        cod.moveTo(test.absoluteVec(new Vec3(8.5,3.1,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(cod),"Could not spawn trophy proximity fixture");
        test.runAfterDelay(28,()->{
            test.assertTrue(head.reactive(),"Nearby living prey did not awaken trophy jaw animation");
            test.assertTrue(!head.active(),"Visual proximity reaction must not auto-enable the gameplay aura");
            cod.discard();
            test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=50)
    public static void damageBreaksJawGrab(GameTestHelper test){
        tank(test);
        TsarPikeEntity boss=spawn(test,8,3,8);
        var victim=EntityType.ZOMBIE.create(test.getLevel());
        var attacker=EntityType.ZOMBIE.create(test.getLevel());
        test.assertTrue(victim!=null && attacker!=null,"Missing jaw-grab fixture");
        victim.setNoAi(true);attacker.setNoAi(true);
        victim.moveTo(test.absoluteVec(new Vec3(8.5,3.2,11.5)));
        attacker.moveTo(test.absoluteVec(new Vec3(11.5,3.2,8.5)));
        test.getLevel().addFreshEntity(victim);test.getLevel().addFreshEntity(attacker);
        try {
            Field held=TsarPikeEntity.class.getDeclaredField("victimId");
            held.setAccessible(true);
            held.set(boss,victim.getUUID());
            test.assertTrue(boss.hurt(attacker.damageSources().mobAttack(attacker),22F),
                "Boss did not receive a legitimate rescue hit");
            test.assertTrue(held.get(boss)==null,
                "A strong rescue hit did not release the captured target");
        }catch(ReflectiveOperationException ex){
            throw new IllegalStateException("Jaw-grab server fixture failed",ex);
        }
        boss.discard();victim.discard();attacker.discard();test.succeed();
    }
}
