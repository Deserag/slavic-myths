Актуальный incremental checkpoint1.9.7.15/source66a19070:
обычный Волколак2 новых события/3 recorded variants; Маринка2 whisper events
активированы, LURE frame22 использует02. Source gaps12→10;458 variants/425 events.
Java21 clean build69s,6 общих+1 scoped offline gates PASS. Runtime NOT_RUN,
hearing NOT_LISTENED. Полный прежний аудит не повторялся; below full inventory
является историческим срезом1.9.7.14. Новые5 файлов: ROUTES_19715.csv.
Установка15 пока NOT_RUN; фактическая версия14 до отдельного receipt.
[Матрица](VOLKOLAK_WHISPER_19715.md).

Актуально:1.9.7.14/source69a77a70 установлен TEST_PREVIEW / OWNER_PENDING.
Java21 clean build47s;6 offline gates PASS;1359 OGG decode PASS,0 JAR mismatches.
453 recorded assignments/421 events;35 vanilla references;12 declared source gaps.
111 registry types /69 independent; scanner154 conservative unresolved joins,
154 manually classified (not proof of runtime);526 helper guards remain.
Provenance:557 recorded chains,426 hash-traced procedural,28 legacy recorded/mix
manifest declarations pending original chain,348 UNKNOWN.345 waveform candidates:
274 shared physical-prop pairs,58 inactive originals,12 legacy/mixed,1 same-character
Nightingale death across phases (intentional). Hearing NOT_LISTENED, runtime NOT_RUN.
35 frost/night events +36 new OGG; previous breath/Finist package14 OGG included.
[Матрица](FROST_NIGHT_19714.md), [routes](FROST_NIGHT_ROUTES.csv),
[доставка](../implementation/audio-60/TEST_PREVIEW_1.9.7.14.json).
Ниже исторические срезы; их totals/install status не текущие.

Последнее:1.9.7.12/source25adb2c5 build+6gates PASS, NOT_INSTALLED (PolyMC открыт).
Фактически установленная остаётся1.9.7.11. Подключены callbacks Алёши hurt и
Финиста sword contact. MANUAL_ROUTES_SUMMARY:154/154 scanner rows source-reviewed;
это не успешная слышимость154 событий.526 helper guards/UNKNOWN/hearing остаются.
[Матрица исправлений](HERO_CALLBACK_REPAIR_61.md). Ниже исторические срезы.

Актуально:1.9.7.11/source bdf72453 установлен TEST_PREVIEW, owner pending.
914 events/1309 OGG;403 recorded assignments для372 events,35 vanilla references.
Java21 clean build50s,6 offline gates PASS,0 запусков.
MANUAL_ROUTES_SUMMARY.json:111 из155 scanner rows классифицированы вручную,44 остаются.
Соловей:19 events/21variants; вдох/смех BLOCKED. Русалка/Кузя защищены.
[Текущая матрица](NIGHTINGALE_PACKAGE_61.md), [установка](../implementation/audio-60/TEST_PREVIEW_1.9.7.11.json).
Ниже исторические срезы; не текущие totals или installation status.

Текущая матрица: [GROUPS_STAGE_61.md](GROUPS_STAGE_61.md);345 событий/374 назначений,1280 production OGG. Далее — исторический срез, не текущие totals.

# #61–#66 — фактическая матрица, 19.10.2026

Новые указания владельца: #61 comment6260650720 и #60 comment6260654991.
Состояние EPIC IN_PROGRESS. Никакая строка ниже не означает owner acceptance.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| #61 сохранить уже проверенные локальные пакеты в GitHub | main5c93408e | PASS |
| #62 все реальные registry IDs классифицированы | PROJECT_ENTITY_COVERAGE.csv;111 IDs/69 самостоятельных/42aux | PASS (source enumeration) |
| #62 каждое текущее event/file учтено и декодировано | EVENT_MAP.csv / ASSETS.csv;898 events/906 OGG | PASS |
| #62 полная доказанная достижимость каждого состояния | PROJECT_EVENT_COVERAGE.csv;216 ambiguous legacy/dynamic/item routes | NOT_RUN для216; IN_PROGRESS |
| #62 license исходников новых пакетов | sources.json / packaged credits | PASS |
| #62 происхождение каждого старого OGG | SOURCE_LICENSES.csv; UNKNOWN оставлены честно | BLOCKED/UNKNOWN до трассировки, не фиктивный PASS |
| #62 сквозное акустическое сравнение | PROJECT_SIMILARITY_METHOD.json / PROJECT_SIMILARITY.csv | PASS (screening, не hearing) |
| #63 дом/лес/поле/вода: полный индивидуальный набор | roster owner_issue63; Овинник выполнен в#60, остальные группы в очереди | IN_PROGRESS; NOT_RUN для непрослушанных |
| #63 Кузя защищён, Русалка отдельно | unchanged source/quarantine; OWNER_DECISIONS.md | PASS protection; #59 BLOCKED_NEEDS_AUDIO_SOURCE |
| #64 предыдущие пять существ + коса/посох/лёд сохранены | main5c93408e / voice-rework / frost-foley | PASS (selected subset); остальной boss roster IN_PROGRESS |
| #65 все состояния шести курганных типов | KURGAN_PACKAGE.md / KURGAN_EVENT_COVERAGE.csv /69 new variants | PASS (technical); OWNER_PENDING |
| #65 остальная фауна/люди/подземелья | PROJECT_ENTITY_COVERAGE.csv owner_issue65 | IN_PROGRESS, не объявлены исправленными |
| #66 source,decode,registration,subtitles,CRC/byte parity,JAR | scoped gates + clean build38s | PASS для реализованных пакетов |
| #66 playable A/B и крупный пакет | AUDIO_ACCEPTANCE_INDEX.md; portable ZIP;6 source-timed22s demos | PASS (files) |
| #66 настоящая owner in-game приёмка всех пакетов | владелец ещё не подтвердил | NOT_RUN |
| #66 новая установка в PolyMC | текущая установка1.9.7.9 со старымSHA4fa880a3… сохранена | POLYMC_NOT_INSTALLED для нового пакета |

Очередь продолжения: точная трассировка оставшихся dynamic/item routes;
#63 household/forest/field с раздельными material/voice слоями; #64 оставшиеся
boss phases; #65 фауна и человеческие противники, затем underground. Не заменять
KEEP ради количества. Конкретные источники/условия/решения сохранять в manifests.
Игру/PolyMC/GameTest/сервер не запускать. Только владелец принимает и закрывает.
