from pathlib import Path
import re
imports='''\nimport net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
'''
setup='var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);PartDefinition body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);int[] index={0};'
for name,width,columns in [('BossAccessoryRenderer',512,8),('HuntAccessoryRenderer',256,4)]:
 p=Path('src/main/java/org/slavicmyths/client/'+name+'.java');s=p.read_text(encoding='utf-8');s=s.replace('\n','\n'+imports,1)
 pat=r'private static void box\(HumanoidModel<LivingEntity> m,int (material|mat),float x,float y,float z,float w,float h,float d\)\{[^\n]+?\}'
 def box(m):
  mat=m[1];return 'private static void box(PartDefinition body,int[] index,int '+mat+',float x,float y,float z,float w,float h,float d){body.addOrReplaceChild("piece_"+index[0]++,CubeListBuilder.create().texOffs('+mat+'%'+str(columns)+'*64,'+mat+'/'+str(columns)+'*64).addBox(x,y,z,w,h,d),PartPose.ZERO);}'
 s=re.sub(pat,box,s)
 s=s.replace('HumanoidModel<LivingEntity> m=new HumanoidModel<>(0);m.texWidth='+str(width)+';m.texHeight='+str(width)+';m.body=new ModelPart(m);',setup)
 if name=='HuntAccessoryRenderer':
  s=s.replace('HumanoidModel<LivingEntity> m=model(false);m.body=new ModelPart(m);',setup)
  s=s.replace('ModelPart diamond=new ModelPart(m);diamond.setPos(0,5.5F,-2.6F);diamond.zRot=.785F;diamond.texOffs(128,0).addBox(-1.8F,-1.8F,-.2F,3.6F,3.6F,.6F);m.body.addChild(diamond);','body.addOrReplaceChild("diamond",CubeListBuilder.create().texOffs(128,0).addBox(-1.8F,-1.8F,-.2F,3.6F,3.6F,.6F),PartPose.offsetAndRotation(0,5.5F,-2.6F,0,0,.785F));')
 s=s.replace('box(m,','box(body,index,').replace('return m;}','return new HumanoidModel<>(LayerDefinition.create(mesh,'+str(width)+','+str(width)+').bakeRoot());}')
 s=s.replace('top.theillusivec4.curios.api.type.capability.ICurio.RenderHelper.followBodyRotations','top.theillusivec4.curios.api.client.ICurioRenderer.followBodyRotations')
 assert 'new ModelPart' not in s and 'addChild' not in s
 p.write_text(s,encoding='utf-8')
# Actual vanilla bootstrapping is required by DamageEffects/SoundEvents. No fake registry or loader.
p=Path('tools/kurgan-tests/org/slavicmyths/verify/PortDamageHeadless.java');s=p.read_text(encoding='utf-8').replace('for(String id:', 'net.minecraft.SharedConstants.tryDetectVersion();\n        net.minecraft.server.Bootstrap.bootStrap();\n        for(String id:');p.write_text(s,encoding='utf-8')
