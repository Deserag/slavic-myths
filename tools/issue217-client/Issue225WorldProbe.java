package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Fresh NORMAL world, real structure starts/chunks/containers. Opt-in QA source set only. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class Issue225WorldProbe {
 static boolean opened,working,done;static int stage,ticks;static final List<Map<String,Object>> rows=new ArrayList<>();
 static final String[] IDS={"kurgan_small","sanctuary_unfading_fire"};
 static final Path OUT=Path.of("D:/slavic-myths/docs/implementation/issue225/natural-world");
 @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
  if(!Boolean.getBoolean("slavicmyths.issue225.world"))return;var mc=Minecraft.getInstance();
  if(!opened&&mc.screen instanceof TitleScreen){opened=true;Files.createDirectories(OUT);mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(.2D);if(Boolean.getBoolean("slavicmyths.issue225.reload")){mc.createWorldOpenFlows().openWorld(Files.readString(OUT.resolve("last-world.txt")).trim(),()->{});return;}String worldName="Issue225-Normal-"+System.currentTimeMillis();Files.writeString(OUT.resolve("last-world.txt"),worldName);mc.createWorldOpenFlows().createFreshLevel(worldName,new LevelSettings("Issue225 natural generation QA",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(2252026L,true,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.NORMAL).value().createWorldDimensions(),mc.screen);return;}
  if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||working)return;
  if(done){if(++ticks<160)return;try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(IDS[stage]+"-native.png"));}stage++;done=false;ticks=0;if(stage==IDS.length){Files.writeString(OUT.resolve(Boolean.getBoolean("slavicmyths.issue225.reload")?"results-reload.json":"results-fresh.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));System.out.println("ISSUE225_NORMAL_WORLD_PASS seed=2252026 structures="+rows.size());mc.stop();return;}}
  if(++ticks<40)return;working=true;String id=IDS[stage];
  mc.getSingleplayerServer().execute(()->{try{
   var world=mc.getSingleplayerServer().overworld();var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,world.getServer());world.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,world.getServer());world.setDayTime(6000);player.setNoGravity(true);
   var registry=world.registryAccess().registryOrThrow(Registries.STRUCTURE);var holder=registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));
   var pos=id.equals("kurgan_small")?new BlockPos(-720,0,-640):new BlockPos(-2240,0,1952); // Prior native locate in this exact seed; replay avoids repeating expensive search.
   if(stage==0){var sentinel=new BlockPos(16,200,16);if(!Boolean.getBoolean("slavicmyths.issue225.reload")){world.setBlockAndUpdate(sentinel,org.slavicmyths.furniture.Furniture.get("wooden_crate").defaultBlockState());var boxEntity=world.getBlockEntity(sentinel);if(!(boxEntity instanceof Container storage))throw new AssertionError("Native crate has no storage");storage.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,3));boxEntity.setChanged();}else{world.getChunk(1,1);var boxEntity=world.getBlockEntity(sentinel);if(!(boxEntity instanceof Container storage)||!storage.getItem(0).is(net.minecraft.world.item.Items.DIAMOND)||storage.getItem(0).getCount()!=3)throw new AssertionError("Real process restart lost crate contents");System.out.println("ISSUE225_ACTUAL_PROCESS_RELOG_CRATE_PASS diamonds=3");}}var cp=new ChunkPos(pos);var chunk=world.getChunk(cp.x,cp.z);var start=chunk.getAllStarts().get(holder.value());if(start==null||!start.isValid())throw new AssertionError("Located structure has no valid generated start: "+id);
   var box=start.getBoundingBox();int chunks=0,containers=0;var loot=new ArrayList<String>();
   if((box.maxX()-box.minX())>160||(box.maxZ()-box.minZ())>160)throw new AssertionError("Structure exceeds bounded QA footprint: "+id);
   for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++){
    var generated=world.getChunk(x,z);chunks++;for(var be:generated.getBlockEntities().values())if(box.isInside(be.getBlockPos())&&be instanceof Container){containers++;var tag=be.saveWithFullMetadata(world.registryAccess());loot.add(be.getBlockPos()+" "+tag.getString("id")+" loot="+tag.getString("LootTable"));}
   }
   var row=new LinkedHashMap<String,Object>();row.put("id",id);row.put("seed",2252026L);row.put("preset","NORMAL");row.put("coordinates_source","prior native locate seed2252026; current native generation/start rechecked");row.put("location",List.of(pos.getX(),pos.getY(),pos.getZ()));row.put("bounding_box",List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()));row.put("native_biome",world.getBiome(new BlockPos(pos.getX(),box.minY(),pos.getZ())).unwrapKey().orElseThrow().location().toString());row.put("generated_chunks",chunks);row.put("containers",containers);row.put("container_metadata",loot);row.put("scope","natural generation/start/chunks/container presence; no forced loot unlock or boss victory");rows.add(row);
   player.teleportTo(world,(box.minX()+box.maxX())*.5,box.maxY()+14,box.maxZ()+28,180,24);System.out.println("ISSUE225_NORMAL_STRUCTURE="+id+" pos="+pos+" chunks="+chunks+" containers="+containers);mc.execute(()->{working=false;done=true;ticks=0;});
  }catch(Throwable error){error.printStackTrace();System.err.println("ISSUE225_NORMAL_WORLD_FAIL "+id+" "+error);try{Files.writeString(OUT.resolve("FAIL.txt"),error.toString());}catch(Exception ignored){}mc.execute(mc::stop);}});
 }
}
