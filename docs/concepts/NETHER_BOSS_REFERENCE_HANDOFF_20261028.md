# Slavic Myths — точная передача источников референсов Codex (28.10.2026)

**Область:** P0 [#327](https://github.com/Deserag/slavic-myths/issues/327), полная недостроенная 2.0.4 [#288](https://github.com/Deserag/slavic-myths/issues/288), Постень [#293](https://github.com/Deserag/slavic-myths/issues/293), трофеи [#302](https://github.com/Deserag/slavic-myths/issues/302), Повелитель магмы [#317](https://github.com/Deserag/slavic-myths/issues/317). Использовать только ветку `hell` и текущий HEAD; архивы/PNG **не считать физически находящимися в GitHub** на основании этой документации.

## 0. Вывод по доступу — НЕ ПЕРЕКЛАДЫВАТЬ НА ВЛАДЕЛЬЦА

Архивы действительно числятся в пользовательской **Library ChatGPT /славянская мифология/**, но **Codex с доступом к GitHub не получает доступ к библиотеке ChatGPT**. Проверено 28.10: попытка получить реальные байты пяти ZIP через `files.materialize(raw_file)` возвращает `This Project file does not have an authorized raw-byte materialization path`. В GitHub есть описания, SVG и SHA, **нет исходных PNG/ZIP**. Прямая скачиваемая URL-ссылка на оригинальные ZIP **не установлена**. Изобретать ссылки, «скачивать» SHA или перерисовывать неизвестный PNG запрещено.

Исполнитель обязан **самостоятельно** проверить доступные ему архивы/вложения/локальные worktree, `Downloads`, `docs/...`, директории других законных доступных источников и запустить `python tools/audit_nether_boss_reference_assets.py --root .`. **Не требовать от владельца повторно выбрать дизайн, вручную создавать каталог или передавать файлы ради продолжения независимого кода.** Если библиотека недоступна и оригиналов нет — честно `ART_SOURCE_BLOCKED` для точного сравнения, продолжать реализацию независимых Java/worldgen/AI/loot/регистраций. Никакой выдуманный `ART_PASS`.

## 1. Единственный утверждённый набор оригиналов — названия, размеры и SHA-256

Утверждённые SHA ниже взяты из текстовых contract файлов ветки `hell`, **не пересчитаны в этой сессии по PNG**. `python tools/audit_nether_boss_reference_assets.py` сопоставляет реальные SHA **внутри ZIP**, даже если исходное имя внутри архива отличается.

### Чертоги заблудших 2.0.4 — 4 ZIP

| Точный архив, Library `/славянская мифология/` | Размер ZIP, байт | Ожидаемый оригинал в архиве → SHA-256 | Issue/назначение |
|---|---:|---|---|
| `Чертоги_заблудших_2.0.4_жёсткое_ТЗ_референсы.zip` | 8837561 | `approved_originals/blocks.png` → `ff3b278772fe276a464c6a5c0da7592552a1e921e016e6c075ffe20cce01e781`; `approved_originals/scenes.png` → `d0d95085857c930a24d6ed6fc67729ae927a5103d77b536a1387ff8db79fe4b2`; `approved_originals/mobs.png` → `a330f127e60c4055dd86aadb91f169e337f4e60c9c90ad8e7982b49cac4be042` | #289–#292/#294; **главный камень №3 первой строки**, 6 сцен S1–S6, 5 видов мобов |
| `Чертоги_заблудших_2.0.4.4_референсы_для_Codex.zip` | 4629422 | Крупные отдельные панели пяти мобов: проверять по исходному `mobs.png`, для панелей SHA не утверждён | #292; крупные детали моделей, не новый альтернативный дизайн |
| `Чертоги_2.0.4_награды_утверждённые_референсы.zip` | 3241149 | 1536×1024 poster наград (в contract имя `a_detailed_high_resolution_infographic_poster_in.png`) → `0eb92eea714205ca75219af6aa9f9f1e586dae30494b6534ad28e412daff9856` | #301; материалы/Путевая метка/Камертон, **не** дизайн мобов |
| `Постень_2.0.4.5_утверждённый_арт_и_чертежи_Codex.zip` | 5106217 | `approved_originals/posten_original_1536x1024.png` → `34e30e8cde56dffa7ab8e2ff0bf917da3ebdd808675e02da5cfeb123502d65d0` | #293/#302/#303; **финальная** форма Постеня, 4 реальных маски |

Истина по значениям: [APPROVED_REFERENCE_ASSET_INDEX_2.0.4.md](lost-halls/APPROVED_REFERENCE_ASSET_INDEX_2.0.4.md), [STRICT_ACCEPTANCE_2.0.4.md](lost-halls/STRICT_ACCEPTANCE_2.0.4.md), [POSTEN_MODEL_COMBAT_LOCK_2.0.4.5.md](lost-halls/POSTEN_MODEL_COMBAT_LOCK_2.0.4.5.md). У постера наград и мобов разные функции. Более ранний `чертоги_заблудших_референсы_2.0.4.zip` и одноимённый дубликат `(1).zip` не переопределяют утверждённые SHA.

### Чёрная гарь 2.0.3 — 1 ZIP

Архив: `Чёрная_гарь_2.0.3_жёсткие_референсы.zip`, размер **8686150** байт, Library `/славянская мифология/`, внутри `approved_refs/`:

| Оригинал | Разрешение | SHA-256 |
|---|---|---|
| `landscape.png` | 1672×941 | `09a92d889d1dcabc2821d872e2287e6400f00bf01baf8674d32323d3bdb3bce8` |
| `blocks_REVISED.png` | 1448×1086 | `8697b9bd400c7790065bfa243db65d02d7468d19f873b9929a09ba54e47c5e0a` |
| `mobs.png` | 1448×1086 | `0210bc96b2b4ff72b8ded59a5a0a2b1ba75f0e980974ec14a86f2ad090b29a81` |
| `zharenitsa.png` | 1312×1199 | `ad805df5f2c2c3e2ee3e2ab0ff86c5103464a54c662d3c2f2e820ed96f4d874e` |

Истина: [STRICT_ACCEPTANCE_2.0.3.md](black-cinder/STRICT_ACCEPTANCE_2.0.3.md), [RECOVERED_VISUAL_SPEC_2.0.3.md](black-cinder/RECOVERED_VISUAL_SPEC_2.0.3.md). **Только `blocks_REVISED.png`**, отменённый первый лист НЕ использовать. Жареница — парящая женская фигура, не тонкий палочный каркас и не переокрашенный Повелитель.

### Горючие острова 2.0.5 — 1 ZIP

Архив: `BURNING_ISLANDS_205_COMPLETE_REFERENCES_FOR_GITHUB.zip`, размер **12002229** байт, Library `/славянская мифология/`; **утверждённый SHA архива** `22256bf4df4f8c8abf6c5aec2ec6a9d30c3be02b0a3d7b59c5751908bc6edada`. Содержимое предназначено для `docs/concepts/burning-islands/references/`.

| Оригинал | SHA-256 |
|---|---|
| `BURNING_ISLANDS_205_BIOME_CONCEPT.png` | `a770acc9cb8401baa8199994f73cf28ee1fb3d43eff25fe0fd5bdbff92fbf605` |
| `BURNING_ISLANDS_205_CREATURES_CONCEPT.png` | `a06f8d7730e43f93003a0a9c465c806e6e28a41260e7a61640d1f336924651d1` |
| `BURNING_ISLANDS_205_RUNE_MATERIALS_CONCEPT.png` | `47a9f11cd0e3c98e48983cf28c8b5ed7c6687689d9abb9cc5ee28edd7a47fdab` |
| `BURNING_ISLANDS_205_BOSS_CONCEPT.png` | `354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6` |
| `BURNING_ISLANDS_205_MAGMA_LORD_HEAD_PLACED_INVENTORY.png` | `287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93` |

Истина: [references/README.md](burning-islands/references/README.md), [CODEX_STAGE3_MAGMA_LORD_IMPLEMENTATION_2.0.5.md](burning-islands/CODEX_STAGE3_MAGMA_LORD_IMPLEMENTATION_2.0.5.md), [IMPORT_REFERENCES.ps1](burning-islands/references/IMPORT_REFERENCES.ps1). Не путать рисунок установленной головы с sprite предмета.

### Навья роща 2.0.2 — финальный Сторож чащи

Истина: [guardian-2.0.2.5-spec.md](navya-grove/guardian-2.0.2.5-spec.md), [STRICT_ACCEPTANCE_2.0.2.md](navya-grove/STRICT_ACCEPTANCE_2.0.2.md), [guardian-2.0.2.5-blueprint.svg](navya-grove/guardian-2.0.2.5-blueprint.svg). **Утверждён последний тёмный проклятый реколор первого Силуэта**, не ранний светлый/бирюзовый концепт. SHA исходного финального PNG **не подтверждён**, точная прямая ссылка **не подтверждена**. Если отсутствует реальный оригинал — только его `ART_SOURCE_BLOCKED`, не делать финальный artwork из выдуманного изображения.

## 2. Где получать сведения при недоступном оригинале

1. **GitHub `hell` — уже доступно Codex:** перечисленные выше Markdown+SVG, `AGENTS.md`, `docs/requirements/`, исходники `src/main/java/org/slavicmyths/{navya,blackcinder,losthalls,burningislands,trophy}/`, `src/main/resources`.
2. **ChatGPT Library, коллекция `/славянская мифология/` — происхождение оригиналов, но не доступный GitHub URL.** Наличие записи в библиотеке НЕ разрешает утверждать, что ZIP читается на машине Codex.
3. **Локальные источники Codex:** проверить текущий и другие разрешённые рабочие каталоги (без изменения чужих файлов), `Downloads`, `docs/concepts/**`, реальные прикреплённые к задаче вложения, при наличии — разрешённые connector действия. Архивы идентифицируются точными basename/размером, исходный PNG — SHA.
4. **Проверка `python tools/audit_nether_boss_reference_assets.py --root .`**: отчёт `found/sha_mismatch/missing` и точный `ZIP!member`; скрипт **не скачивает файлы, не придумывает ссылки и не меняет игровые данные**.
5. Если источник оригинала не доступен: в отчёте назвать **конкретный файл и SHA**, отметить `ART_SOURCE_BLOCKED` и реализовать независимые от изображения задачи; окончательное owner ART acceptance оставить открытым.

## 3. Продолжение #327 — порядок без вопросов к владельцу

Сохранить существующие **незакоммиченные** изменения из `docs/implementation/p0-bosses-327`: Peaceful Повелителя и UUID guard Постеня; `git status` прежде всего. Запуск Minecraft/PolyMC/server запрещён текущим `AGENTS.md` — не выдавать 12077 offline checks за runtime.

- **P0-1:** три модели и оригинальные 3D-трофеи — реальные approved silhouette по #256/#266/#317/#318/#302/#321. Без физического PNG нельзя утверждать полное совпадение, но можно сделать независимо проверяемые части.
- **P0-2:** **полностью закончить 2.0.4** по #289→#290→#291→#292→#301→#293/#303→#302→#294. На `hell@0fcbaf6` только 7 вспомогательных `losthalls` классов; нет полноценного биома/entities/Posten renderer. Не перепутать SavedData с зарегистрированным боссом.
- **P0-3:** Peaceful, bossbar, награды, 4 разные головы, сохранения, old-world IDs, MP по требованиям. Полный worldgen 2.0.5 — отдельная зависимость, без ложного DONE.
- **P0-4:** для каждой Issue доказательная матрица, перечень реализованных/непроверенных требований, список потенциальных вопросов следующего исполнителя **с ответами из точных ссылок**. Два прохода по всей матрице, не повторять уже утверждённые вопросы владельцу.
- Если локальный оригинал появился, сравнить его SHA с матрицей **до** арт-приёмки. Если нет — не останавливать весь проект с ответом «нет архивов», не выдавать генерацию суррогата за утверждённый оригинал.

**Этот каталог — реальные GitHub-данные о референсах, НЕ сами референсы. Бинарное перемещение из ChatGPT Library при текущих полномочиях не удалось; эту техническую границу нельзя скрывать.**
