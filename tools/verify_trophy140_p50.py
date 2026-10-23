"""P50 owner-rejected trophy heads: strict frequencies, visibility, native moving parts and JAR."""
from pathlib import Path
import re,sys,zipfile
R=Path(__file__).resolve().parents[1]
rules=(R/"src/main/java/org/slavicmyths/client/TrophyHeadFxRules.java").read_text()
client=(R/"src/main/java/org/slavicmyths/client/TrophyHeadsClient.java").read_text()
for tok in ("time%64", "1.8*Math.sin", "1.075+.075*Math.sin",
    ".24+.20*Math.sin", ".22+.19*Math.sin", ".38*Math.sin", ".32*Math.sin",
    ".28+.24*Math.sin", ".14*Math.sin", "public static boolean particleDue"):
    assert tok in rules,tok
for kind in ("TUGARIN","SKIM","VOLKOLAK","PODZEMNIK","LIKHO","MOR_LIKHO"):
    assert "case "+kind in client,kind
assert "case TUGARIN->9;case SKIM->10;case VOLKOLAK->9;case PODZEMNIK->11;" in client
assert "case LIKHO->8;case MOR_LIKHO->7;" in client
assert "for(int i=0;i<5;i++)world.addParticle" in client
assert "h.pose(time,gazeX,gazeY)" in client
assert "h.resetPose()" in client
assert "Minecraft.getInstance().isPaused()" in client
assert "tile.clientLastFxTick=now" in client
assert "part.render(pose,out,light,overlay)" in client
assert "glow.x=glowBaseX+" in client
assert "glow.y=glowBaseY+" in client
assert "face.getChild(\"jaw\")" in client
assert "model.trophyHeadPart(\"earL\")" in client
assert "model.trophyHeadPart(\"irisRing\")" in client
assert "head.getChild(\"brows\")" in client
if "--jar" in sys.argv:
    jars=list((R/"build/libs").glob("slavicmyths-*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        assert "org/slavicmyths/client/TrophyHeadsClient.class" in z.namelist()
        assert "org/slavicmyths/client/TrophyHeadFxRules.class" in z.namelist()
print("PASS P50: six existing trophy heads have shorter visible movement cycles, gaze and denser client-local particles; Minecraft/owner visual NOT_RUN")
