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
public final class ForgottenShrineQaTests {
    private static void check(boolean ok,String s){if(!ok)throw new GameTestAssertException(s);}
    @GameTest(template="empty")
    public static void stairsAndRoomsSixteenProfiles(GameTestHelper t) {
        var rows=new StringBuilder("variant\tx\ty\tz\tblockstate\n");
        for(int v=0;v<4;v++)for(var rot:Rotation.values()) {
            var p=new ForgottenShrinePiece(new ForgottenShrineTerrain.Plan(new BlockPos(8,64,8),rot,v,103L));
            int chests=0;
            for(int y=p.bottom();y<=8;y++)for(int x=0;x<15;x++)for(int z=0;z<15;z++) {
                var s=p.voxel(x,y,z);if(s!=null&&s.is(Blocks.CHEST))chests++;
                if(rot==Rotation.NONE&&s!=null)rows.append(v).append('\t').append(x).append('\t').append(y).append('\t').append(z).append('\t').append(s).append('\n');
                check(p.getBoundingBox().isInside(p.at(x,y,z)),"Rotated volume escapes bounding box");
            }
            check(chests==1,"Exactly one chest for whole shrine");
            check(p.voxel(7,p.bottom()+2,5).isAir()&&p.voxel(7,p.bottom()+1,6).isAir(),"Chest lid or approach blocked");
            for(int z=9;z<=13;z++)for(int x=6;x<=8;x++) {
                int y=z-13;
                check(p.voxel(x,y,z).is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS),"Missing three-wide ordinary stair");
                check(p.voxel(x,y,z).getValue(StairBlock.FACING)==Direction.SOUTH,"Primary stair direction");
                check(p.voxel(x,y+1,z).isAir()&&p.voxel(x,y+2,z).isAir(),"Primary stair cuts sealed by ceiling");
            }
            if(p.bottom()==-10)for(int x=7;x<=11;x++)for(int z=3;z<=4;z++) {
                int y=x-16;
                check(p.voxel(x,y,z).is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS),"Missing two-wide second stair");
                check(p.voxel(x,y+1,z).isAir()&&p.voxel(x,y+2,z).isAir(),"Second stair intersects floor/ceiling");
            }
            for(int x:new int[]{4,10})if(!p.voxel(x,5,3).isAir()) {
                check(p.voxel(x,6,3).is(Blocks.CHAIN)&&p.voxel(x,7,3).is(Blocks.CRIMSON_PLANKS),"Floating lantern");
            }
        }
        try {
            var dir=java.nio.file.Path.of("qa-exports");java.nio.file.Files.createDirectories(dir);
            java.nio.file.Files.writeString(dir.resolve("forgotten-shrine.tsv"),rows.toString());
        } catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
        System.out.println("HELLQA_PASS 2.1.4 16 geometry profiles; 1/2 rooms, three-wide surface stair, two-wide second stair, clear lids and anchored lamps");
        t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=1200)
    public static void actualAtomicPlacementReplayAndProtection(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var center=new BlockPos(280008,72,280008);
        var cp=new ChunkPos(center);w.getChunk(cp.x,cp.z);
        var clip=new BoundingBox(cp.getMinBlockX(),0,cp.getMinBlockZ(),cp.getMaxBlockX(),127,cp.getMaxBlockZ());
        for(int v=0;v<4;v++)for(var rot:Rotation.values()) {
            var plan=new ForgottenShrineTerrain.Plan(center,rot,v,103L);
            var p=new ForgottenShrinePiece(plan);
            // Full actual host rock, with an open natural cave above. Test-owned chunk only.
            for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=-11;y<=8;y++)
                w.setBlock(p.at(x,y,z),(y<=0?Blocks.NETHERRACK:Blocks.AIR).defaultBlockState(),2);
            var protectedPos=p.at(4,1,4);
            w.setBlock(protectedPos,Blocks.CHEST.defaultBlockState(),2);
            check(!p.validSite(w,clip),"Foreign chest accepted");
            p.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),RandomSource.create(1),clip,cp,center);
            check(w.getBlockState(protectedPos).is(Blocks.CHEST)&&w.getBlockState(p.at(7,p.bottom(),7)).is(Blocks.NETHERRACK),"Rejected preflight partially wrote");
            w.setBlock(protectedPos,Blocks.AIR.defaultBlockState(),2);
            if(v==0&&rot==Rotation.NONE)for(var obstacle:new net.minecraft.world.level.block.state.BlockState[]{Blocks.LAVA.defaultBlockState(),Blocks.OBSIDIAN.defaultBlockState(),Blocks.BEDROCK.defaultBlockState()}) {
                w.setBlock(protectedPos,obstacle,2);
                check(!p.validSite(w,clip),"Unsafe lava/solid/bedrock accepted");
                p.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),RandomSource.create(1),clip,cp,center);
                check(w.getBlockState(protectedPos).equals(obstacle)&&w.getBlockState(p.at(7,p.bottom(),7)).is(Blocks.NETHERRACK),"Unsafe rejection wrote into host rock");
                w.setBlock(protectedPos,Blocks.AIR.defaultBlockState(),2);
            }
            check(p.validSite(w,clip),"Flat safe host rock rejected");
            p.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),RandomSource.create(1),clip,cp,center);
            var chest=(ChestBlockEntity)w.getBlockEntity(p.at(7,p.bottom()+1,5));
            check(chest!=null,"Physical chest missing");chest.unpackLootTable(null);chest.clearContent();
            var saved=p.createTag(StructurePieceSerializationContext.fromLevel(w));
            var restored=new ForgottenShrinePiece(saved);
            restored.postProcess(w,w.structureManager(),w.getChunkSource().getGenerator(),RandomSource.create(1),clip,cp,center);
            check(chest.isEmpty()&&chest.getLootTable()==null,"Serialized placed piece refilled an emptied chest");
            for(int z=9;z<=13;z++)for(int x=6;x<=8;x++) {
                var at=p.at(x,z-13,z);
                check(w.getBlockState(at).is(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)&&w.isEmptyBlock(at.above())&&w.isEmptyBlock(at.above(2)),"Physical stair passage obstructed");
            }
        }
        System.out.println("HELLQA_PASS 2.1.4 16 actual placement/replay fixtures; foreign chest rejection zero floor writes, ordinary physical stairs, persisted Placed prevents loot refill; true restart NOT_RUN");
        t.succeed();
    }
    private static int count(ChestBlockEntity c,Item item){int n=0;for(int i=0;i<27;i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    @GameTest(template="empty",timeoutTicks=1200)
    public static void nativeLootTenThousand(GameTestHelper t) {
        var w=t.getLevel();var pos=t.absolutePos(new BlockPos(2,2,2));w.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);
        var chest=(ChestBlockEntity)w.getBlockEntity(pos);
        String[][] ids={{"strength","speed","resilience","heat"},{"crushing","wind","blood","fortitude"},{"counterstrike_iii","hunter_iii","last_stand_iii","depths_iii"}};
        Item[][] items=new Item[3][4];
        for(int tier=0;tier<3;tier++)for(int j=0;j<4;j++) {
            items[tier][j]=w.registryAccess().registryOrThrow(Registries.ITEM).get(ForgottenShrineWorldgen.id("rune_"+ids[tier][j]));
            check(items[tier][j]!=Items.AIR,"Unregistered active rune");
        }
        int[] totals=new int[3];
        for(int roll=0;roll<10000;roll++) {
            chest.clearContent();chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,ForgottenShrineWorldgen.id("chests/forgotten_shrine")),21014021L+roll);chest.unpackLootTable(null);
            int[] n=new int[3];for(int tier=0;tier<3;tier++)for(var item:items[tier])n[tier]+=count(chest,item);
            check(n[0]>=2&&n[0]+n[1]+n[2]==3,"Guaranteed active runes invalid");
            check(count(chest,Items.BONE)>=2&&count(chest,Items.BONE)<=6&&count(chest,Items.ROTTEN_FLESH)>=2,"Base loot missing");
            for(int tier=0;tier<3;tier++)totals[tier]+=n[tier]-(tier==0?2:0);
            chest.clearContent();chest.unpackLootTable(null);check(chest.isEmpty(),"Repeated loot unpack refilled empty inventory");
        }
        check(totals[1]>1200&&totals[1]<1800&&totals[2]>50&&totals[2]<160,"Third-rune baseline distribution anomalous "+Arrays.toString(totals));
        System.out.println("HELLQA_PASS 2.1.4 10000 real native chest rolls; 2 active I + third tiers="+Arrays.toString(totals)+"; no blank runes, no reroll");
        w.removeBlock(pos,false);t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=2400)
    public static void actualNaturalForgottenShrine(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var structure=(ForgottenShrineStructure)w.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ForgottenShrineWorldgen.id("forgotten_shrine"));
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
            var piece=(ForgottenShrinePiece)start.getPieces().getFirst();
            int chests=0;
            for(int x=0;x<15;x++)for(int z=0;z<15;z++)for(int y=piece.bottom();y<=8;y++) {
                var expected=piece.voxel(x,y,z);if(expected==null)continue;
                var actual=w.getBlockState(piece.at(x,y,z));
                check(expected.isAir()?actual.isAir():actual.is(expected.getBlock()),"Native physical shrine mismatch "+piece.at(x,y,z)+" expected="+expected+" actual="+actual);
                if(actual.is(Blocks.CHEST))chests++;
            }
            check(chests==1&&w.getBlockEntity(piece.at(7,piece.bottom()+1,5)) instanceof ChestBlockEntity,"No physical single chest");
            System.out.println("HELLQA_PASS 2.1.4 ACTUAL NATURAL SHRINE seed="+w.getSeed()+" chunk="+cp+" biome="+w.getBiome(piece.at(7,0,7)).unwrapKey().orElseThrow().location()+" bottom="+piece.bottom()+" variant="+piece.variant()+" candidates="+candidates+" noiseProposals="+noise+" lateRejected="+rejected+" bbox="+piece.getBoundingBox());
            t.succeed();return;
        }
        throw new GameTestAssertException("No physical natural shrine seed="+w.getSeed()+" candidates="+candidates+" proposals="+noise+" rejected="+rejected);
    }
}
