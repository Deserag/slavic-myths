# Пакет12 — явно согласованный баланс B

22.10.2026 владелец ответил «Согласовать пакет B» на конкретные числа. Это согласие на B, не C/D/новый замах190. A ранее реализован и сохраняется. #158/#159/#87 остаются IN_PROGRESS: остальной полный audit/progression scope ещё не завершён.

| ID / значение | До B | После B | Сохранено |
|---|---:|---:|---|
| perunite_mace attack speed |0.6 |0.8 |damage9.5,pressure.4,tier,reach,durability850 |
| perunite_mace аналитический raw full-charge DPS ceiling |5.7 |7.6 |не игровой замер; crit/enchant/armor/промахи не включены |
| storm_staff базовый magic pulse |3 |6 |range7,одна допустимая цель,ray visibility,dot>.93,тип indirectMagic |
| storm_staff cooldown |80t |60t |старые условия обычного/рунического cooldown |
| storm_staff active midday/shadow rune cooldown |72t |54t |day/dark условия прежние |
| staff_power multiplier |1.12 |1.12 |не усиливался дополнительно |
| thunder rune multiplier |1.15 |1.15 |не усиливался дополнительно |

Изменены ровно4 production-файла относительно frozen source package11: `registry/ModItems.java`, `item/StormStaffItem.java`, `lang/ru_ru.json`, `lang/en_us.json`. В регистрации булавы единственный token delta .6F→.8F. В посохе фактические расчёты вынесены в чистые pulseDamage/cooldownTicks, вызваны теми же use ветками; изменены только согласованные base/CD. RU/EN обновлены ровно4 значения двух tooltip ключей, все остальные языковые ключи и значения сохранены.

Отдельные constants/helpers нужны для проверки actual production расчёта, а не копии формулы. Offline fixture вызывает4 комбинации реальных pulseDamage:6/6.72/6.9/7.728 и два cooldown60/54. 68 actual factory/event snapshots пересобраны из реальных регистраций; baseline native-attributes.json НЕ перезаписан. В verifier отдельное разрешение только на speed modifier одной булавы и соответствующий аналитический DPS; все остальные native поля68 предметов прежние, кроме уже согласованной A прочности.

`weapons87_balance_review.py` проверяет exact source delta обоих Java-файлов и полный production manifest11: никаких иных source/art/registry изменений в этом пакете. Старые target filters, расход1 прочности, particles/sound, параметры рун/знаний не заменены. Это не задача новой анимации или нового эффекта.

`weapons87_balance_b.py` воспроизводит два tooltip ключа в каждом языке, подключён последним canonical overlay; до изменения подготовлен draft, before-balance-b.zip и manifest. Legacy weapon generator sandbox проверяет, что подсказки не возвращаются к4сек. UTF-8 дефект самого verifier при первом чтении включённых language JSON исправлен явным encoding, не изменением ресурсов; неудачный probe-log сохранён.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Явное согласование B | exact user answer, BALANCE-PROPOSALS.md,balance-b-source-checks.json | PASS |
| Булава9.5/.8, остальные modifiers/tier/pressure сохранены | actual68 native constructor/event snapshots; exact registration delta | PASS |
| Actual pulse6 с прежними multiplier,CD60/54 | Weapons87Balance invokes production helpers; exact staff source delta | PASS |
| Только4 production files,ни одного art/ID/boss change | source-manifest-package11 comparison,before-balance-b.zip | PASS |
| RU/EN tooltip/generator reproduction |4 changed values,all other translations equal;exact draft/canonical sandbox | PASS |
| Java21 clean build/native/resource/JAR | package12-build/gates/receipt | PASS |
| Реальные cooldown/крит/armor/руны/сеть/урон в игре | запусков0; владелец | NOT_RUN / OWNER_PENDING |
| Полная158/159 прогрессия/сравнения всех68 | audit продолжается; B не подменяет весь scope | NOT_RUN / EXECUTOR_REMAINING |
| PolyMC | промежуточная цепочка не установлена | NOT_RUN / POLYMC_NOT_INSTALLED |

Точный промежуточный JAR/version/SHA/bytes и uncommitted source manifest — package12-receipt.json. Без commit/push/Project Status/owner-accepted/закрытия. C/D и решение190 остаются на ответе владельца; ни один из них не применён.

Owner: сравнить attack-speed tooltip булавы, обычный удар/crit/armor pressure без изменения9.5; посох по допустимой видимой цели на прежней дальности, базовый разряд6 и3с пауза, active day/night rune2.7с, прежний thunder/staff_power multipliers и расход прочности; блокировка стеной/защищённые цели по старым правилам. Эти игровые сценарии не заявляются выполненными.
