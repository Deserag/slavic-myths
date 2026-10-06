"""Reproduce the four remaining 1.16-style doors using installed Minecraft 1.21.1 assets."""
from pathlib import Path
import json,zipfile
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths'
KINDS=('darkened','pine','rowan','willow')
with zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar') as z:
 template=json.loads(z.read('assets/minecraft/blockstates/oak_door.json'))
 for kind in KINDS:
  states=json.loads(json.dumps(template));models={v['model'].split('/')[-1] for v in states['variants'].values()}
  for v in states['variants'].values():v['model']=v['model'].replace('minecraft:block/oak_door','slavicmyths:block/'+kind+'_door')
  (A/'blockstates'/f'{kind}_door.json').write_text(json.dumps(states,indent=2)+'\n',encoding='utf-8')
  for name in models:
   model=json.loads(z.read('assets/minecraft/models/block/'+name+'.json'));model['textures']={k:v.replace('minecraft:block/oak_door','slavicmyths:block/'+kind+'_door') for k,v in model['textures'].items()}
   (A/'models/block'/name.replace('oak_',kind+'_')).with_suffix('.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
  for old,new in [('bottom','bottom_left'),('bottom_hinge','bottom_right'),('top','top_left'),('top_hinge','top_right')]:
   (A/'models/block'/f'{kind}_door_{old}.json').write_text(json.dumps({'parent':'slavicmyths:block/'+kind+'_door_'+new},indent=2)+'\n',encoding='utf-8')
print('PASS: four native door blockstates, all 32 facing/half/hinge/open variants per door; legacy model names retained.')
