# Not now: stand-alone future Bayun ↔ Baba Yaga mini-patch

IMPORTANT: THIS IS SCOPE DOCUMENTATION, NOT AN IMPLEMENTATION TASK FOR CURRENT 1.6 PATCH.

NEXT mini-patch objective: both characters can provide autonomous and occasionally overlapping quest objectives; if player previously performed same unique boss defeat (even before accepting a given quest), either NPC independently recognizes it but pays only their own reward ONCE. Existing Baba Yaga and Bayun quest IDs, reward items and save data MUST be audited at that time. Keep provenance facts separate from provider claim receipt: `Fact(player, bossId, defeatedAt)` + `QuestClaim(provider, questId, claimedAt)`.

Example: Yaga sends player to defeat Лихо for one motive, Bayun sends for a separate motive. If player defeated it beforehand, completion condition already true and only NPC dialogue/claim remains; neither asks to respawn impossible unique boss. A quest can be shown completed yet unclaimed, and other NPC's claimed flag remains unaffected. Preserve Yaga's existing quest chain and relationships. Add future searching bogatyrs in 1.7 as metadata under actual 1.7 entities only.

Do NOT create `JointQuestManager` empty classes or a fake two-NPC quest tree now. Release current patch with working encyclopedia/encounters/achievements first.
