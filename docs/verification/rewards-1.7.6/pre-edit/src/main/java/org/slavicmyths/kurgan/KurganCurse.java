package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.common.EventBusSubscriber;


@EventBusSubscriber(modid="slavicmyths")
public final class KurganCurse {
    public static final net.neoforged.neoforge.registries.DeferredRegister<MobEffect> EFFECTS=org.slavicmyths.registry.ModEffects.EFFECTS;
    public static final net.neoforged.neoforge.registries.DeferredHolder<MobEffect,MobEffect> CURSE=org.slavicmyths.registry.ModEffects.CURSE;
    private static BurialRecords data(ServerPlayer p){return BurialRecords.get(p.getServer().getLevel(Level.OVERWORLD));}
    public static boolean persistent(ServerPlayer p){return data(p).persistentCurses.containsKey(p.getUUID());}
    public static void apply(ServerPlayer p,UUID source,boolean persistent){if(persistent){BurialRecords d=data(p);d.persistentCurses.computeIfAbsent(p.getUUID(),id->new HashSet<>()).add(source);d.setDirty();}boolean strong=persistent(p);p.addEffect(new MobEffectInstance(CURSE,strong?Integer.MAX_VALUE:36000,strong?1:0,false,false,true));}
    /** Admin removal. Removes both saved sources and the visible effect. */
    public static void clear(ServerPlayer p){BurialRecords d=data(p);d.persistentCurses.remove(p.getUUID());d.setDirty();p.removeEffect(CURSE);}
    /** Source-specific victory hook: retain curses originating in other tombs. */
    public static void bossDefeated(ServerPlayer p,UUID source){BurialRecords d=data(p);Set<UUID> ids=d.persistentCurses.get(p.getUUID());if(ids!=null){ids.remove(source);if(ids.isEmpty())clear(p);else d.setDirty();}}
    public static void sourceDefeated(net.minecraft.server.level.ServerLevel world,UUID source){BurialRecords d=BurialRecords.get(world.getServer().getLevel(Level.OVERWORLD));for(UUID player:d.clearCurseSource(source)){ServerPlayer online=world.getServer().getPlayerList().getPlayer(player);if(online!=null)online.removeEffect(CURSE);}}
    private static void restore(ServerPlayer p){if(!p.isAlive())return;MobEffectInstance e=p.getEffect(CURSE);if(!persistent(p)){if(e!=null&&e.getAmplifier()==1)p.removeEffect(CURSE);return;}if(e==null||e.getAmplifier()!=1)p.addEffect(new MobEffectInstance(CURSE,Integer.MAX_VALUE,1,false,false,true));}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer)restore((ServerPlayer)e.getEntity());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer)restore((ServerPlayer)e.getEntity());}
    // One saved-data lookup per player per second, no entity/world/chunk scan. Covers milk and all generic clear APIs.
    @SubscribeEvent public static void safety(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer&&e.getEntity().tickCount%20==0)restore((ServerPlayer)e.getEntity());}
}
