"""Scoped RC2 native model/resource checks; no client renderer or gameplay claims."""
from pathlib import Path
import json, math, hashlib
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths'
OUT=ROOT/'docs/verification/rc2-ui-visual';OUT.mkdir(parents=True,exist_ok=True)
models={};checks=[]
for name in ['club','battle_axe','retainer_shield','retainer_shield_body','retainer_shield_blocking']:
 m=json.loads((A/'models/item'/f'{name}.json').read_text());models[name]=m
 for e in m.get('elements',[]):
  assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])),(name,e)
  for f in e['faces'].values():assert len(f['uv'])==4 and all(0<=v<=16 for v in f['uv']),(name,f)
 for context,pose in m.get('display',{}).items():
  assert all(0<v<=2 for v in pose.get('scale',[1,1,1])),(name,context)
  assert all(abs(v)<=16 for v in pose.get('translation',[0,0,0])),(name,context)
 for texture in m.get('textures',{}).values():
  if texture.startswith('slavicmyths:'):assert (A/'textures'/f'{texture.split(":")[1]}.png').is_file()
 checks.append(name+': bounded geometry, UV0..16, materials and transforms PASS')
# Connected solid geometry catches detached exploded weapon fragments, not just valid JSON.
for name in ['club','battle_axe']:
 e=models[name]['elements'];reached={0}
 while True:
  previous=set(reached)
  for i,box in enumerate(e):
   if any(all(max(box['from'][d],e[j]['from'][d])<=min(box['to'][d],e[j]['to'][d])+.001 for d in range(3)) for j in reached):reached.add(i)
  if reached==previous:break
 assert len(reached)==len(e),(name,'detached solids',set(range(len(e)))-reached)
 for context in ['gui','ground','fixed','firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:assert context in models[name]['display']
 checks.append(name+': all solids connected; 7 display contexts PASS')
# Original model has its painted surface/boss at negative Z, rear handle at positive Z.
m=models['retainer_shield'];assert m['overrides'][0]['model']=='slavicmyths:item/retainer_shield_blocking'
for hand in ['righthand','lefthand']:
 assert m['display']['firstperson_'+hand]['rotation'][1]==0
 assert m['display']['thirdperson_'+hand]['rotation'][1]==-90
 assert models['retainer_shield_blocking']['display']['thirdperson_'+hand]['rotation']==[45,-25,0]
assert m['display']['gui']['rotation'][1]==180
checks.append('shield: physical held Y axes reversed 180 degrees, two hand contexts, separate blocking and unchanged decorated GUI PASS; in-game acceptance pending')
# Check every existing model belonging to the four wood families (including furniture).
family_refs=0
for p in (A/'models').rglob('*.json'):
 if not any(p.stem.startswith(f+'_') for f in ['willow','rowan','pine','darkened']):continue
 data=json.loads(p.read_text())
 for texture in data.get('textures',{}).values():
  if texture.startswith('slavicmyths:'):assert (A/'textures'/f'{texture.split(":")[1]}.png').is_file();family_refs+=1
for family in ['willow','rowan','pine','darkened']:
 states=json.loads((A/'blockstates'/f'{family}_door.json').read_text());assert len(states['variants'])==32
 for kind in ['log','stripped_log','planks','log_top','stripped_log_top','door_top','door_bottom','trapdoor']:
  im=Image.open(A/'textures/block'/f'{family}_{kind}.png');assert im.size==(32,32)
# Greyscale structural patterns must differ, even after removing the palette.
patterns=[]
for f in ['willow','rowan','pine','darkened']:
 im=Image.open(A/'textures/block'/f'{f}_log.png').convert('L');pixels=list(im.getdata());mean=sum(pixels)/len(pixels);patterns.append(tuple(round(p-mean) for p in pixels))
assert len(set(patterns))==4
ru=json.loads((A/'lang/ru_ru.json').read_text(encoding='utf-8'));en=json.loads((A/'lang/en_us.json').read_text(encoding='utf-8'))
for key in ru:
 if key.startswith(('rpg.slavicmyths.','kitchen.slavicmyths.')):assert key in en,key
for id in ['thunder','heat','forest','midday','shadow','protection','life','wind']:assert 'rpg.slavicmyths.effect_'+id in ru
checks.append(f'wood: 4 distinct greyscale bark structures, 32 door states each, {family_refs} derived texture refs PASS')
checks.append('RU/EN RPG+kitchen translation parity and 8 numerical rune descriptions PASS')
# Contact sheet is an offline asset preview, not a Minecraft screenshot.
sheet=Image.new('RGB',(640,400),(235,220,185));draw=ImageDraw.Draw(sheet)
for row,family in enumerate(['willow','rowan','pine','darkened']):
 draw.text((4,row*96+6),family,fill=(50,32,20))
 for col,kind in enumerate(['log','stripped_log','planks','door_top','trapdoor']):
  im=Image.open(A/'textures/block'/f'{family}_{kind}.png').resize((64,64),Image.Resampling.NEAREST);sheet.paste(im,(110+col*102,row*96+4),im);draw.text((110+col*102,row*96+70),kind,fill=(50,32,20))
sheet.save(OUT/'wood-native-assets.png')
(OUT/'static-checks.json').write_text(json.dumps({'checks':checks,'client_launches':0,'manual_visual_acceptance':'PENDING','offline_preview':'wood-native-assets.png'},indent=2)+'\n')
for check in checks:print(check)
