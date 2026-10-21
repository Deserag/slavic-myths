# #111 · уточнённое художественное решение · 21.10.2026

**Полный исполнительский scope #111 реализован и технически проверен: 9 FIX +1 KEEP. IN_GAME_OWNER_PENDING / Issue OPEN.** Это не игровая приёмка и не завершение #92. Рекомендуемый Project Status — **Тестирование**. Фактический Status не изменён: проверенная21.10 доска показывает #111 в «Ожидании», доступный браузер не авторизован, карточки disabled; GitHub connector не предоставляет Projects mutations. Требуется изменение поля владельцем. Ни статусы DONE, ни owner-accepted, ни закрытие не применялись.

## Актуальные основания / источник

Владелец обновил задачи и явно разрешил продолжение. Прочитаны новые #92/#111 и QA-уточнение #114; импортированы из GitHub main новые `docs/design/TROPHIES_VANILLA_2D_ART_STANDARD.md` и `TROPHIES_111_APPROVED_REFINEMENT.svg`. Этот лист определяет **одобренное направление**, а не персональный выбор A/Б владельцем. Выбор ниже сделал исполнитель по читаемости. Прежний блокер native3D/UV для pearl_fragment **снят явным исключением #111**: его cuboid заменить отдельным generated2D. Никаких новых чертежей по этому пункту от владельца больше не требуется.

HEAD `69a2db12de4baac9cef7c43e6c4b0ae717d3b644` + dirty tree. Commit/push не выполнялись; новый source commit не заявляется. Проверены git status/worktree, доступные чаты: другой активный Codex writer не обнаружен. Исходные чужие правки брони/рун, работа #110 и предыдущие полезные FIX #111 сохранены. Новые изменения поверх частичного результата — только **3 PNG одной партии**, model JSON pearl_fragment, canonical art/verifier/legacy guard и status/evidence docs. [Byte baseline](workspace-before.json), [собственные отличия](workspace-diff.json), [исходный status](git-status-before.txt).

## Решения по десяти ID

| ID | KEEP/FIX | A/Б и причина | Что сделано в этой итерации |
|---|---|---|---|
| old_button | KEEP | исходник уже имеет4 отверстия/обод | PNG/model exact baseline |
| ancient_coin | FIX, сохранён предыдущий | A: бронзовый восьмигранник, простой штамп и кант | прежний удачный FIX сохранён, без смены варианта ради смены |
| pearl_fragment | FIX | A: узкий асимметричный скол; молочная грань, холодная тень, один локальный перламутровый акцент | новая самостоятельная32px sprite и generated model; прежний atlas/UV не переносился |
| shield_boss_fragment | FIX, сохранён предыдущий | A: несимметричный выломанный железный умбон с одной заклёпкой | прежний удачный FIX сохранён |
| old_buckle | FIX, сохранён предыдущий | A: бронзовая полая рамка с отдельным язычком | прежний FIX и вся held3D цепочка сохранены |
| bandit_token | FIX, сохранён предыдущий | B: дешёвый тёмный потёртый металл, неровный край и слабая метка | прежний FIX сохранён; не богатый герб |
| ataman_sign | FIX, уточнён | B: тёмное железо с тёплой инкрустацией; широкий поперечный силуэт из общего направления A/Б | прежний вертикально округлый медальон переработан в широкую пластину |
| voevoda_insignia | FIX, сохранён предыдущий | A: холодный ромб, узкая вертикальная деталь | прежний удачный FIX сохранён |
| princely_seal | FIX, сохранён предыдущий | **A: квадратный металлический штамп**, как прямо указано в новом решении | квадратная геометрия/различимый центр сохранены; в круглую восковую Б не превращалась |
| serpent_seal | FIX, уточнён | A: тёмный округлый обод и золотистая змея | буквоподобная S заменена головой с глазом, угловатой свернутой змеёй и суженным хвостом |

Производственные пути всех10: `assets/slavicmyths/models/item/<ID>.json` и active `textures/item/<ID>.png`, кроме old_buckle с отдельной GUI/held цепочкой (сохранена). Exact native size, SHA before/new и фактические пути — [verification.json](verification.json). Все размеры сохранены:16px у обычных sprites,32px у pearl_fragment/ataman_sign/serpent_seal. Новый pearl использует осмысленные2px кластеры на32px сетке; это новый рисунок, а не масштабирование прежней3D текстуры.

## Настоящие сравнения / границы рендера

- [Исходный до T2 → текущий результат всех10](original-before-after.png): одинаковая рамка, GUI1× и3×nearest.
- [Предыдущая частичная реализация → новая ревизия](before-after.png): явно видны3 фактически изменённых ID, шесть полезных FIX сохранены.
- [Девять предметов без подписей](nine-unlabelled.png), [T2-A](T2-A.png), [T2-B](T2-B.png), [T2-C](T2-C.png).
- [Исходные JSON/PNG перед ревизией](before/), [новый референсный борд](reference-board.png). Последний — только пояснительная схема, не production A/B.
- [Pearl: фактическая модель/UV/texture до и после](pearl-model-before-after.png), [унаследованные transforms](pearl-effective-transforms.json).

**Все панели — OFFLINE, не Minecraft screenshots.** В pearl-ряду GUI baseline получен из настоящего старого cuboid/default UV и PNG, а не из картинки UV atlas. После — source generated sprite; обе стороны в одной16-unit orthographic рамке без auto-fit. `tools/trophy111_source_preview.py` читает parent transforms из установленного `minecraft_1.21.1_client.jar`, JSON и реальные PNG; показывает GUI и отдельно first/thirdperson item-transform. Это изолированная модель, **без рук/игрока**, без Minecraft AO/shaders; не доказательство фактического хвата или runtime. Generated texture визуализируется texel prisms с nearest sampling. Источники сохранены, renderer воспроизводим. На руках нужна ручная игровая проверка.

## Реализация и воспроизводимость

`trophy111_art.py` теперь определяет9 FIX и пустой BLOCKED; old_button остаётся KEEP. Для pearl `write` воспроизводит PNG **и** отдельный `minecraft:item/generated` JSON без старых cuboid/UV/display overrides. Используются штатные generated-item parent transforms. `water_070.generate` имеет адресный guard до старой модели/texture, чтобы не вернуть3D cuboid. `vanilla_item_style` final overlay воспроизводит новый PNG/model. Остальные ранее добавленные scoped hooks сохранены и проверены. Проверка legacy использует только реальные AST statements/FunctionDef в temp-root; полные side-effect pipelines не запускались.

Все другие model/renderer/gameplay/registry/лут/рецепты/rarity/локализации/Creative/NBT остаются byte-exact относительно baseline. #112–114 production не менялись и не объявлялись исполненными. Индивидуальные дизайны по QA-кандидатам #114 этой ревизией не подменены девятью A/Б #111.

## Техническая проверка и точная сборка

`JAVA_HOME=C:/Program Files/Java/jdk-21.0.12`, `gradlew.bat clean build` — **PASS за56s**, compileJava FROM-CACHE; resources/JAR заново построены. [Лог](build.log). Minecraft1.21.1 / NeoForge21.1.255 / Java21.

`python tools/trophy111_source_preview.py`, затем `python tools/verify_trophy111.py` — PASS. 7442 production resources сверены с JAR; 3947 JSON прочитаны; 1339 PNG декодированы. TOML сравнивается со штатным processed output после version expansion. Fixture/tools не попали в production. Canonical/legacy regeneration проверены, включая отсутствие возврата pearl cuboid. [Checks](checks.log), [manifest](verification.json). Количество assertions не заменяет actual PNG/model artifacts.

- Metadata version: **1.9.7.48** (существующий номер не менялся, не новый опубликованный релиз).
- Production JAR: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.48.jar`
- Сохранённый review candidate: `D:/slavic-myths/build/trophy111/slavicmyths-1.9.7.48-trophy111-review.jar`
- Размер: **23505661 bytes**
- SHA256: `b8ad54a55845455b6936d2c92daa86da64cedc8207c61d2bfc80a0418be730b1`
- Source baseline: указанный HEAD + dirty source с byte manifest/diff; автоматического commit нет.
- **POLYMC_NOT_INSTALLED**: установленный48 не заменялся, этот SHA к installed JAR не приписывается.
- Minecraft/dev-client/GameTest/server/PolyMC запускавшихся этой работой: **0**.

## Матрица REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Новые решения #111 прочитаны; выбор A/Б мотивирован | стандарт, SVG, таблица10 ID | PASS |
| Дизайн pearl разблокирован и реализован2D | production JSON/PNG, source renderer | PASS |
|9 FIX +1 KEEP, без массовой замены удачных |manifest, A/B,3 изменённых PNG в этой ревизии | PASS offline |
| Square princely_seal A |настоящая PNG/модель/панель | PASS offline |
| GUI1×/3×, панель9 без подписей |original-before-after/nine-unlabelled | PASS |
| Actual model/UV before-after и transforms pearl |source renderer/source JSON/inherited parent | PASS offline; не hand runtime |
| ID/gameplay/данные/чужие изменения/#110 сохранены |полный src/tools byte audit и whitelist diff | PASS |
| Canonical/legacy generation и model guard |изолированные реальные entrypoints/guards | PASS |
| Java21 build, JSON/PNG/production JAR |build.log/checks.log/verification.json | PASS |
| Project «В работе» → «Тестирование» |доска read-only/без login, #111 в «Ожидании» | BLOCKED_ACCESS; требуется изменение владельцем |
| Реальная игровая приёмка/руки/сохранения |0 запусков, сценарии ниже | NOT_RUN / IN_GAME_OWNER_PENDING |
| Установка PolyMC |не выполнялась | NOT_RUN / POLYMC_NOT_INSTALLED |

Нет исполнительского asset/design/build блокера. Доступ к смене Project Status остаётся отдельным организационным ограничением; ручная приёмка — следующий этап, не PASS.

## Сценарии владельцу

1. В отдельной тестовой копии профиля проверить review candidate по SHA; старый JAR сохранить вне mods, исключить дубли. Пользовательские миры не заменять.
2. В «Материалы и трофеи» посмотреть все10 ID при обычном GUI scale, count overlays, светлом/тёмном фоне; девять новых предметов распознать без hover. Сравнить металл монеты/печатей/умбона и тёмную пластину атамана; у pearl должен быть острый асимметричный скол, не прямоугольный3D кусок.
3. Взять pearl в основную/вторую руку, проверить1P/3P и dropped item, отсутствие исчезновения/клиппинга/неподходящей посадки. Offline transform preview не заменяет этот тест. У old_buckle GUI новая иконка, held3D путь сохранён — проверить обе формы.
4. Проверить прежние stacks этих ID, loot/рецепты-потребители, count/rarity и реальный перезаход на копии мира. Ожидается только новый рисунок/адресный тип модели pearl.
5. Замечания фиксировать ID + GUI scale/ракурс/screenshot. Issue остаётся OPEN до явного решения владельца; #92 не завершена этой партией.
