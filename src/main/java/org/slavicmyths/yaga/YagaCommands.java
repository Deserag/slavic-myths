package org.slavicmyths.yaga;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class YagaCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2)).then(Commands.literal("dev").then(Commands.literal("yaga")
  .then(Commands.literal("locate").executes(c->{ServerWorld w=c.getSource().getServer().getLevel(World.OVERWORLD);YagaHut.prepare(w);YagaData d=YagaData.get(w);if(d.anchor==null){c.getSource().sendFailure(new TranslationTextComponent("yaga.fail.home"));return 0;}c.getSource().sendSuccess(new TranslationTextComponent("yaga.located",d.anchor.toShortString(),d.placed),false);return 1;}))
  .then(Commands.literal("generate").executes(c->{ServerPlayerEntity p=c.getSource().getPlayerOrException();if(p.level.dimension()!=World.OVERWORLD)return 0;YagaData d=YagaData.get((ServerWorld)p.level);if(d.placed){c.getSource().sendFailure(new TranslationTextComponent("yaga.fail.exists"));return 0;}boolean ok=YagaHut.place((ServerWorld)p.level,p.blockPosition().offset(0,0,24));if(!ok)c.getSource().sendFailure(new TranslationTextComponent("yaga.fail.terrain"));else c.getSource().sendSuccess(new TranslationTextComponent("yaga.located",d.anchor.toShortString(),true),false);return ok?1:0;}))
  .then(Commands.literal("status").executes(c->{ServerPlayerEntity p=c.getSource().getPlayerOrException();YagaData d=YagaData.get(p.server.getLevel(World.OVERWORLD));YagaData.Progress v=d.progress(p.getUUID());c.getSource().sendSuccess(new TranslationTextComponent("yaga.status",new TranslationTextComponent("yaga.favor."+v.stage),v.active,v.blocked(p.level.getGameTime())),false);return 1;})))));}
}
