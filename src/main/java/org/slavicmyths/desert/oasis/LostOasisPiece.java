package org.slavicmyths.desert.oasis;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
public final class LostOasisPiece extends StructurePiece {
    private final BlockPos origin;
    public LostOasisPiece(BlockPos o,int min,int max){super(LostOasisStructure.PIECE.get(),0,LostOasisBuild.bounds(o,min,max));origin=o.immutable();}
    public LostOasisPiece(CompoundTag n){super(LostOasisStructure.PIECE.get(),n);origin=BlockPos.of(n.getLong("Origin"));if(n.getInt("PlanVersion")!=1)throw new IllegalArgumentException("Unknown oasis plan");}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){n.putLong("Origin",origin.asLong());n.putInt("PlanVersion",1);}
    @Override public void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        LostOasisBuild.render(w,origin,clip);
        BlockPos center=origin.offset(LostOasisPlan.CX,LostOasisPlan.FLOOR,LostOasisPlan.CZ);
        if((center.getX()>>4)==chunk.x&&(center.getZ()>>4)==chunk.z){var server=w.getLevel();server.getServer().execute(()->LostOasisRecords.get(server).natural(origin));}
    }
}
