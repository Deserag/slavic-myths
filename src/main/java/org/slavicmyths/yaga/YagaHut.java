package org.slavicmyths.yaga;
import java.util.*;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
/** No forced chunks, no global world tick traversal, one durable world home. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class YagaHut {
 private static final YagaHutPlan PLAN=new YagaHutPlan();private static final Map<String,BlockState> STATES=new HashMap<>();
 public static void prepare(ServerLevel w){YagaData d=YagaData.get(w);if(d.placed||d.anchor!=null||d.examined.size()>=64)return;for(BlockPos p:YagaHutPlan.candidates(w.getSeed(),w.getSharedSpawnPos())){if(d.examined.contains(p.asLong()))continue;net.minecraft.core.Holder<Biome> b=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(p.getX()>>2,16,p.getZ()>>2,w.getChunkSource().randomState().sampler());if(b.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST)){d.anchor=p;d.setDirty();return;}d.examined.add(p.asLong());}d.setDirty();}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer)||e.getEntity().tickCount%80!=0||e.getEntity().level().dimension()!=Level.OVERWORLD)return;ServerLevel w=(ServerLevel)e.getEntity().level();YagaData d=YagaData.get(w);if(d.placed)return;prepare(w);if(d.anchor==null||e.getEntity().distanceToSqr(d.anchor.getX(),e.getEntity().getY(),d.anchor.getZ())>128*128)return;BlockPos candidate=d.anchor;if(!loaded(w,candidate))return;if(!place(w,candidate)){d.examined.add(candidate.asLong());d.anchor=null;d.setDirty();prepare(w);}}
 private static boolean loaded(ServerLevel w,BlockPos p){return w.hasChunksAt(p.offset(-10,0,-17),p.offset(10,0,10));}
 private static BlockState state(String id){return STATES.computeIfAbsent(id,s->{try{return BlockStateParser.parseForBlock(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),s,false).blockState();}catch(com.mojang.brigadier.exceptions.CommandSyntaxException ex){throw new IllegalStateException(s,ex);}});}
 public static boolean place(ServerLevel w,BlockPos candidate){return tryPlace(w,candidate).ok;}
 public static YagaPlacement.Result tryPlace(ServerLevel w,BlockPos candidate){YagaData d=YagaData.get(w);if(d.placed)return YagaPlacement.Result.fail("exists",d.anchor);
  try{YagaPlacement.Site site=YagaPlacement.prepare(w,candidate,PLAN,YagaHut::state);if(site.failure!=null)return site.failure;
   BabaYaga npc=ModEntities.BABA_YAGA.get().create(w);if(npc==null)return YagaPlacement.Result.fail("npc",site.anchor);npc.home=site.anchor.offset(YagaHutPlan.HOME);npc.moveTo(npc.home.getX()+.5,npc.home.getY(),npc.home.getZ()+.5,180,0);
   YagaPlacement.Result result=YagaPlacement.commit(w,site,()->w.addFreshEntity(npc),npc::discard);
   if(result.ok){d.anchor=site.anchor;d.npc=npc.getUUID();d.placed=true;d.setDirty();}else if(result.error!=null)org.apache.logging.log4j.LogManager.getLogger().warn("Yaga hut placement failed: "+result.reason+" at "+result.pos,result.error);return result;
  }catch(RuntimeException error){org.apache.logging.log4j.LogManager.getLogger().warn("Yaga hut preflight failed",error);return new YagaPlacement.Result(false,"internal",candidate,error);}
 }
 private YagaHut(){}
}
