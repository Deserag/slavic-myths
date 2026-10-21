package org.slavicmyths.client.runeglow;

import java.io.IOException;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(modid="slavicmyths", value=Dist.CLIENT, bus=EventBusSubscriber.Bus.MOD)
public final class RuneGlowRenderTypes extends RenderType {
    private static ShaderInstance shader;
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/misc/rune_glint.png");
    public static final RenderType MAIN=createGlow(false);
    public static final RenderType TRANSLUCENT_ITEM=createGlow(true);
    private RuneGlowRenderTypes() { super("unused_rune_glow",DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,1536,false,false,()->{},()->{}); }
    private static RenderType createGlow(boolean itemTarget) {
        var state=CompositeState.builder().setShaderState(new ShaderStateShard(()->shader))
                .setTextureState(new TextureStateShard(TEXTURE,false,false))
                .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                .setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(TRANSLUCENT_TRANSPARENCY);
        if (itemTarget) state.setOutputState(ITEM_ENTITY_TARGET);
        return create(itemTarget?"slavicmyths_rune_glow_item":"slavicmyths_rune_glow",
                DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,1536,false,false,state.createCompositeState(false));
    }
    @SubscribeEvent public static void shaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_glint"),DefaultVertexFormat.POSITION_TEX_COLOR),loaded->shader=loaded);
    }
    @SubscribeEvent public static void buffers(RegisterRenderBuffersEvent event) {
        // Fixed buffers prevent switching between normal and overlay consumers from ending a live builder.
        event.registerRenderBuffer(MAIN); event.registerRenderBuffer(TRANSLUCENT_ITEM);
    }
}
