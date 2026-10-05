package org.slavicmyths.bandit;
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
public final class CampStructure extends Structure {
 public final boolean medium;
 public CampStructure(StructureSettings settings,boolean medium){super(settings);this.medium=medium;}
 public static MapCodec<CampStructure> codec(boolean medium){return simpleCodec(settings->new CampStructure(settings,medium));}
 @Override public StructureType<?> type(){return medium?CampStructures.MEDIUM.get():CampStructures.SMALL.get();}
 private boolean candidate(GenerationContext c){return StructureCandidates.clear(c,other->{if(other==null)return 0;if(other.getNamespace().equals("slavicmyths")){String id=other.getPath();if(id.equals(medium?"bandit_camp_medium":"bandit_camp_small")||medium&&id.equals("bandit_camp_small"))return 0;if(id.startsWith("bandit_camp_")||org.slavicmyths.swamp.SwampStructures.TYPES.containsKey(id))return 4;return 0;}if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 10;return StructureCandidates.vanilla(other,"pillager_outpost","ruined_portal")?4:0;});}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
 if(!candidate(context))return Optional.empty();List<StructurePiece> pieces=new ArrayList<>();plan(context,pieces);if(pieces.isEmpty())return Optional.empty();
 BlockPos locate=pieces.getFirst().getBoundingBox().getCenter();return Optional.of(new GenerationStub(locate,builder->pieces.forEach(builder::addPiece)));
 }
 private void plan(GenerationContext context,List<StructurePiece> pieces){
 ChunkGenerator g=context.chunkGenerator(),generator=g;StructureTemplateManager templates=context.structureTemplateManager(),t=templates;int cx=context.chunkPos().x,cz=context.chunkPos().z;var random=context.random();

            int x=cx<<4,z=cz<<4,total=medium?9:5;UUID camp=new UUID(random.nextLong(),random.nextLong());
            List<Object[]> plan=new ArrayList<>();
            if(medium){
                plan.add(new Object[]{"leader",18,1,0});plan.add(new Object[]{"sleeping",2,5,1});plan.add(new Object[]{"sleeping",4,24,3});
                plan.add(new Object[]{"storage_tent",28,5,5});plan.add(new Object[]{"senior",26,26,6});plan.add(new Object[]{"warehouse",33,16,-1});
                plan.add(new Object[]{"watch",1,16,8});plan.add(new Object[]{"canopy",15,28,-1});plan.add(new Object[]{"yard_medium",0,0,-1});
            }else{
                plan.add(new Object[]{"sleeping",1,1,0});plan.add(new Object[]{"storage_tent",15,3,2});plan.add(new Object[]{"senior",8,15,3});plan.add(new Object[]{"yard_small",0,0,-1});
                if(random.nextBoolean())plan.add(new Object[]{"cart",0,12,-1});
            }
            for(Object[] p:plan){String name=(String)p[0];int px=x+(Integer)p[1],pz=z+(Integer)p[2];net.minecraft.core.Vec3i size=templates.getOrCreate(ResourceLocation.fromNamespaceAndPath("slavicmyths","bandit/"+name)).getSize();if(size.equals(BlockPos.ZERO)){pieces.clear();return;}
                int min=255,max=0;for(int a=0;a<size.getX();a+=Math.max(1,(size.getX()-1)/2))for(int b=0;b<size.getZ();b+=Math.max(1,(size.getZ()-1)/2)){
                    int ground=g.getBaseHeight(px+a,pz+b,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState()),surface=g.getBaseHeight(px+a,pz+b,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
                    if(ground<surface||surface<55||surface>110){pieces.clear();return;}min=Math.min(min,ground);max=Math.max(max,ground);
                }
                if(max-min>(name.startsWith("yard")?7:3)){pieces.clear();return;}
                pieces.add(new CampPiece(templates,name,new BlockPos(px,max-1,pz),camp,total,(Integer)p[3]));
            }

 }
}
