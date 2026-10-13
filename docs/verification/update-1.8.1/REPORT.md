# Установка 1.8.1

Собрана существующая рабочая версия1.8.1 без изменения gameplay/UI в этой итерации. Сохранены текущие пользовательские изменения, включая систему Мора. Платформа Minecraft1.21.1,NeoForge21.1.255,Java21.

- gradlew.bat clean build: PASS,86 предупреждений deprecation/unchecked.
- uiLogicChecks: PASS,71.
- morOfflineChecks: PASS,223098 checks/69 native meshes; не игровой запуск.
- Production integrity: PASS,22245 checks;174 непустых event subscribers; Java65,JSON,полнота и совпадение ресурсов/compiled classes.
- Legacy verify_static_data_1211.py: ERROR KeyError biomes; verifier не поддерживает существующие neoforge:none/custom modifiers. Полная семантическая проверка этим инструментом не пройдена.
- Реальных запусков клиента/сервера:0. Gameplay,звук,network/relog и визуальная игровая приёмка NOT_RUN.
- PolyMC Testing:1.8.1 installed,backup1.7.9,274 остальных файлов сохранены; настройки и миры не менялись.

SHA256:eefe22556d5ff83d6dfafd15a56be00d81a30b4898d0af2a4b30eb7739595f68.

Остаются: внедрение согласованного компактного каталога/скрываемого списка книги; возврат графического дерева/явного назначения навыков; длинные подписи; пропорции рук и нейтральный хват оружия четырёх героев/лук; общий лимит двух восстановлений за бой с reset после завершения боя. Эти макеты не включены в установленныйJAR.

Квитанция:polymc-installation.json; проверки:resource-gates.json; журналы:work/update-1.8.1-build.log иwork/update-1.8.1-offline.log. Commit не создан.