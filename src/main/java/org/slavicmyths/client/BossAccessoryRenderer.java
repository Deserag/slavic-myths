package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
/** Small native body models, using the world-boss material atlas and existing Curios attachment API. */
public final class BossAccessoryRenderer{
 private static final BipedModel<LivingEntity> EYE=build(false),BELT=build(true);
 private static void box(BipedModel<LivingEntity> m,int material,float x,float y,float z,float w,float h,float d){ModelRenderer p=new ModelRenderer(m,material%8*64,material/8*64);p.addBox(x,y,z,w,h,d);m.body.addChild(p);}
 private static BipedModel<LivingEntity> build(boolean belt){BipedModel<LivingEntity> m=new BipedModel<>(0);m.texWidth=512;m.texHeight=512;m.body=new ModelRenderer(m);if(belt){box(m,10,-4.5F,9,-2.7F,9,2.8F,5.4F);box(m,11,-1.5F,9.2F,-3.3F,3,2.2F,.6F);for(int i=0;i<18;i++){double a=i*Math.PI*3.5/17,r=1.1*(1-i/24.0);box(m,12,(float)(Math.cos(a)*r)-.15F,(float)(10.3+Math.sin(a)*r),-3.6F,.3F,.35F,.25F);}box(m,10,-2.8F,11.8F,-2.9F,.6F,3,.4F);box(m,10,2.2F,11.8F,-2.9F,.6F,3.7F,.4F);for(int x:new int[]{-4,-2,2,3})box(m,11,x,9.5F,-3,.8F,1.4F,.3F);}else{box(m,2,-1.8F,0,-2.5F,.3F,4,.3F);box(m,2,1.5F,0,-2.5F,.3F,4,.3F);box(m,19,-1.5F,3.8F,-2.7F,3,3.5F,.5F);box(m,7,-1.1F,4.7F,-3.3F,2.2F,1.6F,.3F);box(m,8,-.5F,4.8F,-3.65F,1,1.4F,.3F);box(m,6,-.15F,5,-4,.3F,.9F,.2F);box(m,5,-1.6F,3.7F,-2.9F,3.2F,.45F,.4F);box(m,5,-1.6F,7,-2.9F,3.2F,.45F,.4F);box(m,4,-.3F,7.4F,-2.9F,.6F,2,.3F);}return m;}
 public static void render(ItemStack stack,LivingEntity e,MatrixStack pose,IRenderTypeBuffer buffer,int light){boolean belt=stack.getItem()==org.slavicmyths.registry.ModItems.POYAS_TUGARINA.get();BipedModel<LivingEntity> m=belt?BELT:EYE;top.theillusivec4.curios.api.type.capability.ICurio.RenderHelper.followBodyRotations(e,m);m.body.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("slavicmyths","textures/entity/"+(belt?"tugarin_zmey":"likho_one_eyed")+".png"))),light,OverlayTexture.NO_OVERLAY);}
}
