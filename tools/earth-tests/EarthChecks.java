package org.slavicmyths.client;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.earth.*;
import org.slavicmyths.depths.*;

/** Native mesh builders, pure combat rules and actual SavedData codec; no game bootstrap. */
public final class EarthChecks {
    static int checks;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static java.lang.reflect.Field cubes,children,polygons;
    static JsonArray export(ModelPart p,String path,float x,float y,float z) throws Exception{x+=p.x;y+=p.y;z+=p.z;var out=new JsonArray();for(var raw:(List<?>)cubes.get(p)){var cube=(ModelPart.Cube)raw;check(cube.maxX>cube.minX&&cube.maxY>cube.minY&&cube.maxZ>cube.minZ,"positive dimensions "+path);var json=new JsonObject();json.addProperty("part",path);json.add("min",new Gson().toJsonTree(new float[]{x+cube.minX,y+cube.minY,z+cube.minZ}));json.add("max",new Gson().toJsonTree(new float[]{x+cube.maxX,y+cube.maxY,z+cube.maxZ}));var faces=new JsonArray();for(Object face:(Object[])polygons.get(cube)){if(face==null)continue;var vf=face.getClass().getDeclaredField("vertices");vf.setAccessible(true);var coords=new JsonArray();for(Object vertex:(Object[])vf.get(face)){java.lang.reflect.Field u=vertex.getClass().getDeclaredField("u"),v=vertex.getClass().getDeclaredField("v"),pos=vertex.getClass().getDeclaredField("pos");u.setAccessible(true);v.setAccessible(true);pos.setAccessible(true);float a=u.getFloat(vertex),b=v.getFloat(vertex);check(a>=0&&a<=1&&b>=0&&b<=1,"UV bounds "+path+": "+a+","+b);var at=(org.joml.Vector3f)pos.get(vertex);coords.add(new Gson().toJsonTree(new float[]{x+at.x,y+at.y,z+at.z,a,b}));}faces.add(coords);}json.add("faces",faces);out.add(json);}for(var e:((Map<String,ModelPart>)children.get(p)).entrySet())for(var child:export(e.getValue(),path+"/"+e.getKey(),x,y,z))out.add(child);return out;}
    public static void main(String[] args) throws Exception{
        // Compare the logical release point against actual native ModelPart pivots at hit keys.
        for(var action:List.of(EarthRules.Action.PILLAR,EarthRules.Action.VOLLEY)){
            var model=new EarthClient.BossModel();float time=action==EarthRules.Action.PILLAR?46:26;
            model.pelvis.y=action==EarthRules.Action.PILLAR?-12:-14;model.torso.y=0;
            model.torso.xRot=action==EarthRules.Action.PILLAR?.25F:0;model.torso.yRot=action==EarthRules.Action.PILLAR?.3F:-.12F;
            var shoulder=model.nodes.get("shoulder_r");shoulder.xRot=action==EarthRules.Action.PILLAR?-.8F:-1.3F;
            var elbow=shoulder.getChild("upper").getChild("elbow");elbow.xRot=action==EarthRules.Action.PILLAR?-.1F:-.2F;
            elbow.getChild("forearm").getChild("wrist").xRot=action==EarthRules.Action.PILLAR?-.2F:-.18F;
            var poses=new com.mojang.blaze3d.vertex.PoseStack();float scale=(float)(EarthRules.HEIGHT/6.6875);
            poses.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));poses.scale(-scale,-scale,scale);poses.translate(0,-1.5,0);
            ModelPart part=model.root;part.translateAndRotate(poses);for(String n:List.of("pelvis","lower","torso","shoulder_r","upper","elbow","forearm","wrist","hand")){part=part.getChild(n);part.translateAndRotate(poses);}
            var at=poses.last().pose().transformPosition(new org.joml.Vector3f(0,7/16F,-4/16F));var logical=EarthRules.hand(action,time,6,0,0,1);
            check(Math.abs(at.x-logical.x())<.001&&Math.abs(at.y-logical.y())<.001&&Math.abs(at.z-logical.z())<.001,"release matches native wrist "+action);
        }
        float[] raws={EarthRules.MELEE_RAW,EarthRules.PILLAR_RAW,EarthRules.SPLASH_RAW,EarthRules.STONE_RAW,EarthRules.WAVE_RAW,EarthRules.SPIKE_RAW,EarthRules.SERVANT_RAW};double[] targets={8,13,8,5,10.5,9,1.5};for(int i=0;i<raws.length;i++){float iron=EarthRules.afterArmor(raws[i],15,0),diamond=EarthRules.afterArmor(raws[i],20,8),netherite=EarthRules.afterArmor(raws[i],20,12);check(Math.abs((i==6?iron:diamond)-targets[i])<.001,"approved armour target");check(diamond<iron&&netherite<=diamond,"tougher armour helps");check(net.minecraft.world.damagesource.CombatRules.getDamageAfterMagicAbsorb(diamond,20)<diamond,"Protection helps");System.out.println("raw="+raws[i]+" iron="+iron+" diamond="+diamond+" netherite="+netherite);}
        for(int alive=0;alive<=6;alive++)for(int safe=0;safe<=3;safe++)check(EarthRules.canSummon(alive,safe)==(alive<=3&&safe==3),"complete wave admission");for(int n=0;n<=4;n++)check(Math.abs(EarthRules.runeDamage(n)-(1-.025*n))<.00001,"linear rune reduction");check(EarthRules.phase(560,800)==2&&EarthRules.phase(240,800)==3,"thresholds");
        var data=new EarthArena();var entry=new EarthArena.Entry(UUID.randomUUID(),new BlockPos(-100,40,230),3);entry.state=EarthArena.State.DORMANT;entry.boss=UUID.randomUUID();entry.spent=21;entry.retired.add(UUID.randomUUID());entry.rewardClaimed=true;entry.gateClosed=false;entry.gateStart=400;data.entries.put(entry.id,entry);var tag=data.save(new CompoundTag(),null);var load=EarthArena.class.getDeclaredMethod("load",CompoundTag.class,net.minecraft.core.HolderLookup.Provider.class);load.setAccessible(true);var copy=(EarthArena)load.invoke(null,tag.copy(),null);check(tag.equals(copy.save(new CompoundTag(),null)),"arena real codec roundtrip");
        for(int direction=0;direction<4;direction++){
            var arena=new EarthArena.Entry(UUID.randomUUID(),new BlockPos(50,40,-100),direction);
            check(arena.contains(arena.center.getCenter()),"arena centre counts as participation");
            check(!arena.contains(arena.at(-16,0,0).getCenter()),"antechamber does not prevent sleep");
            check(!arena.contains(arena.at(-14,0,0).getCenter()),"gate counts as outside");
            check(arena.contains(arena.at(-13,0,0).getCenter()),"arena side of gate counts as inside");
        }
        // The exact local placement must fit the existing cave in all rotations/top variants.
        for(int direction=0;direction<4;direction++)for(boolean sky:new boolean[]{false,true}){var plan=new DepthsPlan(UUID.randomUUID(),new DepthsPlan.Point(0,0,0),new DepthsPlan.Point(0,64,0),direction,1,sky,0);var cells=DepthsLayout.build(plan);for(int u=-21;u<=-15;u++)for(int v=-5;v<=5;v++)for(int h=0;h<=7;h++)if(Math.abs(v)==5||h==7)check("air".equals(cells.get(new DepthsLayout.Cell(156+u,-23+h,v))),"antechamber in existing air");for(var offset:EarthArena.PILLAR_OFFSETS)for(int x=0;x<2;x++)for(int z=0;z<2;z++)for(int y=0;y<6;y++)check("air".equals(cells.get(new DepthsLayout.Cell(156+offset[0]+x,-23+y,offset[1]+z))),"column in existing air");}
        cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");cubes.setAccessible(true);children.setAccessible(true);polygons.setAccessible(true);var models=new JsonObject();models.add("boss",export(EarthClient.bossMesh(),"boss",0,0,0));models.add("servant_0",export(EarthClient.servantMesh(0),"servant_0",0,0,0));models.add("servant_1",export(EarthClient.servantMesh(1),"servant_1",0,0,0));models.add("pillar",export(EarthClient.ActorRenderer.columnMesh(),"pillar",0,0,0));for(int i=0;i<3;i++)models.add("rock_"+i,export(EarthClient.ActorRenderer.rockMesh(i),"rock",0,0,0));
        var boss=models.getAsJsonArray("boss");float minY=100,maxY=-100;for(var b:boss){minY=Math.min(minY,b.getAsJsonObject().getAsJsonArray("min").get(1).getAsFloat());maxY=Math.max(maxY,b.getAsJsonObject().getAsJsonArray("max").get(1).getAsFloat());}check((maxY-minY)/16>=6&&(maxY-minY)/16<=7,"boss height "+(maxY-minY)/16);check(boss.size()>60,"original segmented geometry");new EarthClient.BossModel();System.out.println("Boss height="+(maxY-minY)/16+" boxes="+boss.size());var out=Path.of("work/earth/meshes.json");Files.createDirectories(out.getParent());Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(models));System.out.println("EARTH OFFLINE PASS: "+checks+" checks. Minecraft launches: 0. Codec roundtrip is not game relog.");
    }
}
