package org.slavicmyths.gorodishche;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.*;

/** Rare native starts use the exact plan engine exercised by the showcase. */
public final class GorodishcheStructure extends Structure {
 public static final MapCodec<GorodishcheStructure> CODEC=simpleCodec(GorodishcheStructure::new);
 public GorodishcheStructure(StructureSettings settings){super(settings);}
 @Override public StructureType<?> type(){return GorodishcheStructures.TYPE.get();}
 public static int height(GenerationContext c,int x,int z){return StructureCoverageService.baseHeight(c.chunkGenerator(),x,z,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState())-1;}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){try(var sampling=StructureCoverageService.sampleTerrain(c)){for(int[] offset:new int[][]{{0,0},{32,0},{0,32},{-32,0},{0,-32},{32,32},{-32,32},{-32,-32},{32,-32}}){var point=site(c,offset[0],offset[1]);if(point.isPresent())return point;}return Optional.empty();}}
 private Optional<GenerationStub> site(GenerationContext c,int dx,int dz){try{
  int size=CityCatalog.settings().size(),x=c.chunkPos().getMiddleBlockX()-size/2+dx,z=c.chunkPos().getMiddleBlockZ()-size/2+dz;int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE,wet=0,total=0;
  for(int a=0;a<size;a+=16)for(int b=0;b<size;b+=16){int ground=height(c,x+a,z+b),y=StructureCoverageService.baseHeight(c.chunkGenerator(),x+a,z+b,Heightmap.Types.WORLD_SURFACE_WG,c.heightAccessor(),c.randomState())-1;min=Math.min(min,y);max=Math.max(max,y);if(y>ground)wet++;total++;if(max-min>CityCatalog.settings().terrainDelta()){StructureCoverageService.note(StructureCoverageService.Rejection.EXTREME_TERRAIN,"cityHeightDelta",max-min);return Optional.empty();}}
  if(wet>total/12){StructureCoverageService.note(StructureCoverageService.Rejection.DEEP_WATER,"cityWetSamples",wet);return Optional.empty();}if(min<c.heightAccessor().getMinBuildHeight()){StructureCoverageService.note(StructureCoverageService.Rejection.WORLD_BOUNDS,"cityMinFloor",min);return Optional.empty();}
  var biome=c.biomeSource().getNoiseBiome((x+size/2)>>2,(min+max)>>3,(z+size/2)>>2,c.randomState().sampler()).value();String climate=biome.getBaseTemperature()<.25F?"cold":biome.getBaseTemperature()>1F?"warm":"temperate";
  long seed=c.seed()^c.chunkPos().toLong()^125176L;var plan=CityPlan.make(climate,seed,x,z,(px,pz)->height(c,px,pz),false);
  CityPlacement.validate(plan,c.structureTemplateManager(),c.heightAccessor(),null);
  for(var p:plan.placements()){int centerY=p.origin().getY()+p.building().surface();var box=p.box();for(int a:new int[]{box.minX(),box.maxX()})for(int b:new int[]{box.minZ(),box.maxZ()})if(Math.abs(height(c,a,b)-centerY)>CityCatalog.settings().correction()){StructureCoverageService.note(StructureCoverageService.Rejection.TERRAFORM_BUDGET_EXCEEDED,"cityLocalCorrection",Math.abs(height(c,a,b)-centerY));return Optional.empty();}}
  var pieces=CityPlacement.pieces(plan,c.structureTemplateManager(),seed,c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getMaxBuildHeight());
  if(!StructureCoverageService.referencesFit(c.chunkPos(),pieces))return Optional.empty();
  if(!StructureCandidates.clearAccepted(c,c.chunkPos(),id->{if(id==null||id.getNamespace().equals("slavicmyths")&&(id.getPath().equals("gorodishche")||id.getPath().startsWith("kurgan_")))return 0;return StructureCandidates.vanilla(id,"village","woodland_mansion","pillager_outpost")||id.getNamespace().equals("slavicmyths")?12:0;},pieces)){StructureCoverageService.note(StructureCoverageService.Rejection.SPACING_CONFLICT,"cityOverlap",1);return Optional.empty();}
  StructureCoverageService.note(StructureCoverageService.Rejection.ACCEPTED,"cityPieces",pieces.size());
  return Optional.of(new GenerationStub(plan.origin().offset(size/2,1,size/2),builder->pieces.forEach(builder::addPiece)));
 }catch(IllegalStateException ex){if(ex.getMessage()!=null&&(ex.getMessage().startsWith("LOCAL_TERRAIN")||ex.getMessage().startsWith("FRONTAGE_HEIGHT"))){StructureCoverageService.note(StructureCoverageService.Rejection.TERRAFORM_BUDGET_EXCEEDED,"cityLotTerrain",1);}else System.getLogger("slavicmyths.gorodishche").log(System.Logger.Level.WARNING,ex.getMessage());return Optional.empty();}}
}
