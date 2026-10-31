"""Static regression: Nether boss sounds must register before the NeoForge sound bus freezes."""
from pathlib import Path
import json
root = Path(__file__).resolve().parents[1]
main = (root / "src/main/java/org/slavicmyths/SlavicMyths.java").read_text(encoding="utf-8")
registry = (root / "src/main/java/org/slavicmyths/netherboss/NetherBossSounds.java").read_text(encoding="utf-8")
sounds = json.loads((root / "src/main/resources/assets/slavicmyths/sounds.json").read_text(encoding="utf-8"))
assert main.count("NetherBossSounds.init();") == 1
assert main.index("NetherBossSounds.init();") < main.index("ModSounds.SOUNDS.register(bus);")
cues = ("ambient", "windup", "impact", "zone", "wave", "summon", "phase", "hurt", "death", "ritual", "trophy")
for boss in ("morova", "trizna"):
    for cue in cues:
        key = boss + "_" + cue
        assert key in sounds and sounds[key].get("sounds"), f"Missing sound event mapping: {key}"
assert '"morova"' in registry and '"trizna"' in registry
print("PASS: 22 Nether boss cues mapped and initialized before sound registry")
