# Ручная проверка 0.9.4 / 1.21.1

Клиент автоматически не запускался. Этот список не является отчётом о пройденных игровых проверках. Выполнять на отдельном тестовом мире после успешной загрузки, сохраняя оригинальные миры.

- Проверить версии Minecraft/NeoForge/Java и все семь top-level JAR; загрузку core с Curios, затем JEI/Jade/Xaero/FallingTree. Нет лишней standalone XaeroLib при embedded jar-in-jar.
- JEI: четыре категории, рецепты 4×4, зеркальные/бесформенные рецепты, transfer, optional запуск без JEI.
- Curios: head/necklace/ring/belt/charm, 1/1/2/1/1; equip/unequip/смерть/перезаход, эффекты/атрибуты, пять геометрий аксессуаров.
- Все исходные предметы/блоки/яйца в creative; локализации ru_ru/en_us, модели/текстуры/звуки. Перечень registry IDs сверить с parity report.
- Показ HP через Jade; старого общего Combat HUD нет. Boss bars, охотничьи подсказки и интерфейсы RPG/Яги/полёта сохранены.
- Оружие: урон, щиты/пробивание, отдача булавы/NPC, заряд кистеня, копья/луки, руны/перековка. Проверить серверную авторитетность и отсутствие повторных наград.
- Trophy pouch: девять слотов, запрет вложения/переполнения/дюпа, перенос между инвентарями, copy/смерть/перезаход. Проверить импорт старых внутренних stacks и сохранение чужих custom_data keys.
- Hunting horn: выбор цели/cooldown/вызов/сохранение; все существующие охотничьи цели/фазы/призраки/мини-боссы/мировые боссы, cooldown/региональные якоря.
- Broom/Mortar: ownership/pilot, управление/торможение/GUI, cargo девять слотов, падение/демонтаж, pestle animations, Tailwind 0–3, сохранение cargo/перезаход. Попытки управления чужой сущностью сервер отвергает.
- Ritual/utility: altar, cloth cooldown/inventory, offerings, song calming/guardian behavior, invisibility/equipment fade. Уникальные механики сохранены.
- Яга: яйцо, NPC/dialogue/восемь экранов, progression/quests/contracts/reward idempotence/перезаход; locate/generate по README. Избушка в обычном и superflat мире, включая Y<0; дверь/фонарь/растения/NPC arrival; не оставлять частично размещённый дом при отказе.
- Курганы: small/warrior/great, locate/generate/info, природные starts/references, этажи/лабиринт/ниши/саркофаги, encounters/disturbance/curse/death/logout/dev clear. После сохранения продолжает тот же экземпляр; старые layouts/loot не переработаны.
- Бандитские лагеря и болотные строения: все 13 structure IDs, натуральная генерация, bounded locate/next/teleport и реальные высоты, loot/encounters не дублируются после перезагрузки чанков.
- Деревья: четыре вида, saplings, leaves decay/distances, hanging foliage/wood/signs; FallingTree interoperability, инструменты/дропы. Сверить старые visual silhouettes.
- Мебель/BlockEntities: altar/net/pool/rack/coffin/sign/wardrobe/seat, factory/ticker/render bounds, menus/shift-click/обрыв соединения. Проверить изменения в dedicated server + два игрока.
- Xaero minimap/world map: ввод/открытие/сохранение и отсутствие дублирующей собственной карты. Настройки Jade/FallingTree не меняют RPG/охоту/лут.
- Сохранять logs ошибок, seed, координаты, registry ID и точные шаги воспроизведения. Частота структур, существующие AI/worldgen/loot/art проблемы остаются отдельным backlog; не исправлялись ради редизайна.
