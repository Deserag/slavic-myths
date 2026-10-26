# #277 — Неупокоенный князь: IN_PROGRESS / OWNER_PENDING

Финальный босс великого кургана — `slavicmyths:unresting_prince`, Kind.PRINCE, tier2/finalRoom по KurganRoster. Воевода — отдельный противник. Base для независимого изменения: armor-аудит `1ef5dace` (production совпадает с edc91530). Только KurganCreature.java меняет production; #278/#284/броня сохранены. Runtime этого пакета отдельно разрешён владельцем в чате26.10.2026; клиент/PolyMC/user saves не запускались/не менялись.

## Исправлено

1. После принятого урона князь сохраняет настоящего living-attacker в пределах прежнего FOLLOW_RANGE40 и его последнюю позицию на прежние120ticks. До этого HurtByTargetGoal мог назначить невидимого стрелка при memory0, а Fight сразу сбрасывал цель. Учитываются живой/тот же мир/не союзник/canAttack/не creative или spectator. Отклонённый урон не создаёт эту реакцию.
2. Если стрелок стоит за границей арены, старая ветка `allowed(destination)==false → home` заставляла князя возвращаться к домашней точке. Теперь navigation получает ближайшую внутреннюю точку границы в направлении стрелка, с отступом под текущую ширину тела. Обычный pathfinding обходит препятствия. Прежний allowed/leash и rollback при выходе сохранены. Стены/геометрия не меняются, телепорта через стену не добавлено.
3. После projectile damage видимый дальний стрелок разрешает князю прежнюю щитовую стойку. Прежняя периодичность12/36ticks (после смены фазы6/36), фронтальная проверка и коэффициент .3 сохранены. В windup/recovery guard по-прежнему выключен. Это временная направленная защита, стрелы продолжают наносить урон; не новый иммунитет.

У князя нет BOLT — эту способность волхва ему не добавляли. Его существующие SUMMON/RUSH/COMBO/HEAVY/GRAB, три помощника,350HP/11damage/.28speed/40range/16armor/.7KB, пороги65%/30%, cooldowns/aim/release, loot/encounter victory/save IDs не менялись. Путь обычного игрока: найти великий курган → пройти его прежние комнаты/печать → войти в финальную комнату → выстрелить/сражаться → наблюдать сближение, ограниченный guard и прежние фазы → победа с прежней наградой/учётом. Natural discovery/полный Survival проход не выдаются за выполненный GameTest.

## Контракт проверки

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Правильный boss | KurganRoster/Kind.PRINCE | финальная комната великого кургана | roster source guard, native PRINCE registry | unresting_prince, не VOEVODA |
| Реакция на стрелы | hurt/Fight/arenaApproach | лук → стрелять с разных дистанций | actual Arrow flight8/16/24/28; damage source39 | target/движение к стрелку внутри арены |
| Дверь/стена | vanilla navigation/sensing/allowed | стрелять через открытый проход, уйти за стену | three-wide doorway; solid wall | пройти дверь, не ударить/телепортировать через стену |
| Щит | existing GUARD/hurt | обстрел снаружи арены | guard39 + directional damage | конечные окна, положительный damage |
| Цели/фазы/melee | HurtByTargetGoal/Fight/choose/release | второй стрелок → ближний бой при65%/30% | targetSwitchAndMeleePhases | смена attacker, phase1/2, настоящий melee damage |
| Summon/victory | unchanged KurganEncounters/EncounterState | прежняя фаза → победить | summonOnceVictoryAndSavedState |3 minions, LimitedSummon, bossDefeated/slot defeated сохраняются |
| Соседи | unchanged kind-specific handlers | воевода/волхв атакуют | native CHARGE/BOLT observation | прежние actions |

## Доказательства

Seed плана277; отдельный disposable GameTest world в run-prince277-20261026. Registry actors и обычные server ticks/vanilla navigation, survival ServerPlayer с заглушенным соединением. Площадки80×16×80, подготовленный ровный пол/дверь/стена; это **не сгенерированный полный курган и не два сетевых клиента**. Большинство pursuit tests начинают после уже израсходованного LimitedSummon; отдельный test проверяет первоначальный summon. Часть попаданий использует настоящий native projectile DamageSource напрямую для изоляции события;8/16/24/28 используют летящую Arrow. No OP acquisition proof.

| Попытка | Результат | Причина / граница |
|---|---|---|
| baseline-native.log |1PASS/3FAIL,4tests | до правки: home retreat24, забытая hidden target,0guard windows39; melee/phase PASS |
| run2.log |3PASS/1FAIL,4tests | production реакции исправлены; fixture ошибочно ожидал2000HP, штатный health refresh возвращает игроку20 |
| run3.log |8/8PASS | реальные20HP, добавлены8/16/28 doorway и directional guard damage |
| run4.log |10PASS/1FAIL,11tests | added summon/victory/other mobs; VOEVODA fixture наносил hurt в tick0, HurtByTargetGoal timestamp0 не менялся |
| run5.log |11/11PASS | non-prince hurt после2нативных ticks; production после run2 не менялся |

Итого **5 реальных GameTest server запусков,0клиентов**. Ещё prince277-baseline.log — ошибка конфигурации init script до запуска игры; не шестой runtime. Raw logs/trace содержат реальные координаты/время/HP/action/phase/target/guard/navigation/inside. Baseline before source byte snapshot в KurganCreature-before.java; это файл документации, не production sourceSet.

Например final trace outside24 смещается от центра примерно на9блоков к внутреннему краю и остаётся inside=true; baseline за200ticks оставался возле home. navDone=true на достигнутом краю — ожидаемая остановка на leash, а не доказательство преследования за пределами арены. Guarded4raw arrow оставляет около0.4608HPdamage; начальное обычное попадание —1.76HP. Native phase2 GRAB лечит князя90→102 после настоящего melee hit, прежний эффект сохранён. Серверные фикстуры создают большой пол перед стартом; предупреждение Can't keep up в setup не является измеренным MSPT production AI.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Root cause воспроизведён | baseline-native.log,baseline-trace.log | PASS |
|8/16/24/28/39 ranged response | run5 cases/trace | PASS controlled native server |
| Доступный проход3wide | run5 arrow28ThroughThreeWideDoor | PASS prepared geometry |
| Стена/hidden memory/no wall strike | run5 hiddenArrowRetainsRecentAttacker,20ticks | PASS указанного окна; все возможные стены NOT_RUN |
| Finite directional guard / no arrow immunity | run5 farArrow/directionalGuard,damage>0 | PASS |
| Target switch/phase1/2/melee | run5 targetSwitchAndMeleePhases | PASS controlled2players; network MP NOT_RUN |
| Three summons/victory/save state | run5 summonOnceVictoryAndSavedState | PASS server state; loot collection/full quest/relog NOT_RUN |
| CHARGE/BOLT neighboring kinds | run5 voevodaCharge/volkhvBolt | PASS выбранных actions; все6kinds/full fights NOT_RUN |
| Stats/moves/leash/geometry/loot/save / #278/#284 | source-jar.json, unchanged method/source guards | PASS static scope; не substitute runtime |
| Java21 clean build/current resources/production JAR | build.log,current-resources.json,receipt.json | итоговые receipts |
| Natural full encounter/old actual save/relog/client animations/сетевой MP | не запускались | NOT_RUN owner |
| PolyMC delivery/owner acceptance | установки/приёмки не было | POLYMC_NOT_INSTALLED / OWNER_PENDING |

## Как проверить в Minecraft

Точный итоговый JAR/version/SHA256 и source commit: receipt.json. Владелец устанавливает его в отдельный1.21.1/NeoForge21.1.255 test instance с backup прежнего mods/JAR; ровно один slavicmyths JAR. Использовать новый мир и отдельную копию старого, не оригинал сохранения. Автоустановки нет.

1. Без OP найти великий курган обычным исследованием/существующим путём подсказок. Пройти прежние помещения и открыть прежнюю финальную печать. Проверить именно Неупокоенного князя. Естественная доступность не заменяется /summon.
2. Из лука попасть по князю с8,16,24,28,39блоков, где это позволяет настоящая геометрия; записать позиции/наличие прямой видимости. При доступном пути внутри арены он должен сближаться, после стрелы — временами защищаться щитом. Стрелы продолжают сниматьHP. За пределы финальной арены князь не обязан выходить; наблюдать реакцию к её границе, существующий summon и атаки при возвращении в досягаемость.
3. Стрелять через открытый проход; отойти за сплошную стену после попадания. Ожидается движение к последней известной точке в пределах арены; никакого урона/новой телепортации сквозь стену. Негативный сценарий: продолжительно прятаться без новых попаданий — память не бесконечна. Попадание союзника/creative/spectator не должно создавать новую ручную retaliation-цель.
4. В ближнем бою проверить прежние Combo/Rush, на65% и30% здоровья прежние смены фаз/Heavy/Grab. Следить за cooldown, атакой после windup и тремя помощниками; щит не должен непрерывно действовать во время атаки.
5. Два настоящих survival игрока стреляют по очереди с разных сторон: князь реагирует на последнего допустимого нападающего, синхронно видныHP/действия. Выход/перезаход/смена измерения во время боя не дублируют князя/помощников; после убийства прежняя награда и победа остаются после relog, повторный вход не создаёт новый reward.
6. Проверить соседние комнаты: воевода применяет свой Charge/Sweep, волхв свои Bolt/Clones/Seal; их характеристики и награды прежние. OP диагностика допустима отдельно: F3/журнал координат, `/data get entity <UUID>` для CombatPhase/KurganRoom/LimitedSummon; это не Survival путь.

НЕ ПРОВЕРЕНО: полный natural dungeon на нескольких seed/рельефах, все реальные door/pillar/vertical ledge сочетания, реальный relog/respawn/network MP/client animation/audio, сбор награды и вся цепочка progression. Князь с прежним melee набором по-прежнему ограничен arena leash; абсолютная угроза стрелку в любом недоступном месте не заявляется.

## Риски/откат / следующая задача

Риск: на сложной геометрии clamped destination может иметь только partial path; на внешней недоступной высоте щит/граница не гарантируют попадание по стрелку. Retaliation меняет приоритет последнего допустимого attacker только у PRINCE. Static guards и два native regression cases не доказывают все поведения соседей. Для отката отдельно revert коммита #277; #287 проверочный коммит самостоятельный, main не менять. Установленного обновления/backup нет, так как PolyMC не трогали. Issues #277/#287 остаются OPEN/«В работе»/OWNER_PENDING; Project fields не изменялись. Следующая задача — игровая проверка владельца и устранение конкретных оставшихся замечаний; #285/#286/#276/#298/четыре acquisition решения #284 не запускаются автоматически.
