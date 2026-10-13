package org.slavicmyths.sanctuary;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.util.RandomSource;
public final class SanctuaryPiece extends TemplateStructurePiece {
 public final UUID instance;public final BlockPos origin;public final Rotation rotation;public final boolean spring;private final Set<Long> processed=new HashSet<>();
 public SanctuaryPiece(StructureTemplateManager tm,String name,BlockPos p,BlockPos origin,Rotation r,UUID id,boolean spring){super(SanctuaryContent.PIECE.get(),0,tm,SanctuaryContent.id("sanctuaries/"+name),"slavicmyths:sanctuaries/"+name,settings(r),p);this.origin=origin;rotation=r;instance=id;this.spring=spring;}
 public SanctuaryPiece(StructureTemplateManager tm,CompoundTag n){super(SanctuaryContent.PIECE.get(),n,tm,id->settings(rotation(n)));instance=n.getUUID("SanctuaryInstance");origin=BlockPos.of(n.getLong("SanctuaryOrigin"));rotation=rotation(n);spring=n.getBoolean("SanctuarySpring");for(long p:n.getLongArray("SanctuaryMarkers"))processed.add(p);}
 private static Rotation rotation(CompoundTag n){try{return Rotation.valueOf(n.getString("SanctuaryRotation"));}catch(IllegalArgumentException bad){return Rotation.NONE;}}
 private static StructurePlaceSettings settings(Rotation r){return new StructurePlaceSettings().setRotation(r).setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){super.addAdditionalSaveData(c,n);n.putUUID("SanctuaryInstance",instance);n.putLong("SanctuaryOrigin",origin.asLong());n.putString("SanctuaryRotation",rotation.name());n.putBoolean("SanctuarySpring",spring);n.putLongArray("SanctuaryMarkers",processed.stream().mapToLong(Long::longValue).toArray());}
 @Override public synchronized void postProcess(WorldGenLevel w,StructureManager m,net.minecraft.world.level.chunk.ChunkGenerator g,RandomSource r,BoundingBox clip,ChunkPos chunk,BlockPos pivot){super.postProcess(w,m,g,r,clip,chunk,pivot);var server=w.getLevel();server.getServer().execute(()->org.slavicmyths.guardians.GuardianWorld.generated(server,instance,origin,rotation,spring));if(spring)return;for(int x=Math.max(clip.minX(),boundingBox.minX());x<=Math.min(clip.maxX(),boundingBox.maxX());x++)for(int z=Math.max(clip.minZ(),boundingBox.minZ());z<=Math.min(clip.maxZ(),boundingBox.maxZ());z++){var floor=new BlockPos(x,origin.getY(),z);if(w.getBlockState(floor).isAir()||!w.getBlockState(floor).getFluidState().isEmpty())continue;for(int down=1;down<=8;down++){var p=floor.below(down);if(!clip.isInside(p)||!w.getBlockState(p).isAir()&&!LandVegetation(w.getBlockState(p)))break;w.setBlock(p,(down<3?Blocks.COBBLESTONE:Blocks.STONE).defaultBlockState(),2);}}}
 private static boolean LandVegetation(BlockState s){return org.slavicmyths.worldgen.LandTerrain.vegetation(s)&&s.getFluidState().isEmpty();}
 @Override protected synchronized void handleDataMarker(String marker,BlockPos p,ServerLevelAccessor w,RandomSource random,BoundingBox clip){if(!clip.isInside(p)||!processed.add(p.asLong()))return;var parts=marker.split(":");String role=parts[0];if(role.equals("heart")||role.equals("pool")){String block=role.equals("pool")?"tainted_spring_node":spring?"pure_spring_heart":"unfading_brazier";w.setBlock(p,SanctuaryContent.block(block).defaultBlockState().rotate(rotation),2);if(w.getBlockEntity(p) instanceof SanctuaryHeart h)h.configure(instance,origin,rotation,spring,role.equals("pool")?Integer.parseInt(parts[1]):-1);}
  else if(role.equals("loot")||role.equals("cache")){boolean cache=role.equals("cache");BlockState state=(cache?SanctuaryContent.CACHE.get():Blocks.CHEST).defaultBlockState().setValue(ChestBlock.FACING,rotation.rotate(Direction.SOUTH));w.setBlock(p,state,2);if(w.getBlockEntity(p) instanceof SanctuaryChest chest)chest.configure(instance,spring);if(w.getBlockEntity(p) instanceof RandomizableContainerBlockEntity chest){String table=cache?"cache":parts.length>1?parts[1]:"regular";chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,SanctuaryContent.id("chests/"+(spring?"spring_":"fire_")+table)),instance.getMostSignificantBits()^p.asLong());}}
 }
}
