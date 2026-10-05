"""Validate EVERY current recipe/loot/advancement/tag and references against target assets.
Static validation is deliberately separate from target codec/registry loading.
"""
from pathlib import Path
import json,re,zipfile,sys
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
DATA=RES/'data'
errors=[]
def require(ok,message):
    if not ok: errors.append(message)
def qualify(value): return value if ':' in value else 'minecraft:'+value
baseline=json.loads((ROOT/'docs/port/baseline-0.9.3.json').read_text(encoding='utf-8'))
known={kind:{row['target_id'] for row in baseline['entries'] if row['kind']==kind} for kind in ['item','block','entity_type','structure','feature']}
helper=json.loads((ROOT/'docs/port/baseline-helper-entries-0.9.3.json').read_text(encoding='utf-8'))
for entry in helper['entries']:known.setdefault(entry['kind'],set()).add(entry['target_id'])
sources=ROOT/'build/moddev/artifacts/neoforge-21.1.255-sources.jar'
vanilla=ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
with zipfile.ZipFile(sources) as z:
    for kind,file in [('item','world/item/Items'),('block','world/level/block/Blocks'),('entity_type','world/entity/EntityType'),('feature','world/level/levelgen/feature/Feature')]:
        text=z.read('net/minecraft/'+file+'.java').decode()
        known[kind].update(qualify(x) for x in re.findall(r'register\w*\(\s*"([a-z0-9_:/]+)"',text))
with zipfile.ZipFile(vanilla) as z:
    names=set(z.namelist())
    for kind,prefix in [('item','assets/minecraft/models/item/'),('block','assets/minecraft/blockstates/')]:
        known[kind].update('minecraft:'+n[len(prefix):-5] for n in names if n.startswith(prefix) and n.endswith('.json'))
    vanilla_data={n:json.loads(z.read(n)) for n in names if n.startswith('data/minecraft/') and n.endswith('.json')}
    prefix='data/minecraft/worldgen/structure/'
    known['structure'].update('minecraft:'+n[len(prefix):-5] for n in names if n.startswith(prefix) and n.endswith('.json'))
known['item'].add('minecraft:air')
current={str(p.relative_to(RES)).replace('\\','/'):json.loads(p.read_text(encoding='utf-8')) for p in DATA.rglob('*.json')}
all_data={**vanilla_data,**current}
def resource(kind,id):
    namespace,path=qualify(id).split(':',1)
    return f'data/{namespace}/{kind}/{path}.json' in all_data
counts={kind:0 for kind in ['recipe','loot_table','advancement','tags/item','tags/block','tags/entity_type','tags/enchantment','tags/damage_type','enchantment','damage_type','tags/worldgen/biome','neoforge/biome_modifier','worldgen/configured_feature','worldgen/placed_feature','worldgen/structure','worldgen/structure_set']}
refs=0
loot_graph={}
def reference(kind,value,file):
    global refs
    refs+=1
    if value.startswith('#'):
        require(resource('tags/'+kind,value[1:]),f'{file}: missing {kind} tag {value}')
    elif kind in known:
        require(qualify(value) in known[kind],f'{file}: unknown {kind} {value}')
    else: require(resource(kind,value),f'{file}: missing {kind} {value}')
def ingredient(value,file):
    if isinstance(value,list):
        require(bool(value),f'{file}: empty ingredient alternatives')
        for part in value: ingredient(part,file)
    else:
        require(isinstance(value,dict) and bool(value),f'{file}: malformed ingredient')
        if isinstance(value,dict):
            if 'item' in value: reference('item',value['item'],file)
            elif 'tag' in value: reference('item','#'+value['tag'],file)
            else: require(False,f'{file}: unsupported ingredient {value}')
def stack(value,file):
    require(isinstance(value,dict) and 'id' in value and 'item' not in value,f'{file}: obsolete/missing stack ID')
    if isinstance(value,dict) and 'id' in value:
        reference('item',value['id'],file)
        require(isinstance(value.get('count',1),int) and 1<=value.get('count',1)<=99,f'{file}: invalid stack count')
def tree(value,file,loot_id):
    if isinstance(value,list):
        for child in value: tree(child,file,loot_id)
    elif isinstance(value,dict):
        require(value.get('condition') not in ['minecraft:alternative','minecraft:random_chance_with_looting','forge:loot_table_id'],f'{file}: obsolete loot condition')
        require(value.get('function') not in ['minecraft:looting_enchant','minecraft:set_nbt'],f'{file}: obsolete loot function')
        if value.get('type')=='minecraft:item': reference('item',value['name'],file)
        if value.get('type')=='minecraft:tag': reference('item','#'+value['name'],file)
        if value.get('type')=='minecraft:loot_table':
            require('value' in value and 'name' not in value,f'{file}: nested table requires value')
            if isinstance(value.get('value'),str):
                reference('loot_table',value['value'],file)
                loot_graph.setdefault(loot_id,set()).add(qualify(value['value']))
        if value.get('condition')=='minecraft:match_tool':
            require(not any(key in value.get('predicate',{}) for key in ['item','tag','enchantments','nbt']),f'{file}: obsolete item predicate')
        for key in ['items']:
            selection=value.get(key)
            if isinstance(selection,str): reference('item',selection,file)
            elif isinstance(selection,list) and all(isinstance(x,str) for x in selection):
                for item in selection: reference('item',item,file)
        for field,registry in [('structures','structure'),('biomes','worldgen/biome')]:
            selection=value.get(field)
            if isinstance(selection,str): reference(registry,selection,file)
            elif isinstance(selection,list):
                for entry in selection: reference(registry,entry,file)
        for child in value.values(): tree(child,file,loot_id)
for file,value in current.items():
    namespace,tail=file.split('/',2)[1:]
    category=tail.rsplit('/',1)[0]
    kind=next((x for x in counts if tail.startswith(x+'/')),None)
    if kind is None: continue
    counts[kind]+=1
    id=namespace+':'+tail[len(kind)+1:-5]
    if kind=='recipe':
        type=value['type']
        stack(value.get('result'),file)
        if type.endswith('_shaped'):
            maxsize=4 if type.startswith('slavicmyths:') else 3
            pattern=value.get('pattern',[]);key=value.get('key',{})
            require(0<len(pattern)<=maxsize and all(0<len(row)<=maxsize for row in pattern),f'{file}: invalid pattern dimensions')
            require(len({len(row) for row in pattern})==1,f'{file}: unequal rows')
            symbols=set(''.join(pattern))-{' '}
            require(symbols==set(key) and all(len(x)==1 and x!=' ' for x in key),f'{file}: missing/unused/invalid pattern key')
            for entry in key.values(): ingredient(entry,file)
        elif type.endswith('_shapeless'):
            entries=value.get('ingredients',[]);require(0<len(entries)<=(16 if type.startswith('slavicmyths:') else 9),f'{file}: invalid ingredient count')
            for entry in entries: ingredient(entry,file)
        elif type in ['minecraft:smelting','minecraft:smoking','minecraft:campfire_cooking']:
            ingredient(value.get('ingredient'),file)
            require(value.get('cookingtime',200)>0,f'{file}: invalid cooking time')
        else: require(False,f'{file}: recipe type needs audit {type}')
    elif kind=='advancement':
        if 'parent' in value: reference('advancement',value['parent'],file)
        if 'display' in value: stack(value['display']['icon'],file)
        criteria=value.get('criteria',{});require(bool(criteria),f'{file}: empty criteria')
        for group in value.get('requirements',[[name] for name in criteria]): require(bool(group) and all(name in criteria for name in group),f'{file}: undefined advancement requirement')
        for recipe in value.get('rewards',{}).get('recipes',[]): reference('recipe',recipe,file)
        tree(value,file,id)
    elif kind=='loot_table': tree(value,file,id)
    elif kind=='neoforge/biome_modifier':
        require(value.get('type') in ['neoforge:add_spawns','neoforge:add_features'],f'{file}: unsupported biome modifier requires audit')
        reference('worldgen/biome',value['biomes'],file)
        if value.get('type')=='neoforge:add_features':
            for feature in value['features']:reference('worldgen/placed_feature',feature,file)
            require(value.get('step') in ['vegetal_decoration','underground_ores','surface_structures'],f'{file}: unexpected feature step requires audit')
        for spawn in value.get('spawners',[]):
            reference('entity_type',spawn['type'],file)
            require(spawn.get('weight',0)>0 and 0<spawn.get('minCount',0)<=spawn.get('maxCount',0),f'{file}: invalid spawn weights/group')
    elif kind=='worldgen/structure':
        reference('worldgen/biome',value['biomes'],file)
        require(value.get('type')==id and id in known['structure'],f'{file}: structure/type ID drift')
        require(value.get('step')=='surface_structures' and value.get('terrain_adaptation')=='none',f'{file}: structure placement policy drift')
    elif kind=='worldgen/structure_set':
        for entry in value['structures']:
            reference('structure',entry['structure'],file)
            require(entry['weight']==1,f'{file}: structure weight drift')
        placement=value['placement']
        require(placement['type']=='minecraft:random_spread' and 0<=placement['separation']<placement['spacing'] and placement['spread_type']=='linear',f'{file}: invalid structure placement')
    elif kind=='worldgen/configured_feature':
        reference('feature',value['type'],file)
        require(isinstance(value.get('config'),dict),f'{file}: missing feature configuration')
        cfg=value.get('config',{})
        if value['type']=='minecraft:random_patch':
            require(cfg.get('tries',0)>0 and cfg.get('xz_spread')==7 and cfg.get('y_spread')==3,f'{file}: invalid patch bounds')
            feature=cfg['feature']['feature'];reference('feature',feature['type'],file)
            reference('block',feature['config']['to_place']['state']['Name'],file)
        elif value['type']=='minecraft:ore':
            require(cfg.get('size',0)>0 and cfg.get('discard_chance_on_air_exposure')==0,f'{file}: invalid ore shape/exposure')
            for target in cfg['targets']:
                reference('block',target['state']['Name'],file)
                reference('block','#'+target['target']['tag'],file)
    elif kind=='worldgen/placed_feature':
        reference('worldgen/configured_feature',value['feature'],file)
        for modifier in value.get('placement',[]):
            require(modifier['type'] in ['minecraft:rarity_filter','minecraft:biome','minecraft:in_square','minecraft:heightmap','minecraft:height_range','minecraft:count'],f'{file}: placement modifier needs audit')
            if modifier['type']=='minecraft:rarity_filter':require(modifier.get('chance',0)>0,f'{file}: invalid rarity')
    elif kind=='enchantment':
        reference('item',value['supported_items'],file)
        require(value.get('max_level',0)>0 and bool(value.get('slots')),f'{file}: invalid enchantment levels/slots')
        require(isinstance(value.get('effects'),dict),f'{file}: missing enchantment effects map')
    elif kind=='damage_type':
        require(isinstance(value.get('message_id'),str) and value.get('exhaustion',-1)>=0,f'{file}: invalid damage definition')
        require(value.get('scaling') in ['never','always','when_caused_by_living_non_player'],f'{file}: invalid damage scaling')
    else:
        registry=kind[len('tags/') :]
        require(isinstance(value.get('values'),list),f'{file}: malformed tag')
        for entry in value.get('values',[]):
            if isinstance(entry,str): reference(registry,entry,file)
            elif entry.get('required',True): reference(registry,entry['id'],file)
# No cyclic nested table references within this mod.
def visit(id,path):
    require(id not in path,'Recursive loot table chain: '+' -> '.join(path+[id]))
    if id in path: return
    for child in loot_graph.get(id,[]): visit(child,path+[id])
for id in loot_graph: visit(id,[])
report={'scope':'static-schema-and-references','counts':counts,'references_checked':refs,'errors':errors,'target_codec_loading':'Separate loader evidence: finalize-runtime-nojei.log and finalize-runtime-companions.log','runtime_verified':False}
audit=ROOT/('docs/verification/navigation-0.9.5/static-data-audit.json' if '--navigation' in sys.argv else 'docs/port/stage2-data-audit.json')
audit.parent.mkdir(parents=True,exist_ok=True)
audit.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(counts));print(f'{refs} references checked; {len(errors)} errors. This static audit does not execute codecs; see separate loader logs.')
for error in errors[:30]: print(error)
raise SystemExit(bool(errors))
