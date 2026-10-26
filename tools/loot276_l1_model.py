"""Independent finite chest distribution model; no Minecraft imports/bootstrap.
Exact expectations/Pany plus seeded Monte Carlo for *all* structure containers.
"""
from pathlib import Path
import bisect,collections,json,math,random,subprocess
from loot276_l1_resources import R,L,O,BASE
def distribution(v):
    if isinstance(v,(int,float)):return [(int(v),1.)]
    assert v.get('type','minecraft:uniform')=='minecraft:uniform',v
    a,b=int(v['min']),int(v['max']);assert a<=b
    return [(n,1/(b-a+1)) for n in range(a,b+1)]
def probability(conditions):
    result=1.
    for c in conditions:
        assert c['condition']=='minecraft:random_chance',c
        assert 0<=c['chance']<=1;result*=c['chance']
    return result
def groups(id):
    k=id.split(':')[-1];out=[]
    if k.startswith('rune_') and not k.endswith('_fragment') or k.startswith(('mining_rune_','fortune_rune_')):out.append('@whole_rune')
    if k.startswith('rune_') and k.endswith('_fragment'):out.append('@fragment')
    if k in ['perunite','perunite_ore','perunite_dust']:out.append('@perunite')
    if k in ['iron_nugget','iron_ingot','iron_block','raw_iron']:out.append('@iron_resource')
    if k in ['gold_nugget','gold_ingot','gold_block','raw_gold']:out.append('@gold_resource')
    if k=='silver_ingot':out.append('@silver')
    if k=='diamond':out.append('@diamond')
    if k=='enchanted_book':out.append('@book')
    if k.startswith('diamond_') and k in ['diamond_helmet','diamond_chestplate','diamond_leggings','diamond_boots']:out.append('@diamond_armor')
    if k=='netherite_scrap':out.append('@scrap')
    if k.endswith('_armor_trim_smithing_template'):out.append('@trim')
    return out
class Model:
    def __init__(self,overrides=None,baseline=False):self.overrides=overrides or {};self.old=baseline;self.cache={};self.documents={}
    def document(self,key):
        if key not in self.documents:
            if key in self.overrides:self.documents[key]=self.overrides[key]
            elif self.old:self.documents[key]=json.loads(subprocess.check_output(['git','show',BASE+':src/main/resources/data/slavicmyths/loot_table/'+key+'.json'],cwd=R))
            else:self.documents[key]=json.loads((L/(key+'.json')).read_text(encoding='utf8'))
        return self.documents[key]
    def compile(self,key,seen=frozenset()):
        assert key not in seen,('cycle',key)
        if key in self.cache:return self.cache[key]
        d=self.document(key);mean=collections.Counter();none={};pools=[]
        for pool in d['pools']:
            assert pool.get('bonus_rolls',0)==0 and not pool.get('functions'),pool
            chance=probability(pool.get('conditions',[]));rolls=distribution(pool['rolls']);entries=[];cumulative=[];total=0
            for entry in pool['entries']:
                assert not entry.get('conditions'),entry
                count=[(1,1.)];name=entry.get('name','EMPTY');nested=None
                if entry['type']=='minecraft:loot_table':
                    assert not entry.get('functions');nested=self.compile(entry['value'].removeprefix('slavicmyths:'),seen|{key});em,ep=nested[0],nested[1]
                else:
                    assert entry['type'] in ['minecraft:item','minecraft:empty'],entry
                    for f in entry.get('functions',[]):
                        probability(f.get('conditions',[]))
                        if f['function']=='minecraft:set_count':assert not f.get('conditions') and not f.get('add',False);count=distribution(f['count'])
                        else:assert f['function'] in ['minecraft:set_damage','minecraft:set_enchantments','minecraft:enchant_randomly','minecraft:enchant_with_levels'],f
                        if f['function']=='minecraft:set_enchantments' and name=='minecraft:book':assert not f.get('conditions');name='minecraft:enchanted_book'
                    ids=[] if entry['type']=='minecraft:empty' else [name,*groups(name)]
                    em={id:sum(n*p for n,p in count) for id in ids};ep={id:sum(p for n,p in count if n>0) for id in ids}
                weight=entry.get('weight',1);assert weight>0;total+=weight;cumulative.append(total);entries.append((name,weight,count,nested,em,ep))
            nmean=sum(n*p for n,p in rolls)
            for id in set().union(*(e[4] for e in entries)):
                emean=sum(e[1]/total*e[4].get(id,0) for e in entries)
                ehit=sum(e[1]/total*e[5].get(id,0) for e in entries)
                mean[id]+=chance*nmean*emean;none[id]=none.get(id,1)*((1-chance)+chance*sum(p*(1-ehit)**n for n,p in rolls))
            pools.append((chance,rolls,entries,cumulative))
        result=(dict(mean),{k:1-p for k,p in none.items()},pools);self.cache[key]=result;return result
    def sample(self,key,rng):
        def draw(compiled):
            out=collections.Counter()
            for chance,rolls,entries,cumulative in compiled[2]:
                if rng.random()>=chance:continue
                n=rng.randint(rolls[0][0],rolls[-1][0])
                for _ in range(n):
                    name,w,count,nested,em,ep=entries[bisect.bisect_right(cumulative,rng.randrange(cumulative[-1]))]
                    if nested is not None:out.update(draw(nested))
                    elif name!='EMPTY':out[name]+=rng.randint(count[0][0],count[-1][0])
            return out
        return draw(self.compile(key))
    def max_stacks(self,key,seen=frozenset()):
        assert key not in seen;total=0
        for p in self.document(key)['pools']:
            total+=distribution(p['rolls'])[-1][0]*max(self.max_stacks(e['value'].removeprefix('slavicmyths:'),seen|{key}) if e['type']=='minecraft:loot_table' else (0 if e['type']=='minecraft:empty' else 1) for e in p['entries'])
        return total
def manifests():
    # Canonical plans are generated from actual Java by verify_loot276_l1.py.
    file=O/'native-layouts.json';return json.loads(file.read_text(encoding='utf8'))
def camp_variants(tier,after):
    old=R/'docs/implementation/loot276-20261026/template-loot-markers.json';m=json.loads(old.read_text())
    def counts(pieces):
        c=collections.Counter()
        for p in pieces:
            for marker in m['bandit' if tier<2 else 'stronghold'][p]:
                role=marker.split(':')[1]
                if tier<2:key='chests/bandit_'+('small' if tier==0 else 'medium')
                else:key='chests/stronghold_'+role
                if after:
                    if tier<2 and p==('senior' if tier==0 else 'leader'):key=f'rewards276/camp_{tier+1}_main'
                    if tier==2 and p=='ataman_house' and role=='ataman':key='rewards276/camp_3_main'
                    if tier==2 and role=='cache':key='rewards276/camp_3_cache'
                c[key]+=1
        return dict(c)
    if tier==0:
        pieces=['sleeping','storage_tent','senior','yard_small'];return [(.5,counts(pieces)),(.5,counts(pieces+['cart']))]
    if tier==1:return [(1.,counts(['leader','sleeping','sleeping','storage_tent','senior','warehouse','watch','canopy','yard_medium']))]
    pieces=['gate','barracks_porch','forge_open','warehouse_long','kitchen','ataman_house','stable','prison','commander','utility','central_yard','nightingale_yard','tower_roof','tower_open','tower_tall','perimeter_forest']
    return [(.65,counts(pieces)),(.175,counts(pieces+['cache_0'])),(.175,counts(pieces+['cache_1']))]
def kurgan_variants(tier,after):
    return [(x['count']/20000,x['after' if after else 'before']) for x in manifests()[str(tier)]]
def evaluate(model,variants):
    expected=collections.Counter();hit=collections.Counter()
    ids=set().union(*(model.compile(k)[0] for p,m in variants for k in m))
    for frequency,m in variants:
        for id in ids:
            expected[id]+=frequency*sum(model.compile(k)[0].get(id,0)*n for k,n in m.items())
            hit[id]+=frequency*(1-math.prod((1-model.compile(k)[1].get(id,0))**n for k,n in m.items()))
    return dict(expected=dict(expected),probability=dict(hit))
def simulate(model,variants,seed,n=20000):
    rng=random.Random(seed);totals=collections.Counter();hits=collections.Counter();maxima=collections.Counter()
    for _ in range(n):
        manifest=rng.choices([m for p,m in variants],[p for p,m in variants])[0];items=collections.Counter()
        for key,count in manifest.items():
            for __ in range(count):items.update(model.sample(key,rng))
        for id,count in list(items.items()):
            for group in groups(id):items[group]+=count
        totals.update(items);hits.update(id for id,count in items.items() if count>0)
        for id,count in items.items():maxima[id]=max(maxima[id],count)
    return dict(samples=n,seed=seed,mean={k:v/n for k,v in totals.items()},probability={k:v/n for k,v in hits.items()},max_observed=dict(maxima))
