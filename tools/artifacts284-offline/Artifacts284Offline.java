package org.slavicmyths.verify;

import org.slavicmyths.skim.HeroLease;
import org.slavicmyths.sanctuary.SanctuaryRules;
import org.slavicmyths.mor.MorRules;
import net.minecraft.nbt.CompoundTag;

/** Shared production policies, no loader/server/world/entity construction. */
public final class Artifacts284Offline {
    private static int checks;
    private static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
    public static void main(String[] args)throws Exception{
        var n=new CompoundTag();HeroLease.start(n,100);
        check(!HeroLease.expired(n,12099)&&HeroLease.expired(n,12100),"lease exact expiry");
        HeroLease.advance(n,150,true);check(n.getLong("ExpiresAt")==12150,"mandatory encounter pause");
        HeroLease.advance(n,200,false);check(n.getLong("ExpiresAt")==12150,"ordinary encounter must not extend lease");
        check(HeroLease.recovery(n,200)&&HeroLease.recovery(n,201)&&!HeroLease.recovery(n,202),"two recovery budget");
        HeroLease.end(n,203);long until=n.getLong("ReadyAt");HeroLease.end(n,300);check(until==36203&&n.getLong("ReadyAt")==until,"duplicate callbacks cannot extend cooldown");
        var copy=n.copy();check(!HeroLease.expired(copy,500)&&!HeroLease.recovery(copy,500),"inactive saved lease");
        check(SanctuaryRules.shortened(100)==65&&SanctuaryRules.shortened(1)==1,"water duration and lower boundary");
        check(SanctuaryRules.healTick(141)&&!SanctuaryRules.healTick(140)&&!SanctuaryRules.healTick(0),"spring tear heal cadence");
        check(MorRules.flaskRange(9)&&!MorRules.flaskRange(9.00001),"flask inclusive3 radius");
        check(!MorRules.canApply(1,0,true,true)&&MorRules.canApply(1,0,true,false),"protected target policy");
        String json="{\"checks\":"+checks+",\"game_launches\":0,\"scope\":\"actual HeroLease/SanctuaryRules/MorRules only; not gameplay\"}\n";
        java.nio.file.Files.writeString(java.nio.file.Path.of("docs/implementation/artifacts284-20261025/offline.json"),json);
        System.out.println("ARTIFACTS284_OFFLINE PASS checks="+checks+" game launches=0");
    }
}
