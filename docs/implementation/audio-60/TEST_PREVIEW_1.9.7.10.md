# Slavic Myths1.9.7.10 — TEST_PREVIEW / AUDIO_OWNER_PENDING

Разрешение владельца: обновление #61 от19.10.2026 и прямое поручение в чате.
Полный аудиопроект не завершён, выпуск2.0 и owner acceptance не объявляются.

Включает все опубликованные изменения main после установленной1.9.7.9:
- исправленные namespace записанных OGG;
- аудиопакеты духов, фауны, подземелья, курганов, боссов/героев/командиров,
  хранителей, Баюна, псоглавцев и Ночницы, включая уже выбранные Тугарина/Скима;
- раздельные отказ/благодарность Домового и step/contact животных;
- новые weapon windup/contact, draw/щит разбойников. Тяжёлый impact теперь
  срабатывает при успешном попадании, а не до удара; AI/урон/тайминги сохранены.

Русалка остаётся в прежнем карантине; голос Кузи и принятые модели не меняются.
Игоша — только спорные кандидаты вне production. Недостающие песни, breath/voice
роли, remaining routes/provenance и итоговая слуховая проверка остаются в #61–#66.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| source в main, следующий свободный test version | gradle.properties; git history | PASS |
| Java21 clean build и offline gates | TEST_PREVIEW_BUILD.json после фактической сборки | PASS |
| backup/one JAR/installed SHA/version | TEST_PREVIEW_1.9.7.10.json после фактической установки | PASS |
| запуск Minecraft/PolyMC/GameTest/server | запрещён;0 запусков | NOT_RUN |
| слуховая/игровая приёмка | только владелец | NOT_RUN |

Фактически установлено: INSTALLED_TEST_PREVIEW / AUDIO_OWNER_PENDING.
Source commit: `8bf39d787331179f754fa67d86836e31353a8c06`.
Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.10.jar`.
SHA256: `8687b4b2238fbf9c44ec04d8d36719157d2cea210dda00a7db707a44cfe386e8`.
Backup JAR/критичных конфигураций: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-audio-1.9.7.10-20261019-075554Z`.
Активных slavicmyths JAR:1; Minecraft/PolyMC/server/GameTest запусков0.
Подробный проверяемый акт: TEST_PREVIEW_1.9.7.10.json; clean build1m25s,6 offline gates PASS.
