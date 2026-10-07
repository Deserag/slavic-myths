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
    private static HumanoidModel<AbstractClientPlayer> mesh(String id,boolean slim){
        MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();
        CubeListBuilder body=CubeListBuilder.create(),head=CubeListBuilder.create(),leftArm=CubeListBuilder.create(),rightArm=CubeListBuilder.create(),leftLeg=CubeListBuilder.create(),rightLeg=CubeListBuilder.create();
        CubeDeformation thin=new CubeDeformation(.12F);float arm=slim?3:4;
        if(id.equals("linen_shirt")||id.equals("sarafan")){
            body.texOffs(0,0).addBox(-4,0,-2,8,12,4,thin);
            leftArm.texOffs(32,0).addBox(-1,-2,-2,arm,12,4,thin);rightArm.texOffs(32,0).addBox(1-arm,-2,-2,arm,12,4,thin);
            if(id.equals("sarafan")){
                body.texOffs(0,24).addBox(-4.2F,12,-2.2F,8.4F,4,4.4F);
                body.texOffs(0,40).addBox(-4.6F,16,-2.5F,9.2F,4,5);
                body.texOffs(0,56).addBox(-5,20,-2.8F,10,4,5.6F);
            }
        }else if(id.equals("linen_ports")){
            body.texOffs(0,0).addBox(-4,10,-2,8,2,4,thin);
            leftLeg.texOffs(32,24).addBox(-2,0,-2,4,8,4,thin);leftLeg.texOffs(32,44).addBox(-1.8F,8,-1.8F,3.6F,4,3.6F,thin);
            rightLeg.texOffs(32,24).addBox(-2,0,-2,4,8,4,thin);rightLeg.texOffs(32,44).addBox(-1.8F,8,-1.8F,3.6F,4,3.6F,thin);
        }else if(id.equals("linen_headscarf")){
            head.texOffs(64,0).addBox(-4.2F,-8.3F,-4.2F,8.4F,1,8.4F);
            head.texOffs(64,20).addBox(-4.2F,-7.3F,3.8F,8.4F,7.5F,.5F);
            head.texOffs(90,20).addBox(-4.2F,-7.3F,-4,.5F,4,8);head.texOffs(90,36).addBox(3.7F,-7.3F,-4,.5F,4,8);
            head.texOffs(64,40).addBox(-3,0,3.9F,6,2,.5F);head.texOffs(64,46).addBox(-2,2,3.9F,4,2,.5F);head.texOffs(64,52).addBox(-1,4,3.9F,2,2,.5F);
        }else if(id.equals("linen_apron")){
            body.texOffs(0,0).addBox(-3.5F,2,-2.3F,7,10,.3F);
            body.texOffs(0,24).addBox(-4.4F,12,-2.4F,8.8F,10,.4F);
            body.texOffs(32,0).addBox(-4.2F,8,-2.2F,8.4F,1,4.4F);
            body.texOffs(32,12).addBox(-1,8,2.2F,2,5,.4F);
        }else if(id.equals("folk_vest")){
            body.texOffs(0,0).addBox(-4.2F,0,1.9F,8.4F,12,.5F);
            body.texOffs(32,0).addBox(-4.2F,0,-2.3F,3,12,.5F);body.texOffs(48,0).addBox(1.2F,0,-2.3F,3,12,.5F);
            body.texOffs(64,0).addBox(-4.2F,0,-2, .5F,12,4);body.texOffs(80,0).addBox(3.7F,0,-2,.5F,12,4);
        }else if(id.equals("bast_shoes")){
            for(CubeListBuilder leg:List.of(leftLeg,rightLeg)){leg.texOffs(0,0).addBox(-2.15F,9,-3.3F,4.3F,3.1F,6,thin);leg.texOffs(32,0).addBox(-2.1F,8,-2.1F,4.2F,1,4.2F);}
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
            var stack=player.getItemBySlot(slot);if(stack.getItem() instanceof ClothingItem)show(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(),pose,buffers,light);
        }
        if(player.getData(BeltData.BELT).is(Textiles.item("woven_belt")))show("woven_belt",pose,buffers,light);
    }
    private void show(String id,PoseStack pose,MultiBufferSource buffers,int light){var model=garments.get(id);if(model==null)return;getParentModel().copyPropertiesTo(model);model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(Textiles.id("textures/entity/clothing/"+id+".png"))),light,OverlayTexture.NO_OVERLAY,-1);}
}
