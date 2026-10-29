package org.slavicmyths.hellqa;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.netherstructures.*;
import org.slavicmyths.worldgen.StructureCandidates;

@GameTestHolder("slavicmyths_hellqa")
@PrefixGameTestTemplate(false)
public final class TriznaPyreQaTests {
    private static void check(boolean ok,String s){if(!ok)throw new GameTestAssertException(s);}
    @GameTest(template="empty")
    public static void sixteenGeometryProfiles(GameTestHelper t) {
        var rows=new StringBuilder("variant\tx\ty\tz\tblockstate\n");
        int[] fires=new int[4];int[] solids=new int[4];
        for(int v=0;v<4;v++)for(var rot:Rotation.values()) {
            var p=new TriznaPyrePiece(new TriznaPyreTerrain.Plan(new BlockPos(8,64,8),rot,v,103L));
            int chests=0,campfires=0,lit=0;
            for(int y=0;y<=8;y++)for(int x=0;x<15;x++)for(int z=0;z<15;z++) {
                var state=p.voxel(x,y,z);if(state==null)continue;
                check(p.getBoundingBox().isInside(p.at(x,y,z)),"Volume escapes chunk");
                if(state.is(Blocks.CHEST))chests++;
                if(state.is(Blocks.CAMPFIRE)){campfires++;if(state.getValue(CampfireBlock.LIT))lit++;}
                if(rot==Rotation.NONE){rows.append(v).append('\t').append(x).append('\t').append(y).append('\t').append(z).append('\t').append(state).append('\n');if(!state.isAir())solids[v]++;}
                if(state.is(Blocks.LANTERN))check(p.voxel(x,y+1,z).is(Blocks.CHAIN)&&p.voxel(x,y+2,z).is(Blocks.CRIMSON_PLANKS),"Floating lantern");
                if(state.is(Blocks.RED_WALL_BANNER))check(!p.voxel(x,y,z+1).isAir(),"Unsupported banner");
                if(state.is(Blocks.CAMPFIRE)||state.is(Blocks.MAGMA_BLOCK))check(p.voxel(x,y-1,z).isSolid(),"Unstable fire platform");
            }
            check(chests==1&&campfires==5,"Must have one chest and five real campfires");
            check(lit==(v<2?5:v==2?3:0),"Wrong lit variant");fires[v]=lit;
            check(p.voxel(11,2,8).isAir()&&p.voxel(10,1,8).isAir(),"Chest inaccessible");
            for(int z=11;z<=14;z++)for(int x=6;x<=8;x++)check(p.voxel(x,1,z).isAir()&&p.voxel(x,2,z).isAir(),"Blocked south entry");
        }
        check(solids[0]>solids[1]&&solids[1]>solids[2]&&solids[2]>solids[3],"Architecture must differ by damage");
        try{var dir=java.nio.file.Path.of("qa-exports");java.nio.file.Files.createDirectories(dir);java.nio.file.Files.writeString(dir.resolve("trizna-pyre.tsv"),rows.toString());}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        System.out.println("HELLQA_PASS 2.1.6 16 geometry profiles solids="+Arrays.toString(solids)+" lit="+Arrays.toString(fires));t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=1200)
    public static void physicalPlacementAndNoReroll(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);var center=new BlockPos(300008,72,300008);var cp=new ChunkPos(center);w.getChunk(cp.x,cp.z);
        var clip=new BoundingBox(cp.getMinBlockX(),0,cp.getMinBlockZ(),cp.getMaxBlockX(),127,cp.getMaxBlockZ());
        for(int v=0;v<4;v++)for(var rot:Rotation.values()) {
            var p=new TriznaPyrePiece(new TriznaPyreTerrain.Plan(center,rot,v,103L));
            for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=-1;y<=8;y++)w.setBlock(p.at(x,y,z),(y<0?Blocks.NETHERRACK:Blocks.AIR).defaultBlockState(),2);
            var blocked=p.at(7,0,7);
            for(var obstacle:new net.minecraft.world.level.block.state.BlockState[]{Blocks.CHEST.defaultBlockState(),Blocks.LAVA.defaultBlockState(),Blocks.OBSIDIAN.defaultBlockState(),Blocks.BEDROCK.defaultBlockState()}) {
                w.setBlock(blocked,obstacle,2);check(!p.validSite(w,clip),"Unsafe obstacle accepted");
                p.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),RandomSource.create(1),clip,cp,center);
                check(w.getBlockState(blocked).equals(obstacle)&&w.isEmptyBlock(p.at(11,1,8)),"Unsafe rejection wrote chest");w.setBlock(blocked,Blocks.AIR.defaultBlockState(),2);
            }
            w.setBlock(p.at(3,-1,3),Blocks.LAVA.defaultBlockState(),2);check(!p.validSite(w,clip),"Lava under support accepted");w.setBlock(p.at(3,-1,3),Blocks.NETHERRACK.defaultBlockState(),2);
            w.setBlock(p.at(7,7,7),Blocks.NETHERRACK.defaultBlockState(),2);check(!p.validSite(w,clip),"Low ceiling accepted");w.setBlock(p.at(7,7,7),Blocks.AIR.defaultBlockState(),2);
            w.setBlock(p.at(7,-1,7),Blocks.AIR.defaultBlockState(),2);check(!p.validSite(w,clip),"Hollow footing accepted");w.setBlock(p.at(7,-1,7),Blocks.NETHERRACK.defaultBlockState(),2);
            check(p.validSite(w,clip),"Safe fixture rejected");p.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),RandomSource.create(1),clip,cp,center);
            var chest=(ChestBlockEntity)w.getBlockEntity(p.at(11,1,8));check(chest!=null,"No actual chest");chest.unpackLootTable(null);chest.clearContent();
            var restored=new TriznaPyrePiece(p.createTag(StructurePieceSerializationContext.fromLevel(w)));restored.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),RandomSource.create(1),clip,cp,center);
            check(chest.isEmpty()&&chest.getLootTable()==null,"Replay refilled chest");
            for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=0;y<=8;y++){var expected=p.voxel(x,y,z);if(expected!=null)check(w.getBlockState(p.at(x,y,z)).is(expected.getBlock()),"Physical geometry mismatch");}
        }
        System.out.println("HELLQA_PASS 2.1.6 16 actual placements/replays; foreign chest/lava/obsidian/bedrock reject zero writes; lava below rejected; restart NOT_RUN");t.succeed();
    }
    private static int count(ChestBlockEntity c,Item item){int n=0;for(int i=0;i<27;i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    @GameTest(template="empty",timeoutTicks=1200)
    public static void nativeLootTenThousand(GameTestHelper t) {
        var w=t.getLevel();var pos=t.absolutePos(new BlockPos(2,2,2));w.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);
        var chest=(ChestBlockEntity)w.getBlockEntity(pos);
        Item[] optional={Items.ROTTEN_FLESH,Items.FIRE_CHARGE,Items.GOLD_NUGGET,Items.SOUL_SOIL,Items.BLAZE_POWDER,Items.SKELETON_SKULL,w.registryAccess().registryOrThrow(Registries.ITEM).get(TriznaPyreWorldgen.id("rune_heat"))};
        int[] found=new int[7];double[] chance={.45,.30,.22,.15,.08,.02,.03};
        for(int roll=0;roll<10000;roll++) {
            chest.clearContent();chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,TriznaPyreWorldgen.id("chests/trizna_pyre")),21016021L+roll);chest.unpackLootTable(null);
            check(count(chest,Items.BONE)>=2&&count(chest,Items.BONE)<=6&&count(chest,Items.CHARCOAL)>=2&&count(chest,Items.CHARCOAL)<=5,"Guaranteed baseline missing");
            for(int i=0;i<7;i++)if(count(chest,optional[i])>0)found[i]++;
            chest.clearContent();chest.unpackLootTable(null);check(chest.isEmpty(),"Repeated open rerolled");
        }
        for(int i=0;i<7;i++)check(Math.abs(found[i]-chance[i]*10000)<200,"Loot probability anomalous "+Arrays.toString(found));
        System.out.println("HELLQA_PASS 2.1.6 10000 actual native loot rolls optional="+Arrays.toString(found)+" no reroll");
        w.removeBlock(pos,false);t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=2400)
    public static void actualNaturalTriznaPyre(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var structure=(TriznaPyreStructure)w.registryAccess().registryOrThrow(Registries.STRUCTURE).get(TriznaPyreWorldgen.id("trizna_pyre"));
        check(structure.biomes().size()==9,"Expected all nine Nether biomes");
        var overworld=t.getLevel().getServer().overworld();var overworldGenerator=overworld.getChunkSource().getGenerator();
        var forbidden=new Structure.GenerationContext(overworld.registryAccess(),overworldGenerator,overworldGenerator.getBiomeSource(),overworld.getChunkSource().randomState(),overworld.getStructureManager(),overworld.getSeed(),new ChunkPos(0,0),overworld,structure.biomes()::contains);
        check(structure.findGenerationPoint(forbidden).isEmpty(),"Pyre accepted forbidden overworld dimension/biome");
        var placement=StructureCandidates.placement(w,structure);check(placement!=null,"Missing shrine set");
        int candidates=0,noise=0,rejected=0;
        for(int ring=0;ring<=8;ring++)for(int gx=-ring;gx<=ring;gx++)for(int gz=-ring;gz<=ring;gz++) {
            if(Math.max(Math.abs(gx),Math.abs(gz))!=ring)continue;
            var cp=placement.getPotentialStructureChunk(w.getSeed(),gx*placement.spacing(),gz*placement.spacing());
            if(!placement.applyAdditionalChunkRestrictions(cp.x,cp.z,w.getSeed()))continue;candidates++;
            var g=w.getChunkSource().getGenerator();
            var context=new Structure.GenerationContext(w.registryAccess(),g,g.getBiomeSource(),w.getChunkSource().randomState(),w.getStructureManager(),w.getSeed(),cp,w,structure.biomes()::contains);
            if(structure.findGenerationPoint(context).isEmpty())continue;noise++;
            var chunk=w.getChunk(cp.x,cp.z);var start=chunk.getStartForStructure(structure);
            check(start!=null,"No explicit native result");if(!start.isValid()){rejected++;continue;}
            var piece=(TriznaPyrePiece)start.getPieces().getFirst();
            int chests=0;
            for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=0;y<=8;y++) {
                var expected=piece.voxel(x,y,z);if(expected==null)continue;
                var actual=w.getBlockState(piece.at(x,y,z));
                check(expected.isAir()?actual.isAir():actual.is(expected.getBlock()),"Native physical shrine mismatch "+piece.at(x,y,z)+" expected="+expected+" actual="+actual);
                if(actual.is(Blocks.CHEST))chests++;
            }
            check(chests==1&&w.getBlockEntity(piece.at(11,1,8)) instanceof ChestBlockEntity,"No physical single chest");
            System.out.println("HELLQA_PASS 2.1.6 ACTUAL NATURAL SHRINE seed="+w.getSeed()+" chunk="+cp+" biome="+w.getBiome(piece.at(7,0,7)).unwrapKey().orElseThrow().location()+" variant="+piece.variant()+" candidates="+candidates+" noiseProposals="+noise+" lateRejected="+rejected+" bbox="+piece.getBoundingBox());
            t.succeed();return;
        }
        throw new GameTestAssertException("No physical natural shrine seed="+w.getSeed()+" candidates="+candidates+" proposals="+noise+" rejected="+rejected);
    }
}
