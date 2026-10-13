# Русская локализация — обязательные строки/ключи

## Названия
- `item.slavicmyths.wolf_call`: «Волчий зов»
- `item.slavicmyths.mor_ward`: «Оберег от мора»
- `item.slavicmyths.tracker_sense`: «Чутьё следопыта»
- `item.slavicmyths.cleansing_powder`: «Очищающий порошок»
- `book.slavicmyths.chapter_mor`: «Хворь другого мира»
- `book.slavicmyths.mor_quest_185`: «Предвестники бедствия»
- `book.slavicmyths.cleansing_knowledge`: «Знание очищения»

## Квесты
`slavicmyths.quest.mor.intro`: «Недобрый зов»;
`slavicmyths.quest.mor.q1`: «Чужая нечисть»;
`slavicmyths.quest.mor.q2`: «Следы мора»;
`slavicmyths.quest.mor.q3`: «Лики мора»;
`slavicmyths.quest.mor.restore_earth_20`: «Возвращение земли»;
`slavicmyths.quest.mor.q4`: «Зов из глубин»;
`slavicmyths.quest.mor.q5`: «Последнее предостережение»;
`slavicmyths.quest.mor.q6`: «После бедствия»;
`slavicmyths.quest.wolf.w1`: «Чужой след»;
`slavicmyths.quest.wolf.w2`: «Звериный страх»;
`slavicmyths.quest.wolf.w3`: «Верность до конца».

## Действия
`action.accept_teleport` «Перенести к избе»;
`action.manual_visit` «Приду сам»;
`action.begin_research` «Начать расследование»;
`action.not_ready` «Пока не готов»;
`action.continue` «Далее»;
`action.submit` «Сдать»;
`action.claim` «Забрать награду»;
`action.acknowledge_risk` «Я понимаю опасность»;
`action.ask_mor` «Что за Хворь?»;
`action.listen_allies` «Что думают друзья?»;
`action.finish` «Завершить разговор»;
`message.ward_active` «Защита от мора ещё действует»;
`message.tracker_active` «Чутьё уже обострено»;
`message.wolf_present` «Серый Волк уже рядом»;
`message.cleansing_done` «Очищение началось: до 9 блоков»;
`message.nothing_to_clean` «Здесь нечего очищать»;
`message.earth_restoration_progress` «Возвращение земли: {count}/20»;
`message.land_protected` «Восстановленная земля защищена на 90 секунд»;
`action.study_eye` «Исследовать Око мора»;
`action.ask_recipe` «Покажи рецепт очищения»;
`action.earth_training` «Научи очищать»;
`action.earth_reward` «Я очистил землю».

## Технические требования
- Если фактический namespace мода/ключи отличаются — использовать правильный префикс проекта, не регистрировать второй mod namespace.
- Любой текст из `03_PORCH_SCENE/03_DIALOGUE_SCRIPT_RU.md` разнести на локализационные ключи с идентификаторами и условными ветками, не хранить русский текст внутри Java классов.
- Проверить слитность строк, русские имена, переносы строк, pluralisation/progress, отсутствие «кот-бын» и технических английских терминов в интерфейсе.
- Создать/обновить `en_us` при наличии языкового файла, без вставок untranslated на русском в английскую локализацию. RU первична.
