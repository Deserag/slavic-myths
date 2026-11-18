package org.slavicmyths.bossaudit;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
@GameTestHolder("slavicmyths_bossaudit") @PrefixGameTestTemplate(false)
public final class AiStressAudit {
 record Actor(String id,Mob mob,ServerPlayer player,BlockPos anchor,Set<String> states,int[] hits,int[] changes,String[] last){}
 static String state(Mob m){try{
  String c=m.getClass().getSimpleName();
  if(c.equals("AgneyaBoss"))return "swing:"+InterruptionAudit.field(m.getClass(),"swingTick").getInt(m);
  if(c.equals("LedeyaBoss")){float ticks=(float)m.getClass().getMethod("attackTicks",float.class).invoke(m,0F);return ticks<0?"IDLE":m.getClass().getMethod("attack").invoke(m).toString();}
  for(String name:new String[]{"action","move","pose","state"})try{return name+":"+m.getClass().getMethod(name).invoke(m);}catch(NoSuchMethodException ignored){}
  if(c.equals("NineMorSerpent")){var b=new StringBuilder();for(var f:m.getClass().getDeclaredFields())if(f.getName().equals("HEADS")){f.setAccessible(true);for(Object a:(Object[])f.get(null))b.append(m.getEntityData().get((net.minecraft.network.syncher.EntityDataAccessor<Integer>)a)).append(',');}return b.toString();}
  return "UNINSTRUMENTED";
 }catch(Exception ex){return "ERROR:"+ex;}}
 @GameTest(template="empty",timeoutTicks=760,batch="ai_stress")
 public static void repeatedHitsThirtySeconds(GameTestHelper t)throws Exception{
  var w=t.getLevel();var actors=new ArrayList<Actor>();
  String[] ids={"gneteya","tryaseya","agneya","ledeya","lomeya","neveya","viy","morovoy_raven","morovoy_fire_zmey","nine_mor_serpent","cursed_mytar","bauk"};
  int i=0;
  for(String id:ids){BlockPos at=new BlockPos(t.absolutePos(new BlockPos(3,180,3))).offset(i++*256,0,0);
   for(int cx=-1;cx<=1;cx++)for(int cz=-1;cz<=1;cz++){var cp=new net.minecraft.world.level.ChunkPos(at.offset(cx*16,0,cz*16));w.setChunkForced(cp.x,cp.z,true);w.getChunk(cp.x,cp.z);}
   for(int x=-16;x<=16;x++)for(int z=-16;z<=16;z++)w.setBlock(at.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
   var p=AuditPlayers.create(w,"Stress"+i);p.setGameMode(GameType.SURVIVAL);p.getAbilities().invulnerable=true;p.setNoGravity(true);p.setPos(at.getX()+.5,at.getY()+1,at.getZ()+5);w.addNewPlayer(p);
   var m=(Mob)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).create(w);m.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);w.addFreshEntity(m);m.setTarget(p);
   try{m.getClass().getMethod("setArenaAnchor",BlockPos.class).invoke(m,at);}catch(NoSuchMethodException ignored){}
   actors.add(new Actor(id,m,p,at,new HashSet<>(),new int[]{0},new int[]{0},new String[]{""}));
  }
  long start=System.nanoTime();
  t.onEachTick(()->{
   long tick=t.getTick();for(var a:actors){String s=state(a.mob);a.states.add(s);if(!s.equals(a.last[0]))a.changes[0]++;a.last[0]=s;
    if(tick%10==0&&tick<600&&a.mob.isAlive()){float before=a.mob.getHealth();a.player.attack(a.mob);if(a.mob.getHealth()<before)a.hits[0]++;}
   }
  });
  t.runAfterDelay(610,()->{try{
   var rows=new JsonArray();for(var a:actors){var row=new JsonObject();row.addProperty("id",a.id);row.addProperty("alive",a.mob.isAlive());row.addProperty("world_ticks",a.mob.tickCount);row.addProperty("accepted_native_player_hits",a.hits[0]);row.addProperty("state_changes",a.changes[0]);row.addProperty("states_observed",String.join(";",a.states));row.addProperty("no_gravity",a.mob.isNoGravity());row.addProperty("health_end",a.mob.getHealth());row.addProperty("x_displacement",a.mob.getX()-a.anchor.getX());row.addProperty("y_displacement",a.mob.getY()-a.anchor.getY());row.addProperty("wall_seconds",(System.nanoTime()-start)/1e9);row.addProperty("scope","Native AI and native Player.attack with invulnerable Survival ServerPlayer proxy; NOT network MP; does not certify full attack completion");rows.add(row);a.mob.discard();a.player.discard();for(int cx=-1;cx<=1;cx++)for(int cz=-1;cz<=1;cz++){var cp=new net.minecraft.world.level.ChunkPos(a.anchor.offset(cx*16,0,cz*16));w.setChunkForced(cp.x,cp.z,false);}}
   var out=Path.of(System.getProperty("bossaudit.output"));Files.createDirectories(out);Files.writeString(out.resolve("ai-stress.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));System.out.println("BOSS_AUDIT_STRESS_COLLECTION actors="+actors.size()+" ticks=610 NOT_FULL_ATTACK_QA");t.succeed();
  }catch(Exception ex){t.fail(ex.toString());}});
 }
}
