# SLAVIC MYTHS — STRICT CODEX IMPLEMENTATION SPECIFICATION
## Version target: 0.9.2 — «Боссы мира»
## Minecraft 1.16.5 / Forge 36.2.42 / Java 8 / mod id `slavicmyths`
## Status: FINAL IMPLEMENTATION SPECIFICATION

---

# 0. ГЛАВНОЕ ПРАВИЛО

Это НЕ задача на свободный геймдизайн.

В этом документе уже зафиксированы:
- два босса;
- их Minecraft-дизайн;
- пропорции;
- мелкие визуальные детали;
- характеристики;
- фазы;
- атаки;
- тайминги;
- телеграфы;
- recovery;
- world-spawn;
- призыв;
- дроп;
- предметы;
- boss bars;
- spawn eggs;
- звуки;
- анимации;
- anti-cheese;
- интеграция с Hunt System 0.9.0–0.9.1.

Codex должен реализовать именно это.

Codex НЕ должен:
- придумывать третьего босса;
- делать новую большую структуру;
- добавлять Бабу-ягу раньше 0.9.3;
- менять Лихо на другого персонажа;
- менять Тугарина на другого персонажа;
- превращать Тугарина в дракона;
- превращать Лихо в Enderman-reskin;
- переписывать Hunt System с нуля;
- упрощать модели из-за того, что «деталей много»;
- делать обычный случайный mob-spawn настолько редким, что босса практически невозможно встретить;
- запускать Minecraft-клиент.

---

# 1. ЖЁСТКОЕ ОГРАНИЧЕНИЕ РАСХОДА ТОКЕНОВ

Задача предназначена для модели ниже максимального уровня.

Поэтому:

1. НЕ делай полный аудит репозитория.
2. НЕ перечитывай unrelated systems.
3. НЕ пиши длинный предварительный архитектурный документ.
4. Найди только:
   - entity registry;
   - boss entity patterns;
   - Hunt System 0.9.0–0.9.1;
   - Hunt Target tags/helpers;
   - saved encounter data;
   - item registry;
   - accessory/equipment system;
   - loot tables;
   - sounds;
   - boss bars;
   - world-spawn/worldgen helper;
   - commands;
   - JEI;
   - advancements;
   - README;
   - build/deploy.
5. Используй существующие системы.
6. Не создавай `BossSystemV2`, `HuntManager2` и т.п.
7. Не запускать `runClient`.
8. Только headless build/compile/resource validation.

---

# 2. СКОУП 0.9.2

Добавить ровно 2 полноценных босса:

1. `likho_one_eyed` — Лихо Одноглазое.
2. `tugarin_zmey` — Тугарин Змей.

Добавить:
- world-boss encounter placement;
- controllable summon items;
- boss bars;
- boss spawn eggs;
- loot;
- новые trophies/rewards;
- 2 новых функциональных аксессуара/предмета;
- несколько future-use trophies под 0.9.3 Бабу-ягу;
- Hunt/Hunter Charm integration;
- advancements;
- JEI;
- README;
- debug/summon.

Не добавлять:
- Бабу-ягу;
- Избушку;
- Метлу;
- Ступу;
- Живую/Мёртвую воду;
- новый dungeon;
- новый biome;
- отдельную dimension.

---

# 3. ВИЗУАЛЬНЫЕ REFERENCES — ОБЯЗАТЕЛЬНЫ

Папка `references/` является частью ТЗ.

Использовать:
- `REF_00_BOSSES_FULL.png`
- `REF_LIKHO_FULL.png`
- `REF_LIKHO_HERO.png`
- `REF_LIKHO_VIEWS.png`
- `REF_LIKHO_DETAILS.png`
- `REF_LIKHO_BIOME_PALETTE.png`
- `REF_TUGARIN_FULL.png`
- `REF_TUGARIN_HERO.png`
- `REF_TUGARIN_VIEWS.png`
- `REF_TUGARIN_SPIN_KICK.png`
- `REF_TUGARIN_DETAILS_PALETTE.png`

Приоритет:
1. точный текст этого документа;
2. isolated reference;
3. full board;
4. существующий стиль Slavic Myths.

Все мелкие детали с reference должны быть сохранены, если они перечислены ниже.

---

# 4. ОБЩИЙ ВИЗУАЛЬНЫЙ СТАНДАРТ БОССОВ

## 4.1 Texture
Оба босса:
- минимум 512×512 entity texture;
- 256×256 разрешается только как technical fallback, если текущий pipeline 1.16.5 проекта реально не может стабильно использовать 512;
- никакого 64×64 как финала для этих boss models.

## 4.2 Models
- blocky;
- Minecraft-compatible;
- читаемый силуэт;
- не photoreal;
- не vanilla reskin;
- отдельные model parts для деталей, влияющих на silhouette/animation.

## 4.3 Не упрощать
Нельзя:
- убрать у Лиха подвески/черепа/тряпки, потому что «необязательно»;
- убрать у Тугарина усы, наручи, пояс, змеиные знаки, сапоги;
- делать generic zombie arms;
- заменить длинные руки Лиха обычными humanoid hands;
- заменить фирменную вертушку Тугарина generic spin animation.

---


# 4A. КОНТРАКТ 100% ВИЗУАЛЬНОГО СООТВЕТСТВИЯ РЕФЕРЕНСУ

Для 0.9.2 референсы являются НЕ «настроением», а техническим чертежом внешнего вида.

Задача Codex — перенести в Minecraft ВСЕ видимые и перечисленные детали, а не только общий силуэт.

## 4A.1 Что считается недопустимым упрощением

Модель НЕ считается готовой, если:
- деталь присутствует на reference, но отсутствует на модели;
- объёмная деталь заменена одним нарисованным пятном texture, хотя она влияет на силуэт;
- несколько разных материалов слиты в один цвет;
- асимметричные элементы сделаны симметричными «для простоты»;
- мелкие ремни, шнуры, подвесы, бусины, кости, заклёпки, пластины или кисти удалены;
- крупный орнамент заменён абстрактной полосой;
- волосы Лиха заменены одним плоским кубом/cape;
- усы Тугарина заменены чёрной texture полосой на лице;
- змеиная пряжка Тугарина превращена в простой жёлтый круг;
- змеиные эмблемы на наручах исчезли;
- рваные края одежды Лиха сведены в ровный прямоугольный подол;
- детализация фронта есть, а спина/бок остаются пустыми.

## 4A.2 Geometry vs texture

Обязательно делать отдельной геометрией всё, что:
- выступает из silhouette;
- должно двигаться в animation;
- имеет толщину;
- видно в side/back view;
- меняет восприятие формы.

Texture может использоваться для:
- швов;
- мелких царапин;
- теней;
- маленьких трещин;
- небольших рисунков внутри уже существующей объёмной детали.

## 4A.3 Проверка со всех сторон

Перед завершением обе модели сравнить:
- front;
- 3/4 front;
- side;
- back;
- 3/4 back;
- attack pose;
- idle pose.

Если деталь есть только на hero-art и не читается в orthographic view, но перечислена ниже — всё равно реализовать.

## 4A.4 Допустимое техническое отклонение

Разрешено отклониться от reference ТОЛЬКО если:
- Forge/Minecraft 1.16.5 renderer технически не способен безопасно реализовать конкретный эффект;
- или деталь вызывает clipping, делающий анимацию хуже.

В этом случае:
1. сохранить визуальный смысл другим Minecraft-compatible способом;
2. НЕ удалять деталь молча;
3. в финальном ответе Codex указать конкретное отклонение.


---

# 5. КРИТИЧЕСКИЙ BUGFIX ДЛЯ ВСЕХ РЫВКОВ / CHARGE / DASH

Пользователь уже обнаружил баг у существ с рывками:
entity способна начать рывок БОКОМ или СПИНОЙ к игроку.

Это ЗАПРЕЩЕНО.

Правило относится:
- к новым боссам;
- к новым dash/charge в 0.9.2;
- к общему reusable dash helper, если его исправление автоматически улучшит старых существ без риска.

## 5.1 Перед стартом любого рывка
Ability state MUST проходить:

1. остановить обычную navigation;
2. получить target position / predicted point;
3. вычислить требуемый yaw;
4. плавно повернуть bodyYaw/yRot к цели;
5. head yaw также направить к цели;
6. в течение windup продолжать tracking цели, если способность ещё не locked;
7. разрешить старт dash только если:
   - absolute yaw difference <= `18°` для прямых charge;
   - <= `25°` для более широких leap attacks.

## 5.2 Если entity не успела повернуться
Max alignment time:
`20 ticks`.

Если за это время угол не достиг threshold:
- dash CANCEL;
- перейти в short recovery `10 ticks`;
- cooldown не обязательно тратить полностью; можно потратить 30–50%.

НЕ разрешать dash боком.

## 5.3 После старта
После launch:
- heading фиксируется на момент старта;
- entity НЕ может мгновенно развернуть dash на 90°;
- допускается лишь небольшая коррекция <= 5–8°/tick, если ability так задумана.

## 5.4 Animation orientation
Перед dash animation:
- torso/front модели обязательно визуально смотрит в направление движения.

## 5.5 Общий helper
Если в проекте уже есть reusable dash helper:
исправить его там, а не копировать orientation fix в 10 классах.

---

# 6. WORLD BOSS SPAWN — НЕ ИСПОЛЬЗОВАТЬ ОБЫЧНЫЙ РЕДКИЙ MOB RNG

Пользователь отдельно отметил:
обычный спавн многих локаций/существ сейчас слишком редкий.

Для Лиха и Тугарина НЕЛЬЗЯ полагаться только на обычный `MobSpawnInfo` с микроскопическим весом.

Использовать controlled world-boss placement / encounter anchors.

Boss encounter должен быть:
- редким;
- но реально существующим и находящимся.

## 6.1 Общий принцип
Мир делится на крупные boss-regions.

Рекомендуемый размер:
`32×32 chunks` per boss-region.

Для каждой region:
- deterministic random от world seed + region coords + boss salt;
- ищется eligible biome/position;
- encounter anchor сохраняется в world saved data;
- boss не создаётся повторно после kill, пока не выполнено выбранное respawn policy.

## 6.2 Respawn policy
Предпочтение:
world boss anchor после убийства уходит на cooldown `7 Minecraft days`.

После cooldown:
- boss может восстановиться в том же регионе/новом safe anchor.

Это позволяет повторно фармить трофеи, но не каждый час.

Если existing server/world data framework проще поддерживает one-time bosses:
допускается one-time natural boss + unlimited ritual summon.
Но README должен это точно описать.

---

# 7. ТУГАРИН — WORLD SPAWN ДОЛЖЕН БЫТЬ ГАРАНТИРОВАН В СТЕПНОЙ ЗОНЕ

Тугарин НЕ должен быть «теоретически возможен, но я 3 часа бегаю и не вижу».

## Eligible biome concept
В vanilla 1.16.5 под «степь» использовать:
- Plains;
- Sunflower Plains;
- Savanna;
- Savanna Plateau;
- другие реально открытые dry/plains biome IDs проекта;
- modded steppe biomes, если они уже есть и имеют подходящий biome tag.

НЕ использовать:
- Dark Forest;
- Swamp;
- dense Taiga;
- Ocean;
- Snowy Tundra как основной spawn.

## Guaranteed region placement
В каждом boss-region, где найден eligible open steppe/plains biome:
- попытаться создать 1 `Tugarin encounter anchor`;
- если первый candidate плохой, искать до 64 candidates;
- не отказываться из-за травы/цветов/snow layer;
- небольшие неровности рельефа допустимы.

То есть наличие подходящей степной/равнинной зоны в region должно практически гарантировать Tugarin anchor.

Не создавать boss в каждом чанке.
Один anchor на крупную region достаточно.

---

# 8. ЛИХО — ТОЛЬКО DARK FOREST, РЕЖЕ ТУГАРИНА

Лихо natural world encounter:
- только `Dark Forest` / соответствующий тёмный лес;
- если мод использует biome tags, создать/использовать semantic tag;
- НЕ обычный Forest;
- НЕ Taiga.

Пользователь хочет, чтобы Лихо было реже, потому что леса в мире частые.

Поэтому:
- только Dark Forest;
- region chance примерно `35%` среди boss-regions, где Dark Forest вообще найден.

То есть:
- biome restriction = 100% Dark Forest;
- encounter chance within eligible regions = ~35%.

Если Dark Forest area большая:
- максимум один active/natural Likho anchor в region.

Призывной предмет позволяет вызвать босса без ожидания natural RNG.

---

# 9. BOSS BARS

Оба:
- ServerBossInfo / existing Forge 1.16.5 boss bar pattern;
- visible только игрокам, реально находящимся в encounter radius;
- radius ориентир 48 blocks;
- скрывается после смерти/выхода.

Names:
`Лихо Одноглазое`
`Тугарин Змей`

Boss bar color:
- Likho: purple/dark if available;
- Tugarin: red.

Не добавлять новый HUD framework.

---


# 9A. ЛИХО — ПОЛНАЯ ВИЗУАЛЬНАЯ СПЕЦИФИКАЦИЯ ПО СЛОЯМ

ЭТОТ РАЗДЕЛ ИМЕЕТ ПРИОРИТЕТ НАД ЛЮБЫМИ БОЛЕЕ КОРОТКИМИ ОПИСАНИЯМИ ЛИХА НИЖЕ.

References:
- `REF_LIKHO_FULL.png`
- `REF_LIKHO_HERO.png`
- `REF_LIKHO_VIEWS.png`
- `REF_LIKHO_DETAILS.png`

## 9A.1 Общая форма

Лихо должно читаться как:
- очень высокая;
- почти болезненно тонкая;
- человекоподобная;
- но явно нечеловеческая фигура.

Рост:
`3.2–3.5 блока`.

Силуэт:
- узкий торс;
- очень длинные предплечья;
- руки почти до уровня колен;
- ноги ещё визуально длиннее обычного человека;
- огромная масса волос вокруг головы/плеч;
- рваные вертикальные полосы одежды продолжают силуэт вниз.

Лихо НЕ должно напоминать:
- Enderman;
- Witch;
- Zombie;
- обычного высокого человека.

## 9A.2 Голова — форма

Голова:
- небольшая относительно роста;
- вытянута вертикально;
- лицо узкое;
- нижняя челюсть не массивная;
- подбородок слабый/острый;
- большая часть головы окружена волосами.

Голова слегка наклонена вперёд в idle.

Шея:
- тонкая;
- частично скрыта волосами и тканью.

## 9A.3 Единственный центральный глаз

Это главный face-feature и его НЕЛЬЗЯ стилизовать абстрактно.

Расположение:
- строго в центральной верхне-средней части лица;
- НЕ сбоку;
- НЕ обычный левый/правый eye slot.

Форма:
- крупная миндалевидная/овальная глазница;
- ширина глаза примерно 35–40% ширины лица;
- верхнее и нижнее веко отдельными texture/model regions.

Цвета:
- sclera грязно-кремовая, не белоснежная;
- вокруг глаза темноватая воспалённая кожа;
- iris красно-оранжево-коричневая;
- внутреннее кольцо iris ярче;
- pupil маленький и почти чёрный;
- маленький светлый pixel-highlight допустим.

Глаз НЕ должен:
- постоянно светиться как лазер;
- быть красной точкой;
- иметь второй симметричный глаз.

Анимации:
- редкое моргание;
- медленное прикрытие века;
- при `Кривом взгляде` глаз открывается заметно шире;
- iris/внутренний цвет становится ярче, но не fullbright целиком.

## 9A.4 Область лица вокруг глаза

Кожа вокруг глаза:
- бледная;
- серо-бежевая;
- с более тёмными участками у края волос;
- не гладкая розовая кожа.

Рот:
- маленький;
- тёмная тонкая щель;
- не делать огромную пасть.

Нос:
- минимально выраженный, длинный и узкий;
- не квадратный villager nose.

Щёки:
- впалые за счёт shading.

## 9A.5 Волосы — ОБЯЗАТЕЛЬНАЯ сложная геометрия

Волосы — одна из главных частей модели.

Цвет:
- почти чёрный;
- местами charcoal;
- очень редкие грязно-серые/коричневые highlights.

Длина:
- большая часть прядей идёт ниже груди;
- самые длинные почти до колен;
- задние пряди формируют длинную неровную массу почти по всей спине.

Нужны отдельные группы:

### Front-left
- 3–5 длинных прядей;
- часть идёт перед плечом;
- одна тонкая прядь может проходить возле лица.

### Front-right
- 3–5 длинных прядей;
- не полная зеркальная копия левой стороны.

### Side strands
- 2–4 крупных пряди с каждой стороны;
- выступают за silhouette головы/плеч.

### Back mass
- 6–10 крупных вертикальных/ломаных прядей;
- разные длины;
- нижний край неровный.

### Top
- волосы лежат блоками слоями;
- crown shape неровная;
- не гладкий куб/шлем.

Минимальная цель:
`14–24` отдельных крупных hair model parts.

В animation:
- не физика;
- достаточно небольшого lag/покачивания;
- при резком движении часть прядей отклоняется на несколько градусов.

## 9A.6 Плечи и верхняя одежда

Плечи узкие.

На них:
- чёрно-коричневые тканевые лоскуты;
- часть волос поверх одежды;
- несколько верёвочных/костяных подвесок.

Одежда не должна создавать широкую мантию.

## 9A.7 Основной внутренний слой одежды

Под внешними лохмотьями:
- длинная тёмно-коричневая/почти чёрная туника;
- идёт от груди вниз;
- неровный низ;
- часть ткани видна между внешними полосами.

Material read:
грубая старая ткань.

Texture:
- вертикальные грязные полосы;
- потёртые края;
- редкие более светлые коричневые pixels.

## 9A.8 Внешний слой рваной одежды

Поверх:
- несколько отдельных длинных black/charcoal cloth strips.

Спереди:
минимум:
- 2 длинные тёмные полосы слева;
- 2–3 центральные;
- 2 справа;
- разная длина;
- одна-две заканчиваются около колена;
- другие доходят ниже.

Сзади:
- не меньше 5 отдельных hanging strips;
- их длина отличается;
- часть перекрыта волосами.

Подол:
- НЕ ровный;
- клиновидные и ступенчатые края;
- отдельные разрывы.

## 9A.9 Красная ткань

На reference есть тёмная красная/бордовая вертикальная полоса.

Обязательно:
- расположена преимущественно в нижней передней/боковой части;
- узкая;
- грязная;
- не ярко-красная;
- имеет рваный нижний край.

Palette:
- deep red/burgundy;
- затемнённые края.

Она должна отличаться от остальной одежды, но не становиться главным цветом.

## 9A.10 Пояс

Пояс НЕ просто линия texture.

Состав:
- грубая коричневая rope/leather wrapping вокруг талии;
- 2–3 слоя витков;
- асимметричный узел;
- несколько свободных концов;
- маленькие костяные/деревянные бусины на одном конце.

Пояс должен удерживать часть подвесок.

## 9A.11 Большие черепа на поясе

Reference показывает крупные skull charms.

Минимум:
- 2 хорошо читаемых маленьких черепа/черепоподобных костяных амулета;
- один ближе к левому/центральному hip;
- второй ближе к правой/центральной стороне;
- могут быть на разной высоте.

Каждый:
- blocky;
- отдельная геометрия;
- маленькие eye sockets texture;
- нижняя часть чуть уже верхней;
- подвешен на короткой верёвке.

НЕ заменять двумя белыми квадратами.

## 9A.12 Мелкие костяные подвесы

Кроме больших skull charms:
- 3–5 маленьких косточек/бусин/зубоподобных элементов;
- несколько на груди;
- несколько у пояса.

Использовать:
- bone beige;
- dirty shadow.

## 9A.13 Диагональная верёвка/ожерелье

Через верх торса/грудь должна проходить одна заметная тёмно-коричневая rope/cord line.

На ней:
- 2–4 маленьких bone beads;
- 1 более крупная подвеска допустима.

Это отдельная визуальная деталь, не сливаться с поясом.

## 9A.14 Руки — пропорции

Upper arms:
- тонкие;
- бледные;
- длинные.

Forearms:
- особенно длинные;
- очень узкие.

Кожа:
- pale beige-gray;
- более тёмные joints.

Локти:
- читаются угловатым joint geometry.

## 9A.15 Запястья

На reference возле запястий есть тёмные обмотки/браслеты.

На каждой руке:
- 1–2 узкие тёмные cloth/leather bands;
- не идеально одинаковые.

Цвет:
charcoal/brown-black.

## 9A.16 Кисти

Кисть:
- узкая;
- длинная;
- отдельная ладонь;
- пальцы значительно длиннее ладони.

На каждой руке визуально минимум:
- thumb group;
- 3–4 long finger groups.

Пальцы:
- костлявые;
- слегка согнуты;
- разные длины;
- кончики чуть темнее/грязнее.

Grab animation обязательно использует пальцы:
- раскрываются;
- затем сжимаются вокруг условной позиции игрока.

## 9A.17 Ноги

Бёдра очень тонкие.

Колени:
- угловатые;
- немного выступают.

Голени:
- длинные;
- бледные/костлявые;
- частично прикрыты тканью.

Одежда не должна полностью закрывать ноги — длинные тонкие конечности должны читаться.

## 9A.18 Ступни

Ступни:
- узкие;
- вытянутые;
- босые/костлявые;
- пальцы могут быть объединены в 2–3 blocky toe groups.

Не давать Лиху обувь.

## 9A.19 Спина

Back view НЕ должен быть пустым.

Обязательные элементы со спины:
- длинная масса отдельных волос;
- часть torn cloth strips видна ниже волос;
- rope belt wrapping;
- минимум одна боковая bone charm видна;
- красная ткань может слегка читаться сбоку;
- тонкие ноги.

## 9A.20 Asymmetry

Лихо должно быть намеренно асимметричным:
- волосы разной длины слева/справа;
- подвесы не зеркальные;
- красная ткань смещена;
- один skull charm может висеть ниже другого;
- оборванные края одежды разные.

НЕ mirror-copy модель полностью.

## 9A.21 Part naming recommendation

Чтобы Codex не потерял детали, модель желательно разбить минимум на:
- head
- eye_outer
- eye_inner
- jaw_face
- hair_top_*
- hair_front_left_*
- hair_front_right_*
- hair_side_*
- hair_back_*
- torso_inner
- cloth_front_*
- cloth_back_*
- cloth_red
- belt_main
- belt_knot
- belt_loose_*
- chest_cord
- skull_charm_left
- skull_charm_right
- bone_charm_*
- upper_arm_l/r
- forearm_l/r
- wrist_wrap_l/r
- hand_l/r
- finger groups
- thigh_l/r
- shin_l/r
- foot_l/r

Названия не обязаны совпадать буквально, но геометрические части должны существовать.


---

# 10. ЛИХО ОДНОГЛАЗОЕ — ID / STATS

Entity:
`slavicmyths:likho_one_eyed`

Spawn egg:
`slavicmyths:likho_one_eyed_spawn_egg`

Egg:
- primary `#1E1B1D`
- secondary `#C7A77A`

RU:
`Лихо Одноглазое`

EN:
`Likho the One-Eyed`

Stats:
- MAX_HEALTH = `320`
- ATTACK_DAMAGE base = `11`
- MOVEMENT_SPEED = `0.275`
- ARMOR = `10`
- ARMOR_TOUGHNESS = `2`
- KNOCKBACK_RESISTANCE = `0.65`
- FOLLOW_RANGE = `44`

XP:
`120–160`.

Persistent:
yes.

---

# 11. ЛИХО — МОДЕЛЬ: ПРОПОРЦИИ

Reference обязателен.

Visual height:
`3.2–3.5 blocks`.

Target hitbox:
- width `0.95`
- height `3.25`

Silhouette:
- очень высокий;
- очень тонкий;
- длинные руки;
- узкие плечи;
- вытянутые ноги;
- голова немного выдвинута вперёд;
- волосы/лохмотья делают silhouette более широким только сверху/сзади.

Не делать ширину Enderman один в один.

---

# 12. ЛИХО — ГОЛОВА И ЛИЦО

Самая важная деталь:
ОДИН центральный глаз.

## Eye
- расположен в центре вытянутого лица;
- крупный;
- примерно 30–40% ширины лица;
- sclera грязно-белая/бежевая;
- iris оранжево-красно-коричневый;
- маленький тёмный pupil;
- вокруг тёмная воспалённая кожа.

Нет второго глаза:
- НЕ повязка;
- НЕ шрам поверх второго;
- анатомически лицо одноглазое.

Eye должен иметь отдельный model/texture region, чтобы можно было:
- blink;
- widen;
- glow/brighten minimally during Gaze.

Не делать neon eye.

---

# 13. ЛИХО — ВОЛОСЫ

Reference:
очень длинные спутанные чёрные волосы.

Обязательные детали:
- волосы идут почти до колен/ниже;
- состоят из крупных blocky strands;
- часть закрывает плечи;
- часть висит перед грудью;
- часть идёт по спине;
- неровная длина;
- тёмно-чёрные с серо-коричневыми highlights.

Не делать один плоский cape.

Model parts:
ориентир 8–16 крупных hair strips/cubes.

---

# 14. ЛИХО — ОДЕЖДА

Рваные многослойные лохмотья.

Layers:
1. inner dark brown/black long tunic;
2. outer torn black cloth;
3. muted dark-red hanging strip on one side;
4. rope/leather belt;
5. dangling cords;
6. bone charms.

Torn hem:
- uneven;
- отдельные тканевые полосы;
- не идеально симметрично.

Colors:
- near black `#1D1B1D`
- charcoal `#2C292B`
- brown black `#382B27`
- dirty dark red `#642326`
- rope `#6B4B32`

---

# 15. ЛИХО — ПОДВЕСКИ И МЕЛКИЕ ДЕТАЛИ

ЭТИ ДЕТАЛИ НЕЛЬЗЯ УДАЛЯТЬ.

Reference показывает:
- маленькие черепа;
- костяные подвески;
- верёвки;
- бусины;
- амулеты;
- висящие у пояса элементы.

Минимум на финальной модели:
- 2 small skull/bone charm silhouettes;
- 3–5 rope/bone hanging elements;
- 1 longer cord across torso/waist;
- belt knot;
- dark-red cloth strip.

Не использовать человеческие детализированные realistic skull models:
blocky 4–6 cube stylization.

---

# 16. ЛИХО — РУКИ

Ключевая деталь.

Руки:
- визуально длиннее обычных человеческих;
- кисти почти достигают колен;
- пальцы очень длинные.

Каждая рука:
- upper arm;
- forearm;
- hand;
- 4 long finger model groups minimum.

Пальцы должны участвовать в:
- grab;
- wide sweep;
- idle twitch.

Не делать обычный Minecraft humanoid box-hand.

---

# 17. ЛИХО — НОГИ

- очень длинные;
- thin;
- slightly uneven posture;
- ноги/ступни костлявые;
- стопы узкие;
- ткань частично свисает вокруг бедра/голени.

Walk:
не обычный zombie gait.

Движения:
- длинный шаг;
- небольшая асимметрия;
- тело чуть покачивается.

---

# 18. ЛИХО — TEXTURE PALETTE

Skin:
- pale dirty `#C9BAA5`
- shadow `#81766A`
- deep shadow `#504741`
- gray-brown `#9A8D7D`

Eye:
- sclera `#D5C59B`
- iris `#B94B26`
- hot iris `#D7692F`
- pupil `#241713`

Hair:
- `#171719`
- `#232225`
- highlight `#353136`

Cloth:
- black `#1C1A1D`
- brown-black `#302622`
- red `#632126`

Bone:
- `#C2AF8B`
- shadow `#88775B`

Entity texture:
512×512 preferred/required unless pipeline forces 256.

---

# 19. ЛИХО — AI PERSONALITY

Лихо:
- не бегает постоянно;
- медленно давит;
- удерживает игрока в средней дистанции;
- резко использует длинные руки;
- во второй/третьей фазе ускоряется;
- не стоит idle по 3 секунды.

Target memory:
`260 ticks`.

If LOS lost:
идёт к last-known position.

Path recalc:
normal/controlled.

---

# 20. ЛИХО — PHASE 1 (100–65%)

Основные действия:
- long-arm sweep;
- grab;
- reposition;
- short chase.

## Base interval
между обычными melee действиями ориентир:
`22–26 ticks`.

---

# 21. ЛИХО — ABILITY: РАЗМАШИСТЫЙ УДАР

Cooldown:
`50 ticks`.

Range:
до `4.0 blocks`.

Arc:
примерно 110°.

Windup:
`14 ticks`.

Animation:
- плечо отходит назад;
- длинная рука растягивается по дуге;
- пальцы раскрыты.

Hit:
- damage `10`;
- knockback `0.55`.

Если игрок за спиной:
не попадать.

Line/arc calculation обязательна.

Recovery:
`12 ticks`.

---

# 22. ЛИХО — ABILITY: ЗАХВАТ ЛИХА

Signature.

Cooldown:
`180 ticks`.

Use range:
`3.5 blocks`.

## Telegraph
`18 ticks`.

- Лихо вытягивает руку;
- пальцы раскрываются;
- звук сухого вдоха/шёпота.

## Grab check
Только target в frontal cone примерно 60°.

If hit:
1. temporarily lock victim movement only as necessary;
2. поднять визуально/позиционно на ~1.2 block;
3. hold `20 ticks`;
4. throw in forward/side direction.

Damage:
- initial `4`;
- throw impact direct `6`;
- vanilla fall damage afterwards limited so attack не превращается в random one-shot.

Throw velocity:
достаточно сильный, 5–8 blocks horizontal typical.

Apply:
`Durnaya Dolya` / existing/new curse-like short debuff, section below.

If miss:
recovery `26 ticks`.

---

# 23. ЛИХО — ДЕБАФФ «ДУРНАЯ ДОЛЯ»

Registry:
`durnaya_dolya`

RU:
`Дурная доля`

EN:
`Ill Fate`

Negative effect.

Duration from boss abilities:
обычно `160 ticks` = 8 sec.

Effects:
- healing received multiplier `0.80`;
- knockback received multiplier `1.15`.

Не:
- ломает предметы;
- удаляет XP;
- рандомно наносит damage;
- снимает positive effects;
- взаимодействует с persistent Kurgan Curse.

Если framework не позволяет легко модифицировать healing:
можно реализовать event-hook только для этого effect.

Icon:
new 64/128 source acceptable, final UI compatible:
- twisted dark thread loop;
- tiny eye-like knot;
- dull brown-red.

---

# 24. ЛИХО — PHASE 2 (65–30%)

При переходе:
- boss short shriek;
- eye opens wider;
- speed +7%;
- no heal.

Добавляются:
- Кривой взгляд;
- Несчастливая земля.

---

# 25. ЛИХО — ABILITY: КРИВОЙ ВЗГЛЯД

Cooldown:
`200 ticks`.

Telegraph:
`26 ticks`.

Лихо:
- останавливается;
- eye fully opens;
- head/body faces target;
- faint particles form cone.

Range:
`10 blocks`.

Cone:
~50°.

Главная механика:
если игрок в момент финального pulse:
- находится в cone;
- И смотрит достаточно близко в сторону головы/глаза Лиха,

then:
- `Durnaya Dolya` 160 ticks;
- Weakness I 80 ticks;
- damage only `2`.

Если игрок:
- отвернулся;
- вышел за cone;
- спрятался за solid wall,

effect не применяется.

Проверка «смотрит»:
dot product player view vector vs vector to Likho eye.
Порог ориентир >=0.75.

Не требовать pixel-perfect.

---

# 26. ЛИХО — ABILITY: НЕСЧАСТЛИВАЯ ЗЕМЛЯ

Cooldown:
`150 ticks`.

Создать:
3 target zones around/predicted near player.

Telegraph:
`24 ticks`.

Zone radius:
`1.75`.

Visual:
- dark-red/brown particles;
- twisted thread/bone-like particles;
- NO block replacement.

Trigger:
- damage `6`;
- knockback `0.6`;
- Slowness I `50 ticks`.

Zones disappear after trigger.

Не спавнить >3.

---

# 27. ЛИХО — PHASE 3 (<=30%)

Transition:
- scream;
- crouched posture;
- speed +12% relative base total;
- attack spacing shorter ~15%.

Adds:
- Лихая погоня;
- Не поминай лихом.

---

# 28. ЛИХО — ABILITY: ЛИХАЯ ПОГОНЯ

Cooldown:
`220 ticks`.

Duration:
`80 ticks` max.

During:
- speed +25%;
- AI aggressively closes distance;
- can use short claw/sweep;
- Grab disabled during chase to avoid unavoidable combo.

After:
- mandatory exhaustion/recovery `40 ticks`;
- movement speed -30%;
- no major abilities.

Visual:
hair trails;
body leans;
arms low/forward.

---

# 29. ЛИХО — ABILITY: «НЕ ПОМИНАЙ ЛИХОМ»

Это leap/dash-like ability, поэтому ОБЯЗАТЕЛЬНО применить orientation fix section 5.

Cooldown:
`160 ticks`.

Use distance:
8–15 blocks.

Windup:
`18 ticks`.

Перед стартом:
- полный разворот на target;
- yaw difference <=25°;
- если нет — cancel.

Leap:
- по баллистической/controlled trajectory;
- не teleport;
- max distance 13 blocks.

Landing:
- radius 3.5;
- damage `8`;
- knockback `1.0`.

Miss:
recovery `26 ticks`.

No terrain damage.

---

# 30. ЛИХО — ANIMATIONS

Минимум:

1. `idle`
   - slight sway;
   - hair movement;
   - finger twitch;
   - eye blink rarely.

2. `walk`
   - long uneven stride;
   - arms hang.

3. `sweep_windup`
4. `sweep`
5. `grab_windup`
6. `grab_hold`
7. `grab_throw`
8. `gaze_charge`
   - eye opens;
   - head locks.
9. `ground_curse`
10. `phase2_transition`
11. `chase`
12. `leap_windup`
13. `leap`
14. `phase3_transition`
15. `death`
   - legs buckle;
   - long body folds;
   - eye dims/closes;
   - charms drop movement.

Если no GeckoLib:
использовать текущую project model animation approach.

---

# 31. ЛИХО — SOUNDS

Semantic:
- ambient whisper
- ambient groan
- hurt
- death
- sweep
- grab
- gaze_charge
- gaze_pulse
- cursed_ground
- phase_transition
- chase
- leap

Fallback intent:
- low Enderman ambient-like ONLY as layer, not direct obvious copy;
- witch/evoker whisper-ish low pitch where suitable;
- bone rattle for charms;
- leather/cloth;
- deep breath;
- death = long dry groan + bone/cloth collapse.

Не использовать один звук Zombie.

Если custom OGG отсутствуют:
vanilla fallbacks acceptable, but map each semantic event.

---

# 32. ЛИХО — NATURAL ENCOUNTER

Только Dark Forest.

World anchor:
- stored in boss-region data;
- region chance ~35%;
- safe ground search;
- night is required for entity activation/spawn if anchor discovered during day OR boss remains dormant/doesn't spawn until night.

Preferred simpler:
boss entity spawns from anchor only when:
- player within 64;
- night;
- anchor not cleared/cooldown.

Не держать тысячи boss entities loaded.

Encounter anchor remains.

---

# 33. ЛИХО — SUMMON ITEM: УЗЕЛ ДУРНОЙ ДОЛИ

ID:
`uzel_durnoy_doli`

Texture:
512×512.

Stack:
1.

Rarity:
EPIC/RARE depending current project.

## Design
Большой спутанный ритуальный узел.

Mandatory visual:
- black/dark-brown cords;
- one dirty-red cord;
- 3 bone beads;
- one tiny eye-shaped bone/wood bead;
- short black feather/cloth strips;
- central impossible-looking knot.

Palette:
- cord black `#242124`
- brown `#49372D`
- red `#6C2024`
- bone `#C6B28C`
- eye bead iris `#A94A2B`

## Recipe
Use earlier Hunt trophies.

Shapeless:
- `sosud_podveya`
- `nochnoy_kogot`
- `vihrevaya_nit`
- `uzel_podveya`
- string

One-time consumable ONLY on successful ritual.

## Use requirements
- Overworld;
- Dark Forest;
- night;
- open enough 5×5 area nearby;
- no active Likho within 96;
- no owned active boss encounter.

On success:
- 60 tick ritual delay;
- knot consumed at spawn moment;
- sound + dark particles;
- Likho spawns 12–18 blocks away.

Failure:
item not consumed.
Specific localized reason.

---

# 34. ЛИХО — LOOT

Guaranteed:
- `oko_likha`: 1
- `nit_durnoy_doli`: 2–4

Chance:
- `kost_likha`: 35%
- extra `nit_durnoy_doli`: looting-scalable
- `pustoy_ritualny_sosud`: 12%

Boss kill should also mark encounter clear.

---

# 35. ITEM: ОКО ЛИХА

ID:
`oko_likha`

Texture:
512×512.

Stack:
1.

Rarity:
EPIC.

Design:
- large dried eye;
- dirty ivory sclera;
- orange-brown iris;
- tiny black pupil;
- partially enclosed in rough dark metal/bone frame;
- 3 short cords;
- one small bone charm;
- not realistic gore.

Use:
- ingredient `odnoglazyy_obereg`;
- future Baba Yaga exchange in 0.9.3.

Tooltip:
`Оно закрылось, но ощущение взгляда не исчезло.`

---

# 36. ITEM: НИТЬ ДУРНОЙ ДОЛИ

ID:
`nit_durnoy_doli`

Texture:
256×256.

Stack:
32.

Design:
- tangled black/red thread;
- small bone beads;
- one loose end forms eye-like loop.

Use:
- future-use / Baba Yaga;
- optional component in One-Eyed Charm recipe;
- trophy tag.

---

# 37. ITEM: КОСТЬ ЛИХА

ID:
`kost_likha`

Texture:
256×256.

Stack:
16.

Design:
- long thin pale bone;
- unusually elongated;
- black cord around midpoint;
- tiny carved eye mark.

Use:
- One-Eyed Charm;
- future ritual.

---

# 38. ITEM: ОДНОГЛАЗЫЙ ОБЕРЕГ

ID:
`odnoglazyy_obereg`

Texture:
512×512.

Stack:
1.

Rarity:
EPIC.

## Visual
- dark iron oval frame;
- central preserved/stylized eye;
- bone supports top/bottom;
- black cords;
- dark-red tassel;
- small skull/bone bead.

Not a realistic eyeball necklace.

## Recipe
Shaped:
`B T B`
`I E I`
`. K .`

Where:
- B = bone
- T = nit_durnoy_doli
- I = iron_nugget
- E = oko_likha
- K = kost_likha

## Equip
Use existing amulet slot.
Fallback offhand only.

## Effect
When player receives a NEGATIVE timed effect:
- 20% chance;
- reduce NEW effect duration by 50%;
- internal cooldown `600 ticks` = 30 sec.

Exclude:
- `kurgan_curse` persistent variant;
- instant damage effects;
- raid/boss technical markers;
- `durnaya_dolya` may be reducible, yes.

Does not remove already active effect.
Works only at application.

Feedback:
- one eye blink sound/particle;
- cooldown starts only on successful proc.

---


# 38A. ТУГАРИН ЗМЕЙ — ПОЛНАЯ ВИЗУАЛЬНАЯ СПЕЦИФИКАЦИЯ ПО СЛОЯМ

ЭТОТ РАЗДЕЛ ИМЕЕТ ПРИОРИТЕТ НАД БОЛЕЕ КОРОТКИМИ ОПИСАНИЯМИ ТУГАРИНА НИЖЕ.

References:
- `REF_TUGARIN_FULL.png`
- `REF_TUGARIN_HERO.png`
- `REF_TUGARIN_VIEWS.png`
- `REF_TUGARIN_SPIN_KICK.png`
- `REF_TUGARIN_DETAILS_PALETTE.png`

Цель:
узнаваемый, комично-массивный, агрессивный Minecraft-Тугарин, близкий по общей читаемости к выбранному пользователем образу, но как самостоятельная модель Slavic Myths.

## 38A.1 Общий силуэт

Рост:
около `3 блоков`.

Ширина:
значительно больше обычного humanoid.

Главные пропорции:
- огромный прямоугольный/бочкообразный торс;
- очень широкие плечи;
- массивные верхние руки;
- огромные предплечья;
- крупные кулаки;
- маленькая голова относительно тела;
- короткая массивная шея почти не читается;
- относительно короткие ноги;
- очень крупные сапоги.

Silhouette должен быть узнаваем даже без texture:
«огромный верх + маленькая голова + широкие штаны + тяжёлые сапоги».

## 38A.2 Голова — форма

Голова:
- лысая;
- широкая в верхней/средней части;
- небольшая относительно плеч;
- немного вытянута вперёд носом/лицом;
- подбородок широкий.

Skin color:
тёплый tan/orange-brown.

Не делать:
- волосы;
- шлем;
- корону;
- рога.

## 38A.3 Лысина / кожа головы

Верх головы полностью открыт.

Texture:
- основная кожа;
- 1–2 более светлых блока на верхней плоскости;
- чуть более тёмные sides;
- никаких hair pixels сверху.

## 38A.4 Брови

Очень толстые чёрные брови.

Каждая:
- отдельная объёмная/слоистая деталь или очень выраженный raised layer;
- большая;
- направлена вниз к центру лица;
- создаёт сердитое выражение.

Не рисовать тонкие Minecraft brows.

## 38A.5 Глаза

Глаза:
- маленькие относительно лица;
- глубоко посаженные под бровями;
- тёмные/желтовато-коричневые;
- не glowing demon eyes.

Их задача — усиливать сердитое выражение, но НЕ конкурировать с усами.

## 38A.6 Нос

Нос:
- крупный;
- широкий;
- blocky;
- хорошо выступает вперёд.

Не villager nose:
он короче и шире.

## 38A.7 УСЫ — ГЛАВНАЯ FACE DETAIL

Усы нельзя заменить texture.

Структура:
- плотная центральная часть под носом;
- от неё две массивные чёрные половины расходятся влево/вправо;
- затем обе части опускаются вниз почти вертикально;
- концы доходят до верхней/средней груди.

Каждая половина усов:
- 2–4 blocky segments;
- заметная толщина;
- лёгкий изгиб;
- нижний конец сужается.

Общий силуэт:
как огромная перевёрнутая/разделённая чёрная дуга.

Цвет:
- near black;
- subtle dark gray highlights.

Animation:
- в idle едва заметное движение;
- при вертушке усы слегка отстают от вращения;
- при roar/fire breath отводятся наружу на несколько градусов.

НЕ добавлять полноценную бороду.
Усы должны оставаться отдельными длинными усами.

## 38A.8 Рот и зубы

Рот:
- широкий;
- расположен ниже moustache;
- тёмная внутренняя линия.

При roar/fire breath:
- открывается;
- может быть виден светло-кремовый ряд крупных blocky зубов;
- зубы не vampiric fangs.

## 38A.9 Шея

Шея:
- короткая;
- толстая;
- частично «утоплена» между плечами.

Модель должна создавать ощущение, что голова сидит прямо на огромном корпусе.

## 38A.10 Торс

Torso:
- bare skin;
- огромная грудь;
- широкий живот;
- объёмная верхняя спина;
- не bodybuilder six-pack realism.

Нужны крупные blocky planes:
- chest front;
- belly front;
- side torso;
- upper back.

Shading:
- chest upper lighter;
- under chest/belly darker;
- sides darker.

## 38A.11 Плечи

Плечи:
- очень широкие;
- rounded/blocky through several cubes, not one rectangular bar;
- visibly larger than head.

На каждом плече/верхней руке есть змеиные тёмные markings.

## 38A.12 Змеиные татуировки / markings

Reference показывает тёмные змеиные изображения на коже.

Обязательно:
- минимум один крупный snake motif на левом upper arm/shoulder;
- минимум один на правом;
- дополнительный фрагмент может заходить на back/side shoulder.

Форма:
- тёмная извилистая S-линия;
- blocky serpent head/curve;
- не tribal random lines.

Цвет:
dark brown/charcoal, не синий neon.

Эти marks наносятся texture, но должны быть отчётливо видны.

## 38A.13 Кожаные плечевые ремни / harness details

На reference возле верхней груди/плеч видны тёмные ремни с металлическими креплениями.

Реализовать:
- по одному короткому dark leather strap на верхней части каждого плеча/груди;
- прямоугольные металлические/латунные крепления;
- 1–2 заклёпки на каждом.

Это небольшая, но обязательная деталь.

Не превращать в полный доспех/harness.

## 38A.14 Верхние руки

Upper arms:
- огромные;
- bare skin;
- объёмные;
- snake tattoo overlay.

Локти крупные.

## 38A.15 Предплечья / наручи

Наручи — одна из самых важных деталей.

Каждый bracer состоит минимум из 5 визуальных слоёв:

1. base dark leather/near-black cylindrical/blocky cuff;
2. верхний широкий gray-steel rim;
3. нижний gray-steel rim;
4. central darker panel;
5. raised brass/gold snake ornament.

Дополнительно:
- small rivets/studs;
- leather seam lines.

Размер:
- от чуть ниже локтя почти до запястья;
- очень широкий.

Не делать просто чёрный рукав.

## 38A.16 Змеиная эмблема на наручах

На каждом bracer:
- крупная латунная змеиная фигура;
- S/спиральная форма;
- голова змеи может быть утолщена;
- эмблема приподнята визуально относительно тёмного panel.

Цвет:
brass/gold, не ярко-жёлтый.

Left/right emblem могут быть mirrored, но материал и размер одинаковы.

## 38A.17 Кулаки

Кисти/кулаки:
- огромные;
- bare skin;
- 4 blocky knuckle groups через texture/geometry;
- в idle пальцы полусжаты;
- при punch кулак явно закрывается.

Не обычные маленькие Minecraft hands.

## 38A.18 Пояс — КЛЮЧЕВАЯ ДЕТАЛЬ, НЕ УПРОЩАТЬ

Пояс должен быть подробно смоделирован.

### Base belt
- очень широкий;
- dark brown/black leather;
- идёт вокруг всей талии;
- визуальная высота около 0.25–0.35 блока в масштабе модели;
- верхний/нижний край слегка различаются по material/shading.

### Центральная пряжка
- огромная круглая/овальная brass buckle;
- выступает вперёд;
- имеет толстый тёмный outer rim;
- внутренний brass disk;
- внутри ОБЪЁМНЫЙ/рельефный coiled snake symbol.

### Coiled snake symbol
На центральной пряжке:
- змея свёрнута в спираль;
- голова различима как утолщённый конец;
- тело делает минимум 1.5 оборота;
- символ не должен быть просто буквой S.

### Маленькие belt plaques
По обе стороны от центральной buckle:
- минимум по 2 небольшие brass rectangular/square plaques;
- итого минимум 4;
- разнесены по belt;
- имеют маленькие darker centers/rivets.

### Belt rivets
Между plaques:
- маленькие brass/iron studs;
- не обязательно отдельной geometry каждый, можно texture, но они должны читаться.

### Hanging straps
От belt вниз:
- минимум 2 коротких dark leather straps;
- один слева от центра;
- один справа;
- на концах brass caps/plates;
- длина разная;
- слегка лежат поверх красных штанов.

### Side fittings
На боках:
- дополнительная dark buckle/metal connector допустима и желательна по reference.

Пояс должен хорошо читаться:
- спереди;
- сбоку;
- частично сзади.

Спина НЕ может иметь пустую однотонную чёрную полосу.

## 38A.19 Брюки — форма

Штаны:
- deep burgundy/dark red;
- очень широкие;
- baggy;
- состоят из нескольких крупных cloth volumes.

Waist:
- собирается под belt.

Thighs:
- широкие;
- каждый leg piece имеет заметный outward volume.

Crotch:
- отдельная центральная складка/тёмная зона;
- не один ровный красный куб.

Knees:
- ткань собирается/становится уже.

## 38A.20 Брюки — складки

Reference показывает крупные вертикальные/диагональные folds.

На каждой pant leg:
- минимум 3–5 крупных dark/light vertical fold bands texture;
- 1–2 диагональные fold breaks;
- darkest shadows under belt and inner leg.

Никакой случайной pixel-noise texture.

## 38A.21 Нижние манжеты брюк

Перед сапогами:
- тёмная/чёрная cuff band;
- часть burgundy ткани нависает над cuff;
- cuff отличается от boots material.

## 38A.22 Сапоги — общая форма

Сапоги:
- огромные;
- широкие;
- чёрно-charcoal;
- очень толстая подошва;
- blocky toe;
- визуально тяжёлые.

Каждый boot должен быть отдельной сложной частью, а не `leg cube + black texture`.

## 38A.23 Подошва

Sole:
- минимум 2 визуальных слоя:
  - dark upper sole;
  - почти чёрный lower sole;
- заметно выступает по сторонам;
- heel читается сзади.

При spin kick sole видна игроку — поэтому bottom texture должна быть проработана:
- тёмные прямоугольные tread blocks;
- 2–4 groove lines.

## 38A.24 Металлические элементы сапог

Reference:
небольшие gold/brass accents.

Добавить:
- narrow brass buckle/plate возле верхней/средней части каждого boot;
- dark iron small side plate;
- 1–2 rivets.

Не превращать boots в золотую броню.

## 38A.25 Спина Тугарина

Back view обязательно детализирован:

- широкий bare back;
- shoulder/upper-arm snake marking partly visible;
- leather shoulder strap attachments;
- belt wraps fully around;
- back belt has at least 2 plaques/fittings;
- baggy red trousers with rear fold shading;
- boot heels layered.

Не оставлять bare back полностью однотонным:
нужны крупные blocky shadows и возможно tattoo fragment.

## 38A.26 Асимметрия

Тугарин более симметричен Лиха, но не стерильно.

Разрешённые/желательные различия:
- snake tattoo left/right отличается;
- hanging belt straps разной длины;
- scratches on bracers differ;
- brass wear differs;
- trouser fold pattern differs;
- one shoulder strap can sit slightly differently.

## 38A.27 Surface wear

Все вещи не должны выглядеть новыми.

На:
- bracers;
- belt buckle;
- plaques;
- boots;

добавить:
- dark scratches;
- small worn edge pixels;
- occasional lighter metal chips.

На trousers:
- dark dirt near knees/lower hem.

На skin:
- subtle darker marks, но не blood/gore.

## 38A.28 Part hierarchy recommendation

Минимальные semantic parts:
- head
- brow_l/r
- nose
- moustache_center
- moustache_l_1..n
- moustache_r_1..n
- mouth/jaw
- neck
- torso_chest
- torso_belly
- shoulder_l/r
- strap_l/r
- upper_arm_l/r
- forearm_l/r
- bracer_base_l/r
- bracer_rim_top_l/r
- bracer_rim_bottom_l/r
- bracer_snake_l/r
- fist_l/r
- belt_base
- belt_buckle_outer
- belt_buckle_inner
- belt_snake
- belt_plaque_1..4+
- belt_hanging_l/r
- pants_waist
- thigh_cloth_l/r
- lower_pants_l/r
- cuff_l/r
- boot_l/r
- boot_sole_l/r
- boot_metal_l/r

Названия могут отличаться, но детали должны существовать.

## 38A.29 Вертушка — визуальная точность позы

Reference `REF_TUGARIN_SPIN_KICK.png` обязателен.

Во время ability:

### Preparation
- опорная нога слегка согнута;
- атакующая нога поднимается коленом вперёд;
- руки разводятся;
- torso немного назад.

### Spin start
- torso начинает поворот раньше полного выброса ноги;
- атакующая нога ещё согнута;
- опорная стопа остаётся на месте.

### Active kick
- атакующая нога почти полностью выпрямлена горизонтально;
- massive boot направлен наружу;
- torso наклонён в противоположную сторону для баланса;
- одна рука уходит назад, другая вбок;
- moustache отстаёт от вращения;
- hanging belt straps отклоняются по вращению.

### Recovery
- атакующая нога опускается;
- torso ещё имеет остаточный поворот;
- 1–2 тяжёлых шага для восстановления равновесия.

Нельзя:
- крутить всё тело как неподвижную статую;
- держать обе ноги прямо;
- скользить вперёд во время вращения;
- вращать только верхнюю половину тела.


---

# 39. ТУГАРИН ЗМЕЙ — ID / STATS

Entity:
`slavicmyths:tugarin_zmey`

Spawn egg:
`slavicmyths:tugarin_zmey_spawn_egg`

Egg colors:
- primary `#8F3D2F`
- secondary `#C99B45`

RU:
`Тугарин Змей`

EN:
`Tugarin Zmey`

Stats:
- MAX_HEALTH = `350`
- ATTACK_DAMAGE = `12`
- MOVEMENT_SPEED = `0.285`
- ARMOR = `15`
- ARMOR_TOUGHNESS = `4`
- KNOCKBACK_RESISTANCE = `0.78`
- FOLLOW_RANGE = `48`

XP:
`150–190`.

---

# 40. ТУГАРИН — MODEL PROPORTIONS

Reference обязателен.

Visual height:
`~3.0 blocks`.

Hitbox:
- width `1.5`
- height `2.9`

Silhouette:
- очень широкий;
- огромные плечи;
- torso занимает большую часть высоты;
- голова относительно маленькая;
- ноги короткие/массивные;
- руки огромные.

Не делать обычный humanoid scaled ×1.5.

---

# 41. ТУГАРИН — ГОЛОВА

Mandatory:
- bald/blocky head;
- thick black eyebrows;
- small eyes;
- extremely large black moustache;
- moustache splits in two long vertical/downward sections;
- broad nose;
- large mouth.

Moustache:
- separate model parts;
- moves slightly during animations;
- reaches upper chest.

Не делать beard вместо moustache.

---

# 42. ТУГАРИН — TORSO / SKIN

Torso:
- bare upper body;
- huge chest;
- massive abdomen;
- strong blocky shoulders.

Skin:
warm tan/orange.

Add:
- dark stylized snake-like tattoos/marks on shoulders/upper arms/back.

Reference shows dark snake motifs:
их сохранить.

Не делать full-body scales.

---

# 43. ТУГАРИН — НАРУЧИ

Каждая рука:
- massive black/dark bracer;
- metal gray edge bands;
- golden/brass snake emblem.

Bracer model:
- separate outer plates;
- not just painted sleeve.

Colors:
- dark iron `#343438`
- steel `#606165`
- brass `#B88936`
- highlight `#D0A34B`

Snake emblem:
simple blocky S/coil motif.

---

# 44. ТУГАРИН — ПОЯС

Одна из ключевых деталей.

- wide black/brown leather belt;
- giant circular/oval brass buckle;
- buckle carries coiled snake symbol;
- 2–4 smaller metal fittings;
- hanging short straps.

Buckle MUST be raised model/visible geometry or clearly layered texture.

Do not reduce to yellow square.

---

# 45. ТУГАРИН — ШТАНЫ

- wide baggy dark red/burgundy trousers;
- several large cloth folds represented blockily;
- black/dark waistband;
- knee/ankle narrowing.

Palette:
- deep red `#6C2026`
- burgundy `#842B2D`
- highlight `#9A3D38`
- shadow `#47191D`

---

# 46. ТУГАРИН — САПОГИ

Mandatory:
- huge black boots;
- thick soles;
- dark iron bands;
- small brass accents.

Boots visually heavy.

Spin-kick animation must clearly expose boot sole/shape.

---

# 47. ТУГАРИН — SKIN/TEXTURE PALETTE

Skin:
- base `#C47C56`
- highlight `#DD956C`
- shadow `#94553F`
- deep `#6D3A31`

Hair/moustache:
- `#171719`
- highlight `#2A292B`

Pants:
see above.

Leather:
- `#3C2822`
- `#5A392B`

Gold/brass:
- `#A9792F`
- `#CB9B42`

Entity texture:
512×512.

---

# 48. ТУГАРИН — AI PERSONALITY

Тугарин:
- постоянно давит вперёд;
- не очень быстрый постоянно;
- но имеет burst-mobility;
- любит close/mid range;
- использует charge/spin to punish static player;
- anti-range stone throw.

Target memory:
`260 ticks`.

Нет долгого idle.

---

# 49. ТУГАРИН — PHASE 1 (100–70%)

Abilities:
- heavy punch;
- charge;
- signature spin kick.

---

# 50. ТУГАРИН — HEAVY PUNCH

Windup:
`14 ticks`.

Range:
2.8.

Damage:
`12`.

Knockback:
`0.8`.

Recovery:
`10 ticks`.

Animation:
right shoulder back -> torso twist -> forward punch.

Shield:
normal physical block rules.

---

# 51. ТУГАРИН — CHARGE / РАЗБЕГ

ОБЯЗАТЕЛЬНО orientation fix section 5.

Cooldown:
`120 ticks`.

Use distance:
7–16.

Align:
до 20 ticks.

Start condition:
yaw difference <= `18°`.

Windup:
12 ticks после alignment.

Charge duration:
max 22 ticks.

Speed:
~1.7× normal.

Hit:
- damage `10`;
- knockback `1.2`.

One hit per charge.

If hits wall:
- no block destruction;
- recovery/stun `30 ticks`.

If misses:
recovery `20 ticks`.

После старта не может повернуть charge на 90°.

---

# 52. ТУГАРИН — SIGNATURE ABILITY: «ТУГАРИНОВА ВЕРТУШКА»

Это ОБЯЗАТЕЛЬНАЯ фирменная атака.

Reference:
`REF_TUGARIN_SPIN_KICK.png`

Нельзя заменить generic AoE spin.

Cooldown:
`170 ticks`.

## Animation stages

### Stage 1 — подготовка
Duration `12 ticks`.

Тугарин:
- переносит вес на левую/опорную ногу;
- вторую ногу слегка поднимает;
- руки разводит для баланса;
- torso отклоняется.

### Stage 2 — начало вращения
Duration `10 ticks`.

- body начинает rotation;
- lifted leg поднимается выше;
- moustache/straps визуально следуют вращению.

### Stage 3 — полный оборот
Duration `14 ticks`.

- body делает быстрый controlled spin;
- attack leg вытянута наружу;
- boot находится примерно на уровне груди игрока;
- arms counterbalance.

### Stage 4 — impact sweep
Active hit window:
примерно `6 ticks`.

Radius:
`4.0 blocks`.

Arc:
360° around Tugarin.

Damage:
`9`.

Knockback:
очень сильный:
`1.6–1.9` horizontal equivalent.

Vertical:
0.25.

Player должен реально отлетать несколько блоков.

Shield:
- damage reduce;
- knockback reduce only 35%;
- не полностью отменяет.

### Stage 5 — recovery
Duration:
`28 ticks`.

Тугарин:
- ставит ногу;
- делает 1–2 шага восстановления;
- briefly vulnerable.

## Critical restrictions
- ability не запускается, если vertical space/ground stance invalid;
- не запускать в воде глубже 1 block;
- не вращаться, если target уже >7 blocks и attack бессмысленна;
- НЕ скользить по земле во время spin;
- опорная точка остаётся почти фиксированной.

Это должно выглядеть именно как:
«встал на одну ногу → раскрутился → вынес второй ногой».

---

# 53. ТУГАРИН — PHASE 2 (70–35%)

Transition:
- roar;
- speed +6%;
- ability cooldowns -10% max;
- no heal.

Adds:
- double-hand slam;
- stone throw.

---

# 54. ТУГАРИН — ДВОЙНОЙ УДАР РУКАМИ

Cooldown:
`130 ticks`.

Windup:
`20 ticks`.

Тугарин поднимает обе руки.

Hit area:
frontal cone 100°.
Range:
4.5.

Damage:
`11`.

Knockback:
1.0.

Ground particles:
dust only.
No block break.

Recovery:
18 ticks.

---

# 55. ТУГАРИН — МЕТАНИЕ КАМНЯ

Anti-cheese.

Use:
если player >10 blocks или unreachable >50 ticks.

Cooldown:
`100 ticks`.

Visual:
Тугарин НЕ вырывает настоящий блок мира.
Создать projectile visual «каменный обломок».

Windup:
16 ticks.

Projectile:
- moderate speed;
- gravity;
- visible;
- damage 7;
- knockback 0.5;
- no block grief;
- breaks/disappears on collision.

Не использовать explosion.

---

# 56. ТУГАРИН — PHASE 3 (<=35%)

Adds:
- fire breath;
- enhanced spin kick.

Visual:
- mouth subtle ember glow;
- snake marks perhaps faint warm highlight;
- no dragon transformation.

---

# 57. ТУГАРИН — ОГНЕННОЕ ДЫХАНИЕ

Cooldown:
`150 ticks`.

Range:
7 blocks.

Cone:
60°.

Telegraph:
`22 ticks`.

- inhale;
- chest expands;
- mouth orange glow;
- crackle sound.

Active:
`24 ticks`.

Damage:
- tick-based but capped;
- target should receive total around 10–12 max from full exposure;
- ignite 60 ticks.

Do not deal full 6 damage every tick.

Line-of-sight:
required.
No through walls.

After:
recovery 18 ticks.

---

# 58. ТУГАРИН — УСИЛЕННАЯ ВЕРТУШКА

В phase 3:
- base spin kick remains;
- cooldown minimum 150 ticks;
- telegraph slightly shorter, NOT instant;
- radius `4.5`;
- damage `10`;
- knockback up to `2.0`.

Recovery remains >=24 ticks.

Не спамить.

---

# 59. ТУГАРИН — ANIMATIONS

Minimum:

1. idle_heavy
   - belly/chest breathing;
   - moustache slight motion;
   - fists flex.

2. walk
   - heavy steps.

3. heavy_punch.

4. charge_align
   - body turns toward target.

5. charge_start.

6. charge_run.

7. charge_wall_recovery.

8. spin_prepare.

9. spin_start.

10. spin_full.

11. spin_kick_active.

12. spin_recovery.

13. double_slam_charge.

14. double_slam.

15. stone_pick_visual.

16. stone_throw.

17. fire_breath_charge.

18. fire_breath.

19. phase_transition.

20. death
   - knees buckle;
   - torso falls;
   - moustache/arms settle.

Signature spin animations must correspond to reference sequence.

---

# 60. ТУГАРИН — SOUNDS

Semantic:
- ambient grunt
- ambient laugh/snort
- hurt
- death
- punch
- charge_start
- charge_run
- charge_wall
- spin_prepare
- spin_whoosh
- kick_impact
- double_slam
- stone_throw
- fire_breath_charge
- fire_breath
- phase_transition

Fallback:
- Ravager low sounds are acceptable as layer;
- Hoglin/Piglin brute-type grunts if version has usable events;
- heavy armor/boot sounds;
- sweep whoosh for spin;
- fire crackle.

Check actual 1.16.5 names.

Не один Ravager sound на всё.

---

# 61. ТУГАРИН — NATURAL ENCOUNTER

Controlled anchor, section 7.

Activation:
- daylight preferred;
- natural Tugarin can appear day or evening;
- not midnight-only.

When player within ~80 blocks of anchor:
- if cooldown/clear state allows;
- create boss at safe anchor;
- persistent during encounter.

Do not rely on mob cap.

---

# 62. ТУГАРИН — SUMMON ITEM: СТЕПНОЙ ШТАНДАРТ

ID:
`stepnoy_shtandart`

Texture:
512×512.

Stack:
1.

Rarity:
EPIC/RARE.

## Design
Rolled/partly unfurled dark-red steppe banner.

Mandatory:
- short dark wooden pole;
- burgundy cloth;
- golden/brass snake emblem;
- dark iron cap;
- two red tassels;
- worn/torn bottom edge;
- leather wrapping.

Palette:
- cloth `#7A272A`
- shadow `#4C191C`
- brass `#B58534`
- wood `#4D3425`
- iron `#383A3D`
- tassel `#9A302C`

## Recipe
Use 0.9.1 fire trophies.

Shaped:
`R S R`
`L V L`
`. W .`

Where:
- R = red_wool / red material
- S = pylayuschaya_cheshuya
- L = leather
- V = sosud_ognennogo_dyhaniya
- W = stick

If recipe is too expensive/cheap according to existing item values:
DO NOT redesign — keep these ingredients.

## Use
Requirements:
- Overworld;
- eligible steppe/plains/savanna biome;
- open sky;
- safe area;
- no Tugarin within 128;
- no active owned boss encounter.

Time:
day OR evening acceptable.

Ritual:
- place/use;
- 60 ticks dust/banner particle build-up;
- Tugarin appears 20–28 blocks away.

Item consumed only on successful spawn.

---

# 63. ТУГАРИН — LOOT

Guaranteed:
- `us_tugarina`: 1
- `tugarinova_kozha`: 3–6

Chance:
- `zmeinaya_pryazhka`: 45%
- `naboyka_tugarina`: 30%
- extra skin via looting.

Boss encounter clear state set.

---

# 64. ITEM: УС ТУГАРИНА

ID:
`us_tugarina`

Texture:
512×512.

Stack:
1.

Rarity:
EPIC.

Design:
- thick black curved moustache trophy;
- bound at center with brass ring;
- one small red cord;
- not hair tuft blob.

Tooltip:
`Даже отдельно от хозяина ус выглядит вызывающе.`

Future Baba Yaga exchange.

---

# 65. ITEM: ТУГАРИНОВА КОЖА

ID:
`tugarinova_kozha`

Texture:
256×256.

Stack:
32.

Design:
- thick reddish-tan hide patch;
- dark edge;
- small snake mark/tattoo fragment;
- leather-like, not dragon scale.

Use:
- Belt of Tugarin;
- future crafts.

---

# 66. ITEM: ЗМЕИНАЯ ПРЯЖКА

ID:
`zmeinaya_pryazhka`

Texture:
512×512 preferred.

Stack:
8.

Rarity:
RARE.

Design:
- circular/oval brass buckle;
- raised coiled snake;
- dark iron rear plate;
- scratches/wear.

Use:
- Belt of Tugarin;
- future Baba Yaga.

---

# 67. ITEM: НАБОЙКА ТУГАРИНА

ID:
`naboyka_tugarina`

Texture:
256×256.

Stack:
8.

Rarity:
UNCOMMON/RARE.

Design:
- massive dark boot sole/heel plate;
- iron edge;
- brass snake stud;
- worn leather attached.

0.9.2:
future-use trophy.
No active function.

Tooltip:
`Похоже, этой набойкой можно было проломить дверь.`

---

# 68. ITEM: ПОЯС ТУГАРИНА

ID:
`poyas_tugarina`

Texture:
512×512.

Stack:
1.

Rarity:
EPIC.

## Design
Inspired by boss belt but wearable player-scale.

Mandatory:
- wide dark red/brown leather;
- huge brass snake buckle;
- black secondary straps;
- 2 brass plates;
- hanging red tassel;
- tiny moustache/hair charm optional but reference theme allowed.

## Recipe
Shaped:
`L H L`
`S B S`
`. L .`

Where:
- L = tugarinova_kozha
- H = us_tugarina
- S = leather
- B = zmeinaya_pryazhka

## Equip
Existing belt slot.
Fallback offhand only if no belt slot, but prefer existing slot from earlier roadmap implementation.

## Mechanic: НАПОР
While player continuously sprints:
- after `40 ticks` of uninterrupted sprint:
  gain internal `Napor` active state.

Napor bonuses:
- knockback resistance +20%;
- player's melee knockback +15%.

No raw damage increase.
No speed increase.

State ends:
- 20 ticks after sprint stops;
- when player starts swimming/flying;
- on death.

No stacking multiple belts.

Feedback when activates:
- short boot/thump sound;
- small dust particles.

Tooltip:
`Долгий разбег делает владельца труднее остановить.`

---

# 69. HUNTER CHARM / HUNT SYSTEM INTEGRATION

Bosses are not ordinary Hunt mini-bosses, but Hunter Charm can sense them if nearby.

Extend semantic boss target tag/helper:
- likho_one_eyed
- tugarin_zmey

Search:
existing Hunter Charm can report them at increased radius:
`192 blocks`.

Do not add them to Hunting Horn normal target cycle.

Hunting Horn remains:
Ovinnik / Volkolak / Fire Serpent / Podvey.

Boss summons use dedicated ritual items.

---

# 70. TROPHY POUCH

Extend `hunt_trophies`:
- oko_likha
- nit_durnoy_doli
- kost_likha
- us_tugarina
- tugarinova_kozha
- zmeinaya_pryazhka
- naboyka_tugarina
- uzel_durnoy_doli
- stepnoy_shtandart optionally only if current trophy pouch allows ritual components.

Preferred:
finished accessories:
- One-Eyed Charm
- Belt of Tugarin
do NOT go into trophy pouch.

---

# 71. SPAWN EGGS

Mandatory:
- `likho_one_eyed_spawn_egg`
- `tugarin_zmey_spawn_egg`

Creative/debug use ignores biome conditions.

Document:
`/summon slavicmyths:likho_one_eyed`
`/summon slavicmyths:tugarin_zmey`

---

# 72. ADVANCEMENTS

Add max 4–5.

## `name_your_misfortune`
RU:
`Не поминай лихом`
Trigger: kill Likho.

## `steppe_champion`
RU:
`Богатырь степи`
Trigger: kill Tugarin.

## `ill_fate_broken`
RU:
`Перерезать нить`
Trigger: obtain One-Eyed Charm.

## `full_speed_ahead`
RU:
`С разбега`
Trigger: Belt of Tugarin Napor activates.

## Optional
`two_world_bosses`
RU:
`Два бедствия`
Trigger: player has killed both bosses, if easy via advancement criteria.

---

# 73. JEI

Show:
- Knot of Ill Fate recipe;
- One-Eyed Charm;
- Steppe Standard;
- Belt of Tugarin.

Loot-only items no fake recipes.

---

# 74. LOCALIZATION

RU/EN for:
- bosses;
- spawn eggs;
- Ill Fate effect;
- all new items;
- boss ritual messages;
- spawn failure messages;
- boss bar names;
- tooltips;
- advancements.

No hardcoded visible strings if project uses lang.

---

# 75. PERFORMANCE

World boss anchors:
- generated/determined at chunk/region lifecycle, not by global every-tick scans.
- activation only when player near anchor.
- saved data lightweight.

Boss AI:
- no full 128-block entity scans each tick.
- AoE checks only during active windows.
- particles bounded.

---

# 76. ANTI-CHEESE

Both bosses:
- no terrain grief;
- no attack through solid walls;
- no instant teleport to player;
- no side/back dash;
- no permanent projectile immunity;
- no endless adds.

Likho:
- if player towers, can use long-range cursed-ground/gaze where LOS allows, but does not destroy tower.

Tugarin:
- stone projectile handles unreachable ranged camping.
- Charge requires valid forward path.
- Spin cannot climb slabs mid-animation or slide up walls.

---

# 77. README

Update with full section:
`0.9.2 — Боссы мира`.

Must describe:
- natural world spawn system;
- why it is controlled, not mob RNG;
- Tugarin eligible steppe/plains biomes and guaranteed regional placement;
- Likho only Dark Forest + ~35% eligible-region chance;
- both summon rituals;
- boss phases;
- exact HP;
- signature attacks;
- dash orientation bugfix;
- rewards;
- recipes;
- boss eggs;
- commands;
- Hunter Charm behavior;
- respawn/cooldown policy.

Also update roadmap:
- 0.9.3 = Baba Yaga.
- 0.9.4 = final content integration.
- 0.9.5+ = content freeze/audit.

---

# 78. MANUAL TEST CHECKLIST

README/manual section at least:

## Dash orientation regression
1. spawn Tugarin.
2. stand behind him during Charge availability.
3. verify he rotates BEFORE charge.
4. verify charge never starts backward.
5. stand 90° to side.
6. verify same.
7. test any reusable dash helper affected old mobs without breaking them.

## Likho
8. summon via egg.
9. check model height/details.
10. eye exists as one central eye.
11. hair/charms visible.
12. sweep.
13. grab.
14. throw.
15. Ill Fate.
16. gaze counter by looking away.
17. ground zones.
18. phase 2.
19. chase phase.
20. leap correctly faces target.
21. death/loot.
22. One-Eyed Charm.
23. persistent Kurgan Curse excluded from charm shortening.

## Likho natural
24. search Dark Forest region.
25. verify no natural Likho outside Dark Forest.
26. verify controlled anchor logic.
27. verify regional rarity is materially lower than Tugarin.

## Tugarin
28. spawn egg.
29. model details.
30. moustache.
31. snake bracers/buckle.
32. charge.
33. wall miss recovery.
34. signature spin sequence.
35. strong knockback.
36. fixed pivot during spin.
37. phase 2.
38. stone throw.
39. phase 3.
40. fire breath.
41. enhanced spin.
42. death/loot.
43. Belt of Tugarin.

## Tugarin natural
44. locate/test plains/savanna region.
45. verify an anchor is actually generated in eligible region.
46. verify boss availability is not dependent on tiny mob-spawn weight.
47. verify no Tugarin in Dark Forest-only logic.

## Ritual items
48. invalid biome doesn't consume.
49. invalid time for Likho doesn't consume.
50. successful ritual consumes.
51. duplicate nearby boss blocked.
52. boss bar appears/disappears.

---

# 79. BUILD / POLYMC

After implementation:
1. do NOT launch Minecraft;
2. headless build;
3. fix compile/resource errors;
4. produce JAR;
5. update known PolyMC test `mods`;
6. avoid multiple conflicting Slavic Myths JARs;
7. if path unknown, report output path without guessing.

---

# 80. LOW-TOKEN IMPLEMENTATION ORDER

1. Read existing 0.9.0/0.9.1 Hunt + world saved data.
2. Implement shared facing/dash orientation fix.
3. Add boss-region anchor data.
4. Register items/effect/tags.
5. Register both entities + eggs.
6. Models/textures/render.
7. Likho AI/phases.
8. Tugarin AI/phases.
9. Natural boss anchor placement.
10. Ritual items.
11. Boss bars.
12. Loot.
13. Accessories.
14. Hunter Charm/Pouch integration.
15. JEI.
16. Advancements.
17. Localization.
18. README.
19. Headless build.
20. PolyMC copy.

Do not spend time on 0.9.3.

---


# VISUAL COMPLIANCE CHECKLIST — 0.9.2

Codex должен пройти этот checklist перед Definition of Done.

## Likho
- [ ] ровно один центральный глаз;
- [ ] отдельное веко/анимация глаза;
- [ ] лицо узкое и бледное;
- [ ] волосы состоят из многих отдельных прядей;
- [ ] front hair асимметричен;
- [ ] back hair многослойный;
- [ ] длинные руки;
- [ ] отдельные длинные finger groups;
- [ ] dark wrist wraps;
- [ ] внутренняя тёмная туника;
- [ ] много отдельных внешних cloth strips;
- [ ] неровный рваный подол;
- [ ] отдельная бордовая полоса ткани;
- [ ] rope/leather belt с узлом;
- [ ] минимум 2 крупных skull/bone charms;
- [ ] минимум 3–5 мелких bone charms;
- [ ] диагональный chest cord/necklace;
- [ ] ноги длинные и тонкие;
- [ ] босые вытянутые ступни;
- [ ] side view не пустой;
- [ ] back view не пустой;
- [ ] модель не симметричная копия сама себя.

## Tugarin
- [ ] маленькая лысая голова;
- [ ] огромный широкий torso;
- [ ] thick separate eyebrows;
- [ ] большой выступающий нос;
- [ ] огромные усы отдельной геометрией;
- [ ] левая и правая половины усов доходят до груди;
- [ ] бороды нет;
- [ ] короткая толстая шея;
- [ ] snake tattoos на обеих руках/плечах;
- [ ] shoulder leather straps;
- [ ] metal strap fittings/rivets;
- [ ] огромные upper arms;
- [ ] bracers имеют base + metal rims;
- [ ] raised brass snake emblem на каждом bracer;
- [ ] большие кулаки;
- [ ] широкий dark leather belt;
- [ ] центральная пряжка объёмная;
- [ ] на пряжке coiled snake, а не простой круг/S;
- [ ] минимум 4 маленьких brass belt plaques;
- [ ] studs/rivets по поясу;
- [ ] минимум 2 hanging leather straps с brass tips;
- [ ] пояс детализирован сбоку и сзади;
- [ ] бордовые штаны baggy, не прямые кубы;
- [ ] видимые крупные folds;
- [ ] dark cuffs над boots;
- [ ] boots огромные;
- [ ] sole минимум двухслойная;
- [ ] bottom boot tread texture сделана;
- [ ] небольшие brass/iron boot fittings;
- [ ] back view содержит straps/tattoos/belt fittings/folds;
- [ ] металлические элементы имеют wear/scratches;
- [ ] spin kick использует позу с одной опорной ногой;
- [ ] активная kick-leg почти горизонтальна;
- [ ] moustache и straps визуально следуют вращению.

Если хотя бы один пункт отсутствует без технической причины:
визуальная часть задачи НЕ выполнена.

---

# 81. DEFINITION OF DONE

## Shared
- [ ] dash orientation helper prevents side/back rush;
- [ ] controlled world-boss anchor system;
- [ ] no tiny mob RNG reliance;
- [ ] boss bars;
- [ ] spawn eggs;
- [ ] summon commands;
- [ ] sounds;
- [ ] animation states;
- [ ] loot;
- [ ] README.

## Likho
- [ ] 320 HP;
- [ ] Dark Forest only natural anchor;
- [ ] ~35% eligible-region chance;
- [ ] unique 3.2–3.5 block model;
- [ ] one central eye;
- [ ] long hair;
- [ ] long fingers;
- [ ] skull/bone charms;
- [ ] ragged layered clothing;
- [ ] sweep;
- [ ] grab/throw;
- [ ] Ill Fate;
- [ ] Gaze;
- [ ] cursed ground;
- [ ] chase;
- [ ] leap;
- [ ] 3 phases;
- [ ] ritual summon;
- [ ] loot;
- [ ] One-Eyed Charm.

## Tugarin
- [ ] 350 HP;
- [ ] guaranteed eligible-region steppe/plains anchor;
- [ ] unique ~3 block broad model;
- [ ] bald head;
- [ ] huge black moustache;
- [ ] snake tattoos;
- [ ] snake bracers;
- [ ] large snake buckle;
- [ ] burgundy trousers;
- [ ] massive boots;
- [ ] heavy punch;
- [ ] properly aligned charge;
- [ ] exact one-leg spin-kick animation;
- [ ] strong knockback;
- [ ] double slam;
- [ ] stone throw;
- [ ] fire breath;
- [ ] enhanced phase-3 spin;
- [ ] 3 phases;
- [ ] Steppe Standard;
- [ ] loot;
- [ ] Belt of Tugarin.

## Integration
- [ ] Hunter Charm senses bosses;
- [ ] Trophy Pouch accepts trophies;
- [ ] JEI recipes;
- [ ] advancements;
- [ ] localization;
- [ ] headless build;
- [ ] PolyMC update if path known.

---

# 82. FINAL CODEX RESPONSE

Return ONLY:

## Реализовано
Short facts.

## Боссы
Actual IDs / HP / implemented phases.

## World spawn
Actual region rules and biome rules.

## Предметы
Actual IDs / functions.

## Команды
Exact commands.

## Основные файлы
Paths.

## Build
Result + JAR.

## PolyMC
Copied path / unknown.

## Визуальное соответствие
Отдельно подтвердить прохождение `VISUAL_MODEL_CHECKLIST.md`.
Если была техническая замена хотя бы одной обязательной детали — перечислить её.

## Не выполнено
Only real incompleteness.

Не заявлять то, чего нет в файлах.
