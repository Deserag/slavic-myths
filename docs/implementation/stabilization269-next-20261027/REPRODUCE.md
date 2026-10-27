# Команды и ограничения воспроизведения

Все команды из D:/slavic-myths, PowerShell. Только один игровой процесс одновременно. Никакие команды ниже не следует запускать на личном мире.

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build -PbarrelOfflineCheck verifyFermentationOffline --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024M -XX:ActiveProcessorCount=2'
python -X utf8 tools/verify_current_resources.py
```

Source JAR: 23c2728feff82cbf9af60f89e41edfe7f9f86746. Более поздний commit отчётов/QA не меняет production. Manifest JAR хранит версию; привязка source commit/SHA вынесена в BUILD_RESULT.json.

Клиентская проверка использует **подготовленную локальную копию отдельного QA-мира**, не включённую в Git: work/stabilization269-repair-20261027/saves/Stabilization269. В ней есть ранее реально сгенерированный город с owner chunk 88,63, входом 1378,38,1140 и прежняя дальняя ManualStructureRecords цель. Сам исходный seed не заменяет состояние fixture, историю посещений и UUID. Поэтому запуск Repair269 на произвольном пустом мире не является эквивалентным воспроизведением. Логи и receipts фиксируют подготовку, имена профилей, seed, private quest NBT и тестовые изменения. Личных миров в fixture нет.

```powershell
./gradlew.bat -I tools/stabilization269-client.gradle runStabilization269Client -Pqa269Repair=true -Pqa269RepairMode=visual -Pqa269LowLoad=true -Pqa269ClientDir=work/stabilization269-repair-20261027 -PwithoutJei --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024M -XX:ActiveProcessorCount=2'
```

Режимы baseline/fixed/restart/exhausted/completedRestart требуют соответствующего состояния и последовательности. baseline запускался до production-правки, результаты сохранены; не повторять его против исправленного кода с ожиданием прежних FAIL. exhausted намеренно задаёт старое оплаченное состояние, это явно не реальное второе Survival-прохождение. visual снимает интерфейс без новых наград. Изменения fixture делаются только на копии.

Частотная диагностика: tools/stabilization269/Frequency269.java, Gradle init tools/stabilization269.gradle. qa269Scenario=frequency, qa269Run=frequency-next-0/1/2; соответствующие server.properties содержат seed 2692026/12345/-269 и loopback server-ip=127.0.0.1. Выходные JSON различают UNKNOWN и UNEXAMINED. Первый неограниченный вызов на server thread ошибочен, повторять его не следует. Текущий bounded controller не разрешает выводы о редкости при неполной выборке.

Preview #298: tools/stabilization269-client/Art298.java, свойство qa269Art298=true, тот же отдельный QA-профиль с временным resource pack Chud298-preview. Содержимое pack в docs/implementation/chud298-preview-20261027/Chud298-preview.zip. Перед запуском распаковать pack только в resourcepacks QA-профиля. Скриншоты — реальный Minecraft, production-текстуры не менялись; высокое разрешение эскиза не следует принимать за готовую финальную 16×16 текстуру.

Автономный resource verifier пишет runtime_launches=0, потому что сам не запускает игру. Это не счёт всего задания: реальные запуски перечислены отдельно в REPORT.md и BUILD_RESULT.json. Gradle test NO-SOURCE также не является успешным игровым тестом.
