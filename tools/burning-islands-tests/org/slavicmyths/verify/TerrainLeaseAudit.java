package org.slavicmyths.verify;
import java.nio.file.*;import java.util.*;import net.minecraft.core.*;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.nbt.*;import net.minecraft.world.level.block.*;import org.slavicmyths.burningislands.TerrainLeaseRules;
public final class TerrainLeaseAudit {
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();int checks=0;
  for(var original:List.of(Blocks.BLACKSTONE.defaultBlockState(),Blocks.BASALT.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,Direction.Axis.X),Blocks.OBSIDIAN.defaultBlockState(),Blocks.AIR.defaultBlockState())){
   var placed=original.isAir()?Blocks.FIRE.defaultBlockState():Blocks.MAGMA_BLOCK.defaultBlockState();var entry=TerrainLeaseRules.create(original,placed,new UUID(0,1),120);var image=new CompoundTag();image.put("Block",NbtUtils.writeBlockState(placed));image.put("Lease",entry);Path file=Files.createTempFile("slavic-terrain-lease-",".nbt");NbtIo.writeCompressed(image,file);var saved=NbtIo.readCompressed(file,NbtAccounter.unlimitedHeap());
   if(!TerrainLeaseRules.mayRestore(saved.getCompound("Lease"),NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),saved.getCompound("Block"))))throw new AssertionError("Coupled block/lease image");checks++;
   if(!NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),saved.getCompound("Lease").getCompound("Original")).equals(original))throw new AssertionError("Exact original state including properties");checks++;
   if(TerrainLeaseRules.expired(entry,119)||!TerrainLeaseRules.expired(entry,120)||TerrainLeaseRules.mayRestore(entry,Blocks.DIAMOND_BLOCK.defaultBlockState()))throw new AssertionError("Expiry/replacement conflict");checks+=3;
   var corrupt=entry.copy();corrupt.remove("Owner");if(TerrainLeaseRules.mayRestore(corrupt,placed))throw new AssertionError("Missing owner accepted");checks++;Files.delete(file);
  }
  System.out.println("Terrain leases native binary NBT/state/expiry/conflict PASS "+checks+" checks; actual chunk unload/tick/claims/relog/MP NOT_RUN.");
 }
}
