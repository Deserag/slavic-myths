package org.slavicmyths.desert.temple;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class SunTemplePiece extends StructurePiece {
    private final BlockPos origin;
    public SunTemplePiece(BlockPos origin,int min,int max){super(SunTempleStructure.PIECE.get(),0,SunTempleBuild.bounds(origin,min,max));this.origin=origin;}
    public SunTemplePiece(CompoundTag n){super(SunTempleStructure.PIECE.get(),n);origin=BlockPos.of(n.getLong("Origin"));}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){n.putLong("Origin",origin.asLong());}
    @Override public void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        SunTempleBuild.render(w,origin,clip);
        BlockPos center=origin.offset(SunTemplePlan.CX,SunTemplePlan.FLOOR,SunTemplePlan.CZ);
        if((center.getX()>>4)==chunk.x&&(center.getZ()>>4)==chunk.z){
            var server=w.getLevel();server.getServer().execute(()->SunTempleRecords.get(server).natural(origin));
        }
    }
}
