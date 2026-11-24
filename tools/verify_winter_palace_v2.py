"""Verify the exact owner-approved palette, coordinates and furnishing data."""
import base64
import json
import pathlib
import zlib

root = pathlib.Path(__file__).resolve().parents[1]
res = root / "src/main/resources/data/slavicmyths"
header, payload = (res / "winter/palace_v2.txt").read_text().split("\n", 1)
plan = json.loads(header)
data = zlib.decompress(base64.b64decode(payload))
assert len(data) == 65 * 50 * 63
assert zlib.crc32(data) == 2830284266 == plan["crc"]
assert sum(bool(v) for v in data) == 39380 == plan["count"]
assert max(data) <= len(plan["states"])

def at(x, y, z):
    if not (-32 <= x <= 32 and 0 <= y <= 62 and 0 <= z <= 49):
        return None
    i = data[(y * 50 + z) * 65 + x + 32]
    return plan["states"][i-1] if i else None

assert len(plan["loot"]) == 32
assert len({tuple(row[:3]) for row in plan["loot"]}) == 32
assert sum(row[3] == "boss_reward" for row in plan["loot"]) == 2
for x, y, z, kind in plan["loot"]:
    assert at(x,y,z)[0] == "minecraft:chest"
    assert at(x,y-1,z) and not at(x,y+1,z)
    assert any(not at(x+dx,y,z+dz) and not at(x+dx,y+1,z+dz)
               for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)))
    table = json.loads((res / "loot_table/chests/winter_palace" / (kind+".json")).read_text())
    assert table["pools"]
assert len(plan["entities"]) == 7
for x,y,z,kind,props in plan["entities"]:
    assert kind == "minecraft:armor_stand" and at(x,y-1,z)
    assert not at(x,y,z) and not at(x,y+1,z)
# Queen's spawn and second-phase headroom.
assert at(0,24,37)
assert all(not at(x,y,z) for x in (-1,0,1) for z in (36,37,38) for y in range(25,30))
# Both staircase flights have their treads, headroom, and landings.
for x,z,y in ((8,16,5),(-8,30,15)):
    for step in range(10):
        assert at(x,y+step,z+step)[0] == "minecraft:quartz_stairs"
        assert all(not at(x,y+step+h,z+step) for h in (1,2,3))
    assert at(x,y+9,z+10)
for y in range(63):
    for z in range(50):
        for x in range(-32,33):
            b = at(x,y,z)
            if b and b[0] == "minecraft:blue_bed":
                s=b[1]; dx,dz={"north":(0,-1),"south":(0,1),"east":(1,0),"west":(-1,0)}[s["facing"]]
                sign=1 if s["part"]=="foot" else -1
                other=at(x+sign*dx,y,z+sign*dz)
                assert other and other[0]==b[0] and other[1]["part"]!=s["part"] and other[1]["facing"]==s["facing"]
assert at(0,62,29)
print("Palace V2 verified: 39,380 blocks, 32 chests, 7 stands, queen spawn and stair flights")
