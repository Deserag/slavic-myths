# Внедрение в существующую кодовую базу БЕЗ повторного полного аудита

## Порядок точечного осмотра
1. Корневые build files только для реального MC/Forge/Java stack.
2. Только 1–2 ближайших existing boss implementations (морозко если реально закончен), trial state/entry, server-side payout progression/quest fields. При отсутствии прошлой реализации не изобретать ссылки на классы; реализовать минимально совместимо.
3. Entity registry / render model / animations / sounds и vanilla compatible Minecraft abstractions, существующие в проекте.
4. Quest provider interfaces `BabaYaga`, `KotBayun`, reputation access — точечный вызов существующей выдачи NPC.
5. Existing runes insertion/menu, accessory equip/active skill API, key mappings, packets/capabilities.
6. Existing damage handling, undead category/tags, party/bogatyr owner mapping, per-owner cooldown state, multiplayer session storage.
7. World/local client postprocessing hooks нужны только для `Long Night` и `Fear`, без тяжёлой зависимости и fullscreen shader install.

Не искать все файлы по всему репозиторию каждый раз. Получить список директорий 1 раз и искать по конкретным symbols. Никаких upgrades/gradle dependency churn в рамках этой задачи.

## Рекомендуемые разделённые модули (имена условные)
- `KarachunEntity`, `KarachunTrialController`/session, `KarachunAttackScheduler`, `KarachunRenderer`/animated model.
- `KarachunRavenEntity` + `RavenCircleDiveGoal` and tame/summon faction overlay, raven renderer.
- `WaningEffectService`, `FearService`, per-player `TrialDebuffState`; client-only phantom renderer & local hit testing; `LongNightClientEffect`.
- `KarachunScytheItem` + `ScytheComboController` + `ScytheServerAbilities`, `WaningRuneItem` registration, `BlackWingAmuletItem`, `ShardLastNightItem`, `KarachunTrialTotemItem`.
- `KarachunRewardService` uses persistent shared trial reward ledger, not one custom kill drop table.
- `KarachunSounds`, `sounds.json`, PNG textures and 3D models, `ru_ru/en_us`, visual HUD.

## Подключение и реальная версия Forge
- Использовать `DeferredRegister` только если соответствует текущему стеку; иначе текущий pattern. Проверить все required registry ids, models/blockstates, class constructors/events; не делать методов/типов из другой версии.
- На client side регистрировать рендереры в клиентском setup, headless dedicated server не должен загружать `net.minecraft.client.*` классы через common imports.
- При запуске abilities: client input -> validated server packet -> server checks item/cooldown/target -> effect; core item не должен быть рабочим только в одиночке.
- Отображение offhand disabled синхронизировать точно с server state; разрешить независимую ванильную проверку totеm-on-death, не перенаправлять нативную механику.
- Rune sockets/repair/anvil NBT/enchants не разрушать, mirror existing item capabilities.

## Неигровые проверки и сдача
Выполнить доступные compile/test/production reobf build; если что-то запускает Minecraft или требует GUI — **пропустить**. Убедиться, что новый JAR единственный актуальный по версии; если точный PolyMC instance и права доступны — копировать по проектной инструкции, **не запускать PolyMC**; если точный путь неизвестен, не угадывать. Изменить changelog/checklist/continuation только после фактической реализации.
