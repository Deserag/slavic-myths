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
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer)||e.getEntity().tickCount%80!=0||e.getEntity().level().dimension()!=Level.OVERWORLD)return;ServerLevel w=(ServerLevel)e.getEntity().level();YagaData d=YagaData.get(w);if(d.placed||YagaDiscovery.active(w.getServer()))return;prepare(w);if(d.anchor==null||e.getEntity().distanceToSqr(d.anchor.getX(),e.getEntity().getY(),d.anchor.getZ())>128*128)return;BlockPos candidate=d.anchor;if(!loaded(w,candidate))return;YagaDiscovery.create((ServerPlayer)e.getEntity(),candidate);}
 private static boolean loaded(ServerLevel w,BlockPos p){return w.hasChunksAt(p.offset(-10,0,-17),p.offset(10,0,10));}
 private static BlockState state(String id){return STATES.computeIfAbsent(id,s->{try{return BlockStateParser.parseForBlock(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),s,false).blockState();}catch(com.mojang.brigadier.exceptions.CommandSyntaxException ex){throw new IllegalStateException(s,ex);}});}
 public static boolean place(ServerLevel w,BlockPos candidate){return tryPlace(w,candidate).ok;}
 public static YagaPlacement.Cursor beginPlace(ServerLevel w,BlockPos candidate){YagaData d=YagaData.get(w);if(d.placed)return failed(w,YagaPlacement.Result.fail("exists",d.anchor));
  try{YagaPlacement.Site site=YagaPlacement.prepareForCreation(w,candidate,PLAN,YagaHut::state);if(site.failure!=null)return failed(w,site.failure);
   BabaYaga npc=ModEntities.BABA_YAGA.get().create(w);if(npc==null)return failed(w,YagaPlacement.Result.fail("npc",site.anchor));npc.home=site.anchor.offset(YagaHutPlan.HOME);npc.moveTo(npc.home.getX()+.5,npc.home.getY(),npc.home.getZ()+.5,180,0);
   org.slavicmyths.kuzya.KuzyaEntity kuzya=org.slavicmyths.kuzya.KuzyaContent.ENTITY.get().create(w);if(kuzya==null)return failed(w,YagaPlacement.Result.fail("npc",site.anchor));
   java.util.UUID hutId=java.util.UUID.nameUUIDFromBytes((w.dimension().location()+":"+site.anchor.asLong()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
   kuzya.bind(site.anchor,hutId);kuzya.moveTo(site.anchor.getX()+.5,site.anchor.getY()+4,site.anchor.getZ()+.5,180,0);
   return new YagaPlacement.Cursor(w,site,()->{if(!w.addFreshEntity(npc)||!w.addFreshEntity(kuzya))return false;net.minecraft.world.level.block.entity.BlockEntity tile=w.getBlockEntity(site.anchor.offset(-4,4,5));if(!(tile instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel))throw new IllegalStateException("Missing household barrel");net.minecraft.world.level.block.entity.BlockEntity foodTile=w.getBlockEntity(site.anchor.offset(2,4,-2));if(!(foodTile instanceof org.slavicmyths.kitchen.TableTile table))throw new IllegalStateException("Missing food table");barrel.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,org.slavicmyths.kuzya.KuzyaContent.id("chests/baba_yaga_household_supplies")),w.getSeed()^site.anchor.asLong());table.food.set(0,new net.minecraft.world.item.ItemStack(org.slavicmyths.kitchen.KitchenII.item("oat_porridge")));table.food.set(1,new net.minecraft.world.item.ItemStack(org.slavicmyths.kitchen.KitchenII.item("rye_bread")));table.food.set(2,new net.minecraft.world.item.ItemStack(org.slavicmyths.kitchen.KitchenII.item("apple_pie")));table.food.set(3,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.HONEY_BOTTLE));table.setChanged();w.sendBlockUpdated(table.getBlockPos(),table.getBlockState(),table.getBlockState(),2);return true;},()->{npc.discard();kuzya.discard();},result->{
   if(result.ok){d.kuzya=kuzya.getUUID();d.hutId=hutId;d.interiorRevision=1;d.anchor=site.anchor;d.npc=npc.getUUID();d.placed=true;d.setDirty();}else if(result.error!=null)org.apache.logging.log4j.LogManager.getLogger().warn("Yaga hut placement failed: "+result.reason+" at "+result.pos,result.error);});
  }catch(RuntimeException error){org.apache.logging.log4j.LogManager.getLogger().warn("Yaga hut preflight failed",error);return failed(w,new YagaPlacement.Result(false,"internal",candidate,error));}
 }
 public static YagaPlacement.Result tryPlace(ServerLevel w,BlockPos candidate){var cursor=beginPlace(w,candidate);while(!cursor.done())cursor.step(65536,1000000000L);return cursor.result;}
 private static YagaPlacement.Cursor failed(ServerLevel w,YagaPlacement.Result result){var site=new YagaPlacement.Site();site.failure=result;return new YagaPlacement.Cursor(w,site,()->false,()->{},r->{});}
 private YagaHut(){}
}
