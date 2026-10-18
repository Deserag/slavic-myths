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

## Прослушать реальные кандидаты

Все ссылки ниже — настоящие OGG, не исходники скетчей и не синтезатор.

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
