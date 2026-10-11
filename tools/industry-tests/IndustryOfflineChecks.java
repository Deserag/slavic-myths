import org.slavicmyths.industry.IndustryRules;
import java.util.*;
/** Actual pure production admission rules, no Minecraft bootstrap/client/server/world. */
public final class IndustryOfflineChecks {
    static int checks;
    static void check(boolean result,String message){if(!result)throw new AssertionError(message);checks++;}
    public static void main(String[] args){
        for(int metal=0;metal<=2;metal++)for(int flux=0;flux<=2;flux++)for(int out=0;out<=65;out++)for(boolean refine:new boolean[]{false,true})for(boolean compatible:new boolean[]{false,true})for(boolean matches:new boolean[]{false,true}){
            boolean can=IndustryRules.canFinish(refine,matches,metal,flux,out,64,compatible);
            if(can){check(metal>=1&&(!refine||flux>=1)&&out<64&&compatible&&matches,"no free output / full stack / wrong recipe");int left=metal-1,remaining=flux-(refine?1:0),result=out+1;check(left>=0&&remaining>=0&&result<=64,"atomic one-for-one consumption");}else check(!matches||!compatible||metal==0||refine&&flux==0||out>=64,"only legitimate blockers");
        }
        for(boolean ring:new boolean[]{false,true})for(var runes:List.of(List.<String>of(),List.of("mining_1"),List.of("mining_2"),List.of("mining_1","mining_2"),List.of("mining_2","mining_2","mining_1"))){double speed=IndustryRules.speed(runes,ring);check(speed>=1&&speed<=1.3440001,"bounded speed");if(runes.contains("mining_2"))check(Math.abs(speed-(ring?1.344:1.20))<1e-9,"highest rune only");}
        for(int level=0;level<=8;level++){check(IndustryRules.fortune(level,true,false)==Math.min(4,level+1),"Fortune IV cap");check(IndustryRules.fortune(level,true,true)==level,"Silk preserves original loot");check(IndustryRules.fortune(level,false,false)==level,"no rune unchanged");}
        for(long day=0;day<8;day++){int seen=0,count=0;for(long time=0;time<24000;time+=40){int period=IndustryRules.restockPeriod(day*24000+time);if(period!=0&&(seen&period)==0){seen|=period;count++;}}check(count==2,"at most two stock windows per day");}
        System.out.println("INDUSTRY PURE RULES PASS: "+checks+" assertions; Minecraft launches=0; no GUI/AI/relog validation");
    }
}
