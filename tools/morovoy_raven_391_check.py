#!/usr/bin/env python3
"""Fast offline integrity tests. These do NOT replace Minecraft runtime QA."""
import json
import pathlib
import struct
import zlib

root = pathlib.Path(__file__).resolve().parents[1]
java = (root / "src/main/java/org/slavicmyths").resolve()
assets = root / "src/main/resources/assets/slavicmyths"
model = (java / "client/MorovoyRavenModel.java").read_text()
mob = (java / "morovoy/MorovoyRaven.java").read_text()
content = (java / "morovoy/MorovoyRavenContent.java").read_text()
assert all(f'primary_{side}' in model and f'secondary_{side}' in model for side in ("left", "right"))
assert all(f'{side}Primary.zRot' in model and f'{side}Secondary.zRot' in model for side in ("left", "right"))
assert 'MorovoyRaven.DIVE' in model and 'MorovoyRaven.WING' in model
assert all(action in mob for action in ('PLACEHOLDER_NEVER' if False else 'PROJECTILE', 'SUMMON', 'DIVE', 'PLAGUE', 'WING', 'CLAWS', 'BEAK'))
assert all(token in content for token in ('morovoy_raven_feather','mor_core'))
assert 'noPhysics = true' not in mob and 'noClip' not in mob
assert 'setBlock(' not in mob
for item in ("mor_core","morovoy_raven_feather"):
    definition = json.loads((assets / f"models/item/{item}.json").read_text())
    assert definition["parent"] == "minecraft:item/generated"
    assert (assets / f"textures/item/{item}.png").exists()
for path, wh in [
    ("textures/entity/morovoy_raven.png", (256,256)),
    ("textures/entity/morovoy_blight.png", (16,16)),
    ("textures/item/morovoy_raven_feather.png", (16,16)),
    ("textures/item/mor_core.png", (16,16)),
]:
    file = (assets / path).read_bytes()
    assert file[:8] == b"\x89PNG\r\n\x1a\n", path
    assert struct.unpack(">II",file[16:24]) == wh, path
    offset = 8
    while offset < len(file):
        length, = struct.unpack(">I",file[offset:offset+4])
        name = file[offset+4:offset+8]
        payload = file[offset+8:offset+8+length]
        checksum, = struct.unpack(">I",file[offset+8+length:offset+12+length])
        assert checksum == zlib.crc32(name+payload)&0xffffffff, path
        offset += 12 + length
for lang in ("en_us",):
    names = json.loads((assets / f"lang/{lang}.json").read_text())
    assert all(name in names for name in ("entity.slavicmyths.morovoy_raven","item.slavicmyths.mor_core","item.slavicmyths.morovoy_raven_feather"))
print("PASS: four independently keyed wings, combat scope, item models, PNG checksums, English localization")
print("NOT RUN: Java compile, in-game visual QA, multiplayer, persistence and performance are separate gates.")
