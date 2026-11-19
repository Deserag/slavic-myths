package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("slavicmyths_pharaohka")
@PrefixGameTestTemplate(false)
public final class OceanZmeyGameTests {
    private static OceanZmeyEntity spawn(GameTestHelper test){
        for(int x=1;x<19;x++)for(int z=1;z<19;z++)
            for(int y=2;y<=8;y++)test.setBlock(new BlockPos(x,y,z),Blocks.WATER);
        OceanZmeyEntity boss=OceanZmeyContent.BOSS.get().create(test.getLevel());
        test.assertTrue(boss!=null,"Ocean Zmey type not registered");
        boss.moveTo(test.absoluteVec(new Vec3(9.5,4.5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(boss),"Could not spawn Ocean Zmey");
        return boss;
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void independentRegistrationAndBalance(GameTestHelper test){
        OceanZmeyEntity boss=spawn(test);
        test.assertTrue(boss.getMaxHealth()==1500,"Boss health was not applied");
        test.assertTrue(boss.getAttributeValue(Attributes.ATTACK_DAMAGE)==26,"Attack not registered");
        test.assertTrue(OceanZmeyContent.EGG.get().getDefaultInstance().getCount()==1,"Egg missing");
        test.assertTrue(OceanZmeyContent.HEAD_ITEM.get().getDefaultInstance().getCount()==1,"Head missing");
        test.assertTrue(!OceanZmeyContent.BOSS.get().equals(DeepSeaSerpentContent.BOSS.get()),
            "New boss must have independent registry identity");
        test.assertTrue(boss.getParts().length==17,"Articulated hitboxes missing");
        boss.discard();test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void trunkRadiusAndTailTaper(GameTestHelper test){
        test.assertTrue(OceanZmeyEntity.radius(0) >= 2.6F,"Trunk is too thin");
        test.assertTrue(OceanZmeyEntity.radius(.7F) >= 2.4F,"Body shrinks too early");
        test.assertTrue(OceanZmeyEntity.radius(.8F)>OceanZmeyEntity.radius(.95F),
            "Tail must taper gradually");
        test.assertTrue(OceanZmeyEntity.radius(1F)<.3F,"Tip must be narrow");
        test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void stormFunnelsHaveConsistentHitZones(GameTestHelper test){
        Vec3 center=new Vec3(30,4,30);
        for(int tick:new int[]{0,20,60,100}){
            for(int i=0;i<5;i++){
                Vec3 funnel=OceanZmeyEntity.stormCenter(center,tick,i);
                Vec3 offset=funnel.subtract(center);
                test.assertTrue(Math.abs(offset.y)<.001,"Storm height drift");
                test.assertTrue(Math.abs(offset.horizontalDistance()-9)<.001,
                    "Storm telegraph must orbit at 9 blocks");
                for(int j=i+1;j<5;j++){
                    Vec3 other=OceanZmeyEntity.stormCenter(center,tick,j);
                    test.assertTrue(funnel.distanceTo(other)>4.5,
                        "Storm funnels must remain independently identifiable");
                }
            }
        }
        test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void headTrophyLeavesNoOrphanCells(GameTestHelper test){
        OceanZmeyHeadBlock block=(OceanZmeyHeadBlock)OceanZmeyContent.HEAD.get();
        BlockPos anchor=new BlockPos(8,2,8);
        Direction face=Direction.NORTH;
        var state=block.defaultBlockState().setValue(OceanZmeyHeadBlock.FACING,face);
        for(int i=0;i<6;i++)
            test.setBlock(block.at(anchor,face,i),state.setValue(OceanZmeyHeadBlock.PART,i));
        test.getLevel().destroyBlock(test.absolutePos(block.at(anchor,face,5)),true);
        test.runAfterDelay(3,()->{
            for(int i=0;i<6;i++)test.assertTrue(
                test.getBlockState(block.at(anchor,face,i)).isAir(),"Orphan cell "+i);
            test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void restoreIdleOnReload(GameTestHelper test){
        OceanZmeyEntity boss=spawn(test);
        CompoundTag nbt=new CompoundTag();
        boss.addAdditionalSaveData(nbt);
        OceanZmeyEntity copy=OceanZmeyContent.BOSS.get().create(test.getLevel());
        copy.readAdditionalSaveData(nbt);
        test.assertTrue(copy.action()==OceanZmeyEntity.IDLE,"Invalid animation state restored");
        test.assertTrue(copy.getParts().length==17,"Multipart data lost after reload");
        boss.discard();copy.discard();test.succeed();
    }
}
