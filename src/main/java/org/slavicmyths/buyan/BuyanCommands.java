package org.slavicmyths.buyan;

import net.minecraft.commands.Commands;
import org.slavicmyths.commands.CommandTree;

/** One operator command starts the full island; progress controls follow the active phase. */
public final class BuyanCommands {
    private BuyanCommands() {}

    public static void register(CommandTree tree) {
        tree.add(Commands.literal("slavicmyths")
            .then(Commands.literal("dev").requires(source -> source.hasPermission(2))
                .then(Commands.literal("generate")
                    .then(Commands.literal("buyan")
                        .executes(ctx -> BuyanGenerator.generate(ctx.getSource()))))));

        tree.add(Commands.literal("slavicmyths")
            .then(Commands.literal("dev").requires(source -> source.hasPermission(2))
                .then(Commands.literal("buyan")
                    .then(Commands.literal("status")
                        .executes(ctx -> BuyanGenerator.status(ctx.getSource())))
                    .then(Commands.literal("pause")
                        .executes(ctx -> BuyanGenerator.pause(ctx.getSource())))
                    .then(Commands.literal("resume")
                        .executes(ctx -> BuyanGenerator.resume(ctx.getSource())))
                    .then(Commands.literal("harbor")
                        .executes(ctx -> BuyanHarbor.generate(ctx.getSource())))
                    .then(Commands.literal("outpost")
                        .executes(ctx -> BuyanOutpost.generate(ctx.getSource()))
                        .then(Commands.literal("status")
                            .executes(ctx -> BuyanOutpost.status(ctx.getSource()))))
                    .then(Commands.literal("craft")
                        .then(Commands.literal("forge")
                            .executes(ctx -> BuyanCraftQuarter.generate(ctx.getSource())))
                        .then(Commands.literal("status")
                            .executes(ctx -> BuyanCraftQuarter.status(ctx.getSource()))))
                    .then(Commands.literal("shrine")
                        .executes(ctx -> BuyanShrine.generate(ctx.getSource()))
                        .then(Commands.literal("status")
                            .executes(ctx -> BuyanShrine.status(ctx.getSource()))))
                    .then(Commands.literal("terem")
                        .then(Commands.literal("status")
                            .executes(ctx -> BuyanTerem.status(ctx.getSource()))))
                    .then(Commands.literal("nature")
                        .then(Commands.literal("start")
                            .executes(ctx -> BuyanNatureGenerator.start(ctx.getSource())))
                        .then(Commands.literal("status")
                            .executes(ctx -> BuyanNatureGenerator.status(ctx.getSource())))
                        .then(Commands.literal("pause")
                            .executes(ctx -> BuyanNatureGenerator.pause(ctx.getSource())))
                        .then(Commands.literal("resume")
                            .executes(ctx -> BuyanNatureGenerator.resume(ctx.getSource())))))));
    }
}
