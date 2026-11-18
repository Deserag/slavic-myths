package org.slavicmyths.verify;
import java.nio.file.*;import java.util.*;
import com.google.gson.*;
import org.slavicmyths.combat.CreatureProperties;
public final class Nether328Offline {
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  Map<String,CreatureProperties.Nature> natures=new HashMap<>();Map<String,EnumSet<CreatureProperties.Trait>> traits=new HashMap<>();Path dir=Path.of("src/main/resources/data/slavicmyths/tags/entity_type");
  for(var nature:CreatureProperties.Nature.values()){Path file=dir.resolve("nature_"+nature.name().toLowerCase(Locale.ROOT)+".json");if(!Files.exists(file))continue;for(var v:JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonArray("values")){if(!v.isJsonPrimitive())continue;String id=v.getAsString();if(natures.putIfAbsent(id,nature)!=null)throw new AssertionError("multiple natures "+id);}}
  for(var trait:CreatureProperties.Trait.values()){Path file=dir.resolve("trait_"+trait.name().toLowerCase(Locale.ROOT)+".json");if(!Files.exists(file))continue;for(var v:JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonArray("values")){if(v.isJsonPrimitive())traits.computeIfAbsent(v.getAsString(),k->EnumSet.noneOf(CreatureProperties.Trait.class)).add(trait);}}
  var biological=Set.of("heat_salamander","slag_crab","gulokryl","ekhovik");int count=0;
  for(String line:Files.readAllLines(Path.of("docs/implementation/nether-creature-balance/NEW_NETHER_ENTITIES.tsv"))){if(!line.startsWith("Навья")&&!line.startsWith("Чёрная")&&!line.startsWith("Чертоги")&&!line.startsWith("Горючие"))continue;String id=line.split("\t",-1)[1];var n=natures.get(id);if(n==null)throw new AssertionError("unclassified "+id);var t=traits.getOrDefault(id,EnumSet.noneOf(CreatureProperties.Trait.class));boolean bio=biological.contains(id.split(":")[1]);if(CreatureProperties.canBleed(n,t,false,false)!=bio||CreatureProperties.isPhysiologicalPoisonTarget(n,t,false)!=bio)throw new AssertionError("physiology "+id);if(t.contains(CreatureProperties.Trait.FIRE_AFFINITY)&&t.contains(CreatureProperties.Trait.ICE_AFFINITY))throw new AssertionError("conflicting affinity");if(CreatureProperties.fireMultiplier(n,t,true)!=0)throw new AssertionError("native fire immunity");count++;}
  if(count!=20)throw new AssertionError("expected twenty "+count);
  System.out.println("PASS twenty source tag profiles through production bleed/poison/fire functions. Actual runtime tag binding, entity effects and MP NOT_RUN; HP/damage unchanged.");
 }
}
