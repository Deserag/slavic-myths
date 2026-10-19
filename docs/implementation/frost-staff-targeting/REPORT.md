# Посох Морозко — исправление по сообщению владельца 19.10.2026

IN_PROGRESS / OWNER_RUNTIME_PENDING. Не подтверждён игровой урон; запусков0.

Подтверждённые причины в коде:
- FrostWorld.hostile отсеивал мирных LivingEntity. Он использовался снарядом,
  потоком, выбором последней цели для сосулек и волной.
- Узкий поток сравнивал направление только с центром тела. Прямое наведение
  в голову близкой цели могло не проходить угол12.5°, хотя луч пересекал хитбокс.
- Обычный ПКМ по существу мог отдавать действие его vanilla-взаимодействию.
- HUD показывал две клавиши и числа без названий соответствующих способностей.

Исправлено:
- Отдельный FrostStaffTargeting.canHit для игрока: живая цель, кроме себя и
  spectator; для игроков сохраняется canHarmPlayer, фактический hurt сохраняет
  server PvP, неуязвимость и правила самой цели. Мирная цель теперь допустима.
- Все4 пути посоха используют эту политику. Босс Морозко и автономный двойник
  используют прежние candidate/hostile; их AI/баланс и protected state не меняются.
- Поток принимает прямое пересечение ray/hitbox, сохраняя прежний конус,10 блоков
  и line of sight. Стены/дальность не обходятся.
- EntityInteract/EntityInteractSpecific при удержании посоха вызывают его use;
  сервер остаётся авторитетным по cooldown/stream state. По установленным NeoForge
  sources проверено: interactAt отправляет пакет перед cancellable local hook.
- Три RU/EN строки HUD: «ПКМ — морозный поток» с состоянием/временем,
  «Сосульки [клавиша]» и «Морозная волна [клавиша]» с готовностью/перезарядкой.
  Зерцало сдвинуто выше; overlay генератора reward_weapon_texts обновлён.
  Art/audio generators не запускались.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Исправлен фильтр мирных целей во всех4 путях | FrostSkills/FrostShard/FrostHazard; canHit gate | PASS |
| Прямое наведение в хитбокс близкой цели | Native Vec3/AABB: воспроизведение старого отказа и новый hit | PASS |
| Самоатака/spectator/мертвая цель и player team policy | FrostStaffOffline: реальные predicate probes | PASS |
| ПКМ по существу вызывает use; HUD понятен и RU/EN | FrostEvents/FrostClient; lang overlay и JSON/JAR parity | PASS |
| Java21 clean build и compiled production packaging | ../commands-74/FINAL_BUILD.txt; jar-check.json | PASS |
| Нативные scoped checks | FrostStaffOffline13 cases; plain JVM без мира | PASS |
| Снижение HP в игре, hold/release/LOS/PvP и layout HUD | RUNTIME_NOT_RUN;0 игровых запусков | NOT_RUN |
| Установка нового исправления в PolyMC | Не запрошена отдельно; установленная ранее19 сохранена | NOT_RUN |

Проверки entity probes вызывают настоящий фильтр с не сконструированными cow/
zombie/player type probes; они не являются игровыми сущностями в мире.
Не утверждается, что13 assertions доказывают hurt, network или runtime.
Фактический damage не обходит ванильную неуязвимость/специальные правила NPC.
Числа урона/длительности/cooldown сохранены: bolt7, stream1/10ticks, wave4.

Ручной проход владельцу: ЛКМ и удержание ПКМ по мирному мобу и зомби на близкой/
средней дистанции; сосульки по последней поражённой цели, волна по наземной цели;
стена/дальность/PvP, отпускание ПКМ, готовность трёх подписанных строк HUD.
В PolyMC по-прежнему прежняя установленная1.9.7.19, не этот исправленный JAR.
Новый JAR лежит в build/libs; точный SHA — ../commands-74/jar-check.json.
