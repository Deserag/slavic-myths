package org.slavicmyths.client;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.marinka.*;

/** Native baked model checks, not an in-game rendering claim. */
public final class MarinkaChecks {
    private static int checks;private static Field cubes,children,polygons;
    private static void check(boolean ok,String text){checks++;if(!ok)throw new AssertionError(text);}
    @SuppressWarnings("unchecked") private static void export(ModelPart part,PoseStack stack,String path,List<Map<String,Object>> out)throws Exception{
        if(!part.visible)return;stack.pushPose();part.translateAndRotate(stack);
        for(var cube:(List<?>)cubes.get(part)){var faces=new ArrayList<List<float[]>>();for(var face:(Object[])polygons.get(cube))if(face!=null){var f=face.getClass().getDeclaredField("vertices");f.setAccessible(true);var vertices=new ArrayList<float[]>();for(var v:(Object[])f.get(face)){var pos=v.getClass().getDeclaredField("pos");var u=v.getClass().getDeclaredField("u");var t=v.getClass().getDeclaredField("v");pos.setAccessible(true);u.setAccessible(true);t.setAccessible(true);var xyz=stack.last().pose().transformPosition(new org.joml.Vector3f((org.joml.Vector3f)pos.get(v)).div(16));float uv=u.getFloat(v),vv=t.getFloat(v);check(uv>=0&&uv<=1&&vv>=0&&vv<=1,"UV outside atlas: "+path);vertices.add(new float[]{xyz.x*16,xyz.y*16,xyz.z*16,uv,vv});}faces.add(vertices);}out.add(Map.of("part",path,"faces",faces));}
        for(var e:((Map<String,ModelPart>)children.get(part)).entrySet())export(e.getValue(),stack,path+"/"+e.getKey(),out);stack.popPose();
    }
    @SuppressWarnings("unchecked") public static void main(String[] args)throws Exception{
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
        cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");for(var f:List.of(cubes,children,polygons))f.setAccessible(true);
        var output=new LinkedHashMap<String,List<Map<String,Object>>>();
        List<ModelPart> roots=List.of(DobrynyaMesh.create(),MarinkaMesh.create(),AurochsMesh.create(),MagpieMesh.create(),HexFocusMesh.create());
        for(int form=0;form<5;form++){var model=new WitchModel<net.minecraft.world.entity.LivingEntity>(roots.get(form),form);String who=List.of("dobrynya","marinka","aurochs","magpie","anchor").get(form);
            List<WitchAction> clips=form==0?List.of(WitchAction.IDLE,WitchAction.FOLLOW,WitchAction.SIDESTEP,WitchAction.PARRY,WitchAction.COMBO,WitchAction.THRUST,WitchAction.SWEEP,WitchAction.WARD,WitchAction.UNRAVEL,WitchAction.TRANSFORM,WitchAction.REVERT,WitchAction.RETREAT,WitchAction.DRINK,WitchAction.RECOVER,WitchAction.RALLY,WitchAction.SALUTE):form==1?List.of(WitchAction.IDLE,WitchAction.FOLLOW,WitchAction.INTRO,WitchAction.TRACE,WitchAction.FIRE,WitchAction.LURE,WitchAction.SHADOW,WitchAction.VEIL,WitchAction.CRACK,WitchAction.BLINK,WitchAction.BIRD_CHANGE,WitchAction.BIRD_RETURN,WitchAction.CURSE,WitchAction.HERD,WitchAction.RAGE,WitchAction.EXHAUSTED,WitchAction.DEFEAT,WitchAction.CLONE_BREAK):form==2?List.of(WitchAction.AUROCHS,WitchAction.BULL_CHARGE):form==3?List.of(WitchAction.BIRD_FLIGHT):List.of(WitchAction.IDLE);
            for(var action:clips){for(int tick=0;tick<=110;tick++){model.pose(action,tick,tick,.5F,tick,0,0);for(var n:model.nodes.values())check(Float.isFinite(n.xRot)&&Float.isFinite(n.yRot)&&Float.isFinite(n.zRot)&&Float.isFinite(n.x)&&Float.isFinite(n.y)&&Float.isFinite(n.z)&&Float.isFinite(n.xScale)&&Float.isFinite(n.yScale),"Nonfinite pose "+who+action);}model.pose(action,action==WitchAction.IDLE?0:action==WitchAction.COMBO?13:action==WitchAction.DEFEAT?40:16,0,0,action==WitchAction.IDLE?0:16,0,0);var mesh=new ArrayList<Map<String,Object>>();export(model.root,new PoseStack(),"root",mesh);output.put(who+"_"+action,mesh);
                if(action==WitchAction.IDLE&&(form==0||form==1)){double min=1000,max=-1000;for(var cube:mesh){String name=(String)cube.get("part");if(name.contains("/spear")||name.contains("/bottle"))continue;for(var face:(List<List<float[]>>)cube.get("faces"))for(var v:face){min=Math.min(min,v[1]);max=Math.max(max,v[1]);}}check(Math.abs((24-min)/16-(form==0?2.4:2.1))<.0001,"Height "+who+"="+(24-min)/16);check(Math.abs(max-24)<.0001,"Feet "+who);}
            }
            if(form==0){model.pose(WitchAction.DRINK,24,0,0,0,0,0);check(model.nodes.get("bottle").visible&&!model.nodes.get("spear").visible,"Drinking equipment");}
        }
        var rig=new com.google.gson.Gson().fromJson(Files.readString(Path.of("docs/verification/marinka-1.7.2/dobrynya_nikitich-rig.json")),com.google.gson.JsonObject.class);double min=1000,max=-1000;for(var elem:rig.getAsJsonArray("boxes")){var box=elem.getAsJsonObject();if(!box.get("bone").getAsString().equals("spear"))continue;double y=box.getAsJsonArray("center").get(1).getAsDouble(),h=box.getAsJsonArray("size").get(1).getAsDouble();min=Math.min(min,y-h/2);max=Math.max(max,y+h/2);}check(Math.abs((max-min)/16-3.6)<.00001,"Spear3.6");
        Files.createDirectories(Path.of("work/marinka"));Files.writeString(Path.of("work/marinka/meshes.json"),new com.google.gson.Gson().toJson(output));System.out.println("MARINKA OFFLINE PASS checks="+checks+" clips="+output.size()+" NOT_GAME_RENDERING");
    }
}
