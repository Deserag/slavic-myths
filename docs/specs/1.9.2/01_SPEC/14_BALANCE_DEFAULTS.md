# Таблица конфигурации: предложенные default-числа

Не выдавать эти настройки за числа, отдельно утверждённые пользователем, кроме 5% завершения и согласованных качественных способностей. Конфигурация **серверная**; числовые величины легко менять без переписывания AI. Пользователь после тестирования уточнит.

| Ключ | Значение по умолчанию |
|---|---|
| `karachun.maxHealth` | **1200 HP — предложение**, реальный баланс после теста |
| `karachun.yieldHealthFraction` | **0.05** (согласовано) |
| `karachun.auraRadius` | **10 blocks** (согласован ранний вариант как предложение) |
| `karachun.ambientFearMax` | 100 |
| `karachun.waningThresholds` | 25, 50, 75 |
| `karachun.waningNearGain/mid/far` | 8 / 5 / 2 points/s |
| `karachun.waningDecayOut` | 6 points/s after 2sec |
| `karachun.initialRavens` | **4** |
| `karachun.maxRavens` | **4** |
| `karachun.ravenHP` | **20 HP** |
| `karachun.ravenHitDamage` | 2–3 HP default |
| `karachun.ravenResummonCooldown` | 35sec |
| `karachun.longNightSeconds` | 15sec |
| `karachun.longNightCooldown` | 35sec |
| `karachun.lastHourWindup` | 6sec |
| `karachun.finalPhaseHealthFraction` | 0.25 |
| `karachun.rewardScytheChance` | **0.15 — предложение** |
| `karachun.rewardWanRune` | 1, 100%, без других runes |
| `karachun.rewardLastNightShard` | 1, 100% |
| `karachun.rewardBlackWingFirstClear` | 1, 100% first |
| `blackWing.ravenCount/duration/cooldown` | 2 / 20sec / 150sec |
| `scythe.baseDamage` | ~11–12 (проверить баланс) |
| `scythe.lifeEaterHeal/cooldown` | 2 HP / 5sec |
| `scythe.soulHarvestHits/withering` | third hit / Wither I 4sec |
| `scythe.lastBreathRadius/cooldown` | 4 blocks / 18sec |
| `scythe.blackHarvestProjectiles/cooldown` | 3 / 22sec |
| `scythe.undeadBonusDamage/healKill` | 25% / 3 HP |
| `runeWaning.procChance/gain` | 20% / +25 waning |

**Настройки безопасности:** cap stack на клиентах, лимит 3 фантомов, лимит активных настоящих птиц, no AoE hit duplication; отслеживать ownerUUID/sessionId на server; не нарушать Forge save NBT. Профилирование — не сканировать мир глобально каждый tick, обновлять маленькие списки участников и активных summons, particle packets батчить.
