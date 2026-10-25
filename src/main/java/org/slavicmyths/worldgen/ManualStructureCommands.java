package org.slavicmyths.worldgen;
import net.minecraft.commands.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
public final class ManualStructureCommands {
 public static void register(org.slavicmyths.commands.CommandTree tree){
  var ids=new java.util.ArrayList<String>();for(String id:org.slavicmyths.swamp.SwampStructures.IDS)if(!id.equals("swamp_remnants"))ids.add(id);ids.addAll(java.util.List.of("bandit_camp_small","bandit_camp_medium","bandit_camp_large","kurgan_small","kurgan_warrior","kurgan_great"));
  for(String id:ids)tree.add(Commands.literal("locate").requires(s->s.hasPermission(2)).then(Commands.literal("structure").then(Commands.literal("slavicmyths:"+id).executes(c->OnDemandStructures.locate(c.getSource(),id)))));

  var generate=Commands.literal("generate").requires(s->s.hasPermission(2));
  var kurgan=Commands.literal("kurgan");var camps=Commands.literal("bandit_camp");
  String[] k={"small","warrior","great"},b={"small","fortified","large"};
  for(int i=0;i<3;i++){kurgan.then(size(k[i],"kurgan",i));camps.then(size(b[i],"bandit_camp",i));}camps.then(size("medium","bandit_camp",1));
  generate.then(kurgan).then(camps);
  tree.add(Commands.literal("slavicmyths").then(generate).then(Commands.literal("search").requires(x->x.hasPermission(2)).then(Commands.literal("status").executes(c->BoundedStructureSearch.status(c.getSource()))).then(Commands.literal("cancel").executes(c->BoundedStructureSearch.cancel(c.getSource())))).then(Commands.literal("generation").requires(s->s.hasPermission(2)).then(Commands.literal("status").executes(c->ManualStructureJobs.status(c.getSource()))).then(Commands.literal("cancel").executes(c->ManualStructureJobs.cancel(c.getSource())))));
 }
 private static LiteralArgumentBuilder<CommandSourceStack> size(String name,String family,int tier){return Commands.literal(name).executes(c->ManualStructureJobs.start(c.getSource(),family,tier,c.getSource().getLevel().random.nextLong())).then(Commands.argument("seed",LongArgumentType.longArg()).executes(c->ManualStructureJobs.start(c.getSource(),family,tier,LongArgumentType.getLong(c,"seed"))));}
 private ManualStructureCommands(){}
}
