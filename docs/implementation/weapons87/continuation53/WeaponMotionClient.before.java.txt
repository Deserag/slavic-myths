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

/** Local first-person phases only. The server's attack timing, range and damage stay vanilla. */
@EventBusSubscriber(modid="slavicmyths", value=Dist.CLIENT, bus=EventBusSubscriber.Bus.MOD)
public final class WeaponMotionClient {
    private enum Shape { BLADE, HEAVY, SPEAR, POLEAXE, SICKLE, STAFF }
    @SubscribeEvent public static void register(RegisterClientExtensionsEvent event) {
        register(event,Shape.BLADE,"bone_knife","silver_dagger","ancient_carolingian_sword",
                "restored_carolingian_sword","silver_sword","perunite_sword");
        register(event,Shape.HEAVY,"club","wooden_mace","stone_mace","mace","silver_mace",
                "perunite_mace","battle_axe","chekan","ancient_chekan");
        register(event,Shape.SPEAR,"ancient_spear","restored_spear","spear","gold_spear",
                "iron_spear","silver_spear","diamond_spear","perunite_spear",
                "netherite_spear","pool_spear","thunder_spear","victor_spear");
        register(event,Shape.POLEAXE,"berdysh");
        register(event,Shape.SICKLE,"noon_sickle");
        register(event,Shape.STAFF,"carved_staff","veles_staff","storm_staff");
    }
    private static void register(RegisterClientExtensionsEvent event,Shape shape,String... ids) {
        Item[] items=Arrays.stream(ids).map(id->BuiltInRegistries.ITEM.getOptional(
                ResourceLocation.fromNamespaceAndPath("slavicmyths",id)).orElseThrow()).toArray(Item[]::new);
        event.registerItem(new IClientItemExtensions() {
            @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,
                    ItemStack stack,float partialTick,float equip,float swing) {
                animate(pose,player,arm,stack,partialTick,equip,swing,shape);
                return true;
            }
        },items);
    }
    static float ease(float x) {
        x=Mth.clamp(x,0,1);
        return x*x*(3-2*x);
    }
    private static void animate(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,
            float partial,float equip,float swing,Shape shape) {
        boolean left=arm==HumanoidArm.LEFT;
        int sign=left?-1:1;
        InteractionHand hand=arm==player.getMainArm()?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
        float time=player.swinging&&player.swingingArm==hand?Mth.clamp(swing,0,1):0;
        float prep=ease(time/.27F);
        float contact=ease((time-.27F)/.27F);
        float recovery=1-ease((time-.54F)/.46F);
        float windup=(prep-contact)*recovery;
        float strike=contact*recovery;
        float walking=Mth.clamp(player.walkAnimation.speed(partial),0,1);
        float sway=Mth.sin((player.tickCount+partial)*.65F)*walking;
        pose.translate(sign*.56F,-.52F-equip*.6F,-.72F);
        switch(shape) {
            case BLADE -> {
                pose.translate(sign*.05F*strike,-.08F*strike,-.09F*strike);
                pose.mulPose(Axis.YP.rotationDegrees(sign*(-22*windup+60*strike+2*sway)));
                pose.mulPose(Axis.XP.rotationDegrees(-24*windup+18*strike));
                pose.mulPose(Axis.ZP.rotationDegrees(sign*(-12*windup+16*strike)));
            }
            case HEAVY -> {
                pose.translate(sign*.04F*strike,-.12F*windup+.10F*strike,-.12F*strike);
                pose.mulPose(Axis.XP.rotationDegrees(-43*windup+57*strike));
                pose.mulPose(Axis.ZP.rotationDegrees(sign*(17*windup-29*strike+2*sway)));
            }
            case SPEAR -> {
                pose.translate(sign*.04F*windup,-.04F*strike,-.32F*strike+.10F*windup);
                pose.mulPose(Axis.XP.rotationDegrees(-18*windup+8*strike));
                pose.mulPose(Axis.YP.rotationDegrees(sign*(5*sway+7*strike)));
            }
            case POLEAXE -> {
                pose.translate(sign*.08F*strike,-.05F*strike,-.10F*strike);
                pose.mulPose(Axis.YP.rotationDegrees(sign*(-25*windup+53*strike+2*sway)));
                pose.mulPose(Axis.XP.rotationDegrees(-20*windup+20*strike));
            }
            case SICKLE -> {
                pose.translate(sign*.06F*strike,-.03F*strike,-.10F*strike);
                pose.mulPose(Axis.ZP.rotationDegrees(sign*(24*windup-44*strike)));
                pose.mulPose(Axis.YP.rotationDegrees(sign*(13*windup+22*strike)));
            }
            case STAFF -> {
                float cast=stack.getItem() instanceof org.slavicmyths.item.StormStaffItem
                        ?Mth.clamp((player.getCooldowns().getCooldownPercent(stack.getItem(),partial)-.80F)/.20F,0,1):0;
                pose.translate(sign*.04F*strike,-.04F*strike,-.12F*cast);
                pose.mulPose(Axis.XP.rotationDegrees(-19*cast-13*windup+15*strike));
                pose.mulPose(Axis.YP.rotationDegrees(sign*(3*sway+18*strike)));
            }
        }
    }
    private WeaponMotionClient() {}
}
