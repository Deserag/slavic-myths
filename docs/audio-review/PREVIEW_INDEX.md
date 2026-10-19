# Сравнения звуков #62 — не приёмка всей задачи

OLD: первая вариация соответствующего vanilla event, локально из Minecraft 1.21.1. Это воспроизведение исходника с указанным pitch, а не запись из игры. Для большинства строк динамический pitch Java ещё не доказан; см. таблицу. Никаких запусков. NEW — только названный конкретный кандидат.

Vanilla-примеры лежат только в игнорируемом .tools/audio-review-62-local; их нельзя включать в распространяемый пакет. Индекс зависит от локального кэша владельца; на другой машине запустить генератор с её легально установленными assets.

## Тугарин · Босс, фаза 1 · Боевой вызов

Event: `tugarin_phase1_aggro`. Текущая definition: `[{"name": "voice/tugarin/challenge", "attenuation_distance": 32}]`.

OLD reference: `entity.ravager.roar`, illustrative pitch 0.7; случайная вариация/громкость Java не эмулируются.

![Тугарин — OLD Боевой вызов](D:/slavic-myths/.tools/audio-review-62-local/tugarin_phase1_aggro.ogg)

NEW: **APPROVED_EXACT_ROUGH**.

![Тугарин — NEW Боевой вызов](D:/slavic-myths/art/audio/pack-a-60/voice-rework/alternatives/tugarin_challenge_rough.ogg)

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Ским-зверь · Враждебный босс, фаза 2+ · Атакующий рёв

Event: `skim.roar`. Текущая definition: `[{"name": "voice/skim/roar", "attenuation_distance": 48}]`.

OLD reference: `entity.ravager.roar`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Ским-зверь — OLD Атакующий рёв](D:/slavic-myths/.tools/audio-review-62-local/skim_roar.ogg)

NEW: **APPROVED_TIMBRE**.

![Ским-зверь — NEW Атакующий рёв](D:/slavic-myths/art/audio/pack-a-60/voice-rework/preview/skim_roar.ogg)

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Серый Волк · Мифический хищник, добровольное испытание · Предупреждающее рычание

Event: `grey_wolf.growl`. Текущая definition: `[{"name": "minecraft:entity.wolf.growl", "type": "event", "pitch": 0.85}]`.

OLD reference: `entity.wolf.growl`, illustrative pitch 0.85; случайная вариация/громкость Java не эмулируются.

![Серый Волк — OLD Предупреждающее рычание](D:/slavic-myths/.tools/audio-review-62-local/grey_wolf_growl.ogg)

NEW: **CANDIDATE_DOMESTIC_DOG_NOT_WILD_WOLF**.

![Серый Волк — NEW Предупреждающее рычание](D:/slavic-myths/art/audio/pack-a-60/voice-rework/preview/wolf_warning.ogg)

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Овинник · Враждебный дух овина · Яростный крик

Event: `ovinnik_angry`. Текущая definition: `[{"name": "minecraft:entity.ravager.roar", "type": "event", "pitch": 0.7}]`.

OLD reference: `entity.ravager.roar`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Овинник — OLD Яростный крик](D:/slavic-myths/.tools/audio-review-62-local/ovinnik_angry.ogg)

NEW: **NO_VOICE_SOURCE_YET_FIRE_IS_SEPARATE_FOLEY**.

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Идолище Поганое · Босс · Ярость

Event: `idolishche_poganoe.rage_01`. Текущая definition: `[{"name": "minecraft:entity.ravager.roar", "type": "event", "volume": 0.65, "attenuation_distance": 16}]`.

OLD reference: `entity.ravager.roar`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Идолище Поганое — OLD Ярость](D:/slavic-myths/.tools/audio-review-62-local/idolishche_poganoe_rage_01.ogg)

NEW: **NO_CANDIDATE**.

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Упырь · Курганный противник · Удар

Event: `upyr_attack`. Текущая definition: `[{"name": "minecraft:entity.player.attack.sweep", "type": "event", "pitch": 0.72, "volume": 0.85}]`.

OLD reference: `entity.player.attack.sweep`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Упырь — OLD Удар](D:/slavic-myths/.tools/audio-review-62-local/upyr_attack.ogg)

NEW: **NO_CANDIDATE**.

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Навь · Курганный противник · Атака

Event: `nav_attack`. Текущая definition: `[{"name": "minecraft:entity.player.attack.sweep", "type": "event", "pitch": 0.85, "volume": 0.85}]`.

OLD reference: `entity.player.attack.sweep`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Навь — OLD Атака](D:/slavic-myths/.tools/audio-review-62-local/nav_attack.ogg)

NEW: **NO_CANDIDATE**.

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Курганный дружинник · Вооружённая нежить · Удар мечом

Event: `kurgan_druzhinnik_sword_attack`. Текущая definition: `[{"name": "minecraft:entity.player.attack.sweep", "type": "event", "pitch": 0.72, "volume": 0.85}]`.

OLD reference: `entity.player.attack.sweep`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Курганный дружинник — OLD Удар мечом](D:/slavic-myths/.tools/audio-review-62-local/kurgan_druzhinnik_sword_attack.ogg)

NEW: **NO_CANDIDATE**.

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Курганный воевода · Элитная нежить · Смена фазы

Event: `kurgan_voevoda_phase_shift`. Текущая definition: `[{"name": "minecraft:entity.ravager.roar", "type": "event", "pitch": 0.72, "volume": 0.85}]`.

OLD reference: `entity.ravager.roar`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Курганный воевода — OLD Смена фазы](D:/slavic-myths/.tools/audio-review-62-local/kurgan_voevoda_phase_shift.ogg)

NEW: **NO_CANDIDATE**.

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Неупокоенный князь · Курганный босс · Смена фазы

Event: `unresting_prince_phase_shift`. Текущая definition: `[{"name": "minecraft:entity.ravager.roar", "type": "event", "pitch": 0.72, "volume": 0.85}]`.

OLD reference: `entity.ravager.roar`, illustrative pitch 1; случайная вариация/громкость Java не эмулируются.

![Неупокоенный князь — OLD Смена фазы](D:/slavic-myths/.tools/audio-review-62-local/unresting_prince_phase_shift.ogg)

NEW: **NO_CANDIDATE**.

Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.

## Следующий кандидат: Тугарин · босс, фаза 1 · Тяжёлое дыхание в ожидании боя

CANDIDATE_NOT_WIRED. Отдельный исходный take, обработанный в выбранном направлении; одобрение боевого вызова не распространяется на этот файл.

![Тугарин — Тяжёлое дыхание в ожидании боя](D:/slavic-myths/art/audio/pack-a-60/voice-rework/alternatives/tugarin_breath_rough.ogg)

## Следующий кандидат: Тугарин · босс, фаза 1 · Голосовое усилие при ударе

CANDIDATE_NOT_WIRED. Отдельный исходный take, обработанный в выбранном направлении; одобрение боевого вызова не распространяется на этот файл.

![Тугарин — Голосовое усилие при ударе](D:/slavic-myths/art/audio/pack-a-60/voice-rework/alternatives/tugarin_attack_voice_rough.ogg)
