"""Offline actual-model/UV previews of the #106 linden pilot; no Minecraft launch.

Resolves unchanged item/block JSON and installed vanilla parents. A temporary
in-memory atlas contains only exact source PNG pixels with remapped face UV;
it is never a production asset. Uses the existing small orthographic source
renderer (no Minecraft AO, player, world or runtime claim).
"""
from pathlib import Path
from copy import deepcopy
from io import BytesIO
import json,zipfile
from PIL import Image,ImageDraw
from trophy111_source_preview import render,CLIENT
from decor106_linden import ROOT,FIX,KEEP

A=ROOT/'src/main/resources/assets/slavicmyths';OUT=ROOT/'docs/implementation/decor106-linden'

class Sources:
 def __init__(self,before=False):self.before=before;self.zip=zipfile.ZipFile(CLIENT);self.used=set()
 def model(self,ref,stack=()):
  if ref.startswith('builtin/'):return {'parent':ref}
  if ':' not in ref:ref='minecraft:'+ref
  assert ref not in stack,ref
  ns,path=ref.split(':',1);key=f'assets/{ns}/models/{path}.json';self.used.add(key)
  data=json.loads((A/'models'/f'{path}.json').read_text()) if ns=='slavicmyths' else json.loads(self.zip.read(key))
  parent=self.model(data['parent'],stack+(ref,)) if data.get('parent') else {}
  result=deepcopy(parent);result.update(data)
  for k in ['textures','display']:result[k]=parent.get(k,{})|data.get(k,{})
  if parent.get('parent')=='builtin/generated' or ref=='minecraft:item/generated':result['generated']=True
  return result
 def texture(self,ref):
  ns,path=ref.split(':',1);key=f'assets/{ns}/textures/{path}.png';self.used.add(key)
  override=OUT/'before'/f'{Path(path).name}.png'
  if self.before and ns=='slavicmyths' and Path(path).name in FIX:return Image.open(override).convert('RGBA')
  return Image.open(A/'textures'/f'{path}.png').convert('RGBA') if ns=='slavicmyths' else Image.open(BytesIO(self.zip.read(key))).convert('RGBA')

def material(textures,key):
 seen=set()
 while key.startswith('#'):
  assert key not in seen,key
  seen.add(key);key=textures[key[1:]]
 return key

def prepare(sources,ref):
 m=sources.model(ref)
 if m.get('generated'):
  return {'parent':'minecraft:item/generated','display':m.get('display',{})},sources.texture(material(m['textures'],m['textures']['layer0']))
 assert m.get('elements'),ref
 refs=sorted({material(m['textures'],f['texture']) for e in m['elements'] for f in e.get('faces',{}).values()})
 images={ref:sources.texture(ref) for ref in refs};cell=max(max(i.size) for i in images.values());atlas=Image.new('RGBA',(cell*len(refs),cell))
 for i,ref in enumerate(refs):atlas.paste(images[ref],(i*cell,0))
 m=deepcopy(m)
 for e in m['elements']:
  x0,y0,z0=e['from'];x1,y1,z1=e['to']
  default={'west':[z0,16-y1,z1,16-y0],'east':[16-z1,16-y1,16-z0,16-y0],'down':[x0,16-z1,x1,16-z0],'up':[x0,z0,x1,z1],'north':[16-x1,16-y1,16-x0,16-y0],'south':[x0,16-y1,x1,16-y0]}
  for side,f in e['faces'].items():
   assert not f.get('rotation',0),(ref,side,'UV rotation needs explicit renderer support')
   ref=material(m['textures'],f['texture']);offset=refs.index(ref)*cell;w,h=images[ref].size;u,v,s,t=f.get('uv',default[side])
   f['uv']=[(offset+u*w/16)*16/atlas.width,v*h/atlas.height,(offset+s*w/16)*16/atlas.width,t*h/atlas.height]
 m['parent']='minecraft:block/block';return m,atlas

def main():
 names=['linden_planks','linden_log','linden_wood','linden_stripped_log','linden_stripped_wood','linden_stairs','linden_slab','linden_fence','linden_fence_gate','linden_door','linden_trapdoor','linden_pressure_plate','linden_button','linden_sign']
 sources=[Sources(True),Sources(False)];board=Image.new('RGB',(760,34+132*len(names)),'#30363c');d=ImageDraw.Draw(board)
 d.text((10,8),'OFFLINE unchanged source models/UV, actual before / after PNG; item slot 1x and 3x.',fill='white');manifest={}
 for row,name in enumerate(names):
  y=34+row*132;d.text((10,y),name,fill='white')
  manifest[name]=[]
  for j,source in enumerate(sources):
   model,atlas=prepare(source,'slavicmyths:item/'+name);im,pose=render(model,atlas,'gui',64);tiny,_=render(model,atlas,'gui',16);x=16+j*370
   board.paste(im.resize((96,96),Image.Resampling.NEAREST),(x,y+20),im.resize((96,96),Image.Resampling.NEAREST));board.paste(tiny,(x+124,y+24),tiny);large=tiny.resize((48,48),Image.Resampling.NEAREST);board.paste(large,(x+172,y+24),large)
   tiny.save(OUT/(f'{name}-item-'+('before.png' if j==0 else 'after.png')))
   manifest[name].append({'pose':pose,'elements':len(model.get('elements',[])),'source_model':'assets/slavicmyths/models/item/'+name+'.json'})
 board.save(OUT/'dependent-models-before-after.png')
 # Exact 2x2 texture tiles, including both unchanged end grains. No world screenshot claim.
 tileboard=Image.new('RGB',(900,34+200*5),'#30363c');d=ImageDraw.Draw(tileboard);d.text((8,8),'OFFLINE exact surface textures: native / 3x / 2x2 tiling. Before / after.',fill='white')
 for i,name in enumerate(FIX+KEEP):
  y=34+i*200;d.text((8,y),name,fill='white')
  for j,source in enumerate(sources):
   pic=source.texture('slavicmyths:block/'+name);x=12+j*440;tileboard.paste(pic,(x,y+24));tileboard.paste(pic.resize((48,48),Image.Resampling.NEAREST),(x+36,y+24));tile=Image.new('RGBA',(32,32))
   for tx in [0,16]:
    for ty in [0,16]:tile.paste(pic,(tx,ty))
   tileboard.paste(tile.resize((128,128),Image.Resampling.NEAREST),(x+110,y+24))
 tileboard.save(OUT/'surfaces-and-2x2-before-after.png')
 # Four camera yaw angles of actual log/end grain using the same source models.
 turns=Image.new('RGB',(760,300),'#30363c');d=ImageDraw.Draw(turns);d.text((8,8),'OFFLINE log source at four camera yaw angles; before top / after bottom.',fill='white')
 for j,source in enumerate(sources):
  for i,angle in enumerate([0,90,180,270]):
   model,atlas=prepare(source,'slavicmyths:item/linden_log');pose=model.setdefault('display',{}).setdefault('gui',{});r=pose.get('rotation',[30,225,0]);pose['rotation']=[r[0],r[1]+angle,r[2]];im,_=render(model,atlas,'gui',64);im=im.resize((112,112),Image.Resampling.NEAREST);turns.paste(im,(i*190+20,j*130+30),im)
 turns.save(OUT/'log-four-views.png')
 (OUT/'rendered-models.json').write_text(json.dumps({'items':manifest,'dependencies':sorted(sources[1].used),'renderer':'orthographic source-only; no Minecraft AO/runtime'},indent=2)+'\n')

if __name__=='__main__':main()
