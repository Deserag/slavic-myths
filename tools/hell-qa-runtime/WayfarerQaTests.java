package org.slavicmyths.hellqa;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.netherstructures.*;
import org.slavicmyths.worldgen.StructureCandidates;

/** 2.1.3 production geometry, unpacked vanilla chest loot, and an actual NEW Nether chunk. */
@GameTestHolder("slavicmyths_hellqa")
@PrefixGameTestTemplate(false)
public final class WayfarerQaTests {
    private static void check(boolean yes,String what){if(!yes)throw new GameTestAssertException(what);}
    @GameTest(template="empty")
    public static void twelveRotationRuinProfiles(GameTestHelper t) {
        int count=0;
        for(int variant=0;variant<3;variant++)for(Rotation rotation:Rotation.values()) {
            var plan=new WayfarerTerrain.Plan(new BlockPos(8,65,8),rotation,variant,true,102901L);
            var piece=new WayfarerPiece(plan);
            int chests=0,crafting=0,fires=0,lanterns=0,chains=0,stems=0;
            for(int x=0;x<9;x++)for(int z=0;z<7;z++)for(int y=0;y<6;y++) {
                var state=piece.voxel(x,y,z);
                if(state==null)continue;
                if(state.is(Blocks.CHEST))chests++;
                if(state.is(Blocks.CRAFTING_TABLE))crafting++;
                if(state.is(Blocks.CAMPFIRE))fires++;
                if(state.is(Blocks.LANTERN))lanterns++;
                if(state.is(Blocks.CHAIN))chains++;
                if(state.is(Blocks.CRIMSON_STEM))stems++;
            }
            check(chests==1&&crafting==1&&fires==1&&lanterns==1&&chains==1,
                  "Variant functional content duplicated or missing "+variant+"/"+rotation);
            check(stems>=16,"Four supporting posts broken");
            check(piece.voxel(1,4,4).is(Blocks.CRIMSON_PLANKS) &&
                  piece.voxel(1,3,4).is(Blocks.CHAIN) &&
                  piece.voxel(1,2,4).getValue(LanternBlock.HANGING),
                  "Lantern cantilever, chain and hanging flag are not connected");
            check(piece.voxel(4,0,5).is(Blocks.BLACKSTONE) &&
                  piece.voxel(4,1,5).is(Blocks.CAMPFIRE), "No fireproof campfire footing");
            check(piece.voxel(3,2,2).isAir(),"Chest headspace blocked");
            check(piece.voxel(2,0,4).isAir()&&piece.voxel(6,0,4).isAir(),
                  "Foot corridors must explicitly clear vegetation above natural rock");
            check(piece.voxel(2,1,4).isAir() && piece.voxel(2,2,4).isAir() &&
                  piece.voxel(6,1,4).isAir() && piece.voxel(6,2,4).isAir(),
                  "No safe two-wide bypass around the open campfire");
            check(piece.at(3,1,2).getY()==66,"Rotation changed elevation");
            var bounds=piece.getBoundingBox();
            check(bounds.minX()>=0&&bounds.maxX()<16&&bounds.minZ()>=0&&bounds.maxZ()<16,
                  "Piece unexpectedly crosses owning chunk");
            count++;
        }
        check(count==12,"Expected 12 ruin/rotation combinations");
        System.out.println("HELLQA_PASS 2.1.3 12 profiles; real cantilever, chain, hanging lamp, one fireproof campfire, chest headroom and bypass");
        t.succeed();
    }
    private static int amount(ChestBlockEntity chest,Item item) {
        int n=0;for(int s=0;s<chest.getContainerSize();s++)if(chest.getItem(s).is(item))n+=chest.getItem(s).getCount();
        return n;
    }
    @GameTest(template="empty")
    public static void nativeChestLoot1000AndNoReroll(GameTestHelper t) {
        var w=t.getLevel();
        var pos=t.absolutePos(new BlockPos(2,2,2));
        w.setBlock(pos,Blocks.CHEST.defaultBlockState(),3);
        check(w.getBlockEntity(pos) instanceof ChestBlockEntity,"Missing native chest BlockEntity");
        var chest=(ChestBlockEntity)w.getBlockEntity(pos);
        var key=ResourceKey.create(Registries.LOOT_TABLE,WayfarerWorldgen.id("chests/wayfarer_camp"));
        Item[] extra={Items.FLINT_AND_STEEL,Items.STRING,Items.IRON_INGOT,Items.GOLD_NUGGET,Items.OBSIDIAN,Items.GOLDEN_CARROT};
        int[] max={1,3,2,6,2,2};
        int[] observed=new int[extra.length];
        for(int i=0;i<1000;i++) {
            chest.clearContent();
            chest.setLootTable(key,20213013L+i);
            chest.unpackLootTable(null); // real built-in loot generation, not a synthetic RNG simulation
            int pork=amount(chest,Items.COOKED_PORKCHOP), bread=amount(chest,Items.BREAD),
                potato=amount(chest,Items.BAKED_POTATO);
            int foodGroups=(pork>0?1:0)+(bread>0?1:0)+(potato>0?1:0);
            check(foodGroups==1&&
                (pork==0||(pork>=2&&pork<=4))&&
                (bread==0||(bread>=2&&bread<=4))&&
                (potato==0||(potato>=3&&potato<=6)),
                "Guaranteed food group invalid at trial "+i);
            int torches=amount(chest,Items.TORCH),arrows=amount(chest,Items.ARROW),
                charge=amount(chest,Items.FIRE_CHARGE);
            check((torches>0?1:0)+(arrows>0?1:0)+(charge>0?1:0)==1&&
                (torches==0||(torches>=3&&torches<=7))&&
                (arrows==0||(arrows>=4&&arrows<=10))&&
                (charge==0||(charge>=1&&charge<=2)),
                "Guaranteed supplies group invalid at trial "+i);
            for(int j=0;j<extra.length;j++) {
                int qty=amount(chest,extra[j]);
                check(qty>=0&&qty<=max[j],"Optional group max or item invalid "+i+"/"+j);
                if(qty>0)observed[j]++;
            }
            check(amount(chest,Items.DIAMOND)==0&&amount(chest,Items.NETHERITE_INGOT)==0,
                  "Forbidden premium items");
            // Once opened the standard BE clears its deferred loot key; a repeated
            // open MUST NOT generate rewards a second time.
            int initial=0;
            for(int slot=0;slot<chest.getContainerSize();slot++)initial+=chest.getItem(slot).getCount();
            chest.unpackLootTable(null);
            int again=0;
            for(int slot=0;slot<chest.getContainerSize();slot++)again+=chest.getItem(slot).getCount();
            check(initial==again&&chest.getLootTable()==null,"Loot re-generated after opening "+i);
        }
        for(int j=0;j<observed.length;j++)check(observed[j]>0&&observed[j]<750,
                "Independent optional reward never/always occurs "+j+" seen="+observed[j]);
        w.removeBlock(pos,false);
        System.out.println("HELLQA_PASS 2.1.3 1000 real native chest loot generations, 100% two guarantee groups; six independent extras="+
                java.util.Arrays.toString(observed)+"; repeat-open no extra payout (save/restart still NOT_RUN)");
        t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=1200)
    public static void actualNativeNetherWayfarer(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var registered=w.registryAccess().registryOrThrow(Registries.STRUCTURE).get(WayfarerWorldgen.id("wayfarer_camp"));
        check(registered instanceof WayfarerStructure,"Missing native wayfarer structure registry");
        var structure=(WayfarerStructure)registered;
        var placement=StructureCandidates.placement(w,structure);
        check(placement!=null,"Missing wayfarer structure_set");
        var generator=w.getChunkSource().getGenerator();
        int sampled=0,accepted=0,rejected=0;
        var rejectedChunks=new java.util.ArrayList<ChunkPos>();
        for(int ring=0;ring<=6;ring++)for(int gx=-ring;gx<=ring;gx++)for(int gz=-ring;gz<=ring;gz++) {
            if(Math.max(Math.abs(gx),Math.abs(gz))!=ring)continue;
            ChunkPos cp=placement.getPotentialStructureChunk(w.getSeed(),gx*placement.spacing(),gz*placement.spacing());
            if(!placement.applyAdditionalChunkRestrictions(cp.x,cp.z,w.getSeed()))continue;
            sampled++;
            var c=new Structure.GenerationContext(w.registryAccess(),generator,generator.getBiomeSource(),
                w.getChunkSource().randomState(),w.getStructureManager(),w.getSeed(),cp,
                w,structure.biomes()::contains);
            if(structure.findGenerationPoint(c).isEmpty())continue;
            accepted++;
            var chunk=w.getChunk(cp.x,cp.z);
            var start=chunk.getAllStarts().get(structure);
            check(start!=null,"Noise proposal vanished without explicit invalid start at "+cp);
            if(!start.isValid()) {
                rejected++;
                check(start==net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START,
                    "Unsafe site did not remove its phantom native start at "+cp);
                rejectedChunks.add(cp);
                var serialized=start.createTag(
                    net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(w),cp);
                check(serialized.getString("id").equals("INVALID"),"Rejected start would persist as a real structure");
                System.out.println("HELLQA_WAYFARER_SAFE_REJECTION seed="+w.getSeed()+" chunk="+cp+
                    " invalid native start excluded from locate; NOT a physical placement");
                continue;
            }
            var piece=start.getPieces().stream().filter(WayfarerPiece.class::isInstance)
                .map(WayfarerPiece.class::cast).findFirst().orElseThrow();
            var box=piece.getBoundingBox();
            int chests=0,fires=0,tables=0,chains=0,lanterns=0;
            for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)
                for(int y=box.minY();y<=box.maxY();y++) {
                    var state=w.getBlockState(new BlockPos(x,y,z));
                    if(state.is(Blocks.CHEST))chests++;
                    if(state.is(Blocks.CAMPFIRE))fires++;
                    if(state.is(Blocks.CRAFTING_TABLE))tables++;
                    if(state.is(Blocks.CHAIN))chains++;
                    if(state.is(Blocks.LANTERN))lanterns++;
                }
            check(chests==1&&fires==1&&tables==1&&lanterns==1&&chains==1,
                  "Natural footprint missing objects: chest/fire/table/chain/lantern="+
                  chests+"/"+fires+"/"+tables+"/"+chains+"/"+lanterns+" seed="+w.getSeed()+" "+cp);
            var cpChest=piece.at(3,1,2);
            check(w.getBlockEntity(cpChest) instanceof ChestBlockEntity,"Native chest BlockEntity missing");
            check(w.isEmptyBlock(cpChest.above()),"Natural chest cannot open");
            check(w.getBlockState(piece.at(4,0,5)).is(Blocks.BLACKSTONE),"Natural campfire has unsafe footing");
            for(int x=2;x<=6;x++)for(int z=2;z<=6;z++) {
                var floor=w.getBlockState(piece.at(x,-1,z));
                check(WayfarerTerrain.natural(floor),
                      "Natural wayfarer path floats above "+floor+" at "+piece.at(x,-1,z));
            }
            check(w.getBlockState(piece.at(1,4,4)).is(Blocks.CRIMSON_PLANKS) &&
                  w.getBlockState(piece.at(1,3,4)).is(Blocks.CHAIN),
                  "Physical lantern attachment absent");
            System.out.println("HELLQA_PASS 2.1.3 ACTUAL NATURAL WAYFARER seed="+w.getSeed()+
                  " chunk="+cp+" biome="+w.getBiome(piece.at(4,1,3)).unwrapKey().orElseThrow().location()+
                  " Y="+piece.at(4,0,3).getY()+" variant="+piece.variant()+" bbox="+box+
                  " sampledCandidates="+sampled+" acceptedPlans="+accepted+" lateRejectedStarts="+rejected+" physical chest/fire/table/chain/lantern each one");
            // Cache publication is deliberately server-thread-only, after the
            // synchronous chunk request has completed and yielded to the server.
            t.runAfterDelay(1,()->{
                for(var rejectedChunk:rejectedChunks)
                    check(w.structureManager().checkStructurePresence(rejectedChunk,structure,placement,false)==
                        net.minecraft.world.level.levelgen.structure.StructureCheckResult.START_NOT_PRESENT,
                        "Structure presence cache still advertises the rejected campsite at "+rejectedChunk);
                t.succeed();
            });return;
        }
        throw new GameTestAssertException("No accepted native wayfarer in bounded 13x13 sample seed="+
                w.getSeed()+" candidates="+sampled+" accepted="+accepted);
    }
}
