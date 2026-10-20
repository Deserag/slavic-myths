package org.slavicmyths.sivka;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class SivkaCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree){var root=Commands.literal("sivka").requires(s->s.hasPermission(2)).then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();var b=SivkaBonds.get(p.serverLevel()).bonds.get(p.getUUID());c.getSource().sendSuccess(()->Component.literal(b==null?"Sivka: no bond":"Sivka: stage="+b.stage+" expired="+b.expired+" remaining="+Math.max(0,b.expiry-b.play)+" generation="+b.generation+" entity="+b.entity),false);return 1;}));
        if(net.minecraft.SharedConstants.IS_RUNNING_IN_IDE)root.then(Commands.literal("advance_time").then(Commands.argument("ticks",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,72000)).executes(c->{var p=c.getSource().getPlayerOrException();var d=SivkaBonds.get(p.serverLevel());var b=d.bonds.get(p.getUUID());if(b==null)return 0;b.play+=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"ticks");d.setDirty();return 1;})));
        tree.add(Commands.literal("sm").then(root));
    }
    private SivkaCommands(){}
}
