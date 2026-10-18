## Конкретный результат для main

Addresses #43. Один канонический integration PR поверх main `fb2994e958911169317523e1066b8a1ca7a155af`:

- #38: точный минимальный Poloz BodyMelee target-lifecycle guard; не меняет melee баланс/тайминги.
- #37: точный GuardianClient.cube без временных face/normal arrays. 10000 extracted-production emission cases PASS; прирост FPS не заявлен.
- README из #41 с честным статусом качества и единственным authoritative main.
- Сохранённые локальные классовые reports/tools, фактическая инвентаризация13 worktrees, адресная очистка с размерами/backup/командами и проверенная матрица17 прежних PR.

Production delta — только2 Java files; ресурсы/генераторы, mod_version1.9.7.5, registry IDs, баланс, art и прочие механики неизменны. Достижения V3 сохранены локально; отвергнутый55-card #40 не включён. Баланс #27 — отдельный review. #2/#11–14 устарели и имеют реальные конфликты с main.

## Проверки

Java21 `clean build` PASS;3919 JSON,1325 PNG,6804 resource и1685 Java21 class parity с production JAR PASS; test fixtures отсутствуют в JAR. 10000 pure extracted-production cube cases PASS;221 event subscribers PASS. Minecraft/clients/servers/GameTests/worlds: **0 запусков**; новая runtime/GUI/MP/FPS/MSPT приёмка NOT_RUN.

## Как это попадёт в основную версию

Review и merge этого PR в main — первым и единственным merge текущей консолидации. #37/#38/#41 отдельно предварительно merge-ить не требуется. Затем проверить post-merge main HEAD и source blob hashes; только после этого main считается обновлённым. Rollback — обычный revert через PR, без force/reset.

master26 commits behind, unique0: сохранить archive tag, затем только normal fast-forward к проверенному post-merge main после согласования. master не является второй актуальной линией. Каталоги не удалялись; cleanup выполняется отдельным согласованным действием по manifest. `.gitignore`/merge сами по себе не освобождают диск и не обновляют прочие checkout.

Документы: `docs/maintenance/issue-43/REPORT.md`, `LOCAL_CLEANUP_MANIFEST.md`, `LOCAL_SIZES_BEFORE_AFTER.csv`, `PR_MATRIX.md`, `WORKTREES.csv`, `MASTER_PLAN.md`, `GATES.json`.
