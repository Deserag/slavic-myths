# Пакет 16 / #176 — serpent_crossbow: source KEEP (не игровая приёмка)

22.10.2026. [Исходный персональный лист](unique-source-review/serpent_crossbow.png) и [полный source manifest](unique-source-review/source-manifest.json) созданы из реальных production JSON, face UV и PNG до этой итерации: общая камера span48, front/profile/3⁄4, GUI 16×16 и x8 nearest. Лист — аналитический source рендер, НЕ кадр клиента.

**Исходная геометрия:** 20 × 4 × 15 model units, 8 idle/pulling, 9 charged. Текущий serpent_crossbow.json Git blob `3b58fd78f6875708d4933326bac16c8e3d7d117b`; сохранённый raster blob `4b9bccb0d96594fbb8021251615719d90f1127a1`. Привязанные модели и PNG перечислены по точным путям + SHA256 в `unique-source-review/source-manifest.json` (исходный snapshot). `assets/slavicmyths/models/item` исходники фактически находятся под `src/main/resources/`.

**Проверка силуэта:** Стройная поперечина и короткое ложе с относительно простыми деталями. Значительно ниже по профилю, чем three_breath (4 против 10), и не просто тот же 3D силуэт с другим PNG. Принятую компактность сохраняем.

**Решение:** **KEEP самого арта**, без redrawing/retouch ради счётчика изменений. В пакете14/#115 уже отдельно скорректированы только thirdperson translation всех 6 арбалетных состояний в обеих руках; firstperson, GUI, geometry, textures, overrides unchanged.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| Индивидуальный силуэт и палитра; профиль | `unique-source-review/serpent_crossbow.png`, `source-manifest.json`; 8 idle/pulling, 9 charged | PASS source-only |
| Реальные JSON/UV/PNG сохранены | `serpent_crossbow.json` blob `3b58fd78f6875708d4933326bac16c8e3d7d117b`; whitelist коммитов | PASS source-only |
| Модель GUI 1×, два item-held/TP/FP отображения | existing per-ID source sheet, four `display` contexts/либо held companion; #115 isolated TP edit | PASS metadata/source only |
| Steve/Alex, collision/clipping, движение/удар, FP/TP в живом Minecraft | ручная игра; не запускали | NOT_RUN / OWNER_PENDING |
| Java21 clean build/JAR после #115 | branch GitHub Actions Java21 | PENDING_CI |
| Полная #176, окончательное худож. согласование | владелец не принимал в игре | OPEN / OWNER_PENDING |

Если владелец увидит конкретный дефект в игре, открыть адресный FIX для этой модели, не переписывая хорошие части; связанная анимационная работа остаётся в #163/#165.
