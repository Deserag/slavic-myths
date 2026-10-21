# Все деревья мода и производные деревянные поверхности · 21.10.2026

**Source scope нового поручения реализован и технически проверен / IN_GAME_OWNER_PENDING.** Issue #106/#107 и родитель#91 остаются OPEN; owner-accepted не выставлялся, закрытий нет. Для #106 рекомендуется «Тестирование», поле недоступно для изменения (connector без Projects mutations, последняя доступная browser session read-only). Игровая приёмка всех пород и производных не заявляется.

## Новое поручение и границы

После положительной оценки пилота липы владелец явно поручил **все добавленные деревья и блоки, производимые из этой древесины**, с одним общим результатом. Это разрешение продолжать остальные семейства без промежуточных остановок после каждого пилота. Визуальное одобрение липы в чате не выдаётся за игровой PASS остальных пород/функций.

Прочитаны актуальные #91/#106–109, AGENTS/project docs, approved direction и PIXEL_ART_GUIDE. В каталоге проверены все **6 семейств /85 записей**: липа15, ива15, яблоня16, сосна15, рябина15, потемневшая9. Это85 записей, а не85 плохих PNG; исправные поверхности KEEP. Саженцы/листва/плоды не перерисовывались. Никакого нового silhouette furniture redesign или повторной перерисовки курганногоV2/#109.

HEAD `69a2db12de4baac9cef7c43e6c4b0ae717d3b644` + dirty tree, **без commit/push/новой ветки/PR**. [SHA baseline](workspace-before.json), [status до начала](git-status-before.txt), [точный diff](workspace-diff.json). Все исходные Java/model/blockstate/loot/recipe/lang/registry/Creative/NBT файлы, чужая работа и #110/#111 сохранены byte-exact. Новых source ресурсов ровно2 PNG по отсутствовавшим active apple sign paths.

## Конкретный результат по породам

| FAMILY | Основная древесина | Производные |
|---|---|---|
| Липа |3 одобренных pilot PNG и2 торца KEEP |обновлены отдельные door/top/bottom/trapdoor/item door/sign/sign atlas |
| Ива |planks/log/stripped_log FIX: оливковые плавные короткие волокна, редкий вытянутый сучок; оба торца KEEP |двери/люк/иконки/атлас согласованы с новой основой |
| Яблоня |planks/log/stripped_apple_log FIX: тёплые плотные смещённые волокна и овальный сучок; оба торца KEEP |двери/люк/sign+hanging sign/атласы согласованы, исправлены active Material paths |
| Сосна |planks/stripped_log FIX: золотистый материал, отдельный рисунок коротких волокон/сучка; пластинчатая кора и торцы KEEP |двери/люк/sign и мебельные consumers обновлены по материалу |
| Рябина |planks/stripped_log FIX: плотный короткий рисунок; характерная кора с поперечными отметинами и торцы KEEP |двери/люк/sign/атласы согласованы |
| Потемневшая |подтверждённые full-length полосы/сетка в planks/log/stripped_log FIX: короткие нерегулярные трещины/волокна при сохранении тёмного материала; оба торца KEEP |двери/люк/item door согласованы, металлические полосы/ручки сохранены |

**68 контролируемых PNG:17 KEEP /49 FIX /2 ADD_MISSING_ACTIVE_PATH.** Фактически изменилось51 текстура:13 основных поверхностей +38 зависимых текстур/путей. Липовые3 исходные pilot PNG и все12 торцов, кора сосны/рябины сохранены побайтово. У каждой породы собственные layout/ритм/сучки, не перекраска одного шаблона; автоматический контроль подтверждает6 разных масок value clusters у досок.

[Полная карта85 ID](CATALOG.md), [точные68 file paths/SHA old/new/native sizes](ASSETS.md), [машиночитаемый manifest](verification.json). Имя `stripped_apple_log` сохранено, не подменено выдуманным `apple_stripped_log`.

## Деревянные блоки, мебель, двери и таблички

Проверены item parent и все blockstate variants/multipart **80 timber записей**;5 leaf records KEEP. Геометрия/display/UV всех моделей сохранены. Ступени, плиты, заборы, калитки, кнопки и нажимные плиты используют обновлённую основу через existing texture refs. Выявлены **277 прямых model consumers** контролируемых PNG — это число model files, а не число исправленных уникальных предметов.

Отдельные PNG дверей/люков/предметных иконок/sign atlases синхронизированы с породой. При этом сохранены исходные alpha masks, размер каждого PNG, рамы, вырезы, перекладины, металл, ручки и прежние UV. Native16px липы и32px других основ сохранены;16×32 door icons/64×32 entity atlases не ужимались массово.

Семейная мебель липы/сосны, деревянный ящик и другие models с family texture refs автоматически наследуют материал. Generic/vanilla независимые материалы проверены отдельно: например kitchen_table использует vanilla dark_oak_planks, wooden_tub собственную текстуру, household изделия общий household_wood, прялка textile_wood, погребальная колода собственный aged atlas. Они **не являются вариантами определённой новой породы** и сохраняют свои материал/форму. Рецепт с общим tag planks создаёт один общий ID и не хранит использованную породу — создание новых6 вариантов означало бы изменение gameplay/NBT/registry, которое не поручалось. Их возможные самостоятельные дефекты остаются отдельным #108, а не маскируются массовой заменой дерева.

**Исправление яблоневых табличек:** реальный WoodType `slavicmyths:apple`, установленный NeoForge1.21.1 Sheets сохраняет namespace и добавляет `entity/signs/` либо `entity/signs/hanging/` к path. Оригинал содержал лишь лишний уровень `.../signs/slavicmyths/apple.png`. Добавлены правильные active PNG `assets/slavicmyths/textures/entity/signs/apple.png` и `.../hanging/apple.png`; старые2 файла не удалены и синхронизированы. [Installed bytecode](Sheets-bytecode.txt), [path constants](Sheets-sign-path-constants.txt). Это статически доказанная ресурсная ошибка/исправление; отображение текста и реальное размещение ещё требуют игры.

## Настоящие сравнения

- [Общий результат: реальные planks/log/stripped models шести пород](six-family-result-models.png).
- [Native/3×nearest/2×2 surfaces, исходник и результат всех семейств](six-family-surfaces-before-after.png).
- Полные item panels1×/3× для [липы](linden-all-items-before-after.png), [ивы](willow-all-items-before-after.png), [яблони](apple-all-items-before-after.png), [сосны](pine-all-items-before-after.png), [рябины](rowan-all-items-before-after.png), [потемневшей](darkened-all-items-before-after.png).
- [Мебель/универсальные изделия: actual source before/after](furniture-before-after.png), [брёвна/торцы с четырёх ракурсов](all-log-four-views.png).
- [Все89 отрендеренных item sources/poses](rendered-items.json), [модельные зависимости](dependencies.json).

**Все изображения OFFLINE, не Minecraft screenshots.** Source renderer использует настоящие неизменённые JSON/UV, текущие/архивные PNG и installed vanilla1.21.1 parent models/display transforms; одинаковые камеры и масштаб, nearest sampling. Memory-only atlas строится из exact source pixels с face UV remap, не пишется в production. Нет Minecraft AO/shaders/света мира/игрока. 2×2 — повторение текстур;4 camera yaw — дополнительные source ракурсы, не runtime размещение. Иллюстрации референса не выдаются за production до/после.

## Воспроизводимость

`tools/decor_all_woods.py` — scoped canonical writer с независимыми authored motifs, без случайного шума. `decor_wood_templates.zip` содержит70 исходных PNG templates для точного сохранения construction overlays/alpha/KEEP ресурсов. Это необходимые source masters генератора, не данные из других игр. SHA archive и каждого template сохранены в verification.json. Новый рисунок заменяет только wood surface, не перекрашивает ручки/металл/вырезы.

`woodlands_0801.texture`, `gardens_resources.save` имеют scoped guards; rc2_visual_resources и burial_084 имеют current timber final overlay, чтобы исторические проходы не откатывали утверждённую древесину. Полные legacy pipelines не запускались: реальные AST FunctionDef/call nodes исполнены только в temp-root. Повторный canonical apply воспроизводит те же bytes всех68 PNG, включая active apple paths. Код тестов/masters не входит в production JAR.

## Сборка / точный артефакт

Java21 `JAVA_HOME=C:/Program Files/Java/jdk-21.0.12`, `gradlew.bat clean build` — **PASS за49s**, compileJava FROM-CACHE, ресурсы/JAR пересобраны. [Build log](build.log). Minecraft1.21.1 / NeoForge21.1.255 / official mappings.

`python tools/decor_all_preview.py`, `python tools/verify_all_woods.py` — PASS применимых offline gates: **7444 production resources** совпадают с JAR, **3947 JSON** прочитаны, **1341 PNG** декодированы. neoforge.mods.toml сравнивается с processed resource после штатной подстановки версии. Fixtures/tools не попали в JAR; полный baseline src/tools audit сохраняет чужие правки. [Checks](checks.log).

- Версия metadata: **1.9.7.48**, существующий номер не менялся; локальный review candidate, не опубликованный релиз.
- Production JAR: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.48.jar`
- Сохранённая копия: `D:/slavic-myths/build/decor-all-woods/slavicmyths-1.9.7.48-all-woods-review.jar`
- Размер: **23491178 bytes**
- SHA256: `60254dd95066daee7c56808ecd9f3269ffef0de88e29ff127a0b14aae68660f6`
- Source: HEAD выше + dirty tree, provenance в byte baseline/diff; нового source commit нет.
- **POLYMC_NOT_INSTALLED**. Уже установленный48 не заменялся; JAR assembled не объявляется installed.
- Реальных запусков Minecraft/dev client/GameTest/server/PolyMC: **0**.

## Матрица REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
|Все6 пород,85 catalog records |CATALOG/dependencies |PASS |
|Разные зерно/материал/силуэты пород,не recolor одного шаблона |actual surface/model panels,6 value layouts |PASS offline; OWNER_PENDING художественно |
|Липовый пилот,хорошая кора/торцы сохранены |17 byte-exact KEEP |PASS |
|Производные дерево/мебель с family refs наследуют дизайн |277 direct model consumers,89 item renders |PASS offline |
|Отдельные door/trapdoor/sign PNG обновлены |manifest/native size/alpha parity/A-B |PASS offline |
|Active apple sign paths |installed Sheets bytecode +2 PNG/JAR |PASS static; runtime NOT_RUN |
|Geometry/display/UV/gameplay/ID/recipes/NBT/чужие правки |полный baseline hash whitelist |PASS |
|Reproducible masters/legacy guards |temp-root actual functions +canonical bytes |PASS |
|Java21 clean build/resources/JAR |build49s/checks/actual artifact |PASS |
|Creative1×/3×/side/top/bottom/2×2 source proof |offline actual PNG/model panels |PASS offline |
|Реальное размещение,ось log,свет,redstone,sign text,мебель/storage |0 game launches |NOT_RUN / IN_GAME_OWNER_PENDING |
|Project Status update |connector без Projects mutation,read-only browser |BLOCKED_ACCESS; изменение владельцем |
|PolyMC installation |не выполнялась |NOT_RUN / POLYMC_NOT_INSTALLED |

Нет незавершённой texture/build/generator части нового поручения. Ручная визуальная/игровая приёмка и реальный GUI/world остаются отдельным этапом. #106/#107/#91 OPEN; #108 geometry и #109 V2 не объявлены готовыми.

## Сценарии владельцу

1. В отдельной тестовой копии профиля установить именно review JAR по SHA, сохранить предыдущий вне mods и исключить дубли. Пользовательские миры не заменять.
2. В Creative сравнить все6 пород и80 timber items; обычный GUI scale/count overlays, plank/log/wood/stripped variants, doors/trapdoors/buttons/plates/signs. Проверить, что самостоятельные иконки не выбиваются из цвета/фактуры своей породы.
3. Поставить для каждой породы2×2 стены/пол/потолок, брёвна x/y/z, stairs/slab/fence/gate во всех ориентациях, normal/stripped/top/bottom. Сравнить при дневном/ночном освещении; проверить отсутствие чрезмерных швов/рябящих повторов.
4. Двери: facing/half/hinge/open; люки: half/open; кнопки/плиты: размещение и redstone; таблички/подвесные яблоневые таблички: оба способа размещения, текст, редактирование/воск. Особое внимание правильному apple Material path и отсутствию missing texture.
5. Мебель липы/сосны и деревянный ящик: сохранённая форма, новая поверхность, взаимодействие/storage/NBT и существующие вещи в старом тестовом сохранении. Generic независимые материалы сохраняются осмысленно,не обязаны копировать любую выбранную при крафте породу.
6. Реальный перезаход/существующие stacks и блоки; при дефекте записать точный registry ID,вид/ракурс,GUI scale и screenshot. Принимает/закрывает только владелец.
