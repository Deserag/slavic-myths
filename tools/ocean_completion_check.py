#!/usr/bin/env python3
"""Fast release gates independent of the NeoForge Gradle environment."""
import json
import pathlib
import struct
import zlib

root = pathlib.Path("src/main/resources")
assets = root / "assets/slavicmyths"
recipes = root / "data/slavicmyths/recipe"
catalog = (assets / "creative_catalog.tsv").read_text(encoding="utf-8")
languages = {lang: json.loads((assets / "lang" / f"{lang}.json").read_text(encoding="utf-8")) for lang in ("ru_ru", "en_us")}
ids = (
    "storm_anchor", "abyss_staff", "storm_harpoon",
    "tide_ring", "abyss_necklace", "storm_bracelet", "tailwind_feather",
    "tsar_pike_belt", "zhelva_amulet", "marine_compass", "depths_lantern", "breathing_bowl",
)
for item_id in ids:
    assert sum(line.startswith(item_id + "\t") for line in catalog.splitlines()) == 1, item_id + " creative catalog"
    for name, data in languages.items():
        assert data.get("item.slavicmyths." + item_id), (name, item_id)
        assert data.get("item.slavicmyths." + item_id + ".desc"), (name, item_id, "desc")
    model = json.loads((assets / "models/item" / f"{item_id}.json").read_text(encoding="utf-8"))
    if item_id in {"storm_anchor", "abyss_staff", "storm_harpoon"}:
        # These three rewards are rendered as 3D items via the built-in entity renderer,
        # so they intentionally have a particle texture, not a generated-item layer0.
        assert model.get("parent") == "builtin/entity", (item_id, "3d model parent")
        assert model.get("textures", {}).get("particle") == "minecraft:block/prismarine", (item_id, "3d particle")
        for perspective in ("gui", "ground", "firstperson_righthand", "firstperson_lefthand",
                            "thirdperson_righthand", "thirdperson_lefthand"):
            assert perspective in model.get("display", {}), (item_id, "3d display", perspective)
    else:
        assert model.get("parent") == "minecraft:item/generated", (item_id, "generated model parent")
        assert model.get("textures", {}).get("layer0") == "slavicmyths:item/ocean/" + item_id, (item_id, "layer0")
    path = assets / "textures/item/ocean" / (item_id + ".png")
    png = path.read_bytes()
    assert png[:8] == b"\x89PNG\r\n\x1a\n", (item_id, "png signature")
    assert struct.unpack(">II", png[16:24]) == (16,16), (item_id, "size")
    offset, data = 8, b""
    while offset < len(png):
        length = struct.unpack(">I", png[offset:offset+4])[0]
        kind = png[offset+4:offset+8]
        payload = png[offset+8:offset+8+length]
        expected = struct.unpack(">I", png[offset+8+length:offset+12+length])[0]
        assert (zlib.crc32(kind+payload) & 0xffffffff) == expected, (item_id, "crc")
        if kind == b"IDAT":
            data += payload
        offset += 12+length
    assert len(zlib.decompress(data)) == (16*4+1)*16, (item_id,"inflate")
    recipe = json.loads((recipes / (item_id+".json")).read_text(encoding="utf-8"))
    assert recipe["result"]["id"] == "slavicmyths:" + item_id, (item_id,"recipe")
    assert any("slavicmyths:" in json.dumps(ingredient) for ingredient in recipe["key"].values()), item_id + " marine ingredient"
print("12 ocean items: PNG, models, recipes, RU/EN and creative catalog verified")
