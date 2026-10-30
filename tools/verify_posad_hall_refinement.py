#!/usr/bin/env python3
"""Validate schema-8 Veche interior against the immutable compressed R1 voxels."""
import base64
import gzip
import hashlib
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = root / "src/main/resources/data/slavicmyths/posad/veche_hall_r1.gz.base64"
java = root / "src/main/java/org/slavicmyths/posad"
raw = gzip.decompress(base64.b64decode(source.read_text("ascii").strip()))
assert len(raw) == 6003
assert hashlib.sha256(raw).hexdigest() == "8813d5c44e144ba5e6f087584f7acbf8dfa4cf25c0e31f6f9193f0b933035cda"
palette = "BCFGHKNPQRSTUVX"

def symbol(u, y, v):
    index = (y * 23 + v + 1) * 29 + u + 1
    value = raw[index // 2] >> 4 if index % 2 == 0 else raw[index // 2] & 15
    return palette[value - 1] if value else "."

assert sum(symbol(u,y,v) != "." for y in range(18) for v in range(-1,22) for u in range(-1,28)) == 3598
source_code = (java / "PosadHallRefinement.java").read_text("utf-8")
old_code = (java / "PosadFinishes.java").read_text("utf-8")
furniture = [(int(u),int(y),int(v),name) for u,y,v,name in re.findall(r'furniture\((\d+),(\d+),(\d+),"([^"]+)"\)',source_code)]
lamps = [(int(u),int(y),int(v)) for u,y,v in re.findall(r'lantern\((\d+),(\d+),(\d+)\)',source_code)]
prior_furniture = {(int(u),int(y),int(v)) for u,y,v in re.findall(r'item\((\d+),(\d+),(\d+),"[^"]+"\)',old_code)}
assert len(furniture) == 33 and len(lamps) == 4
assert len({item[:3] for item in furniture} | set(lamps)) == 37
for u,y,v,kind in furniture:
    assert 0 <= u <= 26 and 0 <= v <= 20 and y in (1,7)
    assert symbol(u,y,v) == "." and symbol(u,y+1,v) == "."
    assert (u,y,v) not in prior_furniture
    assert kind.startswith(("linden_","pine_"))
for u,y,v in lamps:
    assert symbol(u,y,v) == "."
    assert (u,y,v) not in prior_furniture
for z in (13,33):
    for y in (3,9):
        for x in (45,53,59,67):
            assert symbol(x-43,y,z-13) == "B"
for x in (43,69):
    for y in (3,9):
        for z in (16,20,24,28,32):
            assert symbol(x-43,y,z-13) == "B"
for v in range(21):
    for u in range(27):
        assert any(symbol(u,y,v) != "." for y in range(12,18))

piece = (java / "PosadPiece.java").read_text("utf-8")
command = (java / "PosadCommands.java").read_text("utf-8")
geometry = (java / "PosadGeometry.java").read_text("utf-8")
assert "SCHEMA_VERSION = 10" in geometry
assert "schema >= 8" in piece and "PosadHallRefinement.voxel" in piece
assert 'id.equals("veche_hall_2_2_2")' in command
assert "var finish=PosadHallRefinement.voxel(x,y,z)" in command
print("PASS: 3598 R1 hall voxels, 33 non-overlapping furniture items, 4 lights, facade/ridge and roof coverage")
