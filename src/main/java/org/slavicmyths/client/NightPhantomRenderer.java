package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
/** Render borrowed vanilla meshes, without registering actors in any entity list. */
public final class NightPhantomRenderer {
 private final ZombieModel<Zombie> zombie;
 private final SkeletonModel<Skeleton> skeleton;
 public NightPhantomRenderer(EntityRendererProvider.Context c){zombie=new ZombieModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.ZOMBIE));skeleton=new SkeletonModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SKELETON));}
 public void render(LivingEntity actor,PoseStack pose,MultiBufferSource out,int light,float partial){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-actor.getYRot()));pose.scale(-1,-1,1);pose.translate(Math.sin((actor.tickCount+partial)*.2)*.012,-1.501,0);String path;EntityModel<?> model;if(actor instanceof Zombie z){zombie.setupAnim(z,actor.tickCount*.2f,.18f,actor.tickCount+partial,0,0);model=zombie;path="textures/entity/zombie/zombie.png";}else {skeleton.setupAnim((Skeleton)actor,actor.tickCount*.2f,.18f,actor.tickCount+partial,0,0);model=skeleton;path="textures/entity/skeleton/skeleton.png";}model.renderToBuffer(pose,out.getBuffer(RenderType.entityTranslucent(net.minecraft.resources.ResourceLocation.withDefaultNamespace(path))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xEAADB2DC);pose.popPose();}
}
