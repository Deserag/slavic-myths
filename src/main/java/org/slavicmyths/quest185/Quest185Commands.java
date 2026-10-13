package org.slavicmyths.quest185;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class Quest185Commands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){var root=Commands.literal("quest185").requires(s->s.hasPermission(2));
  for(String action:List.of("status","reset","invite","debug_actors","wolf_call","cleansing_sample","cleansing_status")){final String selected=action;root.then(Commands.literal(action).then(Commands.argument("player",EntityArgument.player()).executes(c->run(c.getSource(),EntityArgument.getPlayer(c,"player"),selected))));}
  root.then(Commands.literal("stage").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("id",StringArgumentType.word()).then(Commands.argument("state",StringArgumentType.word()).executes(c->{var p=EntityArgument.getPlayer(c,"player");int i=Arrays.asList(Quest185State.IDS).indexOf(StringArgumentType.getString(c,"id"));String state=StringArgumentType.getString(c,"state");if(!List.of("active","ready","completed").contains(state))return 0;var n=Quest185Story.data(p);String id=StringArgumentType.getString(c,"id");if(id.equals("likho_eye_researched")){n.putBoolean("Likho",true);if(state.equals("completed")){n.putBoolean("LikhoResearchRewardClaimed",true);n.putBoolean("Cleansing",true);}return 1;}if(id.equals("restore_earth_20")){n.putBoolean("Cleansing",true);CleansingLessons.get(p.serverLevel()).debugCount(p.getUUID(),state.equals("active")?0:20);n.putBoolean("EarthRestorationRewardClaimed",state.equals("completed"));return 1;}if(i<0)return 0;n.putBoolean("IntroDone",true);var q=Quest185State.quest(n,i);q.putBoolean("Accepted",true);q.putBoolean("Claimed",state.equals("completed"));if(state.equals("ready")){switch(i){case 0->n.putInt("MorKills",3);case 2->{n.putBoolean("Likho",true);n.putBoolean("Ovinnik",true);n.putBoolean("TrophiesVerified",true);}case 3->n.putBoolean("Horn",true);case 4->n.putBoolean("WarningRead",true);case 5->n.putBoolean("Poloz",true);case 6->{n.putBoolean("TraceMob",true);n.putBoolean("TraceBlock",true);}case 7->n.putInt("BasicKills",q.getInt("StartKills")+4);case 8->q.putInt("TrainingKills",3);default->{}}}return 1;})))));
  e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2)).then(root)));
 }
 private static int run(CommandSourceStack source,ServerPlayer p,String action){switch(action){case "status"->source.sendSuccess(()->Component.literal(Quest185Story.snapshot(p).toString()),false);case "reset"->{Quest185Sessions.close(p);CleansingLessons.get(p.serverLevel()).reset(p);org.slavicmyths.rpg.PathData.data(p).remove("Quest185");}case "invite"->{Quest185Story.data(p).putInt("MorKills",Math.max(1,Quest185Story.data(p).getInt("MorKills")));Quest185Story.invite(p);}case "debug_actors"->source.sendSuccess(()->Component.literal("Yaga="+Quest185Story.yagaTrust(p)+", Bayun met="+Quest185Story.metBayun(p)+" trust="+Quest185Story.bayunTrust(p)+", Wolf="+Quest185Story.wolfTrust(p)),false);case "wolf_call"->{return WolfSupport.summon(p)?1:0;}case "cleansing_status"->source.sendSuccess(()->Component.literal("cleaned="+CleansingLessons.get(p.serverLevel()).count(p.getUUID())+", tasks="+CleansingTasks.get(p.serverLevel()).activeTasks()+", immunityAtFeet="+CleansedLand.remaining(p.serverLevel(),p.blockPosition().below())),false);case "cleansing_sample"->{var s=new net.minecraft.world.item.ItemStack(Quest185Content.POWDER.get(),12);if(!p.getInventory().add(s))return 0;}}
  return 1;
 }
 private Quest185Commands(){}
}
