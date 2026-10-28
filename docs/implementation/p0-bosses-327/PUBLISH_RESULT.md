# Публикация — 28.10.2026

Прямое разрешение пользователя: после реализации отправить изменения в Git, решения без повторных вопросов фиксировать в файле.

- Production source commit: `c0dafcac9019ac55eaea0f50e4b71a4cfee5f311`.
- Удалённые документационные изменения до `origin/hell@a0c45230` безопасно объединены merge `af3af34d33a7ed2c8905e825aefd733f8046ecc2`. Никаких production изменений от merge: `git diff c0dafcac HEAD -- src/main tools` пуст.
- Source manifest сверён после merge: все файлы совпадают; JAR SHA256 `8e04ee7c70eeb5aaa328942e1f897b11694b71e7766957632f6781b250a47913`.
- Обычный push успешно завершён: `a0c45230..3d30b8b2 hell -> hell`. Следующий documentation-only commit сохраняет это подтверждение, не меняя production/JAR.
- Ветка main, release, теги, GitHub Project Status, owner-accepted и состояние Issues не изменялись. Это IN_PROGRESS, не owner acceptance.
- PolyMC новый кандидат не установлен. Старый installed JAR SHA256 `102952bb4c573fcb370c13fca2e702d19fed189d6d965d89aa26cdde677cc9ab`, ровно один matching mod JAR на момент read-only проверки. Существующий backup не перезаписывался.
- Все игровые/client/server/PolyMC/GameTest запуски0. Подробная матрица и непроверенное: ACCEPTANCE_MATRIX.md, REPORT.md. Решения исполнителя: OPEN_QUESTIONS_AND_RESOLUTIONS.md.
