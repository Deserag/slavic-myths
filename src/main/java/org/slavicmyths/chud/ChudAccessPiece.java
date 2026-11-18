package org.slavicmyths.chud;
import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.util.RandomSource;import net.minecraft.world.level.*;import net.minecraft.world.level.chunk.ChunkGenerator;import net.minecraft.world.level.levelgen.structure.*;import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
/** Independently bounded, serialized and chunk-clipped surface route for new native starts. */
public final class ChudAccessPiece extends StructurePiece {
 public final ChudSurfaceAccess access;
 public ChudAccessPiece(ChudSurfaceAccess a){super(ChudStructures.ACCESS.get(),0,a.bounds);access=a;}
 public ChudAccessPiece(CompoundTag n){super(ChudStructures.ACCESS.get(),n);access=ChudSurfaceAccess.load(n.getCompound("Access"));}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){n.put("Access",access.save());}
 @Override public void postProcess(WorldGenLevel w,StructureManager m,ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){ChudPiece.place(w,boundingBox,access.blueprint(),clip,chunk);}
}
