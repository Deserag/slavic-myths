package org.slavicmyths.kurgan;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slavicmyths.worldgen.StructureCandidates;
public final class KurganStructure extends Structure {
 public final int kind;
 public KurganStructure(StructureSettings settings,int kind){super(settings);this.kind=kind;}
 public static MapCodec<KurganStructure> codec(int kind){return simpleCodec(settings->new KurganStructure(settings,kind));}
 @Override public StructureType<?> type(){return KurganStructures.type(kind);}
 private boolean candidate(GenerationContext c){if(KurganShape.kind(c.seed(),c.chunkPos().x,c.chunkPos().z)!=kind)return false;
 if(!StructureCandidates.clear(c,other->{if(other==null||other.getNamespace().equals("slavicmyths")&&other.getPath().startsWith("kurgan_")||StructureCandidates.vanilla(other,"mineshaft","stronghold","buried_treasure"))return 0;return (KurganShape.RADIUS[kind]+96+15)/16;}))return false;
 if(kind==2){var placement=new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(64,24,net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR,841973);for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){if(dx==0&&dz>=0||dx>0)continue;ChunkPos q=placement.getPotentialStructureChunk(c.seed(),c.chunkPos().x+dx*64,c.chunkPos().z+dz*64);if(KurganShape.kind(c.seed(),q.x,q.z)==2)return false;}}
 return true;}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
 if(!candidate(context))return Optional.empty();List<StructurePiece> pieces=new ArrayList<>();plan(context,pieces);if(pieces.isEmpty())return Optional.empty();
 BlockPos locate=pieces.getFirst().getBoundingBox().getCenter();return Optional.of(new GenerationStub(locate,builder->pieces.forEach(builder::addPiece)));
 }
 private void plan(GenerationContext context,List<StructurePiece> pieces){
 ChunkGenerator g=context.chunkGenerator(),generator=g;StructureTemplateManager templates=context.structureTemplateManager(),t=templates;int cx=context.chunkPos().x,cz=context.chunkPos().z;var random=context.random();

   int x=(cx<<4)+8,z=(cz<<4)+8,radius=new int[]{8,12,18}[kind],low=255,high=0;
   for(int dx=-radius;dx<=radius;dx+=2)for(int dz=-radius;dz<=radius;dz+=2){if(dx*dx+dz*dz>radius*radius)continue;int floor=g.getBaseHeight(x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState()),top=g.getBaseHeight(x+dx,z+dz,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());if(floor!=top||floor<63||floor>140)return;low=Math.min(low,floor);high=Math.max(high,floor);if(high-low>(kind==0?4:6))return;}
   try {
    long layoutSeed=random.nextLong();KurganPlan plan=KurganPlan.create(kind,layoutSeed);BlockPos origin=new BlockPos(x,high-1,z);
    if(!terrainValid(g,plan,origin,context.heightAccessor(),context.randomState()))return;
    pieces.add(new KurganDungeonPiece(plan,origin,new UUID(random.nextLong(),random.nextLong())));
   }catch(IllegalArgumentException invalid){/* bounded planner failed: do not add any piece */}

 }
 public static boolean terrainValid(ChunkGenerator g,KurganPlan plan,BlockPos origin,net.minecraft.world.level.LevelHeightAccessor height,net.minecraft.world.level.levelgen.RandomState randomState){
  KurganPlan.Box bounds=plan.bounds();if(origin.getY()+bounds.y0<height.getMinBuildHeight()+2||origin.getY()+bounds.y1>=height.getMaxBuildHeight()-6)return false;
  for(KurganPlan.Room room:plan.rooms){KurganPlan.Box b=room.box();for(int x=b.x0;x<=b.x1;x+=2)for(int z=b.z0;z<=b.z1;z+=2){int ground=g.getBaseHeight(origin.getX()+x,origin.getZ()+z,Heightmap.Types.OCEAN_FLOOR_WG,height,randomState);if(ground<origin.getY()+b.y1+3)return false;}}
  return true;
 }
}
