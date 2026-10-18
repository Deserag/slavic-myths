package org.slavicmyths.verify;
import java.lang.reflect.*;
import java.util.*;
import org.slavicmyths.client.WorldBossModel;
/** Plain JVM: bakes native ModelPart geometry; never starts Minecraft or opens a window. */
public final class TugarinModelsOffline {
    private static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    public static void main(String[] args)throws Exception{
        int checks=0;
        for(boolean[] shape:new boolean[][]{{true,false},{false,false},{false,true}}){
            var model=new WorldBossModel(shape[0],shape[1]);
            Map<?,?> parts=(Map<?,?>)field(model,"parts");
            for(String required:List.of("root","body","head","armL","armR","legL","legR")){if(!parts.containsKey(required))throw new AssertionError(required);checks++;}
            if(shape[1])for(String required:List.of("wingL","wingR","snout","fangL0","fangR0","clawL0","clawR0")){if(!parts.containsKey(required))throw new AssertionError(required);checks++;}
            for(var part:parts.values())for(Object cube:(List<?>)field(part,"cubes")){
                for(Object polygon:(Object[])field(cube,"polygons"))for(Object vertex:(Object[])field(polygon,"vertices")){
                    float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||u>1||v<0||v>1)throw new AssertionError("atlas UV outside texture");checks++;
                }
            }
            System.out.println("Baked native geometry "+Arrays.toString(shape)+": "+parts.size()+" parts");
        }
        for(boolean second:new boolean[]{false,true}){
            var model=new org.slavicmyths.client.NightingaleModel(second);Map<?,?> parts=(Map<?,?>)field(model,"parts");
            for(String name:List.of("root","body","head","armL","armR","foreL","foreR","legL","legR","bladeL","bladeR")){if(!parts.containsKey(name))throw new AssertionError(name);checks++;}
            for(String name:second?List.of("topknot","moustacheL","moustacheR"):List.of("cloakStrip0","cloakStrip2","beard")){if(!parts.containsKey(name))throw new AssertionError(name);checks++;}
            for(var part:parts.values())for(Object cube:(List<?>)field(part,"cubes"))for(Object polygon:(Object[])field(cube,"polygons"))for(Object vertex:(Object[])field(polygon,"vertices")){
                float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||u>1||v<0||v>1)throw new AssertionError("Nightingale UV outside atlas");checks++;
            }
            System.out.println("Baked Nightingale geometry phase2="+second+": "+parts.size()+" parts");
        }
        System.out.println("Tugarin model offline PASS: "+checks+" checks; no client launch or rendered visual approval");
    }
}
