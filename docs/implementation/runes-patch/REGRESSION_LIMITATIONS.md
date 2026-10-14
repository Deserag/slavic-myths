# Смежные offline FAIL — без изменения чужих патчей

## RewardModelChecks

Команда: Gradle с `-I tools/rewards-offline.gradle`, задача `rewardsOfflineChecks`.

Fixture: `tools/rewards-offline/RewardModelChecks.java:10`. Три native baked mesh дают количества кубов **8 / 8 / 7**. Координаты проверенных кубов finite, у каждого mesh есть геометрия; затем fixture требует `silhouettes.size()==3`, где в set складываются только **числа кубов**. Поэтому набор падает с `Invalid baked reward mesh`.

Число кубов не доказывает одинаковость силуэтов или наличие дефекта. `src/main/java/org/slavicmyths/client/RewardArmor.java` и fixture не изменены относительно стартового HEAD; патч рун не редактирует worn geometry. Этот FAIL нельзя выдавать за PASS, но он также не доказывает регрессию от рун. Существующий новый аудит геометрии брони не подменяет запуск этого набора. Модель или критерий не менялись ради зелёного результата.

## GorynychChecks

Команда: Gradle с `-I tools/gorynych-offline.gradle`, задача `gorynychOfflineChecks`.

Fixture: `tools/gorynych-tests/GorynychChecks.java:18` требует `HP==3600`, `TRANSFORM_HP==1800`, `FLOOR==180`. При старте задачи рабочее дерево уже содержало чужой баланс **2800 / 1500 / 75**, зафиксированный в PROJECT_STATUS и исходных правилах.

Fixture неизменён относительно HEAD, `GorynychRules.java` совпадает побайтно со стартовым baseline патча рун. Сбой воспроизводит несовместимость исторической абсолютной проверки с уже принятым балансом существ. Руна смерти использует новые теги/common guard, а сюжетная выдача отличается только новым provenance полем для будущих creative receipts. Возвращать старые HP или переписывать чужой fixture в рамках этого задания нельзя.

Сопутствующие Earth/Frost/Poloz offline наборы прошли. Логи: `RELATED_REGRESSION.log`, `REMAINING_REGRESSION.log`. Ни один набор не запускал Minecraft, клиент, сервер, GameTest или PolyMC.
