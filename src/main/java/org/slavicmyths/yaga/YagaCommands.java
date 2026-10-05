package org.slavicmyths.yaga;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class YagaCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(Commands.literal("yaga")
  .then(Commands.literal("locate").requires(source->source.hasPermission(2)).executes(c->{ServerLevel w=c.getSource().getServer().getLevel(Level.OVERWORLD);YagaHut.prepare(w);YagaData d=YagaData.get(w);if(d.anchor==null){c.getSource().sendFailure(Component.translatable("yaga.fail.home"));return 0;}c.getSource().sendSuccess(()->d.placed?Component.translatable("yaga.located",d.anchor.toShortString(),true):Component.translatable("yaga.site.planned",d.anchor.getX(),d.anchor.getZ()),false);return 1;}))
  .then(Commands.literal("generate").executes(c->generate(c.getSource())))
  .then(Commands.literal("status").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();YagaData d=YagaData.get(p.server.getLevel(Level.OVERWORLD));YagaData.Progress v=d.progress(p.getUUID());c.getSource().sendSuccess(()->Component.translatable("yaga.status",Component.translatable("yaga.favor."+v.stage),v.active,v.blocked(p.level().getGameTime())),false);return 1;})))));}
 private static int generate(CommandSourceStack source)throws com.mojang.brigadier.exceptions.CommandSyntaxException{ServerPlayer p=source.getPlayerOrException();if(p.level().dimension()!=Level.OVERWORLD){source.sendFailure(Component.translatable("yaga.fail.dimension"));return 0;}
  ServerLevel w=(ServerLevel)p.level();YagaData d=YagaData.get(w);if(d.placed){source.sendFailure(Component.translatable("yaga.fail.exists"));source.sendSuccess(()->Component.translatable("yaga.located",d.anchor.toShortString(),true),false);return 0;}
  YagaPlacement.Result useful=null;for(int distance:new int[]{20,12})for(int[] dir:new int[][]{{0,1},{1,0},{0,-1},{-1,0}}){YagaPlacement.Result r=YagaHut.tryPlace(w,p.blockPosition().offset(dir[0]*distance,0,dir[1]*distance));if(r.ok){source.sendSuccess(()->Component.translatable("yaga.located",r.pos.toShortString(),true),false);return 1;}if(useful==null||!r.reason.equals("chunks"))useful=r;}
  source.sendFailure(Component.translatable("yaga.generation.failed",Component.translatable("yaga.generation.reason."+useful.reason),useful.pos.toShortString()));return 0;
 }
}
