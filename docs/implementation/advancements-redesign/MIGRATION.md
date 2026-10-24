# Совместимость: сохранить факты, сократить только награды на экране

**Проект миграции, не выполненная миграция миров.** Реализация и native доказательства — отдельный PR после рассмотрения дерева. Файлы игроков, миры и PolyMC в этой задаче не открываются и не меняются.

## Контракт всех прежних ID

[DECISIONS.json](DECISIONS.json) задаёт точный old→presentation-successor для всех129, [SOURCE.json](SOURCE.json) содержит все206 исходных definitions с SHA256, criteria, requirements и rewards. Это не список удаления. Каждый прежний ID остаётся загружаемым, в том же namespace, с прежними criterion keys/predicates, requirements и rewards; incomplete progress и timestamps не пересчитываются. Parent/display могут измениться только по утверждённому TREE/DECISIONS. Ни один successor не выдаётся за сам факт MERGE.

| Политика | Число | Изменение | Старый игрок |
|---|---:|---|---|
| M0 | 44 | Оставить display по TREE, обновить parent/текст/рамку; criteria/rewards те же | Старое done/partial сохранено; завершённое не переиздаётся |
| M1 | 85 | Убрать **поле display целиком**, оставить ID/criteria/requirements/rewards; старый parent допустим | Progress продолжает читаться и пополняться; нет карточки, toast/chat. Сохраняет старые recipe/XP награды, если они были |
| M2 | 77 technical | Никаких изменений: namespace/path/parent minecraft:recipes/root/criteria/rewards побайтно | Книга рецептов, partial progress и already granted recipes сохранены |
| M3 | 11 new | Новые ID только в namespace мода, empty rewards; import только подтверждённых персональных фактов | Исторический import без award/reward/chat/toast; где факта нет, не выдумывать |

M1 не превращает `hidden=true`: hidden с display всё ещё может показываться после completion и присылать toast. Только отсутствие display даёт data-only. Старые data-only parent chains не удаляются; все новые **видимые** parent chains ссылаются исключительно на TREE. Vanilla JSON `minecraft:husbandry/*` не редактировать: переставляются/скрываются лишь собственные узлы мода (`full_yard`, linen_craft, garden_harvest и др.). Техрецепты остаются в vanilla recipes tab.

**Визуальное объединение не является логической эквивалентностью.** meet_leshy/leshy_heart не дают defeat_leshy; restored_sanctuary_fire не даёт claimed_fire_fragment; сундук не даёт clear_large_camp; inventory подарок не даёт boss victory; friend_domovoy не даёт bind_hearth. Они остаются отдельными фактами в прежних holders и в Книге. Для каждой строки old-save policy явно M0/M1, successor лишь объясняет, куда ушла визуальная тема.

## Проверка по установленному Minecraft 1.21.1 / NeoForge21.1.255

Исследованы **установленные исходники**, а не API старого Forge: Gradle NeoForm `sourcesAndCompiledWithNeoForge_414bd7305056e2232c1ba590755f2e3084f76e3b_output.jar`, Java21 build этого worktree. Верификатор документации сохраняет hash этого источника и ключевые факты в VERIFICATION.json (при наличии локального source JAR).

- `net.minecraft.advancements.Advancement.CODEC` использует `DisplayInfo.CODEC.optionalFieldOf("display")`. Data-only определение — допустимый advancement, не missing registry entry.
- `AdvancementTree.tryInsert` требует существования parent holder, а не его display. Поэтому старые data-only родители остаются корректны.
- `PlayerAdvancements.load → applyFrom → startProgress` находит holder по ResourceLocation и обновляет сохранённый AdvancementProgress. Удалённый ID игнорировался бы с warning — именно поэтому ID не удаляются.
- `PlayerAdvancements.save → asData` сохраняет все holders с `hasProgress()`, без фильтра display. Скрытие карточки не стирает JSON прогресса.
- `PlayerAdvancements.award` выдаёт rewards лишь на переходе incomplete→done; announcement находится внутри display.ifPresent. Отсутствие display убирает chat, **но не отменяет старые rewards**. Поэтому нельзя «тихо мигрировать» массовым Knowledge.award: это всё равно выполнит rewards.
- `ClientAdvancements.update` показывает advancement toast только если `!packet.shouldReset()` и done и display.shouldShowToast. Первоначальный reset snapshot не показывает импортированные победы. Это основание предлагаемого silent import, **не уже выполненный клиентский тест**.

Этого достаточно для выбора data-only схемы в дизайне. Это статическое подтверждение API; native roundtrip, cancelled death и client receipts ещё предстоят.

## Silent import новых крупных milestones

Предпочтительный проект этапа B: единичный server-only import **в native загрузку PlayerAdvancements до applyFrom и первого reset sync**, объединяющий исходную Data map с максимум11 новыми empty-reward progresses. Обычный Knowledge.award, RewardLedger.victory и runtime reward callbacks не вызываются. Использовать установленные `AdvancementProgress.update(requirements)` и `grantProgress(criterion)` на новом объекте, затем обычную native загрузку; при необходимости узкий accessor/mixin к точке загрузки, с native tests. Не менять private поля вручную и не дописывать player JSON при работающем сервере. До доказательства правильного lifecycle hook import не принимать.

Факты персонального ledger читаются **без создания defaults/новых побед** (например, чтение maps.getOrDefault, не victory/claim/state reconciliation с побочными действиями). Допускаемые предикаты перечислены по всем11 новым ID в NEW_TREE. `kurgan_rest`, `depth_champions`, `night_watch` не импортируются, потому что в текущих данных нет личной заслуги. Новая онлайн победа выдаётся обычным award только в accepted native callback; освобождённый инвентарь или получение награды не должны быть prerequisite.

Новый import:

1. Сначала загрузить и проверить исходную Data; не менять существующие206 progresses, даты, partial или foreign namespace. Вычислить eligibility из уже сохранённых персональных фактов. Never infer по предмету, proximity, chat, recipe или общему world cleared.
2. Добавить только отсутствующие новые IDs с допустимым predicate; timestamp — время миграции с записью provenance, не выдуманная дата старой победы. Уже заполненный new progress не трогать.
3. Применить через штатную native загрузку, которая помечает visibility/progress changes для начального reset. Никаких completion hooks/rewards/chat. Не импортировать после первого reset: если ledger ещё не доступен, отложить до следующей безопасной загрузки, не стрелять online award.
4. Идемпотентность по наличию criterion, плюс версия схемы1 и provenance в изолированном mod metadata (не Knowledge int mask). Revoke административной командой не должен немедленно вновь импортироваться при reload; хранить факт выполненного импорта/осознанного отказа отдельно от done. Не ставить marker раньше успешного сохранения.
5. Если сохранение не удалось или schema неизвестна — исходные progresses неизменны, импорт не помечен завершённым, отчёт об ошибке без автоповтора наград. Native file save использует штатный путь; не создавать второй конкурирующий writer.

Первый вход не выдаёт 60+ toast: M1 без display; M0 не награждаются заново; M3 встроен в reset snapshot и не вызывает award. Это требование native/client теста, а не обещание, основанное только на коде. Fallback при неподходящем hook — **не выпускать автоматический import**, не заменять его массовым login award. Согласовать конкретную реализацию в следующем PR.

## Книга, Knowledge и rewards

Сохранить все234 tales entries, IDs, chapters, stages, Seen/Unread/Verified/Defeated, bonds, hints, delivery, quest adapters Яги и связи старых advancement ID. `TaleCatalog.advancement` продолжает индексировать legacy. `TaleService.reconcile` обязан обходить и display-less holders; фильтр display существует только в `advancements()` для графа. Старые `advancementStage` значения не повышать массово при перегруппировке. Новые победы — additive stage4 по **точному subject**, без перезаписи других полей/ложного Defeated для friendly NPC.

Не расширять `Knowledge.ENTRIES`/int mask новыми боссами; не очищать duplicate depth_gift и не менять legacy индекс в этом redesign. Не использовать mask для новых book milestones, не вызывать его при migration. Книга изучения и большие медали не требуют соответствия1:1.

Сохранить RewardLedger saveId/claims/Seen/First/Gift/Pending и все native victory counts/hero flags/quest states. Добавление achievement callback не вызывает `victory` второй раз и не меняет порядок claim/инвентарных транзакций. Новые ID имеют empty rewards. На старых M1 rewards остаются, включая возможный XP: задача сокращает уведомления, не баланс скрытых наград.

## Gates следующего implementation PR

Сохранить #25 save-contract и mutation tests. Обновление baseline только для явно разрешённых display/parent/локализаций; tests стабильности **206 IDs/criteria/requirements/rewards**, 77 technical byte parity и 234 tales не ослаблять. Явный allowlist — TREE/DECISIONS; удаление проверки для зелёного CI запрещено. Генераторы, содержащие старые JSON и localization, должны воспроизводить новое состояние без resurrection мелких display; поиск всех writer'ов обязателен.

| Test | Обязательное доказательство |
|---|---|
| T-FACT | Для каждого129: loader holder есть; прежние criteria/rewards/requirements совпадают; completed+partial JSON читается новым процессом, сохраняется снова без потерь. Для каждого vanilla trigger — positive/negative в нативной среде, OR и AND отдельно. M1 получает старый прогресс без display/toast/chat |
| T-BOSS | По каждому boss row actual native accepted lifecycle; не invented synthetic victory flag. Отмена смерти, /kill/debug, creative/spectator, projectile owner, игрок-наблюдатель, PvP, лут-подарок, failure/restart cancellation — отрицательные. Горыныч PURIFIED, Калин surrender, Морозко/Карачун completed испытания |
| T-COOP | Два реальных ServerPlayer в native fixtures: оба eligible/только один eligible/внешний last-hit/зритель; owner-only Картаус/Калин не расширяется. Затем два настоящих клиента dedicated: отдельная NOT_RUN позиция до фактического запуска |
| T-IMPORT | Смешанные старые done+partial+77 recipes+foreign IDs, исторические flags пяти story bosses/Горыныча/curse/classIV. Два запуска новых JVM, идемпотентность, админ revoke, invalid/missing data, inventory full; snapshots reset без completion side-effects/повторного loot/XP и без лавины toast |
| T-BOOK | Все234 entries и legacy связи, stages/Unread/Verified/Defeated, book delivery/bonds/Yaga quest read-only; граф55, parent не потерян, новые boss facts только своим субъектам |
| T-GUI | Vanilla + Книга: RU/EN, scales1–4, 1366×768/1920×1080, новые/старые survival копии. Скриншоты и runtime logs, completion-before-parent, pan/zoom, читаемость/соединения, нет M1, нет login toast flood |
| T-BUILD | Java21 clean build, актуальные CI #25/ресурсы/генераторы/production JAR, fixture namespaces вне JAR; число реальных запусков, провалы и исправления честно |

Краткий ручной маршрут: новая survival копия → Книга/одно ремесло → одна мирная дружба → ранняя победа → позднее испытание → restart старой копии → оба GUI/RU/EN → два клиента. Не считать меню проверкой боя, headless protocol проверкой GUI, NBT fixture настоящим client relog; MSPT/FPS измеряются отдельно при необходимости.

## Откат и принятие

До запуска реализации сохранить отдельную резервную копию тестового мира/player advancements/mod SavedData, JAR и manifest checksums. После gates сначала проверить копию; не переносить пользовательский мир ради теста. При rollback к старому JAR прежние206 IDs/criteria/rewards остаются совместимы и старый display вернётся. Новые IDs старый loader может проигнорировать и при save утратить: не обещать сохранность этих11 при downgrade; резервная копия с новой схемой нужна для возврата. Старые факты/личные ledger не изменены импортом, предметные награды не переиздаются. Rollback metadata/provenance сохраняется отдельно в backup.

Ничего автоматически не merge-ить и не устанавливать. Решение владельца касается дерева55 и правил миграции; после этого отдельный небольшой implementation PR. Если понадобятся новые co-op правила, изменения боя или расширение survival источников — отдельное согласование scope, не скрытая часть redesign.
