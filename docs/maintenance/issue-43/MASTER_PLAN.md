> Исторический снимок до фактической очистки. Условия «не удалено», «ожидает согласования» и состояния PR ниже заменены новой инструкцией владельца. Актуальный результат: [REPORT.md](REPORT.md), последняя доставка: [PR #44](https://github.com/Deserag/slavic-myths/pull/44).

# master: один источник истины

Проверено: main fb2994e958911169317523e1066b8a1ca7a155af; master e64b24b8c2f27ed3e4f93353feec913a88d7fd9e. `main...master`:26 слева /0 справа, master предок main, unique commits0. Default branch main.

Решение: master объявляется legacy compatibility pointer, не отдельной актуальной версией. После принятия integration PR — обычный fast-forward master на **проверенный post-merge main**, предварительно сохранив старую вершину именованным archive tag. Это не требует переписывания истории.

Подготовленные действия после окончательного согласования владельцем:

1. Fetch origin и повторно убедиться, что master предок нового main; при расхождении остановиться.
2. `git tag archive/master-before-main-consolidation-20261018 e64b24b8c2f27ed3e4f93353feec913a88d7fd9e`
3. `git push origin refs/tags/archive/master-before-main-consolidation-20261018`
4. `git push origin refs/remotes/origin/main:refs/heads/master` — только обычный FF, без force.
5. Проверить ls-remote main/master: равны ожидаемому post-merge SHA; default main не менять.

Сейчас master не обновлялся и не удалялся. Полный shared Git backup, включая remote refs/reflogs, находится на C:. Если FF политика не принимается, альтернативное решение — оставить master замороженным и явно архивным, без дальнейшей разработки; не создавать вторую расходящуюся актуальную линию.

release/0.9.10-distribution имеет отдельную историю: KEEP/HOLD, не merge/rebase в main, не удалять. Отдельный архивный release, не источник текущего кода.
