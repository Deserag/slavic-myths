"""Register existing entity-texture references used by baked models; never generates artwork/audio."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets';p=A/'minecraft/atlases/blocks.json'
atlas=json.loads(p.read_text(encoding='utf-8'));sprites={s.get('sprite',s.get('resource')) for s in atlas['sources'] if s['type']=='minecraft:single'};needed=set()
for model in (A/'slavicmyths/models').rglob('*.json'):
 for texture in json.loads(model.read_text(encoding='utf-8')).get('textures',{}).values():
  if isinstance(texture,str) and texture.startswith('slavicmyths:entity/'):needed.add(texture)
added=sorted(needed-sprites)
for texture in added:
 namespace,path=texture.split(':',1)
 if not (A/namespace/'textures'/f'{path}.png').is_file():raise SystemExit(f'Missing actual source texture: {texture}')
 atlas['sources'].append({'type':'minecraft:single','resource':texture})
p.write_text(json.dumps(atlas,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Existing texture sprites registered:',added,'; art/UV/models/IDs unchanged by this script')
