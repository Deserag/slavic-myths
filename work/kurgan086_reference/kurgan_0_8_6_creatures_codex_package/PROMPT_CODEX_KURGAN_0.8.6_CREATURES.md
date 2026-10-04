# SLAVIC MYTHS — PROMPT FOR CODEX
## Version target: 0.8.6 — Kurgan Creatures / mini-bosses / boss / combat update
## Minecraft 1.16.5 / Forge 36.2.42 / Java 8 / mod id `slavicmyths`

---

# 0. САМОЕ ГЛАВНОЕ ПРАВИЛО

Это **не** задача на свободный геймдизайн.

Дизайн существ, визуал, роли, боевая философия, базовые характеристики, яйца спавна, звуки, особенности поведения и ограничения уже зафиксированы в этом документе и в папке `references/`.

Твоя роль как Codex:
1. быстро изучить только нужные части репозитория;
2. реализовать новых существ и связанную с ними логику **строго по ТЗ**;
3. не придумывать альтернативные механики, если они не описаны здесь;
4. не "улучшать" дизайн по своему вкусу;
5. обновить README;
6. собрать JAR без запуска Minecraft-клиента;
7. положить актуальный JAR в PolyMC `mods`, если путь уже известен в локальной среде проекта.

Если старый roadmap проекта говорил, что главный босс Великого кургана должен быть позже, то **для этой задачи действует новый план**: в версии `0.8.6` нужно реализовать обычных врагов курганов, 2 разных мини-босса и 1 главного босса.

---

# 1. ПРАВИЛА ЭКОНОМИИ ТОКЕНОВ И КОНТЕКСТА

Пользователь будет запускать тебя на более слабой модели, поэтому нельзя тратить много токенов на лишние исследования и рассуждения.

## 1.1 Не делай полный аудит всего репозитория
Читай только то, что нужно для этой задачи:
- регистрацию entity;
- регистрацию items / spawn eggs;
- регистрацию звуков;
- системы worldgen / kurgan data / disturbance из 0.8.5;
- saved data / capability / persistent player data / project-equivalent;
- AI/goal patterns существ, если в проекте уже есть подходящие примеры;
- loot tables;
- localization;
- README;
- build/output/deployment.

Не изучай подробно рыбалку, еду, bandit-арку, классы и другие несвязанные системы, если они не затрагивают текущую реализацию.

## 1.2 Не создавай параллельные системы
Если в проекте уже есть общий registry helper, общий base entity class, система сохранения данных, система debug-команд, структура README, то переиспользуй их.

## 1.3 Не пиши теоретическое эссе
Сначала кратко изучи нужные файлы, затем работай по ТЗ.

## 1.4 Не запускай Minecraft
Запрещено:
- `runClient`;
- всё, что открывает окно игры;
- автоматические ручные тесты с клиентом.

Разрешено:
- headless compile/build;
- проверка ресурсов без запуска клиента.

---

# 2. ОБЩИЙ СКОУП ВЕРСИИ 0.8.6

Нужно реализовать новую боевую и контентную надстройку над курганами.

## 2.1 Входит в задачу
- 3 обычных врага: `upyr`, `nav`, `kurgan_druzhinnik`;
- 2 мини-босса: `kurgan_voevoda`, `buried_volkhv`;
- 1 главный босс: `unresting_prince`;
- уникальные модели/текстуры под Minecraft-стиль;
- AI и goals;
- активное поведение без пассивных простоев;
- звуки (через custom sound events или чётко заданные fallback-решения);
- яйца спавна для всех 6 существ;
- спавн существ внутри курганов через controlled encounter logic;
- связь с Disturbance / `Нарушение покоя`;
- мини-боссы и главный босс как полноценные fight encounters;
- дропы и loot integration;
- README;
- debug / testing instructions;
- headless build;
- копирование JAR в PolyMC `mods` при известном пути.

## 2.2 Не входит в задачу
- новые курганы, новые этажи и переделка worldgen 0.8.5 с нуля;
- новая броня/новый оружейный tier ради этой арки;
- новые классы персонажа;
- общая overhaul-система магии;
- новые unrelated structures;
- бесконечные wave-spawners;
- traps и головоломки;
- новый UI;
- health bar HUD для боссов, если в проекте для этого нет готового паттерна;
- полная переработка Соловья-разбойника;
- запуск Minecraft-клиента.

---

# 3. ПАПКА REFERENCES — ОБЯЗАТЕЛЬНА

Файлы в `references/` являются не "вдохновением", а ограничением дизайна.

## 3.1 Основные листы
- `BESTIARY_FULL.png`
- `UPYR_FULL.png`
- `NAV_FULL.png`
- `DRUZHINNIK_FULL.png`
- `VOEVODA_FULL.png`
- `VOLKHV_FULL.png`
- `PRINCE_FULL.png`

## 3.2 Нарезанные референсы
По каждому существу даны отдельно: общий hero-view, вид спереди, вид сбоку, вид сзади и отдельные крупные детали (голова, оружие, щит, амулеты и т.п.). Используй их как прямые визуальные constraints.

## 3.3 Приоритет
Если есть неоднозначность: 1) точный текст этого документа; 2) isolated reference image по конкретному существу; 3) full concept sheet; 4) существующий стиль Slavic Myths. Не придумывай пятую опцию.

---

# 4. ОБЩАЯ БОЕВАЯ ФИЛОСОФИЯ КУРГАННЫХ ВРАГОВ

Пользователь заметил, что многие враги в проекте ощущаются слишком пассивно. Это надо исправить системно.

## 4.1 Обязательные правила активности
1. После агра враг не должен часто "зависать" без действий.
2. Если игрок кратко теряет direct line of sight, враг не должен мгновенно забывать цель.
3. У каждого врага должен быть понятный цикл: преследование / позиционирование, телеграф, действие, короткое recovery, повторное давление.
4. Между атаками не должно быть долгих бессмысленных пауз.
5. Боссы и мини-боссы должны менять действия, а не спамить одну атаку.
6. Если игрок долго kite-ит цель, у сильных врагов должна быть механика сокращения дистанции.
7. Активность достигается не только уроном, но и рывками, блоком, ограниченным суммоном, сменой стойки, AoE и repositioning.
8. Никакой "статуи с большим HP" быть не должно.

## 4.2 Общие технические требования к AI
Нужно реализовать или адаптировать общий паттерн поведения: память последней позиции игрока на несколько секунд, повторный path recalculation, cooldown abilities, state machine / goal selection без конфликтов между goals и честные окна для ответа игрока после тяжёлых способностей.

## 4.3 Активность > губка здоровья
Просто увеличить HP недостаточно. Нужно сделать так, чтобы даже игрок в незерите был вынужден двигаться, а shield/armor не отменяли механику боя.

---

# 5. ОБЩАЯ ЛОГИКА СПАВНА В КУРГАНАХ

Используй encounter-подход, а не бесконечный natural spam. Каждая комната должна опираться на room-type, tier кургана, Disturbance, trigger и флаг already triggered/cleared. Одна и та же комната не должна бесконечно генерировать мобов после перезахода игрока.

Рекомендуемая логика Disturbance:
- `0–24`: редкие обычные encounter;
- `25–49`: больше обычных encounter, появляются Нави;
- `50–74`: появляются Дружинники, более опасные комнаты;
- `75–100`: усиленные encounter, мини-боссы, открытие доступа к главному боссу Великого кургана.

Tier-тенденции:
- Малый курган: Upyr / Nav, редко Druzhinnik.
- Воинский / Родовой: Upyr, Nav, Druzhinnik, иногда Voevoda.
- Великий: все обычные враги, оба мини-босса, главный босс.

В 0.8.6 доступ к боссу должен открываться просто и понятно: когда Disturbance данного Великого кургана достигает `>= 75`, и игрок подходит к зоне запечатанного входа/предбоссовой области, seal opens through scripted event, после чего запускается бой с боссом. Не выдумывай сложную систему ключей.

---

# 6. ОБЩЕЕ ПРАВИЛО ДЛЯ МОДЕЛЕЙ И ТЕКСТУР

Все существа должны **реально соответствовать Minecraft-визуалу**, а не быть смесью реализма и Minecraft. Нужный стиль: blocky / voxel-like proportions, чёткие читаемые силуэты, Minecraft-compatible pixel art.

Запрещено:
- `Zombie model + новая texture`;
- `Skeleton model + просто другой цвет`;
- realistic anatomy/high-detail sculpt;
- слишком много микродеталей;
- 16×16 final textures.

Минимум качества: все финальные текстуры минимум 32×32, желательно 64×64; отдельные элементы модели (щит, топор, посох, плащ и т.п.) должны визуально читаться; silhouette каждого существа должен отличаться.

У каждого существа должны быть: модель, текстура, entity registration, renderer, spawn egg item, localization RU/EN, sounds, loot table, integration в encounter logic и README documentation.

---

# 7. РЕГИСТРОВЫЕ ИМЕНА И ЯЙЦА СПАВНА

Entity IDs:
- `slavicmyths:upyr`
- `slavicmyths:nav`
- `slavicmyths:kurgan_druzhinnik`
- `slavicmyths:kurgan_voevoda`
- `slavicmyths:buried_volkhv`
- `slavicmyths:unresting_prince`

Spawn egg IDs:
- `slavicmyths:upyr_spawn_egg`
- `slavicmyths:nav_spawn_egg`
- `slavicmyths:kurgan_druzhinnik_spawn_egg`
- `slavicmyths:kurgan_voevoda_spawn_egg`
- `slavicmyths:buried_volkhv_spawn_egg`
- `slavicmyths:unresting_prince_spawn_egg`

Цвета яиц спавна:
- Upyr: primary `#E4D8CE`, secondary `#8D1717`
- Nav: primary `#CDE7FF`, secondary `#5FA7FF`
- Kurgan Druzhinnik: primary `#7B7A80`, secondary `#B22E2E`
- Kurgan Voevoda: primary `#8A2327`, secondary `#D6A451`
- Buried Volkhv: primary `#2F2D38`, secondary `#B39A63`
- Unresting Prince: primary `#9F1E28`, secondary `#D9B14E`

---

# 8. ЗВУКИ — ОБЯЗАТЕЛЬНО

Нужно зарегистрировать осмысленные sound events и привязать их к ambient, hurt, death, step / movement where relevant, basic attack, special ability и boss/miniboss phase cues when relevant. Если оригинальные `.ogg` пока не создаются, всё равно зафиксируй структуру sound handling в коде, используй чётко описанные vanilla fallback sound events и документируй в README, что используются fallback-источники звука.

---

# 9. UPYR — ПОЛНАЯ СПЕЦИФИКАЦИЯ

Registry ID: `upyr`  
Display name RU: `Упырь`  
Display name EN: `Upyr`

References: `UPYR_FULL.png`, `UPYR_HERO_HERO.png`, `UPYR_FRONT_FRONT.png`, `UPYR_SIDE_SIDE.png`, `UPYR_BACK_BACK.png`, `UPYR_DETAIL_1_HEAD.png`, `UPYR_DETAIL_2_CLAWS.png`, `UPYR_DETAIL_3_BURIAL_SHIRT.png`

Роль: обычный агрессивный ближний враг; быстро давит игрока в узких коридорах и на короткой дистанции.

Внешний вид: рост около `1.9 блока`; сгорбленная, звериная осанка; длинные руки ниже обычных пропорций; большие когти; почти человеческая голова, но с более широкой челюстью; рот с неровными зубами; светящиеся красные глаза в духе референса; бледная грязно-белая кожа; рваная погребальная рубаха с красным орнаментом; босые ноги; никаких плащей, крыльев и vampire-lord образа.

Модель: нельзя использовать vanilla zombie body; нужны изменённые пропорции рук, сгорбленная спина, наклон головы вперёд и собственная анимация атаки когтями.

Базовые характеристики:
- HP: `40`
- базовый урон: `6`
- скорость: `0.31`
- follow range: `28`
- knockback resistance: `0.1`
- armor: `2`

Способности:
- Обычная серия когтями — интервал `~16 тиков`, 1–2 быстрых удара.
- Рывок охотника — cooldown `80–100 тиков`, короткий телеграф, быстрый рывок на средней дистанции.
- Укус — cooldown `120–160 тиков`; наносит доп. урон, лечит Upyr на `4 HP`, снижает эффективность лечения/регенерации игрока примерно на `5 секунд`.

Звуки Upyr: низкий хриплый мертвецкий рык. Fallback: zombie/husk low pitch, plus отдельные cues на leap/bite.

Дроп: common existing remains, uncommon `upyr_fang`, rare `grave_cloth_scrap`.

---

# 10. NAV — ПОЛНАЯ СПЕЦИФИКАЦИЯ

Registry ID: `nav`  
RU: `Навь`  
EN: `Nav`

References: `NAV_FULL.png`, `NAV_HERO_HERO.png`, `NAV_FRONT_FRONT.png`, `NAV_SIDE_SIDE.png`, `NAV_BACK_BACK.png`, `NAV_DETAIL_1_HEAD.png`, `NAV_DETAIL_2_BURIAL_RIBBONS.png`, `NAV_DETAIL_3_PARTIAL_TRANSPARENCY.png`

Роль: мобильный призрачный враг. Опасность — repositioning и замедляющее касание.

Внешний вид: рост около `2.1 блока`; вытянутый силуэт; нижняя часть тела распадается в полупрозрачные погребальные ленты; холодная бело-голубая палитра; маскообразное лицо; светящиеся холодные глаза; частичная прозрачность обязательна.

Модель: отдельная ghost-like модель. Не использовать обычную humanoid-модель без изменений. Hitbox должен оставаться честным.

Базовые характеристики:
- HP: `32`
- базовый урон: `5`
- скорость: `0.30`
- follow range: `30`
- armor: `0`
- knockback resistance: `0.2`

Способности:
- Холодное прикосновение — базовая атака со Slowness примерно на `2 секунды`.
- Уход в Навь — cooldown `70–110 тиков`, частичное исчезновение и быстрый repositioning в сторону/назад; не телепортируется между комнатами и не проходит сквозь защищённую кладку.
- Боковой заход — AI предпочитает смещаться, а не идти строго по прямой.

Звуки Nav: холодный шёпот, spectral hiss, soft whoosh на shift.

Дроп: `nav_essence`, rare `torn_burial_ribbon`.

---

# 11. KURGAN DRUZHINNIK — ПОЛНАЯ СПЕЦИФИКАЦИЯ

Registry ID: `kurgan_druzhinnik`  
RU: `Курганный дружинник`  
EN: `Kurgan Druzhinnik`

References: `DRUZHINNIK_FULL.png`, `DRUZHINNIK_HERO_HERO.png`, `DRUZHINNIK_FRONT_FRONT.png`, `DRUZHINNIK_SIDE_SIDE.png`, `DRUZHINNIK_BACK_BACK.png`, `DRUZHINNIK_DETAIL_1_HELMET_AND_FACE.png`, `DRUZHINNIK_DETAIL_2_SWORD.png`, `DRUZHINNIK_DETAIL_3_SHIELD.png`

Роль: тяжёлый обычный противник. Щитовой боец, который ломает схему "стоять лицом к мобу и кликать ЛКМ".

Внешний вид: рост около `2.0 блока`; кольчуга; конический шлем; круглый щит с красным полем; меч; мертвецкое лицо/череп под шлемом; красные акценты на ткани; стилистика раннего дружинника.

Модель: отдельная модель или значимо переработанная humanoid-модель с собственным щитом, шлемом, читаемой кольчугой и анимацией shield bash / defensive pose. Не допускается просто skeleton with armor.

Базовые характеристики:
- HP: `60`
- урон: `7`
- скорость: `0.25`
- armor: `8`
- knockback resistance: `0.35`
- follow range: `26`

Способности:
- Обычная атака мечом — интервал `~18 тиков`.
- Активный блок щитом — около `60% reduction` фронтального урона, но не абсолютный иммунитет.
- Удар щитом — cooldown `90–120 тиков`, dash, небольшой урон и сильный knockback.
- Ответный выпад — после 2–3 блокированных фронтальных ударов.

Звуки: тяжёлый мертвец-воин, металл, дерево щита, bone/armor collapse.

Дроп: `shield_boss_fragment`, `druzhinnik_blade_fragment`, плюс обычный металлолом/обломки.

---

# 12. KURGAN VOEVODA — МИНИ-БОСС №1

Registry ID: `kurgan_voevoda`  
RU: `Воевода кургана`  
EN: `Kurgan Voevoda`

References: `VOEVODA_FULL.png`, `VOEVODA_HERO_HERO.png`, `VOEVODA_FRONT_FRONT.png`, `VOEVODA_SIDE_SIDE.png`, `VOEVODA_BACK_BACK.png`, `VOEVODA_DETAIL_1_HEAD_AND_CLOAK.png`, `VOEVODA_DETAIL_2_AXE.png`, `VOEVODA_DETAIL_3_SHIELD.png`

Роль: первый мини-босс. Тяжёлый агрессивный щито-топорный боец.

Внешний вид: рост `~2.3 блока`; `150 HP`; массивная фигура; крупный круглый щит; боевой топор; богатая, но ветхая красная накидка/плащ; усиленный шлем; более статусный вид, чем у Дружинника.

Базовые характеристики:
- HP: `150`
- урон базовой атаки: `9`
- скорость: `0.27`
- armor: `12`
- knockback resistance: `0.5`
- follow range: `34`

Поведение: Воевода всегда ощущается давящим и агрессивным; не должен по 3 секунды стоять после удара или слишком долго сидеть в блоке.

Боевой набор:
- Базовый топорный удар — `16 тиков`, после смены стойки `14 тиков`.
- Таран щитом — cooldown `90 тиков`; быстрый рывок вперёд; сильный knockback; при промахе короткое окно уязвимости `~20 тиков`.
- Круговой рубящий удар — cooldown `110–140 тиков`; читаемый замах; широкая атака по дуге.
- Усиленный фронтальный блок — значительное снижение фронтального урона, но не абсолютный блок.

Смена стойки / псевдо-фаза: на `<= 50% HP` чаще атакует, двигается чуть быстрее (`+0.02`) и реже держит щит.

Спавн и encounter: только в dedicated chamber; 1 экземпляр; без бесконечных волн.

Звуки: deeper armored undead, heavy rushing cue, wide whoosh, intimidating roar на смену стойки.

Дроп: основной unique drop — `voevoda_insignia`.

---

# 13. BURIED VOLKHV — МИНИ-БОСС №2

Registry ID: `buried_volkhv`  
RU: `Погребённый волхв`  
EN: `Buried Volkhv`

References: `VOLKHV_FULL.png`, `VOLKHV_HERO_HERO.png`, `VOLKHV_FRONT_FRONT.png`, `VOLKHV_SIDE_SIDE.png`, `VOLKHV_BACK_BACK.png`, `VOLKHV_DETAIL_1_HEAD.png`, `VOLKHV_DETAIL_2_STAFF.png`, `VOLKHV_DETAIL_3_AMULETS.png`

Роль: второй мини-босс. Мистический контроллер пространства.

Внешний вид: рост `~2.1 блока`; `150 HP`; длинная тёмная ритуальная одежда; капюшон; подвески/обереги; длинный посох; сухое лицо/черепообразное лицо; не должен выглядеть как vanilla Witch.

Базовые характеристики:
- HP: `150`
- базовый урон прямой атаки: `6`
- скорость: `0.24`
- armor: `6`
- knockback resistance: `0.2`
- follow range: `36`

Способности:
- Некротический болт / basic ranged attack — интервал `20–24 тиков`.
- Навьи тени — cooldown `120 тиков`; создаёт `3` ложных силуэта, из которых 2 ложные.
- Печать погребения — cooldown `100–130 тиков`; зона на полу, задержка `~1.4 секунды`, затем урон + сильное краткое замедление на `2–3 сек`.
- Ограниченный призыв — один раз за бой на `<= 50% HP`; призывает `2–3` обычных моба (приоритетно `Nav` и/или `Upyr`).

Encounter: 1 экземпляр в ритуальной камере/особой side-chamber.

Звуки: old necrotic caster, whisper burst, ritual hum, summon chant.

Дроп: основной unique drop — `volkhv_amulet`.

---

# 14. UNRESTING PRINCE — ГЛАВНЫЙ БОСС

Registry ID: `unresting_prince`  
RU: `Неупокоенный князь`  
EN: `Unresting Prince`

References: `PRINCE_FULL.png`, `PRINCE_HERO_HERO.png`, `PRINCE_FRONT_FRONT.png`, `PRINCE_SIDE_SIDE.png`, `PRINCE_BACK_BACK.png`, `PRINCE_DETAIL_1_CROWN_AND_FACE.png`, `PRINCE_DETAIL_2_SWORD.png`, `PRINCE_DETAIL_3_SHIELD.png`

Роль: главный босс курганной арки этой версии.

Фиксированные параметры: рост `~2.5–2.7 блока`, HP `350`, активнее Соловья, с уникальными механиками.

Внешний вид: корона/княжеский обруч; длинный красный плащ; меч; большой круглый щит; кольчуга/доспех; более статусный вид, чем у Воеводы; мёртвое лицо/черепное лицо; силуэт должен сразу читаться как главный босс.

Базовые характеристики:
- HP: `350`
- базовый урон: `11`
- armor: `16`
- speed: `0.28`
- knockback resistance: `0.7`
- follow range: `40`

Арена и начало боя: босс появляется в центральной гробнице Великого кургана после scripted opening seal event. Используй prepared final hall from 0.8.5. Не допускай постоянного респавна босса в очищенной комнате.

Трёхфазный бой:
- Фаза I `Погребённый правитель` (100–65%) — меч и щит, наступление, блок, комбо, один ограниченный summon из ниш. Способности: `Княжеский натиск`, `Приказ мёртвым`.
- Фаза II `Нарушенный покой` (65–30%) — быстрее, агрессивнее, меньше опора на щит. Способности: `Разрыв строя`, `Удар княжеского меча` (тяжёлый вертикальный удар с хорошим телеграфом и окном уязвимости после промаха).
- Фаза III `Упырь-князь` (30–0%) — более звериный стиль движений. Способности: `Кровавый захват` (редкий grab, лечит босса на `12 HP`, cooldown `180+ тиков`), `Последний натиск` (ещё более частые действия, но heavy attacks остаются телеграфированными).

Важные ограничения: босс не должен ваншотать адекватно экипированного игрока, но и игрок в незерите не должен просто tank-face-trade весь бой.

Звуки: deep armored undead, ritual resonance на phase transition, powerful block cue, command cue на summon, sharp predatory lunge на grab.

Дроп: обязательный уникальный drop — `princely_seal`.

Связь с Curse: победа над `unresting_prince` должна очищать соответствующее persistent curse state у игрока, либо через явный hook/handler, если система curse уже реализована.

---

# 15. ЗВУКОВАЯ ТАБЛИЦА ПО ENTITY IDs

Нужно завести sound handling для semantic events.
- Upyr: ambient, hurt, death, attack, leap, bite
- Nav: ambient, hurt, death, attack, shift, spectral_appear
- Kurgan Druzhinnik: ambient, hurt, death, sword_attack, shield_block, shield_bash
- Kurgan Voevoda: ambient, hurt, death, axe_attack, shield_charge, shield_impact, phase_shift
- Buried Volkhv: ambient, hurt, death, cast_basic, cast_clones, cast_ground_seal, summon
- Unresting Prince: ambient, hurt, death, combo, summon, phase_shift, heavy_strike, grab

---

# 16. АНИМАЦИИ — ЧТО НУЖНО НЕ ЗАБЫТЬ

Нужен минимум читаемых состояний, а не всё через generic hand swing.
- Upyr: idle hunch breathing, walk/run, claw attack, leap telegraph, bite
- Nav: float idle, drift movement, touch attack, shift/fade
- Druzhinnik: idle guard pose, walk, sword strike, shield block pose, shield bash
- Voevoda: heavy walk, axe strike, shield charge, sweep, stance-change reaction
- Volkhv: ritual idle, reposition, staff cast basic, clone-cast, ground-seal cast, summon gesture
- Prince: regal heavy idle, walk/run, combo strikes, heavy overhead slash, summon command, phase transition, grab

---

# 17. ЛУТ И НОВЫЕ ПРЕДМЕТЫ — МИНИМАЛЬНО, НО ОСМЫСЛЕННО

Допускается следующий минимальный набор item IDs:
- `upyr_fang`
- `grave_cloth_scrap`
- `nav_essence`
- `torn_burial_ribbon`
- `shield_boss_fragment`
- `druzhinnik_blade_fragment`
- `voevoda_insignia`
- `volkhv_amulet`
- `princely_seal`

Текстуры предметов — минимум 32×32. Не делай длинные crafting trees и не раздувай систему.

---

# 18. SPAWN / ENCOUNTER РАСПРЕДЕЛЕНИЕ

Small Kurgan: Upyr common, Nav uncommon, Druzhinnik rare.
Warrior / Clan Kurgan: Upyr common, Nav common, Druzhinnik common.
Great Kurgan: Upyr common, Nav common, Druzhinnik common, plus denser dangerous combinations.

Mini-bosses:
- Voevoda — only dedicated side chamber / warrior burial chamber.
- Volkhv — only dedicated ritual chamber.

Boss:
- Unresting Prince — only Great Kurgan final tomb; exactly one boss encounter per generated Great Kurgan instance unless deliberate reset/debug is used.

---

# 19. PLAYER DATA / CURSE / BOSS CLEAR

Если в проекте уже есть persistent curse system из 0.8.5, интегрируй бой босса нормально. Победа над `unresting_prince` должна снимать связанное persistent curse состояние; debug clear command должен продолжать работать; существующую 0.8.5-логику ломать нельзя.

---

# 20. DEBUG И КОМАНДЫ

README должен содержать точные команды для тестов.

Минимум должны работать и быть задокументированы summon-команды:
- `/summon slavicmyths:upyr`
- `/summon slavicmyths:nav`
- `/summon slavicmyths:kurgan_druzhinnik`
- `/summon slavicmyths:kurgan_voevoda`
- `/summon slavicmyths:buried_volkhv`
- `/summon slavicmyths:unresting_prince`

Если уже есть custom debug root `/slavicmyths`, можно добавить clean helper-команды для summon/encounter/curse, но без разрастания scope.

---

# 21. ЛОКАЛИЗАЦИЯ

Добавь RU/EN названия для entities, spawn eggs и drops.

Entities:
- Упырь / Upyr
- Навь / Nav
- Курганный дружинник / Kurgan Druzhinnik
- Воевода кургана / Kurgan Voevoda
- Погребённый волхв / Buried Volkhv
- Неупокоенный князь / Unresting Prince

Spawn eggs:
- Яйцо призыва упыря / Upyr Spawn Egg
- Яйцо призыва нави / Nav Spawn Egg
- Яйцо призыва курганного дружинника / Kurgan Druzhinnik Spawn Egg
- Яйцо призыва воеводы кургана / Kurgan Voevoda Spawn Egg
- Яйцо призыва погребённого волхва / Buried Volkhv Spawn Egg
- Яйцо призыва неупокоенного князя / Unresting Prince Spawn Egg

Drops:
- Клык упыря / Upyr Fang
- Лоскут погребальной ткани / Grave Cloth Scrap
- Сущность нави / Nav Essence
- Погребальная лента / Torn Burial Ribbon
- Обломок умбона щита / Shield Boss Fragment
- Обломок клинка дружинника / Druzhinnik Blade Fragment
- Знак воеводы / Voevoda Insignia
- Амулет волхва / Volkhv Amulet
- Княжеская печать / Princely Seal

---

# 22. README — ОБЯЗАТЕЛЬНО ОБНОВИТЬ

После реализации обнови README. Нужен отдельный раздел по 0.8.6.

Что описать:
- каких новых существ добавили;
- роли обычных мобов;
- два мини-босса;
- главного босса;
- spawn egg IDs / команды;
- где кто появляется;
- как работает агрессия / encounter logic;
- как запускается бой с главным боссом;
- как связан босс с curse removal;
- какие звуковые fallback-решения использованы, если нет собственных OGG.

Желательна таблица по существам: ID, русское имя, роль, HP, основная механика, где встречается.

README обязан перечислять exact working commands и краткий manual test.

---

# 23. BUILD / DELIVERABLE

После завершения:
1. не запускать Minecraft;
2. сделать headless build;
3. исправить compile/resource errors;
4. собрать JAR;
5. положить/обновить JAR в PolyMC `mods`, если путь известен;
6. не оставлять несколько конфликтующих JAR в test mods.

Если путь до PolyMC неизвестен — не выдумывай. Тогда просто сообщи итоговый путь к JAR.

---

# 24. ПОРЯДОК РАБОТЫ — СТРОГО ДЛЯ ЭКОНОМИИ ТОКЕНОВ

1. быстро найти текущие registry/entity/world-data patterns;
2. переиспользовать kurgan data/disturbance framework;
3. зарегистрировать 6 entity;
4. зарегистрировать 6 spawn eggs;
5. подготовить/подключить модели, текстуры, рендер;
6. добавить sounds / fallbacks;
7. реализовать AI обычных врагов;
8. реализовать AI мини-боссов;
9. реализовать трёхфазного босса;
10. связать encounter logic с курганами;
11. добавить drops / loot tables;
12. обновить localization;
13. обновить README;
14. собрать JAR;
15. обновить PolyMC mods если путь известен.

---

# 25. ЧЕГО НЕЛЬЗЯ ДЕЛАТЬ

Не добавляй:
- ещё 5 новых мобов;
- новый магический интерфейс;
- новую броню ради этой задачи;
- новую большую систему навыков;
- ловушки;
- секретные ключи;
- RPG-дерево босса;
- modern/generic fantasy necromancer aesthetics;
- 16×16 final assets;
- полуреалистичный стиль моделей;
- запуск клиента;
- длинный redesign discussion.

---

# 26. DEFINITION OF DONE

Задача считается выполненной, только если:
- реализованы 3 обычных врага;
- реализованы 2 мини-босса;
- реализован 1 главный босс;
- у всех есть регистрация, модели/текстуры, spawn eggs, RU/EN локализация;
- существа выглядят Minecraft-compatible и это не vanilla reskin;
- текстуры >= 32×32;
- ordinary mobs активны;
- Upyr имеет leap + bite;
- Nav имеет shift/reposition;
- Druzhinnik имеет shield logic;
- Voevoda имеет shield charge + sweep + aggressive second stance;
- Volkhv имеет clones + ground seal + limited summon;
- Prince имеет 3 фазы, summon, heavy strike и grab/lifesteal;
- мини-боссы имеют 150 HP;
- главный босс имеет 350 HP;
- мобы встроены в encounter logic;
- нет бесконечных бесконтрольных волн;
- главный босс сидит в центральной гробнице Великого кургана;
- у всех существ привязаны осмысленные sounds / fallbacks;
- spawn eggs реализованы;
- README обновлён;
- build выполнен без запуска клиента;
- JAR собран;
- PolyMC mods обновлён при известном пути.

---

# 27. ФОРМАТ ИТОГОВОГО ОТВЕТА CODEX

Не пиши длинный текст. Верни только:
- что реализовано;
- основные изменённые файлы;
- команды;
- build и путь к JAR;
- PolyMC status;
- реально отложенные вещи, если они есть.
