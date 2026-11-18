package org.slavicmyths.verify;
import org.slavicmyths.navya.NavyaProfile;
/** Pure profile contract; never creates a Minecraft world or server. */
public final class NavyaProfileCheck {
    public static void main(String[] args) {
        for(long seed:new long[]{0,1,42,2432026,-912734}) {
            int low=999,high=-999,maxStep=0,columns=0,joining=0,lowerJoining=0;
            for(int x=-512;x<512;x++)for(int z=-512;z<512;z+=4) {
                var c=NavyaProfile.at(seed,x,z);var next=NavyaProfile.at(seed,x+1,z);
                if(c.lower()<33 || c.upper()>120 || c.middle()<c.lower()+1 || c.upper()<c.middle()+3)throw new AssertionError(c);
                if(!c.equals(NavyaProfile.at(seed,x,z)))throw new AssertionError("Nondeterminism");
                if(!NavyaProfile.solid(c,c.lower()) || !NavyaProfile.solid(c,c.middle()) || !NavyaProfile.solid(c,c.upper()) || NavyaProfile.solid(c,c.upper()+1))throw new AssertionError("Floor contract");
                maxStep=Math.max(maxStep,Math.abs(c.upper()-next.upper()));low=Math.min(low,c.lower());high=Math.max(high,c.upper());columns++;
                if(c.upper()-c.middle()<=7)joining++;
                if(c.middle()-c.lower()<=4)lowerJoining++;
            }
            if(lowerJoining==0 || joining==0)throw new AssertionError("No tier junctions in sampled profile");
            System.out.printf("seed=%d columns=%d minLower=%d maxUpper=%d maxAdjacentUpperStep=%d upperMiddleJoining=%d middleLowerJoining=%d%n",seed,columns,low,high,maxStep,joining,lowerJoining);
        }
        System.out.println("PASS pure deterministic profile bounds/floors; actual cave traversal, surface, structures and MSPT NOT_RUN.");
    }
}
