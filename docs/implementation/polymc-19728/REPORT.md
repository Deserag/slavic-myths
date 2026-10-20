# PolyMC1.9.7.28 — установлен весь текущий production пакет

INSTALLED_TEST_PREVIEW / OWNER_PENDING. Прямой запрос владельца20.10.2026.

Installed JAR: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.28.jar`.

SHA256: `0d5076880f145dc38ea4a74380aa9f9285611708e072ce3485ed8f25bcb7138f`.

Source commit: `e5924bd909113dc587496940b270f963fe259714` + сохранённые локальные UI/command изменения.
Точный полный source snapshot: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19728-20261020-093527\source-snapshot.zip`.
Snapshot SHA256: `668c1ed411726a51dd1d319321919abe19130a95b83651d73a0a8e9e3b284836`.

Backup прежней25 и63 файлов конфигурации: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19728-20261020-093527`.

Установленный JAR совпал с build/libs по SHA и метаданным. До установки повторно
проверены все production resources и compiled classes; полный исходный manifest
совпал с текущим рабочим деревом. Поэтому доставлены все накопленные изменения:
- #90 v3: одна колонка1/2/законные3, без четвёртого/пустого, стабильный owner/UUID порядок;
- корректные цветные поверхности всех напитков, квас/морс и дебафыII7/III12 из26;
- сохранённые локальные UI классов/branch copy и command subscriber fixes предыдущих сессий.

После замены — один активный slavicmyths JAR28.6 остальных файлов mods сохранены
по SHA. Изменено только имя экземпляра в instance.cfg; остальные конфиги/пакет/options
неизменны. Minecraft1.21.1/NeoForge21.1.255. Миры и старый1.16.5 не тронуты.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Полный build/source/resources/classes gates | hero-hud-90-v3/checks.json + installation source-manifest parity | PASS |
| Все текущие изменения в установленном JAR | installed SHA==build SHA,source ZIP и manifest | PASS |
| Backup прежнего JAR и профиля | Проверенные копии,receipt | PASS |
| Один JAR28, правильные version/platform/path | metadata scan всех активных JAR,INSTALLATION.json | PASS |
| Не изменены другие моды/конфиги/миры | hash guards63 configs/6mods; no save writes | PASS |
| Minecraft runtime/owner acceptance | Codex запусков0 | NOT_RUN |

Запусков Minecraft/PolyMC/GameTest/server0. #90 остаётся OPEN до приёмки владельцем.
