# Срочный приоритет 2.1.x — восстановление runtime, 30.10.2026

## Scope и Git
2.2 приостановлена по прямому распоряжению владельца. 3f7ae53d остаётся в истории hell, StructureSet не включён. Новая ветка codex/hell-bosses-completion-20261030 основана на актуальной hell. Draft PR359 НЕ слит. Проверены обе ветки: morova-trizna откатывает runtime в efef5e1e; runtime-20261029 содержит отдельный восстановимый WIP15cde547. Он cherry-picked как fdfd1587, сохранив автора. main и исходный master не тронуты.

## Реальные изменения
- Исправлены три ошибки компиляции WIP: public visibility сохранения и igniteForSeconds API.
- Убрана vanilla melee goal: скрытые удары больше не обходят scripted attack windup. Движение к цели отделено от удара.
- Выдох Моровой девы: 30тиков подготовки, затем серверный resolve,12тиков recovery. Текущее атакующее состояние/таймер/CD/contact flag сохраняются в NBT.
- MorovaPlagueEffect + ModEffects.MOROVA_PLAGUE:200тиков, три уровня скорости -10/-15/-20%, II=1HP/60тиков, III=2HP/60тиков; обычный MobEffect, без бессрочного stacking. Точный эффект/молоко/refresh cadence требуют дополнительных runtime сценариев.
- Исправлен обнаруженный GameTest дефект HP: vanilla attribute cap1024; используется уже существующий species-local CreatureHealthBalance.uncappedMaximum и access transformer getMaxHealth. Новые боссы1350/1800HP, без глобального изменения vanilla cap.
- RU имена сущностей и RU/EN эффект, optional netherBossCheck fixture, исключённый из production JAR.

## Матрица #353 / #354
| Требование | Доказательство | Результат |
|---|---|---|
| Восстановление WIP из359 на актуальной базе | fdfd1587, исходный15cde547 | PASS |
| Java21 clean build | gates.txt | PASS |
| Production ресурсы/JAR | resources.txt | PASS |
| Реальные1350/1800HP в NeoForge | attributesAndSave | PASS |
| Сохранение action/CD/contact в NBT | attributesAndSave | PASS (НЕ relog) |
| Замедление трёх уровней и снятие modifier | plagueLevels | PASS |
| Полный бой/все атаки/зоны/копии/миньоны/фазы | не реализовано полностью | NOT_RUN |
| Ритуалы/компоненты/survival/рецепты | не реализовано | NOT_RUN |
| Soul/scrolls/трофеи/руны/Curios/anti-dupe | не реализовано | NOT_RUN |
|30+боёв,1000+drop simulations,MP,relog | не выполнялись | NOT_RUN |
| Оригинальный постер, совпадение клиентских ракурсов | оригинал физически не найден/изолированный GUI не подготовлен | BLOCKED_ART_REFERENCE |
| Согласованные отдельные звуки | WIP использует vanilla placeholders; полноценные лицензированные SFX не подтверждены | BLOCKED_AUDIO |
| PolyMC обновление | полнота/QA не достигнуты, профиль не изменён | NOT_RUN |

Два запуска настоящего изолированного GameTest server: первый1/2 (FAIL HP), после исправления2/2PASS. Ни один не является боевым/MP/client test. Java21 clean build успешен, NeoForge21.1.255. JAR SHA25653f0305eb300ca556b9b513a2ef7bb482f475b93058f744b758f73c0a6a1971e, версия пока2.1.6-HELL; это WIP, не новая готовая тестовая версия.

## Как проверить в Minecraft / риски / следующий этап
Текущий WIP НЕ предназначен для приёмки на оригинальных мирах. В отдельном dev/GameTest окружении: JAVA_HOME=JDK21; gradlew.bat clean build -PnetherBossCheck runNetherBossChecks; Python+Pillow tools/verify_resources.py. После полной реализации: новый survival мир -> компоненты в структурах2.1 (мост безлутовый) -> оба ритуала -> фазы/бой/точный дроп -> предметы/Curios/наковальня -> cooldown/повторный ритуал negative -> save/reload -> копия старого мира -> два клиента. Всё это пока НЕ ПРОВЕРЕНО. OP summon лишь диагностика модели/HP, не survival путь.

Сохранившийся WIP содержит неполный generic attack и vanilla SFX, не соответствует полному ТЗ. Миграции старых encounters и восстановление target на relog ещё не проверены. Откат собственных коммитов только на рабочей ветке; не ставить WIP в рабочий PolyMC. Следующая задача: полный Morova combat timeline/зоны/копии/тени, затем князь и общая наградная/ритуальная интеграция. К2.2 не возвращаться. Issues353/354 не закрывать и не объявлять Testing/готовыми.
