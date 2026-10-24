package org.slavicmyths.underground;
import java.util.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slavicmyths.chud.*;
/** A persisted bounded queue of known encounter slots. Chunk events never request chunks or scan the world. */
@EventBusSubscriber(modid="slavicmyths")
public final class EncounterPopulation extends SavedData {
 record Slot(String key,String kind,int variant,BlockPos at,BoundingBox room,long seed){}
 private final Map<String,Slot> pending=new LinkedHashMap<>();private final Map<String,Integer> cursors=new HashMap<>();private final Map<String,String> consumed=new LinkedHashMap<>();private final Set<String> planned=new HashSet<>();
 public static EncounterPopulation data(ServerLevel w){return w.getDataStorage().computeIfAbsent(new Factory<>(EncounterPopulation::new,EncounterPopulation::load,null),"slavicmyths_underground_encounters");}
 /** Read-only proof that this is the deterministic native population slot, not a debug binding. */
 public boolean ownsSpawn(String key,UUID id){return "SPAWNED".equals(consumed.get(key))&&UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).equals(id);}
 public void defeat(String key){consumed.put(key,"DEFEATED");setDirty();}
 private static EncounterPopulation load(CompoundTag n,HolderLookup.Provider lookup){var d=new EncounterPopulation();var c=n.getCompound("Consumed");for(String key:c.getAllKeys())d.consumed.put(key,c.getString(key));for(Tag t:n.getList("Planned",8))d.planned.add(t.getAsString());for(Tag tag:n.getList("Pending",10)){var s=(CompoundTag)tag;int[] box=s.getIntArray("Room");if(box.length==6){var slot=new Slot(s.getString("Key"),s.getString("Kind"),s.getInt("Variant"),BlockPos.of(s.getLong("At")),new BoundingBox(box[0],box[1],box[2],box[3],box[4],box[5]),s.getLong("Seed"));if(!d.consumed.containsKey(slot.key)){d.pending.put(slot.key,slot);d.cursors.put(slot.key,Math.max(0,s.getInt("Cursor")));}}}return d;}
 public CompoundTag save(CompoundTag n,HolderLookup.Provider lookup){var c=new CompoundTag();consumed.forEach(c::putString);n.put("Consumed",c);var p=new ListTag();planned.stream().sorted().forEach(x->p.add(StringTag.valueOf(x)));n.put("Planned",p);var queue=new ListTag();for(var slot:pending.values()){var s=new CompoundTag();s.putString("Key",slot.key);s.putString("Kind",slot.kind);s.putInt("Variant",slot.variant);s.putLong("At",slot.at.asLong());s.putLong("Seed",slot.seed);s.putInt("Cursor",cursors.getOrDefault(slot.key,0));var b=slot.room;s.putIntArray("Room",new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()});queue.add(s);}n.put("Pending",queue);return n;}
 private void queue(AbandonedPlan p,AbandonedPlan.Chamber room,String key,String kind,int variant,int dx,int dz){BlockPos at=p.world(room.floor().offset(dx,1,dz));var a=p.world(new BlockPos(room.x()-room.width()/2+1,room.y()+1,room.z()-room.depth()/2+1));var b=p.world(new BlockPos(room.x()+room.width()/2-1,room.y()+room.height()-1,room.z()+room.depth()/2-1));var box=BoundingBox.fromCorners(a,b);if(!consumed.containsKey(key))pending.putIfAbsent(key,new Slot(key,kind,variant,at,box,p.seed));}
 public void plan(ServerLevel w,AbandonedPlan p){String root=w.dimension().location()+":"+p.rootId+":"+p.kind;if(!planned.add(root))return;var random=new Random(p.seed^0x4d4f42535f313535L);boolean small=p.kind==AbandonedPlan.Kind.ATTACHED_SMALL;var rooms=p.chambers.stream().filter(c->c.role()!=AbandonedPlan.Role.SEALED_GATE&&c.width()>=9&&c.depth()>=9).toList();int count=Math.min(small?4:12,rooms.size()*2);
  for(int i=0;i<count;i++){var r=rooms.get(i/2);double chance=r.module()==AbandonedPlan.Module.TOOL_ROOM||r.module()==AbandonedPlan.Module.ORE_SORTING?.6:r.role()==AbandonedPlan.Role.GALLERY?.4:.25;int variant=random.nextDouble()<chance?1:0;if(small&&i>=2)variant=0;queue(p,r,root+":ordinary:"+i,"podzemnik",variant,i%2==0?-3:3,3);}
  if(!small){var storage=p.chambers.stream().filter(c->c.module()==AbandonedPlan.Module.ABANDONED_STORAGE&&c.width()>=13&&c.depth()>=13&&c.height()>=4).min(Comparator.comparingInt(AbandonedPlan.Chamber::y)).orElse(null);if(storage!=null)queue(p,storage,root+":kladovik","kladovik",0,2,2);
   var hall=p.chambers.stream().filter(c->c.width()>=19&&c.depth()>=19&&c.height()>=5&&c.role()==AbandonedPlan.Role.GALLERY&&c!=storage).min(Comparator.comparingInt(AbandonedPlan.Chamber::y)).orElse(null);if(hall!=null)queue(p,hall,root+":large","large_podzemnik",0,0,3);}
  setDirty();
 }
 @SubscribeEvent public static void chunk(ChunkEvent.Load e){if(e.getLevel() instanceof ServerLevel w){var starts=List.copyOf(e.getChunk().getAllStarts().values());w.getServer().execute(()->{for(var start:starts)for(var piece:start.getPieces())if(piece instanceof AbandonedPiece a&&a.encounterMobsEnabled())data(w).plan(w,a.plan);else if(piece instanceof ChudPiece c&&c.encounterMobsEnabled()&&c.abandoned!=null)data(w).plan(w,c.abandoned);});}}
 @SubscribeEvent public static void tick(LevelTickEvent.Post e){if(e.getLevel() instanceof ServerLevel w&&w.getGameTime()%40==0)data(w).process(w);}
 private void process(ServerLevel w){int tested=0;for(var s:pending.values().stream().limit(4).toList()){if(tested++>=4)break;boolean loaded=true;for(int x=s.room.minX()>>4;x<=s.room.maxX()>>4;x++)for(int z=s.room.minZ()>>4;z<=s.room.maxZ()>>4;z++)if(w.getChunkSource().getChunkNow(x,z)==null)loaded=false;if(!loaded){pending.remove(s.key);pending.put(s.key,s);continue;}if(w.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)continue;
   EntityType<? extends EncounterMob> type=s.kind.equals("kladovik")?UndergroundMobs.KLADOVIK.get():s.kind.equals("large_podzemnik")?UndergroundMobs.LARGE.get():UndergroundMobs.PODZEMNIK.get();var mob=type.create(w);if(mob==null)continue;UUID id=UUID.nameUUIDFromBytes(s.key.getBytes(StandardCharsets.UTF_8));if(w.getEntity(id)!=null){consumed.put(s.key,"SPAWNED");pending.remove(s.key);setDirty();continue;}
   boolean placed=false;int clear=s.kind.equals("kladovik")?5:s.kind.equals("large_podzemnik")?7:1;
   int first=cursors.getOrDefault(s.key,0),candidate=0,testedCandidates=0;boolean exhausted=true;
   candidates:for(int radius=0;radius<=4&&!placed;radius++)for(int dx=-radius;dx<=radius&&!placed;dx++)for(int dz=-radius;dz<=radius&&!placed;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;if(candidate++<first)continue;if(testedCandidates++>=4){exhausted=false;break candidates;}cursors.put(s.key,candidate);BlockPos at=s.at.offset(dx,0,dz);boolean safe=true;for(int a=-clear;a<=clear&&safe;a++)for(int b=-clear;b<=clear&&safe;b++){BlockPos p=at.offset(a,0,b);if(!s.room.isInside(p)||!w.getBlockState(p.below()).isCollisionShapeFullBlock(w,p.below())){safe=false;break;}for(int y=0;y<(s.kind.equals("large_podzemnik")?4:2);y++)if((s.kind.equals("kladovik")?Math.abs(a)<=1&&Math.abs(b)<=1:true)&&(!w.getBlockState(p.above(y)).getCollisionShape(w,p.above(y)).isEmpty()||!w.getFluidState(p.above(y)).isEmpty())){safe=false;break;}}if(!safe)continue;mob.setUUID(id);mob.bind(at,s.key);if(mob instanceof Podzemnik p)p.variant(s.variant);mob.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);if(!w.noCollision(mob)||!w.getEntitiesOfClass(LivingEntity.class,mob.getBoundingBox().inflate(1.5)).isEmpty())continue;placed=w.addFreshEntity(mob);}
   if(!placed&&!exhausted){pending.remove(s.key);pending.put(s.key,s);setDirty();continue;}cursors.remove(s.key);if(mob instanceof RoomBoss&&!placed)com.mojang.logging.LogUtils.getLogger().info("Underground boss skipped: {} no safe arena in {}",s.key,s.room);consumed.put(s.key,placed?"SPAWNED":"SKIPPED_NO_SAFE_FLOOR");pending.remove(s.key);setDirty();
  }}
 public String status(){return "planned="+planned.size()+"; pending="+pending.size()+"; consumed="+consumed.size()+"; defeated="+consumed.values().stream().filter("DEFEATED"::equals).count();}
}
