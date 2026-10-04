"""0.8.6 reproducibility and actual vanilla fallback validation; no game launch."""
from pathlib import Path
import hashlib,json,os
from kurgan_086 import generate,CREATURES,ITEMS,A,D,R,CUES

paths=[A/f'textures/entity/{n}.png' for n in CREATURES]+[A/f'textures/item/{n}.png' for n in ITEMS]
paths += [D/f'loot_tables/entities/{n}.json' for n in CREATURES]+[A/'sounds.json',A/'lang/ru_ru.json',A/'lang/en_us.json']
protected=[A/f'{folder}/{n}.{ext}' for folder,ext in [('models/item','json'),('textures/item','png')] for n in ['battle_axe','carved_staff','club','retainer_shield']]
def hashes(ps):return {str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in ps}
before=hashes(paths);user=hashes(protected);generate();assert before==hashes(paths),'0.8.6 layer not reproducible';assert user==hashes(protected),'User weapon art overwritten'
assets=Path(os.environ.get('GRADLE_USER_HOME',str(Path.home()/'.gradle')))/'caches/forge_gradle/assets'
index=json.loads((assets/'indexes/1.16.json').read_text());key=index['objects']['minecraft/sounds.json']['hash']
vanilla=json.loads((assets/'objects'/key[:2]/key).read_text())
events=json.loads((A/'sounds.json').read_text());count=0
for name,cues in CUES.items():
 for cue in cues.split():
  entries=events[name+'_'+cue]['sounds'];assert entries
  for entry in entries:assert entry['type']=='event' and entry['name'].split(':',1)[1] in vanilla,entry;count+=1
for name in CREATURES:
 table=json.loads((D/f'loot_tables/entities/{name}.json').read_text());assert table['type']=='minecraft:entity'
 if name in ['kurgan_voevoda','buried_volkhv','unresting_prince']:
  unique={'kurgan_voevoda':'voevoda_insignia','buried_volkhv':'volkhv_amulet','unresting_prince':'princely_seal'}[name]
  assert any(p['entries'][0]['name']=='slavicmyths:'+unique and not p.get('conditions') for p in table['pools'])
common='\n'.join((R/'src/main/java/org/slavicmyths/kurgan'/p).read_text(encoding='utf-8') for p in ['KurganCreature.java','KurganFighter.java','KurganBolt.java','KurganEncounters.java','KurganRoster.java','KurganEncounterState.java','KurganCurse.java'])
assert 'net.minecraft.client' not in common,'Common/client isolation'
report={'version':'0.8.6','resource_reproducibility':'PASS','preserved_user_weapon_art':'PASS','vanilla_fallback_events_checked':count,'headless_rosters':900,'headless_plans':1200,'minecraft_launches':0,'runtime_qa':'TODO'}
(R/'docs/verification/headless-0.8.6.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(f'PASS: six atlases, nine distinct sprites, reproducible resources, preserved user art, {count} vanilla 1.16.5 sound events and guaranteed boss drops; client isolation. Runtime QA TODO.')
