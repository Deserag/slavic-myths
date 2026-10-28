# Обновление домашнего ПК — 28.10.2026

Установлен технический кандидат 2.0.8-HELL; приёмка владельца отсутствует.
Minecraft/PolyMC/сервер/GameTest: 0 запусков.

## Источник и сохранность

Рабочая папка D:/slavic-myths. До обновления hell@877b7d69 была чистой;
единственный worktree, пользовательских незакоммиченных изменений не было.
Fetch --prune и pull --ff-only обновили hell до
`d97a625fbe23b9345e7879f39463099fa676b71f`.
main/master не менялись; новых веток, коммитов, push, Issue/Project изменений нет.
Production src/main, build.gradle и gradle.properties соответствуют этому HEAD.
Отчёты и результаты новых проверок — локальные изменения документации.

Исходный JAR: D:/slavic-myths/build/libs/slavicmyths-2.0.8-HELL.jar.
SHA256: `4a76d2198a7c3dbad006774586ef8d041bb8df8dfefb5d02f6935896bcec830b`.
JDK C:/Program Files/Java/jdk-21.0.12. MC1.21.1, NeoForge21.1.255.

## Что подтверждено исходниками

Свежие #304/#313/#316/#317–#323/#327 прочитаны через GitHub; старые фразы
«кода нет» в них не приняты за состояние текущего HEAD.
- Чертоги: HallsContent/HallsWorldgen/HallsTerrain, пять видов жителей,
  PostenMob650HP, четыре маски и разделение4×65, материалы, Камертон,
  метки/навигация, руна, сцены и арена подключены. Физическая генерация,
  доступность и гарантии арен ещё не проверены в игре.
- Горючие острова: три самостоятельных моба, материалы/рецепты/Руны I–II;
  Повелитель850HP, две фазы, шесть действий, нейтральность до нападения,
  журнал и пакет наград. MobCategory.MONSTER, shouldDespawnInPeaceful,
  ранний discard и bar.removeAllPlayers присутствуют; обычная fauna сохранена.
- #327: обновлённая геометрия Сторожа/Жареницы/Повелителя, Постень,
  головы в общей TrophyHeads; существующий zharenitsa_head ID сохранён.
  У Повелителя native jaw и отдельная статичная inventory icon.
- 2.0.5: burning_islands.json зарегистрирован как data, features пустые,
  climate selector не включает его. НЕТ полноценной ocean-only географии,
  островов/центрального вулкана/рва/мостов/естественной арены и босса.

## Матрица исполнения этого поручения

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Актуальный чистый production origin/hell | HEAD выше; git diff -- src build.gradle gradle.properties пуст | PASS |
| Java21 clean build | build.log, 2m17s,100 warnings deprecated/unchecked | PASS |
| Ресурсы/production JAR | resources.json:8449resources/4766JSON/1527PNG/2046models/2090Java21classes | PASS |
| Журналы/модели/регрессии offline | offline.log:32826logic checks,9meshes,412frames/81916checks,6840695legacy-head checks | PASS только offline |
| Climate | 5seeds/20windows/8classifications/53candidate regions | PASS только classification; реальные арены NOT_RUN |
| Строгий ocean-only2.0.5 | ocean.log:534/1053hypothetical climate samples без лавы на sea level; selector disconnected | BLOCKED, код отсутствует |
| Установка/backup/ровно1JAR/другие моды | installation.json,installed SHA равен build | PASS |
| OriginalPNG | references.json; home scan10639PNG/343ZIP,0matches | BLOCKED_REFERENCE_BINARY |
| Оригинальные голоса/OGG205 | burning-islands-205/AUDIO_SOURCE_MATRIX.md | BLOCKED_NEEDS_AUDIO_SOURCE |
| Runtime/Peaceful/battle/MP/relog/oldworld | запуск запрещён владельцем | NOT_RUN |

Не выполнены: точное ART-сравнение, аудио205, worldgen205/#313/#320,
блоковая часть РуныII (fail-closed), cross-player crash exact-once наград.
Полная сдача #304/#316/#317–#323/#327 не заявляется; Issues не закрывались.

## Установка и откат

Заданный C:/Users/user и инстанс2.0.8-HELL-Testing отсутствуют. Реальный selected
instance из polymc.cfg — Slavic-Myths-2.0.0-HELL, display name
«Slavic Myths 2.0.1 - HELL - TEST», MC/NeoForge проверены до копирования.
Установлено:
`C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/.minecraft/mods/slavicmyths-2.0.8-HELL.jar`.
Backup:
`C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL/mod-backups/20261028-202419/slavicmyths-2.0.8-HELL.jar`.
Backup SHA64b042ae3def10952a0c120d8dac9b16c95658070450bb0b323fd48afad3084e.
Другие моды проверены по SHA; миры/config/instance.cfg не изменялись.
Для отката при закрытой игре вернуть backup JAR; мир, уже сохранённый новой
версией, восстанавливать только вместе с согласованной полной копией мира.

## Как проверить в Minecraft — владелец, NOT_RUN

1. Сначала полная копия старого мира. Новый Survival Normal(seed42041), портал
   обычным путём без OP → новые чанки Nether. Найти Чертоги, шесть типов сцен;
   проверить проходы/мосты в обе стороны, пять жителей и добычу без читов.
2. Камертон/метки/руна: получить и применить по рецептам; повторное взаимодействие,
   пустой ресурс и чужой MP-владелец не должны давать бесплатную награду.
3. Постень:650HP, четыре маски, при260HP четыре копии65HP; победа даёт один
   пакет. Проверить полный инвентарь, повторный заход, сохранение в фазеII и2клиента.
4. Отдельный diagnostic flat world: /summon slavicmyths:magma_lord ~ ~ ~.
   До нападения нейтрален;850HP, переход на425, все телеграфы и уклонения.
   /difficulty peaceful во время каста: entity и bar исчезают без дропа.
   Normal и повторный relog не возвращают удалённого босса; обычные3моба
   не исчезают только от Peaceful. Это не проверка естественного появления.
5. Головы четырёх боссов: ровно1за победу; пол/стена/поворот/снятие/relog,
   jaw и статичный GUI Повелителя. В копии старого мира старая zharenitsa_head
   сохраняет ID/ориентацию; прежние6голов работают.
6. Повторить negative/relog/MP и5seeds42041–42045. Проверить соседние vanilla
   биомы/структуры и старые чанки. Горючие острова естественно искать этой
   сборкой бессмысленно: worldgen ещё отсутствует.

OP-диагностика отдельно: /locate biome slavicmyths:lost_halls;
/summon slavicmyths:posten ~ ~ ~; /difficulty peaceful.
Подробные шаги: ../p0-bosses-327/OWNER_SMOKE_CHECKLIST.md
(его старая фраза POLYMC_NOT_INSTALLED теперь заменена installation.json).

Риски: старт loader/mixins, непрерывность генерации, boss lifecycle/награды,
старые головы, MP и MSPT не измерены в игре. Следующая задача — ручная проверка
этого кандидата владельцем; новые функции автоматически не начинаются.
