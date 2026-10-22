"""#163 crossbow active-hand draw progress regression checks (source-only)."""
from pathlib import Path
code=(Path(__file__).resolve().parents[1]/"src/main/java/org/slavicmyths/client/WeaponHands.java").read_text(encoding="utf-8")
assert 'p.isUsingItem()&&p.getUsedItemHand()==e.getHand()?' in code
assert 'p.getUseItem()==e.getItemStack()' not in code
assert 'CrossbowItem.getChargeDuration(e.getItemStack(),p)+10f' in code
assert 'Math.clamp(' in code
assert 'crossbowWrist(pose,sign,swing,pull)' in code
assert 'crossbowKick(p,mc.level,e.getItemStack(),e.getHand(),e.getPartialTick())' in code
assert 'renderGrip(e,sign)' in code
assert 'CrossbowVolley.Toggle()' in code
print("PASS #163: first-person pull follows active hand rather than ItemStack object identity; existing recoil/volley unchanged; Minecraft NOT_RUN")
