# План логики Codex на основании реальных хуков проекта

## Event graph (логический, не навязывает названия Java классов)
```
MOR_OBSERVED -> journal chapter notice
MOR_DEFEATED -> persist kill -> if known Yaga/Bayun && !invitationShown: invitation
INVITATION_ACCEPTED -> safe hut lookup/teleport -> porch session
ARRIVED_HUT / MANUAL_INTERACT_YAGA -> evaluate trust -> scene (Yaga mandatory, Bayun/Wolf optional)
PORCH_INTRO_COMPLETED -> stage Q1 ACTIVE
MOR_KILLS>=3 -> Q1 READY
YAGA_TURN_IN_Q1 -> give ward -> Q2 ACTIVE
DUST>=3 + YAGA_TURN_IN_Q2 -> spend dust -> Q3 ACTIVE
LIKHO_DEFEATED && (Q3 ACTIVE OR valid deferred research) && YAGA_EYE_PROOF -> YAGA_RESEARCH_LIKHO (powder early, once)
LIKHO_DEFEATED && OVINNIK_DEFEATED && TROPHIES_CHECK -> Q3 READY
YAGA_TURN_IN_Q3 -> tracker reward -> Q4 ACTIVE
HORN_CRAFTED/PRESENT/USED -> Q4 READY
YAGA_TURN_IN_Q4 -> Q5 ACTIVE
YAGA_WARNING_ACK -> Q5 COMPLETED, Q6 waiting for evidence
POLOZ_DEFEATED_WITH_CREDIT -> Q6 READY
YAGA_RESEARCH_LIKHO -> cleansing unlock + 4 powder + chapter (independent of Ovinnik/Poloz)
INFECTED_BLOCK_RESTORED -> restore_earth_20 +1 (exactly once per block)
YAGA_TURN_IN_EARTH_20 -> +8 powder + 175 XP (once)
YAGA_TURN_IN_Q6 -> final story chapter/XP, no duplicate cleansing unlock
```

## Изолированное исполнение
- Никакого `scanAllWorldEntities()` в каждом player tick. Только точечная обработка существующих событий, кэширующие фильтры по моровому tag/entity ID.
- Реестр имён/ключей записей один и общий для UI, диалога, серверной state machine.
- При обновлении quest state отправить **только дельту** конкретному игроку, не перепосылать полный NBT каждые 20 ticks.
- При открытии журнала делать snapshot состояния с сервера.
- Client для highlight tracker формирует визуальный контур по серверному разрешению/ограниченному фильтру, не способен выдать себе quest reward.

## Устойчивость к пересечению состояний
- Триггер invite приходит только один раз при выполненном precondition.
- Перенос запускает сцену ровно 1 раз при подтверждении прихода.
- Прямой визит к Яге запускает ту же сцену, без другой ветки наград.
- Игрок, который раньше убил Полоза, проходит исследовательские реплики и закрывает Q6 без повторного боя.
- Смерть/дисконнект во время записи Q2/Q3 не уничтожает предмет/награду дважды.
