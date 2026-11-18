package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.chud.*;

/** Tests the actual story placement queue; deliberately not an NPC/Survival acceptance claim. */
@EventBusSubscriber(modid="slavicmyths")
public final class Story300 {
 static ServerLevel world;static long began;static boolean done,callback;static int phase,initial;
 static ChudCreationJobs.Result result;
 static final BlockPos ORIGIN=new BlockPos(Integer.getInteger("slavicmyths.qa300.storyX",12000),80,Integer.getInteger("slavicmyths.qa300.storyZ",12000));
 static final Map<String,Object> out=new LinkedHashMap<>();
 @SubscribeEvent public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent e){
  if(!"story".equals(System.getProperty("slavicmyths.qa300")))return;world=e.getServer().overworld();began=System.nanoTime();
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){if(world==null||done)return;try{
  if(System.nanoTime()-began>900_000_000_000L)throw new IllegalStateException("15min service bound");
  var source=world.getServer().createCommandSourceStack().withLevel(world).withPosition(net.minecraft.world.phys.Vec3.atCenterOf(ORIGIN)).withPermission(0);
  if(phase==0){initial=org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().size();ChudCreationJobs.createStory(source,r->{if(r!=null)throw new IllegalStateException("cancel published success");callback=true;});ChudCreationJobs.control(source,true);if(!callback||initial!=org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().size())throw new IllegalStateException("early cancel leaked reservation");out.put("cancel_before_write","PASS_NO_SITE_NO_RESERVATION");out.put("actual_site_tickets_after_cancel",TicketEvidence300.siteBuild(world));if(TicketEvidence300.siteBuild(world)!=0)throw new IllegalStateException("cancel leaked actual region tickets");callback=false;ChudCreationJobs.createStory(source,r->{result=r;callback=true;});phase=1;return;}
  if(!callback)return;if(result==null)throw new IllegalStateException("story placement exhausted safe candidates; see runtime reason");
  double distance=Math.hypot(result.entrance().getX()-ORIGIN.getX(),result.entrance().getZ()-ORIGIN.getZ());if(distance<1200||distance>3000)throw new IllegalStateException("story mouth outside distance: "+distance);
  var root=world.getChunkSource().getChunkNow(result.owner().x,result.owner().z);var type=world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));var start=root.getStartForStructure(type);if(start==null||!start.isValid()||start.getPieces().size()!=2)throw new IllegalStateException("missing complete native root/access");
  var city=(ChudPiece)start.getPieces().getFirst();var targets=new LinkedHashMap<String,BlockPos>();city.plan.points.forEach((role,p)->targets.put(role.toString(),city.plan.world(p)));var nav=ChudNavigation.inspect(world,result.bounds(),result.entrance(),targets);out.put("surface_to_city",nav);if(!nav.status().equals("PASS"))throw new IllegalStateException("story real block path: "+nav);
  out.put("origin",ORIGIN.toShortString());out.put("mouth",result.entrance().toShortString());out.put("owner",result.owner().toString());out.put("xz_distance",distance);out.put("records_added",org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().size()-initial);finish(null);
 }catch(Throwable x){finish(x);}}
 static void finish(Throwable error){done=true;int remaining=TicketEvidence300.siteBuild(world);out.put("actual_site_tickets_after_completion",remaining);if(remaining!=0&&error==null)error=new IllegalStateException("completion leaked actual region tickets");out.put("status",error==null?"PASS_STORY_SERVICE_ONLY":"FAIL");out.put("scope","Actual bounded story queue with permission0 source; NPC dialogue, player progression, reward and book NOT_RUN here");out.put("error",error==null?null:error.toString());out.put("elapsed_s",(System.nanoTime()-began)/1e9);try{Files.writeString(Path.of("story300.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(out));}catch(Exception x){x.printStackTrace();}if(error!=null)error.printStackTrace();world.getServer().saveEverything(false,true,true);world.getServer().halt(false);}
}
