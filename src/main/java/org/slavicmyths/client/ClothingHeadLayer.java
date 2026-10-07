package org.slavicmyths.client;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.EquipmentSlot;
/** Clothing has its own fitted garment mesh; all other head items retain vanilla rendering. */
final class ClothingHeadLayer extends CustomHeadLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
 ClothingHeadLayer(PlayerRenderer r,EntityRendererProvider.Context c){super(r,c.getModelSet(),c.getItemInHandRenderer());}
 @Override public void render(PoseStack p,MultiBufferSource b,int light,AbstractClientPlayer e,float a,float f,float partial,float age,float yaw,float pitch){
  if(!(e.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof org.slavicmyths.textile.ClothingItem))super.render(p,b,light,e,a,f,partial,age,yaw,pitch);
 }
}
