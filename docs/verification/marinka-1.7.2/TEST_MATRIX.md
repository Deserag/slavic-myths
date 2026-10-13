# Приёмка 1.7.2 — 59 требований

Среда: MC1.21.1, NeoForge21.1.255, JDK21.0.12, Windows. PASS относится к указанному автоматизированному сценарию. Это не клиентский игровой playtest. Непроверенные части не скрыты за успешной компиляцией.

| ID | Статус | Требование | Свидетельство / ограничение |
|---|---|---|---|
| V01 | NOT_RUN | Dobrynya final accepted white long hair and SHORT light stubble, explicitly not full beard. Head face visible front/side/back/top. | offline front/side/back/top: волосы/нарисованная щетина; клиентская приёмка не проведена. |
| V02 | NOT_RUN | His gray/silver metal visibly dominates torso+shoulders; Ilya and Dobrynya immediately distinguishable even at 8b. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| V03 | NOT_RUN | Dobrynya height exactly 2.4b when compared beside vanilla player 1.8b (user sometimes says "1 block" pictorially: physical actual player 1.8). Reference art is comparative mood; physically verify 1.8b player. | offline exact2.4b + runtime collision2.2; сравнение рядом с настоящим клиентским player не снято. |
| V04 | NOT_RUN | Spear measured tip-butt 3.6b ~=1.5x model height, no floating disconnected shaft, no sword model leakage or clipping hands. | offline tip–butt3.6b; контакт кисти/клиппинг во всех реальных ракурсах не принят. |
| V05 | NOT_RUN | Shield 3D thickness and true wrist attachment; cape folds and hair animate; no duplicate renderer/ghost shield. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| V06 | NOT_RUN | Dobrynya full UV has no scrambled orientation or mirrored emblem; front and back plates differ; at least 64x64 base textures, preferably 128x128. | UV/padding/atlas256 gates PASS; orientation проверена offline, не в клиенте. |
| V07 | NOT_RUN | Marinka model height 2.1b, black braid with hair ribbon and aged dark red layered dress; no generic villager witch face/hat. | offline exact2.1b; runtime collision1.96; клиент не запущен. |
| V08 | NOT_RUN | Marinka particle palette fire red, clone smoke charcoal, aurochs curse gold, shield ruby. Effects legible at 8–16b low graphics. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| V09 | NOT_RUN | Magpie form recognizably black-and-white, not raven; wings flap visibly and reset on return. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| V10 | NOT_RUN | Aurochs has broad gold horns, dark brown body, and is friendly to player even mid-charge. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| V11 | NOT_RUN | All clips contain real bone transform animation, not just particles on a static model. | 38 native clips конечны,216393 проверок; две силы hurt overlay. Визуальная приёмка всех клипов не проведена. |
| D01 | PASS | Spear long thrust hits at <=3.6b in front and misses a target behind stone wall. | thrustFrame + thrustWall: реальная серверная дистанция и каменная преграда. |
| D02 | PASS | Triple-combo hits exactly 3 timed distinct events at most; no per-tick damage loop. | comboFrames: HP600→591→581→566 в трёх отдельных окнах. |
| D03 | PASS | Rotating sweep hits enemy in arc, respects friendlies and target idempotence. | sweepOnce: единственный урон15, владелец не задет. |
| D04 | NOT_RUN | Successful physical parry reduces one strike and performs one clear counter hit, cooldown works. | parrySingle проверяет один физический strike и отсутствие защиты от magic; полный counter/cooldown playtest не выполнен. |
| D05 | PASS | Magic shield reflects eligible single projectile but not floor curse/environment AOE. | reflectOne: из двух снарядов отражён ровно один; печать открыта. |
| D06 | PASS | Spellbreaker changes Marinka barrier state and creates a full 8s vulnerable window. | spellbreakerWindow: событие13 меняет ward/restore cooldown; защитный бюджет160 ticks в серверном состоянии. |
| D07 | PASS | With <=15% HP, hero retreats once, has bottle-drinking animation, heals to capped level, never permanently idles inside solid block. | retreatFinite: одно отступление, cap106, конечный выход после окна160. |
| D08 | PASS | While cursed into aurochs, no spear/sheath render and no human skill; revert correct HP/gear/UUID. | transformIdentity: тот же UUID/HP, два изменения collision/form. |
| D09 | PASS | Dying during retreat marks ally unavailable for encounter, does not free summon slot for another hero. | dyingHeroKeepsStoryLock: смерть не разрешает новый призыв. |
| M01 | NOT_RUN | Boss starts at 600 actual HP, not 800. Phase1->2 at 390, phase3 at 180. | coreStats600/230 и rageCastClock19ticks PASS; полный фазовый бой не проведён. |
| M02 | PASS | Barrier reduces test hit 30%, never active through disappear or magpie; broken after deflect counter for 8s. | veilThirty + veilUptime + spellbreakerWindow: коэффициент0.7, максимум180 ticks, break. |
| M03 | PASS | Exactly 3 bodies after shadow cast: 1 real plus 2 decoys, not four. | clonesExactlyThree: один оригинал и две копии. |
| M04 | NOT_RUN | Before players damage the forms, 3 bodies have same sound/particles/face/clothes/nameplate/animation distributions; screen capture cannot identify true actor. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| M05 | PASS | Decoy receives cumulative 24HP then smoke-dies without boss progress/drops. | falseHealth: накопление24 effectiveHP, falseNoVictory. |
| M06 | NOT_RUN | Damage to real boss reduces original 600HP bar; real persists without special pre-hit outline. | реальные удары уменьшают600HP в combo/thrust; клиентский bossbar не просмотрен. |
| M07 | PASS | Clone simultaneous projectiles obey group-wide damage cap; not triple one-shot from three 6HP attacks. | burstGroup + cloneEffective: групповое окно40 ticks, clone=2.953125 после diamond. |
| M08 | NOT_RUN | Boss can't spam clones; cooldown >=35s after end. No endless clone loop on reload. | конечные cooldown/TTL сохранены кодом; полный35s повторный clone cycle не измерен. |
| M09 | NOT_RUN | Witch blink 8–12b within arena and cannot teleport into wall, closed basement, unloaded chunk, outside fight leash. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| M10 | NOT_RUN | Bird flight is visible and can be interrupted; returns to valid standing cell. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| M11 | NOT_RUN | Footprint hex is visible and avoidable with sensible movement; proper Weakness+Slowness durations. | traceDiamond=3.4944HP; реальное уклонение движущегося клиента не проверено. |
| M12 | PASS | Heavy fire center hit (no defense buffs, full diamond, 24HP) subtracts >=6 actual HP. | fireDiamond: raw17.5→6.5625 HP при24HP/diamond20/toughness8. |
| M13 | PASS | Minor side hit/tile tick subtracts significantly LESS than 6HP, resists unintended multiple same-tick hits. | sideDiamond: raw10→3HP; серверная конечная ember-политика1/20 ticks. |
| M14 | PASS | With valid ward/cover/Protection bonus, damage may reduce rather than forcing unmitigated min. | resistanceDiamond: Resistance I снижает6.5625 до5.25 без искусственного damage floor. |
| M15 | NOT_RUN | Lure does not unavoidably pull into immediate heavy fire; 1.25s safe reaction gap. | lureDiamond≈1.999824HP; pull/gap реализованы, связка с реальным клиентом не проверена. |
| M16 | NOT_RUN | In rage, after 2 casts, real 1.25s recover window; player can land a hit. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| M17 | NOT_RUN | HP0 cleanup cancels hazards/clones/minions and boss bar, victory occurs exactly once. | actual lethal hit/victory/hatch PASS; полный cleanup каждого hazard во всех боевых состояниях не прогнан. |
| S01 | PASS | Yard generated via existing trigger or explicit safe test method once; 60x60, clear 40–45 diameter center and many detailed perimeter features. | builtYardAndHatch: настоящее порционное строительство60×60 через безопасный admin staging. |
| S02 | NOT_RUN | House has grounded beams/broken roof with interior prop logic and support under uneven terrain. | основание на одном неровном участке проверено; крыша/интерьер не осмотрены в клиенте. |
| S03 | NOT_RUN | Well cannot induce unavoidable lethal fall, travel around barn/trees/cart remains navigable. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| S04 | PASS | Story entrance valid Dobrynya and player quest -> intro and boss; incompatible hero -> localized denial; no rogue summoning. | builtYardAndHatch: настоящая WitchYard.start с принадлежащим игроку Добрыней. |
| S05 | PASS | Normal world allows 2 hero slots as before; Marinka personal fight permits Dobrynya only; transformed/retreating hero slot stays locked. | partyLock: владелец/участник заблокированы; посторонний свободен; curse не освобождает слот. |
| S06 | PASS | If Ilya already present he is safely suspended and later restored without being killed or permanently duplicated. | builtYardAndHatch: Илья100HP/UUID сохранён, вынесен за двор, standby снят после победы. |
| S07 | PASS | Break 80HP/3-hit gold anchor -> reverse aurochs state + Marinka 8s window. Anchor never behind wall. | anchorThreeStrikes + anchorRejectsMob: три player strikes, revert + broken ward. |
| S08 | PASS | Losing battle does NOT open secret hatch, nor store boss victory. | failureSealed + falseNoVictory: без победы подвал не открыт. |
| S09 | PASS | Winning once causes seal break -> hatch opens 2.8s after defeat, safe 2b stair. | builtYardAndHatch: настоящая смертельная player attack, закрыт через30 и открыт через60 ticks, проход2×3. |
| S10 | NOT_RUN | Basement contains craft workshop, transformation hall and north hideout; all navigable and visibly furnished. | реальные комнаты построены, clearance/сундуки проверены; полный маршрут игрока не пройден. |
| S11 | PASS | Ordinary chest loot does not respawn when server reloaded or player revisits. | builtYardAndHatch + restart03: забранный emerald не восстановлен; свечи2 сохранены на диске. |
| S12 | PASS | Boss defeat recorded per player; after patch v1.7.6 retroactive unique reward eligibility possible without replay. No unique reward already dispensed. | victoryOnce + actual lethal hit + restart03: DefeatCount1 и future eligibility, только vanilla loot. |
| S13 | PASS | Server restarts mid-hatch: after recovery hatch in a coherent open state and no duplicate buried room. | два процесса restart03: pending WON→OPEN после повторного открытия дискового мира. |
| S14 | NOT_RUN | Player logout mid-clones/curse cannot leave frozen 3 Marinkas or permanent spell lock. | restart03 снимает interrupted COMBAT lock; настоящее отключение клиента во время curse/clones не проведено. |
| S15 | NOT_RUN | Two players cannot both create fight at same yard; protected area and world-chunk checks work. | два synthetic ServerPlayer не запускают один двор; жидкость/diamond block отвергнуты. Проверка с внешним protection plugin не выполнена. |
| A01 | PASS | Every declared sound event points to a real playable OGG in mod assets and no missing registry path. | 39 OGG: registry, sound JSON, origin SHA256, ffprobe и полный ffmpeg decode. |
| A02 | PASS | No copyright movie voice/media present, attributions/provenance manifest includes all audio. | 39 байтовых копий собственного синтезированного аудио ZIP; audio-origin.json и исходная лицензия сохранены. |
| A03 | PASS | All hero/boss skill subtitles have Russian and English entries; UI not revealing true clone. | RU/EN всех sound subtitles,19 реплик и4 записок; оригинал/клоны имеют общий тип и renderer. |
| A04 | NOT_RUN | GUI at scales 2,3,4 without overlap; low particles still preserves telegraphs. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| A05 | NOT_RUN | 10-minute battle and multiple boss restarts not causing unbounded entity/particle count. | Требуется ручной клиентский/длительный игровой тест; не выдаётся за пройденный. |
| A06 | PASS | Dedicated server loads (no client-only class in server init), current project build passes, actual JAR generated. | 36 новых +48 regression dedicated cases; clean build и production packaging. |
| A07 | PASS | Localized title/version remains 1.7.2; no project-wide migrations or 1.7.1 regression. | версия1.7.2, old resource keys сохранены;6823 записей1.7.1 побайтно,48 regression cases. |

PASS: 30; NOT_RUN: 29; текущих FAIL: 0. Первоначальные провалы и исправления сохранены в журналах runtime01–04; итоговый runtime10 обязателен.
