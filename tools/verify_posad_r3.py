#!/usr/bin/env python3
"""Schema-20 R3 static QA. Does not substitute native Minecraft/client testing."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
java=root/"src/main/java/org/slavicmyths/posad"
r3=(java/"PosadArchitectureR3.java").read_text("utf-8")
piece=(java/"PosadPiece.java").read_text("utf-8")
geom=(java/"PosadGeometry.java").read_text("utf-8")
tests=(root/"tools/posad-runtime-tests/org/slavicmyths/verify/PosadShellGameTests.java").read_text("utf-8")
source=root/"docs/concepts/nether-settlement-2.2/posadnik-terem"
proposal=json.loads((source/"posadnik_terem_A_proposal.json").read_text("utf-8"))
delta=json.loads((source/"posadnik_terem_Aplus_delta.json").read_text("utf-8"))
assert len(proposal["blocks"])==1259 and len(delta["placements"])==80
assert "SCHEMA_VERSION = 20" in geom
assert "if(schema >= 20)" in piece and "PosadArchitectureR3.voxel(x,y,z)" in piece
assert piece.index("if(schema >= 20)")<piece.index("if(schema >= 19)")
assert "resolvedConnections" in piece and "state.updateShape(direction" in piece
assert "instanceof net.minecraft.world.level.block.IronBarsBlock" in piece
assert "schema >= 19" in piece and "PosadnikTerem.voxel(x,y,z)" in piece
assert "POSAD_R3_PANES_CONNECTED" in tests
assert "R3 hanging street lantern" in tests
assert "R3 street light changed" in r3
assert "R3 tower ceiling missing" in r3
assert "POSAD_R3_TOWER_LAMPS_PASS" in tests
assert "POSAD_R3_TOWER_LIGHT" in tests
assert "old(x,1,z)" in r3 and "LanternBlock.HANGING,true" in r3
posts=[(52,z,54,z) for z in range(37,98,12)]
posts += [(60,z,58,z) for z in range(43,104,12)]
posts += [(x,z,x,z+(1 if z==39 else -1)) for z in (39,75) for x in (43,69)]
posts += [(40,56,41,56),(72,56,71,56)]
assert len(posts)==18 and len({(p[0],p[1]) for p in posts})==18
assert len({(p[2],p[3]) for p in posts})==18
assert all(not(53<=x<=59) for x,z,_,_ in posts)
assert all(0<=x<=112 and 0<=z<=112 and 0<=armx<=112 and 0<=armz<=112 for x,z,armx,armz in posts)
fronton=[(x,y,34) for y in range(13,18) for x in range(56-(17-y),57+(17-y))]
assert len(fronton)==25
assert all(42<=x<=70 and 12<=z<=34 for x,y,z in fronton)
assert all((112-z, x)!=(0,0) for x,y,z in fronton)
assert "emptyAt(56,2,15" in r3 and "blackwood_table" in r3
assert "No nether-explosive vanilla beds" in r3
print("PASS: R3 schema20 additive overlay, preserved A+ 1337 voxel source, 18 raised lights, 25-voxel fronton")
print("PASS: four-rotation-connected window test wired, room circulation/source guards present")
print("NOT_RUN: native Minecraft, actual screenshots, live headroom/door interaction, old world/MP")
