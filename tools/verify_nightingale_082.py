"""Focused resource, structure and deterministic-generator checks for 0.8.2.
Does not bootstrap Minecraft or claim game/runtime/audio QA.
"""
from pathlib import Path
import hashlib,json,re,subprocess
from verify_swamp_073 import read_nbt
from nightingale_082 import generate,SOUNDS
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';D=RES/'data/slavicmyths';A=RES/'assets/slavicmyths'
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def snapshot():return {str(p.relative_to(RES)):hashlib.sha256(p.read_bytes()).hexdigest()for p in RES.rglob('*')if p.is_file()}
def main():
 before=snapshot();generate();assert before==snapshot(),'Nightingale generator did not reproduce resources'
 n=read_nbt((D/'structures/stronghold/nightingale_yard.nbt').read_bytes());cells={tuple(b['pos']):(n['palette'][b['state']],b.get('nbt',{}))for b in n['blocks']}
 markers={nbt.get('metadata'):p for p,(_,nbt)in cells.items()if nbt.get('metadata')};assert 'nightingale'in markers and 'loot:nightingale'in markers
 x,y,z=markers['nightingale'];assert y==8
 # Width 1.1 and height 2.35, including adjacent cells reached by the hitbox.
 for dx in [-1,0,1]:
  for dz in [-1,0,1]:
   for dy in [0,1,2]:
    name=cells.get((x+dx,y+dy,z+dz),({'Name':'minecraft:air'},{}))[0]['Name'];assert name in ['minecraft:air','minecraft:structure_block'],(dx,dy,dz,name)
 for zz in range(z+1,24):
  for yy in range(y,y+3):assert cells.get((x,yy,zz),({'Name':'minecraft:air'},{}))[0]['Name']=='minecraft:air','blocked platform exit'
 assert cells[(x,y-1,z)][0]['Name']=='slavicmyths:pine_planks'
 loot=read(D/'loot_tables/entities/nightingale.json')['pools'];assert len(loot)==3
 assert all('conditions'not in p for p in loot[:2]);assert loot[2]['conditions'][0]['chance']==.28
 assert [p['entries'][0]['name']for p in loot]==['slavicmyths:nightingale_mark','slavicmyths:nightingale_dagger','slavicmyths:nightingale_lock']
 chest=read(D/'loot_tables/chests/stronghold_nightingale.json')['pools'];assert any(p['entries'][0]['name']=='slavicmyths:bandit_horn'for p in chest)
 assert all(p.get('conditions')for p in chest if p['entries'][0]['name']in ['slavicmyths:rune_wind','slavicmyths:resin_ring'])
 fragile=read(D/'tags/blocks/whistle_fragile.json')['values'];assert len(fragile)<=16
 assert not any(any(s in name for s in ['chest','barrel','stone','planks','log','rack'])for name in fragile)
 recipe=read(D/'recipes/nightingale_whistle.json');assert recipe['key']['L']['item']=='slavicmyths:nightingale_lock'
 sound=read(A/'sounds.json');assert len(sound['nightingale_hurt']['sounds'])==3
 model_hashes=set()
 for item in ['nightingale_mark','nightingale_dagger','nightingale_lock','nightingale_whistle','bandit_horn']:
  m=read(A/f'models/item/{item}.json');model_hashes.add(json.dumps(m['elements'],sort_keys=True));assert len(m['elements'])>=6
  for part in m['elements']:
   assert all(-16<=a<b<=32 for a,b in zip(part['from'],part['to']))
   assert part.get('rotation',{}).get('angle',0)in [-45,-22.5,0,22.5,45]
 assert len(model_hashes)==5
 langs=[read(A/f'lang/{lang}.json')for lang in ['ru_ru','en_us']]
 for n in SOUNDS:
  for lang in langs:assert 'subtitles.slavicmyths.nightingale_'+n in lang
 # Verify every custom sound event is actually referenced by executable Java behavior.
 code='\n'.join(p.read_text()for p in (R/'src/main/java/org/slavicmyths/bandit').glob('*.java'))
 for n in SOUNDS:assert 'ModSounds.NIGHTINGALE_'+n.upper()+'.get()'in code,n
 for lang in langs:
  text=lang['book.slavicmyths.nightingale.text'];assert '0.8.2'not in text and '250'not in text
 print('PASS: 0.8.2 deterministic assets; boss clearance/platform exit; separate chest, guaranteed trophies and 28% lock; safe fragile tag; distinct item models; audio event wiring and RU/EN.')
if __name__=='__main__':main()
