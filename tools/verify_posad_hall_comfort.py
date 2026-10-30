#!/usr/bin/env python3
"""Independent schema-12 Veche hall interior and roof-trim regression.

Source is the APPROVED 3598-voxel R1 model. This is static geometry QA,
not proof of game lighting, door operation, roof appearance or Minecraft GameTest.
"""
import re
import runpy
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/org/slavicmyths/posad"
source = runpy.run_path(str(ROOT / "docs/concepts/nether-settlement-2.2/2.2_VECHE_HALL_VOXELS_R1.py"))
voxels = source["decode"]()
assert len(voxels) == 3598
model = {(b["u"],b["y"],b["v"]):b["symbol"] for b in voxels}

def r1(u,y,v):
    return model.get((u,y,v),".")

f = (JAVA/"PosadFinishes.java").read_text("utf-8")
h = (JAVA/"PosadHallRefinement.java").read_text("utf-8")
c = (JAVA/"PosadHallComfort.java").read_text("utf-8")
piece = (JAVA/"PosadPiece.java").read_text("utf-8")
command = (JAVA/"PosadCommands.java").read_text("utf-8")
geometry = (JAVA/"PosadGeometry.java").read_text("utf-8")

def furniture(code):
    return {(int(x),int(y),int(z)):name for x,y,z,name in re.findall(r'(?<!void )\b(?:item|furniture)\((\d+),(\d+),(\d+),"([^"]+)"\)',code)}

old = furniture(f) | furniture(h)
assert len(old) == 62
cleared = [(int(x),int(y),int(z),name) for x,y,z,name in re.findall(r'\bclear\((\d+),(\d+),(\d+),"([^"]+)"\)',c)]
moved = [(int(x),int(y),int(z),name,facing) for x,y,z,name,facing in re.findall(r'\bplace\((\d+),(\d+),(\d+),"([^"]+)",Direction\.(NORTH|SOUTH|EAST|WEST)\)',c)]
refaced = [(int(x),int(y),int(z),name,facing) for x,y,z,name,facing in re.findall(r'\brefaceShelf\((\d+),(\d+),(\d+),"([^"]+)",Direction\.(NORTH|SOUTH|EAST|WEST)\)',c)]
floor_lights = [tuple(map(int,xyz)) for xyz in re.findall(r'\{(\d+),(\d+),(\d+)\}',c.split("// Light 15 from warm floor lanterns.")[1].split("floorLamp(p[0]")[0])]
wall_lights = [(int(x),int(y),int(z),face) for x,y,z,face in re.findall(r'\bwallLight\((\d+),(\d+),(\d+),Direction\.(NORTH|SOUTH|EAST|WEST)\)',c)]

assert len(cleared) == 17 and len(set(p[:3] for p in cleared)) == 17
assert len(moved) == 9 and len(refaced) == 6
assert len(floor_lights) == 8 and len(wall_lights) == 8
assert set(p[:3] for p in cleared).isdisjoint(set(p[:3] for p in moved))
removed = set()
occupied = set()

for x,y,z,name in cleared:
    assert old.get((x,y,z)) == name and r1(x,y,z) == ".", (x,y,z)
    removed.add((x,y,z))

def check_wall(u,y,v,front):
    dx,dz = {"NORTH":(0,1),"SOUTH":(0,-1),"WEST":(1,0),"EAST":(-1,0)}[front]
    # The player's view direction is front; the backing is its opposite.
    assert r1(u+dx,y,v+dz) in ("B","R","V","Q"), ("shelf anchor",u,y,v,front)

for x,y,z,name,front in moved:
    assert (x,y,z) not in old and r1(x,y,z) == "."
    assert r1(x,y-1,z) != "." and r1(x,y+1,z) == "." and r1(x,y+2,z) == "."
    if name.endswith("_shelf"):
        check_wall(x,y,z,front)
    assert (x,y,z) not in occupied
    occupied.add((x,y,z))

for x,y,z,name,front in refaced:
    assert old.get((x,y,z)) == name and r1(x,y,z) == "." and name.endswith("_shelf")
    check_wall(x,y,z,front)
    assert (x,y,z) not in occupied
    occupied.add((x,y,z))

for x,y,z in floor_lights:
    assert r1(x,y,z) == "." and r1(x,y-1,z) != "." and (x,y,z) not in old
    assert (x,y,z) not in occupied
    occupied.add((x,y,z))

for x,y,z,front in wall_lights:
    assert r1(x,y,z) == "." and (x,y,z) not in old
    dx,dz = {"NORTH":(0,1),"SOUTH":(0,-1),"WEST":(1,0),"EAST":(-1,0)}[front]
    assert r1(x+dx,y,z+dz) == "R", ("wall torch backing",x,y,z,front)
    assert (x,y,z) not in occupied
    occupied.add((x,y,z))

assert len(occupied) == 31
# Replacing ore-like GILDED_BLACKSTONE is restricted to 16 original solid
# facade bricks, not windows/doors and not already placed room furnishings.
ore = [(x,y,z) for z in (13,33) for y in (3,9) for x in (45,53,59,67)]
assert len(ore) == 16
for x,y,z in ore:
    assert r1(x-43,y,z-13) == "B"

# The R1 roof is a complete, symmetric two-pitch structure; only its
# asymmetric decorative stripes are corrected, not the roof volume.
assert sum(r1(u,y,v) != "." for u in range(-1,28) for v in range(-1,22) for y in range(12,18)) > 0
for u in range(0,27):
    for v in range(0,21):
        assert any(r1(u,y,v) != "." for y in range(12,18)), ("roof column",u,v)
for y in range(12,18):
    for v in range(-1,22):
        assert r1(6,y,v) == r1(20,y,v)
        assert r1(0,y,v) == r1(26,y,v)
assert "new int[]{0,6,13,20,26}" in c
assert "new int[]{7,14,21}" in c
assert "Blocks.NETHER_BRICK_STAIRS" in c and "Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS" in c
assert "Blocks.CHISELED_POLISHED_BLACKSTONE" in c and "Blocks.GILDED_BLACKSTONE" in c
assert "Blocks.LANTERN" in c and "Blocks.WALL_TORCH" in c
assert "SCHEMA_VERSION = 14" in geometry
assert "schema >= 12" in piece and "PosadHallComfort.voxel" in piece
assert 'id.equals("veche_hall_2_2_2")' in command
assert "PosadHallComfort.voxel" in command
assert "present.hasBlockEntity()" in command
print("PASS: 3598 R1 blocks, 17 clearances, 9 relocations, 6 anchored shelf facings, 8 floor + 8 wall lights, 16 ore accents, symmetric roof trim")
