package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.yaga.BabaYaga;
public final class BabaYagaRenderer extends MobRenderer<BabaYaga,BabaYagaModel>{
 public BabaYagaRenderer(EntityRendererManager m){super(m,new BabaYagaModel(),.35F);}
 @Override public ResourceLocation getTextureLocation(BabaYaga e){return new ResourceLocation("slavicmyths","textures/entity/baba_yaga.png");}
 @Override protected float getFlipDegrees(BabaYaga e){return 0;}
}
