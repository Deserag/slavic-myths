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
arena = (java / "morovoy/MorovoyArenaPiece.java").read_text()
# Geometry/UV source contracts; real gameplay screenshots remain mandatory.
renderer = (java / "client/MorovoyRavenRenderer.java").read_text()
for wing in ("main_left", "main_right", "upper_left", "upper_right"):
    assert f'"{wing}"' in model, f"Missing independently articulated {wing}"
for bone in ("mainLeft", "mainRight", "upperLeft", "upperRight"):
    assert f'{bone}.zRot =' in model and f'{bone}.xRot =' in model, f"Not independent: {bone}"
for wrist in ("mainLeftOuter", "mainRightOuter", "upperLeftOuter", "upperRightOuter"):
    assert f'{wrist}.zRot =' in model, f"Distal feather fan lacks animation: {wrist}"
assert 'float innerLength = upper ? 40F : 54F' in model, "Main shoulder span contracted"
assert 'float outerLength = upper ? 40F : 60F' in model, "Main outer wing span contracted"
assert 'PartPose.offset(0, -31, -30)' in model, "Forehead anchor moved"
assert 'PartPose.offset(0, -7, 42)' in model, "Tail-base anchor moved"
assert 'buildWing(bird, "main_left", -1, false)' in model, "Main wings must be lower pair"
assert 'buildWing(bird, "upper_left", -1, true)' in model, "Mutant upper V must be independent"
assert all(k in model for k in ('"cranium"', '"upper_beak_wedge"', '"upper_beak_hook"', '"hooked_tip"', '"jaw"', 'jaw.xRot')), "Beak/hinged mandible missing"
assert '"talon_" + toe' in model, "Three hooked foreclaws missing"
assert '"tail_feather_" + i' in model, "Seven tail streamers missing"
assert 'sternum_crack' not in model and 'breast_vein' not in model and 'mor_branch' not in model, "Projected plague must not be hardcoded volume geometry"
assert 'morovoy_raven_emissive.png' in renderer and 'morovoy_raven_glow.png' not in renderer, "Renderer still uses legacy glow resource"
assert 'RenderType.eyes(EMISSIVE)' in renderer, "No same-rig UV emission pass"
assert 'double reach = minion() ? 2.4 : landing ? 5.5 : 9.0' in mob, "Actual 4-wing collision envelope regressed"

bbmodel = json.loads((root / 'docs/implementation/2.3.1-morovoy-raven-391/MOROVOY_RAVEN_WIP.bbmodel').read_text())
assert len({e['uuid'] for e in bbmodel['elements']}) == len(bbmodel['elements']), 'duplicate Blockbench cube UUIDs'
wing_groups = {p['name']: p for p in bbmodel['outliner'][0]['children'] if isinstance(p, dict) and p['name'] in ('main_left','main_right','upper_left','upper_right')}
assert len(wing_groups) == 4, 'Blockbench main/upper wing roots must all exist'
outer_bones = {}
for wing_name, group in wing_groups.items():
    outer = [child for child in group['children'] if isinstance(child, dict) and child.get('name') == 'outer']
    assert len(outer) == 1 and len(outer[0]['children']) >= 3, f'wing distal feathers missing: {wing_name}'
    outer_bones[wing_name] = outer[0]['uuid']
for clip_name in ('flight_cycle','hover','summon_wing_spread'):
    clip = next(a for a in bbmodel['animations'] if a['name'] == clip_name)
    assert all(uid in clip['animators'] for uid in outer_bones.values()), f'distal bone animation missing: {clip_name}'

assert 'MorovoyRaven.TAKEOFF' in model and 'MorovoyRaven.LANDING' in model and 'MorovoyRaven.GROUND_IDLE' in model
assert 'pose(TAKEOFF)' in mob and 'pose(LANDING)' in mob and 'landingSpot(world)' in mob
assert 'boss.setArenaAnchor(center)' in arena and 'MorovoyRavenContent.BOSS.get().create' in arena
assert all(f'"main_{side}"' in model and f'"upper_{side}"' in model for side in ("left", "right"))
assert all(f'{wing}.zRot' in model for wing in ("mainLeft", "mainRight", "upperLeft", "upperRight"))
assert 'MorovoyRaven.DIVE' in model and 'MorovoyRaven.WING' in model
assert all(action in mob for action in ('PLACEHOLDER_NEVER' if False else 'PROJECTILE', 'SUMMON', 'DIVE', 'PLAGUE', 'WING', 'CLAWS', 'BEAK'))
assert all(token in content for token in ('morovoy_raven_feather','mor_core'))
assert 'orbitAngle = (orbitAngle + (pose() == PLAGUE ? .010 : .012))' in mob, 'orbit target outpaces actual raven'
assert 'phaseTicks >= 720' in mob and 'tag.putDouble("OrbitAngle", orbitAngle)' in mob, 'complete plume ring or save persistence missing'
assert 'phaseTicks = Math.clamp(tag.getInt("PhaseTicks"), 0, 800)' in mob, 'phase must resume after 200 ticks'
assert 'wingTelegraph(world)' in mob and 'Vec3 lateral = new Vec3(horizontal.z, 0, -horizontal.x);' in mob, 'telegraphed fan-sector collision missing'
assert 'noPhysics = true' not in mob and 'noClip' not in mob
assert 'setBlock(' not in mob
for item in ("mor_core","morovoy_raven_feather"):
    definition = json.loads((assets / f"models/item/{item}.json").read_text())
    assert definition["parent"] == "minecraft:item/generated"
    assert (assets / f"textures/item/{item}.png").exists()
for path, wh in [
    ("textures/entity/morovoy_raven.png", (256,256)),
    ("textures/entity/morovoy_raven_emissive.png", (256,256)),
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
print("PASS: source-only four independently animated wings, editable rig naming, UV-only emissive PNG, beak hinge, arena and encounter checks")
print("NOT RUN: Java compile, in-game visual QA, multiplayer, persistence and performance are separate gates.")
