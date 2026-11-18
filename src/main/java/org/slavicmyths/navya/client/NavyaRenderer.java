package org.slavicmyths.navya.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.navya.*;
public final class NavyaRenderer extends MobRenderer<NavyaMob,NavyaModel> {
    public NavyaRenderer(EntityRendererProvider.Context context,NavyaContent.Kind kind){
        super(context,new NavyaModel(kind),kind==NavyaContent.Kind.GUARDIAN?1.35F:kind==NavyaContent.Kind.WISP?.12F:.55F);
        addLayer(new RenderLayer<NavyaMob,NavyaModel>(this){
            @Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource out,int light,NavyaMob entity,float a,float b,float partial,float age,float yaw,float pitch){
                var texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+NavyaMesh.name(entity.kind)+"_glow.png");
                getParentModel().renderToBuffer(pose,out.getBuffer(RenderType.eyes(texture)),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,entity.secondPhase()?0xFFFFFFFF:0xBFFFFFFF);
            }
        });
    }
    @Override public void render(NavyaMob mob,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource buffer,int light){
        super.render(mob,yaw,partial,pose,buffer,light);
        // Twelve local particles per second; no global player/world polling or damage.
        if(mob.kind==NavyaContent.Kind.GUARDIAN&&mob.tickCount%10==0&&lastAuraTick.getOrDefault(mob.getId(),-1)!=mob.tickCount){
            lastAuraTick.put(mob.getId(),mob.tickCount);
            if(lastAuraTick.size()>64)lastAuraTick.clear();
            var player=net.minecraft.client.Minecraft.getInstance().player;
            if(player!=null&&player.distanceToSqr(mob)<32*32)for(int i=0;i<6;i++){
                double a=(mob.tickCount*.015+i*Math.PI/3),r=5.8;
                mob.level().addParticle(mob.secondPhase()?net.minecraft.core.particles.ParticleTypes.WITCH:net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE,mob.getX()+Math.cos(a)*r,mob.getY()+.15,mob.getZ()+Math.sin(a)*r,0,.005,0);
            }
        }
    }
    private final java.util.Map<Integer,Integer> lastAuraTick=new java.util.HashMap<>();
    @Override protected float getFlipDegrees(NavyaMob entity){return 0;}
    @Override public ResourceLocation getTextureLocation(NavyaMob mob){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+NavyaMesh.name(mob.kind)+".png");}
}
