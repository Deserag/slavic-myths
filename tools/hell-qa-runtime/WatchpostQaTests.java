package org.slavicmyths.hellqa;

import org.slavicmyths.netherstructures.NetherWatchpostPiece;
import org.slavicmyths.netherstructures.NetherWatchpostWorldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Bounded first-checkpoint geometric and native-loot tests; not natural-worldgen acceptance. */
@GameTestHolder("slavicmyths_hellqa")
@PrefixGameTestTemplate(false)
public final class WatchpostQaTests {
    private static void check(boolean ok, String message) {
        if (!ok) throw new GameTestAssertException(message);
    }

    @GameTest(template="empty")
    public static void nativeVariantsLadderAndOneChest(GameTestHelper t) {
        int profiles = 0;
        for (int sections = 1; sections <= 4; sections++) for (Rotation rot : Rotation.values())
            for (boolean top : new boolean[]{false, true}) for (int ruins = 0; ruins <= 2; ruins++) {
                var piece = new NetherWatchpostPiece(new BlockPos(8,64,8), sections, rot, top, ruins, 4419L);
                int deck = piece.deck();
                int chests = 0, fires = 0, lanterns = 0;
                for (int y = -4; y <= deck+4; y++) for (int x=-5; x<=5; x++) for (int z=-5; z<=5; z++) {
                    BlockState s = piece.state(x,y,z);
                    if (s == null) continue;
                    if (s.is(Blocks.CHEST)) chests++;
                    if (s.is(Blocks.CAMPFIRE)) fires++;
                    if (s.is(Blocks.LANTERN)) lanterns++;
                    if (x==0 && z==0 && y>=1 && y<=deck+1)
                        check(s.is(Blocks.LADDER), "Broken central ladder at " + y + " variant=" + sections);
                }
                check(chests==1 && fires==1 && lanterns==1, "Missing/duplicated functional blocks");
                check(piece.state(2,top?deck+1:1,2).is(Blocks.CHEST), "Chest not reachable at expected floor");
                check(piece.state(2,(top?deck+1:1)+1,2).isAir(), "Chest cannot open");
                check(piece.state(-4,deck,0).is(Blocks.POLISHED_BLACKSTONE_BRICKS), "Deck gap");
                check(piece.getBoundingBox().minX()>=0 && piece.getBoundingBox().maxX()<16 &&
                        piece.getBoundingBox().minZ()>=0 && piece.getBoundingBox().maxZ()<16,
                        "Out of owning chunk footprint");
                profiles++;
            }
        check(profiles==96,"Unexpected profile count "+profiles);
        System.out.println("HELLQA_PASS 2.1.1 watchpost 96 geometry profiles: 4 heights,4 rotations,2 chest positions,3 ruin levels,1 chest/fire/lantern,continuous ladder and chunk bounds");
        t.succeed();
    }

    @GameTest(template="empty")
    public static void nativeWatchpostLoot1000(GameTestHelper t) {
        var world = t.getLevel();
        var pos = t.absolutePos(new BlockPos(2,2,2));
        world.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        check(world.getBlockEntity(pos) instanceof ChestBlockEntity, "No chest tile");
        var chest = (ChestBlockEntity) world.getBlockEntity(pos);
        var key = ResourceKey.create(Registries.LOOT_TABLE,NetherWatchpostWorldgen.id("chests/nether_watchpost"));
        int runs=1000, food=0, arrows=0, diamonds=0, boneArrows=0;
        for(int i=0;i<runs;i++){
            chest.clearContent();
            chest.setLootTable(key,21011021L+i);
            chest.unpackLootTable(null);
            int arrowCount=0,foodCount=0,diamondCount=0;
            for(int slot=0;slot<chest.getContainerSize();slot++){
                var s=chest.getItem(slot);
                if(s.is(Items.ARROW))arrowCount+=s.getCount();
                if(s.is(Items.BREAD)||s.is(Items.COOKED_BEEF))foodCount+=s.getCount();
                if(s.is(Items.DIAMOND_SWORD)||s.is(Items.DIAMOND_HELMET))diamondCount+=s.getCount();
                if(s.is(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(NetherWatchpostWorldgen.id("bone_arrow"))))boneArrows+=s.getCount();
            }
            check(arrowCount>=4&&arrowCount<=12,"Watchpost arrows absent seed "+i+": "+arrowCount);
            check(foodCount>=2&&foodCount<=5,"Watchpost food absent seed "+i+": "+foodCount);
            check(diamondCount<=1,"Watchpost diamond cap exceeded seed "+i);
            if(arrowCount>0)arrows++;
            if(foodCount>0)food++;
            if(diamondCount>0)diamonds++;
        }
        check(food==runs&&arrows==runs&&boneArrows>0&&diamonds>0,
                "Optional/guaranteed loot not functional: arrow/food="+arrows+"/"+food+" bone="+boneArrows+" diamonds="+diamonds);
        world.removeBlock(pos,false);
        System.out.println("HELLQA_PASS 2.1.1 1000 native loot unpack operations: guaranteed food/arrow, rare cap, actual bone_arrow, diamond occurrences="+diamonds);
        t.succeed();
    }
}
