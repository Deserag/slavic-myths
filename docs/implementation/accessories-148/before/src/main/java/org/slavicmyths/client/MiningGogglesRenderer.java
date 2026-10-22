package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slavicmyths.industry.*;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class MiningGogglesRenderer implements ICurioRenderer {
    private final ModelPart model;
    public MiningGogglesRenderer(){var mesh=new MeshDefinition();var b=CubeListBuilder.create();
        for(float x:new float[]{-3.7F,.3F}){
            b.texOffs(0,0).addBox(x,-5.6F,-4.8F,3.4F,.7F,1).addBox(x,-2.7F,-4.8F,3.4F,.7F,1).addBox(x,-4.9F,-4.8F,.7F,2.2F,1).addBox(x+2.7F,-4.9F,-4.8F,.7F,2.2F,1);
            b.texOffs(16,0).addBox(x+.7F,-4.9F,-4.7F,2,2.2F,.6F);
        }
        b.texOffs(0,0).addBox(-.4F,-4.1F,-4.7F,.8F,.6F,.7F);
        b.texOffs(0,8).addBox(-4.25F,-4.1F,-3.8F,.5F,1,8).addBox(3.75F,-4.1F,-3.8F,.5F,1,8).addBox(-4,-4.1F,3.8F,8,1,.5F);
        mesh.getRoot().addOrReplaceChild("goggles",b,PartPose.ZERO);model=LayerDefinition.create(mesh,32,32).bakeRoot();
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->CuriosRendererRegistry.register(IndustryAccessories.GOGGLES.get(),MiningGogglesRenderer::new));}
    public <T extends LivingEntity,M extends EntityModel<T>> void render(ItemStack s,SlotContext c,PoseStack p,RenderLayerParent<T,M> parent,MultiBufferSource buffer,int light,float swing,float amount,float partial,float age,float yaw,float pitch){
        if(c.entity().isInvisible()||!(parent.getModel() instanceof HeadedModel head))return;p.pushPose();head.getHead().translateAndRotate(p);model.render(p,buffer.getBuffer(RenderType.entityCutout(Industry.id("textures/models/miners_goggles.png"))),light,OverlayTexture.NO_OVERLAY);p.popPose();
    }
}
