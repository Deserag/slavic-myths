# #296 — IN_PROGRESS / OWNER_PENDING

База hell 50cfd72de3e6569e327da5d0da9ee673c36fa7e3, рабочее дерево чистое, origin совпадает. #295 опубликован, его остаток не скрывается и в этом пакете не реализуется. Main не изменяется. Minecraft/client/server/GameTest/PolyMC для #296 не запускаются; visual/relog/MP NOT_RUN.

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Координаты книги | TaleScreen, TaleService.publicHints | Открыть известную запись/поиск | Production formatter с public NBT | Actual X/Z/Radius, без Anchor/Target/Y |
| Координаты маркеров | NavigationScreen, SearchArea/SlavicMarker | Открыть навигацию выбранного поручения | Formatter, actual SearchArea.generate/read | Своя область, свой radius/state, не данные Hints |
| Статусы | Public stale/expiry/dimension, marker state | Подойти/сменить измерение/обновить подсказку | Missing/expired/cross-dimension/inside fixtures | Никаких фиктивных координат |
| Читаемость RU/EN | Общий component formatter + cached wrapping/scroll | Прочитать область без условных фигур | Автономные math/translation/layout bounds | Не зависит от компаса, весь текст доступен прокруткой |
| Совместимость/privacy/performance | Не меняются протоколы, saved models и generators | Старые подсказки и задания | NBT roundtrip, source/JAR guards | Нет нового поиска/chunk loading/render polling |
| Реальный UI 854x480/1920x1080 | Minecraft client | GUI Scale 1..auto | Запуск запрещён для этого scope | NOT_RUN, отдельная визуальная приёмка |

## До и после

До: `TaleScreen.renderMap` показывал пунктирный круг, точку игрока, «Север», относительное направление и грубую дистанцию. Координат центра/краёв не было; пояснения ограничивались 2–3 строками. `NavigationScreen` давал короткие усечённые строки dimension/distance/state по57px на marker.

После: в книге известная поисковая запись сразу открывает числовые сведения; прежняя кнопка использует новый текст «Где искать» и переключает к тексту записи. Новая подсказка, пришедшая через snapshot, открывает координаты; удалённая подсказка возвращает обычную страницу/серверный статус поручения. Закрытая пользователем координатная страница не принудительно открывается каждым обновлением прежней подсказки. Круговая иллюстрация и её zoom-кнопка убраны; дерево деяний и его zoom не менялись.

В навигации marker показан отдельной карточкой на странице с прежними track/hide/forget, пагинацией, названием без усечения и собственной прокруткой. При смене marker прокрутка сбрасывается, при обновлении прежнего сохраняется/ограничивается размером текста. В узком GUI длинные границы/примечания переносятся штатным font.split, а не обрезаются через plainSubstrByWidth/maxRows.

Оба экрана используют один формат:
- измерение, состояние, примерный центр X/Z, **свой фактический radius**;
- границы X/Z, округлённые наружу, и пояснение: это охватывающий прямоугольник круга, его углы не входят в область;
- внутри области / расстояние до её края, только в правильном измерении;
- stale/expired/completed/ended не выдают старые координаты как актуальные;
- отсутствующие, неверные, NaN/Infinite значения, Radius0/negative/fractional не превращаются в X0/Z0;
- у области поиска **нет Y**. Известный точечный marker получает X/Y/Z только из уже публичного SlavicMarker после существующей серверной discovery-логики; приватные assignments/Anchor не читаются.

Обычный hint без авторитетного флага открытия называется «Область поиска известна», а не выдуманным «цель найдена». SEARCHING/UNAVAILABLE и подготовка/возврат поручения по-прежнему выводятся из существующих YagaSearch/GuideQuests в обычной странице книги. Новых правил обнаружения/победы не вводилось.

## Источники и изменённые файлы

- `client/TaleScreen.java`: числовой renderMap, cached component/wrapping, обычная прокрутка, обновление/удаление hint, выбор поисковой страницы.
- `navigation/client/NavigationScreen.java`: полный текст карточки, pagination/scroll, существующие действия marker сохранены.
- `navigation/SearchGuidance.java`: общий display formatter. Hint adapter читает только публичные X/Z/Radius/Dimension/Expires/Stale; marker adapter читает **свой SearchArea**, не Hints и не marker.x/y/z-заглушку поисковой области.
- `navigation/SearchCoordinates.java`: только display math, отрицательные координаты/границы/scroll; не новая сохраняемая модель.
- `tales/TaleService.java`: заменена только операция public projection, прежние проверки устаревания/синхронизация сохранены.
- `tales/TaleHintVisibility.java`: whitelist прежних публичных полей с copy; Target, Anchor, неизвестные приватные поля и Y не попадают в hint wire; исходный SavedData hint не мутирует.
- `assets/slavicmyths/lang/ru_ru.json`, `en_us.json`: только новые `navigation.search.*`, старые строки не переписаны.
- `tools/navigation296_resources.py`: безопасный scoped overlay двух локализаций. Повторный запуск даёт byte drift0; legacy asset/audio generators не запускались.
- `tools/navigation296-offline.gradle`, `tools/navigation296-offline/Navigation296Offline.java`, `tools/verify_navigation296.py`: отдельные проверки, отсутствуют в production JAR.

`SearchArea`, `SlavicMarker`, `NavigationState`, `NavigationNetwork`, `NavigationManager`, `TaleData`, `TaleSnapshotMerge` **совпадают с baseline**. Форматы сохранений, сетевые версии, выдача/открытие цели, приватные encounter records и генерация не менялись. Shared helpers не зависят от client-классов. Source audit разрешает только перечисленные production-файлы.

## Примеры действительных production-значений

[offline-examples.json](offline-examples.json) получен выполнением **настоящего SearchArea.generate + нового formatter** в отдельной JVM, с известными тестовыми UUID/salt и quality predicate=true. Это не реальные серверные worldgen/биомы и не координаты из пользовательского мира. Подбор радиусов и offset вычислял production-код, не таблица UI.

| Scale | Примерный центр X / Z | Radius | Границы X | Границы Z |
|---|---|---|---|---|
| MINI | −1551.3 / 547.1 | 196 | −1748 … −1355 | 351 … 744 |
| RARE | −1519.8 / 259.7 | 305 | −1825 … −1214 | −46 … 565 |
| WORLD | −1148.3 / 582.9 | 508 | −1657 … −640 | 74 … 1091 |
| UNIQUE | −1136.9 / 632.7 | 582 | −1719 … −554 | 50 … 1215 |

Отдельный источник книги не подменялся этим генератором: базовый `TaleService.hint` по-прежнему создаёт radius250; `GuideDiscovery` использует прежние180/320 по прогрессу Баюна; поручения могут копировать свою production SearchArea. UI читает реально переданное число, не назначает всем400 и не меняет баланс.

Пример первой строки в RU:
```
Где искать: Обычный мир
Область поиска известна.
Примерный центр: X −1551.3, Z 547.1
Радиус поиска: 196 блоков
Границы по X: от −1748 до −1355
Границы по Z: от 351 до 744
Ищите цель внутри указанной круглой области.
```
Далее выводятся пояснение прямоугольника и фактическое положение игрока относительно круга. EN использует те же данные и правила. Точный приватный target в эти строки не подставляется.

## Автономные проверки

- Java21: `gradlew.bat -I tools/navigation296-offline.gradle -I tools/yaga272-offline.gradle clean build verifyNavigation296Offline verifyYaga272Offline` — **PASS, финальный clean build3m51s**.
- После уточнения горизонтальной матрицы fixture отдельно повторён `verifyNavigation296Offline` — **5068 PASS**, 41с. Production Java после финального clean build не менялся.
- Нативные `Component`/Language RU/EN, NBT public projection и TaleData/SlavicMarker roundtrip, snapshot merge, два изолированных UUID, invalid/missing values, finite checks, expires/state/dimension, actual production SearchArea radii, negative zero/world limit/bounds — PASS.
- #272 regression — **76 PASS**, bare JVM, не Minecraft.
- `gradlew.bat -I tools/book-offline.gradle checkBookOffline` — **50289 PASS**, прежние UiText.Layout cache / UiLine / TaleViewport. Используется native StringSplitter с **фиксированной шириной glyph6**, а не реальный font/render/GPU. Результат сохранён в evidence/book-cache-regression.json, старый benchmark JSON восстановлен байт-в-байт. Allocation fixture не выдаётся за FPS игры.
- Расчёт logical viewport: 854×480 при scale1/2; 1920×1080 при scale1/2/3/4; боковая панель раскрыта/скрыта. Проверены горизонтальные/вертикальные границы и доступ к последним строкам для разного количества wrapped lines. **Font rendering/GPU/реальный экран/перенос Minecraft в runtime не проверены**. Геометрический fixture не выдаётся за скриншот.
- `python tools/verify_navigation296.py` — PASS: scope, отсутствие gameplay/registry/worldgen/protocol/saved-model изменений, серверные проверки stale сохранены, no private placeholder leakage, RU/EN placeholder agreement, overlay drift0, классы JAR совпадают с compiled main classes, test classes отсутствуют.
- `python tools/verify_resources.py` — PASS: 8001 ресурса,4402JSON,1443PNG,1886models,1947Java21 classes.
- `git diff --check` — PASS. Логи и исходные SHA — evidence/, source-jar.json, receipt.json.
- **Minecraft client/server/GameTest/PolyMC запусков для #296: 0.** Прошлые запуски #295 не засчитываются в #296. Никаких JFR/GPU/драйверных экспериментов.

Числовой текст собирается максимум при изменении client game tick, entry/snapshot/language; font split кешируется существующим UiText.Layout по component/width/font/language. В render нет поиска, запросов chunks или packets. Каталог/recipes/deeds caches #235 и placement #274/#295 не изменены. Это source evidence, **не измеренные FPS/MSPT**.

## JAR и установка

Версия **2.0.1-HELL**, файл `D:/slavic-myths/build/libs/slavicmyths-2.0.1-HELL.jar`.

SHA256 **d909681e9478bb0818ed0f39aafc6b2d656924255dd5390a6ae96bc234a0a91d**.

Точная база/исходники — receipt.json; source commit добавлен квитанцией публикации после фиксации кода. Этот JAR содержит также ранее опубликованные изменения #295, не является принятой всей цепочкой/релизом.

**POLYMC_NOT_INSTALLED.** Только read-only проверка прежнего установленного JAR: `C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/.minecraft/mods/slavicmyths-2.0.1-HELL.jar`, SHA273767789fb6555f16c485bf64bc2cf37627787f1ca50255d820bbe36660182c. Новый JAR туда не копировался; config/options/saves не читались/не изменялись, основной PolyMC не запускался. Backup установки не выполнялся, поскольку установки не было.

## Матрица результата

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Реальные X/Z/radius каждого источника | Production formatter/adapters, native generated examples, marker placeholder poison tests | PASS |
| Корректные отрицательные значения, круг/границы/world limit | Native math tests; radius180/250/320/380/550/700 | PASS |
| Нет выдуманных координат при invalid/missing/expired | Explicit invalid/stale/expiry/state assertions | PASS |
| Inside/outside/dimension/status из публичных данных | RU/EN native Component fixtures; stale/expires/marker states | PASS |
| Не выдавать Target/Anchor/Y поисковой области | Public wire projection, original SavedData preserved, SearchArea vs marker point tests | PASS |
| Обновление/удаление hint и кеши | Source receive/load wiring, native hint delta merge; реальный экран отдельно | PASS |
| Сохранённая совместимость | Unchanged models/protocols; native NBT roundtrip and UUID isolation | PASS |
| Полный текст/scroll, layout arithmetic | Source full font.split + clipped viewport/scroll + resolution matrix | PASS |
| Реальный GUI854×480/1920×1080 и GUI Scale/RU/EN | Клиент запускать не разрешено | NOT_RUN |
| Настоящий relog, старый игровой мир, два клиента MP | Не запускались; NBT fixture это не rejoin | NOT_RUN |
| Реальные FPS/frame time/network | Нет игрового профиля | NOT_RUN |
| Java21/resources/production JAR | Final gates, scoped check, resources.json | PASS |
| Приёмка владельца | OWNER_PENDING, #296 OPEN, фактический Project Status «В работе» | NOT_RUN |

## Как проверить в Minecraft владельцу

Сначала **вручную установить** JAR с указанным SHA в отдельную копию HELL1.21.1/NeoForge21.1.255/Java21, сохранив прежний JAR и копию мира; исключить двойные mod JAR. Этот пакет сам ничего не устанавливал. Сначала новый мир для обычного пути, затем копия старого мира с уже известными подсказками/поручениями.

1. **Без OP:** получить сведения/поручение у Баюна или Яги штатным способом. В Книге выбрать соответствующую поисковую запись. Ожидание: сразу понятные dimension/центр/radius/границы; X/Z соответствуют предоставленной области, Y отсутствует. Название записи видно над подсказкой. «Где искать» переключает к числовым сведениям/тексту записи.
2. По vanilla F3 сравнить свою X/Z с центром; подойти внутрь радиуса и отойти за край. Ожидание: статус внутри/вне меняется, снаружи показана дистанция **до круга**, не до точного босса. Точка у угла охватывающего прямоугольника, но вне окружности, не должна считаться внутри.
3. Открыть страницу навигационных маркеров, выбрать поручение стрелками. Ожидание: та же форма числовой информации, но данные своего SearchArea; разные задания могут иметь разные радиусы. Track/stop, hide/show, filters/preferences, forget у разрешённых permanent markers сохраняют прежние действия. При смене marker текст начинается сверху.
4. Пройти в другое измерение обычным порталом и снова открыть книгу/навигацию. Ожидание: указано измерение **цели**, сообщение о другом измерении, нет ложного «Вы внутри»/локальной дистанции в чужом измерении. Вернуться — правильный inside/outside восстановится.
5. Обновить существующую подсказку у NPC/задание штатным действием. Ожидание: новые публичные значения сразу видны; после удаления подсказки не остаётся зависшая старая карта, показывается обычный статус поручения. Для имеющейся старой/устаревшей подсказки координаты не выдаются как актуальные. Если такого save нет, реальная expiry-проверка остаётся NOT_RUN; `/time add` не заменяет gameTime expiry.
6. **Небольшой и большой экран:** 854×480, GUI Scale1/2; 1920×1080, GUI Scale1/2/3/4 и Auto. Повторить RU/EN, оглавление раскрыто/скрыто, обычный/полноэкранный режим. Прокрутить до последнего примечания/статуса. Ожидание: текст полностью доступен, не лезет на нижние кнопки, не обрезан в2–3строки, числовые поля не превращаются в raw translation keys.
7. Сохранить/выйти/зайти в копию старого мира. Ожидание: открытые записи, прежняя область, выбранное отслеживание и прогресс поручений сохранены. При повторном открытии известной поисковой записи сведения читаются с начала. Реальный исходный секретный anchor не должен внезапно появиться; точечный X/Y/Z допустим только после прежнего серверного discovery.
8. **MP, два настоящих игрока:** разные поручения/области; один обновляет подсказку/переходит в Nether/перезаходит. Второй не получает чужие данные/точный секретный target; собственное отслеживание сохраняется. Этот тест в пакете NOT_RUN.

OP-команд для обычной проверки интерфейса не требуется. Книга и NPC не должны запускать генерацию из render; #295 pipeline в этом пакете не менялся.

## Риски и откат

Реальный font/layout при разных масштабах и сторонних resource packs ещё не оценён. Плотность списка NavigationScreen стала одной полной карточкой на страницу, чтобы вместить явные сведения; листание/фильтры и действия сохранены, но удобство требует owner feedback. Техническое ограничение координат±29999984 известно локально; произвольный меньший server worldborder не передаётся старым snapshot и не вычисляется экраном. Нельзя считать clamped bounds проверкой любого custom border.

Риски соседних функций: открытие/обновление записи во время квеста, возвращение из карты в lore после удаления hint, сброс/сохранение scroll при смене marker, resource language reload, старые overlays. Кеши каталога/рецептов/дерева, состояния побед/награды/генерация и optional map integration не переписывались. Runtime регрессии остаются NOT_RUN.

Откат — обратный commit #296 в `hell`, без reset/force и без перезаписи миров. Формат сохранений не менялся; новые public projections не удаляли приватные данные из TaleData. Source commit и JAR SHA обязательны при сравнении.

## Передача дальше

[HANDOFF_278.md](HANDOFF_278.md) подготовлен; #278 не начиналась. #296 остаётся OPEN/OWNER_PENDING и «В работе» до игровой приёмки. Скриншоты потребуют отдельного разрешённого dev-client запуска; основной PolyMC автоматически запускать нельзя.
