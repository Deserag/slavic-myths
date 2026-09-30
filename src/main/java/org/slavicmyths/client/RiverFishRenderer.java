package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.water.RiverFish;
public final class RiverFishRenderer extends MobRenderer<RiverFish,RiverFishModel>{
 private final ResourceLocation texture;
 public RiverFishRenderer(EntityRendererManager m,int k){super(m,new RiverFishModel(k),.2F);texture=new ResourceLocation("slavicmyths","textures/entity/"+(k==0?"pike":k==1?"carp":"crayfish")+".png");}
 public ResourceLocation getTextureLocation(RiverFish e){return texture;}
}
