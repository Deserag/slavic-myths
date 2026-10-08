package org.slavicmyths.verify;

import org.slavicmyths.rpg.RuneBase;
import org.slavicmyths.kurgan.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import io.netty.buffer.Unpooled;
import java.util.*;

/** Plain JVM only. Codec/planning checks do not claim a world, GUI or container run. */
public final class RuneFoundationOffline {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        for(int tier=-1;tier<=4;tier++)for(String material:List.of("iron","gold","coal","lapis","diamond","perunite","unknown","")) {
            boolean valid=tier==1&&(material.equals("iron")||material.equals("gold"))||tier==2&&(material.equals("diamond")||material.equals("perunite"))||tier==3&&material.equals("perunite");
            check(RuneBase.valid(tier,material)==valid,"Tier/material admission");
            var raw=JsonParser.parseString("{\"tier\":"+tier+",\"material\":\""+material+"\"}");
            check(RuneBase.CODEC.parse(JsonOps.INSTANCE,raw).result().isPresent()==valid,"Codec validation");
            if(!valid){try{new RuneBase(tier,material);throw new AssertionError("Invalid constructor");}catch(IllegalArgumentException expected){checks++;}continue;}
            RuneBase base=new RuneBase(tier,material);
            check(RuneBase.CODEC.parse(JsonOps.INSTANCE,RuneBase.CODEC.encodeStart(JsonOps.INSTANCE,base).getOrThrow()).getOrThrow().equals(base),"JSON persistence");
            check(RuneBase.CODEC.parse(NbtOps.INSTANCE,RuneBase.CODEC.encodeStart(NbtOps.INSTANCE,base).getOrThrow()).getOrThrow().equals(base),"NBT persistence");
            var codec=ByteBufCodecs.fromCodec(RuneBase.CODEC);var buffer=Unpooled.buffer();try{codec.encode(buffer,base);check(codec.decode(buffer).equals(base),"Packet synchronization");}finally{buffer.release();}
        }
        int plans=0;int[] min={99,99,99},max={0,0,0};
        for(int tier=0;tier<3;tier++)for(int seed=0;seed<1000;seed++) {
            var plan=KurganPlan.create(tier,seed*0x9E3779B97F4A7C15L);var old=KurganRoster.assign(plan,false);var current=KurganRoster.assign(plan,true);
            int before=old.values().stream().mapToInt(r->(int)r.stream().filter(k->k==KurganFighter.Kind.UPYR).count()).sum();
            int after=current.values().stream().mapToInt(r->(int)r.stream().filter(k->k==KurganFighter.Kind.UPYR).count()).sum();
            check(after==before+1,"One extra finite upyr");check(current.equals(KurganRoster.assign(plan,true)),"Deterministic roster");
            for(var room:plan.rooms)check(!room.hall()||current.get(room.id).isEmpty()||current.get(room.id).equals(old.get(room.id)),"No new hallway spawns");
            check(old.values().stream().flatMap(Collection::stream).filter(KurganFighter.Kind::boss).count()==current.values().stream().flatMap(Collection::stream).filter(KurganFighter.Kind::boss).count(),"Boss roster unchanged");
            min[tier]=Math.min(min[tier],after);max[tier]=Math.max(max[tier],after);plans++;
        }
        System.out.println("RUNE_OFFLINE_CHECKS="+checks+" PLANS="+plans+" UPYR_MIN="+Arrays.toString(min)+" UPYR_MAX="+Arrays.toString(max));
    }
}
