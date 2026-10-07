# Slavic Myths 0.9.10 RC1 — сборка для игроков

Здесь только файлы для установки: готовый мод, список зависимостей, загрузчик и документация. Исходников, Gradle, миров, логов и персональных настроек нет.

## Установка на Windows

1. Нажмите Code → Download ZIP и распакуйте архив.
2. В этой папке откройте PowerShell и выполните:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\DOWNLOAD_MODS.ps1
```

Загрузятся Curios, JEI, Jade, Xaero Minimap, Xaero World Map и FallingTree из закреплённых официальных файлов. Каждый JAR проверяется по SHA-256. Вместе с Slavic Myths папка mods содержит 7 модов. XaeroLib уже встроен в JAR карт.

Для минимального набора Slavic Myths + Curios:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\DOWNLOAD_MODS.ps1 -CoreOnly
```

3. Создайте отдельный профиль Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21. Скопируйте содержимое mods в mods этого профиля. Другой JAR Slavic Myths в профиле нужно убрать вручную.
4. Запускайте игру сами. Скрипт не запускает игру и не меняет существующие профили, конфиги или миры.

Curios обязателен; остальные моды опциональны. На другой ОС скачайте закреплённые файлы по MODS_LOCK.json и проверьте hashes. Полный комплект можно получить без git clone через ZIP этой ветки; такой же архив прикреплён к prerelease.

Это тестовая сборка. Реальная игровая/визуальная приёмка ещё не выполнена. См. REMAINING_WORK.md и KNOWN_ISSUES.md. Issues: https://github.com/Deserag/slavic-myths/issues

Импортируемый launcher pack (.mrpack): https://github.com/Deserag/slavic-myths/releases/download/v0.9.10-rc1/slavicmyths-0.9.10-rc1.mrpack — для Prism/других лаунчеров с поддержкой Modrinth packs. Launcher сам загружает закреплённые зависимости; это не запускает игру.
