package org.slavicmyths.depths;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slavicmyths.registry.ModItems;

@EventBusSubscriber(modid="slavicmyths")
public final class DepthsCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        var root=Commands.literal("depths").requires(s->s.hasPermission(2));
        root.then(Commands.literal("generate").executes(c->{if(!(c.getSource().getEntity() instanceof ServerPlayer p)){c.getSource().sendFailure(Component.translatable("depths.slavicmyths.player_required"));return 0;}return DepthsService.start(p,null)?1:0;}));
        root.then(Commands.literal("map").executes(c->{if(!(c.getSource().getEntity() instanceof ServerPlayer p)){c.getSource().sendFailure(Component.translatable("depths.slavicmyths.player_required"));return 0;}DepthsService.give(p,new ItemStack(ModItems.ANCIENT_DEPTHS_MAP.get()));return 1;}));
        root.then(Commands.literal("locate").executes(c->{
            if(!(c.getSource().getEntity() instanceof ServerPlayer p))return 0;
            var data=DepthsSavedData.get(p.server.getLevel(Level.OVERWORLD));
            var e=data.entries.values().stream().filter(v->v.status.equals("COMPLETE")).min(java.util.Comparator.comparingDouble(v->Math.hypot(v.plan.origin().x()-p.getX(),v.plan.origin().z()-p.getZ()))).orElse(null);
            if(e==null){c.getSource().sendFailure(Component.translatable("depths.slavicmyths.none"));return 0;}
            var o=e.plan.origin();c.getSource().sendSuccess(()->Component.translatable("depths.slavicmyths.located",o.x(),o.y()+1,o.z(),e.plan.id(),e.plan.skylight()?"SKYLIGHT":"CLOSED"),false);return 1;
        }));
        root.then(Commands.literal("status").executes(c->DepthsService.status(c.getSource())));
        event.getDispatcher().register(Commands.literal("slavicmyths").then(root));
    }
    private DepthsCommands(){}
}
