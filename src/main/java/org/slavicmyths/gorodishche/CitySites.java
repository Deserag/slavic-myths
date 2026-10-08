package org.slavicmyths.gorodishche;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
/** One raid-start lookup of loaded native references or indexed manual records. No chunk loads. */
public final class CitySites {
 public record Site(BoundingBox bounds,BlockPos approach,Direction outward){}
 public static Optional<Site> find(ServerLevel level,BlockPos anchor){
  for(var entry:org.slavicmyths.worldgen.ManualStructureRecords.get(level).overlapping(new BoundingBox(anchor)))if(entry.family().equals("gorodishche"))return Optional.of(new Site(entry.bounds(),entry.arrival().south(10),Direction.SOUTH));
  var structure=level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths","gorodishche"));if(structure==null)return Optional.empty();var local=level.getChunkSource().getChunkNow(anchor.getX()>>4,anchor.getZ()>>4);if(local==null)return Optional.empty();var starts=new LinkedHashSet<net.minecraft.world.level.levelgen.structure.StructureStart>();var own=local.getStartForStructure(structure);if(own!=null&&own.isValid())starts.add(own);
  for(long ref:local.getReferencesForStructure(structure)){var cp=new ChunkPos(ref);var loaded=level.getChunkSource().getChunkNow(cp.x,cp.z);if(loaded!=null){var start=loaded.getStartForStructure(structure);if(start!=null&&start.isValid())starts.add(start);}}
  for(var start:starts){var box=start.getBoundingBox();if(!box.isInside(anchor))continue;for(var piece:start.getPieces())if(piece instanceof CityBuildingPiece building){var gate=building.gateApproach();if(gate.isPresent())return Optional.of(new Site(box,gate.get(),building.facing()));}}return Optional.empty();
 }
 private CitySites(){}
}
