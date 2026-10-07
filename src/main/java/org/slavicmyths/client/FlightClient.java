package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.flight.*;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class FlightClient {
 private static final KeyMapping DOWN=new KeyMapping("key.slavicmyths.flight_down",67,"key.categories.slavicmyths"),BRAKE=new KeyMapping("key.slavicmyths.flight_brake",82,"key.categories.slavicmyths"),CARGO=new KeyMapping("key.slavicmyths.flight_cargo",66,"key.categories.slavicmyths");
 private static int tick;private static boolean cargo;private static boolean rendering;private static PlayerRenderer normal,slim;
 public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(DOWN);e.register(BRAKE);e.register(CARGO);}
 public static void renderers(EntityRendererProvider.Context context){normal=new RiderRenderer(context,false);slim=new RiderRenderer(context,true);}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){Minecraft mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof FlyingVessel)){cargo=false;return;}
  cargo|=CARGO.consumeClick();if(++tick%3!=0)return;boolean gui=mc.screen!=null;float f=gui?0:(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0),s=gui?0:(mc.options.keyLeft.isDown()?1:0)-(mc.options.keyRight.isDown()?1:0),u=gui?0:(mc.options.keyJump.isDown()?1:0)-(DOWN.isDown()?1:0);
  FlightNetwork.send(f,s,u,mc.player.getYRot(),gui||BRAKE.isDown(),cargo&&!gui);cargo=false;
 }
 @SubscribeEvent public static void rider(RenderPlayerEvent.Pre e){if(rendering||!(e.getEntity().getVehicle() instanceof FlyingVessel)||e.getEntity().isInvisible())return;
  if(normal==null)return;
  e.setCanceled(true);rendering=true;try{AbstractClientPlayer p=(AbstractClientPlayer)e.getEntity();(p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:normal).render(p,p.getYRot(),e.getPartialTick(),e.getPoseStack(),e.getMultiBufferSource(),e.getPackedLight());}finally{rendering=false;}
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){Minecraft mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof FlyingVessel))return;FlyingVessel v=(FlyingVessel)mc.player.getVehicle();PoseStack m=e.getPoseStack();
  if(v.mortar&&e.getItemStack().getItem()==ModItems.PESTLE.get()){float wave=Mth.sin((mc.player.tickCount+e.getPartialTick())*.19F);m.translate(v.roll()*.004,Math.abs(v.drive())*.025*wave,Math.max(0,v.drive())*.06*wave);m.mulPose(com.mojang.math.Axis.XP.rotationDegrees(v.drive()==-2?-18:wave*9*Math.abs(v.drive())-v.pitch()));m.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(v.roll()*2));}
  else if(!v.mortar)m.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(v.roll()*.25F));
 }
 private static final class RiderRenderer extends PlayerRenderer {
  RiderRenderer(EntityRendererProvider.Context m,boolean slim){super(m,slim);model=new RiderModel(m.bakeLayer(slim?net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM:net.minecraft.client.model.geom.ModelLayers.PLAYER),slim);layers.removeIf(layer->layer instanceof net.minecraft.client.renderer.entity.layers.CustomHeadLayer);addLayer(new ClothingHeadLayer(this,m));addLayer(new TextileClothingLayer(this,slim));addLayer(new FolkEquipmentLayer(this));}
  @Override protected void setupRotations(AbstractClientPlayer p,PoseStack m,float age,float yaw,float partial,float scale){super.setupRotations(p,m,age,yaw,partial,scale);if(p.getVehicle() instanceof FlyingVessel){FlyingVessel v=(FlyingVessel)p.getVehicle();m.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(v.roll()*.55F));m.mulPose(com.mojang.math.Axis.XP.rotationDegrees(v.pitch()*.6F));}}
 }
 private static final class RiderModel extends PlayerModel<AbstractClientPlayer>{
  RiderModel(net.minecraft.client.model.geom.ModelPart root,boolean slim){super(root,slim);}
  @Override public void setupAnim(AbstractClientPlayer p,float walk,float amount,float age,float yaw,float pitch){riding=false;super.setupAnim(p,0,0,age,yaw,pitch);if(!(p.getVehicle() instanceof FlyingVessel))return;FlyingVessel v=(FlyingVessel)p.getVehicle();
   if(v.mortar){leftLeg.xRot=rightLeg.xRot=0;float stroke=Mth.sin(age*.19F)*.18F*Math.abs(v.drive());rightArm.xRot=v.drive()==-2?-1.5F:-.8F+stroke-v.pitch()*.012F;rightArm.yRot=-.2F+v.roll()*.06F;leftArm.xRot=-.5F-stroke;leftArm.yRot=.3F;}
   else{leftLeg.xRot=rightLeg.xRot=-.65F;leftLeg.zRot=.18F;rightLeg.zRot=-.18F;leftArm.xRot=rightArm.xRot=-.9F;leftArm.yRot=.14F;rightArm.yRot=-.14F;}
   leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);jacket.copyFrom(body);
  }
 }
}
