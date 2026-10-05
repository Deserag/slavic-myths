package org.slavicmyths.kurgan;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.nbt.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
/** Event-discovered smoke jobs. Never scans the world or all loaded blocks. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class SmokeAging extends SavedData {
 private static final int LIMIT=512;
 private static final Map<ServerLevel,Set<BlockPos>> pending=new WeakHashMap<>();
 private static final class Job {BlockPos fire,target;String input;int progress,required;Job(BlockPos f,BlockPos t,String i,int p,int r){fire=f;target=t;input=i;progress=p;required=r;}}
 private final Map<BlockPos,Job> jobs=new LinkedHashMap<>();
 public SmokeAging(){super();}
 private static SmokeAging get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(SmokeAging::new,(tag,registries)->{SmokeAging data=new SmokeAging();data.load(tag);return data;}),"slavicmyths_smoke_aging");}
 private static String kind(BlockState s){ResourceLocation id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock());if(id==null||id.getPath().startsWith("darkened_"))return null;if(s.is(BlockTags.PLANKS))return "planks";if(s.is(BlockTags.LOGS))return id.getPath().contains("stripped")?"stripped_log":"log";return null;}
 private void discover(ServerLevel w,BlockPos fire){if(!w.hasChunkAt(fire)||!(w.getBlockState(fire).getBlock()instanceof CampfireBlock))return;
  for(int dy=1;dy<=5;dy++){BlockPos p=fire.above(dy);if(!w.hasChunkAt(p))return;BlockState s=w.getBlockState(p);String k=kind(s);if(k!=null){if(jobs.size()<LIMIT&&!jobs.containsKey(p)){jobs.put(p,new Job(fire.immutable(),p,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString(),0,1200+Math.floorMod(p.hashCode(),3601)));setDirty();}return;}if(!s.getCollisionShape(w,p).isEmpty())return;}
 }
 private static void near(ServerLevel w,BlockPos p){SmokeAging data=get(w);for(int i=0;i<=5;i++)data.discover(w,p.below(i));}
 @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent e){if(e.getLevel()instanceof ServerLevel)near((ServerLevel)e.getLevel(),e.getPos());}
 @SubscribeEvent public static void broken(BlockEvent.BreakEvent e){if(e.getLevel()instanceof ServerLevel){SmokeAging data=get((ServerLevel)e.getLevel());if(data.jobs.values().removeIf(j->j.target.equals(e.getPos())||j.fire.equals(e.getPos())))data.setDirty();}}
 @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock e){if(e.getLevel()instanceof ServerLevel)near((ServerLevel)e.getLevel(),e.getPos());}
 @SubscribeEvent public static void load(ChunkEvent.Load e){if(e.getLevel()instanceof ServerLevel&&e.getChunk()instanceof net.minecraft.world.level.chunk.LevelChunk){ServerLevel w=(ServerLevel)e.getLevel();synchronized(pending){Set<BlockPos> positions=pending.computeIfAbsent(w,k->new HashSet<>());for(Map.Entry<BlockPos,net.minecraft.world.level.block.entity.BlockEntity> entry:((net.minecraft.world.level.chunk.LevelChunk)e.getChunk()).getBlockEntities().entrySet())if(entry.getValue()instanceof net.minecraft.world.level.block.entity.CampfireBlockEntity&&positions.size()<LIMIT)positions.add(entry.getKey().immutable());}}}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post e){if(!(e.getLevel() instanceof ServerLevel)||e.getLevel().getGameTime()%20!=0)return;ServerLevel w=(ServerLevel)e.getLevel();SmokeAging data=get(w);Set<BlockPos> discover;synchronized(pending){discover=pending.remove(w);}if(discover!=null)for(BlockPos p:discover)data.discover(w,p);boolean changed=false;
  for(Iterator<Job> it=data.jobs.values().iterator();it.hasNext();){Job j=it.next();if(!w.hasChunkAt(j.fire)||!w.hasChunkAt(j.target))continue;BlockState s=w.getBlockState(j.target),fire=w.getBlockState(j.fire);String k=kind(s);
   if(k==null||!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString().equals(j.input)||!(fire.getBlock()instanceof CampfireBlock)){it.remove();changed=true;continue;}
   if(!fire.getValue(CampfireBlock.LIT))continue;boolean clear=true;for(int y=j.fire.getY()+1;y<j.target.getY();y++){BlockPos p=new BlockPos(j.fire.getX(),y,j.fire.getZ());if(!w.getBlockState(p).getCollisionShape(w,p).isEmpty()){clear=false;break;}}if(!clear)continue;
   j.progress+=20;changed=true;if(j.progress%200==0)w.sendParticles(ParticleTypes.SMOKE,j.target.getX()+.5,j.target.getY()-.1,j.target.getZ()+.5,2,.15,.05,.15,.015);
   if(j.progress>=j.required){BlockState aged=DarkenedWood.get(k).defaultBlockState();if(aged.hasProperty(RotatedPillarBlock.AXIS)&&s.hasProperty(RotatedPillarBlock.AXIS))aged=aged.setValue(RotatedPillarBlock.AXIS,s.getValue(RotatedPillarBlock.AXIS));w.setBlock(j.target,aged,3);it.remove();}
  }if(changed)data.setDirty();
 }
 public void load(CompoundTag n){jobs.clear();for(Tag raw:n.getList("Jobs",10)){if(jobs.size()>=LIMIT)break;CompoundTag t=(CompoundTag)raw;BlockPos fire=BlockPos.of(t.getLong("Fire")),target=BlockPos.of(t.getLong("Target"));if(fire.getX()!=target.getX()||fire.getZ()!=target.getZ()||target.getY()-fire.getY()<1||target.getY()-fire.getY()>5)continue;jobs.put(target,new Job(fire,target,t.getString("Input"),Math.max(0,t.getInt("Progress")),Math.max(1200,Math.min(4800,t.getInt("Required")))));}}
 @Override public CompoundTag save(CompoundTag n,net.minecraft.core.HolderLookup.Provider registries){ListTag list=new ListTag();for(Job j:jobs.values()){CompoundTag t=new CompoundTag();t.putLong("Fire",j.fire.asLong());t.putLong("Target",j.target.asLong());t.putString("Input",j.input);t.putInt("Progress",j.progress);t.putInt("Required",j.required);list.add(t);}n.put("Jobs",list);return n;}
}
