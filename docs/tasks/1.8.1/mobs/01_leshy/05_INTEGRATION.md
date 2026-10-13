# Моровой Леший — АДРЕСНОЕ ВНЕДРЕНИЕ
1. Найти фактический класс Лешего/Renderer/Model/Texture/Attributes и его существующие AI goals; расширить state/AI через общий `mor_state`, не заменяя UUID или entity ID. Не добавлять новое яйцо.
2. При переходе ACTIVE один раз поставить stable max-health modifier ×2.5, attack ×1.2, speed ×.95, сохранить current health fraction; динамически снять старые friendly/neutral attack restrictions только для игрока, не менять деревенский/лесной ecosystem behavior на уровне всего мира.
3. В renderer переключать на `mor_leshy.png` и отдельные branch/claw/crack ModelParts только в MOR_ACTIVE/TRANSFORMING; обычный texture/model path незатронут. Сама glow-mask должна работать без post-processing.
4. Cast/root visuals broadcast в пределах tracking chunk и только нужные ключевые start/hit/end, **не send 20 packets каждый тик**. Server authoritatively наносит урон по t24.
5. Проверить, что обычный Леший по-прежнему выполняет свои лесные взаимодействия; mor variant их блокирует там, где несовместимы с агрессией, но ничего не удаляет из old logic.
6. Протокол сцены: original normal -> creative flask -> 50t morph -> active -> save/reload -> active -> one cast roots+one wail -> death existing loot. Обычный лесной спавн чаще остаётся нормальным.
