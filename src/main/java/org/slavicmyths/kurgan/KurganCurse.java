package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.*;
import net.minecraft.potion.*;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(modid="slavicmyths")
public final class KurganCurse {
    public static final DeferredRegister<Effect> EFFECTS=DeferredRegister.create(ForgeRegistries.POTIONS,"slavicmyths");
    public static final RegistryObject<Effect> CURSE=EFFECTS.register("kurgan_curse",()->new CurseEffect());
    private static final class CurseEffect extends Effect {
        CurseEffect(){super(EffectType.HARMFUL,0x657780);addAttributeModifier(Attributes.MOVEMENT_SPEED,"4fe2072b-4d7e-4fe9-b8a6-3c925b5817da",-.1,AttributeModifier.Operation.MULTIPLY_TOTAL);addAttributeModifier(Attributes.ATTACK_DAMAGE,"157d7501-d09e-4eaf-973c-9725920872e9",-.1,AttributeModifier.Operation.MULTIPLY_TOTAL);}
        @Override public double getAttributeModifierValue(int amplifier,AttributeModifier modifier){return amplifier>0?-.15:-.10;}
    }
    private static BurialRecords data(ServerPlayerEntity p){return BurialRecords.get(p.getServer().getLevel(World.OVERWORLD));}
    public static boolean persistent(ServerPlayerEntity p){return data(p).persistentCurses.containsKey(p.getUUID());}
    public static void apply(ServerPlayerEntity p,UUID source,boolean persistent){if(persistent){BurialRecords d=data(p);d.persistentCurses.computeIfAbsent(p.getUUID(),id->new HashSet<>()).add(source);d.setDirty();}boolean strong=persistent(p);p.addEffect(new EffectInstance(CURSE.get(),strong?Integer.MAX_VALUE:36000,strong?1:0,false,false,true));}
    /** Admin removal. Removes both saved sources and the visible effect. */
    public static void clear(ServerPlayerEntity p){BurialRecords d=data(p);d.persistentCurses.remove(p.getUUID());d.setDirty();p.removeEffect(CURSE.get());}
    /** Source-specific victory hook: retain curses originating in other tombs. */
    public static void bossDefeated(ServerPlayerEntity p,UUID source){BurialRecords d=data(p);Set<UUID> ids=d.persistentCurses.get(p.getUUID());if(ids!=null){ids.remove(source);if(ids.isEmpty())clear(p);else d.setDirty();}}
    public static void sourceDefeated(net.minecraft.world.server.ServerWorld world,UUID source){BurialRecords d=BurialRecords.get(world.getServer().getLevel(World.OVERWORLD));for(UUID player:d.clearCurseSource(source)){ServerPlayerEntity online=world.getServer().getPlayerList().getPlayer(player);if(online!=null)online.removeEffect(CURSE.get());}}
    private static void restore(ServerPlayerEntity p){if(!p.isAlive())return;EffectInstance e=p.getEffect(CURSE.get());if(!persistent(p)){if(e!=null&&e.getAmplifier()==1)p.removeEffect(CURSE.get());return;}if(e==null||e.getAmplifier()!=1)p.addEffect(new EffectInstance(CURSE.get(),Integer.MAX_VALUE,1,false,false,true));}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getPlayer() instanceof ServerPlayerEntity)restore((ServerPlayerEntity)e.getPlayer());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getPlayer() instanceof ServerPlayerEntity)restore((ServerPlayerEntity)e.getPlayer());}
    // One saved-data lookup per player per second, no entity/world/chunk scan. Covers milk and all generic clear APIs.
    @SubscribeEvent public static void safety(TickEvent.PlayerTickEvent e){if(e.phase==TickEvent.Phase.END&&e.player instanceof ServerPlayerEntity&&e.player.tickCount%20==0)restore((ServerPlayerEntity)e.player);}
}
