package org.slavicmyths.yaga;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
public final class YagaCommands {
 public static void register(org.slavicmyths.commands.CommandTree tree){tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(Commands.literal("yaga")
  .then(Commands.literal("locate").requires(source->source.hasPermission(2)).executes(c->generate(c.getSource())))
  .then(Commands.literal("generate").executes(c->generate(c.getSource())))
  .then(Commands.literal("status").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();YagaData d=YagaData.get(p.server.getLevel(Level.OVERWORLD));YagaData.Progress v=d.progress(p.getUUID());c.getSource().sendSuccess(()->Component.translatable("yaga.status",Component.translatable("yaga.favor."+v.stage),v.active,v.blocked(p.level().getGameTime())),false);return 1;})))));}
 private static int generate(CommandSourceStack source)throws com.mojang.brigadier.exceptions.CommandSyntaxException{ServerPlayer p=source.getPlayerOrException();if(p.level().dimension()!=Level.OVERWORLD){source.sendFailure(Component.translatable("yaga.fail.dimension"));return 0;}YagaHut.prepare(p.serverLevel());return YagaDiscovery.create(p,YagaData.get(p.serverLevel()).anchor)?1:0;}
}
