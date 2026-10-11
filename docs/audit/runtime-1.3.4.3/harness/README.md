# Воспроизведение серверного аудита

Тестовые исходники не входят в main/production JAR. Скопировать RareWeaponGameTests.java в .tools/runtime-audit/tests и server.init.gradle в .tools/runtime-audit. Запуск в PowerShell из D:/slavic-myths:

    $env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
    .\gradlew.bat -PrareRuntimeCheck -I .tools/runtime-audit/server.init.gradle runRareChecks --console=plain

Это создаёт отдельный GameTest world. Нельзя заменять gameDirectory игровым миром. Native stub listener/UUID map используется только в fixture. Observation assertions фиксируют дефекты A02/A18, не требуют исправленного поведения. 37 tests:16 прежних (Heat assertion заменён детерминированной Strength), registry export,2Flight,17Floating,1Knowledge. Floating16=4типа×4дистанции плюс1движущаяся цель, разные высоты изолируют чужие атаки. Нет реального client input, process restart, нескольких целей в одном encounter или MSPT measurement.
