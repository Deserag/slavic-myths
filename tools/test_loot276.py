"""Check offline probability model against analytic cases, saved samples and untouched loot."""
from pathlib import Path
import json,hashlib,math
from audit_loot276 import analyze,L,O
doc={'pools':[{'rolls':2,'conditions':[{'condition':'minecraft:random_chance','chance':.5}],'entries':[{'type':'minecraft:item','name':'test:coin','weight':1,'functions':[{'function':'minecraft:set_count','count':{'min':2,'max':4}}]},{'type':'minecraft:empty','weight':3}]}]}
result,_=analyze(doc)
assert math.isclose(result['expected']['test:coin'],.75)
assert math.isclose(result['probability_at_least_one']['test:coin'],.5*(1-.75**2))
n=2
for p in list(O.glob('camp_*.json'))+list(O.glob('kurgan_*.json')):
    d=json.loads(p.read_text());assert d['samples']==20000
    for id,p_any in d['probability_at_least_one'].items():
        # Distributional agreement, not Minecraft RNG parity; loose >4-sigma bound.
        assert abs(d['observed_probability'].get(id,0)-p_any)<.015,(p.stem,id)
        n+=1
hashes=json.loads((O/'loot-hashes.json').read_text())
for rel,digest in hashes.items():
    assert hashlib.sha256((L.parents[2]/rel).read_bytes()).hexdigest()==digest,rel
    n+=1
print('PASS loot276 checks='+str(n)+'; 6x20000 samples; zero Minecraft launches')
