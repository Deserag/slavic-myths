"""Final evidence report; retain prior status/history and parallel installation reports."""
from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/mor-1.8.1'
g=json.loads((O/'resource-gates.json').read_text());i=json.loads((O/'polymc-installation.json').read_text(encoding='utf-8-sig'))
assert g['status']==i['status']=='PASS' and g['sha256']==i['sha256']
assert hashlib.sha256((R/'build/libs/slavicmyths-1.8.1.jar').read_bytes()).hexdigest()==g['sha256']
(O/'ART_AUDIO_SOURCES.md').write_text('''# Арт и звук моровых форм 1.8.1

Прямые пользовательские эталоны сохранены в [пакете задания](../../tasks/1.8.1/references/README.md). SHA256 всех 39 файлов manifest проверены. Большие референсные изображения не входят в игровой JAR. «Мор» — авторская игровая интерпретация, не историческое название отдельного духа.

| Форма | Native геометрия и материал | Реальная текстура |
|---|---|---|
| Леший | Исходные joints/длинные руки, дополнительные ветви, кора плеч/груди, когти, корни ступней и свисающие полосы оливкового мха | `mor/leshy.png`, 64×64 |
| Полудница | Исходная высокая тонкая фигура, соломенные пряди с открытым лицом, колосья венка, рваные полотнища, почерневшие предплечья/пальцы; серп закреплён на кисти | `mor/poludnitsa.png`, 256×256 |
| Кикимора | Исходный небольшой сутулый rig, горб, пучки длинных тёмных волос, когти, рваные полосы одежды, красные нити крестом спереди/сзади | `mor/kikimora.png`, 256×256 |
| Огненный змей | Исходное число сегментов/длина и wingless rig; четыре угловатых рога, зубы на подвижной челюсти, красный гребень, светлое подбрюшье, три хвостовые ленты; оригинал/копии используют один renderer | `mor/fire_serpent.png`, 512×512 |

До изменения серпа его форма/размеры/материалы определены в ТЗ `mobs/02_poludnitsa/01_VISUAL.md`: короткое жатвенное орудие с загнутой кромкой, тёмной рукоятью, серебристым ребром и багровыми шипами. Используется прежняя геометрия короткого серпа, дополнительный конец рукояти/обратные шипы; это не длинная коса. Нового предмета оружия нет.

Все материалы нарисованы собственным детерминированным `tools/mor_assets.py` по UV реальных native cubes. Для каждого есть отдельная `_forming` стадия; 0–15ticks использует обычную геометрию/текстуру с transform pose, 16–34 — моровую геометрию с тёмными пятнами, 35–50 — финальную текстуру. Обычные atlases сохранены. Склянка — прозрачный 16×16 пиксельный splash sprite с тёмной жидкостью/пробкой/алой риской. Отдельные реальные sprites корня/снаряда и 4px квадратных частиц; частицы fade12ticks. Root — native transient cuboids, не поставленные блоки.

## Звуковые источники

Новых OGG/пустых событий нет. Использованы напрямую установленные vanilla SoundEvents Minecraft 1.21.1. Файлы других игр/модов не копировались; прежний `sounds.json` побайтно сохранён. Это ссылки на ресурсы Minecraft, не перераспространение аудиофайлов.

| Форма/приём | Actual vanilla event | Момент |
|---|---|---|
| Леший | `block.wood.break`, `block.rooted_dirt.break`, `block.wood.step`, `entity.ravager.roar` | ambient12s; transform0/50; roots0/24; wail12 |
| Полудница | `block.grass.step`, `block.grass.break`, `item.axe.scrape`, `block.beacon.power_select`, `entity.breeze.wind_burst`, `entity.player.attack.sweep`, phantom hurt/death | ambient12s; transform0/50; flash0/20; harvest warning0/dash14/swings19/25 |
| Кикимора | silverfish ambient/hurt/death, `item.crossbow.loading_middle`, `entity.witch.celebrate`, `block.wool.step`, `block.tripwire.attach` | ambient12s; transform0/50; dance0/10/16/22; threads0/16 |
| Змей/копии | blaze ambient/hurt/death/shoot, fire ambient/extinguish, phantom flap, `item.elytra.flying`, `entity.illusioner.mirror_move` | ambient12s; transform0/50; flame0/22/27/32; dive0/18; split0/30 |
| Склянка | `entity.splash_potion.throw`, `entity.splash_potion.break` | use/one impact |

В `MorAudio` volume≤0.95, pitch .75–.85; общий hit cue ограничен один раз за приём. Реальные namespaces подтверждены cached vanilla sound catalogue/установленным Java API. Верхнеуровневые оригинал и клоны используют одинаковые звуки/pitch, атласы, renderer и частоты частиц. Idle не проигрывается одновременно старым ambient timer и новым контроллером.

## Проверка

Реальные ModelPart cubes/UV и конечные vertex coordinates проверены в 69 offline profiles, включая ordinary geometry из production 1.7.9, transform, windup/release/recovery. [Леший](leshy-offline-views.png), [Полудница](poludnitsa-offline-views.png), [Кикимора](kikimora-offline-views.png), [змей](fire_serpent-offline-views.png) — собственный offline rasterizer реальных UV meshes, **не скриншоты Minecraft**. Внешний вид в игровом освещении, анимации и прослушивание: **REQUIRES_MANUAL_QA**.
''',encoding='utf-8')
report=f'''# Мор 1.8.1 — итог реализации и проверок, 2026-10-12

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
| Offline production policy/schema/native geometry | PASS **{g['offline_assertions']} assertions**, **{g['native_profiles']} profiles**; большинство assertions проверяют UV/координаты повторных поз, это НЕ число игровых тестов |
| HP fraction/real AttributeInstance save/load | PASS; stable modifier ID/base preservation; НЕ игра/реальный relog |
| Three-state50t, protected/idempotent policy, summon-once/clone identity, interrupted cast/UUIDs/cooldowns | PASS production functions/MorSnapshot NBT; НЕ disk-world restart |
| Natural day/dimension/reason/distance/RNG boundary matrix | PASS; cap hook/source audited; реальная частота появления NOT_RUN |
| Combat release schedule | PASS exact tick pulses/cooldowns source; damage, LOS, collision, эффект на настоящем игроке/полный бой NOT_RUN |
| Four NORMAL native meshes/UV vs1.7.9 | PASS, actual old model classes baked through isolated classloader |
| Existing UI helpers | PASS **{g['ui_checks']}** |
| Production/resource/source gates | PASS **{g['checks']}**; all JSON, models/texture links, existing OGG/vanilla event links, RU/EN old keys retained, generator reproducibility, source/JAR identity, no test fixtures |
| Subscriber bytecode | PASS **{g['event_subscribers']}**, no empty subscribers |
| Prior JAR entries | **{g['old_entries_identical']}** byte-identical; changed old entries explicitly allowlisted in [resource-gates.json](resource-gates.json) |
| Client visual/audio, real multiplayer/rejoin/world restart | **REQUIRES_MANUAL_QA / NOT_RUN** |
| FPS/MSPT | **NOT_MEASURED**; absence of global scan/chunk tickets verified statically, no performance claim |

За эту реализацию: **0 Minecraft client starts, 0 dedicated/GameTest server starts**. Java offline main был запущен **9** раз; три clean build команды; четыре промежуточных compilation failures исправлены. Последний `clean-build-final.log` заменяет предыдущий повтор этого же файла; итоговые build/native/UI результаты PASS. Игра, dev client, PolyMC GUI и миры агентом не запускались.

## Production и установка

JAR: [build/libs/slavicmyths-1.8.1.jar](../../../build/libs/slavicmyths-1.8.1.jar), **{g['size']} bytes**.

SHA256: `{g['sha256']}`.

PolyMC **PASS**: финальный JAR установлен в существующий `Slavic-Myths-1.21.1-Testing` после закрытия PolyMC/Minecraft пользователем и проверки процессов. До финала там уже была промежуточная1.8.1 от параллельной установки с другим SHA; она сохранена в `{i['backup']}`. Прежняя backup1.7.9/её отчёт также сохранены.

Установленный SHA совпадает с gates. **{i['other_files_preserved']}** остальных файлов профиля сохранены побайтно, configs/миры/сторонние JAR/`instance.cfg`/`mmc-pack.json` не изменены; дубликатов нет. Старый1.16.5 не затронут. [Квитанция](POLYMC_INSTALLATION.md), [JSON](polymc-installation.json).

## Краткая ручная приёмка

1. В отдельном TEST world получить `/give @s slavicmyths:mor_flask 1`; четыре старых яйца, splash одной/нескольких целей,50t transform, повтор и protected NPC.
2. Сравнить normal/mor front/side/back и все attack warnings/hits/misses/cover; звук, cooldown, UI/held sprite.
3. Змей≤50%: две opaque копии30HP, скрытые bossbar/Jade health, одинаковые голоса/вид, no clone loot/XP/kill credit.
4. Настоящие save/rejoin/chunk unload во время transform и тройного боя; не считать NBT unit test такой проверкой.
5. Natural условия eighth day/>160/Overworld, сохранение обычных форм; first encounter book ровно один раз для Survival/Adventure, не Creative.
'''
(O/'REPORT.md').write_text(report,encoding='utf-8')
def prepend(name,section):
 p=R/name;old=p.read_text(encoding='utf-8')
 marker=section.splitlines()[0]
 if old.startswith(marker):return
 p.write_text(section+'\n\n'+old,encoding='utf-8')
status=f'''## Мор — финальная реализация 1.8.1, 2026-10-12

Четыре старых entity IDs получили сохраняемые/synced моровые формы:50tick transition, fraction-preserving HP modifiers,9 спецприёмов включая одноразовых двух двойников змея30HP, rare natural4% day8/>160/Overworld, creative Склянка мора и одна existing Book discovery. Четыре native mor rigs/atlases/action poses, реальные vanilla sounds; ordinary geometry/assets/IDs сохранены. Bossbar/Jade HP скрываются на время двойников, configs не меняются. Будущие1.8.0 макеты/системы не переносились.

Final clean build PASS;{g['offline_assertions']} offline assertions/{g['native_profiles']} native profiles,71 UI helpers,{g['checks']} production/resource checks и174 subscribers PASS. Это не игровые тесты: Codex client/server starts0/0; visual/audio/combat/MP/rejoin/MSPT REQUIRES_MANUAL_QA/NOT_RUN. Финальный SHA `{g['sha256']}` установлен в PolyMC Testing с backup промежуточной1.8.1;{i['other_files_preserved']} прочих файлов сохранены. Миры/1.16.5 не менялись; commit нет. [Отчёт](verification/mor-1.8.1/REPORT.md), [установка](verification/mor-1.8.1/POLYMC_INSTALLATION.md). Старые записи ниже описывают свои, в том числе промежуточные, артефакты.'''
prepend('docs/PROJECT_STATUS.md',status)
prepend('docs/ROADMAP.md',f'''## Мор 1.8.1 — реализация завершена, ручная приёмка ожидается

Пакет четырёх моровых существ/Flask/Book внедрён и финальная сборка установлена в существующий PolyMC Testing. Build, offline policy/NBT/native geometry, UI и production gates PASS; реальные client/server starts Codex0/0. Следующий шаг — пользовательская приёмка transform/боёв/двойников/сохранения/natural spawn/рендера/звука; FPS/MSPT не измерены. Ранее согласованные макеты1.8.0 и будущие1.8.2+ не реализованы этой задачей. [Финальная доказательная матрица](verification/mor-1.8.1/REPORT.md).''')
prepend('docs/ARCHITECTURE.md','''## Мор 1.8.1 — entity-local controller и отдельный client visual path

`LandSpiritEntity` владеет optional synced mor state/remaining/skill-clock/clone/concealment и одним `MorController`; NORMAL пропускает mor schedule. `MorGoal` блокирует прежние MOVE/LOOK/JUMP только в моровых состояниях. Четыре `MorLeshy/Poludnitsa/Kikimora/Serpent` разделяют реальные move sets; общие focus/cooldown/save/navigation guards не копируются. `MorRules` — production policy/timings; `MorSnapshot` — namespaced NBT schema без повторной сериализации vanilla identity/equipment. MAX_HEALTH/damage/speed permanent modifiers имеют stable ResourceLocation IDs; movement freeze transient. Копии используют старый fire_serpent type, persistent parent/created count/age и отдельный death path без kill credit; bossbar concealment не меняет здоровье.

`MorEvents` делает 4% natural рулетку после day8/>160 и один loaded cap-query; реальные NeoForge FinalizeSpawnEvent/pre-init и LandSpirit.finalizeSpawn/post-super разделены pending флагом. Combat выбор5t, book40t, parent20t, loaded collision guards; нет global tick registry loop/chunk tickets/world block changes. Flask и bolt/root — технические projectile shells; один new visible Item/no recipes. TaleService/TaleData обеспечивают существующую once-only discovery.

Client-only `MorVisual`/отдельные model instances переключают ordinary/forming/final stages и вызывают native pose methods. `MorBoltRenderer` рисует настоящие cuboid roots; MorParticle имеет finite12t fade. MorAudio использует реальные vanilla SoundEvents. Optional `MorJadeCompat` связывает установленный Jade15 callback reflection только на клиенте, удаляет health-tag для concealed serpents, не вводит dependency/не пишет configs. Common не загружает renderer/UI/Jade.

`tools/mor_assets.py` воспроизводит актуальные отдельные UV atlases/sprites/RU+EN/tale без отката ordinary resources. `mor-offline.gradle`/`mor-tests`/work meshes не входят в production. Gates и runtime limits: [REPORT](verification/mor-1.8.1/REPORT.md); игровые visual/audio/MP/rejoin/MSPT NOT_RUN.''')
prepend('CHANGELOG.md','''## 1.8.1 — 2026-10-12

- Added persistent blighted variants of the existing Leshy, Poludnitsa, Kikimora and Fire Serpent, distinct native models/textures and nine telegraphed abilities including two once-only30HP serpent copies.
- Added creative-only Flask of Blight, rare gated natural variants and one existing lore-book discovery; RU/EN and actual vanilla sound cues.
- Preserved ordinary registry IDs/assets and dependencies. Final clean build/offline/resource gates PASS; installed final JAR in existing PolyMC Testing with backup/hash preservation, no game launch. Manual gameplay/visual/audio/rejoin/MSPT pending.''')
print('Mor report, art/audio sources and four status documents updated; previous history retained')
