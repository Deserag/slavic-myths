package org.slavicmyths.netherstructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
public final class NetherStructureProtection {
    private static final TagKey<Block> CRAFTED=TagKey.create(Registries.BLOCK,TriznaPyreWorldgen.id("nether_structure_protected"));
    public static boolean protectedAt(LevelAccessor w,BlockPos p) {
        var state=w.getBlockState(p);if(state.is(CRAFTED))return true;
        if(!(state.is(Blocks.BLACKSTONE)||state.is(Blocks.BASALT))||!(w instanceof WorldGenLevel world))return false;
        // Existing single-chunk owner only: no searches, new chunk loads or tickets.
        var chunk=world.getChunk(p.getX()>>4,p.getZ()>>4,net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS,false);
        if(chunk==null)return false;
        for(var start:chunk.getAllStarts().values())if(start.isValid())for(var part:start.getPieces())
            if(part instanceof TriznaPyrePiece pyre&&pyre.ownsRaw(p,state))return true;
        return false;
    }
    private NetherStructureProtection(){}
}
