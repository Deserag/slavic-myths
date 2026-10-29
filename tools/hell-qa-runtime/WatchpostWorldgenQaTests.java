package org.slavicmyths.hellqa;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.netherstructures.NetherWatchpostStructure;
import org.slavicmyths.netherstructures.NetherWatchpostWorldgen;
import org.slavicmyths.worldgen.StructureCandidates;

/** One actual NEW natural Nether chunk, separate from /locate and direct/manual placement. */
@GameTestHolder("slavicmyths_hellqa")
@PrefixGameTestTemplate(false)
public final class WatchpostWorldgenQaTests {
    private static void check(boolean ok,String what){if(!ok)throw new GameTestAssertException(what);}
    @GameTest(template="empty",timeoutTicks=1200)
    public static void actualNativeNetherWatchpostStart(GameTestHelper t) {
        var w=t.getLevel().getServer().getLevel(Level.NETHER);
        var r=w.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var registered=r.get(NetherWatchpostWorldgen.id("nether_watchpost"));
        check(registered instanceof NetherWatchpostStructure,"Missing native watchpost StructureType/registry");
        var structure=(NetherWatchpostStructure)registered;
        var placement=StructureCandidates.placement(w,structure);
        check(placement!=null,"Watchpost absent from native Nether structure set");
        var generator=w.getChunkSource().getGenerator();
        int seen=0,eligible=0;
        for(int ring=0;ring<=5;ring++)for(int gx=-ring;gx<=ring;gx++)for(int gz=-ring;gz<=ring;gz++) {
            if(Math.max(Math.abs(gx),Math.abs(gz))!=ring)continue;
            ChunkPos candidate=placement.getPotentialStructureChunk(w.getSeed(),gx*placement.spacing(),gz*placement.spacing());
            if(!placement.applyAdditionalChunkRestrictions(candidate.x,candidate.z,w.getSeed()))continue;
            seen++;
            var c=new Structure.GenerationContext(w.registryAccess(),generator,generator.getBiomeSource(),
                    w.getChunkSource().randomState(),w.getStructureManager(),w.getSeed(),candidate,w,structure.biomes()::contains);
            if(structure.findGenerationPoint(c).isEmpty())continue;
            eligible++;
            // This is a real native generated NEW chunk, not a manual /give, /setblock or direct postProcess.
            var chunk=w.getChunk(candidate.x,candidate.z);
            var start=chunk.getAllStarts().get(structure);
            check(start!=null&&start.isValid(),"Planner accepted but actual natural chunk lacks watchpost start at "+candidate);
            int chestCount=0,ladderCount=0;
            var bounds=start.getBoundingBox();
            for(int x=bounds.minX();x<=bounds.maxX();x++)
                for(int z=bounds.minZ();z<=bounds.maxZ();z++)
                    for(int y=bounds.minY();y<=bounds.maxY();y++) {
                        var state=w.getBlockState(new BlockPos(x,y,z));
                        if(state.is(Blocks.CHEST))chestCount++;
                        if(state.is(Blocks.LADDER))ladderCount++;
                    }
            check(chestCount==1&&ladderCount>=6,
                    "Native generated start had wrong physical chest/ladder count "+chestCount+"/"+ladderCount+" at "+candidate);
            System.out.println("HELLQA_PASS 2.1.1 ACTUAL NATURAL WATCHPOST seed="+w.getSeed()+
                    " chunk="+candidate+" biome="+w.getBiome(new BlockPos(candidate.getMiddleBlockX(),bounds.minY()+4,candidate.getMiddleBlockZ())).unwrapKey().orElseThrow().location()+
                    " bounding="+bounds+" chest="+chestCount+" ladder="+ladderCount+
                    " candidateProbes="+seen+" acceptedPlans="+eligible+" (no direct placement or player GUI)");
            t.succeed();return;
        }
        throw new GameTestAssertException("No accepted native watchpost in bounded 11x11 placement-cell search; seed="+w.getSeed()+" candidates="+seen+" accepted="+eligible);
    }
}
