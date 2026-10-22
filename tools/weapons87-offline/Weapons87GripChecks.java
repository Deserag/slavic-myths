package org.slavicmyths.verify;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;
import org.slavicmyths.client.ScytheGrip;

/** Native ItemTransform math, player-only scythe paths and authored grip pivots. No runtime. */
public final class Weapons87GripChecks {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static final Gson GSON=new GsonBuilder().registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).create();
    static void itemLayer(PoseStack p,boolean left){p.mulPose(Axis.XP.rotationDegrees(-90));p.mulPose(Axis.YP.rotationDegrees(180));p.translate((left?-1F:1F)/16,.125,-.625);}
    public static void main(String[] args)throws Exception {
        int models=0;
        try(var files=Files.list(Path.of("docs/implementation/weapons87/blueprints/grips"))){
            for(var file:files.filter(p->p.toString().endsWith(".json")).toList()){
                var plan=JsonParser.parseString(Files.readString(file)).getAsJsonObject();var model=JsonParser.parseString(Files.readString(Path.of(plan.get("source_model").getAsString()))).getAsJsonObject();
                var grip=plan.getAsJsonArray("grip");models++;
                for(boolean left:new boolean[]{false,true}){
                    String context="thirdperson_"+(left?"lefthand":"righthand");var transform=GSON.fromJson(model.getAsJsonObject("display").getAsJsonObject(context),ItemTransform.class);var p=new PoseStack();transform.apply(left,p);
                    var point=p.last().pose().transformPosition(new Vector3f((grip.get(0).getAsFloat()-8)/16,(grip.get(1).getAsFloat()-8)/16,(grip.get(2).getAsFloat()-8)/16));check(point.length()<1e-5,"Actual native grip centered "+file+context+point);
                }
            }
        }
        check(models==28,"all28 scoped grip models");
        var source=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/models/item/karachun_scythe_held.json"))).getAsJsonObject();
        for(boolean left:new boolean[]{false,true}){
            var nativeIdle=new PoseStack();itemLayer(nativeIdle,left);GSON.fromJson(source.getAsJsonObject("display").getAsJsonObject("thirdperson_"+(left?"lefthand":"righthand")),ItemTransform.class).apply(left,nativeIdle);
            var customIdle=new PoseStack();ScytheGrip.thirdPerson(customIdle,left?HumanoidArm.LEFT:HumanoidArm.RIGHT,0);check(customIdle.last().pose().equals(nativeIdle.last().pose(),1e-6F),"Custom idle exactly matches actual item model "+left);
            var ordinaryFirst=new PoseStack();ordinaryFirst.translate(left?-.56F:.56F,-.52F,-.72F);GSON.fromJson(source.getAsJsonObject("display").getAsJsonObject("firstperson_"+(left?"lefthand":"righthand")),ItemTransform.class).apply(left,ordinaryFirst);
            var customFirst=new PoseStack();customFirst.translate(left?-.56:.56,-.54,-1.08);ScytheGrip.firstPerson(customFirst,left,0);check(ordinaryFirst.last().pose().equals(customFirst.last().pose(),1e-6F),"Ordinary offhand FP matches actual custom mainhand idle for physical side "+left);
            for(int i=0;i<=100;i++){
                float horizontal=i/100F;var p=new PoseStack();ScytheGrip.thirdPerson(p,left?HumanoidArm.LEFT:HumanoidArm.RIGHT,horizontal);var point=p.last().pose().transformPosition(new Vector3f(0,(ScytheGrip.GRIP_Y-8)/16,0));var base=new PoseStack();itemLayer(base,left);var hand=base.last().pose().transformPosition(new Vector3f());check(point.distance(hand)<1e-5,"Third-person pivot remains in palm at every blend");
                p=new PoseStack();ScytheGrip.firstPerson(p,left,horizontal);point=p.last().pose().transformPosition(new Vector3f(0,(ScytheGrip.GRIP_Y-8)/16,0));check(point.length()<1e-5,"First-person actual pivot remains at wrist");
            }
            var contact=new PoseStack();contact.mulPose(Axis.XP.rotationDegrees(-90));ScytheGrip.thirdPerson(contact,left?HumanoidArm.LEFT:HumanoidArm.RIGHT,1);var direction=contact.last().pose().transformDirection(new Vector3f(0,1,0));check(direction.z<-.74F&&Math.abs(direction.y)<1e-5,"Existing contact arm pose leaves shaft horizontal and forward");
        }
        System.out.println("WEAPONS87_GRIPS: native ItemTransform and scythe helper PASS; models="+models+" checks="+checks+"; no game launches");
    }
}
