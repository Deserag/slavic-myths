from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/higher-1.8.2';gate=json.loads((O/'resource-gates.json').read_text(encoding='utf-8-sig'));receipt=json.loads((O/'polymc-installation.json').read_text(encoding='utf-8-sig'))
changed=['gradle.properties','CHANGELOG.md','docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md',*('src/main/java/org/slavicmyths/'+p+'.java' for p in ['entity/LandSpiritEntity','entity/OvinnikEntity','hunt/HuntMob','hunt/WorldBoss','hunt/EmberClump','mor/MorController','mor/MorRules','mor/MorAudio','mor/MorEvents','mor/MorContent','mor/HigherMor','mor/HigherRules','mor/HigherSnapshot','mor/MorZone','mor/MorRejection','client/HuntModel','client/HuntRenderer','client/WorldBossModel','client/WorldBossRenderer','client/MorZoneRenderer','client/ClientSetup']),*('tools/'+p for p in ['higher_mor_assets.py','mor_assets.py','higher-offline.gradle','higher-tests/HigherChecks.java','higher_preview.py','verify_higher_mor.py','install_higher_mor.ps1','report_higher_mor.py']),'src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/lang/en_us.json']
changed += ['src/main/resources/assets/slavicmyths/textures/entity/mor/'+name+'.png' for name in ['likho','likho_forming','ovinnik','ovinnik_forming','ground_likho','ground_ovinnik','cloud']]
(O/'changed-files.json').write_text(json.dumps(dict(version='1.8.2',implementation_files=changed,specification_directory='docs/tasks/1.8.2',verification_directory='docs/verification/higher-1.8.2',scratch_directory='work/higher-1.8.2',git_commit_created=False),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
report=f'''# Slavic Myths 1.8.2 — Высшие моровые

Реализация, build, офлайн-gates и установка PolyMC: **PASS**. Ручная игровая приёмка: **NOT_RUN**. Автоматических запусков клиента/сервера: **0/0**. Коммитов нет; изменения существующей рабочей копии сохранены.

## Реализовано

- Существующие Лихо/Овинник получают усиленное состояние на том же EntityType/UUID: отдельная геометрия, материалы, багровый светящийся глаз/глаза, трещины, рваные одежды/доски/солома/длинные когти, красно-чёрный миазм. Обычные формы и их ресурсы сохранены.
- Лихо: 640 HP, замедленное преследование и более частое сближение после половины HP, луч с подготовкой 30 тиков и фиксированным направлением, площадной удар с подготовкой 22, циклическое облако Хвори радиусом 5; переход при <=50% HP сохраняется.
- Овинник: 350 HP, 3 сгустка на тиках 24/29/34 после подготовки, вспышка частиц и звука при столкновении без разрушения блоков, тяжёлые когти, зольная завеса со Slowness I/Blindness; земля во второй фазе появляется вдвое чаще.
- Земля радиусом 3 — серверная временная зона и клиентские плитки/сухая растительность/миазм, без перезаписи блоков. 60 тиков непрерывного нахождения до Slowness I и 1 урона раз в 20 тиков; облако наносит 2 урона раз в 20. Земля 180 загруженных тиков, облако 120; максимум 12 зон/моб. Смерть удаляет локальные зоны, уход/отсутствие владельца удаляет их через 40 тиков. Видимость/загруженные чанки проверяются.
- Отторжение: стандартные локальные target/avoid goals для обычных Enemy, животных, жителей, NeutralMob и нейтральных духов; приручённые и активные моровые исключены. Высшие защищаются от Enemy, S/A игрок поблизости имеет приоритет. Нет глобального обхода мира/игроков каждый тик.
- Реальные vanilla-звуки с пониженным pitch/volume; анимации idle/walk/melee/подготовки/выпуска/облака/фазы/hurt/death. RU/EN, расширенная подсказка склянки, совместимое NBT. Источники: [ART_AUDIO_SOURCES.md](ART_AUDIO_SOURCES.md).

## Проверки

Команда: `gradlew.bat clean build -I tools/higher-offline.gradle -I tools/ui-offline.gradle higherOfflineChecks uiLogicChecks --offline`, JDK 21.0.12. **BUILD SUCCESSFUL**, Minecraft 1.21.1 / NeoForge 21.1.255 сохранены. Один финальный clean build; ранее один build с неудавшейся проверкой изолированного classloader и последующие исправленные офлайн-прогоны. Ошибка была в изоляции package-private интерфейса тестом, исправлена; production компиляция прошла. Итоговый лог: `work/higher-1.8.2/clean-build-final.log`.

{gate['offline_assertions']} офлайн-проверок, {gate['native_profiles']} настоящих ModelPart/UV профилей (большинство проверок — отдельные вершины/UV), {gate['ui_checks']} UI helpers, {gate['checks']} production/resource gates, {gate['event_subscribers']} event subscribers без пустых обработчиков. Проверены сохранение/границы NBT, неизменность обычных моделей Лиха/Овинника относительно установленной 1.8.1 и прежних четырёх моровых относительно 1.7.9, ссылки моделей/текстур, JSON/PNG, Java21 bytecode, отсутствие тестов в JAR, синхронность build classes/resources/JAR, идемпотентность обоих генераторов. Старые ресурсы неизменны кроме двух lang JSON (добавления и расширенная подсказка); старые registry IDs сохранены.

[Машинные gates](resource-gates.json). Офлайн-виды: [Лихо](likho-offline-views.png), [Овинник](ovinnik-offline-views.png). Это не игровые скриншоты и не подтверждение баланса, реального AI, аудио, MP, перезахода или измеренного MSPT.

## Размеры и доступность

В текущем моде обычное Лихо уже имеет 3.25 блока высоты. Оно сохранено; высшая форма 1.4×4.4, визуальный масштаб 1.35. Примерные 2.7 блока из ТЗ не применены, поскольку уменьшили бы существующее Лихо. Овинник 1.65×3.1, масштаб 1.25×1.15×1.25. Окончательное соответствие референсам требует ручного осмотра в игре.

Новых яиц/независимых видов/естественного спавна нет. В creative: призвать обычного моба и бросить склянку мора; именованные/закреплённые охотой/приручённые защищены. Прямой отладочный призыв в отдельном тестовом мире:

```mcfunction
/summon slavicmyths:likho_one_eyed ~ ~ ~ {{"slavicmyths:mor_state":2}}
/summon slavicmyths:ovinnik ~ ~ ~ {{"slavicmyths:mor_state":2}}
```

Для проверки боя перейти в survival. Чтобы проверить фазу 2, снизить здоровье ниже половины и дождаться перехода; не использовать существующий пользовательский мир для испытаний.

## JAR и PolyMC

Production: `{receipt['source']}` ({gate['size']} байт).

Установлен: `{receipt['installed_jar']}`.

SHA256 обеих копий: `{gate['sha256']}`.

Предыдущий JAR: резервная копия `{receipt['backup']}`. Побайтно сохранены {receipt['other_files_preserved']} остальных файлов профиля; конфигурации/миры и старый 1.16.5 не менялись. PolyMC и игра не запускались. [Квитанция установки](polymc-installation.json), [описание установки](POLYMC_INSTALLATION.md).

## Ручная проверка

1. Обе обычные формы и высшие: размер/хитбокс, единственный глаз Лиха, глаза/когти Овинника, видимость атак и звук.
2. Луч с уклонением и стеной, площадной удар, ровно 3 угля без повреждения мира.
3. Земля: три секунды ожидания, выход сбрасывает экспозицию, два визуальных стиля, автоматическое исчезновение после ухода/смерти.
4. 50% HP: разовый переход, повторные облака с перерывами, чаще преследование и земля Овинника; окна для ближнего боя.
5. Отторжение с зомби/скелетом, животным/жителем/нейтральным духом; приоритет игрока, исключение приручённых/моровых.
6. Сохранение/реальный перезаход, выгрузка/загрузка чанка и MP; измерить MSPT на отдельном тестовом мире. Эти проверки **NOT_RUN**.

## Файлы

Полный перечень: [changed-files.json](changed-files.json). Основные группы: `mor/HigherMor`, `HigherRules`, `HigherSnapshot`, `MorZone`, `MorRejection` и интеграция MorController; размеры/AI в LandSpiritEntity/OvinnikEntity/HuntMob/WorldBoss; клиентские модели/рендеры и MorZoneRenderer; 7 новых PNG, RU/EN; версия, генераторы/проверки/установщик и документы проекта. Архив ТЗ сохранён без изменений в `docs/tasks/1.8.2`.
'''
(O/'REPORT.md').write_text(report,encoding='utf-8');print('Final 1.8.2 report written; installation '+receipt['status'])
