package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.slavicmyths.combat.FieldBowItem;
import org.slavicmyths.combat.WeaponCatalog;
import org.slavicmyths.finist.FinistItems;

/** Vanilla BOW pose, timed to these bows' existing draw durations. Rendering only. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class BowDrawClient {
    @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                    ItemStack stack, float partialTick, float equip, float swing) {
                if(player==null||!player.isUsingItem()||player.getUseItemRemainingTicks()<=0||player.getUseItem()!=stack)return false;
                HumanoidArm usingArm=player.getUsedItemHand()==InteractionHand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite();
                if(usingArm!=arm)return false;
                float ticks=stack.getUseDuration(player)-(player.getUseItemRemainingTicks()-partialTick+1.0F);
                int duration=stack.getItem() instanceof FieldBowItem bow?bow.drawTicks():30; // FinistBow.power uses30.
                drawPose(pose,arm==HumanoidArm.LEFT,equip,ticks,duration);
                return true;
            }
        },WeaponCatalog.item("short_bow"),WeaponCatalog.item("heavy_bow"),FinistItems.BOW.get(),org.slavicmyths.nethersmithing.NetherWeapons.ITEMS.get("infernal_bow").get());
    }

    public static float visualPower(float ticks,int duration) {
        float value=Math.max(0,ticks)/duration;
        return Math.min(1,(value*value+2*value)/3);
    }

    /** Installed ItemInHandRenderer BOW branch, with its20-tick divisor parameterized. */
    public static void drawPose(PoseStack pose,boolean left,float equip,float ticks,int duration) {
        int sign=left?-1:1;
        pose.translate(sign*.56F,-.52F+equip*-.6F,-.72F);
        pose.translate(sign*-.2785682F,.18344387F,.15731531F);
        pose.mulPose(Axis.XP.rotationDegrees(-13.935F));
        pose.mulPose(Axis.YP.rotationDegrees(sign*35.3F));
        pose.mulPose(Axis.ZP.rotationDegrees(sign*-9.785F));
        float power=visualPower(ticks,duration);
        if(power>.1F)pose.translate(0,Mth.sin((ticks-.1F)*1.3F)*(power-.1F)*.004F,0);
        pose.translate(0,0,power*.04F);
        pose.scale(1,1,1+power*.2F);
        pose.mulPose(Axis.YN.rotationDegrees(sign*45F));
    }
    private BowDrawClient() {}
}
