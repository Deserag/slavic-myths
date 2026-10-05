package org.slavicmyths.wood;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class WoodlandCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){
  com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> tree=Commands.literal("growtree");
  for(String s:Woodlands.SETS.keySet())tree.then(Commands.literal(s).executes(c->grow(c.getSource(),s)).then(Commands.literal("random").executes(c->grow(c.getSource(),s))));
  e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(tree)));
 }
 private static int grow(CommandSourceStack source,String species)throws com.mojang.brigadier.exceptions.CommandSyntaxException {
  ServerPlayer player=source.getPlayerOrException();BlockPos front=player.blockPosition().relative(player.getDirection(),8);
  for(int i=0;i<8;i++){BlockPos p=source.getLevel().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,front.offset(i%3*3-3,0,i/3*3-3));
   if(!source.getLevel().getWorldBorder().isWithinBounds(p))continue;
   if(WoodlandWorldgen.TREES.get(species).get().grow(source.getLevel(),source.getLevel().random,p)){source.sendSuccess(()->Component.translatable("woodlands.command.grown",species,p.getX(),p.getY(),p.getZ()),false);return 1;}}
  source.sendFailure(Component.translatable("woodlands.command.blocked"));return 0;
 }
}
