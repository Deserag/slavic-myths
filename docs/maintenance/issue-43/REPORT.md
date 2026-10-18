# Issue43 — конкретная интеграция main и безопасная очистка

## Итог

Подготовлен один integration PR `codex/main-consolidation-43 → main`, base fb2994e958911169317523e1066b8a1ca7a155af.
Новый worktree не создавался: переиспользован чистый D:/slavic-myths-advancements; прежняя PR25 branch сохранена.
GitHub main пока не обновлён: требуется review/merge. master пока не менялся. Никаких удалений, перемещений, gc/prune/reset/force-push или запусков Minecraft.

13 worktrees занимали 23.559 GiB; основная папка 9.106 GiB.
SAFE55 каталогов: 2.218 GiB. Дополнительно 1.441 GiB можно перенести с D на C полным архивированием старого clone.
Освобождение D при этих действиях — 3.019 GiB; C SAFE — 0.640 GiB. Реально удалено0 bytes.
Полные backup zip на C занимают 4.177 GiB: архивация — отдельные затраты, не free-space claim.

## Что реально включено относительно main

- Production: только `src/main/java/org/slavicmyths/poloz/PolozEvents.java` из PR38 и `src/main/java/org/slavicmyths/client/GuardianClient.java` из PR37.
- Guard проверяет фактическую target instance и alive перед обращением; остальной melee path/тайминги/урон сохранены.
- Cube сохраняет24 вершины, UV/normals/цвет/light/overlay и порядок, удаляет собственные временные массивы. Не доказательство роста FPS.
- README #41 и точный план recovery вместо пустого обещания уборки.
-16 локально изменённых файлов основного checkout сохранены в ZIP;13 local-only classes3 reports/tools включены в integration, три status docs объединены вручную без перезаписи пользовательских файлов.
- Три актуальных status docs обновлены; инвентаризация/manifest/матрица всех17 прежних PR и61 refs с ahead/behind/common ancestor.
- Версия1.9.7.5, Minecraft1.21.1, NeoForge21.1.255, Java21; production resources/generators/registry IDs/баланс/модели/звук не менялись. Issue42 не решена этой интеграцией.

## Проверки

Java21 clean build PASS (BUILD.log); предварительный dry task graph не содержит Minecraft runs.
3919 JSON,1325 PNG,6804 resource и1685 Java21 class parity с production JAR PASS.
10000 source-extracted cube parity cases PASS: pure stubs, не native rendering/GL/FPS.221 subscribers PASS.
JAR `build/libs/slavicmyths-1.9.7.5.jar`, SHA256 aee4b68b26010cdef3f9a67b7a35f3c0d53178195d01f77752a18111b60e49ca.
Нет test/verify/smoke/audit classes в production. Runtime/GUI/MP/FPS/MSPT NOT_RUN; Minecraft0 запусков.
Исторические #37/#38 logs находятся в prior-evidence, их launches не считаются новыми запусками.

Shared Git и независимый publish clone: fsck --full exit0, reachable corruption не выявлена. Сохранены raw Git metadata/reflogs/orphans; обслуживания storage не было.
Все9 грязных рабочих копий защищены архивами с проверкой file SHA256. Большие ignored worlds/audio/JFR не объявлены мусором; остаются KEEP/owner decision.

## Что merge-ить и в каком порядке

[PR_MATRIX.md](PR_MATRIX.md): один текущий integration PR включает нужные части #38/#37/#41; их отдельный merge сначала не требуется.
После review merge этого PR → проверить post-merge main HEAD и production blobs → main источник истины.
Затем, отдельно: reviewed V3 на обновлённый main (включает зависимость #40/#25), баланс #27 отдельно.55-card #40/#31 не подменяют V3.
#11–14/#2 — superseded current main и реальные конфликты; #21 implemented; #24 требует согласования отчётов; #32/#36/#39 сохранены как исследования без production fixes.
Ни один чужой PR не закрывался, не переписывался и не merge-ился автоматически.
Rollback принятого PR — обычный revert через PR, без force/reset.

## master и финальные решения

[MASTER_PLAN.md](MASTER_PLAN.md): master26 behind/0 unique, предок main. После принятия integration сохранить archive tag старой вершины и только normal FF master на проверенный post-merge main; не поддерживать вторую актуальную линию.
Требуют окончательного согласования: merge integration PR; применение точных SAFE/ARCHIVE путей manifest; archive tag + FF master.
Не требуют диагностики от владельца: все размеры, статусы, backup, конфликты и команды уже подготовлены.

[LOCAL_CLEANUP_MANIFEST.md](LOCAL_CLEANUP_MANIFEST.md) · [точные пути/команды/bytes CSV](LOCAL_SIZES_BEFORE_AFTER.csv) · [WORKTREES.csv](WORKTREES.csv) · [GATES.json](GATES.json).
.gitignore не освобождает диск. PR/merge не обновляет другие checkout. Ни local main checkout, ни GitHub main не объявляются обновлёнными до фактического принятия и сверки SHA.
