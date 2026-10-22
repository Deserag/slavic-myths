"""Regression: moving either hand away from a crossbow drops stale recoil state."""
from pathlib import Path
code=(Path(__file__).resolve().parents[1]/"src/main/java/org/slavicmyths/client/WeaponHands.java").read_text(encoding="utf-8")
body=code.split("static float crossbowKick(",1)[1].split("static void renderScythe(",1)[0]
reset="if(!(held.getItem() instanceof SerpentCrossbow)){\n   observedCrossbows[index]=null;observedCharged[index]=false;lastShotClock[index]=Double.NEGATIVE_INFINITY;return 0;\n  }"
assert reset in body,"handoff reset missing"
assert body.index("ItemStack held=player.getItemInHand(hand);") < body.index(reset) < body.index("stack=held;")
assert "observedCrossbows[index]!=stack" in body
assert "observedCharged[index]&&!charged&&!player.isUsingItem()" in body
assert "elapsed<0||elapsed>=7" in body
print("PASS crossbow equipment transitions: no retained recoil/charge upon switching away; runtime NOT_RUN")
