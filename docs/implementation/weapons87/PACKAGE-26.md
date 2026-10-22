# #87 — пакет №26 / #157 и #192: актуальный аудит 68 ID и lineage ресурсов

Дата: 22.10.2026. Базовый `main`: `c40fedd45175b5f837bfb498566174301f4c6124`. Прежняя матрица `audit-68.json` (580 KB) и исторические `applied-manifest.json` не исправляются задним числом.

**Реализован** `tools/weapons87_live_audit.py`: получает **ровно 68** weapon ID напрямую из `assets/slavicmyths/creative_catalog.tsv`, сопоставляет их с `audit-68.json` и `native-attributes-after.json`, проверяет существующие item JSON/variants, override models и slavicmyths-текстуры, строит current snapshots SHA256, source-level acquisition notes и сравнивает historic reference hashes, отдельно по 69 строкам манифеста кистеней. Пишет `build/weapons87-live-audit/current-68.json` и `current-68.csv` для просмотра без изменения исходников. После сборки `--jar` дополнительно проверяет фактически упакованные ресурсы (вся текущая коллекция моделей/PNG).

Первый источник-анализ CI обнаружил **109** исторически изменённых SHA-256 в `audit-68.json` (0 отсутствующих путей) и **51** расхождение из 69 записей старого манифеста кистеней (0 отсутствующих путей). Это drift прежних снимков относительно текущего `main`, **не** автоматически 160 дефектов игры. Не менять принятые худож. модели или переписывать старые манифесты ради зелёного PASS. Манифест кистеней используется только как честная отдельная диагностика; старый `verify_weapons87.py --generators --jar` остаётся FAIL и явно не объявляется успешно пройденным.

| REQUIREMENT | EVIDENCE | STATUS |
|---|---|---|
| Проверить 68 каталоговых ID | current catalogue + current native snapshot + архивный аудит | PASS source-only |
| Разрешить активные item models и slavicmyths текстуры | `tools/weapons87_live_audit.py`, exact active SHA256 | PASS source-only |
| Получить отчёт живого состояния без перезаписи истории | JSON+CSV GitHub Actions artifact | PASS source-only |
| Исходные, устаревшие SHA референсы и кистени | 109 + 51 recorded; исходники без изменений | DRIFT_IDENTIFIED, не «исправлено» |
| Сборка и соответствие JAR фактическим ресурсам | Java21 clean CI + `--jar` | PASS Java21 [CI 39764351561](https://github.com/Deserag/slavic-myths/actions/runs/39764351561); 294 JAR assets |
| Доказать «реальный DPS» или прохождение прогрессии в мире | нет runtime, аналитический offline потолок из прошлого snapshot ≠ measured DPS | NOT_RUN / OWNER_PENDING |
| Пройти все требования #157 (PvP, рейды, добыча, боссы, зависимости, 68 визуальных состояний) | собственные ресурсы + owner gameplay testing | IN_PROGRESS |

Мод не запускался; `POLYMC_NOT_INSTALLED`. По #192 исторические design sheets/индивидуальные KEEP остаются, новая массовая перерисовка запрещена. Следующее действие по аудиту — измерить оставшиеся эффекты/соперников/прогрессию отдельно по исходникам и затем в игре; не объявлять #157 или #192 закрытыми.

### Проверенный итог и merge в main — 22.10.2026
- PR [#198](https://github.com/Deserag/slavic-myths/pull/198) merged `4fad4fa2bd231aa07d2bb0cb5c4d68fb2e395686`, gated source HEAD `4f5a3b3c880d94261f12c0a0c246903848cd5425`.
- [CI 39764351561](https://github.com/Deserag/slavic-myths/actions/runs/39764351561) **PASS**, Java21 clean NeoForge build 1m29s, 68 weapon ids source PASS, 294 source-assets byte-verified inside JAR PASS, Thunder Axe source guards PASS.
- Production output `build/libs/slavicmyths-1.9.7.56.jar` SHA256 `406a1027e06f0d6bc30c382e471bb814b7f3624ddd5320fef96cf3b00802d7b9`; downloadable **only as CI artifact**, not installed in PolyMC, client/server NOT_RUN.
- History: `audit-68.json` **109** stale SHA entries, `blueprints/flails/applied-manifest.json` **51** stale SHA entries, no missing historical paths, old full #87 manifest verifier remains **FAIL**; reports record drift rather than changing resources. Numbers reflect source lineage, not a claim of 160 combat bugs.
- Issue #87/#157/#160/#192 **OPEN**, OWNER_PENDING. Approved A/B stats preserved; C/D/#190 blocked, #191 not started.
