"""Resource/registry catalog and unfiltered placement arithmetic. No world or biome simulation."""
from pathlib import Path
import json,re,csv
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/org/slavicmyths';D=R/'src/main/resources/data/slavicmyths';OUT=R/'docs/implementation/structures275-20261025'
def read(p):return p.read_text(encoding='utf-8')
def main():
    definitions={p.stem:json.loads(read(p)) for p in (D/'worldgen/structure').glob('*.json')}
    sets={p.stem:json.loads(read(p)) for p in (D/'worldgen/structure_set').glob('*.json')}
    registered=set()
    for p in J.rglob('*.java'):
        s=read(p)
        registered.update(re.findall(r'STRUCTURES\.register\("([^"]+)"',s))
    s=read(J/'swamp/SwampStructures.java');registered.update(re.findall(r'"([^"]+)"',re.search(r'String\[\] IDS=\{([^}]+)',s).group(1)))
    assert registered==set(definitions),('registry/resource mismatch',registered-set(definitions),set(definitions)-registered)
    membership={name:[] for name in definitions}
    for name,data in sets.items():
        p=data['placement'];assert 0<=p['separation']<p['spacing']
        for entry in data['structures']:
            id=entry['structure'].split(':')[1];assert id in definitions,id
            membership[id].append({'set':name,'weight':entry['weight'],'placement':p})
    assert all(membership.values()),'structure without a set'
    def expand(value):
        if isinstance(value,list):return value
        if isinstance(value,str) and value.startswith('#slavicmyths:'):
            p=D/'tags/worldgen/biome'/(value.split(':',1)[1]+'.json');assert p.is_file(),value
            return json.loads(read(p))['values']
        return value
    def policy(name):
        if name.startswith('bandit_'):return ('bandit/LargeCampStructure.java' if name.endswith('large') else 'bandit/CampStructure.java','surface/no water; templates; terrain/foundation budget; accepted-neighbor/reference bounds','Natural exploration; large camp also created through earned NPC guide quest; OP locate is separate')
        if name.startswith('kurgan_'):return ('kurgan/KurganStructure.java','terrain/height/reference bounds and burial plan; shared weighted set 41/4/1; separate warrior/great sets frequency0 are not sole source','Natural exploration; manual kurgan creation/locate is OP only; no universal survival-book create button')
        if name.startswith('chud_'):return ('chud/ChudRegionPlanner.java','shared regional coordinator; candidate/roof/height/stronghold rules, not surface flatness; shared salt, cannot sum both sets as independent roots','Natural exploration and explicit ChudResearch request from Yaga/Bayun after quest prerequisites')
        if name=='navya_heart':return ('navya/NavyaHeartStructure.java','Nether navya_grove only; fluid atY31 only if it intersects vertical footprint; middle/upper profile; height; postProcess protected chunks; guardian placement','Natural Nether exploration; no equivalent book construction path')
        if name=='gorodishche':return ('gorodishche/GorodishcheStructure.java','village exclusion12; city templates, surface/foundation, reference/neighbor bounds','Natural exploration; explicit local OP construction exists, no universal survival-book create button')
        if name=='serpent_hill':return ('serpenthill/SerpentHillStructure.java','stronghold exclusion12; native terrain/arena/reference limits','Natural exploration and later ritual; explicit OP construction separate')
        if name in ('sanctuary_unfading_fire','abode_pure_spring'):return ('sanctuary/SanctuaryStructure.java','shared set 1:1; stronghold exclusion12; sampled floor/slope; spring also roof/fluid; templates/reference/neighbor rules','Natural exploration and sanctuary ritual; explicit OP construction separate')
        return ('swamp/SwampStructure.java','sampled floor/water/terrain budget; templates; accepted-neighbor priority and reference bounds','Natural exploration; small template create/locate is OP only, not a general survival-book route')
    rows=[]
    for name,data in sorted(definitions.items()):
        source,conditions,path=policy(name);assert (J/source).is_file()
        rows.append({'KIND':'NATIVE_STRUCTURE','ID':'slavicmyths:'+name,'DIMENSION':'minecraft:the_nether' if name=='navya_heart' else 'minecraft:overworld','BIOMES':json.dumps(expand(data['biomes']),ensure_ascii=False),'SETS_OR_FEATURE':json.dumps(membership[name],ensure_ascii=False),'CONDITIONS':conditions,'PLAYER_PATH':path,'SOURCE':source,'MEASURED_FREQUENCY':'NOT_RUN; candidate grid is not real generation'})
    extras=[('saved/slavicmyths_yaga','SAVED_UNIQUE_SITE','yaga/YagaDiscovery.java','nearby forest candidate, shared world placed/anchor/NPC; loaded fallback; safe YagaPlacement transaction','Earned book yaga_search, explicit create button; access itself #272'),('saved/slavicmyths_grey_wolf','ENTITY_HOME_EVENT','wolf/WolfEncounter.java','forest region35%, quota3..12, arena, distance768; not a structure registry','Explore forest and find actual Grey Wolf; entity changes already #271'),('saved/slavicmyths_ancient_depths','EXPLICIT_DUNGEON_INSTANCE','depths/DepthsService.java','Overworld map item starts bounded saved dungeon job, no native structure_set','Obtain/use existing depths map; not natural worldgen')]
    for id,kind,source,conditions,path in extras:
        assert (J/source).is_file();rows.append({'KIND':kind,'ID':id,'DIMENSION':'minecraft:overworld','BIOMES':'see source; no native structure biome tag','SETS_OR_FEATURE':'not a Structure registry entry','CONDITIONS':conditions,'PLAYER_PATH':path,'SOURCE':source,'MEASURED_FREQUENCY':'NOT_RUN'})
    features={'slavicmyths:bayun_nook':'bayun/BayunFeature.java','slavicmyths:bathhouse':'world/HomesteadFeature.java','slavicmyths:old_barn':'world/HomesteadFeature.java','slavicmyths:deep_pool':'depth/DeepPoolFeature.java','slavicmyths:ancient_shrine':'world/ShrineFeature.java','slavicmyths:path_shrine':'world/PathShrineFeature.java'}
    placed={p.stem:json.loads(read(p)) for p in (D/'worldgen/placed_feature').glob('*.json')}
    modifiers={p.stem:json.loads(read(p)) for p in (D/'neoforge/biome_modifier').glob('*.json')}
    for file in sorted((D/'worldgen/configured_feature').glob('*.json')):
        data=json.loads(read(file));source=features.get(data.get('type'))
        if source is None:continue
        owners=[{'placed':name,'placement':d['placement'],'biome_modifiers':{n:v for n,v in modifiers.items() if 'slavicmyths:'+name in json.dumps(v.get('features',[]))}} for name,d in placed.items() if d.get('feature')=='slavicmyths:'+file.stem]
        rows.append({'KIND':'NATURAL_FEATURE_SITE','ID':'configured_feature/slavicmyths:'+file.stem,'DIMENSION':'minecraft:overworld','BIOMES':'in biome_modifiers below','SETS_OR_FEATURE':json.dumps(owners,ensure_ascii=False),'CONDITIONS':'actual feature preflight/support/fluid/terrain gates; not a native structure_set','PLAYER_PATH':'Bayun: natural forest nook or earned book request' if 'bayun' in file.stem else 'Natural exploration and existing shrine/pool/house interaction','SOURCE':source,'MEASURED_FREQUENCY':'NOT_RUN'})
    OUT.mkdir(parents=True,exist_ok=True)
    with (OUT/'all-sites.tsv').open('w',encoding='utf-8',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=list(rows[0]),delimiter='\t');writer.writeheader();writer.writerows(rows)
    results=[]
    for row in rows:
        id=row['ID'];why='Полной недоступности статически не доказано; ограничения и редкость в all-sites.tsv';fix='Генератор и численные параметры сохранены';expected='Естественное исследование; реальная частота NOT_RUN';qa='Новый мир и копия старого, найти без OP, осмотреть блоки/NPC, сохранить и вернуться'
        if id.endswith(('serpent_hill','sanctuary_unfading_fire','abode_pure_spring')):
            why='Имена завершённых manual places отсутствовали в load allowlist';fix='ManualStructureRecords.load сохраняет эти прежние family names';expected='Повторный поиск использует сохранённое место после relog; natural worldgen неизменён'
        if 'bayun' in id:
            why='Строгая площадка, очистка Y+24, отсутствие rollback при отказе NPC и push карты';fix='BayunPlacement + Cursor transaction, local forest candidates, reservation, completion hints';expected='Ограниченное создание места и кота через существующую запись книги; природная rarity1/128 сохранена';qa='Две победы, запрос книги, дождаться карты, дойти до кота, повторить запрос и relog; защитить свою постройку'
        if id=='saved/slavicmyths_yaga':
            why='Не было общей construction reservation; сбой записи после мутации мог пропустить undo';fix='Общая reservation и conditional rollback; eligibility #272 не менялась';expected='Единственная сохранённая избушка, настоящий placed/NPC после транзакции';qa='Разблокировать дорогу, запросить создание, дойти до Яги/Кузи, повторить поиск/relog'
        if 'chud_' in id:
            why='Ровная поверхность уже не требуется; failure callback отправлял sendSuccess';fix='Ошибка/отмена отправляет sendFailure; геометрия и worker pipeline сохранены';expected='Прежний локальный запрос шести площадок и естественный региональный канал';qa='Поручение Яги/Баюна на Чудь, неровная поверхность, вход/комнаты/NPC, отказ занятой области и relog'
        if id.endswith('navya_heart'):
            why='Лава Y31 запрещала даже целиком расположенное выше Сердце';fix='Проверка Y31 только если слой пересекает вертикальную границу centerY-8';expected='Высокая площадка не отклоняется из-за далёкой лавы; frequency0.6 и protected chunks сохранены';qa='Новый Nether, navya_grove, нижний/средний/верхний ярусы, реальное Сердце и Сторож, relog'
        results.append({'Структура':id.rsplit(':',1)[-1],'ID':id,'Где появляется':row['DIMENSION']+'; '+row['BIOMES'],'Почему была недоступна':why,'Что исправлено':fix,'Ожидаемая доступность':expected,'Офлайн-проверка':'Source/resource audit; native transaction/NBT/basal boundary where applicable; candidate arithmetic5seed; GAMEPLAY_NOT_RUN','Что проверить в Minecraft':qa})
    with (OUT/'structure-results.tsv').open('w',encoding='utf-8',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=list(results[0]),delimiter='\t');writer.writeheader();writer.writerows(results)
    (OUT/'biome-tags.json').write_text(json.dumps({p.relative_to(D).as_posix():json.loads(read(p)) for p in (D/'tags/worldgen/biome').rglob('*.json')},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    (OUT/'structure-sets.json').write_text(json.dumps(sets,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('PASS source catalog:',len(definitions),'native structures,',len(sets),'sets,',len(rows),'total structure/feature/event rows. GAMEPLAY_NOT_RUN.')
if __name__=='__main__':main()
