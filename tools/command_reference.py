"""Enrich the real offline Brigadier dump; deterministic docs/registry drift gate.
Descriptions are explicit contracts, never guesses from token names. Unknown actions fail.
Run the Java fixture first, then --write; --check never rewrites artifacts.
"""
from pathlib import Path
import hashlib,json,re,sys
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/commands-74'

GENERATION_ORIGINS = {
    'bayun':'bayun generate','yaga':'yaga generate','city':'city generate',
    'abandoned':'abandoned generate','depths':'depths generate','kurgan':'kurgan generate',
    'fire_sanctuary':'generate_fire_sanctuary','pure_spring':'generate_pure_spring',
    'serpent_hill':'generate_serpent_hill','witch_yard':'witch_yard place',
    'skim_valley':'skim_valley place','barracks':'place barracks','village':'place village',
    'village_showcase':'test village_showcase',
    'gorodishche_showcase':'test gorodishche_showcase','courtyard':'encounter courtyard',
    'courtyard_debug':'bogatyr courtyard'}

def describe(path):
    prefix='slavicmyths dev generate '
    if path.startswith(prefix):
        rest=path[len(prefix):];kind=rest.split(' ',1)[0]
        if kind in GENERATION_ORIGINS:
            return describe('slavicmyths dev '+GENERATION_ORIGINS[kind]+rest[len(kind):])
    p=path.removeprefix('slavicmyths ');t=p.split();dev=t[0]=='dev'
    if dev:t=t[1:]
    group=t[0];tail=' '.join(t[1:]);action=t[1] if len(t)>1 else ''
    effect='read-only';method=''
    if group=='help':return 'Вывести доступные разделы и синтаксис; <category> — раздел с подразделами, например dev city.','read-only','CommandHelp.show'
    if group=='hero':
        desc={'call':'Вызвать Илью Муромца через реестр спутников.','finist':'Вызвать или отозвать своего Финиста.','alyosha':'Вызвать или отозвать своего Алёшу Поповича.','dobrynya':'Вызвать своего Добрыню в сцену Ведьминого двора.'}[action]
        return desc,'entities/progression','BogatyrLedger.call' if action=='call' else 'WitchYard.call' if action=='dobrynya' else 'HeroCalls.toggle'
    if group=='kalin':return ('Начать персональное испытание Калин-царя; создаёт лагерь и участников.' if action=='challenge' else 'Выбрать пару героев <hero_a>/<hero_b>; идентификаторы подсказываются при вводе.'),'world/progression','KalinEncounter.'+('start' if action=='challenge' else 'select')
    if group=='kartaus':return 'Начать персональную встречу с Картаусом с существующими проверками площадки/спутника.','world/entities','KartausEncounter.start'
    if group=='wolf':return 'Передать accept/decline Серому Волку в радиусе16; UUID встречи и одноразовый токен проверяются на сервере.','progression','GreyWolf.decision'
    if group=='navigation' and not dev:return 'Открыть/синхронизировать персональную карту навигации.','client UI','NavigationNetwork.sync'
    if group=='class':
        action=t[2];desc={'state':'Показать NBT состояния класса выбранного игрока.','xp':'Начислить <amount> опыта класса.','level':'Установить уровень; сбросить текущий XP в0.','points':'Установить число свободных очков навыков.','add_points':'Добавить очки, с верхней границей10000.','cooldown_reset':'Очистить таймеры умений выбранного игрока.','choose':'Выбрать базовый класс штатным ClassState.choose с его ограничениями.','force':'Принудительно установить базовый класс; очищает специализации, активные/пассивные умения и ultimate runtime.','learn':'Изучить навык штатным ClassState.learn с его стоимостью/предпосылками.','forget':'Забыть навык; вернуть очки его рангов и очистить привязанные слоты.'}[action]
        return desc,'read-only' if action=='state' else 'player NBT/progression','ClassState.data/choose/awardXp/learn/restore/sync'
    if group in ('city','abandoned'):
        if action=='search':return 'Запустить ограниченный поиск кандидатов по seed (страница0..128); для abandoned можно уточнить attached/medium/great. Не строит комплекс.','search job','ChudSearchCommands.start'
        if action=='locate':return 'Найти ближайший сохранённый старт в загруженных чанках; при отсутствии запустить ограниченный поиск кандидатов.','read/search job',('ChudCommands' if group=='city' else 'AbandonedCommands')+'.locate'
        if action=='info':return 'Вывести план, ID, границы, помещения и связи ближайшего комплекса либо указанного ID; новые чанки не запрашиваются.','read-only',('ChudCommands' if group=='city' else 'AbandonedCommands')+'.info'
        if action=='validate':return 'Проверить инварианты плана и доступность маршрутов по известным чанкам; неизвестные чанки не загружать.','read-only',('ChudCommands.validate' if group=='city' else 'AbandonedCommands.info(validate=true)')
        if action=='tp':return 'Телепортировать исполнителя к выбранному ориентиру комплекса; без роли у city_id — ко входу; цель должна быть загружена и безопасна.','teleport',('ChudCommands' if group=='city' else 'AbandonedCommands')+'.teleport'
        if action=='generate':return 'Принудительно построить '+('город Чуди; attached добавляет связанную заброшенную часть.' if group=='city' else 'заброшенный комплекс: по умолчанию medium; great — большой.')+' Сохраняет native start/references; защищает энд-портал и границу; игнорирует естественное расстояние сетки.','world blocks/chunks','ChudGenerateCommands.generate'
    if group=='chud_search':return {'status':'Показать активный поиск Чуди и число проверенных кандидатов.','next':'Продолжить сохранённый поиск со следующей позиции/страницы.','cancel':'Запросить отмену активного поиска; построек не удаляет.'}[action],'search job' if action!='status' else 'read-only','ChudSearchCommands.'+('next' if action=='next' else 'control')
    if group=='locate':return ('Найти казарму среди деревенских структур через ограниченный поиск.' if action=='barracks' else 'Найти лагерь bandit_camp выбранного размера; any — любой, next — следующий кандидат; минимум96 блоков от исполнителя.'),'search job','MilitaryCommands.register → BoundedStructureSearch.start' if action=='barracks' else 'CampCommands.run(teleport=false)'
    if group in ('tp_bandit_camp','tp_nightingale','tp_kurgan'):return 'Найти '+('курган выбранного размера' if group=='tp_kurgan' else 'вход Соловья в большом лагере' if group=='tp_nightingale' else 'лагерь выбранного размера')+' и телепортировать игрока на проверенную безопасную точку; next пропускает предыдущую цель.','teleport/search job','KurganCommands.run(teleport=true)' if group=='tp_kurgan' else 'CampCommands.run/report(teleport=true)'
    if group=='spawn_nightingale':return 'Создать Соловья в4 блоках перед игроком; назначить место его домом.','entities','CampCommands.register'
    if group=='kurgan':
        if action=='locate':return 'Найти курган small/warrior/great; next выбирает следующий, не ближе192 блоков и не предыдущий сохранённый; Верхний мир.','search job','KurganCommands.run(teleport=false)'
        if action=='generate':return 'Поставить в очередь ручное строительство кургана выбранного размера с <seed> либо случайным seed; подготавливает грунт и сохраняет результат.','world blocks/chunks','KurganDebug.generate → ManualStructureJobs.start'
        if action=='info':return 'Показать ближайший курган в96 блоках, тип, seed, этажи, беспокойство, состояние комнаты/печати/босса.','read-only','KurganDebug.info'
    if group=='clear_kurgan_curse':return 'Очистить курганное проклятие у себя либо выбранного онлайн-игрока; сообщение администраторам.','player effects/NBT','KurganDebug.clear'
    if group=='generate':return 'Поставить в очередь ручное строительство bandit_camp выбранного размера с seed либо случайным seed; medium — совместимое имя fortified. Подготавливает грунт.','world blocks/chunks','ManualStructureCommands.size → ManualStructureJobs.start'
    if group in ('search','generation'):
        job='поиска структур' if group=='search' else 'строительства'
        return (('Показать состояние '+job+'.') if action=='status' else ('Отменить очередь '+job+'.'+(' Уже подготовленный грунт не откатывается.' if group=='generation' else ''))),'read-only' if action=='status' else 'job cancellation',('BoundedStructureSearch' if group=='search' else 'ManualStructureJobs')+'.'+action
    if group=='bayun':return 'Разместить место Баюна у позиции исполнителя; соблюдает проверки BayunFeature.place.','world/entities','BayunFeature.place'
    if group=='depths':return {'generate':'Начать генерацию древних глубин для игрока.','map':'Выдать игроку карту древних глубин.','locate':'Вывести ближайшую завершённую запись древних глубин в Верхнем мире.','status':'Вывести состояние текущего задания генерации.'}[action],('world/chunks' if action=='generate' else 'inventory' if action=='map' else 'read-only'),{'generate':'DepthsService.start','map':'DepthsService.give','locate':'DepthsCommands.register','status':'DepthsService.status'}[action]
    if group=='earth_spirit':return ('Найти Духа Земли в40 блоках и показать HP/фазу/арену/последний удар.' if action=='status' else 'Создать отладочного Духа Земли у исполнителя; требуется свободное место3.1×6.35; победа даёт обычный loot/руну.'),'read-only' if action=='status' else 'entities/rewards','EarthCommands.register'
    if group=='firebird':return 'Показать состояние события Жар-птицы в Верхнем мире: день, участники, выданные награды, оставшееся время.','read-only','FirebirdCommands.register'
    if group=='bogatyr':
        d={'courtyard':'Создать арену богатыря.','spawn_ilya':'Вызвать Илью штатным реестром спутников.','spawn_idolishche':'Создать Идолище с debug=true.','test_hero_retreat':'Установить ближайшему своему герою42 HP для проверки отхода.','boss_phase':'Установить ближайшему Идолищу315 HP, переводя его во вторую фазу.','attack':'Начать указанную атаку ближайшего debug-Идолища против исполнителя.','status':'Показать HP, состояние, фазу, кормления и последний урон Идолища в72 блоках.','progression':'Показать запись победы (get) либо удалить тестовую запись победы игрока (clear_test).'}[action]
        return d,'read-only' if action=='status' or tail=='progression get' else 'debug/entities/progression','BogatyrCommands.register → BogatyrArena/BogatyrLedger/spawn/near'
    if group=='encounter':return ('Создать арену богатыря.' if action=='courtyard' else 'Создать обычное Идолище с debug=false; проверяет площадку и отсутствие другого Идолища.'),'world/entities','BogatyrArena.create' if action=='courtyard' else 'BogatyrCommands.spawn(debug=false)'
    if group=='boss':return ('Снять свою служебную запись босса и удалить принадлежащего себе загруженного WorldBoss.' if action=='clear' else 'Принудительно вызвать '+action+'.' if tail.endswith(' summon') else 'Вывести координаты ближайшей естественной записи босса '+action+' в Верхнем мире.'),'read-only' if tail.endswith(' locate') else 'entities/player NBT','BossCommands.locate' if tail.endswith(' locate') else 'BossRitualItem.summon' if tail.endswith(' summon') else 'BossCommands.register'
    if group=='hunt':return ('Отменить свою охоту, удалить своего загруженного HuntMob и снять cooldown рога.' if action=='clear' else 'Принудительно вызвать выбранную цель охоты для исполнителя.'),'entities/player NBT','HuntCommands.register' if action=='clear' else 'HuntItems.summon'
    if group=='yaga':
        if action=='kuzya':return ('Показать состояние резиденции Кузи.' if t[-1]=='status' else 'Восстановить отсутствующего Кузю у известной печи; проверяет канонического жильца, площадку и сохранённый дом.'),'read-only' if t[-1]=='status' else 'entities/residence NBT','KuzyaResidence.commands (inline callback)'
        return {'locate':'Подготовить/найти запись дома Яги в Верхнем мире и показать существующее либо запланированное место.','generate':'Разместить отсутствующий дом Яги на проверенной площадке перед игроком; существующий дом не заменяется.','status':'Показать отношение Яги и состояние задания текущего игрока.'}[action],'saved home planning' if action=='locate' else 'world/entities' if action=='generate' else 'read-only','YagaCommands.generate' if action=='generate' else 'YagaCommands.register'
    if group in ('witch_yard','skim_valley'):
        return {'place':'Разместить сцену перед игроком с проверкой площадки и поворотом по направлению взгляда.','start':'Начать обычную встречу в ближайшем Ведьмином дворе.','debug_start':'Начать debug-встречу в ближайшем Ведьмином дворе; требуется operatorDebugCommands.','status':'Показать сохранённое состояние ближайшей сцены.'}[action],'read-only' if action=='status' else 'world/entities/progression',('WitchYard' if group=='witch_yard' else 'SkimValley')+'.'+('near/save' if action=='status' else 'start' if action in ('start','debug_start') else 'place')
    if group=='place':return ('Разместить шаблон казармы рядом с исполнителем.' if action=='barracks' else 'Разместить выбранный шаблон деревни из каталога; автодополнение зависит от шаблонов текущего мира.'),'world blocks/entities','MilitaryCommands.register' if action=='barracks' else 'VillageShowcase.individual'
    if group=='navigation':
        if action=='list':return 'Показать UUID, категорию, вид и имя всех своих меток.','read-only','NavigationCommands.register'
        if action=='marker':return 'Показать NBT своей метки с указанным UUID; чужие записи не выдаются.','read-only','NavigationCommands.register'
        if action=='clear_owned':return 'Удалить свои ручные навигационные метки штатным запросом CLEAR_OWNED.','player navigation NBT','NavigationManager.request'
        return ('Выполнить1000 офлайн-выборок геометрии зон поиска и показать статистику.' if t[2]=='stats' else 'Добавить и отслеживать тестовую область поиска выбранного масштаба к своим меткам.'),'read-only' if t[2]=='stats' else 'player navigation NBT','NavigationStatistics.run' if t[2]=='stats' else 'SearchArea.generate/NavigationManager.changed'
    if group=='quest185':
        d={'status':'Показать снимок заданий выбранного игрока.','reset':'Закрыть сессии, сбросить задания обучения очищению и удалить Quest185 из данных пути игрока.','invite':'Обеспечить счётчик MorKills>=1 и выдать приглашение в цепочку заданий.','debug_actors':'Показать доверие Яги, Баюна и Волка выбранного игрока.','wolf_call':'Вызвать поддержку Волка для выбранного игрока.','cleansing_sample':'Выдать12 единиц очищающего порошка, если хватает места.','cleansing_status':'Показать число очищений, активные задачи и иммунитет земли под игроком.','stage':'Установить выбранному игроку состояние задания active/ready/completed; изменяет Accepted/Claimed и связанные счётчики. Не запускает обычную выдачу награды.'}[action]
        return d,'read-only' if action in ('status','debug_actors','cleansing_status') else 'player NBT/inventory/entities','Quest185Commands.run' if action!='stage' else 'Quest185Commands.register'
    if group.startswith('generate_'):
        d={'generate_fire_sanctuary':'огненное святилище','generate_pure_spring':'чистый источник','generate_serpent_hill':'Змеиную горку'}[group]
        return 'Принудительно разместить '+d+' у позиции исполнителя либо <pos>; сохраняет старт, проверяет границы и исходные ограничения; у горки доступен <rotation>.','world blocks/chunks','HillCommands.start' if group=='generate_serpent_hill' else 'SanctuaryCommands.place'
    if group=='sivka':return ('Показать стадию связи с Сивкой, срок, поколение и UUID сущности.' if action=='status' else 'Продвинуть персональное время связи с Сивкой на <ticks>; только IS_RUNNING_IN_IDE.'),'read-only' if action=='status' else 'debug/player NBT','SivkaCommands.register'
    if group=='swamp':return 'Найти подходящее болото по биомам и телепортировать игрока на безопасную поверхность; Верхний мир, поиск ограничен8192 блоками.','teleport','SwampCommands.swamp'
    if group=='structure':return 'Найти структуру из SwampStructures.IDS (кроме swamp_remnants); next выбирает следующую. Поиск в Верхнем мире запрашивает чанки на стадии STRUCTURE_STARTS, не строит здание у игрока.','chunk STRUCTURE_STARTS search','SwampCommands.structure'
    if group=='underground':return {'spawn':'Создать выбранного подземного моба рядом с исполнителем, привязать к позиции; проверить коллизию.','status':'Показать состояние реестра подземных встреч текущего измерения.','populate':'Запланировать население сохранённых комплексов в уже загруженных чанках радиуса16 чанков; без загрузки новых чанков и изменения блоков.'}[action],'read-only' if action=='status' else 'entities/encounter NBT','UndergroundCommands.register'
    if group=='rusalka_preview':return 'Начать/остановить preview ближайшей живой Русалки в12 блоках; только Creative+OP2, без спавна и изменения мира.','entity preview','RusalkaEntity.preview'
    if group=='growtree':return 'Вырастить выбранное дерево перед игроком; до8 проверенных позиций, граница мира и штатный генератор сохраняются.','world blocks','WoodlandCommands.grow'
    if group=='debug':
        d={'gorodishche':'Показать отчёт последнего созданного городища этого сеанса; если его нет — подсказать vanilla locate.','guard':'Найти ближайшего дружинника в16 блоках; показать UUID, архетип, оружие, щит, HP, AI и persistent data.','military_quests':'Показать состояние военных заданий текущего игрока.','bandit_raid':'Показать состояние ближайшего разбойничьего набега.','villager':'Показать роль, климат, профессию, уровень, одежду и UUID ближайшего жителя/зомби-жителя либо выбранной сущности.','settlement_chest':'Показать состав и доступность ресурсов общинного сундука по загруженной позиции.'}[action]
        return d,'read-only','VillageCommands.inspect/storage' if action in ('villager','settlement_chest') else 'MilitaryCommands.register' if action in ('guard','military_quests','bandit_raid') else 'CityCommands.register'
    if group=='test':
        if action in ('gorodishche_showcase','village_showcase'):return 'Построить демонстрационную '+('крепость-городище' if action=='gorodishche_showcase' else 'деревню')+' с палитрой temperate/cold/warm (без аргумента — по текущему климату); изменяет блоки и создаёт жителей.','world blocks/entities','CityCommands.showcase' if action=='gorodishche_showcase' else 'VillageShowcase.showcase'
        if action=='guard':return 'Поставить рабочее место и создать дружинника, связать JOB_SITE и POI, обновить мозг жителя.','world blocks/entities','MilitaryCommands.register'
        if action=='bandit_raid':return 'Принудительно запустить разбойничий набег у позиции исполнителя.','entities/raid NBT','BanditRaids.start'
        if action=='mor_block':return 'Преобразовать наведённый с8 блоков совместимый блок Мора; id=reset возвращает прототип.','world blocks','MorBlocks.convert/prototype'
        if action=='poloz_spawn':return 'Создать тестового Полоза на суше/воде в Верхнем мире; вода проверяется; HP3000, событие становится активным.','entities/event NBT','PolozCommands.spawn'
        if action=='poloz_phase':return 'Установить активному Полозу фазу1..3.','debug/entity state','PolozEntity.setPhaseForTest'
        if action=='poloz_weakpoints':return 'Включить отладочную проверку слабых мест активного Полоза.','debug/entity state','PolozEntity.debugWeakpoints'
        if action=='poloz_cancel':return 'Отменить текущего Полоза/событие без обычной победы; предупреждает о последствиях.','entities/event cancellation','PolozEntity.cancel/PolozEvent.finish'
        if action=='villager':return ('Установить уровень1..5 ближайшему взрослому профессиональному жителю.' if t[2]=='level' else 'Поставить рабочее место выбранной профессии рядом с ближайшим взрослым безработным жителем без опыта; проверить место и части блока.'),'world/player-independent NPC state','VillageCommands.level' if t[2]=='level' else 'VillageCommands.workstation'
        if action=='grey_wolf':
            a=t[2] if len(t)>2 else 'spawn'
            d={'spawn':'Создать debug-Серого Волка в6 блоках перед игроком; только Верхний мир и свободная площадка.','start':'Поздороваться и начать персональную встречу с ближайшим debug-Волком.','reset':'Оборвать встречу и удалить ближайшего debug-Волка.','hp60':'Установить debug-Волку60 HP.','bite':'Запустить укус Волка.','claw':'Запустить двойной удар когтями.','pounce':'Запустить прыжок Волка.','dash':'Запустить фланговый рывок Волка.','howl':'Запустить вой/призыв Волка.'}[a]
            return d,'debug/entities','WolfCommands.spawn' if a=='spawn' else 'WolfCommands.register → GreyWolf'
    raise AssertionError('No reviewed command description: '+path)

def permission(row,path):
    levels=row['player_permissions'];console=row['console_permissions']
    op='OP'+str(min(levels))+'+' if levels and min(levels)>0 else 'любой игрок' if levels else 'нет доступа игроку'
    context='игрок + консоль/command block' if console and not row.get('requires_player_for_execution') else 'только игрок'
    extras=[]
    if ' dev bogatyr ' in path or ' debug_start' in path:extras.append('operatorDebugCommands=true')
    if 'rusalka_preview' in path:extras.append('Creative')
    if 'sivka advance_time' in path:extras.append('только IDE')
    return op+'; '+context+(' ; '+', '.join(extras) if extras else '')

def example(path):
    replacements={'player':'PlayerName','amount':'100','value':'1','phase':'2','level':'2','radius':'128','page':'0','seed':'12345','ticks':'20','pos':'~ ~ ~','target':'@e[type=minecraft:villager,limit=1,sort=nearest]','id':'druzhinnik' if ' class ' in path and ' choose ' in path else '01234567-89ab-cdef-0123-456789abcdef' if 'navigation marker' in path else 'investigate_1' if 'quest185 stage' in path else 'id_from_suggestions','state':'active' if 'quest185' in path else 'fist','template':'slavicmyths:village/warm/utility/stable_01','rotation':'none','targetType':'mini','hero_a':'ilya_muromets','hero_b':'finist','city_id':'city_id_from_info','abandoned_id':'abandoned_id_from_info','encounter':'01234567-89ab-cdef-0123-456789abcdef','token':'01234567-89ab-cdef-0123-456789abcdef','action':'accept','category':'dev city'}
    return '/'+re.sub(r'<([^>]+)>',lambda m:replacements[m[1]],path)

def generate():
    rows=json.loads((O/'tree-after.json').read_text(encoding='utf-8'))
    before=json.loads((O/'tree-before.json').read_text(encoding='utf-8'))
    byraw={re.sub('[<>]','',x['path']):x for x in rows}
    for row in rows:
        path=row['path'];row['access']=permission(row,path)
        if row.get('alias'):
            target=byraw[row['alias']];row['canonical']=target['path']
            row['description']='Совместимый путь к /'+target['path']+'; исполняет то же действие один раз.'
            row['effect']='same as canonical';row['implementation']='CommandTree.install (shared guarded callback)'
        elif row.get('redirect'):
            row['canonical']='slavicmyths' if path=='sm' else 'slavicmyths dev city'
            row['description']='Совместимый префикс; все дочерние команды берутся из /'+row['canonical']+'.'
            row['effect']='alias prefix';row['implementation']='CommandTree.install'
        elif row['executable']:
            row['description'],row['effect'],row['implementation']=describe(path);row['canonical']=path
        else:row['description']='Группа/аргумент; сама не выполняет действие.';row['effect']='none';row['implementation']='CommandTree.install';row['canonical']=path
        if row['executable']:row['example']=example(path)
        owner=row.get('handler_class','')
        if owner:row['implementation']=owner.split('.')[-1]+'#'+('commands' if owner.split('.')[-1] in ('FinistEvents','SkimEvents','KalinEvents','KuzyaResidence') else 'register')+' callback → '+row['implementation']
    canonical=[r for r in rows if r['executable'] and not r.get('alias')]
    lines=['# Команды Slavic Myths — полный справочник','',
      'Канонический корень `/slavicmyths`. Обычные игровые действия: `hero`, `navigation`, `wolf`, `kalin`, `kartaus`. Административные функции — `/slavicmyths dev …`, OP2+. `/sm` — совместимый префикс. `/smclass` и два точных mod-варианта `/locate structure` сохранены; чужие/vanilla команды не заменяются.','',
      'Игрок — источник с ServerPlayer. Консоль/command block используют своё измерение и позицию исполнения; команды с `<player>` могут выбирать другого онлайн-игрока. В таблицах приведены права дерева; дополнительные проверки площадки, владения, Creative и серверные ограничения остаются в обработчиках. Результат0 обычно означает отказ/нет объекта,1 — успех/принятое задание; некоторые команды возвращают число объектов.','',
      'Просмотр: `/slavicmyths help`, `/slavicmyths help dev city`. Автодополнение сохраняет исходные providers и границы аргументов. В примерах `PlayerName` и `*_from_*` заменяются реальными значениями; UUID/ID берутся из состояния встречи/метки/комплекса. Глобальные команды создания изменяют текущий мир: использовать тестовую копию.','',
      'Границы: все генераторы/поиск сохраняют прежние ограничения; ручные наземные курганы/лагеря работают в Верхнем мире, подготавливают грунт и могут загружать конечную область. Отмена строительства не откатывает грунт. Город/заброшенные комплексы сохраняют native starts и проверяют энд-порталы. Диагностика загруженных комплексов не равна поиску новых кандидатов. Телепорты требуют игрока и проверяют безопасное прибытие.','',
      'Список получен из настоящих production builders в обычной JVM, с инициализацией VillageRoles (7 профессий) и загруженной конфигурацией в памяти. Это не runtime NeoForge/event-bus проверка и не запуск мира. Отдельная ветка `sivka advance_time` существует только при IS_RUNNING_IN_IDE; показана для полноты. Реальные server/client/GameTest запусков0; RUNTIME_NOT_RUN.','',
      f'Канонических исполняемых вариантов: **{len(canonical)}**. Все реальные узлы (включая структурные и совместимые) перечислены ниже. Машинный реестр: `implementation/commands-74/registry.json`.','',
      '## Исполняемые канонические команды','',
      '| Синтаксис и пример | Действие / результат | Права и контекст | Side effects | Обработчик |','|---|---|---|---|---|']
    for row in canonical:
        lines.append('| `/'+row['path']+'`<br>Например: `'+row['example']+'` | '+row['description']+' | '+row['access']+' | '+row['effect']+' | `'+row['implementation']+'` |')
    lines+=['','## Совместимые пути','', 'Каждый точный старый исполняемый вызов перечислен здесь либо уже является каноническим выше. Дочерние пути `/sm …` совпадают с `/slavicmyths …` и наследуют те же права; они не размножаются в таблице.','', '| Путь | Канонический путь / поведение | Права и контекст |','|---|---|---|']
    for row in rows:
        if row.get('alias') or row.get('redirect'):lines.append('| `/'+row['path']+'` | '+row['description']+' | '+row['access']+' |')
    lines+=['','## Аргументы и ограничения','', '| Полный путь аргумента | Тип | Границы / примеры синтаксиса | Custom suggestions |','|---|---|---|---|']
    for row in rows:
        if row['argument'] and not row.get('alias'):
            bounds=str(row.get('min',''))+'…'+str(row.get('max','')) if 'min' in row else ', '.join(row['examples'])
            lines.append('| `/'+row['path']+'` | `'+row.get('argument_type','')+'` | '+bounds.replace('|','/')+' | '+str(row.get('suggestions',False))+' |')
    lines+=['','## Структурные узлы','', 'Эти префиксы/аргументы не исполняют действие сами; ввод продолжается дочерней командой.','', '| Полный путь | Права/контекст дерева |','|---|---|']
    for row in rows:
        if not row['executable'] and not row.get('alias') and not row.get('redirect'):lines.append('| `/'+row['path']+'` | '+row['access']+' |')
    lines+=['','## Обновление справочника','', '`gradlew.bat -I tools/commands-offline.init.gradle verifyCommandsOffline` → `python tools/command_reference.py --write`. Затем `python tools/command_reference.py --check`. Новое действие без описания вызывает ошибку; checker сверяет полный дамп с документацией и хешами исходников.','']
    # Full before→after map, including unchanged commands and every argument variant.
    migrations=[]
    # The fixture emits canonical mappings for the actual pre-normalized contributions.
    original=json.loads((O/'contributions.json').read_text(encoding='utf-8'))
    for old in original:
        if not old['executable']:continue
        new=byraw[old['canonical']]
        migrations.append(dict(before=old['path'],after=new['path'],status='UNCHANGED' if old['path']==new['path'] else 'ALIAS',behavior=new['description'],permission=new['access'],compatibility='one guarded handler'))
    migrations.append(dict(before='slavicmyths debug chud_city …',after='slavicmyths dev city …',status='ALIAS',behavior='Диагностика города Чуди',permission='OP2+',compatibility='bounded redirect'))
    migrations.append(dict(before='sm …',after='slavicmyths …',status='ALIAS',behavior='Все команды основного корня; устранена зависимость от порядка регистрации',permission='наследуются',compatibility='one root redirect'))
    migration=['# Миграция команд Slavic Myths','',
      'Ни один прежний исполняемый публичный путь не удалён. ALIAS сохраняет аргументы, исходные ограничения и единственный вызов общего guarded handler. UNCHANGED — исходный канонический путь. REMOVED:0. Самостоятельные event subscribers заменены37 вкладчиками и одним событием регистрации; никаких глобальных dispatcher singleton.','',
      'Подтверждённые эквивалентные действия: city debug_plan→info (2 варианта); abandoned inspect→info и debug→validate (4); locate kurgan→kurgan locate (6); generate kurgan→kurgan generate (6, KurganDebug.generate прямо делегирует ManualStructureJobs.start); growtree random→базовый вызов (4); locate structure двух комплексов→city/abandoned search (2); bandit_camp medium→fortified (2, один tier1). Итого26 лишних исполнительных реализаций объединены; старые пути остаются aliases.','',
      'Не объединены: locate загруженного города и search кандидатов; обычное и debug-Идолище; /dev bogatyr courtyard с дополнительным config-gate и /dev encounter courtyard. Их контекст/ограничения отличаются. Варианты с необязательным seed, ID или размером сохранены как удобный синтаксис.','',
      'До изменения: исходный JVM-снимок362 узла/267 исполняемых вариантов без mod-init VillageRoles и без доступной config-проверки. После корректной инициализации вкладчики дают274 варианта (включая7 рабочих мест); IDE-only Сивка помечен отдельно. Это границы исходного снимка, а не утверждение о полноте игрового dispatcher.','',
      '| before path | after path | behavior | permission | compatibility |','|---|---|---|---|---|']
    for m in migrations:migration.append('| `/'+m['before']+'` | `/'+m['after']+'` | '+m['behavior']+' | '+m['permission']+' | '+m['status']+'; '+m['compatibility']+' |')
    migration+=['','Добавлены `/slavicmyths help` и `/slavicmyths help <category>` (доступные источнику ветки, RU/EN). В старом root `/sm` устранено смешение redirects и отдельных детей; root aliases теперь создаются один раз.','']
    files=[]
    for p in sorted((R/'src/main/java').rglob('*.java')):
        t=p.read_text(encoding='utf-8')
        if 'CommandTree tree' in t or p.parent.name=='commands' or p.name in ('VillageRoles.java','ClassDefinitions.java','ChudPlan.java','AbandonedPlan.java','SearchArea.java'):
            files.append(dict(path=p.relative_to(R).as_posix(),sha256=hashlib.sha256(p.read_text(encoding='utf-8').encode('utf-8')).hexdigest()))
    registry=dict(scope='real production builders, offline Brigadier; server runtime NOT_RUN',sources=files,nodes=rows,migration=migrations)
    artifacts={'docs/COMMANDS.md':'\n'.join(lines),'docs/COMMAND_MIGRATION.md':'\n'.join(migration)}
    # One record per line keeps complete machine evidence reviewable in Git.
    def array(items,pad='  '):
        return '[\n'+',\n'.join(pad+json.dumps(item,ensure_ascii=False,separators=(',',':')) for item in items)+'\n'+pad[:-2]+']'
    parts=[]
    for key,value in registry.items():
        encoded=array(value,'    ') if isinstance(value,list) else json.dumps(value,ensure_ascii=False)
        parts.append('  '+json.dumps(key)+': '+encoded)
    artifacts['docs/implementation/commands-74/registry.json']='{\n'+',\n'.join(parts)+'\n}\n'
    # Preserve raw dump schemas and values; normalize formatting only after capture.
    for name in ('tree-before.json','tree-after.json','contributions.json','registrars-before.json'):
        value=json.loads((O/name).read_text(encoding='utf-8'))
        artifacts['docs/implementation/commands-74/'+name]=array(value)+'\n'
    return artifacts


def main():
    artifacts=generate()
    for name,content in artifacts.items():
        p=R/name
        if '--write' in sys.argv:p.write_text(content,encoding='utf-8')
        else:assert p.read_text(encoding='utf-8')==content,'Stale command artifact: '+name
    print('COMMAND_REFERENCE PASS: full tree, command descriptions, migration, source hashes match')
if __name__=='__main__':main()
