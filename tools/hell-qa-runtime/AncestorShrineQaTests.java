package org.slavicmyths.hellqa;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slavicmyths.worldgen.StructureCandidates;
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
    public static void deltaCannotReplaceCraftedFloor(GameTestHelper t) {
        var w=t.getLevel();var p=t.absolutePos(new BlockPos(8,3,8));
        var config=new net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration(
            Blocks.LAVA.defaultBlockState(),Blocks.BASALT.defaultBlockState(),
            net.minecraft.util.valueproviders.ConstantInt.of(3),net.minecraft.util.valueproviders.ConstantInt.of(1));
        var feature=new net.minecraft.world.level.levelgen.feature.DeltaFeature(
            net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration.CODEC);
        for(int control=0;control<2;control++) {
            for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++) {
                w.setBlock(p.offset(x,-1,z),Blocks.NETHERRACK.defaultBlockState(),2);
                w.setBlock(p.offset(x,0,z),(control==0?Blocks.NETHERRACK:Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState(),2);
                w.setBlock(p.offset(x,1,z),Blocks.AIR.defaultBlockState(),2);
            }
            feature.place(new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(
                java.util.Optional.empty(),w,w.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(22),p,config));
            int lava=0;
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++) {
                var state=w.getBlockState(p.offset(x,0,z));
                if(state.is(Blocks.LAVA))lava++;
                if(control==1)check(state.is(Blocks.POLISHED_BLACKSTONE_BRICKS),"Delta replaced authored floor with "+state);
            }
            check(control==0?lava>0:lava==0,"Delta fixture inactive or crafted floor unprotected");
        }
        System.out.println("HELLQA_PASS real vanilla DeltaFeature: natural control gets lava, crafted blackstone floors preserved; no natural basalt/blackstone tag protection");
        t.succeed();
    }
    @GameTest(template="empty")
    public static void basaltCannotGrowThroughCraftedFloor(GameTestHelper t) {
        var w=t.getLevel();var p=t.absolutePos(new BlockPos(8,3,8));
        var feature=new net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature(
            net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration.CODEC);
        var config=new net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration(
            net.minecraft.util.valueproviders.ConstantInt.of(2),net.minecraft.util.valueproviders.ConstantInt.of(4));
        for(int mode=0;mode<3;mode++) {
            for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)for(int y=-1;y<=6;y++)
                w.setBlock(p.offset(x,y,z),(y<0?Blocks.NETHERRACK:y==0?(mode==1||mode==2&&x>=0?Blocks.POLISHED_BLACKSTONE_BRICKS:Blocks.NETHERRACK):Blocks.AIR).defaultBlockState(),2);
            feature.place(new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(java.util.Optional.empty(),w,
                w.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(22),p.offset(mode==2?-1:0,1,0),config));
            int naturalBasalt=0,protectedBasalt=0;
            for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)for(int y=1;y<=6;y++)if(w.getBlockState(p.offset(x,y,z)).is(Blocks.BASALT)) {
                if(mode==1||mode==2&&x>=0)protectedBasalt++;else naturalBasalt++;
            }
            check(protectedBasalt==0,"Native basalt grew over crafted floor");
            if(mode!=1)check(naturalBasalt>0,"Natural basalt control was disabled");
        }
        System.out.println("HELLQA_PASS real native BasaltColumnsFeature: natural columns present; crafted floor and mixed neighbouring roof protected");
        t.succeed();
    }
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
    /**
     * Native worldgen, not a dev-create command. One new Nether chunk must contain
     * the ACTUAL authored blocks; an accepted plan with zero physical blocks is FAIL.
     * This is a bounded smoke, NOT the required five-seed statistical acceptance.
     */
    @GameTest(template="empty",timeoutTicks=1200)
    public static void actualNaturalAncestorShrine(GameTestHelper t){
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var registered=w.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(AncestorShrineWorldgen.id("ancestor_shrine"));
        check(registered instanceof AncestorShrineStructure,"2.1.5 registry missing");
        var structure=(AncestorShrineStructure)registered;
        var placement=StructureCandidates.placement(w,structure);
        check(placement!=null,"2.1.5 native structure_set absent");
        var generator=w.getChunkSource().getGenerator();
        int sampled=0,planned=0,rejected=0;
        for(int ring=0;ring<=12;ring++)for(int gx=-ring;gx<=ring;gx++)for(int gz=-ring;gz<=ring;gz++){
            if(Math.max(Math.abs(gx),Math.abs(gz))!=ring)continue;
            ChunkPos cp=placement.getPotentialStructureChunk(w.getSeed(),
                    gx*placement.spacing(),gz*placement.spacing());
            if(!placement.applyAdditionalChunkRestrictions(cp.x,cp.z,w.getSeed()))continue;
            sampled++;
            var c=new Structure.GenerationContext(w.registryAccess(),generator,generator.getBiomeSource(),
                    w.getChunkSource().randomState(),w.getStructureManager(),w.getSeed(),cp,
                    w,structure.biomes()::contains);
            if(structure.findGenerationPoint(c).isEmpty())continue;
            planned++;
            var chunk=w.getChunk(cp.x,cp.z);
            var start=chunk.getAllStarts().get(structure);
            check(start!=null,"No explicit native result "+cp);
            if(!start.isValid()) {
                rejected++;
                check(start==net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START,"Rejected shrine is still advertised");
                continue;
            }
            var piece=start.getPieces().stream().filter(AncestorShrinePiece.class::isInstance)
                    .map(AncestorShrinePiece.class::cast).findFirst().orElseThrow();
            var chest=piece.at(8,2,5);
            check(w.getBlockState(chest).is(Blocks.CHEST),
                    "Accepted ancestor shrine generated NO physical chest at "+chest);
            check(w.getBlockEntity(chest) instanceof ChestBlockEntity,
                    "Shrine chest has no native block entity "+chest);
            check(w.isEmptyBlock(chest.above()),"Shrine chest lid blocked "+chest);
            check(w.getBlockState(piece.at(6,1,10)).isSolid(),
                    "Shrine internal floor missing "+piece.at(6,1,10)+" actual="+w.getBlockState(piece.at(6,1,10))+" variant="+piece.variant());
            check(w.isEmptyBlock(piece.at(6,2,12))&&w.isEmptyBlock(piece.at(6,3,12)),
                    "Shrine main entrance not walkable lower="+w.getBlockState(piece.at(6,2,12))+" upper="+w.getBlockState(piece.at(6,3,12))+" variant="+piece.variant()+" chest="+chest+" bbox="+piece.getBoundingBox());
            System.out.println("HELLQA_PASS 2.1.5 ACTUAL NATURAL SHRINE seed="+w.getSeed()+
                    " chunk="+cp+" biome="+w.getBiome(piece.at(6,2,7)).unwrapKey().orElseThrow().location()+
                    " variant="+piece.variant()+" chest="+chest+" sampled="+sampled+
                    " accepted="+planned+" lateRejected="+rejected+" native start and physical blocks CONFIRMED");
            t.succeed();return;
        }
        throw new GameTestAssertException("2.1.5 NO accepted natural Nether shrine seed="+w.getSeed()+
                " sampledCandidates="+sampled+" acceptedPlans="+planned+
                " in bounded 25x25 placement-cell grid; geometry alone is not worldgen success");
    }

}
