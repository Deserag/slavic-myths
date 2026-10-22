package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;
import org.slavicmyths.client.TrophyHeadsClient;

/** Actual baked Steve/Alex meshes and native translateToHand. Fixed source poses, not gameplay. */
public final class Weapons87Player {
    public static void main(String[] args)throws Exception {
        var samples=new JsonArray();
        for(boolean slim:new boolean[]{false,true})for(String poseName:List.of("idle","walk_t10","sneak","swing_sample")) {
            var player=new PlayerModel<>(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,slim),64,64).bakeRoot(),slim);
            player.young=false;
            // Fixed technical source poses. They do not call setupAnim with a simulated entity.
            if(poseName.equals("walk_t10")){
                float phase=10*.6662F,amount=.7F;
                player.rightArm.xRot=(float)Math.cos(phase+Math.PI)*amount*.5F;
                player.leftArm.xRot=(float)Math.cos(phase)*amount*.5F;
                player.rightLeg.xRot=(float)Math.cos(phase)*1.4F*amount;
                player.leftLeg.xRot=(float)Math.cos(phase+Math.PI)*1.4F*amount;
            }
            player.rightArm.xRot-=Math.PI/10;player.leftArm.xRot-=Math.PI/10;
            if(poseName.equals("sneak")){
                player.body.xRot=.5F;player.rightArm.xRot+=.4F;player.leftArm.xRot+=.4F;
                player.rightArm.y=player.leftArm.y=5.2F;player.body.y=3.2F;player.head.y=4.2F;
                player.rightLeg.z=player.leftLeg.z=4;player.rightLeg.y=player.leftLeg.y=12.2F;
            }
            if(poseName.equals("swing_sample")){player.rightArm.xRot=-1.2F;player.leftArm.xRot=-1.2F;}
            player.hat.copyFrom(player.head);player.leftSleeve.copyFrom(player.leftArm);player.rightSleeve.copyFrom(player.rightArm);
            var capture=new TrophyHeadsClient.Capture();var stack=new PoseStack();
            for(var part:List.of(player.head,player.hat,player.body,player.leftArm,player.rightArm,player.leftLeg,player.rightLeg))part.render(stack,capture,0,0);
            var faces=new JsonArray();for(int i=0;i<capture.vertices.size();i+=4){var face=new JsonArray();for(int j=0;j<4;j++){var vertex=new JsonArray();for(int k=0;k<5;k++)vertex.add(capture.vertices.get(i+j)[k]*(k<3?16:1));face.add(vertex);}faces.add(face);}
            var hands=new JsonObject();
            for(var arm:HumanoidArm.values()){
                var p=new PoseStack();player.translateToHand(arm,p);p.mulPose(Axis.XP.rotationDegrees(-90));p.mulPose(Axis.YP.rotationDegrees(180));p.translate((arm==HumanoidArm.LEFT?-1F:1F)/16,.125,-.625);
                var data=new JsonArray();for(float f:p.last().pose().get(new float[16])){if(!Float.isFinite(f))throw new AssertionError("Nonfinite native hand matrix");data.add(f);}hands.add(arm.name(),data);
            }
            var row=new JsonObject();row.addProperty("body",slim?"Alex":"Steve");row.addProperty("pose",poseName);row.addProperty("texture","minecraft:textures/entity/player/"+(slim?"slim/alex":"wide/steve")+".png");row.add("faces",faces);row.add("hands",hands);samples.add(row);
        }
        Files.writeString(Path.of("docs/implementation/weapons87/player-native.json"),new GsonBuilder().create().toJson(samples));
        System.out.println("WEAPONS87_PLAYER: 8 actual baked ModelPart/UV samples + native hand matrices; no entities/world/game");
    }
}
