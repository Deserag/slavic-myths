package org.slavicmyths.nochnitsa;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class NochnitsaConfig {
    public static final ModConfigSpec SPEC;private static final ModConfigSpec.IntValue WEIGHT;private static final ModConfigSpec.BooleanValue DEBUG;
    public static int spawnWeight=6;public static boolean debug;
    static{var b=new ModConfigSpec.Builder();b.comment("Nochnitsa: fixed 50 HP and existing two-rune protection. Spawn weight changes require world reload.");WEIGHT=b.defineInRange("naturalSpawnWeight",6,0,100);DEBUG=b.define("enableDevDebug",false);SPEC=b.build();}
    @SubscribeEvent public static void loading(ModConfigEvent.Loading e){if(e.getConfig().getSpec()==SPEC){spawnWeight=WEIGHT.get();debug=DEBUG.get();}}
    @SubscribeEvent public static void reloading(ModConfigEvent.Reloading e){if(e.getConfig().getSpec()==SPEC){spawnWeight=WEIGHT.get();debug=DEBUG.get();}}
    private NochnitsaConfig(){}
}
