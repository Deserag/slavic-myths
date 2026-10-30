package org.slavicmyths.commands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** One contribution pass per resource reload. No static dispatcher or server state. */
@EventBusSubscriber(modid="slavicmyths")
public final class SlavicCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent event) {
  CommandTree tree=contributions();
  tree.install(event.getDispatcher());
 }
 public static CommandTree contributions() {
  var tree=new CommandTree();
  org.slavicmyths.bandit.CampCommands.register(tree);
  org.slavicmyths.bayun.BayunCommands.register(tree);
  org.slavicmyths.bogatyr.BogatyrCommands.register(tree);
  org.slavicmyths.chud.AbandonedCommands.register(tree);
  org.slavicmyths.chud.ChudCommands.register(tree);
  org.slavicmyths.chud.ChudGenerateCommands.register(tree);
  org.slavicmyths.chud.ChudSearchCommands.register(tree);
  org.slavicmyths.depths.DepthsCommands.register(tree);
  org.slavicmyths.earth.EarthCommands.register(tree);
  org.slavicmyths.finist.FinistEvents.commands(tree);
  org.slavicmyths.firebird.FirebirdCommands.register(tree);
  org.slavicmyths.gorodishche.CityCommands.register(tree);
  org.slavicmyths.hunt.BossCommands.register(tree);
  org.slavicmyths.hunt.HuntCommands.register(tree);
  org.slavicmyths.kalin.KalinEvents.commands(tree);
  org.slavicmyths.kurgan.KurganCommands.register(tree);
  org.slavicmyths.kuzya.KuzyaResidence.commands(tree);
  org.slavicmyths.marinka.MarinkaCommands.register(tree);
  org.slavicmyths.military.MilitaryCommands.register(tree);
  org.slavicmyths.mor.MorBlockCommands.register(tree);
  org.slavicmyths.navigation.NavigationCommands.register(tree);
  org.slavicmyths.poloz.PolozCommands.register(tree);
  org.slavicmyths.quest185.Quest185Commands.register(tree);
  org.slavicmyths.rpg.classes.ClassCommands.register(tree);
  org.slavicmyths.sanctuary.SanctuaryCommands.register(tree);
  org.slavicmyths.serpenthill.HillCommands.register(tree);
  org.slavicmyths.sivka.SivkaCommands.register(tree);
  org.slavicmyths.skim.SkimEvents.commands(tree);
  org.slavicmyths.swamp.SwampCommands.register(tree);
  org.slavicmyths.underground.UndergroundCommands.register(tree);
  org.slavicmyths.village.VillageCommands.register(tree);
  org.slavicmyths.village.VillageShowcase.register(tree);
  org.slavicmyths.water.RusalkaPreviewCommand.register(tree);
  org.slavicmyths.wolf.WolfCommands.register(tree);
  org.slavicmyths.wood.WoodlandCommands.register(tree);
  org.slavicmyths.navya.NavyaCommands.register(tree);
  org.slavicmyths.posad.PosadCommands.register(tree);
  org.slavicmyths.worldgen.ManualStructureCommands.register(tree);
  org.slavicmyths.yaga.YagaCommands.register(tree);
  return tree;
 }
 private SlavicCommands() {}
}
