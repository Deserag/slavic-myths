from pathlib import Path
import json,re,zipfile
r=Path(r'D:\slavic-myths');a=r/'src/main/resources/assets/slavicmyths';d=r/'src/main/resources/data/slavicmyths'
langs=[json.loads((a/f'lang/{lang}.json').read_text(encoding='utf-8')) for lang in ['ru_ru','en_us']]
for fn in list((a/'models/item').glob('rune_*.json'))+[a/'models/block/path_stone.json',a/'models/block/runic_anvil.json']:
 m=json.loads(fn.read_text());assert m['elements'];
 for e in m['elements']:
  assert all(-16<=v<=32 for v in e['from']+e['to']),fn
  assert all(e['from'][i]<e['to'][i] for i in range(3)),fn
  for face in e['faces'].values():assert face['texture'][1:] in m['textures'],fn
for path in list(d.glob('tags/items/rune_*.json'))+[d/'tags/items/no_reforging.json']:
 for name in json.loads(path.read_text())['values']:assert name.split(':')[1].upper() in (r/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text(),name
for path in ['path_stone','runic_anvil']:
 for rel in [f'blockstates/{path}.json',f'models/block/{path}.json',f'models/item/{path}.json']:assert (a/rel).is_file()
 for rel in [f'recipes/{path}.json',f'loot_tables/blocks/{path}.json']:json.loads((d/rel).read_text())
for lang in langs:
 for id in ['own_path','rune_installed','reforged','three_signs']:assert f'advancements.slavicmyths.{id}.title' in lang
 for p in (r/'src/main/java/org/slavicmyths/rpg').glob('*.java'):
  for key in re.findall(r'PathData.message\(p,"([a-z_]+)"',p.read_text()):
   if key.endswith('_'):continue
   assert 'rpg.slavicmyths.'+key in lang,(p,key)
 for key in ['rpg.slavicmyths.install_help','book.slavicmyths.runes.text','book.slavicmyths.reforging.text']:assert key in lang
print('PASS: new JSON models, bounds, texture aliases, tags, recipes, loot, bilingual messages')
