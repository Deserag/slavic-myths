package org.slavicmyths.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.extensions.common.*;
import org.slavicmyths.registry.ModItems;

/** Individual padded coat, forged cuirass and travelling boots; client geometry only. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class BasicArmorModels implements IClientItemExtensions {
 private final String id;
 private final HumanoidModel<?> normal,slim;
 private BasicArmorModels(String id){this.id=id;normal=create(id,false);slim=create(id,true);}
 @SubscribeEvent public static void register(RegisterClientExtensionsEvent e){
  e.registerItem(new BasicArmorModels("gambeson"),ModItems.GAMBESON.get());
  e.registerItem(new BasicArmorModels("plate_cuirass"),ModItems.PLATE_CUIRASS.get());
  e.registerItem(new BasicArmorModels("seven_league_boots"),ModItems.SEVEN_LEAGUE_BOOTS.get());
 }
 private static HumanoidModel<?> create(String id,boolean slim){
  var mesh=new MeshDefinition();var r=mesh.getRoot();
  var body=CubeListBuilder.create();var la=CubeListBuilder.create();var ra=CubeListBuilder.create();var ll=CubeListBuilder.create();var rl=CubeListBuilder.create();float w=slim?3:4;
  if(id.equals("gambeson")){
   body.texOffs(16,16).addBox(-4,0,-2,8,7,4,new CubeDeformation(.45F));
   body.texOffs(16,16).addBox(-4,7,-2,8,5,4,new CubeDeformation(.31F));
   body.texOffs(0,0).addBox(-4.42F,8.4F,-2.46F,8.84F,.8F,4.92F);
   body.texOffs(0,0).addBox(-2,-.3F,-2.5F,1,1.1F,.5F).addBox(1,-.3F,-2.5F,1,1.1F,.5F);
   la.texOffs(40,16).addBox(-1,-2,-2,w,6,4,new CubeDeformation(.42F)).texOffs(40,16).addBox(-1,4,-2,w,5,4,new CubeDeformation(.30F));
   ra.texOffs(40,16).addBox(1-w,-2,-2,w,6,4,new CubeDeformation(.42F)).texOffs(40,16).addBox(1-w,4,-2,w,5,4,new CubeDeformation(.30F));
  }else if(id.equals("plate_cuirass")){
   // Curved front is three depths; straps and back have their own construction.
   body.texOffs(16,16).addBox(-4,0,-2.4F,8,10,4.8F);
   body.texOffs(16,0).addBox(-3.4F,1,-3,6.8F,8,.7F).texOffs(16,0).addBox(-2.1F,2,-3.35F,4.2F,6,.4F);
   body.texOffs(16,16).addBox(-3.8F,1,2.4F,7.6F,8,.5F);
   body.texOffs(0,0).addBox(-4.5F,4,-2.55F,.5F,5,5.1F).addBox(4,4,-2.55F,.5F,5,5.1F);
   body.texOffs(0,0).addBox(-3.9F,9.5F,-2.6F,7.8F,1.2F,5.2F);
   la.texOffs(40,0).addBox(-1.45F,-2.7F,-2.7F,w+.9F,3,5.4F);
   ra.texOffs(40,0).addBox(.55F-w,-2.7F,-2.7F,w+.9F,3,5.4F);
  }else{
   for(int side:new int[]{-1,1}){var leg=side<0?rl:ll;leg.texOffs(0,16).addBox(side<0?-2.3F:-1.8F,4,-2.3F,4.1F,7.5F,4.6F).texOffs(0,0).addBox(side<0?-2.5F:-1.8F,3.6F,-2.5F,4.3F,1.3F,5).texOffs(0,16).addBox(side<0?-2.4F:-1.8F,9.5F,-3.5F,4.2F,2.5F,5.9F).texOffs(24,0).addBox(side<0?-2.45F:-1.8F,11.6F,-3.6F,4.25F,.6F,6.1F);}
  }
  r.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);r.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);r.addOrReplaceChild("body",body,PartPose.ZERO);
  r.addOrReplaceChild("left_arm",la,PartPose.offset(5,2,0));r.addOrReplaceChild("right_arm",ra,PartPose.offset(-5,2,0));r.addOrReplaceChild("left_leg",ll,PartPose.offset(1.9F,12,0));r.addOrReplaceChild("right_leg",rl,PartPose.offset(-1.9F,12,0));
  return new HumanoidModel<>(LayerDefinition.create(mesh,64,32).bakeRoot());
 }
 @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity e,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){return e instanceof AbstractClientPlayer p&&p.getSkin().model()==PlayerSkin.Model.SLIM?slim:normal;}
}
