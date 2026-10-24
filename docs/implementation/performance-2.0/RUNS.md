# Реальные запуски этой итерации

Время MSK, 15/17 октября2026. Где нет отдельного runtime-start receipt, приведено приблизительное время launcher, не придуманное время JVM. Предыдущие PR32/36/37 имеют собственные журналы; их старые запуски здесь не посчитаны. Локальные квитанции `runtime-start.json`, `launch.txt`, события и полный sanitized log каждого каталога остаются рядом с данными.

| Клиент | Дата /старт | Production | Итог |
|---|---|---|---|
| cube-A1 | 15 октября~15:19 (launcher) | main | COMPLETE, условия окна90,51%/0%, исключён |
| cube-B1 | 15 октября~15:03 (launcher) | PR37 | COMPLETE,100%/100%, принят |
| cube-A2 | 15 октября~15:34 (launcher) | main | COMPLETE,100%/100%, принят |
| cube-A3 | 15 октября15:50:55 | main | COMPLETE,0%/0%, исключён |
| cube-A4 | 17 октября14:42:07 | main | COMPLETE,0%/0%, исключён |
| cube-A5 | 17 октября14:55:41 | main | COMPLETE,100%/100%, принят |
| cube-B2 | 17 октября15:02:34 | PR37 | COMPLETE,100%/100%, принят |
| world-mod | 17 октября15:11:39 | main | CRASHED15:38:11, Полозphase2, partial |
| world-fixed | 17 октября15:52:55 | guard8b49c5ca | Сцены/idle пройдены; FORCED_STOP_FIXTURE~16:49, reopen не пройден |
| world-control | 17 октября~16:52 | без мода, ошибочный fixture | FAILED_BOOTSTRAP,0 миров, не baseline |
| world-control2 | 17 октября16:59:34 | без Slavic Myths, runtime5mods | COMPLETE17:19:01 |
| world-bosses-flat | 17 октября17:23:17 | guard8b49c5ca | COMPLETE17:35:41, native GROUND/phase3/phase1 |

Итого12 реальных клиентов/11 integrated server instances; 7 cube +4 запущенных мира, один bootstrap без мира. Отдельно3 headless GameTest JVM в проверке guard (первый fixture некорректен, действительные before/after после исправления). Внутрипроцессные micro-batches и пять lifecycle циклов не считаются независимыми JVM.

`world-city`/совместимость старого save/reopen v2 **NOT_RUN**. 17 октября владелец отменил15min интервалы, затем остановил работу перед выключением ПК. Последний flat успел штатно сохранить мир/выйти. При возобновлении владелец запретил запуск Minecraft: последующая работа — только offline-анализ и публикация. Эта публикация не запускает ни client, ни GameTest server.
