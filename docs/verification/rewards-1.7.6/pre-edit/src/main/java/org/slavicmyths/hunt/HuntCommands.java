package org.slavicmyths.hunt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class HuntCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(Commands.literal("hunt").then(Commands.literal("summon").then(Commands.literal("ovinnik").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),true,true)?1:0)).then(Commands.literal("volkolak").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),false,true)?1:0)).then(Commands.literal("fire_serpent").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),HuntTarget.FIRE_SERPENT,true)?1:0)).then(Commands.literal("podvey").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),HuntTarget.PODVEY,true)?1:0))).then(Commands.literal("clear").executes(c->{net.minecraft.server.level.ServerPlayer p=c.getSource().getPlayerOrException();ServerLevel w=p.server.getLevel(Level.OVERWORLD);HuntRecords d=HuntRecords.get(w);HuntRecords.Hunt h=d.hunts.get(p.getUUID());if(h!=null){d.finish(p.getUUID(),h.target,HuntRecords.Status.FAILED);h.ready=0;d.setDirty();net.minecraft.world.entity.Entity mob=w.getEntity(h.target);if(mob instanceof HuntMob&&p.getUUID().equals(((HuntMob)mob).owner))mob.discard();}p.getCooldowns().removeCooldown(org.slavicmyths.registry.ModItems.OHOTNICHIY_ROG.get());c.getSource().sendSuccess(()->Component.translatable("hunt.cleared"),false);return 1;})))));}
}
