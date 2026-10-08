"""Reproduce Class 2.0 pixel assets and merge bilingual texts without reverting other resources."""
from pathlib import Path
import json
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
NAMES={
'druzhinnik':('Дружинник','Druzhinnik'),'vedun':('Ведун','Vedun'),'razboinik':('Разбойник','Outlaw'),
'vityaz':('Витязь','Knight'),'griden':('Гридень','Retainer'),'bogatyr':('Богатырь','Bogatyr'),'ratoborets':('Ратоборец','Warrior'),'schitonosets':('Щитоносец','Shieldbearer'),'voevoda':('Воевода','Warlord'),
'volhv':('Волхв','Volkhv'),'koldun':('Колдун','Warlock'),'oberezhnik':('Обережник','Wardkeeper'),'znakhar':('Знахарь','Herbalist'),'morovik':('Моровик','Blightkeeper'),'chernoknizhnik':('Чернокнижник','Blackbook keeper'),
'ushkuinik':('Ушкуйник','River raider'),'glavar':('Главарь','Gang leader'),'naletchik':('Налётчик','Raider'),'strelok':('Стрелок','Marksman'),'ataman':('Атаман','Ataman'),'reznik':('Резник','Cutthroat')}
SKILLS={
'lunge':('Выпад','Lunge','Рывок с усиленной атакой первой цели. Требуется оружие ближнего боя.','Dash and strike the first target. Requires a melee weapon.'),
'flurry':('Сеча','Flurry','Временно ускоряет восстановление силы обычных атак.','Temporarily speeds up recovery of normal attack strength.'),
'trip':('Подсечка','Trip','Ближний удар с кратким удержанием. Боссы и игроки только замедляются.','Melee strike with brief control. Bosses and players only slow down.'),
'aerial_strike':('Удар с воздуха','Aerial strike','Прыжок и удар при снижении или приземлении. Защита от падения только на этот прыжок.','Leap, then strike while descending or upon landing. Fall protection covers this leap only.'),
'shield_ram':('Таран щитом','Shield ram','Удар щитом с отбрасыванием. Требуется щит.','Shield impact with knockback. Requires a shield.'),
'temper':('Закалка','Temper','Повышает максимальное здоровье, пока выбран пассивным навыком.','Increases maximum health while equipped as the passive skill.'),
'steadfast':('Стойкость','Steadfast','Небольшое сопротивление отбрасыванию.','Modest knockback resistance.'),
'riposte':('Ответный удар','Riposte','Успешный блок щитом усиливает следующую ближнюю атаку на 3 секунды.','A confirmed shield block empowers the next melee attack within 3 seconds.'),
'second_wind':('Второе дыхание','Second wind','При пересечении порога 30% здоровья временно повышает стойкость.','Temporarily grants resistance when health crosses below 30%.'),
'binding_sign':('Сковывающий знак','Binding sign','Удерживает цель на линии взгляда. Боссы и игроки получают мягкое замедление.','Roots a target in sight. Bosses and players receive soft slowing.'),
'ward':('Оберег','Ward','Временное поглощение урона; не складывается само с собой.','Temporary damage absorption; does not stack with itself.'),
'hex':('Порча','Hex','Ослабляет и замедляет цель на линии взгляда.','Weakens and slows a target in sight.'),
'cleanse':('Очищение','Cleanse','Снимает ограниченное число отрицательных эффектов, кроме защищённых.','Removes a limited number of negative effects except protected ones.'),
'slumber':('Сонная дрёма','Slumber','Кратко мешает преследованию. PvP и боссы получают мягкий эффект.','Briefly disrupts pursuit. PvP and bosses receive a soft effect.'),
'ward_power':('Сила оберегов','Ward power','Усиливает ёмкость и длительность Оберега и Последнего обряда.','Improves Ward and Last rite capacity and duration.'),
'herbalism':('Знахарство','Herbalism','Лечебные травяные продукты мода дополнительно восстанавливают здоровье.','Healing herbal foods of this mod restore additional health.'),
'spirit_sense':('Чутьё нечисти','Spirit sense','Слабый сигнал от близкой видимой нечисти.','A subtle signal from nearby visible mythical creatures.'),
'last_rite':('Последний обряд','Last rite','Однократная защита при пересечении порога 30% здоровья; большой КД.','Protection when crossing below 30% health; long internal cooldown.'),
'keen_eye':('Зоркий глаз','Keen eye','Следующая стрела немного доводится к последней недавно поражённой цели.','The next arrow receives a bounded correction toward the last recently hit target.'),
'dash':('Рывок','Dash','Короткий рывок с проверкой столкновений, без неуязвимости.','A short collision-checked dash without invulnerability.'),
'dirty_strike':('Подлый удар','Dirty strike','Следующий удар сзади или сбоку получает бонус. Лобовой удар сохраняет подготовку.','The next rear or side strike gains damage. Frontal hits keep the preparation.'),
'net':('Сеть','Net','Бросок короткой сети. Контроль игроков и боссов ограничен.','Throw a short-range net. Control against players and bosses is limited.'),
'smoke':('Дымовой уход','Smoke retreat','Дым мешает близким обычным врагам преследовать игрока и ускоряет отход.','Smoke disrupts nearby ordinary enemies and speeds up retreat.'),
'light_step':('Лёгкая поступь','Light step','Небольшой постоянный бонус скорости при выборе пассивным навыком.','A modest movement bonus while equipped as the passive skill.'),
'steady_aim':('Меткий глаз','Steady aim','Небольшой бонус к критическим стрелам.','A modest damage bonus for critical arrows.'),
'cold_blood':('Хладнокровие','Cold blood','Дополнительный урон по цели с менее чем 35% здоровья.','Additional damage against targets below 35% health.'),
'hunters_fervor':('Охотничий пыл',"Hunter's fervor",'Подтверждённое попадание стрелой временно повышает скорость.','A confirmed arrow hit temporarily increases movement speed.'),
'opportunity':('Удачный момент','Opportunity','Уход от близкого атакующего врага Рывком или Дымом усиливает следующую атаку.','Escaping a nearby attacking enemy through Dash or Smoke empowers the next attack.'),
'onslaught':('Натиск','Onslaught','Эволюция Выпада III для Витязя.','Evolution of Lunge III for a Knight.'),
'bulwark':('Оплот','Bulwark','Эволюция Тарана щитом III для Гридня.','Evolution of Shield ram III for a Retainer.'),
'greater_ward':('Великий оберег','Greater ward','Эволюция Оберега III для Обережника.','Evolution of Ward III for a Wardkeeper.'),
'greater_hex':('Глубокая порча','Greater hex','Эволюция Порчи III для Колдуна.','Evolution of Hex III for a Warlock.'),
'true_shot':('Верный выстрел','True shot','Эволюция Зоркого глаза III для Стрелка.','Evolution of Keen eye III for a Marksman.'),
'raider_dash':('Налёт','Raider dash','Эволюция Рывка III для Налётчика.','Evolution of Dash III for a Raider.')}
UI={
'title':('Классы и навыки','Classes and skills'),'choose':('Выбрать: %s','Choose: %s'),'confirm_class':('Подтвердить навсегда: %s','Confirm permanently: %s'),'permanent':('Обычная смена класса недоступна.','Ordinary class changes are unavailable.'),
'progress':('%s · Ур. %s · Опыт %s/%s · Очки %s','%s · Lv. %s · XP %s/%s · Points %s'),'learn':('Изучить / повысить ранг','Learn / increase rank'),'slot':('Слот %s','Slot %s'),'equip_passive':('Выбрать пассивным','Equip passive'),'hud_on':('HUD: включён','HUD: enabled'),'hud_off':('HUD: выключен','HUD: disabled'),
'parameters':('Ранг %s · КД %s с · Время %s с · Дальность %s · Сила %s','Rank %s · CD %s s · Duration %s s · Range %s · Power %s'),'requirements':('Ур. %s · Предшественник %s, ранг %s','Lv. %s · Prerequisite %s, rank %s'),'confirm_branch':('Нажмите повторно для выбора: %s','Click again to choose: %s'),
'wrong_class':('Другой класс','Wrong class'),'max_rank':('Максимальный ранг','Maximum rank'),'level_required':('Недостаточный уровень','Level required'),'prerequisite':('Нужен предыдущий навык','Prerequisite required'),'branch_required':('Нужна соответствующая ветвь','Branch required'),'no_points':('Нет очков навыков','No skill points')}
DESCRIPTIONS={'druzhinnik':('Ближний бой, щит и стойкость.','Melee combat, shields and endurance.'),'vedun':('Знаки, обереги, травы и порча.','Signs, wards, herbs and hexes.'),'razboinik':('Стрельба, манёвры и хитрость.','Ranged attacks, movement and cunning.')}

def icon(name):
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);steel='#D7DEDB';dark='#383E48';gold='#C4A269';green='#819D69';red='#A75852';purple='#9A83A4'
    def line(points,color=steel,width=2): d.line(points,fill=color,width=width)
    def sword(x=8,y=1):d.polygon([(x,y),(x+1,y+2),(x+1,y+8),(x-1,y+8),(x-1,y+2)],fill=steel);line([(x-3,y+9),(x+3,y+9)],gold,1);line([(x,y+9),(x,y+13)],gold,2)
    def shield():d.polygon([(3,2),(12,2),(12,8),(8,14),(3,8)],fill=gold);d.polygon([(5,4),(10,4),(10,8),(8,11),(5,8)],fill=dark)
    def eye():d.polygon([(1,7),(5,3),(10,3),(14,7),(10,11),(5,11)],fill=gold);d.rectangle((7,5,9,9),fill=dark)
    if name in ('druzhinnik','shield_ram','ward','steadfast'):shield();
    if name=='druzhinnik':sword(12,0)
    elif name=='vedun':line([(8,1),(8,14)],gold);line([(2,5),(8,1),(13,5),(8,10),(2,5)],green)
    elif name=='razboinik':d.polygon([(4,1),(11,1),(14,10),(11,13),(3,13),(1,10)],fill=dark);eye();line([(3,12),(12,12)],steel,1)
    elif name=='lunge':line([(1,11),(13,3)],steel);line([(10,2),(14,2),(14,6)],steel,1);line([(2,7),(6,4)],gold,1)
    elif name=='flurry':
        for x in (2,6,10):line([(x,12),(x+3,3)],steel);line([(x,4),(x+3,2)],gold,1)
    elif name=='trip':line([(6,2),(6,9),(3,13)],gold);line([(11,2),(11,9),(14,13)],gold);line([(1,10),(14,7)],steel)
    elif name=='aerial_strike':d.rectangle((6,1,9,3),fill=gold);line([(7,5),(7,9),(3,11)],gold);line([(7,9),(11,11)],gold);line([(11,4),(11,14)],steel);d.polygon([(9,12),(13,12),(11,15)],fill=steel)
    elif name=='shield_ram':line([(1,6),(5,6)],steel);line([(1,9),(5,9)],steel);line([(12,4),(15,2)],steel,1)
    elif name=='temper':d.polygon([(1,5),(3,2),(6,2),(8,4),(10,2),(13,2),(15,5),(14,9),(8,15),(2,9)],fill=red);line([(4,5),(6,5)],steel,1)
    elif name=='steadfast':line([(7,4),(7,10),(12,11)],steel);d.rectangle((5,11,13,13),fill=steel)
    elif name=='riposte':sword(9);line([(2,3),(2,8),(6,8)],gold);line([(4,6),(6,8),(4,10)],gold,1)
    elif name=='second_wind':line([(1,8),(4,8),(6,3),(8,13),(10,6),(14,6)],red);line([(2,2),(5,2)],steel,1)
    elif name=='binding_sign':line([(8,1),(14,8),(8,14),(1,8),(8,1)],green);line([(4,8),(12,8)],gold);line([(8,4),(8,12)],gold)
    elif name=='ward':line([(8,4),(8,10)],green);line([(5,7),(11,7)],green)
    elif name=='hex':d.rectangle((3,2,12,10),fill=purple);d.rectangle((5,5,6,6),fill=dark);d.rectangle((10,5,11,6),fill=dark);line([(7,2),(9,6),(7,8),(8,13)],dark);d.rectangle((5,11,11,13),fill=purple)
    elif name=='cleanse':line([(8,13),(8,5),(4,2)],green);d.ellipse((2,1,6,5),fill=green);d.ellipse((9,5,13,9),fill=green);line([(2,10),(4,12)],steel,1);line([(1,12),(5,10)],steel,1)
    elif name=='slumber':d.polygon([(11,1),(7,3),(6,8),(9,12),(13,13),(7,15),(2,12),(1,7),(4,2)],fill=purple);line([(8,7),(13,7)],dark,1)
    elif name=='ward_power':d.polygon([(8,1),(14,8),(8,15),(1,8)],fill=gold);d.rectangle((6,5,10,11),fill=green);line([(3,8),(12,8)],steel,1)
    elif name=='herbalism':d.rectangle((3,8,12,14),fill=gold);line([(8,8),(8,2)],green);d.polygon([(7,5),(3,1),(2,5)],fill=green);d.polygon([(9,5),(13,1),(14,5)],fill=green)
    elif name=='spirit_sense':d.polygon([(4,2),(10,2),(13,6),(12,13),(9,11),(7,14),(4,11),(1,13),(2,6)],fill=steel);d.rectangle((4,5,5,7),fill=dark);d.rectangle((9,5,10,7),fill=dark)
    elif name=='last_rite':d.rectangle((3,12,12,14),fill=gold);d.rectangle((5,6,10,11),fill=steel);d.polygon([(8,1),(10,4),(8,5),(6,4)],fill=gold)
    elif name=='keen_eye':eye();line([(9,13),(14,13)],steel);d.polygon([(13,11),(15,13),(13,15)],fill=steel)
    elif name=='dash':line([(1,4),(8,4),(5,2)],gold);line([(1,8),(11,8),(8,6)],steel);line([(1,12),(14,12),(11,10)],gold)
    elif name=='dirty_strike':d.rectangle((3,1,6,3),fill=dark);line([(4,5),(4,10),(2,14)],gold);line([(4,10),(7,14)],gold);d.polygon([(13,2),(14,3),(10,9),(8,9)],fill=steel);line([(7,9),(11,11)],gold,1)
    elif name=='net':
        for x in (2,6,10,14):line([(x,2),(x,14)],gold,1);line([(2,x),(14,x)],gold,1)
    elif name=='smoke':
        for box in ((1,9,7,15),(5,5,11,12),(9,1,15,8)):d.ellipse(box,fill='#858B87')
        line([(4,13),(11,9)],dark,2)
    elif name=='light_step':d.polygon([(4,1),(9,1),(9,9),(14,10),(14,13),(3,13)],fill=gold);line([(1,5),(4,5)],steel,1)
    elif name=='steady_aim':d.ellipse((2,2,13,13),outline=gold,width=2);line([(8,1),(8,14)],steel,1);line([(1,8),(14,8)],steel,1);d.rectangle((7,7,9,9),fill=red)
    elif name=='cold_blood':d.polygon([(8,1),(14,9),(13,13),(8,15),(3,13),(2,9)],fill=red);line([(8,4),(8,12)],steel,1);line([(4,8),(12,8)],steel,1)
    elif name=='hunters_fervor':d.polygon([(3,1),(8,4),(5,7),(12,5),(8,10),(14,9),(4,15),(6,10),(1,11),(5,6)],fill=gold)
    elif name=='opportunity':d.ellipse((2,2,13,13),outline=gold,width=2);line([(8,4),(8,8),(12,8)],steel);d.polygon([(1,1),(4,1),(1,4)],fill=steel)
    return im.resize((32,32),Image.Resampling.NEAREST)

def main():
    target=ASSETS/'textures/gui/classes';target.mkdir(parents=True,exist_ok=True)
    for name in ('druzhinnik','vedun','razboinik',*list(SKILLS)[:28]):icon(name).save(target/f'{name}.png')
    for evo,base in {'onslaught':'lunge','bulwark':'shield_ram','greater_ward':'ward','greater_hex':'hex','true_shot':'keen_eye','raider_dash':'dash'}.items():
        im=icon(base);d=ImageDraw.Draw(im);d.rectangle((24,0,31,7),fill='#493E2C');d.line((28,1,28,6),fill='#E5CA7D',width=2);d.line((25,4,31,4),fill='#E5CA7D',width=2);im.save(target/f'{evo}.png')
    icon('binding_sign').save(ASSETS/'textures/mob_effect/class_root.png')
    UI['parameters']=('Ранг %s · КД %s с · Время %s с · Дальность %s','Rank %s · CD %s s · Duration %s s · Range %s')
    UI['branch_info']=('Развитие на ур. %s · %s: ×%s','Advance at lv. %s · %s: ×%s')
    UI['available']=('Доступно для изучения','Available to learn')
    UI['progress_top']=('%s · Ур. %s','%s · Lv. %s')
    UI['progress_bottom']=('Опыт %s/%s · Очки %s','XP %s/%s · Points %s')
    UI.update({'kind_active':('Активный','Active'),'kind_passive':('Пассивный','Passive'),'kind_trigger':('Триггерный','Triggered'),'kind_evolution':('Эволюция','Evolution'),'rank':('Ранг %s/%s','Rank %s/%s'),'loadout':('Слот %s: %s. Нажмите для снятия.','Slot %s: %s. Click to unequip.'),'empty':('Пусто','Empty')})
    for key in ('lunge','dirty_strike','aerial_strike','cold_blood','opportunity'):UI['stat_'+key]=('+%s%% урона','+%s%% damage')
    UI.update({'stat_flurry':('+%s%% скорости атак','+%s%% attack speed'),'stat_temper':('+%s ед. максимального здоровья','+%s maximum health'),'stat_steadfast':('+%s%% сопротивления отбрасыванию','+%s%% knockback resistance'),'stat_riposte':('+%s ед. урона следующего удара','+%s damage on the next strike'),'stat_ward':('%s ед. поглощения урона','%s damage absorption'),'stat_ward_power':('+%s%% ёмкости и времени защитных навыков','+%s%% protective skill capacity and duration'),'stat_herbalism':('+%s ед. лечения от травяной еды','+%s health from herbal food'),'stat_keen_eye':('Угол доводки до %s°; метка живёт 10 с','Correction up to %s°; mark lasts 10 s'),'stat_light_step':('+%s%% скорости движения','+%s%% movement speed'),'stat_steady_aim':('+%s%% урона критической стрелы','+%s%% critical arrow damage'),'stat_cleanse':('Снять до %s отрицательных эффектов','Remove up to %s negative effects'),'stat_last_rite':('%s ед. защитного поглощения','%s defensive absorption')})
    SKILLS['bulwark']=('Заслон','Bulwark','Таран щитом: +20% отбрасывания и 2 с сопротивления урону. Нужен Таран III и Гридень.','Shield ram: +20% knockback and 2 s damage resistance. Requires rank III and Retainer.')
    SKILLS['onslaught']=('Натиск','Onslaught','Выпад: +15% к бонусу урона и +0.5 блока дальности.','Lunge: +15% to damage bonus and +0.5 blocks range.')
    SKILLS['greater_ward']=('Великий оберег','Greater ward','Оберег: +20% ёмкости и длительности.','Ward: +20% capacity and duration.')
    SKILLS['greater_hex']=('Глубокая порча','Greater hex','Порча: +25% длительности.','Hex: +25% duration.')
    SKILLS['true_shot']=('Верный выстрел','True shot','Зоркий глаз: +5° допустимой доводки.','Keen eye: +5° allowed correction.')
    SKILLS['raider_dash']=('Налёт','Raider dash','Рывок: +0.5 блока дальности и -15% КД.','Dash: +0.5 blocks range and -15% cooldown.')
    for index,language in enumerate(('ru_ru','en_us')):
        path=ASSETS/f'lang/{language}.json';data=json.loads(path.read_text(encoding='utf-8'))
        for key,names in NAMES.items():data[f'class.slavicmyths.{key}']=names[index]
        for key,descs in DESCRIPTIONS.items():data[f'class.slavicmyths.{key}.description']=descs[index]
        for key,value in SKILLS.items():data[f'classskill.slavicmyths.{key}']=value[index];data[f'classskill.slavicmyths.{key}.description']=value[index+2]
        for key,value in UI.items():data[f'classes.slavicmyths.{key}']=value[index]
        for n in range(1,4):data[f'key.slavicmyths.class_slot_{n}']=(f'Навык класса — слот {n}' if index==0 else f'Class skill — slot {n}')
        data['key.slavicmyths.class_menu']=('Классы и навыки','Classes and skills')[index]
        data['key.slavicmyths.path_ability']=('Навык класса — слот 1','Class skill — slot 1')[index]
        data['effect.slavicmyths.class_root']=('Сковывание','Binding')[index]
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    tags=ROOT/'src/main/resources/data/slavicmyths/tags'
    for category,name,values in [('entity_type','class_control_resistant',['minecraft:ender_dragon','minecraft:wither','minecraft:warden']),('mob_effect','class_protected_effects',['slavicmyths:kurgan_curse','slavicmyths:durnaya_dolya'])]:
        path=tags/category/f'{name}.json';path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps({'replace':False,'values':values},indent=2)+'\n')
    registry=(ROOT/'src/main/java/org/slavicmyths/registry/ModEntities.java').read_text(encoding='utf-8')
    import re
    values=['slavicmyths:'+s for s in re.findall(r'ENTITIES\.register\("([a-z_]+)"',registry) if any(word in s for word in ('leshy','domovoy','rusalka','vodyan','kikim','yaga','upyr','polud','bannik','ovinnik','likho','nav','polevik'))]
    path=tags/'entity_type/class_mythical.json';path.write_text(json.dumps({'replace':False,'values':values},indent=2)+'\n')
    sheet=Image.new('RGBA',(8*48,5*52),'#28251F')
    for n,name in enumerate(SKILLS):sheet.alpha_composite(Image.open(target/f'{name}.png'),(n%8*48+8,n//8*52+4))
    (ROOT/'work').mkdir(exist_ok=True);sheet.save(ROOT/'work/class-icons-131.png')
    import class_ui_resources_1323
    class_ui_resources_1323.main()
    print(f'Generated {len(SKILLS)} skill icons, 3 class icons, bilingual text and class tags.')

if __name__=='__main__':main()
