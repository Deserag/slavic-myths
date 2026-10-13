"""Production and resource gates; deliberately does not start Minecraft."""
import hashlib, io, json, zipfile
from pathlib import Path
from PIL import Image
from check_event_subscribers import check

ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / 'docs/verification/quest-1.8.5'
REPORT.mkdir(parents=True, exist_ok=True)
jar = ROOT / 'build/libs/slavicmyths-1.8.5.jar'
baseline = Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.8.3.2.jar')
if not baseline.exists():
 candidates = sorted((baseline.parents[2] / '.slavicmyths-backups').rglob('slavicmyths-1.8.3.2.jar'))
 baseline = next(p for p in candidates if hashlib.sha256(p.read_bytes()).hexdigest() == '91da0b9f3552da1be2059a5f214af0d9c9460f6791ccf017ac518cb684013786')
checks = 0
def require(condition, message):
 global checks
 assert condition, message
 checks += 1

log = (ROOT / '.tools/quest185-clean-build.log').read_text(encoding='utf-8', errors='replace')
for marker in ['BUILD SUCCESSFUL', 'checks=124', 'checks=4290', 'PASS 120']:
 require(marker in log, marker)
with zipfile.ZipFile(jar) as z:
 names = z.namelist()
 require(not any('Quest185Checks' in n or '/smoke/' in n or n.startswith('tools/') for n in names), 'test code in production')
 require(b'1.8.5' in z.read('META-INF/neoforge.mods.toml'), 'version')
 for path in (ROOT / 'build/classes/java/main/org/slavicmyths').rglob('*.class'):
  name = path.relative_to(ROOT / 'build/classes/java/main').as_posix()
  require(z.read(name) == path.read_bytes(), 'stale bytecode: ' + name)
 for path in (ROOT / 'src/main/resources').rglob('*'):
  if path.is_file():
   name = path.relative_to(ROOT / 'src/main/resources').as_posix()
   if name != 'META-INF/neoforge.mods.toml':
    require(z.read(name) == path.read_bytes(), 'stale resource: ' + name)
 a = 'assets/slavicmyths/'
 silhouettes = []
 sheet = Image.new('RGBA', (320, 64), '#292323')
 for i, item in enumerate(['wolf_call', 'mor_ward', 'tracker_sense', 'cleansing_powder']):
  im = Image.open(io.BytesIO(z.read(a + 'textures/item/' + item + '.png'))).convert('RGBA')
  require(im.size == (16,16) and im.getbbox() is not None, item + ' sprite')
  silhouettes.append(im.getchannel('A').tobytes())
  sheet.paste(im.resize((64,64), Image.Resampling.NEAREST), (i*80,0), im.resize((64,64), Image.Resampling.NEAREST))
  for suffix in ['', '_icon', '_held']:
   model = json.loads(z.read(a + 'models/item/' + item + suffix + '.json'))
   require(bool(model), item + suffix)
  require(bool(json.loads(z.read(a+'models/item/'+item+'_held.json')).get('elements')), item+' held geometry')
  for locale in ['ru_ru','en_us']:
   require('item.slavicmyths.'+item in json.loads(z.read(a+'lang/'+locale+'.json')), item+' translation')
 require(len(set(silhouettes)) == 4, 'distinct silhouettes')
 sheet.save(REPORT / 'reward-icons.png')
 for item in ['mor_ward','tracker_sense']:
  im = Image.open(io.BytesIO(z.read(a+'textures/mob_effect/'+item+'.png')))
  require(im.size == (18,18), item+' effect icon')
 for item in ['purify_spark','mor_inspection']:
  require(bool(json.loads(z.read(a+'particles/'+item+'.json'))['textures']), item+' particle')
 recipes = {n:json.loads(z.read('data/slavicmyths/recipe/'+n+'.json')) for n in ['mor_ward','tracker_sense','cleansing_powder']}
 require(recipes['tracker_sense']['pattern'] == [' N ','RLR',' D '], 'tracker pattern')
 require(recipes['cleansing_powder']['result']['count'] == 4, 'powder yield')
 require('data/slavicmyths/recipe/wolf_call.json' not in names, 'call must have no recipe')
 sounds = json.loads(z.read(a+'sounds.json'))
 require(len([k for k in sounds if k.startswith('quest185.')]) == 14, '14 sounds')
 require(a+'sounds/quest185/wolf_howl.ogg' in names, 'own howl clip')
 require(b'Quest185CraftingMixin' in z.read('slavicmyths.equipment.mixins.json'), 'crafting gate mixin')
 preserved = 0
 with zipfile.ZipFile(baseline) as old:
  for n in old.namelist():
   if n.startswith(a) and ('poloz' in n or 'mor_horn' in n) and not n.endswith('lang/ru_ru.json'):
    require(z.read(n) == old.read(n), 'changed old Poloz/horn asset: '+n)
    preserved += 1
  for n in ['data/slavicmyths/recipe/mor_horn.json']:
   require(z.read(n) == old.read(n), 'old horn recipe')
  old_sounds = json.loads(old.read(a+'sounds.json'))
  for k,v in old_sounds.items():
   if 'poloz' in k or 'mor_horn' in k:
    require(sounds[k] == v, 'old sound: '+k)
subscribers = check(jar)
result = dict(version='1.8.5', status='PASS', checks=checks, sha256=hashlib.sha256(jar.read_bytes()).hexdigest(), jar=str(jar), event_subscribers=subscribers, preserved_poloz_horn_assets=preserved, offline_checks=124, model_checks=4290, block_checks=120, client_starts=0, server_starts=0, gameplay='NOT_RUN', real_relog='NOT_RUN', mspt_fps='NOT_MEASURED')
(REPORT/'resource-gates.json').write_text(json.dumps(result, indent=2)+'\n', encoding='utf-8')
(REPORT/'clean-build.log').write_text(log, encoding='utf-8')
print(json.dumps(result, indent=2))
