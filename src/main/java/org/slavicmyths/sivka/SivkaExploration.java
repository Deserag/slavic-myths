package org.slavicmyths.sivka;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.world.EncounterPlacement;

/** Personal first opportunity only. Loaded terrain, at most one 12-column search/server/5 seconds. */
@EventBusSubscriber(modid="slavicmyths")
public final class SivkaExploration extends SavedData {
    public final Map<UUID,FirstSivkaProgress> players=new LinkedHashMap<>();
    private final Map<UUID,Sample> samples=new HashMap<>();
    private long attemptAt=Long.MIN_VALUE;
    private record Sample(ServerPlayer player,long distance,int tick){}
    private static final class Travel { private static final ResourceLocation[] IDS={Stats.WALK_ONE_CM,Stats.CROUCH_ONE_CM,Stats.SPRINT_ONE_CM,
            Stats.WALK_ON_WATER_ONE_CM,Stats.WALK_UNDER_WATER_ONE_CM,Stats.SWIM_ONE_CM,Stats.HORSE_ONE_CM,
            Stats.BOAT_ONE_CM,Stats.PIG_ONE_CM,Stats.STRIDER_ONE_CM,Stats.MINECART_ONE_CM,Stats.AVIATE_ONE_CM,Stats.FLY_ONE_CM}; }
    public static SivkaExploration get(ServerLevel world){return world.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(SivkaExploration::new,SivkaExploration::load),"slavicmyths_sivka_exploration");}
    public static SivkaExploration load(CompoundTag tag,HolderLookup.Provider provider){var d=new SivkaExploration();for(var raw:tag.getList("Players",Tag.TAG_COMPOUND)){var n=(CompoundTag)raw;if(n.hasUUID("Player"))d.players.put(n.getUUID("Player"),FirstSivkaProgress.load(n));}return d;}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){var list=new ListTag();players.forEach((id,p)->{var n=p.save();n.putUUID("Player",id);list.add(n);});tag.putInt("Version",1);tag.put("Players",list);return tag;}
    private static long distance(ServerPlayer p){long total=0;for(var id:Travel.IDS)total+=Math.max(0,p.getStats().getValue(Stats.CUSTOM.get(id)));return total;}
    @SubscribeEvent public static void sample(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%20!=0)return;
        var d=get(p.serverLevel());var id=p.getUUID();
        if(!p.isAlive()||p.isCreative()||p.isSpectator()||p.level().dimension()!=Level.OVERWORLD){d.samples.remove(id);return;}
        var progress=d.players.computeIfAbsent(id,k->{d.setDirty();return new FirstSivkaProgress(p.server.overworld().getSeed()^id.getMostSignificantBits()^id.getLeastSignificantBits());});
        if(progress.met){d.samples.remove(id);return;}
        // Existing bonds prove an earlier actual encounter, including pre-update saves.
        if(SivkaBonds.get(p.serverLevel()).bonds.containsKey(id)||org.slavicmyths.tales.TaleData.get(p.serverLevel()).stage(id,"slavicmyths:sivka_burka")>=2){progress.encounter();d.samples.remove(id);d.setDirty();return;}
        long travelled=distance(p);var old=d.samples.put(id,new Sample(p,travelled,p.tickCount));
        if(old!=null&&old.player()==p&&p.tickCount-old.tick()==20){progress.sample(20,Math.max(0,travelled-old.distance()));d.setDirty();}
        if(p.tickCount%100!=0)return;
        var w=p.serverLevel();var nearby=w.getEntitiesOfClass(SivkaEntity.class,p.getBoundingBox().inflate(128),h->h.isAlive());
        if(nearby.stream().anyMatch(h->p.distanceToSqr(h)<=32*32&&p.hasLineOfSight(h))){progress.encounter();d.setDirty();return;}
        if(!progress.pending||!nearby.isEmpty()||!w.isDay()||!w.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)||d.attemptAt==w.getGameTime()/100)return;
        var waiting=w.players().stream().filter(other->{var state=d.players.get(other.getUUID());return state!=null&&state.pending&&!state.met&&other.isAlive()&&!other.isCreative()&&!other.isSpectator();}).toList();
        if(waiting.isEmpty()||waiting.get((int)Math.floorMod(w.getGameTime()/100,(long)waiting.size()))!=p)return;
        d.attemptAt=w.getGameTime()/100;
        // No random reroll on unsafe terrain: pending remains saved until a visible safe opportunity exists.
        for(int i=0;i<12;i++){
            double angle=(p.getYRot()+i*30)*Math.PI/180;int x=(int)Math.floor(p.getX()-Math.sin(angle)*24),z=(int)Math.floor(p.getZ()+Math.cos(angle)*24);
            if(!w.hasChunk(x>>4,z>>4))continue;
            var at=new BlockPos(x,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z);
            var biome=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,SivkaContent.id("sivka_grasslands"));
            if(!w.getBiome(at).is(biome)||!SivkaContent.canAppear(w,at))continue;
            var horse=SivkaContent.ENTITY.get().create(w);if(horse==null)return;horse.moveTo(x+.5,at.getY(),z+.5,p.getYRot(),0);
            if(p.distanceToSqr(horse)>32*32||!p.hasLineOfSight(horse)||!EncounterPlacement.clear(w,horse.getBoundingBox())||!w.noCollision(horse))continue;
            if(w.addFreshEntity(horse)){progress.encounter();d.setDirty();}
            return;
        }
    }
    @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p)get(p.serverLevel()).samples.remove(p.getUUID());}
    @SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event){if(event.getEntity() instanceof ServerPlayer p)get(p.serverLevel()).samples.remove(p.getUUID());}
    @SubscribeEvent public static void mode(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangeGameModeEvent event){if(event.getEntity() instanceof ServerPlayer p)get(p.serverLevel()).samples.remove(p.getUUID());}
}
