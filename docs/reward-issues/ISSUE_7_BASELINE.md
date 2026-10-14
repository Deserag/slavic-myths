# #7 — исходный нативный урон посоха

Ветка codex/issue-7-frost-balance основана на #6/ac6cf40a, без изменения урона.
Фактический запуск: 14 октября2026,19:59MSK.

Настоящий FrostShard.launch → физическое столкновение → native hurt:

| Цель | Armor | Toughness | HP до | HP после | Урон |
|---|---:|---:|---:|---:|---:|
| Зомби, без брони |0|0|1000|993|7|
| Зомби, заданные armor/toughness |20|8|1000|998.11|1.8900146|

Обе цели1000HP только для точного измерения; это не настоящий boss test.
Проверки в tools/reward-issues-tests/org/slavicmyths/verify/FrostBalanceBaselineTests.java.
Шесть native tests, включая четыре lifecycle регрессии #6: PASS.
Поток/волна/сосульки, cooldown-window DPS, реальные bosses и управление
ещё не измерены. Изменение чисел урона не реализовано, #7 не завершён.
Теоретические7/0.8=8.75HP/s не выдаются за realistic DPS.

Общий счёт этой задачи:6 реальных GameTest server,0 dev client,
0 standalone dedicated; между стартами более10 минут. Production сборки
изолированных PR не заменяют пользовательскую1.9.7.3 и не установлены в PolyMC.
