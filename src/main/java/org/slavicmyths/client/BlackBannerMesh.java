package org.slavicmyths.client;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
public final class BlackBannerMesh { public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();
var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(2,2).addBox(-0.75000F,-19.00000F,-0.75000F,1.50000F,31.00000F,1.50000F,CubeDeformation.NONE,1.00000F,1.00000F).texOffs(12,2).addBox(-6.50000F,-17.40000F,-0.70000F,13.00000F,1.40000F,1.40000F,CubeDeformation.NONE,1.00000F,1.00000F),PartPose.offset(0.00000F,12.00000F,0.00000F));
var cloth=body.addOrReplaceChild("cloth",CubeListBuilder.create().texOffs(45,2).addBox(-5.50000F,0.00000F,0.65000F,11.00000F,21.00000F,0.50000F,CubeDeformation.NONE,1.00000F,1.00000F).texOffs(72,2).addBox(-0.55000F,2.00000F,0.20000F,1.10000F,16.00000F,0.40000F,CubeDeformation.NONE,1.00000F,1.00000F).texOffs(79,2).addBox(-3.50000F,10.10000F,0.10000F,7.00000F,0.90000F,0.40000F,CubeDeformation.NONE,1.00000F,1.00000F).texOffs(98,2).addBox(-3.10000F,7.00000F,0.10000F,1.00000F,7.00000F,0.40000F,CubeDeformation.NONE,1.00000F,1.00000F).texOffs(105,2).addBox(2.10000F,7.00000F,0.10000F,1.00000F,7.00000F,0.40000F,CubeDeformation.NONE,1.00000F,1.00000F),PartPose.offset(0.00000F,-15.00000F,0.00000F));
return LayerDefinition.create(mesh,256,256).bakeRoot();} private BlackBannerMesh(){} }
