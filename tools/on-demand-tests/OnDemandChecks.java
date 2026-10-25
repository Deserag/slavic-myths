import java.util.*;
public final class OnDemandChecks {
 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  var random=net.minecraft.util.RandomSource.create(42);
  for(int i=0;i<100;i++){long next=org.slavicmyths.firebird.FirebirdRules.nextNight(10,random);if(next<12||next>14)throw new AssertionError("firebird interval");}
  if(!org.slavicmyths.firebird.FirebirdRules.window(22000)||org.slavicmyths.firebird.FirebirdRules.window(12000))throw new AssertionError("firebird night window");
  if(!org.slavicmyths.mor.MorRules.natural(true,48000,30000,true,11)||org.slavicmyths.mor.MorRules.natural(true,47999,30000,true,0)||org.slavicmyths.mor.MorRules.natural(true,48000,30000,true,12))throw new AssertionError("mor encounter boundary");
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  // Isolated vanilla registry fixture with the six actual hut block implementations.
  var registry=(net.minecraft.core.MappedRegistry<net.minecraft.world.level.block.Block>)net.minecraft.core.registries.BuiltInRegistries.BLOCK;registry.unfreeze();
  registerBlock("yaga_cauldron",new org.slavicmyths.yaga.YagaCauldronBlock());registerBlock("yaga_chicken_leg",new org.slavicmyths.yaga.YagaLegBlock());
  registerBlock("pine_table",new org.slavicmyths.furniture.TableBlock());
  for(String name:List.of("yaga_dried_herbs","yaga_bone_charm","ritual_candle"))registerBlock(name,new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noCollission().noOcclusion()));
  var plant=Class.forName("org.slavicmyths.registry.ModBlocks$WildPlantBlock").getDeclaredConstructors()[0];plant.setAccessible(true);registerBlock("wormwood",(net.minecraft.world.level.block.Block)plant.newInstance(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noCollission().noOcclusion()));registry.freeze();
  registry.bindTags(Map.of(net.minecraft.tags.BlockTags.DIRT,List.of(net.minecraft.world.level.block.Blocks.GRASS_BLOCK.builtInRegistryHolder(),net.minecraft.world.level.block.Blocks.DIRT.builtInRegistryHolder(),net.minecraft.world.level.block.Blocks.PODZOL.builtInRegistryHolder(),net.minecraft.world.level.block.Blocks.COARSE_DIRT.builtInRegistryHolder()),net.minecraft.tags.BlockTags.MUSHROOM_GROW_BLOCK,List.of(net.minecraft.world.level.block.Blocks.PODZOL.builtInRegistryHolder())));
org.slavicmyths.verify.YagaPlacementHeadless.main(args);org.slavicmyths.chud.RasterChecks.run();
 var progress=new org.slavicmyths.yaga.YagaData.Progress();progress.active="main:0";
 if(progress.recognizeVictory(false)||!progress.recognizeVictory(true)||progress.stage!=1||!progress.active.isEmpty()||progress.recognizeVictory(true))throw new AssertionError("victory recognition/idempotence");
 var yaga=new org.slavicmyths.yaga.YagaData();var player=UUID.randomUUID();yaga.players.put(player,progress);var restored=new org.slavicmyths.yaga.YagaData();restored.load(yaga.save(new net.minecraft.nbt.CompoundTag(),null));if(restored.progress(player).recognizeVictory(true)||restored.progress(player).stage!=1)throw new AssertionError("recognition persisted without repeat reward");
 var records=new org.slavicmyths.worldgen.ManualStructureRecords();var id=UUID.randomUUID();var at=new net.minecraft.core.BlockPos(-32,40,60);var box=new net.minecraft.world.level.levelgen.structure.BoundingBox(-40,-10,50,0,90,100);records.add(id,"construction",0,at,box);records.replace(id,"chud_city",0,at,box);var copy=org.slavicmyths.worldgen.ManualStructureRecords.load(records.save(new net.minecraft.nbt.CompoundTag(),null));if(copy.entries().size()!=1||copy.overlapping(box).size()!=1||!copy.entries().getFirst().family().equals("chud_city"))throw new AssertionError("completion record persistence/index");
 System.out.println("PASS: completed construction index survives SavedData roundtrip (not real world restart).");
 }
 static void registerBlock(String name,net.minecraft.world.level.block.Block block){net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",name),block);}
}
