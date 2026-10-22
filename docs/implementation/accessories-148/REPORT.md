# #148 / #149–156 — исполнительская сдача на проверку владельцу

21.10.2026. Реализован исполнительский объём пакета. Остаются **только ручные игровые проверки владельца — NOT_RUN / OWNER_PENDING**. Это не DONE/ACCEPTED/READY FOR RELEASE: Issues остаются OPEN, owner-accepted не добавлен. Рекомендованный этап — «Тестирование», фактическое поле Project Status не изменено (нет технического доступа). Коммиты и push не выполнялись.

Предыдущее сообщение о недостающих референсах — исторический промежуточный этап (phase1-report.md). После прямого поручения «реализуй все а потом обнови версию мода в поли мс» выполнен весь дизайн по текстовым требованиям Issues: точные 16×16 матрицы пикселей в воспроизводимом генераторе, а не концепт вместо PNG. Внешний референс не был получен и не объявляется согласованным; художественный результат ожидает вашей приёмки.

## Что изменилось

- Аудит исходных 38 предметов: audit/REPORT.md, inventory.json, INVENTORY.tsv. Финальный реестр с шестью новинками — FINAL-INVENTORY-44.json. Этапы прохождения оценены по зависимостям, не измерены временем игры.
- Вкладка accessories: 40 функциональных аксессуаров, группы по существующему слоту/способу применения. Прежние индексы 0–9 и все старые item IDs сохранены; новая вкладка — индекс10 после magic, страницы раскладывает NeoForge. Четыре расходника/компонента остаются в magic: mor_ward, fading_flame_charm, warding_charm, volkhv_amulet.
- Пять согласованных изменений баланса: Лунница 5→8%; Гривна 4→6%; Оберег от нежити 9→12%; Ским +10→15% при HP строго ниже35% вместо30% (melee/projectile); охотничий пояс +10→15% projectile только против WildlifeEntity. Остальные старые эффекты и ограничения сохранены.
- Новые плоские GUI-иконки **12 существующих предметов**: depth_amulet, hunter_belt, invisibility_cap, obereg_padayuschey_zvezdy, odnoglazyy_obereg, perun_ring, poyas_tugarina, resin_ring, retribution_charm, veles_amulet, volchiy_poyas, zolny_obereg. Переделаны два дублирующихся пояса, три повторяющихся U-ожерелья, старые геометрические кольца/амулеты/пояс/шапка. Ещё15 носимых иконок — обоснованный KEEP.
- Шесть новых предметов, без новых типов Curios-слотов и без прямых дропов готовых украшений. Для необязательных 5% дропов из #154 выбран разрешённый вариант «только крафт»; все старые loot tables сохранены побайтово.
- Исправлены посадка шапки/охотничьего пояса, отступы body-украшений от нагрудника и очков от шлема. Геометрия/UV прежних предметов сохранены. Добавлены небольшие body-элементы только поясу Подвея и ожерелью Лешего; кольца/банный оберег остаются icon-only.
- Существующие Curios-рендереры получили проверку невидимости: в установленном Curios9.5.1 общий CuriosLayer сам не проверяет isInvisible (curios-layer-bytecode.txt). Шапка по-прежнему использует свою прежнюю механику скрытия игрока, её эффект не менялся.

## Новые украшения

| ID | Слот | Эффект | Получение |
|---|---|---|---|
| pool_ring | ring | +15% SWIM_SPEED только в воде; один modifier независимо от числа копий, удаляется при снятии/выходе из воды | pearl_fragment + water_grass + железо |
| bath_ward | charm | Обычные конечные Poison/Weakness −20% длительности; CD60с. Через Added после проверки применимости; без повторного addEffect и потери hidden/source. Защищённые, моровые, холодовые и сюжетные эффекты исключены | bath_stone + нить + honeycomb |
| harvest_ring | ring | Ручной сбор спелых пищевых культур: 10% на1 дополнительный обычный продукт, CD5с. Без семян, Creative/FakePlayer, FarmerIII/IV и автоматического сбора | noon_ear + field_bundle + медь |
| podvey_belt | belt | При падении ≥12 блоков: урон×0.30; CD60с начисляется после ненулевого фактического урона. Сочетается с stone_path_amulet и Feather Falling | vetrovoy_uzel + vihrevaya_nit + кожа |
| leshy_necklace | necklace | После фактического враждебного урона ≥6HP в forest/taiga: SpeedI80тиков, CD60с; без PvP/союзных/самоповреждения | leshy_heart + birch_bark + amber |
| spark_ring | ring | Успешный прямой player_attack по допустимому врагу: 25% поджечь на3с; CD8с. Без игроков, питомцев, союзных/игроком управляемых существ, огнеиммунных; две копии не удваивают proc | pylayuschaya_cheshuya + ognennoe_pero + медь |

Точный рецепт каждого — data/slavicmyths/recipe/<ID>.json; NEW-MATERIAL-SOURCES.json содержит исходные loot tables и ссылки на runtime-выдачу ингредиентов. Рецепты доступны без новой сюжетной миграции. Harvest поддерживает vanilla wheat/carrot/potato/beetroot и mod rye_grain/barley_grain/oat_grain/turnip/cabbage/pea_pod; flax/hops/семена/ядовитый картофель не удваиваются.

## Доказательства графики и отображения

- ICONS-BEFORE-AFTER-1x-4x.png + gui-matrix.json: все33 Curios ID, реальные production PNG/JSON, KEEP/FIX/NEW и SHA256. Нет точных дублей силуэта по alpha-mask среди33; 18 новых/переделанных sprites — RGBA16×16, только alpha0/255.
- 12 файлов *_held148.json побайтово равны прежним item JSON. Новая перспектива gui отделена через neoforge:separate_transforms; NONE/held остаются на исходной модели. Архив tools/accessories148_held.zip канонически восстанавливает именно прежние модели.
- native-worn.json: настоящие baked ModelPart/UV; native-factory-preservation.json подтверждает неизменность прежних фабрик геометрии. Body/head fitting меняет только матрицу привязки.
- wearer-matrix.json: все33 предмета, индексы0/1 колец, renderer/NONE, bone и координаты. **15 видимых / 18 icon-only**; не добавлена вымышленная геометрия перстней.
- wearer/*.png: для каждого15 видимых ID — Steve/Alex, без брони и с железными chest+helmet, idle/walk_t10/sneak/held_item, front/back/profile/3⁄4, одинаковые масштаб и камера A/B. Четыре комбинации одновременно заполняют necklace+belt+head+ring0+ring1+charm.
- FIT-FIXES-CONTACT.png, WEARER-ARMOR-CONTACT.png и VISIBILITY-SOURCE.png — компактные сравнения. Все изображения являются офлайн-проекциями исходников, **не игровыми скриншотами**. Поза held_item показывает положение руки, без запуска ItemInHandLayer/игрового движка; настоящая анимация, предмет в руке, материализация/исчезновение и произвольная сторонняя броня проверяются владельцем в игре.
- Мотивы новых спрайтов — игровая интерпретация материалов/названия (вода, камень, колос, узел, дерево/янтарь, огненная чешуя). Не заявлена археологическая достоверность.

## REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| #149 полный аудит предметов, источников, чисел, конкурентов, стадий | audit/REPORT.md, INVENTORY.tsv, inventory.json, source-hashes.json; финальное дополнение FINAL-INVENTORY-44.json | PASS |
| #150 отдельная вкладка и перенос, старые категории/порядок/IDs сохранены | CreativeCatalog исполнен Java21; 34 старых переноса +6 новых =40; все остальные записи/порядок сравнены с before | PASS |
| #150 UI/пагинация в настоящем клиенте | Стандартная NeoForge builder() и порядок проверены по установленным исходникам; реальный UI не запускался | NOT_RUN / OWNER_PENDING |
| #151 пять численных изменений согласованы | balance-approval.json: ответ владельца «Согласовать эти пять изменений» | PASS |
| #151 ограничение scope и сохранение старых условий | Точное before/after сравнение трёх обработчиков, только согласованные числа и RU/EN; final-checks.json | PASS |
| #151 реальные боевые значения/сочетания в игре | Сценарии ниже | NOT_RUN / OWNER_PENDING |
| #152 читаемые разные плоские иконки, 1×/4× и исходные SHA | 12 FIX /15 KEEP /6 NEW; gui-matrix.json, ICONS-BEFORE-AFTER-1x-4x.png; alpha и JAR проверки | PASS |
| #152 held/worn не заменены GUI-иконкой | 12 исходных held JSON побайтово сохранены; отдельная gui perspective; канонический архив | PASS |
| #152 художественная приёмка настоящего GUI | Production PNG готовы, утверждение красоты/читаемости владельцем ещё не получено | NOT_RUN / OWNER_PENDING |
| #153 все27 старых и6 новых wearable IDs покрыты | wearer-matrix.json: 33 строки, 15 рендереров /18 NONE | PASS |
| #153 исправления привязок и доказательства A/B | native-factory-preservation.json, native-worn.json, 19 wearer sheets + visibility, body/head источники | PASS |
| #153 движение, invisibility, held item, сторонняя броня в игре | Фиксированные source poses не подменяют движок | NOT_RUN / OWNER_PENDING |
| #154 омут/баня/жатва: регистрация, материалы, крафт, слот, эффект, число/CD, RU/EN и sprite | 3 полных Item + recipe + tag + model/PNG; AccessoryRules и event adapters; NEW-MATERIAL-SOURCES.json | PASS |
| #154 опциональные 5% direct drops | Не выбраны; craft-only разрешён ТЗ; все старые450 loot JSON идентичны baseline | PASS |
| #154 два кольца, cooldown/relog/MP/фарм/вода в игре | Server-only guards и один modifier/event callback проверены исходниково; реальный relog/MP не запускались | NOT_RUN / OWNER_PENDING |
| #155 Подвей/Леший/искра: полная цепь ресурсов/регистрации/эффектов | 3 полных Item, рецепты из старых материалов, event handlers и persistence | PASS |
| #155 пороги/шансы/совместимость и anti-abuse | Реальные shared production policies: 12блоков, 6HP, 10/25% границы, множители/CD; source guards PvP/pet/team/FakePlayer; NBT roundtrip отдельно от relog | PASS |
| #155 игровая совместимость, перезапуск и смерть | Owner-only сценарии ниже | NOT_RUN / OWNER_PENDING |
| #156 сохранения/IDs/слоты/старые loot/прочие задачи | baseline.json: только whitelist; старые registry definitions, HP/AI боссов, оружие/руны/броня/аудио/деревья сохранены | PASS |
| #156 актуальная техническая сборка/ресурсы/генераторы | final-build-gates.log, final-verify.log, final-checks.json; clean build + quarantine + offline tests; воспроизводимость/idempotence ресурсов | PASS |
| #156 production NeoForge JAR и отсутствие test fixtures | Все source JSON/PNG/TSV побайтово в JAR; новые классы/metadata; offline классы не упакованы | PASS |
| #156 реальная игровая интеграция и приёмка всего пакета | Игровых запусков0; проверка владельцем ещё не проведена | NOT_RUN / OWNER_PENDING |
| PolyMC установлен точный прошедший gates JAR, есть backup, нет дублей | installed.json: INSTALLED_VERIFIED, один Slavic Myths mod ID, остальные6 JAR SHA прежние | PASS |

## Проверки и ограничения

Команды (JAVA_HOME=C:/Program Files/Java/jdk-21.0.12, только для команды):

```
gradlew.bat clean build -I tools/accessories148-offline.gradle verifyAccessories148Offline --console=plain
python tools/verify_accessories148_full.py
```

Финальный результат: **BUILD SUCCESSFUL**; 5824 scoped source/resource/JAR проверок. Offline native harness: 505399 проверок, большая часть — конечность вершин/диапазоны UV, а не количество игровых сценариев. Осмысленные проверки shared production policies отдельно проверяют пороги, защищённые/длинные конечные эффекты, шанс, сочетание падения со stone_path, cooldown boundary и сериализацию настоящего NBT. Gradle test — NO-SOURCE. Есть прежние compiler deprecated/unchecked warnings, final build без ошибок. Minecraft/PolyMC/GameTest/сервер не запускались.

Старый независимый tools/creative_source_inventory.py имеет устаревшее ожидание `tier<=3` в неизменённом RuneFoundation.java и не годится как gate этой ревизии. Руны ради старого теста не менялись. Вместо этого применён текущий scoped gate: реальный CreativeCatalog, точный набор старых IDs +6 новых, SHA всех прочих production файлов и все production ресурсы JAR. Это статическая проверка, не загруженный runtime registry.

## Точная установленная сборка

- Версия: **1.9.7.53**, NeoForge21.1.255 / Minecraft1.21.1 / Java21.
- Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.53.jar`
- SHA256: `5cc58c93b828617d607e06d860d40ae03bc15615360e3bf202b6c8b95ac86fc5`; размер 23629676 байт.
- Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mod-backups\accessories-148-20261021-143920\slavicmyths-1.9.7.52.jar`
- Backup SHA256: `1577b7abb0916323a8f0507d1a2d44a70a72108dcf732e0c9987a7b0b8a4bf38`.
- Исходный commit: `32d1733457e7e90360ce16c23860304ece664c64` **+ некоммиченная рабочая копия**, включая сохранённую предыдущую работу. Точный manifest: final-source-manifest.json, SHA256 `a2bfd1291a0b11edc3f2719234b1322c1551fd4bfa922a04844d4cd7b802e0f8`.
- JAR в build/libs и установленный файл имеют одинаковый SHA. Остальные6 модов не изменились; старый PolyMC1.16.5 и миры не тронуты.

## Краткий сценарий владельцу

1. В тестовом экземпляре Slavic-Myths-1.21.1-Testing проверить версию1.9.7.53, открыть новую вкладку после магии, включая соседние страницы. В ней40 предметов; оружие/броня/руны/трофеи в прежних разделах. Проверить 12 новых GUI-иконок при обычном масштабе интерфейса.
2. Новинки получить в creative либо `/give @s slavicmyths:pool_ring`, затем аналогично bath_ward / harvest_ring / podvey_belt / leshy_necklace / spark_ring. Проверить рецепты в JEI. Количество физических Curios-мест: head1 / necklace1 / ring2 / belt1 / charm1.
3. Pool: сравнить плавание с одним/двумя кольцами и без них; выйти на сушу, снять одно/последнее. Depth amulet должен отдельно восстанавливать воздух.
4. Bath: обычный Poison/Weakness, повтор до/после60с, длинный конечный и infinite, сюжетная слабость/мор/холод. Снятие, смерть, relog/restart не должны сбрасывать откат. Проверить вместе с одноглазым оберегом/классовой стойкостью.
5. Harvest: ручной лом и серп по зрелым wheat/carrot/potato/beetroot/перечисленным mod crops. Проверить незрелые культуры, Creative, автоферму, FarmerIII/IV. Дополнительный продукт — максимум1, без семян/ядовитого картофеля, успешные срабатывания не чаще5с; 10% шанс требует серии сборов, не одного блока.
6. Podvey: падение ниже12 / от12 блоков, повтор до/после60с, комбинация stone_path и Feather Falling; полное поглощение/Slow Falling не должно тратить CD без HP-урона. Нельзя получать полную неуязвимость.
7. Leshy: фактический враждебный удар ниже6 / от6 HP в лесу и вне его, с учётом брони/absorption. SpeedI4с, повтор не раньше60с; PvP/самоурон/дружеский урон не должны срабатывать.
8. Spark: серия melee по обычному врагу; projectile/magic/игрок/питомец/союзник/огнеиммунный — без срабатывания. Две копии не дают два proc; после успешного поджога3с следующий не раньше8с.
9. Steve/Alex: head+necklace+belt+2rings+charm, с обычной/модовой бронёй, спереди/сзади/сбоку; ходьба, приседание, предмет в руке. Проверить шапку/очки со шлемом, пояса/обереги с нагрудником и обе разновидности невидимости.
10. Пять согласованных старых эффектов: ночь/день, обычный/обходящий броню урон, нежить/прочие, Wildlife/зомби, HP ровно35% и ниже. После перезахода старые предметы/экипировка сохраняются.

До этих проверок и вашего явного подтверждения пакет не объявлен принятым и Issues не закрываются.
