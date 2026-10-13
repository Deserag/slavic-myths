# Оберег двух зим 1.9.7 — фактическая проверка

Микропатч архива Slavic_Myths_1.9.7_CODEX_PROMPT_TZ.zip для Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21.

Изменены: пакет winters (один предмет, точные рецепты, серверные квитанции, два режима, общий persisted кулдаун, щит, аура); регистрация в SlavicMyths и «Магии»; WintersClient (tooltip/property), узкий Inventory.setChanged mixin и запрет автоматического Crafter для двух удостоверяемых рецептов; три модели/иконки, RU/EN, два entity type тега. Подробнее: [TWO_WINTERS_1.9.7.md](../../TWO_WINTERS_1.9.7.md).

Механики/награды и production bytecode Морозко, Карачуна, Горыныча и Полоза сверены побайтно с установленной 1.9.6. Существующие registry IDs и старые ресурсы сохранены; изменённые прежние ресурсы: slavicmyths.equipment.mixins.json, assets/slavicmyths/lang/en_us.json, assets/slavicmyths/lang/ru_ru.json.

Чистый gradlew.bat clean build с десятью offline наборами: **PASS**. Ресурсные/JAR gates: **PASS**, 10991 проверок; event subscribers: 208. Генератор новых ресурсов воспроизводит их побайтно. В production нет тестовых классов.

WINTERS_OFFLINE_PASS checks=4853; recipe/remaining-items/native metadata, receipt NBT, shield arithmetic and aura boundaries; no client/server launch or live inventory test
GORYNYCH_OFFLINE_PASS checks=8754 nativeUvVertices=7008; no game/server launches; NBT roundtrip is not relog
HILL_OFFLINE_PASS checks=1504 solidVoxels=650174 reachable=8175 rooms=15; static cell graph is not Minecraft pathfinding or a game launch
GUARDIAN_OFFLINE_PASS checks=6439 nativeUvVertices=5064; no game/server launches; NBT roundtrip is not relog
SANCTUARY_OFFLINE_PASS checks=1455 nativeTiles=61 markers=13 authoredBlocks=90484; no game launch; NBT is not relog; synthetic terrain is not seed QA
NIGHT_OFFLINE_PASS checks=40 simulations=10000 nativeUvVertices=8352; no game launches; NBT is not relog
FROST_OFFLINE_PASS checks=6946 seededTrials=1000; nativeModels=1; clientStarts=0 serverStarts=0 (NBT is not relog)
QUEST185_OFFLINE_PASS checks=182; clientStarts=0 serverStarts=0; NBT roundtrip is not relog
POLOZ_OFFLINE_PASS checks=4290 nativeModels=8; no game launch
PASS 120 native block state/physics assertions. No game launches.

Новые 4853 offline-проверки используют реальные Minecraft CraftingInput/CustomRecipe/ItemStack/getRemainingItems и компоненты: точные рецепты, запрет копии как шаблона, возврат одного неизменённого оригинала, неверный тип/отсутствующий токен, сохранение компонентов, серверные квитанции NBT, суммарный щит 10 и границы 30/60/90 с. Изолированный test fixture открывает замороженный vanilla item registry только в отдельном offline JVM; этот код не входит в мод.

Production: build/libs/slavicmyths-1.9.7.jar; SHA256 0f1e76dd6bef952350a67d83da5357cc26bcc8dcb290b7eb281c8e46d2290e0e. PolyMC Testing обновлён: C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.jar; SHA256 совпадает; предыдущий JAR в C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-winters-1.9.7-20261013-192752. Побайтно сохранены 374 остальных файлов профиля, включая миры/конфигурации. Старый 1.16.5 не затронут.

Фактических запусков клиента **0**, сервера **0**, GameTest **0**. Shift-click/полный инвентарь/быстрые клики/отмена/выброс исследованы по штатному ResultSlot и контракту remaining-items, но в игре не выполнялись. Визуал/звук, реальные удары/вероятности/чужие эффекты, dedicated startup, multiplayer, смерть/перезаход/передача, MSPT не проверены. NBT roundtrip не является перезаходом. Запуск без отдельного разрешения не выполнялся.
