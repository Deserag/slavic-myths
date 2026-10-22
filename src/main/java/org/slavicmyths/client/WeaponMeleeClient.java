package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.slavicmyths.combat.WeaponCatalog;
import org.slavicmyths.registry.ModItems;

/** Source-scoped first-person weapon poses; no entity rigs, world writes or damage timing. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class WeaponMeleeClient {
    public enum Group { MACE, SPEAR }

    private static final IClientItemExtensions MACES = new ClientPose(Group.MACE);
    private static final IClientItemExtensions SPEARS = new ClientPose(Group.SPEAR);

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        event.registerItem(MACES,
                ModItems.WOODEN_MACE.get(),ModItems.STONE_MACE.get(),ModItems.MACE.get(),
                ModItems.SILVER_MACE.get(),ModItems.PERUNITE_MACE.get());
        event.registerItem(SPEARS,
                ModItems.SPEAR.get(),ModItems.SILVER_SPEAR.get(),
                WeaponCatalog.item("gold_spear"),WeaponCatalog.item("iron_spear"),
                WeaponCatalog.item("diamond_spear"),WeaponCatalog.item("perunite_spear"),
                WeaponCatalog.item("netherite_spear"));
    }

    /** Four authored stages: neutral(0), preparation(.15), contact(.35), recovery(.75-1). */
    public static float[] sample(float swing) {
        float t=Mth.clamp(swing,0,1);
        float prep=t<=0?0:smooth(t/.18F)*(1-smooth((t-.18F)/.22F));
        float strike=smooth((t-.12F)/.21F)*(1-smooth((t-.38F)/.46F));
        return new float[]{prep,strike};
    }

    private static float smooth(float value) {
        float t=Mth.clamp(value,0,1);
        return t*t*(3-2*t);
    }

    public static void firstPerson(PoseStack pose,Group kind,boolean left,float equip,float swing) {
        int sign=left?-1:1;
        float[] m=sample(swing);
        float prep=m[0],strike=m[1];
        pose.translate(sign*.49F,-.40F-equip*.6F,-.91F);
        if(kind==Group.MACE) {
            // Lift the heavy head, strike downward and recoil without changing vanilla combat.
            pose.translate(sign*(-.08F*prep+.04F*strike),.11F*prep-.15F*strike,-.05F*strike);
            pose.mulPose(Axis.XP.rotationDegrees(-24*prep+58*strike));
            pose.mulPose(Axis.YP.rotationDegrees(sign*(12*prep-8*strike)));
            pose.mulPose(Axis.ZP.rotationDegrees(sign*(-16*prep+21*strike)));
        } else {
            // Keep the spear's grip aligned with the wrist; the point moves toward the camera target.
            pose.translate(sign*(-.045F*prep),.035F*prep,-.38F*strike);
            pose.mulPose(Axis.XP.rotationDegrees(11*prep-12*strike));
            pose.mulPose(Axis.YP.rotationDegrees(sign*(8*prep-6*strike)));
        }
    }

    private record ClientPose(Group kind) implements IClientItemExtensions {
        @Override
        public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,
                ItemStack stack,float partialTick,float equip,float swing) {
            firstPerson(pose,kind,arm==HumanoidArm.LEFT,equip,swing);
            return true;
        }
    }

    private WeaponMeleeClient() {}
}
