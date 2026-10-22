# Пакет 1 — прочность A и статические кистени

Вся #87 остаётся IN_PROGRESS. Здесь только #166/#167/#116/#181 и согласованный пакет прочности A из #158/#159; #192/#115/#121 выполнены частично. Анимация #164 ещё НЕ реализована этим пакетом. Приёмка владельца отсутствует, Issues не закрывались, Project поле не изменялось.

## Production

- `combat/WeaponDefaults.java`: штатное ModifyDefaultComponentsEvent меняет только MAX_DAMAGE восьми согласованных предметов. Урон/скорость/дальность и прочие компоненты 68 IDs сравнены offline до/после события.
- 8 кистеней: отдельная 16px инвентарная иконка и объёмная held-сборка с рукоятью, проушиной, замкнутыми чередующимися звеньями и грузом ниже хвата.
- 7 грузов: собственные профили кубической/ступенчатой/фланцевой конструкции, разные геометрические сигнатуры; 2 комплектующих: рукоять и цепь. Registry IDs и рецепты сохранены.
- 69 файлов моделей/PNG перечислены с SHA в `blueprints/flails/applied-manifest.json`, совпадают с подготовленным ДО production `draft-manifest.json`. Старые source сохранены в двух before ZIP.
- Исторические `weapon_patch_assets.py`, `weapon_feedback_assets.py`, `weapon_visual_repair_assets.py` завершаются каноническим owner `weapons87_flails.py`. Исправлен откат актуального tooltip арбалета в историческом генераторе. Проверки генерации выполнялись в отдельной копии ресурсов, а не поверх чужих доработок.

## Доказательства

`flails-after/`: 17 source A/B в одинаковой камере/масштабе, отдельные GUI16px и первые лица обеих сторон (только item transforms, без подмены их игровым видом). `player-flails/`: 16 листов настоящих mesh/UV Steve/Alex с обеими руками, idle/walk/sneak/swing sample; применён native PlayerModel.translateToHand и ItemInHandLayer matrix. Это фиксированные технические позы, не проигранные игровые анимации.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Восемь согласованных значений прочности | native-attributes.json → native-attributes-after.json, durability-check.log; WeaponDefaults | PASS |
| Остальные компоненты 68 предметов прежние | actual ModifyDefaultComponentsEvent fixture, package-checks.json | PASS |
| 7 разных грузов, проушины, 2 комплектующих | реальные JSON/PNG, 9 индивидуальных чертежей и source A/B | PASS |
| 8 собранных кистеней с грузом ниже хвата | 8 чертежей, 8 held-сборок, native source player sheets | PASS |
| GUI1×, source transforms обоих первых лиц | flails-after/*firstperson-source.png и *before-after.png | PASS |
| Steve/Alex и обе руки, фиксированные source poses | player-native.json, player-flails/*.png и matrix.json | PASS |
| ID/рецепты/лут/gameplay вне MAX_DAMAGE не изменены | baseline preservation, native comparisons | PASS |
| Генераторы не откатывают новые и чужие ресурсы | generator-checks.json: последовательность canonical/repair/feedback/patch/canonical в отдельной копии | PASS |
| Java21 clean build и настоящий JAR | package-build.log, package1-receipt.json; offline classes не упакованы | PASS |
| Динамика цепи #164 | отдельный следующий пакет, в этот статический scope не входит | NOT_RUN |
| Игровые GUI/1P/3P, бой, FPS/MP | запусков Minecraft/сервера/GameTest: 0 | NOT_RUN / OWNER_PENDING |
| Установка этого промежуточного JAR | не поручена и не выполнялась; установленный PolyMC сохранён | NOT_RUN / POLYMC_NOT_INSTALLED |

Точный промежуточный JAR, SHA256, source HEAD + рабочий source manifest приведены в `package1-receipt.json`. Версия пока 1.9.7.53, это локальная промежуточная сборка с другим SHA, НЕ установленная тестовая версия и НЕ финальная доставка незавершённой цепочки.

## Проверка владельца после итогового пакета

Все восемь кистеней: GUI, на земле и в рамке; обе руки и модели Steve/Alex; покой/ходьба/приседание/удар. Проверить ладонь и груз ниже неё, объём звеньев сбоку, отсутствие мерцания. Семь отдельных грузов, рукоять и цепь проверить в инвентаре. Прочность новых стеков должна соответствовать таблице A; древние повреждённые предметы с Damage выше нового максимума могут сломаться на следующем использовании, это следствие согласованного снижения, NBT вручную не переписывается.
