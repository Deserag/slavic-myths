package org.slavicmyths.wolf;
import java.util.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class WolfCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent event){
  var decision=Commands.literal("wolf_decision").then(Commands.argument("encounter",UuidArgument.uuid()).then(Commands.argument("token",UuidArgument.uuid()).then(Commands.argument("action",StringArgumentType.word()).executes(c->{var p=c.getSource().getPlayerOrException();var id=UuidArgument.getUuid(c,"encounter");var token=UuidArgument.getUuid(c,"token");var action=StringArgumentType.getString(c,"action");if(!Set.of("accept","decline").contains(action))return 0;var wolf=p.serverLevel().getEntitiesOfClass(GreyWolf.class,p.getBoundingBox().inflate(16),w->w.encounterId().equals(id)).stream().findFirst().orElse(null);if(wolf==null){GreyWolf.say(p,"invalid");return 0;}return wolf.decision(p,token,action.equals("accept"))?1:0;}))));
  var debug=Commands.literal("grey_wolf").executes(c->spawn(c.getSource().getPlayerOrException()));debug.then(Commands.literal("spawn").executes(c->spawn(c.getSource().getPlayerOrException())));
  for(String action:List.of("start","reset","hp60","bite","claw","pounce","dash","howl"))debug.then(Commands.literal(action).executes(c->{var p=c.getSource().getPlayerOrException();var wolf=nearest(p);if(wolf==null)return 0;return switch(action){case "start"->{wolf.greet(p);yield wolf.start(p)?1:0;}case "reset"->{wolf.abort();wolf.discard();yield 1;}case "hp60"->{wolf.debugHealth(60);yield 1;}default->wolf.debugMove(switch(action){case "bite"->WolfTimeline.Move.BITE;case "claw"->WolfTimeline.Move.DOUBLE_CLAW;case "pounce"->WolfTimeline.Move.POUNCE;case "dash"->WolfTimeline.Move.FLANK_DASH;default->WolfTimeline.Move.HOWL_SUMMON;})?1:0;};}));
  event.getDispatcher().register(Commands.literal("slavicmyths").then(decision).then(Commands.literal("test").requires(s->s.hasPermission(2)).then(debug)));
  if(event.getDispatcher().getRoot().getChild("sm")==null)event.getDispatcher().register(Commands.literal("sm").redirect(event.getDispatcher().getRoot().getChild("slavicmyths")));
 }
 private static GreyWolf nearest(ServerPlayer p){return p.serverLevel().getEntitiesOfClass(GreyWolf.class,p.getBoundingBox().inflate(32),GreyWolf::debug).stream().min(Comparator.comparingDouble(w->w.distanceToSqr(p))).orElse(null);}
 private static int spawn(ServerPlayer p){if(p.level().dimension()!=Level.OVERWORLD)return 0;var wolf=org.slavicmyths.registry.ModEntities.GREY_WOLF.get().create(p.serverLevel());if(wolf==null)return 0;var at=p.position().add(p.getLookAngle().multiply(1,0,1).normalize().scale(6));wolf.moveTo(at.x,at.y,at.z,p.getYRot()+180,0);wolf.debug(true);if(!p.serverLevel().noCollision(wolf))return 0;return p.serverLevel().addFreshEntity(wolf)?1:0;}
 private WolfCommands(){}
}
