package org.slavicmyths.verify;

import java.util.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.item.ItemState.RuneState;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import io.netty.buffer.Unpooled;




/** Formula/component checks only; no entity, world, native attribute instance or game process. */
public final class RuneReworkOffline {
    static int checks;
    static void check(boolean v,String message){checks++;if(!v)throw new AssertionError(message);}
    static void near(double actual,double expected,String message){check(Math.abs(actual-expected)<1e-9,message+": "+actual);}
    public static void main(String[] args){
        near(RuneBalance.bonus(.06,1,1),.06,"Strength 1");near(RuneBalance.bonus(.06,2,1),.102,"Strength 2");near(RuneBalance.bonus(.06,3,1),.129,"Strength 3");
        near(RuneBalance.attackFactor(3,3),.934,"Speed + Crushing combine additively");near(RuneBalance.CRUSHING_DAMAGE*RuneBalance.effective(3),.387,"Crushing 3");near(RuneBalance.CRUSHING_SLOW*RuneBalance.penalty(3),.195,"Crushing penalty 3");
        for(int count=0;count<=12;count++){
            double sum=0,penalty=0;for(int n=1;n<=count;n++){sum+=RuneBalance.effectiveMultiplier(n);penalty+=RuneBalance.penaltyMultiplier(n);}near(RuneBalance.effective(count),sum,"Shared DR");near(RuneBalance.penalty(count),penalty,"Penalty DR");
            check(RuneBalance.bonus(RuneBalance.FIRE_REDUCTION,count,RuneBalance.FIRE_CAP)<=.4,"Fire cap");check(RuneBalance.toughness(count,count)<=4,"Toughness cap");check(RuneBalance.bonus(RuneBalance.FORT_KB,count,RuneBalance.KB_CAP)<=.2,"KB cap");
            check(RuneBalance.movement(count,count,count)>=-.15&&RuneBalance.movement(count,count,count)<=.35,"Movement caps");check(RuneBalance.attackFactor(0,count)>=.5,"Nonnegative swing multiplier");
            check(RuneBalance.bonus(RuneBalance.FIRE_CHANCE,count,RuneBalance.PROC_CAP)<=.5&&RuneBalance.bonus(RuneBalance.BLOOD_CHANCE,count,RuneBalance.PROC_CAP)<=.5,"Proc caps");
        }
        for(var d:RuneBalance.DEFINITIONS)for(int copies=1;copies<=3;copies++){
            var state=new RuneState(3,Collections.nCopies(copies,d.id()));
            check(RuneState.CODEC.parse(JsonOps.INSTANCE,RuneState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow()).getOrThrow().equals(state),"Duplicate JSON");
            check(RuneState.CODEC.parse(NbtOps.INSTANCE,RuneState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow()).getOrThrow().equals(state),"Duplicate NBT");
            var codec=ByteBufCodecs.fromCodec(RuneState.CODEC);var buf=Unpooled.buffer();try{codec.encode(buf,state);check(codec.decode(buf).equals(state),"Duplicate packet");}finally{buf.release();}
            check(Runes.installationReason(1,3,3,Collections.nCopies(copies,d.id()),d.id(),true).equals(copies==3?"socket_full":""),"Repeated installation");
            check(Runes.installationReason(1,3,3,List.of(),d.id(),false).equals("incompatible_rune"),"Compatibility rejection");
        }
        var old=new RuneState(3,List.of("thunder","wind","heat"));check(RuneState.CODEC.parse(JsonOps.INSTANCE,RuneState.CODEC.encodeStart(JsonOps.INSTANCE,old).getOrThrow()).getOrThrow().equals(old),"Old IDs unchanged");
        for(var provenance:List.of(List.<String>of(),List.of("heat"),List.of("wind"),List.of("heat","wind"))){var codec=RuneState.LEGACY_SPECIALS_CODEC;check(codec.parse(JsonOps.INSTANCE,codec.encodeStart(JsonOps.INSTANCE,provenance).getOrThrow()).getOrThrow().equals(provenance),"Provenance JSON");check(codec.parse(NbtOps.INSTANCE,codec.encodeStart(NbtOps.INSTANCE,provenance).getOrThrow()).getOrThrow().equals(provenance),"Provenance NBT");var stream=ByteBufCodecs.fromCodec(codec);var b=Unpooled.buffer();try{stream.encode(b,provenance);check(stream.decode(b).equals(provenance),"Provenance packet");}finally{b.release();}}
        check(RuneState.LEGACY_SPECIALS_CODEC.encodeStart(JsonOps.INSTANCE,List.of("life")).error().isPresent(),"Provenance rejects other IDs");
        check(RuneState.LEGACY_SPECIALS_CODEC.encodeStart(JsonOps.INSTANCE,List.of("heat","heat")).error().isPresent(),"Provenance rejects duplicate flags");
        check(Runes.installationReason(1,3,3,List.of("life"),"life",true).equals("duplicate_rune"),"Unique legacy still unique");
        var legacyDuplicate=new RuneState(3,List.of("life","life"));
        check(RuneState.CODEC.parse(NbtOps.INSTANCE,RuneState.CODEC.encodeStart(NbtOps.INSTANCE,legacyDuplicate).getOrThrow()).getOrThrow().equals(legacyDuplicate),"Historical duplicates preserved, admission remains strict");
        for(String id:AdvancedRuneRules.IDS){check(!RuneBalance.modern(id),"III/IV never enter duplicate-enabled I-II definitions");check(!Runes.installationReason(1,3,3,List.of(id),id,true).isEmpty(),"New duplicate denied");}
        var historicalIV=new RuneState(3,List.of("earth_lord_iv","veles_oath_iv"));check(RuneState.CODEC.parse(JsonOps.INSTANCE,RuneState.CODEC.encodeStart(JsonOps.INSTANCE,historicalIV).getOrThrow()).getOrThrow().equals(historicalIV),"Historical IV sequence preserved");
        check(Runes.installationReason(1,3,3,List.of("earth_lord_iv"),"veles_oath_iv",true).equals("iv_occupied"),"Second IV rejected");
        near(AdvancedRuneRules.mining(false,true,true),1.3,"Mining II + depths capped at thirty percent");near(AdvancedRuneRules.mining(true,false,true),1.3,"Mining I + depths");near(AdvancedRuneRules.mining(true,true,false),1.2,"I and II do not add");
        check(!AdvancedRuneRules.hunter(11.9*11.9)&&AdvancedRuneRules.hunter(144),"Hunter distance boundary");
        check(AdvancedRuneRules.lastStand(6.8,20,3,true)&&!AdvancedRuneRules.lastStand(7.2,20,3,true)&&!AdvancedRuneRules.lastStand(6,20,0,true)&&!AdvancedRuneRules.lastStand(6,20,3,false),"Last stand boundaries");
        for(int marks=0;marks<=8;marks++){near(AdvancedRuneRules.oath(marks,0),1+Math.min(marks,5)*.02,"Oath marks");check(AdvancedRuneRules.oath(marks,.59)*1.59<=1.600000001,"Shared sixty percent cap");}
        for(int nativeCount=0;nativeCount<=6;nativeCount++)for(int runeCount=0;runeCount<=6;runeCount++){check(RuneBalance.bleedStacks(nativeCount,runeCount)<=3,"Combined source cap");check(RuneBalance.bleedStacks(0,runeCount)<=2,"Rune source cap");}
        near(RuneBalance.BLEED_HP,.5,"User preserved bleed HP");check(RuneBalance.BLEED_INTERVAL==40&&RuneBalance.BLEED_DURATION==120,"Bleed cadence/duration");
        near(.7*RuneBalance.safeAttackFactor(.7,0,3),.5635,"Slow mace factor");
        near(.7*RuneBalance.safeAttackFactor(.7,3,3),.6538,"Speed compensates drawback");
        near(RuneBalance.safeAttackFactor(.05,0,3),1,"Below-floor native speed never worsened");
        near(RuneBalance.safeAttackFactor(.1,0,3),1,"Safe floor");
        near(RuneBalance.safeAttackFactor(.7,0,0),1,"Unequipped factor");
        check(RuneState.provenance(List.of("heat","wind","life"),null).equals(List.of("heat","wind")),"Old special effects inferred without rewriting saves");
        check(RuneState.provenance(List.of("wind"),List.of()).isEmpty(),"New installation is not legacy");
        check(RuneState.retainedProvenance(List.of("heat"),List.of("heat","wind")).equals(List.of("heat")),"Removing last wind removes legacy provenance");
        System.out.println("RUNE_REWORK_OFFLINE_CHECKS="+checks);
    }
}
