# Исправления по отзыву — Slavic Myths 1.8.3

Production build, native/resource gates и установка PolyMC: **PASS**. Игровая проверка: **NOT_RUN**, клиент/сервер 0/0. Commit не создавался.

## Что исправлено

**Зависшие полоски HP.** Kartaus продолжал выполнять tick во время длинной анимации смерти и снова добавлял игроков в bar после cleanup. Kalin и HuntMob также не имели достаточного death guard. Добавлены проверки !isAlive до обновления/добавления зрителей, скрытие/очистка bar при смерти и callbacks удаления/выгрузки для связанных новых боссов. Сюжетная регистрация победы/лут/анимация смерти сохраняются.

**Оружие богатырей.** Предыдущие согласованные коррекции оставались в review-макетах, что не исправляло production. Теперь изменения находятся в IlyaMesh/AlyoshaMesh/DobrynyaMesh/FinistMesh и BogatyrClient/SkimModel/WitchModel/FinistModel. Руки Алёши удлинены на 5 модельных px, Добрыни 2.5, Финиста 1, Илья без изменения длины. Рукоять закреплена в ладони с единым поворотом предплечья; сабля связная с умеренным изгибом, палица с головкой 6.8px, копьё наконечником вперёд, меч Финиста прямой без старого постоянного наклона 58°. THRUST не отрывает оружие/кисть от родительской цепи. Различные формы/материалы оружия сохранены.

**Цели богатырей.** HeroThreats раньше допускал только Enemy/debug bosses, а LandSpiritEntity не реализует Enemy. Теперь все ACTIVE моровые формы и двойники распознаются общим target filter четырёх призванных героев. Моровые могут защищаться от напавшего героя. Существующие owner/range/LOS/allied/recall/recovery/story проверки сохранены. Созданные яйцом/командой герои без владельца остаются без companion controller; автоматического присвоения owner и обхода лимита героев нет. Пользователь не уточнил способ создания героев, поэтому исправлен штатный путь призванных компаньонов.

## Проверено

`gradlew.bat clean build -I tools/higher-offline.gradle -I tools/feedback183-offline.gradle -I tools/ui-offline.gradle higherOfflineChecks feedback183OfflineChecks uiLogicChecks --offline` — PASS, JDK21.0.12, Minecraft1.21.1/NeoForge21.1.255 неизменны. Дополнительный прогон feedback183OfflineChecks после добавления проверки связности — PASS.

2224988 native vertex/UV/хват/связность checks, 1004 профилей четырёх героев; 677805 регрессионных Mor checks на 145 профилях; 71 UI helpers; 32807 production/resource gates. Подтверждены сохранение старых JAR entries, ограниченный список изменённых классов/4 атласов, отсутствие тестов в JAR, JSON/PNG/texture links, Java21 bytecode, версии metadata, идемпотентность четырёх hero generators и прежних Mor generators. Проверка bar lifecycle/target predicates — source/static gate, не доказательство реального боя или сетевых remove packets. Начальная попытка генерации ошиблась двойным вызовом apply после UV packing, исправлена; production и повторные генераторы PASS.

## Реальные модели

[Алёша](alyosha-actual-model.png), [Илья](ilya-actual-model.png), [Добрыня](dobrynya-actual-model.png), [Финист](finist-actual-model.png) — UV-текстурированные exports реальных игровых ModelPart, использующих те же модели/контроллеры, которые включены в JAR. Это не новые концепты и не скриншоты игры.

## Артефакт и установка

JAR: `D:\slavic-myths\build\libs\slavicmyths-1.8.3.jar`, 16527224 байт.

Установлен: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.8.3.jar`.

SHA256: `255ee69304b6014a8d7ce6787ff04687bcee774cea84059e0dc184e0241f87c2`.

Backup 1.8.2: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-1.8.3-20261012-160802`. 336 остальных файлов профиля побайтно сохранены; config changes 0, миры и старый1.16.5 не затронуты. Пользователь подтвердил закрытие PolyMC; установщик повторно проверил процессы. [Квитанция](polymc-installation.json), [gates](resource-gates.json).

## Ручная приёмка

1. Убить Kartaus/Kalin/Овинника и дождаться анимации смерти: полоска должна исчезнуть сразу и не возвращаться; также проверить удаление/выгрузку боссов.
2. Осмотреть всех четырёх героев спереди/сбоку в idle, атаке, ходьбе и питье: рукояти в руках, клинки цельные, оружие/кисти не отрываются.
3. Вызвать богатыря предметом призыва и поставить морового в 14–16 блоках с прямой видимостью вне активного сюжетного боя: проверить приобретение цели, урон, защитную реакцию и отсутствие атак на обычных нейтральных духов.

Все три игровых сценария пока NOT_RUN. Компиляция и native bake их не заменяют.

## Изменённые файлы

Gameplay: HuntMob, KartausEntity, KalinEntity, IdolishcheEntity, MarinkaEntity, SkimBeastEntity, HeroThreats, MorController, HigherMor, LandSpiritEntity. Client: четыре Hero Mesh класса, четыре pose controllers и четыре entity atlas PNG. Генераторы: hero_grip_style.py, hero_visual_style.py; версия gradle.properties; инструменты feedback183-offline.gradle/feedback183-tests, feedback183_preview.py, verify_feedback183.py, install_feedback183.ps1. PROJECT_STATUS/ROADMAP/ARCHITECTURE/CHANGELOG и этот отчёт обновлены. Исторические review-макеты не выдаются за установленную реализацию.
