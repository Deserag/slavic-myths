package org.slavicmyths.owl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.trophy.TrophyHeadBlock;
import org.slavicmyths.trophy.TrophyHeadDrops;
import org.slavicmyths.trophy.TrophyHeads;

@GameTestHolder("slavicmyths_marine_armor")
@PrefixGameTestTemplate(false)
public final class TrophyHeadRecoveryGameTests {
    private static void recover(GameTestHelper t,TrophyHeads.Kind kind,int cell,Direction direction){
        var world=t.getLevel();
        var block=(TrophyHeadBlock)TrophyHeads.BLOCKS.get(kind).get();
        var item=TrophyHeads.ITEMS.get(kind).get();
        BlockPos root=t.absolutePos(new BlockPos(5,2,5));
        var state=block.defaultBlockState().setValue(TrophyHeadBlock.FACING,direction);
        world.setBlock(root,state,3);
        block.setPlacedBy(world,root,state,null,new ItemStack(item));
        t.assertTrue(!state.requiresCorrectToolForDrops(),"Trophy requires an unrelated tool: "+kind);
        BlockPos target=kind==TrophyHeads.Kind.WINTER_OWL?TrophyHeadBlock.owlPos(root,direction,cell):
            kind==TrophyHeads.Kind.MOROVOY_RAVEN?root.relative(direction,cell):
            kind==TrophyHeads.Kind.MOROVOY_FIRE_ZMEY?root.relative(direction.getClockWise(),cell):root.above(cell);
        t.assertTrue(world.getBlockState(target).is(block),"Missing trophy cell: "+kind);
        var drops=Block.getDrops(state,world,root,world.getBlockEntity(root),null,ItemStack.EMPTY);
        t.assertTrue(drops.stream().filter(x->x.is(item)).mapToInt(ItemStack::getCount).sum()==1,"Anchor loot missing: "+kind);
        t.assertTrue(world.destroyBlock(target,true),"Cannot break trophy: "+kind);
        t.assertTrue(!world.getBlockState(root).is(block),"Root left behind: "+kind);
        if(kind==TrophyHeads.Kind.WINTER_OWL)for(int i=0;i<36;i++)t.assertTrue(!world.getBlockState(TrophyHeadBlock.owlPos(root,direction,i)).is(block),"Orphan owl cell "+i);
        var items=world.getEntitiesOfClass(ItemEntity.class,new AABB(root).inflate(5),e->e.getItem().is(item));
        t.assertTrue(items.stream().mapToInt(e->e.getItem().getCount()).sum()==1,"Expected exactly one recovered trophy: "+kind);
        ItemStack recovered=items.get(0).getItem().copy();
        for(var entity:items)entity.discard();

        // A recovered item must be placeable again, including all secondary cells.
        world.setBlock(root,state,3);
        block.setPlacedBy(world,root,state,null,recovered);
        t.assertTrue(world.getBlockState(root).is(block),"Recovered trophy cannot be placed: "+kind);
        t.assertTrue(world.getBlockState(target).is(block),"Recovered trophy lost a secondary cell: "+kind);
        t.assertTrue(world.destroyBlock(root,true),"Replaced trophy cannot be broken: "+kind);
        t.assertTrue(!world.getBlockState(target).is(block),"Replaced trophy left a secondary cell: "+kind);
        if(kind==TrophyHeads.Kind.WINTER_OWL)for(int i=0;i<36;i++)t.assertTrue(!world.getBlockState(TrophyHeadBlock.owlPos(root,direction,i)).is(block),"Replaced owl left cell "+i);
        var recoveredAgain=world.getEntitiesOfClass(ItemEntity.class,new AABB(root).inflate(5),e->e.getItem().is(item));
        t.assertTrue(recoveredAgain.stream().mapToInt(e->e.getItem().getCount()).sum()==1,"Repeated placement must return exactly one trophy: "+kind);
        for(var entity:recoveredAgain)entity.discard();
    }
    @GameTest(template="empty") public static void owlHeadNativeCellsRecover(GameTestHelper t){
        t.assertTrue(TrophyHeadDrops.kind("winter_owl",false,false)==TrophyHeads.Kind.WINTER_OWL,"Missing owl reward");
        t.assertTrue(TrophyHeadDrops.kind("winter_owl_spirit",false,false)==null,"Spirit must not drop boss trophy");
        for(Direction direction:new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}){
            recover(t,TrophyHeads.Kind.WINTER_OWL,35,direction);
        }
        recover(t,TrophyHeads.Kind.WINTER_OWL,0,Direction.NORTH);t.succeed();
    }
    @GameTest(template="empty") public static void olderTrophiesRecover(GameTestHelper t){
        recover(t,TrophyHeads.Kind.NORTH_WOLVERINE,0,Direction.NORTH);
        recover(t,TrophyHeads.Kind.LIKHO,1,Direction.NORTH);
        recover(t,TrophyHeads.Kind.MOROVOY_RAVEN,1,Direction.NORTH);
        recover(t,TrophyHeads.Kind.MOROVOY_RAVEN,0,Direction.NORTH);
        recover(t,TrophyHeads.Kind.MOROVOY_FIRE_ZMEY,1,Direction.NORTH);
        recover(t,TrophyHeads.Kind.MOROVOY_FIRE_ZMEY,0,Direction.NORTH);
        t.succeed();
    }
    @GameTest(template="empty") public static void everySharedHeadHasAnchorLoot(GameTestHelper t){
        var world=t.getLevel();var root=t.absolutePos(new BlockPos(4,2,4));
        for(var kind:TrophyHeads.Kind.values()){
            var block=TrophyHeads.BLOCKS.get(kind).get();var item=TrophyHeads.ITEMS.get(kind).get();
            var state=block.defaultBlockState();world.setBlock(root,state,3);
            var items=Block.getDrops(state,world,root,world.getBlockEntity(root),null,ItemStack.EMPTY);
            t.assertTrue(items.stream().filter(x->x.is(item)).mapToInt(ItemStack::getCount).sum()==1,"Missing anchor loot: "+kind);
            t.assertTrue(!state.requiresCorrectToolForDrops(),"Tool gate on trophy: "+kind);
            world.removeBlock(root,false);
        }t.succeed();
    }
    @GameTest(template="empty") public static void independentTrophyAnchorLoot(GameTestHelper t){
        var world=t.getLevel();var root=t.absolutePos(new BlockPos(3,2,3));
        for(String id:java.util.List.of("poloz_head","zhelva_head","bauk_head","deep_sea_serpent_head","ocean_zmey_head","stratim_bird_head","tsar_pike_head")){
            var key=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id);
            var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(key);
            t.assertTrue(block!=net.minecraft.world.level.block.Blocks.AIR,"Missing trophy registration: "+id);
            var state=block.defaultBlockState();world.setBlock(root,state,3);
            t.assertTrue(!state.requiresCorrectToolForDrops(),"Tool gate on independent trophy: "+id);
            var items=Block.getDrops(state,world,root,world.getBlockEntity(root),null,ItemStack.EMPTY);
            t.assertTrue(items.stream().filter(x->x.is(block.asItem())).mapToInt(ItemStack::getCount).sum()==1,"Missing loot: "+id);
            world.removeBlock(root,false);
        }t.succeed();
    }
}
