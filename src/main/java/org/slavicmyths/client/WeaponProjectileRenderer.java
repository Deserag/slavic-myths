package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.util.Mth;
import org.slavicmyths.combat.WeaponProjectile;

/** The actual carried material/item sprite follows flight orientation, not a generic recolored bolt. */
public final class WeaponProjectileRenderer extends EntityRenderer<WeaponProjectile> {
    private final net.minecraft.client.renderer.entity.ItemRenderer items;
    public WeaponProjectileRenderer(EntityRendererProvider.Context context){super(context);items=context.getItemRenderer();}
    @Override public void render(WeaponProjectile projectile,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();
        flightPose(pose,Mth.lerp(partial,projectile.yRotO,projectile.getYRot()),Mth.lerp(partial,projectile.xRotO,projectile.getXRot()),projectile.isKnife());
        // FIXED inherits the item-frame Y180 turn, making the diagonal blade crosswise in flight.
        items.renderStatic(projectile.getItem(),FLIGHT_CONTEXT,light,OverlayTexture.NO_OVERLAY,pose,buffers,projectile.level(),projectile.getId());
        pose.popPose();super.render(projectile,yaw,partial,pose,buffers,light);
    }
    public static final ItemDisplayContext FLIGHT_CONTEXT=ItemDisplayContext.NONE;
    public static void flightPose(PoseStack pose,float yaw,float pitch,boolean knife){
        pose.mulPose(Axis.YP.rotationDegrees(yaw-90));
        pose.mulPose(Axis.ZP.rotationDegrees(pitch-45));
        float scale=knife?.65F:1.25F;pose.scale(scale,scale,scale);
    }
    @Override public ResourceLocation getTextureLocation(WeaponProjectile projectile){return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;}
}
