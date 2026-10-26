"""Conditional full clears, including existing mob drops/personal quest claims.
These are separate from chest-only L1 targets; no entity tables are modified.
"""
import collections,json,subprocess
from pathlib import Path
from verify_rewards276_all import R,O,J,Model,lm,pool,item,PREFIX,save
def main():
    b=R/'build/rewards276-scenarios';b.mkdir(parents=True,exist_ok=True)
    stub=b/'org/slavicmyths/kurgan/KurganInstance.java';stub.parent.mkdir(parents=True,exist_ok=True)
    stub.write_text('package org.slavicmyths.kurgan;public class KurganInstance{public java.util.Map<Integer,java.util.List<KurganFighter.Kind>> population(){throw new UnsupportedOperationException();}}',encoding='utf8')
    f=b/'FullClears.java';f.write_text('''import java.util.*;import org.slavicmyths.kurgan.*;import org.slavicmyths.loot.*;
public class FullClears{public static void main(String[] args){for(int tier=0;tier<3;tier++){Map<String,Integer> hist=new TreeMap<>();for(long seed=0;seed<20000;seed++){var p=KurganPlan.create(tier,seed);var m=new TreeMap<String,Integer>();for(var r:p.rooms)for(int i=0;i<KurganLayout.rewards(r);i++)m.merge(StructureRewards276.kurgan(tier,r.id==p.finalRoom,i,r.loot),1,Integer::sum);for(var list:KurganRoster.assign(p,true).values())for(var mob:list)m.merge("entities/"+mob.id,1,Integer::sum);String key=m.entrySet().stream().map(e->e.getKey()+"="+e.getValue()).reduce((a,c)->a+","+c).orElse("");hist.merge(key,1,Integer::sum);}for(var e:hist.entrySet())System.out.println(tier+"\\t"+e.getValue()+"\\t"+e.getKey());}}}''',encoding='utf8')
    bin=Path('C:/Program Files/Java/jdk-21.0.12/bin');files=[str(J/p) for p in ['kurgan/KurganPlan.java','kurgan/KurganLayout.java','kurgan/KurganRoster.java','kurgan/KurganFighter.java','loot/StructureRewards276.java']]
    subprocess.run([str(bin/'javac.exe'),'-d',str(b),str(stub),str(f),*files],check=True)
    result=subprocess.run([str(bin/'java.exe'),'-Xmx512m','-cp',str(b),'FullClears'],check=True,capture_output=True,text=True)
    (O/'kurgan-full-roster.tsv').write_text(result.stdout,encoding='utf8');variants=collections.defaultdict(list)
    for row in result.stdout.splitlines():
        tier,n,key=row.split('\t');variants[int(tier)].append((int(n)/20000,{k:int(v) for k,v in (e.split('=') for e in key.split(','))}))
    cases={};main_results=json.loads((O/'simulation-results.json').read_text())
    for tier,v in variants.items():cases['kurgan_full_clear/'+str(tier+1)]=(v,v)
    for site,entity in [('camp/3','nightingale')]:
        v=[(w,{**m,'entities/'+entity:1}) for w,m in main_results[site]['after_manifests']];cases[site+'_with_boss']=(v,v)
    v=[(1,{'chests/fishing_supplies':1,'entities/elder_vodyanoy':1})];cases['deep_pool_with_boss']=(v,v)
    overrides={}
    for family in ['fire','spring']:
        key='virtual/'+family+'_fragment';overrides[key]={'pools':[pool('first_personal_claim',[item('mor_'+('water' if family=='spring' else 'fire')+'_amulet_fragment')])]}
        row=main_results['sanctuary/'+family];cases['sanctuary/'+family+'_first_claim']=tuple([(w,{**m,key:1}) for w,m in row[s+'_manifests']] for s in ['before','after'])
    key='virtual/gorynych_claim';overrides[key]={'pools':[pool('scales',[item('gorynych_scale',(4,4))]),pool('story_seal',[item('serpent_seal')]),pool('one_story_rune',[item('rune_'+id) for id in ['mor_breath','dragon_flame','storm_strike']])]}
    row=main_results['serpent_fortress'];cases['serpent_fortress_first_claim']=tuple([(w,{**m,key:1}) for w,m in row[s+'_manifests']] for s in ['before','after'])
    before=Model(overrides,baseline=True);after=Model(overrides);results={}
    for i,(name,(a,b)) in enumerate(cases.items()):
        old=lm.evaluate(before,a);new=lm.evaluate(after,b);sim=lm.simulate(after,b,276900+i,20000)
        for id,p in new['probability'].items():assert abs(sim['probability'].get(id,0)-p)<.02,(name,id)
        results[name]=dict(before=old,after=new,simulation=sim,before_manifests=a,after_manifests=b,condition='all planned enemies defeated, loot recovered; first eligible claim if applicable; no Looting/Luck; not an unconditional per-structure quest reward')
        save('scenario-simulations.json',results);print('20k full-clear',name,flush=True)
    print('PASS',len(results),'conditional full-clear profiles;',len(results)*20000,'draws;60000 actual KurganPlan+Roster calls without game bootstrap')
if __name__=='__main__':main()
