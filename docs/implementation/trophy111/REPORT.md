# #111 · T2 · частичная реализация · 21.10.2026

**Исторический отчёт.** Позднее владелец разрешил generated2D для pearl_fragment;
блокер снят, актуальный полный scope и сборка: [новая ревизия](../trophy111-refinement/REPORT.md).

**IN_PROGRESS / IN_GAME_OWNER_PENDING. Issue OPEN. Не переводить в «Тестирование»: pearl_fragment требует решения по 3D/UV дизайну.** Project должен быть «В работе»; изменить поле не удалось: GitHub connector не предоставляет Projects mutations, последняя доступная браузерная проверка Project №9 без авторизации/read-only. Изменение статуса не заявляется.

## Поручение / scope

Владелец поручил начать следующую реализацию после #110, отдельно подтвердил Княжескую печать из кургана, затем предоставил новый screenshot и замечание о схожести/слабом дизайне. При отсутствии референса дизайн готовит владелец. Прочитаны актуальные #92/#111 и комментарии; AGENTS/project docs. Workflow/playbook отсутствуют локально, ранее в этой же сессии прочитаны из GitHub main.

Только десять ID #111: **8 FIX /1 KEEP /1 BLOCKED**. Пакеты3/3/4, проверяемые отдельно в [T2-A](T2-A.png), [T2-B](T2-B.png), [T2-C](T2-C.png). #112–114 production не начинались. Других активных Codex writer в доступном списке нет; существующий advancements worktree не затронут. Commit/push не выполнялись.

Source HEAD `69a2db12de4baac9cef7c43e6c4b0ae717d3b644` + dirty рабочее дерево. [Исходные SHA src/tools/status docs](workspace-before.json), [status до работы](git-status-before.txt), [наш список отличий](workspace-diff.json). Чужие изменения и результаты #110 побайтово сохранены. Модели/registry/Java/gameplay/лут/рецепты/rarity/локализации/порядок вкладки этой работой не менялись.

## Поимённый результат

| ID | Решение | Проверенный baseline → результат |
|---|---|---|
| old_button | KEEP |4 отверстия/ступенчатый обод уже видны; исходные байты сохранены |
| ancient_coin | FIX |пересекающиеся блики/псевдообъём → восьмигранная монета, кант, прямоугольная чеканка |
| pearl_fragment | BLOCKED_NEEDS_3D_UV_DESIGN |есть плоский эскиз B; фактический source —3D cuboid/32px. Новый native3D/UV чертёж отсутствует; исходные bytes сохранены |
| shield_boss_fragment | FIX |симметричный целый диск → выломанный умбон, неровный край и одна заклёпка |
| old_buckle | FIX |центральная перекладина/два отверстия → полая вытянутая рамка с различимым горизонтальным язычком |
| bandit_token | FIX |контрастная красная абстрактная метка → дешёвый рваный жетон со слабой простой чеканкой |
| ataman_sign | FIX |пурпурный значок с яркой вставкой → тёмный металлический знак с горизонтальной пластинкой и тёплой инкрустацией |
| voevoda_insignia | FIX |нечёткий зауженный медальон → ромбический должностной знак с узким центром |
| princely_seal | FIX |круглая золотая плашка → угловатый квадратный штамп с чёткой границей |
| serpent_seal | FIX |трёхзубый U-знак → тёмная печать со ступенчатым круглым ободом и ломаной змеёй |

[Общий A/B](before-after.png), [панель знаков без подписей](unlabelled-signs.png), [production baseline PNG](before/). Это **offline actual texture comparisons**, 1×/3×nearest, одинаковая рамка. У pearl_fragment показана только неизменённая texture, НЕ baked/rendered3D и НЕ GUI screenshot; этот ряд не доказывает игровую форму. Оригинальный скрин владельца: [owner-screenshot.png](owner-screenshot.png). Схемы B извлечены в [reference-fragments.txt](reference-fragments.txt); concept не выдается за production.

Точные old/new SHA256, native sizes и actual model/texture paths для каждого ID — [verification.json](verification.json). У всех PNG native16px, кроме ataman_sign/serpent_seal32px и сохранённого pearl_fragment32px. Два изменённых32px рисунка используют crisp2px кластеры. old_buckle сохраняет loader `neoforge:separate_transforms`, GUI sprite и отдельный `old_buckle_held` с прежними UV/materials/display. Все модельные JSON побайтово сохранены; общие renderers не менялись.

## Воспроизводимость

`tools/trophy111_art.py` содержит независимые рисунки8 FIX по секции B. Прямой вызов `python tools/trophy111_art.py <ID> ...` пишет только указанные FIX PNG. `vanilla_item_style` сохраняет канонические sprites. Scoped hooks добавлены в `kurgan_086`, `gorynych_assets`, `rare_weapon_resources_1322` (только ataman_sign), старые coin writers `generate_045/046/milestone`. Эти scripts как полные генераторы не запускались: из-за legacy side effects проверяются настоящие AST FunctionDef/guard/tail statements в temp-root. Прочие предметы и оружие из этих scripts не изменялись.

## Технические gates

Java21 `JAVA_HOME=C:/Program Files/Java/jdk-21.0.12`, `gradlew.bat clean build`: **PASS**, финальный45s, compileJava FROM-CACHE; ресурсы/JAR пересобраны. До дополнительной правки пряжки/знака был успешный53s build; он не подменяет финальный. [Лог](build.log).

`python tools/verify_trophy111.py`: PASS технических checks; 7442 production resources побайтово совпадают с JAR, 3947 JSON разобраны, 1339 PNG декодированы, fixture/tools в JAR нет. neoforge.mods.toml сверяется с processed output после штатной version expansion. [Результаты](checks.log).

- Версия metadata: **1.9.7.48**, существующий номер не менялся.
- Production: `D:\slavic-myths\build\libs\slavicmyths-1.9.7.48.jar`
- Копия **частичного** кандидата: `D:/slavic-myths/build/trophy111/slavicmyths-1.9.7.48-trophy111-partial.jar`
- Размер: **23506287 bytes**
- SHA256: `6c406ea3edd971e74165642022e1ae055ded97dd391b1252122570a5429ab12a`
- Source: указанный HEAD + локальный diff; нового source commit нет.
- **POLYMC_NOT_INSTALLED**, установленный48 этим кандидатом не заменялся.
- Запусков Minecraft/dev client/GameTest/server/PolyMC: **0**.

## REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
|10 ID индивидуально проверены |таблица/manifest/схемы B |PASS |
|8 FIX реальные PNG,1 KEEP |before-after/T2-A/B/C/production |PASS offline; OWNER_PENDING in-game |
|pearl_fragment читаемый треугольный фрагмент |плоский concept существует, native3D/UV решения нет |BLOCKED_NEEDS_3D_UV_DESIGN |
|Княжеская печать vanilla-like штамп |princely_seal PNG и A/B |PASS offline; OWNER_PENDING |
|native size/model/anchors/gameplay/чужие изменения |baseline hash audit/actual model JSON |PASS |
|генераторы воспроизводят8 FIX |изолированный тест фактических hooks/functions |PASS |
|Java21 clean build |build.log45s |PASS |
|JSON/PNG/JAR/resource parity |verification.json |PASS |
|Project «В работе» |нет Projects write access |BLOCKED_ACCESS |
|полный объём #111 |1 design blocker |BLOCKED; не передавать в «Тестирование» |
|игровая приёмка/1P/3P/сохранения |запусков0 |NOT_RUN / OWNER_PENDING |

## Что требуется от дизайна

[DESIGN_GAPS.md](DESIGN_GAPS.md): отдельно описан pearl_fragment и отсутствие индивидуальных новых эскизов для части следующих #114/неопределённых украшений. Это не разрешение на массовый redesign. На новом скриншоте видимые firebird_down/firebird_feather/smoldering_feather/mat_bone_plates/mat_mirror_shards/mat_skim_fang относятся к #114; их production не менялся. По неназванной паре наград/украшений ожидаются точные названия владельца.

## Последующая ручная проверка

После решения по pearl_fragment и полного технического gate: проверить в отдельной тестовой копии GUI разных scales, различимость без hover, контраст и count overlays; 1P/3P/выброшенные items. У пряжки отдельно проверить неизменённую held3D модель. Сравнить значения старых stacks, получение из loot, recipes-потребители и перезаход. Текущий screenshot показывает прежние ресурсы, а offline A/B не является игровым PASS. До ручного решения владельца Issue OPEN/owner-accepted отсутствует.
