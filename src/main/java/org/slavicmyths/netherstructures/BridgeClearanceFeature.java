package org.slavicmyths.netherstructures;

import java.util.HashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Final decoration pass: neighbouring fungal features can write across chunk borders
 * after a bridge's structure pass. Only fresh worldgen, only existing bridge decks,
 * only ambient flora; never rebuild roads, carve stone or run on loaded saves. */
public final class BridgeClearanceFeature extends Feature<NoneFeatureConfiguration> {
    public BridgeClearanceFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var world=context.level();
        var cp=new ChunkPos(context.origin());
        var structure=world.registryAccess().registryOrThrow(Registries.STRUCTURE)
            .get(NetherBridgeWorldgen.id("broken_nether_bridge"));
        if(structure==null)return false;
        var starts=new HashSet<Long>();
        // A fungus can extend eight blocks outside its decoration chunk. Clean
        // that same bounded region after its vegetation step, including neighbours.
        for(int cx=cp.x-1;cx<=cp.x+1;cx++)for(int cz=cp.z-1;cz<=cp.z+1;cz++) {
            var chunk=world.getChunk(cx,cz,ChunkStatus.STRUCTURE_REFERENCES,false);
            if(chunk!=null)for(long reference:chunk.getReferencesForStructure(structure))starts.add(reference);
        }
        boolean changed=false;
        for(long reference:starts) {
            var startChunk=world.getChunk(ChunkPos.getX(reference),ChunkPos.getZ(reference),ChunkStatus.STRUCTURE_STARTS,false);
            if(startChunk==null)continue;
            var start=startChunk.getStartForStructure(structure);
            if(start==null||!start.isValid())continue;
            for(var part:start.getPieces())if(part instanceof NetherBridgePiece bridge)
                for(int t=0;t<bridge.length();t++)if(bridge.span(t))for(int lane=-2;lane<=2;lane++) {
                    var floor=bridge.at(lane,0,t);
                    if(floor.getX()<cp.getMinBlockX()-8||floor.getX()>cp.getMaxBlockX()+8||
                       floor.getZ()<cp.getMinBlockZ()-8||floor.getZ()>cp.getMaxBlockZ()+8)continue;
                    // Decoration of a NEW neighbour must not edit an already
                    // completed chunk, where the player may have built since.
                    if(world instanceof net.minecraft.server.level.WorldGenRegion &&
                       world.getChunk(floor).getPersistedStatus().isOrAfter(ChunkStatus.FULL))continue;
                    if(!world.getBlockState(floor).equals(bridge.voxel(lane,0,t)))continue;
                    for(int y=1;y<=2;y++) {
                        var pos=floor.above(y);
                        var old=world.getBlockState(pos);
                        if(NetherBridgePiece.harmlessFlora(old)&&!old.hasBlockEntity()&&world.ensureCanWrite(pos))
                            changed|=world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
                    }
                }
        }
        return changed;
    }
}
