package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.bandit.BanditEntity;
public final class BanditRenderer extends MobRenderer<BanditEntity,BanditModel> {
    public BanditRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m,int role){super(m,new BanditModel(role),.5F);addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,m.getItemInHandRenderer()));}
    @Override public ResourceLocation getTextureLocation(BanditEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/bandit_"+e.role+"_"+e.face()+".png");}
}
