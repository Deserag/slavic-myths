# #278 — исполнительский пакет, OWNER_PENDING / IN_PROGRESS

База `hell`: `6fa28fe9127eb154cb8b3401b321774f4db9869d`, исходное дерево чистое, origin совпадал. Main `8789e08dbcb029ae7b252049dec62c47c7780cce` не изменяется. #296 уже сохранён, его UI/relog/MP приёмка остаётся за владельцем. Поручение из прикреплённого файла требует #278 перед #284. Изолированный GameTest #278 прямо разрешён владельцем в этом чате.

## Исправлено

- `amber_charm`, `perun_charm`, `forest_charm`, `traveler_charm`: существующий единственный Curios charm slot, возможность носить со щитом. Прежний offhand остаётся дополнительным путём, один логический результат не умножает эффект одинаковых копий. Базовые коэффициенты .85/.70 и прежний legacy `charm_power` .80/.65 сохранены; набор перунитовой брони и Perun Ring не переработаны.
- `StormProtection` исполняется только на сервере.
- `FolkAccessoryItem.equipped` и пассивные charms читают фактические active stacks только своего небольшого слота. Curios 9.5.1 кеширует поиск по item на gameTime; первый GameTest доказал несвежий результат при замене внутри одного тика. Устранены кеш поиска и учёт выключенных/чужих слотов без глобального обхода сущностей и без нового tick handler.
- Четыре tooltip, две страницы книги и описание legacy-навыка RU/EN объясняют настоящий слот. `hunter_charm` остаётся активным устройством поиска по ПКМ, CD400: его ложное описание offhand +0.5 урона заменено фактическим назначением.
- Scoped `accessories278_resources.py` воспроизводит изменённые строки/tag; подключён последним в maintained generator. Полный исторический ресурсный/аудиогенератор не запускался. Art, loot, recipes, IDs, число слотов, броня и сюжетные условия не изменены.

## До реализации: требование и путь игрока

| REQUIREMENT | REAL_SOURCE | PLAYER_PATH | TEST | EXPECTED |
|---|---|---|---|---|
| Реестр33+4 | пять Curios tags, ModItems, зарегистрированные accessory classes; CATALOG.md/catalog.json | Штатный рецепт/награда → заявленный slot → действие/эффект → снять | current audit; native slot validators | 37 реальных IDs, пять типов/шесть мест |
| Пассивные charms со щитом | StormProtection; PassiveCharm; Amber/Perun/SimpleCharm | Ритуал/крафт → charm slot, щит offhand → соответствующий урон | GameTest native Player.hurt | .85 или .70 один раз; снятие убирает защиту |
| Повтор/замена/два кольца | actual active slot stacks; FolkAccessoryItem | Поменять/снять в том же тике; два Perun Ring | native damage, same-tick operations | без старого кеша и удвоения одинакового ring |
| Старые данные/слоты | неизменные registry и components; Curios slots/entities | Старый мир → носить → save/rejoin | source topology; native Curios NBT, owner relog | старые IDs читаются, настоящий relog ещё NOT_RUN |
| Книга и подсказки | RU/EN8 keys | Прочитать → найти слот → надеть | source/package exact strings | слот и активный hunter описаны по коду |

Полный поимённый реестр: [CATALOG.md](CATALOG.md), машинные ссылки на рецепты/loot и фактические source locations: [catalog.json](catalog.json). Этот реестр не объявляет старые эффекты проверенными в выживании. `ancient_fibula`/`ancient_beads` остаются археологическими plain Item; носимость/новые бонусы требуют решения владельца.

## Проверки и их границы

GameTest сервер запускается только `-Paccessories278Check runAccessories278Checks` в новом `D:/slavic-myths/run-accessories278-20261025`. Test classes/templates не входят в production JAR. Вспомогательные ServerPlayer имеют embedded connection с подавленной отправкой пакетов: это реальный серверный damage/Curios, **не сетевой MP и не игрок в Survival**.

Три реальных GameTest запуска: run1 **2 PASS/4 FAIL**, run2 **6/6 PASS**, run3 **7/7 PASS**. Первый запуск выявил stale cache и ошибку legacy-fixture; ошибки не скрыты. Run3 добавляет active/inactive, неподходящие слоты и native Curios NBT. NBT roundtrip не считается реальным перезаходом. Death test реально вызывает `kill`, затем вручную публикует Clone; настоящий respawn/rejoin не проверен.

Сохраняются сырые [run1](gametest-run1.log), [run2](gametest-run2.log), [run3](gametest-run3.log). В run3:

- `all37NativeSlotValidatorsAndCapacity`: загруженные registry/tags, Curios validator/canEquip и серверная установка/снятие всех37; шесть физических мест. Это не перетаскивание мышью в GUI.
- `amberActualDamageShieldDuplicateAndUnequip`: реальный fire hurt, щит, две копии, старый offhand, снятие и неверный тип урона.
- `perunAndTravelerActualDamage`: lightning/fall, замена и снятие.
- `forestActualTaggedAttackerAndTwoPlayers`: тегированный Леший/обычная корова, два независимых серверных игрока; без настоящего соединения клиентов.
- `talentAndEquipmentRegression`: прежняя legacy-ветка навыка, Perun Ring, два одинаковых кольца, снятие. Fixture специально восстанавливает legacy Skills после тика; это не доказательство доступности старого навыка в классах2.0.
- `unequipAndDeathDoNotLeaveCharmModifier`: смерть и ручной Clone, у нового пустого игрока нет защиты.
- `inactiveSlotsAndSerializedInventory`: disabled charm/ring, возврат активности, Curios saveInventory/loadInventory, запрет неверных слотов и активного hunter.

Java21 clean build, offline rules, source/resource/JAR результаты и точный SHA сохраняются в receipt.json/source-jar.json/current-resources.json после финальных gates. Предыдущие параллельные Gradle попытки пересеклись и дали неполный JAR; следующий cached build также был отвергнут. Затем сборка повторяется последовательно с `--no-build-cache`. Неудачный bare-JVM item fixture упирался в frozen registry; создание Item вынесено в настоящий GameTest, offline оставлен для production rules. Ни одна из этих неудачных попыток не засчитывается в финальный PASS.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| 37 source IDs, фактические handlers/acquisition index | catalog.json, CATALOG.md | PASS source audit |
| Native серверные validators и установка всех37 | gametest-run3.log all37NativeSlotValidatorsAndCapacity | PASS server API; GUI NOT_RUN |
| Damage четырёх passive charms/замена/remove/duplicates | run3 соответствующие4 damage tests + inactive test | PASS server engine |
| Perun Ring/две копии/снятие | run3 talentAndEquipmentRegression | PASS server engine |
| Остальные32 эффекта, естественное получение/шансы | только исходники, ручной checklist | NOT_RUN gameplay |
| Native Curios NBT | run3 inactiveSlotsAndSerializedInventory | PASS codec only |
| Настоящий relog/respawn/dimension change/сетевой MP/GUI | не запускалось с клиентом | NOT_RUN owner |
| Java21 clean build/offline/resources/JAR | receipt.json + сохранённые logs | смотреть точный receipt |
| PolyMC установка | не разрешена/не выполнялась | POLYMC_NOT_INSTALLED |
| Приёмка владельца | отсутствует | NOT_RUN / OWNER_PENDING |

## Как проверить в Minecraft (владелец)

Точный JAR/SHA — receipt.json; **автоматической установки не было**. Использовать отдельный тестовый инстанс1.21.1 + NeoForge21.1.255 + Curios9.5.1, новый мир; для старого мира только отдельную копию. Перед ручной установкой сохранить прежний mods/JAR и убедиться, что slavicmyths JAR ровно один. Основные миры Codex не открывал.

1. **Получить без читов.** Янтарный: на прежнем алтаре ритуал `warding_charm → amber → juniper_blend`; Перунов: `linen_cloth → thunder_stone → ancient_sign`. Лесной: крафт `herb_pouch + 2 linen_thread + leshy_heart + linen_cloth` по форме рецепта; путника: `fern_flower + 2 linen_thread + ancient_coin + birch_bark`. Для остальных37 использовать конкретный recipe/reward/source index в CATALOG; недоступную цепочку записать FAIL, не заменять /give.
2. **Щит и слот.** Открыть Curios, надеть каждый из четырёх в единственный «оберег», взять щит в левую руку. Ожидается успешная установка, щит остаётся; остальные head/ring/belt/necklace эти четыре не принимают. `hunter_charm` сюда не надевается, работает по ПКМ с20с cooldown.
3. **Защита.** В одинаковой броне сравнить огонь с янтарным/без него, падение с одной и той же безопасной высоты с путником/без него, удар природного Лешего с лесным/без него, молнию в грозу с Перуновым/без него. Ожидаются исходные −15%/−30% до остальных общих модификаторов; чужой тип урона не уменьшается. Щитом не блокировать измеряемый удар. Не делать опасный тест на основном персонаже.
4. **Негативный сценарий/повтор.** Имея две штатно добытые копии, поместить одинаковую в charm и offhand: дополнительного удвоения нет. Снять обе — защита прекращается. Быстро заменить янтарный на путника: огонь теряет защиту, fall получает. Предметы не исчезают и не размножаются.
5. **Кольца.** Надеть штатные Perun Ring и Resin Ring в два ring-места: отдельные эффекты сохраняются. Две копии Perun не дают два lightning multipliers. Снять оба — эффект отсутствует. Не смешивать это с коэффициентами четырёх charms.
6. **Сохранение.** С charm и двумя кольцами выйти полностью из мира/игры и снова зайти в копию; предметы, количество и эффекты сохраняются. Повторить после смены измерения. Отдельно проверить смерть по текущему keepInventory, подбор и повторное надевание; без дубликатов и защиты после утраты предмета.
7. **MP.** Два настоящих клиента: только первый надевает charm, соответствующий урон снижается только у него; снять/передать второму, сменить измерение, перезайти. Проверить отображение слотов и предметов обоим игрокам.
8. **Остальные33.** Поимённо использовать CATALOG: штатно получить → надеть в указанный slot → вызвать именно описанное условие → увидеть эффект/CD → снять/заменить → повторить. Особенно cap invisibility, pool swim modifier, underground belt armor/knockback, healing/full food, harvest/rune exclusion, смерть с retribution, вызовы Grey Pack/Mirror/Black Wing и их cooldown. Эти эффекты целиком не объявлены GameTest PASS.

Не проверено: GUI мышью и клиентский render, natural Survival acquisition, все отдельные способности33, настоящий relog/respawn, реальная смена измерения, сетевой MP. Риски соседних функций: общий FolkAccessoryItem getter используется рендерами, Frost Mirror/Black Wing и защитными accessories; он теперь строго учитывает declared active slot. Ручные проверки этих путей обязательны.

## Риски/откат и следующий этап

Уникальные ключи/артефакты в случайный loot не добавлялись. Нет новых save fields/registry IDs/баланса/art. Для отката вернуть прежний JAR **в тестовом инстансе** из собственного backup, не менять worlds. После сохранения технических результатов #278 — разрешённый аудит #284; #276/#286/#287 только оценка/предложение. Issues остаются OPEN/«В работе», Project поле не изменялось; это не итоговая сдача всей цепочки.
