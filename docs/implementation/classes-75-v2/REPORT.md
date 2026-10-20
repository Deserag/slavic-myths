# #75 V2 — промежуточный отчёт, IN_PROGRESS / OWNER_REJECTED

Прежний UI не принят владельцем. Issue остаётся OPEN. Новый контракт прочитан
из GitHub #75 и двух документов origin/main, зафиксированных в этой папке.
Никакие модели/классы/количество игровых слотов не придуманы заново.

## Точная исходная версия

На диске PolyMC установленный при отклонении JAR: 1.9.7.21, SHA256
`1e0064b015bb0f4103b5d00f04b1ee855c3346becf9180c755299703e11c9be5`.
Проверено совпадение installed SHA с квитанцией и побайтовое совпадение ClassScreen
с ZIP точных исходников этой установки. [Квитанция исходной версии](REJECTED_SOURCE.json).
Исходный Git HEAD `8730595d09f84386d2556a714d7f8126649f3863` + незакоммиченные
правки дерева и загрузки. Это не притворяется commit SHA готовой #75.
На свежем origin/main изменились лишь blueprint-документы; production Java там
остаётся списковым. Правится фактически установленная локальная версия.

## Что изменено в V2

- **A — выбор класса:** только три BASES; одинаковые карточки, реальные роли и описание,
  явное подтверждение; дерево и назначения до выбора не рисуются.
- **B — дерево:** canvas занимает всю ширину и свободную высоту после компактной навигации
  и quickbar. selected="" при новом открытии; нет инспектора по умолчанию и сайдбара.
  Узлы/линии берутся из ClassTreeLayout. Отрисовка и mouse hit используют одну камеру.
  Колесо масштабирует вокруг указателя; ЛКМ на пустоте / MMB перетаскивают;
  «Сброс» вписывает весь граф. Камера сохраняется при выборе, серверной синхронизации
  и возврате, при resize сохраняется мировой центр. Начальный масштаб не мельче65%;
  панорама открывает остальные узлы; fit может уменьшать граф, затем колесо приближает.
- **C — узел:** hover показывает tooltip, не выбирает. Клик открывает контекстный overlay,
  не меняя геометрии canvas. Карточка выбирает свободный угол, сокращает ширину по
  доступному месту около выбранного узла. Название/ранг/эффект/требования/следующий ранг
  и операции изучения/назначения — фактические. Длинное описание прокручивается.
  ESC, крестик или пустота закрывают карточку. Узлы под overlay не получают клики.
- **D — слоты/клавиши:** три активных, P и заблокированный до штатных требований P2,
  по30 GUI px; реальный KeyMapping подписан на каждом активном слоте. Клик открывает
  временный picker только изученных навыков правильного класса/типа. Из карточки можно
  открыть тот же picker и выбрать целевой слот. Смена клавиши, мышиная клавиша,
  очистка назначения/бинда, Save/Cancel; до Save ни бинды, ни назначения не меняются.
  Конфликты с vanilla/другими модами перечисляются по фактическим mappings.
  Второй пассивный слот и запрет дублирования пассивного назначения учитываются UI;
  последнее решение остаётся за ClassState. Дополнительная страница клавиш содержит
  все пять class mappings: три активных, ultimate и открытие меню.
- **E — направления:** отдельная страница с настоящими двумя First/Second ветвями,
  уровнями/усилением/выбранным этапом. Нет quickbar и дерева на этой странице.
- **F — мастерство:** отдельный ClassMasteryScreen с реальным испытанием/текущим
  будущим мастерством. Остальные мастерства показываются по запросу и только своего
  базового пути; огромный общий каталог по умолчанию убран. Исправлен отсутствовавший
  ключ предупреждения о конфликте клавиши на фактический список конфликтов.

ClassDefinitions/ClassState/ClassNetwork/ClassClient и формат сохранений неизменны.
Серверные изучение, стоимость, блокировки и ветви не обходятся новым интерфейсом.

## Матрица

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Установить source/JAR отклонённого UI | REJECTED_SOURCE.json; exact ZIP/ClassScreen match | PASS |
| A: выбор только3 реальных классов, подтверждение | ClassScreen.initChoice/initConfirmation; production code | PASS (code); IN_GAME_NOT_RUN |
| B: полное полотно, без выбора, zoom/pan/fit | ClassScreen + ClassTreeViewport; checks.json | PASS (code/geometry); IN_GAME_NOT_RUN |
| C: контекстная карточка, hover, ESC/пустота | ClassScreen + shared hit transform; checks.json | PASS (code/geometry); IN_GAME_NOT_RUN |
| D: quickbar, purchase → slot, keys/clear/save/cancel/conflicts | ClassScreen picker + unchanged ClassNetwork/State | PASS (code); IN_GAME_NOT_RUN |
| E: реальные направления отдельно | ClassScreen branches from ClassDefinitions | PASS (code); IN_GAME_NOT_RUN |
| F: отдельное мастерство/настоящие trials | ClassMasteryScreen + ClassUltimates/TrialService | PASS (code); IN_GAME_NOT_RUN |
| 1280×720 /1649×928, GUI scale2/3/4 | Actual pure camera/rules JVM: checks.json; bounds/hit/anchor/fit/card/refresh | PASS (geometry only) |
| Читаемость всех шрифтов и указатель в игре в6 сочетаниях | MANUAL_CHECKS.md | NOT_RUN |
| Реальные снимки6 состояний и видеопоследовательность | Никаких макетов вместо скриншотов; IN_GAME_NOT_RUN | NOT_RUN |
| XP/purchase/cooldown/death/relog/MP/старые миры | Rules проверены offline; полный игровой цикл не запускался | NOT_RUN |
| Java21 clean build/resources/production JAR | FINAL_BUILD.txt / checks.json | PASS (offline) |
| Ручная приёмка | Владелец пока отклонил предыдущую версию | NOT_RUN |

Это промежуточный отчёт, не итоговая приёмка. Действующее AGENTS.md запрещает запуск
Minecraft/PolyMC/GameTest/сервера: Codex запусков0. IN_GAME_NOT_RUN относится ко всем
шести рабочим состояниям. Математический тест камеры не равен проверке UX и текста в игре.
Никакие другие Issues не брались в работу. Автоматического commit/push нет.

## Установлен тестовый V2

Профиль: Slavic-Myths-1.21.1-Testing, Minecraft1.21.1/NeoForge21.1.255.
Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.22.jar`.

Version1.9.7.22; installed SHA256 `94fded1f6a1a87223a93391a03fa8ab25035eaabcc04b28451f1df93dbefcc0a` совпадает с production JAR.
Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19722-20261019-221331`.
Source base commit `8730595d09f84386d2556a714d7f8126649f3863` + незакоммиченные V2 и loading fix;
точный src/main/gradle.properties ZIP: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19722-20261019-221331\source-snapshot.zip`.
ZIP SHA256 `0ba1d8f70f84b861c7716d1150913693bad2e335917263d2611d714a7a5e2e44`; patch рядом.
Один активный slavicmyths JAR подтверждён сканированием mod metadata всех mods/*.jar.
Это INSTALLED_TEST_PREVIEW, не финальная поставка. Миры и старый1.16.5 не тронуты.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Установка/backup/source/SHA/без двойных JAR | INSTALLATION.json | PASS |
| Реальные6 состояний/видео/читаемость и поведение | MANUAL_CHECKS.md; IN_GAME_NOT_RUN | NOT_RUN |
| Ручная приёмка V2 | Пока отсутствует | NOT_RUN |
