package org.slavicmyths.combat;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.DifficultyChangeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Event-driven cleanup: one loaded-entity pass on difficulty changes; no tick/world polling. */
@EventBusSubscriber(modid="slavicmyths")
public final class CreatureSafety {
    public static final Set<String> HOSTILES=Set.of("ataman","bandit_archer","bandit_fighter","bandit_heavy","bandit_senior","nightingale","leshy","kikimora","poludnitsa","polevik","bannik","igosha","rusalka","vodyanoy","bolotnik","elder_vodyanoy","ovinnik","volkolak","cursed_volkolak","fire_serpent","podvey","likho_one_eyed","tugarin_zmey","upyr","nav","kurgan_druzhinnik","kurgan_voevoda","buried_volkhv","unresting_prince","nochnitsa","pesigolovets","kladovik","large_podzemnik","earth_spirit","idolishche_poganoe","kartaus","marinka","skim_beast","kalin_tsar","kalin_common_soldier","kalin_elite_soldier","morozko","karachun","great_poloz","zmey_gorynych","extinguished_flame_guardian","defiled_spring_keeper","earth_servant","morozko_ice_double","karachun_raven","marinka_tur_minion");
    public static boolean protectedPlayer(LivingEntity e){return e instanceof Player p&&(p.isCreative()||p.isSpectator());}
    private static boolean ours(Entity e){return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("slavicmyths");}
    public static boolean hostile(Mob e){
        if(!ours(e))return false;
        if(e instanceof org.slavicmyths.guardians.GuardianEntity g&&g.state()>=org.slavicmyths.guardians.GuardianRules.PURIFY)return false;
        if(e instanceof org.slavicmyths.gorynych.Gorynych g&&g.state()>=org.slavicmyths.gorynych.GorynychRules.EXHAUSTED)return false;
        if(e instanceof org.slavicmyths.karachun.NightRaven r&&r.allied())return false;
        return HOSTILES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath())||e.getType().getCategory()==MobCategory.MONSTER;
    }
    public static void cancel(Mob mob){
        if(!(mob.level() instanceof ServerLevel w)||!hostile(mob)||mob.isRemoved())return;
        mob.setTarget(null);mob.setLastHurtByMob(null);mob.getNavigation().stop();
        if(mob instanceof org.slavicmyths.morozko.Morozko frost)frost.cancelPeacefully();
        else if(mob instanceof org.slavicmyths.karachun.Karachun night)night.cancelPeacefully();
        else if(mob instanceof org.slavicmyths.poloz.GreatPoloz poloz)poloz.cancel();
        else if(mob instanceof org.slavicmyths.gorynych.Gorynych dragon)dragon.cancelPeacefully();
        else if(mob instanceof org.slavicmyths.guardians.GuardianEntity guardian)guardian.cancelPeacefully();
        else if(mob instanceof org.slavicmyths.earth.EarthSpirit earth)earth.cancelPeacefully();
        else {
            if(mob instanceof org.slavicmyths.hunt.HuntMob hunt&&hunt.owner!=null)org.slavicmyths.hunt.HuntRecords.get(w).finish(hunt.owner,hunt.getUUID(),org.slavicmyths.hunt.HuntRecords.Status.FAILED);
            if(mob instanceof org.slavicmyths.hunt.WorldBoss boss&&!boss.encounter.isEmpty()){
                var data=org.slavicmyths.hunt.HuntRecords.get(w);var row=data.bosses.get(boss.encounter);
                if(row!=null&&mob.getUUID().equals(row.target)){row.active=false;row.ready=w.getGameTime();data.setDirty();}
            }
            if(mob instanceof org.slavicmyths.entity.LandSpiritEntity spirit)spirit.mor.cleanup();
            if(mob instanceof org.slavicmyths.finist.KartausEntity kartaus){if(kartaus.encounter!=null){var data=org.slavicmyths.finist.KartausEncounter.get(w);var row=data.entries.get(kartaus.encounter);if(row!=null)data.finish(w,row,false);}kartaus.cleanup();}
            if(mob instanceof org.slavicmyths.kalin.KalinEntity kalin){var row=kalin.entry();if(row!=null)org.slavicmyths.kalin.KalinEncounter.get(w).finish(w,row,false);kalin.cleanup();}
            if(mob instanceof org.slavicmyths.marinka.MarinkaEntity witch){var row=witch.yard();if(row!=null)org.slavicmyths.marinka.WitchYard.fail(w,row);witch.cleanup();}
            if(mob instanceof org.slavicmyths.skim.SkimBeastEntity skim){skim.release();var row=skim.valley();if(row!=null)org.slavicmyths.skim.SkimValley.abort(w,row);}
            mob.discard(); // Never die/kill: no victory, death loot or death advancements.
        }
    }
    private static List<Entity> loaded(ServerLevel w){var out=new ArrayList<Entity>();w.getAllEntities().forEach(out::add);return out;}
    private static Set<UUID> references(Entity e){
        var ids=new HashSet<UUID>();var n=new CompoundTag();e.saveWithoutId(n);
        for(String k:n.getAllKeys())if(n.hasUUID(k))ids.add(n.getUUID(k));return ids;
    }
    @SubscribeEvent public static void difficulty(DifficultyChangeEvent event){
        if(event.getDifficulty()!=Difficulty.PEACEFUL)return;
        var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        server.execute(()->{for(var w:server.getAllLevels()){
            var entities=loaded(w);var ids=new HashSet<UUID>();
            for(var e:entities)if(e instanceof Mob m&&hostile(m)){ids.add(e.getUUID());
                if(m instanceof org.slavicmyths.morozko.Morozko f&&f.session!=null)ids.add(f.session);
                if(m instanceof org.slavicmyths.karachun.Karachun n&&n.session!=null)ids.add(n.session);
                cancel(m);
            }
            // Only a difficulty-change pass, no chunk loading; handle owned projectiles/areas outside local boss radius.
            for(var e:entities)if(!e.isRemoved()&&ours(e)&&!(e instanceof LivingEntity)&&!Collections.disjoint(references(e),ids))e.discard();
            for(var e:entities)if(e instanceof LivingEntity l&&protectedPlayer(l))clearControlled(l);
        }});
    }
    @SubscribeEvent public static void join(EntityJoinLevelEvent event){
        if(event.getLevel() instanceof ServerLevel w&&w.getDifficulty()==Difficulty.PEACEFUL&&event.getEntity() instanceof Mob m&&hostile(m)){
            cancel(m);event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void target(LivingChangeTargetEvent event){
        if(event.getEntity() instanceof Mob&&protectedPlayer(event.getNewAboutToBeSetTarget()))event.setNewAboutToBeSetTarget(null);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void damage(LivingIncomingDamageEvent event){
        // Defensive boundary for delayed/projectile/AoE hits; generic_kill remains an explicit admin operation.
        if(event.getSource().is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL))return;
        if(protectedPlayer(event.getEntity())||event.getSource().getEntity() instanceof Mob m&&hostile(m)&&m.level().getDifficulty()==Difficulty.PEACEFUL)event.setCanceled(true);
    }
    private static void clearControlled(LivingEntity player){
        org.slavicmyths.karachun.NightStatus.clearAll(player);
        for(String key:new ArrayList<>(player.getPersistentData().getCompound("Night192Effects").getAllKeys()))try{org.slavicmyths.karachun.NightEffects.clear(player,UUID.fromString(key));}catch(IllegalArgumentException ignored){}
        org.slavicmyths.morozko.FrostCold.stop(player);
        if(player.getVehicle() instanceof org.slavicmyths.volkolak.CursedVolkolak)player.stopRiding();
        player.getPersistentData().remove("VolkolakHolder");player.getPersistentData().remove("VolkolakHeldUntil");
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void mode(PlayerEvent.PlayerChangeGameModeEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p)||event.getNewGameMode()!=GameType.CREATIVE&&event.getNewGameMode()!=GameType.SPECTATOR)return;
        // Event precedes the actual mode assignment; defer so each controller sees the final mode.
        p.server.tell(new net.minecraft.server.TickTask(p.server.getTickCount()+1,()->{if(!protectedPlayer(p))return;clearControlled(p);
            for(var m:p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(128))){
                if(m.getTarget()==p){m.setTarget(null);m.getNavigation().stop();}if(m.getLastHurtByMob()==p)m.setLastHurtByMob(null);
                if(m instanceof org.slavicmyths.gorynych.Gorynych g)g.cancelTarget(p);
                if(m instanceof org.slavicmyths.hunt.WorldBoss boss)boss.cancelTarget(p);
                if(m instanceof org.slavicmyths.skim.SkimBeastEntity skim&&skim.victim!=null&&skim.victim.equals(p.getUUID()))skim.release();
            }
        }));
    }
    private CreatureSafety(){}
}
