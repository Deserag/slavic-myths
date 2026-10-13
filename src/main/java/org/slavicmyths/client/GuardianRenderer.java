package org.slavicmyths.client;
import org.slavicmyths.guardians.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
public final class GuardianRenderer<T extends GuardianEntity> extends MobRenderer<T,GuardianModel<T>> {
 public GuardianRenderer(EntityRendererProvider.Context c,boolean water){super(c,new GuardianModel<T>(water),water?.55f:.9f);addLayer(new RenderLayer<T,GuardianModel<T>>(this){@Override public void render(com.mojang.blaze3d.vertex.PoseStack p,MultiBufferSource out,int light,T e,float a,float b,float partial,float time,float yaw,float pitch){var location=GuardianContent.id("textures/entity/"+name(e)+"_glow.png");getParentModel().renderToBuffer(p,out.getBuffer(RenderType.eyes(location)),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xFFFFFFFF);}});}
 @Override protected RenderType getRenderType(T e,boolean bodyVisible,boolean translucent,boolean glowing){return e.water()&&bodyVisible?RenderType.entityTranslucent(getTextureLocation(e)):super.getRenderType(e,bodyVisible,translucent,glowing);}
 private static String name(GuardianEntity e){return (e.water()?"defiled_spring_keeper":"extinguished_flame_guardian")+(e.state()==GuardianRules.CLEANSED?"_cleansed":e.scene()&&e.sceneAge()>=40?"_purifying_2":e.scene()&&e.sceneAge()>=20?"_purifying_1":e instanceof FireGuardian f&&(e.state()==GuardianRules.PULSE||e.state()==GuardianRules.LOCKDOWN||f.lastEmbers())?"_charged":"_corrupted");}
 @Override public net.minecraft.resources.ResourceLocation getTextureLocation(T e){return GuardianContent.id("textures/entity/"+name(e)+".png");}
}
