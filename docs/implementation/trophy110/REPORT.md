# #110 · T1 · 21.10.2026

Технический результат подготовлен для ручной приёмки. **Issue OPEN / IN_GAME_OWNER_PENDING**.
Рекомендуемый Project Status: **Тестирование / требуется изменение владельцем**.
Фактически Project №9 показывает «Ожидание»: браузер без авторизации, GitHub connector не предоставляет Projects mutations. Статус «В работе» также не удалось выставить; статусные labels не создавались.

## Scope и provenance

Родитель #92, только семь ID #110, партии T1-A (3), T1-B (2), T1-C (2).
HEAD `69a2db12de4baac9cef7c43e6c4b0ae717d3b644`, **dirty working tree**: исходные 94 чужих status entries сохранены. Commit/push не выполнялись. Версия оставлена существующей **1.9.7.48**, это локальный кандидат #110 поверх текущего рабочего дерева, а не новая опубликованная версия.

Исходная карта SHA: [workspace-before.json](workspace-before.json); собственный diff поверх этого состояния: [workspace-diff.json](workspace-diff.json). Исторические изменения брони/рун входят в исходное дерево и сборку, но не относятся к #110. Registry, рецепты, лут, свойства, локализации, NBT, creative order, Java/runtime, модели и display anchors этой работой не менялись.

Прочитаны AGENTS.md, PROJECT_STATUS/ROADMAP/ARCHITECTURE, полные #92/#110 и комментарии. Отсутствующие локально PROJECT_WORKFLOW и CODEX_PROJECT_PLAYBOOK прочитаны через GitHub main. Референсы: секция A TROPHIES_92_PIXEL_CONCEPTS.svg, ITEM_ICON_BLUEPRINTS.svg, настоящий скрин 2 владельца (дубли также видны в baseline). Полный roadmap не исполнялся. Других активных Codex чатов не обнаружено; main writer в этой работе один. Существующий worktree advancements-v3 не изменялся.

## Production A/B

[Общий лист](before-after.png), отдельные семейства: [T1-A](T1-A.png), [T1-B](T1-B.png), [T1-C](T1-C.png).
**Это offline PNG previews, не игровые скриншоты.** Слева baseline, затем result, справа binary alpha mask. Одинаковая рамка, 1× на светлом/тёмном фоне, 3× nearest на тёмном фоне. В [before/](before/) сохранены реальные исходные production PNG.

| ID | Конкретный исходный дефект → FIX | Новая форма / материал |
|---|---|---|
| upyr_fang | один bone sprite на три предмета | узкий прямой клык, корень с малым красным акцентом, слоновая кость |
| klyk_volkolaka | тот же bone sprite | массивное основание, открытая спинка и загнутый зуб, беж/серо-коричневая тень |
| nochnoy_kogot | светлая кость вместо когтя | тонкий серп-крюк с внутренней выемкой, графит/холодный фиолетовый блик |
| ovinnaya_zola | та же кучка с огненными вкраплениями | низкий горизонтальный пепел, 2 отдельные хлопья, ровно 3 серых тона |
| iskra_ovinnika | та же кучка золы | вертикальный уголёк с золотым ядром и 3 отдельными искрами |
| pylayuschaya_cheshuya | общий красный blob с кожей | острая ромбовидная пластинка с ребром и локальным огненным кантом |
| tugarinova_kozha | тот же blob | широкий рваный лоскут, кожаные складки, локальная красная потёртость |

Все actual model paths: `src/main/resources/assets/slavicmyths/models/item/<ID>.json`, parent `minecraft:item/generated`, layer0 `slavicmyths:item/<ID>` → `textures/item/<ID>.png`. Native 16×16 RGBA сохранён. Это generated item extrusion, отдельных ModelPart/UV/renderer нет; модель JSON побайтово сохранена. SHA old/new, bbox, actual paths, components и попарная разница масок записаны по каждому ID в [verification.json](verification.json). Отличия масок внутри семейств: 45.65–76.54% от объединения, а не один изменённый пиксель. Это техническая метрика формы, не замена художественной приёмки.

## Генераторы и сохранность

`tools/trophy110_art.py` — отдельные явные pixel maps. Пять scoped hooks: `vanilla_item_style.sprite`, `kurgan_086.sprite`, `hunt_090.art`, `hunt_091.art`, `hunt_092.art`. Перегенерация только семи ID: `python tools/trophy110_art.py <ID> ...`. Семейства не перекрашиваются из общего шаблона.

Во время первой проверки я ошибочно импортировал legacy-модуль, чьи top-level imports запустили посторонние генераторы. 15 файлов вне scope восстановлены из уже собранного JAR **только после проверки SHA256 на совпадение с сохранённым исходным workspace manifest**. Чужие dirty-ресурсы также восстановлены. [Карта восстановления](recovery-plan.json). Повторная проверка всех исходных src/tools и трёх status docs подтверждает, что вне whitelist отличий не осталось. Верификатор исправлен: исполняет настоящую FunctionDef через AST в temp-root без импорта legacy-модулей и без запуска полной цепочки. Прямые и финальный style entrypoint воспроизводят те же production байты/пиксели. Повторный clean build после восстановления выполнен.

## Сборка

`JAVA_HOME=C:/Program Files/Java/jdk-21.0.12`, `gradlew.bat clean build` — **PASS**, финальный запуск52s (первый80s). [Лог](build.log). Minecraft1.21.1 / NeoForge21.1.255 / official mappings. В первом полном compile были существующие deprecation/unchecked warnings, не ошибки. Финальный compile FROM-CACHE; resources/JAR пересобраны.

- Production JAR: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.48.jar`
- Версия metadata: **1.9.7.48**
- SHA256: `d22d40b68d31ebdf97efa2d7f3c3cf8f8ec093fe375616a645d90314a8455b50`
- Размер: **23506233 bytes**
- Сохранённая копия кандидата: `D:/slavic-myths/build/trophy110/slavicmyths-1.9.7.48-trophy110.jar` (те же байты).
- Ресурсов с JAR parity: **7442**; neoforge.mods.toml сверяется с processed resource после штатной подстановки версии. Остальные ресурсы — с production source.
- Полный verifier: `python tools/verify_trophy110.py`; [результат](checks.log).
- **POLYMC_NOT_INSTALLED**. Существующая установленная48 не является этим кандидатом; её SHA/backup этой работой не менялись.
- Реальных запусков Minecraft/dev client/GameTest/server/PolyMC: **0**.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| 7 production assets, native16px | verification.json, реальные PNG | PASS |
| Уникальные осмысленные формы, три партии3/2/2 | T1-A/B/C, маски, утверждённые референсы | PASS offline; OWNER_PENDING in-game |
| 1×/3× actual baseline/result | before-after.png, before/ | PASS |
| ID/модели/gameplay/data/чужие изменения сохранены | workspace-before.json + workspace-diff.json; повторный byte audit | PASS |
| Генераторы не откатывают семь PNG | изолированные реальные art functions + final style | PASS |
| JSON/PNG/resource и JAR parity, smoke excluded | verification.json | PASS |
| Java21 clean build | build.log | PASS |
| Project «В работе» → «Тестирование» | доска доступна read-only без login | BLOCKED_ACCESS; требуется изменение владельцем |
| Игровая визуальная/функциональная приёмка | сценарии ниже; запусков0 | NOT_RUN / OWNER_PENDING |
| Установка PolyMC | не выполнялась, не заявлена как установленная | NOT_RUN / POLYMC_NOT_INSTALLED |

## Сценарии владельцу

1. На отдельной копии тестового профиля установить именно кандидат по SHA, исключив второй JAR мода и сохранив старый JAR вне mods; пользовательские миры не заменять.
2. Во вкладке «Материалы и трофеи» сравнить три семейства рядом при обычном GUI scale; распознать клык/зуб/коготь без hover и названий, золу/искру и чешую/кожу без опоры на цвет. Проверить также светлый/тёмный фон и count overlays.
3. Для каждого ID проверить hover RU/EN, отсутствие missing texture; взять в основную/вторую руку, посмотреть 1P/3P и выброшенный item при обычных движениях. Это статические generated sprites, новые анимации не добавлялись.
4. На тестовом сохранении проверить прежние stacks этих ID, получение из существующего loot/рецепты-потребители и мешочек трофеев. Убедиться, что меняется рисунок, а не количество, редкость или использование. Проверить перезаход, существующие stacks должны сохраниться.
5. При дефекте записать ID, GUI scale/ракурс и screenshot; Issue остаётся OPEN. Только владелец принимает результат. #111–114 не запускались.
