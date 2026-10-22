"""Owner-reported decor corrections, based on pinned actual source assets.

Axis-aligned solid models expose only their union boundary: no overlapping
coplanar quads. Cropped faces keep the original pixel UV mapping.
"""
from pathlib import Path
from copy import deepcopy
from io import BytesIO
from functools import lru_cache
import json, zipfile
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/slavicmyths'
MASTER=Path(__file__).with_name('decor108_templates.zip')
OUT=ROOT/'docs/implementation/decor108'
FACE={'west':(0,False,2,1),'east':(0,True,2,1),'down':(1,False,0,2),'up':(1,True,0,2),'north':(2,False,0,1),'south':(2,True,0,1)}

def original(rel):
 with zipfile.ZipFile(MASTER) as z:return z.read(rel)

def load(rel):return json.loads(original(rel))

def default_uv(e,f):
 x,y,z=e['from'];X,Y,Z=e['to']
 return {'down':[x,16-Z,X,16-z],'up':[x,z,X,Z],'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],'west':[z,16-Y,Z,16-y],'east':[16-Z,16-Y,16-z,16-y]}[f]

def subtract(rect,cut):
 a,b,c,d=rect;u,v,U,V=cut;u=max(a,u);v=max(b,v);U=min(c,U);V=min(d,V)
 if u>=U or v>=V:return [rect]
 return [q for q in [(a,b,u,d),(U,b,c,d),(u,b,U,v),(u,V,U,d)] if q[0]<q[2] and q[1]<q[3]]

def cropped_faces(e,face,info,rect):
 """Split UV tile crossings rather than sampling neighbouring atlas sprites."""
 import math
 axis,positive,u,v=FACE[face];a,b,c,d=rect
 piece=deepcopy(e);piece['from'][u]=a;piece['from'][v]=b;piece['to'][u]=c;piece['to'][v]=d
 old=default_uv(e,face);new=default_uv(piece,face);uv=info.get('uv',old)
 assert info.get('rotation',0)==0
 cropped=[uv[k%2]+(new[k]-old[k%2])/(old[k%2+2]-old[k%2])*(uv[k%2+2]-uv[k%2]) for k in range(4)]
 for component in [0,1]:
  lo,hi=sorted([cropped[component],cropped[component+2]])
  seam=(math.floor(lo/16)+1)*16
  if lo+1e-7<seam<hi-1e-7:
   t=(seam-cropped[component])/(cropped[component+2]-cropped[component])
   reverse=(face in ['north','east']) if component==0 else face!='up'
   low,high=(a,c) if component==0 else (b,d)
   cut=round((high+(low-high)*t) if reverse else (low+(high-low)*t),9)
   if low+1e-7<cut<high-1e-7:
    rects=[(a,b,cut,d),(cut,b,c,d)] if component==0 else [(a,b,c,cut),(a,cut,c,d)]
    return [p for r in rects for p in cropped_faces(e,face,info,r)]
  offset=16*math.floor(((cropped[component]+cropped[component+2])/2)/16)
  cropped[component]-=offset;cropped[component+2]-=offset
 cropped=[max(0,min(16,value)) for value in cropped]
 piece['faces']={face:dict(info,uv=cropped)}
 return [piece]

def boundary(elements):
 result=[]
 for i,e in enumerate(elements):
  if 'rotation' in e:
   result.append(deepcopy(e));continue
  for face,info in e['faces'].items():
   axis,positive,u,v=FACE[face];plane=e['to' if positive else 'from'][axis]
   rect=(e['from'][u],e['from'][v],e['to'][u],e['to'][v]);parts=[rect]
   for j,other in enumerate(elements):
    if i==j:continue
    if 'rotation' in other:continue
    lo=other['from'][axis];hi=other['to'][axis]
    # An outward ray enters another solid, or a later surface owns this plane.
    hidden=lo<=plane<hi if positive else lo<plane<=hi
    same=plane==other['to' if positive else 'from'][axis] and j>i and face in other['faces']
    if hidden or same:
     cut=(other['from'][u],other['from'][v],other['to'][u],other['to'][v])
     parts=[piece for part in parts for piece in subtract(part,cut)]
   for rect in parts:result.extend(cropped_faces(e,face,info,rect))
 return result

def box(a,b,texture):return {'from':list(a),'to':list(b),'faces':{f:{'texture':'#'+texture,'uv':[0,0,16,16]} for f in FACE}}

def texture(kind):
 # Native 32px clusters, no noise, AA or continuous one-pixel line drawings.
 palette={'weave':(159,118,65),'clay':(140,85,62),'wood':(136,99,62),'stone':(120,121,112),'water':(66,100,112)}
 c=palette[kind];im=Image.new('RGBA',(32,32),c+(255,));d=ImageDraw.Draw(im)
 def col(delta):return tuple(max(0,min(255,v+delta)) for v in c)+(255,)
 if kind=='weave':
  for y in range(0,32,4):
   for x in range(-4,32,8):
    x+=(y//4%2)*4;d.rectangle((x,y,x+5,y+2),fill=col(13));d.line((x,y+3,x+5,y+3),fill=col(-22));d.rectangle((x+6,y,x+7,y+3),fill=col(-9))
 elif kind=='clay':
  for x,y,w in [(2,3,6),(18,5,8),(7,13,5),(22,19,7),(3,25,8),(14,28,6)]:d.rectangle((x,y,x+w,y+1),fill=col(5))
  for x,y in [(12,6),(4,18),(26,28)]:d.rectangle((x,y,x+3,y+1),fill=col(-7))
 elif kind=='wood':
  for x in [0,8,16,24]:
   d.rectangle((x,0,x+1,31),fill=col(-26))
   for y,length in [(2+(x//8)*3,6),(20-(x//8)*2,5)]:d.rectangle((x+4,y,x+5,y+length),fill=col(-10))
   d.line((x+2,0,x+2,31),fill=col(9))
 elif kind=='stone':
  for x,y,w,h,v in [(1,2,5,3,8),(15,1,8,2,-7),(25,6,5,4,5),(7,12,6,4,-8),(19,17,8,3,6),(2,24,6,3,-6),(12,27,7,3,7),(26,27,4,3,-8)]:d.rectangle((x,y,x+w,y+h),fill=col(v))
 else:
  for x,y in [(3,7),(19,16),(8,26)]:d.rectangle((x,y,x+7,y+1),fill=col(9))
 return im

@lru_cache(maxsize=1)
def outputs():
 out={};names=['firewood_bundle','signal_bell','small_keg_0','small_keg_1','small_keg_2','small_keg_3','small_keg_4','millstone','wooden_tub','basket','large_basket','kurgan_pottery','hearth','fire_idol_tree']+['fermentation_vat_'+str(i) for i in range(5)]
 for n in names:
  rel='models/block/'+n+'.json'
  try:m=load(rel)
  except KeyError:continue
  if n=='fire_idol_tree':
   m['textures']['timber']='slavicmyths:block/darkened_stripped_log'
   for e in m['elements']:
    for f,info in e['faces'].items():
     if f not in ['north','south']:info['texture']='#timber'
  if n=='millstone':
   m['textures'].update(wood='slavicmyths:block/pine_planks',stone='slavicmyths:block/decor108_stone',sack='slavicmyths:block/household_cloth')
  if n in ['basket','large_basket']:m['textures']['weave']='slavicmyths:block/decor108_weave'
  if n=='kurgan_pottery':m['textures'].update(clay='slavicmyths:block/decor108_clay',particle='slavicmyths:block/decor108_clay')
  if n=='wooden_tub':
   m['textures'].update(main='slavicmyths:block/decor108_staves',metal='slavicmyths:block/household_metal',water='slavicmyths:block/decor108_water')
   # Separate inset water from the rim; thin raised metal hoops on four sides.
   for y in [3,8]:
    for a,b in [((.8,y,.8),(15.2,y+1,1.1)),((.8,y,14.9),(15.2,y+1,15.2)),((.8,y,1.1),(1.1,y+1,14.9)),((14.9,y,1.1),(15.2,y+1,14.9))]:m['elements'].append(box(a,b,'metal'))
  if n.startswith(('fermentation_vat_','small_keg_')):m['textures']['wood']='slavicmyths:block/decor108_staves'
  if n=='hearth':m['textures'].update(wood='slavicmyths:block/decor108_clay',top='slavicmyths:block/decor108_clay',dark='slavicmyths:block/hearth_dark')
  m['elements']=boundary(m['elements']);out[rel]=m
 # The full-tab audit also found exposed coincident faces on these older solids.
 # Keep their shapes/materials; crop duplicate quads only, including state variants.
 extra=['linden_chair','linden_weapon_rack','pine_chair','pine_weapon_rack','kitchen_table','drying_rack','armorer_table','druzhinnik_table','spinning_wheel','loom_table','fruit_press','bath_stove','wooden_crate','produce_crate','household_chest','storage_shelf','straw_nest','feeder','kurgan_rubble','burial_log_coffin']
 def refs(value):
  if isinstance(value,dict):
   if 'model' in value:yield value['model']
   for v in value.values():yield from refs(v)
  elif isinstance(value,list):
   for v in value:yield from refs(v)
 for n in extra:
  paths={'models/item/'+n+'.json','models/block/'+n+'.json'}
  if n=='spinning_wheel':paths.add('models/block/spinning_wheel_inventory.json')
  try:
   for ref in refs(load('blockstates/'+n+'.json')):
    if ref.startswith('slavicmyths:'):paths.add('models/'+ref.split(':',1)[1]+'.json')
  except KeyError:pass
  for rel in sorted(paths):
   try:m=load(rel)
   except KeyError:continue
   if m.get('elements'):
    m['elements']=boundary(m['elements']);out[rel]=m
 # Several historical items duplicate the full model rather than inheriting it.
 for rel,model in list(out.items()):
  if not rel.startswith('models/block/'):continue
  n=Path(rel).stem;itemrel='models/item/'+n+'.json'
  try:item=load(itemrel)
  except KeyError:continue
  if item.get('elements'):
   updated=deepcopy(model);updated['display']=item.get('display',{});out[itemrel]=updated
 # Full-height item and two block halves share exactly the same source design.
 dummy=load('models/block/training_dummy.json')
 pieces=[box((2,0,2),(14,2,14),'beam'),box((6,2,6),(10,23,10),'beam'),box((0,21,6),(16,23,10),'beam'),box((3,15,4),(13,26,12),'straw'),box((5,26,5),(11,32,11),'straw'),box((2.8,17,3.8),(13.2,18,12.2),'rope')]
 full=deepcopy(dummy);full['elements']=boundary(pieces);out['models/block/training_dummy.json']=full
 for label,low,high in [('lower',0,16),('upper',16,32)]:
  model=deepcopy(dummy);els=[]
  for e in pieces:
   if e['to'][1]<=low or e['from'][1]>=high:continue
   e=deepcopy(e);e['from'][1]=max(low,e['from'][1])-low;e['to'][1]=min(high,e['to'][1])-low;els.append(e)
  model['elements']=boundary(els);out['models/block/training_dummy_'+label+'.json']=model
 out['blockstates/training_dummy.json']={'variants':{f'facing={f},half={h}':{'model':f'slavicmyths:block/training_dummy_{h}','y':i*90} for i,f in enumerate(['north','east','south','west']) for h in ['lower','upper']}}
 out['models/item/training_dummy.json']={'parent':'slavicmyths:block/training_dummy','display':{'gui':{'rotation':[30,225,0],'translation':[0,-3,0],'scale':[.38,.38,.38]},'fixed':{'rotation':[0,0,0],'translation':[0,-4,0],'scale':[.4,.4,.4]}}}
 return out

def apply(root=A):
 for rel,data in outputs().items():
  p=root/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
 for n,kind in [('weave','weave'),('clay','clay'),('staves','wood'),('stone','stone'),('water','water')]:
  p=root/'textures/block'/('decor108_'+n+'.png');p.parent.mkdir(parents=True,exist_ok=True);texture(kind).save(p)
 from decor_all_woods import base
 buffer=BytesIO();base('pine','log').save(buffer,format='PNG');p=root/'textures/block/pine_log.png'
 if not p.exists() or p.read_bytes()!=buffer.getvalue():p.write_bytes(buffer.getvalue())
 loot={'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:training_dummy'}],'conditions':[{'condition':'minecraft:survives_explosion'},{'condition':'minecraft:block_state_property','block':'slavicmyths:training_dummy','properties':{'half':'lower'}}]}]}
 p=root.parent.parent/'data/slavicmyths/loot_table/blocks/training_dummy.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(loot,indent=2)+'\n')

if __name__=='__main__':apply()
