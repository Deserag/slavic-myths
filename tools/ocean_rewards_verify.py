#!/usr/bin/env python3
"""Static marine rewards regression guard. Does not replace Minecraft GameTests."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
resources = root / "src/main/resources/assets/slavicmyths"
code = root / "src/main/java/org/slavicmyths"
materials = {
    "pharaohka_tide_pearl": 3,
    "deep_sea_serpent_scale": 5,
    "ocean_zmey_storm_heart": 3,
    "stratim_bird_feather": 4,
    "tsar_pike_fang": 3,
    "zhelva_shell_plate": 6,
}
tokens = {
    "pharaohka_victory_token": 18,
    "deep_sea_serpent_victory_token": 16,
    "ocean_zmey_victory_token": 22,
    "stratim_bird_victory_token": 20,
    "tsar_pike_victory_token": 12,
    "zhelva_victory_token": 22,
}
ids = set(materials) | set(tokens)
catalog = (resources / "creative_catalog.tsv").read_text(encoding="utf-8")
rows = [line.split("\t") for line in catalog.splitlines() if line and not line.startswith("#")]
for item in ids:
    match = [row for row in rows if row[0] == item]
    assert len(match) == 1, f"Creative catalog missing or duplicate: {item}"
    assert match[0][1] == "resources"
    path = resources / "models/item" / (item + ".json")
    model = json.loads(path.read_text(encoding="utf-8"))
    assert model["parent"] == "minecraft:item/generated"
    assert model["textures"]["layer0"] == f"slavicmyths:item/ocean519/{item}", f"Incorrect marine texture: {item}"
    import struct, zlib
    png_path = resources / "textures/item/ocean519" / (item + ".png")
    png = png_path.read_bytes()
    assert png[:8] == b"\x89PNG\r\n\x1a\n", f"Invalid PNG: {item}"
    assert struct.unpack(">II", png[16:24]) == (16,16), f"Invalid PNG size: {item}"
    offset, compressed = 8, b""
    while offset < len(png):
        n = struct.unpack(">I", png[offset:offset+4])[0]
        label = png[offset+4:offset+8]
        payload = png[offset+8:offset+8+n]
        expected = struct.unpack(">I", png[offset+8+n:offset+12+n])[0]
        assert (zlib.crc32(label+payload) & 0xffffffff) == expected, f"PNG CRC mismatch: {item}"
        if label == b"IDAT": compressed += payload
        offset += 12+n
    assert len(zlib.decompress(compressed)) == 16*(1+16*4), f"Invalid PNG pixels: {item}"
for lang in ("ru_ru", "en_us"):
    data = json.loads((resources / "lang" / (lang + ".json")).read_text(encoding="utf-8"))
    for item in ids:
        assert data.get("item.slavicmyths." + item), f"Missing {item} in {lang}"
source = (code / "marine/MarineRewards.java").read_text(encoding="utf-8")
trade = (code / "buyan/BuyanTrades.java").read_text(encoding="utf-8")
loader = (code / "SlavicMyths.java").read_text(encoding="utf-8")
for item in ids:
    assert ('register("' + item + '"') in source, f"Unregistered item: {item}"
for count in materials.values():
    assert (", " + str(count) + ", ") in source
for token, price in tokens.items():
    assert (", " + str(price) + ", l);") in trade, f"Trade price missing: {token}"
assert "pharaohka.leader()" in source
assert "MarineRewards.init();" in loader
assert "princeToken(o, MarineRewards." in trade
print("Marine rewards static checks PASS: 12 items, 2 languages, 6 trades, boss guard.")
