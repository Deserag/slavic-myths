# #327 / Ад 2.0.4 — production-кандидат, IN_PROGRESS

Выполнен доступный технический объём продолжения по CODEX_RESUME_TASK.md. Это **не завершённая приёмка #327/2.0.4 и не релиз**: отсутствуют оригиналы для точного художественного сравнения, игровые проверки не запускались по запрету владельца. Issues не закрывались, Project Status не менялся. Новый кандидат в PolyMC **не установлен**.

## Изменено

- Подключён `slavicmyths:lost_halls` в Nether preset: production biome JSON, feature, region climate selector, природные четыре семейства блоков, формы, рецепты, loot, языки, Creative. Сохранены vanilla Nether точки; 2.0.5 ocean worldgen этим не реализован.
- Шесть семейств физических помещений, переходы с ограниченным уклоном, две дороги к арене, 32×32 площадка, своды/мост/грибные группы/световые гроты. Feature ограничен текущим новым чанком; не перезаписывает BE, liquids, bedrock, посторонние блоки и chunks с vanilla structure references. Существующие миры не ретрогенерируются.
- Пять настоящих EntityType: sheptun (четыре сохраняемых варианта), oskolnik, gulokryl, ekhovik, griboshmyg. Разные модели/AI/снаряды/роли, приручение Гулокрыла и два мирных способа сбора. Spawn predicates ограничены биомом и типом помещения.
- Материалы иконками16×16, размещаемая 3D метка со светом10 и креплением на стену/пол, камертон с диапазоном48 и cooldown400, посев налёта из спор. Камертон использует только опубликованные записи комнат и resident anchor, не создаёт/не загружает дальние chunks.
- Реальный Постень с 650HP, четырьмя объёмными масками с разной лицевой геометрией, четырьмя атаками/телеграфами, bounded wall transit. При260HP — четыре стабильные копии по65; controller ведёт общий пул/bar. Реальный безопасный пол используется для восстановления, а не теоретическая высота генератора. Защита UUID, malformed/future journals read-only сохранена.
- Руна Теневого лика на нагрудник: existing activation,80ticks invisibility, ручной ранний выход, до3 безопасных skin decoys120ticks,75s cooldown после выхода. Копии без предметов/зачарований/рун, один положительный hit, owner-safe targeting. Чужие potion effects не удаляются.
- Три новые головы интегрированы в TrophyHeads рядом с существующей головой Повелителя магмы; `zharenitsa_head` registry ID сохранён. Модели берутся из фактических head subtrees. Пол/стена, bounded cosmetics, ring/head tracking для Постеня, RU/EN и block loot. Проверки прежних шести голов сохранены.
- BossRewardAuthority/BossRewardInbox закрепляют получателя и batch отдельно от encounter SavedData; не переassign при его откате. Full inventory откладывает весь batch. Player inventory и receipt записываются вместе, targeted save вызывается через PlayerListSaveInvoker. Для Сторожа/Жареницы non-player deaths имеют x1 world head drop; player credit — durable inbox.
- Исправлена Peaceful логика Повелителя магмы с удалением bossbar; обычные островные нейтралы остаются CREATURE. Предыдущие незакоммиченные исправления и отчёты сохранены.
- Изменены реальные геометрия/UV/pivots трёх прежних боссов: segmented guardian trunk/crown/roots, articulated Zharenitsa arms/face/hair/crown, Magma Lord spine/ribs/limbs/crest. Native before/after в одинаковом масштабе и позах, не концептные картинки.

## Проверки и доказательства

`final-gates.log`: Java21 `clean build`, verifyLostHallsLogic, exportNether327Models, auditHallsNativeClimate, verifyLegacyHeadsRegression, verifyBurningIslandsOffline. Реальные числа брать из лога, не суммировать их как gameplay tests.

- 32,826 pure journal/file/NBT/heightfield assertions, включая200 randomized encounters,5000 distant metadata records,20 regions и каждую клетку площадки32×32.
- Native climate: пять seeds42041–42045, четыре окна по2048 блоков с шагом64, восемь classifications и53 полных climate candidate regions. **Ни один мир не был сгенерирован этим тестом;53 — не подтверждённые арены.**
- Native exports9 фактических ModelPart rigs, finite coordinates/UV, до/после3 боссов и pose GIF; новые3 головы2,396,736 rotated vertex checks. Шесть прежних голов6,840,695 проверок; островной offline suite81,916 checks/412 frames.
- `resources.json`: все source resources сопоставлены с production JAR, metadata/version/Java21 classes, тестовые классы отсутствуют. Точный путь/SHA и source manifest — BUILD_DELIVERY.json. Предупреждения deprecated NeoForge EventBus API сохранены в логе, ошибок build не должно быть.
- `halls-generator-repeat.json`: повторный halls_assets.py не меняет ресурсы. Полная воспроизводимость всех старых генераторов этим не утверждается.
- `REFERENCE_AUDIT.json`:14 обязательных original images не найдены; никакого ART_PASS. Native renders визуально просмотрены, но не заменяют оригиналы и runtime.

## Что не сделано / не доказано

**Игровых запусков0.** Не проверены: загрузка loader/mixins в настоящей игре, MP, entity lifecycle и bossbar, бой и damage counterplay, естественные spawn/частоты, полная физическая генерация на20 seed/windows, непрерывность всех путей через реальные vanilla liquids/structures, фактическая доступность всех арен, старый мир/перезаход/краш процесса, звуки на слух, голова zharenitsa_head из старого сохранения, placement/drop/support/explosion/piston, MSPT/память.

Природная арена появляется только при проверенном resident completed region. Из-за сохранения vanilla footprints/жидкостей некоторые кандидаты могут не получить boss; вероятность и требование гарантированной доступности остаются **NOT_RUN**, а не PASS. Нет финальной аудиоприёмки; синтетика не генерировалась и quarantine не отменён. Точная палитра/силуэт относительно raw originals **BLOCKED_REFERENCE**. Естественный ocean-only worldgen Горючих островов2.0.5 не завершён и не включён этим scope.

## Передача

Коммит/push в `hell` разрешены последним сообщением пользователя; точный результат публикации фиксируется отдельно. `main`, release, owner-accepted и Issue close не трогать. Незавершённая цепочка не доставляется в PolyMC как final. Для ручных проверок — OWNER_SMOKE_CHECKLIST.md; любые FAIL фиксировать с seed/координатами/log/видео.
