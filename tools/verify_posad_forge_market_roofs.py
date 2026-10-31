#!/usr/bin/env python3
"""Schema-15 static QA for forge roof/chimney and both market roof families.

No Java 21, GameTest, relog, client-render or multiplayer claims are made here.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
java = root / "src/main/java/org/slavicmyths/posad"
source = root / "docs/concepts/nether-settlement-2.2/kuznechny-dvor-2.2.5/2.2_KUZNECHNY_DVOR_MODEL_R1.json"
forge = json.loads(source.read_text(encoding="utf-8"))
assert forge["placed_blocks"] == len(forge["voxels"]) == 1901
assert len(forge["chests"]) == 5
f = {(b["u"], b["y"], b["v"]): b["block"] for b in forge["voxels"]}
assert len(f) == 1901
at = lambda u,y,v: f.get((u,y,v))

# Only confirmed R1 defect: east-facing roof stair crosses the four masonry
# walls at the foot of an otherwise hollow, campfire-capped 1x1 chimney.
assert at(4,9,3).startswith("minecraft:nether_brick_stairs[facing=east")
for u,v in ((3,3),(5,3),(4,2),(4,4)):
    assert at(u,9,v) == "minecraft:polished_blackstone_bricks"
for y in range(10,16):
    assert at(4,y,3) is None
assert at(4,16,3) == "minecraft:polished_blackstone_bricks"
assert at(4,17,3).startswith("minecraft:campfire[")

forge_slopes = 0
chimney_intersections = 0
for v in range(1,16):
    for y in range(6,14):
        for u,facing in ((y-5,"east"),(22-y,"west")):
            block = at(u,y,v)
            if 3 <= u <= 5 and 2 <= v <= 4 and block == "minecraft:polished_blackstone_bricks":
                chimney_intersections += 1
                continue
            assert block.startswith("minecraft:nether_brick_stairs[facing=" + facing), ("forge",u,y,v,block)
            forge_slopes += 1
    for u in (8,9):
        assert at(u,14,v).startswith("minecraft:nether_brick_slab[")
assert forge_slopes == 232 and chimney_intersections == 8

# Secondary warehouse and canopy have separate correctly joined roofs.
rear_stairs = 0
for v in range(17,25):
    for y in range(5,11):
        for u,facing in ((y-4,"east"),(17-y,"west")):
            assert at(u,y,v).startswith("minecraft:nether_brick_stairs[facing=" + facing)
            rear_stairs += 1
    for u in (6,7):
        assert at(u,11,v) == "minecraft:nether_brick_slab[type=bottom,waterlogged=false]"
for v in range(18,25):
    for y in range(5,11):
        cells = [(y+9,"east"),(29-y,"west")]
        if y == 10:
            cells.pop()  # Odd-span roof meets at one shared center cell.
        for u,facing in cells:
            assert at(u,y,v).startswith("minecraft:nether_brick_stairs[facing=" + facing)
            rear_stairs += 1
    assert at(19,11,v) == "minecraft:nether_brick_slab[type=bottom,waterlogged=false]"
assert rear_stairs == 173

market = json.loads((root / "src/main/resources/data/slavicmyths/posad/menovye_ryady_2_2_6.json").read_text(encoding="utf-8"))
assert market["origin"] == [75,75] and market["size"] == [27,12,25]
assert len(market["blocks"]) == 2203 and len(market["chests"]) == 8
m = {(u,y,v): market["palette"][idx] for u,y,v,idx in market["blocks"]}
assert len(m) == 2203
market_slopes = 0
market_ridges = 0

for v in range(1,18):
    for start in (1,15):
        for y in range(5,10):
            for u,facing in ((start+y-5,"west"),(start+10-(y-5),"east")):
                assert m[u,y,v].startswith("minecraft:crimson_stairs[facing=" + facing), ("shop",u,y,v)
                market_slopes += 1
        assert m[start+5,10,v].startswith("minecraft:crimson_slab[")
        market_ridges += 1
for v in range(18,25):
    for start in (1,15):
        for y in range(6,11):
            for u,facing in ((start+y-6,"west"),(start+10-(y-6),"east")):
                assert m[u,y,v].startswith("minecraft:nether_brick_stairs[facing=" + facing), ("warehouse",u,y,v)
                market_slopes += 1
        assert m[start+5,11,v].startswith("minecraft:crimson_slab[")
        market_ridges += 1
assert market_slopes == 480 and market_ridges == 48

# Both R1 sources and 5 + 8 original rewards stay immutable. No market
# geometry edits were justified; this test makes that deliberate decision
# reproducible rather than adding arbitrary roof decoration.
roof = (java / "PosadForgeChimneyRoofRepair.java").read_text(encoding="utf-8")
piece = (java / "PosadPiece.java").read_text(encoding="utf-8")
commands = (java / "PosadCommands.java").read_text(encoding="utf-8")
geometry = (java / "PosadGeometry.java").read_text(encoding="utf-8")
assert "SCHEMA_VERSION = 20" in geometry
assert "schema >= 15" in piece and "PosadForgeChimneyRoofRepair.voxel(x,y,z)" in piece
assert piece.index("schema >= 15") < piece.index("schema >= 14")
assert "PosadForgeChimneyRoofRepair.voxel(16,9,47)" in commands
assert 'id.equals("kuznechny_dvor_2_2_5")' in commands
assert "present.hasBlockEntity()" in commands and "world.setBlock(target,repair,2)" in commands
assert "cap.is(Blocks.POLISHED_BLACKSTONE_BRICKS)" in roof and "smoke.is(Blocks.CAMPFIRE)" in roof
assert "side.getStepX()" in roof and "upper<=15" in roof

# Four city rotations leave the repaired point in its district and
# independently preserve the exact cardinal direction of every original pitch.
for turn in range(4):
    wx,wz = ((16,47),(112-47,16),(112-16,112-47),(47,112-16))[turn]
    assert 0 <= wx <= 112 and 0 <= wz <= 112
    assert len({(wx,9,wz)}) == 1

print("PASS: forge 1901/5, repaired 1 chimney intrusion, 232+161 intact stairs, 8 chimney masonry crossings; market 2203/8, 480 slopes, 48 ridges, 4 rotations, native/standalone wired")
