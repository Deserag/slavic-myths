package org.slavicmyths.buyan;

import net.minecraft.commands.Commands;
import org.slavicmyths.commands.CommandTree;

/** Generates only the foundation; island size is determined by the world seed. */
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
                        .executes(ctx -> BuyanGenerator.resume(ctx.getSource()))))));
    }
}
