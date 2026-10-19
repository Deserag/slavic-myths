# #65 / #60 — шесть курганных типов

Статус **IN_PROGRESS_OWNER_PENDING**. Полностью реализована выбранная техническая
политика событий семейства; художественная/игровая приёмка NOT_RUN.

| Персонаж | Характер / все состояния | Решение |
|---|---|---|
| Упырь | 2 idle,2 hurt,death; сухая одежда/прыжок/укус/шаги; контакт | Разные ArcadeParty zombie takes + recorded leather/body props |
| Навь | ghost ambient/hurt/death; движение погребальной ткани, shift/appearance, шаг, контакт | Сохранён distinct phantom vocal, новые physical cloth props; не выдано за новый голос |
| Дружинник | 2 idle/hurt/death костей; меч, щит/толчок, экипировка, контакт | Recorded shells изображают кости; разные knife/steel props |
| Воевода | idle,2 hurt,death; топор/разбег/щит/фаза/шаги/контакт | Отдельные vocal takes, более тяжёлые equipment cues |
| Погребённый волхв | caster ambient/hurt/death; basic/clones/seal/summon, шаги, bolt collision | Сохранён evoker vocal, recorded bone/wood/leather spell props |
| Неупокоенный князь | idle,2 hurt,death; combo/summon/phase/heavy/grab/shield/steps/contact | Отдельные vocal takes и полный equipment набор |

Все47 прежних событий явно classified:41 replaced,6 KEEP.6 новых impact events,
69 отдельных исходных takes и69 отдельных decoded clips. Принятые модели/UV,
AI, урон, attack/recovery cooldown, NBT, ID существ и предметов сохранены.

## Звук атаки

Исходный begin сигнал остаётся tell; фактический успешный `strike` получает
свой contact, с отдельным2-tick audio cooldown против наложения AoE по игрокам.
Нет нового RNG draw. Для прежнего повторного heavy cue сохранено прежнее
потребление pitch RNG, сам повтор заменён contact. У снаряда волхва contact
на `onHitEntity`/`onHitBlock`; damage6/lifetime50 unchanged. Windup5/6/8…,
combo spacing10/8/6, facing alignment и все прежние попадания сохранены.
Короткий tell с учётом pitch .9 заканчивается до минимального windup5.

## Источники

ArcadeParty (voice effects), StarNinjas (две реальные ножевые props), Vehicle
(необработанные leather/steel/wood props), congusbongus/blukotek (ракушки как кости),
qubodup (мокрое полотенце как body impact). Всё CC0; для shells проверен и upstream
Freesound. Автор wet towel отдельно перевёл материал в CC0 на первичной странице,
это supersedes старое license note внутри архива. Не выдаётся за запись исторических
мечей, реальных костей или голос самого Codex. Source URL/date/member/archive/SHA
и chain edits в art/audio/kurgan-65/sources.json / selection.json; credits входят в JAR.
Новые клипы не прослушаны владельцем. Художественный результат OWNER_PENDING.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Всё семейство: idle/hurt/death/action/phase по реальным triggers | KURGAN_EVENT_COVERAGE.csv; source KurganCreature/KurganFighter/KurganBolt | PASS (static) |
| Полная47-event policy +6 real contacts | selection.json / kept.json / verifier | PASS |
| Разные take, mono44100 Vorbis, clipping/edge QA | source hashes / decoded hashes / kurgan-65/gates.json | PASS |
| Tell/contact, антиспам, RNG, damage/timing/NBT | exact full-class audio-only comparison в verify_kurgan_audio_65.py | PASS (source) |
| Лицензии и source chain | sources.json, audio_licenses/kurgan-65.txt в JAR | PASS |
| Генераторы не возвращают старые выбранные mappings | shared restore + kurgan_086 hook; byte-idempotence | PASS |
| RU/EN и6 новых registrations | ModSounds / actual maps / lang parity | PASS |
| Production Java21 clean build / JAR | .tools/audio-project-61-build.log,38s; gates.json | PASS |
| Проигрываемые OLD/NEW и6 последовательностей22s | soundboard.html / PREVIEW.md / kurgan-review.zip | PASS (files decoded, not heard) |
| Общий акустический скрининг | PROJECT_SIMILARITY.csv + метод | PASS (screening only) |
| Карантин / Кузя / #58 visuals | предыдущие scoped gates, source comparisons | PASS |
| Слуховая и игровая оценка, mixing на дистанциях | выполнить владельцу | NOT_RUN |
| Новый пакет установлен в PolyMC | установка не запрашивалась, receipt прежнего JAR проверен | NOT_RUN / POLYMC_NOT_INSTALLED |

Production JAR SHA256 `9f2da63fc926933b54fac3e5f8ffbb7f11167397e274379cb8621ff3de5a6995`.
Это новый локальный JAR1.9.7.9, а установленная ранее1.9.7.9 имеет другой SHA4fa880a3… .

## Краткий сценарий владельцу

В отдельном своём тестовом мире: `/summon slavicmyths:upyr`, `nav`,
`kurgan_druzhinnik`, `kurgan_voevoda`, `buried_volkhv`, `unresting_prince`
(каждый с полным namespace). Ambient в Creative, затем атаки в Survival;
для упыря дистанции2м и6м, для воинов блок щитом, для волхва дистанция8–12м.
Воевода меняет фазу ниже50% здоровья; князь ниже65%/30%, combo ускоряется.
Отдельно hurt/death и block/contact/miss: промах не должен играть contact.
Слушать на2/8/20м, затем двух разных мобов рядом; resource reload,
выход/вход, смерть/удаление источника. Новых loop в пакете нет.
Codex эти действия не выполнял. Перенос в PolyMC отдельно по запросу/гейтам.
