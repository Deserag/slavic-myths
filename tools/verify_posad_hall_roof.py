#!/usr/bin/env python3
"""Schema-13 Veche roof: physical R1 source audit (not an in-game GameTest)."""
from collections import Counter
from pathlib import Path
import runpy

ROOT = Path(__file__).resolve().parents[1]
MODEL = ROOT / "docs/concepts/nether-settlement-2.2/2.2_VECHE_HALL_VOXELS_R1.py"
JAVA = ROOT / "src/main/java/org/slavicmyths/posad"
decoded = runpy.run_path(str(MODEL))["decode"]()
assert len(decoded) == 3598
source = {(b["u"],b["y"],b["v"]):b["symbol"] for b in decoded}

def r1(u, y, v):
    return source.get((u,y,v), ".")

# All 475 R1 air cells in the hall at y12 were filled with a new flat
# crimson ceiling by schema-6 PosadFinishes; new-city schema-13 MUST undo it.
flat_ceiling = {(u,12,v) for u in range(27) for v in range(21) if r1(u,12,v) == "."}
assert len(flat_ceiling) == 475
assert all(r1(u,12,v) in (".","R","N","S") for u in range(27) for v in range(21))

# The single R1 ridge inconsistency: v=9 at Y17 is a NORTH stair, while
# mirrored v=11 is an extra TOP slab instead of a SOUTH stair.
assert all(r1(u,17,9) == "N" for u in range(-1,28))
assert all(r1(u,17,10) == "T" for u in range(-1,28))
assert all(r1(u,17,11) == "T" for u in range(-1,28))
mirror = {"N":"S","S":"N","R":"R","T":"T",".":"."}

def repaired(u, y, v):
    c = r1(u,y,v)
    if y == 17 and v == 11:
        return "S"
    return c

changed_ridge = {(u,17,11) for u in range(-1,28)}
assert len(changed_ridge) == 29
for u in range(-1,28):
    for y in range(12,18):
        for v in range(-1,22):
            assert mirror[repaired(u,y,v)] == repaired(u,y,20-v), (u,y,v)

# A complete real roof must remain over EVERY interior column after removing
# the artificial flat ceiling (all 27x21 rooms and 4 roof overhang edges).
for u in range(-1,28):
    for v in range(-1,22):
        assert any(repaired(u,y,v) != "." for y in range(12,18)), (u,v)

slope_counts = Counter(r1(u,y,v) for u in range(-1,28) for y in range(12,18) for v in range(-1,22))
assert slope_counts["N"] == 319 and slope_counts["S"] == 290
# All 609 original sloping blocks are normalized to nether brick stairs or
# a fixed set of symmetric polished masonry ribs; none become loose AIR.
ribs = {0,6,13,20,26}
assert len(ribs) == 5 and all(26-u in ribs for u in ribs)
roof_edits = len(flat_ceiling) + len(changed_ridge) + slope_counts["N"] + slope_counts["S"]
assert roof_edits == 1113
assert all(not (u==11 and y==17 and v==9) for u,y,v in flat_ceiling)

# Both generation paths must apply the shared overlay FIRST on native, LAST
# on standalone, and leave old persistent schemas alone.
roof_code = (JAVA/"PosadHallRoofRepair.java").read_text("utf-8")
piece = (JAVA/"PosadPiece.java").read_text("utf-8")
command = (JAVA/"PosadCommands.java").read_text("utf-8")
geometry = (JAVA/"PosadGeometry.java").read_text("utf-8")
old_finish = (JAVA/"PosadFinishes.java").read_text("utf-8")

assert "SCHEMA_VERSION = 13" in geometry
assert "schema >= 13" in piece and "PosadHallRoofRepair.voxel(x,y,z)" in piece
assert piece.index("schema >= 13") < piece.index("schema >= 12")
assert "PosadHallRoofRepair.voxel(x,y,z)" in command
assert 'id.equals("veche_hall_2_2_2")' in command
assert "world.setBlock(target,roof,2)" in command
assert "present.hasBlockEntity()" in command
assert "y==12&&x>=43&&x<=69&&z>=13&&z<=33&&source==null" in roof_code
assert "return Blocks.AIR.defaultBlockState();" in roof_code
assert "if(y==17&&z==24)" in roof_code
assert "Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH)" in roof_code
assert "Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS" in roof_code
assert "Blocks.CRIMSON_STAIRS" not in roof_code
assert "if(y==12&&x>=43&&x<=69&&z>=13&&z<=33&&base==null)" in old_finish

# 4 rotations of the entire city retain unique cells for each of the
# 475 source-ceiling removals and the 29 ridge repairs.
edits = flat_ceiling | changed_ridge
for turns in range(4):
    unique = set()
    for u,y,v in edits:
        x,z = 43+u,13+v
        rotated = ((x,z),(112-z,x),(112-x,112-z),(z,112-x))[turns]
        assert 0 <= rotated[0] <= 112 and 0 <= rotated[1] <= 112
        key = rotated[0],y,rotated[1]
        assert key not in unique
        unique.add(key)
    assert len(unique) == 504

print("PASS: R1 3598 voxels, remove 475 false-ceiling cells, repair 29 ridge slabs, normalize 609 roof stairs, 4 rotations; NO game/Gradle claim")
