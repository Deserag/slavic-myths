from pathlib import Path
import json,hashlib
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/karachun-1.9.2';O.mkdir(parents=True,exist_ok=True)
s=(R/'tools/install_morozko.ps1').read_text(encoding='utf-8').replace('morozko-1.9.1','karachun-1.9.2').replace('slavicmyths-1.9.1','slavicmyths-1.9.2').replace('morozko-clean-build.log','karachun-clean-build.log').replace("version='1.9.1'","version='1.9.2'").replace('Морозко 1.9.1','Карачуна 1.9.2').replace('org.multimc','org.multimc|org.polymc')
(R/'tools/install_karachun.ps1').write_text(s,encoding='utf-8')
gate=json.loads((O/'resource-gates.json').read_text(encoding='utf-8')) if (O/'resource-gates.json').exists() else None
receipt=json.loads((O/'polymc-installation.json').read_text(encoding='utf-8-sig')) if (O/'polymc-installation.json').exists() else None
verification=f"Финальная чистая сборка и пять offline наборов — PASS; production/resource gates {gate['checks']} — PASS. SHA256 `{gate['sha256']}`." if gate else 'Финальные build/resource gates выполняются; установка ещё не выполнена.'
installation=f"PolyMC Testing обновлён после gates; {receipt['other_files_preserved']} остальных файлов сохранены побайтно. Предыдущий JAR сохранён в `{receipt['backup']}`." if receipt else 'Установка PolyMC ожидает финальных gates.'
header=f'''## Испытание Карачуна — 1.9.2, 2026-10-13

Реализован отдельный server trial: 1200 HP по умолчанию с собственным health ceiling (по существующему паттерну Полоза), остановка на 5%, 4 физических ворона, 3 фазы, голод/мороз/смерть/погребальный взмах/локальная ночь/страх/фиксированный Последний час. Независимые шкалы угасания/страха; логическая блокировка offhand сохраняет предмет и ванильную проверку тотема. Источники эффектов учитываются отдельно. Фантомы — только клиентские модели, без server entity IDs/AI/damage.

Самостоятельные boss/raven rigs (26/7 bones, 293/55 boxes), общая объёмная коса boss/item, отдельные пиксельные иконки, 24 собственных sound events. Коса: три серверных удара по дуге, две активные способности, общий hit ledger трёх лезвий, вампиризм по фактически снятому HP и независимые passive cooldown/target chains. Персональные награды: первое Чёрное крыло, каждый раз одна руна и осколок, независимый шанс косы. Отдельные NPC ветки и SavedData без сброса 1.9.1. Готовый тотем не проверяет репутацию; повторное испытание разрешено.

{verification} {installation} Агент не запускал клиент/сервер/PolyMC/GameTest: 0 запусков. Игровые бой, MP, relog, визуал/звук/анимации — NOT_RUN; MSPT/FPS — NOT_MEASURED. Не реализованы 1.9.3, новые структуры/измерения/порталы и будущий рецепт оберега. [Отчёт](verification/karachun-1.9.2/REPORT.md).

'''
for file in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
 p=R/'docs'/file;old=p.read_text(encoding='utf-8');marker='## Испытание Карачуна — 1.9.2, 2026-10-13\n';
 if old.startswith(marker):old=old[old.index('\n## ',len(marker))+1:]
 p.write_text(header+old,encoding='utf-8')
report=f'''# Карачун 1.9.2 — фактическая реализация

Minecraft 1.21.1, NeoForge 21.1.255, Java 21.0.12, official mappings, mod ID slavicmyths, ветка main. Commit не создавался. Архив [ТЗ](../../specs/1.9.2/00_START_HERE/00_READ_ME_FIRST.md) проверен: 55 SHA256. Пользователь прямо разрешил реализовать 1.9.2 и обновить известный PolyMC Testing; архивные запреты запуска соблюдены. Указания пользователя о работе готовых предметов призыва имеют приоритет над дополнительными reputation gates.

{verification}

{installation}

| Блок | Реализация и внеигровое подтверждение | Игровая приёмка |
|---|---|---|
| Босс / 3 фазы / 5% | Karachun + NightRules; final-damage clamp и setHealth floor; yield идемпотентен | A02, A13–A15: NOT_RUN |
| Геометрия / анимации | KarachunMesh / KarachunModel, 26 bones / 293 boxes, отдельные волосы/мех/подвески/плащ/руки; native UV проверены | Front/side/back и соответствие референсу NOT_RUN |
| Вороны | NightRaven: круг/18tick предупреждение/фиксированный dive/контакт/отлёт; 4 враждебных, 2 союзных; собственные rig, texture, flap/glide/dive poses | A03–A04, A27: NOT_RUN |
| Угасание / offhand | NightStatus: 15% slow, периодический camera wobble, активный offhand и shield блокируются; предмет/тотем не изымаются; на мобах attack damage reduction, big-boss cap II | A05–A07: NOT_RUN |
| Страх / ночь / фантомы | NightNetwork и NightClient; отдельные личные 0–3 zombie/skeleton meshes, ray hit-test без fake packets, приоритет настоящего entity target; local fog, F8 reduced toggle с сохранением | A08–A10: NOT_RUN |
| Атаки | Голод IV 240t через физический снаряд; Frost cone, Wither II ближний удар, sweep; фиксированный круг Last Hour с 120t default предупреждением, без block damage | A11–A13: NOT_RUN |
| Коса / двуручное комбо | NightCombat + NightClient/Reaper/RenderHand: 5/5/7t windup, cone3.5/70°, LOS, по разу на цель, 3 distinct poses; ванильные skin hands/armor | A19–A20: NOT_RUN |
| Коса / пассивы | UUID конкретного оружия и цели, третий успешный hit→Wither I; life heal≤actual HP, 5s owner CD; undead +25% и 3HP за прямое подтверждённое убийство | A21–A22, A25: NOT_RUN |
| Коса / активы | Last Breath: 10t delay, radius4 LOS; Black Harvest: 3 lanes ±15°, no homing, block collision, общий cast target ledger | A23–A24: NOT_RUN |
| Руна / аксессуар / осколок | Единственная waning rune в текущих sockets, 20% +25 и per-target ICD; Curios necklace, 2 физических allied birds 400t / 3000t; гарантированный shard без будущего рецепта | A16–A18, A26–A27: NOT_RUN |
| NPC / сохранения | Yaga action702 и Bayun choice5 с существующими token/дистанцией/доверием; 4 obsidian+blue ice — выбранное проектное подношение. Готовый reusable тотем без reputation gate | A01, A28–A29: NOT_RUN |
| Награды / MP | Отдельный slavicmyths_night192 SavedData; подготовка всех независимых rewards→save→inventory receipt save→ack queue; reload cancels incomplete, complete/first-claim/pending сохраняются | Реальный crash/relog и 2–4 client MP NOT_RUN |
| Звук / RU | 24 оригинально синтезированных sound events, 32 OGG, spatial cue до dive; RU/EN, subtitles, bossbar phase, HUD, lore/advancement | A30: NOT_RUN |

## Проверки

`gradlew.bat -I tools/karachun-offline.gradle -I tools/morozko-offline.gradle -I tools/quest185-offline.gradle -I tools/poloz-offline.gradle -I tools/mor-blocks-offline.gradle clean build nightOfflineChecks frostOfflineChecks quest185OfflineChecks polozOfflineChecks morBlocksOfflineChecks`.

Night: 40 assertions, 10000 seeded reward comparisons, 8352 native UV vertices. Морозко 6946 / quest185 182 / Полоз 4290 / Mor blocks 120. Число UV vertices не выдаётся за игровые сценарии. NBT load не выдаётся за реальный перезаход. Resource gate проверяет JAR bytecode/resources, координаты native/JSON models, генератор, звуки и переводы, отсутствие test классов, 1.9.1 baseline и исправление закрытого буфера Полоза. Dedicated server не запускался: разделение client/common проверено статически.

Результаты: [resource-gates.json](resource-gates.json), [reproducibility.json](reproducibility.json), [установка](POLYMC_INSTALLATION.md), [offline UV preview](offline-preview.png). Offline preview — ортографическая проекция кубоидов, не игровой скриншот и не доказательство правильной позы в руках.

## Честные ограничения и ручная приёмка

Настоящих запусков агента — 0. В первую очередь проверить NPC и призыв; вид boss/raven/scythe и двуручное комбо от первого/третьего лица; блокировку щита при Угасании III вместе с тотемом бессмертия; distinction реального ворона/фантома; обход фиксированного Last Hour; награды двух игроков и полный инвентарь; повторный вход после завершения/прерывания. Полная матрица A01–A32 находится в архиве.

Модели вручную воссозданы из ступенчатых кубоидов; cinematic poster lighting/bloom не воспроизводится пиксель в пиксель. Используются native pose functions, не GeckoLib клипы. Ванильные zombie/skeleton meshes фантомов окрашены и слегка дрожат, без собственного AI. Головокружение реализовано периодической камерой, подавляется F8; постоянное vanilla Nausea намеренно не навязывается. Угасание на мобах снижает скорость движения/урон, без полного выключения AI. Лечение undead kill сейчас учитывает прямой серверный урон косой, не независимый поздний DOT. Утраченный first-clear амулет бесплатно не восстанавливается; тотем восстанавливается через те же NPC. Перезапуск отменяет незавершённое испытание без лута. MSPT/FPS не измерены.
'''
(O/'REPORT.md').write_text(report,encoding='utf-8')
(R/'CHANGELOG_1.9.2.md').write_text('# 1.9.2 — Испытание Карачуна\n\n'+header.split('\n\n',1)[1],encoding='utf-8')
files=list((R/'src/main/java/org/slavicmyths/karachun').glob('*.java'))+[R/'src/main/java/org/slavicmyths/client'/name for name in ['NightClient.java','NightRavenMesh.java','NightRavenRenderer.java','NightBladeRenderer.java','NightPhantomRenderer.java']]+list((R/'src/main/java/org/slavicmyths/client').glob('Karachun*.java'))+list((R/'tools').glob('*karachun*'))
shared=['gradle.properties','src/main/java/org/slavicmyths/SlavicMyths.java','src/main/java/org/slavicmyths/rpg/Runes.java','src/main/java/org/slavicmyths/rpg/RuneDefinition.java','src/main/java/org/slavicmyths/bogatyr/HeroThreats.java','src/main/java/org/slavicmyths/yaga/YagaMenu.java','src/main/java/org/slavicmyths/bayun/BayunNetwork.java','src/main/java/org/slavicmyths/client/YagaScreen.java','src/main/java/org/slavicmyths/client/BayunDialogueScreen.java']
files+=[R/p for p in shared];rows=[{'path':p.relative_to(R).as_posix(),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in files if p.is_file()];(O/'changed-files.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('REPORTS_UPDATED',bool(gate),bool(receipt))
