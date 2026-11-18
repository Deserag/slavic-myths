package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.worldgen.*;
@EventBusSubscriber(modid="slavicmyths")
public final class Generation295Probe {
 static int phase,ticks;static int chosenX=Integer.getInteger("slavicmyths.qaX",0);static long started;static boolean completed;static ServerLevel w;
 static final Map<String,Object> out=new LinkedHashMap<>();
 static BlockPos protectedPos;static boolean refused;
 static SiteChunks terrainChunks;static int terrainX=Integer.MIN_VALUE,terrainIndex;static java.util.List<ChunkPos> terrainFootprint;
 static boolean readyArea(int x,int radius,int zRadius){
  if(terrainX!=x){if(terrainChunks!=null)terrainChunks.close();terrainChunks=new SiteChunks(w);terrainX=x;terrainIndex=0;terrainFootprint=new ArrayList<>();for(int cx=(x-radius)>>4;cx<=(x+radius)>>4;cx++)for(int cz=-zRadius;cz<zRadius;cz++)terrainFootprint.add(new ChunkPos(cx,cz));}
  terrainChunks.prefetch(terrainFootprint,terrainIndex);if(terrainIndex<terrainFootprint.size()){if(terrainChunks.ready(terrainFootprint.get(terrainIndex)))terrainIndex++;return false;}return true;
 }
 static String scenario(){return System.getProperty("slavicmyths.qa295","city");}
 static net.minecraft.commands.CommandSourceStack source(){return w.getServer().createCommandSourceStack().withLevel(w).withPosition(new Vec3(chosenX,80,0)).withPermission(4);}
 static int command(String text)throws Exception{return w.getServer().getCommands().getDispatcher().execute(text,source());}
 static int held()throws Exception{var f=SiteChunks.class.getDeclaredField("ACTIVE");f.setAccessible(true);return ((Set<?>)f.get(null)).size();}
 static int nativeSiteTickets()throws Exception{
  Object manager=null;for(var f:w.getChunkSource().getClass().getDeclaredFields())if(f.getType().getSimpleName().contains("DistanceManager")){f.setAccessible(true);manager=f.get(w.getChunkSource());break;}
  if(manager==null)throw new IllegalStateException("native DistanceManager unavailable");
  java.lang.reflect.Field tickets=null;for(Class<?> c=manager.getClass();c!=null&&tickets==null;c=c.getSuperclass())for(var f:c.getDeclaredFields())if(f.getName().equals("tickets")){tickets=f;break;}
  if(tickets==null)throw new IllegalStateException("native tickets field unavailable");tickets.setAccessible(true);int count=0;for(var set:((Map<?,?>)tickets.get(manager)).values())for(var ticket:(Iterable<?>)set)if(ticket.toString().contains("slavicmyths_site_build"))count++;return count;
 }
 static void check(boolean ok,String reason){if(!ok)throw new IllegalStateException(reason);}
 @SubscribeEvent public static void start(net.neoforged.neoforge.event.server.ServerStartedEvent e){w=e.getServer().overworld();started=System.nanoTime();out.put("seed",w.getSeed());out.put("scenario",scenario());out.put("generator",w.getChunkSource().getGenerator().getClass().getSimpleName());
  if(scenario().equals("hills")){int highest=Integer.MIN_VALUE;var heights=new LinkedHashMap<Integer,Integer>();for(int i=1;i<=24;i++){int x=i*2048;int y=w.getChunkSource().getGenerator().getBaseHeight(x+8,-248,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,w,w.getChunkSource().randomState());heights.put(x,y);if(y>highest){highest=y;chosenX=x;}}out.put("candidate_height_samples",heights);out.put("chosenX",chosenX);out.put("highest_surface",highest);}
}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){if(w==null||completed)return;ticks++;try{
  if(System.nanoTime()-started>240_000_000_000L)throw new IllegalStateException("timeout phase "+phase);
  if(phase==0){
   int count=ManualStructureRecords.get(w).entries().size();int tickets=held();
   command("locate structure slavicmyths:chud_city");command("slavicmyths city search");command("slavicmyths abandoned search");command("locate structure slavicmyths:gorodishche");command("locate structure slavicmyths:serpent_hill");
   check(held()==tickets,"locate acquired tickets");check(ManualStructureRecords.get(w).entries().size()==count,"locate changed records");out.put("empty_locate_no_tickets_or_records",true);
   if(scenario().equals("reload")){
    var saved=com.google.gson.JsonParser.parseString(Files.readString(Path.of("generation295-created.json"))).getAsJsonObject();
    var site=ManualStructureRecords.get(w).entries().stream().filter(v->!v.family().equals("construction")).findFirst().orElseThrow();
    int ownerX=saved.get("ownerX").getAsInt(),ownerZ=saved.get("ownerZ").getAsInt();w.getChunk(ownerX,ownerZ);
    check(ExistingSiteSearch.locate(source().withPosition(Vec3.atCenterOf(site.arrival())),site.family())==1,"saved actual start not found");
    int checked=0;for(var raw:saved.getAsJsonArray("block_witnesses")){var item=raw.getAsJsonObject();var pos=BlockPos.of(item.get("position").getAsLong());w.getChunk(pos.getX()>>4,pos.getZ()>>4);check(net.minecraft.world.level.block.Block.getId(w.getBlockState(pos))==item.get("state").getAsInt(),"saved witness mismatch at "+pos);checked++;}
    check(checked>=16,"insufficient before-save witnesses");out.put("exact_block_witnesses_after_restart",checked);out.put("reloaded_record",site.family()+" "+site.arrival().toShortString());done("PASS");return;
   }
   if(scenario().equals("partialreload")){check(ManualStructureRecords.get(w).entries().stream().anyMatch(v->v.family().equals("construction")),"partial reservation lost after restart");check(ExistingSiteSearch.locate(source(),"chud_city")==0,"partial city reported completed");out.put("partial_reservation_restored_not_locatable",true);done("PASS");return;}
   if(scenario().equals("bayunreload")){phase=44;return;}
   if((scenario().equals("protection")||scenario().equals("waterlogged"))){phase=40;return;}
   if(scenario().equals("snapshot")){out.put("ownerX",0);out.put("ownerZ",-16);terrainChunks=new SiteChunks(w);terrainFootprint=new ArrayList<>();for(int cx=-2;cx<=2;cx++)for(int cz=-18;cz<=-14;cz++)terrainFootprint.add(new ChunkPos(cx,cz));phase=30;return;}
   if(scenario().equals("terrain")){phase=20;return;}
   if(scenario().equals("flat")){check(command("slavicmyths dev create gorodishche")!=0,"create rejected");phase=2;}
   else if((scenario().equals("cancel")||scenario().equals("cancelpartial"))){org.slavicmyths.chud.ChudCreationJobs.create(source(),"city",r->{});phase=3;}
   else {phase=1;String kind=scenario().equals("medium")?"medium":"city";check(org.slavicmyths.chud.ChudCreationJobs.create(source(),kind,r->{if(r==null){out.put("create_failure",true);phase=90;}else{out.put("entrance",r.entrance().toShortString());out.put("owner",r.owner().toString());out.put("ownerX",r.owner().x);out.put("ownerZ",r.owner().z);out.put("bounds",r.bounds().toString());phase=2;}})!=0,"create rejected");}
  }else if(phase==44||phase==45){int x=phase==44?5000:6024;if(!readyArea(x,8,1))return;var home=org.slavicmyths.bayun.BayunHomes.get(w).nearest(new BlockPos(x,201,0));check(home!=null&&home.position().getX()==x,"home not restored");if(w.getEntity(home.entity())==null)return;check(w.getEntity(home.entity()) instanceof org.slavicmyths.bayun.BayunEntity,"restored UUID is not Bayun");out.put("restored_uuid_at_"+x,home.entity().toString());check(!org.slavicmyths.bayun.BayunHomes.get(w).reserveNew(new BlockPos(5512,201,0),UUID.randomUUID()),"spacing lost after restart");if(phase==45)done("PASS");else phase++;
  }else if(phase==40){
   if(terrainChunks==null)terrainChunks=new SiteChunks(w);if(!terrainChunks.ready(new ChunkPos(0,-16)))return;
   int ground=w.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,8,-248);int y=Math.max(w.getMinBuildHeight()+60,ground-90);protectedPos=new BlockPos(8,y+1,-248);w.setBlock(protectedPos,scenario().equals("waterlogged")?Blocks.OAK_FENCE.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED,true):Blocks.CHEST.defaultBlockState(),2|16);
   phase=42;check(org.slavicmyths.chud.ChudCreationJobs.create(source(),"city",r->{refused=r==null;phase=41;})==1,"protected request rejected before preflight");
  }else if(phase==41){
   check(refused,"container was overwritten");check(scenario().equals("waterlogged")?w.getBlockState(protectedPos).is(Blocks.OAK_FENCE):w.getBlockState(protectedPos).is(Blocks.CHEST)&&w.getBlockEntity(protectedPos)!=null,"protected block lost");check(ManualStructureRecords.get(w).entries().isEmpty(),"failed preflight left a record");out.put(scenario().equals("waterlogged")?"waterlogged_fence_refused_and_preserved":"container_refused_and_preserved",true);
   w.setBlock(protectedPos,Blocks.RAW_COPPER_BLOCK.defaultBlockState(),2|16);w.setBlock(protectedPos.east(),Blocks.RAW_IRON_BLOCK.defaultBlockState(),2|16);
   phase=42;check(org.slavicmyths.chud.ChudCreationJobs.create(source(),"city",r->{refused=r==null;phase=43;})==1,"second explicit request rejected");
  }else if(phase==43){
   check(!refused,"natural ore replacement refused");check(!w.getBlockState(protectedPos).is(Blocks.RAW_COPPER_BLOCK)&&!w.getBlockState(protectedPos.east()).is(Blocks.RAW_IRON_BLOCK),"ore witnesses not physically replaced");terrainChunks.close();check(held()==0&&nativeSiteTickets()==0,"protection/veins tickets leaked");out.put("actual_raw_copper_and_iron_replaced",true);out.put("native_tickets_after",nativeSiteTickets());done("PASS");
  }else if(phase==30){terrainChunks.prefetch(terrainFootprint,terrainIndex);if(terrainIndex<terrainFootprint.size()){if(terrainChunks.ready(terrainFootprint.get(terrainIndex)))terrainIndex++;return;}phase=2;
  }else if(phase>=20&&phase<=22){
   int variant=phase-20,x=3000+variant*80;var at=new BlockPos(x,200,0);
   if(!readyArea(x,12,2))return;
   for(int dx=-11;dx<=11;dx++)for(int dz=-18;dz<=11;dz++){int h=variant==0?0:variant==1?Math.floorDiv(dx+11,8):Math.floorDiv(dx+11,2);for(int y=198;y<=214;y++)w.setBlock(new BlockPos(x+dx,y,dz),y<=200+h?Blocks.DIRT.defaultBlockState():Blocks.AIR.defaultBlockState(),2|16);}
   var plan=new org.slavicmyths.yaga.YagaHutPlan();
   var site=org.slavicmyths.yaga.YagaPlacement.prepareForCreation(w,at,plan,id->{try{return net.minecraft.commands.arguments.blocks.BlockStateParser.parseForBlock(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),id,false).blockState();}catch(Exception ex){throw new RuntimeException(ex);}});
   if(variant==2){check(site.failure!=null&&site.failure.reason.equals("slope"),"steep cliff accepted");out.put("yaga_steep_refused_before_write",true);phase=23;}
   else{check(site.failure==null,"hut plan refused "+(site.failure==null?"":site.failure.reason));var result=org.slavicmyths.yaga.YagaPlacement.commit(w,site,()->true,()->{});check(result.ok,"hut commit "+result.reason);int changed=0;for(var entry:site.desired.entrySet())if(!w.getBlockState(entry.getKey()).isAir())changed++;check(changed>100,"hut blocks absent");out.put("yaga_"+(variant==0?"flat":"gentle")+"_physical_blocks",changed);var dump=new ArrayList<Map<String,Object>>();for(var pos:site.desired.keySet()){var st=w.getBlockState(pos);if(!st.isAir())dump.add(Map.of("x",pos.getX(),"y",pos.getY(),"z",pos.getZ(),"block",net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString()));}Files.writeString(Path.of("yaga-"+(variant==0?"flat":"gentle")+"-actual-blocks.json"),new com.google.gson.Gson().toJson(dump));phase++;}
  }else if(phase>=23&&phase<=25){int n=phase-23;int x=5000+n*512;if(!readyArea(x,8,1))return;for(int dx=-7;dx<=7;dx++)for(int dz=-7;dz<=7;dz++)for(int y=198;y<=206;y++)w.setBlock(new BlockPos(x+dx,y,dz),y<=200?Blocks.DIRT.defaultBlockState():Blocks.AIR.defaultBlockState(),2|16);
   boolean placed=org.slavicmyths.bayun.BayunFeature.place(w,new BlockPos(x,201,0));check(placed==(n!=1),"Bayun spacing placement at "+x+"="+placed);out.put("bayun_at_"+x,placed);if(placed){var home=org.slavicmyths.bayun.BayunHomes.get(w).nearest(new BlockPos(x,201,0));check(home!=null&&home.position().getX()==x,"wrong recorded home");check(w.getEntity(home.entity()) instanceof org.slavicmyths.bayun.BayunEntity,"no real Bayun entity");out.put("bayun_uuid_at_"+x,home.entity().toString());}if(n==2){check(org.slavicmyths.bayun.BayunHomes.get(w).nearest(new BlockPos(x,201,0))!=null,"missing actual home");done("PASS");}else phase++;
  }else if(phase==2){var entries=ManualStructureRecords.get(w).entries().stream().filter(v->!v.family().equals("construction")).toList();if(entries.isEmpty())return;
   var site=entries.getFirst();var b=site.bounds();int solids=0,air=0;long hash=0;var witnesses=new ArrayList<Map<String,Long>>();for(int i=0;i<4096;i++){int x=b.minX()+Math.floorMod(i*73,b.getXSpan()),y=b.minY()+Math.floorMod(i*31,b.getYSpan()),z=b.minZ()+Math.floorMod(i*97,b.getZSpan());var p=new BlockPos(x,y,z);if(!w.hasChunkAt(p))continue;var state=w.getBlockState(p);if(state.isAir())air++;else solids++;hash=hash*31+net.minecraft.world.level.block.Block.getId(state);if(witnesses.size()<32&&!state.isAir()&&!ManualStructureJobs.natural(state))witnesses.add(Map.of("position",p.asLong(),"state",(long)net.minecraft.world.level.block.Block.getId(state)));}
   if(out.containsKey("ownerX")){int ox=((Number)out.get("ownerX")).intValue()*16,oz=((Number)out.get("ownerZ")).intValue()*16;for(int y=b.minY();y<=b.maxY()&&witnesses.size()<32;y++)for(int x=ox;x<ox+16&&witnesses.size()<32;x++)for(int z=oz;z<oz+16&&witnesses.size()<32;z++){var pos=new BlockPos(x,y,z);var st=w.getBlockState(pos);if(!st.isAir()&&!ManualStructureJobs.natural(st))witnesses.add(Map.of("position",pos.asLong(),"state",(long)net.minecraft.world.level.block.Block.getId(st)));}}
   check(solids>0,"no physical blocks");out.put("physical_solid_samples",solids);out.put("physical_air_samples",air);out.put("sample_hash",hash);out.put("block_witnesses",witnesses);out.put("record",site.family()+" "+site.arrival().toShortString());int before=entries.size();int tickets=held();
   var near=source().withPosition(Vec3.atCenterOf(site.arrival()));int found=ExistingSiteSearch.locate(near,site.family());check(found==1,"existing start not found");check(held()==tickets,"repeat locate tickets");check(ManualStructureRecords.get(w).entries().size()==before,"duplicate locate record");out.put("existing_locate_no_create",true);if(terrainChunks!=null)terrainChunks.close();check(held()==0,"tickets leaked");out.put("tickets_after",held());check(nativeSiteTickets()==0,"native ticket map leak");out.put("native_tickets_after",nativeSiteTickets());done("PASS");
  }else if(phase==3&&scenario().equals("cancelpartial")){
   var jf=org.slavicmyths.chud.ChudCreationJobs.class.getDeclaredField("JOBS");jf.setAccessible(true);var job=((Map<?,?>)jf.get(null)).get(w);check(job!=null,"job ended before partial cancel");var pf=job.getClass().getDeclaredField("phase");pf.setAccessible(true);var sf=job.getClass().getDeclaredField("section");sf.setAccessible(true);var vf=job.getClass().getDeclaredField("voxel");vf.setAccessible(true);
   if(pf.getInt(job)!=3||(sf.getInt(job)==0&&vf.getInt(job)==0))return;org.slavicmyths.chud.ChudCreationJobs.control(source(),true);check(ManualStructureRecords.get(w).entries().stream().anyMatch(v->v.family().equals("construction")),"partial reservation missing");check(ExistingSiteSearch.locate(source(),"chud_city")==0,"partial city falsely locatable");check(held()==0&&nativeSiteTickets()==0,"partial cancellation tickets leaked");out.put("partial_cancel_retains_reservation",true);out.put("native_tickets_after",nativeSiteTickets());done("PASS");
  }else if(phase==3&&ticks>30){org.slavicmyths.chud.ChudCreationJobs.control(source(),true);check(held()==0,"cancel tickets leaked");out.put("cancel_tickets_after",held());check(org.slavicmyths.chud.ChudCreationJobs.create(source(),"medium",r->{})==1,"restart rejected after cancel");org.slavicmyths.chud.ChudCreationJobs.control(source(),true);check(held()==0,"restart/cancel leaked");out.put("restart_after_cancel",true);check(nativeSiteTickets()==0,"cancel native tickets leaked");out.put("native_tickets_after",nativeSiteTickets());done("PASS");}
  else if(phase==90)throw new IllegalStateException("create failed: inspect server log");
 }catch(Exception ex){out.put("error",ex.toString());ex.printStackTrace();done("FAIL");}}
 static void done(String result){if(terrainChunks!=null)terrainChunks.close();completed=true;out.put("result",result);out.put("ticks",ticks);out.put("elapsed_ms",(System.nanoTime()-started)/1_000_000);System.out.println("GENERATION295 "+out);try{if(result.equals("PASS")&&out.containsKey("ownerX"))Files.writeString(Path.of("generation295-created.json"),new com.google.gson.Gson().toJson(out));Files.writeString(Path.of("generation295.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(out));}catch(Exception e){e.printStackTrace();}w.getServer().halt(false);}
}
