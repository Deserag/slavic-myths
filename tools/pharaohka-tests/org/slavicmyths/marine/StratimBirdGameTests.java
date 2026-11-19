package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Isolated Stratim tests: no production test assets or changes to old bosses. */
@GameTestHolder("slavicmyths_pharaohka")
@PrefixGameTestTemplate(false)
public final class StratimBirdGameTests {
    private static BlockPos cell(BlockPos base,Direction facing,int x,int y,int z){
        return base.relative(facing.getClockWise(),x).above(y).relative(facing,z);
    }
    @GameTest(template="empty",timeoutTicks=55)
    public static void bossRegistryAndTierSevenBalance(GameTestHelper test){
        var boss=StratimBirdContent.BOSS.get().create(test.getLevel());
        test.assertTrue(boss!=null,"Missing registered Stratim bird");
        test.assertTrue(boss.getMaxHealth()==1024,"Stratim must use its own tier VII health");
        test.assertTrue(boss.getAttributeValue(Attributes.ARMOR)==8,"Stratim armor incorrect");
        test.assertTrue(boss.getAttributeValue(Attributes.ATTACK_DAMAGE)==22,"Stratim attack incorrect");
        test.assertTrue(StratimBirdContent.EGG.get().getDefaultInstance().getCount()==1,
            "Missing summon egg");
        test.assertTrue(StratimBirdContent.HEAD_ITEM.get().getDefaultInstance().getCount()==1,
            "Missing trophy item");
        boss.discard();test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=55)
    public static void wingsHeadAndTailCanTakeDamage(GameTestHelper test){
        var bird=StratimBirdContent.BOSS.get().create(test.getLevel());
        test.assertTrue(bird!=null,"Missing Stratim boss");
        bird.moveTo(test.absoluteVec(new net.minecraft.world.phys.Vec3(9.5,5,9.5)));
        test.assertTrue(test.getLevel().addFreshEntity(bird),"Unable to spawn Stratim");
        var segments=bird.getParts();
        test.assertTrue(bird.isMultipartEntity()&&segments.length==8,"Missing wing/head/tail zones");
        test.assertTrue(segments[3].isPickable()&&segments[6].isPickable(),
            "Outer primary feathers must be targetable");
        float old=bird.getHealth();
        test.assertTrue(segments[3].hurt(bird.damageSources().generic(),35F),
            "Wing damage was not forwarded");
        test.assertTrue(bird.getHealth()<old,"Stratim HP did not decrease after wing hit");
        bird.discard();test.succeed();
    }
    @GameTest(template="empty",timeoutTicks=70)
    public static void skullTwelveCellsAndNoOrphans(GameTestHelper test){
        var head=(StratimBirdHeadBlock)StratimBirdContent.HEAD.get();
        BlockPos base=new BlockPos(5,2,5);
        Direction face=Direction.EAST;
        var state=head.defaultBlockState().setValue(StratimBirdHeadBlock.FACING,face);
        for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){
            BlockPos p=cell(base,face,x,y,z);
            test.setBlock(p,state.setValue(StratimBirdHeadBlock.SIDE,x)
                .setValue(StratimBirdHeadBlock.LAYER,y).setValue(StratimBirdHeadBlock.DEPTH,z));
            test.assertTrue(test.getBlockState(p).is(head),"Missing actual skull cell");
        }
        test.assertTrue(test.getLevel().getBlockEntity(test.absolutePos(base))
            instanceof StratimBirdHeadBlock.Tile,"No skull owner tile");
        test.getLevel().destroyBlock(test.absolutePos(cell(base,face,1,1,2)),true);
        test.runAfterDelay(3,()->{
            for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++)
                test.assertTrue(test.getBlockState(cell(base,face,x,y,z)).isAir(),
                    "Orphan skull voxel at "+x+","+y+","+z);
            test.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=75)
    public static void auxiliarySkullBreakReturnsOneTrophy(GameTestHelper test){
        var head=(StratimBirdHeadBlock)StratimBirdContent.HEAD.get();
        BlockPos base=new BlockPos(5,2,5);
        Direction face=Direction.EAST;
        var state=head.defaultBlockState().setValue(StratimBirdHeadBlock.FACING,face);
        for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){
            BlockPos p=cell(base,face,x,y,z);
            test.setBlock(p,state.setValue(StratimBirdHeadBlock.SIDE,x)
                .setValue(StratimBirdHeadBlock.LAYER,y).setValue(StratimBirdHeadBlock.DEPTH,z));
        }
        test.getLevel().destroyBlock(test.absolutePos(cell(base,face,1,1,2)),true);
        test.runAfterDelay(3,()->{
            var area=new net.minecraft.world.phys.AABB(test.absolutePos(base)).inflate(8);
            int dropped=0;
            for(var item:test.getLevel().getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,area)){
                if(item.getItem().is(StratimBirdContent.HEAD_ITEM.get()))
                    dropped+=item.getItem().getCount();
            }
            test.assertTrue(dropped==1,
                "Breaking a skull auxiliary cell must drop exactly one head, got "+dropped);
            test.succeed();
        });
    }

    @GameTest(template="empty",timeoutTicks=55)
    public static void flightWingSamplesRotateWithHeading(GameTestHelper test){
        var center=new net.minecraft.world.phys.Vec3(0.5,64,0.5);
        var northSouth=StratimBirdEntity.wingSample(center,
            new net.minecraft.world.phys.Vec3(0,0,1),1,16,2);
        var eastWest=StratimBirdEntity.wingSample(center,
            new net.minecraft.world.phys.Vec3(1,0,0),1,16,2);
        test.assertTrue(northSouth.getX()==-16&&northSouth.getZ()==0,
            "North/south travel must check wing clearance along X");
        test.assertTrue(eastWest.getX()==0&&eastWest.getZ()==16,
            "East/west travel must check wing clearance along Z");
        test.assertTrue(northSouth.getY()==66&&eastWest.getY()==66,
            "Wing sample height must be preserved");
        test.succeed();
    }

}
