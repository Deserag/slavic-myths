package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.flight.*;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class FlightClient {
 private static final KeyBinding DOWN=new KeyBinding("key.slavicmyths.flight_down",88,"key.categories.slavicmyths"),BRAKE=new KeyBinding("key.slavicmyths.flight_brake",82,"key.categories.slavicmyths"),CARGO=new KeyBinding("key.slavicmyths.flight_cargo",66,"key.categories.slavicmyths");
 private static int tick;private static boolean cargo;private static boolean rendering;private static PlayerRenderer normal,slim;
 public static void setup(){ClientRegistry.registerKeyBinding(DOWN);ClientRegistry.registerKeyBinding(BRAKE);ClientRegistry.registerKeyBinding(CARGO);ScreenManager.register(CargoMenu.TYPE.get(),CargoScreen::new);}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;Minecraft mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof FlyingVessel)){cargo=false;return;}
  cargo|=CARGO.consumeClick();if(++tick%3!=0)return;boolean gui=mc.screen!=null;float f=gui?0:(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0),s=gui?0:(mc.options.keyLeft.isDown()?1:0)-(mc.options.keyRight.isDown()?1:0),u=gui?0:(mc.options.keyJump.isDown()?1:0)-(DOWN.isDown()?1:0);
  FlightNetwork.send(f,s,u,mc.player.yRot,gui||BRAKE.isDown(),cargo&&!gui);cargo=false;
 }
 @SubscribeEvent public static void rider(RenderPlayerEvent.Pre e){if(rendering||!(e.getPlayer().getVehicle() instanceof FlyingVessel)||e.getPlayer().isInvisible())return;
  if(normal==null){normal=new RiderRenderer(Minecraft.getInstance().getEntityRenderDispatcher(),false);slim=new RiderRenderer(Minecraft.getInstance().getEntityRenderDispatcher(),true);}
  e.setCanceled(true);rendering=true;try{AbstractClientPlayerEntity p=(AbstractClientPlayerEntity)e.getPlayer();(p.getModelName().equals("slim")?slim:normal).render(p,p.yRot,e.getPartialRenderTick(),e.getMatrixStack(),e.getBuffers(),e.getLight());}finally{rendering=false;}
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){Minecraft mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof FlyingVessel))return;FlyingVessel v=(FlyingVessel)mc.player.getVehicle();MatrixStack m=e.getMatrixStack();
  if(v.mortar&&e.getItemStack().getItem()==ModItems.PESTLE.get()){float wave=MathHelper.sin((mc.player.tickCount+e.getPartialTicks())*.19F);m.translate(v.roll()*.004,Math.abs(v.drive())*.025*wave,Math.max(0,v.drive())*.06*wave);m.mulPose(Vector3f.XP.rotationDegrees(v.drive()==-2?-18:wave*9*Math.abs(v.drive())-v.pitch()));m.mulPose(Vector3f.ZP.rotationDegrees(v.roll()*2));}
  else if(!v.mortar)m.mulPose(Vector3f.ZP.rotationDegrees(v.roll()*.25F));
 }
 private static final class RiderRenderer extends PlayerRenderer {
  RiderRenderer(EntityRendererManager m,boolean slim){super(m,slim);model=new RiderModel(slim);addLayer(new FolkEquipmentLayer(this));}
  @Override protected void setupRotations(AbstractClientPlayerEntity p,MatrixStack m,float age,float yaw,float partial){super.setupRotations(p,m,age,yaw,partial);if(p.getVehicle() instanceof FlyingVessel){FlyingVessel v=(FlyingVessel)p.getVehicle();m.mulPose(Vector3f.ZP.rotationDegrees(v.roll()*.55F));m.mulPose(Vector3f.XP.rotationDegrees(v.pitch()*.6F));}}
 }
 private static final class RiderModel extends PlayerModel<AbstractClientPlayerEntity>{
  RiderModel(boolean slim){super(0,slim);}
  @Override public void setupAnim(AbstractClientPlayerEntity p,float walk,float amount,float age,float yaw,float pitch){riding=false;super.setupAnim(p,0,0,age,yaw,pitch);if(!(p.getVehicle() instanceof FlyingVessel))return;FlyingVessel v=(FlyingVessel)p.getVehicle();
   if(v.mortar){leftLeg.xRot=rightLeg.xRot=0;float stroke=MathHelper.sin(age*.19F)*.18F*Math.abs(v.drive());rightArm.xRot=v.drive()==-2?-1.5F:-.8F+stroke-v.pitch()*.012F;rightArm.yRot=-.2F+v.roll()*.06F;leftArm.xRot=-.5F-stroke;leftArm.yRot=.3F;}
   else{leftLeg.xRot=rightLeg.xRot=-.65F;leftLeg.zRot=.18F;rightLeg.zRot=-.18F;leftArm.xRot=rightArm.xRot=-.9F;leftArm.yRot=.14F;rightArm.yRot=-.14F;}
   leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);jacket.copyFrom(body);
  }
 }
}
