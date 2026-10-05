package org.slavicmyths.verify;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import org.slavicmyths.item.ItemState.RuneState;
import org.slavicmyths.hunt.HuntTarget;

/** Component values/codecs, without loading item registries or a game. */
public final class PortComponentsHeadless {
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args){
  var ids=new java.util.ArrayList<>(List.of("thunder","life"));var state=new RuneState(3,ids);ids.clear();
  check(state.runes().size()==2,"Component retained mutable caller list");
  try{state.runes().add("wind");throw new AssertionError("Mutable component");}catch(UnsupportedOperationException expected){}
  var json=RuneState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
  check(RuneState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(state),"Rune JSON roundtrip");
  for(String invalid:List.of("{\"slots\":4,\"runes\":[]}","{\"slots\":1,\"runes\":[\"life\",\"wind\"]}","{\"slots\":3,\"runes\":[\"life\",\"life\"]}"))
   check(RuneState.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(invalid)).error().isPresent(),"Malformed component accepted");
  var codec=ByteBufCodecs.fromCodec(RuneState.CODEC);var buffer=Unpooled.buffer();
  try{codec.encode(buffer,state);check(codec.decode(buffer).equals(state),"Rune packet roundtrip");}finally{buffer.release();}
  for(var target:HuntTarget.values())check(HuntTarget.CODEC.parse(JsonOps.INSTANCE,HuntTarget.CODEC.encodeStart(JsonOps.INSTANCE,target).getOrThrow()).getOrThrow()==target,"Target codec changed ID");
  check(HuntTarget.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("\"unknown\"")).error().isPresent(),"Unknown target accepted");
  System.out.println("PASS: immutable rune values, JSON/packet codec, invalid data rejection and every horn target ID");
 }
}
