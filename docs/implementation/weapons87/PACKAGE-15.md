# Пакет 15 / #175 — three_breath_crossbow: source KEEP (не игровая приёмка)

22.10.2026. [Исходный персональный лист](unique-source-review/three_breath_crossbow.png) и [полный source manifest](unique-source-review/source-manifest.json) созданы из реальных production JSON, face UV и PNG до этой итерации: общая камера span48, front/profile/3⁄4, GUI 16×16 и x8 nearest. Лист — аналитический source рендер, НЕ кадр клиента.

**Исходная геометрия:** 20 × 10 × 19 model units, 65 idle/65 pulling, 68 charged. Текущий three_breath_crossbow.json Git blob `19bd65d8a7b127a72edfd55c49de820f9319c0e2`; сохранённый raster blob `9bd65528cc254a79fe2c4cf558e613c3eaf8bb76`. Привязанные модели и PNG перечислены по точным путям + SHA256 в `unique-source-review/source-manifest.json` (исходный snapshot). `assets/slavicmyths/models/item` исходники фактически находятся под `src/main/resources/`.

**Проверка силуэта:** В production есть крупные плечи, объёмный верхний блок с тремя самостоятельными выступами, несколько цветовых акцентов дыханий; это конструкция (65–68 элементов), а не recolor малого арбалета. Три головы не разрослись до гигантской эмблемы. 3/4 и профиль отличаются от serpent; 

**Решение:** **KEEP самого арта**, без redrawing/retouch ради счётчика изменений. В пакете14/#115 уже отдельно скорректированы только thirdperson translation всех 6 арбалетных состояний в обеих руках; firstperson, GUI, geometry, textures, overrides unchanged.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Индивидуальный силуэт и палитра; профиль | `unique-source-review/three_breath_crossbow.png`, `source-manifest.json`; 65 idle/65 pulling, 68 charged | PASS source-only |
| Реальные JSON/UV/PNG сохранены | `three_breath_crossbow.json` blob `19bd65d8a7b127a72edfd55c49de820f9319c0e2`; whitelist коммитов | PASS source-only |
| Модель GUI 1×, два item-held/TP/FP отображения | existing per-ID source sheet, four `display` contexts/либо held companion; #115 isolated TP edit | PASS metadata/source only |
| Steve/Alex, collision/clipping, движение/удар, FP/TP в живом Minecraft | ручная игра; не запускали | NOT_RUN / OWNER_PENDING |
| Java21 clean build/JAR после #115 | branch GitHub Actions Java21 | PENDING_CI |
| Полная #175, окончательное худож. согласование | владелец не принимал в игре | OPEN / OWNER_PENDING |

Если владелец увидит конкретный дефект в игре, открыть адресный FIX для этой модели, не переписывая хорошие части; связанная анимационная работа остаётся в #163/#165.
