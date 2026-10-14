package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.industry.*;
@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ChudPeopleClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(ChudPeople.RESIDENT.get(),Renderer::new);e.registerEntityRenderer(ChudPeople.ARTISAN.get(),Renderer::new);e.registerEntityRenderer(ChudPeople.GUARD.get(),Renderer::new);}
    static final class Model extends HumanoidModel<ChudPerson> {
        private final ModelPart apron,goggles,helmet;
        Model(){super(mesh());apron=body.getChild("apron");goggles=head.getChild("goggles");helmet=head.getChild("helmet");}
        private static ModelPart mesh(){var mesh=new MeshDefinition();var root=mesh.getRoot();
            var head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-7,-3,6,7,6),PartPose.ZERO);
            head.addOrReplaceChild("ears",CubeListBuilder.create().texOffs(24,0).addBox(-4,-5,-.5F,1,3,1).addBox(3,-5,-.5F,1,3,1),PartPose.ZERO);
            head.addOrReplaceChild("goggles",CubeListBuilder.create().texOffs(24,6).addBox(-3,-5.5F,-3.8F,2.5F,2.5F,1).addBox(.5F,-5.5F,-3.8F,2.5F,2.5F,1),PartPose.ZERO);
            head.addOrReplaceChild("helmet",CubeListBuilder.create().texOffs(32,0).addBox(-3.5F,-7.5F,-3.5F,7,2,7),PartPose.ZERO);
            root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
            var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4,0,-2,8,12,4),PartPose.ZERO);
            body.addOrReplaceChild("apron",CubeListBuilder.create().texOffs(32,32).addBox(-3.5F,3,-2.3F,7,12,.6F),PartPose.ZERO);
            root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(40,16).addBox(-2,-1,-1.5F,3,11,3),PartPose.offset(-5,2,0));
            root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(40,16).mirror().addBox(-1,-1,-1.5F,3,11,3),PartPose.offset(5,2,0));
            root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(0,16).addBox(-1.5F,0,-2,3,12,4),PartPose.offset(-2,12,0));
            root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(0,16).mirror().addBox(-1.5F,0,-2,3,12,4),PartPose.offset(2,12,0));
            return LayerDefinition.create(mesh,64,64).bakeRoot();
        }
        public void setupAnim(ChudPerson e,float swing,float amount,float age,float yaw,float pitch){rightArmPose=e.role()==3&&!e.getMainHandItem().isEmpty()?ArmPose.ITEM:ArmPose.EMPTY;super.setupAnim(e,swing,amount,age,yaw,pitch);if(e.activity()==1){rightArm.xRot=-.6F-(float)Math.abs(Math.sin(e.activityAge(0)*.18))*1.3F;}else if(e.activity()==2){body.xRot=.15F;rightArm.xRot=leftArm.xRot=-.4F;}else if(e.activity()==3){float t=e.activityAge(0);rightArm.xRot=t<4?-2F*t/4:-2F+Math.min(1,(t-4)/3)*2.8F;}else body.xRot=0;apron.visible=e.role()==1||e.role()==2;goggles.visible=e.role()==2;helmet.visible=e.role()==3;}
    }
    static final class Renderer extends MobRenderer<ChudPerson,Model> {
        Renderer(EntityRendererProvider.Context c){super(c,new Model(),.35F);addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,c.getItemInHandRenderer()));}
        public ResourceLocation getTextureLocation(ChudPerson e){return Industry.id("textures/entity/chud_"+e.role()+".png");}
        protected void scale(ChudPerson e,PoseStack p,float partial){p.scale(.9F,.9F,.9F);}
    }
}
