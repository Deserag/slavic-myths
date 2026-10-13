package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.morozko.*;
public final class PlayerDoubleRenderer extends MobRenderer<PlayerDouble,PlayerDoubleRenderer.IceModel> {
 private final IceModel wide,slim;
 public PlayerDoubleRenderer(EntityRendererProvider.Context c){super(c,new IceModel(c.bakeLayer(ModelLayers.PLAYER),false),.35f);wide=model;slim=new IceModel(c.bakeLayer(ModelLayers.PLAYER_SLIM),true);addLayer(new HumanoidArmorLayer<>(this,new HumanoidModel<>(c.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),new HumanoidModel<>(c.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),c.getModelManager()));addLayer(new ItemInHandLayer<>(this,c.getItemInHandRenderer()));}
 private net.minecraft.client.multiplayer.PlayerInfo info(PlayerDouble e){var connection=Minecraft.getInstance().getConnection();return connection==null||e.owner()==null?null:connection.getPlayerInfo(e.owner());}
 @Override public ResourceLocation getTextureLocation(PlayerDouble e){var info=info(e);return info==null?net.minecraft.client.resources.DefaultPlayerSkin.get(e.owner()==null?e.getUUID():e.owner()).texture():info.getSkin().texture();}
 @Override public void render(PlayerDouble e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){var i=info(e);model=i!=null&&i.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:wide;p.pushPose();if(e.tickCount<13)p.scale(1,Math.max(.05f,e.tickCount/13f),1);super.render(e,yaw,partial,p,out,light);p.popPose();}
 @Override protected RenderType getRenderType(PlayerDouble e,boolean v,boolean t,boolean g){return RenderType.entityTranslucent(getTextureLocation(e));}
 @Override protected float getFlipDegrees(PlayerDouble e){return 0;}
 static final class IceModel extends PlayerModel<PlayerDouble>{IceModel(net.minecraft.client.model.geom.ModelPart p,boolean slim){super(p,slim);}@Override public void setupAnim(PlayerDouble e,float a,float b,float c,float d,float f){crouching=e.isShiftKeyDown();rightArmPose=e.isUsingItem()?HumanoidModel.ArmPose.BOW_AND_ARROW:HumanoidModel.ArmPose.ITEM;super.setupAnim(e,a,b,c,d,f);}@Override public void renderToBuffer(PoseStack p,VertexConsumer b,int l,int o,int color){super.renderToBuffer(p,b,l,o,0xA0B6E9FF);}}
}
