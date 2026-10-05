package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.yaga.BabaYaga;
public final class BabaYagaRenderer extends MobRenderer<BabaYaga,BabaYagaModel>{
 public BabaYagaRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new BabaYagaModel(),.35F);}
 @Override public ResourceLocation getTextureLocation(BabaYaga e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/baba_yaga.png");}
 @Override protected float getFlipDegrees(BabaYaga e){return 0;}
}
