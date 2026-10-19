"""Curated semantic choices. No filename-driven automatic replacement of voices."""
PLAN={};ALIASES={};BLOCKED={};NEW=set()
def add(group,event,sources,ru,en,cap=.6,rms=.08,**kw):
 assert event not in PLAN and event not in ALIASES
 PLAN[event]=dict(group=group,event=event,sources=sources,ru=ru,en=en,cap=cap,rms=rms,**kw)
def alias(group,event,target,reason):ALIASES[event]=dict(group=group,event=event,target=target,reason=reason)
def a(stem):return 'alba__'+stem+'.wav'
def w(n):return f'wood__crack{n:02d}.mp3.flac'
def wood(n):return f'wood__impactwood{n:02d}.mp3.flac'
def leaf(n):return f'rustle__rustle{n:02d}.flac'
def water(stem):return 'water__'+stem+'.ogg'
def glass(n):return f'break__bfh1_glass_breaking_{n:02d}.ogg'
def rock(n):return f'break__bfh1_rock_falling_{n:02d}.ogg'
def equip(name):return 'equipment__'+name
def female(t,stem):return f'female_t{t}__{stem}.wav'
def add_spirit(event,sources,ru,en,cap=.6,rms=.06,**kw):add('spirits',event,sources,ru,en,cap,rms,**kw)

# Domestic, forest, field and bath spirits. Material bodies remain material;
# an actual voice is never replaced by a label claiming that noise is singing.
add_spirit('domovoy_ambient',[a('Hmm_01'),a('Hmm_02')],'Домовой задумчиво ворчит','Domovoy murmurs',.8,.045)
add_spirit('domovoy_hurt',[a('(grunt)_01'),a('(grunt)_02')],'Домовой вскрикивает','Domovoy grunts in pain',.55)
add_spirit('domovoy_death',[a('(groan)_01')],'Домовой затихает','Domovoy falls silent',1.2)
add_spirit('domovoy_refuse',[a('Hmph_01')],'Домовой недоволен даром','Domovoy refuses the gift',.65);NEW.add('domovoy_refuse')
add_spirit('domovoy_thanks',[a('Hmm_03')],'Домовой одобрительно отзывается','Domovoy acknowledges the gift',.8,.045);NEW.add('domovoy_thanks')
for event,sources,ru,en,cap in [
 ('ambient',[wood(1),wood(2)],'Древесина Лешего скрипит','Leshy wood creaks',.9),
 ('hurt',[w(3),w(4)],'Древесина Лешего трескается','Leshy wood cracks',.5),
 ('death',[w(5)],'Леший рассыпается','Leshy collapses',1.1),
 ('angry',[w(6),w(7)],'Ветви Лешего напрягаются','Leshy branches strain',.65)]:add_spirit('leshy_'+event,sources,ru,en,cap)
for event,sources,ru,en,cap in [
 ('ambient',[leaf(1),leaf(2)],'Кикимора тихо шуршит','Kikimora rustles',.65),
 ('hurt',[female(2,'damaged1'),female(2,'damaged2')],'Кикимора вскрикивает','Kikimora cries out',.55),
 ('death',[[female(2,'damaged3'),leaf(3)]],'Кикимора исчезает','Kikimora vanishes',.8),
 ('angry',[female(2,'attack3')],'Кикимора угрожает','Kikimora threatens',.45),
 ('power',[leaf(4)],'Шорох Кикиморы','Kikimora stirs',.4),
 ('notice',[leaf(5)],'Кикимора выглядывает','Kikimora peeks out',.3),
 ('attack',[female(2,'attack1'),female(2,'attack2')],'Кикимора нападает','Kikimora attacks',.4),
 ('step',[leaf(6),leaf(7)],'Лёгкие шаги Кикиморы','Kikimora shuffles',.2)]:add_spirit('kikimora_'+event,sources,ru,en,cap,.04 if event in ['ambient','step'] else .065)
BLOCKED['kikimora_laugh']='Distinct real crooked female laugh not yet sourced; do not relabel an attack grunt as laughter.'
for event,sources,ru,en,cap in [
 ('ambient',[leaf(8),leaf(9)],'Колосья шелестят у Полудницы','Grain rustles around Poludnitsa',1),
 ('hurt',[female(3,'damaged1'),female(3,'damaged2')],'Полудница вскрикивает','Poludnitsa cries out',.55),
 ('death',[[female(3,'damaged3'),leaf(10)]],'Полудница рассеивается','Poludnitsa dissipates',1.0),
 ('angry',[female(3,'attack3')],'Полудница гневно выдыхает','Poludnitsa exhales sharply',.5),
 ('power',['wind__wind.wav'],'Ветер вокруг Полудницы усиливается','Wind gathers around Poludnitsa',1),
 ('notice',[leaf(11)],'Полудница приближается','Poludnitsa approaches',.35),
 ('transform',[leaf(12)],'Полудница меняет облик','Poludnitsa changes form',.8),
 ('attack',[female(3,'attack1'),female(3,'attack2')],'Полудница наносит удар','Poludnitsa strikes',.5)]:add_spirit('poludnitsa_'+event,sources,ru,en,cap,.04 if event=='ambient' else .065)
for event,sources,ru,en,cap in [
 ('ambient',[leaf(13),leaf(14)],'Трава шуршит у Полевика','Grass rustles around Polevik',.8),
 ('hurt',[a('(grunt)_03')],'Полевик вскрикивает','Polevik cries out',.45),
 ('death',[a('(groan)_02')],'Полевик затихает','Polevik falls silent',1),
 ('angry',[a('Hmph_03')],'Полевик недовольно ворчит','Polevik grumbles',.55),
 ('power',[leaf(15)],'Полевик поднимает траву','Polevik stirs the grass',.6),
 ('step',[leaf(16),leaf(17)],'Полевик шагает по траве','Polevik steps through grass',.2)]:add_spirit('polevik_'+event,sources,ru,en,cap,.035 if event in ['ambient','step'] else .06)
for event,sources,ru,en,cap in [
 ('ambient',[a('(breathing)_laboured')],'Банник тяжело дышит','Bannik breathes heavily',1.6),
 ('hurt',[a('(grunt)_04'),a('(grunt)_05')],'Банник хрипло вскрикивает','Bannik grunts in pain',.55),
 ('death',[a('(groan)_03')],'Банник испускает последний хрип','Bannik gasps his last',1.2),
 ('angry',[a('(breath out)_01')],'Банник сердито выдыхает','Bannik exhales angrily',.7),
 ('power',['steam__steam hisses - Marker #3.wav'],'Пар вырывается из бани','Steam escapes the bathhouse',.7),
 ('steam',['steam__steam hisses - Marker #1.wav','steam__steam hisses - Marker #2.wav'],'Банник выпускает пар','Bannik releases steam',1),
 ('attack',[a('(grunt)_06')],'Банник наносит удар','Bannik strikes',.45),
 ('step',[water('splash_01'),water('splash_02')],'Мокрые шаги Банника','Bannik splashes underfoot',.2)]:add_spirit('bannik_'+event,sources,ru,en,cap,.04 if event in ['ambient','step'] else .075)
for event,source in [('angry',equip('quiver-leather-squeeze-01.wav')),('power',equip('quiver-leather-squeeze-02.wav'))]:
 add_spirit('igosha_'+event,[source],'Игоша тревожно шуршит','Igosha rustles uneasily',.35,.04)
for suffix in ['ambient','hurt','death']:BLOCKED['igosha_'+suffix]='Fragile non-caricature vocal identity needs review; ordinary adult grunt is not an infant recording.'
for event,sources,ru,en in [
 ('stir',[wood(8)],'Яга помешивает в котле','Yaga stirs the cauldron'),
 ('open',['woodmetal__wood_squeak_01.ogg'],'Дверца Яги открывается','Yaga opens a wooden door'),
 ('close',['woodmetal__wood_close_01.ogg'],'Дверца Яги закрывается','Yaga closes a wooden door'),
 ('step',[wood(9),wood(10)],'Шаги Яги по дереву','Yaga steps on wood'),
 ('hut_creak',['woodmetal__wood_squeak_02.ogg'],'Избушка Яги скрипит','Yaga hut creaks'),
 ('hut_step',[wood(11),wood(12)],'Избушка Яги тяжело ступает','Yaga hut stomps')]:add_spirit('yaga_'+event,sources,ru,en,.75 if event=='hut_creak' else .4,.055)
# Existing Vodyanoy real calls only. Gift/dash/grab keep their actual timing;
# adding missing dedicated triggers is a later audio-only integration item.
for event,sources,ru,en,cap in [
 ('ambient',[[a('(breathing)_shallow'),water('bubble_01')]],'Водяной влажно дышит','Vodyanoy breathes through bubbles',1.4),
 ('angry',[[a('(breath out)_02'),water('bubble_02')]],'Водяной грозно выдыхает','Vodyanoy exhales a warning',.7),
 ('hurt',[[a('(grunt)_10'),water('splash_03')]],'Водяной хрипит от боли','Vodyanoy grunts in pain',.5),
 ('death',[water('bubble_03')],'Пузыри Водяного затихают','Vodyanoy bubbles fade',1.3)]:add_spirit('vodyanoy_'+event,sources,ru,en,cap,.05 if event=='ambient' else .075)
for cue,source,ru in [('idle',water('loop_bubbles_1'),'Старший Водяной выпускает пузыри'),('hurt',water('slime_01'),'Старший Водяной содрогается'),('death',water('slime_02'),'Старший Водяной оседает'),('attack',water('splash_04'),'Всплеск Старшего Водяного'),('heavy',water('splash_05'),'Тяжёлый удар воды'),('dash',water('splash_06'),'Водяной прорывает воду'),('wave',water('splash_07'),'Поднимается волна'),('pull',water('slime_03'),'Вода затягивает'),('phase',water('loop_bubbles_02'),'Вода вокруг Старшего бурлит')]:add('spirits','elder_'+cue,[source],ru,'Elder Vodyanoy: '+cue,.9 if cue!='idle' else 1.4,.065)

# Non-mythic wildlife: species-specific vanilla callbacks are legitimate KEEP/
# reference choices; no vanilla files are redistributed. Recorded calls augment
# the bear/boar identity. A cub is not represented by pitching the adult growl.
for event,target in [('ambient','ambient'),('hurt','hurt'),('death','death')]:alias('world','bear_'+event,'entity.polar_bear.'+target,'Natural species-family vanilla reference; actual new warning/roar recorded separately')
add('world','bear_alert',['bear__bear_01.flac'],'Медведь предупреждающе рычит','Bear warns',1.2,.1)
add('world','bear_roar',['bear__bear_02.flac'],'Медведь ревёт','Bear roars',1.5,.14)
for cue,target in [('ambient','entity.polar_bear.ambient_baby'),('alert','entity.polar_bear.ambient_baby'),('hurt','entity.polar_bear.hurt'),('death','entity.polar_bear.death')]:alias('world','cub_'+cue,target,'Baby-specific ambient recording; hurt/death adult-family fallback remains a child-timbre review item')
BLOCKED['cub_hurt']='Temporary vanilla bear fallback; genuine cub-specific hurt recording still needed.'
BLOCKED['cub_death']='Temporary vanilla bear fallback; genuine cub-specific death recording still needed.'
for cue,target in [('ambient','pant'),('alert','growl'),('hurt','hurt'),('death','death'),('roar','howl')]:alias('world','wolf_'+cue,'entity.wolf.'+target,'Ordinary forest wolf, natural vanilla canine callback; no boss pitch processing')
for cue,target in [('ambient','ambient'),('hurt','hurt'),('death','death')]:alias('world','boar_'+cue,'entity.pig.'+target,'Sus scrofa family vanilla reference, warning uses performed wild-boar imitation')
add('world','boar_alert',['boar__voice.ogg'],'Кабан предупреждающе хрюкает','Boar warns',.6,.09)
for kind in ['stag','doe']:
 source='elk__263487_2663250-hq.mp3' if kind=='stag' else 'deer__351113_4790684-hq.mp3'
 add('world',kind+'_ambient',[dict(key=source,start=0,end=2.5)],'Олень подаёт голос' if kind=='stag' else 'Олениха подаёт голос','Deer calls',1.4,.055)
 add('world',kind+'_alert',[dict(key=source,start=3,end=6)],'Тревожный зов оленя','Deer alarm call',1.2,.09)
 for cue in ['hurt','death']:BLOCKED[kind+'_'+cue]='Actual species call is not proof of hurt/death performance; original synthetic replacement needs suitable source.'
for kind in ['bear','cub','wolf','boar','stag','doe']:
 step={'bear':'polar_bear','cub':'polar_bear','wolf':'wolf','boar':'pig','stag':'horse','doe':'horse'}[kind]
 alias('world',kind+'_step','entity.'+step+'.step','Shared material footfall, separate from combat impact');NEW.add(kind+'_step')
 for cue,n in [('attack',1),('impact',2)]:add('world',kind+'_'+cue,[equip(f'body-{n}.flac')], 'Зверь наносит удар' if cue=='attack' else 'Удар зверя достигает цели','Animal strike' if cue=='attack' else 'Animal impact',.22,.075,shared_foley_reason='Common physical body contact prop, not an animal voice')
# The roar field exists for all kinds but only BEAR dispatches it. Preserve others
# as explicit inactive legacy entries rather than inventing extra animal actions.

# Human enemies: distinct ordinary human performances, no monster voice aliases.
for cue,sources,ru,en,cap in [
 ('idle',[a('Hmph_04'),a('Hmph_05')],'Разбойник ворчит','Bandit grumbles',.75),
 ('alert',[a('(attack)_01'),a('(attack)_02')],'Разбойник замечает врага','Bandit spots an enemy',.5),
 ('attack',[a('(attack)_03'),a('(attack)_04')],'Разбойник атакует','Bandit attacks',.35),
 ('hurt',[a('(grunt)_07'),a('(grunt)_08')],'Разбойник вскрикивает','Bandit grunts in pain',.5),
 ('death',[a('(groan)_10')],'Разбойник погибает','Bandit dies',1.2),
 ('command',[a('(attack)_05')],'Атаман подаёт боевой клич','Ataman rallies his men',.7),
 ('heavy',[equip('sword_clash.9.ogg'),equip('sword_clash.10.ogg')],'Тяжёлое оружие разбойника ударяет','Bandit heavy weapon strikes',.35)]:add('world','bandit_'+cue,sources,ru,en,cap,.07)

# Underground bodies: a stone guardian and a small subterranean creature are
# not voiced by the same generic monster. Material signals need no fake vocal.
for event,sources,ru in [
 ('underground.kladovik.idle',[rock(1),rock(2)],'Камни Кладовика поскрипывают'),
 ('underground.kladovik.hurt',[rock(3),rock(4)],'Камни Кладовика трескаются'),
 ('underground.kladovik.death',[rock(5)],'Кладовик обрушивается'),
 ('underground.kladovik.impact',[rock(6)],'Кладовик наносит удар'),
 ('underground.kladovik.rage',[rock(7)],'Кладовик сотрясает камни'),
 ('underground.kladovik.burrow',[rock(8)],'Кладовик уходит в землю'),
 ('underground.kladovik.stone',[rock(9)],'Кладовик бросает камень'),
 ('underground.kladovik.coins',[equip('belt-buckle-04.wav')],'Монеты Кладовика звенят'),
 ('underground.large_podzemnik.idle',[a('(breathing)_normal')],'Большой Подземник дышит'),
 ('underground.large_podzemnik.hurt',[a('(grunt)_11')],'Большой Подземник вскрикивает'),
 ('underground.large_podzemnik.death',[a('(groan)_11')],'Большой Подземник затихает'),
 ('underground.large_podzemnik.warning',[a('(breath out)_03')],'Подземник предупреждающе выдыхает'),
 ('underground.large_podzemnik.impact',[rock(3)],'Подземник ударяет по камню'),
 ('underground.large_podzemnik.rage',[a('(attack)_06')],'Подземник яростно вскрикивает'),
 ('underground.large_podzemnik.charge',[rock(4)],'Подземник разгоняется'),
 ('underground.podzemnik.idle',[leaf(18)],'Малый Подземник шуршит'),
 ('underground.podzemnik.hurt',[leaf(19)],'Малый Подземник содрогается'),
 ('underground.podzemnik.death',[leaf(20)],'Малый Подземник оседает'),
 ('underground.chud.work',[wood(13),wood(14)],'Чудь работает'),
 ('underground.chud.panic',[equip('boots-leather-jump-02.wav')],'Чудь поспешно отступает')]:add('world',event,sources,ru,event.split('.')[-1].replace('_',' '),.65,.065)
# Gorynych: three heads have separate breath/material signatures. Every event
# below already has a real registry and callsite; no combat timing is changed.
def creature(stem):return 'creature__'+stem+'.ogg'
def wing(start,end):return dict(key='wings__wings_flap_large.flac',start=start,end=end)
for cue,sources,ru,en,cap in [
 ('ambient_low',[creature('breath')],'Горыныч тяжело дышит','Gorynych breathes heavily',1.2),
 ('step_heavy',[rock(5),rock(6)],'Тяжёлые шаги Горыныча','Gorynych heavy footsteps',.35),
 ('roar_ground',[creature('roar_01')],'Горыныч угрожающе ревёт','Gorynych roars',1.4),
 ('head_mor_charge',[water('bubble_01')],'Моровая голова набирает дыхание','Mor head draws breath',.8),
 ('head_mor_shot',[water('slime_13')],'Моровая голова выпускает сгусток','Mor head spits',.5),
 ('mor_cloud',[water('loop_bubbles_1')],'Моровое облако бурлит','Mor cloud bubbles',1.2),
 ('head_fire_charge',[creature('grunt_04')],'Огненная голова втягивает воздух','Fire head inhales',.8),
 ('head_fire_breath',['foley__hearth.flac'],'Огненная голова выдыхает пламя','Fire head breathes flame',1.5),
 ('head_fireball',['foley__ignition.flac'],'Горыныч выпускает огненный шар','Gorynych launches a fireball',.5),
 ('head_predator_bite',[creature('monster_06')],'Хищная голова готовит укус','Predator head prepares a bite',.8),
 ('head_predator_grab',[equip('body-3.flac')],'Хищная голова хватает','Predator head grabs',.4),
 ('tail_sweep',['foley__swosh-03.flac'],'Хвост Горыныча рассекает воздух','Gorynych tail sweeps',.5),
 ('ground_spikes_warn',[rock(7)],'Камни под землёй смещаются','Stones shift underground',.9),
 ('ground_spikes_impact',[rock(8)],'Каменные шипы вырываются','Stone spikes erupt',.6),
 ('transform_start',[w(8)],'Тело Горыныча преображается','Gorynych transforms',1),
 ('wings_unfold',[wing(0,1)],'Крылья Горыныча раскрываются','Gorynych unfolds wings',.8),
 ('wings_flap',[wing(1,2)],'Горыныч взмахивает крыльями','Gorynych flaps wings',.7),
 ('wind_gust',['wind__wind.wav'],'Горыныч поднимает порыв ветра','Gorynych raises a wind gust',1),
 ('dive',['foley__swosh-02.flac'],'Горыныч пикирует','Gorynych dives',.6),
 ('land',[rock(9)],'Горыныч приземляется','Gorynych lands',.65),
 ('exhausted',[creature('monster_07')],'Горыныч изнемогает','Gorynych is exhausted',1.2),
 ('cleanse_mor_head',[water('splash_10')],'Моровая голова очищается','Mor head is cleansed',.7),
 ('cleanse_fire_head',['steam__steam hisses - Marker #4.wav'],'Пламя очищаемой головы гаснет','Cleansed head flame subsides',.7),
 ('cleanse_predator_head',[creature('hurt_05')],'Хищная голова освобождается','Predator head is released',.7),
 ('purified_roar',[creature('roar_03')],'Очищенный Горыныч подаёт голос','Purified Gorynych calls',1.5),
 ('bottle_collect',[water('splash_11')],'Дыхание собрано в бутылку','Breath collected in bottle',.4),
 ('crossbow_charge',[equip('quiver-leather-squeeze-06.wav')],'Арбалет Горыныча заряжается','Serpent crossbow charges',.4),
 ('crossbow_special_fire',['foley__ignition.flac'],'Арбалет выпускает огненный заряд','Crossbow fires flame',.4),
 ('crossbow_special_mor',[water('slime_14')],'Арбалет выпускает моровой заряд','Crossbow fires mor',.4),
 ('crossbow_special_storm',[equip('sword_clash.6.ogg')],'Арбалет выпускает грозовой заряд','Crossbow fires storm',.4),
 ('armor_fullset_proc',[equip('belt-buckle-03.wav')],'Срабатывает защита доспеха Горыныча','Serpent armor protection triggers',.45)]:add('bosses','gorynych.'+cue,sources,ru,en,cap,.075)
# Marinka: independent performed actress for pain, not reused spirit vocal takes.
for cue,start,end,ru,en in [('witch_hurt_01',.4,1.15,'Маринка вскрикивает от боли','Marinka grunts in pain'),('witch_rage_01',4.9,6.15,'Маринка напряжённо вскрикивает','Marinka cries with effort'),('witch_defeat_01',11.65,12.6,'Маринка стонет при поражении','Marinka groans in defeat')]:
 add('bosses','marinka.'+cue,[dict(key='aura__female_hurt_grunts_groans_1.ogg',start=start,end=end)],ru,en,1.2,.07)
for cue,sources,ru,en,cap in [
 ('cloth_step_01',['foley__cloth-03.flac'],'Одежда Маринки шуршит при шаге','Marinka cloth steps',.25),
 ('trace_hex_01',[glass(1)],'Маринка начертывает проклятие','Marinka traces a hex',.45),
 ('fire_charge_01',['steam__steam hisses - Marker #5.wav'],'Маринка собирает жар','Marinka gathers heat',.28),
 ('fire_release_01',['foley__ignition.flac'],'Маринка выпускает огонь','Marinka releases fire',.4),
 ('shadow_dissolve_01',[leaf(11)],'Тень Маринки рассеивается','Marinka shadow dissolves',.5),
 ('shadow_clone_break_01',[glass(2)],'Теневая копия разрушается','Shadow clone shatters',.4),
 ('barrier_raise_01',[glass(3)],'Покров Маринки поднимается','Marinka veil rises',.5),
 ('barrier_break_01',[glass(4)],'Покров Маринки разбивается','Marinka veil breaks',.5),
 ('witch_blink_01',['foley__swosh-01.flac'],'Маринка исчезает в рывке','Marinka blinks away',.4),
 ('magpie_change_01',[leaf(12)],'Маринка оборачивается сорокой','Marinka changes into a magpie',.4),
 ('magpie_wings_01',[wing(0,.4)],'Крылья сороки взмахивают','Magpie wings flap',.3),
 ('magpie_wings_02',[wing(1,1.4)],'Крылья сороки взмахивают','Magpie wings flap',.3),
 ('aurochs_hex_01',[w(9)],'Проклятие тура проявляется','Aurochs curse takes hold',.7),
 ('enchanted_herd_01',[rock(2)],'Зачарованное стадо топочет','Enchanted herd stamps',.7)]:add('bosses','marinka.'+cue,sources,ru,en,cap,.065)
# No recorded words/whisper source is available. Preserve the gap explicitly.
for e in ['marinka.witch_whisper_01','marinka.witch_whisper_02']:BLOCKED[e]='Needs a distinct actual whispered performance; no invented whisper from noise or hurt recording.'
# Hero weapon families: spear thrusts, mace blows and sabre cuts use different
# source performances; common props may share only their real material identity.
for prefix,items in [
 ('dobrynya',[
 ('armor_step_01',equip('belt-buckle-01.wav'),'Доспех Добрыни позвякивает','Dobrynya armor steps',.22),
 ('armor_step_02',equip('belt-buckle-02.wav'),'Доспех Добрыни позвякивает','Dobrynya armor steps',.22),
 ('spear_parry_01',equip('sword_clash.1.ogg'),'Копьё Добрыни парирует','Dobrynya spear parries',.25),
 ('shield_ward_01',wood(15),'Щит Добрыни поднимается','Dobrynya raises shield',.4),
 ('magic_deflect_01',glass(5),'Добрыня отражает заклинание','Dobrynya deflects magic',.35),
 ('spear_swing_01','foley__swosh-01.flac','Копьё Добрыни рассекает воздух','Dobrynya spear thrust',.14),
 ('spear_swing_02','foley__swosh-02.flac','Копьё Добрыни рассекает воздух','Dobrynya spear thrust',.14),
 ('spear_impact_01',equip('body-1.flac'),'Копьё Добрыни попадает в цель','Dobrynya spear connects',.22),
 ('spear_sweep_01','foley__swosh-03.flac','Добрыня делает широкий взмах копьём','Dobrynya sweeps spear',.23),
 ('unravel_hex_01',glass(6),'Добрыня разрушает проклятие','Dobrynya unravels hex',.5),
 ('aurochs_transform_01',w(10),'Добрыня превращается в тура','Dobrynya transforms into aurochs',.7),
 ('aurochs_charge_01',equip('boots-leather-jump-03.wav'),'Тур начинает разбег','Aurochs charges',.45),
 ('elixir_drink_01',water('bubble_03'),'Добрыня пьёт эликсир','Dobrynya drinks elixir',.5)]),
 ('ilya_muromets',[
 ('herbal_drink_01',water('bubble_02'),'Илья пьёт травяной настой','Ilya drinks herbal tonic',.5),
 ('mace_impact_01',wood(16),'Булава Ильи тяжело ударяет','Ilya mace impacts',.28),
 ('mace_swing_01',equip('sword.10.ogg'),'Илья замахивается булавой','Ilya swings mace',.23),
 ('shield_block_01',wood(17),'Щит Ильи принимает удар','Ilya shield blocks',.3),
 ('shield_block_02',wood(18),'Щит Ильи принимает удар','Ilya shield blocks',.3),
 ('shield_raise_01',equip('quiver-leather-squeeze-07.wav'),'Илья поднимает щит','Ilya raises shield',.3),
 ('step_heavy_01',equip('boots-leather-step-04.wav'),'Тяжёлые шаги Ильи','Ilya heavy footsteps',.25)]),
 ('alyosha',[
 ('footstep_soft_1',equip('boots-leather-step-01.wav'),'Лёгкие шаги Алёши','Alyosha soft steps',.2),
 ('footstep_soft_2',equip('boots-leather-step-02.wav'),'Лёгкие шаги Алёши','Alyosha soft steps',.2),
 ('dodge_swish_1','foley__cloth-01.flac','Алёша уклоняется','Alyosha dodges',.2),
 ('dodge_swish_2','foley__cloth-05.flac','Алёша уклоняется','Alyosha dodges',.2),
 ('saber_swing_1',equip('sword.1.ogg'),'Сабля Алёши свистит','Alyosha sabre swishes',.16),
 ('saber_swing_2',equip('sword.2.ogg'),'Сабля Алёши свистит','Alyosha sabre swishes',.16),
 ('saber_hit_1',equip('sword_clash.2.ogg'),'Сабля Алёши сталкивается с целью','Alyosha sabre connects',.22),
 ('bow_draw',equip('quiver-leather-squeeze-08.wav'),'Алёша натягивает лук','Alyosha draws bow',.4),
 ('arrow_release','foley__swosh-02.flac','Алёша выпускает стрелу','Alyosha releases arrow',.14),
 ('feint','foley__cloth-03.flac','Алёша делает обманное движение','Alyosha feints',.2),
 ('counter',equip('sword.3.ogg'),'Алёша контратакует','Alyosha counters',.2),
 ('drink_tonic',water('bubble_01'),'Алёша пьёт тоник','Alyosha drinks tonic',.45)]),
 ('entity.finist',[
 ('wing_dash',wing(0,.5),'Финист делает крылатый рывок','Finist wing dash',.35),
 ('transform',leaf(13),'Финист преображается','Finist transforms',.55),
 ('banner_break',w(6),'Знамя Финиста ломается','Finist banner breaks',.45),
 ('tonic',water('splash_12'),'Финист пьёт тоник','Finist drinks tonic',.45),
 ('sword_swing_1',equip('sword.4.ogg'),'Меч Финиста рассекает воздух','Finist sword swing',.2),
 ('sword_swing_2',equip('sword.5.ogg'),'Меч Финиста рассекает воздух','Finist sword swing',.2),
 ('sword_contact',equip('sword_clash.3.ogg'),'Меч Финиста достигает цели','Finist sword connects',.25),
 ('run_step_1',equip('boots-leather-step-03.wav'),'Финист бежит','Finist runs',.2),
 ('run_step_2',equip('boots-leather-step-04.wav'),'Финист бежит','Finist runs',.2)]),
 ('entity.falcon',[
 ('wing_flap_1',wing(0,.5),'Сокол взмахивает крыльями','Falcon wings flap',.3),
 ('wing_flap_2',wing(1,1.5),'Сокол взмахивает крыльями','Falcon wings flap',.3),
 ('dive_start','foley__swosh-01.flac','Сокол пикирует','Falcon dives',.25),
 ('dive_impact',equip('body-2.flac'),'Сокол ударяет цель','Falcon strikes',.25),
 ('intercept','foley__swosh-03.flac','Сокол перехватывает атаку','Falcon intercepts',.25),
 ('chain_break',equip('belt-buckle-04.wav'),'Цепи сокола рвутся','Falcon chains break',.45),
 ('perch',leaf(14),'Сокол садится','Falcon perches',.25),
 ('morph_flight',wing(2,3),'Сокол раскрывает крылья','Falcon unfolds wings',.45)]),
 ('entity.kartaus',[
 ('banner_raise',wood(19),'Картаус поднимает знамя','Kartaus raises banner',.5),
 ('staff_wave','foley__swosh-03.flac','Картаус взмахивает посохом','Kartaus waves staff',.3),
 ('staff_melee',wood(20),'Посох Картауса ударяет','Kartaus staff strikes',.3),
 ('chain_bind',equip('belt-buckle-04.wav'),'Картаус стягивает цепи','Kartaus binds chains',.5),
 ('black_eye_charge',glass(1),'Чёрное око Картауса заряжается','Kartaus black eye charges',.65),
 ('black_eye_impact',glass(2),'Чёрное око Картауса поражает цель','Kartaus black eye impacts',.35),
 ('barrier_break',glass(3),'Барьер Картауса рушится','Kartaus barrier shatters',.5),
 ('heavy_step_1',rock(1),'Тяжёлые шаги Картауса','Kartaus heavy steps',.3),
 ('heavy_step_2',rock(2),'Тяжёлые шаги Картауса','Kartaus heavy steps',.3),
 ('staff_ground',wood(21),'Картаус ударяет посохом оземь','Kartaus staff hits ground',.35)]),
 ('kalin',[
 ('banner_flap','foley__cloth-05.flac','Знамя Калина хлопает','Kalin banner flaps',.6),
 ('battle_seal_use',rock(3),'Боевая печать срабатывает','Battle seal activates',.5),
 ('camp_fire_loop_short','foley__hearth.flac','Лагерный костёр потрескивает','Camp fire crackles',1.5),
 ('kalin_kneel_surrender',equip('belt-buckle-02.wav'),'Калин опускается на колени','Kalin kneels in surrender',.5),
 ('kalin_parry',equip('sword_clash.4.ogg'),'Калин парирует','Kalin parries',.25),
 ('kalin_sabre_heavy',equip('sword.6.ogg'),'Калин тяжело взмахивает саблей','Kalin heavy sabre swing',.3),
 ('kalin_sabre_light',equip('sword.7.ogg'),'Калин делает быстрый взмах саблей','Kalin light sabre swing',.18),
 ('kalin_spin_attack',equip('sword.8.ogg'),'Калин вращает саблю','Kalin spins sabre',.35),
 ('kalin_step_heavy_01',equip('boots-leather-step-03.wav'),'Тяжёлые шаги Калина','Kalin heavy steps',.25),
 ('kalin_step_heavy_02',equip('boots-leather-step-04.wav'),'Тяжёлые шаги Калина','Kalin heavy steps',.25)])]:
 for cue,source,ru,en,cap in items:add('heroes-and-commanders',prefix+'.'+cue,[source],ru,en,cap,.065)
for cue,target in [('horse_gallop_loop','entity.horse.gallop'),('horse_rear','entity.horse.angry'),('horse_snort','entity.horse.breathe'),('horse_step_01','entity.horse.step'),('horse_step_02','entity.horse.step_wood'),('horse_stomp','entity.horse.land')]:alias('world','kalin.'+cue,target,'Actual vanilla horse performance for horse action; no synthetic horse voice')
# Existing autonomous creatures: retain unresolved identity vocals in quarantine,
# while implementing their independently useful movement and weapon material.
for prefix,items in [
 ('doghead',[
 ('hunter.thrust_whoosh','foley__swosh-01.flac','Псоглавец колет копьём','Doghead spear thrust',.14),
 ('hunter.sweep_whoosh','foley__swosh-03.flac','Псоглавец взмахивает копьём','Doghead spear sweep',.22),
 ('scout.bow_draw',equip('quiver-leather-squeeze-08.wav'),'Псоглавец натягивает лук','Doghead draws bow',.4),
 ('scout.bow_release','foley__swosh-02.flac','Псоглавец выпускает стрелу','Doghead releases arrow',.14),
 ('scout.knife_whoosh',equip('sword.2.ogg'),'Нож псоглавца свистит','Doghead knife swishes',.13),
 ('leader.axe_whoosh',equip('sword.8.ogg'),'Топор вожака рассекает воздух','Doghead leader axe swings',.13),
 ('leader.axe_impact',wood(21),'Топор вожака ударяет','Doghead leader axe impacts',.25),
 ('shared.claw_gear',equip('belt-buckle-01.wav'),'Снаряжение псоглавца звенит','Doghead gear clinks',.2),
 ('shared.leather_step',equip('boots-leather-step-01.wav'),'Кожаная обувь псоглавца скрипит','Doghead leather steps',.2),
 ('shared.mud_step',water('splash_02'),'Псоглавец шагает по грязи','Doghead mud steps',.2),
 ('shared.stone_step',rock(1),'Псоглавец шагает по камню','Doghead stone steps',.2),
 ('shared.fur_cloth','foley__cloth-01.flac','Меховая одежда псоглавца шуршит','Doghead fur cloth rustles',.25)]),
 ('nochnitsa',[
 ('step',equip('boots-leather-step-02.wav'),'Ночница тихо ступает','Nochnitsa steps softly',.2),
 ('claw_swing','foley__swosh-02.flac','Когти Ночницы рассекают воздух','Nochnitsa claw swings',.15),
 ('robes_rustle','foley__cloth-05.flac','Одежда Ночницы шуршит','Nochnitsa robes rustle',.25)])]:
 for cue,source,ru,en,cap in items:add('other-hostiles',prefix+'.'+cue,[source],ru,en,cap,.055)
for cue,source,ru,en,cap in [
 ('wing_flap',wing(0,.7),'Жар-птица взмахивает крыльями','Firebird wings flap',.5),
 ('takeoff',wing(1,1.8),'Жар-птица взлетает','Firebird takes off',.6),
 ('glide','wind__wind.wav','Жар-птица планирует','Firebird glides',.75),
 ('perch',leaf(15),'Жар-птица садится','Firebird perches',.3),
 ('drop',leaf(16),'Перо Жар-птицы падает','Firebird feather falls',.3),
 ('depart',wing(2,3),'Жар-птица улетает','Firebird departs',.65)]:add('other-hostiles','firebird_'+cue,[source],ru,en,cap,.055)
# Mineral/elemental bodies: material recordings are explicitly labeled as such;
# they are not presented as a new voiced roar, speech or song.
for cue,source,ru,en,cap in [
 ('fire_guardian_idle','foley__hearth.flac','Угли стража потрескивают','Guardian embers crackle',1.2),
 ('fire_guardian_awaken',rock(4),'Каменное тело стража пробуждается','Stone guardian awakens',.8),
 ('fire_guardian_step',rock(5),'Каменный страж ступает','Stone guardian steps',.25),
 ('fire_guardian_hurt',rock(6),'Каменная оболочка стража трескается','Guardian stone shell cracks',.35),
 ('fire_guardian_swing','foley__swosh-03.flac','Огненный страж замахивается','Fire guardian swings',.3),
 ('fire_pulse_charge','steam__steam hisses - Marker #4.wav','Жар стража нарастает','Guardian heat gathers',.8),
 ('fire_pulse_burst','foley__ignition.flac','Огненный импульс прорывается','Fire pulse bursts',.55),
 ('fire_lockdown_warning',rock(7),'Камни огненной ловушки смещаются','Fire trap stones shift',.8),
 ('fire_lockdown_ignite','foley__ignition.flac','Огненная ловушка вспыхивает','Fire trap ignites',.65),
 ('fire_shield_break',rock(8),'Каменный щит стража рушится','Guardian stone shield breaks',.5),
 ('fire_guardian_ground_impact',rock(9),'Страж ударяет по земле','Guardian slams ground',.55),
 ('fire_guardian_purify','steam__steam hisses - Marker #5.wav','Угли очищенного стража остывают','Cleansed guardian embers cool',.85),
 ('spring_keeper_idle',water('loop_bubbles_02'),'Хранитель родника тихо бурлит','Spring keeper bubbles softly',1.2),
 ('spring_keeper_emerge',water('splash_03'),'Хранитель поднимается из воды','Spring keeper emerges',.7),
 ('spring_keeper_step',water('splash_01'),'Хранитель ступает по воде','Spring keeper splashes underfoot',.22),
 ('spring_keeper_hurt',water('slime_04'),'Водная оболочка хранителя содрогается','Keeper water body shudders',.35),
 ('spring_whip_windup',water('splash_08'),'Водяная плеть поднимается','Water whip rises',.4),
 ('spring_whip_impact',water('splash_09'),'Водяная плеть ударяет','Water whip impacts',.35),
 ('spring_whirlpool_warning',water('bubble_02'),'Вода перед воронкой бурлит','Water bubbles before whirlpool',.8),
 ('spring_whirlpool_loop',water('loop_bubbles_1'),'Водоворот бурлит','Whirlpool churns',1.5),
 ('spring_prison_form',water('slime_05'),'Водяная тюрьма смыкается','Water prison forms',.6),
 ('spring_prison_break',water('splash_10'),'Водяная тюрьма распадается','Water prison breaks',.45),
 ('spring_flood_rise',water('splash_11'),'Вода поднимается','Flood rises',.9),
 ('spring_heal_begin',water('bubble_03'),'Родник начинает восстанавливать хранителя','Spring begins healing keeper',.9),
 ('spring_heal_interrupt',water('slime_06'),'Поток восстановления прерывается','Healing stream interrupts',.4),
 ('spring_valve_switch',equip('belt-buckle-03.wav'),'Вентиль родника переключается','Spring valve switches',.35),
 ('spring_keeper_purify',water('splash_12'),'Родник очищается','Spring is cleansed',.8),
 ('amulet_fragment_claim',equip('belt-buckle-04.wav'),'Фрагмент оберега получен','Amulet fragment claimed',.4)]:add('elemental-guardians','guardians.'+cue,[source],ru,en,cap,.055 if cue.endswith('idle') else .07)
for cue,source,ru,en,cap in [
 ('footstep_1',rock(1),'Земляной дух тяжело ступает','Earth spirit steps',.25),
 ('footstep_2',rock(2),'Земляной дух тяжело ступает','Earth spirit steps',.25),
 ('awaken_rumble',rock(3),'Земляной дух пробуждается','Earth spirit awakens',.9),
 ('pillar_throw','foley__swosh-03.flac','Дух бросает каменный столп','Earth spirit throws pillar',.4),
 ('volley_release',rock(4),'Каменные осколки вылетают','Stone volley releases',.35),
 ('servant_hit',rock(5),'Земляной слуга получает удар','Earth servant is struck',.3),
 ('armor_activate',rock(6),'Каменная броня смыкается','Stone armor activates',.6),
 ('death_fall',rock(7),'Земляной дух обрушивается','Earth spirit collapses',1.1),
 ('pillar_grip',rock(8),'Каменная рука сжимает столп','Stone hand grips pillar',.5),
 ('ambient_1',rock(1),'Камни земляного духа поскрипывают','Earth spirit stones creak',.65),
 ('ambient_2',rock(2),'Камни земляного духа поскрипывают','Earth spirit stones creak',.65),
 ('volley_impact',rock(9),'Каменный снаряд разбивается','Stone projectile impacts',.4),
 ('melee_impact',wood(20),'Каменная рука тяжело ударяет','Stone fist strikes',.3),
 ('shockwave',rock(3),'Каменная ударная волна расходится','Stone shockwave spreads',.8),
 ('pillar_impact',rock(4),'Каменный столп ударяет','Stone pillar impacts',.4),
 ('tremor_impact',rock(5),'Земля содрогается','Ground tremor strikes',.7),
 ('pillar_rip',rock(6),'Столп вырывается из земли','Pillar tears from ground',.7),
 ('phase_change',rock(7),'Каменная оболочка перестраивается','Stone body reforms',.9),
 ('servant_emerge',rock(8),'Земляной слуга появляется','Earth servant emerges',.5),
 ('tremor_warning',rock(9),'Подземный камень предупреждающе смещается','Subterranean stone shifts',.7),
 ('gate_close',wood(19),'Каменные врата закрываются','Stone gate closes',.65),
 ('volley_charge',rock(1),'Каменные снаряды собираются','Stone volley gathers',.6),
 ('melee_windup','foley__swosh-01.flac','Земляной дух замахивается','Earth spirit winds up',.25),
 ('summon_warning',rock(2),'Земля перед призывом дрожит','Ground trembles before summon',.6),
 ('servant_death',rock(3),'Земляной слуга рассыпается','Earth servant crumbles',.6),
 ('servant_idle',rock(4),'Камни земляного слуги шевелятся','Earth servant stones shift',.6),
 ('seal_break',glass(4),'Печать земляного духа ломается','Earth spirit seal breaks',.5)]:add('elemental-guardians','earth_spirit.'+cue,[source],ru,en,cap,.035 if cue.startswith('ambient') else .065)
for cue,source,ru,en,cap in [
 ('earth_rumble',rock(5),'Земля над Полозом грохочет','Earth rumbles above Poloz',1),
 ('ambient_rumble',rock(6),'Чешуя Полоза трётся о камень','Poloz scales scrape stone',.9),
 ('move_crush',rock(7),'Полоз сминает камни','Poloz crushes stones',.5),
 ('armor_deflect',equip('sword_clash.4.ogg'),'Чешуя Полоза отражает удар','Poloz armor deflects',.25),
 ('weakspot_hit',equip('body-3.flac'),'Уязвимое место Полоза поражено','Poloz weakspot is struck',.25),
 ('tail_slam',rock(8),'Хвост Полоза обрушивается','Poloz tail slams',.6),
 ('mor_rain',water('slime_07'),'Моровые капли падают','Mor droplets fall',.9),
 ('burrow',rock(9),'Полоз уходит под землю','Poloz burrows',.8),
 ('emerge',rock(1),'Полоз прорывает поверхность','Poloz emerges',.8),
 ('phase2_cracks',rock(2),'Чешуя Полоза растрескивается','Poloz scales crack',.8),
 ('death',rock(3),'Тело Полоза обрушивается','Poloz body collapses',1.2)]:add('elemental-guardians','poloz_'+cue,[source],ru,en,cap,.065)
# Bayun's ordinary animal identity uses recorded cats, not synthesized meows.
# His existing song is a separate unresolved audit/source item, not a purr relabeled as singing.
for cue,source,ru,en,cap in [
 ('purr.soft','cat__cat_purrsleepy_loop.wav','Баюн тихо мурлычет','Bayun purrs softly',1.5),
 ('purr.happy','cat__cat_purractive_loop.wav','Баюн довольно мурлычет','Bayun purrs happily',1.5),
 ('mew.soft','cat__cat_softmew.wav','Баюн тихо мяукает','Bayun meows softly',.9),
 ('mew.annoyed','cat__cat_mewfood.wav','Баюн настойчиво мяукает','Bayun meows insistently',.9),
 ('stretch','cat__cat_mewpurr.wav','Баюн мурлычет, потягиваясь','Bayun purrs while stretching',.9),
 ('settle','foley__cloth-03.flac','Баюн устраивается поудобнее','Bayun settles down',.3),
 ('pawstep',leaf(16),'Мягкие шаги Баюна','Bayun soft pawsteps',.18),
 ('pounce','foley__cloth-01.flac','Баюн прыгает','Bayun pounces',.22),
 ('claw','foley__swosh-01.flac','Когти Баюна рассекают воздух','Bayun claws swish',.15)]:add('bayun-recorded-cat','bayun.'+cue,[source],ru,en,cap,.04 if cue.startswith('purr') else .055,radius=10)
for cue,target,ru,en in [
 ('breath','entity.cat.purr','Баюн тихо мурлычет','Bayun purrs softly'),
 ('groom','entity.cat.eat','Баюн перебирает шерсть','Bayun grooms'),
 ('milk','entity.cat.eat','Баюн лакает молоко','Bayun laps milk'),
 ('fish','entity.cat.eat','Баюн жуёт рыбу','Bayun chews fish'),
 ('warn','entity.cat.hiss','Баюн предупреждающе шипит','Bayun hisses a warning'),
 ('book.page','item.book.page_turn','Страница Книги сказаний шелестит','Tales book page turns'),
 ('book.open','item.book.page_turn','Книга сказаний открывается','Tales book opens'),
 ('book.discovery','entity.experience_orb.pickup','Книга сказаний отзывается звоном','Tales book chimes')]:
 alias('bayun-recorded-cat','bayun.'+cue,target,'Actual cat/material/notification vanilla reference; never song or human speech');ALIASES['bayun.'+cue].update(ru=ru,en=en)
BLOCKED.pop('kikimora_laugh')
add_spirit('kikimora_laugh',[dict(key='laugh__maniacal_laughter_pack_1.ogg',start=0,end=2.8)],'Кикимора зловеще смеётся','Kikimora laughs wickedly',2.6,.055)
PLAN['bayun.mew.soft']['sources'].append('cat__cat_mewpurr2.wav')
# Doghead role performances: separate original mouth-performed source files,
# not a generic bark pitched up/down for three roles. Shared legacy vocal IDs stay muted.
for role,ambient,hurt,death,ru in [('hunter','grunt_01','hurt_01','monster_01','охотник'),('scout','grunt_02','hurt_02','monster_02','разведчик'),('leader','troll_01','hurt_03','monster_03','вожак')]:
 for cue,source,label,en,cap,rms in [('ambient',ambient,'ворчит','grumbles',.65,.045),('hurt',hurt,'вскрикивает','cries out',.6,.075),('death',death,'испускает последний рык','gives a final growl',1.2,.075)]:
  add('doghead-performed-voices',f'doghead.{role}.{cue}',[creature(source)],f'Псоглавец-{ru} {label}',f'Doghead {role} {en}',cap,rms)
for event,source,ru,en,cap in [
 ('doghead.hunter.bark','barking_01','Псоглавец-охотник лает','Doghead hunter barks',.8),
 ('doghead.scout.spot_bark','barking_02','Псоглавец-разведчик подаёт тревогу','Doghead scout barks an alarm',.8),
 ('doghead.leader.howl_release','howl','Вожак псоглавцев воет','Doghead leader howls',1.6)]:add('doghead-performed-voices',event,[creature(source)],ru,en,cap,.095,peak=.4,radius=48 if 'howl' in event else 32)
# Four distinct performed phrases from an actress's screech session; the gaps
# for real inhale/breath/interrupt remain explicit rather than synthetic replacements.
for cue,start,end,ru,en,cap in [
 ('hurt',.4,1.2,'Ночница резко вскрикивает','Nochnitsa cries out',.8),
 ('target_spotted',1.9,3,'Ночница замечает жертву и вскрикивает','Nochnitsa spots prey and screeches',1),
 ('scream_release',3.8,4.85,'Ночница пронзительно кричит','Nochnitsa shrieks',1),
 ('death',5.6,7.3,'Последний крик Ночницы затихает','Nochnitsa final cry fades',1.4)]:add('nochnitsa-performed-voice','nochnitsa.'+cue,[dict(key='aura__evil_screech_1.ogg',start=start,end=end)],ru,en,cap,.075)
for cue in ['ambient','breath','scream_inhale','scream_interrupt']:BLOCKED['nochnitsa.'+cue]='Separate actual female breath/inhale/interrupt performance still required; no cloned scream, noise or synthetic tone.'
add('other-hostiles','nochnitsa.sunrise_dissolve',[leaf(10)],'Одежда Ночницы осыпается на рассвете','Nochnitsa cloth crumbles at sunrise',.65,.045)
# Hero pain uses the uploader's four recorded male groups rather than one
# synthesized male template or a set of pitch-shifted clones. Other moods remain
# separate work; pain yells are not relabeled as breathing, a taunt or victory.
for event,source,ru,en in [
 ('ilya_muromets.pain_nonverbal_01','3grunt1','Илья глухо вскрикивает от боли','Ilya grunts in pain'),
 ('ilya_muromets.pain_nonverbal_02','3grunt2','Илья глухо вскрикивает от боли','Ilya grunts in pain'),
 ('dobrynya.grunt_hurt_01','1yell1','Добрыня вскрикивает от боли','Dobrynya cries in pain'),
 ('alyosha.hurt_1','2yell1','Алёша вскрикивает от боли','Alyosha cries in pain'),
 ('alyosha.hurt_2','2yell2','Алёша вскрикивает от боли','Alyosha cries in pain'),
 ('entity.finist.hurt_1','yell1','Финист вскрикивает от боли','Finist cries in pain'),
 ('entity.finist.hurt_2','yell2','Финист вскрикивает от боли','Finist cries in pain'),
 ('entity.finist.hurt_3','yell3','Финист вскрикивает от боли','Finist cries in pain')]:add('hero-recorded-pain',event,['hael__'+source+'.wav'],ru,en,.65,.07)
# Real bandit weapon tells/contact, not a human grunt or an impact before a hit.
for cue,source,ru,en,cap in [
 ('sword_windup',equip('sword.4.ogg'),'Разбойник замахивается мечом','Bandit sword winds up',.14),
 ('axe_windup',equip('sword.6.ogg'),'Разбойник замахивается топором','Bandit axe winds up',.23),
 ('mace_windup',equip('sword.10.ogg'),'Разбойник замахивается булавой','Bandit mace winds up',.29),
 ('sword_impact',equip('sword_clash.1.ogg'),'Меч разбойника достигает цели','Bandit sword connects',.2),
 ('axe_impact',wood(21),'Топор разбойника достигает цели','Bandit axe connects',.22),
 ('mace_impact',equip('body-3.flac'),'Булава разбойника достигает цели','Bandit mace connects',.25),
 ('bow_draw',equip('quiver-leather-squeeze-08.wav'),'Разбойник натягивает тетиву','Bandit draws bow',.55),
 ('shield_raise',equip('quiver-leather-squeeze-07.wav'),'Разбойник поднимает щит','Bandit raises shield',.3)]:
 add('bandit-weapon-contact','bandit_'+cue,[source],ru,en,cap,.065);NEW.add('bandit_'+cue)

# Nightingale: actual human whistle phrases, new performed reactions and weapon
# props at existing action ticks. No AI, registry or animation changes.
for cue,start,end,ru,en,cap in [
 ('phase1_idle_whistle',.1,.6,'Соловей коротко посвистывает','Nightingale whistles briefly',.5),
 ('phase1_whistle_release',1.9,3.3,'Соловей выпускает боевой свист','Nightingale releases a battle whistle',1.4),
 ('phase2_wave',3.55,4.95,'Соловей выпускает воздушную волну свистом','Nightingale whistles an air wave',1.4)]:
 add('nightingale-recorded','nightingale_'+cue,[dict(key='stilgar__soft_whistle.ogg',start=start,end=end)],ru,en,cap,.035 if 'idle' in cue else .075,radius=16 if 'idle' in cue else 40)
for cue,sources,ru,en,cap in [
 ('phase1_aggro',[a('Hmph_02')],'Соловей недовольно окликает','Nightingale challenges',.65),
 ('phase1_hurt',[a('(groan)_04'),a('(groan)_05')],'Соловей стонет от боли','Nightingale groans in pain',.7),
 ('phase2_hurt',[a('(groan)_06')],'Соловей хрипло стонет','Nightingale groans hoarsely',.8),
 ('phase1_death',[a('(groan)_07')],'Последний стон Соловья','Nightingale final groan',1.4),
 ('phase1_slash',[equip('sword.7.ogg'),equip('sword.8.ogg')],'Клинок Соловья рассекает воздух','Nightingale blade swishes',.22),
 ('phase2_slash',[equip('sword.10.ogg')],'Соловей резко взмахивает клинком','Nightingale blade cuts the air',.18),
 ('phase1_leap',['foley__cloth-03.flac'],'Одежда Соловья шуршит в прыжке','Nightingale cloth rustles in a leap',.3),
 ('phase2_dash',['foley__swosh-03.flac'],'Соловей проносится в рывке','Nightingale dashes through air',.35),
 ('phase2_whirlwind',['wind__wind.wav'],'Вихрь Соловья бушует','Nightingale whirlwind rushes',1.1)]:
 add('nightingale-recorded','nightingale_'+cue,sources,ru,en,cap,.055 if 'leap' in cue else .075)
add('nightingale-recorded','nightingale_phase2_death',[a('(groan)_07')],'Последний стон Соловья','Nightingale final groan',1.4,.075,shared_foley_reason='Same human death across both phases; intentionally same performance for same action, no pitch variant or distinct-emotion claim')
add('nightingale-recorded','nightingale.phase1.land',[equip('boots-leather-jump-03.wav')],'Соловей тяжело приземляется','Nightingale lands',.25,.08)
BLOCKED['nightingale_phase1_whistle_charge']='Actual human inhalation needed at begin(INHALE/RADIAL) and transition. Do not substitute an exhale, synthetic tone or weapon whoosh.'
BLOCKED['nightingale_phase2_laugh']='Distinct actual male laughter performance needed; phase-change and idle share this callback. No female laugh relabeled as male, no synthetic vocal restoration.'
for cue,source,ru,en,cap in [
 ('draw',[equip('belt-buckle-03.wav'),'foley__cloth-01.flac'],'Клинок Соловья извлекают из ножен','Nightingale blade is drawn',.35),
 ('slash',equip('sword_clash.2.ogg'),'Клинок Соловья ударяет цель','Nightingale blade strikes',.2),
 ('wind_charge','foley__swosh-01.flac','Воздух собирается у клинка Соловья','Air gathers at Nightingale blade',.35),
 ('wind_release','foley__swosh-03.flac','Клинок Соловья выпускает ветер','Nightingale blade releases wind',.45),
 ('wind_hit',equip('body-2.flac'),'Ветер клинка Соловья достигает цели','Nightingale blade wind hits',.25)]:
 add('nightingale-weapon','nightingale_blade_'+cue,[source],ru,en,cap,.065)

# Independently recorded human inhalation and performed evil laugh; no reversed
# exhale, tone oscillator, generated voice or copied film performance.
BLOCKED.pop('nightingale_phase1_whistle_charge')
BLOCKED.pop('nightingale_phase2_laugh')
add('nightingale-recorded','nightingale_phase1_whistle_charge',['otherthings__inhale_10115b.mp3'],'Соловей набирает воздух','Nightingale inhales',.75,.055,radius=28)
add('nightingale-recorded','nightingale_phase2_laugh',[dict(key='qubodup__evil_laughter.flac',start=2.35,end=3.1)],'Соловей коротко злорадно смеётся','Nightingale chuckles wickedly',.55,.065,radius=28)

# Nochnitsa's breath and interrupted gasp are separate recorded performances.
# The short gasp fits the damage interval; an inhale completes before release.
for cue,source,start,end,ru,en,cap,rms in [
 ('ambient','owlstorm__female_breathing_151206.mp3',8.9,9.8,'Ночница тихо дышит','Nochnitsa breathes quietly',.85,.025),
 ('breath','owlstorm__female_breathing_151206.mp3',1.4,2.4,'Ночница тяжело дышит при преследовании','Nochnitsa breathes while pursuing',.95,.045),
 ('scream_inhale','slamaxu__female_gasp_509883.mp3',0,1.161,'Ночница резко набирает воздух','Nochnitsa draws a sharp breath',.6,.07),
 ('scream_interrupt','kanyonwyvern__female_gasp_740310.mp3',0,1.84,'Дыхание Ночницы сбивается','Nochnitsa gasps as interrupted',.38,.065)]:
 BLOCKED.pop('nochnitsa.'+cue)
 add('nochnitsa-recorded-breath','nochnitsa.'+cue,[dict(key=source,start=start,end=end)],ru,en,cap,rms,radius=16 if cue=='ambient' else 24)

# Ilya's calm/retreat/recovery are different actual breath phrases of one actor,
# not the old near-identical tonal files. Dobrynya has a different performer.
for cue,start,end,ru,en,cap,rms in [
 ('ambient_breath_01',.8,1.9,'Илья спокойно дышит','Ilya breathes steadily',1.1,.035),
 ('retreat_breath_01',11.4,12.9,'Илья тяжело дышит, отступая','Ilya breathes hard while retreating',1.45,.07),
 ('recover_breath_01',5.65,6.7,'Илья переводит дыхание','Ilya catches his breath',1.05,.045)]:
 add('hero-recorded-breath','ilya_muromets.'+cue,[dict(key='exe2be__breathing_512741.mp3',start=start,end=end)],ru,en,cap,rms)
add('hero-recorded-breath','dobrynya.breath_idle_01',[dict(key='davor__breathing_346932.mp3',start=6,end=7.8)],'Добрыня ровно дышит','Dobrynya breathes steadily',1.7,.035)

# Four different physical instrumental gestures replace the correlated legacy
# Finist signals. These are magic accents, never speech or vocal emotion.
for cue,source,ru,en,cap,rms in [
 ('summon_chime','chime__gnote_169855.mp3','Появление Финиста сопровождается переливом','Finist arrival chimes',1.4,.05),
 ('mark_cast','chime__connersaw8_125147.mp3','Метка Финиста отзывается коротким звоном','Finist mark rings briefly',.22,.045),
 ('inspire','harp__phonos_490831.mp3','Ободряющая магия Финиста звучит переливом струн','Finist inspiration shimmers with strings',.65,.055),
 ('victory','cymbal__mirko_175932.mp3','Победа Финиста отзывается металлическим звоном','Finist victory rings out',1.3,.055)]:
 add('finist-recorded-magic','entity.finist.'+cue,[source],ru,en,cap,rms,radius=24)
