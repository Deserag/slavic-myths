package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
/** Curios' existing body attachment API, no second accessory renderer framework. */
public final class HuntAccessoryRenderer {
 private static final ResourceLocation TEXTURE=new ResourceLocation("slavicmyths","textures/entity/hunt_accessories.png");
 private static final BipedModel<LivingEntity> BELT=model(true),CHARM=model(false),STAR=star();
 private static void box(BipedModel<LivingEntity> m,int mat,float x,float y,float z,float w,float h,float d){ModelRenderer p=new ModelRenderer(m,mat%4*64,mat/4*64);p.addBox(x,y,z,w,h,d);m.body.addChild(p);}
 private static BipedModel<LivingEntity> star(){BipedModel<LivingEntity> m=model(false);m.body=new ModelRenderer(m);box(m,5,-2,0,-2.4F,.4F,4,.4F);box(m,5,1.6F,0,-2.4F,.4F,4,.4F);ModelRenderer diamond=new ModelRenderer(m);diamond.setPos(0,5.5F,-2.6F);diamond.zRot=.785F;diamond.texOffs(128,0).addBox(-1.8F,-1.8F,-.2F,3.6F,3.6F,.6F);m.body.addChild(diamond);box(m,7,-.25F,4,-3.3F,.5F,3,.3F);box(m,7,-1.5F,5.25F,-3.3F,3,.5F,.3F);box(m,3,-.35F,5,-3.4F,.7F,.7F,.2F);box(m,5,-2.4F,6,-2.8F,.6F,4,.4F);return m;}
 private static BipedModel<LivingEntity> model(boolean belt){BipedModel<LivingEntity> m=new BipedModel<>(0);m.texWidth=256;m.texHeight=256;m.body=new ModelRenderer(m);if(belt){box(m,5,-4.5F,9,-2.7F,9,2.5F,5.4F);box(m,8,-1.2F,9.1F,-3.3F,2.4F,2.2F,.7F);box(m,1,-4.6F,8.7F,-2.8F,9.2F,.7F,5.6F);box(m,3,-3,11,-3,1,3,.7F);box(m,3,-1.6F,11,-3,1,3.4F,.7F);box(m,7,2,11,-3,1.3F,3,.5F);}else{box(m,5,-2.2F,0,-2.3F,.4F,4,.4F);box(m,5,1.8F,0,-2.3F,.4F,4,.4F);box(m,2,-1.7F,3.5F,-2.5F,3.4F,4.2F,.7F);box(m,7,-.3F,4,-3.3F,.6F,2.8F,.2F);box(m,7,-1.1F,5,-3.3F,2.2F,.5F,.2F);box(m,3,-.4F,5.5F,-3.2F,.8F,.8F,.2F);}return m;}
 public static void render(ItemStack stack,LivingEntity wearer,MatrixStack pose,IRenderTypeBuffer buffer,int light){boolean star=stack.getItem()==org.slavicmyths.registry.ModItems.OBEREG_PADAYUSCHEY_ZVEZDY.get();BipedModel<LivingEntity> m=star?STAR:stack.getItem()==org.slavicmyths.registry.ModItems.VOLCHIY_POYAS.get()?BELT:CHARM;top.theillusivec4.curios.api.type.capability.ICurio.RenderHelper.followBodyRotations(wearer,m);m.body.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(star?new ResourceLocation("slavicmyths","textures/entity/hunt_star_accessory.png"):TEXTURE)),light,OverlayTexture.NO_OVERLAY);}
}
