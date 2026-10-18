package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.hunt.*;
/** All pivots and surfaces are generated from the reviewed geometry manifest. */
public final class WorldBossModel extends EntityModel<WorldBoss> implements BossGeometrySink{
 private int texWidth=64,texHeight=32;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private final Map<String,ModelPart> parts=new LinkedHashMap<>();private final Map<String,float[]> neutral=new HashMap<>();private final boolean likho,serpent;private boolean higher;
 public WorldBossModel(boolean likho){this(likho,false);}
 public WorldBossModel(boolean likho,boolean serpent){this.likho=likho;this.serpent=serpent;texWidth=likho?512:1024;texHeight=likho?512:1024;if(likho)WorldBossGeometry.build(this,true);else BossReworkGeometry.build(this,serpent?1:0);}
 public WorldBossModel(boolean likho,boolean serpent,boolean higher){this(likho,serpent);this.higher=higher;if(higher&&likho){
  add("morSocket","head",-1.1F,-1.4F,-2.1F,new float[]{0,0,0,2.2F,2.2F,.2F},6,0,0,0);
  add("morEye","head",-.85F,-1.15F,-2.75F,new float[]{0,0,0,1.7F,1.7F,.22F},9,0,0,0);
  add("morCore","morEye",.52F,.35F,-.08F,new float[]{0,0,0,.6F,.9F,.12F},15,0,0,0);
  for(String side:new String[]{"L","R"}){for(int i=0;i<4;i++)add("morFinger"+side+i,"fore"+side,-1.8F+i*.9F,12,-.9F,new float[]{0,0,0,.7F,4+i%2,1},1,-.2F,0,(i-1.5F)*.09F);add("morShoulder"+side,"arm"+side,-2,-1,-1.8F,new float[]{0,0,0,4,3,4},3,0,0,side.equals("L")?.2F:-.2F);}
  for(int i=0;i<7;i++)add("morTatter"+i,"body",-3+i,-10,-1.8F,new float[]{0,0,0,.65F,14+i%3, .6F},3,.08F,0,(i-3)*.035F);
  for(int i=0;i<5;i++)add("morBackRag"+i,"body",-2.5F+i,-17,1.5F,new float[]{0,0,0,.8F,20-i%3,.7F},3,-.08F,0,0);
 }}
 public void poseHigher(float age,int skill,float elapsed,int hurt,int death){float prep=Math.min(1,elapsed/30),hit=Math.min(1,Math.max(0,elapsed-22)/5);rx("body",.08F+(float)Math.sin(age*.05)*.025F);rx("armL",(float)Math.sin(age*.09)*.035F);rx("armR",(float)Math.sin(age*.09+1)*.035F);p("morEye").xScale=p("morEye").yScale=1+(float)Math.sin(age*.12)*.06F;
  if(skill==10){rx("body",-.12F*prep);rx("head",-.2F*prep);p("morEye").xScale=p("morEye").yScale=1+prep*.3F;}
  if(skill==11){rx("armR",-prep*1.7F+hit*2.8F);p("body").yRot=-prep*.4F+hit*.8F;}
  if(skill==12||skill==15){rx("armL",-prep*.7F);rx("armR",-prep*.7F);rz("armL",-prep*.5F);rz("armR",prep*.5F);rx("body",(float)Math.sin(elapsed*.25)*.1F);}
  if(hurt>0)rx("body",-.12F);if(death>0){p("root").zRot=Math.min(1,death/20F)*1.35F;p("morEye").visible=death<14;}
 }
 public void add(String name,String parent,float x,float y,float z,float[] box,int material,float rx,float ry,float rz){ModelPart p=geometry.part(texWidth,texHeight,likho?material%8*64:material%4*256,likho?material/8*64:material/4*256);p.setPos(x,y,z);p.xRot=rx;p.yRot=ry;p.zRot=rz;if(box!=null)geometry.box(p,box[0],box[1],box[2],box[3],box[4],box[5]);parts.put(name,p);neutral.put(name,new float[]{x,y,z,rx,ry,rz});if(!parent.isEmpty())geometry.attach(parts.get(parent),p);}
 private ModelPart p(String n){return parts.get(n);}private void rx(String n,float a){if(p(n)!=null)p(n).xRot+=a;}private void rz(String n,float a){if(p(n)!=null)p(n).zRot+=a;}
 @Override public void setupAnim(WorldBoss e,float walk,float amount,float age,float yaw,float pitch){
  sample(e.move(),e.animationTime(age-e.tickCount),e.previousTugarinMove(),e.hurtTime,e.deathTime,e.morSkill(),e.morElapsed(age-e.tickCount),walk,amount,age,yaw,pitch);
 }
 public void sample(BossRules.Move m,float t,BossRules.Move previous,int hurtTicks,int deathTicks,int morSkill,float morElapsed,float walk,float amount,float age,float yaw,float pitch){
for(Map.Entry<String,ModelPart> entry:parts.entrySet()){float[] b=neutral.get(entry.getKey());ModelPart p=entry.getValue();p.setPos(b[0],b[1],b[2]);p.xRot=b[3];p.yRot=b[4];p.zRot=b[5];p.xScale=1;p.yScale=1;p.zScale=1;}if(p("mouthGlow")!=null)p("mouthGlow").visible=serpent&&(m==BossRules.Move.BREATH||m==BossRules.Move.TRANSITION);p("head").yRot=yaw*.017453F;p("head").xRot=pitch*.017453F+(likho?.09F:0);float stride=(float)Math.cos(walk*.65)*amount;rx("legL",stride*.6F);rx("legR",-stride*.6F);rx("armL",-stride*.3F);rx("armR",stride*.3F);
  if(!likho){TugarinPoses.sample(parts,serpent,m,t,previous,hurtTicks,deathTicks,walk,amount,age);return;}
  for(int i=0;i<22;i++){rx("hair"+i,(float)Math.sin(age*.045-i*.17)*.025F+amount*.045F);rz("hair"+i,(float)Math.sin(age*.035+i*.25)*.02F);}for(int i=0;i<8;i++)rx("frontRag"+i,(float)Math.sin(age*.04+i)*.025F);for(int i=0;i<6;i++)rx("backRag"+i,-amount*.06F);rx("skullA",(float)Math.sin(age*.07)*.05F);rx("skullB",(float)Math.sin(age*.055+1)*.07F);if(age%100<4&&m!=BossRules.Move.GAZE)p("upperLid").y+=.85F;
   if(m==BossRules.Move.SWEEP){float a=(float)Math.sin(Math.min(1,t/26)*Math.PI);rx("armR",-1.4F*a);p("body").yRot=-.6F*a;rz("foreR",-.65F*a);}
   if(m==BossRules.Move.GRAB||m==BossRules.Move.HOLD){float a=Math.min(1,t/18);rx("armL",-1.2F*a);rx("armR",-1.2F*a);rx("foreL",-.4F*a);rx("foreR",-.4F*a);for(String side:new String[]{"L","R"})for(int i=0;i<4;i++){rz("finger"+side+i,(i-1.5F)*.16F*a);rx("finger"+side+i,m==BossRules.Move.HOLD?-.65F: -.15F);}}
   if(m==BossRules.Move.GAZE){p("upperLid").y-=.25F;p("lowerLid").y+=.15F;rx("head",-.1F);rx("armL",-.4F);rx("armR",-.4F);}
   if(m==BossRules.Move.GROUND){rx("body",.3F);rx("armL",-.7F);rx("armR",-.8F);}if(m==BossRules.Move.CHASE){rx("body",.18F);rx("armL",-.25F);rx("armR",-.25F);}if(m==BossRules.Move.LEAP){rx("body",.15F);rx("armL",-1.5F);rx("armR",-1.5F);if(t<18){rx("legL",-.35F);rx("legR",-.35F);p("root").y+=2;}}

  if(higher){p("morEye").visible=true;poseHigher(age,morSkill,morElapsed,hurtTicks,deathTicks);}
  if(deathTicks>0){p("root").zRot=Math.min(1,deathTicks/20F)*1.35F;p("root").y+=Math.min(12,deathTicks*.6F);}if(m==BossRules.Move.TRANSITION)p("body").xRot+=(float)Math.sin(t*.22)*.08F;
  }
 public void glow(PoseStack pose,VertexConsumer out,WorldBoss e){if(likho?(!higher&&e.move()!=BossRules.Move.GAZE):!serpent||(e.move()!=BossRules.Move.BREATH&&e.move()!=BossRules.Move.TRANSITION))return;pose.pushPose();p("root").translateAndRotate(pose);p("body").translateAndRotate(pose);if(serpent){p("neck").translateAndRotate(pose);p("neckUpper").translateAndRotate(pose);}p("head").translateAndRotate(pose);(likho?p(higher?"morEye":"irisRing"):p("mouthGlow")).render(pose,out,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();}
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){p("root").render(pose,out,light,overlay,color);}
}
