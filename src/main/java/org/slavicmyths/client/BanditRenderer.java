package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.bandit.BanditEntity;
public final class BanditRenderer extends MobRenderer<BanditEntity,BanditModel> {
    public BanditRenderer(EntityRendererManager m,int role){super(m,new BanditModel(role),.5F);addLayer(new HeldItemLayer<>(this));}
    @Override public ResourceLocation getTextureLocation(BanditEntity e){return new ResourceLocation("slavicmyths","textures/entity/bandit_"+e.role+"_"+e.face()+".png");}
}
