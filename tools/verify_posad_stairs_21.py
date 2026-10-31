#!/usr/bin/env python3
"""Schema-21 stair and door source audit; real placement is checked by GameTests."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = root / "src/main/java/org/slavicmyths/posad"
house = json.loads((root / "src/main/resources/data/slavicmyths/posad/communal_house_r1.json").read_text("utf-8"))
military = json.loads((root / "docs/concepts/nether-settlement-2.2/druzhinny-dvor-2.2.4/2.2_DRUZHINNY_DVOR_MODEL_R1.json").read_text("utf-8"))
overlay = (source / "PosadArchitectureR4.java").read_text("utf-8")
piece = (source / "PosadPiece.java").read_text("utf-8")
geometry = (source / "PosadGeometry.java").read_text("utf-8")
commands = (source / "PosadCommands.java").read_text("utf-8")
runtime = (root / "tools/posad-runtime-tests/org/slavicmyths/verify/PosadShellGameTests.java").read_text("utf-8")
command_tests = (root / "tools/posad-runtime-tests/org/slavicmyths/verify/PosadCommandGameTests.java").read_text("utf-8")

assert "SCHEMA_VERSION = 21" in geometry
assert "if(schema >= 21)" in piece and "PosadArchitectureR4.voxel(x,y,z)" in piece
assert piece.index("if(schema >= 21)") < piece.index("if(schema >= 20)")
assert "if(schema >= 20)" in piece and "PosadArchitectureR3.voxel(x,y,z)" in piece
assert "if(schema >= 19)" in piece and "PosadnikTerem.voxel(x,y,z)" in piece
assert 'int x=stair[0],z=stair[1],y=step+1' in runtime
assert "POSAD_R4_STAIRS_NATIVE_PASS" in runtime
assert "Standalone stair faces wrong ascent" in command_tests
assert 'id.equals("communal_house_2_2_3") || id.equals("druzhinny_dvor_2_2_4")' in commands
assert "PosadArchitectureR4.voxel(x,y,z)" in commands
assert "StairBlock.FACING,Direction.SOUTH" in overlay
house_renovation = (source / "PosadHouseRenovation.java").read_text("utf-8")
assert "for(int step=0;step<6;step++)" in house_renovation
assert "for(int x:new int[]{7,8})" in house_renovation
assert "StairBlock.FACING,Direction.NORTH" in house_renovation
assert "PATCH.size()!=24" in overlay

# The approved historical R1 files are left unchanged. Both flights rise as Z
# decreases: Minecraft's stair front/low edge must face SOUTH, opposite ascent.
all_steps = set()
for which, model, xs, local_start, offset in [
    ("communal", house, (7, 8), 11, (13, 15)),
    ("druzhina", military, (11, 12), 9, (12, 75)),
]:
    for step in range(6):
        for x in xs:
            y, z = step + 1, local_start - step
            mark = model["rows_y_z_x"][y][z][x]
            state = model["palette"][mark]
            if which == "communal":
                # These 12 stairs are authored by schema-7 HouseRenovation,
                # not the immutable R1 model. Its displaced chest remains
                # a chest only in R1; the renovation moves its loot marker.
                assert not state.startswith("minecraft:polished_blackstone_brick_stairs[")
            else:
                assert state.startswith("minecraft:polished_blackstone_brick_stairs[")
                assert "facing=north" in state and "half=bottom" in state
            city = (offset[0] + x, y, offset[1] + z)
            assert city not in all_steps
            all_steps.add(city)
            # Each cardinal rotation preserves the 24 distinct city coordinates.
assert len(all_steps) == 24
for turns in range(4):
    mapped = set()
    for x, y, z in all_steps:
        rx, rz = ((x, z), (112-z, x), (112-x, 112-z), (z, 112-x))[turns]
        assert (rx, y, rz) not in mapped and 0 <= rx <= 112 and 0 <= rz <= 112
        mapped.add((rx, y, rz))
    assert len(mapped) == 24

# The existing military gates remain valid and separate from the stair repair.
for z, lower, upper in [(1, "D", "d"), (11, "E", "e")]:
    for x in (13, 14):
        assert military["rows_y_z_x"][1][z][x] == lower
        assert military["rows_y_z_x"][2][z][x] == upper
        assert (12+x, 1, 75+z) not in all_steps
print("PASS: 24 ascending-north stair corrections, historical schemas 7..20 guarded, four rotations, military door source and native-test wiring")
print("NOT_RUN: client traversal, old-world relog, visual appearance")
