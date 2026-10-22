"""Render actual production source before/after and inventory the full decor tab."""
from pathlib import Path
from copy import deepcopy
import json
from PIL import Image,ImageDraw
from decor106_preview import Sources,prepare
from trophy111_source_preview import render
from decor108_assets import A,OUT,FACE,outputs,subtract,original

class ReviewSources(Sources):
 def model(self,ref,stack=()):
  # Override the local source root without importing any legacy generator.
  if self.before and ref.startswith('slavicmyths:'):
   try:data=json.loads(original('models/'+ref.split(':',1)[1]+'.json'))
   except KeyError:data=None
   if data is not None:
    parent=self.model(data['parent'],stack+(ref,)) if data.get('parent') else {};result=deepcopy(parent);result.update(data)
    for k in ['textures','display']:result[k]=parent.get(k,{})|data.get(k,{})
    return result
  return super().model(ref,stack)
 def texture(self,ref):
  if ':' not in ref:ref='minecraft:'+ref
  ns,path=ref.split(':',1)
  if self.before and ns=='slavicmyths':
   p=OUT/'before/textures'/(path+'.png')
   if p.exists():return Image.open(p).convert('RGBA')
  previous=self.before;self.before=False
  try:return super().texture(ref)
  finally:self.before=previous

def overlaps(model):
 es=model.get('elements',[]);result=[]
 for i,e in enumerate(es):
  if 'rotation' in e:continue
  for j,o in enumerate(es[:i]):
   if 'rotation' in o:continue
   for f,(axis,pos,u,v) in FACE.items():
    if f not in e['faces'] or f not in o['faces']:continue
    plane=e['to' if pos else 'from'][axis]
    if abs(plane-o['to' if pos else 'from'][axis])>1e-7:continue
    rect=(max(e['from'][u],o['from'][u]),max(e['from'][v],o['from'][v]),min(e['to'][u],o['to'][u]),min(e['to'][v],o['to'][v]))
    if rect[0]>=rect[2] or rect[1]>=rect[3]:continue
    parts=[rect]
    for k,c in enumerate(es):
     if k in [i,j] or 'rotation' in c:continue
     lo,hi=c['from'][axis],c['to'][axis]
     hidden=lo<=plane<hi if pos else lo<plane<=hi
     if hidden:parts=[p for q in parts for p in subtract(q,(c['from'][u],c['from'][v],c['to'][u],c['to'][v]))]
    if parts:result.append([i,j,f,plane])
 return result

def main():
 names=['training_dummy','firewood_bundle','signal_bell','wooden_crate','small_keg','millstone','wooden_tub','basket','large_basket','fermentation_vat','kurgan_pottery','hearth','fire_idol_tree','pine_log']
 for offset in range(0,len(names),5):
  batch=names[offset:offset+5];board=Image.new('RGB',(800,42+138*len(batch)),'#30363c');d=ImageDraw.Draw(board);d.text((8,8),'OFFLINE actual source models + UV + PNG. BEFORE left / AFTER right. Same scale.',fill='white')
  for row,n in enumerate(batch):
   y=42+row*138;d.text((8,y),n,fill='white')
   for i,before in enumerate([True,False]):
    m,t=prepare(ReviewSources(before),'slavicmyths:item/'+n);pic,_=render(m,t,'gui',64);tiny,_=render(m,t,'gui',16);pic=pic.resize((112,112),Image.Resampling.NEAREST);board.paste(pic,(i*400+12,y+18),pic);board.paste(tiny,(i*400+150,y+24),tiny);big=tiny.resize((48,48),Image.Resampling.NEAREST);board.paste(big,(i*400+200,y+24),big)
    m.setdefault('display',{})['gui']={'rotation':[30,225,0],'translation':[0,-4,0],'scale':[.42,.42,.42]}
    world,_=render(m,t,'gui',64);world=world.resize((112,112),Image.Resampling.NEAREST);board.paste(world,(i*400+280,y+18),world)
  board.save(OUT/f'comparison-{offset//5+1}.png')
 # Audit every non-timber decor record; keep good models, list residual risks.
 catalog=[line.split('\t') for line in (A/'creative_catalog.tsv').read_text().splitlines() if len(line.split('\t'))==3];source=ReviewSources(False);audit=[];changed=set(outputs())
 for n,tab,family in catalog:
  if tab!='decor' or family.startswith('wood_'):continue
  try:
   model=source.model('slavicmyths:item/'+n);conflicts=overlaps(model)
   for tex in model.get('textures',{}).values():
    if not tex.startswith('#'):source.texture(tex)
   decision='FIX' if n in names or any(Path(p).stem==n or Path(p).stem.startswith(n+'_') for p in changed) else 'KEEP'
   if conflicts:decision='NEEDS_GEOMETRY_REVIEW'
   if n.endswith('_wall') and family.startswith('stone_'):decision='KEEP_VANILLA_WALL_JOIN'
   if n=='mor_grass_block':decision='KEEP_INTENTIONAL_ALPHA_OVERLAY'
   audit.append({'id':n,'family':family,'decision':decision,'coplanar_pairs':len(conflicts),'examples':conflicts[:6],'runtime':'NOT_RUN'})
  except (KeyError,FileNotFoundError,AssertionError) as e:audit.append({'id':n,'family':family,'decision':'NEEDS_SPECIAL_RENDERER','detail':str(e),'runtime':'NOT_RUN'})
 (OUT/'catalog-audit.json').write_text(json.dumps(audit,indent=2)+'\n')
 for offset in range(0,len(audit),40):
  batch=audit[offset:offset+40];sheet=Image.new('RGB',(1000,40+110*((len(batch)+4)//5)),'#30363c');draw=ImageDraw.Draw(sheet);draw.text((8,8),'OFFLINE full non-timber decor catalog. Actual source item models. No game screenshot.',fill='white')
  for i,entry in enumerate(batch):
   x=(i%5)*200;y=40+(i//5)*110;draw.text((x+4,y),entry['id'],fill='white')
   try:
    m,t=prepare(source,'slavicmyths:item/'+entry['id']);pic,_=render(m,t,'gui',48);pic=pic.resize((72,72),Image.Resampling.NEAREST);sheet.paste(pic,(x+50,y+20),pic)
   except (KeyError,ValueError,AssertionError) as e:draw.text((x+4,y+40),'NEEDS SPECIAL VIEW',fill='orange');entry['preview_error']=str(e)
  sheet.save(OUT/f'catalog-{offset//40+1}.png')
 print(json.dumps({'catalog_records':len(audit),'residual_coplanar':[a['id'] for a in audit if a['decision']=='NEEDS_GEOMETRY_REVIEW']},indent=2))

if __name__=='__main__':main()
