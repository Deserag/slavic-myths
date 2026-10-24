package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.bogatyr.*;
import org.slavicmyths.skim.HeroCalls;
import org.slavicmyths.yaga.*;
@EventBusSubscriber(modid="slavicmyths")
public final class NetworkServer {
 static final Path OUT=Path.of("D:/slavic-myths/docs/implementation/heroes-coop");
 static int tick,phase,heroIndex;static boolean stopped;static final List<String> results=new ArrayList<>();
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);results.add("PASS "+message);System.out.println("HERO_NETWORK_PASS "+message);}
 @SubscribeEvent public static void tick(ServerTickEvent.Post e)throws Exception{
  if(stopped||++tick%20!=0)return;var server=e.getServer();var players=server.getPlayerList().getPlayers();if(players.size()<2){if(tick>2400)finish(server,"FAIL waiting for two TCP clients");return;}
  var a=players.get(0);var b=players.get(1);var w=server.overworld();
  try{
   if(phase==0){check(!a.connection.getConnection().isMemoryConnection()&&!b.connection.getConnection().isMemoryConnection(),"two actual TCP connections");
    if(Files.exists(OUT.resolve("restart-ready.txt"))){for(var p:players)for(String hero:HeroIntroductions.IDS)check(HeroIntroductions.unlocked(p,hero),"disk restart personal help persists: "+p.getScoreboardName()+"/"+hero);check(HeroCalls.serverCount(w)<=2,"disk restart global cap");finish(server,"PASS_RESTART");return;}
    var pos=w.getSharedSpawnPos();int y=w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.getX(),pos.getZ());for(var p:players){p.setGameMode(GameType.SURVIVAL);p.teleportTo(w,pos.getX()+.5,y,pos.getZ()+.5,0,0);YagaData.get(w).progress(p.getUUID()).stage=1;}
    phase=1;return;
   }
   if(phase==1){String hero=HeroIntroductions.IDS.get(heroIndex);check(HeroIntroductions.offer(a,"yaga",hero)&&HeroIntroductions.offer(b,"yaga",hero),"both players receive "+hero+" direction");var qa=HeroIntroductions.state(a,hero);check(qa.getUUID("Npc").equals(HeroIntroductions.state(b,hero).getUUID("Npc")),"one shared meeting for "+hero);var npc=(Mob)w.getEntity(qa.getUUID("Npc"));for(var p:players){p.teleportTo(w,npc.getX()+2,npc.getY(),npc.getZ(),180,0);HeroIntroductions.interact(npc,p,InteractionHand.MAIN_HAND);}phase=2;return;}
   if(phase==2){String hero=HeroIntroductions.IDS.get(heroIndex);var npc=(Mob)w.getEntity(HeroIntroductions.state(a,hero).getUUID("Npc"));b.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BREAD,2));HeroIntroductions.interact(npc,b,InteractionHand.MAIN_HAND);a.getInventory().clearContent();a.getInventory().add(new ItemStack(Items.BREAD,4));a.getInventory().add(new ItemStack(Items.HONEY_BOTTLE));a.getInventory().add(new ItemStack(Items.IRON_INGOT,4));a.getInventory().selected=8;HeroIntroductions.interact(npc,a,InteractionHand.MAIN_HAND);check(HeroIntroductions.unlocked(a,hero)&&HeroIntroductions.unlocked(b,hero),"personal unlock for both contributors: "+hero);HeroIntroductions.offer(b,"yaga",hero);if(++heroIndex<4)phase=1;else phase=3;return;}
   if(phase==3){check(HeroCalls.toggle(a,"ilya_muromets"),"first actual hero spawned");check(HeroCalls.toggle(b,"dobrynya_nikitich"),"second hero spawned by other player");check(!HeroCalls.toggle(a,"finist")&&HeroCalls.serverCount(w)==2,"third hero denied across players");phase=4;return;}
   if(phase==4){check(HeroCalls.toggle(a,"ilya_muromets"),"first owner recalls hero");phase=5;return;}
   if(phase==5){if(HeroCalls.serverCount(w)!=1)return;check(HeroCalls.toggle(a,"finist")&&HeroCalls.serverCount(w)==2,"released global slot can be reused");Files.createDirectories(OUT);Files.writeString(OUT.resolve("restart-ready.txt"),"Reopen same dedicated test save with both offline UUIDs\n");finish(server,"PASS_INITIAL");}
  }catch(Throwable failure){failure.printStackTrace();finish(server,"FAIL "+failure);}
 }
 static void finish(net.minecraft.server.MinecraftServer server,String result)throws Exception{stopped=true;Files.createDirectories(OUT);Files.writeString(OUT.resolve(result.equals("PASS_RESTART")?"network-restart.txt":"network-initial.txt"),String.join("\n",results)+"\n"+result+"\n");server.halt(false);}
}
