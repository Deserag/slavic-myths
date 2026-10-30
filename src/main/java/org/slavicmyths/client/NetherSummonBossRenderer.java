package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.netherboss.NetherSummonBoss;

/** Native renderer for the original 53/54-cuboid Nether boss models. WIP VFX layers. */
public final class NetherSummonBossRenderer extends MobRenderer<NetherSummonBoss,NetherSummonBossModel> {
    private static final ResourceLocation BRIDE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/morova_bride.png");
    private static final ResourceLocation PRINCE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/trizna_flaming_prince.png");
    public NetherSummonBossRenderer(EntityRendererProvider.Context context,boolean bride){
        super(context,new NetherSummonBossModel(bride),bride?.44F:.7F);
    }
    @Override public void render(NetherSummonBoss boss,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light){
        if(boss.isMorova()){pose.pushPose();pose.translate(0,.35+Math.sin((boss.tickCount+partial)*.06)*.04,0);}
        if(boss.hasCopies())for(int side:new int[]{-1,1}){
            pose.pushPose();pose.translate(side*2,0,.7);model.opacity=.28F;super.render(boss,yaw,partial,pose,buffers,light);pose.popPose();
        }
        model.opacity=Math.max(0,1-boss.deathTime/20F);super.render(boss,yaw,partial,pose,buffers,light);model.opacity=1;
        if(boss.isMorova())pose.popPose();
    }
    @Override protected net.minecraft.client.renderer.RenderType getRenderType(NetherSummonBoss boss,boolean visible,boolean translucent,boolean glowing){return net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(boss));}
    @Override public ResourceLocation getTextureLocation(NetherSummonBoss boss){return boss.isMorova()?BRIDE:PRINCE;}
}
