"""Whole-dungeon Monte Carlo using production tables and exported real KurganPlan/KurganRoster.
No worldgen, chest or save PASS is inferred from this simulation.
"""
from pathlib import Path
import collections, json, random, subprocess
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/kurgan241'
TABLES=ROOT/'src/main/resources/data/slavicmyths/loot_table'
TRACK=['slavicmyths:soul','slavicmyths:soul_fragment','slavicmyths:blank_rune_1','ready_rune_I','minecraft:diamond','minecraft:emerald']

def load(old):
    tables={}
    for p in list((TABLES/'chests').glob('burial_*.json'))+list((TABLES/'chests').glob('kurgan_*.json'))+list((TABLES/'entities').glob('*.json')):
        key=p.relative_to(TABLES).with_suffix('').as_posix()
        data=subprocess.run(['git','show','74a17a297438d8563850c6224cf48eb1b01450dc:'+p.relative_to(ROOT).as_posix()],capture_output=True).stdout if old else p.read_bytes()
        if not data:continue
        tables['slavicmyths:'+key]=json.loads(data)
    return tables

def roll_count(value,rng):
    if isinstance(value,(int,float)):return int(value)
    assert value.get('type','minecraft:uniform')=='minecraft:uniform',value
    return rng.randint(int(value['min']),int(value['max']))

def draw(tables,key,rng):
    result=collections.Counter()
    if key not in tables:return result
    for pool in tables[key]['pools']:
        accepted=True
        for condition in pool.get('conditions',[]):
            assert condition['condition']=='minecraft:random_chance',condition
            accepted &= rng.random()<condition['chance']
        if not accepted:continue
        for _ in range(roll_count(pool['rolls'],rng)):
            entries=pool['entries'];entry=rng.choices(entries,weights=[e.get('weight',1) for e in entries])[0]
            if entry['type']=='minecraft:loot_table':result.update(draw(tables,entry['value'],rng));continue
            assert entry['type']=='minecraft:item',entry
            count=1
            for f in entry.get('functions',[]):
                if f['function']=='minecraft:set_count':count=roll_count(f['count'],rng)
                else:raise ValueError(f)
            result[entry['name']]+=count
            if entry['name'] in ['slavicmyths:rune_strength','slavicmyths:rune_resilience']:result['ready_rune_I']+=count
    return result

def simulate():
    report={}
    for old in [True,False]:
        layouts=json.loads((OUT/('native-layouts-before.json' if old else 'native-layouts.json')).read_text());tables=load(old);rng=random.Random(2412026);version={}
        for tier,label in enumerate(['small','warrior','great']):
            plans=[p for p in layouts if p['tier']==tier];values={k:[] for k in TRACK+['soul_equivalent']}
            for sample in range(20000):
                plan=plans[sample%len(plans)];reward=collections.Counter()
                for role,count in plan['coffins'].items():
                    for _ in range(count):reward.update(draw(tables,f'slavicmyths:chests/kurgan_{label}_{role}',rng))
                for kind,count in plan['mobs'].items():
                    if kind=='UPYR':reward['slavicmyths:soul_fragment']+=sum(rng.random()<(.35 if old else .55) for _ in range(count))
                    elif kind in ['VOEVODA','VOLKHV']:
                        reward['slavicmyths:soul']+=sum(rng.random()<(.5 if old else .7) for _ in range(count))
                    elif kind=='PRINCE':reward['slavicmyths:soul']+=sum(1 if old else rng.randint(1,2) for _ in range(count))
                    # Native nav/drushinnik loot contains no soul pools and is unchanged.
                for k in TRACK:values[k].append(reward[k])
                values['soul_equivalent'].append(reward['slavicmyths:soul']+reward['slavicmyths:soul_fragment']/4)
            version[label]={k:dict(samples=len(v),observed_min=min(v),theoretical_min=(1 if tier==2 and k in ['slavicmyths:soul','soul_equivalent'] else 0),mean=sum(v)/len(v),probability_any=sum(x>0 for x in v)/len(v)) for k,v in values.items()}
        report['before' if old else 'after']=version
    report['scope']='20,000 whole-dungeon trials per tier/version over 64 actual native layouts; observed minima are not mathematical minima; no luck/looting; unchanged nav/drushinnik/relic pools excluded from tracked materials; 4 fragments = 1 soul equivalent, not automatic conversion'
    (OUT/'balance-simulation.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report,indent=2))
if __name__=='__main__':simulate()
