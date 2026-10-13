package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Native start persistence; rasterization touches only the supplied chunk, with no chunk requests. */
public final class ChudPiece extends StructurePiece {
    public final ChudPlan plan;
    public final AbandonedPlan abandoned;
    private volatile List<ChudGeometry.Fill> blueprint;
    private boolean industryContents=true;
    private boolean encounterMobs=true;
    public boolean encounterMobsEnabled(){return encounterMobs;}
    private final Set<Long> industryChunks=java.util.concurrent.ConcurrentHashMap.newKeySet();
    public ChudPiece(ChudPlan plan){this(plan,null);}
    public ChudPiece(ChudPlan plan,AbandonedPlan abandoned){super(ChudStructures.PIECE.get(),0,new BoundingBox(plan.anchor.getX()-127,plan.anchor.getY()-(abandoned==null?44:58),plan.anchor.getZ()-127,plan.anchor.getX()+127,plan.anchor.getY()+73,plan.anchor.getZ()+127));this.plan=plan;this.abandoned=abandoned;if(abandoned!=null&&(abandoned.kind!=AbandonedPlan.Kind.ATTACHED_SMALL||!abandoned.anchor.equals(plan.anchor)||abandoned.rotation!=plan.rotation||!abandoned.rootId.equals(plan.cityId())))throw new IllegalArgumentException("invalid city extension");}
    public ChudPiece(CompoundTag tag){super(ChudStructures.PIECE.get(),tag);plan=ChudPlan.load(tag.getCompound("ChudPlan"));abandoned=tag.contains("AbandonedPlan")?AbandonedPlan.load(tag.getCompound("AbandonedPlan")):null;industryContents=tag.getBoolean("IndustryContents");encounterMobs=tag.getBoolean("EncounterMobs");for(long id:tag.getLongArray("IndustryChunks"))industryChunks.add(id);}
    public List<ChudGeometry.Fill> blueprint(){var b=blueprint;if(b==null){synchronized(this){b=blueprint;if(b==null){var combined=new ArrayList<>(ChudGeometry.build(plan));if(abandoned!=null)combined.addAll(AbandonedGeometry.build(abandoned));blueprint=b=List.copyOf(combined);}}}return b;}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag){tag.put("ChudPlan",plan.save());if(abandoned!=null)tag.put("AbandonedPlan",abandoned.save());tag.putBoolean("IndustryContents",industryContents);tag.putBoolean("EncounterMobs",encounterMobs);tag.putLongArray("IndustryChunks",industryChunks.stream().mapToLong(Long::longValue).sorted().toArray());}
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
        if(industryContents&&industryChunks.contains(chunk.toLong()))return;
        place(world,boundingBox,blueprint(),clip,chunk,new ChudDecor(plan,abandoned));
        if(industryContents){org.slavicmyths.industry.ChudContents.city(world,plan,abandoned,clip);industryChunks.add(chunk.toLong());}
    }
    static void place(WorldGenLevel world,BoundingBox boundingBox,List<ChudGeometry.Fill> blueprint,BoundingBox clip,ChunkPos chunk){
        place(world,boundingBox,blueprint,clip,chunk,null);
    }
    static void place(WorldGenLevel world,BoundingBox boundingBox,List<ChudGeometry.Fill> blueprint,BoundingBox clip,ChunkPos chunk,ChudDecor decor){
        int minY=Math.max(clip.minY(),boundingBox.minY()),maxY=Math.min(clip.maxY(),boundingBox.maxY());
        if(minY>maxY)return;
        var chunkClip=new BoundingBox(chunk.getMinBlockX(),minY,chunk.getMinBlockZ(),chunk.getMaxBlockX(),maxY,chunk.getMaxBlockZ());
        BlockState[] states=new BlockState[(maxY-minY+1)*256];
        ChudGeometry.rasterize(blueprint,chunkClip,(p,s)->states[(p.getY()-minY)*256+(p.getZ()&15)*16+(p.getX()&15)]=s);
        var pos=new BlockPos.MutableBlockPos();
        int[] zones=new int[256];
        if(decor!=null)for(int z=0;z<16;z++)for(int x=0;x<16;x++)zones[z*16+x]=decor.zone(chunk.getMinBlockX()+x,chunk.getMinBlockZ()+z);
        // Fold construction stages first: each destination receives exactly its final state.
        for(int y=minY;y<=maxY;y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++){
            BlockState s=states[(y-minY)*256+z*16+x];if(s==null)continue;
            pos.set(chunk.getMinBlockX()+x,y,chunk.getMinBlockZ()+z);
            if(!clip.isInside(pos)||world.isOutsideBuildHeight(pos))continue;
            var old=world.getBlockState(pos);
            // Last defence against unknown mods' block entities; candidate rejection does the primary work.
            if(old.is(Blocks.BEDROCK)||old.is(Blocks.END_PORTAL)||old.is(Blocks.END_PORTAL_FRAME))continue;
            if(decor!=null){
                int above=(y-minY+1)*256+z*16+x;
                boolean open=y+2<=maxY&&states[above]!=null&&states[above].isAir()&&states[above+256]!=null&&states[above+256].isAir();
                s=decor.surface(s,pos.getX(),y,pos.getZ(),zones[z*16+x],open);
            }
            world.setBlock(pos,s,2);
        }
    }
}
