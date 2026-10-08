"""Gate the actual production artifact, complete resources, and preservation of shipped legacy assets/recipes."""
from pathlib import Path
import hashlib,json,re,zipfile
from PIL import Image
import weapon_resources_1321 as art

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';checks=0
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M).group(1)
jar=ROOT/f'build/libs/slavicmyths-{version}.jar'
def check(condition,message):
 global checks
 checks+=1
 if not condition:raise AssertionError(message)
def snapshot():return {p:hashlib.sha256(p.read_bytes()).hexdigest() for p in RES.rglob('*') if p.is_file()}
before=snapshot();art.main();check(before==snapshot(),'Weapon generator changes canonical resources')
for path in RES.rglob('*.json'):json.loads(path.read_text(encoding='utf-8'));checks+=1
ids=set(art.COMMON)
for material in art.MATERIALS:
 ids.update(material+'_'+part for part in art.PARTS)
 ids.update(art.weapon_id(material,kind) for kind in art.TYPES)
ids-=art.OLD
check(len(ids)==53,'New ID count')
locales={locale:json.loads((RES/f'assets/slavicmyths/lang/{locale}.json').read_text(encoding='utf-8')) for locale in ('ru_ru','en_us')}
for id in ids:
 check(not id.startswith('wood'),'Wooden variant')
 model=json.loads((RES/f'assets/slavicmyths/models/item/{id}.json').read_text())
 for texture in model.get('textures',{}).values():
  if texture.startswith('slavicmyths:'):check((RES/f'assets/slavicmyths/textures/{texture.split(":")[1]}.png').is_file(),'Missing texture '+texture)
 with Image.open(RES/f'assets/slavicmyths/textures/item/{id}.png') as im:check(im.size==(32,32) and im.getbbox() is not None,'Bad icon '+id)
 for locale,data in locales.items():check(bool(data.get('item.slavicmyths.'+id)),'Missing '+locale+' name '+id)
for kind in ('short_bow','heavy_bow'):
 model=json.loads((RES/f'assets/slavicmyths/models/item/{kind}.json').read_text());check(len(model['overrides'])==3,'Missing bow draw frames')
 for entry in model['overrides']:check((RES/('assets/slavicmyths/models/'+entry['model'].split(':')[1]+'.json')).is_file(),'Missing pulling model')
shapes=[art.icon(kind).getchannel('A').tobytes() for kind in (*art.TYPES,'short_bow','heavy_bow')]
check(len(set(shapes))==6,'Weapon types share silhouettes')
bench=list((RES/'data/slavicmyths/recipe').glob('weapon_*.json'))
check(sum(json.loads(p.read_text())['type'].startswith('slavicmyths:armorer') for p in bench)==36,'Bench recipe count')
check(sum(json.loads(p.read_text())['type']=='minecraft:crafting_shaped' for p in bench if p.name.startswith('weapon_part_'))==23,'Part recipe count')
for name in ('WeaponCatalog','WeaponProjectile','ThrowingWeaponItem','FieldBowItem'):
 source=(ROOT/f'src/main/java/org/slavicmyths/combat/{name}.java').read_text();check('net.minecraft.client.' not in source,'Client imports in common '+name)
with zipfile.ZipFile(jar) as archive:
 names=set(archive.namelist())
 check('Implementation-Version: '+version in archive.read('META-INF/MANIFEST.MF').decode(),'Manifest version')
 check(version.encode() in archive.read('META-INF/neoforge.mods.toml'),'NeoForge metadata version')
 check(not any('/verify/' in n or '/smoke/' in n or 'GameTests' in n or any('/'+ns+'/' in n for ns in ('slavicmyths_weapons','slavicmyths_classes','slavicmyths_village','slavicmyths_city','slavicmyths_buildings')) for n in names),'Test-only code/resources bundled')
 for path in RES.rglob('*'):
  if not path.is_file():continue
  name=path.relative_to(RES).as_posix();check(name in names,'Not bundled '+name)
  if name!='META-INF/neoforge.mods.toml':check(archive.read(name)==path.read_bytes(),'Packaged bytes differ '+name)
 for cls in ('combat/WeaponCatalog','combat/WeaponProjectile','combat/ThrowingWeaponItem','combat/FieldBowItem','rpg/RuneRepair','client/WeaponProjectileRenderer','client/FieldBowClient','client/ArmorerRecipeScreen'):
  check('org/slavicmyths/'+cls+'.class' in names,'Missing class '+cls)

# The installed 1.3.1 receipt is a verified local baseline; it is read, never modified here.
receipt=json.loads((ROOT/'docs/verification/polymc-1.3.1-installation.json').read_text())
baseline=Path(receipt['installed'])
if not baseline.exists():
 installation=json.loads((ROOT/'docs/verification/polymc-1.3.2.1-installation.json').read_text())
 baseline=Path(installation['previous_jar'])
check(hashlib.sha256(baseline.read_bytes()).hexdigest()==receipt['sha256'],'Legacy baseline changed')
legacy=0
with zipfile.ZipFile(baseline) as old:
 for name in old.namelist():
  if name.endswith('/') or not (name.startswith(('assets/slavicmyths/models/','assets/slavicmyths/textures/','data/slavicmyths/recipe/','data/slavicmyths/advancement/'))):continue
  path=RES/name;check(path.is_file() and path.read_bytes()==old.read(name),'Legacy asset/recipe/advancement changed '+name);legacy+=1
report={'version':version,'checks':checks,'new_item_ids':sorted(ids),'new_items':len(ids),'weapon_forms':30,'materials':7,'bench_recipes':36,'part_recipes':23,
        'legacy_resources_preserved':legacy,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'bytes':jar.stat().st_size,'jar':str(jar),
        'client_launches':0,'client_visual_acceptance':False,'performance_measured':False}
(ROOT/'docs/verification/weapons-1.3.2.1-resources.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in report.items() if k!='new_item_ids'}))
