package org.slavicmyths.worldgen;
import net.minecraft.commands.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class ManualStructureCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent event){
  var generate=Commands.literal("generate").requires(s->s.hasPermission(2));
  var kurgan=Commands.literal("kurgan");var camps=Commands.literal("bandit_camp");
  String[] k={"small","warrior","great"},b={"small","fortified","large"};
  for(int i=0;i<3;i++){kurgan.then(size(k[i],"kurgan",i));camps.then(size(b[i],"bandit_camp",i));}camps.then(size("medium","bandit_camp",1));
  generate.then(kurgan).then(camps);
  event.getDispatcher().register(Commands.literal("slavicmyths").then(generate).then(Commands.literal("search").requires(x->x.hasPermission(2)).then(Commands.literal("status").executes(c->BoundedStructureSearch.status(c.getSource()))).then(Commands.literal("cancel").executes(c->BoundedStructureSearch.cancel(c.getSource())))).then(Commands.literal("generation").requires(s->s.hasPermission(2)).then(Commands.literal("status").executes(c->ManualStructureJobs.status(c.getSource()))).then(Commands.literal("cancel").executes(c->ManualStructureJobs.cancel(c.getSource())))));
 }
 private static LiteralArgumentBuilder<CommandSourceStack> size(String name,String family,int tier){return Commands.literal(name).executes(c->ManualStructureJobs.start(c.getSource(),family,tier,c.getSource().getLevel().random.nextLong())).then(Commands.argument("seed",LongArgumentType.longArg()).executes(c->ManualStructureJobs.start(c.getSource(),family,tier,LongArgumentType.getLong(c,"seed"))));}
 private ManualStructureCommands(){}
}
