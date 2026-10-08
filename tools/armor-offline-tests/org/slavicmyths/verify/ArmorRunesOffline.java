package org.slavicmyths.verify;

import java.util.*;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.rpg.Runes;
import org.slavicmyths.item.ItemState.RuneState;
import net.minecraft.nbt.NbtOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.ByteBufCodecs;

/** Offline value/codec checks; no client/server/world is created. */
public final class ArmorRunesOffline {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  Map<String,Integer> capacities=Map.ofEntries(Map.entry("minecraft:leather",0),Map.entry("minecraft:chainmail",1),Map.entry("minecraft:gold",1),Map.entry("minecraft:iron",1),Map.entry("minecraft:diamond",2),Map.entry("minecraft:netherite",2),Map.entry("minecraft:turtle",1),Map.entry("slavicmyths:perunite",3),Map.entry("slavicmyths:gambeson",1),Map.entry("slavicmyths:plate",1),Map.entry("unknown:decorative",0));
  for(var entry:capacities.entrySet())check(Runes.armorCapacity(ResourceLocation.parse(entry.getKey()))==entry.getValue(),"Capacity "+entry.getKey());
  for(var entry:capacities.entrySet()){
   int cap=entry.getValue();var installed=new ArrayList<String>();
   for(int i=0;i<cap;i++){
    String id=Runes.IDS[i];check(Runes.installationReason(1,cap,cap,installed,id,true).isEmpty(),"Admit "+entry.getKey());installed.add(id);
    check(Runes.installationReason(1,cap,cap,installed,id,true).equals(org.slavicmyths.rpg.RuneBalance.modern(id)?(installed.size()>=cap?"socket_full":""):"duplicate_rune"),"Duplicate admission");
   }
   check(Runes.installationReason(1,cap,cap,installed,Runes.IDS[cap],true).equals(cap==0?"unsupported":"socket_full"),"Limit "+entry.getKey());
   if(cap>0){
    check(Runes.installationReason(1,cap,0,List.of(),"life",true).equals("socket_full"),"Unforged");
    check(Runes.installationReason(1,cap,cap,List.of(),"unknown",true).equals("need_rune"),"Unknown rune");
    check(Runes.installationReason(1,cap,cap,List.of(),"shadow",false).equals("incompatible_rune"),"Compatibility");
    check(Runes.installationReason(2,cap,cap,List.of(),"life",true).equals("unsupported"),"Stack count");
    check(Runes.installationReason(1,cap,3,installed,Runes.IDS[cap],true).equals("socket_full"),"Forged overflow");
   }
  }
  for(int slots=0;slots<=3;slots++)for(int occupied=0;occupied<=slots;occupied++){
   var ids=new ArrayList<>(Arrays.asList(Runes.IDS).subList(0,occupied));var state=new RuneState(slots,ids);ids.clear();
   check(state.runes().size()==occupied,"Immutable caller list");
   try{state.runes().add("wind");throw new AssertionError("Mutable list");}catch(UnsupportedOperationException expected){checks++;}
   check(RuneState.CODEC.parse(JsonOps.INSTANCE,RuneState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow()).getOrThrow().equals(state),"JSON roundtrip");
   check(RuneState.CODEC.parse(NbtOps.INSTANCE,RuneState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow()).getOrThrow().equals(state),"NBT roundtrip");
   var codec=ByteBufCodecs.fromCodec(RuneState.CODEC);var buf=Unpooled.buffer();try{codec.encode(buf,state);check(codec.decode(buf).equals(state),"Packet roundtrip");}finally{buf.release();}
  }
  for(String invalid:List.of("{\"slots\":4,\"runes\":[]}","{\"slots\":0,\"runes\":[\"life\"]}","{\"slots\":1,\"runes\":[\"life\",\"wind\"]}","{\"slots\":3,\"runes\":[\"life\",\"life\"]}"))check(RuneState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(invalid)).error().isPresent(),"Invalid component");
  System.out.println("ARMOR_OFFLINE_CHECKS="+checks);
 }
}
