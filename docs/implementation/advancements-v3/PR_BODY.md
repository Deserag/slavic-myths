# Заменить дерево достижений #40 утверждённой структурой v3 из75 карточек

Заменяет отклонённую структуру PR #40, а не добавляет второе дерево. Основа: `82a0cccb569307b2992e4c82bf30667a7862fade` (#40); includes его native victory fixes и corrective subset #25, без повторного cherry-pick. Не закрывает и не объединяет #40.

Корень +7 мирных +24 исследовательских +25 боевых +18 сюжетных; полноценные линии Чуди, Яги, Баюна, Мора и всех четырёх поручений Волка. Parent влияет только на отображение. Существующие217 holders,77 recipe unlocks и234 tale records сохраняют criteria/requirements/rewards; нет historical import или принудительной выдачи родителей. Для изменившейся семантики — новые ID и server callbacks.

Проверки: Java21 clean build, graph/save/resource/generator/JAR contracts, pure JVM и mutation regressions. Minecraft не запускался:0 clients/servers/GameTests. Runtime/GUI/MP/restart/performance NOT_RUN; это кандидат для будущей игровой приёмки.

Результаты: `docs/implementation/advancements-v3/REPORT.md`, `TREE.md`, `TREE.png`, `TREE.svg`, `ACHIEVEMENTS.csv`, `COMPATIBILITY.csv`, `CHECKS.json`.

Сравнение только с исходниками #40: `82a0cccb...codex/advancements-v3`. PR не меняет баланс, модели/оружие, AI боссов, PolyMC или миры.
