"""Current source/catalog/resource/production-JAR audit. No game process is started."""
from pathlib import Path
import argparse,collections,csv,hashlib,io,json,re,subprocess,zipfile
from PIL import Image
from creative_source_inventory import inventory, ROOT
from creative_tab_names import TAB_NAMES

RES = ROOT/'src/main/resources'
OUT = ROOT/'docs/implementation/creative-inventory'
OUT.mkdir(parents=True,exist_ok=True)
checks=[]
parser=argparse.ArgumentParser()
parser.add_argument('--build-dir',default='build')
parser.add_argument('--jar',default=None)
parser.add_argument('--build-log',default='.tools/creative-clean-build.log')
parser.add_argument('--offline-log',default='.tools/creative-offline.log')
args=parser.parse_args()
build_dir=ROOT/args.build_dir
def require(ok,label):
    if not ok: raise AssertionError(label)
    checks.append(label)
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()

rows=[];counts=collections.Counter();catalog={}
for line in (RES/'assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf-8').splitlines():
    if not line or line.startswith('#'):continue
    id,tab,group=line.split('\t')
    require(id not in catalog,'unique ID '+id)
    require(tab in TAB_NAMES or tab=='hidden','valid tab '+id)
    row={'registry_id':'slavicmyths:'+id,'category':tab,'order':counts[tab],'group':group,'stack_variants':0 if tab=='hidden' else 1}
    counts[tab]+=1;rows.append(row);catalog[id]=row
source=inventory()
require(set(source)==set(catalog),'complete independent CURRENT SOURCE registration expansion equals catalog')
for id,row in catalog.items():row.update(source[id])

hidden={id for id,row in catalog.items() if row['category']=='hidden'}
expected_hidden={'bear_cub_spawn_egg','doe_spawn_egg','filled_pitcher','light_beer_wort','dark_beer_wort','hopped_beer_wort','honey_beer_wort','forest_beer_wort','thunder_beer_wort','veles_dark_wort','witch_berry_wort','kvass_wort','mead_must','cider_must'}
require(hidden==expected_hidden,'exact intentional hidden set')
def sequence(ids,cat):
    entries=[catalog[id] for id in ids]
    require(all(row['category']==cat for row in entries),'family category '+' '.join(ids))
    orders=[row['order'] for row in entries]
    require(orders==sorted(orders),'family progression '+' '.join(ids))
def contiguous(ids,cat):
    sequence(ids,cat)
    orders=[catalog[id]['order'] for id in ids]
    require(orders==list(range(orders[0],orders[0]+len(ids))),'family contiguous '+' '.join(ids))
materials=['stone','gold','iron','silver','diamond','perunite','netherite']
for kind in ['spear','flail','sulitsa','throwing_knife']:
    ids=[('spear' if m=='stone' and kind=='spear' else 'flail' if m=='iron' and kind=='flail' else m+'_'+kind) for m in materials]
    contiguous(ids,'weapons')
for mat in ['perunite','dragon','poloz']:
    contiguous([mat+'_'+part for part in ['helmet','chestplate','leggings','boots']],'armor')
for mat in ['apple','linden','rowan','willow','pine','darkened']:
    group='wood_'+mat
    family=[row for row in rows if row['group']==group]
    require(bool(family),'existing wood group '+mat)
    contiguous([row['registry_id'].split(':')[1] for row in family],'decor')
sequence(['short_bow','heavy_bow','finist_bow','serpent_crossbow','three_breath_crossbow'],'weapons')
sequence(['carved_staff','storm_staff','veles_staff','likho_staff','morozko_staff'],'weapons')
sequence(['blank_rune_1','blank_rune_2','blank_rune_3','rune_earth_lord_iv','rune_veles_oath_iv'],'magic')
require(all(catalog[id]['category']=='weapons' for id in ['karachun_scythe','noon_sickle','morozko_staff','poloz_whip']),'all unique combat items')
require(all(catalog[id]['category']=='tools' for id in ['sickle','field_hoe','butchering_knife','perunite_axe','rune_chisel']),'working tools separated')
# Compare numeric rune tiers to actual shared definitions and typed recipe substrates.
balance=(ROOT/'src/main/java/org/slavicmyths/rpg/RuneBalance.java').read_text()
for id,tier in re.findall(r'new Definition\("([a-z_]+)",(\d),',balance):
    require(catalog['rune_'+id]['group']=='runes_'+('I' if tier=='1' else 'II'),'actual shared rune tier '+id)
for path in (RES/'data/slavicmyths/recipe').glob('*.json'):
    recipe=json.loads(path.read_text())
    if recipe.get('type')!='slavicmyths:rune_crafting' or not path.name.startswith('rune_rework'):continue
    id=recipe['result']['id'].split(':')[1];tier=recipe['inputs'][0]['base']['tier']
    require(catalog[id]['group']=='runes_'+{1:'I',2:'II',3:'III',4:'IV'}[tier],'actual typed recipe tier '+id)

for lang in ['ru_ru','en_us']:
    data=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
    for tab,names in TAB_NAMES.items():
        actual=data.get('itemGroup.slavicmyths.'+tab)
        require(actual==names[0 if lang=='ru_ru' else 1],lang+' tab '+tab)
    for id in catalog:
        require(bool(data.get('item.slavicmyths.'+id) or data.get('block.slavicmyths.'+id)),lang+' item label '+id)
    before_path=ROOT/f'.tools/creative-before/{lang}.json'
    if before_path.exists():
        before=json.loads(before_path.read_text(encoding='utf-8'))
        allowed={'itemGroup.slavicmyths.'+tab for tab in TAB_NAMES}
        external_path=ROOT/'.tools/creative-parallel-locales.json'
        external=json.loads(external_path.read_text(encoding='utf-8')).get(lang,{}) if external_path.exists() else {}
        require(all(data.get(k)==external.get(k,{}).get('after',v) for k,v in before.items() if k not in allowed),lang+' unrelated translation values preserved including recorded parallel edits')
        require(set(data)-set(before)<={'itemGroup.slavicmyths.weapons'} | set(external),lang+' new keys belong only to creative tab or recorded parallel edits')

props=(ROOT/'gradle.properties').read_text();version=re.search(r'^mod_version=(.+)$',props,re.M)[1]
jar=ROOT/args.jar if args.jar else build_dir/f'libs/slavicmyths-{version}.jar'
with zipfile.ZipFile(jar) as z:
    require(z.testzip() is None,'production JAR CRC')
    names=set(z.namelist())
    require('META-INF/neoforge.mods.toml' in names,'NeoForge metadata')
    require(('version="'+version+'"') in z.read('META-INF/neoforge.mods.toml').decode(),'production version')
    require(not any(n.startswith(('tools/','smoke/')) or 'CreativeCatalogChecks' in n or '/verify/' in n or 'GameTests.class' in n or 'CreatureFeedbackTests' in n for n in names),'offline/runtime fixtures excluded from production')
    for p in RES.rglob('*'):
        if not p.is_file() or p.name=='neoforge.mods.toml':continue
        key=p.relative_to(RES).as_posix();require(key in names and z.read(key)==p.read_bytes(),'production resource '+key)
        if key.endswith('.json'):json.loads(p.read_text(encoding='utf-8-sig'))
        if key.endswith('.png'):
            with Image.open(p) as img:img.verify()
    for p in (build_dir/'classes/java/main').rglob('*.class'):
        key=p.relative_to(build_dir/'classes/java/main').as_posix();require(key in names and z.read(key)==p.read_bytes(),'production class '+key)
    for id in source:require('assets/slavicmyths/models/item/'+id+'.json' in names,'registered item model '+id)
    # Validate all local model parents, textures and override references, including held variants.
    for p in (RES/'assets/slavicmyths/models').rglob('*.json'):
        data=json.loads(p.read_text())
        refs=[('models',data.get('parent'),'json')]
        refs += [('textures',v,'png') for v in data.get('textures',{}).values()]
        refs += [('models',v.get('model'),'json') for v in data.get('overrides',[])]
        for kind,ref,ext in refs:
            if not ref or not ref.startswith('slavicmyths:'):continue
            target='assets/slavicmyths/'+kind+'/'+ref.split(':')[1]+'.'+ext
            require(target in names,'model reference '+p.stem+' -> '+target)

baseline_path=ROOT/'.tools/creative-before/hashes.json'
if baseline_path.exists():
    baseline=json.loads(baseline_path.read_text())
    allowed={'tools/gardens_resources.py','tools/verify_gardens.py'}
    concurrent_path=ROOT/'.tools/creative-concurrent-files.json'
    concurrent=json.loads(concurrent_path.read_text()) if concurrent_path.exists() else []
    allowed.update(concurrent)
    for path,digest in baseline.items():
        if '__pycache__' in Path(path).parts or path.endswith('.pyc'):continue
        if path not in allowed:require(sha(ROOT/path)==digest,'existing unrelated file preserved '+path)
    additions={p.relative_to(ROOT).as_posix() for p in (ROOT/'src/main').rglob('*') if p.is_file()}-set(baseline)
    allowed_new={'src/main/java/org/slavicmyths/registry/CreativeCatalog.java','src/main/resources/assets/slavicmyths/creative_catalog.tsv','src/main/java/org/slavicmyths/registry/ModItemGroup.java','src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/lang/en_us.json'}
    require(additions<=allowed_new | set(concurrent),'no unaccounted new gameplay classes or resources')

manifest_path=ROOT/'.tools/creative-build-inputs.json'
if manifest_path.exists():
    manifest=json.loads(manifest_path.read_text())
    current={p.relative_to(ROOT).as_posix():sha(p) for p in (ROOT/'src/main').rglob('*') if p.is_file()}
    current['gradle.properties']=sha(ROOT/'gradle.properties')
    require(current==manifest,'all production inputs unchanged during final clean build and audit')

require('BUILD SUCCESSFUL' in (ROOT/args.build_log).read_text(encoding='utf-8-sig'),'clean build PASS')
require('CREATIVE_CATALOG_OFFLINE_PASS' in (ROOT/args.offline_log).read_text(encoding='utf-8-sig'),'pure Java catalog test PASS')
(OUT/'INVENTORY.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
with (OUT/'INVENTORY.csv').open('w',encoding='utf-8',newline='') as f:
    writer=csv.DictWriter(f,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
result={'status':'PASS','version':version,'jar':str(jar),'sha256':sha(jar),'source_items':len(source),'categories':dict(counts),'displayed_registry_ids':len(source)-len(hidden),'displayed_stacks':len(source)-len(hidden),'hidden':sorted(hidden),'checks':len(checks),'scope':'CURRENT_SOURCE_STATIC + pure Java parser + production resources; not a loaded registry/gameplay/visual test','client_starts':0,'server_starts':0,'concurrent_files_included':concurrent if baseline_path.exists() else []}
(OUT/'GATES.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
