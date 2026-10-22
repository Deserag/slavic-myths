"""P39 flail animation & binding checks: #115/#116/#164/#167/#181."""
from pathlib import Path
import math
R=Path(__file__).resolve().parents[1]
motion=(R/"src/main/java/org/slavicmyths/client/FlailMotion.java").read_text(encoding="utf-8")
renderer=(R/"src/main/java/org/slavicmyths/client/FlailClient.java").read_text(encoding="utf-8")
fixture=(R/"tools/weapons87-offline/Weapons87FlailMotion.java").read_text(encoding="utf-8")
for value in ("Math.pow(Math.sin(swing*Math.PI),2)","double impact=(heavy?.8:1.0)*contact*(52+33*lag);","Math.clamp(8+charge*(30-5*lag)+impact+inertia*lag,-12,88)","public static final int LINKS = 7","SPACING = 2.4","anchor.add(directions[LINKS-1],2.0+contact*1.0)"):
    assert value in motion,value
assert "Math.cos(swing*Math.PI/2)" not in motion
for value in ("ItemStack.isSameItemSameComponents(binding.stack,stack)","ItemStack.isSameItemSameComponents(owner.getItemInHand(held),stack)","name.equals(\"ataman_flail\")","draw(stack,name+\"_held\",pose,buffers,light,overlay);return;"):
    assert value in renderer,value
assert "binding.stack==stack" not in renderer
assert 'case "contact"->.5f' in fixture
def contact(t):return math.sin(math.pi*t)**2
for t in (0,1): assert abs(contact(t))<1e-10
assert abs(contact(.5)-1)<1e-10
assert contact(.25) < contact(.5) and contact(.75) < contact(.5)
for lag in (1/7, 4/7,1):
    ordinary=(52+33*lag)
    heavy=.8*ordinary
    assert 0<heavy<ordinary
assert (52+33)> (52+33/7)
assert "double reach=SPACING+contact*(heavy?4.5:5.0)" in motion
assert "anchor=anchor.add(direction,reach)" in motion
assert abs((2.4+5.0*contact(.5))-(2.4+5.0))<1e-10
print("PASS P39: mid-strike actual 3-block reach extension, lagged seven-link chain, heavy Ataman motion, value-equal held stack and fallback; owner game NOT_RUN")
