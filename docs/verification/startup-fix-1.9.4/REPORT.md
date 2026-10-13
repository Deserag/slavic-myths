# Исправление загрузки 1.9.4 — 2026-10-13

Пользовательский crash-report `crash-2026-10-13_14.50.38-fml.txt`: регистрация SanctuaryDecor падает из-за скопированной из OAK_LOG функции mapColor, обращающейся к отсутствующему AXIS. Ошибки unbound carved_fire_pillar и guardian_relic_pedestal вторичны.

SanctuaryDecor и аналогичный путь SanctuaryBlock теперь используют постоянный MapColor.WOOD/STONE. Свойства facing/lit, registry IDs, модели, текстуры, баланс и сохранения не меняются. Добавлены 63 проверки создания семи декоративных блоков и восьми состояний каждого. Standalone registry unfreeze применяется только в tools/sanctuary-tests, по существующему образцу mor-blocks-tests; тестовые классы не входят в JAR.

Публикация подготовлена отдельно в `.tools/publish-1.9.4`: начатая 1.9.5 остаётся в основной рабочей папке. Ресурсы сверяются с установленной исходной 1.9.4; изменения Змеиной горы исключены из опубликованной версии по выбору пользователя.

Попытки до финальных gates: неверное имя Gradle task; ошибка компиляции незавершённой 1.9.5; замороженный standalone registry нового теста; сравнение JAR обнаружило изменение StructureCandidates и трёх корневых mixin JSON из 1.9.5 — исключены в отдельной копии. Эти попытки не считаются успешными gates или игровыми запусками.

Финальные gates: clean build PASS; guardians 6429, sanctuary 1455 (63 новых), Karachun 40, Morozko 6946, quest185 182, Poloz 4290, mor-blocks 120 — PASS; production/resource 23304 — PASS. В JAR изменены только SanctuaryDecor.class и SanctuaryBlock.class; все ресурсы побайтно прежние. SHA256 `410407f918508c0b04fef14aca0c7cad7aa39b7d77c4279f2e1088bd870837c3`. PolyMC Testing установлен с backup; 305 остальных файлов сохранены побайтно. Реальных запусков клиента/сервера/GameTest агентом: 0. Игровая проверка NOT_RUN; MSPT/FPS NOT_MEASURED.

Ручная проверка: запустить Testing, убедиться в загрузке меню, войти в отдельный тестовый мир и поставить украшения обоих святилищ.
