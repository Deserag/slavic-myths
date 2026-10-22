"""Offline #87/#115 proof for twelve third-person crossbow models; never launches Minecraft."""
from pathlib import Path
import copy, json, math, zipfile
from weapons87_crossbow_grips import proposal, IDS, SUFFIXES, GRIP
from weapons87_grips import rotate

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
ARCHIVE = ROOT / "docs/implementation/weapons87/before-crossbow-tp-grips.zip"

def original(p, archive):
    return json.loads(archive.read(p).decode("utf-8"))

def shift(model, hand, point):
    meta = model["display"][hand]
    a, t, s = meta["rotation"], meta["translation"], meta["scale"]
    d = rotate([(point[i] - 8) * s[i] for i in range(3)], a)
    return [round(t[i] + d[i], 8) for i in range(3)]

def run():
    assert ARCHIVE.is_file(), "Missing frozen source baseline"
    proposed = proposal()
    sample_count = 0
    hand_checks = 0
    asset_checks = 0
    with zipfile.ZipFile(ARCHIVE) as z:
        for name in IDS:
            for suffix in SUFFIXES:
                path = "models/item/" + name + suffix + ".json"
                prod_path = "src/main/resources/assets/slavicmyths/" + path
                source = original(prod_path, z)
                final = json.loads((ASSETS / path).read_text(encoding="utf-8"))
                assert final == proposed[path], path + " generator reproducibility"
                assert source["elements"] == final["elements"]
                assert source["textures"] == final["textures"]
                assert source.get("overrides") == final.get("overrides")
                before_except = copy.deepcopy(source)
                after_except = copy.deepcopy(final)
                for handed in ("thirdperson_righthand", "thirdperson_lefthand"):
                    assert source["display"][handed]["rotation"] == final["display"][handed]["rotation"] == [-90, 0, 0]
                    assert source["display"][handed]["scale"] == final["display"][handed]["scale"] == [0.55] * 3
                    assert source["display"][handed]["translation"] == [0, 2, 0]
                    assert final["display"][handed]["translation"] == [0, -2.475, -1.375]
                    before_except["display"].pop(handed)
                    after_except["display"].pop(handed)
                    before_distance = math.dist(shift(source, handed, GRIP), [0, 0, 0])
                    after_distance = math.dist(shift(final, handed, GRIP), [0, 0, 0])
                    assert abs(before_distance - math.hypot(4.475, 1.375)) < 1e-7, (path, handed, before_distance)
                    assert after_distance < 1e-7, (path, handed, after_distance)
                    hand_checks += 2
                assert before_except == after_except, path + " changed out-of-scope metadata"
                for texture in set(final.get("textures", {}).values()):
                    if texture.startswith("slavicmyths:"):
                        resolved = ASSETS / "textures" / (texture.split(":", 1)[1] + ".png")
                        assert resolved.is_file(), resolved
                        asset_checks += 1
                grip_matches = [e for e in final["elements"] if all(e["from"][i] <= GRIP[i] <= e["to"][i] for i in range(3))]
                assert len(grip_matches) >= 1, path + " has no authored grip"
                sample_count += 1
    assert sample_count == 12 and hand_checks == 48
    print(f"PASS: {sample_count} production states, {hand_checks} old/new hand pivots, {asset_checks} referenced textures; no geometry/FP/gameplay changes; runtime NOT_RUN")

if __name__ == "__main__":
    run()
