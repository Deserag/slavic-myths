# Boss reimplementation — 1.3.4.5.3

Источник задания: `Slavic_Myths_Boss_Rework_REIMPLEMENTATION_Codex.zip`.
Предыдущий этап 1.3.4.5.2 был отклонён пользователем; его модели не используются
как доказательство приёмки. Ниже описана новая реализация, результаты итоговых
gates фиксируются в `verification/boss-reimplementation`.

1. Registry IDs сохранены: `slavicmyths:tugarin_zmey`, `slavicmyths:nightingale`.
2. Тугарин: `WorldBoss` + отдельный ограниченный `TugarinCombat`. Лихо сохраняет прежнюю ветку поведения.
3. Соловей: `NightingaleEntity`, явные PHASE_ONE / TRANSFORMING / PHASE_TWO, один ServerBossEvent.
4. Четыре новых manifest: `assets/slavicmyths/boss_geometry/{tugarin,nightingale}_phase{1,2}.json`.
5. Генератор оригинальных cuboid моделей: `tools/boss_reimplementation_resources.py`; native `BossReworkGeometry` / ModelPart.
6. Четыре самостоятельных PNG atlas 512 × 512; никакие пиксели reference images не копируются.
7. Отдельные экземпляры моделей и выбор по synced phase; смена геометрии во время перехода. Внешний вид требует клиента.
8. Тугарин: 350 HP, порог 175; переход 60 ticks, DR 85%, без лечения и замены UUID.
9. Соловей: 250 HP, порог 125; переход 40 ticks, DR 75%, без лечения и замены UUID.
10. Сохранение phase/cooldowns; незавершённый переход загружается сразу во вторую форму без повторения звука/анимации. Это native NBT roundtrip, не пользовательский перезаход.
11. Обычный удар Тугарина: raw 12 / 12; измерено 3,8400002 HP / 3,8400002 HP по настоящему ServerPlayer в полной алмазной броне (Armor 20, Toughness 8).
12. Обычный удар Соловья: raw 10 / 12; измерено 3,0 HP / 3,8400002 HP в том же оснащении. Обычный native damage pipeline; обхода брони нет.
13. Тугарин 1: sweep 16 (4 blocks, 130°), charge 14 (ограничение 9 blocks), combo 5 + 5 + 8, обычный удар 12.
14. Тугарин 2: breath 6 за импульс + огонь (8 blocks, 60°), wing dive 16, lunge 14 (5 blocks), smoke burst 8 (5 blocks).
15. Соловей 1: обычный 10, deadly whistle 14 (10 blocks, windup 16), leap landing 12 (radius 2), combo 4 + 4 + 7.
16. Соловей 2: скорость +20%, обычный 12, whirlwind 12 (radius 6), dash 12, wave 14 (12 blocks), onslaught: ограниченный dash, новый фиксированный heading для двух ударов 4 + 4 и свиста 10. Уклонение возможно.
17. Игроки, Villager, WanderingTrader и IronGolem — допустимые автоматические цели. Recent damage map ≤32, expiration 200 ticks, reevaluation 10 ticks, target lock 30 ticks. Нет глобального обхода мира/игроков.
18. Страх жителей: дополнение существующего реального VillagerHostilesSensor, который в 1.21.1 имеет жёсткий список типов. Выдуманный vanilla tag не создаётся. Голем: штатный Enemy для Соловья; отдельный vanilla target goal через mixin для Тугарина, без изменения Лихо.
19. Реальное `nightingale_spawn_egg`: DeferredSpawnEggItem, template_spawn_egg model, RU/EN, существующая mobs Creative Tab.
20. `/summon` обоих боссов и `/give` яйца/кинжала выполняются настоящим command dispatcher; registry/type/creative проверены.
21. Кинжал сохраняет ID `nightingale_dagger`, melee 10; старая дальность 6,5 → 8 blocks, фиксированная ширина 3, wave damage 7, charge 8 ticks, cooldown 140; UseAnim.NONE. Strong whistle: radial radius 6, cooldown 320.
22. WindImpulse гарантирует ограниченный наружный импульс после native knockback resistance; high-resistance Тугарин на 7,5 blocks получил урон и фактическое перемещение 0,5 blocks через native collision movement. Иммунитет задаётся mod tag `wind_immune`, а не большим MAX_HEALTH.
23. 39 требуемых underscore SoundEvent keys зарегистрированы и подключены; старые 39 dotted IDs сохранены. Временные vanilla/original aliases описаны отдельно; финальная звуковая оценка требует клиента. Draw использует equipment-change event.
24. Лут, camp/progression/participants и deferred loot Соловья сохранены: guaranteed mark/weapon/Flight fragments; Тугарин — us, kozha 3–6, Berserker fragments, парные клинки 12%. Production JAR не включает GameTests. Итог build/tests/JAR/install см. машинные отчёты; commit автоматически не выполняется.

Критические автоматические проверки: четыре diamond melee; порог и сохранение фаз;
реальные summon/give/egg/creative; оба нападения на жителя; native fear memory и
цель голема; свист на 6 blocks; wind damage/velocity/native displacement/range;
Weakness ровно один раз; четыре native geometry и UV; registry и подключение звуков;
приземление leap и escapeable onslaught. Они не заменяют [ручную визуальную проверку](MANUAL_VISUAL_CHECK.md).

MSPT не измерялся. Реальный клиент, пользовательские миры и UI PolyMC не запускались.

## Итоговые проверки и установка

Реализованы четыре самостоятельные модели/текстуры, две фазы и атаки обоих боссов, village targeting, яйцо Соловья, волна кинжала и 39 звуковых событий. Последние исправления челюсти Тугарина и спуска Соловья с платформы включены.

В изолированной сборке 1.3.4.5.3: clean build PASS, 40/40 boss + 63/63 combat GameTests, 9176 native model checks, 20491 rune checks и resource/JAR gates PASS. 16 реальных headless запусков / 0 клиентов. Установленная параллельной задачей 1.3.4.5.5 содержит побайтно те же 37 файлов кода/ресурсов боссов и 39 звуковых событий; production JAR совпадает с build/libs. Новая версия не откатывалась, файлы снаряжения не изменялись этой задачей. Подробности: [отчёт](IMPLEMENTATION_REPORT.md), [gates](verification/boss-reimplementation/gates.json), [интеграция](verification/boss-reimplementation/integration-1.3.4.5.5.json). Визуальная/звуковая клиентская приёмка остаётся ручной. Commit/push не выполнялись.
