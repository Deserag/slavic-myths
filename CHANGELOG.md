## Патч оружия — поверх 1.9.7, 2026-10-13

Исправлены фильтр целей/учёт боевых атрибутов косы Карачуна и сброс её позы; компактная объёмная коса/16px иконка, разные модели копья победителя, кистеня атамана, посоха Лихо и Сабли Воеводы. Арбалеты: безопасный pull predicate и компактные first-person transforms. Registry ID и номер версии сохранены. Clean build + Night offline PASS; 15764 resource gates, 921 item models, 208 subscribers PASS. Игровых запусков 0; визуальная/боевая приёмка OPEN. PolyMC Testing обновлён с backup, имя дополнено «Патч оружия». Параллельные правки брони/аудита сохранены отдельно. [Отчёт](docs/verification/weapon-patch/REPORT.md).

## 1.9.7 — Оберег двух зим (2026-10-13)

Один артефакт из существующих наград Морозко/Карачуна; точные рецепты оригинала и копии с серверной квитанцией и возвратом шаблона. Два серверных режима, пассивы во всех измерениях, активный щит/волна только в Нижнем мире, общий persisted кулдаун, персональная поэтапная аура и собственный эффект без удаления чужой Слабости. Старые боссы и награды не изменены.

Чистая production сборка + десять offline наборов **PASS**; новый набор 4853, ресурсные gates 10991. PolyMC Testing обновлён до **1.9.7**, SHA256 0f1e76dd6bef952350a67d83da5357cc26bcc8dcb290b7eb281c8e46d2290e0e; предыдущий JAR в backup, остальные файлы/миры/конфигурации сохранены. Клиент/сервер/GameTest: **0** запусков; игровые проверки не заявляются.

Архитектура: существующий PathData/RPG sync; WintersEvents — события боя и локальный cast, inventory/aura раз в 20 тиков плюс лёгкий Inventory.setChanged; WintersRecipe — exact 3×3 CustomRecipe/native remaining-items; WintersTemplates — SavedData выданных оригиналов. CLIENT property/tooltip; stable KB modifier, свой щит и эффект aura. Автоматический Crafter отключён для этих двух рецептов. Будущие milestone не реализованы.

Подробнее: docs/TWO_WINTERS_1.9.7.md, docs/verification/winters-1.9.7/REPORT.md.

## 1.9.6 — Змей Горыныч

Горыныч Змеиной горы, яйцо в творческой вкладке «Мобы», два этапа боя и очищение трёх голов, собираемые дыхания, персональные награды, арбалеты, три руны и драконья броня. PolyMC Testing обновлён после clean build и offline/resource gates. Игровых запусков агентом: 0. [Использование](docs/GORYNYCH_1.9.6.md), [результаты](docs/verification/gorynych-1.9.6/REPORT.md).

# 1.9.5 — Змеиная гора

Единый редкий Overworld complex, два входа и связанные интерьеры, операторская очередь генерации, 11 блоков/рецептов, свет/звук и индивидуальная опасная аура с существующим mor_ward. Build/offline/resource gates PASS; PolyMC Testing обновлён. Игровых запусков 0.

# 1.9.4 — Осквернённые хранители

Два самостоятельных хранителя в залах 1.9.3, локальные фазовые бои, очищение, персональные части оберега, модели/анимации/звуки/сказания. Старые шаблоны, обычные рецепты и лут сохранены. Build/offline/resource gates PASS; PolyMC Testing обновлён; игровых запусков 0.

# 1.9.3 — Две древние святыни

Две естественные святыни Overworld, локальные ритуалы, девять предметов, семь рецептов, тематический лут и предания. PolyMC Testing обновлён после успешных gates; игровых запусков 0. Отчёт: docs/verification/sanctuaries-1.9.3/REPORT.md.

## 1.8.3.2 — Великий Полоз (включая 1.8.3.1), 2026-10-12

12 поражённых блоков, рог с точным рецептом/дропами/подтверждением, мировой multipart-босс 3000 HP/40 блоков, шесть атак и постоянные последствия. Native/offline/23 headless/production gates PASS, PolyMC Testing обновлён с backup. Клиент не запускался; ручная приёмка и MSPT/FPS открыты. Старые registry IDs сохранены. Подробности: docs/verification/poloz-1.8.3.2/REPORT.md.

## 1.8.3 — исправления bossbar, хвата и целей богатырей

- Полоски боссов не восстанавливаются после смерти; очищаются при удалении/выгрузке.
- Призванные богатыри распознают все активные моровые формы.
- Исправлены реальные руки/хват и связность оружия Ильи, Алёши, Добрыни, Финиста; генераторы обновлены.
- Build/native/resource gates PASS; PolyMC обновлён с backup, игровых запусков 0.

## 1.8.2 — Высшие моровые

- Высшие формы существующих Лиха и Овинника: отдельные силуэты/материалы, светящиеся глаза, анимации и vanilla-звуки.
- Временная заражённая земля, облако Хвори, зольная завеса, луч, три угля и вторая фаза при половине HP; блоки мира не изменяются.
- Отторжение мора через стандартные локальные AI goals, расширенная склянка, RU/EN; обычные формы/ID сохранены.
- Production build и офлайн-проверки PASS. Игровых запусков 0; ручная приёмка ожидается.

## 1.8.1 — 2026-10-12

- Added persistent blighted variants of the existing Leshy, Poludnitsa, Kikimora and Fire Serpent, distinct native models/textures and nine telegraphed abilities including two once-only30HP serpent copies.
- Added creative-only Flask of Blight, rare gated natural variants and one existing lore-book discovery; RU/EN and actual vanilla sound cues.
- Preserved ordinary registry IDs/assets and dependencies. Final clean build/offline/resource gates PASS; installed final JAR in existing PolyMC Testing with backup/hash preservation, no game launch. Manual gameplay/visual/audio/rejoin/MSPT pending.

## 1.6.9 candidate — 2026-10-11

- Added persistent Kot Bayun with personal gifts/trust, four functional dialogue choices, loaded-only ambient actions, warning/song/charm and cancellable physical pounce.
- Rebuilt the existing lore book with once-only world-owned delivery, eight searchable sections, sparse personal knowledge, real recipes, schematic hints and actual advancement graph.
- Added 217 catalogue records with complete current registry coverage and 58 individual Russian living-creature profiles; preserved Yaga quest schema/rewards and all stable registry/legacy advancement IDs.
- Added native143bone/254cube model,17 timelines,19 supplied original synthesized OGG prototypes, distinct32px category icons, RU/EN keys. Fixed four pre-existing corrupted entity names.
- Clean build, native Bayun/Kuzya/doghead regression, production resource gates, final15/15 dedicated GameTests and actual saved-server restart passed. Candidate installed with backup in selected1.21.1 profile;559 other files preserved.19 dedicated launches,0clients.
- Client Russian GUI/visual/audio/controller/natural seed/real multiplayer/old Yaga walkthrough remain NOT_RUN; actual MSPT/FPS NOT_MEASURED. This is not a fully accepted final release. No commit/push.

# Changelog

## 1.4.3

- Отдельный Проклятый Волколак: оригинальный Variant B, две позы/анимации, 400HP, физические серии, прыжок/обход и короткая хватка. Старый Волколак сохранён.
- Дурная кровь ослабляет постепенное лечение/еду; Instant Health сохранён. Существующее кровотечение переиспользовано.
- Четвёртое поручение Волка: ложная смерть, поиск могилы/личного черепа, физический возврат и однократные существующие Лук+Колчан Финиста. Старые claims сохранены.
- Собственное яйцо, RU/EN, objective icons, 28 OGG/23 события с лицензиями.
- Native/runtime/resource/production gates и установка тестового PolyMC; клиентскую приёмку проводит пользователь. [Отчёт](docs/verification/volkolak_1.4.3/REPORT.md).

## 1.3.2.1

- Оружие 2.0: 30 форм, семь материалов, разные силуэты; 53 новых предмета, включая детали.
- Копья/сулицы с досягаемостью, серверные броски ножей/сулиц, кистени против щита и брони, короткий/тяжёлый луки.
- 36 сборочных и 23 рецепта деталей; браузер оружейного верстака без обязательного JEI.
- Ёмкость/категории рун для существующего оружия; сохранение ранее открытых слотов, данных при улучшении и совместимых ремонтах.
- Нативное сохранение projectile/ItemStack и однократный подбор; прежние ID, рецепты, модели и текстуры сохранены.

Clean build/resource/JAR gates PASS; финальные серверные suites 31/31, 11 headless запусков и ноль клиентов. [Реализация и ручные ограничения](docs/WEAPONS_1.3.2.1.md).

## 1.3.1

- Классы 2.0: три базы, 18 ветвей, 28 навыков и шесть эволюций; отдельные XP/очки, три активных слота и один пассивный, автоматические триггеры.
- Серверные проверки, миграция старых путей, сохранение после смерти и загрузки без удвоения атрибутов/сброса КД.
- Самостоятельный экран/дерево, численные параметры, переназначаемые G/H/J и K, HUD, 34 разные skill icons и ru_ru/en_us.
- Сохранены параллельные исправления башен, интерьеров, света, лестниц и окон: [городские изменения](docs/CITY_POLISH_1.3.1.md).

Clean build/resource gates PASS; итоговые suites 82/82. [Отчёт и ручные ограничения](docs/CLASSES_1.3.1.md).

## 1.2.5

- Добавлено редкое городище: 240 шаблонов в трёх климатах, два плана улиц, терем, торговый дом, площадь, городские дома и каменная оборона.
- Добавлена полная `/sm test gorodishche_showcase` для операторской проверки; используются существующие жители и дружинники.
- Исправлены опоры домов и крыша наблюдательной башни.
- Мешок расширен до пяти слотов по 64 и шести визуальных размеров; кружки/бутылки получили объёмные модели.
- Аудит подготовки релиза: резервное перемещение старых QA runtime-папок, ignore runtime output, исключение старых runtime файлов из индекса, безопасный Git gc без prune или переписывания истории.
- Сборка больше не зависит от локального Xaero JAR; immutable Maven version сохраняет API 26.5.0.
- Work AI проверяет loaded chunk до обращения к POI; игровые правила и registry IDs сохранены.
- README, реальные схемы NBT/текстуры, release notes, отчёты размеров/ресурсов/кода и план ручных кадров.

Ранние изменения: [статус проекта](docs/PROJECT_STATUS.md). Будущий roadmap не включён в список реализованных механик.

## 1.5.2 — тестовый кандидат, 2026-10-09

Добавлены native attached-small/independent-medium/great, пять крупных ролей,12 вторичных модулей, лабиринты/двойные лестницы и сохранённые профили колодца76/60/44. Старые generation-v1 города не перестраиваются и остаются sealed.14 native GameTests/11982 production checks PASS; кандидат установлен по запросу пользователя, backup и214 прочих файлов сохранены. Natural12seed/480candidate:5medium/1attached/0great; квота редких случаев, клиентская приёмка/relog/MSPT открыты. Изменения других задач сохранены; commit отсутствует.


Чуди1.5.2, исправление размещения2026-10-09:до10 точек на область с одним root, горы как предпочтение/Overworld biome allowance, удалён12% forest filter, corrected actual lower construction limits без изменений saved layouts. Build/offline/resource gates PASS;development candidate установлен в PolyMC.0 игровых запусков, runtime QA новой генерации NOT_RUN. Отчёт:docs/verification/chud-1.5.2/placement-fix/REPORT.md.
