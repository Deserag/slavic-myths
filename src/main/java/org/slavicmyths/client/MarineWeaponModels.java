package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.marine.*;
/** Authored stepped voxel silhouettes from the approved ocean weapon sheet. Units are model pixels. */
public final class MarineWeaponModels {
 static final int TEAL=0xff24bbc2,NAVY=0xff173b50,BRONZE=0xffb68b47,LIGHT=0xff72e6df,DARK=0xff26343d,WOOD=0xff4f3a2b;
 static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
 final FolkModelGeometry geometry=new FolkModelGeometry();
 final Map<String,Map<Integer,ModelPart>> meshes=new HashMap<>();
 public MarineWeaponModels(){trident();shield();anchor();staff();harpoon();}
 void box(String model,int color,float x,float y,float z,float w,float h,float d){var part=meshes.computeIfAbsent(model,k->new LinkedHashMap<>()).computeIfAbsent(color,k->geometry.part(16,16));geometry.box(part,x,y,z,w,h,d);}
 void shaft(String model,int color){box(model,color,-1,-14,-1,2,30,2);for(int y=-12;y<=14;y+=7)box(model,BRONZE,-1.4F,y,-1.4F,2.8F,1,2.8F);}
 void trident(){String m="trident";shaft(m,NAVY);box(m,BRONZE,-4,-15,-2,8,3,4);for(int side:new int[]{-1,1}){box(m,TEAL,side<0?-8:5,-22,-1.2F,3,8,2.4F);box(m,LIGHT,side<0?-7:6,-26,-.8F,1.5F,4,1.6F);box(m,TEAL,side<0?-6:2,-17,-1.2F,4,3,2.4F);}box(m,TEAL,-1.7F,-29,-1.2F,3.4F,15,2.4F);box(m,LIGHT,-.7F,-32,-.7F,1.4F,4,1.4F);box(m,LIGHT,-2,-15,-2.4F,4,3,1);}
 void shield(){for(int y=-10;y<10;y+=2){float w=(float)Math.sqrt(Math.max(0,100-(y+1)*(y+1)));box("guard_rim",LIGHT,-w-.04F,y-.04F,-2.08F,1.08F,2.08F,4.16F);box("guard_rim",LIGHT,w-1.04F,y-.04F,-2.08F,1.08F,2.08F,4.16F);}String m="shield";for(int y=-10;y<10;y+=2){float width=(float)Math.sqrt(Math.max(0,100-(y+1)*(y+1)));box(m,BRONZE,-width,y,-2,width*2,2,4);float inner=Math.max(0,width-1.5F);if(inner>0)box(m,NAVY,-inner,y+.2F,-3.1F,inner*2,1.6F,2);}for(int y=-7;y<8;y+=4)for(int x=-6;x<=6;x+=6)if(x*x+y*y<72)box(m,(x+y)%3==0?LIGHT:TEAL,x-2.3F,y-1.5F,-4.2F,4.6F,3,2);box(m,WOOD,-3,-4,2,1,8,3);box(m,WOOD,2,-4,2,1,8,3);box(m,WOOD,-3,-4,4,6,1,1);box(m,WOOD,-3,3,4,6,1,1);}
 void anchor(){String m="anchor";shaft(m,DARK);box(m,BRONZE,-10,-12,-2,20,3,4);box(m,TEAL,-7,-11.4F,-2.2F,4,1,1);box(m,TEAL,3,-11.4F,-2.2F,4,1,1);for(int side:new int[]{-1,1}){box(m,DARK,side<0?-7:0,12,-1.8F,7,3,3.6F);box(m,DARK,side<0?-10:6,8,-1.8F,4,6,3.6F);box(m,TEAL,side<0?-12:9,5,-2,3,5,4);box(m,BRONZE,side<0?-3:1.5F,-22,-1.5F,1.5F,7,3);}box(m,BRONZE,-3,-22,-1.5F,6,1.5F,3);box(m,BRONZE,-3,-16.5F,-1.5F,6,1.5F,3);for(int i=0;i<4;i++){float y=-18+i*4;box(m,DARK,5,y,-1,1,4,1);box(m,DARK,8,y,-1,1,4,1);box(m,DARK,5,y,-1,4,1,1);box(m,DARK,5,y+3,-1,4,1,1);}}
 void staff(){String m="staff";shaft(m,WOOD);for(int i=0;i<11;i++){double a=Math.toRadians(65+i*23);float x=(float)Math.cos(a)*7,y=-21+(float)Math.sin(a)*7;box(m,BRONZE,x-1.5F,y-1.5F,-1.5F,3,3,3);box(m,TEAL,x-1,y-1,-1.8F,2,2,1);}box(m,LIGHT,-2,-23,-2,4,4,4);box(m,NAVY,-2,-16,-2,4,3,4);for(int i=0;i<3;i++)box(m,TEAL,5+i,-19-i*2,-.8F,1.4F,4,1.6F);}
 void harpoon(){String m="harpoon";shaft(m,NAVY);for(int y=0;y<9;y++){float width=Math.max(.6F,3.8F-y*.4F);box(m,y%3==0?LIGHT:TEAL,-width,-17-y,-1,width*2,1,2);}for(int side:new int[]{-1,1}){box(m,TEAL,side<0?-5:2,-18,-1,3,3,2);box(m,TEAL,side<0?-3:1,12,-1,2,5,2);}box(m,BRONZE,-2,-15,-1.5F,4,2,3);}
 public void render(String model,PoseStack pose,MultiBufferSource out,int light,int overlay){render(model,pose,out,light,overlay,false);}
 public void render(String model,PoseStack pose,MultiBufferSource out,int light,int overlay,boolean foil){
  for(var part:meshes.get(model).entrySet()){
   var texture=!model.equals("shield")&&part.getKey()==NAVY?ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/marine/weapon_scales.png"):!model.equals("shield")&&part.getKey()==TEAL?ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/marine/weapon_scutes.png"):WHITE;
   var buffer=net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect(out,RenderType.entityCutoutNoCull(texture),true,foil);
   part.getValue().render(pose,buffer,model.equals("staff")&&part.getKey()==LIGHT?0xF000F0:light,overlay,part.getKey());
  }
 }
 public void renderGuard(PoseStack pose,MultiBufferSource out,int overlay){
  render("guard_rim",pose,out,0xF000F0,overlay,false);
 }
 public static String name(ItemStack stack){return stack.is(ZhelvaShield.ITEM.get())?"shield":stack.is(MarineWeapons.ANCHOR.get())?"anchor":stack.is(MarineWeapons.STAFF.get())?"staff":stack.is(PharaohkaTrident.ITEM.get())?"trident":"harpoon";}
}