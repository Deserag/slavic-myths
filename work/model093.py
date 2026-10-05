from pathlib import Path
r=Path('src/main/java/org/slavicmyths')
# native segmented geometry with material atlas; no Witch model inheritance
parts=[]
def add(n,parent,pos,box,mat=0,rot=(0,0,0)):parts.append((n,parent,pos,box,mat,rot))
add('root','',(0,0,0),None)
add('body','root',(0,1,0),(-3.5,0,-2,7,11,4),3,(.16,0,0))
add('head','body',(0,-.5,-1.8),(-2.4,-4,-2.1,4.8,5.3,4),0,(-.12,0,0))
add('noseBridge','head',(0,-1,-2),(-.55,-.8,-1.2,1.1,2.5,1.4),0,(.2,0,0))
add('noseHook','noseBridge',(0,1.1,-.8),(-.6,0,-.6,1.2,1.1,1),0,(.3,0,0))
add('chin','head',(0,1,-1.5),(-1.5,0,-.9,3,.8,1.5),0)
for s in [-1,1]:
 add('eye'+str(s),'head',(s*1.15,-1.65,-2.16),(-.55,-.3,-.1,1.1,.55,.15),1)
 add('brow'+str(s),'head',(s*1.15,-2.25,-2.17),(-.8,-.2,-.1,1.6,.4,.3),2,(0,0,s*.13))
 add('cheek'+str(s),'head',(s*1.8,-.7,-1.9),(-.6,-.4,-.3,1.2,1.3,.6),0)
add('scarf','head',(0,-3.9,0),(-2.65,-.25,-2.25,5.3,1.2,4.5),4)
add('scarfBack','head',(0,-2.9,1.7),(-2.65,0,-.3,5.3,4,1),4)
for s in [-1,1]:add('scarfSide'+str(s),'head',(s*2.3,-2.8,-1.2),(-.5,0,-.4,1,3.6,1.3),4,(0,0,-s*.14))
add('knot','head',(0,1,-1.9),(-.8,0,-.6,1.6,1,1.2),4)
for i in range(10):add('hair'+str(i),'head',((-2 if i<5 else 2),-.4+(i%5)*.2,.1+(i%5)*.35),(-.3,0,-.2,.6,2+(i%3)*.6,.5),2,(.12,0,(-.18 if i<5 else .18)))
add('shawl','body',(0,.7,0),(-4.2,0,-2.5,8.4,3,5),3)
add('belt','body',(0,8.2,0),(-3.8,0,-2.3,7.6,1,4.6),5)
add('apron','body',(0,6.7,-2.35),(-2.9,0,-.2,5.8,10,.45),6,(-.14,0,0))
for i in range(8):
 import math
 a=i*math.pi/7;add('bead'+str(i),'body',(-2.8+5.6*i/7,2+math.sin(a)*1.5,-2.8),(-.35,-.3,-.3,.7,.6,.6),7 if i%2 else 8)
for s in [-1,1]:
 add('arm'+str(s),'body',(s*3.6,2,0),(-1,0,-1,2,5.5,2),3,(.15,0,s*-.12))
 add('fore'+str(s),'arm'+str(s),(0,5.4,0),(-.8,0,-.8,1.6,4.5,1.6),0,(-.38,0,0))
 add('hand'+str(s),'fore'+str(s),(0,4.2,0),(-.9,0,-.45,1.8,1.5,.9),0)
 for i in range(4):add('finger'+str(s)+'_'+str(i),'hand'+str(s),(-.7+i*.45,1.3,-.05),(-.15,0,-.2,.3,1.35,.4),0,(-.2,0,0))
 add('pouch'+str(s),'body',(s*3,9,0),(-.8,0,-1,1.6,2.2,2),5)
for i in range(8):
 a=i*math.pi/4;add('skirt'+str(i),'root',(math.cos(a)*3,11,math.sin(a)*2),(-1.6,0,-.45,3.2,7,.8),3 if i%2 else 4,(.03, -a,0))
add('leg1','root',(1.8,12,0),(-.8,0,-.8,1.6,10,1.6),6)
add('boot','leg1',(0,10,0),(-1.1,0,-2.1,2.2,2,3.1),5)
add('leg-1','root',(-1.8,12,0),(-.5,0,-.5,1,9.4,1),8,(0,0,.06))
add('boneKnee','leg-1',(0,5.2,0),(-.75,-.6,-.65,1.5,1.2,1.3),8)
add('boneFoot','leg-1',(0,9.6,0),(-.7,0,-1.8,1.4,2.4,2.5),8)
add('cane','hand1',(.8,-.5,0),(-.25,0,-.25,.5,12,.5),5,(.35,0,0))
add('caneHook','cane',(0,0,0),(-.9,-.45,-.3,1.4,.6,.6),5)
body='package org.slavicmyths.client;\nimport java.util.*;\nimport com.mojang.blaze3d.matrix.MatrixStack;\nimport com.mojang.blaze3d.vertex.IVertexBuilder;\nimport net.minecraft.client.renderer.entity.model.EntityModel;\nimport net.minecraft.client.renderer.model.ModelRenderer;\nimport org.slavicmyths.yaga.BabaYaga;\npublic final class BabaYagaModel extends EntityModel<BabaYaga>{\n private final Map<String,ModelRenderer> p=new LinkedHashMap<>();private final Map<String,float[]> rest=new HashMap<>();\n public BabaYagaModel(){texWidth=512;texHeight=512;\n'
def fl(n):return str(float(n))+'F'
for n,parent,pos,box,mat,rot in parts:
 body+='a("'+n+'","'+parent+'",'+','.join(map(fl,pos))+','+('null' if box is None else 'new float[]{'+','.join(map(fl,box))+'}')+','+str(mat)+','+','.join(map(fl,rot))+');\n'
body+=''' }
 private void a(String n,String parent,float x,float y,float z,float[] b,int mat,float rx,float ry,float rz){ModelRenderer m=new ModelRenderer(this,mat%8*64,mat/8*64);m.setPos(x,y,z);m.xRot=rx;m.yRot=ry;m.zRot=rz;if(b!=null)m.addBox(b[0],b[1],b[2],b[3],b[4],b[5]);p.put(n,m);rest.put(n,new float[]{x,y,z,rx,ry,rz});if(!parent.isEmpty())p.get(parent).addChild(m);}
 @Override public void setupAnim(BabaYaga e,float walk,float amount,float age,float yaw,float pitch){for(String n:p.keySet()){ModelRenderer m=p.get(n);float[] r=rest.get(n);m.setPos(r[0],r[1],r[2]);m.xRot=r[3];m.yRot=r[4];m.zRot=r[5];}p.get("head").yRot=yaw*.017453F;p.get("head").xRot+=pitch*.017453F;p.get("body").xRot+=(float)Math.sin(age*.04)*.018F;float stride=(float)Math.cos(walk*.6)*amount*.35F;p.get("leg1").xRot+=stride;p.get("leg-1").xRot-=stride*.6F;p.get("arm-1").xRot-=stride*.4F;float t=e.gestureTime(age-e.tickCount),wave=(float)Math.sin(Math.min(1,t/36)*Math.PI);switch(e.gesture()){case 1:p.get("fore-1").xRot-=wave*.8F;p.get("head").xRot+=Math.sin(t*.3)*.08F;break;case 2:p.get("arm-1").zRot-=wave*.6F;break;case 3:p.get("arm-1").xRot-=wave*.8F;p.get("head").xRot-=wave*.12F;break;case 4:p.get("arm-1").xRot-=.65F*wave;p.get("fore-1").yRot+=(float)Math.sin(t*.25)*.45F*wave;break;default:break;}}
 @Override public void renderToBuffer(MatrixStack m,IVertexBuilder v,int l,int o,float r,float g,float b,float a){p.get("root").render(m,v,l,o,r,g,b,a);}
}
'''
(r/'client/BabaYagaModel.java').write_text(body,'utf-8')
(r/'client/BabaYagaRenderer.java').write_text('''package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.yaga.BabaYaga;
public final class BabaYagaRenderer extends MobRenderer<BabaYaga,BabaYagaModel>{
 public BabaYagaRenderer(EntityRendererManager m){super(m,new BabaYagaModel(),.35F);}
 @Override public ResourceLocation getTextureLocation(BabaYaga e){return new ResourceLocation("slavicmyths","textures/entity/baba_yaga.png");}
 @Override protected float getFlipDegrees(BabaYaga e){return 0;}
}
''','utf-8')
p=r/'client/ClientSetup.java';s=p.read_text('utf-8-sig');s=s.replace('event.enqueueWork(() -> {','RenderingRegistry.registerEntityRenderingHandler(ModEntities.BABA_YAGA.get(),BabaYagaRenderer::new);\n        event.enqueueWork(() -> {\n            net.minecraft.client.gui.ScreenManager.register(org.slavicmyths.yaga.YagaMenu.TYPE.get(),YagaScreen::new);\n            RenderTypeLookup.setRenderLayer(ModBlocks.YAGA_DRIED_HERBS.get(),RenderType.cutout());\n            RenderTypeLookup.setRenderLayer(ModBlocks.YAGA_BONE_CHARM.get(),RenderType.cutout());');p.write_text(s,'utf-8')
p=r/'yaga/YagaHut.java';s=p.read_text().replace('org.slavicmyths.SlavicMyths.LOGGER.warn','org.apache.logging.log4j.LogManager.getLogger().warn').replace('net.minecraft.block.LogBlock','net.minecraft.block.RotatedPillarBlock').replace('instanceof LogBlock','instanceof RotatedPillarBlock');p.write_text(s,'utf-8')
# Isolated optional plugin; live recipes come from server catalogue.
s=(r/'compat/JeiKitchen.java').read_text().replace('JeiKitchen','JeiYaga').replace('"kitchen"','"yaga_cauldron"').replace('import org.slavicmyths.kitchen.KitchenRecipes;','import org.slavicmyths.yaga.YagaServices;').replace('KitchenRecipes.COUNT','YagaServices.BREWS.length').replace('ModItems.KITCHEN_TABLE','ModItems.SVYAZKA_TRAV_YAGI').replace('return ModItems.SVYAZKA_TRAV_YAGI.get().getDescription().getString();','return new net.minecraft.util.text.TranslationTextComponent("yaga.cauldron").getString();')
a=s.index('  public void setIngredients(');b=s.index('  public void setRecipe(',a)
s=s[:a]+'''  public void setIngredients(Integer id,IIngredients ing){List<ItemStack> in=new ArrayList<>();for(YagaServices.Need n:YagaServices.BREWS[id].inputs)in.add(n.stack());ing.setInputs(VanillaTypes.ITEM,in);ing.setOutput(VanillaTypes.ITEM,YagaServices.BREWS[id].output());}
'''+s[b:];(r/'compat/JeiYaga.java').write_text(s,'utf-8')
import json
Path('work/yaga_geometry.json').write_text(json.dumps(parts),'utf-8')
for p in (r/'client').glob('*Yaga*.java'):p.write_text(p.read_text('utf-8-sig'),'utf-8')
