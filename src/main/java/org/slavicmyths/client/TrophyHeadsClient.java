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
  private final ModelPart lid,morEye,glow,detail,accent;
  private final float lidBaseY,morEyeScaleX,morEyeScaleY,detailBaseXRot,accentBaseXRot,accentBaseZRot;
  Head(TrophyHeads.Kind kind,ModelPart part,String texture){this(kind,part,texture,null,null,null);}
  Head(TrophyHeads.Kind kind,ModelPart part,String texture,ModelPart lid,ModelPart morEye,ModelPart glow){this(kind,part,texture,lid,morEye,glow,null,null);}
  Head(TrophyHeads.Kind kind,ModelPart part,String texture,ModelPart lid,ModelPart morEye,ModelPart glow,ModelPart detail,ModelPart accent){
   this.kind=kind;this.part=part;this.texture=ResourceLocation.fromNamespaceAndPath("slavicmyths",texture);
   this.lid=lid;this.morEye=morEye;this.glow=glow;this.detail=detail;this.accent=accent;
   lidBaseY=lid==null?0:lid.y;
   morEyeScaleX=morEye==null?1:morEye.xScale;morEyeScaleY=morEye==null?1:morEye.yScale;
   detailBaseXRot=detail==null?0:detail.xRot;
   accentBaseXRot=accent==null?0:accent.xRot;accentBaseZRot=accent==null?0:accent.zRot;
   part.setPos(0,0,0);part.xRot=part.yRot=part.zRot=0;
   var capture=new Capture();part.render(new PoseStack(),capture,0,0);bounds=capture.bounds();
   scale=Math.min(.94F/(bounds[3]-bounds[0]),Math.min(.94F/(bounds[5]-bounds[2]),(kind.height-.1F)/(bounds[4]-bounds[1])));
  }
  public void render(PoseStack pose,VertexConsumer out,int light,int overlay){
   pose.pushPose();pose.translate(.5,.04,.5);pose.scale(scale,-scale,-scale);
   pose.translate(-(bounds[0]+bounds[3])/2,-bounds[4],-(bounds[2]+bounds[5])/2);
   part.render(pose,out,light,overlay);pose.popPose();
  }
  public void pose(float time){
   if(lid!=null)lid.y=lidBaseY+TrophyHeadFxRules.lidOffset(time);
   if(morEye!=null){float pulse=TrophyHeadFxRules.morEyeScale(time);morEye.xScale=morEyeScaleX*pulse;morEye.yScale=morEyeScaleY*pulse;}
   if(detail!=null)detail.xRot=detailBaseXRot+switch(kind){
    case TUGARIN->TrophyHeadFxRules.tugarinJaw(time);
    case SKIM->TrophyHeadFxRules.skimJaw(time);
    case VOLKOLAK->TrophyHeadFxRules.volkolakJaw(time);
    case PODZEMNIK->TrophyHeadFxRules.podzemnikJaw(time);
    default->0F;
   };
   if(accent!=null){
    if(kind==TrophyHeads.Kind.PODZEMNIK)accent.xRot=accentBaseXRot+TrophyHeadFxRules.podzemnikBrow(time);
    else accent.zRot=accentBaseZRot+switch(kind){
     case TUGARIN->TrophyHeadFxRules.tugarinWhisker(time);
     case SKIM->TrophyHeadFxRules.skimMane(time);
     case VOLKOLAK->TrophyHeadFxRules.volkolakEar(time);
     default->0F;
    };
   }
  }
  public void resetPose(){
   if(lid!=null)lid.y=lidBaseY;
   if(morEye!=null){morEye.xScale=morEyeScaleX;morEye.yScale=morEyeScaleY;}
   if(detail!=null)detail.xRot=detailBaseXRot;
   if(accent!=null){accent.xRot=accentBaseXRot;accent.zRot=accentBaseZRot;}
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
  case TUGARIN->{var model=new WorldBossModel(false,true);yield new Head(kind,model.trophyHead(),"textures/entity/tugarin_phase2.png",null,null,null,model.trophyHeadPart("jaw"),model.trophyHeadPart("moustacheL"));}
  case SKIM->{var neck=SkimMesh.create().getChild("body").getChild("neck");yield new Head(kind,neck,"textures/entity/skim_beast.png",null,null,null,neck.getChild("head").getChild("jaw"),neck.getChild("mane_0"));}
  case VOLKOLAK->{var model=new CursedVolkolakModel();var part=model.trophyHead();part.zScale=CursedVolkolakRenderer.DEPTH_SCALE;yield new Head(kind,part,"textures/entity/cursed_volkolak.png",null,null,null,model.trophyHeadPart("jaw"),model.trophyHeadPart("earL"));}
  case LIKHO->{var model=new WorldBossModel(true);yield new Head(kind,model.trophyHead(),"textures/entity/likho_one_eyed.png",model.trophyHeadPart("upperLid"),null,model.trophyHeadPart("irisRing"));}
  case MOR_LIKHO->{var model=new WorldBossModel(true,false,true);yield new Head(kind,model.trophyHead(),"textures/entity/mor/likho.png",null,model.trophyHeadPart("morEye"),model.trophyHeadPart("morEye"));}
  case PODZEMNIK->{var head=UndergroundClient.Model.mesh(3).getChild("body").getChild("head");yield new Head(kind,head,"textures/entity/underground/large_podzemnik.png",null,null,null,head.getChild("jaw"),head.getChild("brows"));}
 };}
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
    if(animated)h.pose(time);
    h.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(h.texture)),light,overlay);
    if(animated&&(block.kind==TrophyHeads.Kind.MOR_LIKHO||TrophyHeadFxRules.likhoGlow(time)))
     h.renderGlow(pose,buffer.getBuffer(RenderType.eyes(h.texture)),overlay);
   }finally{
    if(animated)h.resetPose();
    pose.popPose();
   }
   if(animated)particle(tile,block.kind);
  }
  private static void particle(TrophyHeadBlock.Tile tile,TrophyHeads.Kind kind){
   if(!(tile.getLevel() instanceof ClientLevel world)||Minecraft.getInstance().isPaused()||Minecraft.getInstance().player==null)return;
   BlockPos pos=tile.getBlockPos();
   if(Minecraft.getInstance().player.distanceToSqr(pos.getX()+.5,pos.getY()+1.2,pos.getZ()+.5)>24*24)return;
   long now=world.getGameTime();
   int period=switch(kind){
    case TUGARIN->72;case SKIM->86;case VOLKOLAK->78;case PODZEMNIK->92;
    case LIKHO->52;case MOR_LIKHO->32;
   };
   if(!TrophyHeadFxRules.particleDue(now,tile.clientLastFxTick,period))return;
   tile.clientLastFxTick=now;
   Direction facing=tile.getBlockState().getValue(TrophyHeadBlock.FACING);
   double dx=(world.getRandom().nextDouble()-.5)*.17,dz=(world.getRandom().nextDouble()-.5)*.17;
   double height=switch(kind){
    case TUGARIN->.34;case SKIM->.63;case VOLKOLAK->.7;case PODZEMNIK->.77;
    case LIKHO,MOR_LIKHO->1.25;
   };
   var particle=switch(kind){
    case TUGARIN->ParticleTypes.DRAGON_BREATH;
    case SKIM->ParticleTypes.CLOUD;
    case VOLKOLAK->ParticleTypes.SMOKE;
    case PODZEMNIK->ParticleTypes.ASH;
    case LIKHO->ParticleTypes.SOUL;
    case MOR_LIKHO->ParticleTypes.CRIMSON_SPORE;
   };
   world.addParticle(particle,pos.getX()+.5+facing.getStepX()*.28+dx,
    pos.getY()+height+world.getRandom().nextDouble()*.12,
    pos.getZ()+.5+facing.getStepZ()*.28+dz,0,.006,0);
  }
 }
 public static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {
  public ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
  @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffer,int light,int overlay){if(stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof TrophyHeadBlock block){var h=head(block.kind);h.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(h.texture)),light,overlay);}}
 }
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(TrophyHeads.TILE.get(),Renderer::new);}
 @SubscribeEvent public static void items(RegisterClientExtensionsEvent e){for(var item:TrophyHeads.ITEMS.values())e.registerItem(new IClientItemExtensions(){private ItemRenderer renderer;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ItemRenderer();return renderer;}},item.get());}
}
