"""Completes the site census and unchanged town/village/direct reward accounting."""
import collections,hashlib,json,math,subprocess,zipfile
from pathlib import Path
from verify_rewards276_all import R,O,BASE,Model,lm,pool,item,save
def main():
    inventory=json.loads((O/'template-inventory.json').read_text(encoding='utf8'));templates={};model=Model()
    for name,v in inventory.items():
        manifests=collections.Counter();direct=collections.Counter()
        for c in v['contents']:
            if c.get('table'):manifests[c['table'].removeprefix('slavicmyths:')]+=1
            for s in c.get('items',[]):direct[s['id']]+=s.get('count',s.get('Count',1))
        if manifests:exact=lm.evaluate(model,[(1,dict(manifests))])
        else:exact={'expected':{},'probability':{}}
        for id,n in direct.items():exact['expected'][id]=exact['expected'].get(id,0)+n;exact['probability'][id]=1
        templates[name]=dict(manifests=dict(manifests),direct_items=dict(direct),totals=exact)
    save('template-totals.json',templates)
    corpus=json.loads((O/'city-corpus.json').read_text());extra={}
    for climate,data in corpus.items():
        overrides={};variants=[]
        for i,(weight,v) in enumerate(data['variants']):
            key='virtual/city_'+climate+'_'+str(i)
            overrides[key]={'pools':[pool('direct_'+str(j),[item(id,(n,n))]) for j,(id,n) in enumerate(v['items'].items())]}
            variants.append((weight,{**v['manifest'],key:1}))
        m=Model(overrides);exact=lm.evaluate(m,variants);sim=lm.simulate(m,variants,276800+len(extra),20000)
        for id,p in exact['probability'].items():assert abs(sim['probability'].get(id,0)-p)<.02,(climate,id)
        extra['gorodishche/'+climate]=dict(before=exact,after=exact,simulation=sim,source='unmodified actual CityPlan; 100 flat-terrain test-double plans/climate; includes direct Items. Not natural placement measurement.')
        print('20k city',climate,flush=True)
    # One Yaga household barrel plus four authored serving dishes.
    yaga={'pools':[pool('display_'+str(i),[item(id)]) for i,id in enumerate(['oat_porridge','rye_bread','apple_pie','minecraft:honey_bottle'])]}
    m=Model({'virtual/yaga_display':yaga});v=[(1,{'chests/baba_yaga_household_supplies':1,'virtual/yaga_display':1})]
    exact=lm.evaluate(m,v);extra['yaga_hut']=dict(before=exact,after=exact,simulation=lm.simulate(m,v,276803,20000))
    city={'chests/chud_workshop':1,'chests/chud_storehouse':2,'chests/chud_residential':4};attached={**city,'chests/abandoned_living':1,'chests/abandoned_mine':1}
    v=[(.65,city),(.35,attached)];m=Model();exact=lm.evaluate(m,v)
    extra['chud_city_eligibility_default']=dict(before=exact,after=exact,simulation=lm.simulate(m,v,276804,20000),limits='35% eligibility before terrain fallback; conditional on all optional content placements succeeding. Not observed natural frequency.')
    save('additional-simulations.json',extra)
    a=zipfile.ZipFile('D:/slavic-myths-baselines/loot276-all-5ffe6476/slavicmyths-2.0.3-HELL.jar');b=zipfile.ZipFile(R/'build/libs/slavicmyths-2.0.4-HELL.jar')
    changed=[]
    allow={'DepthsService','SanctuaryPiece','HillCommands','HillPlan','SerpentHillPiece','SwampPiece','ShrineFeature'}
    for name in a.namelist():
        assert name in b.namelist(),('missing old entry',name)
        if a.read(name)!=b.read(name):
            changed.append(name)
            if name in ['META-INF/neoforge.mods.toml','META-INF/MANIFEST.MF']:
                assert a.read(name).replace(b'2.0.3-HELL',b'2.0.4-HELL')==b.read(name),('unexpected metadata change',name)
            else:assert name.endswith('.class') and name.split('/')[-1].split('$')[0].removesuffix('.class') in allow,('unexpected changed packaged entry',name)
    for name in set(b.namelist())-set(a.namelist()):assert name.startswith('data/slavicmyths/loot_table/rewards276/overworld/') or name=='org/slavicmyths/loot/OverworldRewards276.class',name
    protected=[n for n in a.namelist() if n.endswith('.class') and n not in changed]
    receipt=json.loads((R/'build/current-resources.json').read_text());receipt.update(baseline=BASE,baseline_jar_sha256=hashlib.sha256(Path(a.filename).read_bytes()).hexdigest(),changed_old_entries=changed,unchanged_old_classes=len(protected),additional_profiles=len(extra),additional_draws=len(extra)*20000,build='Java21 gradlew.bat clean build --no-build-cache SUCCESS 1m34s; test NO-SOURCE; autonomous tests separate',runtime='NOT_RUN, 0 launches')
    save('package-receipt.json',receipt);print('PASS inventory/package',len(templates),'NBT manifests;',len(extra)*20000,'extra draws;',len(protected),'unchanged old classes')
if __name__=='__main__':main()
