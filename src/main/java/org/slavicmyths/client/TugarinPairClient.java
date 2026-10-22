package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.combat.*;

/** Rendering only: never inserts a second item into a client or server inventory. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class TugarinPairClient {
    public static final net.minecraft.client.resources.model.ModelResourceLocation HELD=net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","item/tugarin_sword_held"));
    public static void registerModels(ModelEvent.RegisterAdditional e){e.register(HELD);}
    public static void bake(ModelEvent.ModifyBakingResult e){var key=net.minecraft.client.resources.model.ModelResourceLocation.inventory(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","tugarin_sword"));var inventory=e.getModels().get(key);var held=e.getModels().get(HELD);if(inventory!=null&&held!=null)e.getModels().put(key,new net.neoforged.neoforge.client.model.BakedModelWrapper<net.minecraft.client.resources.model.BakedModel>(inventory){
        @Override public net.minecraft.client.resources.model.BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left){boolean hand=context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;return(hand?held:originalModel).applyTransform(context,pose,left);}
    });}
    private static final java.util.Map<net.minecraft.world.entity.player.Player,SwingState> SWINGS=new java.util.WeakHashMap<>();
    private static final class SwingState {float previous;int count;InteractionHand original;}
    private static InteractionHand slash(net.minecraft.world.entity.player.Player p,float partial){var state=SWINGS.computeIfAbsent(p,k->new SwingState());float current=p.getAttackAnim(partial);if(current>0&&(state.previous<=0||current<state.previous-.2F))state.count++;state.previous=current;return(state.count&1)==1?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;}
    @SubscribeEvent public static void beforePlayer(RenderPlayerEvent.Pre e){var p=e.getEntity();if(!TugarinPair.held(p))return;var hand=slash(p,e.getPartialTick());var state=SWINGS.get(p);state.original=p.swingingArm;p.swingingArm=hand;}
    @SubscribeEvent public static void afterPlayer(RenderPlayerEvent.Post e){var p=e.getEntity();var state=SWINGS.get(p);if(state!=null&&state.original!=null){p.swingingArm=state.original;state.original=null;}}
    @SubscribeEvent public static void hand(RenderHandEvent e){var mc=Minecraft.getInstance();var p=mc.player;if(p==null||!TugarinPair.held(p))return;e.setCanceled(true);boolean main=e.getHand()==InteractionHand.MAIN_HAND;boolean left=main?p.getMainArm()==HumanoidArm.LEFT:p.getMainArm()==HumanoidArm.RIGHT;var pose=e.getPoseStack();pose.pushPose();
        float swing=slash(p,e.getPartialTick())==e.getHand()?p.getAttackAnim(e.getPartialTick()):0;firstPersonWrist(pose,left,swing);WeaponHands.renderGrip(e,left?-1:1);
        mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(p,p.getMainHandItem(),left?ItemDisplayContext.FIRST_PERSON_LEFT_HAND:ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,left,pose,e.getMultiBufferSource(),e.getPackedLight());pose.popPose();
    }
    public static void layers(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){PlayerRenderer renderer=e.getSkin(skin);if(renderer!=null)renderer.addLayer(new SecondBladeLayer(renderer));}}
    private static final class SecondBladeLayer extends ItemInHandLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        SecondBladeLayer(PlayerRenderer parent){super(parent,Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer());}
        @Override public void render(PoseStack pose,MultiBufferSource buffer,int light,AbstractClientPlayer player,float swing,float amount,float partial,float age,float yaw,float pitch){if(!TugarinPair.held(player))return;var arm=player.getMainArm().getOpposite();renderArmWithItem(player,player.getMainHandItem(),arm==HumanoidArm.LEFT?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,arm,pose,buffer,light);}
    }
    static void firstPersonWrist(PoseStack pose,boolean left,float swing){float sign=left?-1:1;pose.translate(sign*.56,-.52,-.72);pose.mulPose(Axis.YP.rotationDegrees(sign*35));pose.mulPose(Axis.ZP.rotationDegrees(sign*-12));pose.mulPose(Axis.XP.rotationDegrees((float)(-60*Math.sin(Math.sqrt(swing)*Math.PI))));}
    private TugarinPairClient(){}
}
