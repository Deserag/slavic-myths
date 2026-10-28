# Slavic Myths — Исполнительский контракт оружия Ада 2.0.x · 28.10.2026

**Ветка:** `hell`; не менять `main`, `release`, миры или PolyMC без прямого отдельного разрешения. **Контракт мастер-задачи #325**, ресурсы #324, универсальный шаблон кузнечной закалки и броня #326, баланс существ #328. Это подготовленное **рабочее ТЗ** для двух исполнительских задач: MODEL/ART и GAMEPLAY/LOOT. Не объявлять выпуск завершённым до игровой приёмки владельца. Версия 2.0.x уточняется по дорожной карте, 2.0.6/2.0.7 заняты.

## 0. ЗАКРЕПЛЁННЫЕ РЕФЕРЕНСЫ И ДОСТАВКА

Новый исходный целый референс оружия от 28.10.2026: `NETHER_WEAPONS_REFERENCE_FULL_APPROVAL_CANDIDATE.png`, 1672×941, SHA256 **ccbd535c3482b5ef371b8c2678f66790d10577bdb104796daf2135e90f977ef0**.

Отдельные неизменённые crops одного и того же оригинала:
- `01_navya_ostroga.png` SHA256 `c492bf1c42523d4b2f0e19c41a3afd3f8cfe214115c449b77a28a76f5195373b`
- `02_ashen_greatsword.png` SHA256 `1d46dd3cfaabe9ed60dacca6021901ff83c7477ff9c4c43ddbb4f6f679ae14ba`
- `03_shadow_knife.png` SHA256 `9b7613512a5091c14bbec9387f7f737e59882716c083837cd14b8f1cb00d725a`
- `04_volcanic_flail.png` SHA256 `adb160a83f0aee941a5c00553f51d273841d651ec45b535b6f3113133022b7cd`
- `05_infernal_bow.png` SHA256 `2e5f09b3f833721c165ce56f5993063dc04b6b7b5d5223e8619727168deae6f4`

Архив с оригиналом, 5 кропами, README и MANIFEST: `slavic_myths_nether_weapons_reference_pack_v2.zip`, SHA256 **7dfff7dcaea82bcfea62d8edbb99ae10315599ed341f56278ce6c1c9f8ffaed8**. Оригиналы физически находятся в текущем диалоге ChatGPT. **Не утверждать, будто GitHub имеет эти PNG: binary ещё НЕ опубликованы в репозиторий, GitHub-текстовый API их не переносит.** Перед реальной модельной работой получить те же самые байты в `hell/docs/concepts/nether-weapons/approved_originals/`, SHA сверить, добавить файлы в Git. Если Codex не имеет доступа к байтам — `ART_BLOCKED_REFERENCE`, а не самовольная замена generic asset. Механика/тесты могут готовиться параллельно, но художественную часть не закрывать.

**Статус арта:** новые образы разработаны по разрешению владельца переработать дизайн в сторону древнерусских форм/славянского фольклора, но конкретный новый лист ещё **предложен на визуальное принятие**, не отмечать `OWNER_ART_ACCEPTED` до просмотра и согласования. Соответствие исходному листу обязательно после принятия.

**Историческая честность:** рогатина в исторической Руси — обычно одиночное широкое остриё; **двузубая острога Нави** — фантазийное боевое расширение реальной формы рыболовной остроги, а не засвидетельствованное мифологическое оружие. Кистень — действительно известный древнерусский тип с грузом на ремне/цепи. Меч-кладенец — сказочный образ, **этот меч не называется самим кладенцом**. Сложносоставной лук берёт общий тип деревянно-рогового рефлексного оружия Восточной Европы; никаких утверждений о «достоверных славянских рунах» на клинках. Геометрические знаки — авторская орнаментальная фантазия.

## 1. Инвентарь конкретных предметов и registry IDs

Все ID `slavicmyths:`, предварительно проверить collision against latest `hell`; не переименовывать старые `diamond_*`.

**Шесть обычных Жаровых upgrades** — простая перекраска форм исходных diamond предметов, оранжево-коричневая палитра как Жаровая броня:
| ID нового предмета | Основа | Комментарий |
|---|---|---|
| `zhara_sword` | `minecraft:diamond_sword` | обычный меч, светящиеся оранжевые прожилки |
| `zhara_axe` | `minecraft:diamond_axe` | стандартный боевой топор, не трогать ванильное рубление древесины |
| `zhara_spear` | `slavicmyths:diamond_spear` | обычное **одноконечное** копьё, не путать с Навьей острогой |
| `zhara_sulitsa` | `slavicmyths:diamond_sulitsa` | сохраняет исходную механику метания |
| `zhara_throwing_knife` | `slavicmyths:diamond_throwing_knife` | сохраняет состояние/восстановление projectile |
| `zhara_flail` | `slavicmyths:diamond_flail` | сохранить текущий заряженный hit-пайплайн кистеня |

Для каждого из 6: существующий **Шаблон адской закалки #326** + указанная алмазная основа + уже заданная **Жаровая пластина #326** → 1 предмет через кузнечный стол; изношенность/компоненты/текущие руны сохраняются. Общий баланс: **+15% к фактическому unenchanted direct damage по сравнению с точной базе для того же типа, округление вверх до 0.5 HP, +20% к vanilla/dimension durability**, при этом attack speed, reach, throwing behavior и charge time остаются как у diamond. Например diamond sword 7→8 HP, axe 9→10.5 HP. Другие параметры вычислять из **текущих фактических статов из кода**, не копировать чужие скрипты урона. Все шесть имеют 2 рунических гнезда (открываются по действующим правилам), не обладают уникальными активными навыками. Ремонт — Алатырский слиток. Крафт не затрагивает ремонт/заточку vanilla предметов.

**Пять особых upgrades** — отдельные модели/UV и способности:
| ID | Имя RU | diamond/source base | direct damage (без чар), attacks/sec | Durable target |
|---|---|---|---|---|
| `navya_ostroga` | Навья острога | `slavicmyths:diamond_spear` | **10 HP**, **1.15/s** | 1800 |
| `ashen_greatsword` | Пепельный двуручник | `minecraft:diamond_sword` | **15 HP**, **0.90/s**, при занятой левой руке 9 HP | 1950 |
| `shadow_throwing_knife` | Теневой нож | `slavicmyths:diamond_throwing_knife` | 6 HP melee, **8 HP thrown**, melee 2/s | 1650 |
| `volcanic_heart_flail` | Кистень сердца вулкана | `slavicmyths:diamond_flail` | **13 HP**, 0.90/s | 1800 |
| `infernal_bow` | Адский лук | `slavicmyths:heavy_bow` + 2 diamond в catalyst | **10 HP за прямое полное попадание** до чар/критов, полный натяг 24 ticks | 1200 |

У всех пяти **2 гнезда рун** через штатные `Runes`, отдельно открываемые. Реальный cooldown/attack speed и icon tooltip должны отражать именно HP после vanilla attack timing. Указывать current/target in QA table, не заявлять, что реализация точно соответствует модели DPS без нативного GameTest.

## 2. Визуальный дизайн пяти: описания по ModelParts/UV

### W01 «Навья острога»
**Обязательный silhouette:** длинное тёмное древко с резными переплетёнными корнями; **ОДИН рабочий конец, на нём ДВА отдельно выступающих параллельно-сходящихся, изогнутых вверх острых лезвия** с центральным U-образным отрицательным пространством. На другом конце только металлический противовес/колпачок, **НИКАКОГО лезвия**. Геометрия: `shaft`, `root_grips`, `bone_socket`, `left_tine`, `right_tine`, `turquoise_inlay`, `lower_pommel`, каждое лезвие — отдельная физическая форма, не фальшивый прорисованный силуэт плоской текстурой. Палитра тёмная коричневая древесина, бледная слоновая кость и холодный бирюзовый минерал, без однотонного фиолетового. Перед/бок/изометрия, витрина, F5, игрок и рука читаются. Не трезубец и не двусторонний посох.

### W02 «Пепельный двуручник»
Гигантский **один** двусторонне острый прямой клинок, не катана и не короткий обыкновенный меч; длинная двуручная рукоять не короче разумного 2-hand grip; массивная симметричная крестообразная гарда с жёлто-янтарным камнем; основа металла почти чёрная, красные/оранжевые прожилки, светлые острые кромки, в центре вертикальный огненный канал, умеренно сколотые грани. Части `blade_core`, `edges_L_R`, `fissures`, `guard`, `grip_upper/lower`, `pommel`. Не мелкий обычный diamond sword recolor, 3D и 1st/3rd person держать обеими руками при свободной левой руке. Без огня на мире при взмахе.

### W03 «Теневой нож»
Один короткий метательный клинок, широкое бирюзовое режущее ребро, чёрно-фиолетовая внутренняя полоса, 4–5 мелких разноцветных точек Рунника (синий, голубой, фиолетовый, маджента, лавандовый/розовый) на металлическом сердечнике/рукоятке. В конце маленькое кольцо с подвеской; при метании ring не образует самостоятельный второй projectile. `blade`, `dark_spine`, `dots`, `grip`, `ring`, `gem`. Три distinct transforms: инвентарь, в руке, реальный летящий WeaponProjectile; нет прозрачного прямоугольника вместо ножа.

### W04 «Кистень сердца вулкана»
Короткая деревянная/металлическая рукоять, **физическая цепь** из нескольких звеньев, массивная круглая угольно-стальная гиря с 6–10 короткими зубцами, ограниченные красно-оранжевые трещины и тёмные накладки. Силуэт подлинно кистеневой (свободный груз), не цельный молот, не железная дубина. `handle`, `ring`, `chain_links`, `weight_shell`, `spikes`, `lava_core`. При атаке управляемое клиентское раскачивание с правильным pivot, не серверная expensive chain physics. Не позволять цепи/гире пересекать игрока на каждой позе.

### W05 «Адский лук»
Вытянутый изогнутый **сложносоставной** (дерево + рог/кость + обмотка), не гладкий фантазийный металлический круг и не арбалет; 2 загнутых роговых конца (siyah), тёмная полированная древесина, боковые золочёные полоски, центральный красно-янтарный талисман, реальная тетива с 3 стадиями натяга. ModelParts `upper_limb`, `lower_limb`, `horn_tips`, `grip`, `center_amulet`, `string/poses`. Стрелы стандартные, на огненных выстрелах 4–6 частиц около лука/попадания, no perpetual particle storm. Не обесценивать special Горыныча.

**Pixel & UI rules:** полноцветные исходные PNG служат concept reference, **не готовые texture atlas**. Настоящие item texture PNG и UV в пиксельном стиле с чёткими контурами; 16px иконка каждого оружия различима. 3D models требуются там, где геометрия играет роль (вилка, цепь, двуручник, лук), knife можно сделать тонкой моделью если эталон сохранён. Проверять nearest filtering, без размытия/шейдерной зависимости, glint, F5, Steve/Alex, held left and right, pickup/entity, GUI/ground/item frames. Эмиссивны только точечные жилы/камни, не вся тёмная поверхность; glow НЕ block light.

## 3. Точные активные навыки и срабатывания (без новой клавиши)

**Все значения** — назначенный исполнительский баланс, можно тестировать на нативном сервере и предложить точечные поправки по результатам; нельзя произвольно менять без фиксации.

### W01 Навья острога
Обычный hit: 10 HP, 1.15 full attacks/s, до +1.25 блока `ENTITY_INTERACTION_RANGE` поверх vanilla; 20% шанс `Slowness I` 40t, target ICD 80t на конкретный UUID. **Навык «Схватывающий выпад»**: удержать RMB 10 ticks, отпустить; один LOS-проверенный raycast до 4.25 блока, конус узкий (приоритет ближайшей цели), **однократный урон 13 HP**, гарантированный Slowness II 40t, cooldown 240t (=12с) на игроке и стеке, дополнительная потеря durability 4. Мисс расходует cooldown (не дублировать повторным release), но не создаёт free damage. Нет двух ударов из-за двух лезвий, friendly/PvP guards.

### W02 Пепельный двуручник
Обычный удар: **15 HP** при пустом offhand, attack speed 0.9/s, небольшой knockback 0.35; если offhand занят, реально **9 HP** и 2-hand skill заблокирован, tooltip на русском объясняет. Контроль обеих рук и правило защиты предотвращают одновременный полноценный удар со щитом. **Навык «Испепеляющий размах»**: Sneak+RMB удержать 16 ticks, release → 100° дуга радиус 3.5 блока вокруг атакующего впереди, максимум 3 ближайшие LOS цели, **18 HP каждой не чаще одного раза за каст**, knockback 0.6, cooldown 360t (=18c), durability 5; с любой вещью в offhand не активируется. Урон/масштаб учитывать в серверном damage pipeline один раз, не порождать случайный огонь или lava.

### W03 Теневой нож
Обычный melee 6 HP, ~2 full attacks/s; стандартное RMB бросает **ровно 1** предмет `WeaponProjectile`, direct-hit 8 HP, projectile owner/state/enchants сохраняются. При попадании **20% Weakness I 40t**, target ICD 80t. **Навык «Шёпот тени»**: Sneak+RMB → 1 усиленный бросок 10 HP, Weakness I 60t гарантировано по успешному попаданию, cooldown 240t (=12с) после броска, durability loss 3. Сохранять текущий **KNIFE_LOSS_CHANCE=.25** и работу Loyalty, нативное извлечение/возврат/дроп; на miss дебаффа нет, один заряд — один реальный снаряд, без петель сохранения UUID.

### W04 Кистень сердца вулкана
Обычный успешный удар 13 HP, ~0.9 full attacks/s и knockback 0.75. **Навык «Гул магмы»**: Sneak+RMB удержать 18 ticks, release → один server-side импульс в радиусе 3.25 блока от игрока, до 4 видимых законных врагов, **8 HP каждому** и knockback 1.2, cooldown 360t (=18c), durability 4; не ломает мир/огонь/лаву/растения/инвентари и не обходит vanilla mob hurt rules. В обычном RMB без Sneak продолжает действовать прежний FlailItem charged strike; конфликт с новым skill branch проверять.

### W05 Адский лук
Обычная vanilla стрелa: нормализованный direct-hit при ПОЛНОМ натяге 24 ticks **10 HP до модификаторов**; без полного натяга scaling по vanilla механике. 15% шанс поджечь на 40t по hit, только не fire-immune, без block fire. Vanilla arrow/ammo, Power/Infinity/Flame/Punch, крит и т.п. **Навык «Зарница пекла»**: Sneak+RMB, полный натяг ≥24 ticks, release → **одна стрела** direct-hit 13 HP до модификаторов с гарантированным fire duration 60t, cooldown 200t (=10c), durability use 4; если cooldown активен, при отпускании выпускается ОБЫЧНАЯ стрела, не съедает cooldown и не теряет ammo. Разница с Горынычем: current `CrossbowDamage.MIN_SPECIAL_DAMAGE=14` за ОДНУ special стрелу, реальный special `SerpentCrossbow` выпускает **3** стрелы за залп и имеет мор/огонь/гроза; infernal bow single-shot и не должен превосходить тройной залп в сравнимых условиях.

**Общие правила навыков:** сервер authoritative, no extra keybind, правильный main/offhand, spectator/creative, союзники и pet safe, PvP/claim/arena rules, LOS и max distance, cooldown в vanilla UI с отображением, событие no double-hit (одна награда за один cast), cancel when changing hand/offhand/dimension/death, no saved permanent cooldown weirdness, no damage proc twice due to RuneRuntime/Player events, никаких новых potion/witchcraft/способностей классов.

## 4. Рецепты катализаторов, чёткая сетка 3×3

У всех одно правило `Шаблон адской закалки #326 + указанное исходное оружие + указанный катализатор => ровно один upgrade` на кузнечном столе, строго со сохранением всех допустимых данных предмета (включая зачарования, ранг, damage ratio, 2 rune sockets, legacy rune specials, custom name и text components). Не превращать это в `shaped crafting`, которое сбрасывает состояние входящего оружия. **Не создавать второй smithing template**, использовать существующий #326.

Каждый катализатор — новый самостоятельный item с собственной 16px иконкой, RU/EN, creative group, no edible/no new fuel без необходимости.

```text
NAVYA_FORK_CORE  (1)
[Живой корень]       [Ветвь Сторожа чащи] [Живой корень]
[Костяной шип]      [Обожжённый Рунник]  [Костяной шип]
[Обожж. Живокамень] [Алатырский слиток] [Кварц Незера]

ASHEN_BLADE_CORE (1)
[Стержень ифрита]   [Жаростойкая печать] [Стержень ифрита]
[Уголёк Пекельника] [Алатырский слиток] [Уголёк Пекельника]
[Обожж. пек. осколок] [Обожж. пек. осколок] [Обожж. пек. осколок]

SHADOW_KNIFE_CORE (1)
[Нить Шептуна]       [Светожильный осколок] [Нить Шептуна]
[Светожильный осколок] [Обожжённый Рунник] [Светожильный осколок]
[Кварц Незера]       [Алатырский слиток]    [Кварц Незера]

VOLCANIC_FLAIL_CORE (1)
[Клешня краба]        [Магмовое ядро]      [Клешня краба]
[Обожж. пек. осколок] [Алатырский слиток] [Обожж. пек. осколок]
[Магмовый сгусток]   [Фрагмент магмовой руны] [Магмовый сгусток]

INFERNAL_BOW_CORE (1)
[Шкура саламандры] [Стержень ифрита]  [Шкура саламандры]
[Фрагмент магмовой руны] [Алатырский слиток] [Фрагмент магмовой руны]
[Алмаз]            [Обожжённый Алатырит] [Алмаз]
```

**Предлагаемые ID**: `navya_fork_core`, `ashen_blade_core`, `shadow_knife_core`, `volcanic_flail_core`, `infernal_bow_core`. Внешность иконок: ① двухзубая костяная полумесяцная коронка с бирюзовым стержнем/корнями; ② раскалённая плоская пластина лезвия с золотой гардой и продольным жарким каналом; ③ миниатюрный кристаллический фиолетово-бирюзовый ромб с 4–5 цветными точками в чёрном обрамлении; ④ тяжёлая магмовая гиря-капсула на коротком кольце с тремя крупными красными прожилками; ⑤ выгнутая деревянная роговая кибить лука с золотой обмоткой и янтарным сердцем. Не одна и та же иконка с перекраской.

**Survival loot constraints:** `guardian_branch` из Сторожа чащи, `zharenitsa_heat_seal` с Жареницы, `living_root`, `bone_thorn` из местных существ, `pekelnik_ember` — новый droppable по броневой #326, `lost_whisper_thread` и `lightvein_shard` по наградному контракту Чертогов, `crab_claw`, `magma_core`, `magma_clot`, `magma_rune_fragment`, `salamander_hide` по Горючим островам. **Сначала подтвердить точно реальные ID и self-consistent loot**, если у босса дроп ещё не реализован — `BLOCKED_DEPENDENCY` и продолжить остальные задачи, **не подменять** трофей `minecraft:stick` или dev-only `/give`. Острова частично не закончены, #324 новых руд ещё не implemented. Условий «убить мирного питомца» нет.

## 5. Внешность обычной линейки из 6 предметов

Каждое оружие СТРОГО на основе оригинального вида соответствующего алмазного предмета, без пересоздания силуэта или новой физики: тёмно-бурая/угольная сталь, локальные жёлто-оранжевые трещины, горячие сердцевины в щелях, несколько красно-коричневых граней, золотая или коричневая обмотка рукояти. Цветовая семейственность с «Жаровой» алатырной бронёй #326 обязательна; не переименовывать весь набор «незеритовый» и не копировать случайные интернет-сборки. Для 6 — качественный pixel recolor. Для 5 special — полный новый силуэт по отдельным панелям 5×.

**Все**: localizations RU/EN, item icons, item JSON, creative, recipes, enchatment categories, RuneRuntime compatibility, tooltips в HP/сердцах, cooldown, description skills and trigger; искреннее соответствие виду при рендеринге.

## 6. Матрица приёмки — не пропускать

- `A_REF`: full original PNG + five crops SHA256 совпали с загруженными byte-to-byte; если нет — `ART_BLOCKED_REFERENCE`, не PASS.
- `A_MODELS`: отдельная настоящая геометрия обоих зубьев одной остроги, **перед/бок**, giant 2hand blade и 2-hand grip, chain flail и swing, bow draw 0/1/2 + string + bone tips, real flying knife; compare sheet `reference_vs_ingame.png`, не AI-генерированная картинка под видом скриншота.
- `A_STATES`: Steve/Alex, left/right, first/third person, armor stand / item frame / inventory / dropped item, walking/crouch/sprint/jump/swim, GUI scale 2, glint/charged bow, no major clips.
- `S_RECIPE`: для 11 upgrades и 5 intermediate catalysts по `minecraft:smithing_transform` и `crafting_shaped` без дюпов; 1 template/1 base/1 catalyst -> 1; negative variants fail; old-save, no missing item; blueprint/loot checked.
- `S_RUNE`: всего 2 maximum Rune sockets на предмет (открываемые игроком), present 0,1,2; третья не вставляется; сохраняются 2 diamond runes, enchanting/custom name/damage via upgrade; проверка `Runes.category` для всех и совместимости с existing runes (особенно копьё/тяжёлое/лук/нож).
- `S_SKILLS`: 5× normal + active, exact HP/cooldown/range/trigger, 2+ clients and dedicated, hostile/allied/PvP/no LOS, no unexpected direct damage duplication, no durable dupe. Shots that miss consume one shot and right cooldown as defined; cancellation doesn't charge.
- `S_GORYNYCH`: equal unenchanted scenarios and ranged 10/13 vs Gorynych MIN_SPECIAL_DAMAGE 14 per arrow ×3, single v group, with elemental immunity, ammo, rune, enchanted Power, actual DPS not only estimate.
- `S_BALANCE`: benchmark 20 s 5–10 fights each (ordinary mob IV/V, biome bosses VI, comparison VII/VIII), armor diamond/netherite/Жаровая/Пекельный владыка; fully naked/enchants and one/two runes; no PvP cheat, report TTK and abnormal percent.
- `S_PERF`: no periodic world scans, tick new FX only bounded, no ghost projectile after logout, Entity/projectile NBT and item state reload, 2-client latency, no chunks forced.
- `S_BUILD`: Java21 NeoForge1.21.1 clean build, resource validation, production JAR SHA, source commit links, matrix `REQUIREMENT | CODE | SOURCE_ASSET | RUNTIME_EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN`; real gameplay QA and owner acceptance mandatory, **Codex does not self-certify DONE**.

**Порядок Codex:** read `AGENTS.md`, `docs/PROJECT_WORKFLOW.md`, `docs/CODEX_PROJECT_PLAYBOOK.md`, `docs/requirements/{05-WEAPONS-COMBAT,06-TOOLS-EQUIPMENT,07-LOOT-REWARDS,09-BLOCKS-CRAFT,10-ART-AUDIO-UI,12-SAVES-MP-PERFORMANCE,13-TEST-GATES}.md` → existing WeaponCatalog/Runes/NeoForge → exact source asset import check → distinct phases ART and GAMEPLAY → clean build+tests → comparison grid → owner test. **Когда не всё доступно, не задавать владельцу очевидные вопросы:** искать через GitHub refs/этот контракт, фиксировать настоящий `BLOCKED_*` только если исходник или зависимость объективно отсутствует. Не использовать другой дизайн вместо утверждённого.
