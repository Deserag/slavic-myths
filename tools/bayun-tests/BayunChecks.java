package org.slavicmyths.client;

import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.bayun.BayunAnimations;

/** Native mesh evaluation outside Minecraft. This does not validate game rendering. */
public final class BayunChecks {
    private static int checks;private static Field cubes,children,polygons;
    private static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
    @SuppressWarnings("unchecked") private static void export(ModelPart part,PoseStack stack,String path,List<Map<String,Object>> out)throws Exception{
        if(!part.visible)return;stack.pushPose();part.translateAndRotate(stack);
        for(var cube:(List<?>)cubes.get(part)){var faces=new ArrayList<List<float[]>>();for(var face:(Object[])polygons.get(cube))if(face!=null){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);var vertices=new ArrayList<float[]>();for(var v:(Object[])f.get(face)){var pos=v.getClass().getDeclaredField("pos");var u=v.getClass().getDeclaredField("u");var t=v.getClass().getDeclaredField("v");pos.setAccessible(true);u.setAccessible(true);t.setAccessible(true);var xyz=stack.last().pose().transformPosition(new org.joml.Vector3f((org.joml.Vector3f)pos.get(v)).div(16));float uv=u.getFloat(v),vv=t.getFloat(v);check(uv>=0&&uv<=1&&vv>=0&&vv<=1,"UV outside atlas: "+path);vertices.add(new float[]{xyz.x*16,xyz.y*16,xyz.z*16,uv,vv});}faces.add(vertices);}out.add(Map.of("part",path,"faces",faces));}
        for(var e:((Map<String,ModelPart>)children.get(part)).entrySet())export(e.getValue(),stack,path+"/"+e.getKey(),out);stack.popPose();
    }
    @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
        cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");for(var f:List.of(cubes,children,polygons))f.setAccessible(true);
        var model=new BayunClient.Model();check(model.nodes.size()==143,"Native hierarchy");var output=new LinkedHashMap<String,List<Map<String,Object>>>();
        var clips=List.of("idle_breath","idle_sit","sleep_curl_in","sleep_curl_loop","wake_up","stretch_front","groom_paw","walk","turn_attention","loaf_rest","eat_milk","eat_fish","pet_rub","threat_warn","hypnosis_song","pounce_kill","recover");
        for(String clip:clips){for(int tick=0;tick<=BayunAnimations.duration(clip);tick++){var frame=BayunClient.Model.sample(clip,tick);model.pose(clip,frame,tick,0,0);for(var n:model.nodes.values()){check(Float.isFinite(n.xRot)&&Float.isFinite(n.yRot)&&Float.isFinite(n.zRot),"Nonfinite pose "+clip);check(Float.isFinite(n.x)&&Float.isFinite(n.y)&&Float.isFinite(n.z),"Nonfinite translation "+clip);}check(Math.abs(model.nodes.get("jaw").xRot)<=.437F,"Jaw clamp");check(Math.abs(model.nodes.get("ear_L").zRot)<=.385F,"Ear clamp");}
            float age=BayunAnimations.duration(clip)*.5F;model.pose(clip,BayunClient.Model.sample(clip,age),age,0,0);var mesh=new ArrayList<Map<String,Object>>();export(model.root,new PoseStack(),"root",mesh);output.put(clip,mesh);check(mesh.size()*12<16000,"Triangle budget");double min=100,max=-100,paw=-100;for(var cube:mesh)for(var face:(List<List<float[]>>)cube.get("faces"))for(var v:face){min=Math.min(min,v[1]);max=Math.max(max,v[1]);if(cube.get("part").toString().matches(".*/paw_[FB][LR]"))paw=Math.max(paw,v[1]);}System.out.println("BAYUN_POSE_BOUNDS clip="+clip+" height="+((24-min)/16)+" lowest="+((24-max)/16)+" sole="+((24-paw)/16));
        }
        model.pose("sleep_curl_loop",BayunClient.Model.sample("sleep_curl_loop",15),15,0,0);check(model.nodes.get("eyelid_L").yScale>.98,"Sleep eye not closed");check(Math.abs(model.nodes.get("spine").zRot)<1.2,"Sleep merely rotates standing model");check(Math.abs(model.nodes.get("tail_03").yRot)>.3,"Tail not curled");model.pose("pounce_kill",BayunClient.Model.sample("pounce_kill",18),18,0,0);check(model.nodes.get("claws_L").visible,"Attack claws hidden");model.pose("idle_sit",BayunClient.Model.sample("idle_sit",0),0,0,0);check(!model.nodes.get("claws_L").visible,"Idle claws visible");
        Files.createDirectories(Path.of("work/bayun"));Files.writeString(Path.of("work/bayun/meshes.json"),new com.google.gson.Gson().toJson(output));System.out.println("BAYUN OFFLINE PASS checks="+checks+" native clips=17 NOT_GAME_RENDERING");
    }
}
