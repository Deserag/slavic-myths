# Матрица приёмки 1.6.9

Кандидат, не финально принятый релиз. PASS означает проверку указанного условия; native ServerPlayer с тестовым соединением не заменяет настоящий клиент. Частично покрытый комбинированный сценарий помечен NOT_RUN и содержит точную границу. Ни один ручной случай не получил PASS за компиляцию.

Основное evidence: `server-final.log` (15/15), `build.log`, `resource-gates.json`, `audio-qa.json`, `content-density.json`, `regeneration.json`; реальный restart — `restart-write2.log`/`restart-read3.log` под `work/bayun` и копии здесь. Ранние FAIL исправлены, но сохранены в AUDIT. Всего 19 dedicated запусков, 0 клиентов.

| ID | Статус | Проверка | Evidence / ограничение |
|---|---|---|---|
| BK-01 | NOT_RUN | Fresh player joins brand new world | Service fixture new=1/repeat=0 PASS, настоящие 2/3 сетевых login NOT_RUN. |
| BK-02 | PASS | Full player inventory first join | Native pendingInventoryAndLegacy: полный инвентарь → pending → ровно одна выдача; повтор без дубликата. Настоящий сетевой login отдельно NOT_RUN. |
| BK-03 | NOT_RUN | Drop book in fire | Удаление книги из native inventory не вызывает повтор; настоящий огонь и item destruction NOT_RUN. |
| BK-04 | NOT_RUN | Die without keepInventory | Данные SavedData независимы от inventory; настоящая смерть/rejoin без keepInventory NOT_RUN. |
| BK-05 | NOT_RUN | Die with keepInventory | Настоящая смерть/rejoin с keepInventory NOT_RUN. |
| BK-06 | NOT_RUN | Move book chest, relog/dimension switch | Настоящий disk restart сохранения PASS, перемещение книги в сундук/клиентский relog/смена измерения NOT_RUN. |
| BK-07 | NOT_RUN | Obtain second copy through creative | Архитектура UUID-world, не NBT предмета; две реальные клиентские копии NOT_RUN. |
| BK-08 | NOT_RUN | Open in singleplayer/multiplayer | Snapshot encode/decode PASS, 487 actual recipes; реальные экраны SP/MP и freezes NOT_RUN. |
| BK-09 | NOT_RUN | Each 8 book categories / 3 nested levels | Функциональная навигация реализована; ручной проход всех кнопок NOT_RUN. |
| BK-10 | NOT_RUN | Russian at 1366×768 scale2,3,4 | Реальные 1366×768, scale2/3/4 screenshots NOT_RUN. |
| BK-11 | PASS | Search region for actual known anchor | hintAndStateRoundtrip: смещение 25–80, радиус 250, Target/Anchor не отправляются; wire codec roundtrip PASS. |
| BK-12 | PASS | Invalid/moved target | Тот же GameTest: moved target за пределами круга и истёкший срок дают Stale=true. |
| BK-13 | NOT_RUN | Open early boss rumor then defeat | Monotonic stage/no invented defeated PASS; реальная ранняя boss rumor → собственный бой с boss NOT_RUN. |
| BK-14 | PASS | All current entity IDs enumerated | CONTENT_COVERAGE.csv: все 75 actual EntityType, включая явные группы технических типов; gate проверяет владельцев. |
| BK-15 | NOT_RUN | Cross-client: player A discovers, B does not | Native independentVisitors PASS; два настоящих клиента NOT_RUN. |
| BK-16 | NOT_RUN | Missing client book textures/reload assets | Код fallback/icon protection и packed asset checks; реальный client resource reload/missing assets NOT_RUN. |
| BA-01 | NOT_RUN | Find natural Bayun area on generated seed | В GameTest площадка, опоры, отказы до записи и 2 выхода PASS; естественный spawn на обычном seed NOT_RUN. |
| BA-02 | NOT_RUN | Reload chunk / relog / restart | Настоящий dedicated restart UUID/bond/home PASS. Клиентский relog и долгое естественное выгруженное состояние NOT_RUN. |
| BA-03 | PASS | Attempt sword/fire/lava/drown/explosion | immunityAndSpectator: реальные native damage sources sword/fire/lava/drown/explosion/void не наносят урон; огонь/воздух восстановлены. |
| BA-04 | NOT_RUN | First notice from 12 blocks | Native timers/цепочка PASS; настоящее audible/visual first notice NOT_RUN. |
| BA-05 | PASS | Milk bucket gift before 8s | milkFishAndPersonalTrust: milk → empty bucket, trust25, canTalk=true; предупреждение отдельно отменяется warningGiftCancellation. |
| BA-06 | NOT_RUN | 1 raw fish gift before 8s | Рыба consumed1, trust23, safe PASS; настоящее изображение жевания NOT_RUN. |
| BA-07 | PASS | Wrong gift spam/double click | Неподходящий предмет сохраняется, trust0; повторный подход с рыбой после молока не расходует её и не повышает доверие. |
| BA-08 | NOT_RUN | No gift, escape by >15 before 3.5s | Native отход30блоков и отмена PASS; реальный клиентский контроллер NOT_RUN. |
| BA-09 | PASS | No gift stay in radius | actual pounce chain: реальная физика и близкий удар после grace/warn/song/charm; смерть survival native игрока подтверждена. |
| BA-10 | PASS | Run behind solid wall | solidWallCancels: реальная преграда при предупреждении; игрок жив. |
| BA-11 | NOT_RUN | Teleport/dimension/disconnect mid charm | Проверки same level/online/eligible + client TTL статически; реальные teleport/dimension/disconnect mid-charm NOT_RUN. |
| BA-12 | PASS | Creative/spectator approach | Creative survival switch и отдельный spectator GameTest: без смерти/участия в опасной встрече. |
| BA-13 | NOT_RUN | Player dies via another source during charm | Cancellation paths реализованы; смерть от другого источника при реальном клиентском charm NOT_RUN. |
| BA-14 | NOT_RUN | Milk/fish gift cooldown rejoin | Actual disk restart trust25 + native cooldown PASS; настоящий быстрый client rejoin NOT_RUN. |
| BA-15 | NOT_RUN | Pet with empty hand and right state | Empty-hand shift interaction trust+4 once PASS; настоящее rub animation NOT_RUN. |
| BA-16 | PASS | Player B hasn't gifted, A has | independentVisitors: A принёс молоко и жив, B не принёс и погиб от собственного encounter. Native server players; клиентский MP отдельно NOT_RUN. |
| BA-17 | NOT_RUN | Verify all 16+ clips played | 17 actual native animation timelines/joints offline PASS; in-game clips/пять кадров/переходы NOT_RUN. |
| BA-18 | NOT_RUN | Verify 19+ sound events in game | 19 actual samples mono/decode/subtitles/routing PASS; in-game attenuation/микс/прослушивание NOT_RUN. |
| BA-19 | NOT_RUN | Client GUI and server logs under stress | Измерен серверный snapshot, не stress MSPT/FPS/memory. Длительный нагрузочный опыт NOT_RUN. |
| BA-20 | NOT_RUN | Spawned anchor unloaded for hours | Production без chunk tickets/forced loading; многочасовое выгруженное состояние NOT_RUN. Тестовый forced ticket существует только в изолированном restart harness. |
| BA-21 | PASS | Gift during warn | warningGiftCancellation: подарок при warning, отмена опасности, survival жив на последующей проверке. |
| BA-22 | PASS | Physical leap misses because player dodged/cover | physicalMissDoesNotAutokill: пounce действительно начался, игрок сместился после старта, промах не превратился в удалённое убийство. |
| AD-01 | NOT_RUN | Existing save with completed advancements | Native vanilla advancement JSON и disk restart PASS; настоящий существующий пользовательский мир не открывался. |
| AD-02 | NOT_RUN | Real SlavicMyths event triggers new advancement | Actual Knowledge.award/PlayerAdvancements bridge PASS; клиентский toast NOT_RUN. |
| AD-03 | PASS | Noncyclic advancement graph | 175 JSON; ссылки существующие, граф без циклов; hidden nodes фильтруются actual vanilla progress. |
| AD-04 | NOT_RUN | Book achievements fit GUI at scale 4 | Реальное GUI scale4 NOT_RUN. |
| YG-01 | NOT_RUN | Existing Baba Yaga quests and rewards | Yaga classes/data/rewards побайтно сохранены; настоящий игровой проход старого поручения NOT_RUN. |
| YG-02 | NOT_RUN | Interact Bayun and Yaga in same world | Native readonlyBridgeAndRealGraph: чтение не создаёт Yaga Progress; проверен nonce/replay Баюна. Совместный игровой проход NPC NOT_RUN. |
| RU-01 | PASS | Validate ru_ru and en_us keys | UTF-8 JSON, паритет новых RU/EN keys, точное исправление четырёх legacy mojibake имён; новые строки без U+FFFD/CP1251 corruption. |
| RU-02 | NOT_RUN | GUI text on real screenshots | Настоящие скриншоты русского GUI NOT_RUN по запрету пользователя на автоматический client launch. |
| RE-01 | PASS | `gradlew.bat clean build` | build.log: actual clean build, Java21, production JAR; native regressions PASS. |
| RE-02 | PASS | JAR content extraction | resource-gates.json: все текущие code/resources в production JAR, тестовые classes/namespaces отсутствуют. |
| RE-03 | PASS | Existing 1.21.1 PolyMC staging | polymc-installation.json: устанавливается только после gates в прежний 1.21.1-Testing, backup вне mods; профиль 1.16.5 не затронут. |
