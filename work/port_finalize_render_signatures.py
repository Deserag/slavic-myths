from pathlib import Path
import re
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8')
 s=s.replace('com.mojang.blaze3d.matrix.MatrixStack','com.mojang.blaze3d.vertex.PoseStack').replace('com.mojang.blaze3d.vertex.IVertexBuilder','com.mojang.blaze3d.vertex.VertexConsumer')
 s=re.sub(r'\bMatrixStack\b','PoseStack',s);s=re.sub(r'\bIVertexBuilder\b','VertexConsumer',s)
 s=re.sub(r'\bVector3f\.([XYZ])P\.',r'com.mojang.math.Axis.\1P.',s)
 s=s.replace('net.minecraft.client.renderer.tileentity.*','net.minecraft.client.renderer.blockentity.*').replace('TileEntityRendererDispatcher','BlockEntityRendererProvider.Context').replace('TileEntityRenderer<','BlockEntityRenderer<')
 if s!=old:p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/client/BurialCoffinRenderer.java');s=p.read_text(encoding='utf-8').replace('extends BlockEntityRenderer<','implements BlockEntityRenderer<').replace('private static final ResourceLocation TEXTURE','private final FolkModelGeometry geometry=new FolkModelGeometry();\n private static final ResourceLocation TEXTURE').replace('new ModelPart(256,256,','geometry.part(256,256,').replace('{super(d);','{')
s=re.sub(r'(\w+)\.addBox\(',r'geometry.box(\1,',s);p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/client/BanditModel.java');s=p.read_text(encoding='utf-8');start=s.index('    public BanditModel(');end=s.index('    @Override public void setupAnim',start)
ctor=s[start:end];body=ctor[ctor.index('if(role=='):ctor.index('    private void part')];body=body[:body.rfind('    }')]
body=re.sub(r'part\((head|body),',r'part(root.getChild("\1"),',body)
replacement='''    public BanditModel(int role){super(root(role));apron=body.getChild("apron");}
    private static ModelPart root(int role){
        var mesh=HumanoidModel.createMesh(net.minecraft.client.model.geom.builders.CubeDeformation.NONE,0);
        var root=mesh.getRoot();
        root.getChild("body").addOrReplaceChild("apron",net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,32).addBox(-4,1,-2.8F,8,14,1),net.minecraft.client.model.geom.PartPose.ZERO);
'''+body+'''        return net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,64).bakeRoot();
    }
    private static void part(net.minecraft.client.model.geom.builders.PartDefinition parent,int u,int v,float x,float y,float z,float w,float h,float d){
        parent.addOrReplaceChild("clothing_"+parent.getChildren().size(),net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d),net.minecraft.client.model.geom.PartPose.ZERO);
    }
'''
s=s[:start]+replacement+s[end:];p.write_text(s,encoding='utf-8')
