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
  for(String arm:new String[]{"left_arm","right_arm"}){float sign=arm.equals("left_arm")?1:-1;var cubes=CubeListBuilder.create();if(kind==0)cubes.texOffs(40,0).addBox(sign>0?-1.7F:-3.7F,-2.7F,-3,5.4F,3,6).texOffs(40,12).addBox(sign>0?-1.8F:-3.8F,-.2F,-2.8F,5.6F,3,5.6F);else if(kind==2)cubes.texOffs(40,0).addBox(-2.7F,-2.5F,-2.8F,5.4F,2.5F,5.6F);root.addOrReplaceChild(arm,cubes,PartPose.offset(sign*5,2,0));}
  if(kind==0){
   // Separate left/right ribs with exposed bronze between, not three solid bars.
   for(int row=0;row<3;row++)for(int side:new int[]{-1,1})body.addOrReplaceChild("rib_"+row+"_"+side,CubeListBuilder.create().texOffs(0,20).addBox(side<0?-4.4F:.6F,2+row*3,-3.15F,3.8F,1.1F,.7F),PartPose.ZERO);
  }else{
   body.addOrReplaceChild("collar",CubeListBuilder.create().texOffs(0,36).addBox(-4.9F,-1,-2.9F,3.6F,kind==2?3:1.5F,5.8F).addBox(1.3F,-1,-2.9F,3.6F,kind==2?3:1.5F,5.8F),PartPose.ZERO);
   for(String side:new String[]{"tail_left","tail_right"}){
    boolean left=side.endsWith("left");var tail=CubeListBuilder.create().texOffs(left?0:26,48);
    if(kind==1){tail.addBox(left?-4.5F:.4F,0,0,4.1F,10,.5F).texOffs(left?0:26,64).addBox(left?-4.5F:1.5F,10,0,3,3,.5F).addBox(left?-4.5F:3,13,0,1.5F,1,.5F);}
    else tail.addBox(left?-4.8F:.2F,0,0,4.6F,13,.5F);
    var panel=body.addOrReplaceChild(side,tail,PartPose.offset(0,9,3.2F));
    // The two halves of the same red standard follow their mantle panels.
    if(kind==2&&left)panel.addOrReplaceChild("red_banner",CubeListBuilder.create().texOffs(60,48).addBox(-1.5F,0,.55F,3,12,.3F),PartPose.ZERO);
   }
  }
  model=new HumanoidModel<>(LayerDefinition.create(mesh,128,128).bakeRoot());
 }
 @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity e,ItemStack s,EquipmentSlot slot,HumanoidModel<?> original){if(kind!=0){float walk=e.walkAnimation.position();for(String tail:new String[]{"tail_left","tail_right"})model.body.getChild(tail).xRot=(float)(.18+Math.sin(walk*.65+(kind==2||tail.endsWith("left")?0:1.2))*.08*e.walkAnimation.speed());}return model;}
}
