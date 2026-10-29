package org.slavicmyths.nethersmithing.client;
import net.minecraft.client.model.*;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.*;import net.minecraft.client.renderer.entity.layers.RenderLayer;import net.minecraft.world.entity.*;import net.minecraft.world.item.*;import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import org.slavicmyths.nethersmithing.NetherArmor;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class NetherArmorGlow {
 @SuppressWarnings({"rawtypes","unchecked"}) @SubscribeEvent public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){var renderer=e.getSkin(skin);if(renderer instanceof LivingEntityRenderer living)living.addLayer(new Glow(living));}for(var type:e.getEntityTypes()){var renderer=e.getRenderer(type);if(renderer instanceof LivingEntityRenderer living&&living.getModel() instanceof HumanoidModel)living.addLayer(new Glow(living));}}
 public static ResourceLocation texture(NetherArmor.Piece piece,boolean glow){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/armor/nether/"+piece.id+(glow?"_glow":"")+".png");}
 private static final java.util.Map<EquipmentSlot,HumanoidModel<LivingEntity>> STANDARD=new java.util.EnumMap<>(EquipmentSlot.class);
 public static HumanoidModel<LivingEntity> standard(EquipmentSlot slot){return STANDARD.computeIfAbsent(slot,s->new HumanoidModel<>(net.minecraft.client.model.geom.builders.LayerDefinition.create(HumanoidModel.createMesh(new net.minecraft.client.model.geom.builders.CubeDeformation(s==EquipmentSlot.LEGS?.5F:1F),0),64,32).bakeRoot()));}
 public static void visible(HumanoidModel<?> m,EquipmentSlot slot){m.setAllVisible(false);switch(slot){case HEAD->m.head.visible=true;case CHEST->{m.body.visible=true;m.leftArm.visible=true;m.rightArm.visible=true;}case LEGS->{m.body.visible=true;m.leftLeg.visible=true;m.rightLeg.visible=true;}case FEET->{m.leftLeg.visible=true;m.rightLeg.visible=true;}default->{}}}
 private static final class Glow extends RenderLayer<LivingEntity,HumanoidModel<LivingEntity>> {
  Glow(RenderLayerParent<LivingEntity,HumanoidModel<LivingEntity>> parent){super(parent);}
  @Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource buffers,int light,LivingEntity e,float a,float b,float partial,float age,float yaw,float pitch){if(e.isInvisible())return;for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){var stack=e.getItemBySlot(slot);if(!(stack.getItem() instanceof NetherArmor.Piece p))continue;HumanoidModel<LivingEntity> model;
   if(p.king){var custom=NetherArmorClient.model(slot,NetherArmorClient.slim(e));custom.animate(e);model=custom;}else model=standard(slot);getParentModel().copyPropertiesTo(model);visible(model,slot);model.renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(texture(p,true))),LightTexture.FULL_BRIGHT,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xffffffff);
  }}
 }
 private NetherArmorGlow(){}
}
