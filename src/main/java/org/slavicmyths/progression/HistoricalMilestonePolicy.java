package org.slavicmyths.progression;

import java.util.*;

/** INACTIVE import planner; no Minecraft types, file writes, awards or login hook.
 * Input must come from a future read-only pre-reset loader with provenance.
 * Runtime integration deliberately waits for real load/save/toast/revoke tests.
 */
public final class HistoricalMilestonePolicy {
    public record Gorynych(boolean purified,int cleanMask,boolean personalClaim,boolean creativeClaim,boolean standalone){}
    public record Win(boolean victory,boolean personalParticipation,int count){}
    public record ClassIv(boolean knownThird,boolean matchingSecond,int level,boolean completedTrial){}
    public record Curse(boolean returnedInThisEncounter,boolean skullReturnedOrReady){}
    public record Facts(Gorynych gorynych,Map<String,Win> nativeWins,ClassIv classIv,Curse curse){
        public Facts {nativeWins=nativeWins==null?Map.of():Map.copyOf(nativeWins);}
    }
    public static Set<String> candidates(Facts facts){
        if(facts==null)return Set.of();
        var result=new LinkedHashSet<String>();
        var g=facts.gorynych();
        if(g!=null&&g.purified()&&g.cleanMask()==7&&g.personalClaim()&&!g.creativeClaim()&&!g.standalone())result.add("defeat_gorynych");
        for(String id:List.of("defeat_idolishche","defeat_marinka","defeat_skim","defeat_kartaus","defeat_kalin")){
            var win=facts.nativeWins().get(id);
            if(win!=null&&win.victory()&&win.personalParticipation()&&win.count()>0)result.add(id);
        }
        var c=facts.classIv();
        if(c!=null&&c.knownThird()&&c.matchingSecond()&&c.level()>=40&&c.completedTrial())result.add("class_iv");
        var curse=facts.curse();
        if(curse!=null&&curse.returnedInThisEncounter()&&curse.skullReturnedOrReady())result.add("curse_resolved");
        return Set.copyOf(result);
    }
    private HistoricalMilestonePolicy(){}
}
