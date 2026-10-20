"""Native 32px skill art, 64px class crests and crisp wooden GUI sprites."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
import class_resources_131 as definitions
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/slavicmyths';OUT=ASSETS/'textures/gui/classes'
UI={
 'tree':('Дерево','Skill tree'),'branches':('Развитие','Branches'),'controls':('Управление','Controls'),
 'choose_path':('Выберите основной путь','Choose your primary path'),'choose':('Выбрать %s','Choose %s'),
 'study':('Изучить','Learn'),'upgrade':('Улучшить','Upgrade'),'back':('Назад','Back'),'confirm':('Подтвердить','Confirm'),
 'confirm_path':('Путь «%s» станет основным направлением развития. Обычная смена в Survival недоступна. Продолжить?','The path "%s" becomes your main direction. Ordinary changes in Survival are unavailable. Continue?'),
 'selection_requirements':('Требования: основной путь ещё не выбран.','Requires no existing primary path.'),
 'class_features_druzhinnik':('Сильные стороны: ближние атаки, щит и стойкость. Две линии навыков: боевые приёмы и защита. Развитие ведёт к Витязю или Гридню.','Strengths: melee strikes, shields and endurance. Combat techniques and defense lead to Knight or Retainer.'),
 'class_features_vedun':('Сильные стороны: контроль, защита и травы. Знаки и пассивные обереги ведут к Волхву или Колдуну.','Strengths: control, protection and herbs. Signs and passive wards lead to Volkhv or Warlock.'),
 'class_features_razboinik':('Сильные стороны: стрельба, манёвры и выгодная позиция. Активные приёмы и пассивная подготовка ведут к Ушкуйнику или Главарю.','Strengths: archery, movement and positioning. Active techniques and passive training lead to River Raider or Gang Leader.'),
 'class_start':('Стартовый навык: %s. Изучается в дереве.','Starting skill: %s. Learn it in the tree.'),
 'role_druzhinnik':('Бой • щит','Combat • shield'),'role_vedun':('Знаки • защита','Signs • wards'),'role_razboinik':('Стрельба • манёвры','Archery • mobility'),
 'rank_one':('Эффект первого ранга','Rank I effect'),'current_effect':('Текущий эффект','Current effect'),'next_rank':('Следующий ранг: %s → %s','Next rank: %s → %s'),
 'evolution_effect':('Постоянное улучшение исходной способности. Отдельный слот не требуется.','Permanent upgrade to the original ability. No separate slot required.'),
 'value_cooldown':('Перезарядка: %s с','Cooldown: %s s'),'value_duration':('Длительность: %s с','Duration: %s s'),'value_range':('Дальность: %s блоков','Range: %s blocks'),
 'requirements_heading':('Требования','Requirements'),'required_level':('Требуется уровень развития %s. Текущий: %s.','Required development level: %s. Current: %s.'),
 'required_skill':('Требуется %s, ранг %s. Текущий ранг: %s.','Requires %s at rank %s. Current rank: %s.'),'required_branch':('Требуется направление: %s.','Required branch: %s.'),
 'point_cost':('Стоимость: 1 очко навыков. Доступно: %s.','Cost: 1 skill point. Available: %s.'),'creative_free':('Creative: ручное изучение без требований и списаний.','Creative: manual learning without requirements or costs.'),
 'no_slot':('Работает автоматически; слот не занимает.','Works automatically; uses no slot.'),'active_loadout':('Активные способности','Active abilities'),'passive_short':('Пассивный','Passive'),
 'slot_hint':('Выберите изученный навык и нажмите слот. ПКМ — снять.','Select a learned skill, then click a slot. Right-click to clear.'),
 'tree_hint':('Узел → слот. Дерево: перетаскивание или колесо; Shift + колесо — по горизонтали. Карточка: колесо.','Node → slot. Drag or scroll the tree; Shift + wheel pans horizontally. Scroll the card.'),
 'press_key':('Нажмите клавишу… Esc — отмена','Press a key… Esc cancels'),'binding_row':('Активный навык %s     [%s]','Active skill %s     [%s]'),
 'binding_hint':('Нажмите клавишу справа, затем новое назначение. Esc — отменить.','Click a key on the right, then press a new key. Esc cancels.'),
 'conflict_named':('Конфликт: %s','Conflict: %s'),'branch_current':('Текущее направление: %s','Current branch: %s'),
 'settings':('Настройки Slavic Myths','Slavic Myths settings'),'show_hud':('Показывать HUD навыков','Show skill HUD'),'hud_settings_hint':('Скрывается только панель. Способности и назначенные клавиши продолжают работать.','Only the panel is hidden. Abilities and their key mappings continue working.')}

UI.update({
 'tree_hint':('Дерево навыков · прокрутка колесом','Skill tree · scroll with mouse wheel'),
 'tree_maxed':('Максимальный ранг','Maximum rank'),
 'tree_available':('Доступно изучение / улучшение','Learning / upgrade available'),
 'tree_learned':('Изучено; улучшение заблокировано','Learned; upgrade locked'),
 'tree_locked':('Изучение заблокировано','Learning locked'),
})

UI.update({'paths': ('Пути развития', 'Development paths'), 'show_skills': ('Навыки', 'Skills'), 'show_directions': ('Направления', 'Specializations'), 'assign_to': ('Закрепить: %s', 'Assign: %s'), 'slot_occupied': ('%s — сейчас: %s. Нажатие заменит навык.', '%s — currently: %s. Click to replace this skill.'), 'clear_slot': ('Снять навык: %s', 'Clear skill: %s'), 'automatic_skill': ('Действует автоматически после изучения. На клавишу не назначается.', 'Works automatically after learning. No key assignment.'), 'learn_then_assign': ('Сначала «Изучить», затем «Закрепить» на выбранную клавишу.', 'First Learn, then Assign to a key.'), 'assign_instruction': ('Закрепите кнопкой ниже. Наведите на кнопку, чтобы увидеть занятый навык.', 'Assign using a button below. Hover to see its current skill.'), 'binding_hint': ('Здесь меняются клавиши. Назначение навыков — в «Путях развития». Конфликты подписаны.', 'Change keys here. Assign skills in Development paths. Conflicts are named.')})

UI.update({'fit_short': ('Сброс', 'Fit'), 'view_hint': ('Колесо: масштаб к указателю. ЛКМ на пустоте или средняя кнопка: сдвиг. Сброс: всё дерево.', 'Wheel: zoom at pointer. Drag empty space or middle mouse: pan. Fit: whole tree.'), 'zoom_hint': ('Колесо — приблизить; пустое поле — сдвиг', 'Wheel to zoom; drag empty space to pan'), 'quick_assign': ('Назначить', 'Assign'), 'quick_key': ('Клавиша: %s', 'Key: %s'), 'quick_clear': ('Снять навык', 'Clear skill'), 'quick_unbind': ('Убрать клавишу', 'Unbind key'), 'quick_slot_hint': ('Нажмите: назначить навык / сменить клавишу', 'Click: assign skill / change key'), 'save': ('Сохранить', 'Save'), 'cancel': ('Отмена', 'Cancel'), 'capture_hint': ('Esc — отменить ввод. Изменения вступят в силу после «Сохранить».', 'Esc cancels capture. Changes apply after Save.'), 'no_learned': ('Нет изученных навыков этого типа. Сначала изучите навык в дереве.', 'No learned skills of this type. Learn a skill in the tree first.'), 'skill_card': ('Навык', 'Skill'), 'key_menu': ('Меню классов', 'Class menu'), 'key_ultimate': ('Способность мастерства', 'Mastery ability'), 'key_title': ('Назначение клавиши', 'Key binding'), 'mastery_related': ('Другие мастерства пути', 'Other masteries of this path'), 'progress_compact': ('Ур. %s · опыт %s/%s · очки %s', 'Lv %s · XP %s/%s · points %s')})

UI.update({'keys_hint':('Клик по клавише — изменить; Esc — назад','Click a key to change; Esc to go back')})

BRANCH_COPY = {'classbranch.slavicmyths.vityaz.role': ('Наступление: сближение и усиленная первая атака.', 'Offense: closing the gap and a stronger opening strike.'), 'classbranch.slavicmyths.vityaz.effect': ('Выпад: величина бонуса урона +%s%%.', 'Lunge: damage bonus increased by %s%%.'), 'classbranch.slavicmyths.griden.role': ('Защита со щитом: оттеснение и остановка противника.', 'Shield defense: pushing enemies away and interrupting pursuit.'), 'classbranch.slavicmyths.griden.effect': ('Таран щитом: отбрасывание и длительность контроля +%s%%.', 'Shield Ram: knockback and control duration +%s%%.'), 'classbranch.slavicmyths.bogatyr.role': ('Удары с воздуха: усиление атаки при снижении и приземлении.', 'Aerial combat: stronger attacks while descending or landing.'), 'classbranch.slavicmyths.bogatyr.effect': ('Удар с воздуха: бонус урона и время выполнения приёма +%s%%.', 'Aerial Strike: damage bonus and maneuver window +%s%%.'), 'classbranch.slavicmyths.ratoborets.role': ('Продолжительный ближний бой через ускоренные атаки.', 'Sustained melee combat through faster attacks.'), 'classbranch.slavicmyths.ratoborets.effect': ('Сеча: бонус скорости атаки и длительность +%s%%.', 'Flurry: attack speed bonus and duration +%s%%.'), 'classbranch.slavicmyths.schitonosets.role': ('Контроль врага щитом и более сильное отбрасывание.', 'Shield control with stronger knockback.'), 'classbranch.slavicmyths.schitonosets.effect': ('Таран щитом: отбрасывание и длительность контроля +%s%%. Урон удара не увеличивается.', 'Shield Ram: knockback and control duration +%s%%. Hit damage is unchanged.'), 'classbranch.slavicmyths.voevoda.role': ('Выживание при низком здоровье за счёт более долгой защиты.', 'Low-health survival through longer protection.'), 'classbranch.slavicmyths.voevoda.effect': ('Второе дыхание: длительность защиты при низком здоровье +%s%%.', 'Second Wind: low-health protection duration +%s%%.'), 'classbranch.slavicmyths.volhv.role': ('Защитная магия: оберег поглощает больше урона и держится дольше.', 'Protective magic: a ward absorbs more damage and lasts longer.'), 'classbranch.slavicmyths.volhv.effect': ('Оберег: объём поглощаемого урона и длительность +%s%%.', 'Ward: absorption amount and duration +%s%%.'), 'classbranch.slavicmyths.koldun.role': ('Проклятия: ослабление и замедление цели действуют дольше.', 'Curses: weakness and slowness last longer.'), 'classbranch.slavicmyths.koldun.effect': ('Порча: длительность ослабления и замедления +%s%%.', 'Hex: weakness and slowness duration +%s%%.'), 'classbranch.slavicmyths.oberezhnik.role': ('Развитие защитного оберега и доступ к его эволюции.', 'Further ward protection and access to its evolution.'), 'classbranch.slavicmyths.oberezhnik.effect': ('Оберег: объём поглощаемого урона и длительность +%s%%.', 'Ward: absorption amount and duration +%s%%.'), 'classbranch.slavicmyths.znakhar.role': ('Лечение травяными продуктами мода через пассивное Знахарство.', 'Healing with the mod’s herbal foods through the Herbalism passive.'), 'classbranch.slavicmyths.znakhar.effect': ('Знахарство: дополнительное лечение от травяных продуктов +%s%%.', 'Herbalism: additional healing from herbal foods +%s%%.'), 'classbranch.slavicmyths.morovik.role': ('Долгое удержание цели под ослаблением и замедлением Порчи.', 'Keeping enemies weakened and slowed by Hex for longer.'), 'classbranch.slavicmyths.morovik.effect': ('Порча: длительность ослабления и замедления +%s%%.', 'Hex: weakness and slowness duration +%s%%.'), 'classbranch.slavicmyths.chernoknizhnik.role': ('Контроль преследователей с помощью Сонной дрёмы.', 'Controlling pursuers with Slumber.'), 'classbranch.slavicmyths.chernoknizhnik.effect': ('Сонная дрёма: длительность контроля +%s%%. Ограничения для PvP и боссов сохраняются.', 'Slumber: control duration +%s%%. PvP and boss restrictions still apply.'), 'classbranch.slavicmyths.ushkuinik.role': ('Стрельба по недавно поражённой цели с помощью Зоркого глаза.', 'Archery against recently hit targets with Keen Eye.'), 'classbranch.slavicmyths.ushkuinik.effect': ('Зоркий глаз: сила доводки стрелы и время подготовки +%s%%.', 'Keen Eye: arrow correction strength and preparation window +%s%%.'), 'classbranch.slavicmyths.glavar.role': ('Подготовленная атака сзади или сбоку через Подлый удар.', 'Prepared attacks from behind or the side through Dirty Strike.'), 'classbranch.slavicmyths.glavar.effect': ('Подлый удар: бонус урона и время подготовки +%s%%.', 'Dirty Strike: damage bonus and preparation window +%s%%.'), 'classbranch.slavicmyths.naletchik.role': ('Манёвры и отход: более дальний Рывок и доступ к его эволюции.', 'Movement and escape: a longer Dash and access to its evolution.'), 'classbranch.slavicmyths.naletchik.effect': ('Рывок: дальность перемещения +%s%%. Не даёт неуязвимости.', 'Dash: movement distance +%s%%. Does not grant invulnerability.'), 'classbranch.slavicmyths.strelok.role': ('Более сильная доводка стрелы Зорким глазом и его эволюция.', 'Stronger Keen Eye arrow correction and access to its evolution.'), 'classbranch.slavicmyths.strelok.effect': ('Зоркий глаз: сила доводки стрелы и время подготовки +%s%%.', 'Keen Eye: arrow correction strength and preparation window +%s%%.'), 'classbranch.slavicmyths.ataman.role': ('Отход от преследования с более долгим ускорением Дымового ухода.', 'Escaping pursuit with a longer Smoke speed boost.'), 'classbranch.slavicmyths.ataman.effect': ('Дымовой уход: длительность ускорения при отходе +%s%%.', 'Smoke: escape speed boost duration +%s%%.'), 'classbranch.slavicmyths.reznik.role': ('Добивание: усиление бонуса урона по цели с низким здоровьем.', 'Finishing enemies: a stronger damage bonus against low-health targets.'), 'classbranch.slavicmyths.reznik.effect': ('Хладнокровие: величина бонуса урона +%s%% по цели с менее чем 35%% здоровья.', 'Cold Blood: damage bonus increased by %s%% against targets below 35%% health.')}
UI.update({'branch_skill': ('Усиливает навык «%s». Сам навык не выдаётся.', 'Enhances %s. The skill itself is not granted.'), 'branch_unlock': ('Открывает для изучения: %s.', 'Available to learn: %s.'), 'branch_unlock_requirement': ('Изучается отдельно: %s ранга %s, уровень %s и 1 очко.', 'Learn separately: %s rank %s, level %s and 1 point.'), 'branch_future': ('На уровне 25 — дальнейший выбор: %s.', 'At level 25, further choices: %s.'), 'branch_second_passive': ('Открывает второй пассивный слот. Навык назначается отдельно.', 'Unlocks the second passive slot. Assign a skill separately.'), 'branch_mastery': ('Будущее мастерство: %s — уровень 40 и испытание.', 'Future mastery: %s — level 40 and a trial.')})

UI.update({'branch_level_short':('Ур. %s · ваш %s','Lv %s · yours %s')})

def icon(name):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 edge='#252329';steel='#d9ded7';shadow='#76868b';gold='#c9a762';wood='#815932';green='#92ab74';red='#b86150';purple='#a48aa9'
 def line(points,c=steel,w=2):d.line(points,fill=c,width=w)
 def blade(a=(8,24),b=(24,8),wide=4):
  line([a,b],edge,wide+2);line([a,b],shadow,wide);line([(a[0]+1,a[1]-1),(b[0]+1,b[1]-1)],steel,max(1,wide//2));line([(a[0]-4,a[1]-3),(a[0]+3,a[1]+4)],gold,2);line([(a[0]-3,a[1]+3),(a[0]-6,a[1]+6)],wood,3)
 def shield(x=8,y=5):
  d.polygon([(x,y),(x+16,y),(x+15,y+14),(x+8,y+23),(x+1,y+14)],fill=edge);d.polygon([(x+2,y+2),(x+14,y+2),(x+13,y+13),(x+8,y+20),(x+3,y+13)],fill=gold);d.polygon([(x+5,y+5),(x+11,y+5),(x+10,y+13),(x+8,y+16),(x+6,y+13)],fill='#464b48');line([(x+8,y+5),(x+8,y+16)],steel,1)
 def eye(x=3,y=9):
  d.polygon([(x,y+6),(x+6,y+1),(x+17,y+1),(x+25,y+6),(x+18,y+12),(x+7,y+12)],fill=edge);d.polygon([(x+2,y+6),(x+7,y+3),(x+17,y+3),(x+22,y+6),(x+17,y+9),(x+8,y+9)],fill=steel);d.ellipse((x+10,y+2,x+16,y+10),fill=gold);d.rectangle((x+12,y+3,x+14,y+9),fill=edge)
 def boot(x=9,y=4):d.polygon([(x,y),(x+7,y),(x+7,y+15),(x+15,y+17),(x+15,y+21),(x-2,y+21),(x-2,y+16),(x,y+12)],fill=edge);d.polygon([(x+1,y+2),(x+5,y+2),(x+5,y+17),(x+13,y+18),(x+13,y+19),(x,y+19),(x,y+16),(x+1,y+13)],fill=wood);line([(x+1,y+5),(x+5,y+5)],gold,2)
 def sign(c=green):line([(16,3),(27,15),(16,27),(5,15),(16,3)],edge,5);line([(16,4),(26,15),(16,26),(6,15),(16,4)],c,2);line([(16,8),(16,23)],gold,2);line([(10,15),(22,15)],gold,2)
 base={'onslaught':'lunge','bulwark':'shield_ram','greater_ward':'ward','greater_hex':'hex','true_shot':'keen_eye','raider_dash':'dash'}.get(name,name)
 if base=='druzhinnik':shield(5,3);blade((22,22),(27,4),3)
 elif base=='vedun':d.polygon([(16,1),(29,14),(24,28),(8,28),(3,14)],fill=edge);sign();line([(5,28),(27,28)],gold,2)
 elif base=='razboinik':d.polygon([(11,2),(23,2),(29,19),(25,28),(6,28),(3,19)],fill='#4c5157');eye(3,9);line([(7,25),(25,25)],red,2)
 elif base=='lunge':blade((8,23),(27,6));line([(2,12),(11,8)],gold);line([(2,17),(13,13)],steel,1)
 elif base=='flurry':blade((12,23),(21,4));d.arc((1,2,29,30),180,310,fill=gold,width=2);line([(27,25),(29,29),(24,28)],gold)
 elif base=='trip':line([(12,3),(12,16),(7,27)],wood,4);line([(23,3),(23,17),(28,26)],gold,4);blade((6,21),(26,16),3);line([(2,17),(5,17)],red,2)
 elif base=='aerial_strike':blade((18,11),(18,28),4);line([(4,5),(16,5)],gold,2);line([(8,3),(16,5),(8,8)],gold,2);line([(25,5),(25,18)],steel,1);d.polygon([(22,17),(28,17),(25,23)],fill=steel)
 elif base=='shield_ram':shield(10,2);line([(1,9),(8,9)],steel,2);line([(1,15),(8,15)],gold,2);d.polygon([(26,3),(30,1),(29,7)],fill=gold)
 elif base=='temper':d.polygon([(3,11),(4,5),(10,3),(16,8),(22,3),(28,5),(29,11),(26,20),(16,29),(6,20)],fill=edge);d.polygon([(5,11),(6,7),(10,6),(16,11),(22,6),(26,7),(27,11),(24,19),(16,25),(8,19)],fill=red);line([(8,8),(11,8),(13,10)],steel,2);line([(3,23),(8,25)],gold,2)
 elif base=='steadfast':shield(4,1);boot(19,12);line([(2,30),(30,30)],shadow,2)
 elif base=='riposte':blade((18,24),(18,4),4);line([(6,5),(3,11),(5,18),(11,21)],gold,2);d.polygon([(10,17),(15,22),(8,24)],fill=gold)
 elif base=='second_wind':d.ellipse((2,4,28,27),outline=shadow,width=2);line([(3,15),(9,15),(12,8),(16,24),(20,12),(27,12)],red,3);line([(8,3),(13,1)],gold,2)
 elif base=='binding_sign':sign();line([(1,2),(8,6)],steel);line([(31,2),(24,6)],steel);line([(1,29),(8,23)],steel);line([(31,29),(24,23)],steel)
 elif base=='ward':d.arc((9,1,22,12),180,360,fill=shadow,width=2);d.polygon([(16,8),(27,17),(16,29),(5,17)],fill=edge);d.polygon([(16,11),(24,17),(16,25),(8,17)],fill=gold);line([(16,14),(16,22)],green,3);d.arc((3,7,29,31),190,350,fill=steel,width=1)
 elif base=='hex':sign(purple);line([(15,2),(12,11),(19,17),(15,30)],edge,3);d.rectangle((22,24,27,27),fill=purple);line([(2,12),(5,14)],red,2)
 elif base=='cleanse':sign(steel);line([(3,2),(7,6)],gold,2);line([(24,25),(29,30)],gold,2);line([(1,17),(5,17)],green);line([(27,13),(31,13)],green)
 elif base=='slumber':d.polygon([(23,2),(15,6),(13,14),(18,22),(27,25),(19,30),(9,27),(3,18),(4,9),(11,3)],fill=purple);line([(12,5),(7,10),(7,18)],steel,1);line([(20,10),(27,10),(20,16),(27,16)],gold,2)
 elif base=='ward_power':d.polygon([(16,3),(28,16),(16,29),(4,16)],fill=edge);d.polygon([(16,6),(24,16),(16,25),(8,16)],fill=gold);d.rectangle((13,11,19,21),fill=green);line([(2,3),(2,10)],steel,1);line([(29,21),(29,29)],steel,1)
 elif base=='herbalism':d.rectangle((6,19,25,27),fill=edge);d.rectangle((8,20,23,25),fill=wood);line([(16,20),(16,4)],green,3);d.polygon([(14,12),(4,5),(5,13)],fill=green);d.polygon([(18,12),(26,3),(27,11)],fill=green);line([(8,21),(12,21)],gold,1)
 elif base=='spirit_sense':d.polygon([(11,3),(21,3),(27,11),(25,27),(21,23),(17,29),(12,24),(6,28),(5,12)],fill=steel);d.rectangle((10,10,12,14),fill=edge);d.rectangle((20,10,22,14),fill=edge);d.arc((1,0,31,31),210,295,fill=purple,width=2)
 elif base=='last_rite':d.rectangle((4,27,27,30),fill=wood);d.rectangle((11,14,21,26),fill=steel);d.polygon([(16,2),(23,10),(20,14),(13,14),(10,10)],fill=gold);d.polygon([(16,5),(19,10),(16,12),(14,10)],fill=red);line([(16,15),(16,24)],shadow,1)
 elif base=='keen_eye':eye();line([(8,27),(25,27)],gold,2);d.polygon([(23,24),(29,27),(23,30)],fill=steel)
 elif base=='dash':boot(12,4);line([(1,12),(8,12)],steel,2);line([(3,19),(9,19)],gold,2);line([(4,26),(8,26)],shadow,2)
 elif base=='dirty_strike':d.rectangle((7,3,12,8),fill=shadow);line([(10,9),(10,20),(5,29)],wood,4);line([(10,20),(17,29)],wood,4);blade((21,20),(28,5),3);d.rectangle((4,12,5,18),fill=red)
 elif base=='net':d.polygon([(6,4),(26,7),(29,24),(7,29),(2,13)],fill=edge);d.line([(6,4),(26,7),(29,24),(7,29),(2,13),(6,4)],fill=gold,width=2)
 if base=='net':
  for n in range(6,27,5):line([(4,n),(26,n+2)],wood,1);line([(n,6),(n+1,27)],gold,1)
 elif base=='smoke':
  for box in [(2,19,15,30),(6,11,24,27),(17,3,30,17)]:d.ellipse(box,fill=edge);d.ellipse((box[0]+2,box[1]+2,box[2]-2,box[3]-2),fill=shadow)
  d.rectangle((17,14,20,18),fill=gold);line([(18,18),(15,25),(23,28)],wood,3);line([(7,28),(12,28)],steel,1)
 elif base=='light_step':boot(9,3);d.polygon([(3,3),(2,14),(8,9)],fill=steel);line([(3,7),(5,9)],shadow,1)
 elif base=='steady_aim':d.ellipse((5,5,26,26),outline=gold,width=2);line([(16,1),(16,30)],shadow,1);line([(1,16),(30,16)],shadow,1);d.ellipse((13,13,19,19),fill=red);line([(6,5),(9,5)],steel,1)
 elif base=='cold_blood':d.polygon([(16,2),(27,19),(25,26),(17,30),(8,26),(6,19)],fill=edge);d.polygon([(16,6),(24,19),(23,24),(17,27),(10,24),(9,19)],fill=red);line([(16,11),(16,22)],steel,1);line([(11,16),(21,16)],steel,1)
 elif base=='hunters_fervor':d.polygon([(16,2),(24,7),(18,12),(27,10),(23,18),(30,18),(9,30),(12,22),(3,24),(8,15),(2,14),(12,8)],fill=gold);line([(8,19),(20,13)],red,2);d.polygon([(19,11),(25,11),(21,16)],fill=steel)
 elif base=='opportunity':d.ellipse((3,3,28,28),outline=gold,width=2);line([(16,7),(16,16),(24,16)],steel,2);blade((7,26),(16,22),2);d.rectangle((3,2,8,5),fill=shadow)
 if name!=base:d.polygon([(23,1),(31,1),(31,9),(27,12),(23,9)],fill=edge);d.polygon([(25,3),(29,3),(29,8),(27,10),(25,8)],fill=gold);line([(27,3),(27,8)],steel,1)
 if name in definitions.NAMES and name in ('druzhinnik','vedun','razboinik'):im=im.resize((64,64),Image.Resampling.NEAREST)
 return im

def sprites():
 target=OUT/'ui';target.mkdir(parents=True,exist_ok=True)
 for state,border in {'normal':'#7e6240','hover':'#c0a168','pressed':'#8d724b','disabled':'#544b3e','selected':'#d6b46d'}.items():
  im=Image.new('RGBA',(96,24),'#30251b');d=ImageDraw.Draw(im);d.rectangle((0,0,95,23),outline='#171410');d.rectangle((1,1,94,22),outline=border)
  for y in range(5,20,4):d.line((5,y,90,y),fill='#382a1d');d.line((12+y,y,22+y,y),fill='#463421')
  for x,y in [(3,3),(92,3),(3,20),(92,20)]:d.point((x,y),fill=border)
  if state=='pressed':d.line((2,2,93,2),fill='#171410',width=2)
  if state=='disabled':im=Image.alpha_composite(im,Image.new('RGBA',im.size,(0,0,0,45)))
  im.save(target/f'button_{state}.png')
 for state,c in {'locked':'#605446','available':'#d6b46d','learned':'#ba9253','selected':'#f0dfaf'}.items():
  size=48 if state=='selected' else 44;im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
  if state!='selected':d.rectangle((1,1,size-2,size-2),fill='#211c17');d.rectangle((3,3,size-4,size-4),outline='#655035');d.rectangle((5,5,size-6,size-6),fill='#30271e')
  d.rectangle((0,0,size-1,size-1),outline=c,width=2 if state=='selected' else 1)
  for x,y in [(2,2),(size-5,2),(2,size-5),(size-5,size-5)]:d.rectangle((x,y,x+2,y+2),fill=c)
  if state=='learned':d.line((5,size-4,size-6,size-4),fill=c,width=2)
  im.save(target/f'node_{state}.png')

def main():
 OUT.mkdir(parents=True,exist_ok=True)
 for name in ('druzhinnik','vedun','razboinik',*definitions.SKILLS):icon(name).save(OUT/f'{name}.png')
 sprites()
 for index,language in enumerate(('ru_ru','en_us')):
  path=ASSETS/f'lang/{language}.json';data=json.loads(path.read_text(encoding='utf-8'));data.update({'classes.slavicmyths.'+k:v[index] for k,v in UI.items()});data.update({k:v[index] for k,v in BRANCH_COPY.items()})
  for i,key in enumerate(('path_ability','class_slot_2','class_slot_3')):data['key.slavicmyths.'+key]=('Активный навык '+['I','II','III'][i] if index==0 else 'Active skill '+['I','II','III'][i])
  path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 sheet=Image.new('RGBA',(8*64,5*72),'#211c17');d=ImageDraw.Draw(sheet)
 for n,name in enumerate(definitions.SKILLS):sheet.alpha_composite(icon(name).resize((48,48),Image.Resampling.NEAREST),(n%8*64+8,n//8*72+3));d.text((n%8*64+2,n//8*72+53),name[:10],fill='#eadbb9')
 sheet.save(ROOT/'docs/media/class-icons-1323.png');print('Class UI: 34 native 32px skills, 3 crests, 9 GUI sprites and bilingual text.')
if __name__=='__main__':main()
