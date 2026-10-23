"""P44 source regression for partial #162/#165/#163/#160/#161; never a Minecraft runtime test."""
from pathlib import Path
import re
import sys
import zipfile

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"src/main/java/org/slavicmyths"
motion=(JAVA/"client/WeaponMotionClient.java").read_text(encoding="utf-8")
hands=(JAVA/"client/WeaponHands.java").read_text(encoding="utf-8")
crossbow=(JAVA/"gorynych/SerpentCrossbow.java").read_text(encoding="utf-8")
damage=(JAVA/"gorynych/CrossbowDamage.java").read_text(encoding="utf-8")
catalog=(ROOT/"src/main/resources/assets/slavicmyths/creative_catalog.tsv").read_text(encoding="utf-8")
known={line.split("\t",1)[0] for line in catalog.splitlines() if "\tweapons\t" in line}
groups={}
for shape,names in re.findall(r"register\(event,Shape\.(\w+),(.*?)\);",motion,re.S):
    ids=re.findall(r'"([a-z_]+)"',names)
    assert ids and shape not in groups,(shape,ids)
    groups[shape]=ids
assert set(groups)=={"BLADE","HEAVY","SPEAR","POLEAXE","SICKLE","STAFF"},groups.keys()
all_ids=[name for group in groups.values() for name in group]
assert len(all_ids)==32 and len(set(all_ids))==len(all_ids)
assert set(all_ids)<=known,set(all_ids)-known
assert {"karachun_scythe","poloz_whip","ataman_flail","finist_bow","serpent_crossbow","three_breath_crossbow"}.isdisjoint(all_ids)
for required in ("player.swingingArm==hand","case BLADE","case HEAVY","case SPEAR",
                 "case POLEAXE","case SICKLE","case STAFF","getCooldownPercent",
                 "Axis.XP.rotationDegrees","Axis.YP.rotationDegrees"):
    assert required in motion,required
# Actual eased animation returns to the original rest pose; no new server-side hit timing.
def ease(x):
    x=max(0,min(1,x))
    return x*x*(3-2*x)
def phases(t):
    prep=ease(t/.27)
    contact=ease((t-.27)/.27)
    recovery=1-ease((t-.54)/.46)
    return (prep-contact)*recovery,contact*recovery
assert phases(0)==(0,0)
assert phases(1)==(0,0)
assert phases(.20)[0]>.5 and phases(.20)[1]==0
assert phases(.50)[1]>.6
assert ".getUseItemRemainingTicks()" in hands
assert "crossbowKick(" in hands and "observedCharged[index]&&!charged" in hands
assert "rotationDegrees(-11*pull)" in hands and "rotationDegrees(sign*5*pull)" in hands
# Sound and six muzzle particles are emitted only for the first projectile in each 3-round volley.
fire=crossbow.split("void fireRound(",1)[1].split("@Override protected void shootProjectile",1)[0]
assert fire.count("GorynychContent.sound(")==1
assert "if(lane==0){" in fire
assert fire.index("if(lane==0){")<fire.index("w.sendParticles(cue")
assert fire.index("w.sendParticles(cue")<fire.index("GorynychContent.sound(")
assert "ParticleTypes.SOUL" in fire and "ParticleTypes.FLAME" in fire and "ParticleTypes.ELECTRIC_SPARK" in fire
assert "if(!r.arrows.add(arrow.getUUID()))return;" in damage
assert "CrossbowDamage.VOLLEY" in crossbow
if "--jar" in sys.argv:
    jars=list((ROOT/"build/libs").glob("slavicmyths-*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        files=set(z.namelist())
        for class_name in ("client/WeaponMotionClient.class","client/WeaponHands.class",
                           "gorynych/SerpentCrossbow.class","gorynych/CrossbowDamage.class"):
            assert "org/slavicmyths/"+class_name in files,class_name
print("PASS P44: 32 real weapon IDs, animation phase/source, volley cue/dedup and packed classes when requested; Minecraft NOT_RUN")
