package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit operator excavation, with persisted starts and portal-room protection. */
public final class ChudGenerateCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree){
        var city=Commands.literal("city").requires(s->s.hasPermission(2));
        city.then(Commands.literal("generate").executes(c->generate(c.getSource(),"city"))
            .then(Commands.literal("attached").executes(c->generate(c.getSource(),"attached"))));
        var abandoned=Commands.literal("abandoned").requires(s->s.hasPermission(2));
        var generate=Commands.literal("generate").executes(c->generate(c.getSource(),"medium"));
        for(String kind:List.of("medium","great"))generate.then(Commands.literal(kind).executes(c->generate(c.getSource(),kind)));
        abandoned.then(generate);
        tree.add(Commands.literal("slavicmyths").then(city).then(abandoned));
    }
    private static int generate(CommandSourceStack source,String kind){
        return ChudCreationJobs.create(source,kind,result->{});
    }

    private ChudGenerateCommands(){}
}
