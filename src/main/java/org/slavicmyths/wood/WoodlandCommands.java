package org.slavicmyths.wood;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class WoodlandCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){
  com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> tree=Commands.literal("growtree");
  for(String s:Woodlands.SETS.keySet())tree.then(Commands.literal(s).executes(c->grow(c.getSource(),s)).then(Commands.literal("random").executes(c->grow(c.getSource(),s))));
  e.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2)).then(Commands.literal("dev").then(tree)));
 }
 private static int grow(CommandSource source,String species)throws com.mojang.brigadier.exceptions.CommandSyntaxException {
  ServerPlayerEntity player=source.getPlayerOrException();BlockPos front=player.blockPosition().relative(player.getDirection(),8);
  for(int i=0;i<8;i++){BlockPos p=source.getLevel().getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,front.offset(i%3*3-3,0,i/3*3-3));
   if(!source.getLevel().getWorldBorder().isWithinBounds(p))continue;
   if(WoodlandWorldgen.TREES.get(species).get().grow(source.getLevel(),source.getLevel().random,p)){source.sendSuccess(new TranslationTextComponent("woodlands.command.grown",species,p.getX(),p.getY(),p.getZ()),false);return 1;}}
  source.sendFailure(new TranslationTextComponent("woodlands.command.blocked"));return 0;
 }
}
