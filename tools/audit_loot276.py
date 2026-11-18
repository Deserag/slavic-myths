"""Audit only: exact zero-Luck chest probabilities + reproducible offline sampling.

No loot/world/resource mutations. Native pure KurganPlan/Layout generate complete
container manifests without Minecraft. Python RNG is not Minecraft's loot RNG;
simulation describes the distribution, not seed-identical game outcomes.
"""
from pathlib import Path
import sys,copy
import collections,json,random,subprocess,csv,math,hashlib
from verify_swamp_073 import read_nbt
R=Path(__file__).resolve().parents[1];RES=R/'src/main/resources';D=RES/'data/slavicmyths';L=D/'loot_table';O=R/'docs/implementation/loot276-20261026';B=R/'build/loot276-offline'
N=20000
def save(name,data):
    (O/name).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def uniform(v):
    if isinstance(v,(int,float)):return [(int(v),1.)]
    assert v.get('type','minecraft:uniform')=='minecraft:uniform',v
    a,b=int(v['min']),int(v['max']);return [(n,1/(b-a+1)) for n in range(a,b+1)]
def chance(conditions):
    p=1.
    for c in conditions:
        assert c['condition']=='minecraft:random_chance',c
        p*=c['chance']
    return p
def analyze(doc,seen=frozenset()):
    means=collections.Counter();absent={};pools=[]
    for pool in doc['pools']:
        assert pool.get('bonus_rolls',0)==0,pool
        pc=chance(pool.get('conditions',[]));rolls=uniform(pool['rolls']);entries=[]
        for e in pool['entries']:
            assert e['type'] in ['minecraft:item','minecraft:empty','minecraft:loot_table'],e
            assert not e.get('conditions'),e
            count=[(1,1.)]
            for f in e.get('functions',[]):
                if f['function']=='minecraft:set_count':
                    assert not f.get('add',False);count=uniform(f['count'])
                else:assert f['function'] in ['minecraft:set_damage','minecraft:enchant_randomly','minecraft:enchant_with_levels'],f
            nested=None;summary=None
            if e['type']=='minecraft:loot_table':
                key=e['value'];assert key.startswith('slavicmyths:') and key not in seen,key
                assert not e.get('functions'),e
                summary,nested=analyze(json.loads((L/(key.split(':')[1]+'.json')).read_text(encoding='utf8')),seen|{key})
            else:summary=dict(expected={e.get('name','EMPTY'):sum(n*p for n,p in count)},probability_at_least_one={e.get('name','EMPTY'):sum(p for n,p in count if n>0)})
            entries.append((e.get('name','NESTED' if nested is not None else 'EMPTY'),e.get('weight',1),count,nested,summary))
        total=sum(e[1] for e in entries);nr=sum(n*p for n,p in rolls)
        for id in set().union(*(e[4]['expected'] for e in entries)):
            p_hit=sum(e[1]/total*e[4]['probability_at_least_one'].get(id,0) for e in entries)
            expected=pc*nr*sum(e[1]/total*e[4]['expected'].get(id,0) for e in entries)
            nohit=(1-pc)+pc*sum(p*(1-p_hit)**n for n,p in rolls)
            means[id]+=expected;absent[id]=absent.get(id,1.)*nohit
        pools.append((pc,rolls,entries))
    return dict(expected=dict(means),probability_at_least_one={k:1-v for k,v in absent.items()}),pools
def draw(pools,rng):
    out=collections.Counter()
    for pc,rolls,entries in pools:
        if rng.random()>=pc:continue
        n=rng.choices([x[0] for x in rolls],[x[1] for x in rolls])[0]
        for _ in range(n):
            id,_,counts,nested,_=rng.choices(entries,[e[1] for e in entries])[0]
            if nested is not None:out.update(draw(nested,rng))
            elif id!='EMPTY':out[id]+=rng.choices([x[0] for x in counts],[x[1] for x in counts])[0]
    return out
def native_plans():
    B.mkdir(parents=True,exist_ok=True)
    source='''package org.slavicmyths.kurgan;
import java.util.*;
public class Loot276Plans {public static void main(String[] args){
 for(int t=0;t<3;t++){Map<String,Integer> hist=new TreeMap<>();for(long seed=0;seed<20000;seed++){
  var p=KurganPlan.create(t,seed);var counts=new TreeMap<String,Integer>();
  for(var room:p.rooms){int n=KurganLayout.rewards(room);if(n>0)counts.merge(room.loot,n,Integer::sum);}
  String manifest=counts.entrySet().stream().map(e->e.getKey()+":"+e.getValue()).reduce((a,b)->a+","+b).orElse("");hist.merge(manifest,1,Integer::sum);
 }for(var e:hist.entrySet())System.out.println(t+"\\t"+e.getValue()+"\\t"+e.getKey());}
}}
'''
    file=B/'Loot276Plans.java';file.write_text(source,encoding='utf8');bin=Path('C:/Program Files/Java/jdk-21.0.12/bin')
    subprocess.run([str(bin/'javac.exe'),'-d',str(B),str(file),str(R/'src/main/java/org/slavicmyths/kurgan/KurganPlan.java'),str(R/'src/main/java/org/slavicmyths/kurgan/KurganLayout.java')],check=True)
    receipt=subprocess.run([str(bin/'java.exe'),'-Xmx512m','-cp',str(B),'org.slavicmyths.kurgan.Loot276Plans'],check=True,text=True,capture_output=True)
    (O/'native-plan-histogram.tsv').write_text(receipt.stdout,encoding='utf8');hist={t:[] for t in range(3)}
    for line in receipt.stdout.splitlines():
        t,n,manifest=line.split('\t');hist[int(t)].append((int(n),{'kurgan/'+k:int(v) for k,v in (s.split(':') for s in manifest.split(','))}))
    for t in hist:assert sum(n for n,m in hist[t])==N
    return hist
def markers(folder):
    out={}
    for f in (D/f'structure/{folder}').glob('*.nbt'):
        nbt=read_nbt(f.read_bytes());out[f.stem]=[b.get('nbt',{}).get('metadata','') for b in nbt['blocks'] if b.get('nbt',{}).get('metadata','').startswith('loot:')]
    return out
def run():
    O.mkdir(parents=True,exist_ok=True)
    before={p.relative_to(RES).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in L.rglob('*.json')}
    if (O/'native-plan-histogram.tsv').exists():
        native={t:[] for t in range(3)}
        for line in (O/'native-plan-histogram.tsv').read_text().splitlines():
            t,n,manifest=line.split('\t');native[int(t)].append((int(n),{'kurgan/'+k:int(v) for k,v in (s.split(':') for s in manifest.split(','))}))
    else:native=native_plans()
    small=markers('bandit');large=markers('stronghold');save('template-loot-markers.json',{'bandit':small,'stronghold':large})
    tables={};compiled={};unsupported={}
    for p in sorted(list((L/'chests').glob('*.json'))+list((L/'kurgan').glob('*.json'))):
        key=p.relative_to(L).as_posix()[:-5]
        try:tables[key],compiled[key]=analyze(json.loads(p.read_text(encoding='utf8')))
        except AssertionError as e:unsupported[key]=str(e)
    save('tables-exact.json',tables);save('unsupported.json',unsupported)
    required=['chests/bandit_small','chests/bandit_medium']+['kurgan/'+p.stem for p in (L/'kurgan').glob('*.json')]+['chests/'+p.stem for p in (L/'chests').glob('stronghold*.json')]+['chests/'+p.stem for p in (L/'chests').glob('kurgan_*.json')]
    assert len([k for k in required if k.startswith('chests/kurgan_')])==27
    assert all(k in compiled for k in required),unsupported
    def camp_manifest(t,rng):
        if t<2:
            pieces=['sleeping','storage_tent','senior','yard_small'] if t==0 else ['leader','sleeping','sleeping','storage_tent','senior','warehouse','watch','canopy','yard_medium']
            if t==0 and rng.random()<.5:pieces.append('cart')
            return {'chests/bandit_'+('small' if t==0 else 'medium'):sum(len(small[p]) for p in pieces)}
        pieces=['gate',rng.choice(['barracks_porch','barracks_side']),rng.choice(['forge_open','forge_closed']),rng.choice(['warehouse_long','warehouse_square']),'kitchen','ataman_house','stable','prison','commander','utility','central_yard','nightingale_yard','tower_roof','tower_open','tower_tall','perimeter_forest']
        if rng.random()<.35:pieces.append('cache_'+str(rng.randrange(2)))
        counts=collections.Counter()
        for p in pieces:
            for m in large[p]:counts['chests/stronghold_'+m.split(':')[1]]+=1
        return dict(counts)
    def observed_site(name,manifest_fn,seed):
        rng=random.Random(seed);total=collections.Counter();hits=collections.Counter();containers=[];manifests=collections.Counter()
        for _ in range(N):
            manifest=manifest_fn(rng);manifests[json.dumps(manifest,sort_keys=True)]+=1;out=collections.Counter()
            for k,n in manifest.items():
                for __ in range(n):out.update(draw(compiled[k],rng))
            total.update(out);hits.update(k for k,v in out.items() if v>0);containers.append(sum(manifest.values()))
        # exact conditional probabilities averaged over sampled manifests, independent of item-roll sampling
        exact_mean=collections.Counter();exact_hit=collections.Counter()
        for encoded,n in manifests.items():
            m=json.loads(encoded);ids=set().union(*(tables[k]['expected'] for k in m));freq=n/N
            for id in ids:
                exact_mean[id]+=freq*sum(tables[k]['expected'].get(id,0)*c for k,c in m.items())
                exact_hit[id]+=freq*(1-math.prod((1-tables[k]['probability_at_least_one'].get(id,0))**c for k,c in m.items()))
        result=dict(samples=N,seed=seed,containers=dict(min=min(containers),mean=sum(containers)/N,max=max(containers)),expected=dict(exact_mean),probability_at_least_one=dict(exact_hit),observed_expected={k:v/N for k,v in total.items()},observed_probability={k:v/N for k,v in hits.items()},manifest_histogram=dict(manifests),scope='all template/plan containers; no entity drops; Luck=0; reachability/clear time NOT_RUN')
        save(name+'.json',result);print(name,result['containers'],flush=True);return result
    sites={}
    for t in range(3):sites['camp_'+str(t+1)]=observed_site('camp_'+str(t+1),lambda rng,t=t:camp_manifest(t,rng),27600+t)
    for t in range(3):sites['kurgan_'+str(t+1)]=observed_site('kurgan_'+str(t+1),lambda rng,t=t:rng.choices([m for n,m in native[t]],[n for n,m in native[t]])[0],27610+t)
    summary=[]
    for name,s in sites.items():
        for id in sorted(set(s['expected'])|{'slavicmyths:ancient_coin','slavicmyths:perunite','slavicmyths:perunite_ore'}):
            summary.append(dict(site=name,id=id,expected=s['expected'].get(id,0),p_at_least_one=s['probability_at_least_one'].get(id,0),observed_p=s['observed_probability'].get(id,0),samples=N))
    with (O/'site-rewards.tsv').open('w',encoding='utf8',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(summary[0]),delimiter='\t');w.writeheader();w.writerows(summary)
    save('loot-hashes.json',before)
    assert before=={p.relative_to(RES).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in L.rglob('*.json')}
    save('receipt.json',dict(samples_per_site=N,sites=6,pure_layout_plans_per_tier=N,chest_tables_analyzed=len(tables),unsupported=unsupported,loot_tables_changed=0,game_launches=0,runtime='NOT_RUN',luck=0,entity_drops='excluded from site totals; separate #241 boss soul pools preserved',random='Python Mersenne Twister; not Minecraft seed parity',native='actual KurganPlan.java and KurganLayout.java; no Minecraft dependency'))
def categories():
    grouped={}
    for p in (L/'chests').glob('*.json'):
        doc=json.loads(p.read_text(encoding='utf8'))
        for pool in doc['pools']:
            for e in pool['entries']:
                if e['type']=='minecraft:item':
                    name=e['name'];key=name.split(':')[-1]
                    e['name']='RUNE' if key.startswith(('rune_','mining_rune','fortune_rune','frost_resistance_rune')) else 'PERUNITE' if key in ['perunite','perunite_ore','perunite_dust'] else name
        grouped['chests/'+p.stem]=analyze(doc)[0]
    for p in (L/'kurgan').glob('*.json'):grouped['kurgan/'+p.stem]=analyze(json.loads(p.read_text(encoding='utf8')))[0]
    result={}
    for p in list(O.glob('camp_*.json'))+list(O.glob('kurgan_*.json')):
        d=json.loads(p.read_text());row={}
        for id in ['RUNE','PERUNITE','slavicmyths:ancient_coin']:
            mean=hit=0
            for manifest,n in d['manifest_histogram'].items():
                m=json.loads(manifest);mean+=n/N*sum(grouped[k]['expected'].get(id,0)*c for k,c in m.items());hit+=n/N*(1-math.prod((1-grouped[k]['probability_at_least_one'].get(id,0))**c for k,c in m.items()))
            row[id]={'expected':mean,'p_any':hit}
        result[p.stem]=row
    save('category-summary.json',result);print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':
    if '--categories-only' not in sys.argv:run()
    categories()
