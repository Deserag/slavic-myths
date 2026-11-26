package org.slavicmyths.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class DungeonPiece extends StructurePiece {
    private final DungeonPlan plan;
    private final BlockPos origin;
    private final int wellY;
    private boolean registered;
    public DungeonPiece(DungeonPlan plan,BlockPos origin,int wellY){
        super(DungeonStructure.PIECE.get(),0,DungeonBuild.bounds(plan,origin,wellY));
        this.plan=plan;this.origin=origin;this.wellY=wellY;
    }
    public DungeonPiece(CompoundTag n){
        super(DungeonStructure.PIECE.get(),n);
        plan=DungeonPlan.create(n.getLong("Seed"),n.getInt("Floors"),n.contains("PlanVersion")?n.getInt("PlanVersion"):1);
        origin=BlockPos.of(n.getLong("Origin"));wellY=n.getInt("WellY");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){
        n.putLong("Seed",plan.seed);n.putInt("PlanVersion",plan.version);n.putInt("Floors",plan.floors.size());n.putLong("Origin",origin.asLong());n.putInt("WellY",wellY);
    }
    @Override public synchronized void postProcess(WorldGenLevel w,StructureManager sm,ChunkGenerator g,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        if(!registered){registered=true;var server=w.getLevel();server.getServer().execute(()->DungeonRecords.get(server).registerNatural(origin,plan));}
        DungeonBuild.render(w,origin,plan,wellY,clip);
    }
}
