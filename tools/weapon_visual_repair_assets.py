"""Restore the accepted ornate scythe and correct 32px crossbow UVs to model units."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';F=R/'tools/weapon-art-original'
UV={'metal':[0,0,7.5,7.5],'grip':[12,0,15.5,3.5],'seam':[8,0,11.5,3.5],
 'ivory':[8,4,11.5,7.5],'fire':[12,4,15.5,7.5],'mor':[8,8,11.5,11.5],'storm':[12,8,15.5,11.5]}
def write(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def box(a,b,material='metal'):
 return {'from':a,'to':b,'faces':{side:{'texture':'#'+material,'uv':UV[material].copy()} for side in ['north','south','east','west','up','down']}}
def dragon_heads():
 parts=[]
 for center,eye in [(3.25,'fire'),(8,'mor'),(12.75,'storm')]:
  def add(a,b,mat='metal'):parts.append(box([center+a[0],a[1],a[2]],[center+b[0],b[1],b[2]],mat))
  add([-1.5,8.2,3],[1.5,11.4,7]) # neck
  add([-1.8,10.2,0],[1.8,12,5]) # brow/skull
  add([-2.1,8.2,-3],[-1.15,10.4,3]);add([1.15,8.2,-3],[2.1,10.4,3]) # cheeks, mouth void
  add([-1.8,10,-3],[1.8,10.8,2]);add([-1.8,7.4,-3],[1.8,8.2,3]) # upper/lower jaws
  add([-1.35,7.1,-2.7],[1.35,7.5,2],'ivory')
  for side in [-1,1]:
   x=side*1.45
   add([x-.45,12,2.8],[x+.45,14,4.8],'ivory');add([x-.25,14,3.9],[x+.25,15,4.9],'ivory')
   x=side*2.05
   add([x-.15,10.6,.1],[x+.15,11.35,1.4],eye)
   add([x-.25,11.3,-.1],[x+.25,11.8,1.7])
   x=side*.82
   add([x-.23,9.3,-2.8],[x+.23,10.15,-1.8],'ivory')
   add([x-.2,8.1,-2.65],[x+.2,8.9,-1.65],'ivory')
 return parts
def legacy_apply():
 # Entity atlases are not automatically stitched into the block/item atlas.
 p=R/'src/main/resources/assets/minecraft/atlases/blocks.json';p.parent.mkdir(parents=True,exist_ok=True)
 d=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'sources':[]}
 for name in ['karachun','morozko','morozko_crystal','great_poloz_head']:
  source={'type':'minecraft:single','resource':'slavicmyths:entity/'+name}
  if source not in d['sources']:d['sources'].append(source)
 write(p,d)
 for name in ['karachun_scythe_held.json','karachun_scythe_icon.json']:
  d=json.loads((F/name).read_text(encoding='utf-8'))
  if name.endswith('_held.json'):
   d['textures']['particle']=d['textures']['surface']
   for side in ['righthand','lefthand']:d['display']['thirdperson_'+side]={'rotation':[90,0,0],'scale':[.75]*3,'translation':[0,0,0]}
  write(A/'models/item'/name,d)
 icon=A/'textures/item/karachun_scythe.png';data=(F/'karachun_scythe.png').read_bytes()
 if not icon.exists() or icon.read_bytes()!=data:icon.write_bytes(data)
 for name in ['serpent_crossbow','three_breath_crossbow']:
  for p in (A/'models/item').glob(name+'*.json'):
   d=json.loads(p.read_text(encoding='utf-8'))
   for e in d.get('elements',[]):
    for f in e['faces'].values():f['uv']=UV[f['texture'].lstrip('#')].copy()
   if name=='three_breath_crossbow':
    original=json.loads((F/p.name).read_text(encoding='utf-8'))
    d['elements']=original['elements'][:8]+dragon_heads()
    if p.stem.endswith(('_arrow','_firework')):
     for center in [3.25,8,12.75]:d['elements'].append(box([center-.2,9,-5],[center+.2,9.5,8],'ivory'))
    d['display']['gui']={'rotation':[25,40,0],'translation':[0,-1,0],'scale':[.65]*3}
   write(p,d)
 # The #87 canonical component/flail owner runs last; legacy models must not resurrect.
 from weapons87_flails import main as flails87
 flails87(True)
def apply():
 # Historical dragon-head reconstruction predates #184/#222 and must not run
 # ahead of the guarded canonical models. Keep helpers for source provenance.
 from weapons87_flails import main as current_weapons
 current_weapons(True)

if __name__=='__main__':apply()
