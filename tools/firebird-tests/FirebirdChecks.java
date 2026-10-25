package org.slavicmyths.firebird;

import net.minecraft.util.RandomSource;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

public final class FirebirdChecks {
    static int checks;static void require(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        require(!FirebirdRules.night(12999),"night start");require(FirebirdRules.night(13000),"night boundary");require(FirebirdRules.night(22999),"night end");require(!FirebirdRules.night(23000),"dawn");require(FirebirdRules.night(24000+14000),"day modulo");
        require(!FirebirdRules.window(12999)&&FirebirdRules.window(13000)&&FirebirdRules.window(22999)&&!FirebirdRules.window(23000),"early window");
        var r=RandomSource.create(161);int[] counts=new int[3];boolean[] gaps=new boolean[3];
        for(int i=0;i<100000;i++){counts[FirebirdRules.resource(r.nextInt(100))]++;long day=FirebirdRules.nextNight(100,r);require(day>=102&&day<=104,"schedule bounds");gaps[(int)day-102]=true;int first=FirebirdRules.delay(true,r),next=FirebirdRules.delay(false,r);require(first>=600&&first<=1200&&next>=1200&&next<=2000,"timers");}
        require(Math.abs(counts[0]-10000)<1000&&Math.abs(counts[1]-30000)<1500&&Math.abs(counts[2]-60000)<1500,"100k distribution");for(boolean b:gaps)require(b,"inclusive schedule outcomes");
        var d=new FirebirdEncounter();d.event=UUID.randomUUID();d.bird=UUID.randomUUID();d.planned=3;d.issued=3;d.remaining=1450;d.outcomes=new int[]{0,2,1};d.observed=50;d.processed=50;d.eligible=72;d.lastSeen=12345;
        var copy=FirebirdEncounter.load(d.save(new CompoundTag(),null),null);require(copy.event.equals(d.event)&&copy.bird.equals(d.bird)&&copy.issued==3&&copy.planned==3&&copy.remaining==1450&&copy.outcomes[2]==1,"checkpoint budget and identities");
        var malformed=d.save(new CompoundTag(),null);malformed.putInt("Planned",999);malformed.putInt("Issued",255);malformed.putInt("Remaining",-99);malformed.putIntArray("Outcomes",new int[]{999});copy=FirebirdEncounter.load(malformed,null);require(copy.planned==3&&copy.issued==7&&copy.remaining==0&&copy.outcomes[0]==-1,"malformed bounds");
        System.out.println("Firebird offline PASS: "+checks+" assertions; distribution="+java.util.Arrays.toString(counts)+". No Minecraft launch, no runtime claim.");
    }
}
