package org.slavicmyths.pesigolovtsy;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class DogheadConfig {
 public static final ModConfigSpec SPEC;private static final ModConfigSpec.IntValue WEIGHT,HUNT_GAP;private static final ModConfigSpec.DoubleValue LEADER,PREY;private static final ModConfigSpec.BooleanValue DEBUG;
 public static int weight=20,huntGap=2400;public static double leaderChance=.25,preyChance=.12;public static boolean debug;
 static{var b=new ModConfigSpec.Builder();b.comment("Dog-headed ordinary packs. Natural weight changes require world reload. Combat markers fixed by authored clips.");WEIGHT=b.defineInRange("naturalSpawnWeight",20,0,100);LEADER=b.defineInRange("leaderChance",.25,0,1);PREY=b.defineInRange("passivePreyChance",.12,0,1);HUNT_GAP=b.defineInRange("passiveHuntCooldownTicks",2400,400,24000);DEBUG=b.define("enableDevDebug",false);SPEC=b.build();}
 private static void bake(ModConfigEvent e){if(e.getConfig().getSpec()!=SPEC)return;weight=WEIGHT.get();leaderChance=LEADER.get();preyChance=PREY.get();huntGap=HUNT_GAP.get();debug=DEBUG.get();}
 @SubscribeEvent public static void loading(ModConfigEvent.Loading e){bake(e);}@SubscribeEvent public static void reloading(ModConfigEvent.Reloading e){bake(e);}
 private DogheadConfig(){}
}
