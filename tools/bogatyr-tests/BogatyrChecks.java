package org.slavicmyths.client;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.bogatyr.*;
import org.slavicmyths.bogatyr.BogatyrRules.State;
/** Native baked model checks, not an in-game rendering claim. */
public final class BogatyrChecks {
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
        var hero=new BogatyrClient.Model<IlyaEntity>(IlyaMesh.create(),true);var boss=new BogatyrClient.Model<IdolishcheEntity>(IdolishcheMesh.create(),false);
        for(boolean isHero:List.of(true,false)){var model=isHero?hero:boss;String who=isHero?"ilya":"idol";check(model.nodes.size()==(isHero?20:40),"Hierarchy "+who);
            var clips=isHero?List.of(State.IDLE,State.FOLLOW,State.PURSUE,State.PROTECTOR_DASH,State.TAUNT_CAST,State.SHIELD_RAISE,State.SHIELD_HOLD,State.SHIELD_RECOVER,State.MACE_WINDUP,State.SHOCKWAVE_CAST,State.UNYIELDING,State.RETREAT_START,State.RETREAT_MOVING,State.HEAL_DRINK,State.RECOVERY,State.RETURNING):List.of(State.IDLE,State.CHASE,State.FIST_WINDUP,State.FIST_SLAM,State.STOMP_TELEGRAPH,State.STOMP_PULSE,State.GRAB_START,State.GRAB_HOLD,State.GRAB_THROW,State.FEED_SUMMON,State.FEED_GRAB,State.FEED_CHEW,State.BREATH_WINDUP,State.BREATH_ACTIVE,State.CHARGE_WINDUP,State.CHARGE_RUNNING,State.CONTEST,State.STUNNED,State.RAGE_TRANSITION,State.DEATH);
            for(var state:clips){for(int tick=0;tick<=110;tick++){model.pose(state,tick,1,.5F,tick,0,0,false);for(var n:model.nodes.values())check(Float.isFinite(n.xRot)&&Float.isFinite(n.yRot)&&Float.isFinite(n.zRot)&&Float.isFinite(n.x)&&Float.isFinite(n.y)&&Float.isFinite(n.z),"Nonfinite pose "+state);}model.pose(state,0,0,0,0,0,0,false);if(state!=State.IDLE)model.pose(state,state==State.MACE_WINDUP?19:state==State.FEED_CHEW?26:state==State.DEATH?88:16,0,0,16,0,0,false);var mesh=new ArrayList<Map<String,Object>>();export(model.root,new PoseStack(),"root",mesh);output.put(who+"_"+state,mesh);if(state==State.IDLE){double min=1000,max=-1000;for(var cube:mesh)for(var face:(List<List<float[]>>)cube.get("faces"))for(var v:face){min=Math.min(min,v[1]);max=Math.max(max,v[1]);}check(Math.abs((24-min)/16-(isHero?2.5:9))<.001,"Height "+who+" min="+min);check(Math.abs(max-24)<.001,"Feet plane "+who);}}
        }
        hero.pose(State.HEAL_DRINK,24,0,0,0,0,0,false);check(hero.nodes.get("flask").visible&&!hero.nodes.get("mace").visible,"Drink flask");hero.pose(State.IDLE,0,0,0,0,0,0,false);check(!hero.nodes.get("flask").visible&&hero.nodes.get("mace").visible,"Idle item duplication");boss.pose(State.FEED_CHEW,1,0,0,0,0,0,false);check(boss.nodes.get("jaw").xRot>=.62F,"Maw not open 36deg");boss.pose(State.IDLE,0,0,0,0,0,0,true);check(boss.nodes.get("rage_eye_0").visible,"Rage eyes");
        check(BogatyrRules.diamondNet(20)>=6,"Fist diamond baseline");check(BogatyrRules.diamondNet(26)>BogatyrRules.diamondNet(22)&&BogatyrRules.diamondNet(22)>BogatyrRules.diamondNet(20),"Stomp gradient");
        for(var state:List.of(State.GRAB_START,State.GRAB_HOLD,State.GRAB_THROW,State.FEED_GRAB,State.FEED_CHEW,State.FIST_WINDUP,State.FIST_SLAM,State.CHARGE_WINDUP,State.CHARGE_RUNNING,State.CONTEST))for(int tick=0;tick<=65;tick++){boss.pose(state,tick,0,0,tick,0,0,false);var stack=new PoseStack();boss.root.translateAndRotate(stack);for(String name:List.of("hips","belly","chest","right_shoulder","right_arm","right_forearm","right_hand"))boss.nodes.get(name).translateAndRotate(stack);var point=stack.last().pose().transformPosition(new org.joml.Vector3f());var actual=new net.minecraft.world.phys.Vec3(point.x,1.5-point.y,-point.z);var shared=BogatyrMotion.wrist(state,tick,tick,0);check(actual.distanceToSqr(shared)<.000001,"Shared wrist mismatch "+state+" "+tick+" "+actual+" "+shared);}
        Files.createDirectories(Path.of("work/bogatyr"));Files.writeString(Path.of("work/bogatyr/meshes.json"),new com.google.gson.Gson().toJson(output));System.out.println("BOGATYR OFFLINE PASS checks="+checks+" clips="+output.size()+" NOT_GAME_RENDERING");
    }
}
