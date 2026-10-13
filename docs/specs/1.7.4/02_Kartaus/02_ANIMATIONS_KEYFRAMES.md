# Kartaus — animation state table and frame-by-frame telegraphs

All lengths in ticks, 20t/s. Use named arms, cloak panels, crown/head, staff grip, root bones. Do not allow casting while ragdolled/knocked; root shift actual position only when server AI says so. Kartu's intended motion is DELIBERATE/CONTROLLING, slower than Marinka: walk <=0.22 blocks/tick; caster pivot rather than teleport. Each ability needs a distinct readable windup from >=14 blocks in low particles.

| State | Length | Keyframes and VFX/strike events |
| --- | --- | --- |
| idle / command | 70 loop | 0 feet apart root yaw=0 staff base on ground; 20 staff wrist -8°, head yaw +7°; 40 staff rises 4px, cape sways 3°, red eye breathes; 70 reset. No obvious constantly flashing laser. |
| walk | 28 loop | t0 left step X+18° right -18°, robe split +/-4°; t7 pass; t14 opposite; t21 pass; t28 loop; staff counterswings 5°. |
| melee staff punish | 24 | t0 threat head forward, t6 staff X-37° raised, t10 shoulder rotates Y+27°, t13 staff sweep horizontal (hit t13–15), t19 followthrough, t24 neutral; rectangular/swept melee range <=3.3 blocks. |
| black banner cast | 48 | t0 right arm drops, t8 two-handed grip, t16 staff above head, t24 strikes earth (first ring), t31 banner grows vertically, t40 fully planted, t48 recover. Banner entity activates at t32, no direct invisible buff before visible banner. |
| call black guard | 42 | t0 crest red flicker; t9 staff held level, left hand opens; t18 purple summoning squares on floor; t25 minion spawn; t33 pointing order; t42 idle. Floor spawning telegraphed at least 14t. |
| staff wave | 34 | t0 staff high at right; t10 draws across body, t18 slam (launch at t19), t26 recoil, t34 return. Visible linear ground wave from staff to 14 blocks. Damage only collision when wave reaches player. |
| dark chains | 38 | t0 left hand toward player; t8 hooked wrists; t14 purple ring on target feet; t22 chains emerge and bind; t28 slam; t38 return. On t22 resolve server cast with LoS/radius; prewarning 14t. |
| black eye / sky sigil | 54 | t0 staff eye to sky; t12 torso back X-8°, t21 eye appears centered target tile; t30 ring expands radius 2, t42 vertical strike, t54 fade. Target mark snapshots player's location at t21, NEVER constantly homing till t42. |
| falcon curse | 52 | t0 Kartaus sights circling bird; t10 points staff; t20 one purple chain shoots; t27 bird is bound; t34 curse anchor spawns on ground; t45 laugh pose; t52 resume. Falcon not permanently destroyed. Anchor can be attacked immediately after visible spawn; cast cannot fail silently. |
| banner buff gesture | 25 | left gauntlet raised t8, banner sigil pulses t15, arm returns t25. Playback <=1 every 7 sec. |
| stagger / barrier break | 32 | 0 interruption cancels noncommitted cast; t5 bent X+22°, staff lowered 12px; t12 knee bends; t23 recovers; t32 neutral. Window for Finist/owner counterattack 50–80t defined AI file. |
| rage entrance (30% HP) | 60 | t0 recoil; t12 holds staff into ground; t22 dark crimson banner glow; t35 staff vortex ring, t50 eye turns red; t60 return. Never invulnerable for the full 60t: 20t introductory protection max and readable. |
| hit reactions | 10 | torso X -8° t3; recovery t10; no constant stagger lock from fast weapon. |
| death | 70 | t0 staff loses eye, t14 falls from hand, t27 kneels, t45 head bows + cloak spreads, t65 sinks to ground, t70 cleanup; banners dissolve immediately upon confirmed death (do not wait for animation to apply victory). |

### Telegraph collisions
Ground runes must persist >=12t before damage; display two-stage warnings: outline at launch and expanding fill 6t before hit. Chain/root force must NOT hide target's own weapon model or block inventory UI. No cinematic camera shakes >small local impact; no full-screen postprocessing. Each animation may be sampled through networking; state and start serverTick are synced so newly tracking clients render consistent late phase rather than restarting invisible effects.
