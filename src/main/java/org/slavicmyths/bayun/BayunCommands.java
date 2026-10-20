package org.slavicmyths.bayun;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class BayunCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree){tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2)).then(Commands.literal("bayun").then(Commands.literal("generate").executes(c->{var source=c.getSource();boolean ok=BayunFeature.place(source.getLevel(),BlockPos.containing(source.getPosition()));source.sendSuccess(()->Component.translatable(ok?"bayun.site.created":"bayun.site.blocked"),false);return ok?1:0;})))));}
    private BayunCommands(){}
}
