package org.slavicmyths.sanctuary;
import java.util.*;
/** Pure policy for the same sampled columns used by the native planner. */
public final class SanctuaryTerrain {
 public static OptionalInt floor(List<Integer> heights,boolean spring,int slope){if(heights.isEmpty())return OptionalInt.empty();var h=new ArrayList<>(heights);Collections.sort(h);if(h.getFirst()<63||h.getLast()>130||h.getLast()-h.getFirst()>slope)return OptionalInt.empty();int y=spring?h.getFirst()-18:h.get(h.size()/2);return spring&&(y<25||y>55)?OptionalInt.empty():OptionalInt.of(y);}
 private SanctuaryTerrain(){}
}
