# DAMAGE / TELEGRAPHS / HEARTS — quantitative combat contract

## Units — MUST BE CONSISTENT
1 Minecraft HEART = **2 HP / health points**. Original vanilla player max =20 HP /10 hearts; target late-midgame player typically has 24 HP /12 hearts via modded items. Full diamond armor: armor value=20, armor toughness=8. No protective accessories for baseline tests.

The boss target minimum "6 health points with full diamond armor" means **6 HP AFTER ARMOR = 3 hearts** for heavy clean hits. DO NOT mistakenly set raw attack to 6 and then allow armor reduce it to 1 HP. Tune attack attribute or DamageSource using real game's combat reduction. Do not bypass all armor globally, or make full netherite/shield useless. Difficulty- and enchantment-aware behavior is essential.

| Attack | Base target after full diamond normal difficulty | UI consequence | Lead time |
|---|---:|---|---|
| Single light claw swing | 2.5 HP | 1.25 hearts | 10+ ticks |
| Third heavy claw | >=6 HP | 3+ hearts | 20+ ticks from combo start, distinct tell |
| Beast leap direct | ~7 HP | 3.5 hearts | >=12 ticks visible target circle before impact |
| Lion roar near | >=6 HP | 3+ hearts | rear pose 18+ ticks |
| Whistle cone | <=1 HP | mostly debuff, can break LoS | 14+ ticks |
| Flank bite/pounce | >=6 HP | 3+ hearts | 10+ ticks pivot + short dust |
| Grip full unresolved | ~11 HP total over multiple windows | dangerous but **rescuable** | grab windup 14+ ticks; damage pulses spaced |
| Alyosha ordinary saber hit | 2.0–2.6 raw HP BEFORE armor | weak distraction | fast 4-hit multi |
| Alyosha dodge counter | 4.0 raw HP BEFORE armor | weak to moderate counter | after enemy misses |

Test with a real player's 24 HP and diamond set under the intended difficulty, no food regen first; record health before/after attack; rerun with normal food regen and see boss damage still meaningful. If existing game system uses different attributes, implement calibrated values instead of blindly overriding via direct health set.

## Damage cooldowns and stacking
- Each 1 strike hits each player one time, avoid double hits due to intersecting multipart hitboxes. Honor normal hurt time unless deliberately designing combo windows; never bypass invulnerability frames indiscriminately. Grip damage pulses may be low due to hurt-cooldown; if so implement a safe single aggregate hit or controlled damage entry rather than multiplying events unpredictably.
- Avoid passive contact damage continuously simply for touching boss (huge hitbox). Whistle DOT not continuous, only pulse + short debuff. Scenery collision should not kill player via physics glitches.
- With Ilya or Dobrynya extra protection, boss may do less; that is a meaningful buff. Diamond+enchants reduce damage. If player is naked, they risk high damage; high-skill attacks telegraphed rather than guaranteed unpreventable one-hit.

## Telegraph language
Skim leap: muted rust-red landing ring radius2.2, fades/collapses t12->27. Roar: body rears to 4.5 blocks and shoulders lift 18 ticks before cone. Whistle: head/mouth glow + floating red-gray concentric rings 14 ticks before cone. Claws: foreleg raises and dust direction mark before EACH strong third strike. Grab: giant mouth open and short colored breath visible >=14 ticks. All cues have optional sound and visible body pose; particles are supportive not sole cue. Keep contrast for fog/night/colorblind settings (pattern differences, not just red hue).
