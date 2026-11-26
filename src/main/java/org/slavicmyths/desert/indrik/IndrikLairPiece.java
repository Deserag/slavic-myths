package org.slavicmyths.desert.indrik;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class IndrikLairPiece extends StructurePiece {
    private final BlockPos origin;
    private final int version;
    public IndrikLairPiece(BlockPos o){super(IndrikLairStructure.PIECE.get(),0,IndrikLairBuild.bounds(o));origin=o.immutable();version=IndrikLairPlan.VERSION;}
    public IndrikLairPiece(CompoundTag n){
        super(IndrikLairStructure.PIECE.get(),n);
        origin=BlockPos.of(n.getLong("Origin"));
        version=n.getInt("PlanVersion");
        IndrikLairPlan.cells(version);
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){
        n.putLong("Origin",origin.asLong());n.putInt("PlanVersion",version);
    }
    @Override public void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        IndrikLairBuild.render(w,origin,clip,version);
        if((origin.getX()>>4)==chunk.x&&(origin.getZ()>>4)==chunk.z){
            var server=w.getLevel();
            server.getServer().execute(()->IndrikLairRecords.get(server).register(origin));
        }
    }
}
