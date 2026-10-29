from pathlib import Path
import json, math
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data'
IDS=['navya_ostroga','ashen_greatsword','shadow_throwing_knife','volcanic_heart_flail','infernal_bow']
def write(p,j):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def tag(ns,n,ids):
 p=D/ns/'tags/item'/f'{n}.json';j=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};j['values']=list(dict.fromkeys(j['values']+['slavicmyths:'+v for v in ids]));write(p,j)
PAL=['#25272f','#444650','#9599a0','#59272d','#de6728','#edc66d','#39bdc4','#643e28','#d8c8a1','#121520']
im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im)
for i,c in enumerate(PAL):dr.rectangle((i%4*4,i//4*4,i%4*4+3,i//4*4+3),fill=c)
im.save(A/'textures/item/nether_weapon_materials.png')
def box(a,b,c):
 u=c%4*4;v=c//4*4
 return {'from':a,'to':b,'faces':{f:{'uv':[u+.25,v+.25,u+3.75,v+3.75],'texture':'#m'} for f in ['up','down','north','south','east','west']}}
def model(elements):return {'ambientocclusion':False,'textures':{'m':'slavicmyths:item/nether_weapon_materials','particle':'slavicmyths:item/nether_weapon_materials'},'elements':elements,'display':{'gui':{'rotation':[0,0,-40],'scale':[.32]*3},'ground':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.3]*3},'fixed':{'scale':[.35]*3},'firstperson_righthand':{'rotation':[0,-90,20],'translation':[1,1,-2],'scale':[.65]*3},'firstperson_lefthand':{'rotation':[0,90,-20],'translation':[1,1,-2],'scale':[.65]*3},'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,2,1],'scale':[.65]*3},'thirdperson_lefthand':{'rotation':[0,90,0],'translation':[0,2,1],'scale':[.65]*3}}}
models={}
b=lambda a,z,c:box(a,z,c)
e=[b([7,-10,7],[9,23,9],7),b([6,19,6],[10,24,10],5),b([7,-12,7],[9,-10,9],6)]
for s in [-1,1]:
 x=8+s*4;e += [b([min(8,x),23,7],[max(8,x)+1,25,9],1),b([x,24,7],[x+1.5,29,9],2),b([x-s,29,7],[x-s+.8,32,9],6)]
models[IDS[0]]=model(e)
e=[b([7,-11,7],[9,4,9],3),b([6,-13,6],[10,-10,10],5),b([1,3,6],[15,5,10],1),b([5,5,7],[11,28,9],1),b([6,28,7],[10,31,9],2),b([7,31,7],[9,32,9],2),b([7.5,6,6.8],[8.5,28,7],4)]
for y in range(-8,3,3):e.append(b([6.8,y,6.8],[9.2,y+.8,9.2],5))
models[IDS[1]]=model(e)
e=[b([7,1,7],[9,8,9],9),b([5,7,6],[11,9,10],1),b([6,9,7],[10,17,9],6),b([7,17,7],[9,20,9],6),b([6,1,7],[7,4,9],5),b([9,1,7],[10,4,9],5),b([7,0,7],[9,1,9],5)]
for i,c in enumerate([4,5,6,3,8]):e.append(b([7.4,9+i*1.5,6.7],[8.6,10+i*1.5,7],c))
models[IDS[2]]=model(e)
handle=[b([7.2,1,7.2],[8.8,15,8.8],7),b([6.7,13,6.7],[9.3,16,9.3],5),b([6.7,0,6.7],[9.3,2,9.3],1)]
ball=[b([5,5,5],[11,11,11],1),b([4,6,6],[12,10,10],1),b([6,4,6],[10,12,10],1),b([6,6,4],[10,10,12],1)]
for axis in range(3):
 for sign in [-1,1]:
  lo=[7,7,7];hi=[9,9,9];lo[axis]=1 if sign<0 else 11;hi[axis]=5 if sign<0 else 15;ball.append(b(lo,hi,4))
ball.append(b([6.5,6.5,3.8],[9.5,9.5,4],4))
held=handle.copy()
for y in range(-6,1,2):held.append(b([7.5,y,7.5],[8.5,y+2,8.5],2))
for p in ball:
 q=json.loads(json.dumps(p));q['from'][1]-=17;q['to'][1]-=17;held.append(q)
models[IDS[3]+'_held']=model(held);models[IDS[3]+'_handle87']=model(handle);models[IDS[3]+'_ball87']=model(ball)
write(A/'models/item'/f'{IDS[3]}.json',{'parent':'builtin/entity','textures':{'particle':'slavicmyths:item/nether_weapon_materials'}})
for pull in range(4):
 e=[b([7,2,7],[9,14,9],7),b([6.8,5,6.8],[9.2,11,9.2],3)]
 # Symmetric laminated limbs, recurved tips remain visible at every pull.
 for s in [-1,1]:
  for i in range(4):
   y=8+s*(5+i*3);x=8+(i//2)*2-pull*i*.35
   e.append(b([x-1,min(y,y+s*4),7],[x+1,max(y,y+s*4),9],7));e.append(b([x-1.2,min(y,y+s*4),6.7],[x-.7,max(y,y+s*4),9.3],8))
  y=8+s*17;e.append(b([9-pull, min(y,y+s*2),7],[13-pull,max(y,y+s*2),9],8))
 # thin actual string, bent toward hand as drawn, no second projectile model
 for s in [-1,1]:
  for i in range(8):
   t=i/8;x=(11-pull)*(1-t)+(4-pull*2)*t;y=8+s*17*(1-t)
   e.append(b([x-.15,y-1.2,7.85],[x+.15,y+1.2,8.15],8))
 name=IDS[4]+('' if pull==0 else '_pulling_'+str(pull-1));models[name]=model(e)
models[IDS[4]]['overrides']=[{'predicate':{'pulling':1},'model':'slavicmyths:item/infernal_bow_pulling_0'},{'predicate':{'pulling':1,'pull':.65},'model':'slavicmyths:item/infernal_bow_pulling_1'},{'predicate':{'pulling':1,'pull':.9},'model':'slavicmyths:item/infernal_bow_pulling_2'}]
for n,m in models.items():write(A/'models/item'/f'{n}.json',m)
# Independently authored readable pixel silhouettes, not scaled screenshots.
for i,n in enumerate(IDS):
 im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im)
 if i==0:
  dr.line((3,14,11,4),fill=PAL[7],width=2);dr.line((9,6,8,2,10,0),fill=PAL[6],width=2);dr.line((9,6,13,5,15,2),fill=PAL[6],width=2)
 elif i==1:
  dr.polygon([(5,10),(12,0),(15,0),(15,3),(8,11)],fill=PAL[2]);dr.line((3,8,10,13),fill=PAL[5],width=2);dr.line((2,15,6,10),fill=PAL[3],width=2);dr.line((7,9,13,2),fill=PAL[4])
 elif i==2:
  dr.ellipse((2,11,6,15),outline=PAL[5]);dr.line((5,11,8,8),fill=PAL[9],width=3);dr.polygon([(7,7),(15,0),(13,7),(9,10)],fill=PAL[6]);
  for k in range(5):dr.point((9+k,7-k),fill=PAL[[4,5,6,3,8][k]])
 elif i==3:
  dr.line((3,14,5,6),fill=PAL[7],width=3);dr.line((5,5,10,3),fill=PAL[2]);dr.ellipse((9,2,14,7),fill=PAL[1]);dr.line((10,0,15,9),fill=PAL[4]);dr.line((7,4,15,4),fill=PAL[4])
 else:
  dr.line((4,1,9,3,12,7,10,12,5,15),fill=PAL[8],width=2);dr.line((4,1,5,15),fill=PAL[5]);dr.rectangle((10,6,12,9),fill=PAL[3])
 im.save(A/'textures/item'/f'{n}.png');write(A/'models/item'/f'{n}_icon.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+n}})
bases=['slavicmyths:diamond_spear','minecraft:diamond_sword','slavicmyths:diamond_throwing_knife','slavicmyths:diamond_flail','slavicmyths:heavy_bow'];cores=['navya_fork_core','ashen_blade_core','shadow_knife_core','volcanic_flail_core','infernal_bow_core']
for n,base,core in zip(IDS,bases,cores):
 write(D/'slavicmyths/recipe'/f'{n}.json',{'type':'minecraft:smithing_transform','template':{'item':'slavicmyths:nether_hardening_template'},'base':{'item':base},'addition':{'item':'slavicmyths:'+core},'result':{'id':'slavicmyths:'+n,'count':1}})
 write(D/'slavicmyths/advancement/recipes/nether'/f'{n}.json',{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'slavicmyths:'+core}]}}},'rewards':{'recipes':['slavicmyths:'+n]}})
for n,ids in [('rune_spear',[IDS[0]]),('rune_heavy',[IDS[1],IDS[3]]),('rune_dagger',[IDS[2]])]:tag('slavicmyths',n,ids)
for n,ids in [('enchantable/durability',IDS),('enchantable/weapon',IDS[:4]),('enchantable/sword',IDS[:2]),('enchantable/sharp_weapon',IDS[:3]),('enchantable/bow',[IDS[4]])]:tag('minecraft',n,ids)
ru=['Навья острога','Пепельный двуручный меч','Теневой метательный нож','Кистень вулканического сердца','Пекельный лук'];en=['Navya Ostroga','Ashen Greatsword','Shadow Throwing Knife','Volcanic Heart Flail','Infernal Bow']
for lang,names in [('ru_ru',ru),('en_us',en)]:
 p=A/'lang'/f'{lang}.json';j=json.loads(p.read_text(encoding='utf-8'))
 for n,title in zip(IDS,names):j['item.slavicmyths.'+n]=title
 write(p,j)
print('5 distinct meshes, 5 GUI sprites, 4 bow states, articulated flail pieces, 5 exact transforms generated')

p=A/'creative_catalog.tsv';s=p.read_text(encoding='utf-8')
for n in IDS:
 if not any(line.split('\t')[0]==n for line in s.splitlines()):s+=n+'\tweapons\tnether_special\n'
p.write_text(s,encoding='utf-8')

