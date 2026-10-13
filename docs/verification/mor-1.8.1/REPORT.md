# Мор 1.8.1 — итог реализации и проверок, 2026-10-12

Реализован пользовательский пакет [Slavic_Myths_1.8.1](../../tasks/1.8.1/00_START_HERE.md) поверх рабочей 1.7.9. Minecraft **1.21.1**, NeoForge **21.1.255**, Java **21**, `slavicmyths`, прежние registry IDs/зависимости сохранены. Ветка `main`; commit/push/новые ветки не создавались.

## Результат

- Четыре старых entity IDs: Леший, Полудница, Кикимора, `ElementHuntMob` именно типа `fire_serpent`. Отдельные моровые яйца/Овинник/будущие milestone не добавлены.
- Общий `MorController`/`MorSnapshot`: NORMAL →50ticks TRANSFORMING → ACTIVE, synced remaining/skill clock/clone/concealment; namespaced NBT, pause on unload, продолжение transform/cast после чтения данных, idempotent Flask. Данные старых сущностей не копируются в новые объекты.
- MAX_HEALTH permanent `ResourceLocation` modifiers ×2.5/2/2/2.5 сохраняют base и текущую долю HP. Отдельные damage/speed modifiers только ACTIVE; transient freeze при transform; входящий урон допускается. Защищены custom-named/friend/encounter-owner/player passenger/clone; неподдерживаемые NPC не заражаются.
- `MorLeshy`: корни24t/4HP/hold30t и вопль20t/3HP. Три кратковременных native корня, не блоки. `MorPoludnitsa`: flash20t/2HP, noon5800..6600=3HP, blindness30t; dash14t, swings19/25 по3HP. Прежний reveal не запускается поверх мора, а прежний размер формы сохраняется при сохранении/загрузке.
- `MorKikimora`: три collision-checked шага/прыжка10/16/22 и финал32t/4HP; thread16t/2HP/hold20t/life14t. `MorSerpent`: warning22t, projectiles22/27/32 по3HP/life24t; dive18..28/6HP и recovery до40t. Серверные hit gates, focus UUID, LOS/loaded collision checks; специальные атаки не меняют блоки и не поджигают мир.
- Змей при≤50% однократно сохраняет summon flag, после30t создаёт две копии прежнего entity type по30HP. Сохраняются UUID, число созданных копий, interrupted cast, bounded retry, clone age. Нет рекурсии/повторной генерации после гибели; clones flame2HP/dive3HP, life1800 loaded ticks, orphan grace200ticks. Clone death bypasses vanilla kill statistics/quests/loot/XP;10tick fade. Bossbar скрыт, пока есть копии. Отдельный synced concealment скрывает Jade health row у всей тройки через optional reflection callback без зависимостей/правки configs; contract установленного Jade15.10.6 проверен [javap](jade-api-contract.txt), клиентский callback в игре НЕ тестировался.
- Единственный новый видимый предмет `slavicmyths:mor_flask`: existing magic Creative tab, `/give`, без рецептов/лутов/варки/диспенсерного броска, 16px sprite/tooltip RU+EN. Throwable shell расходует1 в Survival/Adventure, cooldown10t, радиус3 от impact до центра AABB; один серверный impact, без облака/эффекта на игрока. Два технических entity IDs только для Flask/effect bolt; яйца не создаются.
- Natural4% после dayTime≥168000 в Overworld и horizontal distance>160 от spawn, только NATURAL/SPAWNER, с одним loaded AABB cap запросом после удачной рулетки. Реальное FinalizeSpawnEvent вызывается **до** finalizeSpawn; pending кандидат применяется в override **после** super.finalizeSpawn, без новых весов/биомов/спавнеров.
- Единственная новая запись существующей Книги «Мор: первые признаки», RU/EN/icon Flask; серверный encounter≤16 для живого Survival/Adventure, каждые40t, once-only через прежний TaleData.promote. Creative/spectator/копии не открывают её.
- Отдельные native экземпляры четырёх моделей/atlases, дискретные transform stages, реальные action cues/particles. Источники/ракурсы: [ART_AUDIO_SOURCES](ART_AUDIO_SOURCES.md).

Ранее обсуждавшиеся книга/дерево навыков, оружие/руки богатырей и лимит восстановлений из макетов 1.8.0 не внедрялись этой задачей. Остальные прежние изменения пользователя и параллельные отчёты сохранены.

## Выполненные gates

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build -I tools/mor-offline.gradle -I tools/ui-offline.gradle morOfflineChecks uiLogicChecks --offline
python tools/verify_mor.py
python tools/mor_preview.py
./tools/install_mor.ps1
```

| Проверка | Итог / реальные границы |
|---|---|
| Final clean build | PASS, последний запуск1m; [log](../../../work/mor-1.8.1/clean-build-final.log); старые deprecated event-bus warnings остаются |
| Offline production policy/schema/native geometry | PASS **237786 assertions**, **69 profiles**; большинство assertions проверяют UV/координаты повторных поз, это НЕ число игровых тестов |
| HP fraction/real AttributeInstance save/load | PASS; stable modifier ID/base preservation; НЕ игра/реальный relog |
| Three-state50t, protected/idempotent policy, summon-once/clone identity, interrupted cast/UUIDs/cooldowns | PASS production functions/MorSnapshot NBT; НЕ disk-world restart |
| Natural day/dimension/reason/distance/RNG boundary matrix | PASS; cap hook/source audited; реальная частота появления NOT_RUN |
| Combat release schedule | PASS exact tick pulses/cooldowns source; damage, LOS, collision, эффект на настоящем игроке/полный бой NOT_RUN |
| Four NORMAL native meshes/UV vs1.7.9 | PASS, actual old model classes baked through isolated classloader |
| Existing UI helpers | PASS **71** |
| Production/resource/source gates | PASS **33390**; all JSON, models/texture links, existing OGG/vanilla event links, RU/EN old keys retained, generator reproducibility, source/JAR identity, no test fixtures |
| Subscriber bytecode | PASS **174**, no empty subscribers |
| Prior JAR entries | **7563** byte-identical; changed old entries explicitly allowlisted in [resource-gates.json](resource-gates.json) |
| Client visual/audio, real multiplayer/rejoin/world restart | **REQUIRES_MANUAL_QA / NOT_RUN** |
| FPS/MSPT | **NOT_MEASURED**; absence of global scan/chunk tickets verified statically, no performance claim |

За эту реализацию: **0 Minecraft client starts, 0 dedicated/GameTest server starts**. Java offline main был запущен **9** раз; три clean build команды; четыре промежуточных compilation failures исправлены. Последний `clean-build-final.log` заменяет предыдущий повтор этого же файла; итоговые build/native/UI результаты PASS. Игра, dev client, PolyMC GUI и миры агентом не запускались.

## Production и установка

JAR: [build/libs/slavicmyths-1.8.1.jar](../../../build/libs/slavicmyths-1.8.1.jar), **15901494 bytes**.

SHA256: `8ea198902455bbde7406f06cb1387a971dbb9b673403267d2697e0375f975322`.

PolyMC **PASS**: финальный JAR установлен в существующий `Slavic-Myths-1.21.1-Testing` после закрытия PolyMC/Minecraft пользователем и проверки процессов. До финала там уже была промежуточная1.8.1 от параллельной установки с другим SHA; она сохранена в `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-1.8.1-20261012-150533`. Прежняя backup1.7.9/её отчёт также сохранены.

Установленный SHA совпадает с gates. **327** остальных файлов профиля сохранены побайтно, configs/миры/сторонние JAR/`instance.cfg`/`mmc-pack.json` не изменены; дубликатов нет. Старый1.16.5 не затронут. [Квитанция](POLYMC_INSTALLATION.md), [JSON](polymc-installation.json).

## Краткая ручная приёмка

1. В отдельном TEST world получить `/give @s slavicmyths:mor_flask 1`; четыре старых яйца, splash одной/нескольких целей,50t transform, повтор и protected NPC.
2. Сравнить normal/mor front/side/back и все attack warnings/hits/misses/cover; звук, cooldown, UI/held sprite.
3. Змей≤50%: две opaque копии30HP, скрытые bossbar/Jade health, одинаковые голоса/вид, no clone loot/XP/kill credit.
4. Настоящие save/rejoin/chunk unload во время transform и тройного боя; не считать NBT unit test такой проверкой.
5. Natural условия eighth day/>160/Overworld, сохранение обычных форм; first encounter book ровно один раз для Survival/Adventure, не Creative.
