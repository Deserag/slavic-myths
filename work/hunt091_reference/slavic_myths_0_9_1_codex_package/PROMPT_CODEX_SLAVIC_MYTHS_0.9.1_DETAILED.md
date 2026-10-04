# SLAVIC MYTHS — STRICT CODEX IMPLEMENTATION SPECIFICATION
## Version target: 0.9.1 — «Охота на нечисть II»
## Minecraft 1.16.5 / Forge 36.2.42 / Java 8 / mod id `slavicmyths`
## Status: FINAL IMPLEMENTATION SPECIFICATION

---

# 0. ОСНОВНОЕ ПРАВИЛО

Это НЕ запрос на разработку геймдизайна.

Геймдизайн, визуальный язык, характеристики, предметы, механики, анимации, звуки, spawn eggs, loot, summon rules и integration rules уже описаны здесь.

Codex должен:
1. прочитать минимально необходимую часть существующего проекта;
2. найти реализацию 0.9.0 Hunt System;
3. РАСШИРИТЬ её;
4. реализовать указанные сущности и предметы;
5. обновить README;
6. собрать JAR без запуска Minecraft;
7. обновить PolyMC `mods`, если локальный путь уже известен.

Codex НЕ должен:
- проектировать новую hunting framework вместо 0.9.0;
- переписывать Овинника и Волколака без технической необходимости;
- добавлять Бабу-ягу;
- добавлять Лихо;
- добавлять Тугарина;
- возвращать Живую/мёртвую воду;
- строить новую большую локацию;
- придумывать новых существ;
- менять утверждённые визуальные концепты;
- запускать Minecraft-клиент.

---

# 1. ЖЁСТКОЕ ОГРАНИЧЕНИЕ ПО ТОКЕНАМ

Задача предназначена для модели ниже максимального уровня.

Поэтому:

- НЕ делай полный аудит репозитория.
- НЕ перечитывай unrelated modules.
- НЕ анализируй подробно еду, курганы, рыбалку, бандитов и т.п., если текущий код их не вызывает.
- НЕ пиши длинное вступление/план.
- Найди существующие классы/registry 0.9.0 и сразу расширяй.
- Используй существующие:
  - HuntTarget/HuntEncounter/HuntManager или их фактические аналоги;
  - Hunting Horn;
  - Hunter Charm;
  - Trophy Pouch;
  - item/entity tags;
  - accessory system;
  - entity registration;
  - sounds pattern;
  - loot/JEI/advancement pattern.
- Не создавай вторую систему, если первая работает.
- Не запускать `runClient`.
- Только headless build/compile/resource validation.

---

# 2. СКОУП 0.9.1

Добавить ровно ДВА новых мини-босса:

1. `fire_serpent` — Огненный змей.
2. `podvey` — Подвей.

Добавить новые предметы:

1. `pylayuschaya_cheshuya` — Пылающая чешуя.
2. `ognennoe_pero` — Огненное перо.
3. `obereg_padayuschey_zvezdy` — Оберег падающей звезды.
4. `uzel_podveya` — Узел Подвея.
5. `vihrevaya_nit` — Вихревая нить.
6. `vetrovoy_uzel` — Ветровой узел.
7. `sosud_ognennogo_dyhaniya` — Сосуд огненного дыхания.
8. `sosud_podveya` — Сосуд Подвея.

Расширить:
- Hunting Horn;
- Hunter Charm;
- Trophy Pouch;
- Hunt Target tag;
- loot;
- JEI;
- advancements;
- README.

Добавить:
- spawn eggs обоим mini-boss;
- vanilla `/summon` support;
- debug commands только если существующий `/slavicmyths dev hunt` уже есть.

НЕ добавлять:
- новых больших структур;
- третьего mini-boss;
- NPC;
- boss;
- новую dimension;
- новую крупную magic framework.

---

# 3. REFERENCES — ОБЯЗАТЕЛЬНЫ

`references/` — часть технического задания.

Приоритет:
1. точные требования этого документа;
2. isolated PNG;
3. full concept PNG;
4. существующий стиль мода.

Нельзя заменять дизайн собственной интерпретацией.

## Mob references
- `REF_00_MINIBOSSES_FULL.png`
- `REF_MOB_FIRE_SERPENT_FULL.png`
- `REF_MOB_FIRE_SERPENT_HERO.png`
- `REF_MOB_FIRE_SERPENT_FRONT.png`
- `REF_MOB_FIRE_SERPENT_SIDE.png`
- `REF_MOB_FIRE_SERPENT_BACK.png`
- `REF_MOB_FIRE_SERPENT_DETAILS.png`
- `REF_MOB_PODVEY_FULL.png`
- `REF_MOB_PODVEY_HERO.png`
- `REF_MOB_PODVEY_FRONT.png`
- `REF_MOB_PODVEY_SIDE.png`
- `REF_MOB_PODVEY_BACK.png`
- `REF_MOB_PODVEY_DETAILS.png`

## Item references
- `REF_01_ITEMS_FULL.png`
- `REF_ITEM_01_PYLAYUSCHAYA_CHESHUYA.png`
- `REF_ITEM_02_OGNENNOE_PERO.png`
- `REF_ITEM_03_OBEREG_PADAYUSCHEY_ZVEZDY.png`
- `REF_ITEM_04_UZEL_PODVEYA.png`
- `REF_ITEM_05_VIHREVAYA_NIT.png`
- `REF_ITEM_06_VETROVOY_UZEL.png`
- `REF_ITEM_07_SOSUD_OGNENNOGO_DYHANIYA.png`
- `REF_ITEM_08_SOSUD_PODVEYA.png`

---

# 4. ОБЩИЙ ВИЗУАЛЬНЫЙ СТАНДАРТ

Новые существа должны выглядеть как качественные Minecraft 1.16.5 mod entities:
- blocky;
- voxel readable;
- не реалистичный sculpt;
- не vanilla mob recolor;
- силуэт читается издалека;
- детали соответствуют reference.

## Entity textures
Минимум:
- 256×256.

Для Огненного змея:
- 512×512 предпочтительно из-за сегментов, трещин и огненных участков.

Для Подвея:
- 512×512 предпочтительно из-за маски, тканевых лент, вихря и материалов.

## Items
Трофеи:
- минимум 256×256.

Сложные:
- `obereg_padayuschey_zvezdy`
- `vetrovoy_uzel`
- `sosud_ognennogo_dyhaniya`
- `sosud_podveya`

=> 512×512 предпочтительно.

Не создавать 16×16/32×32 final assets.

---

# 5. ОБЯЗАТЕЛЬНО РАСШИРИТЬ 0.9.0, А НЕ ДУБЛИРОВАТЬ

Hunting Horn target cycle становится:

1. `OVINNIK`
2. `VOLKOLAK`
3. `FIRE_SERPENT`
4. `PODVEY`

После Podvey -> снова Ovinnik.

Hunter Charm через существующий `hunt_targets` tag автоматически ищет все 4 цели.

Trophy Pouch через `hunt_trophies` tag принимает новые трофеи.

Если 0.9.0 реализовал enum иначе:
- расширить его;
- не создавать HuntTargetV2.

---

# 6. ENTITY IDS / SPAWN EGGS

## Fire Serpent
Entity:
`slavicmyths:fire_serpent`

Spawn egg:
`slavicmyths:fire_serpent_spawn_egg`

Egg colors:
- primary `#29191A`
- secondary `#F04B16`

## Podvey
Entity:
`slavicmyths:podvey`

Spawn egg:
`slavicmyths:podvey_spawn_egg`

Egg colors:
- primary `#56626F`
- secondary `#C5E4F1`

Spawn eggs:
- находятся в том же creative tab, что остальные spawn eggs Slavic Myths;
- спавнят entity напрямую и не требуют природных условий;
- нужны для creative/debug/manual testing.

---

# 7. ОГНЕННЫЙ ЗМЕЙ — ОБЩАЯ РОЛЬ

Огненный змей — воздушный mini-boss.

Его сложность:
- движение;
- изменение высоты;
- атакующие пролёты;
- пикирование;
- визуальные ложные цели;
- давление на игрока, который пытается стоять на месте.

Он НЕ:
- маленький Змей Горыныч;
- обычный дракон;
- Blaze;
- Phantom recolor.

Нет:
- ног;
- огромных крыльев;
- трёх голов.

---

# 8. FIRE SERPENT — MODEL DESIGN

References:
- `REF_MOB_FIRE_SERPENT_HERO.png`
- FRONT/SIDE/BACK/DETAILS.

## 8.1 Размер
Общая visual length:
примерно `5.0–6.0 блоков` с учётом хвоста.

Entity hitbox:
не растягивать на весь хвост.

Target body hitbox:
- width `1.25`
- height `1.55`

Center/hitbox следует за основной грудной/головной частью.

Хвост — визуальная модель, не отдельные damageable entities.

## 8.2 Сегменты
Модель строится из:
- head;
- jaw;
- neck 1;
- neck 2;
- torso;
- body segment 1;
- body segment 2;
- body segment 3;
- tail 1;
- tail 2;
- tail tip;
- dorsal plates/horns.

Предпочтительно 20–40 крупных cubes/parts, а не сотни мелких.

## 8.3 Голова
- широкая угловатая змеиная голова;
- массивная верхняя челюсть;
- отдельная нижняя челюсть;
- короткие назад направленные рога/пластины;
- яркие маленькие глаза;
- внутри пасти виден жар.

Не делать голову похожей на Ender Dragon.

## 8.4 Корпус
Внешняя оболочка:
- почти чёрные пластины;
- каждая секция имеет угловатую Minecraft-геометрию.

Между пластинами:
- раскалённые красно-оранжевые трещины.

Внутренний core:
- визуально ощущается огненным;
- не прозрачный целиком.

## 8.5 Хвост
Хвост:
- длинный;
- сужающийся;
- последние сегменты окружены огненными particles;
- никаких лопастей/драконьего наконечника.

## 8.6 Texture palette
- shell black `#24191A`
- shell warm dark `#35201D`
- deep red `#771D15`
- ember red `#B92716`
- orange `#F34E13`
- hot orange `#FF7A17`
- yellow `#FFC33A`
- hottest `#FFF0A0`

## 8.7 Emissive
Если существующий renderer позволяет emissive texture:
отдельный emissive layer:
- глаза;
- пасть;
- внутренние трещины.

Не делать весь mob fullbright.

---

# 9. FIRE SERPENT — STATS

- MAX_HEALTH = `155`
- ATTACK_DAMAGE basic = `7`
- MOVEMENT_SPEED ground fallback = `0.24`
- FLYING_SPEED / custom flight target ~= `0.34`
- ARMOR = `5`
- KNOCKBACK_RESISTANCE = `0.35`
- FOLLOW_RANGE = `40`
- FIRE_IMMUNE = true

XP:
`55–70`.

Persistent during Hunt Encounter:
true.

Не despawn mid-fight.

---

# 10. FIRE SERPENT — FLIGHT CONTROLLER

НЕ использовать Phantom AI целиком.

Нужен контролируемый mini-boss movement.

States:
1. `APPROACH`
2. `ORBIT`
3. `ALIGN_DASH`
4. `FIRE_DASH`
5. `ALIGN_DIVE`
6. `STAR_DIVE`
7. `DECOY_CAST`
8. `MELEE_SNAP`
9. `WATER_STUN`
10. `RECOVERY`
11. `RETURN_TO_ANCHOR`

## 10.1 Боевая высота
Обычно:
- 3–8 блоков над local ground;
- может подниматься до 10–12 для dive telegraph.

Не должен постоянно улетать выше 20 блоков.

## 10.2 Orbit
На дистанции 6–12 блоков:
- облетает игрока;
- изменяет угол;
- меняет высоту +/- 2 blocks;
- выбирает способность.

Orbit не должен длиться более ~2–3 секунд без атаки.

## 10.3 Path failure
Если flying path blocked:
- выбрать новую боковую точку;
- не пытаться бесконечно лететь через стену;
- после нескольких неудач возвращаться к encounter anchor.

---

# 11. FIRE SERPENT — BASIC MELEE SNAP

Если игрок очень близко:
distance <= 2.4.

Animation:
- голова отводится назад;
- jaw opens;
- 7 tick windup;
- snap.

Damage:
`7`.

Cooldown:
примерно `22 ticks`.

Это fallback, не основная атака.

---

# 12. FIRE SERPENT — ABILITY 1: ОГНЕННЫЙ ПРОЛЁТ

Signature ability.

Cooldown:
`100 ticks`.

Use range:
обычно 7–16 blocks.

## Sequence
### Align
- Змей отходит/смещается;
- выбирает прямую линию через predicted player position.
- max align duration 30 ticks.

### Telegraph
`16 ticks`.

В telegraph:
- тело выпрямляется;
- огненные трещины становятся ярче;
- звук нарастающего жара;
- частицы вытягиваются назад.

### Dash
Duration:
до `18 ticks`.

Speed:
примерно `1.6–1.8× normal flight`.

Hit:
- damage `8`;
- ignite `60 ticks`;
- horizontal knockback `0.7`.

Один игрок не может получить damage от одного dash более 1 раза.

## Trail
Позади остаётся НЕ fire blocks, а temporary lightweight trail areas/particles.

Duration:
`40 ticks`.

Trail contact:
- damage `2 fire damage`;
- per-target cooldown `20 ticks`.

Trail:
- не ломает блоки;
- не поджигает деревья;
- не ставит настоящий огонь.

## Miss
После промаха:
RECOVERY `18 ticks`.

Игрок получает короткое окно для ranged/melee ответа.

---

# 13. FIRE SERPENT — ABILITY 2: ПАДАЮЩАЯ ЗВЕЗДА

Cooldown:
`150 ticks`.

## Preparation
Змей поднимается:
- примерно на 6–10 блоков выше игрока.

Telegraph:
`24 ticks`.

На земле:
- круг/кольцо огненных particles;
- radius ~3 blocks;
- ясно показывает target zone.

Никаких невидимых AoE.

## Dive
Змей резко пикирует в точку.

Impact:
- radius `3.25`;
- damage `10`;
- knockback horizontal;
- small vertical knockback;
- ignite `40 ticks`.

Не разрушает terrain.

## Miss/recovery
Если игрок вышел:
- attack misses;
- Змей остаётся низко `30 ticks`;
- movement speed -25%;
- хороший punish window.

---

# 14. FIRE SERPENT — ABILITY 3: ОГНЕННЫЕ ДВОЙНИКИ

Cooldown:
`220 ticks`.

Не чаще одного набора одновременно.

## Cast
Telegraph:
`18 ticks`.

Создать:
- 2 визуальных ложных serpent projections;
- настоящий Змей остаётся третьей целью.

Decoys:
- существуют максимум `70 ticks`;
- имеют 1 HP либо собственный simple projection type;
- не drop loot;
- не дают XP;
- не запускают отдельные Hunt Encounters.

Movement:
- расходятся в разные стороны;
- повторяют огненный trail visual;
- могут сделать fake dash windup.

Damage:
- fake copies НЕ наносят full boss damage;
- максимум 1 cosmetic/light `1 damage` touch ИЛИ вообще 0 damage.
Предпочтительно 0 damage.

Попадание игрока по ложной копии:
- копия исчезает;
- короткий burst particles.

Не превращать ability в summon spam.

---

# 15. FIRE SERPENT — WATER / WEATHER

## Direct water contact
Если основное тело в water:
- `4 damage` каждые 20 ticks;
- перейти в `WATER_STUN`;
- stun/recovery `50 ticks`;
- emissive intensity lowered;
- Fire Dash unavailable ещё `80 ticks`.

Не должен мгновенно умирать.

## Rain
Если raining + open sky:
- fire-type damage Змея -20%;
- ignite durations -25%;
- Fire Dash cooldown +20%;
- НЕ наносить ему прямой постоянный rain damage.

Thunder:
не даёт ему дополнительных статов.

---

# 16. FIRE SERPENT — ANTI-CHEESE

Если игрок находится:
- под низким 2-block roof;
- и flying direct attacks не имеют valid path более 80 ticks,

Змей:
- НЕ проходит через крышу;
- НЕ разрушает её;
- отходит к reachable opening;
- либо использует controlled small flame projectile.

Fallback projectile:
- cooldown 100;
- damage 4;
- no terrain grief;
- не primary attack.

Если encounter невозможно продолжить из-за полной изоляции:
- сохраняет target memory;
- ждёт/патрулирует у выхода;
- не despawn.

---

# 17. FIRE SERPENT — NATURAL SPAWN

Это редкий mini-boss, НЕ обычный mob.

Условия:
- Overworld;
- night;
- open sky;
- solid terrain below;
- не ocean;
- не deep dense cave;
- preferred open biomes:
  - plains;
  - sunflower plains;
  - mountain/hill/open highland compatible biomes;
  - forest edge allowed if clear sky.
- max 1 Fire Serpent within 96 blocks.

Natural chance:
очень низкая.

Если существующая Hunt natural-spawn система 0.9.0 имеет configurable rarity:
использовать её.

---

# 18. HUNTING HORN — FIRE SERPENT RULES

Target:
`FIRE_SERPENT`.

Requirements:
- Overworld;
- night;
- open sky at player;
- no owned active Hunt Encounter;
- cooldown ready.

Safe-space search:
- 20–36 blocks from player;
- candidate must have air volume:
  примерно 5×5×5 free enough for entity movement;
- no lava/water at spawn center;
- target Y may be 4–8 blocks above ground.

Search:
до 40 candidates.

Failure keys:
- `hunt.fire_serpent.day`
- `hunt.fire_serpent.no_open_sky`
- `hunt.fire_serpent.no_air_space`
- common active encounter/cooldown errors.

Successful summon:
- horn sound;
- particles high in air;
- entity arrives from selected spawn point.

---

# 19. FIRE SERPENT — ANIMATIONS

Минимум:

1. `idle_flight`
   - segment wave;
   - jaw subtle;
   - tail wave.

2. `orbit_flight`
   - stronger body S-curve.

3. `bite`
   - head back;
   - jaw open/close.

4. `fire_dash_charge`
   - body straightens;
   - jaw opens slightly;
   - dorsal elements pull backward.

5. `fire_dash`
   - nearly straight streamline.

6. `star_dive_charge`
   - head points down;
   - coils tighten.

7. `star_dive`
   - elongated downward motion.

8. `decoy_cast`
   - body curls into loose ring;
   - flame pulse.

9. `water_stun`
   - body hangs lower;
   - movements slower;
   - ember sections dim.

10. `death`
   - body loses lift;
   - flame fades;
   - entity falls/curls and disappears normally.

Если проект НЕ использует GeckoLib:
не добавлять dependency только ради этого.
Реализовать через существующую model animation system.

---

# 20. FIRE SERPENT — SOUNDS

Custom audio creation не требуется, если файлов нет.
Но sound behavior обязательно.

Semantic events:
- ambient
- hurt
- death
- bite
- dash_charge
- dash
- dive_charge
- impact
- decoy_cast
- water_stun

Fallback intent:
- ambient: low Blaze crackle + airy hiss;
- hurt: Blaze hurt/Phantom-like shriek at lower pitch;
- death: longer fire extinguish + spectral/large creature cue;
- dash_charge: fire crackle rising;
- dash: fast whoosh;
- impact: dull explosion cue WITHOUT explosion gameplay;
- water_stun: extinguish/hiss.

Codex должен проверить реальные Forge/MCP 1.16.5 SoundEvents names.

Не использовать один sound для всего.

---

# 21. FIRE SERPENT — LOOT

Loot table only.

Guaranteed:
- `pylayuschaya_cheshuya`: `3–6`.

Chance:
- `ognennoe_pero`: `35%`.
- `pustoy_ritualny_sosud` from 0.9.0: `10%`.

Looting:
- scale quantity may gain normal looting bonus capped reasonably;
- Feather chance may increase modestly, max ~50%;
- Ritual Vessel chance NOT boosted heavily.

---

# 22. ПОДВЕЙ — РОЛЬ

Podvey — mini-boss контроля пространства.

Главные идеи:
- вихрь;
- pull/push;
- смена позиции;
- отбрасывание;
- временная защита от ranged;
- открытые пространства.

Он НЕ:
- flying skeleton;
- tornado particle with no body;
- Evoker recolor.

---

# 23. PODVEY — MODEL DESIGN

References:
- `REF_MOB_PODVEY_HERO.png`
- FRONT/SIDE/BACK/DETAILS.

## 23.1 Размер
Visual height:
`2.5–2.7 blocks`.

Hitbox:
- width `1.1`
- height `2.55`.

Нижняя tornado visual выходит за hitbox, но не должна мешать попаданиям.

## 23.2 Верхняя часть
Head/mask:
- вытянутая бело-серая маска;
- угловатые «рога»/ветровые отростки назад;
- узкие ярко-голубые глаза;
- не skull face.

Torso:
- тёмное ядро;
- рёбероподобные бело-серые wind bands;
- порванные тёмно-красные тканевые ленты.

Arms:
- длинные;
- dark forearms;
- claw-like black hands.

## 23.3 Нижняя часть
Нет нормальных ног.

Ниже torso:
- несколько спиральных wind bands;
- сужение;
- маленький tornado base.

Visual debris:
- листья;
- маленькие wood/stone particles;
- snow pieces in snowy biomes.

Debris лучше particles, а не десятки model cubes.

## 23.4 Palette
- wind white `#D7DEE2`
- cold gray `#9BA8B1`
- blue gray `#657583`
- dark core `#292C31`
- deep charcoal `#1E2125`
- eye cyan `#8DE5FF`
- cloth dark red `#6B2529`
- cloth highlight `#934047`
- wood debris `#6B4A32`

## 23.5 Texture
512×512 preferred.
Emissive only eyes if supported.

---

# 24. PODVEY — STATS

- MAX_HEALTH = `160`
- ATTACK_DAMAGE = `7`
- MOVEMENT_SPEED = `0.29`
- ARMOR = `4`
- KNOCKBACK_RESISTANCE = `0.75`
- FOLLOW_RANGE = `38`

Podvey hovers:
- 0.2–0.6 block visual hover;
- still navigates ground-like unless project has safe flying navigation.

Не должен улетать в небо.

XP:
55–70.

---

# 25. PODVEY — AI STATES

1. `HUNT`
2. `STRAFE`
3. `VORTEX_CHARGE`
4. `VORTEX_ACTIVE`
5. `GUST_CHARGE`
6. `GUST_RELEASE`
7. `BROKEN_PATH_DASH`
8. `RECOVERY`
9. `RETURN_TO_ANCHOR`

Target memory:
`220 ticks`.

Path failure:
- strafe/choose another point;
- no indefinite standing.

---

# 26. PODVEY — BASIC CLAW

Range:
2.5.

Damage:
7.

Windup:
8 ticks.

Interval:
20 ticks.

Visual:
one long claw arcs across.

Purpose:
fallback only.

---

# 27. PODVEY — ABILITY 1: ВОРОНКА

Signature ability.

Cooldown:
`160 ticks`.

## Charge
Duration:
`20 ticks`.

Telegraph:
- wind bands spread outward;
- debris begins rotating;
- cyan eyes brighten;
- audible rising wind.

## Active
Duration:
`60 ticks`.

Radius:
`7 blocks`.

Pull:
- outer 7–5 blocks: weak;
- 5–3: medium;
- <3: strong.

Do NOT instantly teleport player.
Use gradual horizontal velocity vector.

Center:
if player reaches <1.5:
- damage `6`;
- launch upward ~0.5;
- push outward after hit;
- per-player center-hit cooldown `30 ticks`.

Player can escape by:
- sprinting away;
- using terrain;
- mobility item;
- timing movement.

## Ranged deflection
ONLY while VORTEX_ACTIVE:
- ordinary arrows/projectiles entering ~5 block radius are redirected sideways;
- not reflected directly back;
- projectiles owned by Podvey ignored;
- do not affect Ender Pearls if implementation risks breaking player movement;
- boss/special projectiles can be excluded.

Do not grant permanent projectile immunity.

## Recovery
After vortex:
`24 ticks`.
Ranged vulnerability returns immediately.

---

# 28. PODVEY — ABILITY 2: РЕЖУЩИЙ ПОРЫВ

Cooldown:
`110 ticks`.

Range:
8 blocks.

Cone:
~65 degrees.

Telegraph:
`14 ticks`.

Visual:
two-three curved white/gray particle streaks.

Hit:
- damage `4`;
- horizontal knockback strong ~1.25;
- slight vertical 0.15.

Shield:
- reduces damage normally;
- reduces knockback by about 45%, not 100%.

Line-of-sight:
required.
Do not hit through full stone walls.

---

# 29. PODVEY — ABILITY 3: ЛОМАНАЯ ТРОПА

Cooldown:
`120 ticks`.

Use range:
4–10.

Telegraph:
`10 ticks`.

Podvey rapidly dashes THROUGH/PAST player's current vector.

Dash:
up to 7 blocks.
No teleport.

If intersects:
- damage `6`;
- Weakness I `60 ticks`;
- Slowness I `40 ticks`;
- small sideways knockback.

One damage instance per dash.

If misses:
recovery 16 ticks.

---

# 30. PODVEY — STRAFE BEHAVIOR

Between abilities:
- avoids standing directly in front of player;
- choose side position 4–7 blocks;
- strafe every 30–60 ticks if useful;
- no endless circular orbit.

If player gets close:
- basic claw OR dash opportunity.

If player kites:
- Broken Path priority increases.

---

# 31. PODVEY — ENVIRONMENT / WEATHER

Podvey prefers open terrain but Hunt Horn may summon without requiring storm.

## Natural spawn bonus
Natural encounter chance increases during:
- rain;
- thunder;
- snow weather where applicable.

## Thunder
During thunder:
- visual debris density +20%;
- NO damage/HP buff.

## Snow biome
Use additional snow particles.
Stats unchanged.

## Water
Podvey is not damaged by water.
If submerged deeply:
- AI should leave water;
- abilities requiring open wind disabled underwater.

---

# 32. PODVEY — NATURAL SPAWN

Conditions:
- Overworld;
- open sky;
- night OR bad weather;
- not dense cave;
- preferred:
  - plains;
  - snowy plains;
  - hills/mountains/open terrain;
- max 1 Podvey within 96 blocks.

Very rare.

---

# 33. HUNTING HORN — PODVEY RULES

Target:
`PODVEY`.

Requirements:
- Overworld;
- open sky;
- player standing in open-enough area;
- allowed biome/open terrain;
- no active owned Hunt Encounter;
- cooldown ready.

Horn should NOT require active rain/thunder.

Safe spawn:
- 14–26 blocks away;
- solid ground;
- 4 blocks vertical air;
- not water/lava.

Failure keys:
- wrong biome/open terrain;
- no open sky;
- no safe position;
- active encounter;
- cooldown.

---

# 34. PODVEY — ANIMATIONS

Minimum:

1. `idle_hover`
   - wind bands slowly rotate;
   - cloth moves;
   - hands slight movement.

2. `move`
   - body leans;
   - lower vortex stretches.

3. `claw`
   - shoulder/forearm sweep.

4. `vortex_charge`
   - arms spread;
   - torso lifts;
   - wind bands widen.

5. `vortex_active`
   - fast lower-body rotation;
   - arms open;
   - head stable.

6. `gust_charge`
   - one arm pulls back;
   - wind compresses near hand/chest.

7. `gust_release`
   - arm thrust.

8. `broken_path_dash`
   - torso leans sharply;
   - lower vortex narrows.

9. `recovery`
   - vortex slows briefly.

10. `death`
   - body bands separate;
   - wind collapses;
   - debris drops/fades.

---

# 35. PODVEY — SOUNDS

Semantic:
- ambient
- hurt
- death
- claw
- vortex_charge
- vortex_active
- gust
- dash
- recovery

Fallback intent:
- ambient: low wind/cave-like ambience locally, subtle;
- hurt: airy hiss + spectral cue;
- death: long descending wind;
- vortex: elytra/wind-like layered whoosh if available;
- gust: strong sweep;
- dash: short sharp wind.

Do NOT play ambient every second.
Use normal randomized mob ambient timing.

Check actual 1.16.5 SoundEvents.

---

# 36. PODVEY — LOOT

Guaranteed:
- `uzel_podveya`: `1–2`.

Chance:
- `vihrevaya_nit`: `35%`.
- `pustoy_ritualny_sosud`: `10%`.

Loot table only.

---

# 37. ITEM — ПЫЛАЮЩАЯ ЧЕШУЯ

ID:
`pylayuschaya_cheshuya`

Reference:
`REF_ITEM_01_PYLAYUSCHAYA_CHESHUYA.png`

Texture:
256×256 minimum.

Stack:
32.

## Design
Большая расколотая чешуя.

Silhouette:
- вытянутый ромб/лист;
- неровные зазубренные края;
- несколько слоёв чёрных пластин;
- по центру глубокие огненные cracks.

Palette:
- black `#251A1A`
- dark red `#6E1B17`
- ember red `#B82717`
- orange `#F45115`
- yellow `#FFB52B`

Не делать плоской красной чешуйкой.

## Function
- trophy;
- ingredient Falling Star Charm;
- ingredient Fire Breath Vessel;
- tag `hunt_trophies`.

Tooltip:
`Чешуя всё ещё хранит жар полёта.`

---

# 38. ITEM — ОГНЕННОЕ ПЕРО

ID:
`ognennoe_pero`

Reference:
`REF_ITEM_02_OGNENNOE_PERO.png`

Texture:
512×512 preferred.

Stack:
16.

Rarity:
UNCOMMON.

## Design
Это условное «перо» — вытянутая огненная пластина/след Змея.

Mandatory:
- яркий центральный fiery shaft;
- 7–10 тёмных лопастей;
- края почти чёрные;
- между ними огненные gaps;
- tip black/red.

Not bird feather realism.

Palette:
- shaft yellow `#FFC43A`
- orange `#F55A16`
- red `#B92518`
- black `#24181A`

## Function
- rare trophy;
- Falling Star Charm;
- Fire Breath Vessel.

Tooltip:
`Пламя держит форму даже после смерти Змея.`

---

# 39. ITEM — ОБЕРЕГ ПАДАЮЩЕЙ ЗВЕЗДЫ

ID:
`obereg_padayuschey_zvezdy`

Reference:
`REF_ITEM_03_OBEREG_PADAYUSCHEY_ZVEZDY.png`

Texture:
512×512.

Stack:
1.

Rarity:
RARE.

## Design
Dark stone/metal diamond-shaped charm.

Mandatory:
- irregular black diamond body;
- central orange starburst/crack;
- red braided cord loop;
- small brass/iron beads;
- red tassel;
- lower pointed stone.

Colors:
- body `#282323`
- edge `#3E3331`
- crack red `#A4251A`
- crack orange `#F35A17`
- hot center `#FFC13A`
- cord `#8C1E25`
- brass `#9B6B31`

## Recipe
Shaped:
`S I S`
`. F .`
`S T S`

Where:
- S = `pylayuschaya_cheshuya`
- I = iron_nugget
- F = `ognennoe_pero`
- T = string

## Equip
Use existing amulet/accessory slot if available.
Fallback:
OFFHAND only.
No passive inventory effect.

## Mechanics
Automatic emergency fall protection.

Cooldown:
`1200 ticks` = 60 sec.

Activation conditions:
- properly equipped;
- cooldown ready;
- player is falling;
- `fallDistance >= 5.5`;
- vertical velocity clearly downward.

On activation:
- apply Slow Falling for `100 ticks` (5 sec);
- reset/limit accumulated fall distance so vanilla landing does not still apply pre-activation full fall damage;
- play short fire-star sound;
- 6–10 ember particles around player;
- set cooldown.

Must NOT:
- activate on normal jumps;
- activate while Elytra-like flight if incompatible;
- trigger every tick;
- provide permanent slow falling.

Tooltip:
`Спасает от опасного падения.`
`Перезарядка: 60 сек.`

---

# 40. ITEM — УЗЕЛ ПОДВЕЯ

ID:
`uzel_podveya`

Reference:
`REF_ITEM_04_UZEL_PODVEYA.png`

Texture:
256×256.

Stack:
16.

Rarity:
UNCOMMON.

## Design
Dense wind knot.

Components:
- 4–6 thick rope loops;
- bone/gray-white cord;
- dark blue-gray loops;
- tiny beads/metal;
- center contains visible small spiral of pale wind.

Colors:
- rope `#C5BBAA`
- cold rope `#9DAAB4`
- dark blue `#41536A`
- center `#C9EDFF`
- iron `#4C5055`

Not magic glowing orb with string pasted around it.

## Function
- trophy;
- Wind Knot recipe;
- Podvey Vessel recipe.

Tooltip:
`Узел затянут ветром, а не руками.`

---

# 41. ITEM — ВИХРЕВАЯ НИТЬ

ID:
`vihrevaya_nit`

Reference:
`REF_ITEM_05_VIHREVAYA_NIT.png`

Texture:
256×256.

Stack:
32.

## Design
Thin flowing strip/string of wind.

Icon:
- 2–3 curved interwoven ribbons;
- pale gray center;
- blue shadows;
- frayed ends;
- small particles.

Do NOT make a literal white Minecraft String recolor.

Palette:
- white `#DCE4E7`
- pale blue `#B8CFDB`
- mid blue gray `#748C9D`
- shadow `#4B5D6C`

## Function
- rare trophy;
- Wind Knot;
- Podvey Vessel.

Tooltip:
`Нить дрожит даже в закрытом сундуке.`

---

# 42. ITEM — ВЕТРОВОЙ УЗЕЛ

ID:
`vetrovoy_uzel`

Reference:
`REF_ITEM_06_VETROVOY_UZEL.png`

Texture:
512×512.

Stack:
1.

Rarity:
RARE.

## Design
Practical talisman made from:
- curved bone/fang-like white piece;
- leather wrapping;
- central small wind spiral;
- cords;
- beads;
- metal seal;
- tassels.

This is NOT necklace jewelry.
It looks like rough hunting equipment.

Palette:
- bone `#D8C7A7`
- leather `#5E3B2A`
- wind `#C7E7F2`
- blue gray `#658096`
- iron `#45494D`
- red accent `#8E2427`

## Recipe
Shaped:
`T U T`
`L B L`
`. S .`

Where:
- T = `vihrevaya_nit`
- U = `uzel_podveya`
- L = leather
- B = bone
- S = string

## Function
Active utility item.

Right click:
- cooldown `500 ticks` = 25 sec;
- emits radial wind pulse radius `4 blocks`;
- no direct damage.

Effects:
### normal hostile mobs
horizontal push ~1.1.

### elite mobs / mini-bosses
push multiplier ~0.35.

### full bosses
push multiplier max 0.15 or immunity if existing boss framework requires.

Players:
- do NOT push other players by default to avoid grief/PvP complexity.

Projectiles:
- no global projectile reflection in 0.9.1.

Sound:
short wind burst.

Particles:
ring of white/blue wind particles, 20–40 max.

Tooltip:
`Отбрасывает обычных врагов порывом ветра.`
`Перезарядка: 25 сек.`

---

# 43. ITEM — СОСУД ОГНЕННОГО ДЫХАНИЯ

ID:
`sosud_ognennogo_dyhaniya`

Reference:
`REF_ITEM_07_SOSUD_OGNENNOGO_DYHANIYA.png`

Texture:
512×512.

Stack:
8.

Rarity:
RARE.

## Design
Основа — чёрный ритуальный сосуд 0.9.0.

Changes:
- seams/cracks glow red/orange;
- central bright red/orange geometric sign;
- black stopper;
- red cords;
- small fang/bone charms;
- faint smoke/ember visual in icon.

Palette:
- ceramic `#211F20`
- iron ring `#3F3E40`
- red `#A62418`
- orange `#F45B16`
- hot `#FFC23B`
- cord `#821D20`

## Recipe
Shapeless:
- `pustoy_ritualny_sosud`
- `pylayuschaya_cheshuya`
- `pylayuschaya_cheshuya`
- `ognennoe_pero`

## 0.9.1 function
INTENTIONALLY no right-click ability.

It is a prepared ritual component for later 0.9.x.

Tooltip:
`Внутри заперт жар летающего змея.`

No fake placeholder mechanic.

Tag:
`hunt_trophies`.

---

# 44. ITEM — СОСУД ПОДВЕЯ

ID:
`sosud_podveya`

Reference:
`REF_ITEM_08_SOSUD_PODVEYA.png`

Texture:
512×512.

Stack:
8.

Rarity:
RARE.

## Design
Black/dark-gray ritual vessel.

Mandatory:
- visible circular pale-blue wind spiral in front/inside;
- gray metal bands;
- dark stopper;
- blue-gray cords;
- bone tassels;
- small tag/seal.

Palette:
- vessel `#26272B`
- metal `#4B5056`
- wind light `#C8EEFF`
- wind blue `#78AFC8`
- shadow blue `#416174`
- cord `#344C63`
- bone `#D2C19F`

## Recipe
Shapeless:
- `pustoy_ritualny_sosud`
- `uzel_podveya`
- `vihrevaya_nit`
- `vihrevaya_nit`

## 0.9.1 function
No right-click ability yet.

Tooltip:
`Даже запечатанный ветер ищет выход.`

Tag:
`hunt_trophies`.

---

# 45. TROPHY POUCH EXTENSION

Do NOT change 9-slot count.

Extend `slavicmyths:hunt_trophies` with:
- pylayuschaya_cheshuya
- ognennoe_pero
- uzel_podveya
- vihrevaya_nit
- sosud_ognennogo_dyhaniya
- sosud_podveya

Falling Star Charm and Wind Knot are EQUIPMENT/TOOLS and do NOT need to be trophy-tag items unless current design convention says all hunt rewards are accepted.

Preferred:
do NOT put finished gear into trophy tag.

---

# 46. HUNTER CHARM EXTENSION

No new scan logic.

Existing item must find:
- Ovinnik
- Volkolak
- Fire Serpent
- Podvey

through `hunt_targets` tag/helper.

If Fire Serpent is airborne:
distance still uses entity position.

Do not permanently glow targets outside existing short-range rule.

---

# 47. TAGS

Extend/create:

## Entity tag/equivalent
`slavicmyths:hunt_targets`
- ovinnik
- volkolak
- fire_serpent
- podvey

## Item tag
`slavicmyths:hunt_trophies`
Existing 0.9.0 entries +
new trophy/vessel entries.

Do not hardcode class checks throughout UI/item logic.

---

# 48. ADVANCEMENTS

Add 4 maximum.

## `fire_in_the_sky`
RU:
`Огонь в небе`

Trigger:
kill Fire Serpent.

## `catch_the_wind`
RU:
`Поймать ветер`

Trigger:
kill Podvey.

## `falling_star`
RU:
`Падающая звезда`

Trigger:
Falling Star Charm activates successfully.

## `bottled_elements`
RU:
`Запечатанные стихии`

Trigger:
obtain both:
- Fire Breath Vessel
- Podvey Vessel

If two-item criterion is expensive to implement in current advancement pattern:
trigger on obtaining either vessel and use a simpler name.
Prefer full criterion if straightforward.

---

# 49. JEI

Crafting recipes shown normally:
- Falling Star Charm
- Wind Knot
- Fire Breath Vessel
- Podvey Vessel

Loot-only:
- Burning Scale
- Fire Feather
- Podvey Knot
- Whirlwind Thread

No fake recipes.

---

# 50. LOCALIZATION

Add RU/EN:
- entities;
- spawn eggs;
- 8 items;
- tooltips;
- Hunt Horn selected target names;
- error messages;
- advancements;
- relevant death/encounter messages only if existing system uses them.

No hardcoded user-facing strings if current project uses lang keys.

---

# 51. SOUNDS.JSON / RESOURCE RULE

If mod already contains custom OGG pipeline:
Codex may add semantic sound events referencing existing suitable custom files.

If no audio assets are supplied:
do NOT fabricate binary audio.
Use validated vanilla sounds in code.

README must state whether 0.9.1 uses:
- custom sounds;
or
- vanilla fallback sounds.

---

# 52. DEBUG / COMMANDS

At minimum document:

`/summon slavicmyths:fire_serpent`

`/summon slavicmyths:podvey`

`/give @s slavicmyths:fire_serpent_spawn_egg`

`/give @s slavicmyths:podvey_spawn_egg`

`/give @s slavicmyths:ognennoe_pero`

`/give @s slavicmyths:obereg_padayuschey_zvezdy`

`/give @s slavicmyths:vetrovoy_uzel`

`/give @s slavicmyths:sosud_ognennogo_dyhaniya`

`/give @s slavicmyths:sosud_podveya`

If 0.9.0 already added:
`/slavicmyths dev hunt summon <target>`

extend accepted targets:
- fire_serpent
- podvey

Do not create second debug root.

---

# 53. NATURAL SPAWN PERFORMANCE

No world-wide recurring scans.

Natural encounter must reuse 0.9.0 approach.

Limits:
- rare;
- local;
- one mini-boss per wide area;
- server-safe.

Do not scan all players every tick for weather+biome and manually spawn bosses.

---

# 54. COMMON COMBAT QUALITY RULES

Both new mini-bosses:
- remember target;
- re-path/reposition;
- no 3-second dead idle periods;
- ability telegraphs are visible;
- heavy attacks have punish windows;
- no unavoidable instant hit;
- no terrain grief;
- no attacks through full walls;
- no infinite summons;
- no vanilla mob reskin behavior.

---

# 55. README UPDATE

Create/update section:

`0.9.1 — Охота на нечисть II`

Must include:
- Fire Serpent description;
- Podvey description;
- exact HP;
- abilities;
- water/rain Fire Serpent behavior;
- Podvey vortex/projectile deflection;
- natural encounter locations;
- Hunting Horn conditions;
- spawn eggs;
- drops;
- every new item;
- exact recipes;
- which vessels are intentionally future-use;
- commands;
- manual test checklist.

Also update 0.9.x roadmap:
- 0.9.2 = Лихо + Тугарин;
- 0.9.3 = Баба-яга;
- do not implement them now.

---

# 56. MANUAL TEST CHECKLIST

README must include at least:

## Fire Serpent
1. spawn egg works;
2. `/summon` works;
3. natural spawn constraints checked;
4. Horn day failure;
5. Horn no-open-sky failure;
6. Horn successful open-sky summon;
7. entity does not fly away infinitely;
8. orbit behavior;
9. bite;
10. Fire Dash telegraph;
11. Fire Dash hit;
12. Fire Dash miss/recovery;
13. trail does not place fire blocks;
14. Star Dive zone visible;
15. Star Dive miss punish window;
16. Decoys appear/disappear;
17. water damage/stun;
18. rain nerf;
19. loot quantities.

## Podvey
20. egg and summon;
21. Horn open-terrain summon;
22. Vortex pull;
23. center launch;
24. arrow deflection during vortex;
25. arrows work outside vortex;
26. Gust knockback;
27. shield reduces gust effect;
28. Broken Path debuffs;
29. environment particles;
30. loot.

## Items
31. craft Falling Star Charm;
32. normal jump does not trigger it;
33. dangerous fall triggers it once;
34. 60-sec cooldown;
35. craft Wind Knot;
36. normal mob push;
37. mini-boss reduced push;
38. boss no-cheese behavior;
39. craft Fire Vessel;
40. craft Podvey Vessel;
41. vessels have no active effect;
42. Pouch accepts new trophies;
43. Hunter Charm finds both new targets;
44. Horn cycles all 4 targets.

---

# 57. BUILD / POLYMC

After implementation:
1. do NOT launch Minecraft;
2. headless build;
3. fix compile/resource issues caused by patch;
4. build JAR;
5. copy current JAR to known PolyMC mods path;
6. remove/replace conflicting prior Slavic Myths test JAR if project workflow already does that;
7. if PolyMC path unknown, report JAR output path and do not guess.

---

# 58. LOW-TOKEN IMPLEMENTATION ORDER

1. Find 0.9.0 Hunt target enum/tag and encounter manager.
2. Extend targets with Fire Serpent + Podvey.
3. Register new items.
4. Extend tags.
5. Register entities + spawn eggs.
6. Build model/renderer/resources from references.
7. Fire Serpent AI.
8. Podvey AI.
9. Horn conditions.
10. Hunter Charm extension.
11. Pouch tag extension.
12. Item mechanics.
13. Loot.
14. Recipes/JEI.
15. Advancements.
16. Localization.
17. README.
18. Headless build.
19. PolyMC copy.

Do not wander into unrelated code.

---

# 59. DEFINITION OF DONE

## Fire Serpent
- [ ] entity registered;
- [ ] spawn egg;
- [ ] Minecraft-style unique model;
- [ ] >=256 texture, 512 preferred;
- [ ] 155 HP;
- [ ] controlled flight;
- [ ] orbit;
- [ ] bite;
- [ ] Fire Dash;
- [ ] temporary trail;
- [ ] Star Dive;
- [ ] Decoys;
- [ ] water stun/damage;
- [ ] rain debuff;
- [ ] anti-cheese fallback;
- [ ] sounds;
- [ ] animations;
- [ ] loot;
- [ ] natural encounter;
- [ ] Hunting Horn summon.

## Podvey
- [ ] entity registered;
- [ ] spawn egg;
- [ ] unique tornado/humanoid model;
- [ ] >=256 texture, 512 preferred;
- [ ] 160 HP;
- [ ] active movement;
- [ ] claw;
- [ ] Vortex;
- [ ] gradual pull;
- [ ] projectile deflection only during Vortex;
- [ ] Gust;
- [ ] Broken Path;
- [ ] weather particles;
- [ ] sounds;
- [ ] animations;
- [ ] loot;
- [ ] natural encounter;
- [ ] Hunting Horn summon.

## Items
- [ ] 8 new items;
- [ ] high-resolution reference-faithful assets;
- [ ] exact recipes;
- [ ] Falling Star Charm works;
- [ ] Wind Knot works;
- [ ] both vessels intentionally future-use;
- [ ] Pouch accepts trophies;
- [ ] Hunter Charm finds new targets.

## Integration
- [ ] Horn cycles 4 hunt targets;
- [ ] no parallel Hunt system;
- [ ] tags used;
- [ ] JEI;
- [ ] advancements;
- [ ] README;
- [ ] commands documented;
- [ ] build passes;
- [ ] Minecraft client not auto-launched.

---

# 60. FINAL CODEX RESPONSE FORMAT

Return only:

## Реализовано
Short factual bullets.

## Мини-боссы
IDs, HP, actual abilities.

## Предметы
IDs and actual functions.

## Команды
Exact working commands.

## Основные файлы
Paths.

## Build
Result and JAR path.

## PolyMC
Copied path or unknown.

## Не выполнено
Only genuinely incomplete requirements.

Do NOT claim implementation that is not present in files.
