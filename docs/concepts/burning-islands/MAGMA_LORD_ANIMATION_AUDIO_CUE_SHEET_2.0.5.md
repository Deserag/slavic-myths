# Повелитель магмы 2.0.5 — покадровое расписание анимаций, звуков и телеграфов

> **Статус:** исполнительские ориентиры для #318 (3D анимации), #319 (боевой AI), #321 (голова-трофей) и #322 (аудио). **Секунды/тики ниже инженерные предложения**, в реальном бою подлежат проверке и возможной балансировке владельцем. **Звуковых OGG-файлов пока НЕТ в репозитории** — не выдавать эту таблицу за готовое audio. Геймплей на Minecraft 1.21.1/NeoForge 21.1.255/Java21 с текущим AGENTS.md без запуска Minecraft. Сохранять branch hell.

## Визуальный каркас

- [Открываемый 3D-конструктивный SVG трёх ракурсов босса](references/MAGMA_LORD_ORTHOGRAPHIC_DESIGN_GUIDE.svg).
- [Открываемый SVG головы: поставленная модель + GUI](references/MAGMA_LORD_TROPHY_HEAD_MODEL_GUI_GUIDE.svg).
- [Реестр оригинальных PNG и их SHA](references/README.md): оригинальные PNG пока не импортированы; физически присутствуют только SVG-схемы. Обязательно получить ZIP из текущей беседы, извлечь в `hell/docs/concepts/burning-islands/references`, SHA check, затем Native ModelPart/UV сравнение.

## Лист игровых анимаций босса

| Clip / фаза | Windup кадры | Damage кадры | Recovery кадры | Визуальное предупреждение | Звук на событие |
| --- | --- | --- | --- | --- | --- |
| `neutral_idle` P1 | цикл ~80–120 тиков | нет | нет | мягкий парящий idle, стягивание магмовых осколков к ядру | `magma_lord_idle_rumble`, редко, не каждый цикл |
| `on_attacked_awaken` | 0–35: поворот, шипы/конечности открываются | нет до конца анимации | 35–45 вход в combat | ярко пульсирует ядро, слышен первый рёв | `magma_lord_aggro_roar` |
| `claws_swing` P1/P2 | 0–22: когти разводятся, каменный треск | 23: передняя дуга **14 HP** | 24–40 | чёткий взмах на ~1.1с, уклонение в сторону | `magma_lord_claws_prepare` → `magma_lord_claws_impact` |
| `fracture` P1/P2 | 0–26: три безопасно различимые линии на земле | 27: 3 направленных разлома **18 HP** | 28–45 | линии видны минимум ~1.3с | `magma_lord_fracture_charge` → `magma_lord_fracture_release` |
| `shadow_pull` P1/P2 | 0–26: конечности тянутся, пульс границы зоны | 27–34: притяжение **0 прямого HP** | 35–52 | возможность выйти из зоны, не притягивает в лаву без шанса | `magma_lord_pull_start` → `magma_lord_pull_end` |
| `inferno_hands` P2 | 0–24: одна дальняя рука подсвечивается | 25: направленный удар **16 HP** | 26–46 | нельзя атаковать без видимого предупреждения | `magma_lord_hands_prepare` → `magma_lord_hands_strike` |
| `shadow_eruption` P2 | 0–30: последовательно маркируются три зоны | 31/39/47: отдельные импульсы, **18 HP за успешный damage event** | 48–67 | зоны не пересекаются так, чтобы не было ни одной safe zone; иммунитет к нечестному chain-hit | `magma_lord_shadow_eruption_charge` → distinct `magma_lord_shadow_eruption_release` |
| `eruption_ring` P2 | 0–36: грудное ядро широко раскрывается | 37: растущее кольцо, **20 HP** | 38–68 | яркий expanding ring, возможность безопасно переместиться | `magma_lord_ring_charge` → `magma_lord_ring_impact` |
| `core_expose` P2 | после тяжёлой атаки: створки расходятся | ~60 тиков дополнительной уязвимости (ориентир +35%) | створки закрываются плавно | явно видна слабая точка, не permanent invulnerability/weakness | `magma_lord_core_exposed` → `magma_lord_core_close` |
| `phase2_transition` | 0–20: затихание, каменные пластины дрожат | 21–55: физический разлом модели, **не повреждает игрока без отдельного телеграфа** | 56–70: P2 ready | ядро открывается; новый боевой набор только после перехода | `magma_lord_phase_break` → `magma_lord_phase_roar` |
| `hurt` | <1–4 тика | нет отдельного retaliation | 4–10 | recoil только геометрии | `magma_lord_hurt` |
| `death` | 0–25: замедление/затухание ядра | нет | 25–80: каменные сегменты опадают, затем выдача лута | без permanent lava/terrain changes | `magma_lord_death` |

**Правила:** кадры не складывают невидимый дополнительный урон поверх таблицы; реальный hitbox+MobEffect no blind AoE; frame-to-sound-to-damage contract server authoritative, клиент только renderer/частицы. При Hard сильные удары способны убить железный набор без чар — обязательно dodge/избегаемый навык, 1.5× raw до брони, нельзя каскадно применять импульсы с обманом invulnerability. Не блокировать все переходы разом.

## Лист trophy head (последний оригинальный PNG, одобрен владельцем)

| State | Trigger | ModelPart действие | Звук/VFX |
| --- | --- | --- | --- |
| `head_idle` | установленная и видимая, вне ближнего радиуса | медленный тлеющий glow ~80–100 тиков; отдельные осколки медленно покачиваются | очень редкое тихое `magma_lord_head_idle`, не compulsory |
| `head_wake` | первый реальный игрок в пределах **6 блоков** | jaw pivot раскрывается **12–20° за ~12–20 тиков**, глаза/трещины усиливаются; никаких full head yaw к игроку | **однократно** `magma_lord_head_jaw_crack` + `magma_lord_head_wake_exhale`; 1–2 мягких particles |
| `head_awake_hold` | рядом игрок, 6–8 блоков hysteresis | пасть слегка открыта, редкие колебания жара, shards; без перерегистрации BlockState | cooldown/no audio spam, 1–2 particles за 8–12 сек |
| `head_calm` | игроков не осталось в пределах **8 блоков** | jaw closes за ~20–40 тиков, гаснет local emissive (не actual blocklight) | без постоянного шипения |
| `head_place` / `head_break` | отдельное реальное взаимодействие | блок с нормальной facing/shape | `magma_lord_head_place` / `magma_lord_head_break` по одному разу |
| `head_item_gui` | предмет в слоте/creative | **полностью статичная** читаемая иконка | без idle animation, FX или звуков |

**Отсутствие эффектов:** декоративная голова НИКОГДА не наносит damage, не поджигает мир/игрока, не меняет блоки, не заставляет NPC агриться; не сканирует серверный мир каждый тик. Клиентский bounded view-distance+visible-only renderer, ограниченный particle budget. Реальные звуки OGG и перевод subtitles обязательны.

## Звуковые файлы / лицензии / отчёт
Для каждого события из таблиц указать точный путь к реально существующему `assets/slavicmyths/sounds/**.ogg`, `sounds.json`, `subtitle` ru_ru/en_us, конкретную причину playback, результат owner audio acceptance, материал источника/licensing. **НЕЛЬЗЯ** маркировать PASS если sound event просто зарегистрирован, а ogg отсутствует/пустой, если все события — копия одного шума с pitch, либо synthetic oscillator filler. Честный статус `BLOCKED_NEEDS_AUDIO_SOURCE` до получения пригодных записей; не использовать чужое коммерческое game audio без прав.

## Негативные тесты
- Два игрока одновременно слушают разные навыки, listener facing, несколько голов в базе; все новые звуки не перекрывают важные телеграфы.
- Save/relog в середине перехода P1→P2, во время уязвимости, когда босс потерял target; нет повторного нанесения 18/20 HP и лишней смерти.
- Lava shore/bridge/roof, боковой подход; фазы не обнуляют HP и не рушат натуральную арену.
- Голова: поставить 10 штук, выйти/войти на 5.9/6.1/7.9/8.1, два клиента, поворот, чанковая выгрузка, не создаётся постоянный фоновой sound.
- Если GameTest/Minecraft/PolyMC/server запрещены `hell/AGENTS.md` — реальные runtime проверки `NOT_RUN`. Нельзя подделывать доказательства.

**Связанные задачи:** [#318](https://github.com/Deserag/slavic-myths/issues/318), [#319](https://github.com/Deserag/slavic-myths/issues/319), [#321](https://github.com/Deserag/slavic-myths/issues/321), [#322](https://github.com/Deserag/slavic-myths/issues/322), [#323](https://github.com/Deserag/slavic-myths/issues/323).
