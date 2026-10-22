package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Small cloth belt / wooden pendant. GUI sprites remain flat and independent. */
public final class NewAccessoryRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/accessories148.png");
    private final boolean belt;
    private final HumanoidModel<LivingEntity> model;
    public NewAccessoryRenderer(boolean belt){this.belt=belt;model=model(belt);}
    static HumanoidModel<LivingEntity> model(boolean belt){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);
        var body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);
        if(belt){
            body.addOrReplaceChild("woven_band",CubeListBuilder.create().texOffs(0,0).addBox(-4.5F,9,-2.7F,9,2,5.4F),PartPose.ZERO);
            body.addOrReplaceChild("wind_knot",CubeListBuilder.create().texOffs(0,16).addBox(-1.5F,9,-3.3F,3,2.5F,.7F),PartPose.ZERO);
            body.addOrReplaceChild("left_tail",CubeListBuilder.create().texOffs(0,0).addBox(-1.3F,11.5F,-3,1,2.5F,.5F),PartPose.ZERO);
            body.addOrReplaceChild("right_tail",CubeListBuilder.create().texOffs(0,0).addBox(.3F,11.5F,-3,1,3.5F,.5F),PartPose.ZERO);
        }else{
            for(float x:new float[]{-1.8F,1.4F})body.addOrReplaceChild("cord_"+x,CubeListBuilder.create().texOffs(0,0).addBox(x,0,-2.6F,.4F,4,.4F),PartPose.ZERO);
            body.addOrReplaceChild("birch_pendant",CubeListBuilder.create().texOffs(0,0).addBox(-1.3F,3.7F,-3,2.6F,3.6F,.7F),PartPose.ZERO);
            body.addOrReplaceChild("amber",CubeListBuilder.create().texOffs(0,16).addBox(-.7F,4.5F,-3.3F,1.4F,1.5F,.4F),PartPose.ZERO);
            body.addOrReplaceChild("leaf",CubeListBuilder.create().texOffs(12,16).addBox(-2.4F,3.6F,-3,1.5F,1.5F,.4F),PartPose.ZERO);
        }
        return new HumanoidModel<>(LayerDefinition.create(mesh,32,32).bakeRoot());
    }
    @Override public <T extends LivingEntity,M extends EntityModel<T>> void render(ItemStack stack,SlotContext context,PoseStack pose,
            RenderLayerParent<T,M> parent,MultiBufferSource buffer,int light,float walk,float amount,float partial,float age,float yaw,float pitch){
        if(context.entity().isInvisible())return;
        ICurioRenderer.followBodyRotations(context.entity(),model);pose.pushPose();
        AccessoryBodyFit.apply(pose,AccessoryBodyFit.armored(context.entity()));
        model.body.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,OverlayTexture.NO_OVERLAY);pose.popPose();
    }
}
