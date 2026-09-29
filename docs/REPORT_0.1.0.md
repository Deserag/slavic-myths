# ОТЧЕТ О ПРОХОДЕ

## Реализовано

- [x] Рабочая папка изучена до создания проекта; существующего Forge-проекта не найдено.
- [x] Создан проект slavic-myths, mod ID slavicmyths, версия 0.1.0.
- [x] Minecraft 1.16.5 / Forge 36.2.42 / Java 8; Gradle Wrapper 7.3.3, ForgeGradle 5.1.77.
- [x] Единый реестр: берестяной свиток, громовой камень, оберег.
- [x] Три модели и настоящие оригинальные PNG 16×16 RGBA; проверены CRC и размеры, изображения просмотрены.
- [x] Три стандартных рецепта, три видимых достижения и три служебных advancement для книги рецептов.
- [x] Русские и английские названия и тексты достижений.
- [x] Документация мифологии, классов, архитектуры, roadmap и текущего статуса.
- [x] Инструкции следующих итераций и ручного smoke-test.
- [x] Фактическая Gradle-сборка с reobfJar и структурная проверка собранного JAR.

Флажки контента означают созданные и структурно проверенные файлы. Работа в игре ещё не подтверждена.

## Не реализовано

- [ ] Классы, прокачка, GUI, дерево навыков, мана, способности и заклинания — по границам задания.
- [ ] Данные игрока, Capability и сеть — пока не нужны инертным предметам.
- [ ] Мобы, NPC, боги, новые блоки/биомы/структуры/измерения и сложные ритуалы.
- [ ] Механики знаний свитка, защиты оберега и молний камня.
- [ ] Игровая проверка клиента и выделенного сервера.

## Реализовано частично

- [ ] Проверка совместимости: компиляция и упаковка подтверждены, игровая загрузка не проверена.
- [ ] Баланс: заданы простые цены (камень требует алмаз), результаты выживания не измерялись.

Документы будущих систем выполнены в объёме задания; отсутствие реализации классов — намеренное ограничение.

## Созданные файлы

Все исходные файлы проекта, документация и сохранённые протоколы:

- `.gitignore`
- `AGENTS.md`
- `README.md`
- `build.gradle`
- `docs/ARCHITECTURE.md`
- `docs/CLASS_SYSTEM.md`
- `docs/MYTHOLOGY.md`
- `docs/PROJECT_STATUS.md`
- `docs/REPORT_0.1.0.md`
- `docs/ROADMAP.md`
- `docs/TESTING.md`
- `docs/verification/gradle-build.log`
- `docs/verification/resources-check.log`
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew`
- `gradlew.bat`
- `settings.gradle`
- `src/main/java/org/slavicmyths/SlavicMyths.java`
- `src/main/java/org/slavicmyths/registry/ModItems.java`
- `src/main/resources/META-INF/mods.toml`
- `src/main/resources/assets/slavicmyths/lang/en_us.json`
- `src/main/resources/assets/slavicmyths/lang/ru_ru.json`
- `src/main/resources/assets/slavicmyths/models/item/birch_bark_scroll.json`
- `src/main/resources/assets/slavicmyths/models/item/thunder_stone.json`
- `src/main/resources/assets/slavicmyths/models/item/warding_charm.json`
- `src/main/resources/assets/slavicmyths/textures/item/birch_bark_scroll.png`
- `src/main/resources/assets/slavicmyths/textures/item/thunder_stone.png`
- `src/main/resources/assets/slavicmyths/textures/item/warding_charm.png`
- `src/main/resources/data/slavicmyths/advancements/birch_bark_scroll.json`
- `src/main/resources/data/slavicmyths/advancements/recipes/birch_bark_scroll.json`
- `src/main/resources/data/slavicmyths/advancements/recipes/thunder_stone.json`
- `src/main/resources/data/slavicmyths/advancements/recipes/warding_charm.json`
- `src/main/resources/data/slavicmyths/advancements/thunder_stone.json`
- `src/main/resources/data/slavicmyths/advancements/warding_charm.json`
- `src/main/resources/data/slavicmyths/recipes/birch_bark_scroll.json`
- `src/main/resources/data/slavicmyths/recipes/thunder_stone.json`
- `src/main/resources/data/slavicmyths/recipes/warding_charm.json`
- `src/main/resources/pack.mcmeta`
- `tools/create_resources.py`
- `tools/verify_resources.py`

Сборочный результат: `build/libs/slavicmyths-0.1.0.jar`.
SHA-256: `41BECA816074B070DBA364023CC3DFA6DA1D0779BE7D9EA60D3B0962EBC5B3A3`.

Локальные служебные файлы: `.tools/mdk.zip`, распакованный официальный MDK,
`.tools/jdk8.zip`, распакованный Temurin JDK 8u504-b01, `.tools/build.log`,
`.tools/create_resources.py`; Gradle создал рабочие кэши `.gradle/`, `build/`
и кэш зависимостей в профиле пользователя. Они не являются исходниками мода
и не входят в JAR. Воспроизводимый генератор ресурсов сохранён в `tools/create_resources.py`.

## Измененные файлы

Ранее существовавшие пользовательские файлы не изменялись. Все перечисленные файлы проекта новые.
В скопированном wrapper properties версия Gradle заменена с 8.4 на 7.3.3 для Java 8.
Git-репозиторий в рабочей папке отсутствует; commit не создавался.

## Проверка сборки

Реально выполнено: `gradlew.bat build --console=plain`, JAVA_HOME на локальный JDK 8.
Результат: **BUILD SUCCESSFUL in 3m 13s**, код возврата **0**, 7 задач выполнено,
включая compileJava, processResources, jar, reobfJar. Лог сохранён в
`docs/verification/gradle-build.log`.

`python tools/verify_resources.py`: **PASS**. Проверены JSON, связи локальных ресурсов,
переводы, PNG CRC/16×16/RGBA, наличие и совпадение ресурсов в JAR,
подстановка версии mods.toml и class major version 52 (Java 8).
Лог: `docs/verification/resources-check.log`.

Gradle test: **NO-SOURCE**, автоматических игровых тестов и JUnit-тестов нет.
Структурная проверка не является проверкой Minecraft JSON-парсером в игре.

## Проверка запуска

**НЕ ПРОВЕРЕНО.** Клиент Minecraft и выделенный сервер не запускались.
Иконки просмотрены как файлы, но не в инвентаре Minecraft. Процедура проверки — TESTING.md.

## Ошибки

Ошибок компиляции, упаковки и структурной проверки ресурсов не обнаружено.
При первичном обследовании команда java отсутствовала в PATH; для сборки загружен
переносной JDK 8. Проверка Git подтвердила отсутствие репозитория в Documents.
Глобальные настройки Java не менялись. Игровые ошибки пока неизвестны.

## Предупреждения

- Gradle сообщил об устаревших возможностях, несовместимых с Gradle 8; wrapper закреплён на 7.3.3.
- ForgeGradle вывел стандартное уведомление о лицензии official mappings Mojang.
- Access Transformer сообщил о замене промежуточного выходного JAR в своём кэше; сборка завершилась успешно.
- RARE у громового камня определяет оформление, не редкость генерации. Генерации пока нет.
- Связь advancements визуальная: parent не требует предварительного выполнения родителя.
- Мифологические связи и рецепты здесь авторские; документ не выдаёт реконструкции за доказанную историю.

## Что осталось сделать

- Запустить клиент и выделенный сервер с версией 0.1.0.
- Проверить предметы, крафт, книгу рецептов, достижения, локализации и сохранение по TESTING.md.
- Зафиксировать игровой результат в PROJECT_STATUS.md и исправить реальные найденные дефекты.

## Следующий шаг

Один следующий этап: полный игровой smoke-test 0.1.0 на клиенте и выделенном сервере,
без добавления новых механик.
