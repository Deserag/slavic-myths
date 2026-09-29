from pathlib import Path
import json,struct,zlib,math,random,wave,subprocess,shutil
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';J=R/'src/main/java/org/slavicmyths/client'
# Native cuboid masters, independent geometry for each spirit. No vanilla mob mesh.
PAL={'skin':['4c4840','777567','a09c84','c0b797'],'cloth':['666457','95917d','c2bca1','e0d8bc'],'hair':['272a29','3b3d36','55534a','737064'],'leg':['252a28','41483b','686b4e','8c8864'],'wood':['443322','765a32','a88c4e','d0b777'],'straw':['615036','94804c','c3ab65','e6cf8a'],'green':['283b28','496141','74845a','a2ab76'],'ash':['24272b','3c3c3d','5b5852','7c7569'],'ember':['442820','7e3824','d8792e','f2ba51'],'bone':['554735','93836a','c4b494','e2d5b9'],'silver':['303b47','6c7e88','acb7ba','e2e6d9']}
D={}
def mob(name):D[name]={};return D[name]
def bone(m,name,pos):m[name]={'pos':pos,'boxes':[]};return name
def box(m,b,xyz,size,mat):m[b]['boxes'].append((xyz,size,mat))
# Kikimora: forward head, needle nose, rags and articulated bird feet.
m=mob('Kikimora')
for b,p in [('head',(0,-5,-2)),('body',(0,-4,0)),('rightArm',(-3,-3,0)),('leftArm',(3,-3,0)),('rightLeg',(-1.5,5,0)),('leftLeg',(1.5,5,0)),('bonnet',(0,-5,-2)),('cap',(0,-5,-2))]:bone(m,b,p)
box(m,'head',(-2,-6,-2),(4,7,4),'skin');box(m,'head',(-.6,-3,-6),(1,4,4),'skin')
for x,z,h in [(-2.5,-1,11),(1.8,0,13),(-1,2,12)]:box(m,'head',(x,-5,z),(1,h,1),'hair')
box(m,'body',(-2.5,0,-1.7),(5,9,4),'cloth');box(m,'body',(-3,7,-2),(2,4,4),'cloth');box(m,'body',(1,7,-2),(2,3,4),'cloth')
for b in ('rightArm','leftArm'):
 box(m,b,(-.7,0,-.7),(1.5,13,1.5),'skin');box(m,b,(-1,12,-1),(2,3,2),'skin')
 for x in (-1,0,1):box(m,b,(x,14,-1),(0.5,3,.6),'skin')
for b in ('rightLeg','leftLeg'):
 box(m,b,(-.8,0,-.8),(1.6,7,2),'skin');box(m,b,(-.5,7,.6),(1,10,1),'leg');box(m,b,(-.8,15,-.3),(1.6,2,2),'leg')
 for x in (-1,0,1):box(m,b,(x,17,-3),(0.5,1,4),'leg')
 box(m,b,(-.3,17,1),(.6,1,2),'leg')
box(m,'bonnet',(-3,-7,-2.5),(6,2,5),'cloth');box(m,'bonnet',(-3,-5,1),(6,3,2),'cloth');box(m,'cap',(-2.5,-7,-2.5),(5,2,5),'cloth')
# Poludnitsa: narrow ivory dress, wheat crown, curved sickle; reveal adds jaw/ribs/fingers.
m=mob('Poludnitsa')
for b,p in [('head',(0,-1,0)),('body',(0,0,0)),('rightArm',(-3.5,1,0)),('leftArm',(3.5,1,0)),('rightLeg',(-1.5,14,0)),('leftLeg',(1.5,14,0)),('dress',(0,9,0)),('jaw',(0,-1,0)),('ribs',(0,0,0))]:bone(m,b,p)
box(m,'head',(-2.5,-7,-2.5),(5,7,5),'skin');box(m,'head',(-3,-6,1),(6,15,2),'straw')
for x in (-3,-1,1,3):box(m,'head',(x,-9,-1),(1,3,1),'straw')
box(m,'head',(-3,-7,-3),(6,1,1),'wood');box(m,'body',(-2.5,0,-1.5),(5,10,3),'cloth')
box(m,'dress',(-3,0,-2),(6,7,4),'cloth');box(m,'dress',(-4,6,-2.5),(8,8,5),'cloth')
for b in ('rightArm','leftArm'):box(m,b,(-1,0,-1),(2,12,2),'cloth');box(m,b,(-1,11,-1),(2,2,2),'skin')
for b in ('rightLeg','leftLeg'):box(m,b,(-1,0,-1),(2,10,2),'skin')
# Held sickle is geometry attached to the right hand, not a recolored sword.
box(m,'rightArm',(-.5,11,-.5),(1,7,1),'wood')
for xyz,size in [((-.5,15,-1),(5,1,1)),((4,12,-1),(1,4,1)),((2,11,-1),(3,1,1)),((1,12,-1),(1,2,1))]:box(m,'rightArm',xyz,size,'silver')
box(m,'jaw',(-2,-1,-2.7),(4,3,2),'skin')
for y in (3,5,7):box(m,'ribs',(-2.7,y,-1.8),(5.4,.5,.5),'bone')
# Polevik: individual stalk bundles, roots, long ear-of-wheat beard.
m=mob('Polevik')
for b,p in [('head',(0,-3,0)),('body',(0,-2,0)),('rightArm',(-3.5,-1,0)),('leftArm',(3.5,-1,0)),('rightLeg',(-2,10,0)),('leftLeg',(2,10,0))]:bone(m,b,p)
box(m,'head',(-2.5,-6,-2),(5,6,4),'wood')
for x,h in [(-3,4),(-1,5),(1,3),(3,4)]:box(m,'head',(x,-6-h,0),(1,h,1),'straw')
for x,h in [(-2,7),(-1,10),(0,11),(1,9),(2,6)]:box(m,'head',(x,-1,-3),(1,h,1),'straw')
for x in (-2,-1,0,1,2):box(m,'body',(x,0,-1),(1,12,2),'straw')
for x in (-2,1):box(m,'body',(x,3,-2),(1,5,1),'green')
for b in ('rightArm','leftArm'):
 box(m,b,(-.7,0,-.7),(1.4,16,1.4),'straw')
 for x in (-1,0,1):box(m,b,(x,15,-1),(.5,4,.5),'wood')
for b in ('rightLeg','leftLeg'):
 for x in (-1,0,1):box(m,b,(x,0,-.5),(.7,13,1.3),'straw')
 box(m,b,(-1.5,12,-2),(3,2,4),'wood')
# Bannik: broad low chest, bald heavy brow, oversized hands and leafy beard.
m=mob('Bannik')
for b,p in [('head',(0,5,-3)),('body',(0,6,0)),('rightArm',(-7,6,0)),('leftArm',(7,6,0)),('rightLeg',(-3,17,0)),('leftLeg',(3,17,0))]:bone(m,b,p)
box(m,'head',(-4,-7,-3),(8,7,6),'skin');box(m,'head',(-4,-5,-3.7),(8,1.5,1),'hair');box(m,'head',(-1.5,-3,-5),(3,2,2),'skin')
box(m,'head',(-3,0,-4),(6,5,3),'hair');box(m,'head',(-2,5,-3),(4,3,2),'hair');box(m,'head',(1,2,-4.2),(1,2,1),'green')
box(m,'body',(-6,0,-3),(12,10,7),'skin');box(m,'body',(-6,8,-3.3),(12,5,7.6),'cloth')
for b in ('rightArm','leftArm'):
 box(m,b,(-2,0,-2),(4,12,4),'skin');box(m,b,(-2.5,10,-2.5),(5,4,5),'skin')
 for x in (-2,0,2):box(m,b,(x,13,-2),(1,2,1),'skin')
for b in ('rightLeg','leftLeg'):box(m,b,(-2,0,-2),(4,7,4),'skin')
# Igosha: oversized still mask, cloth-wrapped knot, stubs instead of human limbs.
m=mob('Igosha')
for b,p in [('head',(0,19,-1)),('body',(0,19,0)),('rightArm',(-3,21,0)),('leftArm',(3,21,0)),('rightLeg',(-1.5,23,0)),('leftLeg',(1.5,23,0))]:bone(m,b,p)
box(m,'head',(-4,-7,-3.5),(8,7,7),'skin');box(m,'body',(-2.5,0,-2),(5,4,4),'cloth')
box(m,'rightArm',(-1,0,-1),(2,2,2),'skin');box(m,'leftArm',(-1,0,-1),(2,1,2),'skin')
box(m,'rightLeg',(-1,0,-1),(2,1,3),'cloth');box(m,'leftLeg',(-1,0,-1),(2,1,2),'cloth')
# Ovinnik: broad arched quadruped, side ears, bare heavy paw and two-part tail.
m=mob('Ovinnik')
for b,p in [('head',(0,7,-11)),('body',(0,7,0)),('rightArm',(-5,12,-8)),('leftArm',(5,12,-8)),('rightLeg',(-4.5,13,9)),('leftLeg',(4.5,13,9)),('tail',(0,7,12)),('jaw',(0,7,-11))]:bone(m,b,p)
box(m,'body',(-6,-5,-10),(12,12,23),'ash');box(m,'body',(-5,-7,-5),(10,3,14),'hair');box(m,'body',(-7,-3,-10),(14,8,6),'hair')
box(m,'head',(-5,-5,-4),(10,9,8),'ash');box(m,'head',(-4,0,-7),(8,3,4),'hair')
box(m,'head',(-7,-8,-1),(3,5,2),'hair');box(m,'head',(4,-8,-1),(3,5,2),'hair')
box(m,'jaw',(-3,3,-6),(6,2,4),'ember')
for x in (-3,2):box(m,'jaw',(x,2,-6),(1,2,1),'bone')
for b in ('rightLeg','leftLeg'):box(m,b,(-2.3,0,-2.5),(4.6,11,5),'ash')
box(m,'rightArm',(-2.5,0,-2.5),(5,12,5),'hair');box(m,'leftArm',(-2.3,0,-2.5),(4.6,11,5),'skin');box(m,'leftArm',(-3,9,-4),(6,3,6),'skin')
for x in (-2,0,2):box(m,'leftArm',(x,11,-6),(1,1,3),'bone')
box(m,'tail',(-1.5,-1.5,0),(3,3,11),'ash');box(m,'tail',(-2,-2,9),(4,4,5),'hair')

ANIM={
'Kikimora':'''head.xRot += .16F + MathHelper.sin(age*.035F)*.06F; body.xRot=.13F; float step=MathHelper.cos(walk*(entity.state()==4?1.5F:1))*amount;rightLeg.xRot=step*.9F;leftLeg.xRot=-step*.9F;rightArm.xRot=-.3F-step*.25F-MathHelper.sin(attackTime*3.14159F)*1.7F;leftArm.xRot=-.3F+step*.25F;bonnet.visible=(entity.variant()/4)==0;cap.visible=!bonnet.visible;bonnet.xRot=head.xRot;bonnet.yRot=head.yRot;cap.xRot=head.xRot;cap.yRot=head.yRot;if(entity.state()==1){rightLeg.xRot=leftLeg.xRot=0;}''',
'Poludnitsa':'''float reveal=entity.form()/20F;head.xRot-=reveal*.15F;rightArm.xRot=-.15F-MathHelper.sin(attackTime*3.14159F)*1.8F;leftArm.xRot=-.1F-reveal*.3F;rightLeg.xRot=MathHelper.cos(walk*.4F)*amount*.25F;leftLeg.xRot=-rightLeg.xRot;dress.zRot=MathHelper.sin(age*.035F)*.012F;jaw.visible=entity.form()>10;ribs.visible=jaw.visible;jaw.yRot=head.yRot;''',
'Polevik':'''rightLeg.xRot=MathHelper.cos(walk*.6F)*amount*.6F;leftLeg.xRot=-rightLeg.xRot;rightArm.zRot=-.09F-MathHelper.sin(age*.05F)*.08F;leftArm.zRot=-rightArm.zRot;rightArm.xRot=-MathHelper.sin(attackTime*3.14159F)*1.5F;if(entity.state()==1){head.y+=8;body.y+=8;rightArm.y+=8;leftArm.y+=8;rightLeg.xRot=-1.15F;leftLeg.xRot=-1.15F;}if(entity.state()==2){rightArm.xRot=leftArm.xRot=-1;}''',
'Bannik':'''body.xRot=.15F;head.xRot+=.1F;rightLeg.xRot=MathHelper.cos(walk*.7F)*amount*.5F;leftLeg.xRot=-rightLeg.xRot;rightArm.xRot=-.15F-MathHelper.sin(attackTime*3.14159F)*1.2F;leftArm.xRot=-.15F;if(entity.state()==4){head.xRot=-.35F;rightArm.xRot=leftArm.xRot=-1.1F;}''',
'Igosha':'''float hop=entity.isOnGround()?0:MathHelper.sin(age*.65F)*.08F;body.zRot=hop;head.zRot=-hop*.4F;rightArm.zRot=-.3F;leftArm.zRot=.4F;rightLeg.xRot=leftLeg.xRot=0;''',
'Ovinnik':'''rightArm.xRot=MathHelper.cos(walk*.65F)*amount*.7F;leftArm.xRot=-rightArm.xRot;rightLeg.xRot=-rightArm.xRot;leftLeg.xRot=rightArm.xRot;tail.yRot=MathHelper.sin(age*.06F)*.25F;tail.xRot=entity.state()==0?.35F:-.35F;jaw.visible=entity.state()!=0;jaw.yRot=head.yRot;if(entity.state()==0){body.y+=3;head.y+=3;rightArm.xRot=leftArm.xRot=-.65F;rightLeg.xRot=leftLeg.xRot=-.8F;}if(entity.state()==4)leftArm.xRot=-1.6F;if(entity.state()==5){body.y+=2;head.xRot=-.25F;}'''}

def png(path,rows):
 def ch(t,d):return struct.pack('!I',len(d))+t+d+struct.pack('!I',zlib.crc32(t+d)&0xffffffff)
 h=len(rows);w=len(rows[0]);raw=b''.join(b'\0'+bytes(c for p in row for c in (*p,255)) for row in rows)
 path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(b'\x89PNG\r\n\x1a\n'+ch(b'IHDR',struct.pack('!IIBBBBB',w,h,8,6,0,0,0))+ch(b'IDAT',zlib.compress(raw))+ch(b'IEND',b''))
def colors(mat):return [tuple(int(s[i:i+2],16) for i in (0,2,4)) for s in PAL[mat]]
def jfloat(x):return str(float(x))+'F'

def build_models():
 for name,m in D.items():
  rows=[[(24,26,27)]*256 for _ in range(256)];u=v=rowh=0;code=[];face=None
  for b,bd in m.items():
   code.append(f'        {b}=new ModelRenderer(this);{b}.setPos('+','.join(map(jfloat,bd['pos']))+');')
   for xyz,size,mat in bd['boxes']:
    w,h,d=[math.ceil(a) for a in size];tw=2*(w+d);th=h+d
    if u+tw>=256:u=0;v+=rowh+1;rowh=0
    assert v+th<256,(name,b);pal=colors(mat)
    if mat=='skin' and name in ('Poludnitsa','Igosha','Ovinnik'):
     overrides={'Poludnitsa':['8c8879','b7b39f','d7d3bc','ebe4cc'],'Igosha':['6a6d69','929792','b9bdb3','d2d4c9'],'Ovinnik':['211f20','3e3430','60504a','837062']}
     pal=[tuple(int(c[i:i+2],16) for i in (0,2,4)) for c in overrides[name]]
    for y in range(th):
     for x in range(tw):
      shade=2 if y<d else 1
      if x%max(2,w)==0:shade=0
      if y%7==2 and x%5 in (1,2):shade=min(3,shade+1)
      if mat in ('straw','hair','wood') and x%3==1:shade=min(3,shade+1)
      rows[v+y][u+x]=pal[shade]
    code.append(f'        {b}.texOffs({u},{v}).addBox('+','.join(map(jfloat,xyz+size))+');')
    if b=='head' and face is None:face=(u+d,v+d,w,h)
    u+=tw+1;rowh=max(rowh,th)
  variants=4 if name=='Kikimora' else 2 if name=='Poludnitsa' else 1
  for var in range(variants):
   image=[row[:] for row in rows];fu,fv,fw,fh=face
   eye=(149,166,100) if name=='Kikimora' else (93,203,235) if name=='Polevik' else (247,143,34) if name=='Ovinnik' else (190,197,170)
   for ex in (fu+1,fu+fw-2):
    for yy in (fv+max(2,fh//2)-1,fv+max(2,fh//2)):image[yy][ex]=(32,34,31)
    image[fv+max(2,fh//2)][ex]=eye
   for xx in range(fu+fw//2-1,fu+fw//2+1):image[fv+fh-2][xx]=(68,57,49)
   if name=='Kikimora' and var:
    base=colors('cloth');dest=[(a,b,c) for a,b,c in ([(46,57,42),(68,82,54),(103,112,77),(133,140,100)] if var==1 else [(62,39,33),(94,53,43),(125,76,57),(151,105,78)] if var==2 else [(60,57,51),(87,80,69),(118,109,93),(144,133,115)])]
    mapping=dict(zip(base,dest));image=[[mapping.get(p,p) for p in row] for row in image]
   if name=='Poludnitsa' and var:
    for yy in range(fv+fh-3,fv+fh-1):
     for xx in range(fu+1,fu+fw-1):image[yy][xx]=(39,33,30)
    image=[[tuple(int(c*.8) for c in p) if p in colors('cloth') else p for p in row] for row in image]
   suffix='_'+str(var) if name=='Kikimora' else '_revealed' if name=='Poludnitsa' and var else ''
   png(A/f'textures/entity/{name.lower()}{suffix}.png',image)
  fields=', '.join(m)
  reset='\n'.join(f'        {b}.y={jfloat(bd["pos"][1])};{b}.xRot={b}.yRot={b}.zRot=0;' for b,bd in m.items())
  render='\n'.join(f'        {b}.render(p,b,l,o,r,g,blue,a);' for b in m)
  src=f'''package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.{name}Entity;
public final class {name}Model extends EntityModel<{name}Entity> {{
    private final ModelRenderer {fields};
    public {name}Model() {{texWidth=256;texHeight=256;
{chr(10).join(code)}
    }}
    @Override public void setupAnim({name}Entity entity,float walk,float amount,float age,float yaw,float pitch) {{
{reset}
        head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
        {ANIM[name]}
    }}
    @Override public void renderToBuffer(MatrixStack p,IVertexBuilder b,int l,int o,float r,float g,float blue,float a){{
{render}
    }}
}}
'''
  (J/f'{name}Model.java').write_text(src,encoding='utf-8')
  texture='"'+name.lower()+'.png"'
  if name=='Kikimora':texture='"kikimora_"+(e.variant()%4)+".png"'
  if name=='Poludnitsa':texture='e.form()>10?"poludnitsa_revealed.png":"poludnitsa.png"'
  scale=''
  if name=='Poludnitsa':scale='@Override protected void scale(PoludnitsaEntity e,com.mojang.blaze3d.matrix.MatrixStack p,float partial){float s=1+e.form()*.016F;p.scale(s,s,s);}'
  (J/f'{name}Renderer.java').write_text(f'''package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.{name}Entity;
public final class {name}Renderer extends MobRenderer<{name}Entity,{name}Model> {{
 public {name}Renderer(EntityRendererManager m){{super(m,new {name}Model(),{'.7F' if name=='Ovinnik' else '.4F'});}}
 @Override public ResourceLocation getTextureLocation({name}Entity e){{return new ResourceLocation("slavicmyths","textures/entity/"+({texture}));}}
 {scale}
}}
''',encoding='utf-8')
 (R/'tools/land_spirit_geometry.json').write_text(json.dumps(D),encoding='utf-8')

if __name__=='__main__':build_models()
