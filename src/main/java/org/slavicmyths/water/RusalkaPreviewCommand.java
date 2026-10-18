package org.slavicmyths.water;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit Creative/operator-only preview of an existing nearby singer; never spawns or edits worlds. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class RusalkaPreviewCommand {
 @SubscribeEvent public static void register(RegisterCommandsEvent event){
  var preview=Commands.literal("rusalka_preview");
  for(boolean enable:new boolean[]{true,false})preview.then(Commands.literal(enable?"start":"stop").executes(context->{
   var player=context.getSource().getPlayerOrException();if(!player.isCreative())return 0;
   var singer=player.level().getEntitiesOfClass(RusalkaEntity.class,player.getBoundingBox().inflate(12),r->r.isAlive()&&(enable?r.getTarget()==null:r.previewing()))
    .stream().min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
   if(singer==null||!singer.preview(player,enable)){context.getSource().sendFailure(Component.translatable("rusalka.preview.unavailable"));return 0;}
   context.getSource().sendSuccess(()->Component.translatable(enable?"rusalka.preview.started":"rusalka.preview.stopped"),false);return 1;
  }));
  event.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2)).then(preview)));
 }
}
