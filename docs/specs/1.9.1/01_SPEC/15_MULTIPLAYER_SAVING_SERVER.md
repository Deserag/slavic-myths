# Dedicated server, сохранения, синхронизация, безопасность/производительность

## Сервер — единственный авторитет
Все HP/добавления cold/cooldowns/casting/targeting/projectile impacts/rewards/quest flags/claim store вычисляются на **сервере**. Клиент — только input, визуал, звуки, локализация, HUD. Никогда не получать от клиента `damage`, `reward`, `target UUID` без серверной валидации.

## Сохранения
- `MorozkoTrialSession`: unique UUID, spawn anchor + dimension, owner UUID, boss UUID, start time/ticks, phase, participants/contribution, `rewardedPlayers`, list clone UUIDs, and completion state. Использовать существующие SavedData/Capability или другой текущий механизм; не вводить глобальный статический map, теряющийся после рестарта.
- `PlayerProgress`: permanent unlock totem, completed trial, win count, mirror claimed, item skill cooldowns keyed by per-player UUID + item/action, on demand recovery of lost totem. Нельзя сбросить, перезапустив игру или перекинув вещь из рук в инвентарь.
- `PlayerCold`: temporary cold 0..100 и lastHit times синхронизировать, чистить при смерти/logout/неактивной сессии; не сохранять вечную негативную ауру между измерениями.
- После безопасной перезагрузки в ходе боя: либо корректно восстановить ровно один boss+копии по persisted session, либо **отменить испытание без награды и убрать осиротевшие копии** с коротким сообщением участникам. Строго не допускать `REWARDING` twice. При shutdown во время claim — persist event before UI confirm или transactional idempotency.

## Мультиплеер
- Один boss с общим HP/фазой/3 ледяными двойниками, всеми видимыми для клиентов. Несколько игроков могут вместе биться, каждый получает **свою** руну(одну 50/50), 1 Heart, свой независимый 15% staff roll; Зерцало при первой победе.
- Участие союзных богатырей **разрешено**; damage/aggro учитывается; собственные атаки Морозко могут поражать и героев, aura 50% набора на героев. Владельцы призванных героев получают credit по правилам сессии.
- Ледяные копии игрока от амулета: max1 на player, вне общего лимита вражеских ледяных двойников; у каждой своя owner/session. Только owner инициирует clone mirror action, но видят все клиенты; конфликт чужого урона проверяет PvP rules.
- Снизить частоту expensive entity scans: aura targets iterate only tracked participants/nearby candidates throttled 5/sec; avoid full dimension/all players/world blocks scan each tick. Никакого forced chunk loading 80-block радиуса и глобальной weather-control.
- Сверять, что loot не исчезает/не удваивается при reconnect, full inventory, logout в момент `YIELDING`, death и 10 concurrent users; сложная логика должна иметь проверяемые tests.

## Защита мира
- Нет `setBlock` для льда/сосулек: ледяные пики и телеграфы только particles/temp hitbox; не порча terrain, не уничтожение сундуков, посевов, спаунеров и построек.
- Не поражать игроков за пределами radius 40 trial anchor (вылетевший снаряд должен despawn/не наносить попадание по постороннему); запрет permanent particles/world ticks after cancel.
- Запрет редких permanent NBT records на снарядах/ледяных сколах, не бесконечные эффекты/вечная метель.
- Client resource fallback: все нужные image/audio registries packaged, dedicated server не загружает классы клиентского рендера.

## Минимальные пакеты/обновления
- `C2S`: summon request via item use (стандартный server event), staff G/V requests, stream start/stop, mirror summon activation.
- `S2C`: session state/phase, cold gauge sampled values, cooldown snapshot, summon visual cue, claim outcome; при наличии единой сетевой инфраструктуры использовать её, а не отдельную новую систему.
- C2S валидация: held item, actual equipped amulet, range, cooldown, allowed action, nearest valid target, session/owner; rate-limit даже при спаме/модифицированном клиенте. Слишком частые request игнорировать без бесконечного лог-шума.
