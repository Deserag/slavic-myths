package org.slavicmyths.yaga;
import java.util.*;
import com.mojang.brigadier.StringReader;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.block.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
/** No forced chunks, no global world tick traversal, one durable world home. */
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class YagaHut {
 private static final YagaHutPlan PLAN=new YagaHutPlan();private static final Map<String,BlockState> STATES=new HashMap<>();
 public static void prepare(ServerWorld w){YagaData d=YagaData.get(w);if(d.placed||d.anchor!=null||d.examined.size()>=64)return;for(BlockPos p:YagaHutPlan.candidates(w.getSeed(),w.getSharedSpawnPos())){if(d.examined.contains(p.asLong()))continue;Biome b=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(p.getX()>>2,16,p.getZ()>>2);if(b.getBiomeCategory()==Biome.Category.FOREST){d.anchor=p;d.setDirty();return;}d.examined.add(p.asLong());}d.setDirty();}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayerEntity)||e.player.tickCount%80!=0||e.player.level.dimension()!=World.OVERWORLD)return;ServerWorld w=(ServerWorld)e.player.level;YagaData d=YagaData.get(w);if(d.placed)return;prepare(w);if(d.anchor==null||e.player.distanceToSqr(d.anchor.getX(),e.player.getY(),d.anchor.getZ())>128*128)return;BlockPos candidate=d.anchor;if(!loaded(w,candidate))return;if(!place(w,candidate)){d.examined.add(candidate.asLong());d.anchor=null;d.setDirty();prepare(w);}}
 private static boolean loaded(ServerWorld w,BlockPos p){return w.hasChunksAt(p.offset(-10,0,-17),p.offset(10,0,10));}
 private static BlockState state(String id){return STATES.computeIfAbsent(id,s->{try{return new BlockStateParser(new StringReader(s),false).parse(false).getState();}catch(com.mojang.brigadier.exceptions.CommandSyntaxException ex){throw new IllegalStateException(s,ex);}});}
 private static int ground(ServerWorld w,int x,int z){int y=w.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,x,z)-1;for(int i=0;i<48&&y>4;i++,y--){BlockState s=w.getBlockState(new BlockPos(x,y,z));if(s.getMaterial().isLiquid())return -1;if(s.isSolidRender(w,new BlockPos(x,y,z))&&!(s.is(net.minecraft.tags.BlockTags.LOGS))&&!(s.getBlock() instanceof LeavesBlock))return y;}return -1;}
 public static boolean place(ServerWorld w,BlockPos candidate){YagaData d=YagaData.get(w);if(d.placed||!loaded(w,candidate))return false;int min=255,max=0;for(int x=-9;x<=9;x+=3)for(int z=-15;z<=9;z+=3){int y=ground(w,candidate.getX()+x,candidate.getZ()+z);if(y<0)return false;min=Math.min(min,y);max=Math.max(max,y);}if(max-min>3||max+15>=w.getMaxBuildHeight())return false;BlockPos anchor=new BlockPos(candidate.getX(),max+1,candidate.getZ());LinkedHashMap<BlockPos,BlockState> desired=new LinkedHashMap<>(),old=new LinkedHashMap<>();
  // Refuse inventory blocks, protected masonry and non-natural building materials.
  for(Map.Entry<BlockPos,String> e:PLAN.blocks.entrySet()){BlockPos p=anchor.offset(e.getKey());BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null||before.getMaterial().isLiquid())return false;net.minecraft.util.ResourceLocation id=before.getBlock().getRegistryName();if(id!=null&&id.getNamespace().equals("slavicmyths")&&!before.getMaterial().isReplaceable())return false;if(!before.isAir(w,p)&&!before.getMaterial().isReplaceable()&&!(before.getBlock() instanceof LeavesBlock)&&!(before.is(net.minecraft.tags.BlockTags.LOGS))&&!(before.getBlock() instanceof GrassBlock)&&!(before.getBlock()==Blocks.DIRT || before.getBlock()==Blocks.COARSE_DIRT || before.getBlock()==Blocks.PODZOL))return false;desired.put(p,state(e.getValue()));old.put(p,before);}
  for(Map.Entry<BlockPos,String> e:PLAN.blocks.entrySet()){if(e.getKey().getY()!=0)continue;BlockPos base=anchor.offset(e.getKey()).below();int terrain=ground(w,base.getX(),base.getZ());if(terrain<0||anchor.getY()-1-terrain>4)return false;for(int y=terrain+1;y<anchor.getY();y++){BlockPos p=new BlockPos(base.getX(),y,base.getZ());BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null||before.getMaterial().isLiquid()||!(before.isAir(w,p)||before.getMaterial().isReplaceable()))return false;if(!old.containsKey(p))old.put(p,before);desired.put(p,Blocks.COARSE_DIRT.defaultBlockState());}}
  // Daylight mushrooms need podzol; this support is validated and rolled back with the house.
  for(Map.Entry<BlockPos,String> e:PLAN.blocks.entrySet())if(e.getValue().equals("minecraft:brown_mushroom")){BlockPos p=anchor.offset(e.getKey()).below();BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null||!(before.isAir(w,p)||before.getMaterial().isReplaceable()||before.getBlock()==Blocks.GRASS_BLOCK||before.getBlock()==Blocks.DIRT||before.getBlock()==Blocks.COARSE_DIRT||before.getBlock()==Blocks.PODZOL))return false;if(!old.containsKey(p))old.put(p,before);desired.put(p,Blocks.PODZOL.defaultBlockState());}
  BabaYaga npc=ModEntities.BABA_YAGA.get().create(w);if(npc==null)return false;npc.home=anchor.offset(YagaHutPlan.HOME);npc.moveTo(npc.home.getX()+.5,npc.home.getY(),npc.home.getZ()+.5,180,0);
  try{for(Map.Entry<BlockPos,BlockState> e:desired.entrySet())if(!w.setBlock(e.getKey(),e.getValue(),2)&&!w.getBlockState(e.getKey()).equals(e.getValue()))throw new IllegalStateException("Hut placement refused");for(Map.Entry<BlockPos,BlockState> e:desired.entrySet()){BlockState current=w.getBlockState(e.getKey()),connected=Block.updateFromNeighbourShapes(current,w,e.getKey());if(!connected.equals(current)){if(connected.isAir(w,e.getKey())&&!current.isAir(w,e.getKey()))throw new IllegalStateException("Unsupported hut decoration at "+e.getKey());w.setBlock(e.getKey(),connected,2);}}if(!w.addFreshEntity(npc))throw new IllegalStateException("Yaga spawn refused");d.anchor=anchor;d.npc=npc.getUUID();d.placed=true;d.setDirty();return true;}catch(RuntimeException failure){npc.remove();for(Map.Entry<BlockPos,BlockState> e:old.entrySet())w.setBlock(e.getKey(),e.getValue(),2);org.apache.logging.log4j.LogManager.getLogger().warn("Yaga hut rolled back",failure);return false;}
 }
 private YagaHut(){}
}
