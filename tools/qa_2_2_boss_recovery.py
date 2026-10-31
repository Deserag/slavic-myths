#!/usr/bin/env python3
"""Asset/registry sanity checks for 2.2.13/14 boss recovery (not a Minecraft runtime test)."""
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = Path("src/main/resources/assets/slavicmyths")
JAVA = Path("src/main/java/org/slavicmyths")


def read(path):
    file = ROOT / path
    assert file.is_file(), f"Missing {path}"
    return file.read_text(encoding="utf-8")


def contains(path, *tokens):
    text = read(path)
    for token in tokens:
        assert token in text, f"Missing {token!r} in {path}"


def png(path, width, height):
    file = ROOT / path
    assert file.is_file(), f"Missing {path}"
    image = file.read_bytes()
    assert image[:8] == b"\x89PNG\r\n\x1a\n", f"Invalid PNG signature: {path}"
    assert len(image) > 33 and image[12:16] == b"IHDR", f"Invalid PNG header: {path}"
    assert struct.unpack(">II", image[16:24]) == (width, height), f"Wrong PNG size: {path}"


def main():
    contains(JAVA / "registry/ModEntities.java", 'ENTITIES.register("cursed_mytar"', 'ENTITIES.register("bauk"', 'event.put(CURSED_MYTAR.get()', 'event.put(BAUK.get()')
    contains(JAVA / "registry/ModItems.java", 'ITEMS.register("mytar_seal"', 'ITEMS.register("mytar_sickle"', 'ITEMS.register("mytar_shackled_sickle"', 'ITEMS.register("bauk_fang"', 'ITEMS.register("bauk_hide_charm"')
    contains(JAVA / "registry/ModBlocks.java", 'BLOCKS.register("bauk_head"')
    contains(JAVA / "posad/boss/PosadBoss.java", 'chainVictim', 'pullChainedVictim()', 'finishChainedVictim()', 'PosadBossLedger.get(server)', 'markNaturalEncounter()')
    contains(JAVA / "posad/boss/PosadBossWorld.java", 'for (int probe = 0; probe < 16; probe++)', 'safe(level, pos, kind)')
    contains(JAVA / "client/PosadBossModel.java", 'leftSickle', 'chainSickle', 'Right horn taper')
    for name in ("PosadBossLedger", "BaukCharmEffect", "BaukBellInterrupt", "ShackledSickleItem"):
        read(JAVA / f"posad/boss/{name}.java")
    for name in ("cursed_mytar", "bauk"):
        png(ASSETS / f"textures/entity/{name}.png", 128, 128)
        png(ASSETS / f"textures/item/{name}_spawn_egg.png", 32, 32) if (ROOT / ASSETS / f"textures/item/{name}_spawn_egg.png").is_file() else None
    for name in ("mytar_shackled_sickle", "mytar_sickle"):
        png(ASSETS / f"textures/item/{name}.png", 32, 32)
    for name in ("bauk_head_closed", "bauk_head_open"):
        block = json.loads(read(ASSETS / f"models/block/{name}.json"))
        assert len(block["elements"]) >= 8, f"Mounted Bauk head missing sculpted horns: {name}"
    for locale in ("ru_ru", "en_us"):
        lang = json.loads(read(ASSETS / f"lang/{locale}.json"))
        for key in ("entity.slavicmyths.cursed_mytar", "entity.slavicmyths.bauk", "item.slavicmyths.mytar_shackled_sickle", "item.slavicmyths.bauk_fang"):
            assert key in lang, f"Missing {locale} localization: {key}"
    for slot, item in (("belt", "slavicmyths:mytar_purse"), ("charm", "slavicmyths:bauk_hide_charm")):
        tags = json.loads(read(Path("src/main/resources/data/curios/tags/item") / f"{slot}.json"))
        assert item in tags["values"], f"Missing Curios {slot}: {item}"
    for name in ("bauk_hide_charm", "mytar_shackled_sickle"):
        json.loads(read(Path("src/main/resources/data/slavicmyths/recipe") / f"{name}.json"))
    print("PASS: registries, boss source, UV PNG sizes, icons, localization, tags and recipes")
    print("NOT VERIFIED: Java compilation, GameTest, actual spawn frequency, model rendering, combat, MP loot")


if __name__ == "__main__":
    main()
