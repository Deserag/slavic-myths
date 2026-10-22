# Пакет 17 / #177 — karachun_scythe: source KEEP (не игровая приёмка)

22.10.2026. [Исходный персональный лист](unique-source-review/karachun_scythe.png) и [полный source manifest](unique-source-review/source-manifest.json) созданы из реальных production JSON, face UV и PNG до этой итерации: общая камера span48, front/profile/3⁄4, GUI 16×16 и x8 nearest. Лист — аналитический source рендер, НЕ кадр клиента.

**Исходная геометрия:** 18.45 × 34.875 × 2.775 model units, 60 held cuboids. Текущий karachun_scythe_held.json Git blob `38dd6b0fb30b2943fb577205b629c79c2202ae8e`; сохранённый raster blob `b4e48f13fc49954090bff02cc54f3b32c68fa622`. Привязанные модели и PNG перечислены по точным путям + SHA256 в `unique-source-review/source-manifest.json` (исходный snapshot). `assets/slavicmyths/models/item` исходники фактически находятся под `src/main/resources/`.

**Проверка силуэта:** На исходной front/side/3/4 проекции отчётливо изогнутый серповидный длинный клинок, отдельные украшения/подвесы и тонкое древко. Не разрушать ранее возвращённую ornate geometry; root builtin/entity рендерит через item renderer, держим _held отдельно от _icon.

**Решение:** **KEEP самого арта**, без redrawing/retouch ради счётчика изменений. Не меняем item model, boss rig, анимации и механики; текущие firstperson и thirdperson display для обеих рук присутствуют в исходном held JSON.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Индивидуальный силуэт и палитра; профиль | `unique-source-review/karachun_scythe.png`, `source-manifest.json`; 60 held cuboids | PASS source-only |
| Реальные JSON/UV/PNG сохранены | `karachun_scythe_held.json` blob `38dd6b0fb30b2943fb577205b629c79c2202ae8e`; whitelist коммитов | PASS source-only |
| Модель GUI 1×, два item-held/TP/FP отображения | existing per-ID source sheet, four `display` contexts/либо held companion; #115 isolated TP edit | PASS metadata/source only |
| Steve/Alex, collision/clipping, движение/удар, FP/TP в живом Minecraft | ручная игра; не запускали | NOT_RUN / OWNER_PENDING |
| Java21 clean build/JAR после #115 | branch GitHub Actions Java21 | PASS — [Java21 run 39757888300](https://github.com/Deserag/slavic-myths/actions/runs/39757888300) |
| Полная #177, окончательное худож. согласование | владелец не принимал в игре | OPEN / OWNER_PENDING |

Если владелец увидит конкретный дефект в игре, открыть адресный FIX для этой модели, не переписывая хорошие части; связанная анимационная работа остаётся в #163/#165.
