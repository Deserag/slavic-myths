package org.slavicmyths.kurgan;

import java.util.*;
import static org.slavicmyths.kurgan.KurganFighter.Kind;
import static org.slavicmyths.kurgan.KurganPlan.Archetype;

/** Seeded instance-wide quotas, independent of disturbance and natural monster spawning. */
public final class KurganRoster {
    public static Map<Integer,List<Kind>> assign(KurganPlan plan) {
        Map<Integer,List<Kind>> result=new LinkedHashMap<>();
        for(var room:plan.rooms)result.put(room.id,new ArrayList<>());
        Set<Integer> dedicated=new HashSet<>();
        if(plan.tier>0){
            var martial=plan.rooms.stream().filter(r->r.archetype==Archetype.WARRIOR).findFirst()
                .orElseGet(()->plan.rooms.stream().filter(r->r.role==KurganPlan.Role.LARGE_BURIAL&&!r.hall()).findFirst().orElseThrow());
            result.get(martial.id).add(Kind.VOEVODA);dedicated.add(martial.id);
            if(plan.tier==2){
                var ritual=plan.rooms.stream().filter(r->r.archetype==Archetype.RITUAL).findFirst()
                    .orElseGet(()->plan.rooms.stream().filter(r->r.role==KurganPlan.Role.OFFERING&&r.id!=martial.id).findFirst().orElseThrow());
                result.get(ritual.id).add(Kind.VOLKHV);dedicated.add(ritual.id);
                result.get(plan.finalRoom).add(Kind.PRINCE);dedicated.add(plan.finalRoom);
            }
        }
        Random random=new Random(plan.seed^0x431B8E22L);
        int[] remaining={new int[]{2,3,5}[plan.tier]+random.nextInt(plan.tier==2?4:3),
            plan.tier==0?(random.nextInt(100)<30?1:0):new int[]{0,2,3}[plan.tier]+random.nextInt(plan.tier==2?3:2),
            plan.tier==0?(random.nextInt(4)==0?1:0):new int[]{0,2,4}[plan.tier]+random.nextInt(3)};
        for(var room:plan.rooms){
            if(dedicated.contains(room.id))continue;
            int guards=room.id==plan.finalRoom?1:room.archetype==Archetype.TREASURY?(plan.tier==2?2:1):room.archetype==Archetype.RELIQUARY?1:0;
            for(int n=0;n<guards;n++)for(int k:new int[]{2,0,1})if(remaining[k]>0&&allowed(room,Kind.values()[k],plan.tier)){
                result.get(room.id).add(Kind.values()[k]);remaining[k]--;break;
            }
        }
        List<KurganPlan.Room> rooms=new ArrayList<>(plan.rooms);Collections.shuffle(rooms,random);
        for(int k:new int[]{2,1,0})while(remaining[k]>0){
            Kind kind=Kind.values()[k];KurganPlan.Room chosen=null;
            for(var room:rooms)if(!dedicated.contains(room.id)&&allowed(room,kind,plan.tier)&&result.get(room.id).size()<capacity(room)){
                if(chosen==null||result.get(room.id).size()<result.get(chosen.id).size())chosen=room;
            }
            if(chosen==null)throw new IllegalStateException("No legal room for "+kind+" in tier "+plan.tier+" seed "+plan.seed);
            result.get(chosen.id).add(kind);remaining[k]--;
        }
        result.replaceAll((id,list)->List.copyOf(list));return Collections.unmodifiableMap(result);
    }
    private static int capacity(KurganPlan.Room r){
        if(r.archetype==null&&r.roomNode&&r.role!=KurganPlan.Role.ATMOSPHERIC&&r.role!=KurganPlan.Role.TRANSITION)return 3;
        if(r.archetype==null||r.archetype==Archetype.VESTIBULE||r.archetype==Archetype.CROSSROADS||r.archetype==Archetype.DESCENT||r.archetype==Archetype.OFFERING)return 1;
        return r.archetype==Archetype.RELIQUARY?2:3;
    }
    private static boolean allowed(KurganPlan.Room r,Kind k,int tier){
        if(r.hall()||r.archetype==Archetype.TRAP)return false;
        if(r.archetype==Archetype.VESTIBULE)return k==Kind.UPYR;
        if(r.archetype==null&&r.roomNode&&k==Kind.DRUZHINNIK)return r.role==KurganPlan.Role.LARGE_BURIAL||r.role==KurganPlan.Role.FINAL||tier>0&&r.role!=KurganPlan.Role.RUINED;
        if(k==Kind.DRUZHINNIK)return r.archetype==Archetype.BURIAL||r.archetype==Archetype.WARRIOR||r.archetype==Archetype.TREASURY||r.archetype==Archetype.DEEP||tier>0&&(r.archetype==null||r.archetype==Archetype.CROSSROADS||r.archetype==Archetype.DESCENT)||tier==2&&r.archetype==Archetype.RELIQUARY;
        if(r.archetype==Archetype.RITUAL||r.archetype==Archetype.FLOODED)return k==Kind.NAV;
        return true;
    }
    public static List<Kind> roster(KurganInstance i,KurganPlan.Room room){return i.population().getOrDefault(room.id,List.of());}
    private KurganRoster(){}
}
