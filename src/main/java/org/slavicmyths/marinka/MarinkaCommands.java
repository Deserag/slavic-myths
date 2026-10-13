package org.slavicmyths.marinka;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
/** Explicit admin staging is the safe fallback; no unsolicited generation in existing worlds. */
@EventBusSubscriber(modid="slavicmyths")
public final class MarinkaCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths")
  .then(Commands.literal("hero").then(Commands.literal("dobrynya").executes(c->WitchYard.call(c.getSource().getPlayerOrException())?1:0)))
  .then(Commands.literal("witch_yard").requires(s->s.hasPermission(2))
   .then(Commands.literal("place").executes(c->{var p=c.getSource().getPlayerOrException();var at=BlockPos.containing(p.position().add(p.getLookAngle().multiply(40,0,40))).below();boolean ok=WitchYard.place(p,at,Math.floorMod(Math.round(p.getYRot()/90),4));if(!ok)p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("msg.slavicmyths.yard_invalid"));return ok?1:0;}))
   .then(Commands.literal("start").executes(c->{var p=c.getSource().getPlayerOrException();var a=WitchYard.get(p.serverLevel()).near(p.position());return a!=null&&WitchYard.start(p,a.at(0,1,0),false)?1:0;}))
   .then(Commands.literal("debug_start").requires(s->org.slavicmyths.bogatyr.BogatyrConfig.DEBUG.get()).executes(c->{var p=c.getSource().getPlayerOrException();var a=WitchYard.get(p.serverLevel()).near(p.position());return a!=null&&WitchYard.start(p,a.at(0,1,0),true)?1:0;}))
   .then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();var a=WitchYard.get(p.serverLevel()).near(p.position());c.getSource().sendSuccess(()->net.minecraft.network.chat.Component.literal(a==null?"No witch yard":a.save().toString()),false);return 1;}))));}
 private MarinkaCommands(){}
}
