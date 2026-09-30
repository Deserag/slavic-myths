package org.slavicmyths.client;
import java.util.WeakHashMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.*;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class GusliClient {
 private static final WeakHashMap<AbstractClientPlayerEntity,Music> sounds=new WeakHashMap<>();private static boolean rendering;private static PlayerRenderer normal,slim;
 private static boolean playing(AbstractClientPlayerEntity p){return p.isAlive()&&p.isUsingItem()&&p.getUseItem().getItem()==ModItems.GUSLI.get();}
 @SubscribeEvent public static void update(LivingEvent.LivingUpdateEvent e){if(!(e.getEntityLiving() instanceof AbstractClientPlayerEntity))return;AbstractClientPlayerEntity p=(AbstractClientPlayerEntity)e.getEntityLiving();if(playing(p)&&p.getTicksUsingItem()>=10&&!sounds.containsKey(p)){Music s=new Music(p);sounds.put(p,s);Minecraft.getInstance().getSoundManager().play(s);}if(!playing(p))sounds.remove(p);}
 private static final class Music extends TickableSound {
  final AbstractClientPlayerEntity player;Music(AbstractClientPlayerEntity p){super(ModSounds.GUSLI_LOOP.get(),SoundCategory.PLAYERS);player=p;looping=true;delay=0;volume=.55F;pitch=1;x=p.getX();y=p.getY();z=p.getZ();}
  public void tick(){if(!playing(player)||player.removed||Minecraft.getInstance().level!=player.level){stop();sounds.remove(player);return;}x=player.getX();y=player.getY()+1;z=player.getZ();if(player.tickCount%30==0)player.level.addParticle(net.minecraft.particles.ParticleTypes.ENCHANT,x,y,z,0,.015,0);}
 }
 @SubscribeEvent public static void render(RenderPlayerEvent.Pre e){if(rendering||!(e.getPlayer() instanceof AbstractClientPlayerEntity)||!playing((AbstractClientPlayerEntity)e.getPlayer())||e.getPlayer().isInvisible())return;if(normal==null){normal=new Musician(Minecraft.getInstance().getEntityRenderDispatcher(),false);slim=new Musician(Minecraft.getInstance().getEntityRenderDispatcher(),true);}e.setCanceled(true);rendering=true;try{AbstractClientPlayerEntity p=(AbstractClientPlayerEntity)e.getPlayer();(p.getModelName().equals("slim")?slim:normal).render(p,p.yRot,e.getPartialRenderTick(),e.getMatrixStack(),e.getBuffers(),e.getLight());}finally{rendering=false;}}
 private static final class Musician extends PlayerRenderer {
  Musician(EntityRendererManager m,boolean slim){super(m,slim);model=new PlayerModel<AbstractClientPlayerEntity>(0,slim){@Override public void setupAnim(AbstractClientPlayerEntity p,float a,float b,float age,float y,float x){super.setupAnim(p,a,b,age,y,x);rightArm.xRot=-.95F+MathHelper.sin(age*.6F)*.08F;rightArm.yRot=-.45F;leftArm.xRot=-1.05F;leftArm.yRot=.5F;rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);}};addLayer(new FolkEquipmentLayer(this));}
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){Minecraft mc=Minecraft.getInstance();if(mc.player==null||!playing(mc.player)||e.getItemStack().getItem()!=ModItems.GUSLI.get())return;e.setCanceled(true);MatrixStack m=e.getMatrixStack();m.pushPose();m.translate(.05,-.48,-.8);m.mulPose(Vector3f.XP.rotationDegrees(65));m.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.sin((mc.player.tickCount+e.getPartialTicks())*.6F)*1.5F));mc.getItemRenderer().renderStatic(e.getItemStack(),ItemCameraTransforms.TransformType.NONE,e.getLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,m,e.getBuffers());m.popPose();}
}
