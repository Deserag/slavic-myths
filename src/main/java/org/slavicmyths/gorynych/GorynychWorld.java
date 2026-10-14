package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="slavicmyths")
public final class GorynychWorld {
 public static void ensure(ServerLevel w,UUID id){if(w.dimension()!=Level.OVERWORLD)return;var hill=org.slavicmyths.serpenthill.HillWorld.get(w).row(id);if(!hill.contains("Origin")||hill.getBoolean("Building"))return;var d=GorynychLedger.get(w);d.ensure(id,hill);var n=d.row(id);if(w.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL&&n.getInt("State")<GorynychRules.EXHAUSTED)return;long now=w.getGameTime();if(n.contains("PopulationAt")&&now<n.getLong("PopulationAt"))return;n.putLong("PopulationAt",now+40);
  if(n.hasUUID("Boss")){var known=w.getEntity(n.getUUID("Boss"));if(known instanceof Gorynych)return;for(var realm:w.getServer().getAllLevels())if(realm!=w&&realm.getEntity(n.getUUID("Boss")) instanceof Gorynych)return;if(n.contains("LastPosition")){var last=BlockPos.of(n.getLong("LastPosition"));if(!w.hasChunkAt(last)||!w.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(last)))return;}if(!n.contains("MissingSince")){n.putLong("MissingSince",now);return;}if(now-n.getLong("MissingSince")<200)return;}
  var found=w.getEntitiesOfClass(Gorynych.class,GorynychPlaces.bounds(n),e->id.equals(e.instance));if(!found.isEmpty()){var keeper=found.stream().filter(e->n.hasUUID("Boss")&&n.getUUID("Boss").equals(e.getUUID())).findFirst().orElse(found.getFirst());for(var e:found)if(e!=keeper)e.discard();n.putUUID("Boss",keeper.getUUID());n.remove("MissingSince");d.setDirty();return;}
  var boss=GorynychContent.BOSS.get().create(w);if(boss==null)return;boss.configure(id,n);int start=n.contains("Node")?n.getInt("Node"):Math.floorMod((int)n.getLong("Seed"),GorynychPlaces.NODES.length);for(int i=0;i<GorynychPlaces.NODES.length;i++){int node=(start+i)%GorynychPlaces.NODES.length;Vec3 pos=n.contains("LastPosition")&&i==0?Vec3.atBottomCenterOf(BlockPos.of(n.getLong("LastPosition"))):GorynychPlaces.node(n,node);if(!GorynychPlaces.safe(w,boss,pos))continue;boss.node=node;boss.setYRot((node>=3?-90:0)+GorynychPlaces.rotation(n).rotate(Direction.SOUTH).toYRot());boss.yBodyRot=boss.getYRot();boss.setPos(pos);if(w.addFreshEntity(boss)){n.putUUID("Boss",boss.getUUID());n.putInt("Node",node);n.putLong("LastPosition",boss.blockPosition().asLong());n.remove("MissingSince");d.setDirty();}return;}
 }
 @SubscribeEvent public static void chunk(net.neoforged.neoforge.event.level.ChunkEvent.Load e){if(e.getLevel() instanceof ServerLevel w&&w.dimension()==Level.OVERWORLD){var cp=e.getChunk().getPos();w.getServer().execute(()->{for(UUID id:org.slavicmyths.serpenthill.HillWorld.get(w).near(cp.getMiddleBlockPosition(w.getMinBuildHeight())))ensure(w,id);});}}
 @SubscribeEvent public static void player(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%40==0&&p.serverLevel().dimension()==Level.OVERWORLD)for(UUID id:org.slavicmyths.serpenthill.HillWorld.get(p.serverLevel()).near(p.blockPosition()))ensure(p.serverLevel(),id);}
 private GorynychWorld(){}
}
