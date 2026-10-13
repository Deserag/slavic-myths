package org.slavicmyths.gorodishche;
import java.util.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.slavicmyths.village.VillageFoundation;

/** Native saved piece; each clipped resident marker is processed at most once. */
public final class CityBuildingPiece extends TemplateStructurePiece {
 private final long seed;private long processed;private boolean defer;private int spawnedVillagers,spawnedGuards;
 public int spawnedGuards(){return spawnedGuards;}
 public void deferResidents(boolean value){defer=value;}
 public java.util.Optional<BlockPos> gateApproach(){if(!templateName.contains("gate_main_"))return java.util.Optional.empty();var front=StructureTemplate.transform(new BlockPos(7,1,0),Mirror.NONE,placeSettings.getRotation(),BlockPos.ZERO).offset(templatePosition);return java.util.Optional.of(front.relative(placeSettings.getRotation().rotate(Direction.NORTH),10));}
 public Direction facing(){return placeSettings.getRotation().rotate(Direction.NORTH);}
 public int spawnResidents(ServerLevelAccessor world,BoundingBox clip,RandomSource random){int before=spawnedVillagers;for(var info:template.filterBlocks(templatePosition,placeSettings.copy().setBoundingBox(clip),Blocks.STRUCTURE_BLOCK))if(info.nbt()!=null)handleDataMarker(info.nbt().getString("metadata"),info.pos(),world,random,clip);return spawnedVillagers-before;}

 public CityBuildingPiece(StructureTemplateManager manager,CityPlan.Placement p,long seed){super(GorodishcheStructures.BUILDING.get(),0,manager,p.building().id(),p.building().id().toString(),settings(p.rotation()),p.origin());this.seed=seed;boundingBox=p.box();}
 public CityBuildingPiece(StructureTemplateManager manager,CompoundTag n){super(GorodishcheStructures.BUILDING.get(),n,manager,id->settings(Rotation.valueOf(n.getString("CityRotation"))));seed=n.getLong("CitySeed");processed=n.getLong("CityProcessed");}
 private static StructurePlaceSettings settings(Rotation r){return new StructurePlaceSettings().setRotation(r).setIgnoreEntities(true).setKnownShape(true).addProcessor(VillageFoundation.INSTANCE).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){super.addAdditionalSaveData(c,n);n.putString("CityRotation",placeSettings.getRotation().name());n.putLong("CitySeed",seed);n.putLong("CityProcessed",processed);}
 @Override public synchronized void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){var accepted=boundingBox;try{super.postProcess(world,manager,generator,random,clip,chunk,pivot);}finally{boundingBox=accepted;}}
 @Override protected void handleDataMarker(String marker,BlockPos pos,ServerLevelAccessor world,RandomSource random,BoundingBox clip){
  if(defer||!clip.isInside(pos)||!marker.startsWith("spawn|"))return;world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);String[] data=marker.split("\\|");int slot=Integer.parseInt(data[1]);if(slot<0||slot>=63||(processed&(1L<<slot))!=0)return;
  var id=UUID.nameUUIDFromBytes((seed+":"+templateName+":"+pos.asLong()+":"+slot).getBytes(StandardCharsets.UTF_8));if(world.getLevel().getEntity(id)!=null){processed|=1L<<slot;return;}
  Entity e;String role=data[2];
  if(role.equals("horse"))e=EntityType.HORSE.create(world.getLevel());else if(role.equals("cow"))e=EntityType.COW.create(world.getLevel());else if(role.equals("sheep"))e=EntityType.SHEEP.create(world.getLevel());else e=EntityType.VILLAGER.create(world.getLevel());
  if(e==null)throw new IllegalStateException("NPC_SPAWN_FAILED "+role);e.setUUID(id);e.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,placeSettings.getRotation().rotate(Direction.NORTH).toYRot(),0);
  if(e instanceof Mob mob){mob.finalizeSpawn(world,world.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null);mob.setPersistenceRequired();}
  if(e instanceof Villager v&&!role.equals("resident")){var key=ResourceLocation.parse(role.contains(":")?role:"minecraft:"+role);if(!net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION.containsKey(key))throw new IllegalStateException("WORKSTATION_ROLE_MISSING "+key);v.setVillagerData(v.getVillagerData().setProfession(net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION.get(key)));v.setVillagerXp(1);v.refreshBrain(world.getLevel());}
  if(world.addFreshEntity(e)){processed|=1L<<slot;if(e instanceof Villager v){spawnedVillagers++;if(org.slavicmyths.military.Military.guard(v))spawnedGuards++;}}
 }
}
