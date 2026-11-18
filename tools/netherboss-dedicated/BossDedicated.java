package org.slavicmyths.bossdedicated;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.common.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.netherboss.*;
@Mod("slavicmyths_bossdedicated")
@EventBusSubscriber(modid="slavicmyths_bossdedicated")
public final class BossDedicated {
 private static int ticks;private static final Properties ids=new Properties();
 private static Path manifest(MinecraftServer s){return s.getWorldPath(LevelResource.ROOT).resolve("boss-restart-qa.properties");}
 private static void check(boolean result,String msg){if(!result)throw new AssertionError(msg);}
 @SubscribeEvent public static void started(ServerStartedEvent e)throws Exception{
  var s=e.getServer();check(s.isDedicatedServer(),"Not a real dedicated server");
  var w=s.overworld();w.setChunkForced(4,4,true);w.getChunk(4,4);
  if(System.getProperty("bossqa.stage").equals("write")){
   check(!Files.exists(manifest(s)),"Refusing to reuse existing restart fixture");
   for(int x=64;x<80;x++)for(int z=64;z<80;z++)w.setBlock(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState(),2);
   for(boolean morova:new boolean[]{true,false}){
    var b=(morova?ModEntities.MOROVA_DEVA:ModEntities.TRIZNA_PRINCE).get().create(w);b.moveTo(morova?68:75,100,68,0,0);b.setNoAi(true);b.setHealth(morova?900:1200);b.setPersistenceRequired();
    b.getPersistentData().putString("BossQaPersist","disk-restart");w.addFreshEntity(b);ids.setProperty(morova?"morova":"trizna",b.getUUID().toString());
    var r=ModEntities.NETHER_RETAINER.get().create(w);r.configure(b,morova?30:50,morova?5:7,1200);r.moveTo(morova?68:75,100,74,0,0);w.addFreshEntity(r);ids.setProperty(morova?"shadow":"retainer",r.getUUID().toString());
   }
   var mob=EntityType.HUSK.create(w);mob.moveTo(71,100,71);mob.setPersistenceRequired();mob.setNoAi(true);
   mob.getPersistentData().putLong("MorovaSilenceUntil",s.overworld().getGameTime()+240);mob.getPersistentData().putInt("MorovaSilenceDebuff",300);mob.getPersistentData().putBoolean("MorovaSilencePriorNoAI",false);w.addFreshEntity(mob);ids.setProperty("silenced",mob.getUUID().toString());
   var ledger=NetherBossLedger.get(w);var row=new net.minecraft.nbt.CompoundTag();row.putString("State","RESERVED");ledger.records.put("restart-interrupted-ritual",row);ledger.flush(w);
   try(var out=Files.newOutputStream(manifest(s))){ids.store(out,"Disposable actual dedicated server save/restart fixture");}
  }else try(var in=Files.newInputStream(manifest(s))){ids.load(in);}
 }
 @SubscribeEvent public static void tick(ServerTickEvent.Post e)throws Exception{
  var s=e.getServer();if(++ticks<80)return;var w=s.overworld();String stage=System.getProperty("bossqa.stage");
  if(stage.equals("write")){
   s.saveEverything(true,true,true);System.out.println("BOSS_DEDICATED_WRITE_PASS actual entities/chunks/SavedData written to disk; clean shutdown");s.halt(false);return;
  }
  if(ticks==80){
   for(boolean morova:new boolean[]{true,false}){
    var b=(NetherSummonBoss)w.getEntity(UUID.fromString(ids.getProperty(morova?"morova":"trizna")));
    check(b!=null&&b.getHealth()==(morova?900:1200)&&b.getMaxHealth()==(morova?1350:1800)&&b.getPersistentData().getString("BossQaPersist").equals("disk-restart"),"Actual boss disk persistence "+morova);
    var r=(NetherRetainer)w.getEntity(UUID.fromString(ids.getProperty(morova?"shadow":"retainer")));
    check(r!=null&&r.isAlive()&&r.owner().equals(b.getUUID())&&r.getMaxHealth()==(morova?30:50)&&r.shadow()==morova,"Actual minion disk persistence "+morova);
   }
   check(NetherBossLedger.get(w).records.getCompound("restart-interrupted-ritual").getString("State").equals("ENDED"),"Interrupted reservation not released on actual disk reload");
   var mob=(net.minecraft.world.entity.Mob)w.getEntity(UUID.fromString(ids.getProperty("silenced")));check(mob!=null&&mob.isNoAi(),"Silence did not survive actual chunk save");
   System.out.println("BOSS_DEDICATED_RESTART_PASS two actual bosses + two retainers + silence restored from entity region files; interrupted reservation ended");
  }
  if(ticks==260){
   var mob=(net.minecraft.world.entity.Mob)w.getEntity(UUID.fromString(ids.getProperty("silenced")));check(mob!=null&&!mob.isNoAi()&&!mob.getPersistentData().contains("MorovaSilenceUntil"),"Reloaded silence tracker did not restore AI");
   System.out.println("BOSS_DEDICATED_CONTROL_RELOAD_PASS real restored entity control expires without permanent NoAI");
  }
  if(ticks>=1250){
   check(w.getEntity(UUID.fromString(ids.getProperty("shadow")))==null&&w.getEntity(UUID.fromString(ids.getProperty("retainer")))==null,"Retainer expiry leaked an entity after disk reload");
   w.setChunkForced(4,4,false);s.saveEverything(true,true,true);System.out.println("BOSS_DEDICATED_TTL_PASS actual 1200 tick expiry after reload, no duplicate minions; fixture complete");s.halt(false);
  }
 }
}
