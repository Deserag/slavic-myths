# Тугарин Змей — 1.3.4.5.1

2026-10-08. Реализация по архиву Slavic_Myths_1.3.4.5.1_Tugarin_Rework_Codex. Код и автоматические gates завершены; внешний вид, аудио и ощущение боя требуют ручной клиентской приёмки: **REQUIRES_MANUAL_CLIENT_CHECK**. Клиент, GUI и PolyMC не запускались. Общий баланс, курган, Knowledge и Classes GUI не входят в этот проход.

## 1. Аудит до изменений

WorldBoss обслуживал одновременно Лихо и Тугарина. У Тугарина были три стадии при 70%/35%, 18-тиковая смена стадии, Punch/Spin/Charge/Slam/Stone/Breath, движение +6% и цели HurtByTargetGoal/NearestAttackableTargetGoal. Punch наносил 12 raw; это кодовое число, а не измерение старого урона по игроку. Реального старого боя в клиенте не проверяли. Модель уже имела широкий живот, усы, наручи, сапоги и пояс, но не имела крыльев и самостоятельной змеиной формы. EntityType: 1.5 × 2.9. Звуки — Ravager, Iron Golem и общие vanilla cues. Loot уже гарантировал ус, кожу и rare fragment: задача не требовала нового пустого материала. Парное оружие уже было одним stack с offhand escrow и двумя render-only клинками; отсутствовали чередование взмахов и звук убирания.

Отдельного Tugarin raid нет: BossAnchors создаёт естественные региональные встречи, BossRitualItem — управляемый призыв. BanditRaids к Тугарину не относится.

## 2. Production-файлы этого прохода

Новые: `hunt/TugarinCombat.java`, `client/TugarinSerpentGeometry.java`.

Изменённые относительно рабочего состояния 1.3.4.4: `hunt/WorldBoss.java`, `hunt/BossRules.java`, `hunt/BossAnchors.java`, `registry/ModEntities.java`, `client/WorldBossModel.java`, `client/WorldBossRenderer.java`, `client/TugarinPairClient.java`, `combat/TugarinPair.java`; два `lang/{ru_ru,en_us}.json`, `loot_table/entities/tugarin_zmey.json`, `gradle.properties`, `build.gradle`. Все Java-пути — внутри `src/main/java/org/slavicmyths`, ресурсы — `src/main/resources`. Ранее незакоммиченные правки 1.3.4.4 и пользовательские файлы сохранены.

## 3. Тесты и tooling

`tools/tugarin-tests/org/slavicmyths/hunt/TugarinGameTests.java`, `tools/tugarin-test-resources/data/slavicmyths_tugarin/structure/empty.nbt`, `tools/tugarin-model-tests/org/slavicmyths/verify/TugarinModelsOffline.java`, `tools/tugarin_resources_13451.py`, `tools/verify_tugarin_13451.py`, `tools/install_tugarin_13451.py`; прежний `combat_fix_resources_1344.py` подключает актуальный additive overlay. Source sets включаются только opt-in свойствами. Production JAR не содержит GameTests/offline fixtures.

## 4. Состояние и сохранение

Синхронизированный TugarinState: PHASE_ONE → TRANSFORMING → PHASE_TWO. Порог <=50%, тот же UUID/entity и bossbar; max HP 350, лечение отсутствует. Переход запускается сразу при успешном пересекающем ударе, либо на следующем AI-тике после прямой установки здоровья. Stage и cooldown записаны в NBT. При загрузке сохранения <=50%, PHASE_TWO или прерванного TRANSFORMING сразу восстанавливается вторая форма; полная сцена повторно не проигрывается. Старые сохранения ниже порога мигрируют. Вторая фаза не откатывается при лечении выше 50%. Нативный NBT load проверен; настоящий клиентский перезаход и перезапуск мира этим тестом не считаются.

## 5. Трансформация

60 тиков / 3 секунды. Навигация остановлена, атак нет, входящий урон ×0.15 через обычный damage pipeline; это конечное снижение, не абсолютная неуязвимость. Дым каждые 10 тиков; на 20-м — growl + костяной crack; на 30-м клиент выбирает отдельную геометрию Змея; на 40-м — огненные частицы, взмах крыльев и LOS knockback без урона; на 60-м — вторая фаза и 12 тиков recovery. До смены геометрии торс постепенно собирается; после неё раскрываются крылья. Параметры защиты сохраняются при следующем ударе в тот же тик.

## 6. Баланс

| Параметр | Земная форма | Истинный облик |
|---|---:|---:|
| Max HP | 350 | 350, без heal |
| Melee raw / attribute | 6 | 6.6 |
| Movement speed | 0.285 | 0.35055 (+23%) |
| Armor / toughness | 15 / 4 | 15 / 4 |
| Knockback resistance | 0.78 | 0.65 |
| Обычный цикл при доступной близкой цели | 30 тиков / 1.5 с | 24 тика / 1.2 с |
| Размер hitbox | 1.3 × 2.35 | тот же |
| Eye height | 2.12 | 2.12 |

Melee проходит нативные броню, сложность, щит и эффекты. Weakness не применяется дважды: для Punch берётся attribute, для специальных ударов учитывается отдельно. В GameTest проверены 6 HP на unarmored ServerPlayer и 2 HP при Weakness; 6.6 HP второй фазы проверены на unarmored mob. Значения по игроку с бронёй/на разных сложностях и сложность реального боя остаются ручными.

## 7. Способности первой фазы

| Действие | Условия/радиус | Подготовка | Raw damage | Cooldown |
|---|---|---:|---:|---:|
| Heavy Punch | <=2.9 | 10 тиков | 6 | 24 тика; полный цикл 30 |
| Sweep | <=4, дуга 120° | 14 | 8, KB 0.65 | 200 / 10 с |
| Charge | 5–10, grounded | 10 | 8, один hit, KB 0.8, Slow 14 тиков | 240 / 12 с |
| Existing Stone | 8–18 | 18 | прежний BossStone 7 | 280 / 14 с |

Charge держит стартовое направление; скорость 0.8 блока/тик, движение ограничено 12 тиками и примерно 9–9.6 блоками, заканчивается при горизонтальном столкновении. Recovery 24 тика. Камень переиспользует существующие entity/renderer/ID. Sweep, charge и обычные удары проверяют LOS; сквозь стены sweep не проходит.

## 8. Способности второй фазы

| Действие | Условия/радиус | Подготовка | Raw damage | Cooldown |
|---|---|---:|---:|---:|
| Fire Breath | 3–8, конус 50° | 18 тиков | 3 × 3 импульса, Ignite 2 с | 240 / 12 с |
| Wing Dive | 5–12, grounded | 8 | 7, один hit, KB 0.8 | 280 / 14 с |
| Serpent Lunge | <=5, hit <=3.8 | 8 | 4.5, Slow 45 тиков | 150 / 7.5 с |
| Smoke/Fire Burst | <=4.5 | 14 | 3, Ignite 2 с, Slow 35 тиков, KB 0.55 | 340 / 17 с |

Breath: smoke у рта на подготовке, три LOS cone hit в течение 24 тиков; не размещает fire blocks. Dive: короткий takeoff, glide до 20-го тика, фиксированный dive, посадка/столкновение/36-й тик/дистанция завершают действие. Не включает noGravity или способности полёта, сбрасывает fallDistance только в собственном воздушном действии. Burst не даёт blindness. Lunge и charge не дают длительного stun. PUNCH второй фазы сохраняется; старые Spin/Slam/Stone/Charge не выбираются второй фазой.

## 9. Планировщик

Один контроллер на одного Тугарина, один текущий synced Move. Проверка выбора действия раз в 6 тиков только в IDLE, cooldown map уменьшается локально; действия и recovery эксклюзивны. LastSpecial предотвращает немедленное повторение одного типа спецприёма. Направление фиксируется при начале, rotation догоняет его на telegraph; новое действие не ретаргетирует уже выпущенный рывок. Лихо продолжает пользоваться прежним Fight; оставшиеся старые ветви Тугарина в legacy shared code обходятся ранним dispatch в TugarinCombat и не выбираются новым scheduler.

## 10. Aggro

Вредящие сущности регистрируются через успешный hurt: максимум 32 UUID, свежесть 200 тиков, оценка учитывает накопленный недавний damage и дистанцию. Reevaluation раз в 10 тиков, обычная фиксация 30 тиков; ранний switch возможен при score >2× текущего +8. Умершая/невалидная цель заменяется сразу. При отсутствии цели — локальный поиск видимых игроков в AABB32, без поиска произвольных мобов по всему миру. Проверены сигналы настоящего playerAttack и skeleton mobAttack, фиксация цели и переключение после смерти игрока. Нет 30-секундного периода нерешительности. Фактический клиентский бой с игроком и скелетом остаётся ручным.

## 11. Bossbar и ownership

Один прежний ServerBossEvent. Во время смены — «Пробуждение», далее «Истинный облик» (RU/EN). Имя обновляется стандартным периодом 20 тиков, HP/progress — каждый server tick. Сохраняются encounter, bossOwner, home, hunt completion, award и loot hooks. Нового entity spawn при трансформации нет.

## 12. Модели, материалы, пропорции

Земная форма: исходная авторская широкая грудь/живот/усы, пояс с пряжкой, тяжёлые сапоги и наручи; кожа груди и живота закрыта отдельной бордовой тканью. Геометрия — 225 ModelPart. Общий scale0.81 даёт около 2.33 блока по высоте вместо прежней 2.9.

Змей: самостоятельная геометрия, не recolor прежней модели: более узкий торс, вытянутый snout/jaw, два клыка, прорезные глаза, накладные чешуйки, более длинные руки и по три когтя, умеренные составные крылья с рёбрами. 64 ModelPart. Крылья сложены назад через Y rotation; раскрываются, делают takeoff/flap/glide/dive и складываются в landing recovery. Обе формы используют существующий авторский 512×512 atlas tugarin_zmey.png: charcoal/бордовый/бронза/малый огненный акцент. Новых заимствованных текстур и новых PNG нет. Native ModelPart bake и UV проверены в обычной JVM, 9772 проверки; сцену Minecraft это не рендерит.

## 13. Анимации

Земная: idle breath, walk, punch, sweep, charge lean, stone windup, hurt, death. Transition: сжатие/выпрямление торса, смена силуэта в середине, раскрытие крыльев. Змей: idle, walk, folded/open wings, takeoff/glide/dive/landing, breath jaw+mouth glow, lunge, burst, hurt/death. Используется прежняя native model-state архитектура, новых animation dependencies нет. Синхронизирован previous move для landing recovery. Текстуры/модель/pose/видимость на экране не прошли клиентскую приёмку.

## 14. Звуки

Точные vanilla события (собственных/чужих OGG не добавлено):

- Земной ambient/hurt/death: `PIGLIN_BRUTE_AMBIENT`, `PIGLIN_BRUTE_HURT`, `PIGLIN_BRUTE_DEATH`; тяжёлые шаги `IRON_GOLEM_STEP`.
- Punch/charge: `IRON_GOLEM_ATTACK`; sweep: `PLAYER_ATTACK_SWEEP`; stone: `SNOWBALL_THROW`.
- Transform: `RAVAGER_ROAR`, `BONE_BLOCK_BREAK`, `ENDER_DRAGON_GROWL`, `ENDER_DRAGON_FLAP`.
- Истинный ambient/hurt/death: `ENDER_DRAGON_GROWL`, `ENDER_DRAGON_HURT`, `ENDER_DRAGON_DEATH`; fire/burst `FIRECHARGE_USE`; dive `ENDER_DRAGON_FLAP`; lunge `ENDER_DRAGON_GROWL`.
- Парные клинки: `ARMOR_EQUIP_IRON` draw volume0.45/pitch1.25, sheath volume0.3/pitch0.85.

Профиль фаз различается, но подбор тембра и громкости без прослушивания не считается принятым. Нет звуков козы/овцы/зомби, копирования другой игры или фиктивных OGG.

## 15. Loot

Все прежние pools сохранены: гарантированный `us_tugarina`×1, `tugarinova_kozha`×3–6 (+ прежний looting), berserker fragment×1; пряжка45%, набойка30%, extra fragment25%, full rare rune4%. Добавлен отдельный pool готового `tugarin_sword`12%. Кожа уже используется в существующем оружейном рецепте, поэтому новый материал без назначения не добавлен. Нативное убийство босса даёт кожу и fragment в GameTest. Frost/reflection/future rune pools не добавлены.

## 16. Парные клинки

Остаётся один ItemStack; второй клинок — только client render. Прежнее persisted offhand escrow возвращает полный stack с компонентами после переключения/смерти/logout; инвентарь/дроп используются при занятом offhand. Добавлены render-only чередующиеся slash в первой/третьей персоне и sheath sound. Нет второго native урона от декоративного клинка, registry ID и native bleed0.5 HP/max3 сохранены. Регрессионные 61 tests включают no-loss/no-dupe/switch/death/crafting/components. Клиентские slash/render/sounds требуют ручной проверки.

## 17. Частота появления

Один изменённый множитель: `BossRules.eligibleRegion`, Tugarin 1.0→0.25; Likho0.35 сохранён. Примерно четверть естественных регионов, а не увеличение всех cooldown. Для прежних сохранённых natural anchors тот же детерминированный eligibility применяется при активации: повторный probability roll не добавлен, множитель не возводится в квадрат. Уже активные боссы не удаляются; управляемые ритуальные призывы не ограничены этим natural gate. 7-дневный respawn unchanged. Статистический GameTest10000 регионов PASS. Это ожидание по seed-region, не измеренная частота рейдов в игровых часах.

## 18. Автоматическая приёмка

5 реальных автономных server launches, 0 client launches. Первый: 11/14, найден неинициализированный aggro lock и два неверных fixture flags (gravity/ground). Исправлены. Второй16/16, третий19/19, combat regression61/61, итоговый Tugarin21/21. Все логи сохранены в docs/verification/tugarin-1.3.4.5.1. Среди проверок есть реальный server AI tick трансформации, native player damage, LOS wall, native loot, native save/load и сигналы multi-attacker; часть scheduler scenarios ускорена прямыми вызовами контроллера на нативных сущностях. Fake player connection не является подключением клиента. Ни один тест не измеряет MSPT/FPS, игровую визуализацию, аудио или полноценный клиентский relog.

## 19. Build и ресурсы

`gradlew.bat clean build -PrareRuneOfflineCheck verifyRareRunes` PASS, Java21/MC1.21.1/NeoForge21.1.255. 20491 pure rune checks; 14 существующих deprecated/unchecked warnings. `verifyTugarinModels` PASS9772. Production/resource gate PASS11357, 3021 JSON,901 PNG; все ресурсы старого1.3.4.4 JAR кроме двух добавочных переводов и Tugarin loot сохранены побайтно. Model references, one-stack pair templates и отсутствие fixture classes в production проверены. `git diff --check` PASS.

## 20. Production JAR и установка

`build/libs/slavicmyths-1.3.4.5.1.jar`. SHA256: 5192078ab48f3fe1511f233bb2251c78f3327c03eb5264292474c266440b8522.

Установлен в существующий `Slavic-Myths-1.21.1-Testing/.minecraft/mods`, один активный Slavic JAR.1.3.4.4 сохранён в `.slavicmyths-backups`;765 прочих файлов, миры и настройки побайтно сохранены. MC/NeoForge компоненты unchanged. Установка выполнена через файловую систему, launcher/client не запускались; старый1.16.5 не затронут. Receipt: `verification/polymc-1.3.4.5.1-installation.json`.

## 21. Ручная приёмка и ограничения

**REQUIRES_MANUAL_CLIENT_CHECK**. Краткий список: [MANUAL_CHECK_TUGARIN.md](MANUAL_CHECK_TUGARIN.md). До ручного теста нельзя утверждать правильность silhouette/hitbox alignment, отсутствие визуальных clipping/wing jitter, достаточность звуков, чередование клинков на экране и качество telegraph/dodge windows. Никаких клиентских запусков по запрету из архива. Сравнения со старым реальным боем и измерений производительности нет.

## 22. Git / дальнейшая работа

Commit/push не сделаны: актуальный AGENTS.md запрещает автоматический commit. Пользовательские изменения и незакоммиченный предыдущий патч сохранены. Реализация этой итерации завершена в рамках разрешённых автоматических действий; следующий шаг — разрешённая пользователем ручная приёмка, затем только подтверждённые дефекты. [Машинные gates](verification/tugarin-1.3.4.5.1.json).
