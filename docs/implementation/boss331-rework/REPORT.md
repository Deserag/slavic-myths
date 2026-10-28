# #331 — доработки и настоящие клиентские кадры, 28.10.2026

Изменения реализованы в `hell`, локально, без commit/push/новых веток. `main/master`, прежние изменения пользователя, миры и конфигурации PolyMC сохранены. Художественная приёмка принадлежит владельцу; Issues не закрыты, owner-accepted не назначен.

## Исправлено

**Сторож чащи.** NavyaMesh/texture/glow/pose: основной ствол собран из отдельных центральной/прикорневой частей, несимметричных наростов, широких развилок и корневых опор. Вместо прежнего face20×22×2 — полный череп18×22×14, отдельный затылок16×18×7, корневая челюсть/подбородок, нос, щёки и ветви. Лицо остаётся в середине ствола. Прежняя яркая прямоугольная грудная плита заменена узкой сердцевиной с разветвлёнными трещинами. Новые UV и actual256²PNG; без HD/PBR.

Трофей использует полный skull-subtree. Двигаются челюсть и ветви; пробуждение на6блоках/затихание за8, всплеск6CRIMSON_SPORE, пульс emissive глаз. Ветки при пробуждении до~6.3°, не прежние1.43°. Новые эффекты client-only, не наносят урон и не меняют мир. У всех голов общий бюджет24частицы/clienttick; idle Сторожа1ASH/20ticks только в24блоках и при рендере, новыеburst расходуют тот же бюджет. Fit учитывает горизонтальный sweep и запас для jaw/ветвей.

У живого Сторожа есть видимый ограниченный контур ауры12частиц/сек на видимую сущность в32блоках, челюсть/ветви в позах. Аура Weakness и damage не менялись. Реальный подъём корней теперь15–20tick, совпадает с прежним damageframe20; не наносит урон до полного подъёма. Синхронно обновлён offline exporter. Не менялись700HP, cooldown, радиус/сила ауры и AI.

**Жареница.** Полностью пересобран boss rig: объёмные грудь/плечи/талия/бёдра, отдельные верхние руки/предплечья/кисти/пальцы, лицо/челюсть и угольный венец. Три перекрывающихся слоя парящего платья16/22/28ед. ширины и складки создают сплошной силуэт вместо прежнего веера спиц. Волосы/платье двигаются, угольная палитра отличается от магмового камня. В первом клиентском прогоне выявлены прижатые руки; поза исправлена и переснята. Голова-трофей извлекается из нового head rig с тем же UV/PNG.750HP/две фазы/атаки не изменялись.

**Повелитель магмы.** Центральная ось7×29×7 вместо большого коробчатого торса, независимые рёбра/раскрываемые боковые сегменты, два пересекающихся объёмных элемента ядра, длинные парящие конечности/когти и отделённые нижние сегменты без тяжёлых ботинок. ВоIIфазе рёбра расходятся; ядро пульсирует в реальном renderer. Текстура512² с локальными трещинами вместо диагональных полос по всему телу.850HP/нейтральность/боевые damageframes/Peaceful-удаление сохранены. Трофейная jaw/shards и отдельная статичная GUI-иконка сохранены.

**Постень — увеличен именно трофей.** Убран дополнительный множитель.85; отдельный `posten_trophy.json`, маски15→19ед. высоты, кольцо radius10→9ед., заново упакованы UV и соответствующие base/glowPNG. Модель живого босса не уменьшалась/не увеличивалась. Сохранены4разных физические маски и плавный цикл90°,6/8proximity. Трофей остаётся в одной ячейке, старый ID/posten_head/BlockState не менялись. Масштаб считается по sweep-радиусу, а не по одному фронтальному кадру.

**Голова Жареницы/получение.** Сохранены CinderBlocks registration и старый ID`zharenitsa_head`. Добавлен TrophyHeadMigration: только событие загрузки уже загруженного чанка, только секции с её блоками, только отсутствующий LOWER Tile. Нет tick polling/загрузки чанков/замены существующего BE. Tile отправляет стандартный update packet/tag. В реальном отдельном мире проверено удаление Tile, производственное восстановление и последующий рендер.

Ручной /summon/egg без home по-прежнему не даёт boss loot: защита природной победы не снята. Вместо немого отказа игрок получает RU/EN сообщение об обязательной зарегистрированной арене. Нельзя называть это новым summon-фармом или доказательством natural availability. При законной player victory голова по-прежнему идёт через inbox; остальной лут Жареницы отдельно.

**Звук.** IslandsSounds +17событий приготовления/удара/idle/hurt/death/wake/phase на разных реальных vanilla sounds, явные callsites в IslandsMob и RU/EN subtitles. Никаких синтетических OGG или копий сторонних игр. `static-gates.json` содержит event→Minecraft event→реальные cachedOGG+SHA1. Прежние sounds.json entries побайтово по смыслу сохранены. Это работающие разрешённые vanilla cues, **не утверждённые оригинальные голоса** и не owner audio acceptance.

## Скриншоты из Minecraft

Все PNG в `client/` — **необработанный framebuffer настоящего Minecraft1.21.1/NeoForge21.1.255**,1600×1000. Не imagegen, не программная перерисовка ModelPart, не концепт. Съёмка в отдельном плоском диагностическом мире, daylight6000, стандартный renderer, FOV55, NoAI/invulnerable для статичной демонстрации. Это подтверждает реальную загрузку и отображение; не заменяет survival-бой/MP/Nether lighting acceptance.

Основные кадры:
- [Сторож, три четверти](client/01-guardian-three-quarter.png), [спереди](client/00-guardian-front.png), [сзади](client/03-guardian-back.png).
- [Жареница, три четверти](client/05-zharenitsa-three-quarter.png), [спереди](client/04-zharenitsa-front.png), [сзади](client/07-zharenitsa-back.png).
- [Повелитель, три четверти](client/09-magma_lord-three-quarter.png), [спереди](client/08-magma_lord-front.png), [IIфаза](client/18-magma-lord-phase2.png).
- [Все четыре головы](client/12-four-trophy-heads.png), [объёмный череп Сторожа](client/13-guardian-head-three-quarter.png), [его затылок](client/14-guardian-head-back.png), [крупный Постень](client/15-posten-head-awake.png), [Жареница](client/16-zharenitsa-head.png), [Повелитель](client/17-magma-lord-head.png), [инвентарь](client/19-heads-inventory.png).

Native before/after отдельно: `*-native-comparison.png`, одинаковые4ракурса/масштабы. Before — скопированный фактический #327 after до начала текущей работы; after — новый production exporter. Эти сравнения **не подписаны игровыми скриншотами**. Генератор `tools/boss331_preview.py`.

## Реальные проверки и границы

Точный итоговый source/JAR/installedSHA/backup/runtime count — **installation.json**. Исходники локально поверх `d97a625fbe23b9345e7879f39463099fa676b71f`; `source-manifest.json`, `source-delta.zip` и `source.patch` фиксируют реальные незакоммиченные байты. Не приписывать эту реализацию опубликованному коммиту без delta.

- Java21 clean build и actual native export: `delivery-build.log`. Native geometry/UV/finite poses и sweep трёх новых голов, включая относительное вращение mask_ring и gaze Постеня, jaw/awake Сторожа.
- Предшествующий полный build/offline: `build-native-offline.log`:32826journal/NBT/route assertions;81916islands checks/412nativeframes;6840695checks шести старых голов. Это не такое число игровых боёв.
- Проверка всех8452resources/4767JSON/1529PNG/2046models/2092Java21classes, точное совпадение JAR payload с источниками и отсутствие tools/verify/client fixture в production: `resources.json`, `static-gates.json`.
- Повтор scope-генераторов после остановки клиента: `generator-repeat.json`, drift0. Исправлена повторная вставка soul pools в head-generator: существующая loot-семантика sheptun/oskolnik не изменилась. Сохранены пиксельно идентичные PNG остальных4жителей (старые bytes, без случайной смены компрессии).
- В клиенте все4головы установлены **через ItemStack.useOn→BlockItem**, проверены реальные Tile, рендер/GUI. Симуляция отсутствующего старого Tile Жареницы восстановлена production ChunkEvent handler. После снимков реальное destroyBlock даёт ровно1соответствующий ItemEntity: `client/live-head-regression.json`, строка `BOSS331_HEAD_BREAK_DROP` в окончательном клиентском логе.
- Клиентские прогоны перечислены в installation.json. Run1 — первая съёмка; run2 — приближение/поправка рук; run3 — исправленный inventory fixture и break/drop. В прежнем run2 инвентарный стенд перезаписывал slot0 при очередном useOn: дефект стенда, не item registry. В финальном прогоне используется slot8 для размещения, все4предмета отдельно. Финальный повтор после уточнения общего particle budget подтверждает окончательные классы. Только отдельные disposable worlds в `work/boss331-client`, не пользовательские saves.
- Промежуточные отказы сохранены в backup logs: file-sharing Windows при записи старого offline отчёта (новый output path и успешный повтор), отсутствующий импорт Difficulty в тестовом capture-классе, PowerShell разбил незакавыченный `.json` аргумент (исправлен), консольcp1251 не напечатала стрелку после успешных asserts (ASCIIprint и успешный повтор). Production assertion/render failure не скрыты; успешные повторы лежат отдельно.

## Матрица #331

| REQUIREMENT / PLAYER_PATH | REAL_SOURCE | TEST / EXPECTED | EVIDENCE | RESULT |
|---|---|---|---|---|
| G01объёмный трофей Сторожа; победа→голова→поставить | NavyaMesh.face/occiput/jaw + TrophyHeadsClient | бок/затылок/челюсть,1block bounds | native comparison,client13/14 | PASS реализации/реального отображения; original match BLOCKED |
| G02видимые boundedFX трофея | Head.guardianPose/Renderer/Tile |6/8awake,ветви/jaw/6burst,global24budget | source + close client frames | PASS source и видимой формы;10/50headFPS NOT_RUN |
| БоевыеFX Сторожа | NavyaPose/NavyaEffectRenderer/NavyaRenderer | roots full-rise к20damageframe,видимаяаура | nativeposes/source; live static aura | PASS технически; полноценный бой NOT_RUN |
| P01крупнее трофей Постеня без слома4масок | posten_trophy UV + safeHead fit | sweep ring/yaw/bounds,живой renderer | native checks + client15 | PASS для выбранной геометрии/рендера; save/MP NOT_RUN |
| H01четыре head fullpath | TrophyHeads/CinderBlocks/BlockItem/Tile/BER | placement→render→break×1 | client logs + live-head-regression.json | PASS4placement/Tile/render/break; не доказательство naturaldrops |
| Старый zharenitsa_head | TrophyHeadMigration/Tile update | missingTile recreated безloads/IDrename | actualloadedchunk repair log | PASS симулированный отсутствующийTile; реальный старыйsave NOT_RUN |
| Manual vs naturalZharenitsa | CinderMob.home/canClaim + RU/EN | manual безнаграды с честной причиной; naturalgate сохранён | source/локализации | PASS static; naturalvictory/inbox/relog NOT_RUN |
| B01три разных переработанных босса | NavyaMesh/CinderModel/IslandsModel/PNG |3D в реальномclient,4views/P2 | client00–11/18 | PASS демонстрации; художественная приёмка OWNER_PENDING |
| Аудио безсинтетики | IslandsSounds/IslandsMob/sounds/lang |17routes→реальные vanilla bytes | static-gates.json | PASS routing; originalvoice/слуховаяприёмка BLOCKED/NOT_RUN |
| Registry/save/MP | старыеIDs/height1;миграцияmissingBE | старыймир/relog/2клиента | staticIDpreservation | MP/relog/oldworld NOT_RUN |
| Build/JAR/install | Java21/currentresources/receipt | no test classes,backup,1installedJAR,SHA | delivery-build/resources/installation | PASS технически |
| OriginalPNG соответствие | прежний#331reference audit | реальные bytes+SHA | originals не поступили | BLOCKED_REFERENCE_BINARY,безподменыSVG |

## Что не сделано

Точное соответствие отсутствующим originalPNG и художественное принятие владельцем не заявлены. Полные survival-бои/атаки/Peaceful/естественные встречи/природная выдача наград/MP/перезаход/реальные старые размещения/10–50голов с измеренным FPS/MSPT не проверены этим визуальным стендом. Рендер в дневном flatworld не доказывает читаемость в каждом адском освещении и модпаке. Дев-клиент содержал Curios/JEI; весь установленный PolyMC модпак (Jade/Xaero/FallingTree) в этом прогоне не проверялся.

Незавершённый ocean-only worldgen2.0.5, вулкан/арена и прочие прежние ограничения не входят в исправление #331 и не объявлены исправленными. Исходная неизвестная голова из эпизода владельца по-прежнему не установлена однозначно: теперь4head paths реально проверены в новом мире, а manualЖареница объяснена; старый мир остаётся отдельной проверкой.

## Как проверить в Minecraft — при необходимости после просмотра кадров

Для оценки внешности достаточно приложенных настоящих кадров; повторять технические операции владельцу не требуется. Для последующей игровой приёмки: сначала полная копия старого мира и сверка installedSHA из installation.json. Обычный Survival→портал→естественный биом→босс→победа→inventory/head→поставить/снять. Повторное получение/полный инвентарь/чужой MP-получатель не должны дублировать награду. Отдельно сохранение воIIфазе и реальный relog;2клиента и старый zharenitsa_head до ломания/перестановки. Горючие острова naturalpath пока BLOCKED.

OP-диагностика только в тестовой копии: `/give @s slavicmyths:grove_guardian_head`, аналогично zharenitsa_head/posten_head/magma_lord_head; `/data get block X Y Z` у LOWER ожидает slavicmyths:trophy_head. Поставить пол/стена/4facing, отойти8/подойти6, проверить маски/jaw/GUI/снятие. Manual `/summon slavicmyths:zharenitsa` без зарегистрированной home ожидаемо не выдаёт награды; его нельзя считать naturalтестом. Проверить соседние старые6голов, режимPeaceful уПовелителя и модпак overlays отдельно.

## Риски / откат / следующая задача

Резервные JAR сохранены вне mods, точные пути иSHA — installation.json. Откат при закрытой игре: вернуть backup JAR; мир, уже пересохранённый новым кандидатом, возвращать вместе с согласованной полной копией. Не использовать reset/clean и не удалять чужие изменения. Следующий шаг — оценка владельцем показанного внешнего вида; никакая Issue не объявлена принятой автоматически, новый milestone не запускается.
