package org.slavicmyths.earth;

import java.util.*;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.bus.api.SubscribeEvent;

/** Encounter balance, loaded before attribute registration. Changes require a restart.
 * HP are half-hearts; ticks are server ticks (20 TPS); raw damage is not HP after iron. */
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
public final class EarthConfig {
    public static final ModConfigSpec SPEC;
    private static final Map<String,ModConfigSpec.DoubleValue> D=new LinkedHashMap<>();
    private static final Map<String,ModConfigSpec.IntValue> I=new LinkedHashMap<>();
    private static final Map<EarthRules.Action,ModConfigSpec.IntValue> C=new EnumMap<>(EarthRules.Action.class);
    static {
        var b=new ModConfigSpec.Builder();b.comment("Restart required. HP = half-hearts; ticks = 20 TPS. Keep defaults for first gameplay QA. Raw damage does not bypass armour.");
        d(b,"maxHP",800,1,1024);d(b,"height",6.5,6,7);d(b,"servantHP",18,15,20);d(b,"speed",.18,.1,.3);
        d(b,"phase2Threshold",.70,.5,.9);d(b,"phase3Threshold",.30,.1,.49);
        d(b,"runeHP",2,0,20);d(b,"runeReduction",.025,0,.20);d(b,"runeKB",.15,0,.25);
        // These counts are a fixed contract of the supplied arena and atomic three-servant cast.
        i(b,"servantsPerWave",3,3,3);i(b,"servantCap",6,6,6);i(b,"pillarCount",5,5,5);
        i(b,"servantIntervalTicks",800,800,7200);i(b,"volleyCountMin",4,4,4);i(b,"volleyCountMax",6,6,6);
        i(b,"balanceRevision",0,0,1);i(b,"volleyShotTicks",8,6,16);i(b,"meleePressure",100,80,160);
        b.push("cooldowns");for(var a:EarthRules.Action.values())if(a.cooldown>0)C.put(a,b.defineInRange(a.name().toLowerCase(Locale.ROOT),a.cooldown,16,7200));b.pop();
        SPEC=b.build();
    }
    private static void d(ModConfigSpec.Builder b,String key,double value,double min,double max){D.put(key,b.defineInRange(key,value,min,max));}
    private static void i(ModConfigSpec.Builder b,String key,int value,int min,int max){I.put(key,b.defineInRange(key,value,min,max));}
    @SubscribeEvent public static void load(ModConfigEvent.Loading e){if(e.getConfig().getSpec()!=SPEC)return;
        // Upgrade only the old shipped defaults once. Other tuned values remain untouched.
        if(I.get("balanceRevision").get()==0){
            if(I.get("volleyShotTicks").get()==14)I.get("volleyShotTicks").set(8);
            Map<EarthRules.Action,Integer> previous=Map.of(EarthRules.Action.MELEE,50,EarthRules.Action.PILLAR,560,EarthRules.Action.VOLLEY,360,EarthRules.Action.COUNTER,400,EarthRules.Action.TREMOR,440);
            previous.forEach((a,old)->{if(C.get(a).get().equals(old))C.get(a).set(switch(a){case MELEE->30;case PILLAR->280;case VOLLEY->120;case COUNTER,TREMOR->180;default->throw new IllegalArgumentException(a.name());});});
            I.get("balanceRevision").set(1);
            SPEC.save();
        }
        EarthRules.HEALTH=D.get("maxHP").get();EarthRules.HEIGHT=D.get("height").get();EarthRules.SERVANT_HEALTH=D.get("servantHP").get();EarthRules.SPEED=D.get("speed").get();
        EarthRules.PHASE_II=D.get("phase2Threshold").get();EarthRules.PHASE_III=D.get("phase3Threshold").get();
        EarthRules.RUNE_HP=D.get("runeHP").get();EarthRules.RUNE_DAMAGE=D.get("runeReduction").get();EarthRules.RUNE_KB=D.get("runeKB").get();
        EarthRules.INTERVAL=I.get("servantIntervalTicks").get();EarthRules.SHOT_INTERVAL=I.get("volleyShotTicks").get();EarthRules.PRESSURE=I.get("meleePressure").get();
        C.forEach((a,v)->a.cooldown=v.get());
    }
    private EarthConfig(){}
}
