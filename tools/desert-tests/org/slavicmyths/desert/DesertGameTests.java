package org.slavicmyths.desert;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("slavicmyths_accessories278")
@PrefixGameTestTemplate(false)
public final class DesertGameTests {
    private static DesertSealBlock seal(String id) { return (DesertSealBlock)DesertBlocks.ALL.get(id).get(); }
    private static boolean open(GameTestHelper h,BlockPos p) {
        return h.getLevel().getBlockState(p).getValue(DesertSealBlock.OPEN);
    }
    @GameTest(template="empty")
    public static void connectedSeals(GameTestHelper h) {
        var w=h.getLevel();
        var solar=seal("solar_seal");var wind=seal("wind_seal");
        BlockPos a=h.absolutePos(new BlockPos(2,2,2)),b=a.east(),c=b.above(),d=c.west();
        BlockPos separate=c.east().east(),other=b.south();
        for(var p:new BlockPos[]{a,b,c,d,separate})w.setBlock(p,solar.defaultBlockState(),Block.UPDATE_ALL);
        w.setBlock(other,wind.defaultBlockState(),Block.UPDATE_ALL);
        h.assertTrue(solar.toggle(w,a,true)==DesertSealBlock.ToggleResult.OK,"opening rejected");
        for(var p:new BlockPos[]{a,b,c,d}){
            h.assertTrue(open(h,p),"looped network not opened");
            var state=w.getBlockState(p);
            h.assertTrue(state.getCollisionShape(w,p,CollisionContext.empty()).isEmpty(),"marker collides");
            h.assertTrue(!state.getShape(w,p,CollisionContext.empty()).isEmpty(),"marker cannot be selected");
        }
        h.assertTrue(!open(h,separate)&&!open(h,other),"disconnected or different seal changed");
        h.assertTrue(solar.toggle(w,c,false)==DesertSealBlock.ToggleResult.OK,"closing rejected");
        for(var p:new BlockPos[]{a,b,c,d})h.assertTrue(!open(h,p),"looped network not closed");
        h.assertTrue(!w.getBlockState(a).getCollisionShape(w,a,CollisionContext.empty()).isEmpty(),"closed seal not solid");
        w.destroyBlock(b,false);
        h.assertTrue(w.getBlockState(b).isAir(),"seal cannot be broken");
        w.setBlock(b,solar.defaultBlockState(),Block.UPDATE_ALL);
        h.assertTrue(!open(h,b),"replacement is not sealed");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void occupiedSealCannotClose(GameTestHelper h) {
        var w=h.getLevel();var seal=seal("desert_serpent_seal");
        BlockPos a=h.absolutePos(new BlockPos(2,2,2)),b=a.east();
        w.setBlock(a,seal.defaultBlockState().setValue(DesertSealBlock.OPEN,true),Block.UPDATE_ALL);
        w.setBlock(b,seal.defaultBlockState().setValue(DesertSealBlock.OPEN,true),Block.UPDATE_ALL);
        var pig=EntityType.PIG.create(w);
        if(pig==null)throw new IllegalStateException("Cannot create pig");
        pig.moveTo(new Vec3(b.getX()+.5,b.getY(),b.getZ()+.5));pig.setNoGravity(true);
        w.addFreshEntity(pig);
        h.assertTrue(seal.toggle(w,a,false)==DesertSealBlock.ToggleResult.OCCUPIED,"occupied passage closed");
        h.assertTrue(open(h,a)&&open(h,b),"partially closed network");
        pig.discard();
        h.assertTrue(seal.toggle(w,a,false)==DesertSealBlock.ToggleResult.OK,"empty passage rejected");
        h.succeed();
    }
    private DesertGameTests(){}
}
