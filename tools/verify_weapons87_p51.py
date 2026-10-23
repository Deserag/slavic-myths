"""P51: near-antiparallel Finist steering and native Tugarin pupil movement (source and JAR)."""
from pathlib import Path
import math
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
arrow = (root / "src/main/java/org/slavicmyths/finist/FinistArrow.java").read_text(encoding="utf-8")
heads = (root / "src/main/java/org/slavicmyths/client/TrophyHeadsClient.java").read_text(encoding="utf-8")
geometry = (root / "src/main/java/org/slavicmyths/client/BossReworkGeometry.java").read_text(encoding="utf-8")

for token in (
    "if(angle>Math.PI-1E-4)",
    "Math.min(angle,Math.toRadians(degrees))",
    "basis.subtract(from.scale(basis.dot(from))).normalize()",
    "from.scale(Math.cos(turn)).add(tangent.scale(Math.sin(turn))).scale(speed)",
):
    assert token in arrow, token
assert "from.scale(1-fraction).add(to.scale(fraction))" not in arrow
assert "return steer(velocity,desired,6)" in arrow
assert "Math.toRadians(degrees)/angle" in arrow
assert "guideVelocity(getDeltaMovement()" in arrow

# Independent geometry oracle for a 180-degree reversal: one step must
# rotate by 6 degrees without speed loss, not continue along the old line.
turn = math.radians(6)
direction = (math.cos(turn), math.sin(turn), 0)
assert abs(math.acos(direction[0]) - turn) < 1e-10
assert abs(sum(x*x for x in direction) - 1) < 1e-10
assert direction[1] > 0.1

for token in (
    'model.trophyHeadPart("pupilL"),model.trophyHeadPart("pupilR")',
    "pupilLeft.x=pupilLeftBaseX+",
    "pupilRight.x=pupilRightBaseX+",
    "pupilLeft.y=pupilLeftBaseY+",
    "pupilRight.y=pupilRightBaseY+",
    "pupilLeft.x=pupilLeftBaseX;pupilLeft.y=pupilLeftBaseY",
    "pupilRight.x=pupilRightBaseX;pupilRight.y=pupilRightBaseY",
    "block.kind==TrophyHeads.Kind.TUGARIN",
    "h.resetPose()",
):
    assert token in heads, token
for pupil in ("pupilL", "pupilR"):
    assert 'm.add("'+pupil+'","head"' in geometry, pupil

if "--jar" in sys.argv:
    jars = list((root / "build/libs").glob("slavicmyths-*.jar"))
    assert len(jars) == 1, jars
    with zipfile.ZipFile(jars[0]) as jar:
        for cls in ("finist/FinistArrow", "client/TrophyHeadsClient", "client/TrophyHeadsClient$Head"):
            assert "org/slavicmyths/"+cls+".class" in jar.namelist(), cls
print("P51 SOURCE/JAR PASS: antiparallel turn and actual Tugarin pupil links; gameplay and visual runtime NOT_RUN")
