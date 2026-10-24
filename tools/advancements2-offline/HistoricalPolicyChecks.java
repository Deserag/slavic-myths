import java.util.*;
import org.slavicmyths.progression.HistoricalMilestonePolicy;
import org.slavicmyths.progression.HistoricalMilestonePolicy.*;

/** Ordinary Java21 only; no Minecraft libraries, bootstrap, worlds or server. */
public final class HistoricalPolicyChecks {
    static int checks;
    static void check(boolean value){checks++;if(!value)throw new AssertionError("Policy case "+checks);}
    public static void main(String[] args){
        check(HistoricalMilestonePolicy.candidates(null).isEmpty());
        check(HistoricalMilestonePolicy.candidates(new Facts(null,null,null,null)).isEmpty());
        for(int bits=0;bits<16;bits++)for(int mask=0;mask<8;mask++){
            boolean purified=(bits&1)!=0,claim=(bits&2)!=0,creative=(bits&4)!=0,standalone=(bits&8)!=0;
            var g=new Gorynych(purified,mask,claim,creative,standalone);
            var result=HistoricalMilestonePolicy.candidates(new Facts(g,null,null,null));
            check(result.contains("defeat_gorynych")== (purified&&mask==7&&claim&&!creative&&!standalone));
        }
        for(String id:List.of("defeat_idolishche","defeat_marinka","defeat_skim","defeat_kartaus","defeat_kalin","depth_champions","kurgan_rest","night_watch","unknown"))
            for(boolean victory:List.of(false,true))for(boolean participant:List.of(false,true))for(int count:List.of(-1,0,1,20)){
                var input=Map.of(id,new Win(victory,participant,count));
                var result=HistoricalMilestonePolicy.candidates(new Facts(null,input,null,null));
                check(result.contains(id)==(id.startsWith("defeat_")&&victory&&participant&&count>0));
            }
        for(int bits=0;bits<8;bits++)for(int level:List.of(1,39,40,100)){
            var c=new ClassIv((bits&1)!=0,(bits&2)!=0,level,(bits&4)!=0);
            check(HistoricalMilestonePolicy.candidates(new Facts(null,null,c,null)).contains("class_iv")== (bits==7&&level>=40));
        }
        for(int bits=0;bits<4;bits++){
            var c=new Curse((bits&1)!=0,(bits&2)!=0);
            check(HistoricalMilestonePolicy.candidates(new Facts(null,null,null,c)).contains("curse_resolved")== (bits==3));
        }
        var mutable=new HashMap<String,Win>();mutable.put("defeat_skim",new Win(true,true,1));
        var snapshot=new Facts(null,mutable,null,null);mutable.clear();
        var wins=new HashMap<String,Win>();
        for(String id:List.of("defeat_idolishche","defeat_marinka","defeat_skim","defeat_kartaus","defeat_kalin"))wins.put(id,new Win(true,true,1));
        check(HistoricalMilestonePolicy.candidates(new Facts(new Gorynych(true,7,true,false,false),wins,new ClassIv(true,true,40,true),new Curse(true,true))).size()==8);
        check(HistoricalMilestonePolicy.candidates(snapshot).equals(Set.of("defeat_skim")));
        check(HistoricalMilestonePolicy.candidates(snapshot).equals(HistoricalMilestonePolicy.candidates(snapshot)));
        try{HistoricalMilestonePolicy.candidates(snapshot).add("night_watch");throw new AssertionError("Mutable result");}catch(UnsupportedOperationException expected){checks++;}
        System.out.println("OFFLINE_HISTORY_POLICY_PASS cases="+checks+" Minecraft classes=0; import activation NOT_RUN");
    }
}
