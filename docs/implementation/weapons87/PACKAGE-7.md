# Пакет7 — #170: связная серебряная булава

Технический art scope всех9 IDs #170 реализован:8 KEEP, silver_mace FIX. Общая #87 IN_PROGRESS. Issue OPEN и owner runtime pending; поле Project технически не менялось.

Подтверждённые причины: головка #silver ссылалась на розовато-коричневую light_gray_terracotta; две тёмные полосы находились отдельно перед ней. Старая модель имела3 связных компонента (основное оружие+2 плавающие полосы). Новая имеет1: связную ступенчатую четырёхребристую серебряную головку, шейку/навершие/помель и деревянную обмотанную рукоять. Это замена формы/материала/UV, а не recolor. Используется уже существующий16px weapons87_materials atlas; PNG edits0. GUI front-lit, front orthographic angle-35 и центрирование дают пиксельный профиль без прежнего объёмного ракурса. Height18.5,head5.4x5.4,shaft1.3,grip(8,4,8).

Все TP/FP/ground/fixed transforms побайтно совпадают с прежними. Принятые чеканы не меняются. Сохраняются ID/рецепты/урон/скорость/reach, только ранее утверждённый A прочности остаётся в общей цепочке. Production edit1 JSON, новый канонический tools/weapons87_silver_mace.py воспроизводит его последним в цепочке overlays.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Все9 IDs:8 KEEP,1 FIX с причиной | FAMILY-170.md, review.json, actual source gallery | PASS |
| Индивидуальный чертёж до production | blueprints/silver-mace/silver_mace.png/plan.json/draft-assets | PASS |
| Реальные model/UV/GUI1x до/после | before-silver-mace.zip, source diagrams и native-before-after.png | PASS |
| Связная головка вместо плавающих полос | old connected components3 →new1 | PASS |
| Хват/обе руки/Steve/Alex/FP/TP |20 actual native source cases, одинаковые hand transforms; idle head candidates0 AFTER | PASS |
| Чеканы/остальные8 art и все PNG не изменены | family-art-review/archived comparisons + baseline whitelist | PASS |
| Числа/ID/механики не менялись |68 actual component snapshots + preservation gate | PASS |
| Java21 clean build + native fixtures | package7-build.log; test NO-SOURCE | PASS |
| Actual resource/JAR и applicable historical generator replay | package7-gates.log/package7-receipt.json, точное совпадение с pre-production draft | PASS |
| Игровая визуальная/анимационная проверка | launches0, owner-only | NOT_RUN / OWNER_PENDING |
| Установка в PolyMC | не поручена для промежуточной цепочки | NOT_RUN / POLYMC_NOT_INSTALLED |

Локальный промежуточный JAR `D:\slavic-myths\build\libs\slavicmyths-1.9.7.53.jar`,version1.9.7.53,SHA256 `50ff06b3732b5f0a335e02f3ef4a6d594f60931c47c7097f13d3b9290039a438`,23691997 bytes. Source base `32d1733457e7e90360ce16c23860304ece664c64` + uncommitted manifest SHA `b33e2b283f89fb0ba9bbe3746fc5c5220bbedada7028092286c5b687bed05aeb`. Не commit/push/установка/финальная сборка87. Историческая source gallery до FIX сохранена в family-art-review/package6-original-review.zip.

Owner: silver_mace рядом с stone/iron/perunite mace и club, GUI/FP/TP/обе руки/Steve-Alex/движение/рамка/выброшенный предмет/glint; убедиться в читаемом серебре, связной голове и прежнем хвате. Чеканы визуально сравнить без изменения принятой ориентации. Все игровые результаты ещё OWNER_PENDING.

Дополнительно #168: финальная source-согласованность деталей с #171/#173 и ролью стрелы/кольца показана components-168/ROLE-CONTRAST-1X.png.20 оставшихся деталей KEEP;14 принятых наконечников/iron_rings QA-only. Их production не редактировался. Общая integration/generator программа #121/#191 и дальнейшие дети #87 ещё продолжаются.
