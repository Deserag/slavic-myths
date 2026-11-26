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
    private static final String[] PAIRS={"tugarin_sword","ice_captain_axes","snow_wanderer_hammers","silence_blades","northern_beast_claws","lomeya_hammer","four_winds_fists","scorpion_double_sting"};
    private static final String[] DESERT_HELD={"desert_sword","desert_axe","desert_mace","desert_halberd","desert_scythe","stinger_dagger","sand_sling","desert_glass_blade","dune_mace","buried_hammer","hala_solar_staff","colony_scepter","lamya_weapon","quicksand_whip"};
    private static java.util.stream.Stream<String> heldIds(){return java.util.stream.Stream.concat(java.util.Arrays.stream(PAIRS),java.util.Arrays.stream(DESERT_HELD));}
    private static net.minecraft.client.resources.model.ModelResourceLocation heldModel(String id){
        return net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","item/"+id+"_held"));
    }
    public static void registerModels(ModelEvent.RegisterAdditional e){heldIds().forEach(id->e.register(heldModel(id)));}
    public static void bake(ModelEvent.ModifyBakingResult e){
        for(String id:heldIds().toList()){
            var key=net.minecraft.client.resources.model.ModelResourceLocation.inventory(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id));
            var inventory=e.getModels().get(key);var held=e.getModels().get(heldModel(id));
            if(inventory!=null&&held!=null)e.getModels().put(key,new net.neoforged.neoforge.client.model.BakedModelWrapper<net.minecraft.client.resources.model.BakedModel>(inventory){
                @Override public net.minecraft.client.resources.model.BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left){
                    boolean hand=context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    boolean flight=context==ItemDisplayContext.NONE&&(id.equals("ice_captain_axes")||id.equals("silence_blades"));
                    return(hand||flight?held:originalModel).applyTransform(context,pose,left);
                }
            });
        }
    }
    private static boolean secondVisible(net.minecraft.world.entity.player.Player p){
        return WeaponProjectile.pairedInFlight(p).isEmpty();
    }
    private static final java.util.Map<net.minecraft.world.entity.player.Player,SwingState> SWINGS=new java.util.WeakHashMap<>();
    private static final class SwingState {float previous;int count;InteractionHand original;}
    private static InteractionHand slash(net.minecraft.world.entity.player.Player p,float partial){var state=SWINGS.computeIfAbsent(p,k->new SwingState());float current=p.getAttackAnim(partial);if(current>0&&(state.previous<=0||current<state.previous-.2F))state.count++;state.previous=current;return(state.count&1)==1?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;}
    @SubscribeEvent public static void beforePlayer(RenderPlayerEvent.Pre e){var p=e.getEntity();if(!TugarinPair.holdsPair(p))return;var hand=slash(p,e.getPartialTick());var state=SWINGS.get(p);state.original=p.swingingArm;p.swingingArm=hand;}
    @SubscribeEvent public static void afterPlayer(RenderPlayerEvent.Post e){var p=e.getEntity();var state=SWINGS.get(p);if(state!=null&&state.original!=null){p.swingingArm=state.original;state.original=null;}}
    static boolean uprightWinter(net.minecraft.world.item.ItemStack stack){
        return stack.is(org.slavicmyths.registry.ModItems.ICE_CAPTAIN_AXES.get())
                ||stack.is(org.slavicmyths.registry.ModItems.SNOW_WANDERER_HAMMERS.get())
                ||stack.is(org.slavicmyths.registry.ModItems.ABSOLUTE_SILENCE_SWORD.get());
    }
    @SubscribeEvent public static void hand(RenderHandEvent e){var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
        boolean pair=TugarinPair.holdsPair(p),upright=uprightWinter(p.getMainHandItem()),main=e.getHand()==InteractionHand.MAIN_HAND;
        if(!pair&&(!upright||!main))return;
        e.setCanceled(true);if(pair&&!main&&!secondVisible(p))return;
        boolean left=main?p.getMainArm()==HumanoidArm.LEFT:p.getMainArm()==HumanoidArm.RIGHT;var pose=e.getPoseStack();pose.pushPose();
        float swing=pair?(slash(p,e.getPartialTick())==e.getHand()?p.getAttackAnim(e.getPartialTick()):0):e.getSwingProgress();
        if(p.getMainHandItem().is(org.slavicmyths.registry.ModItems.FOUR_WINDS.get())){
            float punch=(float)Math.sin(Math.sqrt(swing)*Math.PI);pose.translate((left?-1:1)*(.48-.08*punch),-.48+.05*punch,-.65-.3*punch);
        }else if(upright)winterWrist(pose,left,swing);else firstPersonWrist(pose,left,swing);
        WeaponHands.renderGrip(e,left?-1:1);
        mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(p,p.getMainHandItem(),left?ItemDisplayContext.FIRST_PERSON_LEFT_HAND:ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,left,pose,e.getMultiBufferSource(),e.getPackedLight());pose.popPose();
    }
    public static void layers(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){PlayerRenderer renderer=e.getSkin(skin);if(renderer!=null)renderer.addLayer(new SecondBladeLayer(renderer));}}
    private static final class SecondBladeLayer extends ItemInHandLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
        SecondBladeLayer(PlayerRenderer parent){super(parent,Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer());}
        @Override public void render(PoseStack pose,MultiBufferSource buffer,int light,AbstractClientPlayer player,float swing,float amount,float partial,float age,float yaw,float pitch){if(!TugarinPair.holdsPair(player)||!secondVisible(player))return;var arm=player.getMainArm().getOpposite();renderArmWithItem(player,player.getMainHandItem(),arm==HumanoidArm.LEFT?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,arm,pose,buffer,light);}
    }
    static void firstPersonWrist(PoseStack pose,boolean left,float swing){float sign=left?-1:1;pose.translate(sign*.56,-.52,-.72);pose.mulPose(Axis.YP.rotationDegrees(sign*35));pose.mulPose(Axis.ZP.rotationDegrees(sign*-12));pose.mulPose(Axis.XP.rotationDegrees((float)(-60*Math.sin(Math.sqrt(swing)*Math.PI))));}
    static void winterWrist(PoseStack pose,boolean left,float swing){
        float sign=left?-1:1,arc=(float)Math.sin(Math.sqrt(swing)*Math.PI);
        // Same forward yaw in both hands: never mirror the cutting or impact face.
        pose.translate(sign*.59,-.59,-1.45);
        pose.mulPose(Axis.XP.rotationDegrees(-42*arc));
    }
    private TugarinPairClient(){}
}
