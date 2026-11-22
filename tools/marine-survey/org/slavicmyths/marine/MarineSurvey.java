package org.slavicmyths.marine;
import com.mojang.authlib.GameProfile;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class MarineSurvey {
 static FakePlayer player; static ServerLevel world; static int ticks,site,allMobs; static final List<BlockPos> sites=new ArrayList<>();
 static final Set<UUID> seen=new HashSet<>(); static final Map<String,Integer> counts=new TreeMap<>(); static final List<String> rows=new ArrayList<>();
 @SubscribeEvent public static void start(ServerStartedEvent e){
  world=e.getServer().overworld(); e.getServer().tickRateManager().setTickRate(100F);
  for(int i=0;i<0;i++)for(boolean river:new boolean[]{false,true}){
   var found=world.findClosestBiome3d(b->b.is(river?BiomeTags.IS_RIVER:BiomeTags.IS_DEEP_OCEAN),new BlockPos(i*6000,63,i*2000),5000,64,64);
   if(found!=null)sites.add(found.getFirst().atY(64));
  }
  var wreck=world.getChunkSource().getGenerator().findNearestMapStructure(world,
      net.minecraft.core.HolderSet.direct(world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getHolderOrThrow(net.minecraft.world.level.levelgen.structure.BuiltinStructures.SHIPWRECK)),
      BlockPos.ZERO,100,false);
  if(wreck!=null)sites.add(wreck.getFirst().atY(64));
  if(sites.isEmpty())throw new IllegalStateException("No survey biomes");
  player=new FakePlayer(world,new GameProfile(UUID.fromString("69b38822-ae08-443c-8a58-c0422786c372"),"MarineSurvey"));
  player.setGameMode(GameType.CREATIVE);player.moveTo(sites.get(0).getX()+.5,64,sites.get(0).getZ()+.5);
  world.addNewPlayer(player);
  world.getChunkSource().setViewDistance(8);world.getChunkSource().setSimulationDistance(8);
 }
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){
  if(e.getLevel()==world && e.getEntity() instanceof Mob) allMobs++;
  if(e.getLevel()!=world||!(e.getEntity() instanceof Mob mob)||!mob.getType().toString().matches(".*(ocean_zmey|deep_sea_serpent|tsar_pike|zhelva|stratim_bird|pharaohka).*"))return;
  if(seen.add(mob.getUUID())) {
   counts.merge(mob.getType().toString(),1,Integer::sum);
   String detail="SPAWN seed="+world.getSeed()+" type="+mob.getType()+" pos="+mob.blockPosition()+" biome="+world.getBiome(mob.blockPosition()).unwrapKey();
   rows.add(detail);System.out.println("MARINE_SURVEY "+detail);
  }
 }
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){
  if(player==null)return;
  var foundation=org.slavicmyths.buyan.BuyanState.get(world);
  var nature=org.slavicmyths.buyan.BuyanNatureState.get(world);
  if(foundation.paused||nature.paused)throw new IllegalStateException("Buyan generation paused: "+foundation.lastError+" "+nature.lastError);
  if(foundation.active||nature.active)return;
  if(++ticks%1200!=0)return;
  int land=0;
  for(var island:foundation.completedSites())for(int dx:new int[]{-80,0,80})for(int dz:new int[]{-80,0,80}) {
   var sample=new BlockPos(island.x()+dx,island.waterLevel()+2,island.z()+dz);world.getChunk(sample);
   if(!world.getBlockState(sample).isAir()&&world.getFluidState(sample).isEmpty())land++;
  }
  if(foundation.completedSites().isEmpty()||nature.completedCount()==0||land<5)throw new IllegalStateException("Buyan not physically completed: land="+land);
  rows.add("BUYAN_LAND samples="+land);
  rows.add("BUYAN foundations="+foundation.completedSites()+" natureCompleted="+nature.completedCount());
  var pos=sites.get(site);rows.add("seed="+world.getSeed()+" site="+site+" pos="+pos+" actualPlayer="+player.blockPosition()+" biome="+world.getBiome(player.blockPosition()).unwrapKey()+" ticks=1200 allMobJoins="+allMobs+" spawnState="+(world.getChunkSource().getLastSpawnState()!=null)+" uniqueSpawns="+counts);
  System.out.println("MARINE_SURVEY "+rows.getLast());counts.clear();allMobs=0;
  rows.add("WRECK_DEBUG "+world.getChunk(pos).getAllStarts().keySet()+" data="+PharaohkaShipwreckSpawns.get(world).save(new net.minecraft.nbt.CompoundTag(),world.registryAccess()));
  if(++site>=sites.size()){
   try{Files.write(Path.of("marine-survey-results.txt"),rows);}catch(Exception ex){throw new RuntimeException(ex);}
   player=null;e.getServer().halt(false);return;
  }
  var next=sites.get(site);player.moveTo(next.getX()+.5,64,next.getZ()+.5);world.getChunkSource().move(player);
 }
}