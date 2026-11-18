package org.slavicmyths.netherstructures;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slavicmyths.worldgen.ManualStructureRecords;
/** Explicit OP models from the SAME authored voxels as native generation. No biome/rarity search. */
public final class ManualNetherBuildings {
 public static final List<String> IDS=List.of("nether_watchpost","broken_nether_bridge","wayfarer_camp","forgotten_shrine","ancestor_shrine","trizna_pyre");
 public static void register(org.slavicmyths.commands.CommandTree tree){
  var generate=Commands.literal("generate").requires(source->source.hasPermission(2));
  for(String id:IDS)generate.then(Commands.literal(id).executes(c->place(c.getSource(),id,org.slavicmyths.worldgen.ManualTerrain.ground(c.getSource()))));
  tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").then(generate)));
  var root=Commands.literal("nether").requires(s->s.hasPermission(2));
  for(String id:IDS){var node=Commands.literal(id).executes(c->place(c.getSource(),id,BlockPos.containing(c.getSource().getPosition())));
   node.then(Commands.argument("pos",net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos()).executes(c->place(c.getSource(),id,net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(c,"pos"))));
   root.then(Commands.literal("create").then(node));}
  tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").then(root)));
 }
 @FunctionalInterface interface Voxels {BlockState at(int x,int y,int z);}
 public static int place(CommandSourceStack source,String id,BlockPos origin){
  if(!source.hasPermission(2))return 0;
  var world=source.getLevel();Voxels voxel;int minX=0,minY=0,minZ=0,maxX,maxY,maxZ;
  switch(id){
   case "nether_watchpost"->{var p=new NetherWatchpostPiece(origin,2,Rotation.NONE,true,0,0);voxel=p::state;minX=minZ=-5;minY=-4;maxX=maxZ=5;maxY=p.deck()+4;}
   case "broken_nether_bridge"->{int[] supports=new int[34];Arrays.fill(supports,2);var p=new NetherBridgePiece(new NetherBridgeTerrain.Plan(origin,Rotation.NONE,17,1,7,9,supports,0,0,0));voxel=p::voxel;minX=-3;maxX=3;minY=-2;maxY=2;maxZ=16;}
   case "wayfarer_camp"->{var p=new WayfarerPiece(new WayfarerTerrain.Plan(origin,Rotation.NONE,0,true,0));voxel=p::voxel;maxX=8;maxY=5;maxZ=6;}
   case "forgotten_shrine"->{voxel=(x,y,z)->ForgottenShrinePiece.voxel(0,x,y,z);maxX=maxZ=14;minY=-5;maxY=8;}
   case "ancestor_shrine"->{var p=new AncestorShrinePiece(new AncestorShrineTerrain.Plan(origin,Rotation.NONE,0,0));voxel=p::voxel;maxX=12;maxY=10;maxZ=14;}
   case "trizna_pyre"->{voxel=(x,y,z)->TriznaPyrePiece.voxel(0,x,y,z);maxX=maxZ=14;maxY=8;}
   default->{source.sendFailure(Component.literal("Неизвестная постройка: "+id));return 0;}
  }
  var a=origin.offset(minX,minY,minZ);var b=origin.offset(maxX,maxY,maxZ);var bounds=new BoundingBox(a.getX(),a.getY(),a.getZ(),b.getX(),b.getY(),b.getZ());
  if(a.getY()<world.getMinBuildHeight()||b.getY()>=world.getMaxBuildHeight()||!world.getWorldBorder().isWithinBounds(a)||!world.getWorldBorder().isWithinBounds(b)){source.sendFailure(Component.literal("Постройка выходит за границы мира."));return 0;}
  for(int cx=a.getX()>>4;cx<=b.getX()>>4;cx++)for(int cz=a.getZ()>>4;cz<=b.getZ()>>4;cz++)if(world.getChunkSource().getChunkNow(cx,cz)==null){source.sendFailure(Component.literal("Чанк не загружен: "+cx+","+cz+". Подойдите к площадке."));return 0;}
  var records=ManualStructureRecords.get(world);
  if(!records.overlapping(bounds).isEmpty()){source.sendFailure(Component.literal("Место уже занято зарегистрированной постройкой."));return 0;}
  for(var at:BlockPos.betweenClosed(a,b)){
   var old=world.getBlockState(at);
   if(old.hasBlockEntity()||old.is(Blocks.BEDROCK)||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(at)||!(org.slavicmyths.worldgen.ManualStructureJobs.natural(old))){source.sendFailure(Component.literal("Защищённый/чужой блок: "+at.toShortString()+" "+old));return 0;}
  }
  java.util.Map<BlockPos,BlockState> foundation;
  try{foundation=org.slavicmyths.worldgen.ManualTerrain.foundation(world,a,b);}catch(IllegalArgumentException failure){source.sendFailure(Component.literal(failure.getMessage()));return 0;}
  records.add(UUID.randomUUID(),id,0,origin,bounds);world.getDataStorage().save();foundation.forEach((at,state)->world.setBlock(at,state,2));int changed=0;
  for(var at:BlockPos.betweenClosed(a,b))if(!world.getBlockState(at).isAir()){world.setBlock(at,Blocks.AIR.defaultBlockState(),2);changed++;}
  for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++){
   var next=voxel.at(x,y,z);if(next==null)continue;var at=origin.offset(x,y,z);
   if(!world.getBlockState(at).equals(next)){world.setBlock(at,next,2);changed++;}
   if(world.getBlockEntity(at) instanceof RandomizableContainerBlockEntity chest){
    var loot=ResourceLocation.fromNamespaceAndPath("slavicmyths","chests/"+id);
    chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,loot),world.getSeed()^at.asLong());
   }
  }
  final int count=changed;source.sendSuccess(()->Component.literal("Построено: "+id+" в "+origin.toShortString()+", изменено блоков: "+count),false);return changed>0?1:0;
 }
 private ManualNetherBuildings(){}
}
