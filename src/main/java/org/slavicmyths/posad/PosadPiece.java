package org.slavicmyths.posad;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slavicmyths.netherstructures.NetherBridgeTerrain;

/** Persisted shell, clipped before any world read/write. No rewards or retrofit here. */
public final class PosadPiece extends StructurePiece {
    private final BlockPos anchor;
    private final int turns;
    public PosadPiece(BlockPos anchor,int turns) {
        super(PosadWorldgen.PIECE.get(),0,new BoundingBox(anchor.getX(),anchor.getY()-2,anchor.getZ(),anchor.getX()+112,anchor.getY()+17,anchor.getZ()+112));
        this.anchor=anchor.immutable(); this.turns=Math.floorMod(turns,4);
    }
    public PosadPiece(CompoundTag n) {
        super(PosadWorldgen.PIECE.get(),n);
        anchor=BlockPos.of(n.getLong("Anchor"));turns=Math.floorMod(n.getInt("Turns"),4);
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n) {
        n.putLong("Anchor",anchor.asLong());n.putInt("Turns",turns);n.putInt("Schema",PosadGeometry.SCHEMA_VERSION);
    }
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot) {
        // At most 16x16x20 voxel decisions per chunk; never scan the whole city per chunk.
        int x0=Math.max(clip.minX(),boundingBox.minX()),x1=Math.min(clip.maxX(),boundingBox.maxX());
        int z0=Math.max(clip.minZ(),boundingBox.minZ()),z1=Math.min(clip.maxZ(),boundingBox.maxZ());
        for(int wz=z0;wz<=z1;wz++)for(int wx=x0;wx<=x1;wx++) {
            var local=PosadGeometry.rotate(new PosadGeometry.Cell(wx-anchor.getX(),wz-anchor.getZ()),-turns);
            for(int y=-2;y<18;y++) {
                var state=PosadShell.voxel(local.x(),y,local.z());
                if(state==null)continue;
                BlockPos at=new BlockPos(wx,anchor.getY()+y,wz);
                if(!clip.isInside(at)||!world.ensureCanWrite(at))continue;
                var old=world.getBlockState(at);
                if(old.hasBlockEntity()||old.is(Blocks.BEDROCK)||!(old.isAir()||NetherBridgeTerrain.natural(old)))continue;
                if(state.isAir()&&!old.isAir())continue;
                world.setBlock(at,state.rotate(Rotation.values()[turns]),2);
            }
        }
    }
}
