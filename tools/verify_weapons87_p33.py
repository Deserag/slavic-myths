from pathlib import Path
import sys,zipfile
r=Path(__file__).resolve().parents[1]
s=(r/'src/main/java/org/slavicmyths/item/StormStaffItem.java').read_text()
assert 'if(e.hurt(player.damageSources().indirectMagic(player,player),pulseDamage(' in s
assert 'level.sendParticles(ParticleTypes.ELECTRIC_SPARK' in s
assert s.count('ParticleTypes.ELECTRIC_SPARK')==1
assert 'org.slavicmyths.rpg.RuneEffects.staff(player,stack,e);}}' in s
assert s.count('ParticleTypes.ENCHANT')==1
assert 'e.getBbHeight()*.5' in s
assert '12,.22,.25,.22,.02' in s
assert 'BASE_PULSE_DAMAGE=6.0F' in s and 'PULSE_COOLDOWN=60' in s and 'RUNE_COOLDOWN=54' in s
if '--jar' in sys.argv:
 jars=list((r/'build/libs').glob('slavicmyths-*.jar'))
 assert len(jars)==1
 with zipfile.ZipFile(jars[0]) as z:assert 'org/slavicmyths/item/StormStaffItem.class' in z.namelist()
print('PASS #161 storm staff targeted sparks only on successful damage, fixed 12 particle budget, no base damage/cooldown changes; runtime NOT_RUN')
