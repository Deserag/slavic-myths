#!/usr/bin/env python3
"""Source-of-truth geometry and schema19 wiring. This is static QA, NOT Minecraft."""
import json
from pathlib import Path
from collections import deque

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"docs/concepts/nether-settlement-2.2/posadnik-terem"
base=json.loads((SRC/"posadnik_terem_A_proposal.json").read_text("utf-8"))
delta=json.loads((SRC/"posadnik_terem_Aplus_delta.json").read_text("utf-8"))
assert base["block_count"]==delta["baseline_blocks"]==1259
assert len(base["blocks"])==1259 and len(delta["placements"])==80
voxels={(x,y,z):base["palette"][p] for x,y,z,p in base["blocks"]}
assert len(voxels)==1259
prior=dict(voxels)
for x,y,z,p in delta["placements"]:
    assert 0<=p<len(delta["palette"])
    voxels[x,y,z]=delta["palette"][p]
assert len(voxels)==1337
assert sum((x,y,z) in prior for x,y,z,p in delta["placements"])==2
assert len({(x,y,z) for x,y,z,p in delta["placements"]})==80
for (x,y,z),state in voxels.items():
    assert 60<=x<=72 and 78<=z<=95 and 0<=y<=17
    assert not 53<=x<=59
    assert state.startswith(("minecraft:","slavicmyths:"))
assert all(voxels[(x,y,z)]==s for (x,y,z),s in prior.items() if y>=12)
districts=[(42,70,12,34),(12,37,12,37),(76,100,12,39),(12,37,44,69),(76,100,44,69),(12,39,75,99),(75,101,75,99)]
for x,y,z in voxels:
    assert all(not (a<=x<=b and c<=z<=d) for a,b,c,d in districts),(x,y,z)
    assert not (40<=x<=72 and 40<=z<=72)
assert (60,1,79) not in voxels and (60,1,91) not in voxels,"existing street lantern conflict"
for z in (84,85):
    assert "crimson_door" in voxels[62,1,z] and "crimson_door" in voxels[62,2,z]
    assert (61,1,z) not in voxels and (61,2,z) not in voxels
for x in (67,68):
    for y in (1,2): assert (x,y,89) not in voxels
    for y in (7,8,9):assert (x,y,88) not in voxels
for step in range(6):
    x,y,z=64,step+1,step+81
    assert "crimson_stairs" in voxels[x,y,z]
    if step<5:assert (x,y+1,z) not in voxels and (x,y+2,z) not in voxels
for (x,y,z),state in voxels.items():
    if not state.startswith("slavicmyths:"):continue
    assert (x,y-1,z) in voxels,("unsupported furniture",x,y,z)
    if "_shelf" in state:
        dx=1 if "facing=west" in state else -1
        assert voxels.get((x+dx,y,z),"").startswith(("minecraft:polished_blackstone_bricks","minecraft:polished_basalt")),("shelf support",x,y,z)

def open_cell(x,y,z):return (x,y,z) not in voxels and (x,y+1,z) not in voxels and (x,y-1,z) in voxels
def reachable(start,y):
    found={start};queue=deque([start])
    while queue:
        x,z=queue.popleft()
        for nx,nz in ((x+1,z),(x-1,z),(x,z+1),(x,z-1)):
            if (nx,nz) not in found and 62<=nx<=71 and 79<=nz<=94 and open_cell(nx,y,nz):
                found.add((nx,nz));queue.append((nx,nz))
    return found
assert {(67,90),(67,82)}<=reachable((66,84),1)
assert {(67,91),(67,83)}<=reachable((67,87),7)
for turns in range(4):
    transformed=set()
    for x,y,z in voxels:
        for _ in range(turns):x,z=112-z,x
        assert (x,y,z) not in transformed
        transformed.add((x,y,z))
    assert len(transformed)==1337

java=ROOT/"src/main/java/org/slavicmyths/posad"
geometry=(java/"PosadGeometry.java").read_text("utf-8")
piece=(java/"PosadPiece.java").read_text("utf-8")
commands=(java/"PosadCommands.java").read_text("utf-8")
terem=(java/"PosadnikTerem.java").read_text("utf-8")
manual=(java/"PosadnikTeremManual.java").read_text("utf-8")
gradle=(ROOT/"build.gradle").read_text("utf-8")
physical=(ROOT/"tools/posad-runtime-tests/org/slavicmyths/verify/PosadShellGameTests.java").read_text("utf-8")
standalone=(ROOT/"tools/posad-runtime-tests/org/slavicmyths/verify/PosadCommandGameTests.java").read_text("utf-8")
assert "SCHEMA_VERSION = 21" in geometry and "POSADNIK_TEREM_ENVELOPE" in geometry
assert "schema >= 19" in piece and "PosadnikTerem.voxel(x,y,z)" in piece
assert piece.index("schema >= 19") < piece.index("schema >= 18")
assert "posadnik_terem_A_proposal.json" in gradle and "posadnik_terem_Aplus_delta.json" in gradle
assert "posadnik_terem_A_proposal.json" in terem and "posadnik_terem_Aplus_delta.json" in terem
assert "PosadnikTeremManual.place(source,pos)" in commands
assert "posadnik_terem_2_2_9" in commands and "POSADNIK_A_PLUS_NATIVE_PASS" in physical
assert "posadnikStandaloneBuildAndNoDuplicate" in standalone and "BLOCK_COUNT=1337" in terem
assert "records.add(token,\"construction\"" in manual and "records.replace(token" in manual
print("PASS: approved 1337 A+ voxels, exact source roof, six-step stair, rooms and street lamp safety")
print("PASS: no approved slot/road/square intersections, 4 geometrical rotations, schema19 source loading")
print("PASS: native, standalone and Minecraft GameTest entry points wired")
print("NOT_RUN: Java21 build, physical server, game client, NPC and owner visual acceptance")
