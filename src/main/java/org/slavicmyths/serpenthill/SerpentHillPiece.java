package org.slavicmyths.serpenthill;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
public final class SerpentHillPiece extends StructurePiece {
 public final BlockPos origin;public final Rotation rotation;public final long seed;public final UUID instance;
 private HillPlan plan;private final Set<Long> chestMarkers=new HashSet<>();
 public SerpentHillPiece(BlockPos origin,Rotation r,long seed,UUID id){super(HillContent.PIECE.get(),0,HillGeometry.bounds(origin,r));this.origin=origin;rotation=r;this.seed=seed;instance=id;}
 public SerpentHillPiece(CompoundTag n){super(HillContent.PIECE.get(),n);origin=BlockPos.of(n.getLong("Origin"));rotation=Rotation.valueOf(n.getString("Rotation"));seed=n.getLong("Seed");instance=n.getUUID("Instance");for(long p:n.getLongArray("ChestMarkers"))chestMarkers.add(p);}
 public synchronized HillPlan plan(){if(plan==null)plan=new HillPlan(seed);return plan;}
 @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag n){n.putLong("Origin",origin.asLong());n.putString("Rotation",rotation.name());n.putLong("Seed",seed);n.putUUID("Instance",instance);n.putLongArray("ChestMarkers",chestMarkers.stream().mapToLong(Long::longValue).toArray());}
 @Override public synchronized void postProcess(WorldGenLevel w,StructureManager manager,net.minecraft.world.level.chunk.ChunkGenerator generator,net.minecraft.util.RandomSource random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){var p=plan();var cursor=new BlockPos.MutableBlockPos();for(int x=Math.max(clip.minX(),boundingBox.minX());x<=Math.min(clip.maxX(),boundingBox.maxX());x++)for(int z=Math.max(clip.minZ(),boundingBox.minZ());z<=Math.min(clip.maxZ(),boundingBox.maxZ());z++){var local=HillGeometry.local(origin,rotation,new BlockPos(x,origin.getY(),z));if(!p.authored(local.getX(),local.getZ()))continue;for(int y=Math.max(clip.minY(),boundingBox.minY());y<=Math.min(clip.maxY(),boundingBox.maxY());y++){cursor.set(x,y,z);if(w.getBlockState(cursor).is(Blocks.BEDROCK))continue;var next=HillContent.palette.state(p.state(local.getX(),y-origin.getY(),local.getZ())).rotate(rotation);if(!w.getBlockState(cursor).equals(next))w.setBlock(cursor,next,2);}}
  for(var c:p.rewardContainers()){var at=origin.offset(new BlockPos(c.x(),c.y(),c.z()).rotate(rotation));if(clip.isInside(at)&&chestMarkers.add(at.asLong())&&w.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity chest&&chest.getLootTable()==null&&chest.isEmpty())chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,HillContent.id(org.slavicmyths.loot.OverworldRewards276.fortress(c.x(),c.y(),c.z(),w.getBlockState(at).is(Blocks.BARREL)))),instance.getMostSignificantBits()^at.asLong());}
  // Population may run on a worker. Only the server thread mutates the spatial index.
  var level=w.getLevel();level.getServer().execute(()->{HillWorld.get(level).ensure(instance,origin,rotation,seed);org.slavicmyths.gorynych.GorynychWorld.ensure(level,instance);});
 }
}
