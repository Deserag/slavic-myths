package org.slavicmyths.client;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
/** Checks actual baked meshes without launching Minecraft or claiming render acceptance. */
public final class RewardModelChecks {
 static int checks,cubesCount;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("Invalid baked reward mesh");}
 static void visit(ModelPart part)throws Exception{var field=ModelPart.class.getDeclaredField("cubes");field.setAccessible(true);var cubes=(List<?>)field.get(part);cubesCount+=cubes.size();for(var c:cubes){for(String name:new String[]{"minX","minY","minZ","maxX","maxY","maxZ"}){var f=c.getClass().getField(name);check(Float.isFinite(f.getFloat(c)));}}var f=ModelPart.class.getDeclaredField("children");f.setAccessible(true);for(var child:((Map<?,?>)f.get(part)).values())visit((ModelPart)child);}
 public static void main(String[] args)throws Exception{var f=RewardArmor.class.getDeclaredField("model");f.setAccessible(true);Set<Integer> silhouettes=new HashSet<>();for(int kind=0;kind<3;kind++){cubesCount=0;var armor=new RewardArmor(kind);var m=(net.minecraft.client.model.HumanoidModel<?>)f.get(armor);visit(m.body);visit(m.leftArm);visit(m.rightArm);check(cubesCount>0);silhouettes.add(cubesCount);System.out.println("REWARD_MODEL kind="+kind+" cubes="+cubesCount);}check(silhouettes.size()==3);System.out.println("REWARD_MODEL_CHECKS_PASSED "+checks);}
}
