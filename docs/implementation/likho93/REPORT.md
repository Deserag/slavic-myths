# #93 — боевые механики Лиха: техническая реализация, OWNER_PENDING

21.10.2026. Issue остаётся OPEN. Рекомендуемый Project Status: **Тестирование** после перечисленных технических gates; доступные инструменты не меняют поле Projects, фактическая смена поля не заявляется. Ни owner-accepted, ни закрытие не выполнялись. #142 **не начата**.

Source base main `32d1733457e7e90360ce16c23860304ece664c64` + незакоммиченный snapshot предыдущих задач. Точная граница перед #93 — `baseline.json`; результат — `source-manifest.json`, список изменённых существовавших файлов — `changed-existing.json`. Commit/push/ветки/PR не создавались. Декор и #140/#141 сохранены. Версия candidate **1.9.7.52**.

## Причины и исправления

1. **Подтверждено:** `area()` и GROUND выбирали Player; зомби, скелет и паук не попадали в выборку независимо от красивого замаха. Новый `victims()` расширяет выборку исключительно для Лиха. Общая `players()` и ветка Тугарина сохранены.
2. **Подтверждено:** GAZE выбирался и против мобов, хотя реализация зависит от взгляда игрока; GRAB/SWEEP стояли ниже. GAZE теперь выбирается только против игрока. При двух и более видимых противниках в четырёх блоках приоритет у SWEEP, затем разрешённого фазой GROUND. Защитный замах направляется к ближайшему допустимому противнику. При одиночной цели прежние особенности сохранены. В фазе 3 LEAP больше не затенён CHASE на всём своём диапазоне 8–15.
3. **Подтверждено:** `MorZone` перебирала только Player и могла продолжать вредить после потери owner в период отсутствия до удаления. Зоны Лиха выбирают локальных LivingEntity через существующий `HigherMor.enemy`, сохраняют горизонтальный радиус, высоту, LOS, экспозицию и периодичность. Без живого присутствующего хозяина в 32 блоках не вредят, накопление сбрасывается; удаление после 40 ticks отсутствия прежнее. Ветка зон Овинника вынесена без изменения тела, включая её прежнюю семантику отсутствующего хозяина: исправление этого отдельного поведения не входило в разрешённый scope.
4. **Подтверждённый путь отмены в source:** пересечение порога фазы немедленно заменяло активную атаку переходом, обнуляя её время до hit frame; у высшего Морового Лиха аналогично заменяло skill. Теперь переход ждёт IDLE/RECOVERY обычной формы либо завершения текущего morSkill. Порог HP, длительность перехода и художественная последовательность прежние.
5. **Подтверждённый источник перенаправления:** HurtByTargetGoal может менять текущую цель, а `release(t)` использовал новый target при уже зафиксированном heading. Телеграфируемый приём теперь хранит UUID допустимой исходной цели; CHASE продолжает следить за текущей целью. После завершения следующая атака использует актуальный target. Смерть/защита цели, дальность, LOS, leash, переход к моровому состоянию и остановка Goal остаются законными причинами отказа/остановки.
6. **НЕ подтверждено runtime:** сброс именно от каждого обычного hurt и полная причина наблюдавшегося пятиминутного боя. `hurt()` по-прежнему вызывает `super.hurt` и не обнуляет MOVE. Knockback не ослаблялся; HP/защиты не повышались. Для различения причин добавлена выключенная по умолчанию ограниченная диагностика: begin/release/recover(reason), target.change, phase, goal.stop, hurt до/после, knockback и реальные `LivingDamageEvent.Post` с final damage. Она ничего не меняет в уроне и не отменяет события.

## Навыки

| Навык | Изменение | Сохранено |
|---|---|---|
| SWEEP | Допустимые LivingEntity; приоритет при окружении | 10 урона, r4, конус55°, windup14, shield/LOS/knockback |
| CHASE | Тот же SWEEP по LivingEntity каждые25 ticks | 80 ticks действия, урон10/r4, tracking, recovery40 |
| LEAP | Взрыв приземления по LivingEntity; достижимый приоритет | windup18, фиксируемый heading, урон8/r3.5, условия приземления/timeout |
| GROUND обычного | LivingEntity; hits UUID исключает повтор трёх маркеров | один release на24, 6 урона, r1.75 маркера, оба LOS, slow50, без постоянной зоны |
| GRAB/HOLD/бросок | Дополнительная проверка допустимости цели и стабильный focus | исходные 4+6 урона, подготовка18, удержание20, радиусы/slow falling |
| GAZE | Только применимый Player, общая защита союзников/сюжетных | 2 урона, взгляд/конус/LOS, ill fate/weakness, подготовка26 |
| Mor sweep/beam/cloud | Ближний sweep при окружении; фаза не заменяет активный skill; bounded trace | существующие 14/12 урона, времена22/30/24, радиусы/направление/cooldowns |
| Mor ground/cloud | Допустимые враги получают прежние эффекты/дозированный урон | r3/r5, life180/120, cap12, sample10, ground>=60, damage раз в20 экспозиции |

У облака первое замедление происходит в первом 10-тиковом sample, первый урон на20 экспозиции: это прежняя периодичность, а не новое мгновенное дополнительное попадание. У земли первый урон на60. При непрерывном пребывании весь срок одна земля даёт7×1, одно облако6×2; выход/LOS/высота разрывают накопление. Новый MorExposure не позволяет повторно применить sample одной зоны одному UUID в тот же tick. Механика нескольких самостоятельных зон не заменялась новой глобальной системой.

## Изменённые файлы

- Production: `hunt/WorldBoss.java`, `mor/HigherMor.java`, `mor/MorZone.java`.
- Новые production helpers: `hunt/LikhoTargets.java`, `LikhoCombatRules.java`, `LikhoCombatTrace.java`, `mor/MorExposure.java`.
- `gradle.properties`: 1.9.7.52; проектные статусы/архитектура/roadmap, этот отчёт.
- Проверки: `tools/likho93-offline.gradle`, `tools/likho93-offline/Likho93Offline.java`, `tools/verify_likho93.py`.

## Замороженные области и доказательства

`verify_likho93.py` сравнивает все ранее существовавшие src/resources/tools с baseline. Из production разрешены только три файла выше. **Все ресурсы, все client models/renderers, registry, trophy #140 и остальной код байт-в-байт сохранены.** Внутри WorldBoss дополнительно сравниваются attributes, dimensions, registerGoals, save/load, die, Tugarin state/move/hurt/choice; его scheduler по-прежнему возвращается в TugarinCombat до ветки Лиха. HigherMor.enemy/zone cap/save/load и HigherRules неизменны; тело ветки зон Овинника совпадает с исходным после технического переименования локальной карты.

Фактический `BossKind.LIKHO.hp` **350**, mor_health modifier **+1 ADD_MULTIPLIED_TOTAL**, обычный максимум без дополнительных модификаторов **350**, моровый **700**. Архивное упоминание640 в Issue не использовалось для изменения баланса. `MorController`, `CreatureHealthBalance`, NBT, формулы и все базовые атрибуты сохранены. Реальные сохранённые дополнительные modifiers продолжают действовать; здоровье не принудительно устанавливается новым числом.

## Проверки и матрица

Java21 `gradlew.bat clean build` — журнал `build-likho93.log`. Production pure policy/exposure test `verifyLikho93Offline`: **145 checks**; реальная классификация Zombie/Skeleton/Spider как Enemy, выбор фаз/навыков/границ/КД, накопление/сброс/дубликаты экспозиции. Native NBT/attribute regression `creatureHealthOfflineChecks`: **1545 checks**, включая ограниченный JVM lifecycle stub существующего теста; не игровой сервер/перезаход/бой. Source freeze и JSON/PNG/JAR parity — `verification.json`. `test` стандартной сборки NO-SOURCE, не выдаётся за боевые тесты.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Обычные SWEEP/CHASE/LEAP/GROUND допускают Enemy | production victims/strike/ground, shared CombatTargeting; offline classifications/choice | PASS (source/policy) |
| Normal против zombie+skeleton+spider, 5 минут | Требуется настоящая игра | NOT_RUN / OWNER_PENDING |
| Mor против той же группы, 5 минут | Требуется настоящая игра | NOT_RUN / OWNER_PENDING |
| Земля/облако: пороги, периоды, parity entries, сброс, повтор sample | MorExposure production, offline.json | PASS |
| Zone radius/height/LOS/owner/caps/lifetime/no block writes | source gates и freeze HigherRules/HigherMor.zone | PASS (source) |
| Реальное воздействие зон на игроков/мобов, LOS/союзники в мире | Игровые сценарии ниже | NOT_RUN / OWNER_PENDING |
| Устойчивость фазы/цели в production | phaseBoundary/committed подключены к scheduler, source checks | PASS (source/policy) |
| Непрерывный hurt, knockback и совпадение кадра с hit в бою | Диагностика готова; исходный runtime-сценарий не воспроизведён | NOT_RUN / OWNER_PENDING |
| Смена цели, смерть/уход цели, вода и leash в мире | Source gates сохранены; требуется игра | NOT_RUN / OWNER_PENDING |
| HP/MaxHealth, NBT, dimensions и атрибуты | baseline freeze + native NBT/attributes regression | PASS |
| Models/UV/PNG/particles/sounds и #140 | byte-exact source/resources baseline | PASS |
| Тугарин/Овинник/другие: несвязанный код/параметры | scope freeze и неизменная ветка Ovinnik | PASS (source) |
| Реальный боевой regression других существ | Игра не запускалась | NOT_RUN / OWNER_PENDING |
| Java21 clean build, production JAR, resource decode/parity | build-likho93.log, verification.json | PASS |

**Фактических запусков Minecraft, PolyMC, GameTest, сервера: 0.** Здесь нет runtime PASS. Только заранее обозначенные игровые сценарии остаются владельцу.

## Проверка владельцем

На отдельной копии тестового мира, без сюжетной встречи/защиты:

1. Записать HP обычного и морового Лиха до/после обновления, сравнить сохранённые атрибуты. Наблюдать каждую форму против zombie+skeleton+spider пять минут. Особенно смотреть SWEEP и удар приземления; затем повторить один зомби и игрок Survival со щитом.
2. В фазах2/3 проверить, что по мобам не тратится GAZE, при окружении используется SWEEP; против игрока взгляд работает при взгляде на босса. Проверить уклонение из конуса/дистанции, LOS за стеной.
3. Mor ground: находиться непрерывно≥60 ticks в r3; выйти/вернуться и проверить сброс. Cloud: r5, slow в первом sample, damage с20 экспозиции. Повторить игроком и каждым из трёх мобов, союзником/creative/spectator/сюжетным NPC; проверить высоту, стену и исчезновение владельца.
4. Бить Лихо непрерывно во время windup и на границе фазы. Оно продолжает получать урон/отбрасывание; готовый удар срабатывает лишь при сохранённых геометрических условиях. Убить/увести текущую цель и наблюдать корректный следующий target; отдельно вода, выход из leash, перезагрузка мира.
5. Коротко сверить Тугарина, Овинника и новые головы #140; прежний лут/HP/визуал должны сохраниться.

Если замах по-прежнему кажется отменённым, перед ручным запуском можно добавить JVM-флаг `-Dslavicmyths.likho.trace=true`, сохранить `latest.log`, затем убрать флаг. Префикс `LIKHO93` содержит UUID, tick, MOVE/elapsed/morSkill/target, причину recover/reject и actualDamage. Максимум160 строк на сущность за её загруженный экземпляр; после лимита нужен новый экземпляр/новая сессия для следующего короткого теста. По умолчанию логирование выключено. Наблюдение может объясняться честным выходом из радиуса/конуса/LOS или knockback; это не автоматически сброс server action.

Точный JAR/SHA256/source и результаты — `verification.json`; если установлен тестовый candidate, точный installed path/backup/no duplicates фиксируется в `installed.json`. Установка не считается игровой приёмкой. #93 не закрывать до личного подтверждения владельца; к #142 автоматически не переходить.

Тестовый candidate **установлен**: `C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.9.7.52.jar`, SHA256 `1577b7abb0916323a8f0507d1a2d44a70a72108dcf732e0c9987a7b0b8a4bf38`. Backup1.9.7.51 и точный source manifest SHA — [installed.json](installed.json); дублей0, остальные моды сверены и неизменны.
