package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.posad.boss.MytarSickleProjectile;

/** Continuous textured tether from the sickle projectile to its owner during throw, pull and return. */
public final class MytarChainProjectileRenderer extends ThrownItemRenderer<MytarSickleProjectile> {
    private static final ResourceLocation CHAIN_TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/block/military_chain.png");
    public MytarChainProjectileRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override public void render(MytarSickleProjectile projectile, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        super.render(projectile, yaw, partialTick, pose, buffers, light);
        Entity owner = projectile.getOwner();
        if (owner == null || owner.level() != projectile.level()) return;
        Vec3 start = projectile.getPosition(partialTick);
        Vec3 target = owner instanceof net.minecraft.world.entity.LivingEntity living ? MytarSickleProjectile.handAnchor(living, projectile.offhand(), partialTick) : owner.getPosition(partialTick);
        Vec3 span = target.subtract(start);
        double length = span.length();
        if (length < .04D || length > 18D) return;
        Vec3 forward = span.scale(1D / length);
        Vec3 right = forward.cross(new Vec3(0D, 1D, 0D));
        if (right.lengthSqr() < .0001D) right = forward.cross(new Vec3(1D, 0D, 0D));
        right = right.normalize().scale(.055D);
        Vec3 up = forward.cross(right).normalize().scale(.055D);
        int links = Math.min(72, Math.max(1, (int) Math.ceil(length * 4D)));
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE));
        PoseStack.Pose matrix = pose.last();
        for (int i = 0; i < links; i++) {
            Vec3 a = forward.scale(length * i / links);
            Vec3 b = forward.scale(length * (i + 1) / links);
            drawLink(vertices, matrix, a, b, right, light);
            drawLink(vertices, matrix, a, b, up, light);
        }
    }

    private static void drawLink(VertexConsumer out, PoseStack.Pose matrix, Vec3 a, Vec3 b, Vec3 side, int light) {
        put(out, matrix, a.add(side), 0F, 0F, light);
        put(out, matrix, a.subtract(side), 1F, 0F, light);
        put(out, matrix, b.subtract(side), 1F, 1F, light);
        put(out, matrix, b.add(side), 0F, 1F, light);
    }

    private static void put(VertexConsumer out, PoseStack.Pose matrix, Vec3 point, float u, float v, int light) {
        out.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
            .setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(matrix, 0F, 1F, 0F);
    }
}
