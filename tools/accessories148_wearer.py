"""Source-backed native ModelPart and actual item JSON/UV wearer A/B. No game rendering."""
from pathlib import Path
import copy,functools,io,json,math,re,hashlib,sys
import numpy as np
from PIL import Image,ImageDraw
import armor88_preview as paint
from accessories148_art import NEW,OLD
RESUME='--resume' in sys.argv
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths';O=ROOT/'docs/implementation/accessories-148';B=O/'before';OUT=O/'wearer';OUT.mkdir(exist_ok=True)
@functools.lru_cache(maxsize=128)
def tex(name):
 if name.startswith('minecraft:'):return np.array(Image.open(io.BytesIO(paint.Z.read('assets/minecraft/'+name.split(':')[1]))).convert('RGBA'))
 return np.array(Image.open(A/name).convert('RGBA'))
paint.texture=tex

def load_model(name,before=False):
 p=A/'models'/(name.removeprefix('slavicmyths:')+'.json');old=B/p.relative_to(ROOT);d=json.loads((old if before and old.exists() else p).read_text())
 if d.get('loader')=='neoforge:separate_transforms':d=d['base']
 parent=d.get('parent','')
 if parent.startswith('slavicmyths:'):
  base=load_model(parent,before);base['textures']=base.get('textures',{})|d.get('textures',{});base['display']=base.get('display',{})|d.get('display',{});base.update({k:v for k,v in d.items() if k not in ['textures','display']});return base
 return d

def rot(angles):
 x,y,z=angles;cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
 return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
# Exact arguments in the six production FolkEquipmentLayer.show callsites.
def show_arguments(source):
 result={}
 for match in re.finditer(r'show\((.+?),p,pose,buffer,light,([\d.\-]+),([\d.\-]+),([\d.\-]+),([\d.\-]+)F,(true|false)\);',source):
  key,*args=match.groups();quoted=re.findall('"([a-z_]+)"',key);id=quoted[0] if quoted else re.search(r'ModItems.([A-Z_]+)',key)[1].lower();result[id]=(*map(float,args[:4]),args[4]=='true')
 assert len(result)==6
 return result
ITEMS=show_arguments((ROOT/'src/main/java/org/slavicmyths/client/FolkEquipmentLayer.java').read_text())
ITEMS_BEFORE=show_arguments((B/'src/main/java/org/slavicmyths/client/FolkEquipmentLayer.java').read_text())

def item_layers(id,sample,before):
 d=load_model('slavicmyths:item/'+id,before);textures=d['textures'];x,y,z,scale,head=(ITEMS_BEFORE if before else ITEMS)[id];anchor=sample['anchors']['head' if head else 'body'];ar=rot(anchor[3:]);rz=rot([0,0,math.pi]);layers={}
 if 'elements' not in d:
  assert d.get('parent')=='minecraft:item/generated',(id,d)
  ref=textures['layer0'];namespace,path=ref.split(':');pixels=tex(('minecraft:' if namespace=='minecraft' else '')+'textures/'+path+'.png');height,width=pixels.shape[:2];elements=[]
  elements.append({'from':[0,0,7.5],'to':[16,16,8.5],'faces':{'north':{'texture':'#layer0','uv':[16,0,0,16]},'south':{'texture':'#layer0','uv':[0,0,16,16]}}})
  for py in range(height):
   for px in range(width):
    if pixels[py,px,3]<=127:continue
    exposed=[]
    for side,dx,dy in [('west',-1,0),('east',1,0),('up',0,-1),('down',0,1)]:
     nx,ny=px+dx,py+dy
     if nx<0 or ny<0 or nx>=width or ny>=height or pixels[ny,nx,3]<=127:exposed.append(side)
    if exposed:
     uv=[px*16/width,py*16/height,(px+1)*16/width,(py+1)*16/height]
     elements.append({'from':[px*16/width,(height-py-1)*16/height,7.5],'to':[(px+1)*16/width,(height-py)*16/height,8.5],'faces':{side:{'texture':'#layer0','uv':uv} for side in exposed}})
  d['elements']=elements
 for e in d['elements']:
  x0,y0,z0=e['from'];x1,y1,z1=e['to'];points={'west':[[x0,y1,z0],[x0,y0,z0],[x0,y0,z1],[x0,y1,z1]],'east':[[x1,y1,z1],[x1,y0,z1],[x1,y0,z0],[x1,y1,z0]],'north':[[x1,y1,z0],[x1,y0,z0],[x0,y0,z0],[x0,y1,z0]],'south':[[x0,y1,z1],[x0,y0,z1],[x1,y0,z1],[x1,y1,z1]],'up':[[x0,y1,z0],[x0,y1,z1],[x1,y1,z1],[x1,y1,z0]],'down':[[x0,y0,z1],[x0,y0,z0],[x1,y0,z0],[x1,y0,z1]]}
  default={'west':[z0,16-y1,z1,16-y0],'east':[16-z1,16-y1,16-z0,16-y0],'down':[x0,16-z1,x1,16-z0],'up':[x0,z0,x1,z1],'north':[16-x1,16-y1,16-x0,16-y0],'south':[x0,16-y1,x1,16-y0]}
  for side,face in e['faces'].items():
   ref=face['texture']
   while ref.startswith('#'):ref=textures[ref[1:]]
   namespace,path=ref.split(':');texture=('minecraft:' if namespace=='minecraft' else '')+'textures/'+path+'.png'
   vertices=np.array(points[side],dtype=float)
   if 'rotation' in e:
    r=e['rotation'];axis='xyz'.index(r['axis']);angles=[0.,0.,0.];angles[axis]=math.radians(r['angle']);origin=np.array(r['origin']);vertices=(vertices-origin)@rot(angles).T
    if r.get('rescale'):vertices*=np.array([1 if k==axis else 1/math.cos(angles[axis]) for k in range(3)])
    vertices+=origin
   vertices=(vertices-8)*scale
   if not before and id=='hunter_belt':vertices*=np.array([1.05,1,1.6])
   vertices=vertices@rz.T+[x*16,y*16,z*16]
   if not before and sample['armor']:vertices*=np.array([1.35,1,1.4] if head else [1.15,1,1.25])
   vertices=vertices@ar.T+np.array(anchor[:3])
   a,b,c,f=face.get('uv',default[side]);uv=np.array([[a,b],[a,f],[c,f],[c,b]])/16;uv=np.roll(uv,-face.get('rotation',0)//90,axis=0)
   layers.setdefault(texture,[]).append(np.concatenate([vertices,uv],axis=1).tolist())
 return [{'id':id,'texture':name,'faces':faces} for name,faces in layers.items()]

native=json.loads((O/'native-worn.json').read_text());samples=native['samples']
for s in samples:
 for id in ITEMS:s['artifacts'][id]={'layers':item_layers(id,s,not s['after'])}
 # New accessories have no old representation.
 if not s['after']:
  for id in ['podvey_belt','leshy_necklace']:s['artifacts'].pop(id)

def scene(sample,ids):
 layers=list(sample['base_layers'])
 for id in ids:
  if id in sample['artifacts']:
   d=sample['artifacts'][id];layers+=d.get('layers',[d])
 return {'layers':layers}

BASE_CACHE={}
def painted(sample,ids,yaw):
 key=(sample['body'],sample['pose'],sample['armor'],yaw)
 if key not in BASE_CACHE:BASE_CACHE[key]=paint.render({'layers':sample['base_layers']},yaw,return_depth=True)
 layers=scene(sample,ids)['layers'][len(sample['base_layers']):]
 return paint.render({'layers':layers},yaw,base=BASE_CACHE[key])

def sheet(name,ids):
 image=Image.new('RGB',(1600,16*286+55),(22,27,34));draw=ImageDraw.Draw(image);draw.text((8,6),name+' / ACTUAL SOURCE MESH-UV-PNG / BEFORE | AFTER',fill='white');draw.text((8,24),'Same camera/scale; fixed source poses, not game screenshots. Real animation/visibility/armor approval OWNER_PENDING.',fill='white');row=0
 for body in ['Steve','Alex']:
  for armor in [False,True]:
   for pose in ['idle','walk_t10','held_item','sneak']:
    y=55+row*286;row+=1;draw.text((5,y),body+' / '+pose+' / '+('IRON CHEST+HELMET' if armor else 'NO ARMOR'),fill='white')
    for col,(view,yaw) in enumerate([('FRONT',0),('BACK',180),('PROFILE',90),('3/4',-35)]):
     for side,after in enumerate([False,True]):
      s=next(s for s in samples if s['body']==body and s['pose']==pose and s['armor']==armor and s['after']==after)
      image.paste(painted(s,ids,yaw),(col*400+side*200,y+15));draw.text((col*400+side*200+4,y+17),view+(' AFTER' if after else ' BEFORE'),fill='white')
 image.save(OUT/(name+'.png'));print('WORN_SOURCE '+name,flush=True)
# Every actually visible ID: 13 old and 2 new. Icon-only rings/charms remain NONE.
ids=list(samples[-1]['artifacts'])
for id in ids:
 if not RESUME or not (OUT/(id+'.png')).exists():sheet(id,[id])
for name,ids in {
 'COMBINATION-BOSS':['poyas_tugarina','odnoglazyy_obereg','miners_goggles','perun_ring','resin_ring','grave_ward'],
 'COMBINATION-NEW':['podvey_belt','leshy_necklace','invisibility_cap','pool_ring','spark_ring','bath_ward'],
 'COMBINATION-REWARDS':['gift_alyosha_belt','trophy_skim_necklace','invisibility_cap','gift_dobrynya_ring','prospectors_ring','retribution_charm'],
 'COMBINATION-QUIVER':['finist_quiver','gift_finist_amulet','miners_goggles','perun_ring','resin_ring','grave_ward'],
}.items():
 if not RESUME or not (OUT/(name+'.png')).exists():sheet(name,ids)
# Matrix covers every old and new wearable, including intentionally icon-only rings.
original=json.loads((O/'audit/inventory.json').read_text());matrix=[]
for item in original+[{'id':id,'curios_slot':v[0],'worn_renderer':'NewAccessoryRenderer' if id in ['podvey_belt','leshy_necklace'] else 'NONE: icon-only'} for id,v in NEW.items()]:
 if not item['curios_slot']:continue
 id=item['id'];slot=item['curios_slot'];visible=id in samples[-1]['artifacts'];kind='head' if slot=='head' else 'body';matrix.append({'id':id,'slot':slot,'slot_indices':'0,1' if slot=='ring' else '0','renderer':item['worn_renderer'],'anchor':kind if visible else 'NONE','visible_expected':visible,'source_proof':str((OUT/(id+'.png')).relative_to(ROOT)) if visible else 'No renderer registered, no invented ring geometry','runtime':'NOT_RUN / OWNER_PENDING','invisibility':'hidden: FolkEquipmentLayer or Curios renderer guard' if visible else 'NONE'})
(O/'wearer-matrix.json').write_text(json.dumps(matrix,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
(O/'wearer-source-sha256.json').write_text(json.dumps({p.relative_to(ROOT).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for folder in [ROOT/'src/main/java/org/slavicmyths/client',ROOT/'src/main/resources/assets/slavicmyths/models/item'] for p in folder.glob('*') if p.is_file()},indent=2)+'\n')

(O/'wearer-texture-sha256.json').write_text(json.dumps({p.relative_to(ROOT).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (A/'textures').rglob('*.png')},indent=2)+'\n')

# Visualize only source visibility branches, separately from a real invisibility runtime test.
image=Image.new('RGB',(800,60+8*285),(22,27,34));draw=ImageDraw.Draw(image);draw.text((8,8),'SOURCE VISIBILITY BRANCHES / VISIBLE before-after | INVISIBLE before-after',fill='white');draw.text((8,26),'Potion invisibility, no cap: installed CuriosLayer has no isInvisible gate; not a game screenshot.',fill='white')
for row,(body,pose) in enumerate((b,p) for b in ['Steve','Alex'] for p in ['idle','walk_t10','held_item','sneak']):
 y=60+row*285;draw.text((5,y),body+' / '+pose,fill='white')
 for col,(after,invisible) in enumerate([(False,False),(True,False),(False,True),(True,True)]):
  sample=next(s for s in samples if s['body']==body and s['pose']==pose and s['armor'] and s['after']==after)
  selected=scene(sample,['poyas_tugarina','odnoglazyy_obereg','miners_goggles'])
  if invisible:selected={'layers':[] if after else [sample['artifacts'][id] for id in ['poyas_tugarina','odnoglazyy_obereg']]}
  image.paste(paint.render(selected,0),(col*200,y+15))
image.save(O/'VISIBILITY-SOURCE.png')
