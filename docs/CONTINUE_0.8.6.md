# Продолжение после 0.8.6

Проект `D:\slavic-myths`, Java 8 / Forge 1.16.5-36.2.42. Новый пакет реализован
в существующей системе курганов. Главный босс включён в 0.8.6 согласно новому ТЗ.
Новый геймдизайн, классы, броня, ключи/головоломки и новые dungeon не добавлялись.

Начать с PROJECT_STATUS/ROADMAP/ARCHITECTURE и актуального README. Игровых
запусков 0, ручной QA TODO в MANUAL_QA_0.8.6.md. Не выдавать headless проверки
за игровой бой/перезаход. Не запускать Minecraft без нового запроса пользователя.

Код: kurgan/KurganCreature, KurganFighter, KurganRoster, KurganEncounters,
KurganEncounterState, KurganBolt; расширены KurganInstance/BurialRecords,
KurganCurse/Disturbance/Debug, существующие registries/ClientSetup. Модели и
renderer только в client. UUID живых участников сохраняются; отсутствие
загруженного entity не считается смертью, triggered запрещает повторный спавн.

Ресурсы: `python tools/kurgan_086.py` отдельно. Старый глобальный генератор
может затронуть пользовательское оружие, поэтому в этой итерации не запускался.
Сохранять четыре модели и 256px атласы battle_axe/carved_staff/club/retainer_shield.
Sound manifest содержит 47 разрешённых vanilla fallback событий, собственные
OGG для новых мобов не заявляются.

Проверки: Java8 `gradlew.bat --offline clean build verifyKurgan`, затем
`python tools/verify_kurgan_086.py` и `python tools/verify_resources.py`.
verifyKurgan проверяет 1200 планов, 900 population решений, фазы/профили,
встречи/NBT/миграцию и source-specific снятие проклятия офлайн.

Артефакт/установка: build/libs/slavicmyths-0.8.6.jar, отчёты
docs/verification/headless-0.8.6.json и install-0.8.6.json.
PolyMC: `%APPDATA%\PolyMC\instances\Slavic-Myths-Testing\.minecraft\mods`.
JEI/Curios не менять; старый Slavic JAR архивировать перед заменой. Не commit.
Не очищать пользовательские work файлы или старые референсы автоматически.
