package org.slavicmyths.hellqa;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.netherstructures.*;

/** Packaged native 2.1.5 checks: three real authored voxel sets, four rotations, chest loot. */
@GameTestHolder("slavicmyths_hellqa")
@PrefixGameTestTemplate(false)
public final class AncestorShrineQaTests {
    private static void check(boolean good,String what){if(!good)throw new GameTestAssertException(what);}
    @GameTest(template="empty")
    public static void exactVoxelGeometryTwelveProfiles(GameTestHelper t){
        int[] expected={644,604,370};
        for(int variant=0;variant<3;variant++)for(Rotation rotation:Rotation.values()){
            var piece=new AncestorShrinePiece(
                new AncestorShrineTerrain.Plan(new BlockPos(8,69,8),rotation,variant,123456L));
            int n=0,chest=0,lamps=0,chains=0,memorial=0;
            Set<BlockPos> rotated=new HashSet<>();
            for(int y=0;y<11;y++)for(int x=0;x<13;x++)for(int z=0;z<15;z++){
                var state=piece.voxel(x,y,z);
                if(state==null)continue;
                n++;
                check(rotated.add(piece.at(x,y,z)),"Overlapping rotated coordinates");
                if(state.is(Blocks.CHEST))chest++;
                if(state.is(Blocks.SOUL_LANTERN)){
                    lamps++;
                    check(state.getValue(LanternBlock.HANGING),"Soul lantern must hang");
                }
                if(state.is(Blocks.CHAIN))chains++;
                if(state.is(Blocks.CHISELED_POLISHED_BLACKSTONE))memorial++;
            }
            check(n==expected[variant],"TSV block count changed "+variant+" observed="+n);
            check(chest==1&&lamps==2&&chains==2&&memorial==4,
                    "Missing shrine memorial/chest/lamps");
            check(piece.voxel(8,2,5).is(Blocks.CHEST),"Chest coord changed");
            check(piece.voxel(8,3,5)==null,"Chest has blocked lid");
            for(int y=2;y<=3;y++)for(int z=12;z<=14;z++)
                check(piece.voxel(6,y,z)==null,"Central entrance obstructed");
            var box=piece.getBoundingBox();
            check(box.minX()>>4==0&&box.maxX()>>4==0&&box.minZ()>>4==0&&box.maxZ()>>4==0,
                    "Structure must fit its one owning chunk");
        }
        System.out.println("HELLQA_PASS 2.1.5 12 native geometry profiles: A644/B604/C370, one chest, open entrance/lid, lamps and memory wall");
        t.succeed();
    }
    private static int number(ChestBlockEntity chest,net.minecraft.world.item.Item item){
        int n=0;
        for(int i=0;i<chest.getContainerSize();i++)
            if(chest.getItem(i).is(item))n+=chest.getItem(i).getCount();
        return n;
    }
    @GameTest(template="empty")
    public static void chestLoot1000AndNoDuplication(GameTestHelper t){
        var w=t.getLevel();
        var pos=t.absolutePos(new BlockPos(2,2,2));
        w.setBlock(pos,Blocks.CHEST.defaultBlockState(),3);
        check(w.getBlockEntity(pos) instanceof ChestBlockEntity,"Failed to place chest");
        var chest=(ChestBlockEntity)w.getBlockEntity(pos);
        var key=ResourceKey.create(Registries.LOOT_TABLE,AncestorShrineWorldgen.id("chests/ancestor_shrine"));
        int extras=0;
        for(int i=0;i<1000;i++){
            chest.clearContent();
            chest.setLootTable(key,21015021L+i);
            chest.unpackLootTable(null);
            int bones=number(chest,net.minecraft.world.item.Items.BONE);
            check(bones>=1&&bones<=4,"No guaranteed bones "+i);
            check(number(chest,net.minecraft.world.item.Items.DIAMOND)==0,"Premium reward leaked");
            if(number(chest,net.minecraft.world.item.Items.SOUL_TORCH)>0)extras++;
            int before=0;
            for(int slot=0;slot<chest.getContainerSize();slot++)before+=chest.getItem(slot).getCount();
            chest.unpackLootTable(null);
            int after=0;
            for(int slot=0;slot<chest.getContainerSize();slot++)after+=chest.getItem(slot).getCount();
            check(before==after&&chest.getLootTable()==null,"Reroll on repeat-open "+i);
        }
        check(extras>350&&extras<750,"Optional soul torches probability anomalous: "+extras);
        System.out.println("HELLQA_PASS 2.1.5 1000 native loot rolls, guaranteed bones, no reroll, soul-torch-containing="+extras);
        w.removeBlock(pos,false);
        t.succeed();
    }
}
