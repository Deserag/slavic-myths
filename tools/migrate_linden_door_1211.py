"""Regenerate the authorized linden door fix from pinned Minecraft 1.21.1 assets."""
from pathlib import Path
import json,zipfile
R=Path(__file__).resolve().parents[1];out=R/'src/main/resources/assets/slavicmyths'
def migrate():
 with zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar') as jar:
  states=json.loads(jar.read('assets/minecraft/blockstates/oak_door.json'));models={v['model'].split('/')[-1] for v in states['variants'].values()}
  for value in states['variants'].values():value['model']=value['model'].replace('minecraft:block/oak_door','slavicmyths:block/linden_door')
  (out/'blockstates/linden_door.json').write_text(json.dumps(states,indent=2)+'\n',encoding='utf-8')
  for name in models:
   model=json.loads(jar.read('assets/minecraft/models/block/'+name+'.json'));model['textures']={k:v.replace('minecraft:block/oak_door','slavicmyths:block/linden_door') for k,v in model['textures'].items()};(out/'models/block'/name.replace('oak_','linden_')).with_suffix('.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
 for old,new in [('bottom','bottom_left'),('bottom_hinge','bottom_right'),('top','top_left'),('top_hinge','top_right')]:
  (out/'models/block'/('linden_door_'+old+'.json')).write_text(json.dumps({'parent':'slavicmyths:block/linden_door_'+new},indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':migrate();print('Native linden door resources reproduced')
