package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.posad.boss.PosadBoss;
import org.slavicmyths.registry.ModEntities;

/** Dedicated 3D rigs, textures and animation poses; does not modify wildlife rendering. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class PosadBossRenderer extends MobRenderer<PosadBoss,PosadBossModel> {
    private static final ResourceLocation MYTAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/cursed_mytar.png");
    private static final ResourceLocation BAUK_TEXTURE = ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/bauk.png");
    public PosadBossRenderer(EntityRendererProvider.Context context, boolean bauk) { super(context, new PosadBossModel(bauk), bauk ? 1.5F : .8F); }
    @Override public ResourceLocation getTextureLocation(PosadBoss entity) { return entity.kind() == PosadBoss.Kind.BAUK ? BAUK_TEXTURE : MYTAR_TEXTURE; }
    @Override protected void scale(PosadBoss entity, PoseStack pose, float partialTick) { pose.scale(2F, 2F, 2F); }
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CURSED_MYTAR.get(), context -> new PosadBossRenderer(context, false));
        event.registerEntityRenderer(ModEntities.MYTAR_CHAIN_PROJECTILE.get(), MytarChainProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BAUK.get(), context -> new PosadBossRenderer(context, true));
    }
}
