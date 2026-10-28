package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.*;
import org.slavicmyths.trophy.*;

/** Independent instances of the actual mob heads, with unchanged native cubes and UV. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class TrophyHeadsClient {
 public static final class Head {
  public final ModelPart part;public final ResourceLocation texture;public final TrophyHeads.Kind kind;
  public final float[] bounds;public final float scale;
  private final ModelPart lid,morEye,glow,detail,accent,pupilLeft,pupilRight;
  private ModelPart earLeft,earRight;
  private float earLeftBaseZ,earRightBaseZ;
  private final float lidBaseZ,lidScaleY,lidBaseX,lidScaleX;
  private final float lidBaseY,morEyeScaleX,morEyeScaleY,detailBaseXRot,detailBaseY,accentBaseXRot,accentBaseZRot,accentBaseY,glowBaseX,glowBaseY,pupilLeftBaseX,pupilLeftBaseY,pupilRightBaseX,pupilRightBaseY;
  Head(TrophyHeads.Kind kind,ModelPart part,String texture){this(kind,part,texture,null,null,null);}
  Head(TrophyHeads.Kind kind,ModelPart part,String texture,ModelPart lid,ModelPart morEye,ModelPart glow){this(kind,part,texture,lid,morEye,glow,null,null);}
  Head(TrophyHeads.Kind kind,ModelPart part,String texture,ModelPart lid,ModelPart morEye,ModelPart glow,ModelPart detail,ModelPart accent){this(kind,part,texture,lid,morEye,glow,detail,accent,null,null);}
  Head(TrophyHeads.Kind kind,ModelPart part,String texture,ModelPart lid,ModelPart morEye,ModelPart glow,ModelPart detail,ModelPart accent,ModelPart pupilLeft,ModelPart pupilRight){
   this.kind=kind;this.part=part;this.texture=ResourceLocation.fromNamespaceAndPath("slavicmyths",texture);
   this.lid=lid;this.morEye=morEye;this.glow=glow;this.detail=detail;this.accent=accent;this.pupilLeft=pupilLeft;this.pupilRight=pupilRight;
   pupilLeftBaseX=pupilLeft==null?0:pupilLeft.x;pupilLeftBaseY=pupilLeft==null?0:pupilLeft.y;
   pupilRightBaseX=pupilRight==null?0:pupilRight.x;pupilRightBaseY=pupilRight==null?0:pupilRight.y;
   lidBaseY=lid==null?0:lid.y;glowBaseX=glow==null?0:glow.x;glowBaseY=glow==null?0:glow.y;
   lidBaseZ=lid==null?0:lid.z;lidScaleY=lid==null?1:lid.yScale;lidBaseX=lid==null?0:lid.x;lidScaleX=lid==null?1:lid.xScale;
   morEyeScaleX=morEye==null?1:morEye.xScale;morEyeScaleY=morEye==null?1:morEye.yScale;
   detailBaseXRot=detail==null?0:detail.xRot;
   detailBaseY=detail==null?0:detail.y;
   accentBaseXRot=accent==null?0:accent.xRot;accentBaseZRot=accent==null?0:accent.zRot;accentBaseY=accent==null?0:accent.y;
   part.setPos(0,0,0);part.xRot=part.yRot=part.zRot=0;
   var capture=new Capture();part.render(new PoseStack(),capture,0,0);bounds=capture.bounds();
   if(kind==TrophyHeads.Kind.POSTEN||kind==TrophyHeads.Kind.GROVE_GUARDIAN){
    // Fit the full horizontal sweep, not just a neutral frontal bounding box.
    float radius=0;for(var vertex:capture.vertices)radius=Math.max(radius,(float)Math.hypot(vertex[0],vertex[2]));
    scale=Math.min(.96F/(2*radius+(kind==TrophyHeads.Kind.GROVE_GUARDIAN?.08F:0)),.9F/(bounds[4]-bounds[1]));
   }else scale=Math.min(.94F/(bounds[3]-bounds[0]),Math.min(.94F/(bounds[5]-bounds[2]),(kind.height-.1F)/(bounds[4]-bounds[1])));
  }
  public void render(PoseStack pose,VertexConsumer out,int light,int overlay){render(pose,out,light,overlay,0xffffffff);}
  public void render(PoseStack pose,VertexConsumer out,int light,int overlay,int color){
   pose.pushPose();pose.translate(.5,.04,.5);pose.scale(scale,-scale,-scale);
   pose.translate(-(bounds[0]+bounds[3])/2,-bounds[4],-(bounds[2]+bounds[5])/2);
   part.render(pose,out,light,overlay,color);pose.popPose();
  }
  public void pose(float time){pose(time,0,0);}
  private Head ears(ModelPart left,ModelPart right){earLeft=left;earRight=right;earLeftBaseZ=left.zRot;earRightBaseZ=right.zRot;return this;}
  public void pose(float time,float lookX,float lookY){
   if(kind==TrophyHeads.Kind.POSTEN){part.getAllParts().forEach(ModelPart::resetPose);part.setPos(0,0,0);return;}
   if(kind==TrophyHeads.Kind.GROVE_GUARDIAN){guardianPose(time,0);return;}
   if(kind==TrophyHeads.Kind.ZHARENITSA){part.getAllParts().forEach(ModelPart::resetPose);part.setPos(0,0,0);for(String name:kind==TrophyHeads.Kind.GROVE_GUARDIAN?List.of("twig_left","twig_right"):List.of("hair0","hair1","hair2","hair3","hair4","ash_tress0","ash_tress1"))if(part.hasChild(name)){var child=part.getChild(name);child.zRot+=(float)Math.sin(time*.035+child.y)*.025F;}return;}
   if(kind==TrophyHeads.Kind.MAGMA_LORD){magmaPose(time,0);return;}
   float blink=TrophyHeadFxRules.lidOffset(time);
   if(lid!=null){float closure=blink/1.8F;lid.x=lidBaseX-.54F*closure;lid.xScale=lidScaleX*(1+.6F*closure);lid.y=lidBaseY-(kind==TrophyHeads.Kind.MOR_LIKHO?.25F:0)*closure;lid.yScale=lidScaleY*(1+(kind==TrophyHeads.Kind.MOR_LIKHO?7:5)*closure);lid.z=lidBaseZ-(kind==TrophyHeads.Kind.MOR_LIKHO?1.1F:.65F)*blink/1.8F;}
   if(glow!=null&&(kind==TrophyHeads.Kind.LIKHO||kind==TrophyHeads.Kind.MOR_LIKHO)){
    glow.x=glowBaseX+net.minecraft.util.Mth.clamp(lookX,-1.1F,1.1F)*.28F;
    glow.y=glowBaseY+net.minecraft.util.Mth.clamp(lookY,-.75F,.75F)*.20F;
   }
   if(pupilLeft!=null){pupilLeft.x=pupilLeftBaseX+net.minecraft.util.Mth.clamp(lookX,-1.1F,1.1F)*.28F;pupilLeft.y=pupilLeftBaseY+net.minecraft.util.Mth.clamp(lookY,-.75F,.75F)*.20F;}
   if(pupilRight!=null){pupilRight.x=pupilRightBaseX+net.minecraft.util.Mth.clamp(lookX,-1.1F,1.1F)*.28F;pupilRight.y=pupilRightBaseY+net.minecraft.util.Mth.clamp(lookY,-.75F,.75F)*.20F;}
   if(morEye!=null){float pulse=TrophyHeadFxRules.morEyeScale(time);morEye.xScale=morEyeScaleX*pulse;morEye.yScale=morEyeScaleY*pulse;}
   if(earLeft!=null){float twitch=kind==TrophyHeads.Kind.VOLKOLAK?TrophyHeadFxRules.volkolakEar(time):.22F*(float)Math.sin(time*.15);earLeft.zRot=earLeftBaseZ+twitch;earRight.zRot=earRightBaseZ-twitch;}
   if(detail!=null)detail.xRot=detailBaseXRot+switch(kind){
    case TUGARIN->TrophyHeadFxRules.tugarinJaw(time);
    case SKIM->TrophyHeadFxRules.skimJaw(time);
    case VOLKOLAK->TrophyHeadFxRules.volkolakJaw(time);
    case PODZEMNIK->TrophyHeadFxRules.podzemnikJaw(time);
    default->0F;
   };
   // These isolated jaw subtrees were authored around the mob origin, not
   // a trophy hinge. Retract the jaw as it opens so it clears the support.
   // Preserve the requested angular motion and the neutral native placement.
   if(detail!=null){
    float travel=switch(kind){case TUGARIN->18F;case VOLKOLAK->4F;case PODZEMNIK->6F;default->0F;};
    detail.y=detailBaseY-travel*(float)Math.sin(detail.xRot-detailBaseXRot);
   }
   if(accent!=null){
    if(kind==TrophyHeads.Kind.LIKHO)accent.y=accentBaseY-.32F*blink;
    if(kind==TrophyHeads.Kind.PODZEMNIK)accent.xRot=accentBaseXRot+TrophyHeadFxRules.podzemnikBrow(time);
    else accent.zRot=accentBaseZRot+switch(kind){
     case TUGARIN->TrophyHeadFxRules.tugarinWhisker(time);
     case SKIM->TrophyHeadFxRules.skimMane(time);
     case VOLKOLAK->TrophyHeadFxRules.volkolakEar(time);
     default->0F;
    };
   }
  }
  public void guardianPose(float time,float awake){
   part.getAllParts().forEach(ModelPart::resetPose);part.setPos(0,0,0);part.xRot=part.yRot=part.zRot=0;
   for(String n:List.of("twig_left","twig_right"))if(part.hasChild(n)){
    var branch=part.getChild(n);float side=n.endsWith("left")?-1:1;
    branch.zRot+=side*(float)Math.sin(time*.045)*( .025F+awake*.085F);
   }
   var jaw=part.getChild("jaw");jaw.xRot=.13F*awake+(.025F+.035F*awake)*(float)Math.sin(time*.055);jaw.y-=.5F*awake;
  }
  public void magmaPose(float time,float open){
   part.getAllParts().forEach(ModelPart::resetPose);part.setPos(0,0,0);part.xRot=part.yRot=part.zRot=0;
   if(detail!=null){detail.xRot=.28F*open;detail.y=detailBaseY-2F*open;}
   for(int i=0;i<2;i++){var shard=part.getChild("shard_head"+i);shard.y=shard.getInitialPose().y+(float)Math.sin(time*.06+i)*.12F;shard.yRot=(float)Math.sin(time*.03+i)*.1F;}
  }
  public void resetPose(){
   if(kind==TrophyHeads.Kind.GROVE_GUARDIAN||kind==TrophyHeads.Kind.ZHARENITSA||kind==TrophyHeads.Kind.POSTEN||kind==TrophyHeads.Kind.MAGMA_LORD){part.getAllParts().forEach(ModelPart::resetPose);part.setPos(0,0,0);part.xRot=part.yRot=part.zRot=0;return;}
   if(lid!=null){lid.x=lidBaseX;lid.xScale=lidScaleX;lid.y=lidBaseY;lid.z=lidBaseZ;lid.yScale=lidScaleY;}
   if(earLeft!=null){earLeft.zRot=earLeftBaseZ;earRight.zRot=earRightBaseZ;}
   if(glow!=null){glow.x=glowBaseX;glow.y=glowBaseY;}
   if(pupilLeft!=null){pupilLeft.x=pupilLeftBaseX;pupilLeft.y=pupilLeftBaseY;}
   if(pupilRight!=null){pupilRight.x=pupilRightBaseX;pupilRight.y=pupilRightBaseY;}
   if(morEye!=null){morEye.xScale=morEyeScaleX;morEye.yScale=morEyeScaleY;}
   if(detail!=null){detail.xRot=detailBaseXRot;detail.y=detailBaseY;}
   if(accent!=null){accent.xRot=accentBaseXRot;accent.zRot=accentBaseZRot;accent.y=accentBaseY;}
  }
  public void renderGlow(PoseStack pose,VertexConsumer out,int overlay){
   if(glow==null)return;
   pose.pushPose();pose.translate(.5,.04,.5);pose.scale(scale,-scale,-scale);
   pose.translate(-(bounds[0]+bounds[3])/2,-bounds[4],-(bounds[2]+bounds[5])/2);
   glow.render(pose,out,0xF000F0,overlay);pose.popPose();
  }
 }
 /** Used by the real renderer and offline native-face exporter; no Minecraft instance needed. */
 public static Head create(TrophyHeads.Kind kind){return switch(kind){
  case TUGARIN->{var model=new WorldBossModel(false,true);yield new Head(kind,model.trophyHead(),"textures/entity/tugarin_phase2.png",null,null,null,model.trophyHeadPart("jaw"),model.trophyHeadPart("moustacheL"),model.trophyHeadPart("pupilL"),model.trophyHeadPart("pupilR"));}
  case SKIM->{var neck=TrophySkimMesh.create();var face=neck.getChild("head");yield new Head(kind,neck,"textures/entity/skim_beast.png",null,null,null,face.getChild("jaw"),neck.getChild("mane_0"),face.getChild("eyeL"),face.getChild("eyeR")).ears(face.getChild("earL"),face.getChild("earR"));}
  case VOLKOLAK->{var model=new CursedVolkolakModel();var part=model.trophyHead();part.zScale=CursedVolkolakRenderer.DEPTH_SCALE;yield new Head(kind,part,"textures/entity/cursed_volkolak.png",null,null,null,model.trophyHeadPart("jaw"),null).ears(model.trophyHeadPart("earL"),model.trophyHeadPart("earR"));}
  case LIKHO->{var model=new WorldBossModel(true);yield new Head(kind,model.trophyHead(),"textures/entity/likho_one_eyed.png",model.trophyHeadPart("upperLid"),null,model.trophyHeadPart("irisRing"),null,model.trophyHeadPart("lowerLid"),model.trophyHeadPart("iris"),model.trophyHeadPart("pupil"));}
  case MOR_LIKHO->{var model=new WorldBossModel(true,false,true);yield new Head(kind,model.trophyHead(),"textures/entity/mor/likho.png",model.trophyHeadPart("upperLid"),model.trophyHeadPart("morEye"),model.trophyHeadPart("morEye"));}
  case GROVE_GUARDIAN->{var rig=org.slavicmyths.navya.client.NavyaMesh.create(org.slavicmyths.navya.NavyaContent.Kind.GUARDIAN);yield new Head(kind,rig.nodes().get("face"),"textures/entity/grove_guardian.png");}
  case ZHARENITSA->{var mesh=new org.slavicmyths.losthalls.client.HallsMesh(org.slavicmyths.losthalls.client.HallsMesh.read("zharenitsa_trophy"));yield new Head(kind,mesh.root.getChild("head"),"textures/entity/lost_halls/zharenitsa_trophy.png");}
  case POSTEN->{var mesh=new org.slavicmyths.losthalls.client.HallsMesh("posten_trophy");yield new Head(kind,mesh.parts.get("head"),"textures/entity/lost_halls/posten_trophy.png");}
  case MAGMA_LORD->{var model=org.slavicmyths.burningislands.client.IslandsModel.bundled(org.slavicmyths.burningislands.IslandsContent.Kind.MAGMA_LORD);yield magmaHead(model.part("head"));}
  case PODZEMNIK->{var head=UndergroundClient.Model.mesh(3).getChild("body").getChild("head");yield new Head(kind,head,"textures/entity/underground/large_podzemnik.png",null,null,null,head.getChild("jaw"),head.getChild("brows"));}
 };}
 public static Head magmaHead(ModelPart head){return new Head(TrophyHeads.Kind.MAGMA_LORD,head,"textures/entity/burning_islands/magma_lord.png",null,null,null,head.getChild("jaw"),head.getChild("shard_head0"));}
 private static final Map<TrophyHeads.Kind,Head> HEADS=new EnumMap<>(TrophyHeads.Kind.class);
 private static Head head(TrophyHeads.Kind kind){return HEADS.computeIfAbsent(kind,TrophyHeadsClient::create);}
 public static final class Capture implements VertexConsumer {
  public final List<float[]> vertices=new ArrayList<>();private float[] last;
  public VertexConsumer addVertex(float x,float y,float z){last=new float[]{x,y,z,0,0};vertices.add(last);return this;}
  public VertexConsumer setColor(int r,int g,int b,int a){return this;}
  public VertexConsumer setUv(float u,float v){last[3]=u;last[4]=v;return this;}
  public VertexConsumer setUv1(int u,int v){return this;}
  public VertexConsumer setUv2(int u,int v){return this;}
  public VertexConsumer setNormal(float x,float y,float z){return this;}
  public float[] bounds(){float[] b={Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};for(var v:vertices)for(int i=0;i<3;i++){b[i]=Math.min(b[i],v[i]);b[i+3]=Math.max(b[i+3],v[i]);}return b;}
 }
 /** Model +X is opposite the facing-relative world side after placement rotation. */
 static float eyeLook(double side,double forward){return (float)net.minecraft.util.Mth.clamp(-side/Math.max(2,forward)*.9,-1.1,1.1);}
 public static final class Renderer implements BlockEntityRenderer<TrophyHeadBlock.Tile> {
  public Renderer(BlockEntityRendererProvider.Context c){}
  public void render(TrophyHeadBlock.Tile tile,float partial,PoseStack pose,MultiBufferSource buffer,int light,int overlay){
   var block=(TrophyHeadBlock)tile.getBlockState().getBlock();var h=head(block.kind);
   boolean animated=true;
   float time=tile.getLevel()==null?0:tile.getLevel().getGameTime()+partial+Math.floorMod(tile.getBlockPos().asLong(),79);
   pose.pushPose();
   try{
    pose.translate(.5,0,.5);
    pose.mulPose(Axis.YP.rotationDegrees(-tile.getBlockState().getValue(TrophyHeadBlock.FACING).toYRot()));
    pose.translate(-.5,0,-.5);
    float gazeX=0,gazeY=0;
    if(animated){
     var player=Minecraft.getInstance().player;
     if(player!=null&&(block.kind==TrophyHeads.Kind.LIKHO||block.kind==TrophyHeads.Kind.MOR_LIKHO||block.kind==TrophyHeads.Kind.TUGARIN||block.kind==TrophyHeads.Kind.SKIM||block.kind==TrophyHeads.Kind.POSTEN)){
      var f=tile.getBlockState().getValue(TrophyHeadBlock.FACING);
      double dx=player.getX()-(tile.getBlockPos().getX()+.5),dz=player.getZ()-(tile.getBlockPos().getZ()+.5);
      double forward=dx*f.getStepX()+dz*f.getStepZ();
      if(forward>0&&dx*dx+dz*dz<24*24){
       double side=-dx*f.getStepZ()+dz*f.getStepX();
       gazeX=eyeLook(side,forward);
       gazeY=(float)net.minecraft.util.Mth.clamp((player.getEyeY()-(tile.getBlockPos().getY()+1.15))*-.32,-.75,.75);
      }
     }
     h.pose(time,gazeX,gazeY);
    }
    if((block.kind==TrophyHeads.Kind.MAGMA_LORD||block.kind==TrophyHeads.Kind.POSTEN||block.kind==TrophyHeads.Kind.GROVE_GUARDIAN)&&tile.getLevel()!=null){
     long tick=tile.getLevel().getGameTime();
     if(tile.magmaPoseTick!=tick){
      boolean near=tile.getLevel().getNearestPlayer(tile.getBlockPos().getX()+.5,tile.getBlockPos().getY()+.5,tile.getBlockPos().getZ()+.5,tile.magmaAwake?8:6,false)!=null;
      if(near&&!tile.magmaAwake&&block.kind==TrophyHeads.Kind.GROVE_GUARDIAN)tile.guardianBurst=6;tile.magmaAwake=near;tile.maskPrevious=tile.maskAngle;if(block.kind==TrophyHeads.Kind.POSTEN&&near){int phase=tile.maskCycle++%45;if(phase>=8&&phase<24){float a=(phase-8)/16F,b=(phase-7)/16F;tile.maskAngle+=(b*b*(3-2*b)-a*a*(3-2*a))*(float)Math.PI/2;}}
      tile.magmaOpenness=net.minecraft.util.Mth.clamp(tile.magmaOpenness+(near?.0625F:-.033333F),0,1);tile.magmaPoseTick=tick;
     }
     if(block.kind==TrophyHeads.Kind.POSTEN){h.part.getChild("mask_ring").yRot=net.minecraft.util.Mth.lerp(partial,tile.maskPrevious,tile.maskAngle);h.part.yRot=net.minecraft.util.Mth.clamp(gazeX*.3F,-.35F,.35F);}else if(block.kind==TrophyHeads.Kind.GROVE_GUARDIAN)h.guardianPose(time,tile.magmaOpenness);else h.magmaPose(time,tile.magmaOpenness);
    }
    h.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(h.texture)),light,overlay);
    if(block.kind.ordinal()>=TrophyHeads.Kind.GROVE_GUARDIAN.ordinal())h.render(pose,buffer.getBuffer(RenderType.eyes(ResourceLocation.fromNamespaceAndPath("slavicmyths",h.texture.getPath().replace(".png","_glow.png")))),15728880,overlay,((int)(175+45*Math.sin(time*.065))<<24)|0xffffff);
    if(block.kind==TrophyHeads.Kind.MAGMA_LORD)h.render(pose,buffer.getBuffer(RenderType.eyes(org.slavicmyths.burningislands.client.IslandsClient.texture(org.slavicmyths.burningislands.IslandsContent.Kind.MAGMA_LORD,true))),15728880,overlay,((int)(120+80*tile.magmaOpenness+25*Math.sin(time*.065))<<24)|0xffffff);
    if(animated&&(block.kind==TrophyHeads.Kind.MOR_LIKHO||TrophyHeadFxRules.likhoGlow(time)))
     h.renderGlow(pose,buffer.getBuffer(RenderType.eyes(h.texture)),overlay);
   }finally{
    if(animated)h.resetPose();
    pose.popPose();
   }
   if(animated)particle(tile,block.kind);
  }
  private static long particleBudgetTick=Long.MIN_VALUE;private static int particleBudget;
  private static void particle(TrophyHeadBlock.Tile tile,TrophyHeads.Kind kind){
   if(!(tile.getLevel() instanceof ClientLevel world)||Minecraft.getInstance().isPaused()||Minecraft.getInstance().player==null)return;
   BlockPos pos=tile.getBlockPos();
   if(Minecraft.getInstance().player.distanceToSqr(pos.getX()+.5,pos.getY()+1.2,pos.getZ()+.5)>24*24)return;
   long now=world.getGameTime();
   if(particleBudgetTick!=now){particleBudgetTick=now;particleBudget=24;}
   if(particleBudget==0)return;
   if(kind==TrophyHeads.Kind.GROVE_GUARDIAN&&tile.guardianBurst>0){
    int count=Math.min(tile.guardianBurst,Math.min(particleBudget,3));tile.guardianBurst-=count;particleBudget-=count;
    for(int i=0;i<count;i++)world.addParticle(ParticleTypes.CRIMSON_SPORE,pos.getX()+.2+world.random.nextDouble()*.6,pos.getY()+.5+world.random.nextDouble()*.3,pos.getZ()+.2+world.random.nextDouble()*.6,0,.015,0);
   }
   if(particleBudget<=0)return;
   int period=switch(kind){
    case GROVE_GUARDIAN->20;case POSTEN,ZHARENITSA->60;case MAGMA_LORD->200;case TUGARIN->9;case SKIM->10;case VOLKOLAK->9;case PODZEMNIK->11;
    case LIKHO->8;case MOR_LIKHO->7;
   };
   if(!TrophyHeadFxRules.particleDue(now,tile.clientLastFxTick,period))return;
   tile.clientLastFxTick=now;
   Direction facing=tile.getBlockState().getValue(TrophyHeadBlock.FACING);
   double dx=(world.getRandom().nextDouble()-.5)*.17,dz=(world.getRandom().nextDouble()-.5)*.17;
   double height=switch(kind){
    case POSTEN,GROVE_GUARDIAN,ZHARENITSA->.65;case MAGMA_LORD->.65;case TUGARIN->.37;case SKIM->.66;case VOLKOLAK->.77;case PODZEMNIK->.95;
    case LIKHO,MOR_LIKHO->1.25;
   };
   var particle=switch(kind){
    case POSTEN->ParticleTypes.SOUL;case GROVE_GUARDIAN->ParticleTypes.ASH;case ZHARENITSA->ParticleTypes.SMALL_FLAME;case MAGMA_LORD->ParticleTypes.SMALL_FLAME;case TUGARIN->ParticleTypes.DRAGON_BREATH;
    case SKIM->ParticleTypes.CLOUD;
    case VOLKOLAK->ParticleTypes.SMOKE;
    case PODZEMNIK->ParticleTypes.ASH;
    case LIKHO->ParticleTypes.SOUL;
    case MOR_LIKHO->ParticleTypes.CRIMSON_SPORE;
   };
   int count=Math.min(particleBudget,kind==TrophyHeads.Kind.MAGMA_LORD||kind==TrophyHeads.Kind.POSTEN||kind==TrophyHeads.Kind.GROVE_GUARDIAN||kind==TrophyHeads.Kind.ZHARENITSA?1:5);particleBudget-=count;
   for(int i=0;i<count;i++)world.addParticle(particle,pos.getX()+.5+facing.getStepX()*.28+dx+(world.getRandom().nextDouble()-.5)*.14,
    pos.getY()+height+world.getRandom().nextDouble()*.25,
    pos.getZ()+.5+facing.getStepZ()*.28+dz+(world.getRandom().nextDouble()-.5)*.14,0,.018,0);
  }
 }
 public static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {
  public ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
  @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffer,int light,int overlay){if(stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof TrophyHeadBlock block){var h=head(block.kind);h.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(h.texture)),light,overlay);}}
 }
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(TrophyHeads.TILE.get(),Renderer::new);}
 @SubscribeEvent public static void items(RegisterClientExtensionsEvent e){for(var item:TrophyHeads.ITEMS.values())e.registerItem(new IClientItemExtensions(){private ItemRenderer renderer;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ItemRenderer();return renderer;}},item.get());}
}
