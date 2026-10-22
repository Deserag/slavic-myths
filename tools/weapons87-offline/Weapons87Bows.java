package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.item.Item;
import org.slavicmyths.client.BowDrawClient;
import org.slavicmyths.combat.FieldBowItem;
import org.slavicmyths.finist.FinistBow;

/** Actual Item factories and native matrices, no entities/worlds/client/game. */
public final class Weapons87Bows {
    static void vanilla(PoseStack pose,boolean left,float equip,float ticks) {
        int sign=left?-1:1;
        pose.translate(sign*.56F,-.52F+equip*-.6F,-.72F);pose.translate(sign*-.2785682F,.18344387F,.15731531F);
        pose.mulPose(Axis.XP.rotationDegrees(-13.935F));pose.mulPose(Axis.YP.rotationDegrees(sign*35.3F));pose.mulPose(Axis.ZP.rotationDegrees(sign*-9.785F));
        float f=ticks/20F;f=(f*f+f*2F)/3F;f=Math.min(1,f);
        if(f>.1F)pose.translate(0,net.minecraft.util.Mth.sin((ticks-.1F)*1.3F)*(f-.1F)*.004F,0);
        pose.translate(0,0,f*.04F);pose.scale(1,1,1+f*.2F);pose.mulPose(Axis.YN.rotationDegrees(sign*45F));
    }
    public static void main(String[] args)throws Exception {
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
        net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
        var registry=(net.minecraft.core.MappedRegistry<Item>)net.minecraft.core.registries.BuiltInRegistries.ITEM;registry.unfreeze();
        var intrusive=net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");intrusive.setAccessible(true);intrusive.set(registry,new IdentityHashMap<>());
        var bows=new HashMap<String,Item>();
        for(String id:List.of("short_bow","heavy_bow","finist_bow")){var bow=Weapons87Native.factory(id);net.minecraft.core.Registry.register(registry,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id),bow);bows.put(id,bow);}
        var gson=new GsonBuilder().registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).create();
        var inputs=JsonParser.parseString(Files.readString(Path.of("docs/implementation/weapons87/bow-native-inputs.json"))).getAsJsonArray();var samples=new JsonArray();int comparisons=0;
        for(var input:inputs){var in=input.getAsJsonObject();String id=in.get("id").getAsString();boolean left=in.get("left").getAsBoolean();var bow=bows.get(id);int duration=bow instanceof FieldBowItem field?field.drawTicks():30;
            for(int ticks=0;ticks<=100;ticks++){float server=bow instanceof FieldBowItem field?field.drawPower(ticks):((FinistBow)bow).power(ticks);if(Math.abs(server-BowDrawClient.visualPower(ticks,duration))>1e-6)throw new AssertionError("Server visual power mismatch "+id+":"+ticks);comparisons++;}
            for(float equip:new float[]{0,.5F})for(float ticks:new float[]{0,1,3,6,9,12,19.5F,20,30,45}) {
                var reference=new PoseStack();vanilla(reference,left,equip,ticks);var check=new PoseStack();BowDrawClient.drawPose(check,left,equip,ticks,20);
                if(!reference.last().pose().equals(check.last().pose(),1e-6F))throw new AssertionError("Vanilla pose changed beyond timing");
                for(boolean after:new boolean[]{false,true}){var pose=new PoseStack();if(after)BowDrawClient.drawPose(pose,left,equip,ticks,duration);else vanilla(pose,left,equip,ticks);
                    gson.fromJson(in.get("display"),ItemTransform.class).apply(left,pose);var row=new JsonObject();row.addProperty("id",id);row.addProperty("left",left);row.addProperty("equip",equip);row.addProperty("ticks",ticks);row.addProperty("duration",duration);row.addProperty("after",after);row.addProperty("power",BowDrawClient.visualPower(ticks,after?duration:20));var matrix=new JsonArray();for(float f:pose.last().pose().get(new float[16])){if(!Float.isFinite(f))throw new AssertionError("Nonfinite matrix");matrix.add(f);}row.add("matrix",matrix);samples.add(row);
                }
            }
        }
        Files.writeString(Path.of("docs/implementation/weapons87/bow-native.json"),new GsonBuilder().setPrettyPrinting().create().toJson(samples));
        System.out.println("PASS: "+comparisons+" actual server-power comparisons;120 independent vanilla-pose comparisons;240 native before/after bow transforms, no world/entity/game.");
    }
}
