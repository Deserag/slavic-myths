"""Deterministic pixel assets for the four supplied voxel references. No old assets overwritten.
Authored UV materials: bark/moss; linen/straw/black forearms; hair/rags/thread-bound
skin; black scales/pale ventral plates/crimson fins. No external image/audio sources.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json, math, re
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';J=R/'src/main/java/org/slavicmyths/client'
OUT=A/'textures/entity/mor';OUT.mkdir(parents=True,exist_ok=True)
COLORS={'bark':('#382D2A','#231D1C','#625148'),'moss':('#66694A','#929276','#4B4D38'),
 'linen':('#C9BEB0','#978F86','#66504D'),'straw':('#AD8C58','#746447','#C1A572'),
 'black':('#291C20','#1C181B','#4E373A'),'skin':('#9B8180','#6E5A5D','#B8A09A'),
 'rags':('#352629','#4E373A','#1C181B'),'hair':('#241C22','#1C181B','#352629'),
 'scales':('#201B25','#352C35','#594447'),'belly':('#97817B','#6E5A5D','#B19A90'),
 'fin':('#B9263B','#E74855','#66141D'),'eye':('#FF555A','#F34250','#A81828'),
 'metal':('#A5A2A0','#545050','#291C20')}
def material(im,rect,name,veins=True):
 x,y,w,h=[int(math.ceil(v)) for v in rect];pix=im.load();palette=COLORS[name]
 for py in range(max(0,y),min(im.height,y+h)):
  for px in range(max(0,x),min(im.width,x+w)):
   rx,ry=px-x,py-y;color=palette[(rx//2+ry//3)%3] if (rx*7+ry*11)%13<3 else palette[0]
   if veins and name not in ('eye','straw','moss'):
    # One angular seam with short branches, on each mapped surface.
    seam=w//2+(ry//3%3)-1
    if rx==seam and ry>h//4:color='#F0444E' if ry%4==0 else '#7F1821'
    if ry%7==3 and abs(rx-seam)<=2:color='#7F1821'
   pix[px,py]=tuple(bytes.fromhex(color[1:]))+(255,)
def cube(im,u,v,w,h,d,name,veins=True):
 # Minecraft box UV: two caps and four walls. Never paint outside the UV island.
 u,v=int(u),int(v)
 for index,rect in enumerate([(u+d,v,w,d),(u+d+w,v,w,d),(u,v+d,d,h),(u+d,v+d,w,h),(u+d+w,v+d,d,h),(u+2*d+w,v+d,w,h)]):material(im,rect,name,veins and index in (3,5))
number=r'(-?\d+(?:\.\d+)?F?)'
pattern=re.compile(r'geometry\.box\((\w+),\s*'+r',\s*'.join([number]*8)+r'\);')
def humanoid(name,size):
 im=Image.new('RGBA',(size,size),(35,29,30,255));source=(J/(name.capitalize()+'Model.java')).read_text()
 for match in pattern.finditer(source):
  part,*nums=match.groups();u,v,x,y,z,w,h,d=[float(a.rstrip('F')) for a in nums]
  lower=part.lower()
  if name=='poludnitsa':
   kind='straw' if any(a in lower for a in ('hair','wreath','ear')) else 'black' if any(a in lower for a in ('fore','hand','finger','sickle')) else 'skin' if 'head' in lower or 'nose' in lower else 'linen'
  else:kind='hair' if any(a in lower for a in ('hair','bonnet','cap')) else 'skin' if any(a in lower for a in ('head','nose','arm','fore','leg','shin','foot','toe')) else 'black' if any(a in lower for a in ('hand','finger')) else 'rags'
  if name=='poludnitsa' and 'sickle' in lower and 188<=u<=214:kind='metal'
  cube(im,u,v,w,h,d,kind,kind!='straw' and not any(k in lower for k in ('head','nose')))
  if 'head' in lower and w>3 and h>5:
   frontx=int(u+d);fronty=int(v+d);draw=ImageDraw.Draw(im);draw.rectangle((frontx+1,fronty+2,frontx+1,fronty+3),fill='#FF555A');draw.rectangle((frontx+int(w)-2,fronty+2,frontx+int(w)-2,fronty+3),fill='#FF555A')
 # Feature islands match the additional mor geometry, not the ordinary mesh.
 material(im,(224,224,32,16),'straw' if name=='poludnitsa' else 'hair',False)
 material(im,(224,240,32,16),'straw' if name=='poludnitsa' else 'hair',False)
 material(im,(192,224,16,16),'linen' if name=='poludnitsa' else 'rags')
 material(im,(192,240,16,16),'black')
 material(im,(208,224,16,16),'rags');material(im,(208,240,16,16),'black')
 material(im,(216,244,8,8),'eye',False)
 return im
images={n:humanoid(n,256) for n in ('poludnitsa','kikimora')}
lesh=Image.new('RGBA',(64,64),(35,29,28,255))
for args in [(0,0,6,8,6),(0,16,6,13,4),(24,16,3,19,3),(0,36,3,14,4),(40,0,2,11,2),(40,26,4,2,2)]:cube(lesh,*args,'bark')
material(lesh,(48,48,16,16),'moss',False)
draw=ImageDraw.Draw(lesh);draw.rectangle((7,10,8,11),fill='#FF555A');draw.rectangle((10,10,11,11),fill='#FF555A')
images['leshy']=lesh
snake=Image.new('RGBA',(512,512),(32,27,37,255))
for index,kind in enumerate(['scales','belly','scales','fin','eye','scales','belly','fin','eye','scales']):material(snake,(index%4*128,index//4*128,128,128),kind,kind not in ('eye','fin'))
images['fire_serpent']=snake
for name,im in images.items():
 im.save(OUT/(name+'.png'))
 # Middle texture is a discrete dark-spotted stage, with dimmer crimson seams.
 stage=im.copy();pixels=stage.load()
 for y in range(im.height):
  for x in range(im.width):
   r,g,b,a=pixels[x,y]
   if (x//4+y//5)%4==0:pixels[x,y]=(r//2,g//2,b//2,a)
   elif r>g*2:pixels[x,y]=(max(60,r//2),g,b,a)
 stage.save(OUT/(name+'_forming.png'))
flask=Image.new('RGBA',(16,16));d=ImageDraw.Draw(flask)
d.rectangle((6,1,9,3),fill='#6C4A30');d.line((6,1,9,1),fill='#AB8873')
d.rectangle((6,4,9,6),fill='#28181F');d.rectangle((4,6,11,8),fill='#28181F');d.rectangle((3,8,12,13),fill='#28181F');d.rectangle((4,14,11,14),fill='#28181F')
d.rectangle((4,8,11,12),fill='#55202C');d.rectangle((5,13,10,13),fill='#55202C');d.line((4,8,11,8),fill='#E04A4F');d.rectangle((6,10,7,11),fill='#9E2435');d.rectangle((9,11,10,12),fill='#E04A4F');d.line((4,9,4,11),fill='#AB8873')
flask.save(A/'textures/item/mor_flask.png')
(A/'models/item/mor_flask.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/mor_flask'}},indent=2)+'\n')
bolt=Image.new('RGBA',(16,16));d=ImageDraw.Draw(bolt);d.rectangle((3,3,12,12),fill='#66141D');d.rectangle((5,1,10,14),fill='#B9263B');d.rectangle((6,5,9,10),fill='#201B25');d.rectangle((3,7,4,9),fill='#E74855');bolt.save(OUT/'bolt.png')
root=Image.new('RGBA',(32,32),(35,29,28,255));material(root,(0,0,32,32),'bark');root.save(OUT/'root.png')
(A/'textures/particle').mkdir(exist_ok=True);spark=Image.new('RGBA',(4,4),(255,255,255,255));spark.putpixel((0,0),(210,210,210,255));spark.save(A/'textures/particle/mor_square.png')
(A/'particles').mkdir(exist_ok=True)
for name in ('mor_spark','mor_ash'):(A/'particles'/f'{name}.json').write_text(json.dumps({'textures':['slavicmyths:mor_square']})+'\n')
for lang in ('ru_ru','en_us'):
 p=A/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));ru=lang=='ru_ru'
 data.update({'item.slavicmyths.mor_flask':'Склянка мора' if ru else 'Flask of Blight','item.slavicmyths.mor_flask.tooltip':'Для испытаний: превращает Лешего, Полудницу, Кикимору и Огненного змея в моровые формы, Лихо и Овинника — в высшие моровые.' if ru else 'For testing: blights Leshy, Poludnitsa, Kikimora and Fire Serpent; grants greater blighted forms to Likho and Ovinnik.',
 'tales.entry.mor_first_signs.title':'Мор: первые признаки' if ru else 'Blight: First Signs',
 'tales.entry.mor_first_signs.known':'В привычном мире появились моровые твари. Багровые трещины и злоба искажают прежних обитателей. Откуда взялась хворь — неизвестно.' if ru else 'Blighted creatures have appeared in the familiar world. Crimson cracks and malice distort its former inhabitants. The origin of this sickness is unknown.'})
 for slug,ru_name,en_name in [('leshy','Моровой Леший','Blighted Leshy'),('poludnitsa','Моровая Полудница','Blighted Poludnitsa'),('kikimora','Моровая Кикимора','Blighted Kikimora'),('fire_serpent','Моровой Огненный змей','Blighted Fire Serpent')]:data[f'entity.slavicmyths.mor_{slug}']=ru_name if ru else en_name
 for stage in ('rumor','discovered','studied','verified'):data[f'tales.entry.mor_first_signs.{stage}']=data['tales.entry.mor_first_signs.known']
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
tale={'part':'creatures','chapter':'forest','section':'inhabitants','title':'tales.entry.mor_first_signs.title','icon':'slavicmyths:mor_flask','initial':0,'subjects':[],'advancements':[],'links':[]}
(R/'src/main/resources/data/slavicmyths/tales/mor_first_signs.json').write_text(json.dumps(tale,indent=2)+'\n')
print('MOR_ASSETS generated four distinct UV atlases, four forming stages, flask and bolt; RU/EN + one tale')
