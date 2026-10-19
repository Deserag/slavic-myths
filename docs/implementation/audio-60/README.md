## Продолжение 19.10: коса, посох, лёд

[Матрица и ограничения](FROST_FOLEY.md). 11 записей / 9 существующих событий,
Java21 clean build и offline gates PASS. IN_PROGRESS; новый пакет в PolyMC не установлен.

# Текущая тестовая установка — 1.9.7.9, 19.10.2026

[Акт установки](POLYMC_INSTALLATION.json): файл установлен в тестовый PolyMC,
одна активная копия, backup проверен,240 остальных файлов не изменены.
SHA256 4fa880a3d7becbb819071c9dec838b0fa2d31f4ae19468f16f142143eb99a3cd.
Java21 clean build PASS1m14s; offline gates PASS; runtime0. Ниже история итераций,
ранние записи «не установлен» относятся к предыдущему состоянию.

# #60 пакет A — IN_PROGRESS, НЕ ГОТОВО

Новая итерация исправляет только 3 одинаковых взмаха SPIN Тугарина. Голоса,
топот/transform, Овинник целиком, Волк, нежить, Ским и Идолище не завершены.
Пакет B не начинался, Кузя не затронут. Владелец не принимал качество на слух.

| REQUIREMENT | ACTUAL SOURCE/ASSET | STATUS |
|---|---|---|
| Убрать одинаковый sweep в трёх импульсах Тугарина | TugarinCombat tick16/28/40 → tugarin_spin_blade_1/2/3 → sounds/boss/tugarin/foley/spin_1/2/3.ogg | PASS source/JAR; owner NOT_RUN |
| Отдельные законные записи, не pitch copies | qubodup: swosh-01/02/03.flac, записывал взмахи бамбуком у микрофона; исходные FLAC + hashes/CC0 в art/audio/pack-a-60/sources.json | PASS provenance; listening NOT_RUN |
| RU/EN, громкость и антиспам | один subtitle, mono44100, peak/rms в outputs.json; только один server playSound на прежний hit, volume.8/pitch1/range24 | PASS source; runtime NOT_RUN |
| Нет изменений тайминга/урона/AI/NBT | сравнение всего TugarinCombat с956f4645 допускает ровно одну замену вызова звука; остальные sound mappings прежние | PASS offline |
| Генераторы сохраняют актуальные записи | audio_60_foley.py воспроизводит три OGG; полный boss_reimplementation_resources вызывает restore_events | PASS scoped reproducibility |
| Овинник: огонь вместо чужого roar | два реальных fire/ignition preview, пока не подключены; живой выдох отсутствует | IN_PROGRESS / voice BLOCKED |
| Ткань/другие категории пакета A | 3 cloth preview; остальных обязательных окончательных записей нет | FAIL полного пакета |
| OLD/NEW listening pack | 8 NEW previews + audition.ogg; OLD — действующий vanilla alias player.attack.sweep pitch.55, отдельного старого файла в моде нет | IN_PROGRESS; полный A/B NOT_RUN |
| Java21 clean build/production JAR | gates.json; quarantine остаётся | PASS technical, не приёмка |
| Установка модели для владельца перед продолжением | weapon-grip-58/POLYMC_INSTALLATION.json, source956f4645, SHA65894e8c… | PASS installed; manual NOT_RUN |
| Новые звуки в установленном профиле | текущий профиль намеренно остаётся на модели956f4645; эта audio-итерация ещё не установлена | NOT_INSTALLED |

## Требует подписей и более убедительного характера угрозы

Уточнение владельца: каждый пример должен иметь подпись «существо / роль / действие / фаза». У враждебных мобов, мини-боссов и боссов голос должен передавать уместную угрозу; представленные эффекты звучат недостаточно выразительно для этой роли. Огонь/ткань/оружие не запрещены, но это сопутствующие эффекты, не замена голосу. Неподписанный общий монтаж не использовать для приёмки. Голосовая часть НЕ РЕАЛИЗОВАНА.

- [Все 8 по порядку](../../../art/audio/pack-a-60/preview/audition.ogg)
- [Клинок16](../../../art/audio/pack-a-60/preview/tugarin_spin_1.ogg), [клинок28](../../../art/audio/pack-a-60/preview/tugarin_spin_2.ogg), [клинок40](../../../art/audio/pack-a-60/preview/tugarin_spin_3.ogg).
- [Очаг](../../../art/audio/pack-a-60/preview/ovinnik_hearth.ogg), [вспышка](../../../art/audio/pack-a-60/preview/ovinnik_ignition.ogg).
- [Ткань1](../../../art/audio/pack-a-60/preview/cloth_1.ogg), [ткань3](../../../art/audio/pack-a-60/preview/cloth_3.ogg), [ткань5](../../../art/audio/pack-a-60/preview/cloth_5.ogg).

## Авторы/лицензии и редактура

[qubodup bamboo](https://opengameart.org/content/swish-bamboo-stick-weapon-swhoshes),
[qubodup ignition](https://opengameart.org/content/flare-ignition),
[PagDev fireplace](https://opengameart.org/content/fireplace-sound-loop),
[OwlishMedia cloth](https://opengameart.org/content/202-more-sound-effects): CC0.
Ignition уже ускорен самим автором из записи спички — это fire foley, не голос.
Ткань/огонь — lossless FLAC из исходных WAV, отдельные bamboo FLAC без изменения.
Сохранены SHA скачанных архивов/файлов и выбранные члены, не весь лишний pack.
Редактура только mono, crop, gain, короткий fade; без pitch shift, формант,
генерации шума и тона. 3 production takes раздельные; montage не входит в JAR.

## Важная фактическая находка

Овинник наследует HuntMob: rage/HOWL и шаги используют прямые SoundEvents,
часть ambient/hurt идёт через LandSpiritEntity. Замены только ovinnik_* в JSON
не устранят все слышимые повторы. В этой итерации этот голос не подменён огнём.
Тугарин SPIN тоже играл PLAYER_ATTACK_SWEEP напрямую; исправлена именно эта
реальная точка, а не неиспользуемый alias. Прежний roar при старте и step при
recovery ещё не исправлены и не скрываются статусом нового файла.

Minecraft/PolyMC/GameTest/server0 запусков. Ни одна Issue не закрыта.

Финал технических проверок: Java21 clean build PASS1m14s, source/audio/resource/JAR
7731 checks PASS. SHA256 нового audio JAR c7850539726a1ef1b97a917cb9e8d2f7c43d6bfc1e4e7d4736ebbb95f08b969b.
Это другой, более поздний артефакт, чем установленная перед работой модельная сборка.


## Следующая итерация после уточнения владельца

Текущее обновление: точные rough-дыхание/удар Тугарина также одобрены владельцем,
подключены к tugarin_phase1_idle/attack. Отдельный audio-only cooldown24 тика
предотвращает наложение1.1s голоса из begin/комбо; gameplay/NBT не менялись.
Java21 clean build PASS1m35s;6975+7732 offline checks PASS.
Новый JAR SHA d7c86ea453ac76c377efe4e306091903722740d4139805984b926112f9a2fc84.
Разрешён дальнейший автономный выбор подходящих звуков; спорные — кандидаты.
Итоговая игровая оценка остаётся за владельцем, runtime0. Коммиты/PolyMC пока
не разрешены; исторические описания ниже отражают более ранние итерации.

[Восемь подписанных голосовых кандидатов](../../../art/audio/pack-a-60/voice-rework/README.md)
отделены от этого набора предметных эффектов. Каждому присвоены существо, роль,
действие, фаза, художественная цель и планируемая фактическая точка вызова.
Это не уже установленная озвучка и не положительная оценка угрозы владельцем.
Кандидаты находятся только в art/audio; assets/sounds, текущий JAR и PolyMC
этими голосами не подменялись. Лицензии/изменения в LICENSES.md рядом с ними.


## Одобрение тембра двух файлов владельцем

В чате владелец одобрил показанные tugarin_challenge и skim_roar как выбранное
направление и разрешил оставить их. Это не приёмка остальных6 и не игровая
проверка всей #60. В production добавлены именно услышанные два файла, SHA
зафиксированы в voice-rework/acceptance.json. Tugarin phase1 агрессия → голос
человека; serpent fallback сохраняет прежний alias. Skim roar → голос со
стабильным pitch1, прежний draw RNG сохранён. Server timing/урон/NBT не менялись.
Credits CC-BY упакованы в JAR. Тугарину отдельно сделан необязательный EQ160Hz+2dB,
он в production не включён. Реплики из мультфильма не копировались.
Новая голосовая сборка в PolyMC ещё не установлена.


Техническая интеграция голосов: Java21 clean build PASS1m49s, новая проверка
source/декодирование/одобренные SHA/production JAR —6962 checks PASS.
JAR SHA595df8b7e3afd347a95e7ab75ccc6853f63d0a25793aee1f91be66b3870b4460.
Ручная игровая проверка NOT_RUN, новая голосовая сборка не установлена в PolyMC.

## Approved rough Tugarin voice — 2026-10-18

Owner approved the lower, rougher challenge preview. The exact heard OGG
(e66735ee332a3c4d51c865d00123f56614a86d6fc0f1ae9393fbf60ee35ee596)
now replaces the earlier high challenge in production. approved_files and frozen
SHA select it explicitly; regeneration reproduces the rough processing before
restoring approved assets. Skim remains unchanged. Other six voices remain
candidates; full issue/runtime acceptance is still false. PolyMC not updated.
Validation: Java21 clean build PASS (1m17s), final resource rebuild PASS (1m18s); voice source/audio/JAR parity 6962 checks PASS; production JAR SHA256 f40cc9c7f2de752b27afec63ff94001bcaa191367454442e572f9068729702d5. Runtime launches: 0.

Final validation: Java21 clean build PASS1m18s; voice gates6988 PASS, existing audio gates7733 PASS. JAR SHA256 6a1432c6ca7da5166a9142bec111299143f7860837df3866e8f8eefbbf1bdafa. Runtime0; NO_COMMIT; PolyMC unchanged.

Batch validation: Java21 clean build PASS1m49s; scoped batch248 checks PASS, voice/resource/JAR7019 PASS, existing audio7733 PASS. JAR SHA256 1dd5273b80cc7a9ee6956a4bcf225f4d7923010915afd5ab2d046355ff13f121. 17 new recorded files /18 changed events; no runtime/PolyMC installation/commit.
