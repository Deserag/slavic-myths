package org.slavicmyths.blackcinder;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Engineering defaults; owner balance remains untested. Boss HP is canonical. */
public final class CinderConfig {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.IntValue CRAWLER_HP,SPIRIT_HP,PEKELNIK_HP,SPARK_HP;
 static {var b=new ModConfigSpec.Builder();b.comment("Black Cinder: adjustable engineering health defaults, not accepted gameplay balance.");CRAWLER_HP=b.defineInRange("coalCrawlerHealth",90,60,300);SPIRIT_HP=b.defineInRange("ashSpiritHealth",70,60,300);PEKELNIK_HP=b.defineInRange("pekelnikHealth",120,60,300);SPARK_HP=b.defineInRange("sparklingHealth",16,1,40);SPEC=b.build();}
 public static double health(CinderContent.Kind k){if(!SPEC.isLoaded())return switch(k){case COAL_CRAWLER->90;case ASH_SPIRIT->70;case PEKELNIK->120;case SPARKLING->16;case ZHARENITSA->750;};return switch(k){case COAL_CRAWLER->CRAWLER_HP.get();case ASH_SPIRIT->SPIRIT_HP.get();case PEKELNIK->PEKELNIK_HP.get();case SPARKLING->SPARK_HP.get();case ZHARENITSA->750;};}
 private CinderConfig(){}
}
