# Пакет 19 / #183 — nightingale_dagger: source KEEP (не игровая приёмка)

22.10.2026. [Исходный персональный лист](unique-source-review/nightingale_dagger.png) и [полный source manifest](unique-source-review/source-manifest.json) созданы из реальных production JSON, face UV и PNG до этой итерации: общая камера span48, front/profile/3⁄4, GUI 16×16 и x8 nearest. Лист — аналитический source рендер, НЕ кадр клиента.

**Исходная геометрия:** 5 × 19.3 × 2 model units, 12 held cuboids. Текущий nightingale_dagger.json Git blob `9b45763b1dc0671467330010e7ce60432ff44b2a`; сохранённый raster blob `202d67b9fe179ba971ec6d474256eed3fc5eaec8`. Привязанные модели и PNG перечислены по точным путям + SHA256 в `unique-source-review/source-manifest.json` (исходный snapshot). `assets/slavicmyths/models/item` исходники фактически находятся под `src/main/resources/`.

**Проверка силуэта:** На source листе узкий клинок, выраженный наконечник, короткая кожаная рукоять, двухступенчатые переходы металла. По пропорциям не напоминает широкую саблю; тонкий 2-unit профиль без лишней толщины. В GUI 1× клинок и рукоять различимы; металл/кожа/edge имеют раздельные texture roles.

**Решение:** **KEEP самого арта**, без redrawing/retouch ради счётчика изменений. Не трогаем звук/способность Соловья; все четыре held contexts указаны явно в корневой модели.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Индивидуальный силуэт и палитра; профиль | `unique-source-review/nightingale_dagger.png`, `source-manifest.json`; 12 held cuboids | PASS source-only |
| Реальные JSON/UV/PNG сохранены | `nightingale_dagger.json` blob `9b45763b1dc0671467330010e7ce60432ff44b2a`; whitelist коммитов | PASS source-only |
| Модель GUI 1×, два item-held/TP/FP отображения | existing per-ID source sheet, four `display` contexts/либо held companion; #115 isolated TP edit | PASS metadata/source only |
| Steve/Alex, collision/clipping, движение/удар, FP/TP в живом Minecraft | ручная игра; не запускали | NOT_RUN / OWNER_PENDING |
| Java21 clean build/JAR после #115 | branch GitHub Actions Java21 | PASS — [Java21 run 39757888300](https://github.com/Deserag/slavic-myths/actions/runs/39757888300) |
| Полная #183, окончательное худож. согласование | владелец не принимал в игре | OPEN / OWNER_PENDING |

Если владелец увидит конкретный дефект в игре, открыть адресный FIX для этой модели, не переписывая хорошие части; связанная анимационная работа остаётся в #163/#165.
