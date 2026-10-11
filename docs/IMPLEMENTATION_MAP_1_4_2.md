# Implementation map 1.4.2

Verified checkout:1.4.1, existing dirty source preserved with work/grey-wolf-142/baseline.json. Input spec extracted under work/grey-wolf-142/spec, all numbered documents and3 images reviewed.

| Feature | Verified actual integration |
|---|---|
| Trial/NPC | wolf.GreyWolf, WolfProgress.data/passed; POST_TRIAL_NPC,50HP; existing personal wolf_test_passed. |
| Quest persistence | military.Military.QUESTS serialized CompoundTag attachment,copyOnDeath; extend MilitaryQuests with GreyWolfStory data, no new attachment/questbook. |
| Bandit eligibility | existing slavicmyths:bandits tag:bandit_fighter,bandit_archer,bandit_heavy,bandit_senior,ataman. |
| Kurgans | BurialRecords.instances / KurganInstance / KurganEncounters slots; internal plan.tier0/1/2 equals publicI/II/III; tierIII prince, tierII dedicated voevoda. Existing bossDefeated persists III only; extend final-main-boss clear and record actual participating player UUID. Old unknown attribution cannot retroactively award. |
| Likho | ModEntities.LIKHO_ONE_EYED / hunt.WorldBoss / BossKind.LIKHO / BossAnchors / HuntRecords.Boss; canonical biome dark_forest. Reuse encounter lifecycle; exact server anchor withheld from story snapshot. |
| Whole medium soul | RuneFoundation.item("empowered_soul"),display Усиленная душа; verified recipe/info for blank_rune_2. No new soul ID or renaming existing item. |
| Actual tierII rune pool | RuneBalance.DEFINITIONS tier2:crushing,wind,blood,fortitude; existing rune_<id> items. |
| Heavy bow | WeaponCatalog heavy_bow, FieldBowItem heavy:true,draw30,velocity3.6,baseDamage×1.15,durability500. |
| Bow runes | existing Runes category bow/capacity2, Rune Anvil preview/operation; RpgEvents.arrow snapshots bow + applies speed and damage once. |
| Accessories | Curios9.5.1 ring/belt current slots; equipped lookup FolkAccessoryItem.equipped, existing render registry and Humanoid body-follow renderer API. Ring uses ring; quiver uses belt without adding slot. |
| Item data | item.ItemState.COMPONENTS persistent/synced typed data; extend quiver state. |
| Network/UI | existing RegisterPayloadHandlers + common Event notifications and client-only subscribers pattern; valid server snapshot only. |
| Navigation | existing NavigationManager/NavigationRecords/Xaero; coarse quest references only, never exact Likho XYZ. |
| Future | Existing legacy HuntTarget.VOLKOLAK predates this archive despite “does not exist” wording. Preserve its IDs/behavior; no new Volkolak, future quest always locked, no new skull/rewards. |

Implementation checkpoint verified; see verification/grey_wolf_1.4.2/REPORT.md. Shared version1.4.2.1 preserved; next user-authorized archive1.4.3 continues this chain.
