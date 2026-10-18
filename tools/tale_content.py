"""Generate the catalogue from an audited registry and reviewed gameplay profiles.

Registry presence does not award knowledge. Technical entities belong to their
owner's entry. Recipes remain the real recipe manager's recipes, not new recipes.
All text is UTF-8; the source is deliberately kept reproducible.
"""
import csv
import json
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
AUDIT = ROOT / 'docs/verification/bayun-book-final'
DATA = json.loads((AUDIT / 'registry-audit.json').read_text(encoding='utf8'))
LOCALES = {lang: json.loads((RES / f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf8')) for lang in ('ru_ru', 'en_us')}
for key in ('podzemnik','podzemnik_miner','kladovik','large_podzemnik'):
    full='entity.slavicmyths.'+key
    value=LOCALES['ru_ru'][full]
    try: LOCALES['ru_ru'][full]=value.encode('cp1251').decode('utf8')
    except (UnicodeEncodeError,UnicodeDecodeError): pass
VANILLA_RU = json.loads((ROOT / 'docs/bayun/vanilla-ru-1.21.json').read_text(encoding='utf8'))
OUT = RES / 'data/slavicmyths/tales'
OUT.mkdir(parents=True, exist_ok=True)
entries, coverage = {}, []

PARTS = {
    'tales': ('Предания', 'Tales'), 'creatures': ('Существа и духи', 'Creatures and spirits'),
    'people': ('Люди и поселения', 'People and settlements'), 'places': ('Места и тайны', 'Places and secrets'),
    'crafts': ('Ремёсла и еда', 'Crafts and food'), 'nature': ('Звери и растения', 'Animals and plants'),
    'quests': ('Поручения и поиски', 'Tasks and searches'), 'deeds': ('Деяния и достижения', 'Deeds and achievements')}
CHAPTERS = {
    'home': ('Дом и двор', 'Home and yard'), 'forest': ('Лесные тропы', 'Forest paths'),
    'water': ('Вода и болота', 'Water and wetlands'), 'field': ('Поле и ночь', 'Fields and night'),
    'depths': ('Древние глубины', 'Ancient depths'), 'burial': ('Курганы', 'Burial mounds'),
    'roads': ('Дороги', 'Roads'), 'legends': ('Особые встречи', 'Unusual encounters'),
    'materials': ('Материалы и оборудование', 'Materials and equipment'),
    'food': ('Пища и напитки', 'Food and drink'), 'plants': ('Растения и древесина', 'Plants and timber'),
    'animals': ('Животные', 'Animals'), 'journal': ('Дневник поручений', 'Task journal'),
    'skills': ('Пути и обряды', 'Paths and rituals')}
SECTIONS = {'inhabitants': ('Обитатели', 'Inhabitants'), 'encounters': ('Встречи', 'Encounters'),
            'catalogue': ('Каталог', 'Catalogue'), 'work': ('Работа', 'Work'),
            'landmarks': ('Ориентиры', 'Landmarks'), 'knowledge': ('Знания', 'Knowledge')}

def text(key, ru, en):
    LOCALES['ru_ru'][key] = ru
    LOCALES['en_us'][key] = en

def name(kind, ident, lang):
    ns, path = ident.split(':', 1)
    return LOCALES[lang].get(f'{kind}.{ns}.{path}', LOCALES[lang].get(f'block.{ns}.{path}', path.replace('_', ' ').capitalize()))

def entry(key, part, chapter, subjects, titles, pages, initial=0, icon='slavicmyths:lore_book', advances=(), links=()):
    ident = 'slavicmyths:' + key
    assert ident not in entries, ident
    entries[ident] = dict(part=part, chapter=chapter, section='inhabitants' if part in ('creatures','people') else 'catalogue',
                          title='tales.entry.'+key+'.title', icon=icon, initial=initial,
                          subjects=sorted(set(subjects)), advancements=list(advances), links=list(links))
    text('tales.entry.'+key+'.title', *titles)
    for i, suffix in enumerate(('rumor','discovered','studied','verified')):
        text('tales.entry.'+key+'.'+suffix, pages['ru_ru'][i], pages['en_us'][i])
    return ident

# Reviewed distinctions. No entry claims a creature was defeated merely because
# an old loot advancement exists. Numbers are kept only where audited this pass.
PROFILES = {
 'kot_bayun': ('legends', 'creatures', 'У старого лесного пня живёт большой серый кот с янтарными глазами. Сказочный Баюн усыпляет путников речью; здесь этот образ превращён в опасную встречу с предупреждением, песней и настоящим прыжком. Его нельзя победить силой. Подойти с пустыми руками можно, но оставаться возле незнакомого кота долго опасно. Приготовьте молоко или сырую рыбу и оставьте себе путь отступления.', 'A large grey cat with amber eyes lives at an old forest stump. The fairy-tale Bayun lulls travellers with speech; this encounter turns that image into a warning, a song and a physical pounce. Force cannot defeat him. Prepare milk or raw fish and keep an escape route.', 'Он замечает путника в пределах 12 блоков. Сначала даёт восемь секунд на знакомство, затем предупреждает ещё три с половиной секунды. Песня слышна до 15 блоков; после двух секунд она может очаровать. Стена или своевременный уход прерывают опасную встречу. Не стойте вплотную, ожидая конца песни.', 'He notices travellers within 12 blocks. An eight-second grace period is followed by a three-and-a-half-second warning. The song reaches 15 blocks and may charm after two seconds. A wall or timely retreat interrupts the encounter. Do not wait next to him.', 'В пределах трёх блоков дайте ведро молока, сырую треску или лосося. Молоко возвращает пустое ведро. Первый дар добавляет 15 доверия; молоко ещё 10, рыба ещё 8. Повторный дар принимается через игровые сутки. Безопасное Shift+ПКМ пустой рукой в пределах 2,5 блока даёт четыре доверия раз в двое суток. У каждого путника своё отношение.', 'Within three blocks offer a milk bucket, raw cod or salmon. Milk returns an empty bucket. The first gift adds 15 trust, plus 10 for milk or 8 for fish. Gifts have a one-day cooldown. Safe sneak-interaction within 2.5 blocks adds four trust every two days. Each traveller has a separate bond.', 'Дружба наступает при 85 доверия и сохраняется для вашего UUID в этом мире. Принятый дар также делает текущую встречу безопасной. Четыре ответа Баюна открывают рассказ, знакомство, честную подсказку о его собственном месте или прощание. Карта показывает приблизительную область, а не точные координаты. Подтверждённая дружба не является победой над котом.', 'Friendship begins at 85 trust and persists for your UUID in this world. An accepted gift also makes the meeting safe. Four dialogue choices offer a tale, an introduction, an honest hint to his own home or goodbye. The map shows an approximate area, not exact coordinates. Friendship is not a victory over the cat.'),
 'domovoy': ('home','creatures','Домовой хранит обжитое место и помнит отношение хозяина. Его дружба требует даров, а удары оставляют обиду. Это игровой вариант фольклорного домашнего духа.','Domovoy watches a settled home and remembers how he is treated. Gifts build friendship; blows leave resentment. This is a game interpretation of the household spirit.','Он принимает хлеб, молоко, мёд и печенье. Между дарами требуется минута. Не пытайтесь подружиться побоями: сильно обиженный дух откажется от еды.','He accepts bread, milk, honey and cookies, with a minute between gifts. A deeply offended spirit refuses food.','Дружбу можно закрепить у очага. Подойдите со своим другом и взаимодействуйте с очагом; чужого хранителя присвоить нельзя.','A friendship can be bound to a hearth. Interact with the hearth while your own friend is nearby; another player’s guardian cannot be claimed.','Преданный хранитель помогает у домашнего очага. Его отношение и домашняя привязка сохраняются; потеря книги не уничтожает дружбу.','A loyal guardian helps at the home hearth. The bond and home binding persist independently of the book.'),
 'kuzya': ('home','people','Маленький домовой Кузя обжился в доме Бабы-яги. Он не ждёт приказа путника: домашние заботы чередуются с отдыхом. Это отдельный игровой персонаж, а не ещё один противник.','Kuzya has settled in Baba Yaga’s house. Household chores alternate with rest without a traveller’s command. He is a separate game character, not another enemy.','Кузя ходит между домашними рабочими местами и может отдыхать на печи. Первое занятие начинается вскоре после загрузки дома.','He walks between household work spots and can rest on the stove. His first activity begins shortly after the house loads.','Заговорите правой кнопкой. Его короткие реплики не означают выдачу нового поручения. Не перекрывайте проходы к столу и печи.','Interact to talk. His short remarks do not create a new quest. Keep the routes to the table and stove open.','Домашняя сцена и маршрут сохраняются. Печь требует физического подъёма и спуска; Кузя не телепортируется по дому ради бытового занятия.','His household scene and route persist. Stove visits use a physical ascent and descent rather than chore teleports.'),
 'baba_yaga': ('home','people','Хозяйка дома на курьих ножках проверяет путника делами, а не красивыми словами. Образ из сказок соединён здесь с личной репутацией и настоящими поручениями.','The mistress of the chicken-legged house judges travellers by deeds. Her fairy-tale image is combined with personal reputation and real tasks.','Дом Яги имеет свой интерьер и домашнего Кузю. Разговор и подношения проверяются сервером; чужие успехи не становятся вашими.','Her house has its own interior and Kuzya. Dialogue and offerings are checked by the server; another traveller’s progress is not yours.','Пройдите вступительное знакомство и следуйте текущему поручению. Книга показывает уже существующее состояние её линии, не выдавая вторую копию награды.','Complete the introduction and follow the active task. This book displays the existing quest state without granting duplicate rewards.','У Яги есть три основных этапа и отдельные побочные договоры. Доверие, расход предметов и завершение остаются в её прежней системе; книга только читает её дневник.','Yaga has three main stages and separate side contracts. Her existing system owns trust, item consumption and completion; the book only reads the journal.'),
 'leshy': ('forest','creatures','Хозяин леса прячется за деревьями, но лесная тропа не делает его вашим слугой. Леший — фольклорный образ; его боевые приёмы являются игровой интерпретацией.','The forest master hides among trees. Leshy is a folklore figure; his combat abilities are a game interpretation.','Прежде чем сближаться, следите за его поворотом и замахом. Деревья и расстояние важнее прямой погони.','Watch his turn and wind-up before approaching. Trees and distance matter more than chasing directly.','Корни и ложное присутствие мешают движению. Урон и контроль проходят серверные проверки; одна встреча не подтверждает победу.','Roots and false presences hinder movement. Damage and control are server-checked; an encounter is not proof of victory.','Леший имеет собственные боевые состояния. Запись о победе появляется только от реального события убийства самим путником, отдельно от простого изучения.','Leshy has his own combat states. A defeated mark requires an actual kill by the traveller, separately from studying him.'),
 'bannik': ('home','creatures','Баня бывает занята не только людьми. Банник не любит беспечности возле жара и пара; его образ взят из поверий о банном духе.','A bathhouse is not always occupied only by people. Bannik dislikes carelessness near heat and steam; his image comes from bathhouse-spirit beliefs.','Не тесните духа у печи. Его пар и ближний удар опасны на коротком расстоянии.','Do not crowd him beside the stove. Steam and close strikes are dangerous at short range.','Банный веник, хлеб и бутылка воды подходят для дара. После воды остаётся стеклянная бутылка. Принятый дар временно делает встречу мирной.','A bath broom, bread and water bottle are accepted gifts. Water leaves a glass bottle; a gift makes the encounter temporarily peaceful.','Мир после дара ограничен игровым временем. Дружеское состояние не доказывает, что дух был побеждён; книги различают эти события.','Gift peace lasts a limited amount of game time. Peace is not proof of defeating the spirit.'),
 'igosha': ('home','creatures','Игоша тревожит людей маленькими неприятностями. Это не взрослый воин и не новый босс; игровой образ связан с домашними поверьями.','Igosha troubles people with small misfortunes. He is neither an adult warrior nor a new boss; his game image is linked to household beliefs.','Слабость и помехи зрению затрудняют обычные дела рядом с ним.','Weakness and vision disturbances hinder everyday work nearby.','Предложите хлеб, медовый хлеб или лён. Принятое подношение позволяет ему уйти; иногда остаётся старая пуговица.','Offer bread, honey bread or flax. An accepted offering lets him depart; sometimes an old button remains.','Уход занимает короткую сцену, а не мгновенную выдачу боевой победы. Подношение расходуется сервером только при принятии.','Departure is a short scene, not an instant combat victory. The server consumes an offering only when it is accepted.'),
 'kikimora': ('home','creatures','Кикимора связана с беспокойным домашним местом. В игре это враждебное существо со своим обликом и ближним боем.','Kikimora is associated with a troubled household place. In the game she is a hostile creature with her own appearance and close combat.','Она сближается для коротких ударов. Слишком тесный проход мешает отступить.','She closes in for short strikes. Narrow passages make retreat difficult.','Двойной удар может замедлить путника. Не считайте первый замах завершением всей серии.','A double strike may slow a traveller. The first swing is not the end of the sequence.','Настоящая победа фиксируется отдельно от встречи и взаимодействия. Прядь Кикиморы связана с существующими обрядовыми материалами.','An actual victory is recorded separately from meeting or interacting. Kikimora’s strand belongs to the existing ritual materials.'),
 'polevik': ('field','creatures','Полевик напоминает, что поле имеет своего хозяина. Подношение и бой здесь разные способы встречи с игровым духом.','Polevik reminds travellers that a field has its own master. Offerings and combat are different ways to meet this game spirit.','В поле следите за корнями и ловушкой движения. Не подходите без возможности выйти на свободное место.','Watch for roots and movement traps. Keep an open escape route.','Оставленные вами хлеб, пшеница и медовый хлеб могут умиротворить хозяина. Дары учитывают владельца, а не любого соседнего игрока.','Bread, wheat and honey bread dropped by you may appease the master. Gifts are attributed to their owner.','Накопленное умиротворение может дать удачу и редкий полевой свёрток. Эти события не заменяют запись о реальном убийстве.','Appeasement may grant luck and a rare field bundle. Neither event replaces a real kill record.'),
 'poludnitsa': ('field','creatures','Полуденница появляется в образах опасного полуденного поля. В этой игре её жара и оружие превращают близкую встречу в испытание.','Poludnitsa evokes the danger of a noon field. In this game her heat and weapon make a close encounter hazardous.','Жар вокруг неё ослабляет путника. Не задерживайтесь в пределах ближнего боя.','Nearby heat weakens travellers. Do not linger at melee distance.','Следите за направлением конусного удара и уходите в сторону во время подготовки.','Watch the direction of her cone strike and move sideways during its preparation.','Сухой колос связан с существующей руной Полдня. Победа и получение материала являются разными подтверждениями.','The dry ear of grain belongs to the existing Noon rune. Victory and obtaining a material are separate confirmations.'),
}

# Non-template factual observations for the remaining registered living actors.
OBSERVATIONS = {
 'ovinnik': ('home','creatures','Овинник связан с хозяйственным строением и огнём.','Ovinnik is associated with farm buildings and fire.'),
 'nochnitsa': ('field','creatures','Ночница тревожит ночью криком и атакой из тёмного покрова. Волки избегают её зловещего присутствия.','Nochnitsa disturbs the night with screams and attacks from a dark cloak. Wolves avoid her ominous presence.'),
 'podvey': ('field','creatures','Подвей — игровой противник ветреных дорог; держите пространство для отступления и следите за подготовкой атаки.','Podvey is a game enemy of windswept roads; leave room to retreat and watch the attack preparation.'),
 'bolotnik': ('water','creatures','Болотник держится сырого места. Топь и узкие проходы делают его ближние атаки опаснее.','Bolotnik keeps to wet ground. Mire and narrow passages make his close attacks more dangerous.'),
 'rusalka': ('water','creatures','Русалка здесь человекоподобна, без рыбьего хвоста. Её предупреждение переходит в песню и притяжение; препятствие или уход срывают зов.','Rusalka is human-shaped, without a fish tail. Her warning develops into a song and pull; an obstacle or retreat interrupts the call.'),
 'vodyanoy': ('water','creatures','Водяной реагирует на подношения и вылов в своём водоёме. Хлеб, каравай и перламутр могут улучшить отношение; скопление сетей раздражает хозяина.','Vodyanoy reacts to gifts and fishing in his water. Bread, a round loaf and mother-of-pearl may improve relations; concentrated nets irritate him.'),
 'elder_vodyanoy': ('water','creatures','Старший Водяной — авторская игровая версия древнего духа воды. Омут пробуждают знаком у центрального камня; победа даёт жемчужины.','Elder Vodyanoy is an original game version of an ancient water spirit. A token awakens the pool at its central stone; victory yields pearls.'),
 'grey_wolf': ('legends','creatures','Серый Волк — участник особой встречи, а не обычный приручаемый волк. Его испытание включает укусы, прыжок, обход и зов помощников.','Grey Wolf belongs to a special encounter, not ordinary wolf taming. His trial includes bites, pounces, flanking and calling helpers.'),
 'sivka_burka': ('legends','creatures','Сивка-Бурка связан с личным обрядом и призывом. Обычные поводок, седло и конская броня не заменяют его собственную систему связи.','Sivka-Burka uses a personal ritual and summon. A normal lead, saddle or horse armour does not replace his bond system.'),
 'firebird': ('legends','creatures','Жар-птица связана с отдельной редкой встречей и огненным пером. Её знакомство нельзя свести к обычной охоте за курицей.','Firebird belongs to a separate rare encounter and a fiery feather. Meeting her is not ordinary chicken hunting.'),
 'nightingale': ('roads','people','Соловей-разбойник — широкоплечий человек у помоста большого стана. Перед свистом он набирает воздух; толстое укрытие помогает, хлипкая ограда ненадёжна.','Nightingale the Robber is a broad-shouldered man by the large camp platform. He draws breath before whistling; substantial cover helps, flimsy fences are unreliable.'),
 'earth_spirit': ('depths','creatures','Хранитель Сердца Земли пробуждается у разрушенной печати. Столбы вырываются лишь однажды; постоянные укрытия защищают от камней. Уход с арены усыпляет его с сохранённым здоровьем.','The Earth Heart guardian awakens at the broken seal. Pillars can be uprooted only once; permanent cover blocks stones. Leaving the arena lets him sleep with his health retained.'),
 'earth_servant': ('depths','creatures','Земляной слуга связан с боем хранителя глубин. Это помощник встречи, а не самостоятельная редкая легенда.','An earth servant belongs to the depths guardian encounter. It is an encounter helper, not a separate rare legend.'),
 'podzemnik': ('depths','creatures','Подземники защищают глубины; шахтёрский вариант отличается железной киркой и боевыми свойствами.','Podzemniks defend the depths; the miner variant has an iron pickaxe and different combat attributes.'),
 'large_podzemnik': ('depths','creatures','Крупный подземник готовит тяжёлый удар, сотрясает землю и разгоняется физически. Преграды прерывают его натиск.','The large podzemnik prepares heavy blows, trembles the ground and charges physically. Obstacles interrupt its rush.'),
 'kladovik': ('depths','creatures','Кладовик прячется в землю между атаками, бросает камни и создаёт шипы. Монетная волна отталкивает; после гибели его собственные снаряды убираются.','Kladovik burrows between attacks, throws stones and creates spikes. A coin wave knocks back; his owned projectiles are cleared on death.'),
 'tugarin_zmey': ('roads','creatures','Тугарин имеет две боевые фазы с переходом. Каменные снаряды и змеиный образ принадлежат его встрече, а не новым отдельным боссам.','Tugarin has two combat phases with a transition. Stone projectiles and the serpent image belong to his encounter, not separate bosses.'),
 'likho_one_eyed': ('field','creatures','Лихо Одноглазое связано с дурной долей и собственной встречей. Полученный оберег сам по себе не доказывает личную победу.','One-Eyed Likho belongs to ill fate and its own encounter. Receiving its charm alone does not prove a personal victory.'),
 'volkolak': ('field','creatures','Волколак отличается от лесного волка: это враждебный мифологический образ, а не домашний спутник.','Volkolak differs from a forest wolf: it is a hostile mythological figure, not a household companion.'),
 'cursed_volkolak': ('field','creatures','Проклятый волколак чередует укусы, серии когтей, прыжки и обход. Его вой и захват требуют внимания к подготовке и выходу из ближнего боя.','The cursed volkolak alternates bites, claw sequences, pounces and flanking. Its howl and grab demand attention to preparation and escape from melee.'),
 'upyr': ('burial','creatures','Упырь — враждебный мертвец курганного мира. Серебро связано с существующей системой уязвимости нечисти, а не с новым универсальным уроном по всем мобам.','Upyr is a hostile undead of the burial world. Silver belongs to the existing vulnerability system, not universal bonus damage against all mobs.'),
 'nav': ('burial','creatures','Навь принадлежит беспокойным мертвецам. Осмотр погребального места и реальная победа дают разные уровни знания.','Nav belongs to the restless dead. Examining a burial site and actually defeating one provide different knowledge levels.'),
 'kurgan_druzhinnik': ('burial','creatures','Курганный дружинник сохраняет воинский облик и охраняет погребальное место. Не принимайте его за мирного жителя.','The burial warrior retains a martial appearance and guards a burial place. Do not mistake him for a peaceful resident.'),
 'kurgan_voevoda': ('burial','creatures','Курганный воевода выделяется среди погребальных защитников. Его вооружение и подготовка требуют отдельного внимания.','The burial voevoda stands out among burial defenders. His equipment and preparation require particular attention.'),
 'buried_volkhv': ('burial','creatures','Погребённый волхв связан с обрядами курганов. Его атаки принадлежат собственному боевому автомату, а не общему шаблону разбойника.','The buried volkhv belongs to burial rituals. His attacks use his own combat state machine, not the bandit template.'),
 'unresting_prince': ('burial','creatures','Неупокоенный князь — особый защитник погребальной истории. Одна находка в кургане не подтверждает, что вы его победили.','The unresting prince is a special defender of a burial story. Finding a burial item does not prove you defeated him.'),
 'pesigolovets': ('roads','creatures','Песиголовцы действуют стаей: охотник, разведчик и вожак используют разное оружие. Копьё, лук, нож и топор требуют разных ответов на замах.','Dogheads work in packs: hunter, scout and leader use different weapons. Spear, bow, dagger and axe call for different responses to their wind-ups.'),
 'bandit_fighter': ('roads','people','Боец шайки подходит для удара с видимым замахом и восстановлением. Разбойники здесь люди, а не нечисть.','The gang fighter closes in with a visible wind-up and recovery. Bandits are people, not supernatural creatures.'),
 'bandit_archer': ('roads','people','Разбойник-лучник держит дистанцию и стреляет. Укрытие и обход безопаснее прямого бега по открытому месту.','The bandit archer keeps distance and shoots. Cover and flanking are safer than charging across open ground.'),
 'bandit_heavy': ('roads','people','Тяжёлый разбойник ненадолго поднимает щит. Дождитесь окончания защиты и следите за следующим замахом.','The heavy bandit raises a shield briefly. Wait for the guard to end and watch the next wind-up.'),
 'bandit_senior': ('roads','people','Старший малой шайки отличается экипировкой, но не является отдельным боссом. Основные припасы находятся в лагере.','The small gang’s senior has different equipment but is not a separate boss. Most supplies are in the camp.'),
 'ataman': ('roads','people','Атаман помогает ближайшим подчинённым удерживать цель. У него тяжёлый замах и короткие фазы щита; после его смерти организационная помощь исчезает.','Ataman helps nearby subordinates hold their target. He has a heavy wind-up and short shield phases; his coordination ends on death.'),
 'chud_resident': ('depths','people','Житель Чуди принадлежит поселению глубин. Разговор и торговля не являются убийством или наградой за зачистку.','A Chud resident belongs to a settlement in the depths. Conversation and trade are not kills or clearing rewards.'),
 'chud_artisan': ('depths','people','Ремесленник Чуди предлагает товары своей профессии. Очищающий плавень используется настоящим очистным горном.','The Chud artisan offers profession goods. Refining flux is used by the actual refining hearth.'),
 'chud_guard': ('depths','people','Страж Чуди защищает своих жителей. Не путайте охранника поселения с враждебным подземником.','The Chud guard protects residents. Do not confuse a settlement guard with a hostile podzemnik.'),
 'brown_bear': ('animals','nature','Бурый медведь защищает себя и семейство. У него тяжёлое тело и сильный ближний удар; медвежонок не является уменьшенным боссом.','The brown bear defends itself and its family. It has a heavy body and strong close strikes; a cub is not a miniature boss.'),
 'bear_cub': ('animals','nature','Медвежонок слабее взрослого зверя. Его присутствие связано с семейной защитой медведей.','A bear cub is weaker than an adult. Its presence is linked to the bears’ family defence.'),
 'forest_wolf': ('animals','nature','Лесной волк охотится и защищается, а при сильном ранении может отступить. Не смешивайте его с Серым Волком из особой встречи.','A forest wolf hunts and defends itself and may retreat when badly injured. It is distinct from Grey Wolf’s special encounter.'),
 'boar': ('animals','nature','Кабан отличается широким тяжёлым туловищем. Его ближняя защита опаснее, чем вид мирно стоящего зверя.','A boar has a broad, heavy body. Its close defence is more dangerous than its quiet stance suggests.'),
 'stag': ('animals','nature','Олень-самец отличается рогами и может защищать семейство. Это животное леса, а не новый мифологический противник.','A stag has antlers and may defend its family. It is a forest animal, not a new mythological enemy.'),
 'doe': ('animals','nature','Олениха связана с лесным семейством и бегством от угрозы. Берегите путь зверя к свободному месту.','A doe belongs to the forest family and flees danger. Keep its escape path open.'),
 'domestic_goat': ('animals','nature','Домашняя коза входит в существующее хозяйство и пищевые ресурсы. Это отдельное животное со своей моделью.','The domestic goat belongs to the existing husbandry and food resources and has its own model.'),
 'duck': ('animals','nature','Утка — водоплавающая птица существующего хозяйственного мира. Её мясо и яйцо относятся к реальным пищевым предметам.','The duck is a water bird of the existing farming world. Its meat and egg are actual food items.'),
 'goose': ('animals','nature','Гусь отличается от утки более длинной шеей и собственным обликом. Его продукты представлены отдельными предметами.','The goose differs from the duck by its longer neck and own appearance. Its products have separate item entries.'),
 'pike': ('water','nature','Щука вытянута и делает короткие рывки. Пресноводный улов дополняет обычную удочку; приготовленная щука нужна для ухи.','Pike is elongated and makes short bursts. Freshwater catches supplement the normal rod; cooked pike is used in fish soup.'),
 'carp': ('water','nature','Карп широк и спокоен. Его можно получить в пресноводном промысле; копчение связано с существующими рецептами.','Carp is broad and calm. Freshwater fishing can yield it; smoking uses existing recipes.'),
 'crayfish': ('water','nature','Рак держится у дна пресной воды. Его клешни и ходьба отличают его от рыб; это реальный водный обитатель.','Crayfish stays near the freshwater bottom. Claws and walking distinguish it from fish; it is an actual water inhabitant.'),
 'fire_serpent': ('home','creatures','Огненный змей имеет собственную сущность и огненный облик. Его не следует смешивать с проекцией Тугарина.','Fire Serpent has its own entity and fiery appearance. It is distinct from Tugarin’s projection.'),
}

OWNER = {'thrown_net':'vodyanoy','flying_broom':'baba_yaga','flying_mortar':'baba_yaga',
 'kladovik_spike':'kladovik','kladovik_stone':'kladovik','tugarin_stone':'tugarin_zmey',
 'serpent_projection':'tugarin_zmey','earth_encounter_actor':'earth_spirit','hot_stone':'earth_spirit',
 'ember_clump':'fire_serpent','kurgan_bolt':'buried_volkhv','doghead_arrow':'pesigolovets',
 'rune_visual':'runes','weapon_projectile':'weapons','furniture_seat':'furniture',
 'carcass':'hunting','finist_magic_arrow':'weapons'}

for key, pair in PARTS.items(): text('tales.part.'+key, *pair)
for key, pair in CHAPTERS.items(): text('tales.chapter.'+key, *pair)
for key, pair in SECTIONS.items(): text('tales.section.'+key, *pair)

recipe_labels={
 'open':('Рецепты: следующий / закрыть','Recipes: next / close'), 'unavailable':('Этот рецепт сейчас недоступен.','This recipe is currently unavailable.'),
 'condition':('Условие','Condition'), 'group':('Любой подходящий предмет из группы %s','Any matching item from group %s'),
 'yes':('да','yes'),'no':('нет','no'), 'type':('Рабочее место','Workstation'), 'pattern':('Ряды раскладки','Pattern rows'),
 'key':('Обозначения раскладки','Pattern key'),'ingredient':('Ингредиент','Ingredient'), 'ingredients':('Ингредиенты','Ingredients'),
 'inputs':('Последовательность материалов','Ordered materials'),'result':('Результат','Result'),'tool':('Инструмент','Tool'),
 'flux':('Очищающий плавень','Refining flux'),'chisel':('Требуется резец','Chisel required'),
 'time':('Время, игровых тиков','Time, game ticks'),'duration':('Длительность, игровых тиков','Duration, game ticks'),
 'cookingtime':('Время приготовления, игровых тиков','Cooking time, game ticks'), 'drying_time':('Время сушки, игровых тиков','Drying time, game ticks'),
 'servings':('Порции','Servings'),'Servings':('Порции','Servings'),'remainders':('Остающаяся тара','Returned containers'),
 'mode':('Способ приготовления','Preparation mode'),'experience':('Опыт','Experience'),'base':('Основа руны','Rune base'),
 'material':('Материал','Material'),'tier':('Ступень','Tier'),'catalyst':('Катализатор','Catalyst'),
 'retain_catalyst':('Катализатор сохраняется','Catalyst retained'),'copy_components':('Свойства исходного предмета сохраняются','Original item properties retained'),
 'components':('Свойства','Properties'),'Beverage':('Напиток','Beverage'),'Container':('Тара','Container'),
 'Quality':('Качество','Quality'),'minecraft:custom_data':('Дополнительные свойства','Additional properties'),
 'slavicmyths:rune_base':('Основа руны','Rune base'), 'mode.POT':('В горшке','Pot'), 'mode.ROLLING':('Раскатка теста','Dough rolling')}
for key,pair in recipe_labels.items():text('tales.recipe.'+key,*pair)
recipe_types={
 'crafting_shaped':('Верстак, раскладка','Crafting table, shaped'),'crafting_shapeless':('Верстак, свободное сочетание','Crafting table, shapeless'),
 'smelting':('Печь','Furnace'),'smoking':('Коптильня','Smoker'),'campfire_cooking':('Костёр','Campfire'),
 'stonecutting':('Камнерез','Stonecutter'),'armorer_shaped':('Оружейный стол, раскладка 4×4','Armorer table, 4×4 pattern'),
 'armorer_shapeless':('Оружейный стол, свободное сочетание','Armorer table, shapeless'),
 'chud_bloomery':('Домница Чуди','Chud bloomery'),'refining_hearth':('Очистной горн','Refining hearth'),
 'drying':('Сушильная стойка','Drying rack'),'kitchen':('Кухонный стол','Kitchen table'),
 'rune_crafting':('Рунное ремесло','Rune crafting'),'vat':('Бродильный чан','Fermentation vat')}
for key,pair in recipe_types.items():text('tales.recipe.type.'+key,*pair)
recipe_tags={'c.dyes.brown':('Коричневый краситель','Brown dye'),'c.dyes.red':('Красный краситель','Red dye'),
 'minecraft.planks':('Доски подходящей породы','Matching planks'),'minecraft.wooden_slabs':('Деревянные плиты','Wooden slabs'),
 'slavicmyths.berries':('Ягоды','Berries'),'slavicmyths.cooking.cooked_meats':('Приготовленное мясо','Cooked meat'),
 'slavicmyths.cooking.eggs':('Яйца','Eggs'),'slavicmyths.cooking.fish':('Подходящая рыба','Matching fish'),
 'slavicmyths.cooking.milk':('Молоко','Milk'),'slavicmyths.cooking.mushrooms':('Грибы','Mushrooms'),
 'slavicmyths.cooking.raw_meats':('Сырое мясо','Raw meat')}
for wood in ('apple','darkened','linden','pine','rowan','willow'):
    recipe_tags['slavicmyths.'+wood+'_logs']=(name('block','slavicmyths:'+wood+'_log','ru_ru'),name('block','slavicmyths:'+wood+'_log','en_us'))
for key,pair in recipe_tags.items():text('tales.recipe.tag.'+key,*pair)

for ident in DATA['entity_type'] + ['slavicmyths:kot_bayun']:
    key = ident.split(':')[1]
    if key in OWNER: continue
    if key in PROFILES:
        p = PROFILES[key]
        chapter, part = p[:2]
        pages = {lang: [p[2+i*2+j] for i in range(4)] for j,lang in enumerate(LOCALES)}
    else:
        chapter, part, ru, en = OBSERVATIONS[key]
        # Short specific observations are intentionally not inflated with
        # invented loot, folklore attribution or unsupported balance numbers.
        pages = {'ru_ru':[ru, ru+'\n\nНаблюдение рядом открывает запись о встрече. Книга не раскрывает точное место всех подобных существ.',
                          ru+'\n\nИзучение отмечается отдельным взаимодействием или связанным игровым событием.',
                          ru+'\n\nПодтверждённое знание и отметка «побеждён» хранятся отдельно. Получение предмета не считается личной победой.'],
                 'en_us':[en, en+'\n\nObservation nearby opens the encounter record. The book does not reveal every creature’s exact location.',
                          en+'\n\nStudy is recorded by an interaction or associated gameplay event.',
                          en+'\n\nVerified knowledge and a defeated mark are stored separately. Obtaining an item is not a personal victory.']}
    advanced = {'kot_bayun':['meet_bayun','friend_of_bayun'],'nightingale':['meet_nightingale','defeat_nightingale'],
                'baba_yaga':['guest_of_yaga','trusted_by_yaga'],'domovoy':['meet_domovoy','offer_domovoy','friend_domovoy','bind_hearth'],
                'earth_spirit':['earth_heart'],'tugarin_zmey':['steppe_champion']}.get(key,[])
    entry(key,part,chapter,['entity/'+ident],tuple(name('entity',ident,l) for l in LOCALES),pages,
          initial=1 if key in ('kot_bayun','grey_wolf','firebird','sivka_burka','baba_yaga') else 0,
          advances=['slavicmyths:'+a for a in advanced])

MECHANICS = {
 'runes': ('Руны', 'Runes', 'Руны Slavic Myths — вымышленные игровые знаки. Их создают обрядами на алтаре и устанавливают в совместимые гнёзда через Рунную наковальню. Класс для рун не нужен. Повторные одинаковые руны запрещены; обычные чары, имя и прочность предмета сохраняются.', 'Slavic Myths runes are fictional game signs. Altar rituals create them; the runic anvil installs them in compatible sockets. No class is required. Duplicate runes are forbidden; normal enchantments, name and durability are retained.'),
 'paths': ('Классы и Камень Пути', 'Classes and the Path Stone', 'Камень Пути открывает существующие Классы 2.0 и ставит путевую отметку. Три основы — дружинник, ведун и разбойник. Классовый опыт и очки развития отделены от ванильных уровней; ветви выбираются на уровнях 10 и 25. Три активных слота по умолчанию связаны с G/H/J, один пассивный слот и автоматические триггеры дополняют их. Экран открывается клавишей K или у камня. Сервер проверяет уровень, очки, ранги, оружие, цель и перерыв между применениями. Смерть не стирает развитие; книга не выдаёт бесплатных навыков.', 'The Path Stone opens the existing Classes 2.0 screen and records a waymark. The three bases are warrior, vedun and rogue. Class XP and skill points are separate from vanilla levels; branches are chosen at levels 10 and 25. Three active slots default to G/H/J, with one passive slot and automatic triggers. K or the stone opens the screen. The server checks level, points, ranks, weapons, targets and cooldowns. Death does not erase progression; this book grants no free skills.'),
 'weapons': ('Оружие и защита', 'Weapons and protection', 'Меч, кинжал, копьё, топор, булава, посох и щит имеют разные назначения и силуэты. Оружейный стол использует сетку 4×4 и собственные рецепты. Рецепт и свойства конкретного предмета смотрите в обычной книге рецептов, подсказке предмета и JEI, если он установлен.', 'Sword, dagger, spear, axe, mace, staff and shield have distinct roles and silhouettes. The armorer table uses a 4×4 grid and its own recipes. Consult actual recipe-book entries, item tooltips and optional JEI for individual recipes and properties.'),
 'furniture': ('Мебель и хранение', 'Furniture and storage', 'Столы, стулья, скамьи, шкафы и сундуки составляют хозяйственный интерьер. Сиденье — техническая сущность мебели, а не новый живой обитатель. Хранилища и рабочие блоки сохраняют своё содержимое обычным способом.', 'Tables, chairs, benches, wardrobes and chests form household interiors. A seat is a technical furniture entity, not a living inhabitant. Containers and work blocks retain their contents normally.'),
 'hunting': ('Охота', 'Hunting', 'Туша — результат охоты, а не самостоятельное мифологическое существо. Её обработка относится к существующим инструментам и пищевым ресурсам. Не смешивайте добычу животного с победой над особой легендой.', 'A carcass is a hunting result, not an independent mythological creature. Processing uses existing tools and food resources. An animal harvest is distinct from defeating a special legend.'),
 'fishing': ('Рыбалка и сети', 'Fishing and nets', 'Удочка сохраняет ванильную рыбалку; пресноводные уловы дополняют её. Сеть ставят в воде у дна, улов забирают правой кнопкой. Shift+ПКМ снимает сеть. Скопление сетей снижает эффективность и может раздражать Водяного.', 'The rod retains vanilla fishing with additional freshwater catches. Place a net in water near the bottom and interact to collect catches. Sneak-interaction removes it. Concentrated nets reduce efficiency and may irritate Vodyanoy.'),
 'industry': ('Ремесло Чуди', 'Chud industry', 'Железистый комок плавится в Домнице Чуди. Очистной горн расходует металлический слиток, очищающий плавень и топливо для очищенного металла. Плавень продают ремесленники и плавильщики; очищенные металлы связаны с настоящими аксессуарами.', 'The Chud bloomery smelts bog-iron lumps. The refining hearth consumes an ingot, refining flux and fuel to produce refined metal. Artisans and smelters sell flux; refined metals are used for actual accessories.'),
 'rituals': ('Обряды', 'Rituals', 'Алтарь принимает дары строго по порядку; неверный предмет не расходуется. Пустая рука подсказывает следующий шаг. Незавершённый обряд сохраняется в блоке и может быть продолжен другим путником. Разрушение алтаря отменяет незавершённую последовательность.', 'The altar accepts offerings in order and does not consume a wrong item. An empty hand reveals the next step. An unfinished ritual persists in the block and another traveller can continue it. Breaking the altar cancels the unfinished sequence.'),
 'book': ('Память путника', 'Traveller’s memory', 'Новая книга выдаётся один раз, когда есть место в инвентаре. Знания принадлежат вашему UUID в мире, а не экземпляру книги. Смерть и потеря предмета не стирают записи. Чтение само по себе не подтверждает победу и не выдаёт наград чужих поручений.', 'A new book is delivered once when inventory space is available. Knowledge belongs to your world UUID, not the book item. Death and item loss do not erase entries. Reading does not prove a victory or grant someone else’s quest rewards.'),
}
for key,(ru_title,en_title,ru,en) in MECHANICS.items():
    subjects=[]
    for item in DATA['items']:
        p=item['id'].split(':')[1]; cls=item['class']
        if (key=='runes' and ('rune' in p or p=='runic_anvil') or key=='paths' and p=='path_stone' or
            key=='weapons' and any(v in cls for v in ('Sword','Dagger','Spear','Axe','Mace','Staff','Shield','Armor','Flail')) or
            key=='furniture' and any(v in p for v in ('chair','bench','wardrobe','shelf','stool','bedside','table','chest')) or
            key=='hunting' and any(v in p for v in ('carcass','skinning','hunting')) or
            key=='fishing' and any(v in p for v in ('net','fishing')) or
            key=='industry' and any(v in p for v in ('refined','flux','bloomery','bog_iron','refining_hearth')) or
            key=='rituals' and p in ('altar','ancient_sign','ritual_bowl','ritual_knife') or
            key=='book' and p=='lore_book'):
            subjects.append('item/'+item['id'])
            if item['id'] in DATA['block']: subjects.append('block/'+item['id'])
    entry(key,'crafts','skills' if key in ('runes','paths','rituals') else 'materials',subjects,(ru_title,en_title),
          {'ru_ru':[ru]*4,'en_us':[en]*4},initial=1 if key=='book' else 0)

entry('welcome','tales','legends',['item/slavicmyths:lore_book'],('Как читать сказания','Reading the tales'),
      {'ru_ru':['Книга соединяет рассказы о мире и память о ваших собственных встречах. Слева выберите раздел, затем главу, тему и запись. Стрелки листают доступные страницы; колесо прокручивает длинный текст. Поиск находит запись в текущем разделе. Слух открывает рассказ, встреча — наблюдения, изучение — практические сведения. Проверенная запись требует настоящего события. Отметка о победе хранится отдельно. Карта слухов появляется только после достоверной подсказки и показывает приблизительную область.']*4,
       'en_us':['The book combines world tales with memories of your own encounters. On the left choose a category, chapter, topic and entry. Arrows turn available pages; the wheel scrolls long text. Search finds entries in the current category. Rumours reveal stories, encounters reveal observations and study reveals practical information. Verification requires an actual event. Defeated marks are separate. Rumour maps require a reliable hint and show an approximate area.']*4},initial=1,advances=['slavicmyths:first_book_entry'])
entry('yaga_journal','quests','journal',[],('Поручения Бабы-яги','Baba Yaga’s tasks'),
      {'ru_ru':['Здесь отображается состояние уже принятого поручения Бабы-яги. Возьмите работу в разговоре с хозяйкой и выполняйте её настоящие условия. Книга служит дневником: она не принимает поручение вместо вас, не расходует дары и не выдаёт награду второй раз. Основная линия имеет три этапа; побочные договоры остаются в прежней системе Яги. Баюн рассказывает свои истории, но не завершает её работу за путника.']*4,
       'en_us':['This journal displays an existing task accepted from Baba Yaga. Speak to her to accept work and fulfil its actual conditions. The book does not accept a task for you, consume offerings or issue a reward twice. The main story has three stages; side contracts remain in Yaga’s existing system. Bayun tells his own tales but does not finish Yaga’s work for a traveller.']*4},initial=1)

# Every actual item is represented in a searchable, bounded family catalogue.
# These are reference lists, not fabricated individual mechanics descriptions.
groups=defaultdict(list)
for item in DATA['items']:
    ident=item['id']; p=ident.split(':')[1]
    if p.endswith('spawn_egg'): family='spawn_eggs'
    elif item['food']: family='food'
    elif 'Beverage' in item['class']: family='drinks'
    elif 'BlockItem' in item['class']:
        family=next((wood for wood in ('pine','linden','rowan','willow','apple','darkened') if p.startswith(wood+'_') or p.startswith('stripped_'+wood+'_')), 'blocks')
    else: family=re.sub(r'[^a-z0-9_.-]','_',item['class'].split('.')[-1].lower())
    groups[family].append(item)
groups['spawn_eggs'].append({'id':'slavicmyths:kot_bayun_spawn_egg','class':'DeferredSpawnEggItem','food':False})
for family, items in sorted(groups.items()):
    # Split long catalogues into useful alphabetic pages; each retains a unique
    # real subject list and all titles come from the installed localization.
    items=sorted(items,key=lambda i:name('item',i['id'],'ru_ru').casefold())
    for start in range(0,len(items),18):
        chunk=items[start:start+18]; key=f'items_{family}_{start//18+1}'
        subjects=['item/'+i['id'] for i in chunk]
        subjects+=['block/'+i['id'] for i in chunk if i['id'] in DATA['block']]
        pages={}
        for lang in LOCALES:
            listing='\n'.join('• '+name('item',i['id'],lang) for i in chunk)
            pre='Пищевые предметы' if family=='food' else 'Предметы и блоки'
            if lang=='ru_ru':
                pages[lang]=[f'{pre} существующего мира. Запись открывается после получения предмета или знакомства с блоком.\n\n'+listing,
                             'В этом разделе перечислены реальные предметы, а не обещания будущих обновлений.\n\n'+listing,
                             'Рецепты проверяются сервером. Используйте соответствующее рабочее место; точные количества показывает обычная книга рецептов или JEI.\n\n'+listing,
                             'Полученный и изготовленный предмет — разные события знания. Эта запись не является наградой за победу над владельцем материала.\n\n'+listing]
            else:
                pages[lang]=['Actual possessions in the existing world. Obtain an item or examine a block to discover this record.\n\n'+listing,
                             'This section lists real items, not promises of future updates.\n\n'+listing,
                             'Recipes are server-checked. Use the appropriate workstation; exact quantities are shown by the normal recipe book or JEI.\n\n'+listing,
                             'Obtaining and crafting are different knowledge events. This record is not a victory reward for defeating a material’s owner.\n\n'+listing]
        titles=(name('item',chunk[0]['id'],'ru_ru')+' — '+name('item',chunk[-1]['id'],'ru_ru'),
                name('item',chunk[0]['id'],'en_us')+' — '+name('item',chunk[-1]['id'],'en_us'))
        entry(key,'nature' if family in ('pine','linden','rowan','willow','apple') else 'crafts',
              'plants' if family in ('pine','linden','rowan','willow','apple') else 'food' if family in ('food','drinks') else 'materials',subjects,titles,pages,icon=chunk[0]['id'])

# Technical entity types are discoverable through the real associated owner,
# but are not counted or narrated as separate creatures.
for child,owner in OWNER.items():
    entries['slavicmyths:'+owner]['subjects'].append('entity/slavicmyths:'+child)

represented_blocks={s for e in entries.values() for s in e['subjects'] if s.startswith('block/')}
garden=[]
for ident in DATA['block']:
    if 'block/'+ident in represented_blocks: continue
    key=ident.split(':')[1]
    if key.startswith('yaga_'):
        entries['slavicmyths:baba_yaga']['subjects'].append('block/'+ident)
    elif '_wall_' in key:
        counterpart='item/slavicmyths:'+key.replace('_wall_','_')
        owner=next((e for e in entries.values() if counterpart in e['subjects']),None)
        assert owner is not None,ident
        owner['subjects'].append('block/'+ident)
    else: garden.append('block/'+ident)
if garden:
    entry('garden','nature','plants',garden,('Огород и ягодные кусты','Garden and berry bushes'),
          {'ru_ru':['Злаки, овощи, лён и ягодные кусты имеют собственные растущие блоки. Семена и урожай представлены отдельными предметами. Осматривайте посадки и собирайте зрелые растения; книга не заменяет их настоящие правила роста.']*4,
           'en_us':['Grain, vegetables, flax and berry bushes have their own growing blocks. Seeds and harvests are separate items. Examine crops and gather mature plants; the book does not replace their actual growth rules.']*4})

PLACE_TEXT = {
 'swamp_hut': ('Старая избушка поднята над сырой землёй. Печь и сени напоминают о прежних жильцах; повреждённая кровля иногда пропускает воду.','An old hut is raised above wet ground. Stove and entrance room recall former residents; its damaged roof may admit water.'),
 'abandoned_settlement': ('Колодец связывает двор, дома и сарай. Один дом сохранился лучше, другой постепенно ушёл под воду.','A well links the yard, houses and barn. One house is better preserved; another has gradually flooded.'),
 'bog_causeway': ('Продольные брёвна держат старый настил. Гать помогает пройти мокрый участок, хотя отдельные доски просели.','Lengthwise logs support an old walkway. The causeway crosses wet ground, though some boards have subsided.'),
 'flooded_shrine': ('Сломанные столбы очерчивают площадку со старым знаком. Он не требует нового обряда; это авторское игровое место.','Broken posts outline a platform with an old sign. It requires no new ritual; this is an original game location.'),
 'fishing_camp': ('Навес, бочки и рабочее место принадлежали рыбакам. Разрушенный причал и корпус лодки напоминают о старом промысле.','A canopy, barrels and workplace belonged to fishers. A broken pier and boat hull recall their old trade.'),
 'underwater_ruins': ('Фундаменты и балки лежат на дне. Запомните путь к поверхности: снаряжение не отменяет запас воздуха.','Foundations and beams lie on the bottom. Remember the route to the surface: equipment does not remove the need for air.'),
 'bandit_camp_small': ('Низкие шатры, костёр и припасы выдают временную стоянку небольшой шайки. Старший не является боссом.','Low tents, a fire and supplies mark a small gang’s temporary camp. The senior is not a boss.'),
 'bandit_camp_medium': ('Склад, дозорная площадка и частокол защищают шайку. Зачистка проверяет постоянных защитников, а не просто открытый сундук.','A store, lookout and palisade protect the gang. Clearing checks its permanent defenders, not merely an opened chest.'),
 'bandit_camp_large': ('Большой разбойничий стан связан с Соловьём и его помостом у старого дерева. Припасы не означают, что двор безопасен.','The large bandit camp belongs to Nightingale and his platform by an old tree. Supplies do not mean the yard is safe.'),
 'chud_city': ('Владения Чуди соединяют жителей, ремесло и охрану в глубинах. Это существующее поселение, а не новое измерение.','Chud holdings combine residents, crafts and guards in the depths. This is an existing settlement, not a new dimension.'),
 'chud_abandoned': ('Покинутые владения Чуди сохраняют следы подземного ремесла. Жилое поселение и руины имеют разные условия встречи.','Abandoned Chud holdings retain traces of underground crafts. A living settlement and ruins have different encounter conditions.'),
 'kurgan_small': ('Малый курган хранит погребальное пространство. Внимательно осматривайте камень и проходы.','A small mound preserves a burial space. Examine its stonework and passages carefully.'),
 'kurgan_warrior': ('Воинский курган связан с вооружёнными погребальными защитниками. Узкий проход не гарантирует защиту.','A warrior mound is associated with armed burial defenders. A narrow passage does not guarantee safety.'),
 'kurgan_great': ('Великий курган отличается масштабом и внутренними погребальными частями. Его находки не заменяют настоящей победы над защитником.','A great mound has larger burial chambers. Its finds do not replace an actual victory over a defender.'),
 'gorodishche': ('Городище сохраняет укреплённый человеческий двор и хозяйственные следы. Это игровая реконструкция.','The fortified settlement retains a human yard and household traces. It is a game reconstruction.'),
 'swamp_watchtower': ('Болотная дозорная башня возвышается над сырым местом. Осматривайте опоры и проходы перед подъёмом.','A swamp watchtower rises above wet ground. Examine supports and passages before climbing.'),
 'swamp_remnants': ('Болотные остатки показывают, как человеческие постройки разрушаются в сырой земле.','Swamp remnants show human buildings decaying in wet ground.')}
PLACE_NAMES={'chud_abandoned':'Покинутые владения Чуди','bandit_camp_large':'Большой разбойничий стан',
             'kurgan_great':'Великий курган','bandit_camp_medium':'Укреплённый лагерь шайки','chud_city':'Владения Чуди',
             'swamp_watchtower':'Болотная дозорная башня','kurgan_small':'Малый курган','bandit_camp_small':'Малая разбойничья стоянка',
             'gorodishche':'Городище','kurgan_warrior':'Воинский курган','swamp_remnants':'Болотные остатки'}
for ident in DATA['worldgen/structure']:
    key=ident.split(':')[1]; ru,en=PLACE_TEXT[key]
    titles=(LOCALES['ru_ru'].get('book.slavicmyths.'+key+'.title',PLACE_NAMES.get(key,key.replace('_',' ').capitalize())),
            LOCALES['en_us'].get('book.slavicmyths.'+key+'.title',key.replace('_',' ').capitalize()))
    entry('place_'+key,'places','roads' if 'bandit' in key else 'burial' if 'kurgan' in key else 'depths' if 'chud' in key else 'water',
          ['structure/'+ident],titles,{'ru_ru':[ru]*4,'en_us':[en]*4},advances=['slavicmyths:find_'+key])

# Feature variants are grouped under the actual world-generation family. Seeing
# an associated plant or block reveals the family, never an exact location.
features=defaultdict(list)
FEATURE_NAMES={'ancient_shrine':'Древнее капище','bathhouse':'Баня','berry_patch':'Ягодная полянка','deep_pool':'Старый омут',
               'blackcurrant_garden_patch':'Смородиновый сад','blueberry_garden_patch':'Черничная полянка',
               'bog_iron':'Болотная руда','cranberry_garden_patch':'Клюквенная полянка','lingonberry_garden_patch':'Брусничная полянка',
               'old_barn':'Старый овин','ore_perunite':'Перунитовая жила','ore_silver':'Серебряная жила',
               'patch_fireweed':'Заросли иван-чая','patch_flax':'Заросли льна','patch_juniper':'Можжевельник',
               'patch_nettle':'Заросли крапивы','patch_st_johns_wort':'Зверобой','patch_wormwood':'Заросли полыни',
               'path_shrine':'Малое святилище Пути','raspberry_garden_patch':'Малинник','swamp_dead':'Мёртвое болотное дерево',
               'swamp_detail':'Болотные подробности','swamp_nature':'Болотная растительность','swamp_oak':'Болотный дуб',
               'water_patch':'Водные растения'}
for registry in ('worldgen/feature','worldgen/configured_feature','worldgen/placed_feature'):
    for ident in DATA[registry]+(['slavicmyths:bayun_nook'] if registry!='worldgen/structure' else []):
        key=ident.split(':')[1]
        family=next((v for v in ('pine','willow','linden','rowan','apple') if v in key),key)
        features[family].append('feature/'+ident)
for family, subjects in sorted(features.items()):
    if family=='bayun_nook': entries['slavicmyths:kot_bayun']['subjects']+=subjects;continue
    if family in ('pine','willow','linden','rowan','apple'):
        block=family+'_log'; titles=tuple(name('block','slavicmyths:'+block,l) for l in LOCALES)
        subjects+=['block/slavicmyths:'+block,'item/slavicmyths:'+family+'_sapling']
        ru='Деревья этой породы появляются в новых подходящих чанках. Саженец позволяет вырастить своё дерево. Доски, двери, ограды и таблички используют настоящие рецепты этой древесины.'
        en='Trees of this family appear in suitable new chunks. A sapling grows your own tree. Planks, doors, fences and signs use actual timber recipes.'
    else:
        titles=(LOCALES['ru_ru'].get('book.slavicmyths.'+family+'.title',FEATURE_NAMES[family]),family.replace('_',' ').capitalize())
        ru=LOCALES['ru_ru'].get('book.slavicmyths.'+family+'.text','Эта особенность входит в существующую генерацию мира. Она появляется только в подходящих новых чанках; книга не создаёт её и не загружает далёкие области для поиска.')
        en=LOCALES['en_us'].get('book.slavicmyths.'+family+'.text','This feature belongs to the existing world generation. It appears in suitable new chunks; the book neither creates it nor loads distant areas to search.')
        marker={'ancient_shrine':'ancient_idol','bathhouse':'bath_stove','deep_pool':'pool_stone','path_shrine':'path_stone',
                'bog_iron':'bog_iron_ore','ore_silver':'silver_ore','ore_perunite':'perunite_ore',
                'patch_fireweed':'fireweed','patch_flax':'flax','patch_juniper':'juniper_berries','patch_nettle':'nettle',
                'patch_st_johns_wort':'st_johns_wort','patch_wormwood':'wormwood',
                'blackcurrant_garden_patch':'blackcurrant_bush','blueberry_garden_patch':'blueberry_bush',
                'cranberry_garden_patch':'cranberry_bush','lingonberry_garden_patch':'lingonberry_bush',
                'raspberry_garden_patch':'raspberry_bush','berry_patch':'raspberry_bush','water_patch':'white_lily',
                'swamp_detail':'reed'}.get(family)
        if marker:subjects+=['block/slavicmyths:'+marker,'item/slavicmyths:'+marker]
    entry('feature_'+family,'nature','plants',subjects,titles,{'ru_ru':[ru]*4,'en_us':[en]*4})

# Link recipes to the entry of their actual result, including custom serializers.
by_subject=defaultdict(list)
for ident,e in entries.items():
    for s in e['subjects']:by_subject[s].append(ident)
for ident in DATA['recipes']:
    if ident == 'slavicmyths:wooden_barrel':continue  # Legacy block, no obtainable recipe.
    key=ident.split(':')[1]
    path=RES/'data/slavicmyths/recipe'/f'{key}.json'
    recipe=json.loads(path.read_text(encoding='utf8')) if path.exists() else {}
    result=recipe.get('result',recipe.get('output',{}))
    result=result if isinstance(result,str) else result.get('id',result.get('item','')) if isinstance(result,dict) else ''
    owner=next(iter(by_subject.get('item/'+result,[])), 'slavicmyths:weapons' if 'armorer' in str(recipe.get('type','')) else 'slavicmyths:rituals')
    entries[owner]['subjects'].append('recipe/'+ident)

legacy_links={'leshy':['meet_leshy'],'bannik':['meet_bannik'],'vodyanoy':['meet_vodyanoy'],
              'rusalka':['meet_rusalka'],'elder_vodyanoy':['depth_clue','wake_depth_master','defeat_depth_master'],
              'feature_ancient_shrine':['shrine','ancient_find'],'rituals':['altar','first_ritual'],
              'fishing':['new_catch','net_catch'],'runes':['rune_installed'],
              'industry':['bog_iron','refined_metal'],'weapons':['learn_armorer'],
              'place_bandit_camp_small':['find_small_camp'],
              'place_bandit_camp_medium':['find_medium_camp','without_ataman'],
              'place_bandit_camp_large':['find_large_camp','clear_large_camp'],
              'nightingale':['black_feather','meet_nightingale','defeat_nightingale']}
for key in PLACE_TEXT:
    legacy_links.setdefault('place_'+key,[]).append('explore_'+key)
adv_dir=RES/'data/slavicmyths/advancement'
for key,ids in legacy_links.items():
    if 'slavicmyths:'+key in entries:
        entries['slavicmyths:'+key]['advancements']+=['slavicmyths:'+a for a in ids if (adv_dir/(a+'.json')).exists()]

# Observed default attributes and loot references are exported by the real
# dedicated-server audit. This adds useful specifics without inventing attacks.
facts_path=AUDIT/'registry-after.json'
if facts_path.exists():
    facts=json.loads(facts_path.read_text(encoding='utf8')).get('livingFacts',{})
    def loot_items(value):
        result=[]
        if isinstance(value,dict):
            if value.get('type')=='minecraft:item':result.append(value.get('name',''))
            for v in value.values():result+=loot_items(v)
        elif isinstance(value,list):
            for v in value:result+=loot_items(v)
        return result
    for ident, fact in facts.items():
        key=ident.split(':')[1]
        if key not in OBSERVATIONS:continue
        title_key='tales.entry.'+key
        loot_path=RES/'data/slavicmyths/loot_table/entities'/f'{key}.json'
        drops=sorted(set(loot_items(json.loads(loot_path.read_text(encoding='utf8'))))) if loot_path.exists() else []
        for lang in LOCALES:
            ru=lang=='ru_ru';baseline=OBSERVATIONS[key][2 if ru else 3]
            attr=(f'Обычное здоровье: {fact["health"]:g} HP. Броня: {fact["armor"]}. Эти базовые значения проверены на сервере; варианты и боевые состояния могут их изменять.' if ru else f'Default health: {fact["health"]:g} HP. Armour: {fact["armor"]}. These defaults were checked on the server; variants and combat states may alter them.')
            discovery_attr=(f'Базовое здоровье: {fact["health"]:g} HP. Броня: {fact["armor"]}.' if ru else f'Default health: {fact["health"]:g} HP. Armour: {fact["armor"]}.')
            LOCALES[lang][title_key+'.discovered']=baseline+'\n\n'+discovery_attr
            if drops:
                listing=', '.join(name('item',d,lang) if d.startswith('slavicmyths:') else (VANILLA_RU if ru else LOCALES[lang]).get('item.'+d.replace(':','.'),(VANILLA_RU if ru else LOCALES[lang]).get('block.'+d.replace(':','.'),d.split(':')[-1].replace('_',' '))) for d in drops[:12])
                LOCALES[lang][title_key+'.studied']=baseline+'\n\n'+('В настоящей таблице добычи указаны: '+listing+'. Выпадение зависит от её условий и случайности, а не гарантируется чтением книги.' if ru else 'The actual loot table references: '+listing+'. Drops depend on its conditions and chance; reading the book guarantees none.')
            LOCALES[lang][title_key+'.verified']=baseline+'\n\n'+attr+'\n\n'+('Подтверждение относится к событию, которое совершили именно вы. Чужая находка или простое чтение не засчитывают личное убийство; отметка «побеждён» требует настоящей победы.' if ru else 'Verification belongs to an event performed by you. Someone else’s find or merely reading does not count as your kill; a defeated mark requires an actual victory.')

# Individually authored context, deliberately not one padded bestiary template.
# Concise English pages retain all existing mechanical facts; Russian is primary.
notes=json.loads((ROOT/'docs/bayun/creature-notes.json').read_text(encoding='utf8'))
for key,note in notes.items():
    assert 'slavicmyths:'+key in entries,key
    for suffix in ('rumor','discovered','studied','verified'):
        title='tales.entry.'+key+'.'+suffix
        LOCALES['ru_ru'][title]+='\n\n'+note
        if suffix in ('studied','verified') and key in PROFILES and 'slavicmyths:'+key in facts:
            f=facts['slavicmyths:'+key]
            LOCALES['ru_ru'][title]+=f'\n\nБазовые характеристики текущей реализации: здоровье {f["health"]:g} HP, броня {f["armor"]}. Изменения состояния существа следует оценивать во время самой встречи.'
            LOCALES['en_us'][title]+=f'\n\nCurrent default attributes: health {f["health"]:g} HP, armour {f["armor"]}. Evaluate changes of state during the actual encounter.'

for registry,kind in [('items','item'),('entity_type','entity'),('block','block'),('recipes','recipe'),
                       ('worldgen/structure','structure'),('worldgen/feature','feature'),
                       ('worldgen/configured_feature','feature'),('worldgen/placed_feature','feature')]:
    by_subject=defaultdict(list)
    for ident,e in entries.items():
        for s in e['subjects']:by_subject[s].append(ident)
    values=DATA[registry]+([dict(id='slavicmyths:kot_bayun_spawn_egg')] if registry=='items' else ['slavicmyths:kot_bayun'] if registry=='entity_type' else ['slavicmyths:bayun_nook'] if registry.startswith('worldgen/') and registry!='worldgen/structure' else [])
    for v in values:
        ident=v['id'] if isinstance(v,dict) else v
        owners=by_subject.get(kind+'/'+ident,[])
        display=LOCALES['ru_ru'][entries[owners[0]]['title']] if owners and kind in ('recipe','structure','feature') else name('entity' if kind=='entity' else 'block' if kind=='block' else 'item',ident,'ru_ru')
        coverage.append(dict(registry=registry,id=ident,kind=kind,display_name_ru=display,book_entry_id=';'.join(owners),stage={'entity':'2/3/4; separate defeated','item':'2 obtained, 4 crafted','block':'2 examined','recipe':'4 actual crafting result','structure':'2 associated discovery','feature':'2 associated marker'}[kind],verified='STATIC_REGISTRY_AUDIT; runtime details in TEST_RESULTS.md',entries=';'.join(owners),
                             trigger={'entity':'bounded observation/interact/death; technical types grouped','item':'pickup/craft/smelt/container close','block':'right click/obtained item','recipe':'real result craft/smelt','structure':'loaded local structure bounding box / existing advancement','feature':'associated marker/plant only; no location scan'}[kind],
                             status='REPRESENTED' if owners else 'MISSING'))
for ident,e in entries.items():
    e['subjects']=sorted(set(e['subjects']))
    e['advancements']=sorted(a for a in set(e['advancements']) if (adv_dir/(a.split(':')[1]+'.json')).exists())
    (OUT/(ident.split(':')[1]+'.json')).write_text(json.dumps(e,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
for lang,locale in LOCALES.items():
    (RES/f'assets/slavicmyths/lang/{lang}.json').write_text(json.dumps(locale,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
with (AUDIT/'CONTENT_COVERAGE.csv').open('w',encoding='utf8',newline='') as f:
    writer=csv.DictWriter(f,fieldnames=['id','kind','display_name_ru','book_entry_id','trigger','stage','verified','registry','entries','status']);writer.writeheader();writer.writerows(coverage)
assert all(r['status']=='REPRESENTED' for r in coverage), [r for r in coverage if r['status']=='MISSING']
print(f'{len(entries)} entries; {len(coverage)} audited registry rows; all represented (not all runtime triggers verified).')
