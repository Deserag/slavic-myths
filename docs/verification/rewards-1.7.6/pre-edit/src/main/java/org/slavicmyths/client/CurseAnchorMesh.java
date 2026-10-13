package org.slavicmyths.client;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
public final class CurseAnchorMesh { public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot();
var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(2,2).addBox(-3.50000F,-1.00000F,-1.00000F,7.00000F,11.00000F,2.00000F,CubeDeformation.NONE,1.00000F,1.00000F).texOffs(24,2).addBox(-2.25000F,1.50000F,-1.50000F,4.50000F,4.50000F,0.60000F,CubeDeformation.NONE,1.00000F,1.00000F).texOffs(39,2).addBox(-0.50000F,3.00000F,-1.95000F,1.00000F,2.00000F,0.50000F,CubeDeformation.NONE,1.00000F,1.00000F),PartPose.offset(0.00000F,12.00000F,0.00000F));
return LayerDefinition.create(mesh,256,256).bakeRoot();} private CurseAnchorMesh(){} }
