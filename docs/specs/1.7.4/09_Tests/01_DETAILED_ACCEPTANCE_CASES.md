# Acceptance checks — real game outcomes, exact evidence

This is a minimum acceptance matrix. Run fastest focused tests available first, then smoke test actual JAR. Mark individually PASS/FAIL/NOT RUN (never pretend all passed merely because compilation succeeded).

| ID | Steps | Expected acceptance |
| --- | --- | --- |
| G01 | Open world, spawn Finist via feather | 1 visible 2.25-block hero plus 1 bird, correct approved hair/cape/sword/sounds, item retained. |
| G02 | Click feather repeatedly 20x with 1 hero | No duplicate hero/bird, inventory unchanged, no infinite entities. |
| G03 | Two other heroes active then feather | Denied gracefully, no new hero; existing pair unchanged. |
| G04 | Summon Finist then relog | Exactly one owned hero/companion (or project-consistent safe dismiss) no ghost models. |
| G05 | Visit another dimension and return | No cross-dimension duplication, persistent item, safe owner. |
| G06 | Hero walking/running | Legs and two-piece cape animate; no sword clip or wrong arm. |
| G07 | Finist below 15% HP in Kartaus event | Retreat/decoction animation, no replacement call, slot held, no midfight re-entry. |
| G08 | Finist gets normal attacks | Real HP drops, does not auto-heal instantly, is not immune unless 8t dodge frame. |
| F01 | Idle bird on left arm | Bird visible as distinct small falcon, no collision jitter. |
| F02 | Scout after hostile nearby | Up to 3 valid threat markers, no friendly outlining, duration <=100t. |
| F03 | Dive target in clear line | Single contact and cooldown, doesn't dive through blocks. |
| F04 | Falcon Guard saves owner | Cancels exactly one interruptible attack, not all AOE, 240t CD. |
| F05 | Morph during heavy telegraph | One enlarged falcon, original model hidden; bird returns afterward, no duplicate entities. |
| F06 | Morph under low ceiling | Aborts safely / chooses new safe return point; no phase-through or death in block. |
| F07 | Kartaus curses bird | Visible chain, anchor 12 HP and HUD prompt; skills disabled, hero sword still works. |
| F08 | Break anchor | Immediately free bird and stun boss for 50t, 180t curse immunity. |
| F09 | Ignore anchor | Auto-release <=240t, no permanent pet freeze. |
| F10 | Owner leaves encounter with active curse | Anchor/chain/bird state cleaned without lingering effects. |
| K01 | Creative egg spawn | Kartaus 650 HP, correct model/staff/crown/moustache, no story credit. |
| K02 | Seal use on valid land | Boss once after telegraph; item not consumed. |
| K03 | Seal in tight interior/lava/forest dense area | Denied safely, no terrain modification or partial boss. |
| K04 | Seal with Ilya / Dobrynya / Alyosha active | Denied, no silent hero deletion. |
| K05 | Seal with Finist only | Story battle begins, future summons blocked and no swap. |
| K06 | Early phase banner | Damageable 45 HP banner with visible increase boss mitigation, 1 max P1. |
| K07 | Destroy one/two banners | Exact mitigation decrease + readable stagger; stop re-cast 160t. |
| K08 | Boss trying banner inside solid block | Finds valid alternative or fails cast, no immortal hidden flag. |
| K09 | Guard summon repeated | Hard max 3 alive, correct owner, clears at end, no guard farming. |
| K10 | Staff wave | Path 14x3 blocks, clearly painted windup, one hit max, dodge sideways. |
| K11 | Dark chains hit & expiration | Slowness <=50t, capped DOT, player can still act, effect reliably expires. |
| K12 | Black Eye | Locks player's former location at t21 and hits t42, moving out safe. |
| K13 | Kartaus phase 2 at 65% | Black Eye/curse unlocked, no early fight indefinite curse. |
| K14 | Kartaus phase 3 at <=195HP | Rage once, action sped but windups remain, 2 banners/3 guards maximum. |
| K15 | Fight benchmark: full diamond +24 HP | severe unblocked direct hit >=6 **HP** (3 hearts) absent extra modifiers; record observed values. |
| K16 | Boss receives fast repeated melee hits | Doesn't infinitely stagger or freeze AI, weapon animations synchronized. |
| K17 | Boss death | tags/guards/banner/anchor/FX cleared, victory state stored if story, no unique loot. |
| K18 | Abandon >48 blocks for >160t | Encounter stops cleanly, no progression or boss left ticking. |
| K19 | Owner death during fight | No ghost boss, usable seal for future retry, no loot duplication. |
| K20 | Dedicated server, 2 players | No cross-player hero ownership or shared banner count. |
| K21 | Two challenges in nearby overlapping areas | Second attempt refused or isolated, minions never cross-attach. |
| K22 | Disconnect midfight & reconnect | No duplicates, UI cleared or safely restored, item still there. |
| R01 | All item sprites inventory + hand | 64x64 crisp, correct 3 distinct icons and names, not missing textures. |
| R02 | All 2 entity texture atlases + falcon | >=128x128, no flat skins, user-approved silhouette front/side/back. |
| R03 | All listed sounds loaded | OGG valid and sounds.json exists, descriptive subtitles correct, no silent missing event. |
| R04 | Low particles setting | Enemy staff/eye/chain still telegraphed by motion/ground outlines. |
| R05 | Performance 2+ nearby event slots | >=10t bounded scans, <=3 guards /2 banners each, no expanding lag. |
| P01 | First STORY victory | Persist owner victory/Finist participation/pending_reward_1_7_6; no unique reward. |
| P02 | Creative spawned boss slain | No story progress awarded. |
| P03 | Repeated personal victory | Clear count may increment; no duplicate unique reward or achievements. |
| P04 | Verify old hero summons/bosses | 1.7.1–1.7.3 behavior not changed and normal max2 preserved. |
| P05 | `gradlew build` | Final JAR produced; report exact path, no invented success. |
| P06 | Clean repo/lint relevant code | No stray registry keys, unsafe tick loops, obsolete API calls, or resource typos. |
