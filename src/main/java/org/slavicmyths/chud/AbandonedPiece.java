package org.slavicmyths.chud;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
public final class AbandonedPiece extends StructurePiece {
    public final AbandonedPlan plan;
    private volatile List<ChudGeometry.Fill> blueprint;
    private boolean industryContents=true;
    private boolean encounterMobs=true;
    public boolean encounterMobsEnabled(){return encounterMobs;}
    private final Set<Long> industryChunks=java.util.concurrent.ConcurrentHashMap.newKeySet();
    public AbandonedPiece(AbandonedPlan p){super(AbandonedStructures.PIECE.get(),0,p.nativeBounds());plan=p;}
    public AbandonedPiece(CompoundTag n){super(AbandonedStructures.PIECE.get(),n);plan=AbandonedPlan.load(n.getCompound("AbandonedPlan"));industryContents=n.getBoolean("IndustryContents");encounterMobs=n.getBoolean("EncounterMobs");for(long id:n.getLongArray("IndustryChunks"))industryChunks.add(id);}
    public List<ChudGeometry.Fill> blueprint(){var saved=blueprint;if(saved==null)synchronized(this){saved=blueprint;if(saved==null)blueprint=saved=AbandonedGeometry.build(plan);}return saved;}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag n){n.put("AbandonedPlan",plan.save());n.putBoolean("IndustryContents",industryContents);n.putBoolean("EncounterMobs",encounterMobs);n.putLongArray("IndustryChunks",industryChunks.stream().mapToLong(Long::longValue).sorted().toArray());}
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){if(industryContents&&industryChunks.contains(chunk.toLong()))return;ChudPiece.place(world,boundingBox,blueprint(),clip,chunk,new ChudDecor(null,plan));if(industryContents){org.slavicmyths.industry.ChudContents.abandoned(world,plan,clip);industryChunks.add(chunk.toLong());}}
}
