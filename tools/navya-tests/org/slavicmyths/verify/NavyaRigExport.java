package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.navya.*;
import org.slavicmyths.navya.client.*;
public final class NavyaRigExport {
    public static void main(String[] args)throws Exception {
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
        var data=new JsonObject();
        for(var kind:NavyaContent.Kind.values()) {
            var rig=NavyaMesh.create(kind);var entry=metadata(rig);var frames=new JsonArray();
            for(String state:List.of("idle","walk","alert","hurt","death")) {
                NavyaPose.pose(rig,kind,NavyaMob.Action.IDLE,0,20,state.equals("walk")?2:0,state.equals("walk")?.7F:0,state.equals("alert")?25:0,0,state.equals("hurt")?6:0,state.equals("death")?15:0);
                frames.add(frame(rig,state,0));
            }
            var actions=switch(kind){case NAVKA->List.of(NavyaMob.Action.MELEE,NavyaMob.Action.SHOT);case ROOTLING->List.of(NavyaMob.Action.MELEE,NavyaMob.Action.ROOTS);case CRAWLER->List.of(NavyaMob.Action.MELEE,NavyaMob.Action.SHOT,NavyaMob.Action.DASH);case WISP->List.<NavyaMob.Action>of();case GUARDIAN->List.of(NavyaMob.Action.MELEE,NavyaMob.Action.ROOTS,NavyaMob.Action.THROW,NavyaMob.Action.SUMMON,NavyaMob.Action.PHASE);};
            for(var action:actions)for(float time:new float[]{NavyaTiming.windup(kind,action)*.5F,NavyaTiming.windup(kind,action)-1,NavyaTiming.windup(kind,action),NavyaTiming.windup(kind,action)+5,NavyaTiming.duration(kind,action)-1}) {
                NavyaPose.pose(rig,kind,action,time,20,0,0,0,0,0,0);frames.add(frame(rig,action.name(),time));
            }
            entry.add("frames",frames);data.add(NavyaMesh.name(kind),entry);
        }
        var roots=NavyaMesh.roots();var rootEntry=metadata(roots);var rootFrames=new JsonArray();
        for(float age:new float[]{5,19,25,44,53}) {
            for(var part:roots.nodes().values())part.resetPose();float rise=Math.max(0,Math.min(1,(age-15)/9)),recede=Math.max(0,Math.min(1,(age-45)/15));
            roots.nodes().get("body").y+=(1-rise+recede)*32;roots.nodes().get("warning").visible=age<23;rootFrames.add(frame(roots,"ROOTS",age));
        }
        rootEntry.add("frames",rootFrames);data.add("navya_roots",rootEntry);
        for(int mode=0;mode<3;mode++){var rig=NavyaMesh.projectile(mode);var entry=metadata(rig);var frames=new JsonArray();frames.add(frame(rig,"FLIGHT",0));entry.add("frames",frames);data.add("navya_shot_"+mode,entry);}
        var out=Path.of("docs/implementation/navya-grove/native-rigs.json");Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(data));
        System.out.println("Native ModelPart UV/finite pose export PASS, 9 rigs; no Minecraft client/server/world.");
    }
    private static JsonObject metadata(NavyaMesh.Rig rig){var object=new JsonObject();object.addProperty("atlas",rig.atlas());object.add("boxes",new Gson().toJsonTree(rig.boxes()));return object;}
    private static JsonObject frame(NavyaMesh.Rig rig,String action,float time) {
        var result=new JsonObject();result.addProperty("action",action);result.addProperty("time",time);var faces=new JsonArray();
        rig.root().visit(new PoseStack(),(pose,path,index,cube)->{
            String name=path.substring(path.lastIndexOf('/')+1);var node=rig.nodes().get(name);
            if(node!=null&&!node.visible)return;
            try {
                var field=ModelPart.Cube.class.getDeclaredField("polygons");field.setAccessible(true);
                for(Object polygon:(Object[])field.get(cube)) {
                    var vertices=polygon.getClass().getDeclaredField("vertices");vertices.setAccessible(true);var face=new JsonArray();
                    for(Object vertex:(Object[])vertices.get(polygon)) {
                        var posField=vertex.getClass().getDeclaredField("pos");var uField=vertex.getClass().getDeclaredField("u");var vField=vertex.getClass().getDeclaredField("v");posField.setAccessible(true);uField.setAccessible(true);vField.setAccessible(true);
                        Vector3f local=(Vector3f)posField.get(vertex);var transformed=pose.pose().transformPosition(local.x/16,local.y/16,local.z/16,new Vector3f()).mul(16);
                        float u=uField.getFloat(vertex),v=vField.getFloat(vertex);if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||u>1||v<0||v>1)throw new AssertionError("UV "+path+" "+u+","+v);
                        var point=new JsonArray();for(float value:new float[]{transformed.x,transformed.y,transformed.z,u,v}){if(!Float.isFinite(value))throw new AssertionError("Nonfinite pose");point.add(value);}face.add(point);
                    }
                    faces.add(face);
                }
            }catch(ReflectiveOperationException exception){throw new RuntimeException(exception);}
        });
        result.add("faces",faces);return result;
    }
}
