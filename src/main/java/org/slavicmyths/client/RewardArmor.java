package org.slavicmyths.client;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
/** Bone shoulders, split witch tails and square military mantle have separate geometry. */
public final class RewardArmor implements IClientItemExtensions {
 private final HumanoidModel<LivingEntity> model;
 private final int kind;
 public RewardArmor(int kind){this.kind=kind;var mesh=HumanoidModel.createMesh(new CubeDeformation(.6F),0);var root=mesh.getRoot();root.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);root.addOrReplaceChild("left_leg",CubeListBuilder.create(),PartPose.ZERO);root.addOrReplaceChild("right_leg",CubeListBuilder.create(),PartPose.ZERO);
  // Armor, fitted witch robe and military surcoat retain different chest profiles.
  var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(kind==1?-4.2F:-4.5F,-.5F,kind==1?-2.3F:-2.5F,kind==1?8.4F:9,kind==1?11:12.8F,kind==1?4.6F:5),PartPose.ZERO);
  for(String arm:new String[]{"left_arm","right_arm"}){float sign=arm.equals("left_arm")?1:-1;var cubes=CubeListBuilder.create();if(kind==0)cubes.texOffs(40,0).addBox(-2.7F,-2.7F,-3,5.4F,3,6).texOffs(40,12).addBox(-2.8F,-.2F,-2.8F,5.6F,3,5.6F);else if(kind==2)cubes.texOffs(40,0).addBox(-2.7F,-2.5F,-2.8F,5.4F,2.5F,5.6F);root.addOrReplaceChild(arm,cubes,PartPose.offset(sign*5,2,0));}
  if(kind==0){body.addOrReplaceChild("bone_ribs",CubeListBuilder.create().texOffs(0,20).addBox(-4.6F,2,-3,9.2F,1.3F,1).addBox(-4.6F,5,-3,9.2F,1.3F,1).addBox(-4.6F,8,-3,9.2F,1.3F,1),PartPose.ZERO);}
  else{body.addOrReplaceChild("collar",CubeListBuilder.create().texOffs(0,36).addBox(-4.9F,-1,-2.9F,9.8F,kind==2?3:1.5F,5.8F),PartPose.ZERO);for(String side:new String[]{"tail_left","tail_right"}){
   boolean left=side.endsWith("left");var tail=CubeListBuilder.create().texOffs(left?0:26,48);
   if(kind==1){tail.addBox(left?-4.5F:.4F,0,0,4.1F,13,.5F).texOffs(left?0:26,48).addBox(left?-4.5F:1.5F,13,0,3,3,.5F).addBox(left?-4.5F:3,16,0,1.5F,1,.5F);}
   else tail.addBox(left?-4.8F:.2F,0,0,4.6F,15,.5F);
   body.addOrReplaceChild(side,tail,PartPose.offset(0,4,2.9F));
  }if(kind==2)body.addOrReplaceChild("red_banner",CubeListBuilder.create().texOffs(60,48).addBox(-1.5F,3,3.5F,3,14,.35F),PartPose.ZERO);}
  model=new HumanoidModel<>(LayerDefinition.create(mesh,128,128).bakeRoot());
 }
 @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity e,ItemStack s,EquipmentSlot slot,HumanoidModel<?> original){if(kind!=0){float walk=e.walkAnimation.position();for(String tail:new String[]{"tail_left","tail_right"})model.body.getChild(tail).xRot=(float)(.06+Math.sin(walk*.65+(tail.endsWith("left")?0:1.2))*.13*e.walkAnimation.speed());}return model;}
}
