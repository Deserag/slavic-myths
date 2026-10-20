# Slavic Myths 2.0 — индекс ИНДИВИДУАЛЬНЫХ ТЗ и чертежей (20.10.2026)

**Самый высокий приоритет в каждом пакете имеет ТЕКУЩЕЕ ТЗ В СООТВЕТСТВУЮЩЕЙ GITHUB ISSUE.** Этот документ — только навигация. Старые иллюстрации 19.10.2026 с восемью разными кружками, четырьмя одновременно активными героями или фиксированными процентами площади дерева **ОТМЕНЕНЫ**. Никогда не реализовывать их как требования.

## Что изменилось по принципу контроля

1. **Сначала сохранить работающее**: source SHA, screenshot/рендер, сопутствующие механики, ID, UV, сохранения и права.
2. **Потом доказать точный дефект** — не переписывать десятки рабочих файлов, если один неправильный.
3. **Исправлять локально** по индивидуальному ракурсу/схеме и фактическим исходникам. Не ставить DONE по эскизам и одной компиляции.
4. **После** проверить тот же кейс + соседние регрессии; owner принимает реальную игру самостоятельно.
5. **Коммуникация**: один законченный результат → одно сообщение с коммитом/скрином/тестами; 2–3 пакета → новый чат Codex с handoff. Без просьб «оценить черновик» каждые 5 минут.

## Действующие микрозадачи с настоящим отдельным ТЗ

| Задача | Результат, референс / источник истины |
|---|---|
| [#73](https://github.com/Deserag/slavic-myths/issues/73) | QA уже внесённого UTF-8; [сообщения до/после](./COMMAND_LOCALE_EYES_BLUEPRINTS.svg), нет фиктивного повторного переписывания |
| [#74](https://github.com/Deserag/slavic-myths/issues/74) | QA уже существующей единой командной системы; [дерево разрешений](./COMMAND_LOCALE_EYES_BLUEPRINTS.svg), реальный [COMMANDS](../COMMANDS.md) и [MIGRATION](../COMMAND_MIGRATION.md) |
| [#75](https://github.com/Deserag/slavic-myths/issues/75) | [Шесть состояний UI классов](./CLASS_UI_V2_BLUEPRINT.svg). Дерево без фиксированного %; компактные слоты/смена клавиш **в этом же окне** |
| [#76](https://github.com/Deserag/slavic-myths/issues/76) | [Индивидуальный 4-view blueprint проклятого оборотня](./ISSUE_76_VOLKOLAK_MODEL_SPEC.svg) + настоящие production pose рендеры |
| [#77](https://github.com/Deserag/slavic-myths/issues/77) | [Индивидуальный 4-view blueprint Ским-зверя](./ISSUE_77_SKIM_MODEL_SPEC.svg) + ROAR/CLAWS/LEAP/EXHAUSTED |
| [#78](https://github.com/Deserag/slavic-myths/issues/78) | [Цикл времени призыва и cooldown](./MECHANICS_FLOW_BLUEPRINTS.svg), сохранение реальных сюжетных исключений |
| [#79](https://github.com/Deserag/slavic-myths/issues/79) | [Глобальная модель отношений/Мора](./MECHANICS_FLOW_BLUEPRINTS.svg) на существующих классах/тегах; все зарегистрированные семейства |
| [#80](https://github.com/Deserag/slavic-myths/issues/80) | [4 диагностики отсутствующих глаз](./COMMAND_LOCALE_EYES_BLUEPRINTS.svg); сначала установить **истинный ID персонажа**, не менять другого |
| [#81](https://github.com/Deserag/slavic-myths/issues/81) | [Материалы/трофеи](./ITEM_ICON_BLUEPRINTS.svg), [Pixel Art Guide](../PIXEL_ART_GUIDE.md), исправлять только подтверждённо плохие ID |
| [#82](https://github.com/Deserag/slavic-myths/issues/82) | [Растения и реальные стадии](./ITEM_ICON_BLUEPRINTS.svg), не менять growth/loot |
| [#83](https://github.com/Deserag/slavic-myths/issues/83) | [Еда](./ITEM_ICON_BLUEPRINTS.svg), не менять food stats и пиво |
| [#84](https://github.com/Deserag/slavic-myths/issues/84) | **[Восемь одинаковых кружек, цвет меняется ТОЛЬКО сверху](./ISSUE_84_BEER_TOP_SURFACE_ONLY.svg)** + опьянение с 4-й обычной кружки. Никакой бутылки сбоку! |
| ~~[#85](https://github.com/Deserag/slavic-myths/issues/85)~~ | **Закрыта как DUPLICATE**, вся реализация в #84. Не запускать отдельный workstream |
| [#86](https://github.com/Deserag/slavic-myths/issues/86) | [Руны](./ITEM_ICON_BLUEPRINTS.svg): реальный список отсутствующих иконок и точечная унификация семейств |
| [#87](https://github.com/Deserag/slavic-myths/issues/87) | [Индивидуальный 4-view item/held blueprint](./ISSUE_87_TOOL_RENDER_VIEWS.svg), не портить рабочее оружие |
| [#88](https://github.com/Deserag/slavic-myths/issues/88) | [Индивидуальный 4-view armor blueprint](./ISSUE_88_ARMOR_SHEET.svg), Steve/Alex/UV/layers/trim |
| [#89](https://github.com/Deserag/slavic-myths/issues/89) | [Одна значимая веха вместо трёх спам-тостов](./MECHANICS_FLOW_BLUEPRINTS.svg), preserve old progression |
| [#90](https://github.com/Deserag/slavic-myths/issues/90) | **[Реальный HUD 1/2/3](./ISSUE_90_HERO_HUD_REAL_LIMITS.svg)**: `HeroCalls` допускает 2 обычно и 3 у Полоза; НЕ 4 |
| [#91](https://github.com/Deserag/slavic-myths/issues/91) | [Декор: top/side/bottom/slot/tile/rotate](./ARMOR_DECOR_HUD_BLUEPRINTS.svg); минимальная починка вместо массовых 216 PNG |

## Отдельно о типе доказательства

- SVG = **референс планируемых видов или логики**, а не готовая Minecraft модель, текстура, проверка animations или игровая приёмка.
- Скрин из настоящего Minecraft может подтвердить только запускавший игру, **с указанием того JAR, который получил эти кадры**. Offline model render называется именно offline.
- Issue считается соответствующей требованиям, когда **конкретный дефект исправлен и все ранее работающие функции не испорчены**. Никакие «зелёные offline gates» не отменяют плохую анатомию, разбитую кружку, неработающий узел или исчезнувшего третьего героя.
