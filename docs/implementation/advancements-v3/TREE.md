# Дерево достижений, версия 3

75 карточек: корень + 7 / 24 / 25 / 18. Родитель задаёт только расположение.

[PNG](TREE.png) · [SVG](TREE.svg) · [Полная машинная ведомость](ACHIEVEMENTS.csv) · [Совместимость всех прежних ID](COMPATIBILITY.csv)

## Связи дерева

- **Славянские мифы** (`root`)
  - **Первые соседи** (`first_neighbors`)
    - **Свой урожай** (`own_harvest`)
      - **От стебля до полотна** (`linen_woven`)
      - **У деревенского стола** (`kitchen_meal`)
        - **Пенное ремесло** (`self_brewed_beer`)
      - **Полный двор** (`full_yard`)
    - **Тепло родного дома** (`bind_hearth`)
  - **Первый след** (`first_structure`)
    - **Разбойничья тропа** (`find_small_camp`)
      - **Большой стан** (`find_large_camp`)
    - **Заброшенное селение** (`explore_abandoned_settlement`)
      - **Земля наших предков** (`explore_gorodishche`)
    - **Тропы топей** (`traces_of_past`)
      - **Исследователь топей** (`bog_explorer`)
        - **Скрытый омут** (`find_deep_pool`)
    - **От липы до сосны** (`all_timber`)
    - **Отголосок старины** (`ancient_find`)
      - **Курганная тропа** (`explore_kurgan`)
      - **Древнее святилище** (`found_ancient_sanctuary`)
        - **Огонь и вода** (`found_two_sanctuaries`)
          - **Очищенное пламя** (`claimed_fire_fragment`)
          - **Чистый источник** (`claimed_water_fragment`)
      - **Открытая книга** (`first_book_entry`)
        - **Хранитель сказаний** (`bookkeeper`)
        - **Ответ на дар** (`first_ritual`)
          - **Три знака** (`three_signs`)
            - **Сказочный оберег** (`rare_accessory`)
    - **Город Чуди** (`explore_chud_city`)
      - **Залы под горой** (`chud_halls`)
      - **Заброшенные владения** (`explore_chud_abandoned`)
        - **Сердце лабиринта** (`chud_labyrinth`)
  - **Незваные гости** (`meet_bandits`)
    - **Стан обезврежен** (`clear_large_camp`)
      - **Без атамана** (`without_ataman`)
        - **Свист умолк** (`defeat_nightingale`)
    - **Степной богатырь** (`steppe_champion`)
      - **Лихая доля** (`name_your_misfortune`)
        - **Выдержавший стужу** (`endured_frost`)
          - **Последняя ночь** (`endured_last_night`)
      - **Рухнувший исполин** (`defeat_idolishche`)
        - **Долина свободна** (`defeat_skim`)
          - **Чёрное знамя пало** (`defeat_kartaus`)
            - **Сломленная рать** (`defeat_kalin`)
        - **Чары рассеяны** (`defeat_marinka`)
          - **Три головы, одна воля** (`defeat_gorynych`)
    - **Свой путь** (`own_path`)
      - **Имя в предании** (`class_iv`)
    - **Лес помнит** (`defeat_leshy`)
      - **Загасить овин** (`extinguish_the_barn`)
        - **Серебряное средство** (`silver_remedy`)
      - **Омут больше не зовёт** (`defeat_depth_master`)
      - **До рассвета** (`night_watch`)
    - **Покой кургана** (`kurgan_rest`)
      - **Подземные стражи** (`depth_champions`)
        - **Сердце земли** (`earth_heart`)
          - **Падение Полоза** (`great_poloz_fall`)
  - **Сказки оживают** (`tales_come_alive`)
    - **Избушка на курьих ножках** (`find_the_hut`)
      - **Гость Яги** (`guest_of_yaga`)
        - **Путеводный клубок** (`yaga_thread_earned`)
          - **Доверенный Яги** (`trusted_by_yaga`)
          - **Тайны Ягиного котла** (`cauldron_service`)
          - **Над лесом** (`first_flight`)
    - **Сказитель на тропе** (`talk_to_bayun`)
      - **Сказки Баюна** (`bayun_new_tale`)
        - **Доверие Баюна** (`friend_of_bayun`)
      - **Песня Баюна** (`bayun_song`)
    - **Мор зовёт** (`mor_invitation`)
      - **Последняя страница расследования** (`mor_story_complete`)
    - **Испытание Серого Волка** (`wolf_trial_passed`)
      - **Волчья охота** (`grey_wolf_bandits`)
        - **Курганный долг** (`grey_wolf_kurgan`)
          - **По следу Лиха** (`grey_wolf_likho`)
            - **Проклятый Волколак** (`curse_resolved`)

## Все 75 условий

| ID | Родитель | RU / EN | Условие |
|---|---|---|---|
| `root` | `—` | Славянские мифы / Slavic Myths | Впишите свою историю в мир сказаний. |
| `first_neighbors` | `root` | Первые соседи / First neighbors | Поговорите с жителем новой профессии мода. |
| `own_harvest` | `first_neighbors` | Свой урожай / Your own harvest | Посадите культуру мода и соберите её зрелой. |
| `linen_woven` | `own_harvest` | От стебля до полотна / From stem to cloth | Изготовьте льняное полотно. |
| `kitchen_meal` | `own_harvest` | У деревенского стола / At the village table | Заберите приготовленное блюдо из кухни. |
| `self_brewed_beer` | `kitchen_meal` | Пенное ремесло / The brewing craft | Завершите собственное брожение пива в бочке. |
| `full_yard` | `own_harvest` | Полный двор / A full yard | Разведите гусей, уток и домашних коз. |
| `bind_hearth` | `first_neighbors` | Тепло родного дома / The warmth of home | Привяжите очаг с помощью Домового. |
| `first_structure` | `root` | Первый след / The first trail | Обнаружьте любую зарегистрированную постройку мода. |
| `find_small_camp` | `first_structure` | Разбойничья тропа / The bandit trail | Найдите малый разбойничий лагерь. |
| `find_large_camp` | `find_small_camp` | Большой стан / The great camp | Найдите большой укреплённый стан |
| `explore_abandoned_settlement` | `first_structure` | Заброшенное селение / An abandoned settlement | Исследовать: Топь помнит |
| `explore_gorodishche` | `explore_abandoned_settlement` | Земля наших предков / Land of our ancestors | Найдите городище. |
| `traces_of_past` | `first_structure` | Тропы топей / Wetland trails | Найдите любую из шести болотных построек. |
| `bog_explorer` | `traces_of_past` | Исследователь топей / Wetland explorer | Посетите все шесть видов болотных построек. |
| `find_deep_pool` | `bog_explorer` | Скрытый омут / The hidden pool | Найти камень Омута |
| `all_timber` | `first_structure` | От липы до сосны / From linden to pine | Соберите древесину всех четырёх местных пород. |
| `ancient_find` | `first_structure` | Отголосок старины / An echo of the past | Получите древнюю монету или осколок идола. |
| `explore_kurgan` | `ancient_find` | Курганная тропа / The burial trail | Найдите малый, воинский или великий курган. |
| `found_ancient_sanctuary` | `ancient_find` | Древнее святилище / An ancient sanctuary | Найди одну из двух древних святынь |
| `found_two_sanctuaries` | `found_ancient_sanctuary` | Огонь и вода / Fire and water | Откройте оба вида древних святилищ. |
| `claimed_fire_fragment` | `found_two_sanctuaries` | Очищенное пламя / Cleansed flame | Заберите свой огненный фрагмент после очищения хранителя. |
| `claimed_water_fragment` | `found_two_sanctuaries` | Чистый источник / A pure spring | Заберите свой водный фрагмент после очищения хранителя. |
| `first_book_entry` | `ancient_find` | Открытая книга / An open book | Впервые откройте Книгу сказаний. |
| `bookkeeper` | `first_book_entry` | Хранитель сказаний / Keeper of tales | Доведите десять записей Книги до полного изучения. |
| `first_ritual` | `first_book_entry` | Ответ на дар / An answer to a gift | Успешно завершите обряд у алтаря. |
| `three_signs` | `first_ritual` | Три знака / Three signs | Установите три руны на одном предмете. |
| `rare_accessory` | `three_signs` | Сказочный оберег / A fairy-tale charm | Наденьте редкий сказочный аксессуар. |
| `explore_chud_city` | `first_structure` | Город Чуди / The Chud city | Обнаружьте подземный город Чуди. |
| `chud_halls` | `explore_chud_city` | Залы под горой / Halls beneath the mountain | Посетите все главные залы одного города Чуди. |
| `explore_chud_abandoned` | `explore_chud_city` | Заброшенные владения / Abandoned domains | Обнаружьте заброшенный комплекс Чуди. |
| `chud_labyrinth` | `explore_chud_abandoned` | Сердце лабиринта / Heart of the labyrinth | Войдите в настоящий проход лабиринта заброшенного комплекса Чуди. |
| `meet_bandits` | `root` | Незваные гости / Uninvited guests | Встретьте живого разбойника в пределах видимости. |
| `clear_large_camp` | `meet_bandits` | Стан обезврежен / The camp is cleared | Завершите настоящую зачистку большого стана. |
| `without_ataman` | `clear_large_camp` | Без атамана / Without an Ataman | Победите Атамана; добыча из сундука не считается победой. |
| `defeat_nightingale` | `without_ataman` | Свист умолк / The whistle falls silent | Одержите засчитанную победу над Соловьём-разбойником. |
| `steppe_champion` | `meet_bandits` | Степной богатырь / Champion of the steppe | Заслужите победу над Тугарином. |
| `name_your_misfortune` | `steppe_champion` | Лихая доля / An ill fate | Заслужите победу над Лихом Одноглазым. |
| `endured_frost` | `name_your_misfortune` | Выдержавший стужу / Enduring the frost | Завершите испытание Морозко; его смерть не требуется. |
| `endured_last_night` | `endured_frost` | Последняя ночь / The last night | Завершите испытание Карачуна. |
| `defeat_idolishche` | `steppe_champion` | Рухнувший исполин / The fallen giant | Заслужите победу над Идолищем Поганым. |
| `defeat_skim` | `defeat_idolishche` | Долина свободна / The valley is free | Заслужите победу над Скимом в его долине. |
| `defeat_kartaus` | `defeat_skim` | Чёрное знамя пало / The black banner falls | Завершите встречу с Картаусом вместе с Финистом. |
| `defeat_kalin` | `defeat_kartaus` | Сломленная рать / A broken army | Добейтесь засчитанного завершения битвы с Калином. |
| `defeat_marinka` | `defeat_idolishche` | Чары рассеяны / The spell is broken | Заслужите победу над Маринкой в ведьмином дворе. |
| `defeat_gorynych` | `defeat_marinka` | Три головы, одна воля / Three heads, one will | Доведите настоящую встречу с Горынычем до полного очищения. |
| `own_path` | `meet_bandits` | Свой путь / Your own path | Выберите свой первый класс. |
| `class_iv` | `own_path` | Имя в предании / A name in legend | Достигните 40-го уровня, пройдите испытание и примите класс IV. |
| `defeat_leshy` | `meet_bandits` | Лес помнит / The forest remembers | Победите Лешего; его редкое сердце не требуется. |
| `extinguish_the_barn` | `defeat_leshy` | Загасить овин / Extinguish the barn | Победите Овинника. |
| `silver_remedy` | `extinguish_the_barn` | Серебряное средство / A silver remedy | Победите Волколака серебряным оружием. |
| `defeat_depth_master` | `defeat_leshy` | Омут больше не зовёт / The pool calls no more | Заслужите победу над Старшим Водяным. |
| `night_watch` | `defeat_leshy` | До рассвета / Until dawn | Победите Ночницу в честном бою. |
| `kurgan_rest` | `meet_bandits` | Покой кургана / Rest for the burial mound | Завершите бой с главным хранителем кургана. |
| `depth_champions` | `kurgan_rest` | Подземные стражи / Underground guardians | Победите Кладовика или Большого подземника в их встрече. |
| `earth_heart` | `depth_champions` | Сердце земли / Heart of the earth | Заслужите победу над Духом Земли. |
| `great_poloz_fall` | `earth_heart` | Падение Полоза / The fall of Poloz | Заслужите победу над Великим Полозом. |
| `tales_come_alive` | `root` | Сказки оживают / Tales come alive | Вступите в настоящее взаимодействие с Ягой, Баюном или Серым Волком. |
| `find_the_hut` | `tales_come_alive` | Избушка на курьих ножках / The chicken-legged hut | Позвать хозяйку лесной избушки. |
| `guest_of_yaga` | `find_the_hut` | Гость Яги / Yaga's guest | Заслужить право на обмен. |
| `yaga_thread_earned` | `guest_of_yaga` | Путеводный клубок / The guiding thread | Завершите второе основное поручение Яги и получите клубок. |
| `trusted_by_yaga` | `yaga_thread_earned` | Доверенный Яги / Trusted by Yaga | Дойдите до доверия Яги в её поручениях. |
| `cauldron_service` | `yaga_thread_earned` | Тайны Ягиного котла / Secrets of Yaga's cauldron | Сварить отвар в избушке. |
| `first_flight` | `yaga_thread_earned` | Над лесом / Above the forest | Совершите первый полёт на сказочном транспорте. |
| `talk_to_bayun` | `tales_come_alive` | Сказитель на тропе / A storyteller on the trail | Начните настоящий разговор с Котом Баюном. |
| `bayun_new_tale` | `talk_to_bayun` | Сказки Баюна / Bayun's tales | Узнайте от Баюна ранее неизвестное предание. |
| `friend_of_bayun` | `bayun_new_tale` | Доверие Баюна / Bayun's trust | Заслужите дружбу Кота Баюна. |
| `bayun_song` | `talk_to_bayun` | Песня Баюна / Bayun's song | Переживите настоящие чары Баюна и освободитесь, оставаясь в живых. |
| `mor_invitation` | `tales_come_alive` | Мор зовёт / Mor calls | Получите приглашение Яги или Баюна к расследованию Мора. |
| `mor_story_complete` | `mor_invitation` | Последняя страница расследования / The investigation's last page | Завершите шестую основную главу расследования Мора. |
| `wolf_trial_passed` | `tales_come_alive` | Испытание Серого Волка / The Grey Wolf's trial | Пройдите настоящее испытание Серого Волка и получите признание. |
| `grey_wolf_bandits` | `wolf_trial_passed` | Волчья охота / The wolf's hunt | Завершите и сдайте поручение grey_wolf_bandits: 15 разбойников. |
| `grey_wolf_kurgan` | `grey_wolf_bandits` | Курганный долг / A burial debt | Завершите и сдайте поручение grey_wolf_kurgan против главного стража. |
| `grey_wolf_likho` | `grey_wolf_kurgan` | По следу Лиха / On Likho's trail | Завершите и сдайте grey_wolf_likho против связанного Лиха. |
| `curse_resolved` | `grey_wolf_likho` | Проклятый Волколак / The Cursed Volkolak | Верните подлинный череп и завершите историю Проклятого волколака. |
