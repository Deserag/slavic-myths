package org.slavicmyths.buyan;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import org.slavicmyths.commands.CommandTree;

/** Operator-only manual foundation generation; not a natural worldgen claim. */
public final class BuyanCommands {
    private BuyanCommands() {}

    public static void register(CommandTree tree) {
        tree.add(Commands.literal("slavicmyths")
            .then(Commands.literal("dev").requires(source -> source.hasPermission(2))
                .then(Commands.literal("generate")
                    .then(Commands.literal("buyan")
                        .executes(ctx -> BuyanGenerator.generate(ctx.getSource(), 700))
                        .then(Commands.argument("radius",
                            IntegerArgumentType.integer(BuyanTerrain.MIN_RADIUS, BuyanTerrain.MAX_RADIUS))
                            .executes(ctx -> BuyanGenerator.generate(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "radius"))))))));

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
