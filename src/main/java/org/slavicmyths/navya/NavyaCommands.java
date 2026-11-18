package org.slavicmyths.navya;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public final class NavyaCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree) {
        var command=Commands.literal("navya");
        var trees=Commands.literal("tree");
        String[] sizes={"small","normal","giant"};
        for(int i=0;i<sizes.length;i++) {
            int size=i;
            trees.then(Commands.literal(sizes[i]).executes(context->{
                var source=context.getSource();var player=source.getPlayerOrException();
                var pos=player.blockPosition().relative(player.getDirection(),8);
                boolean success=NavyaTreeFeature.grow(source.getLevel(),pos,source.getLevel().random,size);
                if(success) source.sendSuccess(()->Component.translatable("navya.command.tree",pos.getX(),pos.getY(),pos.getZ()),false);
                else source.sendFailure(Component.translatable("navya.command.blocked"));
                return success?1:0;
            }));
        }
        command.then(trees);
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(command)));
    }
    private NavyaCommands() { }
}
