package org.slavicmyths.firebird;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class FirebirdCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("sm").then(Commands.literal("firebird").requires(s->s.hasPermission(2)).then(Commands.literal("status").executes(c->{var w=c.getSource().getServer().overworld();var d=FirebirdEncounter.get(w);c.getSource().sendSuccess(()->Component.literal("Firebird: eligible day="+d.eligible+", observed="+d.observed+", event="+d.event+", bird="+d.bird+", planned="+d.planned+", issued="+Integer.bitCount(d.issued)+", remaining ticks="+d.remaining),false);return 1;}))));}
}
