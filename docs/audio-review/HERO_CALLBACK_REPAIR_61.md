# #64 / #65: реальные callbacks Алёши и Финиста

IN_PROGRESS / OWNER_PENDING; hearing NOT_LISTENED, runtime NOT_RUN.

Аудит выявил, что записанные `alyosha.hurt_1/2` были зарегистрированы, но
не имели реального callback: AlyoshaEntity/SkimActor не переопределяли
getHurtSound. Добавлен стандартный getHurtSound с чередованием двух ранее
подключённых записанных takes. Отдельный audio counter не использует игровой
RNG и не сохраняется в NBT; после загрузки порядок начинается заново.

`entity.finist.sword_contact` теперь звучит только если существующий вызов
committed.hurt(...) вернул true. Все прежние условия остаются: SWORD, frame8,
allowed target, cone2.7/95, line of sight; damage5 не изменён. Используется
playSound с pitch1, чтобы новый cue не потреблял игровой random draw.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Реальный callback болевой реакции Алёши | AlyoshaEntity.getHurtSound → SkimContent.sound("alyosha.hurt_" + variant) | PASS (source) |
| Контакт меча после успешного damage | FinistEntity SWORD frame8 → successful hurt → sword_contact | PASS (source) |
| Только звук, без изменения AI/урона/кадров/NBT/RNG | tools/hero_audio_routes_61.py; full-file baseline comparison в verify_audio_groups_61.py | PASS |
| Сборка и compiled JAR parity | Java21 clean build50s,6 offline gates, actual compiled class comparison | PASS |
| Реальная слышимость в игре | Владелец проверяет отдельно, Codex не запускает клиент | NOT_RUN |

Это исправление callbacks, не новый принятый голос героев. Неподключённые
recovery/summon/варианты других действий остаются отдельным остатком аудита.

Сборка1.9.7.12/source25adb2c5 сохранена в GitHub. POLYMC_NOT_INSTALLED:
polymc.exe PID10580 открыт; guard остановился до замены/backup. В тестовом
профиле остаётся1.9.7.11. Акт попытки: ../implementation/audio-60/TEST_PREVIEW_1.9.7.12.json.
