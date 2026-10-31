#!/usr/bin/env python3
"""Deterministic schema-17 R2 architectural geometry; NOT Minecraft visual QA."""
from collections import Counter
from pathlib import Path
import runpy

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/org/slavicmyths/posad"
src = runpy.run_path(str(ROOT / "docs/concepts/nether-settlement-2.2/2.2_VECHE_HALL_VOXELS_R1.py"))["decode"]()
assert len(src) == 3598
r1 = {(b["u"],b["y"],b["v"]): b["symbol"] for b in src}
def original(u,y,v):
    return r1.get((u,y,v), ".")
edits = {}
def add(u,y,v,expected,new):
    key = (u,y,v)
    assert key not in edits, key
    assert original(u,y,v) == expected, (key, original(u,y,v), expected)
    assert -1 <= u <= 27 and -1 <= v <= 21 and 0 <= y <= 16
    edits[key] = new

for y in range(1,5):
    for u in (8,9,10,16,17,18):
        add(u,y,15,"R",".")
    for u in (7,19):
        add(u,y,15,"R","P")
for u in (7,13,19):
    add(u,5,15,"R","C")
for y in (2,3,8,9):
    for u in (9,10,16,17):
        add(u,y,20,"B","G")
for u in (7,19):
    add(u,0,21,".","B")
    for y in range(1,11):
        add(u,y,21,".","B" if y == 1 else "C" if y == 10 else "P")
for u in (11,15):
    for y in range(1,6):
        add(u,y,20,"B","C" if y == 5 else "P")
for u in (12,13,14):
    add(u,4,20,"B","C")
for u in range(-1,28):
    for v in (-1,21):
        assert original(u,12,v) in ("N","S")
        add(u,11,v,".","T")
for u in (0,26):
    for y in (14,15):
        add(u,y,10,"R","G")
        for v in (9,11):
            add(u,y,v,"R","C")
    add(u,13,10,"R","V")
    add(u,16,10,"R","C")

assert len(edits) == 160
assert Counter(edits.values()) == Counter({ "T":58,"P":32,".":24,"C":20,"G":20,"B":4,"V":2})
# Two 3-wide arched openings were extended to an eleven-wide shared passage;
# the east/west pillars, top lintel, second floor and main approach remain.
for y in range(1,5):
    assert all(edits.get((u,y,15),original(u,y,15)) == "." for u in range(8,19))
    assert all(edits[u,y,15] == "P" for u in (7,19))
for u in (12,13,14):
    for v in range(14,20):
        for y in (1,2):
            assert original(u,y,v) == "."
for u in range(7,20):
    assert original(u,6,15) == "F"
for turns in range(4):
    rotated = set()
    for (u,y,v) in edits:
        x,z = 43+u,13+v
        wx,wz = ((x,z),(112-z,x),(112-x,112-z),(z,112-x))[turns]
        assert 42 <= x <= 70 and 12 <= z <= 34
        assert 0 <= wx <= 112 and 0 <= wz <= 112
        assert (wx,y,wz) not in rotated
        rotated.add((wx,y,wz))
    assert len(rotated) == 160

r2 = (JAVA/"PosadHallR2.java").read_text("utf-8")
piece = (JAVA/"PosadPiece.java").read_text("utf-8")
commands = (JAVA/"PosadCommands.java").read_text("utf-8")
geometry = (JAVA/"PosadGeometry.java").read_text("utf-8")
assert "SCHEMA_VERSION = 21" in geometry
assert "PATCH.size()!=160" in r2 and "PosadBuildings.voxel(x,y,z)" in r2
assert "isOpening(int x,int y,int z)" in r2
assert "schema >= 17" in piece and "PosadHallR2.voxel(x,y,z)" in piece
assert piece.index("schema >= 17") < piece.index("schema >= 16")
assert "PosadHallR2.isOpening(local.x(),y,local.z())" in piece
assert 'id.equals("veche_hall_2_2_2")' in commands and "PosadHallR2.voxel(x,y,z)" in commands
assert "Standalone Veche R2/NBT mismatch" in commands
assert "present.hasBlockEntity()" in commands
assert "world.setBlock(target,r2,2)" in commands
assert "LootTable" not in r2 and "Furniture" not in r2
print("PASS: R1 3598 unchanged; R2 160 bounded edits, 24 safe arch openings, 13x13 combined ceremonial nave, supported eaves/portico/windows, four rotations; static only")
