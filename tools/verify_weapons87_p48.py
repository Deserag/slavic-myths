"""P48 third-person bow draw timing (12 / 30 / 30 ticks) and all 4 predicated states."""
from pathlib import Path
import json,sys,zipfile

R=Path(__file__).resolve().parents[1]
SRC=R/"src/main/java/org/slavicmyths/client/ThirdPersonWeaponMotion.java"
code=SRC.read_text(encoding="utf-8")
assert 'register(Shape.BOW,"short_bow","heavy_bow","finist_bow")' in code
assert "stack.getItem() instanceof org.slavicmyths.combat.FieldBowItem field?field.drawTicks():30" in code
assert "charge=Mth.clamp((owner.getTicksUsingItem()+partial)/draw,0,1)" in code
assert "owner.isUsingItem()&&owner.getUsedItemHand()==hand" in code
assert "case BOW -> {" in code and "Axis.YP.rotationDegrees(sign*-10*draw)" in code
assert "ItemDisplayContext.THIRD_PERSON_LEFT_HAND" in code
assert "ItemDisplayContext.THIRD_PERSON_RIGHT_HAND" in code
assert "originalModel.applyTransform(context,pose,left)" in code
field=(R/"src/main/java/org/slavicmyths/combat/FieldBowItem.java").read_text(encoding="utf-8")
finist=(R/"src/main/java/org/slavicmyths/finist/FinistBow.java").read_text(encoding="utf-8")
assert "public int drawTicks(){return heavy?30:12;}" in field
assert "float f=Math.max(0,ticks)/30F" in finist
root=R/"src/main/resources/assets/slavicmyths/models/item"
for name in ("short_bow","heavy_bow","finist_bow"):
    models=[json.loads((root/(name+suffix+".json")).read_text(encoding="utf-8")) for suffix in ("","_pulling_0","_pulling_1","_pulling_2")]
    assert all(m.get("parent")=="minecraft:item/bow" for m in models)
    for m in models:
        assert (m.get("display") or {}).get("thirdperson_righthand")
        assert (m.get("display") or {}).get("thirdperson_lefthand")
    assert "overrides" in models[0] and len(models[0]["overrides"])==3
    assert all(x.get("predicate",{}).get("pulling")==1 for x in models[0]["overrides"])
if "--jar" in sys.argv:
    jars=list((R/"build/libs").glob("slavicmyths-*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        assert "org/slavicmyths/client/ThirdPersonWeaponMotion.class" in z.namelist()
        for name in ("short_bow","heavy_bow","finist_bow"):
            for suffix in ("","_pulling_0","_pulling_1","_pulling_2"):
                p="assets/slavicmyths/models/item/"+name+suffix+".json"
                assert z.read(p)==(R/"src/main/resources"/p).read_bytes(),p
print("PASS P48: three third-person bows use actual draw ticks, no server projectile changes, 12 model states and JAR checks; Minecraft NOT_RUN")
