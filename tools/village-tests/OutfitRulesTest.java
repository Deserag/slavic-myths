import java.util.*;
import org.slavicmyths.village.OutfitRules;

/** Exercises the actual production pure function, without Minecraft stubs. */
public class OutfitRulesTest {
    public static void main(String[] args) {
        int assertions=0;Set<Integer> variants=new HashSet<>();
        for(int i=0;i<10000;i++){
            UUID id=UUID.nameUUIDFromBytes(("villager-"+i).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            int variant=OutfitRules.variant(id);variants.add(variant);
            if(variant<0||variant>1||variant!=OutfitRules.variant(UUID.fromString(id.toString())))throw new AssertionError("Unstable or invalid outfit");assertions++;
        }
        if(variants.size()!=2)throw new AssertionError("No visual diversity");assertions++;
        for(String type:List.of("snow","snowy","taiga")){if(OutfitRules.climate(type)!=OutfitRules.Climate.COLD)throw new AssertionError(type);assertions++;}
        for(String type:List.of("desert","savanna","jungle")){if(OutfitRules.climate(type)!=OutfitRules.Climate.WARM)throw new AssertionError(type);assertions++;}
        for(String type:List.of("plains","swamp","unknown")){if(OutfitRules.climate(type)!=OutfitRules.Climate.TEMPERATE)throw new AssertionError(type);assertions++;}
        System.out.println("OUTFIT_RULES_PASS assertions="+assertions+" UUIDs=10000 variants=2 climateGroups=3");
    }
}
