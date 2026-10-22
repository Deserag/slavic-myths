"""Regression guard: Storm Staff seven-block reach, VFX, and owner-adjusted -0.3-second cooldowns."""
from pathlib import Path
p=Path(__file__).resolve().parents[1]/"src/main/java/org/slavicmyths/item/StormStaffItem.java"
s=p.read_text(encoding="utf-8")
checks=[
    'look.scale(7)',
    'e.distanceToSqr(player) <= 49.0',
    'org.slavicmyths.rpg.Abilities.canHit(player,e)',
    'ClipContext.Block.COLLIDER',
    'e.position().subtract(start).normalize().dot(look) > 0.93',
    'ParticleTypes.ELECTRIC_SPARK',
    'ParticleTypes.ENCHANT',
    'if(e.hurt(',
    'BASE_PULSE_DAMAGE=6.0F',
    'PULSE_COOLDOWN=48',
    'RUNE_COOLDOWN=42',
]
for token in checks: assert token in s, token
assert s.index('e.distanceToSqr(player) <= 49.0') < s.index('targets.stream().min(')
assert s.index('ParticleTypes.ELECTRIC_SPARK') < s.index('RuneEffects.staff(')
assert 'setWeatherParameters(' not in s
print("PASS Storm Staff source constraints: 7-block target cutoff, LOS, approved damage/cooldown, impact VFX; runtime NOT_RUN")
