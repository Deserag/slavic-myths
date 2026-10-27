package org.slavicmyths.chud;

import java.util.*;
import java.util.concurrent.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/** Read-only diagnostics: inspect loaded starts or forecast natural starts. Never build. */
public final class ChudSearchCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree) {
        for(String name:List.of("chud_city","chud_abandoned")) {
            // Exact literals take precedence over vanilla's argument; other structures retain vanilla locate.
            tree.add(Commands.literal("locate").requires(s->s.hasPermission(2))
                .then(Commands.literal("structure").then(Commands.literal("slavicmyths:"+name)
                    .executes(c->start(c.getSource(),name,null,0)))));
        }
        for(String branch:List.of("city","abandoned")) {
            String structure=branch.equals("city")?"chud_city":"chud_abandoned";
            var search=Commands.literal("search").executes(c->start(c.getSource(),structure,null,0))
                .then(Commands.argument("page",IntegerArgumentType.integer(0,128))
                    .executes(c->start(c.getSource(),structure,null,IntegerArgumentType.getInteger(c,"page"))));
            if(branch.equals("abandoned"))for(var kind:AbandonedPlan.Kind.values()) {
                String label=switch(kind){case ATTACHED_SMALL->"attached";case INDEPENDENT_MEDIUM->"medium";case INDEPENDENT_GREAT->"great";};
                search.then(Commands.literal(label).executes(c->start(c.getSource(),structure,kind,0))
                    .then(Commands.argument("page",IntegerArgumentType.integer(0,128))
                        .executes(c->start(c.getSource(),structure,kind,IntegerArgumentType.getInteger(c,"page")))));
            }
            tree.add(Commands.literal("slavicmyths").then(Commands.literal(branch)
                .requires(s->s.hasPermission(2)).then(search)));
        }
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("chud_search")
            .requires(s->s.hasPermission(2))
            .then(Commands.literal("status").executes(c->control(c.getSource(),false)))
            .then(Commands.literal("next").executes(c->next(c.getSource())))
            .then(Commands.literal("cancel").executes(c->control(c.getSource(),true)))));
    }
    private static int control(CommandSourceStack source,boolean cancel){return ChudFutureSearch.control(source,cancel);}
    private static int next(CommandSourceStack source){return ChudFutureSearch.next(source);}
    static int searchCity(CommandSourceStack source){return start(source,"chud_city",null,0);}
    static int searchAbandoned(CommandSourceStack source,AbandonedPlan.Kind kind){return start(source,"chud_abandoned",kind,0);}
    private static int start(CommandSourceStack source,String name,AbandonedPlan.Kind kind,int page){
        if(page==0&&kind==null&&org.slavicmyths.worldgen.ExistingSiteSearch.locate(source,name,1024,false)!=0)return 1;
        return ChudFutureSearch.start(source,name,kind,page);
    }
    private ChudSearchCommands(){}
}
