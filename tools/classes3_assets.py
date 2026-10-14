"""Reproduce only Classes 3.0 UI assets; never edits weapon art or gameplay."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
names=[
 ('gromoboy','earth_thunder','Громобой','Earthbreaker','Земной гром','Earth Thunder','Прыжок и ударная волна при приземлении: 9 HP, радиус 4, до 6 врагов.','Jump and landing shockwave: 9 HP, radius 4, up to 6 foes.'),
 ('master_sechi','unyielding_flurry','Мастер сечи','Master of the Fray','Неудержимая сеча','Unyielding Flurry','6 с: до 5 ручных попаданий создают дугу 40% фактического урона, до 3 дополнительных врагов.','6s: up to 5 melee hits create arcs for 40% actual damage, up to 3 extra foes.'),
 ('nesokrushimy','living_wall','Несокрушимый','Unbreakable','Живая стена','Living Wall','6 с с поднятым щитом: передний сектор 120°, защита себя и союзников позади на 30%, общий пул 20 HP.','6s while blocking: frontal 120-degree sector, 30% protection for you and allies behind you; shared 20 HP pool.'),
 ('verkhovny_voevoda','banner_of_druzhina','Верховный воевода','High Voivode','Знамя дружины','Banner of the Druzhina','12 с, радиус 10: вы и до 5 союзников получаете +10% урона и защиту 8%.','12s, radius 10: you and up to 5 allies gain +10% damage and 8% protection.'),
 ('khranitel_kruga','sacred_circle','Хранитель круга','Keeper of the Circle','Священный круг','Sacred Circle','Неподвижный круг радиусом 5 на 10 с. Защита союзников 20%, общий пул 24 HP.','Stationary radius-5 circle for 10s. Allies take 20% less damage; shared 24 HP pool.'),
 ('zhivotvorets','living_water','Животворец','Lifegiver','Живая вода','Living Water','Волна радиусом 8: до 5 союзников, до 6 HP настоящего лечения и снятие одного обычного недуга.','Radius-8 wave: up to 5 allies regain up to 6 missing HP and lose one ordinary ailment.'),
 ('povelitel_mora','plague_seal','Повелитель мора','Plague Lord','Моровая печать','Plague Seal','Метка на 8 с: 1 HP каждые 2 с, ослабление до 4 врагов рядом с целью.','8s mark: 1 HP every 2s; weakens up to 4 nearby foes.'),
 ('tainoved','shadow_pact','Тайновед','Secret Keeper','Теневая сделка','Shadow Pact','Цена 4 HP, остаток не менее 4 HP. Теневой снаряд: 10 HP, область 2,5 блока, до 4 целей.','Costs 4 HP, leaving at least 4 HP. Shadow projectile: 10 HP, radius 2.5, up to 4 foes.'),
 ('vikhrevoy_naletchik','piercing_breakthrough','Вихревой налётчик','Whirlwind Raider','Сквозной прорыв','Piercing Breakthrough','Два рывка по 3 блока за 3 с. Каждый наносит 3 HP до 3 врагам на пути. Стены останавливают движение.','Two 3-block dashes within 3s; each deals 3 HP to up to 3 foes along the path. Walls stop movement.'),
 ('sokoliny_glaz','vulnerable_point','Соколиный глаз','Falcon Eye','Уязвимая точка','Vulnerable Point','Прицельная метка: следующая настоящая стрела по цели за 6 с получает +6 HP и до 20% компенсации брони; против боссов +3 HP без пробития.','Aimed mark: next real arrow hitting the target within 6s gains +6 HP and up to 20% armor compensation; bosses: +3 HP, no penetration.'),
 ('tenevoy_ataman','false_ambush','Теневой атаман','Shadow Ataman','Ложная засада','False Ambush','На 6 с отвлекает до 3 обычных преследователей. Первый удар союзника по каждой отвлечённой цели сильнее на 10%.','Distracts up to 3 ordinary pursuers for 6s. First allied hit on each distracted foe gains 10% damage.'),
 ('krovnik','blood_mark','Кровник','Bloodbound','Кровавая метка','Blood Mark','Метка на 10 с: до 4 зарядов от ударов, кровотечение суммарно до 8 HP и один бонус 5 HP при здоровье цели ниже 25%. Нежить и бесплотные невосприимчивы.','10s mark: up to 4 melee charges, up to 8 total bleed HP and one +5 HP hit below 25% target health. Undead and bloodless foes are immune.'),
]
passives=[('battle_discipline','Боевая дисциплина','Battle Discipline','Перезарядка подсечки и удара щитом: −3/5/7%.','Trip and shield ram cooldown: -3/5/7%.'),('iron_will','Железная воля','Iron Will','После настоящего блока: следующий урон за 2 с меньше на 6/9/12%. Перезарядка 20 с.','After a real block: next damage within 2s reduced by 6/9/12%. 20s cooldown.'),('spiritual_fortitude','Духовная стойкость','Spiritual Fortitude','Новые обычные недуги короче на 10/15/20%. Сюжетные эффекты защищены.','New ordinary ailments last 10/15/20% less. Story effects are protected.'),('sign_mastery','Сила знаков','Sign Mastery','Длительность собственных знака связывания и проклятия +8/12/16%, с сохранением защиты боссов.','Binding sign and hex duration +8/12/16%, preserving boss protections.'),('ambush_master','Мастер засады','Ambush Master','Первый удар по неготовому врагу +8/12/16%, общий предел начальных усилений 80%.','First hit on an unaware foe +8/12/16%; combined opener bonus capped at 80%.'),('survival_instinct','Инстинкт выживания','Survival Instinct','После потери не менее 6 HP: скорость +10/15/20% на 3 с. Перезарядка 60 с.','After losing at least 6 HP: +10/15/20% speed for 3s. 60s cooldown.')]
trials=[('bogatyr','В 3 схватках поразить воздушным приземлением не менее 2 врагов.','Hit 2 foes in one aerial landing in 3 encounters.'),('ratoborets','В 5 схватках нанести 3 ручных удара за 8 с.','Land 3 melee hits within 8s in 5 encounters.'),('schitonosets','Щитом заблокировать 35 HP в 3 схватках.','Block 35 HP with a shield across 3 encounters.'),('voevoda','В 3 схватках устранить врага, угрожающего видимому союзнику или жителю.','Eliminate a threat to a visible ally or villager in 3 encounters.'),('oberezhnik','Оберегом поглотить 30 HP в 3 схватках.','Absorb 30 HP with ward across 3 encounters.'),('znakhar','Восстановить 35 HP после реального вражеского урона.','Restore 35 HP lost to actual hostile damage.'),('morovik','Проклясть 15 разных врагов за 3 схватки.','Hex 15 different foes across 3 encounters.'),('chernoknizhnik','В 3 схватках проклясть и затем усыпить врага за 10 с.','Hex then slumber a foe within 10s in 3 encounters.'),('naletchik','Рывком уйти от настоящей ближней атаки в 10 схватках.','Dash away from an actual melee threat in 10 encounters.'),('strelok','Поразить стрелами 15 разных врагов с расстояния от 16 блоков.','Hit 15 distinct foes with arrows from at least 16 blocks.'),('ataman','В 5 схватках дымом разорвать преследование минимум 2 врагов.','Break pursuit of at least 2 foes using smoke in 5 encounters.'),('reznik','В 15 схватках нанести грязный удар с фланга или тыла.','Land a dirty strike from flank or rear in 15 encounters.')]
ui={
 'xp_gain':('+%s опыта класса','+%s Class XP'),'mastery':('Мастерство IV','Mastery IV'),'passive_second':('Пассивный II','Passive II'),'passive_locked':('Второй пассивный слот: уровень 25 и выбранная III ветвь.','Second passive slot: level 25 and a chosen stage III branch.'),'passive_open':('Два пассивных слота открыты.','Two passive slots unlocked.'),'ultimate_key':('Высшее умение: %s','Ultimate: %s'),'claim_mastery':('Завершить испытание','Claim mastery'),'trial_progress':('Испытание: %s / %s','Trial: %s / %s'),'trial_encounters':('Подтверждено схваток: %s. Новая схватка после 10 с без боя.','Confirmed encounters: %s. New encounter after 10s out of combat.'),'ultimate_status':('%s · перезарядка %s с','%s · cooldown %ss'),'ultimate_catalogue':('Высшие классы и умения (PvE)','Higher classes and abilities (PvE)'),'ultimate_numbers':('КД %s с · срок %s с · радиус %s · сила %s','CD %ss · duration %ss · radius %s · power %s'),'stage_base':('I: %s','I: %s'),'stage_first':('II: %s','II: %s'),'stage_second':('III: %s','III: %s'),'stage_third':('IV: %s','IV: %s'),'ultimate_locked':('Нужны уровень 40, выбранная III ветвь и завершённое испытание.','Requires level 40, stage III and a completed trial.'),'ultimate_state':('Умение недоступно в текущем состоянии.','Cannot use this ability in the current state.'),'ultimate_cooldown':('Высшее умение восстанавливается.','Ultimate is on cooldown.'),'ultimate_position':('Нужна безопасная позиция на земле рядом с врагом.','Requires a safe grounded position near a foe.'),'ultimate_weapon':('Требуется оружие ближнего боя.','Requires a melee weapon.'),'ultimate_shield':('Поднимите настоящий щит.','Raise a real shield.'),'ultimate_target':('Нет подходящей цели или полезного эффекта.','No eligible target or useful effect.'),'ultimate_health':('Нужно не менее 8 HP для безопасной сделки.','Requires at least 8 HP for a safe pact.')}
for language,col in [('ru_ru',0),('en_us',1)]:
 p=ASSETS/'lang'/f'{language}.json';data=json.loads(p.read_text(encoding='utf-8'))
 for key,values in ui.items():data['classes.slavicmyths.'+key]=values[col]
 data['key.slavicmyths.class_ultimate']=('Высшее классовое умение','Class ultimate')[col]
 for cls,skill,ru,en,sru,sen,dru,den in names:
  data['class.slavicmyths.'+cls]=(ru,en)[col];data['classskill.slavicmyths.'+skill]=(sru,sen)[col];data['classskill.slavicmyths.'+skill+'.description']=(dru,den)[col]
 for skill,ru,en,dru,den in passives:
  data['classskill.slavicmyths.'+skill]=(ru,en)[col];data['classskill.slavicmyths.'+skill+'.description']=(dru,den)[col]
 for branch,ru,en in trials:data['classtrial.slavicmyths.'+branch]=(ru,en)[col]
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def icon(index,emblem=False):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);gold='#d8b76b';ivory='#ece2c0';dark='#4d3a37';blue='#81b6c3';red='#b6594b';green='#78a47a'
 if emblem:d.polygon([(16,0),(29,6),(28,24),(16,31),(3,24),(2,6)],outline=gold,width=2)
 if index==0: # split ground and forked lightning
  d.polygon([(18,3),(10,15),(16,15),(13,25),(23,11),(17,11)],fill=gold);d.line([(3,25),(10,22),(16,28),(22,22),(29,25)],fill=dark,width=3)
 elif index==1: # crossing curved blades and sweeping arc
  d.arc((3,3,29,29),200,345,fill=gold,width=3);d.polygon([(6,5),(11,7),(24,24),(21,26),(8,12)],fill=ivory);d.polygon([(26,5),(21,7),(8,24),(11,26),(24,12)],fill=blue)
 elif index==2: # broad shield and masonry
  d.polygon([(6,5),(26,5),(25,19),(16,28),(7,19)],fill=blue,outline=ivory,width=2);d.line((16,7,16,23),fill=gold,width=3);d.line((9,13,23,13),fill=gold,width=3)
 elif index==3: # pennant on spear pole
  d.line((9,3,9,29),fill=ivory,width=2);d.polygon([(11,5),(27,5),(23,11),(27,17),(11,17)],fill=red);d.line((14,8,20,14),fill=gold,width=2)
 elif index==4: # ritual circle and four stones
  d.ellipse((5,9,27,26),outline=blue,width=3);d.polygon([(16,4),(19,13),(16,18),(13,13)],fill=gold);[d.rectangle((x,y,x+3,y+4),fill=ivory) for x,y in [(3,14),(25,14),(14,25)]]
 elif index==5: # water droplet over bowl
  d.polygon([(16,3),(10,13),(10,17),(14,21),(19,20),(22,16),(21,12)],fill=blue);d.arc((5,16,27,29),0,180,fill=ivory,width=3);d.line((16,23,16,29),fill=gold,width=2)
 elif index==6: # seal tablet and spreading spores
  d.rectangle((8,7,24,24),fill=dark,outline=green,width=2);d.ellipse((12,11,20,19),outline=gold,width=2);[d.rectangle((x,y,x+2,y+2),fill=green) for x,y in [(3,3),(25,4),(3,24),(26,26)]]
 elif index==7: # open palm beneath crescent
  d.ellipse((14,3,26,15),fill=blue);d.ellipse((18,1,28,12),fill=(0,0,0,0));d.polygon([(5,25),(7,14),(10,14),(10,21),(19,17),(24,20),(17,26)],fill=ivory);d.rectangle((5,26,13,29),fill=dark)
 elif index==8: # two separate chevrons and speed trails
  d.line([(7,5),(17,15),(7,25)],fill=ivory,width=4);d.line([(17,5),(27,15),(17,25)],fill=blue,width=3);d.line((2,12,6,12),fill=gold,width=2)
 elif index==9: # eagle eye and diagonal arrow
  d.polygon([(3,14),(11,8),(20,8),(29,14),(20,20),(11,20)],outline=gold,width=2);d.ellipse((12,10,20,18),fill=blue);d.line((6,27,24,9),fill=ivory,width=2);d.polygon([(24,5),(24,12),(18,6)],fill=ivory)
 elif index==10: # paired masks with single smoke curl
  d.polygon([(4,8),(15,10),(14,22),(8,25),(3,19)],fill=dark,outline=ivory);d.polygon([(17,5),(28,7),(27,19),(21,22),(16,16)],fill=blue);d.rectangle((6,13,10,14),fill=red);d.rectangle((20,10,24,11),fill=gold);d.arc((5,23,26,31),180,330,fill=ivory,width=2)
 elif index==11: # hooked knife and blood drop
  d.polygon([(8,3),(14,7),(13,18),(8,23),(6,20),(10,15)],fill=ivory);d.rectangle((6,23,10,29),fill=dark);d.polygon([(23,14),(18,24),(20,28),(25,28),(27,24)],fill=red)
 elif index==12: # disciplined three spear heads
  for x in [7,16,25]:d.line((x,11,x,27),fill=dark,width=2);d.polygon([(x,3),(x-3,12),(x+3,12)],fill=ivory)
 elif index==13: # iron ring and clenched fist
  d.ellipse((3,3,29,29),outline=blue,width=3);d.rectangle((10,10,22,21),fill=ivory);d.line((13,8,13,15),fill=dark,width=2);d.line((18,8,18,15),fill=dark,width=2)
 elif index==14: # rooted spirit tree
  d.line((16,8,16,25),fill=gold,width=3);d.line([(5,13),(16,6),(27,13)],fill=green,width=3);d.line([(6,27),(16,23),(26,27)],fill=blue,width=2)
 elif index==15: # inscribed cross sign
  d.rectangle((5,5,27,27),outline=gold,width=2);d.line((9,10,23,22),fill=blue,width=3);d.line((23,10,9,22),fill=ivory,width=3);d.rectangle((14,2,18,6),fill=red)
 elif index==16: # hood with hidden dagger
  d.polygon([(16,3),(26,14),(25,26),(7,26),(6,14)],fill=dark,outline=blue,width=2);d.line((12,14,20,14),fill=gold,width=2);d.polygon([(22,17),(27,25),(21,23)],fill=ivory)
 else: # winged boot
  d.polygon([(14,5),(22,5),(21,21),(27,23),(26,27),(11,27),(11,19)],fill=dark,outline=ivory,width=2);d.line([(14,12),(5,8),(8,14),(3,14),(12,20)],fill=blue,width=3)
 return im
out=ASSETS/'textures/gui/classes';out.mkdir(parents=True,exist_ok=True)
for i,row in enumerate(names):
 icon(i).save(out/(row[1]+'.png'));icon(i,True).save(out/(row[0]+'.png'))
for i,row in enumerate(passives):icon(12+i).save(out/(row[0]+'.png'))
preview=Image.new('RGBA',(6*128,5*128),'#24252a')
for i,p in enumerate([out/(row[1]+'.png') for row in names]+[out/(row[0]+'.png') for row in names]+[out/(row[0]+'.png') for row in passives]):preview.paste(Image.open(p).resize((96,96),Image.Resampling.NEAREST),(i%6*128+16,i//6*128+16),Image.open(p).resize((96,96),Image.Resampling.NEAREST))
dest=ROOT/'docs/implementation/classes3';dest.mkdir(parents=True,exist_ok=True);preview.save(dest/'icons.png')
print('Classes 3.0: 30 icons and RU/EN keys generated')
tag=ROOT/'src/main/resources/data/slavicmyths/tags/entity_type/class_bloodless.json'
tag.parent.mkdir(parents=True,exist_ok=True)
tag.write_text(json.dumps({'replace':False,'values':['minecraft:blaze','minecraft:breeze','minecraft:ghast','minecraft:vex','minecraft:slime','minecraft:magma_cube',{'id':'slavicmyths:nav','required':False}]},indent=2)+'\n',encoding='utf-8')
