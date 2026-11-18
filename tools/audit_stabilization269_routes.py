"""Join real registry census with static source routes; absence of a route is never a gameplay FAIL."""
import collections,json,re
from pathlib import Path
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/stabilization269-20261026';J=R/'src/main/java'
census=json.loads((O/'census-results.json').read_text(encoding='utf-8-sig'));index=json.loads((O/'mechanics-inventory.json').read_text(encoding='utf-8-sig'))
catalog={x['registry_path']:x for x in index['creative_catalog']};sources={p:p.read_text(encoding='utf-8') for p in J.rglob('*.java')}
inputs=collections.defaultdict(set);outputs=collections.defaultdict(set);loot=collections.defaultdict(set)
def values(x):
 if isinstance(x,str):yield x
 elif isinstance(x,list):
  for v in x:yield from values(v)
 elif isinstance(x,dict):
  for v in x.values():yield from values(v)
def ingredient_refs(x,out):
 if isinstance(x,list):
  for v in x:ingredient_refs(v,out)
 elif isinstance(x,dict):
  for k,v in x.items():
   if k=='result':continue
   if k in ('item','id') and isinstance(v,str):out.add(('item',v))
   elif k=='tag' and isinstance(v,str):out.add(('tag',v))
   else:ingredient_refs(v,out)
recipe_refs=[]
for p in (R/'src/main/resources/data/slavicmyths/recipe').rglob('*.json'):
 x=json.loads(p.read_text());name=str(p.relative_to(R));refs=set();ingredient_refs(x,refs);recipe_refs.append((name,refs))
 for v in values(x.get('result',{})):outputs[v].add(name)
for p in (R/'src/main/resources/data/slavicmyths/loot_table').rglob('*.json'):
 for v in values(json.loads(p.read_text())):loot[v].add(str(p.relative_to(R)))
static_blocks={'Block','RotatedPillarBlock','SlabBlock','StairBlock','FenceBlock','WallBlock','CarpetBlock','GlassBlock','StainedGlassBlock','StainedGlassPaneBlock','PaneBlock'}
items=[]
for item in census['item_census']:
 row=dict(item);key=row['id'].split(':',1)[1];cat=catalog.get(key,{});row['creative_tab']=cat.get('tab');row['creative_group']=cat.get('group');tags=set(row['tags']);uses=[p for p,refs in recipe_refs if ('item',row['id']) in refs or any(('tag',t) in refs for t in tags)];row['static_recipe_inputs']=uses;row['static_recipe_outputs']=sorted(outputs[row['id']]);row['static_loot_tables']=sorted(loot[row['id']]);cls=row['java_class'].split('.')[-1];block=row.get('block_class','').split('.')[-1]
 if 'SpawnEgg' in cls:kind='CREATIVE_SPAWN_EGG; не обычная Survival-награда'
 elif row.get('food_component') or cls in ('BeverageItem','YagaUtilityItem'):kind='CONSUMABLE_OR_UTILITY; эффект/получение проверять отдельно'
 elif cat.get('tab')=='decor' and block in static_blocks:kind='DECOR_CONSTRUCTION; также может быть материалом рецептов'
 elif row.get('block_class'):kind='WORLD_BLOCK; декор/хранение/станок/растение требуют проверки назначения'
 elif any(t.startswith('curios:') for t in tags):kind='ACCESSORY_CANDIDATE; тег не доказывает работающий эффект'
 elif cat.get('tab') in ('weapons','armor','tools'):kind='EQUIPMENT; крафт/экипировка/боевой эффект отдельные требования'
 elif key.endswith(('_wort','_must')):kind='BREW_INTERMEDIATE; скрытость не означает отсутствие механики'
 elif uses:kind='MATERIAL_IN_DECLARED_RECIPE; доступность ингредиента отдельно'
 else:kind='ROLE_REVIEW; не объявлять неработающим по отсутствию статической ссылки'
 row['role_scope']=kind;row['full_survival_usefulness']='NOT_RUN';items.append(row)
entities=[]
resource_files=list((R/'src/main/resources/data/neoforge/biome_modifier').glob('*.json'))+list((R/'src/main/resources/data/slavicmyths/worldgen').rglob('*.json'))
resource_values={p:set(values(json.loads(p.read_text()))) for p in resource_files}
for entity in census['entity_census']:
 row=dict(entity);key=row['id'].split(':',1)[1];bindings=set();refs=[]
 for p,text in sources.items():
  for line in text.splitlines():
   if f'register("{key}"' in line and 'EntityType' in line:
    prefix=line.split(f'register("{key}"')[0];matches=re.findall(r'\b([A-Z][A-Z0-9_]*)\s*=',prefix)
    if matches:bindings.add(p.stem+'.'+matches[-1])
 for p,text in sources.items():
  if '/client/' in p.as_posix():continue
  lines=text.splitlines()
  for n,line in enumerate(lines):
   if any(b in line for b in bindings) or f'"{row["id"]}"' in line or f'"{key}"' in line:
    context=' '.join(lines[max(0,n-3):n+4]);
    if re.search(r'spawn|addFreshEntity|\.create\(|SpawnPlacement|summon',context,re.I):refs.append({'source':str(p.relative_to(R)),'line':n+1,'context':context[:700]})
 row['source_symbols']=sorted(bindings);row['spawn_entrypoint_candidates']=refs;row['resource_references']=[str(p.relative_to(R)) for p,vals in resource_values.items() if row['id'] in vals];row['kind_scope']='LIVING_MOB_OR_NPC' if 'default_max_health' in row else 'PROJECTILE_VEHICLE_OR_TECHNICAL; не требовать случайный spawn без проверки роли';row['natural_spawn_and_ai']='NOT_RUN';entities.append(row)
out={'scope':'native facts + static route candidates, not complete behavioral audit; dynamic source/template NBT routes can be missing','items':items,'entities':entities,'role_counts':dict(collections.Counter(x['role_scope'] for x in items))}
(O/'registry-routes.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf-8')
lines=['# Предметы и существа: проверяемые факты и незакрытые маршруты','', 'Это дополнение к MECHANICS.md. Статические ссылки и классы — ориентиры для аудита, не доказательство пользы/естественной доступности. Полный Survival по строкам пока NOT_RUN.','', '| Роль по ограниченным данным | Количество |','|---|---:|']
lines += [f'| {k} | {v} |' for k,v in out['role_counts'].items()]
lines += ['', 'Creative-каталог содержит 247 строк decor, но интерактивные блоки и материалы не объявляются бесполезным декором. 14 hidden включают промежуточные сусла/настои; нельзя удалять их как технический мусор. Полные IDs, классы, item tags, ссылки рецептов/loot и источники появления находятся в registry-routes.json.','', '| Сущность | Фактический класс / роль | Кандидаты источников появления | Остаток |','|---|---|---|---|']
for e in entities:
 refs=[f'{r["source"]}:{r["line"]}' for r in e['spawn_entrypoint_candidates'][:3]]+e['resource_references'][:2]
 lines.append(f'| {e["id"]} | {e["java_class"]}; {e["kind_scope"]} | '+('; '.join(refs) if refs else 'Динамический маршрут требует отдельного чтения source/NBT')+' | natural/AI/бой NOT_RUN; factory PASS |')
lines += ['', 'Отсутствие JSON add_spawns не доказывает недоступность: существуют события, квесты, вызовы оружием и structure NBT. Наличие SpawnPlacements лишь задаёт predicate, само по себе не добавляет моба в biome spawn list. Цены/рецепты/частоты не изменялись.']
(O/'REGISTRY_ROUTES.md').write_text('\n'.join(lines)+'\n',encoding='utf-8');print('registry source routes:',len(items),'items,',len(entities),'entities; behavioral acceptance NOT_RUN')
