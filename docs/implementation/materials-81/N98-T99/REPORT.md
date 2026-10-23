# #98 + #99 — природные материалы и наконечники, 22.10.2026

Взяты из фактического Status **Ожидание**, Project №9, представление «Очередь Codex» (19 элементов, https://github.com/users/Deserag/projects/9/views/2). Прямое поручение владельца — реализовать несколько ожидающих задач за один прогон. Полные тексты #81/#98/#99 и комментарии прочитаны; #97 и #130 изучены, но не включены в этот пакет. Отложенный shield не запущен, #100 зависит от T1 и сюда не входит.

**Scope #98:** 4 природных PNG. **Scope #99:** 8 PNG crafting-компонентов; этапы T1-A stone/gold и T1-B iron/silver выполнены последовательно, между ними проверен whitelist/канонические пиксели/привязки моделей. Оружие и его held/flight/GUI модели, Java, рецепты, локализации, registry, Creative ordering, прочность/эффекты/лут не менялись.

Нативные размеры сохранены: береста и кость 16×16, жир/шкура/8 наконечников 32×32. Рисунок crisp pixel art, без blur/AA/hue-shift и без новых файлов текстур/моделей в production. Обычный наконечник каждой пары имеет >25% дополнительных непрозрачных пикселей; четыре материала имеют четыре разные alpha-маски обычного наконечника. Это source-метрики и визуальные сравнения, не игровая приёмка.

| ID | Дефект → реализация | Реальный production asset до/после | A/B | Результат |
|---|---|---|---|---|
| `birch_bark` | Плоский лист с точками, напоминающими лицо → скрученный/расслоённый край и редкие горизонтальные чечевички коры. | `src/main/resources/assets/slavicmyths/textures/item/birch_bark.png` | [AB-natural](AB-natural.png) | PASS / OWNER_PENDING |
| `animal_fat` | Овальное белёсое пятно → неровный сливочно-бежевый кусок с лопастями, читаемыми верхними плоскостями и тенью; без магического орнамента. | `src/main/resources/assets/slavicmyths/textures/item/animal_fat.png` | [AB-natural](AB-natural.png) | PASS / OWNER_PENDING |
| `goat_hide` | Компактная щитовидная форма с ровной полосой → асимметричная распластанная шкура, шея/четыре выступа и естественный матовый ворс. | `src/main/resources/assets/slavicmyths/textures/item/goat_hide.png` | [AB-natural](AB-natural.png) | PASS / OWNER_PENDING |
| `large_animal_bone` | Тонкая диагональная палочка → более толстый диафиз и ясно выраженные суставные концы. | `src/main/resources/assets/slavicmyths/textures/item/large_animal_bone.png` | [AB-natural](AB-natural.png) | PASS / OWNER_PENDING |
| `stone_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → широкий тяжёлый наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/stone_spearhead.png` | [AB-stone-gold](AB-stone-gold.png) | PASS / OWNER_PENDING |
| `stone_light_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → удлинённый узкий лёгкий наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/stone_light_spearhead.png` | [AB-stone-gold](AB-stone-gold.png) | PASS / OWNER_PENDING |
| `gold_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → широкий тяжёлый наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/gold_spearhead.png` | [AB-stone-gold](AB-stone-gold.png) | PASS / OWNER_PENDING |
| `gold_light_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → удлинённый узкий лёгкий наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/gold_light_spearhead.png` | [AB-stone-gold](AB-stone-gold.png) | PASS / OWNER_PENDING |
| `iron_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → широкий тяжёлый наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/iron_spearhead.png` | [AB-iron-silver](AB-iron-silver.png) | PASS / OWNER_PENDING |
| `iron_light_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → удлинённый узкий лёгкий наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/iron_light_spearhead.png` | [AB-iron-silver](AB-iron-silver.png) | PASS / OWNER_PENDING |
| `silver_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → широкий тяжёлый наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/silver_spearhead.png` | [AB-iron-silver](AB-iron-silver.png) | PASS / OWNER_PENDING |
| `silver_light_spearhead` | Одинаковый старый шаблон материалов / слабое отличие пары → удлинённый узкий лёгкий наконечник с индивидуальным профилем материала, гранью и втулкой. | `src/main/resources/assets/slavicmyths/textures/item/silver_light_spearhead.png` | [AB-iron-silver](AB-iron-silver.png) | PASS / OWNER_PENDING |

## Требования и доказательства

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Реальная регистрация и model→texture | ModItems BIRCH_BARK/LARGE_ANIMAL_BONE, Textiles.items animal_fat/goat_hide, WeaponCatalog parts; assets в verification.json | PASS |
| Все 4 ID #98 и все 8 ID #99 исполнены | 12 PNG, before snapshots, AB-natural/stone-gold/iron-silver.png, all-eight.png | PASS |
| Заданные материалы, самостоятельные силуэты, natural без магических прожилок | actual PNG review + alpha-mask/размер/пиксели canonical checker | PASS (source) |
| Защита исправлений от исторических писателей | материалы write_owned через materials81_m1; textile save, generate_065 png; финальный exact overlay create_resources | PASS |
| Старые реальные writer functions в изолированной папке | AST import/defs-only вызовы textile.save, generate_065.png, generate_textures.png, weapon_resources_1321.asset; намеренный red/green input не откатывает PNG | PASS |
| Канонический генератор и идемпотентность | два apply в sandbox, ровно 12 PNG, соседний linen_thread не захвачен | PASS |
| Производственный scope | production-baseline.json от начала этого пакета: изменились ровно 12 разрешённых PNG, новых src/main путей нет, gradle.properties тот же | PASS |
| Java21 clean build | materials98-99-clean-build.log, BUILD SUCCESSFUL 56s; test NO-SOURCE | PASS |
| Production NeoForge JAR и ресурсы | verify_materials81_n98_t99.py --jar, все 7601 ресурса byte-exact | PASS |
| Игровой вид в реальных слотах, освещение/ресурспаки | запусков Minecraft/server/GameTest агентом = 0; только личная проверка владельца | NOT_RUN / OWNER_PENDING |
| Фактический перевод #98/#99 в «Тестирование» | проверен GitHub API: INSUFFICIENT_SCOPES, имеющийся credential без project; browser signed out; connector не имеет Projects action | BLOCKED / NOT_CHANGED |

**Исполнительские код/asset/build результаты по #98/#99 выполнены; рекомендуемый статус обеих задач — «Тестирование». Поле Status в GitHub Project фактически не изменено.** Не считать комментарий, этот отчёт или локальный JSON сменой поля. Issue не закрывались, owner-accepted не добавлялся.

Версия мода **не повышалась** (остаётся 1.9.7.57). PolyMC **не обновлялся**: установленный JAR по-прежнему `0793672e1c652d19410aa4c7491139e27a4977448b9a4cea65371567217d33ad`, это предыдущая сборка голов. Новые иконки пока только в source и build/libs. Последняя инструкция владельца требует отдельной команды на подготовку/установку версии для его тестирования.

Новый локально собранный JAR: `build/libs/slavicmyths-1.9.7.57.jar`, SHA256 `171da7ba91fbf69828db20c16ec13a46cb90ddb8b2cd83cd2cc16305fed40c8a`. Он НЕ установлен. Source baseline `1192e229` + сохранённые ранее локальные доработки голов + этот пакет ресурсов. Snapshot начала пакета в production-baseline.json отличает его от предыдущей незакоммиченной работы. Commit/push/merge не выполнялись.

Приёмка #140: владелец отдельно в этом чате подтвердил головы и разрешил «Готово». Решение зафиксировано в trophy140-rework/OWNER_ACCEPTANCE.json; реальное изменение Project Status #140 также NOT_CHANGED из-за отсутствия прав.
