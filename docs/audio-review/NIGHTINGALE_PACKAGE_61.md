# #64: Соловей-разбойник и клинок — IN_PROGRESS

AGENT_SELECTED / OWNER_PENDING. Hearing NOT_LISTENED; runtime NOT_RUN.

19 существующих событий получают 21 записанный вариант. Три отдельных фразы
человеческого свиста stilgar заменяют idle / release / air wave; это запись
исполнителя, не осциллятор. Новые реплики Alba MacKenna — недовольный отклик,
болезненные стоны и смерть; это тот же лицензированный исполнитель, уже
используемый в проекте, но отдельные исходные takes, не копии с новым pitch.
Смерть обеих фаз намеренно использует одно исполнение одного действия.

Клинок имеет свои draw/contact/charge/release/hit записи; замах босса,
приземление, прыжок, рывок и вихрь отделены от голоса. Draw — художественный
foley застёжки/ткани ножен, а не документальная запись извлечения этого клинка.
Предметные источники могут совпадать с другими предметами из того же материала.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Живой человеческий свист, права и исходный hash | art/audio/project-61/sources.json: stilgar__soft_whistle.ogg; [author dedication](https://commons.wikimedia.org/wiki/File:Soft_whistle.ogg#Licensing) | PASS |
| Отдельные реакции и предметные действия | selection.json, группа nightingale-recorded / nightingale-weapon; подписанный soundboard.html и review.zip | PASS |
| Начало и момент атаки сохранены | NightingaleEntity.java: begin charge, attackTick release t16, break-away t8, onslaught t46, landing guard; Java не изменён | PASS |
| Реальные события оружия | NightingaleDagger.use/releaseUsing/hurtEnemy, WindAttack.bladeWave successful hurt, BossWeaponSounds equipment change | PASS |
| Registry, NBT, AI, баланс и визуальные ресурсы сохранены | Полный frozen-source/resource gate verify_audio_groups_61.py | NOT_RUN |
| Java21 clean build / decode / JAR parity | Проверка после сохранения production-коммита | NOT_RUN |
| Подходящий настоящий вдох | nightingale_phase1_whistle_charge отключён; нужен записанный вдох, не выдох | BLOCKED |
| Подходящий мужской смех | nightingale_phase2_laugh отключён; общий callback смены фазы и редкого idle | BLOCKED |
| Владелец слушает характер, угрозу и громкость в игре | Клиент/PolyMC/GameTest/сервер не запускать | NOT_RUN |

BLOCKED не означает завершение персонажа. Старые basic/dotted registry IDs и
OGG оставлены для совместимости; новые mappings подключены только к фактическим
underscore callbacks актуального класса плюс dotted landing callback.
Неиспользуемые legacy IDs не объявляются полноценной реализацией.
