package org.slavicmyths.hunt;
import net.minecraft.command.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class HuntCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2)).then(Commands.literal("dev").then(Commands.literal("hunt").then(Commands.literal("summon").then(Commands.literal("ovinnik").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),true,true)?1:0)).then(Commands.literal("volkolak").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),false,true)?1:0)).then(Commands.literal("fire_serpent").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),HuntTarget.FIRE_SERPENT,true)?1:0)).then(Commands.literal("podvey").executes(c->HuntItems.summon(c.getSource().getPlayerOrException(),HuntTarget.PODVEY,true)?1:0))).then(Commands.literal("clear").executes(c->{net.minecraft.entity.player.ServerPlayerEntity p=c.getSource().getPlayerOrException();ServerWorld w=p.server.getLevel(World.OVERWORLD);HuntRecords d=HuntRecords.get(w);HuntRecords.Hunt h=d.hunts.get(p.getUUID());if(h!=null){d.finish(p.getUUID(),h.target,HuntRecords.Status.FAILED);h.ready=0;d.setDirty();net.minecraft.entity.Entity mob=w.getEntity(h.target);if(mob instanceof HuntMob&&p.getUUID().equals(((HuntMob)mob).owner))mob.remove();}p.getCooldowns().removeCooldown(org.slavicmyths.registry.ModItems.OHOTNICHIY_ROG.get());c.getSource().sendSuccess(new net.minecraft.util.text.TranslationTextComponent("hunt.cleared"),false);return 1;})))));}
}
