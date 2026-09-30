"""0.5.1 native hierarchical mesh/pixel/audio masters. Only four targeted spirits."""
from pathlib import Path
import math,json,struct,zlib,random,wave,subprocess,shutil,sys
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';J=R/'src/main/java/org/slavicmyths'
PAL={
 'skin':['8b7561','b39a81','d4bea1','e6d2b6'], 'linen':['857966','b6a68a','d2c4a6','e5d8bd'],
 'wheat':['695334','9b7d4a','bea065','d5bb81'], 'soil':['30291f','514030','785f43','967a52'],
 'stalk':['665336','927944','b4a060','d0b775'], 'olive':['34422d','536344','768461','9ba27c'],
 'bark':['413728','695338','947a50','b4996b'], 'hair':['30302d','4a473e','686050','887d68'],
 'mud':['39332f','605349','827367','a18e7c'], 'cloth':['50463b','786a55','9a8a6d','b6a285'],
 'sickle':['34434e','6c7d87','a1afb3','d0d6cf'], 'red':['53352d','775044','9e6f59','ba8a6b'],
 'bird':['32302a','504a36','766a4a','938362']}
def rgba(s):return tuple(int(s[i:i+2],16) for i in (0,2,4))+(255,)
def png(path,rows):
 def c(t,d):return struct.pack('!I',len(d))+t+d+struct.pack('!I',zlib.crc32(t+d)&0xffffffff)
 h=len(rows);w=len(rows[0]);raw=b''.join(b'\0'+bytes(c for p in row for c in p) for row in rows)
 path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(b'\x89PNG\r\n\x1a\n'+c(b'IHDR',struct.pack('!IIBBBBB',w,h,8,6,0,0,0))+c(b'IDAT',zlib.compress(raw))+c(b'IEND',b''))
def js(p,o):p.write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def f(n):return str(float(n))+'F'
class Mesh:
 def __init__(self):self.parts={}
 def part(self,n,parent=None,pos=(0,0,0),rot=(0,0,0)):
  self.parts[n]={'parent':parent,'pos':pos,'rot':rot,'boxes':[]};return n
 def box(self,n,p,s,mat,detail=''):
  self.parts[n]['boxes'].append({'pos':p,'size':s,'mat':mat,'detail':detail});return n
 def limb(self,n,parent,pos,size,mat,rot=(0,0,0)):
  self.part(n,parent,pos,rot);self.box(n,(-size[0]/2,0,-size[2]/2),size,mat);return n

def emit(name,m,animation,variants=1):
 rows=[[(0,0,0,0)]*256 for _ in range(256)];u=v=rowh=0;code=[];surfaces=[]
 for bn,bd in m.parts.items():
  code.append(f' {bn}=new ModelRenderer(this);{bn}.setPos('+','.join(map(f,bd['pos']))+');')
  if bd['parent']:code.append(f' {bd["parent"]}.addChild({bn});')
  for bi,box in enumerate(bd['boxes']):
   w,h,d=[math.ceil(x) for x in box['size']];tw=2*(w+d);th=h+d
   if u+tw>=256:u=0;v+=rowh+2;rowh=0
   assert v+th<256
   pal=[rgba(c) for c in PAL[box['mat']]]
   # Fractional cuboid dimensions interpolate UVs between integer texels.
   # Fill the allocation first so those edges never sample transparent gaps.
   for py in range(v,v+th):
    for px in range(u,u+tw):rows[py][px]=pal[1]
   # Paint all six box faces independently: shaded edges, central cloth folds, quiet pixel clusters.
   faces=[(u+d,v,w,d),(u+d+w,v,w,d),(u,v+d,d,h),(u+d,v+d,w,h),(u+d+w,v+d,d,h),(u+2*d+w,v+d,w,h)]
   for fi,(xx,yy,fw,fh) in enumerate(faces):
    for y in range(fh):
     for x in range(fw):
      shade=2 if fi in (0,3) else 1
      if x==0 or x==fw-1:shade=max(0,shade-1)
      if y==fh-1:shade=max(0,shade-1)
      if box['mat'] in ('hair','wheat','stalk','bark') and x%3==1:shade=max(0,shade-1)
      if box['mat'] in ('linen','cloth') and fw>3 and x==fw//2:shade=min(3,shade+1)
      if box['mat']=='mud' and ((x//2+bi)%4==0 and (y//2+fi)%3==0):shade=0
      if box['mat']=='linen' and y>fh-3 and (x+bi)%4<2:shade=1
      rows[yy+y][xx+x]=pal[shade]
   fx,fy=u+d,v+d
   if box['detail']=='face':
    eyes=(74,85,89,255) if name=='Poludnitsa' else (70,165,193,255) if name=='Polevik' else (132,132,104,255)
    for x in (1,w-2):rows[fy+max(2,h//2)][fx+x]=eyes
    for x in range(w//2-1,w//2+1):rows[fy+h-2][fx+x]=(97,70,59,255)
   if box['detail']=='hem':
    for x in range(1,w-1,3):rows[fy+max(0,h-2)][fx+x]=rgba(PAL['red'][1])
   surfaces.append((box['mat'],box['detail'],faces,bi))
   code.append(f' {bn}.texOffs({u},{v}).addBox('+','.join(map(f,box['pos']+box['size']))+');')
   u+=tw+2;rowh=max(rowh,th)
 for variant in range(variants):
  image=[r[:] for r in rows]
  if name=='Poludnitsa' and variant:
   for mat,detail,faces,bi in surfaces:
    if mat in ('skin','linen'):
     for xx,yy,fw,fh in faces:
      for y in range(fh):
       for x in range(fw):
        c=image[yy+y][xx+x];image[yy+y][xx+x]=tuple(int(v*.82) for v in c[:3])+(255,)
    if detail=='face':
     xx,yy,fw,fh=faces[3]
     for x in (1,fw-2):image[yy+fh//2][xx+x]=(48,45,40,255)
     for x in range(1,fw-1):image[yy+fh-2][xx+x]=(84,66,57,255)
  if name=='Kikimora':
   choices=[['51483f','786b59','9b8b71','b4a48a'],['374036','535d48','778066','92997c'],['4a322d','6a443b','8c5c4c','a97860'],['696251','8e836c','b1a487','c9b998']]
   mapping=dict(zip([rgba(c) for c in PAL['cloth']],[rgba(c) for c in choices[variant]]))
   image=[[mapping.get(c,c) for c in row] for row in image]
   # Structural cloth differences: stitched patches and different damaged hems, not hue-only variants.
   for mat,detail,faces,bi in surfaces:
    if mat!='cloth' or bi%3!=variant%3:continue
    xx,yy,fw,fh=faces[3]
    for y in range(max(1,fh//2),min(fh,fh//2+3)):
     for x in range(1,min(fw,4)):image[yy+y][xx+x]=rgba(PAL['red' if variant%2 else 'linen'][1 if x%2 else 2])
  suffix='_'+str(variant) if name=='Kikimora' else '_revealed' if name=='Poludnitsa' and variant else ''
  png(A/f'textures/entity/{name.lower()}{suffix}.png',image)
 resets=[]
 for b,bd in m.parts.items():
  resets.append(f' {b}.setPos('+','.join(map(f,bd['pos']))+');'+''.join(f'{b}.{axis}Rot={f(bd["rot"][i])};' for i,axis in enumerate(('x','y','z'))))
 roots=[b for b,d in m.parts.items() if not d['parent']]
 render=''.join(f'{b}.render(p,b,l,o,r,g,blue,a);' for b in roots)
 extra=''
 if name=='Poludnitsa':
  extra='private float presentationScale=1;'
  render='p.pushPose();p.translate(0,1.5,0);p.scale(presentationScale,presentationScale,presentationScale);p.translate(0,-1.5,0);'+render+'p.popPose();'
 src=f'''package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.{name}Entity;
public final class {name}Model extends EntityModel<{name}Entity>{{
 private final ModelRenderer {','.join(m.parts)};{extra}
 public {name}Model(){{texWidth=256;texHeight=256;
{chr(10).join(code)}
 }}
 @Override public void setupAnim({name}Entity e,float walk,float amount,float age,float yaw,float pitch){{
{chr(10).join(resets)}
 {animation}
 }}
 @Override public void renderToBuffer(MatrixStack p,IVertexBuilder b,int l,int o,float r,float g,float blue,float a){{{render}}}
}}
'''
 (J/f'client/{name}Model.java').write_text(src,encoding='utf-8')
 work=R/'work/visual051';work.mkdir(parents=True,exist_ok=True);js(work/(name+'.json'),m.parts)
 return m

def audio(name,kinds):
 sounds=json.loads((A/'sounds.json').read_text());tmp=R/'work/audio051';tmp.mkdir(parents=True,exist_ok=True)
 for kind in kinds:
  dur={'ambient':2.4,'notice':.55,'transform':1.0,'attack':.38,'hurt':.55,'death':1.5,'step':.22,'steam':1.2,'angry':.65,'laugh':.5}.get(kind,.7)
  sr=22050;rng=random.Random('051'+name+kind);low=prev=0;phase=0;samples=[]
  for i in range(int(sr*dur)):
   t=i/sr;u=t/dur;n=rng.uniform(-1,1);low=.92*low+.08*n;high=n-low
   env=min(1,t/.025)*max(0,1-u)**1.7
   if kind=='ambient':env=(math.sin(math.pi*u)**2)*(.65+.35*math.sin(t*3))
   base={'poludnitsa':205,'polevik':78,'bannik':67,'kikimora':155}[name]
   phase+=2*math.pi*(base*(1-.12*u)+math.sin(t*17)*2)/sr
   voice=(math.sin(phase)+.3*math.sin(2*phase)+.12*math.sin(3*phase))*.08
   breath=low*.5+high*.015
   if name=='poludnitsa':value=voice*.45+breath*.8
   elif name=='polevik':value=breath*.8+voice*.15
   elif name=='bannik':value=voice*.75+low*.8
   else:value=(voice*.3+breath)*(.7+.3*math.sin(t*28))
   if kind in ('transform','angry','hurt','attack'):value*=1.4
   if kind=='transform':value+=high*.075*math.exp(-((u-.2)/.1)**2)+voice*.8*u
   if kind=='steam':value=(high*.09+low*.7)*(1-math.exp(-t*25))
   if kind=='step':value=(low*.35+math.sin(2*math.pi*(90 if name=='bannik' else 440)*t)*.08)*math.exp(-t*20)
   if kind=='laugh':value*=math.sin(t*22)**4
   if name=='polevik' and kind in ('hurt','death','step'):
    for crack in (.06,.23,.54):value+=high*.12*math.exp(-abs(t-crack)*140)
   samples.append(int(max(-.85,min(.85,value*env))*32767))
  wav=tmp/(name+'_'+kind+'.wav')
  with wave.open(str(wav),'wb') as w:w.setparams((1,2,sr,len(samples),'NONE',''));w.writeframes(struct.pack('<'+'h'*len(samples),*samples))
  out=A/f'sounds/land/{name}_{kind}.ogg'
  subprocess.run([shutil.which('ffmpeg'),'-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(out)],check=True)
  sounds[name+'_'+kind]={'subtitle':'subtitles.slavicmyths.'+name+'_'+kind,'sounds':[{'name':'slavicmyths:land/'+name+'_'+kind,'volume':.6}]}
  p=J/'registry/ModSounds.java';s=p.read_text();id=name+'_'+kind
  if 'sound("'+id+'")' not in s:s=s.replace('    private static RegistryObject<SoundEvent> sound',f'    public static final RegistryObject<SoundEvent> {id.upper()} = sound("{id}");\n    private static RegistryObject<SoundEvent> sound');p.write_text(s)
 js(A/'sounds.json',sounds)
 ru={'poludnitsa':'Полудница','polevik':'Полевик','bannik':'Банник','kikimora':'Кикимора'}
 labels={'ambient':'тихий шорох','notice':'вдох','transform':'преображение','attack':'резкий выдох','hurt':'хрип','death':'затихает','step':'шаг','steam':'шипение пара','angry':'предупреждение','laugh':'сухой смешок'}
 for lang in ('ru_ru','en_us'):
  p=A/f'lang/{lang}.json';o=json.loads(p.read_text(encoding='utf-8'))
  for kind in kinds:o['subtitles.slavicmyths.'+name+'_'+kind]=(ru[name]+': '+labels[kind]) if lang=='ru_ru' else name.capitalize()+' '+kind
  js(p,o)

def poludnitsa():
 m=Mesh()
 for prefix,reveal in [('n',False),('r',True)]:
  root=m.part(prefix+'Root');body=m.part(prefix+'Body',root,(0,-5 if reveal else 2,0),(.18 if reveal else .015,0,0))
  m.box(body,(-2 if reveal else -2.6,0,-1.5),(4 if reveal else 5.2,12 if reveal else 7,3),'linen','hem')
  m.box(body,(-1.8,10 if reveal else 7,-1.4),(3.6,3,2.8),'linen')
  head=m.part(prefix+'Head',body,(0,-4 if reveal else -.5,-.9 if reveal else 0))
  m.box(body,(-.75,-4 if reveal else -1,-.7),(1.5,4 if reveal else 1,1.4),'skin')
  m.box(head,(-2.5,-7 if reveal else -6,-2.5),(5,7 if reveal else 6,5),'skin','face')
  m.box(head,(-1.8,-.6,-2.3),(3.6,1,3.8),'skin')
  nose=m.part(prefix+'Nose',head,(0,-2.3,-2.5),(.08,0,0));m.box(nose,(-.45,-.4,-.7),(.9,1.3,.9),'skin')
  hair=m.part(prefix+'BackHair',head,(0,-5,1.5),(.08,0,0));m.box(hair,(-2.8,0,0),(5.6,8,1.5),'wheat')
  for side,sign in [('Left',1),('Right',-1)]:
   strand=m.part(prefix+side+'Hair',head,(sign*2.5,-4,-1.5),(.03,0,sign*(.2 if reveal else .055)));m.box(strand,(-.6,0,-.5),(1.2,9 if side=='Left' else 7,1.1),'wheat')
  wreath=m.part(prefix+'Wreath',head,(0,-5.9 if not reveal else -6.9,0),(0,0,.09 if reveal else 0))
  for pos,size in [((-2.8,0,-2.8),(5.6,.55,.65)),((-2.8,0,2),(5.6,.55,.65)),((-2.8,0,-2.1),(.6,.55,4.2)),((2.2,0,-2.1),(.6,.55,4.2))]:m.box(wreath,pos,size,'bark')
  for i,(x,z,angle,h) in enumerate([(-2,-2,.35,1.6),(0,-2.7,-.22,2),(2,-1.5,-.4,1.4),(-2.6,.5,.42,1.2),(1.8,2,-.25,1.8),(0,2,.15,1.1)]):
   ear=m.part(prefix+'Ear'+str(i),wreath,(x,0,z),(.15 if i>3 else 0,0,angle));m.box(ear,(-.18,-h,0),(.36,h,.4),'wheat');m.box(ear,(-.45,-h+.2,-.15),(.9,.8,.65),'stalk')
  dress=m.part(prefix+'Dress',root,(0,8 if reveal else 11,0))
  m.box(dress,(-3,0,-2),(6,8 if reveal else 6,4),'linen');m.box(dress,(-3.5,6,-2.2),(7,5,4.4),'linen','hem')
  for i,(x,h) in enumerate([(-3.8,5),(-2,6),(0,4.5),(2,5.5)]):
   hem=m.part(prefix+'Hem'+str(i),dress,(x,10, -2.5 if i%2 else 1.2),(0,0,(i-1.5)*.025));m.box(hem,(0,0,0),(1.8,h if reveal else 2.2,1.4),'linen','hem')
  for side,sign in [('Left',1),('Right',-1)]:
   arm=m.limb(prefix+side+'Arm',body,(sign*(3.2 if reveal else 3.4),1,0),(1.4 if reveal else 1.8,8 if reveal else 4.8,1.7),'linen',(0,0,-sign*.12 if reveal else -sign*.04))
   fore=m.limb(prefix+side+'Fore',arm,(0,8 if reveal else 4.8,0),(1.1 if reveal else 1.6,9 if reveal else 4,1.3),'skin' if reveal else 'linen',(-.12,0,0))
   hand=m.limb(prefix+side+'Hand',fore,(0,9 if reveal else 4,0),(1.5,2,1.3),'skin')
   if reveal:
    for i,h in enumerate((2.3,3.1,2.6)):m.limb(prefix+side+'Finger'+str(i),hand,(-.6+i*.6,1.7,-.2),(.35,h,.45),'skin',(.1+i*.03,0,(i-1)*.06))
   m.limb(prefix+side+'Leg',root,(sign*1.4,17,0),(1.8,7,2),'skin')
  sickle=m.part(prefix+'Sickle',prefix+'RightHand',(0,1,0),(0,0,.1));m.box(sickle,(-.4,0,-.4),(.8,6 if reveal else 4,.8),'bark')
  for pos,size in [((0,3,-.5),(3,1,1)),((3,0,-.5),(1,4,1)),((1,-1,-.5),(3,1,1)),((.5,0,-.5),(1,1,1))]:m.box(sickle,pos,size,'sickle')
 anim='''float t=MathHelper.clamp((e.form()+(e.form()>0 && e.form()<20?age-(int)age:0))/20F,0,1);nRoot.visible=t<.5F;rRoot.visible=!nRoot.visible;presentationScale=(30.5F+11*t)/(t<.5F?30.5F:41.5F);
 nHead.yRot=rHead.yRot=yaw*.0174533F;nHead.xRot=rHead.xRot=pitch*.0174533F;
 nBody.zRot=MathHelper.sin(age*.022F)*.012F;nLeftArm.xRot=MathHelper.cos(walk*.5F)*amount*.1F;nRightArm.xRot=-nLeftArm.xRot;
 nLeftLeg.xRot=MathHelper.cos(walk*.5F)*amount*.25F;nRightLeg.xRot=-nLeftLeg.xRot;nDress.zRot=MathHelper.sin(walk*.4F)*amount*.015F;
 rBody.xRot=.18F+MathHelper.sin(age*.03F)*.015F;rHead.xRot-=.12F;rLeftFore.xRot=-.2F;rRightFore.xRot=-.25F;
 rRightArm.xRot=-MathHelper.sin(attackTime*3.14159F)*1.6F;rLeftArm.zRot=-.12F;rRightArm.zRot=.12F;
 nLeftHair.zRot+=MathHelper.sin(age*.035F)*.02F;rLeftHair.zRot+=MathHelper.sin(age*.06F)*.06F;
 if(t>0 && t<1){nHead.zRot=rHead.zRot=MathHelper.sin(t*3.14159F)*.32F;}'''
 emit('Poludnitsa',m,anim,2)
 p=J/'client/PoludnitsaRenderer.java';s=p.read_text();start=s.index(' @Override protected void scale');s=s[:start]+''' @Override protected void scale(PoludnitsaEntity e,com.mojang.blaze3d.matrix.MatrixStack p,float partial){shadowRadius=.3F+.08F*e.form()/20F;}
}
''';p.write_text(s)
 audio('poludnitsa',['ambient','notice','transform','attack','hurt','death'])


def polevik():
 m=Mesh();root=m.part('root');body=m.part('body',root,(0,0,0),(.07,0,-.06))
 m.box(body,(-1.6,0,-1.2),(3.2,11,2.4),'soil')
 for i,(x,y,h,ang) in enumerate([(-2.1,-.4,11,.12),(-.9,1,10,-.04),(.7,-1,12,-.09),(1.9,1.2,9,-.2)]):
  stem=m.part('torsoStem'+str(i),body,(x,y,-1.5),(.02,0,ang));m.box(stem,(-.45,0,-.35),(.9,h,.7),'stalk')
 m.box(body,(-.7,3,-1.7),(1,4,.7),'olive');m.box(body,(1,6,-1.5),(.6,3,.6),'olive')
 head=m.part('head',body,(0,-1,-.4),(.07,0,.03));m.box(head,(-2,-6,-1.9),(4,6,4),'bark','face');m.box(head,(-1.4,-.5,-2),(2.8,1,3),'soil')
 nose=m.part('nose',head,(0,-2.8,-1.9),(.25,0,0));m.box(nose,(-.45,0,-1.4),(.9,2,1.5),'bark')
 for i,(x,z,h,ang) in enumerate([(-1.7,0,2.8,.4),(-2,1,1.8,.6),(.8,1.5,3.4,-.18),(1.8,.5,2,-.55),(1.5,-1,1.3,-.35)]):
  hair=m.part('grainHair'+str(i),head,(x,-5.4,z),(.1*(i-2),0,ang));m.box(hair,(-.2,-h,-.2),(.4,h,.4),'bark');m.box(hair,(-.5,-h,-.4),(1,1.6,.8),'stalk')
 beard=m.part('beard',head,(0,-.4,-2),(.14,0,0))
 for i,(x,h,a) in enumerate([(-1.6,5,.14),(-.6,8,.03),(.4,7,-.08),(1.4,4.5,-.25)]):
  b=m.part('beardStrand'+str(i),beard,(x,0,0),(0,0,a));m.box(b,(-.5,0,-.45),(1,h,.9),'stalk');m.box(b,(-.3,h-.8,-.4),(.6,1.4,.8),'bark')
 for side,sign in [('Left',1),('Right',-1)]:
  arm=m.limb(side+'Arm',body,(sign*3,-1 if sign==1 else 1,0),(1.2,7 if sign==1 else 8,1.4),'soil',(0,0,sign*.11))
  m.box(arm,(-.7,.3,-.8),(.6,6,1),'stalk')
  fore=m.limb(side+'Fore',arm,(0,7 if sign==1 else 8,0),(1,7 if sign==1 else 8,1.2),'bark',(-.13,0,.05*sign))
  for i,h in enumerate((2,2.7,1.5)):m.limb(side+'RootFinger'+str(i),fore,(-.65+i*.6,6.5 if sign==1 else 7.5,0),(.4,h,.5),'soil',(.15,0,(i-1)*.2))
  leg=m.limb(side+'Leg',root,(sign*1.6,10,0),(1.2,7,1.4),'bark',(0,0,sign*.04))
  shin=m.limb(side+'Shin',leg,(0,7,0),(1,6,1.2),'stalk')
  for i in range(3):m.limb(side+'Toe'+str(i),shin,((i-1)*.7,5.5,-.3),(.6,1,2.5),'soil',(0,(i-1)*.35,0))
  m.box(leg,(-.85,0,-.65),(.45,7,1.3),'stalk')
 for i in range(3):
  ear=m.part('shoulderGrain'+str(i),'LeftArm',(0,i*.8,0),(0,0,-.5-i*.12));m.box(ear,(-.2,-3,-.2),(.4,3,.4),'stalk');m.box(ear,(-.5,-3,-.4),(1,1.4,.8),'wheat')
 for i in range(2):m.limb('danglingRoot'+str(i),'RightArm',(-.7+i,3,.5),(.4,4+i,.5),'soil',(0,0,.15))
 anim='''head.yRot=yaw*.0174533F;head.xRot+=pitch*.0174533F;body.zRot=-.06F+MathHelper.sin(age*.025F)*.035F;
 LeftLeg.xRot=MathHelper.cos(walk*.42F)*amount*.45F;RightLeg.xRot=-LeftLeg.xRot;LeftArm.xRot=RightLeg.xRot*.15F;RightArm.xRot=LeftLeg.xRot*.12F-MathHelper.sin(attackTime*3.14159F)*1.5F;
 beard.xRot=.14F+MathHelper.sin(age*.035F+.6F)*.035F;grainHair0.zRot+=MathHelper.sin(age*.03F)*.04F;grainHair2.xRot+=MathHelper.sin(age*.026F+1)*.035F;
 if(e.state()==1){body.y=8;LeftLeg.xRot=RightLeg.xRot=-.95F;LeftShin.xRot=RightShin.xRot=.7F;}if(e.state()==2){LeftFore.xRot=RightFore.xRot=-.7F;}'''
 emit('Polevik',m,anim)
 p=J/'client/PolevikRenderer.java';p.write_text(p.read_text().replace('new PolevikModel(),.4F','new PolevikModel(),.27F'))
 audio('polevik',['ambient','angry','step','hurt','death'])
 p=J/'entity/PolevikEntity.java';s=p.read_text()
 if 'getAmbientSoundInterval' not in s:
  i=s.rfind('}');s=s[:i]+'''    @Override public int getAmbientSoundInterval(){return 650;}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState block){if(!level.isClientSide)voice("step",.12F);}
'''+s[i:]
 p.write_text(s)

def bannik():
 m=Mesh();root=m.part('root');body=m.part('body',root,(0,8,0),(.21,0,.02));m.box(body,(-6,0,-3),(12,9,6),'mud');m.box(body,(-5.5,-1,0),(11,5,4),'mud')
 m.box(body,(-5.4,8,-3),(10.8,4,6),'cloth','hem')
 head=m.part('head',body,(0,0,-3),(-.08,0,0));m.box(head,(-3.7,-6,-2.8),(7.4,6,5.6),'mud','face')
 m.box(head,(-3.7,-4.3,-3.2),(3.2,1,1),'mud');m.box(head,(.4,-4.5,-3.2),(3.3,1,1),'mud')
 nose=m.part('nose',head,(0,-2.5,-2.8),(.15,0,0));m.box(nose,(-1.2,-.3,-1.3),(2.4,1.8,1.4),'mud')
 for i,(x,z,h) in enumerate([(-3.8,0,4.5),(3.3,1,6),(-2,2,5)]):
  hair=m.part('hair'+str(i),head,(x,-3,z),(.1,0,(i-1)*.06));m.box(hair,(-.5,0,-.4),(1,h,.8),'hair')
 beard=m.part('beard',head,(0,-.4,-3.1),(.2,0,0));m.box(beard,(-3,0,-.5),(6,2,1.5),'hair')
 for i,(x,h,ang) in enumerate([(-2.2,4,.16),(-.8,6,.04),(.7,4.8,-.06),(2,3.5,-.22)]):
  b=m.part('beardLock'+str(i),beard,(x,1.5,0),(0,0,ang));m.box(b,(-.7,0,-.6),(1.4,h,1.2),'hair')
 for side,sign in [('Left',1),('Right',-1)]:
  arm=m.limb(side+'Arm',body,(sign*7,-.4 if sign==1 else .5,0),(4,5,4),'mud',(-.1,0,sign*.08))
  fore=m.limb(side+'Fore',arm,(0,5,0),(3.5,4,3.5),'mud',(-.28,0,0))
  hand=m.limb(side+'Hand',fore,(0,4,-.3),(4.5,3,4),'mud')
  for i,h in enumerate((1.8,2.5,2.8,2.1)):m.limb(side+'Finger'+str(i),hand,(-1.8+i*1.15,2.6,-.9),(.8,h,1),'mud',(.2+(i%2)*.12,0,(i-1.5)*.025))
  m.limb(side+'Leg',root,(sign*3,17,0),(3.7,7,4),'mud')
 for i,(x,y,z) in enumerate([(4,1,3.7),(-4,8,2.9),(3,9,-3.2)]):
  leaf=m.part('whiskLeaf'+str(i),body,(x,y,z),(.2,0,.4));m.box(leaf,(-.6,0,-.15),(1.2,2,.3),'olive')
 anim='''head.yRot=yaw*.0174533F;head.xRot+=pitch*.0174533F;body.xRot=.21F+MathHelper.sin(age*.065F)*.012F;
 LeftArm.zRot+=MathHelper.sin(age*.065F)*.01F;RightArm.zRot-=MathHelper.sin(age*.065F)*.01F;
 LeftLeg.xRot=MathHelper.cos(walk*.4F)*amount*.42F;RightLeg.xRot=-LeftLeg.xRot;LeftArm.xRot+=RightLeg.xRot*.22F;RightArm.xRot+=LeftLeg.xRot*.22F;
 float hit=MathHelper.sin(attackTime*3.14159F);RightArm.xRot-=hit*1.1F;RightArm.zRot-=hit*.7F;RightFore.xRot-=hit*.5F;beard.xRot+=MathHelper.sin(age*.04F)*.025F;
 if(e.state()==4){body.xRot=.31F;head.xRot=-.2F;LeftArm.zRot=.45F;RightArm.zRot=-.45F;LeftFore.xRot=RightFore.xRot=-.5F;}'''
 emit('Bannik',m,anim)
 p=J/'client/BannikRenderer.java';p.write_text(p.read_text().replace('new BannikModel(),.4F','new BannikModel(),.55F'))
 audio('bannik',['ambient','step','angry','attack','hurt','death','steam'])
 p=J/'entity/BannikEntity.java';s=p.read_text().replace('voice("power",.7F);nextPower','voice("steam",.6F);nextPower')
 if 'getAmbientSoundInterval' not in s:
  i=s.rfind('}');s=s[:i]+'''    @Override public int getAmbientSoundInterval(){return 560;}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState block){if(!level.isClientSide)voice("step",.2F);}
    @Override public boolean doHurtTarget(net.minecraft.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level.isClientSide)voice("attack",.45F);return hit;}
'''+s[i:]
 p.write_text(s)

def kikimora():
 m=Mesh();root=m.part('root');body=m.part('body',root,(0,-3,0),(.22,0,-.025));m.box(body,(-2.1,0,-1.7),(4.2,8,3.4),'cloth');m.box(body,(-1.8,-.7,.7),(3.6,3,2),'cloth')
 for i,(x,h) in enumerate([(-2.3,3.5),(-.8,2.3),(.7,4)]):m.box(body,(x,7,-1.8),(1.5,h,3.6),'cloth','hem')
 head=m.part('head',body,(0,-1.5,-2),(.11,0,0));m.box(head,(-1.9,-6.5,-1.8),(3.8,6.5,3.6),'skin','face');m.box(head,(-1,-.3,-2.1),(2,1.2,2.9),'skin')
 nose=m.part('nose',head,(0,-3.5,-1.8),(.3,0,0));m.box(nose,(-.4,0,-1.8),(.8,2.4,2),'skin');m.box(nose,(-.3,1.8,-2.1),(.6,1,1),'skin')
 for i,(x,z,h,a) in enumerate([(-2,-1,6,.08),(1.7,-1.3,8,-.08),(-1.5,1.5,9,.1),(.8,1.7,10,-.09)]):
  hair=m.part('hair'+str(i),head,(x,-5.3,z),(.05,0,a));m.box(hair,(-.35,0,-.3),(.7,h,.6),'hair')
 bonnet=m.part('bonnet',head,(0,-6.4,0),(.03,0,.04));m.box(bonnet,(-2.3,-.8,-2.1),(4.6,1.5,4.2),'cloth');m.box(bonnet,(-2.4,.5,.8),(4.8,2,1.5),'cloth')
 cap=m.part('cap',head,(0,-6.5,0),(.02,0,-.04));m.box(cap,(-2.1,-.4,-2),(4.2,1.2,4),'cloth')
 m.box(bonnet,(-2.5,.4,-.3),(.8,2,1),'cloth');m.box(bonnet,(1.8,.4,-.3),(.8,2.5,1),'cloth')
 for side,sign in [('Left',1),('Right',-1)]:
  arm=m.limb(side+'Arm',body,(sign*2.9,.8,0),(1.1,7,1.2),'cloth',(0,0,sign*.065))
  fore=m.limb(side+'Fore',arm,(0,7,0),(.85,7,1),'skin',(-.35,0,0));hand=m.limb(side+'Hand',fore,(0,7,0),(2,2.7,1.5),'skin')
  for i,h in enumerate((2.5,3.4,2.8)):m.limb(side+'Finger'+str(i),hand,(-.8+i*.8,2.2,-.3),(.45,h,.5),'skin',(.12+i*.07,0,(i-1)*.1))
  thigh=m.limb(side+'Leg',root,(sign*1.4,6,0),(1.3,7,1.5),'skin',(-.12,0,sign*.025))
  shin=m.limb(side+'Shin',thigh,(0,7,-.5),(.8,9,1),'bird',(.17,0,0));m.box(shin,(-.6,8,-.3),(1.2,1.3,1.4),'bird')
  foot=m.part(side+'Foot',shin,(0,9,0),(-.05,0,0))
  for i in range(3):
   toe=m.part(side+'Toe'+str(i),foot,((i-1)*.65,0,0),(0,(i-1)*.23,0));m.box(toe,(-.2,0,-3.1),(.4,.65,3.6),'bird')
  m.box(foot,(-.2,0,.4),(.4,.6,1.5),'bird')
 anim='''head.yRot=yaw*.0174533F;head.xRot+=pitch*.0174533F;bonnet.visible=e.variant()<4;cap.visible=!bonnet.visible;
 float run=e.state()==4?1:0;body.xRot=.22F+run*.14F;float step=MathHelper.cos(walk*(1.05F+run*.35F))*amount*.42F;
 LeftLeg.xRot=-.12F+step;RightLeg.xRot=-.12F-step;LeftShin.xRot=.17F+Math.max(0,-step)*.5F;RightShin.xRot=.17F+Math.max(0,step)*.5F;
 LeftArm.xRot=run*.12F-step*.12F;RightArm.xRot=run*.12F+step*.12F-MathHelper.sin(attackTime*3.14159F)*1.6F;RightFore.xRot-=MathHelper.sin(attackTime*3.14159F)*.45F;
 if(((int)age+e.getId()*17)%230<7)head.zRot=.09F*MathHelper.sin(age*1.7F);
 LeftHand.zRot=MathHelper.sin(age*.018F)*.025F;if(e.state()==1){LeftLeg.xRot=RightLeg.xRot=-.12F;}'''
 emit('Kikimora',m,anim,4)
 p=J/'client/KikimoraRenderer.java';p.write_text(p.read_text().replace('new KikimoraModel(),.4F','new KikimoraModel(),.31F'))
 audio('kikimora',['ambient','notice','laugh','attack','hurt','death','step'])
 p=J/'entity/KikimoraEntity.java';s=p.read_text()
 if 'nextNoticeSound' not in s:
  s=s.replace('private int attention;','private long nextNoticeSound;private int attention;')
  s=s.replace('{state(1);getNavigation().stop();}','{if(state()!=1 && level.getGameTime()>nextNoticeSound){voice("notice",.25F);nextNoticeSound=level.getGameTime()+500;}state(1);getNavigation().stop();}')
 if 'getAmbientSoundInterval' not in s:
  i=s.rfind('}');s=s[:i]+'''    @Override public int getAmbientSoundInterval(){return 650;}
    @Override public void playAmbientSound(){if(!level.isClientSide){if(tickCount%3==0)voice("laugh",.22F);else voice("ambient",.25F);}}
    @Override protected void playStepSound(net.minecraft.util.math.BlockPos pos,net.minecraft.block.BlockState block){if(!level.isClientSide && tickCount%2==0)voice("step",.1F);}
    @Override public boolean doHurtTarget(net.minecraft.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level.isClientSide)voice("attack",.4F);return hit;}
'''+s[i:]
 p.write_text(s)

def generate():
 poludnitsa();polevik();bannik();kikimora()

if __name__=='__main__':
 globals()[sys.argv[1] if len(sys.argv)>1 else 'generate']()
