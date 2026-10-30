# Команды генерации строений · #141

Общий путь: `/slavicmyths dev generate <строение> [размер/вариант] [аргументы]`.
Требуются OP2/читы; прежние config gates и требования к игроку сохранены.
Размер указывается только у построек, где он существует. Старые команды и `/sm` работают как совместимые варианты; они не запускают второе действие.

## Частые примеры

```mcfunction
/slavicmyths dev generate bandit_camp small
/slavicmyths dev generate bandit_camp fortified
/slavicmyths dev generate bandit_camp large
/slavicmyths dev generate kurgan small
/slavicmyths dev generate kurgan warrior
/slavicmyths dev generate kurgan great
/slavicmyths dev generate kurgan great 42
/slavicmyths dev generate fire_sanctuary
/slavicmyths dev generate pure_spring
/slavicmyths dev generate serpent_hill
/slavicmyths dev generate city
/slavicmyths dev generate city attached
/slavicmyths dev generate abandoned medium
/slavicmyths dev generate abandoned great
/slavicmyths dev generate depths
/slavicmyths dev generate yaga
/slavicmyths dev generate bayun
/slavicmyths dev generate skim_valley
/slavicmyths dev generate witch_yard
/slavicmyths dev generate barracks
/slavicmyths dev generate village_showcase temperate
/slavicmyths dev generate gorodishche_showcase cold
/slavicmyths dev generate courtyard
/slavicmyths help dev generate
```

`city` без варианта использует native defaults, `attached` — существующий вариант, не новый размер. Для `abandoned` сохранён default medium. У лагерей `medium` остаётся старым alias для `fortified`. `courtyard_debug` — прежний debug-вызов той же арены со своим config gate, отдельно от обычного `courtyard`. Не сливать их права. `village <template>` сохраняет родной каталог шаблонов и автодополнение. Для showcase существуют temperate/cold/warm; это климат, не размер.

`/slavicmyths dev swamp` остаётся телепортом к болотному биому: он ничего не генерирует. Поиск/locate, развитие встреч, summon героя, growtree, управление очередью `generation status/cancel` не переименованы в генерацию сооружений.

## Полное фактическое дерево генерации

| Исполняемый путь | Игрок OP | Консоль OP |
|---|---|---|
| `/slavicmyths dev generate bayun` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate courtyard_debug` | [2, 3, 4] | [] |
| `/slavicmyths dev generate courtyard` | [2, 3, 4] | [] |
| `/slavicmyths dev generate city` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate city attached` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate abandoned` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate abandoned medium` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate abandoned great` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate depths` | [2, 3, 4] | [] |
| `/slavicmyths dev generate gorodishche_showcase` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate gorodishche_showcase temperate` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate gorodishche_showcase cold` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate gorodishche_showcase warm` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate kurgan small` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate kurgan small <seed>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate kurgan warrior` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate kurgan warrior <seed>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate kurgan great` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate kurgan great <seed>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate witch_yard` | [2, 3, 4] | [] |
| `/slavicmyths dev generate barracks` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate fire_sanctuary` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate fire_sanctuary <pos>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate pure_spring` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate pure_spring <pos>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate serpent_hill` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate serpent_hill <pos>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate serpent_hill <pos> <rotation>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate skim_valley` | [2, 3, 4] | [] |
| `/slavicmyths dev generate village_showcase` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate village_showcase temperate` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate village_showcase cold` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate village_showcase warm` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate village <template>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate bandit_camp small` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate bandit_camp small <seed>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate bandit_camp fortified` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate bandit_camp fortified <seed>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate bandit_camp large` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate bandit_camp large <seed>` | [2, 3, 4] | [2, 3, 4] |
| `/slavicmyths dev generate yaga` | [2, 3, 4] | [] |

Полный справочник всех команд: [COMMANDS](COMMANDS.md); полная совместимость: [COMMAND_MIGRATION](COMMAND_MIGRATION.md). Brigadier parsing/suggestions/permissions проверены offline с production builders; построек в мире Codex не создавал.

## Адские постройки и Посад — тот же канонический путь

```mcfunction
/slavicmyths dev generate nether_posad
/slavicmyths dev generate veche_hall
/slavicmyths dev generate communal_house
/slavicmyths dev generate druzhinny_dvor
/slavicmyths dev generate kuznechny_dvor
/slavicmyths dev generate zeleynaya
/slavicmyths dev generate granary
/slavicmyths dev generate menovye_ryady
/slavicmyths dev generate nether_watchpost
/slavicmyths dev generate broken_nether_bridge
/slavicmyths dev generate wayfarer_camp
/slavicmyths dev generate forgotten_shrine
/slavicmyths dev generate ancestor_shrine
/slavicmyths dev generate trizna_pyre
```

OP2/читы. Координаты, номера микропатча и дополнительная фраза подтверждения не требуются. В обычном мире опорой служит настоящий грунт, а не крона дерева или снег. Используется существующая классификация природного рельефа ManualStructureJobs/LandTerrain; отдельного списка разрешённых материалов у новых зданий нет. Природный рельеф расчищается в пределах построек, основание подготавливается; исходные модели и лут сохраняются. Ручные команды работают в текущем измерении; правила естественного Nether worldgen от этого не меняются.

Посад: начало участка113×113 на12блоков к югу, центр поX относительно исполнителя. Поэтапный preflight/подготовка/расчистка/опоры/постройка подключённой геометрии; старый запрет HEADROOM для ручной генерации не применяется. Natural StructureSet не включён. Полная реализация всех районов2.2 не заявляется. `/slavicmyths dev posad status` и `cancel` остаются управлением текущим заданием, не новыми именами генерации.

Старые пути из предыдущей тестовой сборки сохранены как совместимость, не являются рекомендуемым новым интерфейсом. Размещённые структуры и контейнеры защищаются от повторной записи; распознавание произвольных домов из природного камня и сторонних claims не объявляется универсальным.


### Меновые ряды (#352)

`/slavicmyths dev generate menovye_ryady` — авторский рынок 27x25 из 2203 блоков, с восьмью сундуками и изначальными таблицами лута. `dev posad building menovye_ryady_2_2_6 [pos]` — альтернативный путь. При `dev generate nether_posad` рынок размещается внутри общей схемы 5 вместе с другими зданиями. Прежние сохранения не достраиваются; natural StructureSet выключен.
