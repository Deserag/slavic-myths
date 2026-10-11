# Команды Чуди — 1.5.2.2

Нужны cheats/operator2. `/sm` — сокращение `/slavicmyths`. Тестовый PolyMC1.21.1; Minecraft агентом не запускается.

## Создать в текущем месте

```mcfunction
/sm city generate
/sm city generate attached
/sm abandoned generate
/sm abandoned generate medium
/sm abandoned generate great
```

`city`: город; `attached`: город с гарантированной малой пристройкой; `abandoned generate` по умолчанию medium; great явно выбирает большой комплекс (один из трёх допустимых по высоте профилей). В flat весь комплекс над поверхностью. В обычном мире крыша под грунтом20 или под дном воды5 блоков по ограниченным16-блоковым пробам рельефа. Вход также под землёй. Y исполнителя не задаёт глубину. X/Z привязаны к центру его чанка. Для другого места: `/execute positioned X Y Z run sm abandoned generate great`.

Команда может заменять другие конструкции; естественный интервал2000 для принудительного размещения не действует. Энд-портал/крепость защищены; при конфликте размещение отклоняется до записи. Недостаточная высота мира также приводит к отказу. Повторный старт того же типа в одном чанке отклоняется. Созданный native start сохраняется; доступны info/validate/tp. Старые chunks сами не перестраиваются.

## Найти естественную генерацию

```mcfunction
/locate structure slavicmyths:chud_city
/locate structure slavicmyths:chud_abandoned
/sm city search
/sm abandoned search
/sm abandoned search attached
/sm abandoned search medium
/sm abandoned search great
/sm chud_search status
/sm chud_search cancel
/sm chud_search next
/sm city locate
/sm city locate 512
/sm abandoned locate
/sm abandoned locate attached
/sm abandoned locate medium
/sm abandoned locate great
```

Search допускает номер страницы0..128, например `/sm abandoned search great 1`. До96 областей/60секунд на страницу, background worker, один поиск на сервер. `next` продолжает. Поиск выдаёт планы новых чанков, не создаёт структуры и не доказывает наличие в ранее созданных областях. Locate сначала читает готовые загруженные starts, при отсутствии запускает search. Для естественного теста нужны новые chunks.

## Осмотр созданного комплекса

```mcfunction
/sm city info
/sm city debug_plan
/sm city validate
/sm abandoned info
/sm abandoned inspect
/sm abandoned debug
/sm abandoned validate
/sm city tp entrance
/sm city tp square
/sm city tp residential
/sm city tp forge
/sm city tp store
/sm city tp council
/sm city tp gate
/sm abandoned tp entrance
/sm abandoned tp quarter
/sm abandoned tp gallery
/sm abandoned tp shaft
/sm abandoned tp labyrinth
/sm abandoned tp gate
```

Перед названием зоны можно добавить ID из info: `/sm city tp chud-v1-X-Z square`, `/sm abandoned tp abandoned-v2-X-Z gallery`. У info/debug_plan/validate города и info/inspect/debug/validate abandoned также есть необязательный ID. Для abandoned используйте rootComplexId без двоеточия. Shaft есть только у great, gate — если предусмотрены запечатанные ворота. TP требует загруженной безопасной точки; при NOT_LOADED дождитесь загрузки, осмотрите в spectator. Полное покрытие bbox может превышать малую view distance.

## Вспомогательные vanilla команды

```mcfunction
/gamemode spectator
/gamemode creative
/locate structure minecraft:stronghold
```

Vanilla `/place structure slavicmyths:chud_city` и `/place structure slavicmyths:chud_abandoned` остаются штатными командами с ограничениями native planner, без сохранения нового StructureStart. Для данной проверки используйте новые `generate`.

Короткая ручная проверка: ordinary/гора/под водой/flat; город и medium/great; проход вход→помещения→лабиринт; отказ рядом с энд-порталом; настоящий save/exit/relog с повторным info/tp. Агент выполнил0 клиентов/0 серверов; игровые результаты пока не подтверждены.

В1.5.2.2 добавлены освещение и палитры отделки. Для их осмотра создайте новый комплекс в другом месте; старый автоматически не обновляется.
