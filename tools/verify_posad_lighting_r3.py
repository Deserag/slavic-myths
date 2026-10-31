#!/usr/bin/env python3
"""Schema-18 light placement and routing geometry; does not substitute Minecraft lighting QA."""
from pathlib import Path
import runpy

root=Path(__file__).resolve().parents[1]
java=root/"src/main/java/org/slavicmyths/posad"
r1=runpy.run_path(str(root/"docs/concepts/nether-settlement-2.2/2.2_VECHE_HALL_VOXELS_R1.py"))["decode"]()
src={(b["u"],b["y"],b["v"]):b["symbol"] for b in r1}
assert len(r1)==3598
hall=[(9,1,18),(17,1,18),(8,1,11),(18,1,11),(10,1,8),(16,1,8),(10,1,5),(16,1,5),(10,7,5),(16,7,5)]
road=[(52,z) for z in range(37,98,12)]+[(60,z) for z in range(43,104,12)]
road += [(x,z) for z in (39,75) for x in (43,69)]
road += [(x,56) for x in (40,72)]
assert len(set(hall))==10 and len(set(road))==18
for u,y,v in hall:
    assert src.get((u,y,v)) is None,("Occupied R1 hall lamp cell",u,y,v)
    assert src.get((u,y-1,v)) is not None,("Unsupported R1 lamp",u,y,v)
    assert u!=12,"R2 nave center must stay unobstructed"
slots=[(42,70,12,34),(12,37,12,37),(76,100,12,39),(12,37,44,69),(76,100,44,69),(12,39,75,99),(75,101,75,99)]
for x,z in road:
    assert not 53<=x<=59,("Public carriageway obstructed",x,z)
    assert all(not(a<=x<=b and c<=z<=d) for a,b,c,d in slots),("District intrusion",x,z)
for turn in range(4):
    cells=set()
    for x,y,z in [(43+u,y,13+v) for u,y,v in hall]+[(x,1,z) for x,z in road]:
        wx,wz=((x,z),(112-z,x),(112-x,112-z),(z,112-x))[turn]
        assert 0<=wx<=112 and 0<=wz<=112
        assert (wx,y,wz) not in cells
        cells.add((wx,y,wz))
    assert len(cells)==28
piece=(java/"PosadPiece.java").read_text("utf-8")
cmd=(java/"PosadCommands.java").read_text("utf-8")
light=(java/"PosadLightingR3.java").read_text("utf-8")
geometry=(java/"PosadGeometry.java").read_text("utf-8")
workflow=(root/".github/workflows/posad-schema16-qa.yml").read_text("utf-8")
assert "SCHEMA_VERSION = 21" in geometry
assert "schema >= 18" in piece and "PosadLightingR3.voxel(x,y,z)" in piece
assert piece.index("schema >= 18")<piece.index("schema >= 17")
assert "PosadLightingR3.hallVoxel(x,y,z)" in cmd
assert "Standalone Veche R3/NBT mismatch" in cmd
assert "world.setBlock(target,light,2)" in cmd
assert "halls!=10||roads!=18||LIGHTS.size()!=28" in light
assert "tools/verify_posad_lighting_r3.py" in workflow
print("PASS: 28 schema-18 lights, immutable 3598 R1 voxels, no district/road-lane collision, 4 rotations, native/standalone wiring; Minecraft brightness NOT_RUN")
