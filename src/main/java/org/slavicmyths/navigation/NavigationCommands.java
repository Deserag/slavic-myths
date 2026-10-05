package org.slavicmyths.navigation;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class NavigationCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("navigation").executes(c->{NavigationNetwork.sync(c.getSource().getPlayerOrException(),true);return 1;})));
        var navigation=Commands.literal("navigation")
            .then(Commands.literal("list").executes(c->{var p=c.getSource().getPlayerOrException();var s=NavigationRecords.get(p.serverLevel()).player(p.getUUID());s.markers.values().forEach(m->c.getSource().sendSuccess(()->Component.literal(m.id()+" "+m.category()+" "+m.kind()+" "+m.name()),false));return s.markers.size();}))
            .then(Commands.literal("marker").then(Commands.argument("id",net.minecraft.commands.arguments.UuidArgument.uuid()).executes(c->{var p=c.getSource().getPlayerOrException();java.util.UUID id=net.minecraft.commands.arguments.UuidArgument.getUuid(c,"id");var m=NavigationRecords.get(p.serverLevel()).player(p.getUUID()).markers.get(id);if(m==null)return 0;c.getSource().sendSuccess(()->Component.literal(m.save().toString()),false);return 1;})))
            .then(Commands.literal("clear-owned").executes(c->{var p=c.getSource().getPlayerOrException();NavigationManager.request(p,new NavigationNetwork.Request(NavigationNetwork.Action.CLEAR_OWNED,new java.util.UUID(0,0),0,true));return 1;}))
            .then(Commands.literal("searcharea")
                .then(Commands.literal("stats").executes(c->{var stats=NavigationStatistics.run(1000);c.getSource().sendSuccess(()->Component.literal(stats.toString()),false);return stats.outside()==0?1:0;}))
                .then(Commands.literal("create-test").then(Commands.argument("targetType",StringArgumentType.word()).suggests((c,b)->{for(var kind:SearchArea.Scale.values())b.suggest(kind.name().toLowerCase());return b.buildFuture();}).executes(c->{var p=c.getSource().getPlayerOrException();SearchArea.Scale scale;try{scale=SearchArea.Scale.valueOf(StringArgumentType.getString(c,"targetType").toUpperCase(java.util.Locale.ROOT));}catch(IllegalArgumentException bad){return 0;}
                    var id=java.util.UUID.randomUUID();var area=SearchArea.generate(p.getX()+300,p.getZ()+200,scale,id,p.serverLevel().getSeed(),0,(x,z,r)->true);
                    if(area==null)return 0;var s=NavigationRecords.get(p.serverLevel()).player(p.getUUID());s.upsert(new SlavicMarker(id,"dev:"+id,MarkerCategory.QUEST,SlavicMarker.Kind.SEARCH_AREA,SlavicMarker.Lifetime.UNTIL_EVENT_END,"navigation.slavicmyths.test_area",p.level().dimension().location(),(int)area.centerX(),0,(int)area.centerZ(),p.level().getGameTime(),0,area));s.track(id);NavigationManager.changed(p);return 1;}))));
        e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2)).then(navigation)));
    }
}
