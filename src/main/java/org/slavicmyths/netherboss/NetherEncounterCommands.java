package org.slavicmyths.netherboss;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slavicmyths.worldgen.ManualStructureRecords;

/** Operator diagnostics: only saved encounters and existing structure records are located. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class NetherEncounterCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> dispatcher=event.getDispatcher();
        dispatcher.register(Commands.literal("netherboss").requires(source->source.hasPermission(2))
            .then(Commands.literal("list").executes(context->list(context.getSource())))
            .then(Commands.literal("nearest").executes(context->nearest(context.getSource(),null))
                .then(Commands.argument("type",StringArgumentType.word()).executes(context->nearest(context.getSource(),StringArgumentType.getString(context,"type")))))
            .then(Commands.literal("posad").executes(context->posad(context.getSource()))));
    }
    private static ServerLevel nether(CommandSourceStack source){
        ServerLevel w=source.getServer().getLevel(Level.NETHER);
        if(w==null)source.sendFailure(Component.literal("Незер недоступен"));
        return w;
    }
    private static int list(CommandSourceStack source){
        ServerLevel w=nether(source);if(w==null)return 0;
        var records=NetherEncounterRecords.get(w);
        source.sendSuccess(()->Component.literal("Зарегистрировано встреч в Незере: "+records.encounters.size()),false);
        for(var e:records.encounters.values())source.sendSuccess(()->Component.literal(e.type+" ["+e.key+"] "+e.anchor.toShortString()+" ("+(e.spawned?"создан":"ожидает")+")"),false);
        return records.encounters.size();
    }
    private static int nearest(CommandSourceStack source,String type){
        ServerLevel w=nether(source);if(w==null)return 0;
        var pos=source.getPosition();
        var nearest=NetherEncounterRecords.get(w).encounters.values().stream().filter(e->type==null||e.type.equals(type)).min(java.util.Comparator.comparingDouble(e->e.anchor.distToCenterSqr(pos.x,pos.y,pos.z))).orElse(null);
        if(nearest==null){source.sendFailure(Component.literal("Подходящая сохранённая встреча пока не найдена"));return 0;}
        source.sendSuccess(()->Component.literal(nearest.type+": "+nearest.anchor.toShortString()+" ("+(nearest.spawned?"создан":"ожидает")+")"),false);
        return 1;
    }
    private static int posad(CommandSourceStack source){
        ServerLevel w=nether(source);if(w==null)return 0;
        var pos=source.getPosition();
        var nearest=ManualStructureRecords.get(w).entries().stream().filter(e->e.family().equals("nether_posad")).min(java.util.Comparator.comparingDouble(e->e.arrival().distToCenterSqr(pos.x,pos.y,pos.z))).orElse(null);
        if(nearest==null){source.sendFailure(Component.literal("Сохранённые Пекельные посады не найдены"));return 0;}
        source.sendSuccess(()->Component.literal("Пекельный посад: "+nearest.arrival().toShortString()),false);
        return 1;
    }
    private NetherEncounterCommands(){}
}
