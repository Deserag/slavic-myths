# Owner actual game feedback · 22 October 2026, #87

Before-production source commit 64e89143e2d76d3e492bd1b5fa00462011ed718f; owner screenshot confirms third-person crossbow stock vertical through chest, and calls out Thunder Axe without any visible lightning and Kalin sabre without payoff after three kills.

## #115/#163 crossbows
Actual authored stock runs along Z from [6.5,6,2] to [9.5,9,16], grip [8,5.5,12.5]. Current X=-90 rotation turns long Z stock upright (visible owner failure); P14 only recentered grip. Proposed thirdperson_[right|left]hand rotation [0,0,0], scale0.55, grip centered translation [0,1.375,-2.475], in both crossbows × six states (default/pull0/pull1/pull2/arrow/firework). Preserve geometry/UV/PNG, FP/GUI, loaded ammos, body animations, gameplay. Need source A/B and true game retest.

## #160/#161 Thunder Axe
Current event has only 20% proc +1HP and small enchant particles/TRIDENT sound, not visible lightning. Add visual-only LightningBolt exactly once on proc/target, do not call weather API, do not deal second damage, light fires or change existing 20%/60tick cooldown. Run Java21 tests.

## #160 Kalin sabre
Old mechanic: kill3 ordinary hostiles in30s THEN separate grounded/non-sprinting charged melee followed by a damage event within one tick. Hidden fourth-strike prerequisite explains user's three dead slimes without wave. Proposed hit3: three accepted saber melee hits within30s, displayed charge1/3,2/3; third hit triggers automatic directional frontal 95deg radius4.5 max3 additional ordinary hostile non-allied targets at 60% accepted hit damage; no boss/PVP/ally, no target rehit, no recursive source; show FX even if no adjacent targets. Preserve registry, base stats, loot, other gifts/runes.

## QA gates
Source before/after metadata, own plans and actual resources; Java21 clean build and JAR; owner hand/GUI/combat multiplayer remains NOT_RUN in this session. Do not close issues on static tests. No balance C/D/#190/#191.
