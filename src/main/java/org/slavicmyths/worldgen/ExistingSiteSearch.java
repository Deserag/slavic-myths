package org.slavicmyths.worldgen;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Inspect actual starts only. Never requests a chunk or invokes placement/worldgen. */
public final class ExistingSiteSearch {
 public static int locate(CommandSourceStack source,String id){return locate(source,id,1024);}
 public static int locate(CommandSourceStack source,String id,int radius){
  var world=source.getLevel();var at=BlockPos.containing(source.getPosition());
  var type=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));
  BlockPos nearest=null;double distance=(double)radius*radius;int range=(radius+15)/16;
  // Completed manual records survive unload/restart; construction reservations never qualify.
  // If their owning start is loaded below, the actual start provides the independent native path.
  var saved=ManualStructureRecords.get(world).entries().stream().filter(v->v.family().equals(id)&&v.arrival().distSqr(at)<=(double)radius*radius&&v.bounds().isInside(v.arrival())&&validLoadedRecord(world,type,v.arrival())).min(java.util.Comparator.comparingDouble(v->v.arrival().distSqr(at)));
  if(saved.isPresent()){var pos=saved.get().arrival();source.sendSuccess(()->Component.literal(id+": сохранённая завершённая постройка "+pos.toShortString()+" (чанки не загружались)"),false);return 1;}
  if(type!=null)for(int x=(at.getX()>>4)-range;x<=(at.getX()>>4)+range;x++)for(int z=(at.getZ()>>4)-range;z<=(at.getZ()>>4)+range;z++){
   var chunk=world.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;
   var start=chunk.getStartForStructure(type);if(start==null||!start.isValid())continue;
   var pos=start.getBoundingBox().getCenter();double d=pos.distSqr(at);if(d<distance){distance=d;nearest=pos;}
  }
  if(nearest==null){source.sendFailure(Component.literal(id+": существующий старт не найден в загруженных чанках (радиус "+radius+"). Неизвестные чанки не загружались; строительство не запускалось."));return 0;}
  var result=nearest;source.sendSuccess(()->Component.literal(id+": существующая структура "+result.toShortString()),false);return 1;
 }
 private static boolean validLoadedRecord(net.minecraft.server.level.ServerLevel world,net.minecraft.world.level.levelgen.structure.Structure type,BlockPos arrival){
  if(type==null)return false;
  var chunk=world.getChunkSource().getChunkNow(arrival.getX()>>4,arrival.getZ()>>4);if(chunk==null)return true;
  var refs=chunk.getAllReferences().get(type);if(refs==null||refs.isEmpty())return false;
  for(long ref:refs){var pos=new net.minecraft.world.level.ChunkPos(ref);var owner=world.getChunkSource().getChunkNow(pos.x,pos.z);if(owner==null)return true;var start=owner.getStartForStructure(type);if(start!=null&&start.isValid())return true;}
  return false;
 }
 private ExistingSiteSearch(){}
}
