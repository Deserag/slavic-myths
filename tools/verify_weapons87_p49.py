"""P49 owner-rejected gameplay/visual regressions: strict source + built JAR checks."""
from pathlib import Path
import json
import sys
import zipfile

R=Path(__file__).resolve().parents[1]
def read(path): return (R/path).read_text(encoding="utf-8")
def model(name): return json.loads(read("src/main/resources/assets/slavicmyths/models/item/"+name+".json"))
def source(name): return read("src/main/java/org/slavicmyths/"+name+".java")
m1=model("ancient_carolingian_sword_held")
m2=model("restored_carolingian_sword_held")
for a in ("firstperson_righthand","firstperson_lefthand","thirdperson_righthand","thirdperson_lefthand"):
    assert m1["display"][a]==m2["display"][a],a
assert len(m1["elements"])==len(m2["elements"])==10
assert m1["display"]["thirdperson_righthand"]["rotation"]==[0,-90,0]
assert m1["display"]["thirdperson_lefthand"]["rotation"]==[0,90,0]
assert m1["display"]["firstperson_righthand"]["translation"][1]==7

poloz=model("poloz_whip")
assert poloz["parent"]=="minecraft:item/generated"
assert poloz["textures"]["layer0"]=="slavicmyths:item/poloz_whip"
assert len(poloz["overrides"])==5
for i in range(5):
    pose=model("poloz_whip_pose_"+str(i))
    assert len(pose["elements"])==36 and pose["textures"]["lash"]=="slavicmyths:item/poloz_whip_segments"

spear=model("thunder_spear")
assert len(spear["elements"])==37
for base in (10,13,16,19,22):
    assert "west" not in spear["elements"][base]["faces"]
    assert spear["elements"][base]["from"][0]==spear["elements"][base+1]["from"][0]
    assert "west" in spear["elements"][base+1]["faces"]

horn=model("beast_challenge_horn")
assert horn["parent"]=="minecraft:block/block"
assert len(horn["elements"])==10
assert all(len(e.get("faces",{}))==6 for e in horn["elements"])
assert all(x in horn["display"] for x in ("firstperson_righthand","firstperson_lefthand","thirdperson_righthand","thirdperson_lefthand"))
assert (R/"src/main/resources/assets/slavicmyths/textures/item/beast_challenge_horn.png").exists()

flail=source("client/FlailMotion")
assert "double reach=SPACING+contact*(heavy?4.5:5.0)" in flail
assert "anchor=anchor.add(direction,reach)" in flail
assert "Math.clamp(8+charge*(30-5*lag)+impact+inertia*lag,-12,88)" in flail
assert "public static final double SPACING = 2.4" in flail
assert "Vec3 eye=player.getEyePosition(1)" in source("combat/FlailItem")
assert "lookAngle" not in flail

finist=source("finist/FinistArrow")
assert "FinistMarks.target(p)!=target" not in finist
assert "!p.hasLineOfSight(target)" not in finist
assert "GreyPackRing.enemy(p,target)" in finist
assert "w.clip(new net.minecraft.world.level.ClipContext" in finist
assert "Math.toRadians(6)" in finist
assert "FinistOriginalGuide" in finist

rare=source("combat/RareCombat")
assert "PlayerTickEvent.Post" in rare
assert "d.remove(\"RareSecondTarget\")" in rare
assert "Component.empty(),true" in rare
assert "PathData.data(p).putLong(\"CD_rare_second\",now(p)+SECOND_COOLDOWN)" in rare
assert "p.attackStrengthTicker=1000" in rare

staff=source("item/StormStaffItem")
assert "PULSE_COOLDOWN=54" in staff and "RUNE_COOLDOWN=48" in staff
assert "BASE_PULSE_DAMAGE=6.0F" in staff
thunder=source("item/ThunderSpearCombat")
assert "event.setAmount(event.getAmount() + 2.0F)" in thunder
assert "world.random.nextInt(4) == 0" in thunder
assert "bolt.setVisualOnly(true)" in thunder

fx=source("client/TrophyHeadFxRules")
head=source("client/TrophyHeadsClient")
block=source("trophy/TrophyHeadBlock")
assert ".34*Math.sin" in fx and ".24*Math.sin" in fx
assert ".17+.16*Math.sin" in fx
assert "h.pose(time,gazeX,gazeY)" in head
assert "glow.x=glowBaseX+" in head and "glow.y=glowBaseY+" in head
assert "TrophyHeadFxRules.lidOffset(time)" in head
assert "for(int i=0;i<3;i++)world.addParticle" in head
assert "for(int i=0;i<24;i++)w.addParticle" in block
assert "w.destroyBlock(at,true)" in block and "w.removeBlock(at,false)" in block

if "--jar" in sys.argv:
    jars=list((R/"build/libs").glob("slavicmyths-*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        for cl in ("client/FlailMotion","client/TrophyHeadsClient","client/TrophyHeadFxRules","item/StormStaffItem","item/ThunderSpearCombat","combat/RareCombat","finist/FinistArrow","trophy/TrophyHeadBlock"):
            assert "org/slavicmyths/"+cl+".class" in z.namelist(),cl
        for name in ("ancient_carolingian_sword_held","restored_carolingian_sword_held","poloz_whip","thunder_spear","beast_challenge_horn"):
            path="assets/slavicmyths/models/item/"+name+".json"
            assert z.read(path)==(R/"src/main/resources"/path).read_bytes(),path
print("PASS P49: owner-rejected sword grip, whip rollback, flail hit extension, guided arrow, victor text, thunder, staff, six heads and hunting horn; runtime NOT_RUN")
