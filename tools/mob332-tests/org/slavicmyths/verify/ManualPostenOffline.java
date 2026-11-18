package org.slavicmyths.verify;
import java.util.*;import java.io.*;
import net.minecraft.core.RegistryAccess;import net.minecraft.nbt.*;
import org.slavicmyths.losthalls.PostenEncounters;
/** Production journal arithmetic and binary NBT, no Minecraft world/loader/server. */
public final class ManualPostenOffline {
 static void require(boolean b){if(!b)throw new AssertionError("Manual Posten isolation");}
 static CompoundTag roundTrip(PostenEncounters d)throws Exception{var out=new ByteArrayOutputStream();NbtIo.write(d.save(new CompoundTag(),RegistryAccess.EMPTY),new DataOutputStream(out));return NbtIo.read(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));}
 public static void main(String[] args)throws Exception{
  UUID arena=UUID.randomUUID(),body=UUID.randomUUID();var natural=new PostenEncounters();require(natural.register(arena,body));var original=roundTrip(natural);
  for(String dimension:List.of("overworld","the_nether","the_end")){
   var manual=PostenEncounters.createManual();require(manual.register(arena,body));require(!manual.register(arena,UUID.randomUUID()));
   require(manual.damageBody(arena,body,390).splitNow());require(manual.snapshot(arena).orElseThrow().copies().size()==4);
   manual=PostenEncounters.loadManual(roundTrip(manual),RegistryAccess.EMPTY);
   for(var copy:manual.snapshot(arena).orElseThrow().copies())manual.damageCopy(arena,copy.entity(),65);
   require(manual.snapshot(arena).orElseThrow().health()==0);require(!manual.claimRewards(arena,body));
   require(!PostenEncounters.loadManual(roundTrip(manual),RegistryAccess.EMPTY).claimRewards(arena,body));
   require(original.equals(roundTrip(natural)));
   var cancelled=PostenEncounters.createManual();require(cancelled.register(arena,body));cancelled.damageBody(arena,body,390);cancelled.cancelManual(arena,UUID.randomUUID());require(cancelled.snapshot(arena).orElseThrow().health()==260);cancelled.cancelManual(arena,body);require(cancelled.snapshot(arena).orElseThrow().health()==0);require(!cancelled.claimRewards(arena,body));
  }
  System.out.println("PASS production manual journal: independent contexts, 650/260/4x65, binary NBT, reward refusal, foreign UUID, cancellation; dimension entity runtime NOT_RUN");
 }
}
