package org.slavicmyths.navya.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.slavicmyths.navya.*;
public final class NavyaEffectRenderer<T extends Entity> extends EntityRenderer<T> {
    private final NavyaMesh.Rig[] rigs;
    private final boolean roots;
    public NavyaEffectRenderer(EntityRendererProvider.Context context,boolean roots){super(context);this.roots=roots;rigs=roots?new NavyaMesh.Rig[]{NavyaMesh.roots()}:new NavyaMesh.Rig[]{NavyaMesh.projectile(0),NavyaMesh.projectile(1),NavyaMesh.projectile(2)};}
    @Override public ResourceLocation getTextureLocation(T entity){int mode=entity instanceof NavyaShot s?s.mode():0;return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/navya_"+(roots?"roots":"shot_"+mode)+".png");}
    @Override public void render(T entity,float yaw,float partial,PoseStack stack,MultiBufferSource buffers,int light){
        stack.pushPose();int mode=entity instanceof NavyaShot shot?shot.mode():0;var rig=rigs[mode];
        for(var part:rig.nodes().values())part.resetPose();
        if(roots){
            float age=((NavyaRoots)entity).age(partial);float rise=Math.max(0,Math.min(1,(age-15)/9)),recede=Math.max(0,Math.min(1,(age-45)/15));
            rig.nodes().get("body").y+=(1-rise+recede)*32;
            rig.nodes().get("warning").visible=age<23;
            stack.translate(0,1.5,0);stack.scale(1,-1,-1);
        }else{
            var delta=entity.getDeltaMovement();stack.mulPose(Axis.YP.rotation((float)Math.atan2(delta.x,delta.z)));
            stack.mulPose(Axis.XP.rotation((float)-Math.atan2(delta.y,delta.horizontalDistance())));stack.scale(1,-1,-1);
        }
        rig.root().render(stack,buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))),mode==0&&!roots?15728880:light,OverlayTexture.NO_OVERLAY,-1);
        if(roots){var glow=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/navya_roots_glow.png");rig.root().render(stack,buffers.getBuffer(RenderType.eyes(glow)),15728880,OverlayTexture.NO_OVERLAY,-1);}
        stack.popPose();super.render(entity,yaw,partial,stack,buffers,light);
    }
}
