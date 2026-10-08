package org.slavicmyths.verify;

import java.util.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.item.ItemState.RuneState;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.ByteBufCodecs;
import io.netty.buffer.Unpooled;

/** Actual shared rules/installed component/wire codec, no Minecraft process or world bootstrap. */
public final class RareRuneOffline {
    static int checks;
    static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static void near(double n,double expected){check(Math.abs(n-expected)<1e-9,"numeric rule "+n);}
    public static void main(String[] ignored){
        check(RareRuneRules.IDS.size()==6&&new HashSet<>(RareRuneRules.IDS).size()==6,"six unique rare IDs");
        int[] cooldown={700,900,1200,400,0,800},duration={120,100,400,0,0,160};
        for(int i=0;i<6;i++){String id=RareRuneRules.IDS.get(i);check(RareRuneRules.cooldown(id)==cooldown[i],"cooldown "+id);check(RareRuneRules.duration(id)==duration[i],"duration "+id);
            var value=new RuneState(3,List.of(id,"strength","strength"));
            var json=RuneState.CODEC.encodeStart(JsonOps.INSTANCE,value).getOrThrow();check(RuneState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(value),"JSON rare + ordinary duplicates");
            var nbt=RuneState.CODEC.encodeStart(NbtOps.INSTANCE,value).getOrThrow();check(RuneState.CODEC.parse(NbtOps.INSTANCE,nbt).getOrThrow().equals(value),"NBT rare");
            var b=Unpooled.buffer();try{var codec=ByteBufCodecs.fromCodec(RuneState.CODEC);codec.encode(b,value);check(codec.decode(b).equals(value)&&!b.isReadable(),"packet rare");}finally{b.release();}
            try{new RuneState(2,List.of(id,id));throw new AssertionError("rare duplicate accepted");}catch(IllegalArgumentException expected){checks++;}
            check(Runes.installationReason(1,3,3,List.of(id),id,true).equals("duplicate_rune"),"server admission rejects duplicate");check(Runes.installationReason(1,3,3,List.of(),id,false).equals("incompatible_rune"),"wrong equipment");
        }
        for(int max=1;max<=100;max++)for(int quarter=0;quarter<=max*4;quarter++){double hp=quarter/4.0;int expected=hp<=0?0:hp<=max*.15?3:hp<=max*.30?2:hp<=max*.5?1:0;check(RareRuneRules.tier(hp,max)==expected,"health thresholds");}
        near(RareRuneRules.damage(3),.30);near(RareRuneRules.speed(3),.15);near(RareRuneRules.defenseMultiplier(3),1.25);near(RareRuneRules.defenseMultiplier(2),1/.88);near(RareRuneRules.defenseMultiplier(1),1/.95);near(RareRuneRules.defenseMultiplier(0),1);
        for(double hp:new double[]{0,6,6.01,12,12.01,20}){check(RareRuneRules.canPay(hp,6)==(hp>6),"sacrifice safety");check(RareRuneRules.canPay(hp,12)==(hp>12),"execute price safety");}
        check(!RareRuneRules.canPay(Double.NaN,6)&&!RareRuneRules.canPay(Double.POSITIVE_INFINITY,6),"finite prices");
        for(double victim:new double[]{0,.5,5,5.01,20})for(double owner:new double[]{0,12,12.01,20})for(boolean immune:new boolean[]{false,true})check(RareRuneRules.execute(victim,owner,immune)==(!immune&&victim>0&&victim<=5&&owner>12),"execute gate");
        int shields=3;for(int i=0;i<3;i++){shields=RareRuneRules.remainingShields(shields);check(shields==2-i,"three hit shield depletion");}check(RareRuneRules.remainingShields(0)==0,"no negative shields");
        List<Integer> strikes=new ArrayList<>();for(int age=1;age<=120;age++)if(RareRuneRules.echoCycle(age)==11&&strikes.size()<3)strikes.add(age);check(strikes.equals(List.of(24,52,80)),"first strike and 1.4 s intervals");
        for(String id:RareRuneRules.ACTIVE){long now=17000,until=now+RareRuneRules.cooldown(id);CompoundTag state=new CompoundTag();state.putLong("CD_"+id,until);var copy=state.copy();check(copy.getLong("CD_"+id)==until,"NBT cooldown copies");check(!RareRuneRules.ready(now,until)&&!RareRuneRules.ready(until-1,until)&&RareRuneRules.ready(until,until),"cooldown boundary");}
        for(int i=-8;i<9;i++)for(int count=0;count<=3;count++)check(RareRuneRules.selected(i,count)==(count==0?0:Math.floorMod(i,count)),"bounded selection");
        for(boolean cycle:new boolean[]{false,true}){var b=new net.minecraft.network.FriendlyByteBuf(Unpooled.buffer());try{var value=new RareRuneNetwork.Intent(cycle);RareRuneNetwork.Intent.CODEC.encode(b,value);check(b.readableBytes()==1,"bounded intent");check(RareRuneNetwork.Intent.CODEC.decode(b).equals(value),"intent roundtrip");}finally{b.release();}}
        System.out.println("RARE_RUNE_OFFLINE_CHECKS="+checks);
    }
}
