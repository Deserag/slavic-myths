package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import java.util.WeakHashMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class GusliClient {
 private static final WeakHashMap<AbstractClientPlayer,Music> sounds=new WeakHashMap<>();private static boolean rendering;private static PlayerRenderer normal,slim;
 private static boolean playing(AbstractClientPlayer p){return p.isAlive()&&p.isUsingItem()&&p.getUseItem().getItem()==ModItems.GUSLI.get();}
 @SubscribeEvent public static void update(net.neoforged.neoforge.event.tick.EntityTickEvent.Post e){if(!(e.getEntity() instanceof AbstractClientPlayer))return;AbstractClientPlayer p=(AbstractClientPlayer)e.getEntity();if(playing(p)&&p.getTicksUsingItem()>=10&&!sounds.containsKey(p)){Music s=new Music(p);sounds.put(p,s);Minecraft.getInstance().getSoundManager().play(s);}if(!playing(p))sounds.remove(p);}
 private static final class Music extends AbstractTickableSoundInstance {
  final AbstractClientPlayer player;Music(AbstractClientPlayer p){super(ModSounds.GUSLI_LOOP.get(),SoundSource.PLAYERS,net.minecraft.util.RandomSource.create());player=p;looping=true;delay=0;volume=.55F;pitch=1;x=p.getX();y=p.getY();z=p.getZ();}
  public void tick(){if(!playing(player)||player.isRemoved()||Minecraft.getInstance().level!=player.level()){stop();sounds.remove(player);return;}x=player.getX();y=player.getY()+1;z=player.getZ();if(player.tickCount%30==0)player.level().addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,x,y,z,0,.015,0);}
 }
 @SubscribeEvent public static void render(RenderPlayerEvent.Pre e){if(rendering||!(e.getEntity() instanceof AbstractClientPlayer)||!playing((AbstractClientPlayer)e.getEntity())||e.getEntity().isInvisible())return;if(normal==null)return;e.setCanceled(true);rendering=true;try{AbstractClientPlayer p=(AbstractClientPlayer)e.getEntity();(p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:normal).render(p,p.getYRot(),e.getPartialTick(),e.getPoseStack(),e.getMultiBufferSource(),e.getPackedLight());}finally{rendering=false;}}
 public static void renderers(EntityRendererProvider.Context context){normal=new Musician(context,false);slim=new Musician(context,true);}
 private static final class Musician extends PlayerRenderer {
  Musician(EntityRendererProvider.Context m,boolean slim){super(m,slim);model=new PlayerModel<AbstractClientPlayer>(m.bakeLayer(slim?net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM:net.minecraft.client.model.geom.ModelLayers.PLAYER),slim){@Override public void setupAnim(AbstractClientPlayer p,float a,float b,float age,float y,float x){super.setupAnim(p,a,b,age,y,x);rightArm.xRot=-.95F+Mth.sin(age*.6F)*.08F;rightArm.yRot=-.45F;leftArm.xRot=-1.05F;leftArm.yRot=.5F;rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);}};addLayer(new FolkEquipmentLayer(this));}
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){Minecraft mc=Minecraft.getInstance();if(mc.player==null||!playing(mc.player)||e.getItemStack().getItem()!=ModItems.GUSLI.get())return;e.setCanceled(true);PoseStack m=e.getPoseStack();m.pushPose();m.translate(.05,-.48,-.8);m.mulPose(com.mojang.math.Axis.XP.rotationDegrees(65));m.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(Mth.sin((mc.player.tickCount+e.getPartialTick())*.6F)*1.5F));mc.getItemRenderer().renderStatic(e.getItemStack(),ItemDisplayContext.NONE,e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,m,e.getMultiBufferSource(),mc.level,0);m.popPose();}
}
