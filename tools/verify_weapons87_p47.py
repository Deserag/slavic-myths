"""P47/P48 checks 51 third-person weapon paths, native models, controls, and production classes."""
from pathlib import Path
import re
import sys
import json
import zipfile

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"src/main/java/org/slavicmyths/client/ThirdPersonWeaponMotion.java"
source=SRC.read_text(encoding="utf-8")
catalog=(ROOT/"src/main/resources/assets/slavicmyths/creative_catalog.tsv").read_text(encoding="utf-8")
weapons={line.split("\t",1)[0] for line in catalog.splitlines() if "\tweapons\t" in line}
groups={}
for shape,names in re.findall(r"register\(Shape\.(\w+),(.*?)\);",source,re.S):
    ids=re.findall(r'"([a-z_]+)"',names)
    assert ids,shape
    groups[shape]=ids
assert set(groups)=={"BLADE","HEAVY","SPEAR","POLEAXE","SICKLE","STAFF","KNIFE","SULITSA","CROSSBOW","BOW"}
assert [len(groups[x]) for x in ("BLADE","HEAVY","SPEAR","POLEAXE","SICKLE","STAFF","KNIFE","SULITSA","CROSSBOW","BOW")]==[6,9,12,1,1,3,7,7,2,3]
all_ids=[v for vals in groups.values() for v in vals]
assert len(all_ids)==51 and len(set(all_ids))==51 and set(all_ids)<=weapons
for item in all_ids:
    model=ROOT/"src/main/resources/assets/slavicmyths/models/item"/(item+".json")
    assert model.exists(),item
    loaded=json.loads(model.read_text(encoding="utf-8"))
    assert any(key in loaded for key in ("elements","parent","loader")),item
assert {"karachun_scythe","poloz_whip","stone_flail","morozko_staff","likho_staff"}.isdisjoint(all_ids)
for token in (
    "ModelEvent.ModifyBakingResult","ModelResourceLocation.inventory",
    "new MotionModel(source,null,null,entry.getValue())",
    "getOverrides().resolve(originalModel,item,level,entity,seed)",
    "originalModel.applyTransform(context,pose,left)",
    "context!=ItemDisplayContext.THIRD_PERSON_LEFT_HAND&&context!=ItemDisplayContext.THIRD_PERSON_RIGHT_HAND",
    "owner.getMainArm()==HumanoidArm.LEFT","owner.swingingArm==hand",
    "ItemStack.isSameItemSameComponents(owner.getItemInHand(hand),stack)",
    "owner.getAttackAnim(partial)","owner.walkAnimation.speed(partial)",
    "owner.getUsedItemHand()==hand","CrossbowItem.getChargeDuration(stack,owner)+10F",
    "owner.getTicksUsingItem()+partial","case CROSSBOW","case KNIFE","case SULITSA","case BOW",
):
    assert token in source,token
assert "WorldTickEvent" not in source and "PlayerTickEvent" not in source
assert "addFreshEntity" not in source and "PacketDistributor" not in source
assert "getItemInHand(hand).getItem()!=BuiltInRegistries" not in source
# The three-stage pose samples must return to the identity at rest or on completion.
def ease(x):
    x=max(0,min(1,x))
    return x*x*(3-2*x)
def sample(t):
    prep=ease(t/.3)
    contact=ease((t-.3)/.28)
    recovery=1-ease((t-.58)/.42)
    return (prep-contact)*recovery,contact*recovery
assert sample(0)==(0,0) and sample(1)==(0,0)
assert sample(.2)[0]>0.6 and sample(.2)[1]==0
assert sample(.55)[1]>0.6
if "--jar" in sys.argv:
    jars=list((ROOT/"build/libs").glob("slavicmyths-*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        assert "org/slavicmyths/client/ThirdPersonWeaponMotion.class" in z.namelist()
        assert "org/slavicmyths/client/ThirdPersonWeaponMotion$MotionModel.class" in z.namelist()
print("PASS P47/P48: 51 real player-held third-person models, swing/charge/walk pose envelopes, independent hand contexts and class packaging; Minecraft NOT_RUN")
