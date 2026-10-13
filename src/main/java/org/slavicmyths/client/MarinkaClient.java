package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.marinka.*;
/** Shared renderer for all three witches; never reads server-only original pointer. */
public final class MarinkaClient {
 private static ResourceLocation texture(String name){return MarinkaContent.id("textures/entity/"+name+".png");}
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(MarinkaContent.DOBRYNYA.get(),HeroRenderer::new);e.registerEntityRenderer(MarinkaContent.MARINKA.get(),WitchRenderer::new);e.registerEntityRenderer(MarinkaContent.TUR.get(),TurRenderer::new);e.registerEntityRenderer(MarinkaContent.ANCHOR.get(),AnchorRenderer::new);e.registerEntityRenderer(MarinkaContent.BOLT.get(),BoltRenderer::new);e.registerEntityRenderer(MarinkaContent.RUNE.get(),RuneRenderer::new);}
 }
 
 public static final class HeroRenderer extends MobRenderer<DobrynyaEntity,WitchModel<DobrynyaEntity>> {
  private final WitchModel<DobrynyaEntity> human=new WitchModel<>(DobrynyaMesh.create(),0),bull=new WitchModel<>(AurochsMesh.create(),2);
  public HeroRenderer(EntityRendererProvider.Context c){super(c,new WitchModel<>(DobrynyaMesh.create(),0),.55F);}
  @Override public ResourceLocation getTextureLocation(DobrynyaEntity e){return texture(e.bull()?"dobrynya_aurochs":"dobrynya_nikitich");}
  @Override public void render(DobrynyaEntity e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){model=e.bull()?bull:human;super.render(e,yaw,partial,p,b,light);if(e.action()==WitchAction.WARD||e.action()==WitchAction.UNRAVEL)ring(p,b,0,1.2,0,.95,0xCAE9FF,.6F,e.age(partial)*4,3);}
  @Override protected boolean shouldShowName(DobrynyaEntity e){return false;}@Override protected float getFlipDegrees(DobrynyaEntity e){return 0;}
 }
 public static final class WitchRenderer extends MobRenderer<MarinkaEntity,WitchModel<MarinkaEntity>> {
  private final WitchModel<MarinkaEntity> human=new WitchModel<>(MarinkaMesh.create(),1),bird=new WitchModel<>(MagpieMesh.create(),3);
  public WitchRenderer(EntityRendererProvider.Context c){super(c,new WitchModel<>(MarinkaMesh.create(),1),.38F);}
  @Override public ResourceLocation getTextureLocation(MarinkaEntity e){return texture(e.bird()?"marinka_magpie":"marinka");}
  @Override protected boolean shouldShowName(MarinkaEntity e){return false;}@Override protected float getFlipDegrees(MarinkaEntity e){return 0;}
  @Override public void render(MarinkaEntity e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){model=e.bird()?bird:human;super.render(e,yaw,partial,p,buffers,light);if(e.veil())for(int i=0;i<3;i++)ring(p,buffers,0,.4+i*.6,0,.85,0xCC5D73,.45F,e.age(partial)*2,2);
   if(e.action()==WitchAction.FIRE){double angle=Math.toRadians(e.getYRot());for(int i=1;i<=8;i++){double x=-Math.sin(angle)*i,z=Math.cos(angle)*i;double y=ground(e,x,z);if(Double.isFinite(y))quad(p,buffers,x-.28,y,z-.28,x+.28,z+.28,0xF85B59,.65F);}}
   if(e.action()==WitchAction.BLINK||e.action()==WitchAction.BIRD_CHANGE){var dest=e.arrival();double x=dest.getX()+.5-e.getX(),z=dest.getZ()+.5-e.getZ(),y=ground(e,x,z);if(Double.isFinite(y))ring(p,buffers,x,y,z,1.2,0xB52A47,.75F,0,4);}
  }
 }
 public static final class TurRenderer extends MobRenderer<TurMinion,WitchModel<TurMinion>> {public TurRenderer(EntityRendererProvider.Context c){super(c,new WitchModel<>(AurochsMesh.create(),2),.6F);}@Override public ResourceLocation getTextureLocation(TurMinion e){return texture("marinka_tur_minion");}@Override protected boolean shouldShowName(TurMinion e){return false;}}
 public static final class AnchorRenderer extends MobRenderer<HexAnchor,WitchModel<HexAnchor>> {public AnchorRenderer(EntityRendererProvider.Context c){super(c,new WitchModel<>(HexFocusMesh.create(),4),.4F);}@Override public ResourceLocation getTextureLocation(HexAnchor e){return texture("marinka_hex_focus");}@Override protected boolean shouldShowName(HexAnchor e){return false;}@Override public void render(HexAnchor e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){super.render(e,yaw,partial,p,b,light);for(int i=e.hits();i<3;i++)ring(p,b,0,.5+i*.4,0,.7,0xE4C67B,.8F,e.tickCount,3);}}
 public static final class BoltRenderer extends EntityRenderer<WitchBolt> {
  private final ModelPart shape;
  public BoltRenderer(EntityRendererProvider.Context c){super(c);var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("bolt",CubeListBuilder.create().texOffs(0,0).addBox(-2,-2,-2,4,4,4),PartPose.ZERO);shape=LayerDefinition.create(mesh,64,64).bakeRoot();}
  @Override public ResourceLocation getTextureLocation(WitchBolt e){return texture("marinka_magic");}@Override public void render(WitchBolt e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.mulPose(com.mojang.math.Axis.YP.rotationDegrees((e.tickCount+partial)*9));shape.render(p,b.getBuffer(RenderType.entityTranslucent(getTextureLocation(e))),0xF000F0,OverlayTexture.NO_OVERLAY,0xFFF85B59);p.popPose();}
 }
 public static final class RuneRenderer extends EntityRenderer<WitchRune> {
  public RuneRenderer(EntityRendererProvider.Context c){super(c);}@Override public ResourceLocation getTextureLocation(WitchRune e){return texture("marinka_magic");}
  @Override public void render(WitchRune e,float yaw,float partial,PoseStack p,MultiBufferSource b,int light){if(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(e.position())>96*96)return;int color=e.kind()==0?0xCC8844:e.kind()==1?0xF85B59:0xB52A47;quad(p,b,-1.25,.025,-1.25,1.25,1.25,color,.3F);ring(p,b,0,.03,0,1.15,color,.85F,0,e.kind()==0?4:3);}
 }
 private static double ground(Entity e,double x,double z){var pos=BlockPos.containing(e.getX()+x,e.getY(),e.getZ()+z);if(!e.level().hasChunkAt(pos))return Double.NaN;for(int y=2;y>=-3;y--){var floor=pos.offset(0,y-1,0);if(e.level().getBlockState(floor).isCollisionShapeFullBlock(e.level(),floor)&&e.level().getBlockState(floor.above()).getCollisionShape(e.level(),floor.above()).isEmpty())return floor.getY()+1.025-e.getY();}return Double.NaN;}
 private static void quad(PoseStack p,MultiBufferSource b,double x1,double y,double z1,double x2,double z2,int color,float alpha){var v=b.getBuffer(RenderType.entityTranslucent(texture("marinka_magic")));double[][] q={{x1,z1},{x2,z1},{x2,z2},{x1,z2}};for(int i=0;i<4;i++)v.addVertex(p.last().pose(),(float)q[i][0],(float)y,(float)q[i][1]).setColor((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,alpha).setUv(i==0||i==3?0:1,i<2?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p.last(),0,1,0);}
 private static void ring(PoseStack p,MultiBufferSource b,double x,double y,double z,double r,int color,float alpha,float rotation,int pattern){for(int i=0;i<36;i++){if(i%pattern==pattern-1)continue;double a=Math.toRadians(rotation)+i*Math.PI/18;double xx=x+Math.cos(a)*r,zz=z+Math.sin(a)*r;quad(p,b,xx-.055,y,zz-.055,xx+.055,zz+.055,color,alpha);}}
}
