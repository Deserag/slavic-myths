package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Existing geometry stays in its renderers; common items do not load client classes. */
@EventBusSubscriber(modid="slavicmyths", bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class CuriosAccessories implements ICurioRenderer {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(ModItems.POYAS_TUGARINA.get(), CuriosAccessories::new);
            CuriosRendererRegistry.register(ModItems.ODNOGLAZYY_OBEREG.get(), CuriosAccessories::new);
            CuriosRendererRegistry.register(ModItems.OBEREG_PADAYUSCHEY_ZVEZDY.get(), CuriosAccessories::new);
            CuriosRendererRegistry.register(ModItems.ZOLNY_OBEREG.get(), CuriosAccessories::new);
            CuriosRendererRegistry.register(ModItems.VOLCHIY_POYAS.get(), CuriosAccessories::new);
        });
    }
    @Override public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext context,
            PoseStack pose, RenderLayerParent<T,M> parent, MultiBufferSource buffer, int light,
            float limbSwing, float limbAmount, float partial, float age, float yaw, float pitch) {
        if (stack.is(ModItems.POYAS_TUGARINA.get()) || stack.is(ModItems.ODNOGLAZYY_OBEREG.get()))
            BossAccessoryRenderer.render(stack, context.entity(), pose, buffer, light);
        else HuntAccessoryRenderer.render(stack, context.entity(), pose, buffer, light);
    }
}
