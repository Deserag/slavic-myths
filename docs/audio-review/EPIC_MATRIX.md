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
