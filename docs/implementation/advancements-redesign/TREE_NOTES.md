# Новое дерево: 55 узлов, шесть самостоятельных ветвей

**Проект для рассмотрения владельцем, этап A #30.** После его принятия — отдельный implementation PR с серверными тестами и настоящей проверкой интерфейса. Текущий PR не меняет ни одного runtime JSON или класса.

![Полный проект дерева](TREE.svg)

[Открыть SVG отдельно](TREE.svg). Это схема проекта, не снимок игрового интерфейса. Все 55 карточек с полными RU/EN названиями, подсказками, parent, stage, frame, icon и display flags перечислены в [TREE_TABLE.md](TREE_TABLE.md); машиночитаемый источник — [TREE.json](TREE.json). Судьба **всех** прежних ID — [DECISIONS.md](DECISIONS.md) и [DECISIONS.json](DECISIONS.json).

## Ритм и визуальная композиция

В одном дереве шесть дорожек: **У родного огня → Слово и знак → Дороги богатырей → Лес и тихие воды → Память под землёй → На краю сказания**. Стрелки в этой фразе означают порядок чтения дорожек, не обязательную последовательность прохождения. Все шесть — прямые дети root, выдаются штатным section tick без toast/chat. Вехи каждой дорожки распределяются на колонки обычного vanilla layout; рамка challenge выделяет большие завершения, goal — ремесло/отношения/элитное испытание.

Начало даёт несколько понятных ориентиров: книга, кухня, полотно, след древности и выбор пути. Средняя часть — доверие, руны, экспедиции, полноценный стан. Поздняя — самостоятельные боссы и испытания. На старте нет серии наград за бересту → свиток → предмет книги → ПКМ. Подсказки называют фактический результат, не обещают новые предметные rewards.

| Дорожка | Вех без раздела | Характер |
|---|---:|---|
| У родного огня | 6 | Домовой, Баюн, Яга, кухня, полотно, разведение трёх видов |
| Слово и знак | 7 | Открытие/освоение Книги, обряд, руны, редкий аксессуар, класс I и IV |
| Дороги богатырей | 9 | Лагерь, серьёзная зачистка, Атаман, Соловей, полёт; Идолище/Ским/Картаус/Калин |
| Лес и тихие воды | 9 | Коллекция дерева, болота, песня; Леший/Водяной/Овинник/Волколак/проклятие/Ночница |
| Память под землёй | 8 | Древность, первый след, оба святилища, два хранителя, Земля, курган, подземные элиты |
| На краю сказания | 9 | Лихо, Тугарин, Морозко, Карачун, Полоз, Горыныч, Маринка; цветок/звезда |
| **Всего** | **48** | **+6 разделов +1 root =55** |

Это единая свободная прогрессия, а не кампания с обязательным порядком боссов. Родитель определяет визуальную связь и поле `Available` Книги, **не условие выдачи**. Ни одна новая criteria не проверяет completion родителя. `class_iv` следует `own_path`; `bookkeeper` — открытию книги; фрагменты — обзорной вехе двух святилищ; лагерные результаты — обнаружению лагеря. Все боссы остальных глав — братья, а не цепочка убийств. Можно честно победить босса до открытия его визуального родителя и сразу получить achievement. Начальные milestones и разделы не требуют принудительного изучения Книги для свободной игры.

Выбор шести дорожек сохраняет различия домашних отношений, мастерства, путешествий, природы, археологии и великих испытаний. У root больше нет отдельного «висячего» ряда Морозко/Карачуна. В Память перенесён Дух Земли; случайные forest_guardian/learn_armorer/two_rings убраны из старых чуждых рубрик. Иконки используют существующие item assets, без нового art или ребаланса. Толстые рамки в SVG — challenge, не оценка DPS босса.

## Условия 44 сохраняемых узлов

Точные текущие trigger, все criterion names, predicates и логическая форма requirements находятся в соответствующей строке [полной ведомости](DECISIONS.md) и [SOURCE.json](SOURCE.json). **Критерии и rewards каждого старого ID сохраняются**; новые parent/display — только из TREE. Stage0 — рубрика, 1 — освоение, 2 — экспедиция/мастерство, 3 — большая история; это UX-метка, не новый уровень/рецепт/проверка.

Не меняются OR `ancient_find` (монета ИЛИ осколок), `bad_people` (малый ИЛИ средний лагерь), `traces_of_past` (любая из6), AND `all_timber` (4 породы), `bog_explorer` (6 строений), `full_yard` (гусь+утка+коза). `bad_people` намеренно оставлен с текущим OR: большой стан имеет собственный серьёзный итог; новый критерий large не добавляется ради косметики и сохранности partial progress.

Уточнения server dispatch, не формата сохранённых критериев:

- `defeat_depth_master`, `without_ataman`, `name_your_misfortune`, `steppe_champion`: подтверждённая личная победа вместо радиуса/непроверенного source Player; adapter не меняет бой и право на старый loot. Проектная co-op политика и native-only ограничения — в BOSS_COVERAGE. Там, где факта участия нет, требуется ограниченный encounter-local tracker, а не глобальный поиск.
- `great_poloz_fall`: victory только в accepted native завершении; `TaleService.read` с ядром изучает запись, но больше не создаёт новую победу. Уже выданное (включая показ ядра) сохраняется как историческое done. Честная новая подсказка — победа, `hidden=false`, без загадки, навсегда скрывающей поздний контент.
- `claimed_fire_fragment`, `claimed_water_fragment`: одно завершение каждой истории хранителя, подсказывается **claim после очищения**. Criterion `claim` не переименовывается. Новая медаль проверяет принадлежность к завершённой встрече и participation; сами выдача фрагмента/Claims/receipt не перерабатываются. Старые claims/advancements, даже без восстановимой заслуги, не отзываются.
- `kitchen_meal` и claimed_* полагаются на исправленный dispatch #25. `first_book_entry` — успешное серверное open; `bookkeeper` — десять записей stage≥4, не три бита legacy Knowledge mask. `lore_keeper` остаётся data-only со своей прежней семантикой.
- `bind_hearth` сохраняет настоящее событие привязки. Его текст не требует отдельного нового threshold дружбы. `silver_remedy` сохраняет серебряный fatal strike; ordinary `volkolak` не Cursed Volkolak.
- `falling_star` оставлен как единственный выразительный результат редкого звёздного оберега; self_playing_music/table_is_set/not_today/full_speed_ahead убраны из display. `rare_accessory` — экипировка, не победа. Ни редкое украшение, ни цветок не подменяют boss credit.

## Условия 11 новых узлов

Все ID в `slavicmyths`, без рецептов и новых предметных наград. Для каждой новой медали — один criterion `victory` (`minecraft:impossible`), а для `class_iv` — `mastery`; список requirements из одной группы с этим criterion. Все новые `rewards={}`: историческое восстановление не начисляет XP, лут, рецепты или достижения повторно. Заголовки/подсказки/иконки/рамки/parent — в TREE_TABLE, без неоговорённых fallback.

| Новый ID | Фактический server callback / eligibility | Что не засчитывать | Историческое восстановление |
|---|---|---|---|
| defeat_gorynych | Реальный GorynychLedger instance, !Standalone, mask7 и переход в PURIFIED после native таймера; личный Participants, survival/non-debug | EXHAUSTED, die(), debug/standalone, предмет чешуи/печать, claim с full inventory | PURIFIED + personal receipt Claimed + !CreativeClaim + !Standalone; одного общего PURIFIED недостаточно |
| defeat_idolishche | После accepted BogatyrLedger.victory и durable participationFlag/defeatCount | spawn, последний hit без native допуска, показ материала | RewardLedger.history(0): participationFlag && defeatCount>0 |
| defeat_marinka | После WitchYard.victory accepted participant/contributed и записи progress | только освобождение Добрыни, debug, spectator, зритель | marinka_defeated && marinka_participation && DefeatCount>0 |
| defeat_skim | SkimValley.win для native eligible игрока (contributor OR attendance≥60) | подарок клыка/вызов Алёши, nearby без attendance | skim_beast_defeated && participationFlag && defeat_count>0 |
| defeat_kartaus | KartausEncounter.finish(true) после записи victory/clear_count, native owner | смерть чужого босса, ABANDONED/restart cancellation | player story.finist_kartaus.victory && clear_count>0 |
| defeat_kalin | KalinEncounter.finish(win) после native credited owner, калин сдался | raw death, GENERIC_KILL, зритель, обычный солдат | kalin_battle_won && times_kalin_defeated>0 |
| class_iv | Успешный ClassState.branch: ClassUltimates definition, level≥40, matching Second, completed trial, Third установлен | creative/debug прямое изменение NBT, выбор класса I, открытие окна | валидный Third из ClassUltimates, Level≥40, completed trial для parent; invalid состояние не чинить автоматически |
| curse_resolved | VolkolakStory.handOver после проверки Owner/Encounter/Nonce настоящего черепа, returned UUID, SKULL_RETURNED/ready | CursedVolkolak.die, фаза ESCAPED, чужой череп, общий RESOLVED | личный returned в том же encounter; не один мировой stage |
| kurgan_rest | Native KurganEncounters.died: principal boss (Prince либо tier1 Voevoda), actual dead и accepted instance transition; новый local player credit | chunk unload, retire/restore slot, RuneSummoned, смерть каждого Upyr; не nearby | **Нет**: нынешний bossDefeated не содержит личной заслуги |
| depth_champions | Actual accepted death bound encounter Kladovik ИЛИ LargePodzemnik, EncounterPopulation.defeat(key) + новый player credit | обычный podzemnik, debug/unbound, consumed flag без игрока | **Нет**: consumed map — world state |
| night_watch | Actual accepted player-causal death Nochnitsa после native cancellation checks, non-debug/survival; поддержать projectile owner | proximity, /kill, падение без личной заслуги, PvP | **Нет**: старого personal ledger не существует |

Eligibility/anti-debug guards относятся только к **новым** awards; настоящие правила native encounter/loot не заменяются. Где старый ledger уже содержит принятый персональный факт, его не переоценивают по текущей экипировке/классу/нахождению игрока. Исторические данные могут не доказать отсутствие старого creative обхода: такие случаи описать в отчёте, а не выдавать несуществующие runtime гарантии.

Classes 3 в main уже содержит ClassXpService, 12 ClassUltimates, ClassTrialService и проверку Level40/trial в ClassState.branch. Значит, узел имеет survival server route в коде; полный ручной Survival путь к IV и скорость прокачки **не измерены этой задачей**. Его нельзя объявить принятым на основании таблицы.

## Display и игровая проверка

Корень/рубрики: task, toast=false, chat=false. Task/goal milestones: toast=true, chat=false. Challenge: toast=true, chat=true, только первый личный completion. `hidden=false` у всех55; отсутствие story-spoiler до готовности решается текстом, а не исчезновением босса из карты. Vanilla сам ограничивает раскрытие дерева; 55 — количество карточек в полном дереве, не требование показать все при первом входе.

У Книги `Available` сейчас вычисляется из parentDone; её легенда/tooltip должна объяснять «ветка открыта», а не обещать запрет боя или выдачи. Не менять эту сетевую структуру и не вводить новые пакеты ради дизайна. После удаления display у legacy эти узлы исчезают из snapshot, но их done остаётся источником reconcile. Все видимые parent указывают на видимые узлы этого дерева.

На этапе B обязательны: vanilla и Книга, RU/EN, scales1/2/3/4, 1366×768 и1920×1080, новая/старая игра; проверить clipping длинных русских названий, соединения, late cards, completed-before-parent, drag/pan/zoom/scroll, отсутствие пустых рубрик/data-only карточек и логическую доступность. SVG не заменяет эти проверки. Никакого автоматического client запуска в этапе A.
