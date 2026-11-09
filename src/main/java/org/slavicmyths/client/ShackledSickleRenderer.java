package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slavicmyths.posad.boss.ShackledSickleProjectile;

public final class ShackledSickleRenderer extends ThrownItemRenderer<ShackledSickleProjectile> {
    private static final double LINK_SPACING=0.20;
    private static final ItemStack CHAIN=new ItemStack(Items.CHAIN);
    public ShackledSickleRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public void render(ShackledSickleProjectile sickle,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        Entity owner=sickle.getOwner();
        if(owner!=null&&owner.level()==sickle.level()){
            Vec3 start=new Vec3(Mth.lerp(partial,owner.xOld,owner.getX()),Mth.lerp(partial,owner.yOld,owner.getY())+owner.getEyeHeight()-.35,Mth.lerp(partial,owner.zOld,owner.getZ()));
            Vec3 end=new Vec3(Mth.lerp(partial,sickle.xOld,sickle.getX()),Mth.lerp(partial,sickle.yOld,sickle.getY()),Mth.lerp(partial,sickle.zOld,sickle.getZ()));
            Vec3 line=end.subtract(start);
            double length=line.length();
            if(length>.20&&length<30){
                Vec3 normal=line.normalize();
                Vec3 origin=new Vec3(start.x-end.x,start.y-end.y,start.z-end.z);
                int segments=Math.min(145,Math.max(1,(int)Math.ceil(length/LINK_SPACING)));
                var mc=Minecraft.getInstance();
                var link=mc.getModelManager().getModel(FlailClient.model("flail_link87"));
                Quaternionf direction=new Quaternionf().rotationTo(new Vector3f(0,1,0),new Vector3f((float)normal.x,(float)normal.y,(float)normal.z));
                for(int i=0;i<segments;i++){
                    Vec3 center=origin.add(line.scale((i+.5)/segments));
                    pose.pushPose();
                    pose.translate(center.x,center.y,center.z);
                    pose.mulPose(direction);
                    if((i&1)==0)pose.mulPose(Axis.YP.rotationDegrees(90));
                    pose.scale(1.25F,1.25F,1.25F);
                    mc.getItemRenderer().render(CHAIN,ItemDisplayContext.NONE,false,pose,buffers,light,OverlayTexture.NO_OVERLAY,link);
                    pose.popPose();
                }
            }
        }
        super.render(sickle,yaw,partial,pose,buffers,light);
    }
}
