#!/usr/bin/env python3
"""Schema-11 military gates: R1 parity, paired hinges and free approaches.

Static geometry only: real NeoForge door opening, collision and MP QA remain required.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
java = root / "src/main/java/org/slavicmyths/posad"
src = root / "docs/concepts/nether-settlement-2.2/druzhinny-dvor-2.2.4/2.2_DRUZHINNY_DVOR_MODEL_R1.json"
model = json.loads(src.read_text(encoding="utf-8"))
rows = model["rows_y_z_x"]
palette = model["palette"]
doors = (java / "PosadDruzhinaEntrances.java").read_text("utf-8")
piece = (java / "PosadPiece.java").read_text("utf-8")
commands = (java / "PosadCommands.java").read_text("utf-8")
geometry = (java / "PosadGeometry.java").read_text("utf-8")

assert len(rows) == 18 and len(rows[0]) == 25
assert model["non_air_block_count"] == 2440
assert sum(c != "." for layer in rows for row in layer for c in row) == 2440
assert "SCHEMA_VERSION = 16" in geometry

def original(x,y,z):
    mark = rows[y][z][x]
    return palette.get(mark) if mark != "." else None

result = {}
patched = set()
for z,direction in ((1,"north"),(11,"south")):
    for x in (13,14):
        hinge = ("left" if x == 13 else "right") if direction == "north" else ("right" if x == 13 else "left")
        for y,half in ((1,"lower"),(2,"upper")):
            old = original(x,y,z)
            assert old is not None and old.startswith("minecraft:crimson_door["), (x,y,z)
            assert f"half={half}" in old
            expected = f"minecraft:crimson_door[facing={direction},half={half},hinge={hinge}]"
            result[x,y,z] = expected
            patched.add((x,y,z))
        assert original(x,3,z) is None, ("lintel should only cover R1 air",x,z)
        assert original(x,4,z) == "minecraft:polished_blackstone_bricks"
        result[x,3,z] = "minecraft:polished_blackstone_bricks"
        patched.add((x,3,z))
        assert original(x,1,0 if z == 1 else 12) is None
        assert original(x,2,0 if z == 1 else 12) is None
        assert original(x,1,2 if z == 1 else 10) is None
        assert original(x,2,2 if z == 1 else 10) is None

assert len(patched) == 12
# Full 2-wide double doors are aligned and each pair has opposite OUTER
# hinges; wall returns at both ends remain unchanged by this overlay.
for z in (1,11):
    assert original(12,3,z) == "minecraft:polished_blackstone_bricks"
    assert original(15,3,z) == "minecraft:polished_blackstone_bricks"
    assert result[13,1,z] != result[14,1,z]

# Every right-angle city rotation is an injective mapping for all 12 edits.
for turns in range(4):
    rotated = set()
    for x,y,z in patched:
        cityx,cityz = 12+x,75+z
        wx,wz = ((cityx,cityz),(112-cityz,cityx),(112-cityx,112-cityz),(cityz,112-cityx))[turns]
        assert (wx,y,wz) not in rotated and 0 <= wx <= 112 and 0 <= wz <= 112
        rotated.add((wx,y,wz))
    assert len(rotated) == 12

assert 'doorway(1,Direction.NORTH)' in doors and 'doorway(11,Direction.SOUTH)' in doors
assert 'put(u,3,v,Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),false)' in doors
assert 'PATCH.size()!=12' in doors
assert 'DoorBlock.HALF,half' in doors and 'DoorBlock.HINGE,hinge' in doors
assert 'DoorBlock.FACING,facing' in doors
assert 'schema >= 11' in piece and 'PosadDruzhinaEntrances.voxel(x,y,z)' in piece
assert 'id.equals("druzhinny_dvor_2_2_4")' in commands
assert 'PosadDruzhinaEntrances.voxel(12+u,y,75+v)' in commands
assert 'world.setBlock(target,finish,2)' in commands
assert 'present.hasBlockEntity()' in commands
print("PASS: R1 2440 blocks immutable; 8 consistent paired door halves, 4 solid lintels, clear 2-block approaches and 4 rotations")
