# Коса по образцу босса — ревизия 2026-10-14

Клинок игрока адаптирован из силуэта tools/karachun_assets.py::scythe: нисходящая параболическая дуга, тёмный металл, фиолетовая кромка, ледяные зубцы, костяные кольца и две подвески. Палитры wood/metal/bone/ice импортируются из генератора босса. Древко34, вылет19.3, толщина лезвия0.16..0.94: пропорции адаптированы под игрока. Модель/атлас/AI босса не менялись в этой ветке. 8 материалов32px, непрерывная закрытая OBJ вместо прежнего независимого полумесяца.

PlayerRenderer больше не заменяется на Reaper во время удара. Клиентский PlayerModel TAIL mixin меняет только исходную модель и рукава; все её зарегистрированные слои остаются. Убрана серверная vanilla swing packet, оставлены собственный Pose и плавная motion/influence0→1→0. Native first-person рука следует тому же движению и держит древко. Опорная рука третьего лица сведена к древку. Зеркальный левый хват сохранён. Базовый урон12 HP, abilities/passives/registry IDs/version1.9.7 не менялись.

## Фактическая проверка

- clean build + nightOfflineChecks PASS (29s):40 assertions,10000 reward simulations,8352 UV vertices.
- После ревизии один GameTest server:37 сценарных проверок PASS, реальные LivingEntity.hurt и projectile collision; транспорт тестового игрока заглушен.
- 5 реальных dev-client запусков на AMD RX6600M/OpenGL4.6. Первые2 запуски дали непригодные первые кадры (пауза и hideGui скрывал руки); третий выявил смещение хвата; четвёртый — направление опорной руки и незакрытый Creative inventory. Пятый исправленный запуск:15 framebuffer PNG, нормальная загрузка OBJ/MTL и материалов, boss/first/third/attack/after/left/FOV70/90/110/GUI/Steve WIDE/Alex SLIM/две способности. Steve/Alex — scripted native RemotePlayer actors и Pose event, не реальная multiplayer-сессия. Общая камера/атака основного игрока использует настоящий Action packet.
- Нули HUD — секунды готовности Breath/Harvest/Wing. Они не являются уроном; 100→88 HP подтверждены GameTest.
- У косы missing texture не обнаружено. Три старых атласных предупреждения Морозко/Полоза вынесены в интеграционную ветку.

Настоящие игровые кадры: [босс](native-client/boss-approved-scythe.png), [первое лицо](native-client/first-person-idle.png), [третье лицо](native-client/third-person-idle.png), [удар](native-client/third-person-attack.png), [после](native-client/third-person-after.png), [Steve](native-client/steve-wide.png), [Alex](native-client/alex-slim.png), [иконка](native-client/gui-icon.png). Прежние images/ остаются явно помеченными программными проекциями.

Дополнительные сцены ходьбы/бега/прыжка/приседания/левой руки/брони/другого оружия предусмотрены opt-in fixture для повторного запуска в итоговой интеграции. tools/smoke никогда не попадает в production. Авторская игровая и независимая ChatGPT приёмка ещё не выполнены; длительная survival/multiplayer/FOV с пользовательскими настройками остаётся ручной.

Эта ветка — только оружейная PR№2. Руны, HP stage1 и завершённый creature-feedback включаются отдельными snapshot commits в test/integrated-197-20261014. main не объединяется автоматически.
