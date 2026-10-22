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
| Сборка и соответствие JAR фактическим ресурсам | Java21 clean CI + `--jar` | PENDING_CI |
| Доказать «реальный DPS» или прохождение прогрессии в мире | нет runtime, аналитический offline потолок из прошлого snapshot ≠ measured DPS | NOT_RUN / OWNER_PENDING |
| Пройти все требования #157 (PvP, рейды, добыча, боссы, зависимости, 68 визуальных состояний) | собственные ресурсы + owner gameplay testing | IN_PROGRESS |

Мод не запускался; `POLYMC_NOT_INSTALLED`. По #192 исторические design sheets/индивидуальные KEEP остаются, новая массовая перерисовка запрещена. Следующее действие по аудиту — измерить оставшиеся эффекты/соперников/прогрессию отдельно по исходникам и затем в игре; не объявлять #157 или #192 закрытыми.
