package org.slavicmyths.rpg.classes;

import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ClassCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        var player=Commands.argument("player",EntityArgument.player());
        player.then(Commands.literal("state").executes(c->{var p=EntityArgument.getPlayer(c,"player");c.getSource().sendSuccess(()->Component.literal(ClassState.data(p).toString()),false);return 1;}));
        player.then(Commands.literal("xp").then(Commands.argument("amount",IntegerArgumentType.integer(1,ClassDefinitions.MAX_XP_GRANT)).executes(c->{ClassState.awardXp(EntityArgument.getPlayer(c,"player"),IntegerArgumentType.getInteger(c,"amount"));return 1;})));
        player.then(Commands.literal("level").then(Commands.argument("value",IntegerArgumentType.integer(1,ClassDefinitions.MAX_LEVEL)).executes(c->{var p=EntityArgument.getPlayer(c,"player");ClassState.data(p).putInt("Level",IntegerArgumentType.getInteger(c,"value"));ClassState.data(p).putLong("Xp",0);ClassState.sync(p);return 1;})));
        player.then(Commands.literal("points").then(Commands.argument("value",IntegerArgumentType.integer(0,10000)).executes(c->{var p=EntityArgument.getPlayer(c,"player");ClassState.data(p).putInt("Points",IntegerArgumentType.getInteger(c,"value"));ClassState.sync(p);return 1;})));
        player.then(Commands.literal("add_points").then(Commands.argument("value",IntegerArgumentType.integer(1,10000)).executes(c->{var p=EntityArgument.getPlayer(c,"player");var d=ClassState.data(p);d.putInt("Points",Math.min(10000,d.getInt("Points")+IntegerArgumentType.getInteger(c,"value")));ClassState.sync(p);return 1;})));
        player.then(Commands.literal("cooldown_reset").executes(c->{var p=EntityArgument.getPlayer(c,"player");ClassState.data(p).put("Cooldowns",new net.minecraft.nbt.CompoundTag());ClassState.sync(p);return 1;}));
        for(String action:java.util.List.of("choose","force","learn","forget"))player.then(Commands.literal(action).then(Commands.argument("id",StringArgumentType.word()).suggests((c,b)->{(action.equals("choose")||action.equals("force")?ClassDefinitions.BASES:ClassDefinitions.SKILLS.keySet()).forEach(b::suggest);return b.buildFuture();}).executes(c->{var p=EntityArgument.getPlayer(c,"player");String id=StringArgumentType.getString(c,"id");if(action.equals("choose"))return ClassState.choose(p,id)?1:0;if(action.equals("learn"))return ClassState.learn(p,id)?1:0;var d=ClassState.data(p);
            if(action.equals("force")){if(!ClassDefinitions.BASES.contains(id))return 0;d.putString("Base",id);d.remove("First");d.remove("Second");for(int n=0;n<3;n++)d.remove("Active"+n);d.remove("Passive");}
            else {if(!ClassDefinitions.SKILLS.containsKey(id))return 0;int rank=d.getCompound("Ranks").getInt(id);d.getCompound("Ranks").remove(id);d.putInt("Points",Math.min(10000,d.getInt("Points")+rank));for(int n=0;n<3;n++)if(d.getString("Active"+n).equals(id))d.remove("Active"+n);if(d.getString("Passive").equals(id))d.remove("Passive");}
            ClassState.restore(p);ClassState.sync(p);return 1;})));
        e.getDispatcher().register(Commands.literal("smclass").requires(s->s.hasPermission(2)).then(player));
    }
    private ClassCommands(){}
}
