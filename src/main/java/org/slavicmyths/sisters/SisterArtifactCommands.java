package org.slavicmyths.sisters;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import java.util.ArrayList;
import java.util.List;

public final class SisterArtifactCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("sisters_artifact")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("type", StringArgumentType.word())
                    .then(Commands.argument("power1", StringArgumentType.word())
                        .then(Commands.argument("power2", StringArgumentType.word())
                            .then(Commands.argument("power3", StringArgumentType.word())
                                .executes(context -> give(context.getSource().getPlayerOrException(),
                                    EntityArgument.getPlayer(context, "player"),
                                    StringArgumentType.getString(context, "type"),
                                    List.of(StringArgumentType.getString(context, "power1"),
                                        StringArgumentType.getString(context, "power2"),
                                        StringArgumentType.getString(context, "power3"))))))))));
    }

    private static int give(ServerPlayer executor, ServerPlayer target, String type, List<String> powers) {
        var holder = SisterArtifacts.ITEMS.get(type);
        if (holder == null || powers.stream().anyMatch(power -> !SisterArtifacts.IDS.contains(power)) || powers.stream().distinct().count() != powers.size()) {
            executor.sendSystemMessage(Component.literal("Invalid artifact type or powers. Types: " + SisterArtifacts.TYPES + "; powers: " + SisterArtifacts.IDS));
            return 0;
        }
        ItemStack stack = new ItemStack(holder.get());
        for (String power : powers) {
            stack = SisterArtifacts.addPower(stack, power);
            if (stack.isEmpty()) return 0;
        }
        if (!target.getInventory().add(stack)) target.drop(stack, false);
        executor.sendSystemMessage(Component.literal("Created " + type + " with powers " + powers + " for " + target.getName().getString()));
        return 1;
    }

    private SisterArtifactCommands() {}
}
