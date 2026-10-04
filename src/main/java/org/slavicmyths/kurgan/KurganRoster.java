package org.slavicmyths.kurgan;
import java.util.*;
import static org.slavicmyths.kurgan.KurganFighter.*;
/** Pure, deterministic room population policy; no registry/world bootstrap. */
public final class KurganRoster {
    /** Stable per-room distribution, decided before entities are spawned. */
    public static List<Kind> roster(KurganInstance i,KurganPlan.Room room){List<Kind> out=new ArrayList<>();Random random=new Random(i.plan.seed+room.id*104729L);
        if(room.hall()){if(i.plan.tier==2&&i.disturbance>=75)out.add(Kind.PRINCE);return out;}
        if(i.plan.tier>0){
            int warrior=-1,ritual=-1;for(KurganPlan.Room r:i.plan.rooms){if(warrior<0&&r.role==KurganPlan.Role.LARGE_BURIAL)warrior=r.id;if(ritual<0&&r.role==KurganPlan.Role.OFFERING)ritual=r.id;}
            boolean voevoda=room.id==warrior&&(i.plan.tier==2||random.nextInt(3)==0);
            boolean volkhv=room.id==ritual&&i.plan.tier==2;
            if(voevoda||volkhv){if(i.disturbance>=75)out.add(voevoda?Kind.VOEVODA:Kind.VOLKHV);return out;}
        }
        if(room.role==KurganPlan.Role.TRANSITION||room.role==KurganPlan.Role.ATMOSPHERIC||room.role==KurganPlan.Role.BLOCKED_SIDE)return out;
        if(i.disturbance<25&&random.nextInt(4)!=0)return out;
        int count=i.plan.tier==2?2+random.nextInt(2):1+random.nextInt(2);
        for(int n=0;n<count;n++){int roll=random.nextInt(100);Kind k=i.disturbance>=50&&roll<35?Kind.DRUZHINNIK:i.disturbance>=25&&roll<65?Kind.NAV:Kind.UPYR;if(i.plan.tier==0&&k==Kind.DRUZHINNIK&&roll>8)k=Kind.UPYR;out.add(k);}return out;
    }
}
