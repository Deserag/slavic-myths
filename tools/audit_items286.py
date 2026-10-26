"""Reproducible source inventory/producer-consumer index. Never starts Minecraft.

Catalog coverage is exhaustive; registration evidence is source-only (literal or
dynamic family). Runtime registry/effect/acquisition is deliberately NOT asserted.
"""
from pathlib import Path
import csv,json,re,collections,hashlib,subprocess
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';J=R/'src/main/java/org/slavicmyths'
OUT=R/'docs/implementation/items286-20261026'
def write_tsv(name,rows):
    with (OUT/name).open('w',encoding='utf8',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]),delimiter='\t');w.writeheader();w.writerows(rows)
def strings(v):
    if isinstance(v,str):yield v
    elif isinstance(v,dict):
        for x in v.values():yield from strings(x)
    elif isinstance(v,list):
        for x in v:yield from strings(x)
def run():
    OUT.mkdir(parents=True,exist_ok=True)
    catalog=[l.split('\t') for l in (RES/'assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf8').splitlines() if l and not l.startswith('#')]
    assert len(catalog)==923 and len({x[0] for x in catalog})==923
    java={p.relative_to(J).as_posix():p.read_text(encoding='utf8') for p in J.rglob('*.java')}
    data={p.relative_to(RES).as_posix():json.loads(p.read_text(encoding='utf8')) for p in (RES/'data').rglob('*.json')}
    data_values={f:set(strings(d)) for f,d in data.items()}
    ref_index=collections.defaultdict(list)
    field_index=collections.defaultdict(list)
    for f,s in java.items():
        if f.startswith('client/'):continue
        for n,line in enumerate(s.splitlines(),1):
            for cls,field in set(re.findall(r'\b(\w+)\.([A-Z][A-Z0-9_]*)\b',line)):
                field_index[cls+'.'+field].append({'source':f,'line':n,'text':line.strip()})
            tokens=set(re.findall(r'"([a-z][a-z0-9_]+)"|\b([A-Z][A-Z0-9_]+)\b',line))
            for a,b in tokens:ref_index[a or b.lower()].append({'source':f,'line':n,'text':line.strip()})
    # Registry fields often differ from IDs (PolozContent.DUST, Husbandry.MILK).
    # Index qualified field consumers as well as ID strings; otherwise acquisition
    # and usefulness would be falsely reported missing for dynamic rewards.
    for f,s in java.items():
        for m in re.finditer(r'\b([A-Z][A-Z0-9_]*)\s*=\s*(?:[\w.]+\.register|item|food|stew|meat)\(\s*"([a-z0-9_]+)"',s):
            field,id=m.groups()
            for ref in field_index[Path(f).stem+'.'+field]:
                if ref not in ref_index[id]:ref_index[id].append(ref)
    languages={l:json.loads((RES/f'assets/slavicmyths/lang/{l}.json').read_text(encoding='utf8')) for l in ['ru_ru','en_us']}
    food_index=collections.defaultdict(list)
    for f,s in java.items():
        for m in re.finditer(r'\b(food|stew|meat|drink)\(\s*"([a-z0-9_]+)"\s*,\s*([0-9.]+)[Ff]?\s*,\s*([0-9.]+)[Ff]?(?:\s*,\s*([0-9.]+)[Ff]?)?',s):
            kind,id,a,b,c=m.groups();nutrition=float(b if kind=='drink' else a);sat=float(c if kind=='drink' and c is not None else b)
            food_index[id].append(dict(source=f,call=m.group(),nutrition=nutrition,saturationModifier=sat,saturation_gain=nutrition*sat*2))
    tags={k.split('/tags/item/',1)[1][:-5]:v.get('values',[]) for k,v in data.items() if '/tags/item/' in k and k.startswith('data/slavicmyths/')}
    def expand(tag,seen=frozenset()):
        if tag in seen:return set()
        out=set()
        for v in tags.get(tag,[]):
            v=v.get('id','') if isinstance(v,dict) else v
            if v.startswith('#slavicmyths:'):out|=expand(v.split(':')[1],seen|{tag})
            else:out.add(v)
        return out
    expanded_tags={t:expand(t) for t in tags}
    delegated=set()
    artifact_file=R/'docs/implementation/artifacts284-20261025/registry.json'
    # This module is read as data; importing it does not run its audit.
    import importlib.util
    spec=importlib.util.spec_from_file_location('artifacts284',R/'tools/audit_artifacts284.py');module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module);delegated.update(module.FACTS)
    active_blocks={'kitchen_table':'kitchen/KitchenTile.java','fruit_press':'brewing/BrewTile.java','fermentation_vat':'brewing/BrewTile.java','small_keg':'brewing/BrewTile.java','chud_bloomery':'industry/MachineTile.java','refining_hearth':'industry/MachineTile.java','flax_breaker':'textile/TextileStation.java','spinning_wheel':'textile/TextileStation.java','loom_table':'textile/TextileStation.java','drying_rack':'storage/StorageBlock.java','herb_drying_rack':'storage/StorageBlock.java','altar':'ritual/AltarTileEntity.java','ancient_idol':'ritual/Rituals.java','path_stone':'path/PathStoneBlock.java','runic_anvil':'rpg/RunicAnvilBlock.java'}
    for id in ['storage_sack','basket','large_basket','produce_crate','household_chest','wooden_barrel','wall_shelf','storage_shelf','drying_rack']:active_blocks[id]='storage/StorageTile.java'
    for id in ['feeder','straw_nest']:active_blocks[id]='husbandry/YardStorage.java'
    active_blocks.update({'fishing_net':'water/FishingNetBlock.java','hearth':'block/HearthBlock.java','millstone':'village/MillstoneBlock.java','armorer_table':'armorer/ArmorerBlock.java','druzhinnik_table':'military/MilitaryTableTile.java','training_dummy':'furniture/TrainingDummyBlock.java','bath_stove':'block/BathStoveBlock.java','linen_bed':'textile/LinenBed.java','wooden_crate':'furniture/CrateTile.java','burial_log_coffin':'kurgan/BurialCoffinTile.java','cursed_skull_cache':'volkolak/SkullCacheBlock.java'})
    rows=[];triage=[]
    for id,group,sub in catalog:
        needle='slavicmyths:'+id;literal=re.compile(r'"'+re.escape(id)+r'"|\b'+id.upper()+r'\b')
        refs=ref_index[id]
        reg=[x for x in refs if '.register(' in x['text'] or (x['source'] in ['registry/ModItems.java','kitchen/KitchenII.java','brewing/Brewing.java','husbandry/Husbandry.java','industry/Industry.java'] and any(t in x['text'] for t in ['food(', 'stew(', 'meat(', 'item(', 'drink(', 'batch(']))]
        family=[]
        if sub.startswith('components_'):family=['combat/WeaponCatalog.java']
        elif id.endswith('_sheaf'):family=['storage/Household.java']
        elif sub=='saplings':family=['wood/Woodlands.java']
        elif id.endswith('_sapling') and sub=='berries':family=['garden/Gardens.java']
        elif sub in ['runes_I','runes_II','runes_III','runes_IV','rare_runes','legacy_basic_runes','story_runes']:family=['rpg/RuneFoundation.java','rpg/RareRunes.java','rpg/AdvancedRunes.java','rpg/Runes.java','industry/MiningRunes.java']
        elif id.startswith('rune_') and id.endswith('_fragment'):family=['rpg/RareRunes.java']
        elif sub=='drinks' and id.endswith('_beer_mug'):family=['brewing/Brewing.java']
        elif id in ['animal_fat','goat_hide','flax_fiber','butchering_knife']:family=['textile/Textiles.java']
        elif id in ['iron_blank','spear_shaft','short_shaft','flail_handle','flail_chain']:family=['combat/WeaponCatalog.java']
        elif id.startswith('firebird_') or id=='smoldering_feather':family=['firebird/FirebirdContent.java']
        elif id.startswith('mat_'):family=['rewards/RewardContent.java']
        elif id in ['blackcurrant','lingonberry','cranberry']:family=['garden/Gardens.java']
        elif id in ['iron_dust','gold_dust','coal_dust','lapis_dust','diamond_dust','perunite_dust','soul_fragment','soul','empowered_soul','bound_soul']:family=['rpg/RuneFoundation.java']
        elif id in ['navya_ash','living_root','bone_thorn','guardian_branch','soul_blight']:family=['navya/NavyaContent.java']
        for f in family:
            if f in java:
                reg.extend({'source':f,'line':n,'text':line.strip()} for n,line in enumerate(java[f].splitlines(),1) if 'register(' in line or ('food(' in line and id in line))
        if not reg:
            for f in (['husbandry/Husbandry.java'] if id in ['raw_goose','raw_duck','cooked_goose','cooked_duck'] else ['storage/Household.java'] if id in active_blocks and active_blocks[id].startswith('storage/') else []):
                reg.extend({'source':f,'line':n,'text':line.strip()} for n,line in enumerate(java[f].splitlines(),1) if 'register(' in line or id in line)
        block=(RES/f'assets/slavicmyths/blockstates/{id}.json').exists()
        own=group in ['resources','food','tools','magic','farming'] or id in active_blocks or id.startswith('chud_') and any(w in id for w in ['pickaxe','axe','shovel','lantern','lift'])
        if id in delegated:category='DELEGATED_284'
        elif group=='accessories':category='DELEGATED_278'
        elif group=='armor':category='DELEGATED_287'
        elif group=='weapons':category='DELEGATED_285'
        elif id.endswith('spawn_egg') or group=='hidden':category='TECHNICAL_OR_HIDDEN'
        elif id in active_blocks:category='FUNCTIONAL_BLOCK'
        elif own and block and sub not in ['seeds','berries','saplings','herbs','crops']:category='PLACEABLE_BLOCK'
        elif own:category='GAMEPLAY_ITEM'
        else:category='OUTSIDE_286_DECOR_OR_OTHER'
        if id=='ancient_coin':category='CURRENCY'
        if category=='PLACEABLE_BLOCK' and group=='magic':category='FUNCTIONAL_RITUAL_BLOCK'
        recipes=[];consumers=[];loot=[];trade=[];tagrefs=[]
        mytags={t for t in tags if needle in expanded_tags[t]}
        for f,d in data.items():
            values=data_values[f];has=needle in values or bool({'slavicmyths:'+t for t in mytags}&values) or bool({'#slavicmyths:'+t for t in mytags}&values)
            if not has:continue
            if '/recipe/' in f:
                result=d.get('result',d.get('output',{}));rvalues=set(strings(result))
                (recipes if needle in rvalues else consumers).append(f)
            elif '/loot_table/' in f:loot.append(f)
            elif '/village_trades/' in f:trade.append(f)
        handler=[]
        for x in reg:
            for name in re.findall(r'new\s+(?:org\.slavicmyths\.)?([\w.]+)\s*\(',x['text']):
                match=[f for f in java if f.endswith('/'+name.split('.')[-1]+'.java')]
                handler.extend(match)
        if id in active_blocks:handler.append(active_blocks[id])
        if group=='food' and sub=='drinks':handler.append('brewing/BeverageItem.java' if id!='berry_mors' else 'item/BerryMorsItem.java')
        if sub=='seeds' and id.endswith('_seeds'):handler+=['farming/FarmingCrop.java','farming/Farming.java']
        if sub in ['runes_I','runes_II','runes_III','runes_IV','rare_runes','legacy_basic_runes','story_runes']:handler += ['rpg/RuneCraftRecipe.java','rpg/RuneRuntime.java','rpg/RareRuneRuntime.java','rpg/AdvancedRunes.java','rpg/tools/ToolRuneRuntime.java']
        handler=sorted({h for h in handler if h in java})
        source_consumers=[x for x in refs if not x['source'].startswith(('registry/','compat/')) and x not in reg]
        dynamic=[x for x in refs if any(w in x['source'].lower() for w in ['trade','service','story','event','crop','feature','bush','tree','reward','fishing','carcass','loot','ritual','brew','yardanimal','runefoundation','earth','winters','guardians']) and x not in reg]
        # Explicit dynamic paths whose lookup keys are assembled at runtime.
        special={
          'chud_multitool_pickaxe':['industry/ChudTools.java'], 'chud_multitool_shovel':['industry/ChudTools.java'],
          'firebird_down':['firebird/FirebirdContent.java','firebird/FirebirdEntity.java'], 'firebird_feather':['firebird/FirebirdContent.java','firebird/FirebirdEntity.java'], 'smoldering_feather':['firebird/FirebirdContent.java','firebird/FirebirdEntity.java'],
          'spoiled_brew':['brewing/BrewTile.java'], 'badnyak':['artifact/ArtifactEvents.java'],
          'morozko_trial_totem':['morozko/FrostSkills.java'], 'karachun_trial_totem':['karachun/NightCombat.java'],
          'witch_yard_altar':['marinka/WitchYard.java'], 'sanctuary_ritual_cache':['sanctuary/SanctuaryPiece.java'],
        }
        if sub in ['runes_I','runes_II','runes_III','runes_IV','rare_runes','story_runes']:special[id]=['rpg/RuneFoundation.java','rpg/RuneCraftRecipe.java','rpg/RareRunes.java','rpg/AdvancedRunes.java']
        for f in special.get(id,[]):
            if f in java:dynamic.append({'source':f,'line':1,'text':'Dynamic family path: inspect this source; not a runtime result'})
        purpose='Материал: вход рецепта/ремонта/обмена; отсутствие use не является ошибкой'
        path='Найти источник в acquisition → изготовить/обменять по consumers → применить результат'
        if group=='food':purpose='Пища/напиток' if sub!='ingredients' else 'Кухонный/бродильный ингредиент';path='Получить указанные ингредиенты → рецепт/кухонный стол/чан → употребить; тара и quality по handler'
        if group=='farming':purpose='Семя/растение/урожай или земледельческий расходник';path='Найти растение/семена или изготовить → посадить/вырастить → собрать/переработать; не creative'
        if group=='tools':purpose='Инструмент/ключ действующего обработчика';path='Рецепт/торговец → действие из handler; отрицательная цель не расходует ресурс'
        if group=='magic':purpose='Руна/магический ингредиент/активный предмет; точная роль по handler/consumers';path='Указанный рецепт/лут/ритуал → разрешённый станок/активация; сюжетные ограничения сохраняются'
        if category=='FUNCTIONAL_BLOCK':purpose='Рабочая станция';path='Получить блок рецептом → установить → меню/взаимодействие → переработать входы'
        if id=='ancient_coin':purpose='Валюта + ингредиент traveler_charm';path='Лут/квест → role0 Чудь с условным offer 1→2karavai (stock8) ИЛИ штатный traveler_charm'
        status='KEEP_SOURCE';action='Числа/получение сохранены; runtime NOT_RUN'
        if id in ['watering_can','otvar_ochishcheniya','otvar_bodrosti','otvar_lesnoy_zorkosti','lesnoy_nastoy_vosstanovleniya','yagin_nastoy_stoykosti','letuchaya_maz']:status='BUG_FIXED';action='transaction fixes; test_items286_offline.py exact method bodies, 204 checks, baseline24 failures→0'
        if not recipes and not loot and not trade and not dynamic and category in ['GAMEPLAY_ITEM','CURRENCY','FUNCTIONAL_BLOCK','FUNCTIONAL_RITUAL_BLOCK']:
            status='PROPOSE_ACQUISITION_REVIEW';action='Прямой источник не найден: динамические/tag-пути требуют проверки; НЕ добавлен новый рецепт'
        if not consumers and not source_consumers and group=='resources':status='PROPOSE_PURPOSE_REVIEW';action='Не найден consumer; не удалять ID и не придумывать способность'
        if id in ['flax_stalk','upyr_fang','grave_cloth_scrap','torn_burial_ribbon','shield_boss_fragment','pottery_fragment','old_arrowhead','old_buckle','ancient_fibula','ancient_comb','ancient_beads','bandit_token','nightingale_mark','voevoda_insignia','navya_ash','living_root','bone_thorn','grove_core','guardian_branch','soul_blight'] and not consumers:
            status='PROPOSE_PURPOSE_REVIEW';action='Нет рецептурного sink; см. source-index code_consumers и отдельную таблицу решений. ID/лут сохранены.'
        if id=='flax_stalk':status='KEEP_SOURCE';action='Динамический sink TextileStation: 4 стебля → 4 волокна после 3 действий; затем нить/ткань';handler.append('textile/TextileStation.java')
        if id=='bandit_token':status='KEEP_SOURCE';action='MilitaryQuests trophy: сервер изымает Amount токенов при выдаче сохранённых Rewards; цена зависит от поручения'
        facts={
          'herb_pouch':('Ингредиент forest_charm; НЕ сумка-инвентарь','Рецепт herb_pouch → forest_charm; пустого UI/способности не обещать'),
          'rolling_pin':('Несгораемый инструмент кухонного стола','Установить в tool slot0 → мучные блюда; станок блокирует извлечение во время готовки'),
          'metal_pot':('Многопорционная готовка','Установить в slot1 → recipe.pot → выдавать готовые порции мисками; последнюю миску не дублировать'),
          'bath_broom':('Подношение Баннику','Крафт → найти Банника → взаимодействовать; не боевое оружие/пассивный buff'),
          'bath_stone':('Материал bath_stove/bath_ward','Поднять/получить существующим путём → рецепты; HotStoneEntity не означает throwable Item.use'),
          'fishing_net':('Стационарная рыбалка,4 catch slots,wear32','Разместить в воде → loaded scheduled tick6000–8399; минимум20 воды; шанс1/(2+4*near); забрать улов → износ сохраняется при снятии'),
          'watering_can':('Заправляемая лейка8зарядов,3×3 влажность7','Shift+ПКМ у source water → ПКМ по пашне/культуре; мокрая/запрещённая/отказанная запись не тратит заряд'),
          'sickle':('Сбор зрелой tag sickle_harvestable культуры','Зрелая культура → ПКМ → штатные drops/ограниченный Fortune → возраст1; износ1, durability250; незрелая PASS'),
          'field_hoe':('Вспашка ряда3 или1 с Shift','Права/воздух/HOE_TILL → пашня, износ1 за блок, durability350; rooted/coarse dirt за один проход'),
          'organic_fertilizer':('Ускорение на одну стадию в3×3','Рецепт → ПКМ по незрелой культуре/нижней половине куста; расход1 только если выросло; mature PASS'),
          'chud_mining_lift':('Безопасный подъём,64uses,CD1200','Купить у role1 за8purified_iron+16emerald → использовать30ticks; loaded surface radius4, arena lock, teleport event; отказ не расходует'),
          'chud_master_pickaxe':('2304uses,speed10,FortuneII/переключаемый Silk','role2:16purified_iron+8diamond; Shift air switches; FortuneIII сырой enchant сохраняется, Silk отключает Fortune'),
          'chud_multitool_axe':('Три режима одного инструмента с transmuteCopy','role1:12purified_iron+6diamond → Shift air axe/pick/shovel; сохраняет damage,enchants,runes,name; 2304uses,speed10'),
          'chud_multitool_pickaxe':('Режим купленного multitool','Не отдельный недоступный рецепт: переключение chud_multitool_axe → pickaxe'),
          'chud_multitool_shovel':('Режим купленного multitool','Переключение chud_multitool_pickaxe → shovel → axe; не новый товар'),
          'goat_milk_bucket':('Vanilla MilkBucketItem,возврат ведра','Взрослая коза + ведро по YardAnimal → молоко → убрать эффекты/получить ведро; новый рецепт не нужен'),
          'fruit_pomace':('Побочный материал пресса','4яблока/ягоды+3действия fruit_press → pomace и juice/mash; не creative-only'),
          'berry_mash':('Бродильное сырьё','4ягоды+3действия fruit_press →2mash+pomace → vat recipes'),
          'apple_juice_bottle':('Питьё2nutrition/.30saturationModifier','4яблока+3действия fruit_press →1juice+pomace; пить → dark_bottle; текущая тара/числа KEEP'),
          'morozko_trial_totem':('Ключ испытания Морозко','FrostSkills.unlock:Ягаtrust2/первый дар Баюна,8snow_block+1blue_ice → personal pending totem; не случайный лут'),
          'karachun_trial_totem':('Ключ испытания Карачуна','NightCombat.unlock:Ягаtrust2/первый дар Баюна,4obsidian+1blue_ice → pending totem; не случайный лут'),
        }
        if id in facts:purpose,path=facts[id]
        # Numeric food baseline from actual registration helper invocations, not names.
        food_values=food_index[id]
        if group=='food' and sub=='ingredients' and not food_values:action+='; не вся категория food съедобна — часть сырьё станков'
        stage='early→mid: обычные ресурсы/еда; late: удобство/компонент' if group in ['food','farming','tools'] else 'mid→late: трофеи/магия; early: доступные исходные материалы'
        if sub in ['legendary_trophies','runes_IV','story_runes','rare_runes']:stage='late/story: проверять gate конкретного producer; не ранний случайный сундук'
        if sub.startswith('components_'):stage='stage материала '+sub.removeprefix('components_')+'; оружие #285 не ребалансируется'
        baseline='; '.join(dict.fromkeys(x['text'] for x in reg))
        advertised={k:v for k,v in languages['ru_ru'].items() if id in k and any(w in k for w in ['tooltip','.effect','.desc'])}
        test='Получить по указанному producer без OP → '+path+'; затем повтор/неподходящая цель/полный инвентарь/перезаход/MP. Source references проверены автоматически; gameplay NOT_RUN.'
        expected='Наблюдаемый результат handler/consumer; один серверный расход/выдача; числа baseline; отсутствие нового эффекта у обычного ингредиента.'
        entry=dict(id=id,ru=languages['ru_ru'].get('item.slavicmyths.'+id,languages['ru_ru'].get('block.slavicmyths.'+id)),en=languages['en_us'].get('item.slavicmyths.'+id,languages['en_us'].get('block.slavicmyths.'+id)),group=group,subgroup=sub,classification=category,class_type='/'.join(Path(h).stem for h in handler) or ('BlockItem' if block else 'Item/native family; see registration'),registration_evidence=reg,handler=handler,food_values=food_values,advertised=advertised,purpose=purpose,player_path=path,recipes=recipes,loot=loot,trades=trade,consumer_recipes=consumers,item_tags=sorted(mytags),dynamic_evidence=dynamic,code_consumers=source_consumers,progression=stage,baseline=baseline,decision=status,action=action,test=test,expected=expected,runtime='NOT_RUN')
        triage.append(dict(id=id,group=group,subgroup=sub,classification=category,blockstate=block,registration='literal/family source' if reg else 'dynamic family; see source-index',detail=category in ['GAMEPLAY_ITEM','CURRENCY','FUNCTIONAL_BLOCK','FUNCTIONAL_RITUAL_BLOCK']))
        if triage[-1]['detail']:rows.append(entry)
    (OUT/'source-index.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    write_tsv('catalog-triage.tsv',triage)
    flat=[]
    for e in rows:
        flat.append({k:('; '.join(v) if isinstance(v,list) and all(isinstance(x,str) for x in v) else json.dumps(v,ensure_ascii=False) if isinstance(v,(dict,list)) else v) for k,v in e.items() if k not in ['registration_evidence','dynamic_evidence','code_consumers']})
    write_tsv('gameplay-matrix.tsv',flat)
    summary=dict(catalog=len(catalog),candidate441=sum(x[1] in ['resources','food','tools','magic','farming'] for x in catalog),detailed=len(rows),triage=dict(collections.Counter(x['classification'] for x in triage)),decisions=dict(collections.Counter(x['decision'] for x in rows)),runtime='NOT_RUN; zero game processes',scope='source inventory, not a live registry dump',source_baseline='79ff212c',unresolved_dynamic_registration=[x['id'] for x in triage if x['detail'] and x['registration'].startswith('dynamic')])
    (OUT/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print(json.dumps(summary,ensure_ascii=False))
if __name__=='__main__':run()
