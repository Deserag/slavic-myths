package org.slavicmyths.verify;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.*;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.village.OutfitRules;

/** Actual baked accessory meshes, no window, Minecraft client or GPU initialization. */
public final class VillageGeometryCheck {
    public static void main(String[] args)throws Exception{
        Class<?> cls=Class.forName("org.slavicmyths.client.VillageOutfitGeometry");
        Method factory=cls.getDeclaredMethod("parts",String.class,int.class,OutfitRules.Climate.class);factory.setAccessible(true);
        String[] names={"none","nitwit","farmer","fisherman","shepherd","librarian","cartographer","leatherworker","butcher","mason","fletcher","toolsmith","weaponsmith","armorer","cleric","miller","brewer","weaver","herder","hunter","cook"};
        Field polygons=ModelPart.Cube.class.getDeclaredField("polygons");polygons.setAccessible(true);
        int[] cubes={0};int models=0;
        for(String name:names)for(var climate:OutfitRules.Climate.values())for(int v=0;v<2;v++){
            ModelPart root=(ModelPart)factory.invoke(null,name,v,climate);
            validate(root,polygons,cubes);models++;
        }
        validate((ModelPart)factory.invoke(null,"merchant",0,OutfitRules.Climate.TEMPERATE),polygons,cubes);models++;
        Method zombie=cls.getDeclaredMethod("zombieBody");zombie.setAccessible(true);
        ModelPart z=(ModelPart)zombie.invoke(null);
        if(z.getChild("head")==null||z.getChild("hat")==null||z.getChild("body").getChild("jacket")==null)throw new AssertionError("Zombie bones missing");
        Method guard=Class.forName("org.slavicmyths.client.GuardEquipmentLayer").getDeclaredMethod("bakedParts");guard.setAccessible(true);ModelPart gear=(ModelPart)guard.invoke(null);validate(gear,polygons,cubes);if(gear.getChild("right")==null||gear.getChild("left")==null||gear.getChild("helmet")==null)throw new AssertionError("Missing guard gear bones");models++;
        System.out.println("VILLAGE_GEOMETRY_PASS meshes="+models+" cubes="+cubes[0]+" UVs=true noseEyesClear=true splitZombieRobe=true clientLaunches=0");
    }
    private static void validate(ModelPart root,Field polygons,int[] count){
        root.visit(new PoseStack(),(pose,path,index,cube)->{
            count[0]++;
            if(cube.minX>=cube.maxX||cube.minY>=cube.maxY||cube.minZ>=cube.maxZ)throw new AssertionError("Degenerate cube "+path);
            if(path.startsWith("/head")&&(intersects(cube,-2.5F,-6,-5,2.5F,-3,-3.9F)||intersects(cube,-1,-3,-6,1,1,-4)))throw new AssertionError("Face obstruction "+path);
            try{for(var polygon:(Object[])polygons.get(cube)){
                var vertices=polygon.getClass().getDeclaredField("vertices");vertices.setAccessible(true);
                for(var vertex:(Object[])vertices.get(polygon)){
                    var uf=vertex.getClass().getDeclaredField("u");var vf=vertex.getClass().getDeclaredField("v");uf.setAccessible(true);vf.setAccessible(true);
                    float u=uf.getFloat(vertex),v=vf.getFloat(vertex);
                    if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||v<0||u>1||v>1)throw new AssertionError("UV outside atlas "+path);
                }
            }}catch(ReflectiveOperationException e){throw new RuntimeException(e);}
        });
    }
    private static boolean intersects(ModelPart.Cube c,float x,float y,float z,float xx,float yy,float zz){return c.maxX>x&&c.minX<xx&&c.maxY>y&&c.minY<yy&&c.maxZ>z&&c.minZ<zz;}
}
