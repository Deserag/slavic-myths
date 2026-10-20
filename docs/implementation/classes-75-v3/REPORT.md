# #75 — уточнения по скриншотам владельца, 20.10.2026, IN_PROGRESS

Владелец отметил, что остальная V2 нравится; замечания касаются дублей подсказок,
избыточного hover и непонятных выгод направлений. Это частичный положительный отзыв,
не автоматическая приёмка всей Issue. Никакие другие Issues в работу не брались.

## Что исправлено

1. Навигационные кнопки больше не повторяют своё название в tooltip. Причина дубля:
   ClassTheme.WoodButton создавал native Tooltip, а ClassScreen рисовал второй вручную.
   В ClassScreen native tooltip отключён, ручные подсказки только явно заданные;
   фокус кнопки без наведения их не вызывает. Общий ClassTheme для других экранов не менялся.
2. Hover узла/навыка в picker показывает только название. Описание, rank, параметры
   и условия остаются в карточке по клику. Текст с переносами, где он нужен, рисуется
   через font.split и ограничение ширины, а не одной строкой с LF-глифами за экраном.
3. Кнопки выбора направления не выводят всплывающие копии названия и требований.
   Требуемый и текущий уровни постоянно видны под заголовком карточки; недостаточный
   уровень подсвечен. Нет необходимости ловить широкую подсказку поверх соседнего выбора.
4. Для всех18 реальных First/Second BRANCHES добавлены RU/EN: игровая роль и точная
   выгода (с процентом из branch.multiplier), усиливаемый навык, доступные для отдельного
   изучения эволюции с prerequisites/уровнем/очком, дальнейшие ветви / второй пассивный
   слот / будущее мастерство. Начисление нового навыка автоматически не обещается.
   Например, Таран щитом усиливает knockback/control, но его постоянный урон2 не растёт;
   Воевода продлевает Второе дыхание, а не обещает несуществующий новый эффект.
   Основание: ClassDefinitions, ClassBalance, ClassRuntime, ClassEvents. [Все18](branches.json).
   Два длинных описания прокручиваются независимо под мышью.
5. Одобренное полотно дерева, выбор/карточка, zoom/pan/fit, quickbar, назначение/бинды
   и мастерство сохранены; серверные определения/правила/баланс/формат данных не менялись.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Воспроизведение прежних замечаний | Реальные crop-снимки владельца owner-before-1/2/3.png | PASS (owner observation) |
| Убрать дубли вкладок/кнопок выбора | ClassScreen single tooltip path, no default label tips | PASS (code), IN_GAME_NOT_RUN |
| Hover только название, детали по клику | ClassScreen render + preserved skillText/card | PASS (code), IN_GAME_NOT_RUN |
| Понятные выгоды всех направлений | branches.json:18 real IDs, RU/EN + runtime audit | PASS (copy/code), IN_GAME_NOT_RUN |
| Сохранить одобренные части и authority | Viewport/State/Network/Definitions/Mastery/Client не менялись в V3 | PASS (source audit) |
| Java21 clean build, ресурсы, production JAR | FINAL_BUILD.txt, checks.json | PASS (offline) |
| PolyMC installed SHA/version/backup/source/one JAR | INSTALLATION.json после установки | PASS |
| Реальная читаемость, клики, XP/slots/MP/relog и ручная приёмка исправления | Запусков Codex0; владельцу проверить | NOT_RUN |

В этой поставке нет новых «после» скриншотов Minecraft: IN_GAME_NOT_RUN. Макеты не выдаются
за игровое доказательство. Старые миры и профиль1.16.5 не изменялись. Issue OPEN/IN_PROGRESS.
Автоматического commit/push и закрытия Issue нет.

## Установка тестовой версии

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.23.jar`.
Version1.9.7.23, SHA256 `3025394692093dcfdc491a07a0528c9364eb1d70c3c1aa1de6378fd20dbc5174`.
Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19723-20261020-082252`. Один активный JAR проверен по mod metadata.
Source base commit: `8730595d09f84386d2556a714d7f8126649f3863` + незакоммиченные изменения;
точный source ZIP: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19723-20261020-082252\source-snapshot.zip`, SHA256 `60952bfbd1a76057ddf3dd977f8952ed36d7843c7cb1272e8db9a1d90cdcd560`;
working-tree.patch рядом. [Квитанция](INSTALLATION.json). Это INSTALLED_TEST_PREVIEW.
Minecraft/PolyMC/GameTest/сервер Codex не запускал, миров не менял.
