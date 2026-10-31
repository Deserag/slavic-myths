#!/usr/bin/env python3
"""Source-driven Posadnik A+ static QA. This is not a Minecraft GameTest."""
import json
from collections import Counter, deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HERE = ROOT / "docs/concepts/nether-settlement-2.2/posadnik-terem"
base = json.loads((HERE / "posadnik_terem_A_proposal.json").read_text(encoding="utf-8"))
delta = json.loads((HERE / "posadnik_terem_Aplus_delta.json").read_text(encoding="utf-8"))
assert base["block_count"] == delta["baseline_blocks"] == 1259
assert len(base["palette"]) == 15 and len(base["blocks"]) == 1259
assert len(delta["placements"]) == 80
source = {(x,y,z):base["palette"][p] for x,y,z,p in base["blocks"]}
assert len(source) == 1259
final = dict(source)
seen = set()
replaced = 0
for x,y,z,p in delta["placements"]:
    assert (x,y,z) not in seen, ("duplicate",x,y,z)
    seen.add((x,y,z))
    assert 0 <= p < len(delta["palette"])
    assert 60 <= x <= 72 and 0 <= y <= 17 and 78 <= z <= 95
    assert x >= 60 and not (70 <= z <= 74)
    if (x,y,z) in final: replaced += 1
    final[x,y,z] = delta["palette"][p]
assert replaced == 2
assert len(final) == delta["expected_merged_blocks"] == 1337
assert all(final[k] == v for k,v in source.items() if k[1] >= 12), "roof changed"
assert all(not (40 <= x <= 72 and 40 <= z <= 72) for x,y,z in final)
for z in (84,85):
    assert all((61,y,z) not in final for y in (1,2,3))
    assert all("crimson_door" in final[(62,y,z)] for y in (1,2))
for x in (67,68):
    assert all((x,y,89) not in final for y in (1,2))
    assert all((x,y,88) not in final for y in (7,8,9))
for n in range(6):
    assert "crimson_stairs" in final[(64,n+1,81+n)]
    if n < 5: assert all((64,n+1+k,81+n) not in final for k in (1,2)), ("headroom",n)
assert (64,7,87) not in final and (64,8,87) not in final
for (x,y,z),name in final.items():
    if "slavicmyths:" in name:
        assert (x,y-1,z) in final, ("furniture floor missing", x,y,z)
    if "slavicmyths:" in name and "_shelf" in name:
        dx = 1 if "facing=west" in name else -1
        assert final.get((x+dx,y,z), "").startswith(("minecraft:polished_blackstone_bricks","minecraft:polished_basalt")), ("no shelf wall",x,y,z)
def traversable(x,y,z):
    return (x,y,z) not in final and (x,y+1,z) not in final and (x,y-1,z) in final
def flood(start,y):
    found = {start}; todo = deque([start])
    while todo:
        x,z = todo.popleft()
        for a,b in ((x-1,z),(x+1,z),(x,z-1),(x,z+1)):
            if (a,b) not in found and 62 <= a <= 71 and 79 <= b <= 94 and traversable(a,y,b):
                found.add((a,b)); todo.append((a,b))
    return found
assert (67,90) in flood((66,84),1) and (67,82) in flood((66,84),1)
assert (67,91) in flood((67,87),7) and (67,83) in flood((67,87),7)
for turns in range(4):
    def rot(x,z):
        for _ in range(turns):x,z=112-z,x
        return x,z
    coords = [rot(x,z) for x,y,z in final]
    assert len(set((x,y,*rot(x,z)) for x,y,z in final)) == len(final)
    assert all(0 <= x <= 112 and 0 <= z <= 112 for x,z in coords)
print("PASS: A+ 1259 base + 80 placements (2 replacements) -> 1337 blocks")
print("PASS: retained complete original roof; 2-wide doorways; 6-step staircase; shelf support")
print("PASS: rooms reachable on sampled floors, all 4 rotations geometrically valid")
print("PASS: original source immutable, plot bounds; no gameplay QA has been run")
print("NOT_RUN: Minecraft client/GameTest, material survival, NPC navigation, light, JAR")
