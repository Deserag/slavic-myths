# G2 — пять особых оружий и приёмов

Техническая реализация, не owner acceptance. Источник: WEAPON_PRODUCTION_CONTRACT_2.0.X.md и #326/#330. Реальные оригиналы PNG не найдены: ART_BLOCKED_REFERENCE. Геометрия по письменным чертежам, не SHA-совпавший оригинал; четыре вида фактических JSON в weapons-source-four-views.png. Before: эти пять registry IDs отсутствовали; существующие шесть Жаровых моделей не менялись.

Пять точных smithing transforms используют существующие template/base/catalyst. Нормализованный износ и custom components сохраняет прежний NetherSmithingMixin. Два rune sockets, открытие через прежнюю систему. RU/EN, Creative, enchantable tags, GUI sprites; арбалет/второй projectile engine не добавлены. Кистень подключён к FlailClient/FlailMotion, нож — к WeaponProjectile и его Loyalty/потере, лук — к FieldBow/native arrows. Двуручный меч двигает обе настоящие руки при пустом offhand, занятый offhand даёт −6 урона и запрещает special. Обычные удары и выстрелы остаются доступны на долгом cooldown навыка.

## Инженерные решения / BALANCE_PROPOSED

- Для ножа special: Weakness I 60t и −2 ARMOR 60t без сложения; обычный20% Weakness I40t, target ICD80t. В комментариях к Issue окончательных чисел не найдено. Это не утверждённый владельцем баланс.
- Сильное поджигание160t (8с), отличимо от vanilla Flame5с. Реальный временный огонь60t, только подтверждённый природный пол новых островов, четыре позиции в радиусе1,5. Вне безопасных условий постановка отказывается; основной урон остаётся. Владельцы claims могут отменить EntityPlaceEvent.
- Обычный заряженный удар кистеня14,444 raw: прежнее соотношение10/9 к13, не новый независимый движок.
- Долгий cooldown хранится у игрока и в carried stack custom component, показывается actionbar. Не применён vanilla item cooldown ко всему предмету: он запретил бы обычные выстрелы/заряд на время навыка. Короткий прежний метательный cooldown сохраняется.
- Навыки melee используют EnchantmentHelper.modifyDamage и post-effects по одному разу; не имитируют повторную Player.attack. Стрела нормализует base raw по текущей скорости до native impact/enchant/crit. Пробитие не умножает поджигание (InfernalHitDone).

## Проверки

G2 clean build и native pure тест1233 проверок скорости/округления стрелы; SOURCE bounds/recipes90 проверок. Шесть Жаровых exact recipes/геометрия regression PASS. Это не подтверждает live hitboxes, PVP, Loyalty, навыки, реальный cooldown/relog/MP, хват, resource reload или enchantment UI — всё RUNTIME NOT_RUN. Проверки блокировки союзников/стен реализованы в production, но game runtime не запускался.
