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
| source в main, следующий свободный test version | gradle.properties; git history | IN_PROGRESS |
| Java21 clean build и offline gates | TEST_PREVIEW_BUILD.json после фактической сборки | NOT_RUN на момент подготовки |
| backup/one JAR/installed SHA/version | TEST_PREVIEW_1.9.7.10.json после фактической установки | NOT_RUN на момент подготовки |
| запуск Minecraft/PolyMC/GameTest/server | запрещён;0 запусков | NOT_RUN |
| слуховая/игровая приёмка | только владелец | NOT_RUN |
