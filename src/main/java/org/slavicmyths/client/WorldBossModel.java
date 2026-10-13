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
 @Override public void setupAnim(WorldBoss e,float walk,float amount,float age,float yaw,float pitch){for(Map.Entry<String,ModelPart> entry:parts.entrySet()){float[] b=neutral.get(entry.getKey());ModelPart p=entry.getValue();p.setPos(b[0],b[1],b[2]);p.xRot=b[3];p.yRot=b[4];p.zRot=b[5];p.xScale=1;p.yScale=1;p.zScale=1;}if(p("mouthGlow")!=null)p("mouthGlow").visible=serpent&&(e.move()==BossRules.Move.BREATH||e.move()==BossRules.Move.TRANSITION);float t=e.animationTime(age-e.tickCount);BossRules.Move m=e.move();p("head").yRot=yaw*.017453F;p("head").xRot=pitch*.017453F+(likho?.09F:0);float stride=(float)Math.cos(walk*.65)*amount;rx("legL",stride*.6F);rx("legR",-stride*.6F);rx("armL",-stride*.3F);rx("armR",stride*.3F);
  if(likho){for(int i=0;i<22;i++){rx("hair"+i,(float)Math.sin(age*.045-i*.17)*.025F+amount*.045F);rz("hair"+i,(float)Math.sin(age*.035+i*.25)*.02F);}for(int i=0;i<8;i++)rx("frontRag"+i,(float)Math.sin(age*.04+i)*.025F);for(int i=0;i<6;i++)rx("backRag"+i,-amount*.06F);rx("skullA",(float)Math.sin(age*.07)*.05F);rx("skullB",(float)Math.sin(age*.055+1)*.07F);if(age%100<4&&m!=BossRules.Move.GAZE)p("upperLid").y+=.85F;
   if(m==BossRules.Move.SWEEP){float a=(float)Math.sin(Math.min(1,t/26)*Math.PI);rx("armR",-1.4F*a);p("body").yRot=-.6F*a;rz("foreR",-.65F*a);}
   if(m==BossRules.Move.GRAB||m==BossRules.Move.HOLD){float a=Math.min(1,t/18);rx("armL",-1.2F*a);rx("armR",-1.2F*a);rx("foreL",-.4F*a);rx("foreR",-.4F*a);for(String side:new String[]{"L","R"})for(int i=0;i<4;i++){rz("finger"+side+i,(i-1.5F)*.16F*a);rx("finger"+side+i,m==BossRules.Move.HOLD?-.65F: -.15F);}}
   if(m==BossRules.Move.GAZE){p("upperLid").y-=.25F;p("lowerLid").y+=.15F;rx("head",-.1F);rx("armL",-.4F);rx("armR",-.4F);}
   if(m==BossRules.Move.GROUND){rx("body",.3F);rx("armL",-.7F);rx("armR",-.8F);}if(m==BossRules.Move.CHASE){rx("body",.18F);rx("armL",-.25F);rx("armR",-.25F);}if(m==BossRules.Move.LEAP){rx("body",.15F);rx("armL",-1.5F);rx("armR",-1.5F);if(t<18){rx("legL",-.35F);rx("legR",-.35F);p("root").y+=2;}}
  }else{rx("moustacheL",(float)Math.sin(age*.04)*.03F+amount*.06F);rx("moustacheR",(float)Math.sin(age*.04+.7)*.03F+amount*.06F);
   if(m==BossRules.Move.PUNCH){float a=(float)Math.sin(Math.min(1,t/24)*Math.PI);rx("armR",-1.8F*a);rx("foreR",-.35F*a);p("body").yRot=-.25F*a;}
   if(m==BossRules.Move.BLADE_COMBO){int stage=Math.min(2,(int)(t/12));float local=t-stage*12,swing=(float)Math.sin(Math.min(1,local/20)*Math.PI);String side=(stage&1)==0?"R":"L";rx("arm"+side,-1.9F*swing);rx("fore"+side,-.4F*swing);p("body").yRot=((stage&1)==0?-.35F:.35F)*swing;}
   if(m==BossRules.Move.CHARGE){rx("body",.22F);rx("armL",-.55F);rx("armR",-.55F);}
   if(m==BossRules.Move.SPIN){
    float spread=Math.min(1,t/12),active=Math.max(0,Math.min(1,(t-16)/36));
    float turns=active*active*(3-2*active)*3*net.minecraft.util.Mth.TWO_PI;
    p("root").yRot=turns;p("armL").zRot=-1.48F*spread;p("armR").zRot=1.48F*spread;
    rx("foreL",-.18F*spread);rx("foreR",-.18F*spread);rx("body",.12F*spread);
    p("body").zRot=(float)Math.sin(turns)*.045F;p("body").y+=(float)Math.sin(turns*2)*.45F;
    p("legL").xRot=(float)Math.sin(turns)*.2F;p("legR").xRot=-(float)Math.sin(turns)*.2F;
    rz("moustacheL",-.14F*spread);rz("moustacheR",.14F*spread);
   }
   if(m==BossRules.Move.RECOVERY&&e.previousTugarinMove()==BossRules.Move.SPIN){float a=Math.max(0,1-t/24);p("armL").zRot=-1.48F*a;p("armR").zRot=1.48F*a;rx("body",.2F*a);rx("foreL",-.25F*a);rx("foreR",-.25F*a);}
   if(m==BossRules.Move.SLAM){float a=t<20?Math.min(1,t/20):Math.max(0,1-(t-20)/9);rx("armL",-2.8F*a);rx("armR",-2.8F*a);rx("body",t>=20?.25F:0);}
   if(m==BossRules.Move.STONE){rx("armR",-2.5F*Math.min(1,t/16));p("body").yRot=-.25F;}
   if(m==BossRules.Move.BREATH||m==BossRules.Move.TRANSITION){rx("head",-.2F);p("jaw").y+=.75F;rx("armL",-.4F);rx("armR",-.4F);}
  }
  if(!likho){
   p("body").y+=(float)Math.sin(age*.07)*.1F;
   if(m==BossRules.Move.SWEEP){float a=(float)Math.sin(Math.min(1,t/24)*Math.PI);rx("armR",-1.4F*a);rz("armR",.6F*a);p("body").yRot=-.6F*a;}
   if(e.hurtTime>0){rx("body",-.12F);rx("head",-.15F);}
   if(serpent){rx("neck",(float)Math.sin(age*.045)*.04F);rx("neckUpper",(float)Math.sin(age*.045-.5)*.035F);for(int i=0;i<4;i++)if(p("tail"+i)!=null)p("tail"+i).yRot+=(float)Math.sin(age*.06-i*.6)*.09F;float open=.12F+(float)Math.sin(age*.06)*.025F;
    if(m==BossRules.Move.TRANSITION)open=.15F+Math.min(1,Math.max(0,(t-30)/20))*.8F;
    if(m==BossRules.Move.WING_DIVE){open=t<8?t/8*.9F:t<20?.9F+(float)Math.sin(t*.8)*.2F:t<36?.7F:.15F;rx("body",t>20?.6F:-.2F);rx("legL",-.4F);rx("legR",-.4F);}
    if(m==BossRules.Move.RECOVERY&&e.previousTugarinMove()==BossRules.Move.WING_DIVE&&t<12){float land=1-t/12;open=.12F+.6F*land;rx("body",.25F*land);rx("legL",-.3F*land);rx("legR",-.3F*land);}
    rz("wingL",-open);rz("wingR",open);p("wingL").yRot=-1.35F*(1-Math.min(1,open));p("wingR").yRot=1.35F*(1-Math.min(1,open));
    if(m==BossRules.Move.BREATH){p("jaw").y+=1;rx("jaw",Math.min(.42F,t/18*.42F));rx("head",-.15F);rx("body",.15F);}
    if(m==BossRules.Move.SERPENT_LUNGE){rx("body",.4F);rx("armR",-1.8F);rx("head",.15F);}
    if(m==BossRules.Move.SMOKE_BURST){rx("armL",-1.1F);rx("armR",-1.1F);rz("wingL",-.6F);rz("wingR",.6F);}
   }else if(m==BossRules.Move.TRANSITION){float a=Math.min(1,t/30);p("body").xScale=1-.18F*a;rx("body",-.2F*a);}
  }
  if(higher){p("morEye").visible=true;poseHigher(age,e.morSkill(),e.morElapsed(age-e.tickCount),e.hurtTime,e.deathTime);}
  if(e.deathTime>0){p("root").zRot=Math.min(1,e.deathTime/20F)*1.35F;p("root").y+=Math.min(12,e.deathTime*.6F);}if(m==BossRules.Move.TRANSITION)p("body").xRot+=(float)Math.sin(t*.22)*.08F;
 }
 public void glow(PoseStack pose,VertexConsumer out,WorldBoss e){if(likho?(!higher&&e.move()!=BossRules.Move.GAZE):!serpent||(e.move()!=BossRules.Move.BREATH&&e.move()!=BossRules.Move.TRANSITION))return;pose.pushPose();p("root").translateAndRotate(pose);p("body").translateAndRotate(pose);if(serpent){p("neck").translateAndRotate(pose);p("neckUpper").translateAndRotate(pose);}p("head").translateAndRotate(pose);(likho?p(higher?"morEye":"irisRing"):p("mouthGlow")).render(pose,out,15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();}
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){p("root").render(pose,out,light,overlay,color);}
}
