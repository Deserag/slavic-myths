# Установка 1.9.7.60 в тестовый PolyMC

По прямому запросу владельца. Точные пути, версии, source commit + dirty manifest, SHA256 и backup: [installed.json](installed.json).

REQUIREMENT | EVIDENCE | RESULT
--- | --- | ---
Установлен production JAR | Файл installed_jar существует; SHA256 совпадает с production артефактом | PASS
Сохранён предыдущий JAR | Backup существует; SHA256 проверен | PASS
Нет двойных JAR мода | Проверка mod ID при установке; duplicate_slavicmyths_jars=0 | PASS
Источник прослеживается | source_commit + DIRTY + source_manifest_sha256 в квитанции | PASS
Запуск при установке | launches_this_install=0; игра не запускалась | NOT_RUN (не требуется для установки)
Ручная приёмка владельцем | Ожидается; статусы задач не закрыты | NOT_RUN
