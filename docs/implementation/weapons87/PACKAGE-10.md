# Пакет10 — #163, синхронизация натяжения трёх луков

22.10.2026. #163 и #87 IN_PROGRESS/OPEN. Это завершённый технический bow timing пакет, не вся Issue163: charge/shot/reload/burst/fan/modes/MP арбалетов ещё подлежат отдельной проверке. Нельзя переводить всю163 в Тестирование на основании этого отчёта.

Причина: installed Minecraft1.21.1 ItemInHandRenderer BOW делит время на20, но реальная серверная мощность short_bow использует12, heavy_bow и finist_bow30. Предикаты PNG уже используют правильную мощность; камера отстаёт от короткого лука и опережает тяжёлый/Финиста. На12 тиках short camera=.52 вместо1; на20 heavy/Finist camera=1 вместо.5925926.

Добавлен только production `client/BowDrawClient.java`: официальный IClientItemExtensions.applyForgeHandTransform зарегистрирован на3 конкретных существующих предмета. Во время фактического использования именно этого ItemStack, при положительном remaining и совпавшей физической руке — та же vanilla BOW pose, тот же equip offset, оси/дрожание/stretch; visual divisor соответствует существующему drawTicks12/30 либо FinistBow.power30. Тот же partialTick−1 convention vanilla; отрицательный начальный прогресс ограничен0, как серверная power. Во всех остальных случаях hook возвращаетfalse и остаются vanilla idle/swing/equip. Перед hook сохраняются vanilla camera bob/view transforms; hand bob не отменяется глобально.

Нет пакетов сети, world polling, новых звуков, изменений стрел/урона/скорости/боеприпасов/сроков/registry/save. Third-person BOW_AND_ARROW и drawing PNG predicates прежние. Зарегистрированных ранее IClientItemExtensions на эти3 item нет; существующие armor/flail extensions не изменяются.

`before-ranged-timing.zip` фиксирует реальные модели/PNG всех5 ranged ID, серверные классы и существующие client paths. Все эти файлы побайтно сохранены. Vanilla BOW источник прочитан из установленного neoforge21.1.255 sources JAR (см. NATIVE-API-EVIDENCE), не из воспоминаний или устаревшего API. `weapons87_bow_review.py` строит source-storyboard из actual pulling моделей/UV, `Weapons87Bows.java` использует настоящие Item factories и native PoseStack/ItemTransform. Мир/сущность/клиент не создавались.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Подтверждённая причина и адресный visual fix | installed BOW /20 vs FieldBow12/30,Finist30; BowDrawClient | PASS |
| Реальная server power и visual power совпадают |606 comparisons actual item methods, ticks0..100, both hands | PASS |
| Только timing, vanilla pose сохранена |120 независимых matrix comparisons старой vanilla ветки и новой функции при divisor20 | PASS |
| Обе физические руки/equip/натяжение/overhold |240 native before/after transforms,6 actual model display inputs | PASS |
| Контрольные source-ракурсы actual PNG/model/UV |3 storyboard PNG,72 visible FP source cases,3 power graphs;18 pulling display inputs совпадают | PASS |
| ID/арт/урон/ammo/старые handlers неизменны | ranged163-bows/preserved-files.json, baseline68 snapshots | PASS |
| Java21 clean build + applicable offline fixtures | package10-build.log; test NO-SOURCE | PASS |
| Ресурсы/production JAR/generators | package10-gates.log/receipt10 | PASS |
| Игровой вид/переключение рук/MP/реальные выстрелы луков | запусков0, владелец | NOT_RUN / OWNER_PENDING |
| Полная исполнительская charge/shot/reload/modes/burst/fan часть crossbows163 | пакет3 даёт6 source states/руки; полный timing/feedback аудит ещё не выполнен | NOT_RUN / EXECUTOR_REMAINING |
| PolyMC | промежуточная цепочка, установка не поручена | NOT_RUN / POLYMC_NOT_INSTALLED |

Source-projections не являются runtime. Snapshot предмета/числовой тест не выдаётся за проверку сетевой синхронизации. Арбалетный denominator +10 не удалялся: SerpentCrossbow getUseDuration добавляет10, onUseTick/release передают left+10, что с виртуальным getUseDuration влияет на реальный charge; дальнейший разбор должен сохранить серверный срок, не «исправить» его вслепую.

Точные промежуточные JAR/version/SHA/bytes и uncommitted source manifest — package10-receipt.json. Без commit/push/PolyMC/игровых запусков. Owner: короткий лук12t, тяжёлый/Финист30t, ранний/полный/долгий натяг; обе главные руки и offhand, смена предмета/отмена use/экипировка; посмотреть движение тетивы и камеры, расход обычных/магических стрел без изменения правил. Остальные части87 продолжаются.
