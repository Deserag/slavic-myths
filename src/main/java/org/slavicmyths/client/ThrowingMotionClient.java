package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Arrays;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Player-local throw windup: no replacement projectile, entity tick scans or server timing changes. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ThrowingMotionClient {
    private static final String[] KNIVES={"shadow_throwing_knife","stone_throwing_knife","gold_throwing_knife",
            "iron_throwing_knife","silver_throwing_knife","diamond_throwing_knife","zhara_throwing_knife",
            "perunite_throwing_knife","netherite_throwing_knife"};
    private static final String[] SULITSAS={"stone_sulitsa","gold_sulitsa","iron_sulitsa",
            "silver_sulitsa","diamond_sulitsa","zhara_sulitsa","perunite_sulitsa","netherite_sulitsa"};
    @SubscribeEvent public static void register(RegisterClientExtensionsEvent event) {
        install(event,true,KNIVES);
        install(event,false,SULITSAS);
    }
    private static void install(RegisterClientExtensionsEvent event,boolean knife,String[] ids) {
        Item[] items=Arrays.stream(ids).map(name->BuiltInRegistries.ITEM.getOptional(
                ResourceLocation.fromNamespaceAndPath("slavicmyths",name)).orElseThrow()).toArray(Item[]::new);
        event.registerItem(new IClientItemExtensions() {
            @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,
                    ItemStack stack,float partial,float equip,float swing) {
                InteractionHand hand=arm==player.getMainArm()?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
                float charge=!knife&&player.isUsingItem()&&player.getUsedItemHand()==hand&&player.getUseItem()==stack
                        ?Mth.clamp((stack.getUseDuration(player)-player.getUseItemRemainingTicks()+partial)/20F,0,1):0;
                float attack=player.swinging&&player.swingingArm==hand?Mth.clamp(swing,0,1):0;
                firstPerson(pose,arm==HumanoidArm.LEFT,equip,charge,attack,knife);
                return true;
            }
        },items);
    }
    static float ease(float value) {
        value=Mth.clamp(value,0,1);
        return value*value*(3-2*value);
    }
    static void firstPerson(PoseStack pose,boolean left,float equip,float charge,float swing,boolean knife) {
        int sign=left?-1:1;
        float windup=ease(charge);
        float throwArc=Mth.sin((float)Math.PI*Mth.sqrt(Mth.clamp(swing,0,1)));
        pose.translate(sign*.56F,-.52F-equip*.6F,-.72F);
        if(knife) {
            pose.translate(sign*.07F*throwArc,.03F*throwArc,-.20F*throwArc);
            pose.mulPose(Axis.XP.rotationDegrees(-48*throwArc));
            pose.mulPose(Axis.YP.rotationDegrees(sign*37*throwArc));
        } else {
            pose.translate(sign*.10F*windup,-.12F*windup,.12F*windup-.22F*throwArc);
            pose.mulPose(Axis.XP.rotationDegrees(-72*windup+42*throwArc));
            pose.mulPose(Axis.YP.rotationDegrees(sign*(-18*windup+28*throwArc)));
        }
    }
    private ThrowingMotionClient() {}
}
