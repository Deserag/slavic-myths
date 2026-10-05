package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.hunt.*;
/** All pivots and surfaces are generated from the reviewed geometry manifest. */
public final class WorldBossModel extends EntityModel<WorldBoss>{
 private int texWidth=64,texHeight=32;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private final Map<String,ModelPart> parts=new LinkedHashMap<>();private final Map<String,float[]> neutral=new HashMap<>();private final boolean likho;
 public WorldBossModel(boolean likho){this.likho=likho;texWidth=512;texHeight=512;WorldBossGeometry.build(this,likho);}
 void add(String name,String parent,float x,float y,float z,float[] box,int material,float rx,float ry,float rz){ModelPart p=geometry.part(texWidth,texHeight,material%8*64,material/8*64);p.setPos(x,y,z);p.xRot=rx;p.yRot=ry;p.zRot=rz;if(box!=null)geometry.box(p,box[0],box[1],box[2],box[3],box[4],box[5]);parts.put(name,p);neutral.put(name,new float[]{x,y,z,rx,ry,rz});if(!parent.isEmpty())geometry.attach(parts.get(parent),p);}
 private ModelPart p(String n){return parts.get(n);}private void rx(String n,float a){if(p(n)!=null)p(n).xRot+=a;}private void rz(String n,float a){if(p(n)!=null)p(n).zRot+=a;}
 @Override public void setupAnim(WorldBoss e,float walk,float amount,float age,float yaw,float pitch){for(Map.Entry<String,ModelPart> entry:parts.entrySet()){float[] b=neutral.get(entry.getKey());ModelPart p=entry.getValue();p.setPos(b[0],b[1],b[2]);p.xRot=b[3];p.yRot=b[4];p.zRot=b[5];}if(p("mouthGlow")!=null)p("mouthGlow").visible=e.phase()>=3;float t=e.animationTime(age-e.tickCount);BossRules.Move m=e.move();p("head").yRot=yaw*.017453F;p("head").xRot=pitch*.017453F+(likho?.09F:0);float stride=(float)Math.cos(walk*.65)*amount;rx("legL",stride*.6F);rx("legR",-stride*.6F);rx("armL",-stride*.3F);rx("armR",stride*.3F);
  if(likho){for(int i=0;i<22;i++){rx("hair"+i,(float)Math.sin(age*.045-i*.17)*.025F+amount*.045F);rz("hair"+i,(float)Math.sin(age*.035+i*.25)*.02F);}for(int i=0;i<8;i++)rx("frontRag"+i,(float)Math.sin(age*.04+i)*.025F);for(int i=0;i<6;i++)rx("backRag"+i,-amount*.06F);rx("skullA",(float)Math.sin(age*.07)*.05F);rx("skullB",(float)Math.sin(age*.055+1)*.07F);if(age%100<4&&m!=BossRules.Move.GAZE)p("upperLid").y+=.85F;
   if(m==BossRules.Move.SWEEP){float a=(float)Math.sin(Math.min(1,t/26)*Math.PI);rx("armR",-1.4F*a);p("body").yRot=-.6F*a;rz("foreR",-.65F*a);}
   if(m==BossRules.Move.GRAB||m==BossRules.Move.HOLD){float a=Math.min(1,t/18);rx("armL",-1.2F*a);rx("armR",-1.2F*a);rx("foreL",-.4F*a);rx("foreR",-.4F*a);for(String side:new String[]{"L","R"})for(int i=0;i<4;i++){rz("finger"+side+i,(i-1.5F)*.16F*a);rx("finger"+side+i,m==BossRules.Move.HOLD?-.65F: -.15F);}}
   if(m==BossRules.Move.GAZE){p("upperLid").y-=.25F;p("lowerLid").y+=.15F;rx("head",-.1F);rx("armL",-.4F);rx("armR",-.4F);}
   if(m==BossRules.Move.GROUND){rx("body",.3F);rx("armL",-.7F);rx("armR",-.8F);}if(m==BossRules.Move.CHASE){rx("body",.18F);rx("armL",-.25F);rx("armR",-.25F);}if(m==BossRules.Move.LEAP){rx("body",.15F);rx("armL",-1.5F);rx("armR",-1.5F);if(t<18){rx("legL",-.35F);rx("legR",-.35F);p("root").y+=2;}}
  }else{rx("moustacheL",(float)Math.sin(age*.04)*.03F+amount*.06F);rx("moustacheR",(float)Math.sin(age*.04+.7)*.03F+amount*.06F);
   if(m==BossRules.Move.PUNCH){float a=(float)Math.sin(Math.min(1,t/24)*Math.PI);rx("armR",-1.8F*a);rx("foreR",-.35F*a);p("body").yRot=-.25F*a;}
   if(m==BossRules.Move.CHARGE){rx("body",.22F);rx("armL",-.55F);rx("armR",-.55F);}
   if(m==BossRules.Move.SPIN){p("legL").xRot=0;p("legR").xRot=0;p("armL").zRot=-1.1F;p("armR").zRot=1.1F;if(t<=12){p("root").x=3.3F;p("body").x-=3.3F;p("legL").x-=3.3F;p("legR").x-=3.3F;p("legR").xRot=-.6F*Math.min(1,t/12);}else{p("root").x=3.3F;p("body").x-=3.3F;p("legL").x-=3.3F;p("legR").x-=3.3F;float progress=Math.min(1,(t-12)/10);p("legR").zRot=1.5708F*progress;p("root").yRot=(float)(Math.max(0,Math.min(1,(t-22)/14))*Math.PI*2);p("moustacheL").zRot=-.25F*progress;p("moustacheR").zRot=.25F*progress;}}
   if(m==BossRules.Move.SLAM){float a=t<20?Math.min(1,t/20):Math.max(0,1-(t-20)/9);rx("armL",-2.8F*a);rx("armR",-2.8F*a);rx("body",t>=20?.25F:0);}
   if(m==BossRules.Move.STONE){rx("armR",-2.5F*Math.min(1,t/16));p("body").yRot=-.25F;}
   if(m==BossRules.Move.BREATH||m==BossRules.Move.TRANSITION){rx("head",-.2F);p("jaw").y+=.75F;rx("armL",-.4F);rx("armR",-.4F);}
  }
  if(e.deathTime>0){p("root").zRot=Math.min(1,e.deathTime/20F)*1.35F;p("root").y+=Math.min(12,e.deathTime*.6F);}if(m==BossRules.Move.TRANSITION)p("body").xRot+=(float)Math.sin(t*.22)*.08F;
 }
 public void glow(PoseStack pose,VertexConsumer out,WorldBoss e){if(likho?e.move()!=BossRules.Move.GAZE:e.phase()<3)return;pose.pushPose();p("root").translateAndRotate(pose);p("body").translateAndRotate(pose);p("head").translateAndRotate(pose);(likho?p("irisRing"):p("mouthGlow")).render(pose,out,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();}
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){p("root").render(pose,out,light,overlay,color);}
}
