package org.slavicmyths.client;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.block.model.ItemTransform;

/** Real production pose helpers and native arm mesh/UV; no Minecraft instance or entities. */
public final class Weapons87FirstPerson {
    static final Gson GSON=new GsonBuilder().registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).create();
    static JsonArray matrix(PoseStack p){var out=new JsonArray();for(float f:p.last().pose().get(new float[16])){if(!Float.isFinite(f))throw new AssertionError("Nonfinite FP pose");out.add(f);}return out;}
    static JsonArray arm(boolean slim,boolean left,PoseStack wrist){
        var player=new PlayerModel<>(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,slim),64,64).bakeRoot(),slim);player.young=false;var arm=left?player.leftArm:player.rightArm;var sleeve=left?player.leftSleeve:player.rightSleeve;arm.xRot=arm.yRot=arm.zRot=sleeve.xRot=sleeve.yRot=sleeve.zRot=0;
        var p=new PoseStack();p.mulPose(wrist.last().pose());WeaponHands.gripArm(p,arm);var capture=new TrophyHeadsClient.Capture();arm.render(p,capture,0,0);sleeve.render(p,capture,0,0);var faces=new JsonArray();
        for(int i=0;i<capture.vertices.size();i+=4){var face=new JsonArray();for(int j=0;j<4;j++){var v=new JsonArray();for(float f:capture.vertices.get(i+j))v.add(f);face.add(v);}faces.add(face);}return faces;
    }
    static JsonObject row(String id,boolean left,String body,String frame,boolean after,PoseStack item,JsonArray arm,String model){var row=new JsonObject();row.addProperty("id",id);row.addProperty("left",left);row.addProperty("body",body);row.addProperty("frame",frame);row.addProperty("after",after);row.addProperty("model",model);row.add("matrix",matrix(item));row.add("arm_faces",arm);return row;}
    public static void main(String[] args)throws Exception{
        var inputs=JsonParser.parseString(Files.readString(Path.of("docs/implementation/weapons87/fp-inputs.json"))).getAsJsonArray();var samples=new JsonArray();
        for(var input:inputs){var in=input.getAsJsonObject();String id=in.get("id").getAsString(),custom=in.get("custom").getAsString();boolean left=in.get("left").getAsBoolean();int sign=left?-1:1;
            if(custom.equals("standard")){
                // Installed ItemInHandRenderer.applyItemArmTransform at idle/equip=0.
                for(boolean after:new boolean[]{false,true}){var p=new PoseStack();p.translate(sign*.56F,-.52F,-.72F);GSON.fromJson(in.getAsJsonObject(after?"after":"before"),ItemTransform.class).apply(left,p);String frame=in.has("slot")?in.get("slot").getAsString():"idle";String model=id.equals("karachun_scythe")?"karachun_scythe_held":id.equals("tugarin_sword")?"tugarin_sword_held":id;samples.add(row(id,left,"ITEM_ONLY",frame,after,p,new JsonArray(),model));}continue;
            }
            for(boolean slim:new boolean[]{false,true})for(boolean after:new boolean[]{false,true}){
                String body=slim?"Alex":"Steve";
                if(custom.equals("crossbow"))for(int state=0;state<6;state++){
                    float pull=state==1?0:state==2?.6F:state==3?1:0;var wrist=new PoseStack();WeaponHands.crossbowWrist(wrist,sign,0,pull);var item=new PoseStack();item.mulPose(wrist.last().pose());WeaponHands.crossbowModel(item);String model=id+(state==1?"_pulling_0":state==2?"_pulling_1":state==3?"_pulling_2":state==4?"_arrow":state==5?"_firework":"");samples.add(row(id,left,body,"state"+state,after,item,arm(slim,left,wrist),model));
                }
                else if(custom.equals("pair"))for(int state=0;state<4;state++){
                    float swing=state==0?0:state==1?.2F:state==2?.5F:.95F;var wrist=new PoseStack();TugarinPairClient.firstPersonWrist(wrist,left,swing);var item=new PoseStack();item.mulPose(wrist.last().pose());GSON.fromJson(in.getAsJsonObject(after?"after":"before"),ItemTransform.class).apply(left,item);samples.add(row(id,left,body,"swing"+state,after,item,after?arm(slim,left,wrist):new JsonArray(),"tugarin_sword_held"));
                }
                else for(int action=0;action<5;action++){
                    int mode=action<3?0:action-2,combo=action<3?action:0,hit=mode==0?(combo==2?7:5):10,duration=mode==0?(combo==2?20:combo==1?15:14):26;
                    for(int tick:new int[]{0,hit-2,hit,hit+2,duration}){
                        float motion=NightSwingPose.motion(tick,mode,combo,duration),horizontal=NightSwingPose.horizontal(tick,mode,combo,duration);var wrist=new PoseStack();
                        if(after){
                            WeaponHands.scytheWrist(wrist,sign,motion,horizontal,combo==1?-1:1);
                            var at=wrist.last().pose().transformPosition(new org.joml.Vector3f());
                            if(at.z>=-.5F||Math.abs(at.x/-at.z)>Math.tan(Math.toRadians(35))*16/9)throw new AssertionError("Scythe wrist left source camera frustum at "+action+":"+tick);
                            if(motion==0){var contact=new PoseStack();contact.translate(0,-.42,-.3);contact.translate(sign*(.56-.20*horizontal),-.12,-.78);if(!contact.last().pose().equals(wrist.last().pose(),1e-6F))throw new AssertionError("Existing contact/rest wrist changed");}
                        }
                        else{ // Exact archived before-grip-source.zip WeaponHands shoulder math.
                            wrist.translate(0,-.42,-.3);wrist.mulPose(Axis.YP.rotationDegrees(sign*motion*70*(combo==1?-1:1)));wrist.translate(sign*(.56-.20*horizontal),-.12,-.78);
                        }
                        var item=new PoseStack();item.mulPose(wrist.last().pose());
                        if(after)ScytheGrip.firstPerson(item,left,horizontal);else{item.mulPose(Axis.XP.rotationDegrees(-90*horizontal));item.mulPose(Axis.YP.rotationDegrees(sign*-10*(1-horizontal)));item.scale(.65F,.65F,.65F);}
                        samples.add(row(id,left,body,"a"+action+"_t"+tick,after,item,arm(slim,left,wrist),"karachun_scythe_held"));
                    }
                }
            }
        }
        Files.writeString(Path.of("docs/implementation/weapons87/fp-native.json"),new GsonBuilder().create().toJson(samples));System.out.println("WEAPONS87_FIRST_PERSON: "+samples.size()+" native transforms/actual hand meshes; all68 IDs,4 custom paths; no game launches");
    }
}
