# Baba Yaga — narrow compatible changes NOW, quest union LATER

Baba Yaga already exists in actual mod. Do not recreate, retexture, reanimate, rescale, reassign ID, rewrite boss/quest rewards or change hut generation. Preserve existing relationships, dialog, spells, mortar/broom movement, whatever is actually present after code audit.

Current final 1.6 implementation MAY:
- Register Book encyclopedia page from existing Yaga entity/known event with existing history and discovery triggers; ensure lore unknown/seen/finished statuses use real quest/combat facts.
- Build a read-only adapter into Book for Yaga's **already existing** current quest data, if they are exposed by stable API. Never mutate original flags.
- Wrap existing Yaga quest status with a minimal interface `QuestProviderView {providerId, displayNameKey, readOnlyActiveQuests(player), readOnlyCompletedQuests(player)}` if truly needed; keep old storage and reward code intact. Use `baba_yaga` as ID only if actual source confirms; otherwise reuse actual ID.
- Render book UI labels pointing to Yaga and current known quests; future Baюн shared mission integration is not present or visible.

NOT NOW: cross-credit for same boss, two providers finishing a quest via one kill, retroactive rewards, merged quest ID scheme, modified Yaga rewards, new Yaga quest chain, rumors of 1.7 heroes, refactor of unrelated `BabaYagaEntity` AI. All are the NEXT standalone mini-patch. Use `11_NEXT_MINIPATCH/BOUNDARY.md` as notes only, do not implement it.

Mandatory regression: an older world with completed/active Yaga quests still shows original status; old rewards cannot be claimed again due to update; Yaga dialogs and location work; interacting with Bayun does not alter any Yaga data; aborting Bayun hypnosis does not disable Yaga AI. No fake reconciliation if no data exists.
