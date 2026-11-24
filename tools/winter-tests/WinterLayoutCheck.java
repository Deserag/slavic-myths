import java.util.EnumSet;
import static org.slavicmyths.winter.WinterEncounterLayout.*;
public final class WinterLayoutCheck {
    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        require(cell(-1)==-1 && cell(-512)==-1 && cell(-513)==-2, "negative region arithmetic");
        require(CELL-2*SEARCH==384, "minimum spacing");
        require(key(-1,2)!=key(2,-1), "coordinate keys");
        require(select(Habitat.FOREST,0,0,true,false,0)==Boss.QUEEN,"queen");
        require(select(Habitat.OCEAN,1,0,true,false,0)==Boss.CAPTAIN,"captain");
        require(select(Habitat.FOREST,2,0,true,false,0)==Boss.AUKA,"Auka");
        require(select(Habitat.LAND,3,0,true,false,0)==Boss.WOLVERINE,"wolverine");
        require(select(Habitat.LAND,0,-1,true,false,0)==Boss.OWL,"owl");
        require(select(Habitat.FOREST,1,-1,true,true,0)==Boss.GLUHEYA,"reserved Gluheya forest slot");
        EnumSet<Boss> found=EnumSet.noneOf(Boss.class);
        for(int x=-30;x<=30;x++)for(int z=-30;z<=30;z++){
            require(select(Habitat.OTHER,x,z,false,true,7)==null,"warm biome excluded");
            Boss ocean=select(Habitat.OCEAN,x,z,false,true,7);
            require(ocean==Boss.CAPTAIN||ocean==Boss.OWL,"ocean habitat");
            Boss land=select(Habitat.LAND,x,z,false,true,7);
            require(land==Boss.WOLVERINE||land==Boss.OWL,"land habitat");
            Boss forest=select(Habitat.FOREST,x,z,false,true,7);
            require(forest!=Boss.CAPTAIN,"forest habitat");
            require(forest==select(Habitat.FOREST,x,z,false,true,7),"deterministic");
            found.add(forest);
        }
        require(found.containsAll(EnumSet.of(Boss.QUEEN,Boss.AUKA,Boss.GLUHEYA,Boss.OWL,Boss.WOLVERINE)),"forest distribution");
        System.out.println("PASS winter layout: spacing, habitat, deterministic selection, negative coordinates");
    }
}
