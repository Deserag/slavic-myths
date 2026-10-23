# #226 — корректный block entity contract, 23.10.2026

Обнаружено реальным NORMAL прогоном #225, а не предположением по исходникам. Простые FurnitureBlock больше не EntityBlock; настоящие столы/стойки/ящик сохраняют EntityBlock и старые registry types. Для multipart Storage/Coffin/Kitchen/Trophy helpers hasBlockEntity теперь совпадает с прежним null factory; только root владеет NBT. Новых registryIDs нет, инвентари/текстуры/ModelPart/UV/мир пользователя не менялись. Это общий server/client mixin, без client imports, tick polling или chunk loading в production.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Реальный диагноз worldgen warnings | ../issue225/logs/issue225-world-first.log;world-fixed.log | PASS reproduced |
|21furniture и176multipart states, реальные factory/type | runtime-multipart.log/runtime-release.log | PASS |
| NORMAL natural starts/chunks, отсутствие прежних warnings | ../issue225/world-gates.json | PASS two sampled structures |
| Отдельный процесс реального перезахода, содержимое ящика3diamonds | world-reload.log ACTUAL_PROCESS_RELOG_CRATE_PASS | PASS |
| Java21 production build/JAR, IDs/данные root сохранены | ../issue225/build-and-runtime.json;production-diff.json | PASS |
| Все биомы/все структуры/MP | вне проверенного scope, не заявлены | NOT_RUN / OUT_OF_SCOPE |
| Личная итоговая приёмка | ожидается | OWNER_PENDING |

«Тестирование», не «Готово». Issue OPEN, owner-accepted не добавлен. [Общий отчёт и точные source/JAR SHA](../issue225/REPORT.md). PolyMC не обновлялся этой итерацией.
