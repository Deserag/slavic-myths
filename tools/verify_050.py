from pathlib import Path
import json,re,zipfile,struct,zlib,subprocess,shutil
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';J=ROOT/'src/main/java/org/slavicmyths'
ids=['kikimora','poludnitsa','polevik','bannik','igosha','ovinnik']
items=set(re.findall(r'\.register\(\s*"([a-z_]+)"',(J/'registry/ModItems.java').read_text(encoding='utf-8')))
# Only new content and its dependencies, not an unrelated gameplay audit.
for name in ids:
 assert f'"{name}"' in (J/'registry/ModEntities.java').read_text()
 cls=name.capitalize();model=(J/f'client/{cls}Model.java').read_text()
 assert 'extends EntityModel<' in model and f'{cls}Entity' in model
 assert (J/f'entity/{cls}Entity.java').is_file()
 assert (A/f'models/item/{name}_spawn_egg.json').is_file()
 json.loads((R/f'data/slavicmyths/loot_tables/entities/{name}.json').read_text())
 for lang in ('ru_ru','en_us'):
  labels=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));assert f'entity.slavicmyths.{name}' in labels
 for suffix in (['_0','_1','_2','_3'] if name=='kikimora' else ['','_revealed'] if name=='poludnitsa' else ['']):
  p=A/f'textures/entity/{name}{suffix}.png';b=p.read_bytes();assert b[:8]==b'\x89PNG\r\n\x1a\n'
  assert struct.unpack('!II',b[16:24])==(256,256)
  off=8;compressed=b''
  while off<len(b):
   n=struct.unpack('!I',b[off:off+4])[0];kind=b[off+4:off+8];body=b[off+8:off+8+n]
   assert zlib.crc32(kind+body)&0xffffffff==struct.unpack('!I',b[off+8+n:off+12+n])[0]
   if kind==b'IDAT':compressed+=body
   off+=n+12
  assert len(zlib.decompress(compressed))==256*(1+256*4)
 sounds=json.loads((A/'sounds.json').read_text())
 for kind in ('ambient','hurt','death','angry','power'):
  name2=name+'_'+kind;assert name2 in sounds
  for entry in sounds[name2]['sounds']:
   p=A/('sounds/'+entry['name'].split(':')[1]+'.ogg');assert p.is_file()
   subprocess.run([shutil.which('ffmpeg'),'-v','error','-i',str(p),'-f','null','-'],check=True,stdout=subprocess.DEVNULL,stderr=subprocess.PIPE)
for name in ('bath_stove','wooden_tub'):
 for path in (f'blockstates/{name}.json',f'models/block/{name}.json',f'models/item/{name}.json'):json.loads((A/path).read_text())
 assert name in items
# Confirm every mod-scoped new model texture, parent and recipe ingredient resolves.
for p in (A/'models').rglob('*.json'):
 m=json.loads(p.read_text())
 for ref in m.get('textures',{}).values():
  if ref.startswith('slavicmyths:'):assert (A/('textures/'+ref.split(':')[1]+'.png')).is_file(),ref
for p in (R/'data/slavicmyths/recipes').glob('*.json'):
 m=json.loads(p.read_text())
 for ing in m.get('ingredients',[])+list(m.get('key',{}).values()):
  if ing.get('item','').startswith('slavicmyths:'):assert ing['item'].split(':')[1] in items,(p,ing)
with zipfile.ZipFile(ROOT/'build/libs/slavicmyths-0.5.0.jar') as z:
 assert b'version="0.5.0"' in z.read('META-INF/mods.toml')
 for name in ids:
  cl='org/slavicmyths/entity/'+name.capitalize()+'Entity.class';assert struct.unpack('!H',z.read(cl)[6:8])[0]==52
 assert b'func_78016_d' in z.read('org/slavicmyths/registry/ModItemGroup$1.class')
 for p in R.rglob('*'):
  if p.is_file() and p.name!='mods.toml':assert z.read(p.relative_to(R).as_posix())==p.read_bytes(),p
 assert not any('smoke/' in n or n.endswith('.java') for n in z.namelist())
print('PASS: six entity/model/loot/egg registrations, ten entity textures, 30 decoded original sounds, block/item references, recipes, current production resources, Java 8/reobfuscation.')
print('No Minecraft launch or gameplay assertions performed.')
