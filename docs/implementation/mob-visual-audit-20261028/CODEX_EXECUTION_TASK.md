# Готовое поручение Codex — 28.10.2026: Posten summon + spider gait + palette / 16 mob animations

Владелец разрешает **приступить к техническому исправлению** проблем, зафиксированных в новом аудите GitHub, и собрать тестовый JAR. **Не ограничивайся аудитом/размышлениями**, при условии соблюдения остальных правил AGENTS. Ветка для production изменений и commit/push — **только hell**, `main` не трогать.

## Единые источники и вопросы, ответы на которые уже есть в GitHub

Эпик: https://github.com/Deserag/slavic-myths/issues/332.
Исполнительские задачи: #333 Постень (Overworld summon), #334 костяной/угольный ползуны, #335 палитры, #336 анимации 16 видов.
**Сначала прочитать полный проверенный исходный аудит**: https://github.com/Deserag/slavic-myths/blob/hell/docs/implementation/mob-visual-audit-20261028/MOB_VISUAL_AND_POSTEN_DIAGNOSTIC.md
Соседний issue #331 — трофейные головы; не начинать полную перерисовку боссов: владелец их уже «более-менее принимает». Кодекс не должен спрашивать, какие лапы у паука или где стоят текстуры — всё описано в issue и аудите.

## 1. Сам обнови исходники и сборочную рабочую копию
- На моём Windows-ПК самостоятельно найди подключённые репозитории `Deserag/slavic-myths` и рабочие каталоги (ориентиры: `C:\Users\user\Documents\slavic-myths`, `C:\Users\user\.codex\worktrees\lost-halls\slavic-myths`).
- `git status -sb`, `git worktree list --porcelain`, `git fetch origin --prune`, `git rev-parse origin/hell`, лог коммитов. **Прежде любых изменений** сохранить чужие незакоммиченные изменения, не делать force/reset --hard/clean, не менять main/master и другие worktrees.
- Выбрать единственный чистый writer по `hell` или организовать безопасный отдельный worktree (без создания сторонней feature-ветки). Использовать последние реальные файлы `hell`, не старый snapshot из предыдущей сессии.
- Проверить фактический SHA предыдущего тестового JAR; предыдущая проблема «исправления не видно» могла быть вызвана установкой старого JAR, но нынешние gameplay-ошибки по source тоже подтверждены.

## 2. Начать исправлять в порядке P0 → P1
**P0 #333 — Постень**: сейчас `PostenMob.tick()` делает `discard` везде, кроме `Level.NETHER`, а `PostenEncounters.get(ServerLevel)` может выбросить исключение вне Незера. Для ручного `/summon slavicmyths:posten` и `posten_spawn_egg` сделать безопасный **debug/manual encounter** во всех трёх измерениях (Overworld/Nether/End), модель, bossbar, 4 маски, 2 фазы, копии, без уникальных трофеев/фарма и без изменения журнала настоящей природной арены. Настоящий Постень естественно по-прежнему только в «Чертогах заблудших» в Незере. Удалить один dimension check без безопасной модели state запрещено.

**P0 #334 — пауки/ползуны**:
- `bone_crawler`: восемь физических legs уже есть, но анимируются только bases root по yRot/zRot, а knees/shins/feet неподвижны. Сделать реальные шарниры коленей, голени и опорные стопы, alternating 4+4 gait, swing/lift/plant, attack windup и idle; 8 ног не должны двигаться синхронно.
- `coal_crawler`: 4 примитивных root legs+общий CinderModel gait; сверить канон референса, переработать суставную походку, до/после с собственным отличным ритмом, при утверждённом паучьем образе — настоящие 8 ног. Не пересоздавать сущность/AI, не ломать ID.
- В обязательных Native before/after показать модель с 4 сторон, 2 разных опорных фазы ходьбы, сгибание knee, walk→idle и удар. Сдать не GIF чужой анимации, а реальный экспорт изменённых ModelParts.

**P1 #335 — палитра/силуэт**: три моба Горючих островов по текстурам ~95–99% histogram overlap, угольный ползун/Пекельник ~94%. Это сравнение PNG, не финальное rendered appearance. Сделать корректный UV/render occupancy audit всех 16 видов и 4 вариантов Шептуна, затем **реальные** различимые pixel textures/при необходимости силуэты. База: саламандра тёмно-бордовая с горячими полосами; краб коричнево-медный/шлаково-зелёный с крупными клешнями; Жерловик холодный серо-базальтовый с большим ярким ядром. Угольный ползун — чёрный уголь и трещины, Пекельник — обожжённая кирпичная броня, Искорник — яркий золотистый жар, костяной паук — бледная кость/лиловый. В Чертогах разнести cloth/crystal/bat wing/fungus materials. **Впечатление в игре важнее точного хеш-совпадения и метрики.** Не выдавать новый recolor одного и того же куба за отличный моб.

**P1 #336 — остальные модели/позы**: персональные idle/move/alert/attack/recovery/hurt/death sequences для 16 видов в матрице аудита. Приоритет: crab lateral, salamander lagging tail_tip, golem weight shifting, Gulokryl wing two-segment glide/tamed-sit, Oskolnik crystal-charge, Ekhovi body resonance, Griboshmyg cap bounce, Sheptun all four preserved variants. Не вводить новые attack skills, HP/loot/spawn/biome changes.

## 3. Референсы — не повторять ошибку «нет»
- Физически доступные настоящие code resources уже есть в `src/main/java/.../client`, `src/main/resources/assets/slavicmyths/{cinder_rigs,lost_halls_rigs,burning_islands_rigs,textures/entity}`. Сначала use actual rig/texture/renderer, не придумывать свои duplicate assets.
- Документация о source art: `docs/concepts/NETHER_BOSS_REFERENCE_HANDOFF_20261028.md`, `docs/concepts/navya-grove/STRICT_ACCEPTANCE_2.0.2.md`, `docs/concepts/black-cinder/RECOVERED_VISUAL_SPEC_2.0.3.md`, `docs/concepts/lost-halls/MODEL_AND_AI_LOCK_2.0.4.4.md`, `docs/concepts/burning-islands/MOBS_AND_REFERENCE_LOCK_2.0.5.md` и `references/README.md`. **Оригинальные PNG в GitHub отсутствуют, Markdown SHA не equals доступный PNG**. Проверь локальные folders/ZIP сам; без оригинала art matching `BLOCKED_REFERENCE`, но реальную кинематику и differentiation можно исправлять независимо.
- Для каждой модели: before/after native 3D renders **того же source** в одинаковом масштабе и свете, 4 стороны и actual walk/attack clips, atlas/UV, palette pairing, file path evidence. Нет новых звуковых плейсхолдеров; существующие OGG/sound event не ломать, если новых качественных правомерных OGG нет — `BLOCKED_NEEDS_AUDIO_SOURCE`.

## 4. Проверки и публикация
- Java21, NeoForge 21.1.255, Minecraft 1.21.1. `gradlew.bat --no-daemon clean build --console=plain` + source-specific offline pose/rig/UV/registry/texture tests; JAR real SHA. Existing tests не подгонять к ложным PASS; физические runtime-only проверки оставить `NOT_RUN` в соответствии с текущим запретом `AGENTS.md` на Minecraft/PolyMC/server/GameTest.
- Проверить spawn egg/command in all dimensions только source/offline в сессии, составить **точные owner QA commands** для Minecraft, чтобы пользователь сам проверил: 3 мира, 8 ног/3 фазы ходьбы, групповая цветопередача/поведение.
- Если код исправлен и сборка прошла, **создать коммит(ы) в hell и выполнить обычный push origin hell**, если нет конфликтов / других writer, без force. Привести точный source HEAD, push SHA, имена файлов, сборку/JAR SHA, matrix `ISSUE|REQUIREMENT|BEFORE|AFTER|EVIDENCE|PASS/FAIL/BLOCKED/NOT_RUN`.
- **В рамках этого поручения разрешена осторожная установка нового прошедшего сборку JAR в существующий тестовый PolyMC инстанс без запуска игры**, только после проверки текущего выбранного инстанса и закрытых Minecraft/PolyMC, полного backup прошлого JAR и предотвращения двух slavicmyths JAR. Никогда не удалять пользовательские saves/другие mods. Если процессы ещё работают или нет точного target, не принуждать их завершать, не устанавливать — отметить `POLYMC_NOT_INSTALLED`. Установленная версия и SHA должны совпасть с build SHA. Не обновлять/открывать старый единственный мир.
- Не закрывать issues, не ставить `owner-accepted`. Продуктовую проверку и окончательное принятие выполняет владелец, даже если offline build PASS.

**Не заканчивай ответом «всё готово», когда у паука ещё неподвижные колени или 16 видов различаются только значением hue. Исполняй весь независимый технический объём в этой сессии, блокеры с фактами, а не только план.**
