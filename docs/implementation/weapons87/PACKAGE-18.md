# Пакет 18 / #179 — morozko_staff: source KEEP (не игровая приёмка)

22.10.2026. [Исходный персональный лист](unique-source-review/morozko_staff.png) и [полный source manifest](unique-source-review/source-manifest.json) созданы из реальных production JSON, face UV и PNG до этой итерации: общая камера span48, front/profile/3⁄4, GUI 16×16 и x8 nearest. Лист — аналитический source рендер, НЕ кадр клиента.

**Исходная геометрия:** 9.5 × 31 × 2.75 model units, 66 held cuboids. Текущий morozko_staff_held.json Git blob `889cf5a1127f496d2d697a8651bd177f39606a66`; сохранённый raster blob `90981d80dda3a7d84668173ea3ce4eaf3ca84589`. Привязанные модели и PNG перечислены по точным путям + SHA256 в `unique-source-review/source-manifest.json` (исходный snapshot). `assets/slavicmyths/models/item` исходники фактически находятся под `src/main/resources/`.

**Проверка силуэта:** Высокий посох с узким стержнем и характерной многолучевой ледяной короной. Силуэт отличается от тёмного посоха Лихо и деревянного carved/storm; отдельные _icon и _held обеспечивают GUI против рук без правки босса.

**Решение:** **KEEP самого арта**, без redrawing/retouch ради счётчика изменений. Не меняем item model, boss rig, анимации и механики; текущие firstperson и thirdperson display для обеих рук присутствуют в исходном held JSON.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Индивидуальный силуэт и палитра; профиль | `unique-source-review/morozko_staff.png`, `source-manifest.json`; 66 held cuboids | PASS source-only |
| Реальные JSON/UV/PNG сохранены | `morozko_staff_held.json` blob `889cf5a1127f496d2d697a8651bd177f39606a66`; whitelist коммитов | PASS source-only |
| Модель GUI 1×, два item-held/TP/FP отображения | existing per-ID source sheet, four `display` contexts/либо held companion; #115 isolated TP edit | PASS metadata/source only |
| Steve/Alex, collision/clipping, движение/удар, FP/TP в живом Minecraft | ручная игра; не запускали | NOT_RUN / OWNER_PENDING |
| Java21 clean build/JAR после #115 | branch GitHub Actions Java21 | PASS — [Java21 run 39757888300](https://github.com/Deserag/slavic-myths/actions/runs/39757888300) |
| Полная #179, окончательное худож. согласование | владелец не принимал в игре | OPEN / OWNER_PENDING |

Если владелец увидит конкретный дефект в игре, открыть адресный FIX для этой модели, не переписывая хорошие части; связанная анимационная работа остаётся в #163/#165.
