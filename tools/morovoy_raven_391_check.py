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
# Source-contract checks only; all original-PNG comparisons require real game frames.
assert 'PartPose.offset(side*15,-17,-8)' in model, 'lower main pair root changed'
assert 'PartPose.offset(side*12,-32,3)' in model, 'upper mutant pair root changed'
assert '"main_flight_"+i' in model and '"upper_flight_"+i' in model, 'feather rows missing'
assert 'leftSecondary.zRot = .90F + second' in model, 'upper wings not held in V'
# Four shoulders and four additional distal bones: all feathers must retain a parent in Blockbench.
assert 'PartPose.offset(side*56,0,0)' in model and 'PartPose.offset(side*36,0,0)' in model, 'independent feather fan pivots missing'
assert all(f'{side}{pair}Tip.zRot' in model for side in ('left','right') for pair in ('Primary','Secondary')), 'all four distal fans need animation'
assert 'sternum_crack_trunk' in model and 'sternum_crack_arm_left' in model and 'sternum_crack_arm_right' in model, 'branched chest Mor lesion missing'
bbmodel = json.loads((root / 'docs/implementation/2.3.1-morovoy-raven-391/MOROVOY_RAVEN_WIP.bbmodel').read_text())
assert len({e['uuid'] for e in bbmodel['elements']}) == len(bbmodel['elements']), 'duplicate Blockbench cube UUIDs'
wing_groups = {p['name']: p for p in bbmodel['outliner'][0]['children'] if isinstance(p, dict) and p['name'] in ('primary_left','primary_right','secondary_left','secondary_right')}
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
assert all(f'primary_{side}' in model and f'secondary_{side}' in model for side in ("left", "right"))
assert all(f'{side}Primary.zRot' in model and f'{side}Secondary.zRot' in model for side in ("left", "right"))
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
    ("textures/entity/morovoy_raven_glow.png", (256,256)),
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
print("PASS: source-only articulated 4-wing rig, editable Blockbench clips, full-lap plague path, telegraphed fan sector, crater anchor, PNG CRC, localization")
print("NOT RUN: Java compile, in-game visual QA, multiplayer, persistence and performance are separate gates.")
