package org.slavicmyths.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.slavicmyths.village.OutfitRules;

/** Pure mesh factory: no renderer state, world, registries or graphics initialization. */
public final class VillageOutfitGeometry {
    public static ModelPart zombieBody(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();
        root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-10,-4,8,10,8).texOffs(24,0).addBox(-1,-3,-6,2,4,2),PartPose.ZERO);
        var hat=root.addOrReplaceChild("hat",CubeListBuilder.create().texOffs(32,0).addBox(-4,-10,-4,8,10,8,new CubeDeformation(.5F)),PartPose.ZERO);
        hat.addOrReplaceChild("hat_rim",CubeListBuilder.create().texOffs(30,47).addBox(-8,-8,-6,16,16,1),PartPose.rotation(-(float)Math.PI/2,0,0));
        var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,20).addBox(-4,0,-3,8,12,6),PartPose.ZERO);
        body.addOrReplaceChild("jacket",CubeListBuilder.create().texOffs(0,38).addBox(-4,0,-3,8,20,6,new CubeDeformation(.05F)),PartPose.ZERO);
        root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(44,22).addBox(-3,-2,-2,4,12,4),PartPose.offset(-5,2,0));
        root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(44,22).mirror().addBox(-1,-2,-2,4,12,4),PartPose.offset(5,2,0));
        root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(0,22).addBox(-2,0,-2,4,12,4),PartPose.offset(-2,12,0));
        root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(0,22).mirror().addBox(-2,0,-2,4,12,4),PartPose.offset(2,12,0));
        return LayerDefinition.create(mesh,64,64).bakeRoot();
    }
    private static CubeListBuilder cube(int material,float x,float y,float z,float w,float h,float d){return CubeListBuilder.create().texOffs(material%2*64,material/2*32).addBox(x,y,z,w,h,d);}
    private static void box(PartDefinition root,String name,int material,float x,float y,float z,float w,float h,float d){root.addOrReplaceChild(name,cube(material,x,y,z,w,h,d),PartPose.ZERO);}
    public static ModelPart parts(String role,int variant,OutfitRules.Climate climate){
        MeshDefinition mesh=new MeshDefinition();var root=mesh.getRoot();var head=root.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);var body=root.addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);
        boolean idler=role.equals("none")||role.equals("nitwit"),merchant=role.equals("merchant");
        int cap=climate==OutfitRules.Climate.COLD?3:variant==1?4:0;
        if(merchant){
            box(head,"hood_top",6,-4.6F,-10.7F,-4.6F,9.2F,1.2F,9.2F);
            box(head,"hood_back",6,-4.6F,-9.5F,3.9F,9.2F,9,1);
            box(head,"hood_left",6,-4.6F,-9.5F,-4.6F,1,8,8.5F);box(head,"hood_right",6,3.6F,-9.5F,-4.6F,1,8,8.5F);
            box(body,"pack",0,-3.5F,2.9F,3.7F,7,9,3);
            box(body,"rolled_rug",5,-4,0,5,8,2,2);
            box(body,"shoulder_strap_l",0,-3,0,3.2F,1,12,1);box(body,"shoulder_strap_r",0,2,0,3.2F,1,12,1);
            box(body,"trade_satchel",0,-6,9,-1,3,5,3);
            box(body,"pack_pouch",0,3.3F,7,3,2,4,2);
        }else if(climate==OutfitRules.Climate.WARM&&(role.equals("farmer")||role.equals("herder"))||role.equals("farmer")&&variant==0&&climate!=OutfitRules.Climate.COLD){
            box(head,"straw_crown",1,-4.2F,-12,-4.2F,8.4F,2.2F,8.4F);box(head,"straw_brim",1,-6,-10,-5.5F,12,.6F,11);
        }else if(variant==1&&!role.equals("armorer")&&!role.equals("weaponsmith")){
            box(head,"kerchief_top",cap,-4.3F,-10.4F,-4.3F,8.6F,1.2F,8.6F);
            box(head,"kerchief_back",cap,-4.3F,-9.2F,3.7F,8.6F,9.5F,1);
            box(head,"kerchief_left",cap,-4.5F,-9.2F,-4.1F,.8F,8.5F,8);box(head,"kerchief_right",cap,3.7F,-9.2F,-4.1F,.8F,8.5F,8);
        }else{
            box(head,"cap",role.equals("cook")||role.equals("miller")?4:cap,-4.2F,-11.3F,-4.2F,8.4F,2.8F,8.4F);
            if(climate==OutfitRules.Climate.COLD){box(head,"earflap_l",3,-4.5F,-8.5F,0,1,5,4);box(head,"earflap_r",3,3.5F,-8.5F,0,1,5,4);}
            // Beard is wholly below the eyes and split around the existing nose.
            if(!role.equals("cook")){box(head,"beard_l",2,-3.5F,-1.1F,-4.2F,2.3F,3,1);box(head,"beard_r",2,1.2F,-1.1F,-4.2F,2.3F,3,1);box(head,"beard_chin",2,-1.2F,1.1F,-4.2F,2.4F,1.8F,1);}
        }
        box(body,"belt",idler?2:0,-4.6F,9.8F,-3.6F,9.2F,1,7.2F);
        if(climate==OutfitRules.Climate.COLD){box(body,"coat_collar",3,-4.6F,-.4F,3.1F,9.2F,2.4F,1.5F);box(body,"coat_trim",3,-4.6F,18.4F,-3.6F,9.2F,1,7.2F);}
        if(!idler){
            box(body,"pouch",0,4.2F,10,-1.5F,2.3F,3.4F,3);
            switch(role){
                case "fisherman" -> {box(body,"net",2,-4,3,3.6F,7,7,.3F);box(body,"float",4,-5.4F,9,-2,1,2,1);}
                case "shepherd" -> box(body,"wool_roll",3,-3,1,3.5F,6,4,2);
                case "librarian" -> {box(body,"scroll",4,-5.8F,10,0,1.5F,5,1.5F);box(body,"book",5,-3.5F,9,3.5F,4,5,1.5F);}
                case "cartographer" -> {box(body,"map_case",0,-3,0,3.6F,2,11,2);box(body,"map_roll",4,-2.7F,-1,3.9F,1.4F,3,1.4F);}
                case "fletcher" -> {box(body,"quiver",0,-3,1,3.5F,3,9,2);box(body,"arrow_tips",4,-2.5F,-1,4,2,3,1);box(head,"feather",4,3,-12,1,1,4,1);}
                case "armorer" -> {box(body,"plate",7,-3,8,-3.7F,6,2,.7F);box(body,"rivet",7,-5.5F,10,-2,1,2,1);}
                case "weaponsmith" -> {box(body,"steel_blank",7,-5.7F,10,-1,1,6,1);box(body,"hammer",0,-5.6F,9,1,1,5,1);box(body,"hammer_head",7,-6.6F,9,.5F,3,1.5F,2);}
                case "toolsmith","mason" -> {box(body,"tool_shaft",0,-5.6F,9,.5F,1,5,1);box(body,"tool_head",7,-6.6F,9,0,3,1.5F,2);}
                case "butcher","hunter" -> {box(body,"sheath",0,-5.5F,10,0,1.4F,5,1.2F);box(body,"knife_grip",2,-5.4F,8.5F,.1F,1.2F,2,1);if(role.equals("hunter"))box(body,"fur_collar",3,-4.5F,0,3.1F,9,3,1.5F);}
                case "cleric" -> {box(body,"herb_bag",2,-5.8F,10,0,2,3,2);box(body,"bottle",7,-2,10,-3.8F,1,2,1);}
                case "miller" -> box(body,"grain_scoop",4,-5.5F,10,0,1.8F,4,1);
                case "brewer" -> {box(body,"cup",7,-5.6F,10,0,2,2,2);box(body,"cup_handle",7,-6.6F,10.2F,.4F,1,1.4F,1);}
                case "weaver" -> {box(body,"spool",5,-5.6F,10,0,1.8F,3,1.8F);box(body,"shuttle",0,-2,10,-3.8F,3,.7F,.7F);}
                case "herder" -> {box(body,"bell",7,-5.5F,10,0,1.5F,2,1.5F);box(body,"rope",2,-3,11,3.5F,3,3,1);}
                case "cook" -> {box(body,"spoon",0,-5.4F,9.5F,0,.7F,4,1);box(body,"spoon_bowl",0,-5.9F,9,0,1.7F,1.3F,1);}
                default -> {}
            }
        }
        for(int i=2;i<=5;i++){
            var part=root.addOrReplaceChild("level_"+i,CubeListBuilder.create(),PartPose.ZERO);
            if(!idler)box(part,"detail",i==5?5:0,-3.5F+(i-2)*1.7F,11.3F,-3.7F,1.3F,i==2?2:1,.6F);
        }
        return LayerDefinition.create(mesh,128,128).bakeRoot();
    }
    private VillageOutfitGeometry(){}
}
