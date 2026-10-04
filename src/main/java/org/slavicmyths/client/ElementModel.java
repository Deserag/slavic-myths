package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.MobEntity;
import org.slavicmyths.hunt.*;
/** Authored 5.7-block plated serpent and 2.6-block masked, legless wind spirit. */
public final class ElementModel<T extends MobEntity> extends EntityModel<T> {
 private final boolean fire;private final ModelRenderer root,glow,head,jaw,left,right;private final List<ModelRenderer> segments=new ArrayList<>(),bands=new ArrayList<>(),cloth=new ArrayList<>();private final Map<ModelRenderer,ModelRenderer> lights=new LinkedHashMap<>();
 public ElementModel(boolean fire){this.fire=fire;texWidth=512;texHeight=512;root=part(null,0,24,0);glow=part(null,0,24,0);lights.put(root,glow);
  if(fire){root.y=10;glow.y=10;box(root,0,-5.5F,-5,-6,11,10,14);head=part(root,0,-1,-16);box(head,0,-6,-5,-10,12,8,12);box(head,1,-5,-4,-13,10,5,5);jaw=part(head,0,3,-1);box(jaw,2,-5.5F,0,-12,11,2.5F,13);box(head,0,-7,-7,0,2,3,8);box(head,0,5,-7,0,2,3,8);bright(head,4,-6.1F,-3,-8,1,1.5F,3);bright(head,4,5.1F,-3,-8,1,1.5F,3);bright(jaw,3,-4.5F,-.5F,-10,9,.6F,10);
   ModelRenderer neck=part(root,0,0,-8);box(neck,1,-4,-4,-8,8,8,8);segments.add(neck);ModelRenderer neck2=part(root,0,0,-15);box(neck2,1,-3,-3,-6,6,6,6);segments.add(neck2);ModelRenderer prev=root;float z=8;for(int i=0;i<7;i++){ModelRenderer s=part(prev,0,0,z);segments.add(s);float w=10-i*1.05F,h=9-i*.95F,length=i<3?10:8;box(s,0,-w/2,-h/2,0,w,h,length);bright(s,3,-w*.4F,-h*.4F,-.3F,w*.8F,h*.8F,1);box(s,1,-1,-h/2-2,2,2,3,5);prev=s;z=length-.5F;}bright(root,3,-4.5F,-4.5F,-6.5F,9,9,1);left=right=null;
  }else{head=part(root,0,-31,0);box(head,5,-3.3F,-8,-3.5F,6.6F,9,5);box(head,6,-1.2F,-6,-4,2.4F,9,1.5F);box(head,6,-5,-9,0,2,7,3);box(head,6,3,-9,0,2,7,3);box(head,7,-6.5F,-11,1,2,4,4);box(head,7,4.5F,-11,1,2,4,4);bright(head,8,-2.6F,-4,-4.1F,1.4F,.8F,.3F);bright(head,8,1.2F,-4,-4.1F,1.4F,.8F,.3F);jaw=null;
   box(root,7,-4,-29,-2.5F,8,12,5);for(int i=0;i<3;i++){ModelRenderer rib=part(root,0,-28+i*3,0);box(rib,6,-5,-.6F,-3,10,1.2F,6);rib.zRot=i%2==0?.13F:-.13F;}
   left=arm(-1);right=arm(1);for(int i=0;i<5;i++){ModelRenderer ring=part(root,0,-16+i*3.4F,0);bands.add(ring);float rad=5.5F-i*.9F;box(ring,5,-rad,-.8F,-rad,rad*2,1.6F,1.5F);box(ring,6,-rad,-.2F,rad-1.5F,rad*2,1.5F,1.5F);box(ring,5,-rad,0,-rad,1.5F,1.5F,rad*2);box(ring,6,rad-1.5F,.4F,-rad,1.5F,1.5F,rad*2);}
   for(int i=0;i<4;i++){ModelRenderer strip=part(i<2?root:i==2?left:right,i<2?(i==0?-4:4):0,i<2?-28:8,0);cloth.add(strip);box(strip,9,-1,0,-3,2,10+i,1);box(strip,9,-.6F,10+i,-3,1.2F,3,1);}
  }
 }
 private ModelRenderer part(ModelRenderer parent,float x,float y,float z){ModelRenderer p=new ModelRenderer(this);p.setPos(x,y,z);if(parent!=null){parent.addChild(p);ModelRenderer g=new ModelRenderer(this);g.setPos(x,y,z);lights.get(parent).addChild(g);lights.put(p,g);}return p;}
 private void box(ModelRenderer p,int material,float x,float y,float z,float w,float h,float d){p.texOffs(material%4*128,material/4*128).addBox(x,y,z,w,h,d);}
 private void bright(ModelRenderer p,int m,float x,float y,float z,float w,float h,float d){box(lights.get(p),m,x,y,z,w,h,d);}
 private ModelRenderer arm(int sign){ModelRenderer a=part(root,sign*5,-28,0);box(a,7,sign<0?-4:0,0,-2,4,10,4);ModelRenderer fore=part(a,sign*1.5F,9,0);box(fore,7,sign<0?-3:0,0,-2,3,10,4);for(int i=0;i<3;i++){box(fore,7,(sign<0?-3:0)+i,9,-3,1,5,1);box(fore,7,(sign<0?-3:0)+i,13,-4.5F,1,1.5F,2);}return a;}
 @Override public void setupAnim(T e,float stride,float amount,float age,float yaw,float pitch){ElementHuntMob mob=e instanceof ElementHuntMob?(ElementHuntMob)e:null;ElementRules.State action=mob==null?ElementRules.State.ORBIT:mob.move();float elapsed=mob==null?e.tickCount:mob.moveElapsed(age-e.tickCount),death=e.deathTime/20F;
  root.xRot=root.zRot=0;root.y=fire?10:24+(float)Math.sin(age*.08)*2;head.yRot=yaw*(float)Math.PI/180*.3F;head.xRot=pitch*(float)Math.PI/180*.25F;
  if(fire){boolean dash=action==ElementRules.State.FIRE_DASH||action==ElementRules.State.ALIGN_DASH;for(int i=0;i<segments.size();i++){ModelRenderer s=segments.get(i);s.yRot=dash?0:(float)Math.sin(age*(action==ElementRules.State.WATER_STUN?.035:.12)-i*.7)*(.20F+i*.008F);s.xRot=dash?0:(float)Math.cos(age*.09-i*.7)*.09F;}jaw.xRot=.09F+(float)Math.sin(age*.07)*.04F;if(action==ElementRules.State.MELEE_SNAP){head.xRot=-.3F;jaw.xRot=elapsed<7?.7F:.05F;}if(action==ElementRules.State.ALIGN_DIVE){head.xRot=.55F;for(ModelRenderer s:segments)s.yRot=.25F;}if(action==ElementRules.State.STAR_DIVE)root.xRot=-.8F;if(action==ElementRules.State.DECOY_CAST)for(ModelRenderer s:segments)s.yRot=.45F;if(action==ElementRules.State.WATER_STUN){root.zRot=.12F;root.y+=2;head.xRot=.3F;}if(death>0){root.zRot=death*.6F;jaw.xRot=.3F;for(ModelRenderer s:segments)s.yRot=death*.3F;}}
  else{float spin=action==ElementRules.State.VORTEX_ACTIVE?.4F:action==ElementRules.State.RECOVERY?.025F:.09F;for(int i=0;i<bands.size();i++){ModelRenderer b=bands.get(i);b.yRot=age*spin+i*.7F;b.xRot=.12F;b.zRot=.08F;b.y=-16+i*3.4F-death*i;}
   left.zRot=.15F;right.zRot=-.15F;left.xRot=right.xRot=0;boolean vortex=action==ElementRules.State.VORTEX_ACTIVE||action==ElementRules.State.VORTEX_CHARGE;if(vortex){left.zRot=.9F;right.zRot=-.9F;root.y-=2;}if(action==ElementRules.State.MELEE_SNAP)right.xRot=elapsed<8?-1.2F:.7F;if(action==ElementRules.State.GUST_CHARGE)right.xRot=-.9F;if(action==ElementRules.State.GUST_RELEASE)right.xRot=-1.6F;if(action==ElementRules.State.BROKEN_PATH_DASH)root.xRot=.55F;for(int i=0;i<cloth.size();i++){cloth.get(i).xRot=(float)Math.sin(age*.15+i)*.2F;cloth.get(i).zRot=(float)Math.cos(age*.12+i)*.18F;}if(death>0){left.zRot=.15F+death;right.zRot=-.15F-death;root.y+=death*6;}}
  for(Map.Entry<ModelRenderer,ModelRenderer> entry:lights.entrySet())entry.getValue().copyFrom(entry.getKey());
 }
 @Override public void renderToBuffer(MatrixStack pose,IVertexBuilder b,int light,int overlay,float red,float green,float blue,float alpha){root.render(pose,b,light,overlay,red,green,blue,alpha);}
 public void glow(MatrixStack p,IVertexBuilder b,T e){float brightness=e.deathTime>0?1-e.deathTime/20F:e instanceof ElementHuntMob&&((ElementHuntMob)e).move()==ElementRules.State.WATER_STUN?.25F:1;glow.render(p,b,15728880,OverlayTexture.NO_OVERLAY,brightness,brightness,brightness,1);}
}
