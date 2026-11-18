"""Scoped offline acceptance: actual pure routing/plans, pinned IDs/enchantment
rules, exact total rewards, 20k simulations/site, and production preservation.
No Minecraft/GameTest/server bootstrap, resource generators or UI launches.
"""
from pathlib import Path
import argparse,collections,copy,hashlib,json,math,re,shutil,subprocess,zipfile
from loot276_l1_resources import R,L,O,BASE,ROLES,CRAFT_I,CRAFT_II,FRAGMENTS,tables
from loot276_l1_model import Model,groups,distribution,probability,camp_variants,kurgan_variants,evaluate,simulate
from verify_swamp_073 import read_nbt
B=R/'build/loot276-l1-offline';J=R/'src/main/java/org/slavicmyths'
BIN=Path('C:/Program Files/Java/jdk-21.0.12/bin');checks=[]
def check(ok,name):
    assert ok,name
    checks.append(name)
def save(name,data):
    O.mkdir(parents=True,exist_ok=True);(O/name).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def git(*args):return subprocess.check_output(['git',*args],cwd=R)
def native_plans():
    B.mkdir(parents=True,exist_ok=True);args=[];markers={}
    for group in ['bandit','stronghold']:
        for f in (R/f'src/main/resources/data/slavicmyths/structure/{group}').glob('*.nbt'):
            roles=[b.get('nbt',{}).get('metadata','').split(':')[1] for b in read_nbt(f.read_bytes())['blocks'] if b.get('nbt',{}).get('metadata','').startswith('loot:')]
            markers[group+'/'+f.stem]=roles
            for role in roles:args.append(group+':'+f.stem+':'+role)
    save('markers.json',markers)
    fixture=B/'Loot276Native.java';fixture.write_text('''import java.util.*;
import org.slavicmyths.kurgan.*;import org.slavicmyths.loot.StructureRewards276;
public class Loot276Native {
static String encoded(Map<String,Integer> m){return m.entrySet().stream().map(e->e.getKey()+"="+e.getValue()).reduce((a,b)->a+","+b).orElse("");}
public static void main(String[] args){
for(int tier=0;tier<3;tier++) {Map<String,Integer> hist=new TreeMap<>();for(long seed=0;seed<20000;seed++){
 var plan=KurganPlan.create(tier,seed);if(!plan.validate().isEmpty())throw new AssertionError("invalid actual plan");
 Map<String,Integer> before=new TreeMap<>(),after=new TreeMap<>();int mains=0,total=0;
 for(var room:plan.rooms)for(int index=0;index<KurganLayout.rewards(room);index++){
  before.merge("kurgan/"+room.loot,1,Integer::sum);String key=StructureRewards276.kurgan(tier,room.id==plan.finalRoom,index,room.loot);after.merge(key,1,Integer::sum);total++;
  if(key.endsWith("_main"))mains++;
 }
 if(mains!=1||total!=KurganLayout.metrics(plan).containers())throw new AssertionError("main count/container count "+tier+"/"+seed);
 hist.merge(encoded(before)+"\\t"+encoded(after),1,Integer::sum);
 }for(var e:hist.entrySet())System.out.println("K\\t"+tier+"\\t"+e.getValue()+"\\t"+e.getKey());}
 for(String arg:args){String[] a=arg.split(":");if(a[0].equals("bandit")){for(boolean medium:new boolean[]{false,true})System.out.println("R\\t"+arg+":"+medium+"\\t"+StructureRewards276.camp(medium,a[1]));}
 else System.out.println("R\\t"+arg+"\\t"+StructureRewards276.stronghold(a[1],a[2]));}
 try{StructureRewards276.kurgan(3,true,0,"burial");throw new AssertionError("bad tier accepted");}catch(IllegalArgumentException expected){}
}}
''',encoding='utf8')
    subprocess.run([str(BIN/'javac.exe'),'-d',str(B),str(fixture),str(J/'kurgan/KurganPlan.java'),str(J/'kurgan/KurganLayout.java'),str(J/'loot/StructureRewards276.java')],check=True)
    result=subprocess.run([str(BIN/'java.exe'),'-Xmx512m','-cp',str(B),'Loot276Native',*args],check=True,text=True,capture_output=True)
    (O/'native-plans.tsv').write_text(result.stdout,encoding='utf8');hist={str(t):[] for t in range(3)};routes={}
    for line in result.stdout.splitlines():
        cols=line.split('\t')
        if cols[0]=='R':routes[cols[1]]=cols[2];continue
        _,tier,count,before,after=cols
        def parsed(s):return {k:int(n) for k,n in (x.split('=') for x in s.split(','))}
        hist[tier].append(dict(count=int(count),before=parsed(before),after=parsed(after)))
    for t in hist:check(sum(x['count'] for x in hist[t])==20000,'actual native plans per tier '+t)
    for medium,pieces in [(False,['sleeping','storage_tent','senior','yard_small']),(True,['leader','sleeping','sleeping','storage_tent','senior','warehouse','watch','canopy','yard_medium'])]:
        for cart in [False,True]:
            keys=[routes['bandit:'+p+':'+role+':'+str(medium).lower()] for p in pieces+(['cart'] if cart and not medium else []) for role in markers['bandit/'+p]]
            check(sum(k.endswith('_main') for k in keys)==1,'one actual main small/medium '+str(medium)+'/'+str(cart))
    # Every current large variant must keep exactly one main regardless of skin/cache.
    for barrack in ['barracks_porch','barracks_side']:
     for forge in ['forge_open','forge_closed']:
      for warehouse in ['warehouse_long','warehouse_square']:
       for perimeter in ['perimeter_forest','perimeter_hill','perimeter_plains']:
        for cache in ['', 'cache_0','cache_1']:
            pieces=['gate',barrack,forge,warehouse,'kitchen','ataman_house','stable','prison','commander','utility','central_yard','nightingale_yard','tower_roof','tower_open','tower_tall',perimeter]+([cache] if cache else [])
            keys=[routes['stronghold:'+p+':'+role] for p in pieces for role in markers['stronghold/'+p]]
            check(sum(k.endswith('_main') for k in keys)==1,'one large main '+';'.join([barrack,forge,warehouse,perimeter,cache]))
    save('native-layouts.json',hist);save('native-routes.json',routes)
    save('native-source-hashes.json',{str(p.relative_to(R)):hashlib.sha256(p.read_bytes()).hexdigest() for p in [J/'kurgan/KurganPlan.java',J/'kurgan/KurganLayout.java',J/'loot/StructureRewards276.java']})
    print('PASS native 60000 actual layouts and all camp template variants',flush=True)
def source_guards():
    for file,current,old in [
      ('kurgan/KurganArchitecture.java','org.slavicmyths.loot.StructureRewards276.kurgan(plan.tier,r.id==plan.finalRoom,index,r.loot)','"kurgan/"+r.loot'),
      ('bandit/CampPiece.java','org.slavicmyths.loot.StructureRewards276.camp(total>=7,name)','"chests/bandit_"+(total>=7?"medium":"small")'),
      ('bandit/LargeCampPiece.java','org.slavicmyths.loot.StructureRewards276.stronghold(name,parts[1])','"chests/stronghold_"+parts[1]')]:
        source=(J/file).read_text(encoding='utf8');baseline=git('show',BASE+':src/main/java/org/slavicmyths/'+file).decode().replace('\r\n','\n')
        check(source.count(current)==1 and source.replace(current,old)==baseline,'only loot routing changed; exact original placement/persistence/AI guards '+file)
    check(git('branch','--show-current').decode().strip()=='hell','branch hell')
def validate_tables(override):
    z=zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');names=set(z.namelist())
    def tag(key,seen=frozenset()):
        check(key not in seen,'acyclic vanilla item tag '+key)
        data=json.loads(z.read('data/minecraft/tags/item/'+key+'.json'));out=set()
        for v in data['values']:
            v=v.get('id') if isinstance(v,dict) else v
            if v.startswith('#minecraft:'):out|=tag(v.removeprefix('#minecraft:'),seen|{key})
            else:out.add(v)
        return out
    recipes={}
    for p in (R/'src/main/resources/data/slavicmyths/recipe').rglob('*.json'):
        d=json.loads(p.read_text(encoding='utf8'));result=d.get('result',{});id=result.get('id',result.get('item','')) if isinstance(result,dict) else ''
        if id.startswith('slavicmyths:'):recipes.setdefault(id,[]).append(str(p.relative_to(R)))
    rune_proof={}
    definitions={id:int(tier) for id,tier in re.findall(r'new Definition\("([a-z_]+)",([12]),', (J/'rpg/RuneBalance.java').read_text(encoding='utf8'))}
    for id in CRAFT_I+CRAFT_II:
        check(definitions.get(id.removeprefix('rune_'))==(1 if id in CRAFT_I else 2),'actual RuneBalance tier '+id)
        check('slavicmyths:'+id in recipes,'selected whole rune has existing recipe '+id)
        rune_proof[id]=dict(tier=1 if id in CRAFT_I else 2,recipes=recipes['slavicmyths:'+id],source='rpg/RuneBalance.java + RuneRework.java + RuneRuntime.java')
    for id in FRAGMENTS:
        restored=id.removesuffix('_fragment');p=R/('src/main/resources/data/slavicmyths/recipe/rare_rune_restore_'+restored.removeprefix('rune_')+'.json');d=json.loads(p.read_text())
        check(d['inputs']==[{'ingredient':{'item':'slavicmyths:'+id},'count':3}] and d['result']['id']=='slavicmyths:'+restored,'fragment real restoration 3→1 '+id)
        rune_proof[id]=dict(recipe=str(p.relative_to(R)),role='ingredient, not a whole rune',limits='flight100ticks/CD900, boots only' if restored=='rune_flight' else 'rare_protection400ticks/CD1200, chestplate only; bounded charges',source='rpg/RareRunes.java;RareRuneRuntime.java;RareRuneRules.java')
    enchant_proof=[]
    for key,d in override.items():
        check(d['type']=='minecraft:chest','chest codec type '+key)
        for p in d['pools']:
            probability(p.get('conditions',[]));distribution(p['rolls'])
            for e in p['entries']:
                if e['type']!='minecraft:item':continue
                id=e['name'];ns,k=id.split(':')
                check(('assets/minecraft/models/item/'+k+'.json' in names) if ns=='minecraft' else (R/f'src/main/resources/assets/slavicmyths/models/item/{k}.json').is_file(),'real item model ID '+id)
                if k.startswith('rune_') and not k.endswith('_fragment'):check(k in CRAFT_I+CRAFT_II,'whole rune whitelist I/II '+id)
                check(not any(x in k for x in ['_iii','_iv','gusli','skatert','kalin_battle_seal','kartaus_black_seal']),'no III/IV/blocked artifact '+id)
                if k.endswith('_armor_trim_smithing_template'):check('data/minecraft/recipe/'+k+'.json' in names,'vanilla trim template duplication recipe '+k)
                if k=='netherite_scrap' or k.startswith('diamond_') and k.endswith(('helmet','chestplate','leggings','boots')):check(key=='rewards276/kurgan_3_main','rare high item only III main '+id)
                for f in e.get('functions',[]):
                    probability(f.get('conditions',[]))
                    if f['function']=='minecraft:set_enchantments':
                        for enchant,level in f['enchantments'].items():
                            data=json.loads(z.read('data/minecraft/enchantment/'+enchant.removeprefix('minecraft:')+'.json'))
                            check(1<=distribution(level)[0][0]<=distribution(level)[-1][0]<=data['max_level'],'enchantment level within pinned max '+id+'/'+enchant)
                            supported=data['supported_items'];allowed=tag(supported.removeprefix('#minecraft:')) if isinstance(supported,str) and supported.startswith('#minecraft:') else set(supported if isinstance(supported,list) else [supported])
                            check(id=='minecraft:book' or id in allowed,'item/enchantment compatibility '+id+'/'+enchant)
                            enchant_proof.append(dict(table=key,item=id,enchantment=enchant,levels=level,max=data['max_level'],supported=supported))
                    elif f['function']=='minecraft:set_damage':check(.0<=f['damage']['min']<=f['damage']['max']<=1,'damage remaining-durability range '+id)
                    elif f['function']=='minecraft:set_count':check(distribution(f['count'])[-1][0]<=64,'finite count '+id)
                    else:raise AssertionError('Unexpected new function '+f['function'])
    model=Model(override)
    for key in override:
        model.compile(key)
        if key.startswith('rewards276/kurgan_') and key.endswith('_main'):check(model.max_stacks(key)<=5,'fits actual5slot main coffin '+key)
        if key.startswith('rewards276/camp_') and key.endswith('_main'):check(model.max_stacks(key)<=27,'fits actual27slot barrel '+key)
    save('rune-proof.json',rune_proof);save('enchantment-proof.json',enchant_proof)
def model_boundary_checks():
    # Independent closed-form case: conditional two draws, weight1 of4, count2–4.
    trial={'pools':[{'rolls':2,'conditions':[{'condition':'minecraft:random_chance','chance':.5}],'entries':[{'type':'minecraft:item','name':'minecraft:diamond','weight':1,'functions':[{'function':'minecraft:set_count','count':{'min':2,'max':4}}]},{'type':'minecraft:empty','weight':3}]}]}
    nested={'pools':[{'rolls':1,'entries':[{'type':'minecraft:loot_table','value':'slavicmyths:trial'}]}]}
    model=Model({'trial':trial,'parent':nested});mean,hit,_=model.compile('parent')
    check(math.isclose(mean['minecraft:diamond'],.75),'independent weighted/conditional/nested expected value')
    check(math.isclose(hit['minecraft:diamond'],.21875),'independent weighted/conditional/nested Pany')
    check(mean['minecraft:diamond']==model.compile('trial')[0]['minecraft:diamond'],'single nested reference does not duplicate its result')
    try:Model({'cycle':{'pools':[{'rolls':1,'entries':[{'type':'minecraft:loot_table','value':'slavicmyths:cycle'}]}]}}).compile('cycle')
    except AssertionError:check(True,'cycle rejected')
    else:raise AssertionError('recursive cycle accepted')
def totals(override,do_simulate=False,label='preflight'):
    before=Model(baseline=True);after=Model(override);reports={}
    for family in ['camp','kurgan']:
        for tier in range(3):
            variants=camp_variants if family=='camp' else kurgan_variants
            old=evaluate(before,variants(tier,False));new=evaluate(after,variants(tier,True))
            site=family+'_'+str(tier+1);reports[site]=dict(before=old,after=new)
            if do_simulate:
                sim=simulate(after,variants(tier,True),276300+tier+(10 if family=='kurgan' else 0));reports[site]['simulation']=sim
                for id,p in new['probability'].items():check(abs(sim['probability'].get(id,0)-p)<.015,'20k simulation/exact agreement '+site+'/'+id)
                if family=='kurgan' and tier==2:
                    check(sim['max_observed'].get('@scrap')==1 and sim['max_observed'].get('@diamond_armor')==1,'max one scrap / diamond armor per entire III')
            rune_target=([.05,.20,.35] if family=='camp' else [.08,.25,.45])[tier]
            check(math.isclose(new['probability']['@whole_rune'],rune_target,abs_tol=1e-12),'whole-site rune target '+site)
            check(math.isclose(new['probability'].get('@perunite',0),([0,.08,.25] if family=='camp' else [0,.12,.30])[tier],abs_tol=1e-12),'whole-site perunite target '+site)
            check(math.isclose(old['expected'].get('slavicmyths:ancient_coin',0),new['expected'].get('slavicmyths:ancient_coin',0),abs_tol=1e-12),'expected coin quantity unchanged '+site)
            check(math.isclose(old['probability'].get('slavicmyths:ancient_coin',0),new['probability'].get('slavicmyths:ancient_coin',0),abs_tol=1e-12),'coin probability unchanged '+site)
            if family=='camp' or tier<2:check(new['probability'].get('@scrap',0)==0 and new['probability'].get('@diamond_armor',0)==0,'no netherite/diamond armor outside III '+site)
            else:
                check(math.isclose(new['probability']['@scrap'],.15,abs_tol=1e-12),'III exact15% scrap')
                check(math.isclose(new['probability']['@diamond_armor'],.03,abs_tol=1e-12),'III exact3% enchanted diamond armor')
                check(new['expected'].get('@iron_resource',0)==0,'III no raw iron/ingot/nugget/block')
            print(site,'rune',round(new['probability']['@whole_rune'],5),'perunite',new['probability'].get('@perunite',0),'scrap',new['probability'].get('@scrap',0),flush=True)
    save(label+'-totals.json',reports)
    return reports
def package(override):
    expected={'src/main/java/org/slavicmyths/'+f for f in ['loot/StructureRewards276.java','bandit/CampPiece.java','bandit/LargeCampPiece.java','kurgan/KurganArchitecture.java']}
    expected|={'src/main/resources/data/slavicmyths/loot_table/'+k+'.json' for k in override}
    changed=set(git('diff',BASE,'--name-only','--','src/main').decode().splitlines())|set(git('ls-files','--others','--exclude-standard','--','src/main').decode().splitlines())
    check(changed==expected,'exact production scope; all other structures/registry/saves/bosses/coins/trades/#277/#278/#284/#286/#287 unchanged')
    baseline=Path('D:/slavic-myths-baselines/loot276-l1-fe3d2752/slavicmyths-2.0.2-HELL.jar');jar=R/'build/libs/slavicmyths-2.0.3-HELL.jar'
    with zipfile.ZipFile(jar) as z,zipfile.ZipFile(baseline) as old:
        additions={n for n in z.namelist() if not n.endswith('/')} - {n for n in old.namelist() if not n.endswith('/')};removed=set(old.namelist())-set(z.namelist())
        new_directories={n for n in z.namelist() if n.endswith('/')} - set(old.namelist())
        check(new_directories=={'data/slavicmyths/loot_table/rewards276/'},'only expected new loot ZIP directory entry')
        check(not removed,'no removed production entries')
        check(additions=={'org/slavicmyths/loot/StructureRewards276.class'}|{'data/slavicmyths/loot_table/'+k+'.json' for k in override},'only helper and14table additions in JAR')
        delta=[n for n in old.namelist() if old.read(n)!=z.read(n)]
        check(set(delta)=={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','org/slavicmyths/bandit/CampPiece.class','org/slavicmyths/bandit/LargeCampPiece.class','org/slavicmyths/kurgan/KurganArchitecture.class'},'exact3routing class deltas; old tables/entities/other classes preserved')
        check(not any('Loot276Native' in n or '/verify/' in n or n.startswith('tools/') for n in z.namelist()),'no tests in production')
        for k,d in override.items():check(json.loads(z.read('data/slavicmyths/loot_table/'+k+'.json'))==d,'packaged actual table '+k)
    save('package.json',dict(jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),baseline=BASE,added_entries=sorted(additions),modified_entries=delta,game_launches=0,polymc='NOT_INSTALLED; installed2.0.2 remains untouched'))
def main():
    p=argparse.ArgumentParser();p.add_argument('--preflight',action='store_true');p.add_argument('--plans',action='store_true');args=p.parse_args();O.mkdir(parents=True,exist_ok=True)
    if args.plans:native_plans()
    for rel,digest in json.loads((O/'native-source-hashes.json').read_text()).items():check(hashlib.sha256((R/rel).read_bytes()).hexdigest()==digest,'actual native plan/routing inputs unchanged since60k run '+rel)
    source_guards();override,receipt=tables();validate_tables(override);model_boundary_checks()
    if args.preflight:totals(override,label='preflight')
    else:
        for k,d in override.items():check(json.loads((L/(k+'.json')).read_text())==d,'scoped generator reproduces actual table '+k)
        totals(override,True,label='after');package(override)
    save('preflight-checks.json' if args.preflight else 'checks.json',dict(count=len(checks),checks=checks,runtime='NOT_RUN; game launches0',mode='preflight' if args.preflight else 'full'))
    print('PASS offline checks='+str(len(checks)),flush=True)
if __name__=='__main__':main()
