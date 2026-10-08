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
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial,projectile.yRotO,projectile.getYRot())-90));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial,projectile.xRotO,projectile.getXRot())-45));
        float scale=projectile.isKnife()?.65F:1.25F;pose.scale(scale,scale,scale);
        items.renderStatic(projectile.getItem(),ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,projectile.level(),projectile.getId());
        pose.popPose();super.render(projectile,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(WeaponProjectile projectile){return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;}
}
