#!/usr/bin/env python3
"""10,000-seed structural simulation of the current 2.2.3 production loot tables.

Historical loot drafts are validated separately as retained design baselines. This script
executes the production JSON pool/weight/count/random-chance semantics offline; it does
NOT claim Minecraft loot execution or chest save/reload coverage.
"""
import json
import random
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
LOOT=ROOT/"src/main/resources/data/slavicmyths/loot_table/chests"
NAMES=("pantry","utility","personal","elder")
FORBIDDEN={"minecraft:bed","minecraft:netherite_ingot"}

def limits(value):
    if isinstance(value,int):return value,value
    assert value["type"]=="minecraft:uniform"
    lo,hi=value["min"],value["max"]
    assert isinstance(lo,int) and isinstance(hi,int) and 0<=lo<=hi
    return lo,hi

def entry_spec(entry):
    assert entry["type"]=="minecraft:item"
    assert entry["name"] not in FORBIDDEN
    assert entry.get("weight",1)>0
    functions=entry.get("functions",[])
    assert len(functions)==1 and functions[0]["function"]=="minecraft:set_count"
    lo,hi=limits(functions[0]["count"])
    return entry["name"],entry.get("weight",1),lo,hi

def load(name):
    data=json.loads((LOOT/f"nether_communal_{name}.json").read_text())
    assert data["type"]=="minecraft:chest" and data["pools"]
    pools=[]
    for pool in data["pools"]:
        roll_lo,roll_hi=limits(pool["rolls"])
        entries=[entry_spec(entry) for entry in pool["entries"]]
        assert entries
        conditions=pool.get("conditions",[])
        chance=1.0
        if conditions:
            assert len(conditions)==1 and conditions[0]["condition"]=="minecraft:random_chance"
            chance=conditions[0]["chance"]
            assert 0.0<=chance<=1.0
        pools.append((roll_lo,roll_hi,chance,entries))
    return pools

tables={name:load(name) for name in NAMES}
coin_pools=[pool for pool in tables["elder"] if any(e[0]=="slavicmyths:ancient_coin" for e in pool[3])]
assert len(coin_pools)==1 and coin_pools[0][0:3]==(1,1,0.2)
assert coin_pools[0][3]==[("slavicmyths:ancient_coin",1,1,2)]
assert all(not any(e[0]=="slavicmyths:ancient_coin" for e in pool[3]) for name in ("pantry","utility","personal") for pool in tables[name])

total=Counter();bonus_houses=0
for seed in range(10_000):
    rng=random.Random(seed);house=Counter()
    for pools in tables.values():
        for min_rolls,max_rolls,chance,entries in pools:
            if chance<1.0 and rng.random()>=chance:continue
            weight=sum(e[1] for e in entries)
            for _ in range(rng.randint(min_rolls,max_rolls)):
                pick=rng.randrange(weight)
                for item,w,lo,hi in entries:
                    if pick<w:
                        house[item]+=rng.randint(lo,hi);break
                    pick-=w
                else:raise AssertionError("Invalid weighted selection")
    coins=house["slavicmyths:ancient_coin"]
    assert 0<=coins<=2,("Unexpected elder chest coins",seed)
    bonus_houses+=coins>0;total.update(house)
assert 1800<=bonus_houses<=2200,bonus_houses
assert 2600<=total["slavicmyths:ancient_coin"]<=3400,total["slavicmyths:ancient_coin"]
print(f"PASS offline 10000 seeded houses: production pools={sum(map(len,tables.values()))}, coin chest probability={bonus_houses/10000:.2%}, expected coin quantity={total['slavicmyths:ancient_coin']/10000:.4f}/house")
print("PASS current production JSON: rolls/weights/counts validated; elder coin remains 20%; forbidden bed/netherite absent")
