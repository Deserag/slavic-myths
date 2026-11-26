package org.slavicmyths.desert.winds;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class FourWindsPiece extends StructurePiece {
    private final BlockPos origin;
    public FourWindsPiece(BlockPos origin){
        super(FourWindsStructure.PIECE.get(),0,FourWindsBuild.bounds(origin));
        this.origin=origin.immutable();
    }
    public FourWindsPiece(CompoundTag n){
        super(FourWindsStructure.PIECE.get(),n);
        origin=BlockPos.of(n.getLong("Origin"));
        if(n.getInt("PlanVersion")!=2)throw new IllegalArgumentException("Unknown Four Winds plan");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){
        n.putLong("Origin",origin.asLong());n.putInt("PlanVersion",2);
    }
    @Override public void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        FourWindsBuild.render(w,origin,clip);
        if((origin.getX()>>4)==chunk.x&&(origin.getZ()>>4)==chunk.z){
            var server=w.getLevel();
            server.getServer().execute(()->FourWindsRecords.get(server).register(origin));
        }
    }
}
