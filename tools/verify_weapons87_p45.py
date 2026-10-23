"""P45 throw animation source/JAR guards, not a game render or projectile flight test."""
from pathlib import Path
import re
import sys
import zipfile

ROOT=Path(__file__).resolve().parents[1]
client=(ROOT/"src/main/java/org/slavicmyths/client/ThrowingMotionClient.java").read_text(encoding="utf-8")
server=(ROOT/"src/main/java/org/slavicmyths/combat/ThrowingWeaponItem.java").read_text(encoding="utf-8")
catalog=(ROOT/"src/main/resources/assets/slavicmyths/creative_catalog.tsv").read_text(encoding="utf-8")
ids={line.split("\t",1)[0] for line in catalog.splitlines() if "\tweapons\t" in line}
knife=re.search(r"KNIVES=\{(.*?)\};",client,re.S)
sulitsa=re.search(r"SULITSAS=\{(.*?)\};",client,re.S)
assert knife and sulitsa
knives=re.findall(r'"([a-z_]+)"',knife.group(1))
sulitsas=re.findall(r'"([a-z_]+)"',sulitsa.group(1))
assert len(knives)==len(sulitsas)==7
assert len(set(knives+sulitsas))==14
assert set(knives+sulitsas)<=ids
assert all(i.endswith("_throwing_knife") for i in knives)
assert all(i.endswith("_sulitsa") for i in sulitsas)
assert "install(event,true,KNIVES)" in client and "install(event,false,SULITSAS)" in client
assert "getUseItemRemainingTicks()" in client and "player.getUsedItemHand()==hand" in client
assert "player.swingingArm==hand" in client
assert "-72*windup+42*throwArc" in client
assert "rotationDegrees(-48*throwArc)" in client
assert "getUseAnimation(ItemStack stack){return UseAnim.SPEAR;}" in server
assert "if(level.isClientSide)player.swing(hand)" in server
assert "if(level.isClientSide)player.swing(player.getUsedItemHand())" in server
assert "if(!level.addFreshEntity(projectile))return;" in server
assert "1.3F+Math.min(held,20)/20F*1.2F" in server
assert "projectile.shootFromRotation(" in server
assert "if(!creative)stack.shrink(1)" in server
assert "knife?8:16" in server
def ease(x):
    x=max(0,min(1,x))
    return x*x*(3-2*x)
assert ease(0)==0 and ease(1)==1
assert ease(.5)==.5
assert 0<ease(.25)<.25 and ease(.75)>.75
if "--jar" in sys.argv:
    jars=list((ROOT/"build/libs").glob("slavicmyths-*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        for cls in ("org/slavicmyths/client/ThrowingMotionClient.class",
                    "org/slavicmyths/combat/ThrowingWeaponItem.class"):
            assert cls in z.namelist(),cls
print("PASS P45: 7 knife / 7 sulitsa local throw poses, unchanged projectile and balance, JAR class gate when requested; Minecraft NOT_RUN")
