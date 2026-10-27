# АУДИТ НЕПРИНЯТЫХ ЗАДАЧ АДА — 27.10.2026

Этот аудит **не** превращает все OPEN Issues в список невыполненных. Открытая Issue, поле Project Status и реальные исходные файлы — **три независимых вещи**. Реальная новая сборка Codex, находящаяся в незапушенной рабочей копии, **не проверена и не видна через GitHub**. Codex не имеет права уничтожать такие файлы только потому, что их нет в `main`.

## 1. Репозиторная проверка — что подтверждено

- GitHub на момент проверки имеет **две различающиеся ветки `main` и `hell`**, у `src/main/java/org/slavicmyths/SlavicMyths.java` обнаружены разные blob SHA (`main 15d1cd93e4d5c290a8c10206335a935db67a8206`; `hell 6db04b2af76431abc23d160d0deb24a8d13e68be`). Не предполагать одинаковое состояние или молча выбирать ветку.
- В `main` **точно имеется** `src/main/java/org/slavicmyths/blackcinder/ZharenitsaSeals.java` с частичным обновлением железной/алмазной брони и MAX_HEALTH modifier; **это не весь #266**. Незавершённые модели/ассеты/поведение мобов/босса не считать готовыми по наличию печати. Поиск в `main` по `Zharenitsa` обнаруживает только связанные с печатью файлы, но не доказывает отсутствия работы в `hell`/локальном checkout.
- Для #288–#294, #261–#267 и #251–#257 Issues проверены как **OPEN**. **OPEN ≠ NOT_IMPLEMENTED**; реальное состояние и владельческая приёмка должны сверяться с Project №9 и отчётами Codex/QA, не выводиться из issue title.
- Документация прежних задач содержала **смешение устаревших и текущих требований**: например, в #250 ещё были исторические редкие/необязательные биомные боссы, в #261 устаревшие слова «мобы/босс не утверждены», в ранних художественных схемах Чёрной гари — отменённая «Жаркая гарь». Новые приоритетные контракты требуют не ссылаться на эти фразы как на задания.

## 2. Перепроверенные 21 Issue

| Пакет | Основная/подзадачи | Новый приоритетный документ | Критичные проверки |
|---|---|---|---|
| **2.0.2 Навья роща** | [#251](https://github.com/Deserag/slavic-myths/issues/251), [#252](https://github.com/Deserag/slavic-myths/issues/252), [#253](https://github.com/Deserag/slavic-myths/issues/253), [#254](https://github.com/Deserag/slavic-myths/issues/254), [#255](https://github.com/Deserag/slavic-myths/issues/255), [#256](https://github.com/Deserag/slavic-myths/issues/256), [#257](https://github.com/Deserag/slavic-myths/issues/257) | [STRICT_ACCEPTANCE_2.0.2.md](navya-grove/STRICT_ACCEPTANCE_2.0.2.md) | оригинальный ранний бестиарий и тёмный Сторож, HP 80/120/150/20 и 700, все трофеи, guaranteed arena/real worldgen |
| **2.0.3 Чёрная гарь** | [#261](https://github.com/Deserag/slavic-myths/issues/261), [#262](https://github.com/Deserag/slavic-myths/issues/262), [#263](https://github.com/Deserag/slavic-myths/issues/263), [#264](https://github.com/Deserag/slavic-myths/issues/264), [#265](https://github.com/Deserag/slavic-myths/issues/265), [#266](https://github.com/Deserag/slavic-myths/issues/266), [#267](https://github.com/Deserag/slavic-myths/issues/267) | [STRICT_ACCEPTANCE_2.0.3.md](black-cinder/STRICT_ACCEPTANCE_2.0.3.md) | точный пересмотренный блоковый арт, нет новых Жаркая гарь/Частая магма, neutral Ash Spirit, Жареница 750 HP/руна/голова/печати, guaranteed arena |
| **2.0.4 Чертоги** | [#288](https://github.com/Deserag/slavic-myths/issues/288), [#289](https://github.com/Deserag/slavic-myths/issues/289), [#290](https://github.com/Deserag/slavic-myths/issues/290), [#291](https://github.com/Deserag/slavic-myths/issues/291), [#292](https://github.com/Deserag/slavic-myths/issues/292), [#293](https://github.com/Deserag/slavic-myths/issues/293), [#294](https://github.com/Deserag/slavic-myths/issues/294) | [STRICT_ACCEPTANCE_2.0.4.md](lost-halls/STRICT_ACCEPTANCE_2.0.4.md); [BLOCK_PIXEL](lost-halls/BLOCK_PIXEL_CONTRACT_2.0.4.md); [SCENES](lost-halls/SCENE_AND_NAVIGATION_CONTRACT_2.0.4.3.md); [MOBS](lost-halls/MODEL_AND_AI_LOCK_2.0.4.4.md); [WORLDGEN QA](lost-halls/WORLDGEN_GATES_2.0.4.6.md) | Камень №3, 6 залов, 5 mobs/4 variants Шептуна, HP 60/90/30/60/60, proper art 3D, boss #293 unapproved, 5 seeds×4 windows |

## 3. Блокирующие условия до передачи Codex

1. **Наличие оригинальных PNG в рабочей сессии Codex**. Для 2.0.4 и 2.0.3 оригиналы идентифицированы в беседе, SHA есть в strict docs и пользовательские ZIP подготовлены; **в GitHub binary PNG не залиты**. Утверждённые изображения первой серии 2.0.2 также **не найдены как repo assets**, их нужно извлечь из прежнего одобренного архива владельца и зафиксировать SHA. При отсутствии эталона художественная работа BLOCKED_REFERENCE, не фальсифицировать MATCH.
2. **Согласование личности босса Чертогов #293** пока не завершено. **Не выдавать #294** на полный natural+arena worldgen без отдельного выбора. Codex не может выдумать этого босса.
3. **Контроль branch+writer**. Работа #274/#275 может находиться в `hell`, подготовленные docs/Issues — в `main`; нужна ясность в рабочем состоянии, не cherry-pick вслепую.
4. **Критическая приёмка**. На каждую фичу: exact code path, 3D модель/UV, screenshot from actual Minecraft, server logs и разные версии. Если текущие правила запрещают игру/GameTest — честно `NOT_RUN_ENV_BLOCK`; независимые unit tests не заменяют последующие runtime gates.
5. **Поля Project №9**: редактирование Issue **не меняет** project Status. Изменения status через автоматизацию нужно проверять чтением; не заявлять «перевёл в работу» без read-back.
6. **Исторические формулировки**: дописки `STRICT_...` и строгие docs имеют приоритет над старыми разделами, где концепт ещё не был одобрен. Историю не следует использовать как актуальное техническое решение.

## 4. Условия качества для последующих задач Ада 2.0.5–2.0.7

Отсутствуют подробно согласованные интерьерные/мобные/боссные части, поэтому **не создавать implementation Issues со случайными персонажами**. Как только пользователь утвердит визуал/мобов/босса, применять тот же `SOURCE_SHA → REPRODUCIBLE_3D/UV → GAME_SCREENSHOT → SEED_COVERAGE → OWNER_ACCEPTANCE`. Во всех новых биомах своя гарантированная арена в каждом полном регионе и размещаемая голова уникального босса.

**Никакая из обновлённых Issue этим аудитом не была закрыта, и никаких Minecraft/Gradle/test процессов в ходе аудита не запускалось.**
