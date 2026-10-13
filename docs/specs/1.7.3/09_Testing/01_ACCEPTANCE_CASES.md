# 1.7.3 ACCEPTANCE TEST MATRIX — run in actual game

Mark each PASS/FAIL, do not claim a test was executed when it was only inspected in code. Unless clearly designated as implementation default, this is mandatory. Use dedicated survival world for non-destructive tests.

| ID | Setup / steps | Expected |
|---|---|---|
| MODEL-01 | Spawn Alyosha day, front at distance 5 and 15 | Blond hair, blue eyes, clean-shaven; slim cream/red/brown costume, obvious bow/quiver/saber |
| MODEL-02 | Compare Alyosha/Ilya/Dobrynya side-by-side | Not same gear colored; blond Alyosha 2.15 < Dobrynya 2.4 < Ilya 2.5 blocks, armor silhouettes distinct |
| MODEL-03 | Turn Alyosha 4 directions and crouch | No clipping floating quiver, feet grounded, cloak segments continuous |
| MODEL-04 | Render Skim from front/side/back/top | Four legs, giant ragged mane, ivory tusks, tail and red scar details; 3-block quadruped height |
| MODEL-05 | Rear roar Skim near 5-block ceiling | 4.5 visual height with legal ceiling, no body-head clipping or strange giant AABB exploit |
| MODEL-06 | Inspect resource pixel dimensions | Alyosha atlas >=128×128, Skim >=256×256, inventory item textures >=32×32 |
| HERO-01 | `/give ...alyosha_call_whistle`, use twice in survival | One hero spawns, then recalls outside fight, item count unchanged; 5s cooldown prevents spam |
| HERO-02 | Try summon next to lava/wall/void | Refuses safely, no spawn in solid collision, item intact |
| HERO-03 | Use two different hero call items in overworld | Exactly max2 heroes, 3rd rejected, items intact |
| HERO-04 | Re-log/dimension travel during ally summoned | No two Alyoshas duplicated, ownership correctly restored or cleanly dismissed |
| HERO-05 | Call Alyosha in range of 4 hostile mobs | Taunt switches significant portion of mob attention to him, never friendly villagers/cows |
| HERO-06 | Keep Skim aggro on Alyosha for 2 min while player hits | Boss occasionally re-evaluates and attacks player, not 100% permanent decoy |
| HERO-07 | Observe 10 telegraphed boss heavies while Alyosha dodges | Several attacks miss from correctly timed lateral dodge, not invincible roll continuously |
| HERO-08 | Force target behind wall when dodge called | Alyosha doesn't clip through solid wall or move 15 blocks |
| HERO-09 | Stand with Alyosha in boss AOE outside dodge displacement | Dodge does not magically immunize splash/whistle DOT |
| HERO-10 | Alyosha hits boss for 30 sec | Reasonable weak DPS (roughly <=3 boss HP/sec), boss not solo-melted |
| HERO-11 | Alyosha HP reaches 15% then encounter continues | Retreat + tonic animation, preserved slot, unavailable for substitution, survives exit if feasible |
| HERO-12 | Lethal high hit from 30 HP | Once per activation prevents permanent death and retreats; no invincibility loop |
| HERO-13 | Try item in active Skim encounter | Cannot summon, recall or replace hero after starting |
| HERO-14 | Try Ilya/Dobrynya in Skim pre-start and active fight | Story rejects wrong hero, item count unchanged, clear RU/EN message |
| HERO-15 | Summon matching Alyosha before STARTING | Allowed and recognized; no duplicate auto-hero |
| BOSS-01 | Creative use Skim egg at safe site | Skim renders/attacks, full HP750, egg ID localizes correctly |
| BOSS-02 | Creative use Idolishe and Marinka eggs | Previous bosses spawn/render normally, exact existing entity preserved, no combat rebalance |
| BOSS-03 | `/summon slavicmyths:skim_beast` | Entity exists, model and bossbar work; no fabricated story victory unless valid encounter |
| BOSS-04 | `beast_challenge_horn` right-click outside valley | No boss, clear message, horn not consumed |
| BOSS-05 | Use Beast Horn at marker 5 blocks, repeated 6 times | Exactly one boss starts, no duplicate, no horn count shrink |
| BOSS-06 | Boss lands 3 heavy leap hits on 24HP player full diamond | Each direct hit >=6 health points (3 hearts), read debug health before/after |
| BOSS-07 | Same with diamond Protection enchant/buffs | Damage can decrease legitimately, no global armor bypass; document results |
| BOSS-08 | Claw combo in close proximity | 3 independent hit windows and clean third heavy warning; no hit from behind out of range |
| BOSS-09 | Witness 5 leaps | Visible locked landing zone and jump animation, collision recovery, no midair perfect homing |
| BOSS-10 | Stand behind boulder during snake whistle | Line-of-sight cover works, debuff finite and readable |
| BOSS-11 | Whistle with sound muted/reduced particles | Body pose / ring still signals attack, playable |
| BOSS-12 | Witness ground-to-reared roar | Height changes visually, knockback and 6 HP near damage, no instant untelegraphed roar |
| BOSS-13 | Grab player, have Alyosha rescue | Player releases early, no ghost tether; boss resumes normally |
| BOSS-14 | Grab Alyosha, damage jaw as player | Rescue works within window; if not rescued release on timer |
| BOSS-15 | Disconnect while grabbed | On reconnect no permanent riding/tether/desync |
| BOSS-16 | Keep beast active using all 3 major attacks | Stamina eventually EXHAUSTED, 70tick vulnerable, no attacks during exhausted |
| BOSS-17 | Damage beast to 225HP and lower | One rage transition, speed rises, exhaustion remains available and telegraphed |
| BOSS-18 | Push boss against player-built house/chests | Does not grief random structures or delete chest contents |
| BOSS-19 | Spawn boss outside story via egg and kill | No story quest completion/unique rewards granted |
| ARENA-01 | Place/locate 65×65 valley | Cliff rim, 40+ block clear field, cave entrance N, ruined camp SW, bone props E, safe approach S |
| ARENA-02 | Walk center with Skim and Alyosha | No stuck path loops, boulder cover accessible, no tree-trunk permanent trap |
| ARENA-03 | Leave boss arena, reload/unload/re-enter | Resets without duplication, generation not repeated, horn not consumed |
| ARENA-04 | Win active encounter and inspect den | Single cave with trophies and ordinary chests; gated stone entry opens if gate implemented |
| ARENA-05 | Win then relog and re-enter valley | Structure and cave unlock preserved; boss doesn't respawn accidentally |
| PROGRESS-01 | Clear story boss with player+Al | Server persists victory and Alyosha involvement even after restart |
| PROGRESS-02 | Check drops after victory | No unique 1.7.6 boss loot, no unique heroic rewards yet |
| UI-01 | Creative inventory icons and item use 1st person | No missing purple textures, labels correct, all call tools have distinct shapes |
| UI-02 | Client language ru_ru and en_us | New messages and subtitles available without placeholders |
| AUDIO-01 | Play each registered custom effect in game | All sound keys exist and read OGG, varying repeated sounds where specified |
| AUDIO-02 | Test boss roar with surrounding players | One positional roar sound/event, no audio explosion from multiplied packets |
| MULTI-01 | 2 players use horn simultaneously | One boss, no duplicated encounter, both receive expected UI |
| MULTI-02 | Dedicated server startup | No client-side renderer ClassNotFound or wrong registry deferred init |
| BUILD-01 | Run wrapper build/gradle test | `BUILD SUCCESSFUL`, jar available; report exact path |

## Acceptance blocker levels
BLOCKER: cannot compile, no egg/item, duplicate boss, death/desync exploit, 6HP diamond baseline fails, completely missing animation/model, incompatible old saves. MAJOR: bad FOV/telegraph, no cave, music/VO issues, wrong localization, no taunt. MINOR: decorative object placement off by 1–2 blocks or nonessential particles.
