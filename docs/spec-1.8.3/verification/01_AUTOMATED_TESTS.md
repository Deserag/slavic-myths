# Исполнимые тесты/проверки до PolyMC

## Блоки 1.8.3.1
1. Все 12 IDs, item/blockstate/model/texture/loot/ru_ru/en_us доступны в JAR, исходные vanilla материалы не перетекстурены и не переопределены.
2. Conversion API: только конкретный блок, axis/leaf/persistent/waterlog сохранены, unsupported/BE не заменяются, во время unloaded chunk не пишут.
3. Контакт игрока 2.9sec без эффекта; 3.1 sec эффекты, 5sec pain tick; уход/Creative сброс; **не суммируется** урон разных блоков.
4. Перезапуск server saved blockstates сохранил — автоматический тест сериализации / fake SaveData roundtrip без заявлений о реальном UI-перезаходе.

## Рецепт/предмет/звук
1. Recipe pattern `["EGH", "MRM", "MMM"]`: расход ровно Eye1 Gold1 Heart1 Horn1 Dust5 и выдача одного Mor horn. Шифты/любая goat_horn variant. Нельзя скрафтить без одного из ингредиентов.
2. Eye/Heart 100% loot при высшей форме, не при базовой; Mor dust с обычных моровых и serpent root, но не иллюзий/призванных.
3. Horn data `hasSummonedOnce`, first free, second costs 4 dust, atomic failure no cost; no item consumed, cooldown 24000 ticks, mobGriefing=false rejection.
4. Проверить наличие/декодирование `.ogg`, 9±0.3 sec total sound scenario, mono/no clipping, корректные SoundEvent links.

## Босс
1. 3000 HP; размеры и collision совпадают с рендером и не дают попасть через невидимую root точку.
2. Фаза 1: head/tail/shards hurt, остальные 0 damage от игрока, лучника, голема, богатыря, модового оружия, AoE. Нет скрытого damage через root UUID.
3. Фаза 2 строго <=2100 HP: 3-сек reveal, body hits work от любых attacker types; фаза 3 строго <=900 HP: баф, всё тело vulnerable.
4. Multiple attackers hit root without duplicate-hit single projectile and without iframe abuse; Sweep attack не наносит 10x сегментный урон.
5. Мировой spawn: radial 200–300 blocks; проверять отдельные land и water случаи; open water exits to shore.
6. Каждая из шести способностей: telegraph, cooldown, effect, targeting, no double-hit.
7. Combat factions: zombie/skeleton/guard/богатырь атакуют, villagers бегут, summoned Mor не атакуют boss; через death AI нормализован.
8. Реальное разрушение обычных деревьев/построек и замена выбранных 12 блоков без повреждения vanilla registry, player chest drops 1 раз и без silent loss.
9. Урон minions drops/XP 0, root drops 1 раз.
10. Save/reload logical simulation active boss no duplication, HP/cooldowns/phase/corrupted blocks stable.
11. Two simultaneous horn activation requests yields exactly one event and one successful cost transaction.

## Команды точечных dev тестов
Следовать реально существующему `/sm test ...`; добавить/адаптировать operator-only (permission >=2) подкоманды:
- `/sm test mor_block <id>` — заразить целевой блок;
- `/sm test mor_block reset` — восстановить прототип под прицелом (только dev);
- `/sm test poloz_spawn land|water` — в тестовом мире сразу создать одну root entity без рецепта, с боевой геометрией;
- `/sm test poloz_phase 1|2|3` — server side выставить HP в середину фазы и обновить state для визуальной ручной проверки, без duplicated phase events;
- `/sm test poloz_weakpoints` — debug particles точных hit AABB на 10sec;
- `/sm test poloz_cancel` — остановить активное событие и призванных миньонов, **НЕ** восстанавливать разрушенные блоки, предупредить пользователя в чате.
- Все команды запрещены простому survival игроку; не вызывать массовую порчу без предупреждения.

## Ни одного запуска клиента
GameTests/headless специализированные разрешены, но клиент/PolyMC автоматом не запускать. Реальный бой, качество анимации и восприятие звука обозначить «РУЧНАЯ ПРОВЕРКА», даже если unit gates PASS.
