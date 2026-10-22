"""P52 #161: distinct MOR/FIRE impact cues on accepted special crossbow arrow hits only."""
from pathlib import Path
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
java = root / "src/main/java/org/slavicmyths"
damage = (java/"gorynych/CrossbowDamage.java").read_text(encoding="utf-8")
crossbow = (java/"gorynych/SerpentCrossbow.java").read_text(encoding="utf-8")
storm = (java/"gorynych/StormCrossbow.java").read_text(encoding="utf-8")

assert "if(event.getNewDamage()<=0" in damage
assert "if(!r.arrows.add(arrow.getUUID()))return;" in damage
assert "crossbow.special" in damage
assert "int mode=arrow.getPersistentData().getInt(\"GorynychImpactMode\")-1;" in damage
mor = damage.split("if(mode==0){", 1)[1].split("if(mode==1){",1)[0]
fire = damage.split("if(mode==1){",1)[1].split("\n        }\n    }",1)[0]
assert "MobEffects.WEAKNESS,40" in mor
assert "ParticleTypes.SOUL" in mor and "ServerLevel server" in mor
assert "igniteForSeconds(2)" in fire
assert "ParticleTypes.FLAME" in fire and "ServerLevel server" in fire
assert "8,.18,.22,.18,.012" in mor
assert "8,.18,.22,.18,.012" in fire
assert damage.count("server.sendParticles(")==2
assert "ParticleTypes.ELECTRIC_SPARK" not in damage
assert "StormVfx.play(level,at,ends)" in storm
assert "if(lane==0){" in crossbow
assert "w.sendParticles(cue,muzzle.x" in crossbow
assert "MIN_SPECIAL_DAMAGE=14" in damage
assert "r.arrows.contains(arrow.getUUID())" in damage

if "--jar" in sys.argv:
    jars=list((root/"build/libs").glob("slavicmyths-*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        for cls in ("gorynych/CrossbowDamage","gorynych/StormCrossbow","gorynych/SerpentCrossbow"):
            assert "org/slavicmyths/"+cls+".class" in z.namelist(),cls
print("P52 PASS: accepted MOR/FIRE hits get bounded distinct server particles once per arrow; Storm existing VFX preserved; runtime NOT_RUN")
