"""Static geometry and atlas check for the voxel snow armor."""
from collections import Counter
from pathlib import Path
import json, struct, zlib

assets = Path("src/main/resources/assets/slavicmyths")
rig = json.loads((assets / "snow_armor/rig.json").read_text())
assert rig["atlas"] == [256, 128]
assert set(rig["materials"]) == {"dark", "plate", "steel", "edge", "ice", "gem", "frost"}
valid = {"head": {"head"}, "chest": {"body", "left_arm", "right_arm"},
         "legs": {"body", "left_leg", "right_leg"}, "feet": {"left_leg", "right_leg"}}
counts = Counter()
for slot, bone, mat, x, y, z, w, h, depth in rig["parts"]:
    assert bone in valid[slot]
    assert mat in rig["materials"]
    assert min(w, h, depth) > 0
    assert 2 * (w + depth) <= 64 and h + depth <= 64, (slot, bone)
    counts[slot] += 1
    if bone == "left_leg":
        assert x >= -1.65
    if bone == "right_leg":
        assert x + w <= 1.651
assert all(counts[s] > 5 for s in valid)
assert min(p[4] for p in rig["parts"] if p[0] == "head") < -13
def check_png(path, expected):
    data = path.read_bytes()
    assert data[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", data[16:24]) == expected
    pos = 8
    packed = []
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos+4])[0]
        tag = data[pos+4:pos+8]
        payload = data[pos+8:pos+8+length]
        crc = struct.unpack(">I", data[pos+8+length:pos+12+length])[0]
        assert zlib.crc32(tag + payload) == crc
        if tag == b"IDAT":
            packed.append(payload)
        pos += length+12
    assert len(zlib.decompress(b"".join(packed))) == expected[1]*(1+expected[0]*4)
for i in (1, 2):
    check_png(assets / f"textures/models/armor/snow_armor_layer_{i}.png", (256, 128))
for piece in ("helmet", "chestplate", "leggings", "boots"):
    check_png(assets / f"textures/item/snow_{piece}.png", (16,16))
print("Snow armor geometry and 6 PNGs verified:", dict(counts))
