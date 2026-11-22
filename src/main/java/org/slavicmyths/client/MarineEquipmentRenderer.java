package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.marine.*;

/** Vanilla meshes and glint, using the marine recolors in hand and during throwing. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class MarineEquipmentRenderer extends BlockEntityWithoutLevelRenderer {
    private MarineWeaponModels oceanModels=new MarineWeaponModels();
    private ShieldModel shield;
    private TridentModel trident;
    public MarineEquipmentRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());
    }
    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/marine/"+name+".png");
    }
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource out,int light,int overlay) {
        if(stack.is(MarineWeapons.ANCHOR.get())&&(context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND)&&MarineAnchorRenderer.localFlight())return;
        pose.pushPose();pose.translate(.5,.5,.5);pose.scale(1,-1,-1);
        oceanModels.render(MarineWeaponModels.name(stack),pose,out,light,overlay,stack.hasFoil());
        if(stack.is(ZhelvaShield.ITEM.get())&&Minecraft.getInstance().level!=null&&ZhelvaShield.ready(stack,Minecraft.getInstance().level.getGameTime()))oceanModels.renderGuard(pose,out,overlay);
        pose.popPose();
    }    @Override public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager){shield=null;trident=null;oceanModels=new MarineWeaponModels();}
    @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions(){
            private MarineEquipmentRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){
                if(renderer==null)renderer=new MarineEquipmentRenderer();return renderer;
            }
        },PharaohkaTrident.ITEM.get(),MarineWeapons.HARPOON.get(),ZhelvaShield.ITEM.get(),MarineWeapons.ANCHOR.get(),MarineWeapons.STAFF.get());
    }
    @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
    public static final class Receiver {
        @SubscribeEvent public static void receive(MarineTridentAppearance.Received event) {
            var world=Minecraft.getInstance().level;if(world==null)return;
            var entity=world.getEntity(event.value.entityId());
            if(entity instanceof net.minecraft.world.entity.projectile.ThrownTrident && event.value.variant()>=1 && event.value.variant()<=2)
                entity.getPersistentData().putInt(MarineTridentAppearance.KEY,event.value.variant());
        }
    }
    @SubscribeEvent public static void projectiles(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(net.minecraft.world.entity.EntityType.TRIDENT,context->
            new net.minecraft.client.renderer.entity.ThrownTridentRenderer(context){
                private final MarineWeaponModels shapes=new MarineWeaponModels();
                @Override public void render(net.minecraft.world.entity.projectile.ThrownTrident entity,float yaw,float partial,PoseStack pose,MultiBufferSource out,int light){
                    int variant=entity.getPersistentData().getInt(MarineTridentAppearance.KEY);
                    if(variant==0){super.render(entity,yaw,partial,pose,out,light);return;}
                    pose.pushPose();
                    pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(net.minecraft.util.Mth.lerp(partial,entity.yRotO,entity.getYRot())-90));
                    pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(net.minecraft.util.Mth.lerp(partial,entity.xRotO,entity.getXRot())+90));
                    shapes.render(variant==1?"trident":"harpoon",pose,out,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,entity.isFoil());
                    pose.popPose();
                }                @Override public ResourceLocation getTextureLocation(net.minecraft.world.entity.projectile.ThrownTrident entity) {
                    int variant=entity.getPersistentData().getInt(MarineTridentAppearance.KEY);
                    if(variant==1)return texture("pharaohka_trident");
                    if(variant==2)return texture("storm_harpoon");
                    var item=entity.getWeaponItem();
                    if(item!=null&&item.is(PharaohkaTrident.ITEM.get()))return texture("pharaohka_trident");
                    if(item!=null&&item.is(MarineWeapons.HARPOON.get()))return texture("storm_harpoon");
                    return super.getTextureLocation(entity);
                }
            });
    }
}