"""Offline finite reward model; never starts Minecraft. Baseline is accepted L1."""
import collections, hashlib, json, math, re, subprocess, zipfile
from pathlib import Path
import loot276_l1_model as lm
from rewards276_all_resources import R,L,O,BASE,PREFIX,all_tables,swamp_selectors,III,IV,CRAFT_I,CRAFT_II
from loot276_l1_resources import item,pool
J=R/'src/main/java/org/slavicmyths'; B=R/'build/rewards276-offline'
checks=[]
def check(ok,label):
    assert ok,label
    checks.append(label)
def save(name,data): (O/name).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
oldgroups=lm.groups
def groups(id):
    k=id.split(':')[-1]; out=oldgroups(id)
    if k in CRAFT_II:out.append('@rune_II')
    if k.startswith('rune_'):
        for suffix in ['ii','iii','iv']:
            if k.endswith('_'+suffix):out.append('@rune_'+suffix.upper())
    if k.startswith('poloz_') and k.endswith(('helmet','chestplate','leggings','boots')):out.append('@poloz')
    if k=='ancient_rune_fragment' and '@fragment' not in out:out.append('@fragment')
    return out
lm.groups=groups
class Model(lm.Model):
    def document(self,key):
        if self.old and key not in self.overrides and key not in self.documents:
            self.documents[key]=json.loads(subprocess.check_output(['git','show',BASE+':src/main/resources/data/slavicmyths/loot_table/'+key+'.json'],cwd=R))
        return super().document(key)
def native():
    B.mkdir(parents=True,exist_ok=True)
    selectors=swamp_selectors()
    cases='\n'.join('if(!OverworldRewards276.swamp("%s","%s",%s).equals("%sswamp_%s_main"))throw new AssertionError("%s");'%(k,v['base'].split('/')[-1],','.join(map(str,v['pos'])),PREFIX,v['kind'],k) for k,v in selectors.items())
    code='''import org.slavicmyths.serpenthill.*;import org.slavicmyths.loot.*;
public class Rewards276Native {public static void main(String[] args){
for(long seed:new long[]{0,1,-1,276,Long.MAX_VALUE}){var p=new HillPlan(seed);int main=0,chests=0,barrels=0;
for(var c:p.rewardContainers()){String state=p.state(c.x(),c.y(),c.z());boolean barrel=state.equals("minecraft:barrel");
if(!barrel&&!state.startsWith("minecraft:chest"))throw new AssertionError("not a container "+state);
if(barrel)barrels++;else chests++;String route=OverworldRewards276.fortress(c.x(),c.y(),c.z(),barrel);if(route.endsWith("_main"))main++;
if(seed==0)System.out.println(route);}
if(main!=1||chests!=6||barrels!=5)throw new AssertionError("whole fortress routing");}
CASES
if(!OverworldRewards276.swamp("v097_boardwalk_0","swamp_supplies",1,2,3).equals("chests/swamp_supplies"))throw new AssertionError("fallback");
if(!OverworldRewards276.sanctuary(true,"cache").endsWith("spring_main"))throw new AssertionError("cache");
}}'''.replace('CASES',cases)
    f=B/'Rewards276Native.java';f.write_text(code,encoding='utf8');bin=Path('C:/Program Files/Java/jdk-21.0.12/bin')
    subprocess.run([str(bin/'javac.exe'),'-d',str(B),str(f),str(J/'loot/OverworldRewards276.java'),str(J/'serpenthill/HillPlan.java'),str(J/'serpenthill/HillRules.java')],check=True)
    run=subprocess.run([str(bin/'java.exe'),'-Xmx512m','-cp',str(B),'Rewards276Native'],check=True,capture_output=True,text=True)
    (O/'native-routing.txt').write_text(run.stdout,encoding='utf8')
    check(len(run.stdout.splitlines())==11,'actual HillPlan 5 seeds: six chests, five authored barrels, one main; all 21 swamp selectors')
    return dict(collections.Counter(run.stdout.splitlines()))
def manifests(fortress):
    inventory=json.loads((O/'template-inventory.json').read_text(encoding='utf8'));sel=swamp_selectors();out={}
    for full,v in inventory.items():
        if full.startswith('swamp/'):
            name=full.split('/',1)[1]
            if not name.startswith(('v097_','ruin_')):continue
            before=collections.Counter();after=collections.Counter();mains=0
            for x in v['contents']:
                marker=x.get('marker') or ''
                if not marker.startswith('loot:'):continue
                key='chests/'+marker.split(':')[1];before[key]+=1
                if name in sel and x['pos']==sel[name]['pos']:
                    key=PREFIX+'swamp_'+sel[name]['kind']+'_main';mains+=1
                after[key]+=1
            check(mains==(1 if name in sel else 0),'bounded swamp main '+name)
            out['swamp/'+name]=([(1,dict(before))],[(1,dict(after))])
    # Exact NBT marker counts, including locked cache and guaranteed fire box.
    for family in ['fire','spring']:
        matches=[v for k,v in inventory.items() if k.startswith('sanctuaries/'+family+'/')]
        check(bool(matches),'actual multipart sanctuary templates '+family)
        before=collections.Counter();after=collections.Counter()
        for x in [x for v in matches for x in v['contents']]:
            marker=x.get('marker') or ''
            if marker.startswith(('loot:','cache:')):
                role='cache' if marker.startswith('cache:') else marker.split(':')[1]
                before['chests/'+family+'_'+role]+=1
                after[PREFIX+family+'_'+('main' if role=='cache' else role)]+=1
        out['sanctuary/'+family]=([(1,dict(before))],[(1,dict(after))])
    out['serpent_fortress']=([(1,{'chests/serpent_fortress':6})],[(1,fortress)])
    for family,variants in [('camp',lm.camp_variants),('kurgan',lm.kurgan_variants)]:
        for tier in range(3):
            current=variants(tier,True);out[family+'/'+str(tier+1)]=(current,current)
    for key in ['ancient_shrine']:
        out[key]=([(1,{'chests/'+key:1})],[(1,{PREFIX+key:1})])
    # Conditional planned maxima: Chud placement may skip unsafe sites in game.
    for variant in range(3):
        m={'chests/chud_workshop':1,'chests/chud_storehouse':2,'chests/chud_residential':4,
           'chests/abandoned_living':1,'chests/abandoned_mine':1}
        out['chud_city_with_attached/'+str(variant)]=([(1,m)],[(1,m)])
    for kind,mines in [('medium',2),('great',3)]:
        m={'chests/abandoned_living':3,'chests/abandoned_mine':mines}
        rich={**m,'chests/abandoned_treasury':1}
        # Medium has 9 of 12 rotating secondary modules; storage exists 9/12.
        variants=[(.25,m),(.75,rich)] if kind=='medium' else [(1,rich)]
        out['chud_abandoned/'+kind]=(variants,variants)
    # Audit every other chest table (town/village specialised buildings included)
    # individually; these are conditional per-container, not fictitious city totals.
    for p in (L/'chests').rglob('*.json'):
        key=p.relative_to(L).with_suffix('').as_posix()
        if any(key in m for a,b in out.values() for _,m in a):continue
        out['unchanged_container/'+key]=([(1,{key:1})],[(1,{key:1})])
    return out
def validate(tables):
    z=zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');names=set(z.namelist());proof=[]
    recipes={}
    for p in (R/'src/main/resources/data/slavicmyths/recipe').rglob('*.json'):
        d=json.loads(p.read_text(encoding='utf8'));r=d.get('result',{});id=r.get('id',r.get('item','')) if isinstance(r,dict) else ''
        recipes.setdefault(id,[]).append(str(p.relative_to(R)))
    for key,d in tables.items():
        check(json.loads((L/(key+'.json')).read_text())==d,'reproducible scoped table '+key)
        for p in d['pools']:
            for e in p['entries']:
                if e['type']!='minecraft:item':continue
                id=e['name'];ns,k=id.split(':')
                check(('assets/minecraft/models/item/'+k+'.json' in names) if ns=='minecraft' else (R/f'src/main/resources/assets/slavicmyths/models/item/{k}.json').is_file(),'actual item ID '+id)
                check(k not in ['gusli','skatert','kartaus_black_seal','kalin_battle_seal'],'blocked artifact excluded '+id)
                if k.startswith('rune_') and not k.endswith('_fragment'):
                    check(k in CRAFT_I+CRAFT_II+III+IV,'rune whitelist '+k)
                    check(bool(recipes.get(id)),'existing craftable rune '+id)
                    if k in III+IV:check(key==PREFIX+'fortress_main','III/IV confined to single fortress main')
                for f in e.get('functions',[]):
                    if f['function']=='minecraft:set_enchantments':
                        check(id=='minecraft:book','new enchantment only on book')
                        for ench,level in f['enchantments'].items():
                            rule=json.loads(z.read('data/minecraft/enchantment/'+ench.split(':')[-1]+'.json'))
                            check(1<=lm.distribution(level)[0][0]<=lm.distribution(level)[-1][0]<=rule['max_level'],'pinned enchantment level '+ench)
                            proof.append(dict(table=key,enchantment=ench,level=level,max=rule['max_level']))
    save('enchantment-proof.json',proof)
    # Existing resources (including all old loot, L1, registry and art) unchanged.
    changed=subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main/resources'],cwd=R,text=True).splitlines()
    check(all(p.startswith('src/main/resources/data/slavicmyths/loot_table/'+PREFIX) for p in changed),'all existing resources byte preserved from L1 baseline')
    m=Model()
    for key in tables:check(m.max_stacks(key)<=27,'worst-case stacks fit 27-slot container '+key)
    for source in ['loot/StructureRewards276.java','gorynych/GorynychRewards.java']:
        path=J/source
        if path.exists():check(subprocess.check_output(['git','show',BASE+':'+path.relative_to(R).as_posix()],cwd=R).decode().replace('\r\n','\n')==path.read_text(encoding='utf8'),'protected production source unchanged '+source)
def main():
    fortress=native();tables=all_tables();validate(tables);sites=manifests(fortress)
    # Dynamic depths old/main distributions and unchanged starter, plus two guaranteed guardian coins.
    new=tables[PREFIX+'depths_main'];old={'type':'minecraft:chest','pools':new['pools'][:4]}
    starter={'pools':[pool('iron',[item('minecraft:iron_ingot',(1,3))]),pool('torch',[item('minecraft:torch',(2,5))]),pool('bread',[item('minecraft:bread',(1,2))]),pool('gold',[item('minecraft:gold_nugget',(1,3))]),pool('coin',[item('ancient_coin',(1,2))],.3),pool('guardian_coins',[item('ancient_coin',(2,2))])]}
    before=Model({'virtual/depths_main':old,'virtual/depths_starter':starter},baseline=True);after=Model({'virtual/depths_starter':starter})
    sites['ancient_depths']=([(1,{'virtual/depths_starter':1,'virtual/depths_main':1})],[(1,{'virtual/depths_starter':1,PREFIX+'depths_main':1})])
    results={};summary=['site\tP_rune_before\tP_rune_after\tE_perunite_before\tE_perunite_after\tE_diamond_before\tE_diamond_after\tE_coin_before\tE_coin_after']
    for i,(site,(a,b)) in enumerate(sites.items()):
        old=lm.evaluate(before,a);new=lm.evaluate(after,b);sim=lm.simulate(after,b,276400+i,20000)
        for id,p in new['probability'].items():check(abs(sim['probability'].get(id,0)-p)<.02,'Monte Carlo/exact whole profile '+site+'/'+id)
        for field in ['expected','probability']:check(math.isclose(old[field].get('slavicmyths:ancient_coin',0),new[field].get('slavicmyths:ancient_coin',0),abs_tol=1e-12),'coins preserved '+site+'/'+field)
        results[site]=dict(before=old,after=new,simulation=sim,before_manifests=a,after_manifests=b)
        save('simulation-results.json',results)
        summary.append('\t'.join(map(str,[site,old['probability'].get('@whole_rune',0),new['probability'].get('@whole_rune',0),old['expected'].get('@perunite',0),new['expected'].get('@perunite',0),old['expected'].get('@diamond',0),new['expected'].get('@diamond',0),old['expected'].get('slavicmyths:ancient_coin',0),new['expected'].get('slavicmyths:ancient_coin',0)])))
        print('20k '+site,flush=True)
    fort=results['serpent_fortress'];check(math.isclose(fort['after']['expected']['@poloz'],1.08),'Poloz expected 1.08')
    for id,p in [('@poloz',1),('@rune_IV',.03),('@rune_III',.18),('@rune_II',1)]:check(math.isclose(fort['after']['probability'][id],p,abs_tol=1e-12),'whole fortress target '+id)
    check(fort['simulation']['max_observed']['@poloz']==2,'at most two Poloz pieces observed')
    for id in ['slavicmyths:serpent_crossbow']:
        for field in ['expected','probability']:check(math.isclose(fort['before'][field].get(id,0),fort['after'][field].get(id,0),abs_tol=1e-12),'legacy fortress unique pool preserved '+id+'/'+field)
    save('simulation-results.json',results);save('checks.json',dict(checks=checks,count=len(checks),profiles=len(results),samples_per_profile=20000,total_samples=len(results)*20000,game_launches=0,baseline=BASE))
    (O/'before-after.tsv').write_text('\n'.join(summary)+'\n',encoding='utf8')
    print('PASS',len(checks),'checks;',len(results),'profiles;',len(results)*20000,'draws')
if __name__=='__main__':main()
