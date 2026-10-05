package org.slavicmyths.verify;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/** Actual target codecs, without pretending to initialize a mod registry. */
public final class PortDamageHeadless {
    public static void main(String[] args) throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        for(String id:new String[]{"slavic_path","slavic_retribution","fire_serpent","serpent_trail","ovinnik_trail","ovinnik_slam","ovinnik_ash"}){
            var json=JsonParser.parseString(Files.readString(Path.of(args[0],id+".json"),StandardCharsets.UTF_8));
            var type=DamageType.DIRECT_CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
            if(!type.msgId().equals(id)||Math.abs(type.exhaustion()-.1F)>.00001F)throw new AssertionError(id);
            var scaling=id.equals("ovinnik_ash")?DamageScaling.NEVER:DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
            if(type.scaling()!=scaling)throw new AssertionError(id+" scaling");
            var encoded=DamageType.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE,type).getOrThrow();
            if(!DamageType.DIRECT_CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow().equals(type))throw new AssertionError(id+" roundtrip");
        }
        System.out.println("PASS: seven existing custom damage messages, exhaustion/scaling and actual target codec roundtrips. Registry loading remains unverified.");
    }
}
