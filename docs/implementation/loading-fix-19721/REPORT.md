# Сбой загрузки 1.9.7.20 — исправление 1.9.7.21, IN_PROGRESS

Владелец запустил установленную 1.9.7.20 и получил экран ошибки; latest.log подтверждает:
`YagaCommands has no @SubscribeEvent methods, but register was called anyway`.
[Наблюдавшаяся ошибка](observed-error.txt). Сам Codex игру/сервер не запускал.

Причина: в #74 обработчики RegisterCommandsEvent перенесены в SlavicCommands,
но у 31 класса без оставшихся событий сохранилась @EventBusSubscriber.
Автоматический загрузчик NeoForge регистрирует эти пустые классы и отклоняет их.
Офлайн-проверка Brigadier этого раньше не покрывала; предыдущий build PASS
не доказывал загрузку мода.

Исправление: аннотации удалены только у 31 пустого contributor. Шесть contributor
с другими событиями сохраняют регистрацию; SlavicCommands — единый обработчик команд.
[Полный список](subscribers.json). Gameplay/IDs и незакоммиченные изменения #75 сохранены.

Регрессионная проверка CommandCatalogOffline теперь:
- подтверждает, что реальный NeoForge EventBus отклоняет пустой класс;
- проверяет все исходные automatic subscribers на наличие обработчиков;
- реально регистрирует SlavicCommands и шесть оставшихся contributor в native bus,
  без запуска сервера и без отправки игровых событий.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Подтвердить причину | Скриншот владельца, latest.log, observed-error.txt | PASS |
| Удалить пустые подписки | 31 аннотация удалена, subscribers.json | PASS |
| Native bus regression + команды | FINAL_BUILD.txt после gates | PASS (offline) |
| Java21 clean build/resource/JAR gates | Акт установки | PASS |
| PolyMC backup/SHA/один активный JAR | Акт установки | PASS |
| Реальная успешная загрузка исправления | Требуется запуск владельцем; Codex запусков0 | NOT_RUN |

Issue #74/#75 не закрываются; ручная приёмка ещё не выполнена. Это тестовое исправление,
не финальная поставка. Миры и 1.16.5 не меняются. Автоматического commit/push нет.

Установлен `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.21.jar`. SHA256 `1e0064b015bb0f4103b5d00f04b1ee855c3346becf9180c755299703e11c9be5`.
Backup: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-19721-20261019-213102`. Source base: `8730595d09f84386d2556a714d7f8126649f3863` + незакоммиченные правки, точный ZIP указан в INSTALLATION.json.
