package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.textile.*;

/** Thin independently authored garment meshes; not the vanilla armor layer. */
public final class TextileClothingLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private final Map<String,HumanoidModel<AbstractClientPlayer>> garments=new LinkedHashMap<>();
    public TextileClothingLayer(PlayerRenderer r,boolean slim){super(r);for(String id:List.of("linen_shirt","linen_ports","sarafan","linen_headscarf","linen_apron","folk_vest","bast_shoes","woven_belt"))garments.put(id,mesh(id,slim));}
    static <E extends net.minecraft.world.entity.LivingEntity> HumanoidModel<E> mesh(String id,boolean slim){
        MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();
        CubeListBuilder body=CubeListBuilder.create(),head=CubeListBuilder.create(),leftArm=CubeListBuilder.create(),rightArm=CubeListBuilder.create(),leftLeg=CubeListBuilder.create(),rightLeg=CubeListBuilder.create();
        CubeDeformation thin=new CubeDeformation(.12F),cloth=new CubeDeformation(.32F);float arm=slim?3:4;
        if(id.equals("linen_shirt")){
            // A real open neck, separate sleeves and a back seam, outside skin overlays.
            body.texOffs(0,0).addBox(-4,2,-2,8,10,4,cloth);
            body.texOffs(0,20).addBox(-4,0,-2,2.4F,2,4,cloth).addBox(1.6F,0,-2,2.4F,2,4,cloth);
            leftArm.texOffs(32,0).addBox(-1,-2,-2,arm,10,4,new CubeDeformation(.34F));
            rightArm.texOffs(32,0).addBox(1-arm,-2,-2,arm,10,4,new CubeDeformation(.34F));
        }else if(id.equals("linen_ports")){
            body.texOffs(0,0).addBox(-4,10.4F,-2,8,1.6F,4,new CubeDeformation(.40F));
            for(int side:new int[]{-1,1}){
                CubeListBuilder leg=side<0?rightLeg:leftLeg;float x=side<0?-2:-1.5F;
                leg.texOffs(32,24).addBox(x,0,-2,3.5F,6.8F,4,cloth);
                leg.texOffs(32,44).addBox(x,6.8F,-2,3.5F,1.4F,4,new CubeDeformation(.34F));
            }
        }else if(id.equals("linen_headscarf")){
            // No cap over the hair: forehead band, narrow sides, detached rear knot.
            head.texOffs(64,0).addBox(-4.65F,-7,-4.65F,9.3F,1,1);
            head.texOffs(64,20).addBox(-4.65F,-7,3.7F,9.3F,6,1);
            head.texOffs(90,20).addBox(-4.7F,-6,-3.7F,.5F,5,7.4F).texOffs(90,36).addBox(4.2F,-6,-3.7F,.5F,5,7.4F);
            head.texOffs(64,40).addBox(-1.3F,-1,4.8F,2.6F,2,.5F);
            head.texOffs(64,46).addBox(-2.2F,1,4.9F,1.7F,3,.35F).texOffs(64,52).addBox(.5F,1,4.9F,1.7F,2,.35F);
        }else if(id.equals("bast_shoes")){
            for(int side:new int[]{-1,1}){
                CubeListBuilder leg=side<0?rightLeg:leftLeg;
                leg.texOffs(0,0).addBox(side<0?-2.3F:-1.8F,9,-3.2F,4.1F,2.6F,5.7F);
                leg.texOffs(32,0).addBox(side<0?-2.4F:-1.8F,11.6F,-3.4F,4.2F,.6F,6);
            }
        }else if(id.equals("sarafan")){
            body.texOffs(0,0).addBox(-4,3,-2,8,9,4,new CubeDeformation(.34F));
            body.texOffs(32,0).addBox(-3.8F,0,-2.45F,1.6F,3,.4F).addBox(2.2F,0,-2.45F,1.6F,3,.4F);
            body.texOffs(32,16).addBox(-3.8F,0,2.1F,1.6F,3,.4F).addBox(2.2F,0,2.1F,1.6F,3,.4F);
            // Split front/back skirt panels are leg children, not a torso-fixed barrel.
            for(int side:new int[]{-1,1}){CubeListBuilder leg=side<0?rightLeg:leftLeg;
                float x=side<0?-2.8F:-1.6F;
                leg.texOffs(0,24).addBox(x,0,-2.6F,4.4F,5,.4F).texOffs(0,40).addBox(x,0,2.2F,4.4F,5,.4F);
                float low=side<0?-3.2F:-1.6F;
                leg.texOffs(0,56).addBox(low,5,-2.8F,4.8F,5,.4F).texOffs(32,56).addBox(low,5,2.4F,4.8F,5,.4F);
            }
        }else if(id.equals("linen_apron")){
            body.texOffs(0,0).addBox(-3,2,-2.45F,6,9,.35F);
            body.texOffs(32,0).addBox(-4.35F,9,-2.35F,8.7F,1,4.7F);
            body.texOffs(32,12).addBox(-1,10,2.5F,.8F,3,.3F).addBox(.3F,10,2.5F,.8F,4,.3F);
            leftLeg.texOffs(0,24).addBox(-1.6F,0,-2.5F,3.2F,8,.3F);
            rightLeg.texOffs(0,24).addBox(-1.6F,0,-2.5F,3.2F,8,.3F);
        }else if(id.equals("folk_vest")){
            body.texOffs(0,0).addBox(-4.3F,0,2.15F,8.6F,10,.5F);
            body.texOffs(32,0).addBox(-4.3F,1,-2.65F,3,9,.5F).texOffs(48,0).addBox(1.3F,1,-2.65F,3,9,.5F);
            body.texOffs(64,0).addBox(-4.5F,1,-2.15F,.5F,9,4.3F).texOffs(80,0).addBox(4,1,-2.15F,.5F,9,4.3F);
            body.texOffs(32,16).addBox(-4.3F,0,-2.2F,2.4F,1,4.4F).addBox(1.9F,0,-2.2F,2.4F,1,4.4F);
        }else if(id.equals("woven_belt")){
            body.texOffs(0,0).addBox(-4.25F,10,-2.25F,8.5F,1.3F,4.5F);
            body.texOffs(32,0).addBox(-.9F,11.3F,-2.4F,1.5F,6,.4F);body.texOffs(40,0).addBox(.7F,11.3F,-2.4F,1.3F,4.5F,.4F);
        }
        root.addOrReplaceChild("head",head,PartPose.ZERO);root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);root.addOrReplaceChild("body",body,PartPose.ZERO);
        root.addOrReplaceChild("left_arm",leftArm,PartPose.offset(5,2,0));root.addOrReplaceChild("right_arm",rightArm,PartPose.offset(-5,2,0));root.addOrReplaceChild("left_leg",leftLeg,PartPose.offset(1.9F,12,0));root.addOrReplaceChild("right_leg",rightLeg,PartPose.offset(-1.9F,12,0));
        return new HumanoidModel<>(LayerDefinition.create(mesh,128,128).bakeRoot());
    }
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
        if(player.isInvisible()||player.isSpectator())return;
        for(EquipmentSlot slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){
            var stack=player.getItemBySlot(slot);if(stack.getItem() instanceof ClothingItem)show(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(),pose,org.slavicmyths.client.runeglow.RuneGlowBuffers.clothing(stack,buffers,pose,player,partial),light);
        }
        if(player.getData(BeltData.BELT).is(Textiles.item("woven_belt")))show("woven_belt",pose,buffers,light);
    }
    private void show(String id,PoseStack pose,MultiBufferSource buffers,int light){var model=garments.get(id);if(model==null)return;getParentModel().copyPropertiesTo(model);model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(Textiles.id("textures/entity/clothing/"+id+".png"))),light,OverlayTexture.NO_OVERLAY,-1);}
}
