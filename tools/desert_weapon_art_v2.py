"""Native pixel silhouettes extruded into bevelled OBJ models; deterministic material shading."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json,math,random
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';OUT=R/'art/desert-weapons/revision-2';OUT.mkdir(parents=True,exist_ok=True)
T=A/'textures/item/desert_weapons';M=A/'models/item/desert_mesh';T.mkdir(parents=True,exist_ok=True);M.mkdir(parents=True,exist_ok=True)
COL=[(47,30,25),(94,56,36),(149,93,43),(205,143,53),(247,196,94),(255,235,165),(117,104,75),(185,168,118),(232,220,169),(255,246,207),(22,64,67),(29,111,113),(49,171,164),(137,231,208),(74,55,47),(116,83,61),(176,111,40),(255,152,27),(255,213,68),(255,249,183)]
palette=Image.new('RGB',(len(COL)*4,16));pd=ImageDraw.Draw(palette)
for i,c in enumerate(COL):pd.rectangle((i*4,0,i*4+3,15),fill=c)
palette.save(T/'palette.png')
(M/'desert.mtl').write_text('newmtl desert\nKa 0.1 0.1 0.1\nKd 1 1 1\nKs 0 0 0\nillum 1\nmap_Kd #palette\n')
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def design(kind,legend=False):
 im=Image.new('L',(40,56),255);d=ImageDraw.Draw(im)
 def poly(p,c):d.polygon(p,fill=c)
 def line(p,c,w=1):d.line(p,fill=c,width=w)
 def rect(b,c):d.rectangle(b,fill=c)
 def gem(x,y,size=3):
  poly([(x,y-size),(x+size,y),(x,y+size),(x-size,y)],16);poly([(x,y-size+1),(x+size-1,y),(x,y+size-1),(x-size+1,y)],17);line([(x-1,y),(x,y-1),(x+1,y)],19)
 def shaft(y=12):
  rect((19,y,21,51),1);line([(19,y),(19,51)],2)
  for a in range(y+2,50,5):line([(18,a),(21,a+1)],3);line([(19,a),(21,a)],4)
  poly([(18,51),(22,51),(23,53),(20,55),(17,53)],3);line([(18,52),(21,52)],5)
 def guard(y=34):poly([(12,y-2),(14,y),(25,y),(27,y-2),(27,y+2),(23,y+3),(17,y+3),(12,y+2)],3);line([(13,y),(17,y+1),(24,y+1),(26,y)],5);gem(20,y+1,2)
 if kind in ['sword','glass','dagger','scorpion']:
  shaft(35);guard(35)
  if kind=='scorpion':
   poly([(18,33),(16,25),(16,17),(19,11),(26,5),(23,12),(21,19),(22,27),(22,33)],7);line([(18,30),(18,19),(20,13),(24,8)],9);line([(21,27),(20,20),(22,14)],6)
  else:
   end=16 if kind=='dagger' else 3
   poly([(17,33),(17,end+6),(20,end),(23,end+6),(23,33)],10 if kind=='glass' else 6)
   poly([(18,33),(18,end+7),(20,end+2),(20,33)],12 if kind=='glass' else 8)
   line([(18,31),(18,end+7),(20,end+2)],13 if kind=='glass' else 9)
   line([(21,32),(21,end+6)],11 if kind=='glass' else 7)
 elif kind in ['axe','halberd','scythe','lamya']:
  shaft(7 if kind!='axe' else 21)
  if kind in ['scythe','lamya']:
   poly([(18,9),(12,8),(5,10),(1,16),(4,7),(10,3),(19,3),(28,5),(35,10),(39,18),(31,12),(23,10)],6 if kind=='scythe' else 16)
   line([(3,11),(10,5),(19,5),(28,7),(35,13)],9 if kind=='scythe' else 19,2)
   if kind=='lamya':
    poly([(20,14),(27,10),(32,3),(31,13),(24,20),(21,21)],17);line([(23,17),(29,12),(31,6)],19)
    poly([(18,15),(11,18),(8,25),(9,16),(15,10)],3);gem(20,10,4)
  else:
   y=14 if kind=='halberd' else 23
   poly([(18,y-3),(12,y-5),(8,y-9),(8,y-3),(4,y+2),(4,y+10),(10,y+11),(16,y+7),(18,y+4)],6)
   poly([(7,y+1),(11,y-2),(16,y),(16,y+5),(10,y+8),(6,y+7)],7)
   line([(8,y-6),(9,y-2),(5,y+2),(5,y+9),(10,y+10)],9)
   line([(10,y),(13,y+1),(11,y+4),(14,y+5)],8)
   if kind=='halberd':poly([(19,8),(20,0),(22,8)],8);poly([(22,y),(28,y+2),(23,y+4)],8)
   rect((18,y-3,23,y+6),3);line([(18,y-2),(22,y-2)],5)
 elif kind in ['mace','dune','hammer']:
  shaft(20)
  if kind=='hammer':
   poly([(7,10),(13,7),(31,7),(35,11),(35,23),(30,27),(8,27),(4,23),(4,14)],0)
   rect((6,12,33,22),14);rect((8,10,31,24),15)
   for x in [9,28]:rect((x,9,x+2,24),3);line([(x,10),(x,23)],5)
   rect((14,12,25,22),1);poly([(16,14),(23,14),(23,20),(18,20),(18,17),(21,17)],4);gem(20,16,2)
   line([(6,12),(10,9),(30,9),(33,12)],7);line([(6,14),(6,22)],6)
  else:
   poly([(20,8),(24,12),(29,12),(27,17),(31,21),(26,24),(25,29),(20,27),(15,29),(14,24),(9,21),(13,17),(11,12),(16,12)],6)
   poly([(20,11),(26,17),(26,22),(20,27),(14,22),(14,17)],2)
   for x,y in [(13,13),(26,13),(12,22),(26,22),(19,9),(19,27)]:rect((x,y,x+2,y+2),9)
   gem(20,19,4 if kind=='dune' else 3)
 elif kind in ['sun','colony']:
  shaft(22)
  if kind=='sun':
   for dx,dy in [(0,-9),(0,9),(-9,0),(9,0),(-7,-7),(7,-7),(-7,7),(7,7)]:line([(20+dx*.65,13+dy*.65),(20+dx,13+dy)],4,2)
   poly([(16,7),(24,7),(27,10),(27,16),(24,19),(16,19),(13,16),(13,10)],3);gem(20,13,5)
  else:
   # Legendary ant-queen crown: hooked mandibles and six separate legs around amber.
   poly([(18,7),(20,2),(22,7),(26,9),(27,16),(23,22),(17,22),(13,16),(14,9)],0)
   for sign in [-1,1]:
    for y in [8,14,20]:poly([(20+sign*5,y),(20+sign*10,y-3),(20+sign*13,y+2),(20+sign*8,y),(20+sign*6,y+5)],14)
    line([(20+sign*6,9),(20+sign*10,7),(20+sign*12,9)],3)
   gem(20,14,5);rect((17,24,23,26),3);line([(17,24),(23,24)],5)
   for y in [30,38,46]:gem(20,y,1)
 elif kind=='fists':
  # One hollow cuff and articulated knuckles; the renderer places a second in the other hand.
  poly([(10,37),(11,28),(10,22),(12,14),(17,12),(21,13),(27,12),(31,16),(32,27),(29,32),(29,39)],0)
  rect((12,28,28,39),2);rect((13,31,27,37),14)
  for x in [12,17,22,27]:rect((x,14,x+3,22),7);line([(x,14),(x+2,14)],9);rect((x,23,x+3,26),3)
  poly([(12,20),(8,23),(8,29),(13,32),(16,28),(15,23)],3)
  rect((11,29,29,31),3);rect((11,38,29,40),3);line([(11,29),(29,29)],5);gem(21,34,3)
  line([(17,34),(19,32),(23,32),(25,34),(23,36),(19,36)],5)
 elif kind in ['sling','whip']:
  if kind=='sling':
   line([(17,47),(9,33),(7,17),(14,7),(25,6),(31,17),(28,33),(23,46)],0,3)
   line([(17,47),(10,33),(8,17),(14,8),(25,7),(30,17),(27,33),(23,46)],2)
   poly([(9,25),(14,22),(26,22),(29,26),(25,32),(13,32)],1);poly([(13,25),(18,22),(24,24),(26,27),(18,29)],7);line([(15,24),(20,23),(23,25)],9)
   for x in [17,23]:rect((x-1,44,x+1,48),3)
  else:
   shaft(38);line([(20,38),(11,33),(7,25),(9,14),(17,7),(28,8),(33,16),(31,23),(25,27),(18,24),(19,18),(24,16)],0,3)
   line([(20,38),(11,33),(7,25),(9,14),(17,7),(28,8),(33,16),(31,23),(25,27),(18,24),(19,18),(24,16)],3)
   gem(24,16,2)
 return im
IDS={'desert_sword':'sword','desert_axe':'axe','desert_mace':'mace','desert_halberd':'halberd','desert_scythe':'scythe','scorpion_double_sting':'scorpion','stinger_dagger':'dagger','sand_sling':'sling','desert_glass_blade':'glass','dune_mace':'dune','buried_hammer':'hammer','hala_solar_staff':'sun','colony_scepter':'colony','four_winds_fists':'fists','lamya_weapon':'lamya','quicksand_whip':'whip'}
LEG={'colony_scepter','four_winds_fists','lamya_weapon','buried_hammer'}
def sprite(mask):
 im=Image.new('RGBA',mask.size);pix=im.load()
 for y in range(mask.height):
  for x in range(mask.width):
   k=mask.getpixel((x,y))
   if k!=255:pix[x,y]=(*COL[k],255)
 return im
DISPLAY=json.loads((A/'models/item/ice_sword.json').read_text())['base']['display']
def mesh(id,mask,kind):
 # Grip at the same centre used by the accepted winter OBJ models.
 grip=33 if kind=='fists' else 45
 scale=.34 if kind not in ['halberd','scythe','lamya','sun','colony'] else .39
 verts=[];faces=[];uv=[]
 def quad(points,k):
  n=len(verts)+1;verts.extend(reversed(points));u=len(uv)+1;uv.append(((k+.5)/len(COL),.5));faces.append('f '+' '.join(f'{n+i}/{u}' for i in range(4)))
 pixels=mask.load()
 def thickness(k,y):
  depth=(4.8 if kind=='hammer' else 3.4 if kind in ['mace','dune','fists'] else 1.3) if y<32 else 1.1
  if k>=16:depth+=.6
  if k in [8,9,12,13]:depth*=.7
  return depth
 for y in range(56):
  for x in range(40):
   k=pixels[x,y]
   if k==255:continue
   a=(8+(x-20)*scale)/16;b=(8+(grip-y)*scale)/16;c=a+scale/16;d=b-scale/16
   depth=thickness(k,y)
   front=(8-depth/2)/16;back=(8+depth/2)/16
   quad([(a,d,front),(c,d,front),(c,b,front),(a,b,front)],k)
   quad([(c,d,back),(a,d,back),(a,b,back),(c,b,back)],k)
   for dx,dy,pts in [(-1,0,[(a,d,back),(a,d,front),(a,b,front),(a,b,back)]),(1,0,[(c,d,front),(c,d,back),(c,b,back),(c,b,front)]),(0,-1,[(a,b,front),(c,b,front),(c,b,back),(a,b,back)]),(0,1,[(a,d,back),(c,d,back),(c,d,front),(a,d,front)])]:
    if not(0<=x+dx<40 and 0<=y+dy<56) or pixels[x+dx,y+dy]==255 or thickness(pixels[x+dx,y+dy],y+dy)<depth:quad(pts,[0,0,1,2,3,4,6,6,7,8,10,10,11,12,0,14,2,16,17,18][k])
 text='mtllib desert.mtl\no '+id+'\n'+''.join('v '+' '.join(f'{n:.6f}' for n in p)+'\n' for p in verts)+''.join(f'vt {u:.6f} {v:.6f}\n' for u,v in uv)+'usemtl desert\n'+'\n'.join(faces)+'\n'
 (M/f'{id}.obj').write_text(text)
 display=json.loads(json.dumps(DISPLAY))
 for hand in ['righthand','lefthand']:display['firstperson_'+hand]['scale']=[.58 if kind=='fists' else .62 if kind in ['halberd','scythe','lamya','sun','colony'] else .75]*3
 if kind=='fists':
  for hand in ['righthand','lefthand']:display['thirdperson_'+hand]['scale']=[.7]*3
 base={'loader':'neoforge:obj','model':f'slavicmyths:models/item/desert_mesh/{id}.obj','mtl_override':'slavicmyths:models/item/desert_mesh/desert.mtl','textures':{'palette':'slavicmyths:item/desert_weapons/palette','particle':'slavicmyths:item/desert_weapons/palette'},'automatic_culling':False,'shade_quads':True,'flip_v':False,'display':display}
 save(A/f'models/item/{id}_held.json',base)
 return base
if __name__=='__main__':
 board=Image.new('RGB',(1200,math.ceil(len(IDS)/4)*270),(25,28,32));draw=ImageDraw.Draw(board)
 for i,(id,kind) in enumerate(IDS.items()):
  mask=design(kind,id in LEG);im=sprite(mask);base=mesh(id,mask,kind)
  icon=im.crop(im.getbbox()).rotate(40,resample=Image.Resampling.NEAREST,expand=True) if kind not in ['fists','sling','whip'] else im.crop(im.getbbox())
  icon=icon.crop(icon.getbbox())
  icon.thumbnail((30,30),Image.Resampling.NEAREST)
  gui=Image.new('RGBA',(32,32));gui.alpha_composite(icon,((32-icon.width)//2,(32-icon.height)//2))
  if id in ['four_winds_fists','scorpion_double_sting']:
   single=im.crop(im.getbbox());single.thumbnail((15,28),Image.Resampling.NEAREST);gui=Image.new('RGBA',(32,32));gui.alpha_composite(single,(0,2));gui.alpha_composite(single.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(17,2))
  gui.save(T/f'{id}.png')
  model={'loader':'neoforge:separate_transforms','gui_light':'front','base':base,'perspectives':{'gui':{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/desert_weapons/{id}'}}}}
  save(A/f'models/item/{id}.json',model)
  x=i%4*300;y=i//4*270;draw.rectangle((x+4,y+4,x+295,y+265),outline=(177,125,56) if id in LEG else (65,70,74),width=2)
  board.paste(im.resize((120,168),Image.Resampling.NEAREST),(x+90,y+30),im.resize((120,168),Image.Resampling.NEAREST));board.paste(gui.resize((64,64),Image.Resampling.NEAREST),(x+218,y+176),gui.resize((64,64),Image.Resampling.NEAREST));draw.text((x+12,y+235),id,fill=(255,213,119) if id in LEG else (223,220,203))
 board.save(OUT/'actual-assets.png');print('Generated 16 native OBJ models and separate inventory icons')
