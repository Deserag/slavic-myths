# Сборка, проверка PolyMC и ограничения Codex

- Работать с реальным Forge/Gradle/Java стеком из проекта. Известный контекст: MC 1.16.5, Forge 36.2.42, Java 8, modid `slavicmyths`; **фактический `build.gradle` приоритетен**.
- Допустимо: `gradlew compileJava`, доступные tests, `gradlew build`/`reobfJar`, анализ output/log, проверка `assets/slavicmyths` / jar listing, unit-проверка JSON и registered resource paths.
- Запрещено без отдельной просьбы: GUI запуск клиента Minecraft/PolyMC, многочасовая серия инстансов, полная повторная индексация всего кода.
- Созданный production JAR проверить по размеру/manifest/assets/нет лишних dev jars, записать путь и SHA-256.
- Определить **фактически известный** путь к используемому PolyMC instance из текущей документации/скрипта. Если он найден и доступен: резервная копия старого slavicmyths JAR → удалить устаревшие `slavicmyths-*.jar` **только из выбранной папки mods** → скопировать один актуальный JAR. Нельзя затрагивать другие моды/instance. Если путь не установлен — явно сказать `PolyMC update skipped: path not verified`, не угадывать.
- После успешной сборки обновить чеклист/README/continuation; не оставлять `DONE` для не сделанных NBT/loot/эффектов.
