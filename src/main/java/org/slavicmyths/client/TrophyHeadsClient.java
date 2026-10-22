package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
  Head(TrophyHeads.Kind kind,ModelPart part,String texture){
   this.kind=kind;this.part=part;this.texture=ResourceLocation.fromNamespaceAndPath("slavicmyths",texture);
   part.setPos(0,0,0);part.xRot=part.yRot=part.zRot=0;
   var capture=new Capture();part.render(new PoseStack(),capture,0,0);bounds=capture.bounds();
   scale=Math.min(.94F/(bounds[3]-bounds[0]),Math.min(.94F/(bounds[5]-bounds[2]),(kind.height-.1F)/(bounds[4]-bounds[1])));
  }
  public void render(PoseStack pose,VertexConsumer out,int light,int overlay){
   pose.pushPose();pose.translate(.5,.04,.5);pose.scale(scale,-scale,-scale);
   pose.translate(-(bounds[0]+bounds[3])/2,-bounds[4],-(bounds[2]+bounds[5])/2);
   part.render(pose,out,light,overlay);pose.popPose();
  }
 }
 /** Used by the real renderer and offline native-face exporter; no Minecraft instance needed. */
 public static Head create(TrophyHeads.Kind kind){return switch(kind){
  case TUGARIN->new Head(kind,new WorldBossModel(false,true).trophyHead(),"textures/entity/tugarin_phase2.png");
  case SKIM->new Head(kind,SkimMesh.create().getChild("body").getChild("neck"),"textures/entity/skim_beast.png");
  case VOLKOLAK->{var part=new CursedVolkolakModel().trophyHead();part.zScale=CursedVolkolakRenderer.DEPTH_SCALE;yield new Head(kind,part,"textures/entity/cursed_volkolak.png");}
  case LIKHO->new Head(kind,new WorldBossModel(true).trophyHead(),"textures/entity/likho_one_eyed.png");
  case MOR_LIKHO->new Head(kind,new WorldBossModel(true,false,true).trophyHead(),"textures/entity/mor/likho.png");
  case PODZEMNIK->new Head(kind,UndergroundClient.Model.mesh(3).getChild("body").getChild("head"),"textures/entity/underground/large_podzemnik.png");
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
   var block=(TrophyHeadBlock)tile.getBlockState().getBlock();var h=head(block.kind);pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-tile.getBlockState().getValue(TrophyHeadBlock.FACING).toYRot()));pose.translate(-.5,0,-.5);h.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(h.texture)),light,overlay);pose.popPose();
  }
 }
 public static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {
  public ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
  @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffer,int light,int overlay){if(stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof TrophyHeadBlock block){var h=head(block.kind);h.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(h.texture)),light,overlay);}}
 }
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(TrophyHeads.TILE.get(),Renderer::new);}
 @SubscribeEvent public static void items(RegisterClientExtensionsEvent e){for(var item:TrophyHeads.ITEMS.values())e.registerItem(new IClientItemExtensions(){private ItemRenderer renderer;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ItemRenderer();return renderer;}},item.get());}
}
