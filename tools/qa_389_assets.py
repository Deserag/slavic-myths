#!/usr/bin/env python3
"""Static P0 #389 audit. PASS here does NOT prove Minecraft runtime or visual compliance."""
import json
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
DATA = ROOT / "src/main/resources/data"


def read(path):
    path = ROOT / path
    if not path.is_file():
        raise AssertionError(f"Missing resource: {path}")
    return path.read_text(encoding="utf-8")


def obj(path):
    return json.loads(read(path))


def png(path):
    data = (ROOT / path).read_bytes()
    assert data[:8] == b"\x89PNG\r\n\x1a\n", path
    off, pixels = 8, bytearray()
    width = height = None
    while off < len(data):
        count = struct.unpack_from(">I", data, off)[0]
        chunk = data[off + 4:off + 8]
        payload = data[off + 8:off + 8 + count]
        expected = struct.unpack_from(">I", data, off + 8 + count)[0]
        assert zlib.crc32(chunk + payload) & 0xFFFFFFFF == expected, f"CRC {path}"
        if chunk == b"IHDR":
            width, height, depth, color = struct.unpack_from(">IIBB", payload)
            assert (width, height, depth, color) == (512, 512, 8, 6), (path, width, height, depth, color)
        if chunk == b"IDAT":
            pixels.extend(payload)
        off += count + 12
        if chunk == b"IEND":
            break
    rgba = zlib.decompress(pixels)
    assert len(rgba) == height * (width * 4 + 1), f"Corrupt decompressed atlas {path}"


def textured_model(path, minimum):
    model = obj(path)
    assert len(model.get("elements", [])) >= minimum, path
    textures = model.get("textures", {})
    for cube in model["elements"]:
        assert len(cube["from"]) == len(cube["to"]) == 3
        assert all(a < b for a, b in zip(cube["from"], cube["to"])), (path, cube)
        assert all(-16 <= v <= 32 for v in cube["from"] + cube["to"]), f"Minecraft block model exceeds [-16,32] bake bounds: {path}"
        for face in cube.get("faces", {}).values():
            assert face["texture"].lstrip("#") in textures, (path, face)
    return model


def audit():
    java = "src/main/java/org/slavicmyths/"
    entities = read(java + "registry/ModEntities.java")
    items = read(java + "registry/ModItems.java")
    blocks = read(java + "registry/ModBlocks.java")
    boss = read(java + "posad/boss/PosadBoss.java")
    hook = read(java + "posad/boss/MytarSickleProjectile.java")
    weapon = read(java + "posad/boss/ShackledSickleItem.java")
    rig = read(java + "client/PosadBossModel.java")
    tether = read(java + "client/MytarChainProjectileRenderer.java")
    for key in ('ENTITIES.register("cursed_mytar"', 'ENTITIES.register("bauk"', 'ENTITIES.register("mytar_chain_projectile"'):
        assert key in entities, key
    for key in ('ITEMS.register("okovny_sickle"', 'ITEMS.register("mytar_seal"', 'ITEMS.register("bauk_fang"'):
        assert key in items, key
    assert 'BLOCKS.register("bauk_head"' in blocks
    assert '.sized(3.5F, 3.5F)' in entities
    assert '1350D : 1800D' in boss and 'getHealth() <= 630F' in boss
    assert 'chainWeaponAway()' in rig and 'root.setPos(0F, 24F, 0F)' in rig
    assert 'handAnchor' in hook and 'handAnchor' in tether
    assert 'MAX_RUNE_SOCKETS = 3' in weapon and 'Runes.occupied(stack)' in weapon
    capacity = obj("src/main/resources/data/slavicmyths/tags/item/rune_capacity_3.json")["values"]
    assert "slavicmyths:okovny_sickle" in capacity and "slavicmyths:mytar_shackled_sickle" in capacity
    for slot, item in (("belt", "mytar_purse"), ("charm", "bauk_hide_charm")):
        assert f"slavicmyths:{item}" in obj(f"src/main/resources/data/curios/tags/item/{slot}.json")["values"]
    recipe = obj("src/main/resources/data/slavicmyths/recipe/mytar_shackled_sickle.json")
    assert recipe["result"]["id"] == "slavicmyths:okovny_sickle"
    for name in ("bauk", "cursed_mytar"):
        png(f"src/main/resources/assets/slavicmyths/textures/entity/{name}.png")
    png("src/main/resources/assets/slavicmyths/textures/block/bauk_head.png")
    for name in ("bauk_head_closed", "bauk_head_open"):
        model = textured_model(f"src/main/resources/assets/slavicmyths/models/block/{name}.json", 40)
        assert model["textures"]["particle"] == "slavicmyths:block/bauk_head"
    item = textured_model("src/main/resources/assets/slavicmyths/models/item/okovny_sickle.json", 30)
    assert sum(1 for e in item["elements"] if any(f["texture"] == "#rune" for f in e["faces"].values())) == 3
    assert len(obj("src/main/resources/assets/slavicmyths/blockstates/bauk_head.json")["variants"]) == 8
    for locale in ("ru_ru", "en_us"):
        lang = obj(f"src/main/resources/assets/slavicmyths/lang/{locale}.json")
        for key in ("entity.slavicmyths.cursed_mytar", "entity.slavicmyths.bauk", "item.slavicmyths.okovny_sickle", "tooltip.slavicmyths.okovny_sickle.sockets"):
            assert key in lang, (locale, key)
    for name in ("BAUK", "MYTAR"):
        source = obj(f"docs/concepts/nether-settlement-2.2/bosses-2.2/sources/{name}_RIG_389.json")
        assert source["format"] == "slavic-myths-editable-voxel-rig-v1"
        assert sum(map(len, source["cuboids"].values())) >= (180 if name == "BAUK" else 140)
    print("PASS: 3 complete decompressed atlases, 3D block/item geometry, rig sources, registries, 3 rune slots, Curios, loot recipes and RU/EN keys")
    print("NOT_RUN: Java compilation, Forge GameTests, real Minecraft client render, dedicated multiplayer, PolyMC and owner acceptance")


if __name__ == "__main__":
    audit()
