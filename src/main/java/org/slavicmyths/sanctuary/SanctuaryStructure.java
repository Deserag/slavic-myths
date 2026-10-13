package org.slavicmyths.sanctuary;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.*;
/** Native multi-chunk templates, bounded read-only terrain probes and acyclic collision priority. */
public final class SanctuaryStructure extends Structure {
 private final boolean spring;
 public SanctuaryStructure(StructureSettings s,boolean spring){super(s);this.spring=spring;}
 public static MapCodec<SanctuaryStructure> codec(boolean spring){return simpleCodec(s->new SanctuaryStructure(s,spring));}
 @Override public StructureType<?> type(){return spring?SanctuaryContent.SPRING.get():SanctuaryContent.FIRE.get();}
 @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
  if(c.heightAccessor() instanceof net.minecraft.world.level.WorldGenLevel w&&w.getLevel().dimension()!=net.minecraft.world.level.Level.OVERWORLD)return Optional.empty();
  if(c.random().nextInt(SanctuaryConfig.RARITY.get())!=0)return Optional.empty();
  try(var sample=StructureCoverageService.sampleTerrain(c)){
   for(BlockPos anchor:StructureCoverageService.localCandidates(c,this)){
    if(Math.max(Math.abs(anchor.getX()-c.chunkPos().getMinBlockX()),Math.abs(anchor.getZ()-c.chunkPos().getMinBlockZ()))>32)continue;
    var r=Rotation.getRandom(c.random());int width=SanctuaryGeometry.width(spring),depth=SanctuaryGeometry.depth(spring);int extentX=r==Rotation.CLOCKWISE_90||r==Rotation.COUNTERCLOCKWISE_90?depth:width,extentZ=r==Rotation.CLOCKWISE_90||r==Rotation.COUNTERCLOCKWISE_90?width:depth;
    var heights=new ArrayList<Integer>();boolean safe=true;
    for(int x=0;x<extentX;x+=8)for(int z=0;z<extentZ;z+=8){int px=anchor.getX()+x,pz=anchor.getZ()+z;int floor=StructureCoverageService.baseHeight(c.chunkGenerator(),px,pz,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());int surface=StructureCoverageService.baseHeight(c.chunkGenerator(),px,pz,Heightmap.Types.WORLD_SURFACE_WG,c.heightAccessor(),c.randomState());if(floor!=surface||floor<63||floor>130)safe=false;heights.add(floor);}
    var floor=SanctuaryTerrain.floor(heights,spring,SanctuaryConfig.SLOPE.get());if(!safe||floor.isEmpty())continue;int y=floor.getAsInt();
    BlockPos origin=SanctuaryGeometry.root(new BlockPos(anchor.getX(),y,anchor.getZ()),spring,r);
    if(spring){for(int x=8;x<width;x+=16)for(int z=8;z<depth;z+=16){var at=origin.offset(SanctuaryGeometry.rotate(x,0,z,r));var column=c.chunkGenerator().getBaseColumn(at.getX(),at.getZ(),c.heightAccessor(),c.randomState());for(int dy=-2;dy<=17;dy+=2)if(!column.getBlock(y+dy).getFluidState().isEmpty())safe=false;for(int dy=15;dy<=17;dy++)if(column.getBlock(y+dy).isAir())safe=false;}if(!safe)continue;}
    UUID instance=new UUID(c.random().nextLong(),c.random().nextLong());var pieces=new ArrayList<StructurePiece>();
    for(int x=0;x<width;x+=16)for(int z=0;z<depth;z+=16){String name=(spring?"spring":"fire")+"/"+x+"_"+z;var template=c.structureTemplateManager().get(SanctuaryContent.id("sanctuaries/"+name));if(template.isEmpty()||template.get().getSize().getY()==0){safe=false;break;}pieces.add(new SanctuaryPiece(c.structureTemplateManager(),name,origin.offset(SanctuaryGeometry.rotate(x,0,z,r)),origin,r,instance,spring));}
    if(!safe||!StructureCoverageService.referencesFit(c.chunkPos(),pieces))continue;
    if(!StructureCandidates.clearAccepted(c,new ChunkPos(anchor),other->other==null||other.getNamespace().equals("slavicmyths")&&Set.of("sanctuary_unfading_fire","abode_pure_spring").contains(other.getPath())?0:other.getNamespace().equals("minecraft")&&StructureCandidates.vanilla(other,"village","woodland_mansion","stronghold","pillager_outpost","ruined_portal")?12:other.getNamespace().equals("slavicmyths")?8:0,pieces))continue;
    BlockPos locate=origin.offset(SanctuaryGeometry.rotate(spring?48:40,spring?19:2,spring?82:70,r));return Optional.of(new GenerationStub(locate,b->pieces.forEach(b::addPiece)));
   }
  }return Optional.empty();
 }
}
