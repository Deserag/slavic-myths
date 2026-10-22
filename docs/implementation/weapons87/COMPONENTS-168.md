# #168 — source ревизия остальных комплектующих

IN_PROGRESS общей цепочки #87. Художественный scope проверенных20 IDs: KEEP. Не приёмка владельца.

`tools/weapons87_components.py` воспроизводит `components-168/audit.json` и `SOURCE-20.png`: actual JSON/UV, GUI16px и8x nearest, фронт/профиль/3-4, recipe references и SHA моделей. Raw atlas не выдаётся за GUI: weapon_wrap/silver_fitting используют объёмные UV-модели. Их фактические формы — кожаная петля и серебряная втулка. iron_rings использует vanilla textures; отсутствие отдельного PNG не является missing texture.

Пять собственных деталей iron_blank, spear_shaft, short_shaft, weapon_wrap, silver_fitting оставлены KEEP: заготовка, длинное/короткое деревянное древко, обмотка и крепление имеют разные назначения и формы. Разная длина древок сохраняется.14 наконечников #99/#100 только QA: широкие втульчатые копейные и узкие лёгкие суличные формы различимы; материал внутри семейства является разрешённым recolor. iron_rings — QA-only кольчужная деталь, не редактировалась. Ещё9 деталей кистеней имеют собственные отчёты #166/#167, здесь не перерабатываются повторно.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Реальные20 зарегистрированных комплектующих и рецепты | components-168/audit.json, WeaponCatalog/ModItems, actual recipe references | PASS |
| Различимость форм на1x, геометрия/UV | components-168/SOURCE-20.png, ручной source review | PASS |
| Сохранение14 принятых наконечников и iron_rings | baseline.json + текущий package resource gate; zero edits этих файлов | PASS |
| Арт без необходимости не переписан | KEEP per-ID с обоснованиями; production edits0 по #168 | PASS |
| Согласованность с обновляемыми готовыми копьями | #171 ещё выполняется; финальная сверка после его реализации | NOT_RUN |
| Игровая визуальная проверка | запусков0; OWNER_PENDING | NOT_RUN |

#168 не передаётся на окончательное тестирование до финальной сверки с #171/#173. Генератор отчёта не изменяет production. Новых компонентов/ID/рецептов не добавлялось.


## Финальная source-согласованность после #171/#173

components-168/ROLE-CONTRAST-1X.png/context-comparison.json показывает actual GUI1x/8x iron_rings, bone_arrow, тяжёлый/лёгкий наконечник, готовое копьё/сулицу/нож и flail_chain. Формы различимы;29 зарегистрированных деталей покрыты:9 реальных flail components в #166/#167 +20 текущего аудита. Невыдуманные guards/blade/arrow parts не добавлялись. JSON recipe search рекурсивный, source references сохранены. Эта executor-проверка теперь PASS. Принятые14 tip assets и кольчужные кольца не менялись. Проверенные applicable generator/resource/JAR gates — package7-receipt.json; полная program integration #121/#191 ещё продолжается. Для самого audit scope #168 остался owner runtime NOT_RUN, технический scope передаваем на Тестирование при доступе к Project. Actual Status не менялся, Issue OPEN.
