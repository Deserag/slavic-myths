"""Production and resource gates; deliberately does not start Minecraft."""
import hashlib, io, json, zipfile, re
from pathlib import Path
from PIL import Image
from check_event_subscribers import check

ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / 'docs/verification/quest-1.8.5-final'
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

log = (ROOT / '.tools/quest185-final-clean-build.log').read_text(encoding='utf-8', errors='replace')
for marker in ['BUILD SUCCESSFUL', 'checks=182', 'checks=4290', 'PASS 120']:
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
 for item in ['purify_spark','purify_green_spark','mor_inspection','cleansed_land_ward']:
  require(bool(json.loads(z.read(a+'particles/'+item+'.json'))['textures']), item+' particle')
 recipes = {n:json.loads(z.read('data/slavicmyths/recipe/'+n+'.json')) for n in ['mor_ward','tracker_sense','cleansing_powder']}
 require(recipes['tracker_sense']['pattern'] == [' N ','RLR',' D '], 'tracker pattern')
 require(recipes['cleansing_powder'] == {'type':'minecraft:crafting_shaped','pattern':['BDB',' C ','   '],'key':{'B':{'item':'minecraft:bone_meal'},'D':{'item':'minecraft:dandelion'},'C':{'item':'minecraft:charcoal'}},'result':{'id':'slavicmyths:cleansing_powder','count':4}}, 'exact FINAL powder recipe')
 for p in (ROOT/'src/main/resources/data/slavicmyths/recipe').glob('*.json'):
  value=json.loads(p.read_text(encoding='utf-8'))
  if value.get('result',{}).get('id')=='slavicmyths:cleansing_powder':require(p.stem=='cleansing_powder','no alternative powder recipe')
 for locale in ['ru_ru','en_us']:
  lang=json.loads(z.read(a+'lang/'+locale+'.json'))
  for key in ['study_eye','earth_title','earth_training','virtual_clean','ask_recipe','earth_progress','land_protected']:
   require(bool(lang.get('quest185.'+key)), 'new UI localization '+key)
  for key in ['early.0','early.1','early.2','early.3','early.cat','early.wolf','early.reward','early.lesson','ritual.5','ritual.end']:
   require(bool(lang.get('quest185.dialogue.'+key)), 'early dialogue '+key)
  require('светопыль' not in lang['tales.entry.cleansing_knowledge.studied'], 'book uses current ingredients')
 mixins=json.loads(z.read('slavicmyths.equipment.mixins.json'))
 require('Quest185BlockChangeMixin' in mixins['mixins'], 'immunity/change hook registered')
 for name in ['Quest185CraftingMixin','Quest185CrafterMixin']:
  source=(ROOT/'src/main/java/org/slavicmyths/equipment/mixin'/ (name+'.java')).read_text(encoding='utf-8')
  require('cleansing_powder' not in source and 'POWDER.get()' not in source, 'powder crafting has no story gate')
 require('case 5->ItemStack.EMPTY' in (ROOT/'src/main/java/org/slavicmyths/quest185/Quest185Story.java').read_text(encoding='utf-8'), 'Q6 has no new powder reward')
 init=(ROOT/'src/main/java/org/slavicmyths/quest185/Quest185Content.java').read_text(encoding='utf-8')
 require(init.index('CleansedLand.IMMUNITY.getId()')<init.index('Quest185Origins.TYPES.register(bus)'), 'attachment initialized before bus registration')
 installed=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.8.5.jar')
 expected_prior='1be024d3a1cd07757bb7b078e8bc773caf01c8a0c4c64b1acb4edd04b3883b4b'
 if not installed.exists() or hashlib.sha256(installed.read_bytes()).hexdigest()!=expected_prior:
  installed=next(p for p in (installed.parents[2]/'.slavicmyths-backups').rglob('slavicmyths-1.8.5.jar') if hashlib.sha256(p.read_bytes()).hexdigest()==expected_prior)
 if installed.exists():
  with zipfile.ZipFile(installed) as prior:
   for n in prior.namelist():
    if n.startswith(a) and any(term in n for term in ['textures/entity/bayun','textures/entity/yaga','textures/entity/grey_wolf','models/entity/bayun','models/entity/yaga','models/entity/grey_wolf']) or n.startswith(a+'models/item/') and any('/'+item in n for item in ['wolf_call','mor_ward','tracker_sense']):
     require(z.read(n)==prior.read(n), 'other approved assets unchanged: '+n)

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
result = dict(version='1.8.5', status='PASS', checks=checks, sha256=hashlib.sha256(jar.read_bytes()).hexdigest(), jar=str(jar), event_subscribers=subscribers, preserved_poloz_horn_assets=preserved, offline_checks=182, model_checks=4290, block_checks=120, client_starts=0, server_starts=0, gameplay='NOT_RUN', real_relog='NOT_RUN', mspt_fps='NOT_MEASURED')
(REPORT/'resource-gates.json').write_text(json.dumps(result, indent=2)+'\n', encoding='utf-8')
(REPORT/'clean-build.log').write_text(log, encoding='utf-8')
print(json.dumps(result, indent=2))
