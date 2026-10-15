"""Reproduce the documentation proposal; never writes production resources."""
from pathlib import Path
import collections, hashlib, html, json, subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
RES = ROOT / 'src/main/resources'
JAVA = ROOT / 'src/main/java/org/slavicmyths'
BASE = 'fb2994e958911169317523e1066b8a1ca7a155af'

def read(p): return json.loads(p.read_text(encoding='utf-8'))
def write(name, value): (HERE/name).write_text(value, encoding='utf-8')
def dump(name, value): write(name, json.dumps(value, ensure_ascii=False, indent=2)+'\n')
def cell(s): return str(s).replace('|', '&#124;').replace('\n', ' ')
def link(path, line=None):
    suffix = '#L'+str(line) if line else ''
    return f'[{Path(path).name}](https://github.com/Deserag/slavic-myths/blob/{BASE}/{path}{suffix})'

# Each tuple explicitly specifies the proposed card, including editorial copy.
# Stages are presentation guidance, never additional award prerequisites.
CARDS = '''
root|—|Славянские мифы|Slavic Myths|Впишите свою историю в мир сказаний.|Write your story in a world of tales.|0|task|minecraft:book
group_home|root|У родного огня|By the Hearth|Дом, дружба и доверие.|Home, friendship and trust.|0|task|slavicmyths:hearth
group_knowledge|root|Слово и знак|Word and Sign|Книга, обряды и собственное мастерство.|The book, rituals and your own mastery.|0|task|slavicmyths:lore_book
group_roads|root|Дороги богатырей|Heroes' Roads|От разбойничьего стана до княжеского войска.|From bandit camps to a royal army.|0|task|minecraft:compass
group_water|root|Лес и тихие воды|Woods and Quiet Waters|Узнайте край и выдержите его испытания.|Learn the land and face its trials.|0|task|minecraft:oak_sapling
group_burial|root|Память под землёй|Memory Beneath|Руины, святилища и древние хранители.|Ruins, sanctuaries and ancient guardians.|0|task|slavicmyths:ancient_coin
group_legends|root|На краю сказания|At the Edge of Legend|Великие испытания и редкие чудеса.|Great trials and rare wonders.|0|task|slavicmyths:fern_flower
bind_hearth|group_home|Тепло родного дома|A Hearth of Your Own|Привяжите очаг с помощью Домового.|Bind a hearth with the Domovoy's help.|1|goal|slavicmyths:hearth
friend_of_bayun|group_home|Доверие Баюна|Bayun's Trust|Заслужите дружбу Кота Баюна.|Earn Cat Bayun's friendship.|2|goal|minecraft:cod
trusted_by_yaga|group_home|Доверенный Яги|Yaga's Confidant|Дойдите до доверия Яги в её поручениях.|Earn Yaga's trust through her quests.|2|goal|slavicmyths:lore_book
kitchen_meal|group_home|У деревенского стола|At the Village Table|Заберите приготовленное блюдо из кухни.|Collect a prepared dish from the kitchen.|1|task|minecraft:bowl
linen_craft|group_home|От стебля до полотна|From Stalk to Cloth|Получите льняное полотно.|Obtain linen cloth.|1|task|slavicmyths:linen_cloth
full_yard|group_home|Полный двор|A Full Yard|Разведите гусей, уток и домашних коз.|Breed geese, ducks and domestic goats.|2|goal|minecraft:wheat
first_book_entry|group_knowledge|Открытая книга|An Open Book|Впервые откройте Книгу сказаний.|Open the Book of Tales for the first time.|1|task|slavicmyths:lore_book
bookkeeper|first_book_entry|Хранитель записей|Keeper of Records|Доведите десять записей Книги до полного изучения.|Fully study ten entries in the Book.|2|goal|minecraft:writable_book
first_ritual|group_knowledge|Ответ на дар|An Offering Answered|Успешно завершите обряд у алтаря.|Complete a ritual at an altar.|1|goal|slavicmyths:ancient_sign
three_signs|group_knowledge|Три знака|Three Signs|Установите три руны на одном предмете.|Install three runes on one item.|2|goal|slavicmyths:ancient_sign
rare_accessory|group_knowledge|Из сказки|Out of a Tale|Наденьте редкий сказочный аксессуар.|Equip a rare tale accessory.|2|goal|slavicmyths:warding_charm
own_path|group_knowledge|Свой путь|Your Own Path|Выберите свой первый класс.|Choose your first class.|1|task|minecraft:iron_sword
class_iv|own_path|Имя в предании|A Name in Legend|Достигните 40-го уровня, пройдите испытание и примите класс IV.|Reach level 40, complete a trial and adopt a class IV path.|3|challenge|minecraft:nether_star
bad_people|group_roads|Недобрые люди|Ill Company|Найдите малый или укреплённый лагерь разбойников.|Find a small or medium bandit camp.|1|task|minecraft:crossbow
clear_large_camp|bad_people|Стан обезврежен|The Stronghold Falls|Завершите настоящую зачистку большого стана.|Complete the clearing of a large bandit camp.|2|goal|minecraft:iron_axe
without_ataman|bad_people|Без атамана|Without an Ataman|Победите Атамана; добыча из сундука не считается победой.|Defeat an Ataman; looting a chest is not a victory.|2|goal|slavicmyths:spear
defeat_nightingale|bad_people|Свист умолк|The Whistle Falls Silent|Одержите засчитанную победу над Соловьём-разбойником.|Earn a credited victory over Nightingale the Robber.|3|challenge|slavicmyths:mysterious_black_feather
first_flight|group_roads|Над лесом|Above the Woods|Совершите первый полёт на сказочном транспорте.|Take your first flight on a tale vessel.|2|goal|minecraft:feather
defeat_idolishche|group_roads|Рухнувший исполин|The Fallen Colossus|Заслужите победу над Идолищем Поганым.|Earn victory credit against Idolishche Poganoe.|3|challenge|minecraft:shield
defeat_skim|group_roads|Долина свободна|The Valley Is Free|Заслужите победу над Скимом в его долине.|Earn victory credit against Skim in its valley.|3|challenge|minecraft:bow
defeat_kartaus|group_roads|Чёрное знамя пало|The Black Banner Falls|Завершите встречу с Картаусом вместе с Финистом.|Complete the Kartaus encounter with Finist.|3|challenge|minecraft:black_banner
defeat_kalin|group_roads|Сломленная рать|The Broken Host|Добейтесь засчитанного завершения битвы с Калином.|Earn a successful resolution of the Kalin battle.|3|challenge|minecraft:iron_helmet
all_timber|group_water|От липы до сосны|From Linden to Pine|Соберите древесину всех четырёх местных пород.|Collect logs from all four local tree families.|2|goal|minecraft:oak_log
bog_explorer|group_water|Исследователь топей|Explorer of the Bogs|Посетите все шесть видов болотных построек.|Visit all six kinds of wetland structures.|2|goal|minecraft:lily_pad
break_water_call|group_water|Свой голос|Your Own Voice|Освободитесь от зова Русалки.|Break free of the Rusalka's call.|1|goal|minecraft:nautilus_shell
defeat_leshy|group_water|Лес помнит|The Forest Remembers|Победите Лешего; его редкое сердце не требуется.|Defeat a Leshy; its rare heart is not required.|2|goal|slavicmyths:leshy_heart
defeat_depth_master|group_water|Омут больше не зовёт|The Pool Falls Silent|Заслужите победу над Старшим Водяным.|Earn victory credit against the Elder Vodyanoy.|2|challenge|minecraft:trident
extinguish_the_barn|group_water|Загасить овин|Quench the Barn|Победите Овинника.|Defeat an Ovinnik.|2|goal|minecraft:campfire
silver_remedy|group_water|Серебряное средство|A Silver Remedy|Победите Волколака серебряным оружием.|Defeat a Volkolak with a silver strike.|2|goal|minecraft:iron_sword
curse_resolved|group_water|Разорванное проклятие|The Curse Unbound|Верните подлинный череп и завершите историю Проклятого волколака.|Return the authentic skull and resolve the Cursed Volkolak story.|3|challenge|slavicmyths:cursed_volkolak_skull
night_watch|group_water|До рассвета|Until Dawn|Победите Ночницу в честном бою.|Defeat a Nochnitsa in legitimate combat.|2|goal|minecraft:clock
ancient_find|group_burial|Отголосок старины|An Echo of Old|Получите древнюю монету или осколок идола.|Obtain an ancient coin or idol fragment.|1|task|slavicmyths:ancient_coin
traces_of_past|group_burial|Следы прошлого|Traces of the Past|Найдите любую из шести болотных построек.|Find any of the six wetland structures.|1|task|minecraft:mossy_stone_bricks
found_two_sanctuaries|group_burial|Огонь и вода|Fire and Water|Откройте оба вида древних святилищ.|Discover both kinds of ancient sanctuary.|2|goal|minecraft:ender_eye
claimed_fire_fragment|found_two_sanctuaries|Очищенное пламя|A Flame Renewed|Заберите свой огненный фрагмент после очищения хранителя.|Claim your fire fragment after the guardian is cleansed.|3|challenge|slavicmyths:mor_fire_amulet_fragment
claimed_water_fragment|found_two_sanctuaries|Источник вновь чист|The Spring Runs Clear|Заберите свой водный фрагмент после очищения хранителя.|Claim your water fragment after the keeper is cleansed.|3|challenge|slavicmyths:mor_water_amulet_fragment
earth_heart|group_burial|Сердце земли|Heart of the Earth|Заслужите победу над Духом Земли.|Earn victory credit against the Earth Spirit.|3|challenge|minecraft:emerald
kurgan_rest|group_burial|Покой кургана|Rest for the Barrow|Завершите бой с главным хранителем кургана.|Complete the battle against a barrow's principal guardian.|3|challenge|minecraft:bone
depth_champions|group_burial|Подземные стражи|Wardens Below|Победите Кладовика или Большого подземника в их встрече.|Defeat a Kladovik or Large Podzemnik in its encounter.|2|goal|minecraft:raw_gold
name_your_misfortune|group_legends|Назови своё несчастье|Name Your Misfortune|Заслужите победу над Лихом Одноглазым.|Earn victory credit against Likho the One-Eyed.|3|challenge|slavicmyths:odnoglazyy_obereg
steppe_champion|group_legends|Степной богатырь|Champion of the Steppe|Заслужите победу над Тугарином.|Earn victory credit against Tugarin.|3|challenge|slavicmyths:poyas_tugarina
endured_frost|group_legends|Выдержавший стужу|One Who Endured the Frost|Завершите испытание Морозко; его смерть не требуется.|Complete Morozko's trial; killing him is not required.|3|challenge|minecraft:blue_ice
endured_last_night|group_legends|Последняя ночь|The Last Night|Завершите испытание Карачуна.|Complete Karachun's trial.|3|challenge|minecraft:coal
great_poloz_fall|group_legends|Падение Великого Полоза|Fall of the Great Poloz|Заслужите победу над Великим Полозом.|Earn victory credit against the Great Poloz.|3|challenge|minecraft:gold_ingot
defeat_gorynych|group_legends|Три головы, одна воля|Three Heads, One Will|Доведите настоящую встречу с Горынычем до полного очищения.|Bring a real Gorynych encounter to full purification.|3|challenge|slavicmyths:serpent_seal
defeat_marinka|group_legends|Чары рассеяны|The Spell Is Broken|Заслужите победу над Маринкой в ведьмином дворе.|Earn victory credit against Marinka in the witch yard.|3|challenge|minecraft:amethyst_shard
fern_flower|group_legends|Несбыточный цветок|The Impossible Flower|Обретите цветок папоротника.|Obtain a fern flower.|3|goal|slavicmyths:fern_flower
falling_star|group_legends|Падающая звезда|A Falling Star|Пусть звёздный оберег спасёт вас от опасного падения.|Let the star charm save you from a dangerous fall.|2|goal|minecraft:glowstone_dust
'''

def cards():
    result=[]
    for line in CARDS.strip().splitlines():
        id,parent,ru,en,dr,de,stage,frame,icon=line.split('|')
        section=id=='root' or id.startswith('group_')
        result.append(dict(id=id,parent=None if parent=='—' else parent,title_ru=ru,title_en=en,description_ru=dr,description_en=de,stage=int(stage),frame=frame,icon=icon,show_toast=not section,announce_to_chat=frame=='challenge',hidden=False))
    return result

# Families explain disappearance of cards, not automatic grant of successor criteria.
FAMILIES = [
 ('meet_leshy leshy_heart','defeat_leshy','Одна победа вместо ПКМ/боя/RNG-трофея; сердце остаётся фактом Книги.'),
 ('find_small_camp find_medium_camp find_large_camp disperse_bandits stolen_goods meet_nightingale black_feather','bad_people','Мелкие обнаружения/сундук/перо остаются в Книге; отдельны зачистка, Атаман и Соловей.'),
 ('meet_domovoy offer_domovoy friend_domovoy','bind_hearth','Один итог отношений — связанный очаг; ранний разговор и разовый дар без медалей.'),
 ('birch_bark birch_bark_scroll lore_book lore_keeper','first_book_entry','Материалы и владение книгой не равны её освоению; остаются открытие и десять полных записей.'),
 ('flax linen_thread','linen_craft','Сырьё и промежуточная нить объединены в результат текстильного ремесла.'),
 ('wormwood berry_garden garden_harvest new_seeds winter_stores village_feast generous_table first_brew too_much','kitchen_meal','Повседневные ингредиенты/еда/напитки остаются процессом, не серией уведомлений.'),
 ('full_yard','—','RETIRE: разведение сохраняет vanilla-прогресс и запись; отдельной медали мода не нужно.'),
 ('explore_swamp_hut explore_abandoned_settlement explore_bog_causeway explore_flooded_shrine explore_fishing_camp explore_underwater_ruins','traces_of_past','Оставить первый след и общий итог всех шести, без медали на каждую постройку.'),
 ('woodlands','all_timber','Одна коллекционная веха за четыре породы; OR первого бревна не считать ошибкой.'),
 ('meet_vodyanoy meet_rusalka water_call depth_clue find_deep_pool wake_depth_master depth_gift new_catch net_catch','defeat_depth_master','Знакомство/рыбалка/призыв/подарок — факты энциклопедии; не доказательства победы.'),
 ('rune_installed reforged learn_armorer armorer_craft','three_signs','Освоение рун вместо трёх соседних наград за операции стола.'),
 ('found_ancient_sanctuary','found_two_sanctuaries','Одна обзорная веха за оба типа; не награждать за каждый шаг маршрута.'),
 ('restored_sanctuary_fire','claimed_fire_fragment','Ремонт сердца и бой хранителя различны; первая карточка убирается, никакого переноса победы.'),
 ('cleansed_sanctuary_pool','claimed_water_fragment','Очищение сердца не доказывает победу хранителя; факты остаются независимыми.'),
 ('first_accessory two_rings warding_charm','rare_accessory','Одна значимая экипировка вместо награды за каждый слот/промежуточный оберег.'),
 ('find_the_hut guest_of_yaga not_empty_handed follow_the_thread cauldron_service','trusted_by_yaga','Поручения Яги сохранены целиком; медаль за итог доверия, не каждую кнопку.'),
 ('meet_bayun','friend_of_bayun','Первое знакомство остаётся записью; награда за заслуженную дружбу.'),
 ('old_weapons perunite thunder_stone storm_staff thunder_axe ancient_sign shrine altar','first_ritual','Материалы/первая экипировка/постройка — этапы обряда и Книги, не отдельные трофеи.'),
 ('forest_guardian self_playing_music table_is_set not_today full_speed_ahead falling_star ill_fate_broken','rare_accessory','Артефактная коллекция вместо отдельных срабатываний; карточка описывает экипировку, не победу.'),
 ('hunt_begins first_hunt trophy_keeper wildlife_encounter boar_charge_block','—','RETIRE: старт охоты/мясо/сумка/обычная фауна не крупные вехи; Книга и механики сохраняются.'),
 ('fire_in_the_sky catch_the_wind bottled_elements','—','RETIRE: стихийная охота остаётся в Книге и наградах; не выдавать по медали на промежуточный ресурс.'),
 ('amber','ancient_find','Рядовая янтарная находка не отдельный трофей; монета/идол остаются самостоятельным OR.'),
 ('group_depths','group_burial','Слить древние глубины с археологией.'),
 ('group_forest','group_water','Слить лес и воды в единый природный край.'),
 ('group_night','group_legends','Включить ночные испытания в великие сказания.'),
]

SPECIAL = {
 'root':('KEEP','Корень одного дерева; шесть ветвей вместо пятнадцати прямых детей.'),
 'defeat_leshy':('RELOCATE','Единственная награда Лешего; нет требования сердца или предварительного ПКМ.'),
 'defeat_depth_master':('REWORK','Сохранить done, добавить проверку заслуги: нынешняя выдача всем в радиусе24 не доказывает участие.'),
 'great_poloz_fall':('REWORK','Убрать spoiler-hidden; сохранить victory и старые выдачи, новый показ ядра не должен выдавать победу.'),
 'claimed_fire_fragment':('REWORK','Один итог огненного хранителя; сначала #25 исправляет claim/event; сверить право участника отдельно.'),
 'claimed_water_fragment':('REWORK','Один итог водного хранителя; #25 исправляет claim/event; не подменять победой простое восстановление сердца.'),
 'without_ataman':('REWORK','Сохранить значимую победу Атамана, отделить её от общего исследования и зачистки; добавить native eligibility.'),
 'name_your_misfortune':('REWORK','Перенести в легенды; текущий source Player не исключает creative/debug и не описывает кооп.'),
 'steppe_champion':('REWORK','Тугарин уже присутствует; уточнить server credit и переоформить, без новой награды за пояс.'),
 'defeat_nightingale':('RELOCATE','Содержательная победа отдельно от визита и пера; не требовать Атамана.'),
 'first_book_entry':('RELOCATE','Первое реальное открытие книги вместо медалей за бересту и владение предметом.'),
 'three_signs':('RELOCATE','Оставить весомый результат рун, не раннюю вставку и перековку.'),
}

def source_snapshot():
    # PR25 dispatch is an index only; verify resources and resolve evidence against main.
    upstream=json.loads(subprocess.check_output(['git','show','origin/pr25:docs/implementation/advancements-micro-patch/AUDIT.json'],cwd=ROOT))
    hints={n['id']:n['dispatch'] for n in upstream['nodes']}
    langs={code:read(RES/f'assets/slavicmyths/lang/{code}.json') for code in ['ru_ru','en_us']}
    rows=[]
    for f in sorted((RES/'data/slavicmyths/advancement').rglob('*.json')):
        id='slavicmyths:'+f.relative_to(RES/'data/slavicmyths/advancement').with_suffix('').as_posix()
        data=read(f); d=data.get('display',{}); refs=[]; missing=[]
        for path in sorted(set(hints.get(id,[]))):
            content=(ROOT/path).read_text(encoding='utf-8')
            matches=[i for i,line in enumerate(content.splitlines(),1) if '"'+id.split(':')[1]+'"' in line]
            refs += [dict(path=path,line=i) for i in matches]
            if not matches: missing.append(path)
        rows.append(dict(id=id,path=f.relative_to(ROOT).as_posix(),sha256=hashlib.sha256(f.read_bytes()).hexdigest(),recipe='/recipes/' in id.replace(':','/'),title_ru=langs['ru_ru'].get(d.get('title',{}).get('translate'),d.get('title',{}).get('text','')),title_en=langs['en_us'].get(d.get('title',{}).get('translate'),d.get('title',{}).get('text','')),resource=data,dispatch=refs,pr25_dispatch_absent_in_main=missing))
    return rows

def trigger_summary(n):
    data=n['resource']; parts=[]
    for name,c in data['criteria'].items():
        t=c['trigger'].removeprefix('minecraft:'); cond=c.get('conditions')
        if cond:
            # Full predicates are losslessly retained in SOURCE.json.
            vals=[]
            def walk(x):
                if isinstance(x,dict):
                    for k,v in x.items():
                        if k in ('items','type','structures') and isinstance(v,(str,list)): vals.append(json.dumps(v,ensure_ascii=False,separators=(',',':')))
                        else: walk(v)
                elif isinstance(x,list):
                    for y in x: walk(y)
            walk(cond)
            t+='('+','.join(vals)+')'
        parts.append(name+'='+t)
    req=data.get('requirements',[[k] for k in data['criteria']])
    return '; '.join(parts)+'; '+ ' AND '.join('('+' OR '.join(g)+')' for g in req)

def main():
    if subprocess.run(['git','diff','--quiet',BASE,'--','src','build.gradle','gradle.properties'],cwd=ROOT).returncode:
        raise SystemExit('Production differs from the audited baseline; do not overwrite the source audit.')
    allrows=source_snapshot(); rows=[n for n in allrows if not n['recipe']]; visible=cards(); selected={n['id']:n for n in visible}
    assert len(rows)==129 and len(allrows)==206
    assert len(visible)==55
    families={}
    for ids,succ,reason in FAMILIES:
        for id in ids.split(): families[id]=(succ,reason)
    decisions=[]
    for n in rows:
        id=n['id'].split(':')[1]
        if id in selected:
            card=selected[id]
            action,reason=SPECIAL.get(id,('RELOCATE' if card['parent']!=n['resource'].get('parent','').split(':')[-1] else 'KEEP','Сохранить самостоятельный результат; подача и положение приведены к общей главе.'))
            successor=id
        else:
            assert id in families, id
            successor,reason=families[id]; action='RETIRE' if successor=='—' else 'MERGE'
        decisions.append(dict(id=id,decision=action,reason=reason,successor=None if successor=='—' else successor,display=id in selected,migration='M0' if id in selected else 'M1',test='T-BOSS' if any(s in id for s in ['defeat','endured','poloz','earth_heart','ataman','champion','misfortune','claimed_']) else 'T-FACT'))
    assert all(n['successor'] is None or n['successor'] in selected for n in decisions)
    dump('SOURCE.json',dict(base=BASE,pr25_dispatch_index='e2ca8a8f998bb390cd6538f8979a2df13da006cc',nodes=allrows))
    dump('DECISIONS.json',decisions)
    dump('TREE.json',visible)
    write('DECISIONS.md','# Полная ведомость 129 игровых узлов\n\nВсе ID ниже имеют namespace `slavicmyths:`. Точные predicates, rewards и исходный display каждого узла: [SOURCE.json](SOURCE.json). Каждый successor — **редакторское объединение**, не правило выдачи; M1 не выдаёт successor. M0: прежний ID/criteria/rewards и partial progress сохраняются; M1: то же, но без display. T-FACT/T-BOSS описаны в [MIGRATION.md](MIGRATION.md).\n\n| ID | RU / EN сейчас | trigger / requirements | parent сейчас | category / boss-stage | решение | причина | successor | server dispatch в main | old-save | test |\n|---|---|---|---|---|---|---|---|---|---|---|\n'+'\n'.join('| '+' | '.join(map(cell,[d['id'],n['title_ru']+' / '+n['title_en'],trigger_summary(n),n['resource'].get('parent','—'),n['resource'].get('parent','root').split(':')[-1]+'; '+('boss/trial' if d['test']=='T-BOSS' else 'fact / '+str(selected.get(d['id'],{}).get('stage','legacy'))),d['decision'],d['reason'],d['successor'] or '—',('; '.join(link(r['path'],r['line']) for r in n['dispatch']) or ('НЕТ award в main; дефект #25' if any(c['trigger']=='minecraft:impossible' for c in n['resource']['criteria'].values()) else 'vanilla trigger; внешнего dispatch нет'))+ ('; дополнительные места dispatch есть только в #25' if n['pr25_dispatch_absent_in_main'] else ''),d['migration'],d['test']+':'+d['id']]))+' |' for n,d in zip(rows,decisions))+'\n')
    write('TREE_TABLE.md','# Полный предложенный display-контракт\n\nЭто проект, не установленные достижения. Все 55 карточек (включая root и 6 разделов) перечислены ниже. `hidden=false` у всех; `task/goal/challenge` — рамки. Все иконки — существующие предметы. RU/EN descriptions — буквальный текст будущих подсказок. Условия старых узлов точно приведены в SOURCE/DECISIONS, изменения и новые события — в NEW_TREE.\n\n| ID | Parent | RU / EN | Подсказка RU / EN | этап | frame | icon | toast / chat / hidden |\n|---|---|---|---|---|---|---|---|\n'+'\n'.join('| '+' | '.join(map(cell,[n['id'],n['parent'] or '—',n['title_ru']+' / '+n['title_en'],n['description_ru']+' / '+n['description_en'],n['stage'],n['frame'],n['icon'],f"{n['show_toast']} / {n['announce_to_chat']} / {n['hidden']}"]))+' |' for n in visible)+'\n')
    # Six consistent lanes, explicitly showing every real edge, with readable labels.
    lanes=[c for c in visible if c['id'].startswith('group_')]
    byid={c['id']:c for c in visible}
    def lane(n):
        while n['parent'] and not n['id'].startswith('group_'): n=byid[n['parent']]
        return n['id']
    svg=['<svg xmlns="http://www.w3.org/2000/svg" width="2160" height="1730" viewBox="0 0 2160 1730">','<rect width="2160" height="1730" fill="#101b21"/>','<style>text{font-family:Segoe UI,Arial,sans-serif;fill:#f0eadb} .small{font-size:12px;fill:#bac9cb} .name{font-size:17px;font-weight:600}</style>','<text x="60" y="52" font-size="28">Славянские мифы — проект 55 узлов / 6 ветвей</text>','<text x="60" y="82" class="small">DOCUMENTATION PROPOSAL · не игровой скриншот · линии = визуальный parent, не условие выдачи</text>']
    colors=['#e2b26b','#85bdd0','#d49776','#85b78a','#b9a5d2','#e1ca7a']
    positions={'root':(1050,120)}
    svg+=['<rect x="880" y="103" width="340" height="54" rx="10" fill="#263a43" stroke="#e2b26b"/>','<text x="1050" y="136" text-anchor="middle" class="name">Славянские мифы</text>']
    for idx,(group,color) in enumerate(zip(lanes,colors)):
        x=30+idx*355; y=210; positions[group['id']]=(x+170,y+24)
        svg += [f'<path d="M1050 157 V180 H{x+170} V210" fill="none" stroke="{color}" opacity=".5"/>',f'<rect x="{x}" y="{y}" width="340" height="54" rx="9" fill="#22353c" stroke="{color}"/>',f'<text x="{x+170}" y="{y+32}" text-anchor="middle" class="name">{html.escape(group["title_ru"])}</text>']
        children=[n for n in visible if not n['id'].startswith('group_') and n['id']!='root' and lane(n)==group['id']]
        for j,n in enumerate(children):
            y=300+j*115; positions[n['id']]=(x+170,y+30)
            inset=12 if n['parent']!=group['id'] else 0
            svg += [f'<rect x="{x+inset}" y="{y}" width="{340-inset}" height="90" rx="{3 if n["frame"]=="challenge" else 10}" fill="#1c2b33" stroke="{color}" stroke-width="{3 if n["frame"]=="challenge" else 1}"/>',f'<text x="{x+20+inset}" y="{y+30}" class="name">{html.escape(n["title_ru"])}</text>',f'<text x="{x+20+inset}" y="{y+53}" class="small">{n["id"]}</text>',f'<text x="{x+20+inset}" y="{y+74}" class="small">Этап {n["stage"]} · {n["frame"]} · parent: {n["parent"]}</text>']
            py=positions[n['parent']][1]
            svg += [f'<path d="M{x+8} {py+23} V{y+10} H{x+inset+15}" fill="none" stroke="{color}" opacity=".45"/>']
    svg+=['<text x="60" y="1650" class="small">55 карточек = 1 корень + 6 разделов + 48 вех. 77 recipe unlocks сохраняются отдельно; остальные прежние ID — data-only.</text>','<text x="60" y="1680" class="small">Золотая/цветная толстая рамка: challenge. Ни одна победа не требует убийства другого нерелевантного босса.</text>','</svg>']
    write('TREE.svg','\n'.join(svg))
    write('AUDIT.md',(HERE/'AUDIT_NOTES.md').read_text(encoding='utf-8')+'\n\n'+(HERE/'BOSS_COVERAGE.md').read_text(encoding='utf-8')+'\n\n'+(HERE/'DECISIONS.md').read_text(encoding='utf-8'))
    write('NEW_TREE.md',(HERE/'TREE_NOTES.md').read_text(encoding='utf-8')+'\n\n'+(HERE/'TREE_TABLE.md').read_text(encoding='utf-8'))
    print(json.dumps(dict(gameplay=len(rows),recipes=len(allrows)-len(rows),proposed_visible=len(visible),old_visible=sum(d['display'] for d in decisions),new=sum(n['id'] not in {d['id'] for d in decisions} for n in visible),decisions=dict(collections.Counter(d['decision'] for d in decisions))),ensure_ascii=False))

if __name__=='__main__': main()
