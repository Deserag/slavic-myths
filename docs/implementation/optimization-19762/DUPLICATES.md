# Duplicates and compatibility

Old path | Shared/current path | Calls/contract | Decision
--- | --- | --- | ---
LoreScreen | TaleScreen | LoreNetwork legacy payload still registered; LoreBookItem uses TaleService | KEEP protocol and legacy adapter, shared UiText.Layout
Knowledge.ENTRIES depth_gift12/13 | vanilla advancements | integer mask positions14..28 would shift if deduped | KEEP both bits; no migration
FolkUi button and LoreScreen wrapping | UiText.Layout | local content/width/rows/font/language keys | CONSOLIDATE calculation, preserve visuals
ClassScreen per-frame tree map | init treeIndex | same current tree nodes | MOVE to init
PeruniteArmor/PolozArmor | independent art/UV | superficially similar meshes, intentional identities | KEEP
CargoScreen/PouchScreen | independent menu types | similar frame, different ownership/inventory | KEEP; no runtime evidence for rewrite
ClassEvents/RpgEvents shield hooks | class XP vs rune reaction | different authority and side effects | KEEP, not duplicate rewards
Yaga quests/Bayun/Volk | existing TaleData/YagaData/WolfProgress | independent persisted story meanings | #236 extends, not a parallel quest book

Clone candidates: ../book-performance-20261024/clone-candidates.json; token overlap is only a signal, not proof of dead mechanics.
