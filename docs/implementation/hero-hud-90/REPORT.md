> SUPERSEDED: актуальное ТЗ v3 отменяет4 одновременно. См. [ревизию28](../hero-hud-90-v3/REPORT.md).

# #90 — HUD всех загруженных богатырей,1.9.7.27

IN_PROGRESS / OWNER_PENDING, Issue OPEN. Выполнялась одна следующая Issue #90.
#84/#85 не перерабатывались. Upstream main534546c0 содержит два новых model spec SVG;
они объединены обычным merge с локальным main, без reset/rebase/стеша чужой работы.

## Реальное до/после

`before-HeroHud.java` — исходный production renderer: находил4 типа, затем
`Math.min(2,heroes.size())` отбрасывал остальные строки; восстановление заменяло имя.
Теперь production HeroHud → HeroHudView.select/layout/draw → GuiGraphics adapter:
каждый загруженный живой спутник владельца в прежних64 блоках получает строку.
UUID dedup не объединяет разные сущности одинакового типа. Сортировка typeID+UUID
не зависит от порядка entitiesForRendering. Повторного поиска мира, новых пакетов,
chunk loading или server tick polling нет. Только существующий обход client entities.

Имя и HP всегда остаются в первой строке, состояние — во второй; HP-bar — ниже.
Длинный текст обрезается с явным многоточием, а не лезет в HP. Status из существующих
synced owner/action/recovering данных: Помогает / Восстановление / Отступает / Проклят
для cursed Finist. Серверные поля lastStand/recoveryEnd/restSince для UI не читаются.
Богатыри и KalinParty/HeroCalls не менялись. Слоты5/8/12 при искусственном числе
спутников переносятся в колонки, а не обрезаются; обычные1–4 — одна компактная стопка.

Верхняя зона и нижние vanilla bars зарезервированы. По умолчанию панель слева,
стандартная minimap/status effects справа и bossbar сверху не получают её строки.
Другие произвольно настроенные оверлеи автоматически не обнаруживаются: клиентский
`config/slavicmyths-hero-hud.toml` даёт horizontalOffset/verticalOffset в GUI-pixel,
с ограничением координат рабочей областью. Размер зависит от текущего GUI viewport.
Старый companionHud=false, F1, открытые экраны (в том числе ChatScreen), отсутствие
player/level по-прежнему скрывают HUD. Недавние сообщения закрытого чата и нестандартная
minimap требуют общей ручной проверки/настройки смещения; отсутствие конфликтов во
всех произвольных модпаках не заявляется. Нового экрана/биндов/зависимостей minimap нет.

Lifetime/cooldown не добавлены: #78 ещё не даёт авторитетных synced данных. Счётчиков
в HeroHud нет — это прямое ограничение #90. Gameplay/баланс/registry/NBT не менялись.

## Проверки и артефакты

- Actual production JVM selection/layout/draw: 40 случаев 0–4 героя,1280×720 и
  1920×1080,GUIscale1–4; player absent/other owner/dead/range64 boundary/duplicateUUID,
  row bounds/nonoverlap/bottom reserve/overflow/offset clamp/long names+status/zero maxHP.
- Ширины и native glyph preview читают default bitmap providers из установленного
  Minecraft1.21.1 resources JAR; числа advances по формуле actual BitmapProvider.
- production-draw-trace.json содержит вызовы настоящего HeroHudView.draw, тот же код
  вызывается из игрового renderer. Fixture snapshots1–4 используют реальные типы/имена
  и разныеHP/status. CPU replay + vanilla glyphs в hud-1-4-source-preview.png.
- Это НЕ OpenGL/Minecraft screenshot и НЕ runtime entity spawning. Старый renderer
  зафиксирован source-файлом; игровые before/after1–4 остаются NOT_RUN.
- Java21 clean build PASS1m4s. Production JAR/JSON/all resources/all compiled classes
  совпали с рабочим деревом; тестовые классы не упакованы.
- Проверки защищают исходники4 героев/KalinParty/HeroCalls от изменений, а client config
  регистрируется из common без загрузки Minecraft client типов.

JAR: `build/libs/slavicmyths-1.9.7.27.jar`.
SHA256: `9596c31c309fe70a72a939b2baad0d663e5d525eb12c35d72da530913d4527c4`.
Точный manifest production source в build-source-sha256.json: JAR включает сохранённые
незакоммиченные командные/UI правки из предыдущих сессий. Scoped commit #90 не выдаётся
за полный commit всего рабочего дерева. Commit/push предыдущих пользовательских правок нет.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все4 типа вместо cap2, без UUID дублей/чужих/мёртвых | actual HeroHudView.select tests + source adapter guards | PASS |
| Имя/HP/status для каждого, retreat/recovery различимы | source synchronized action mapping + actual draw trace | PASS |
| GUIscale1–4/1–4 героя/2 viewport sizes/overflow/ellipsis |40 actual production JVM cases + boundary cases | PASS |
| Safe area и клиентские смещения | production layout bounds tests, config | PASS |
| UI/owner/64block guard, без world/network expansion | source checks; protected server files unchanged | PASS |
| Не рисовать выдуманные lifetime/cooldown | полей/счётчиков нет; #78 отдельно | PASS |
| RU/EN,Java21 clean build/resources/JAR | checks.json/build.log | PASS |
| Реальные игровые before/after при1–4 героях,чат/minimap/effects/bossbar | Запуск Minecraft запрещён | NOT_RUN |
| Реальные loaded entities/statuses/GUI fonts/MP | Владелец проверит вместе | NOT_RUN |
| Установка PolyMC | Не запрошена; текущая25 сохранена | NOT_RUN |
| Owner acceptance | Нет явного решения по #90 | NOT_RUN |

Codex Minecraft/PolyMC/GameTest/server запусков0. Миры не менялись.
POLYMC_NOT_INSTALLED для27. Никакой Issue/owner-accepted не закрывались/не ставились.
