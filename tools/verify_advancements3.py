"""Offline save/graph/callback/resource contracts. Never starts a game or a server."""
import argparse,copy,csv,hashlib,json,re,subprocess,zipfile
from PIL import Image
from pathlib import Path
from advancements3_resources import ROOT,OUT,apply

def read(p):return json.loads(p.read_text('utf8'))
def canonical(v):return json.dumps(v,ensure_ascii=False,sort_keys=True,separators=(',',':'))

def run(jar=None):
    res=ROOT/'src/main/resources';directory=res/'data/slavicmyths/advancement'
    baseline=read(OUT/'BASE_PR40.json');specs=read(OUT/'TREE.json');tree={n['id']:n for n in specs}
    nodes={p.relative_to(directory).as_posix()[:-5]:read(p) for p in directory.rglob('*.json')}
    assert len(tree)==75 and len(specs)==75
    assert set(baseline)<=set(nodes)
    save=lambda n:{k:n.get(k) for k in ('criteria','requirements','rewards')}
    for k,n in baseline.items():
        assert save(n)==save(nodes[k]),k+' changed persisted semantics'
        if k.startswith('recipes/'):
            raw=subprocess.check_output(['git','show','82a0cccb:src/main/resources/data/slavicmyths/advancement/'+k+'.json'],cwd=ROOT)
            assert raw.replace(b'\r\n',b'\n')==(directory/(k+'.json')).read_bytes().replace(b'\r\n',b'\n'),k+' recipe unlock Git bytes'
    assert {k for k,n in nodes.items() if 'display'in n}==set(tree)
    children={k:[] for k in tree};depth={}
    for k,n in tree.items():
        assert nodes[k].get('parent')==('slavicmyths:'+n['parent'] if n['parent'] else None)
        at=k;seen=set()
        while at:
            assert at in tree and at not in seen,(k,'cycle/missing parent',at)
            seen.add(at);at=tree[at]['parent']
        depth[k]=len(seen)-1
        if n['parent']:children[n['parent']].append(k)
    assert children['root']==['first_neighbors','first_structure','meet_bandits','tales_come_alive']
    def branch(k):return 1+sum(branch(c) for c in children[k])
    counts=[branch(k) for k in children['root']];assert counts==[7,24,25,18],counts
    assert not any(k.startswith('group_') for k in tree)
    locales={l:read(res/f'assets/slavicmyths/lang/{l}.json') for l in ('ru_ru','en_us')}
    sources={p.relative_to(ROOT).as_posix():p.read_text('utf8') for p in (ROOT/'src/main/java').rglob('*.java')}
    dispatch={}
    for path,s in sources.items():
        if '/client/'in path or '/compat/'in path:continue
        for call in re.findall(r'\b(?:award|grant|nativeVictory|victory)\(([^;]*?)\);',s):
            for name in re.findall(r'"([a-z_]+)"',call):dispatch.setdefault(name,[]).append(path)
    # The wolf IDs are the existing definition array, selected only after a committed claim.
    for k in ('grey_wolf_bandits','grey_wolf_kurgan','grey_wolf_likho'):
        assert 'if(i<3)org.slavicmyths.progression.AdvancementJourney.award(p,IDS[i])' in sources['src/main/java/org/slavicmyths/military/GreyWolfStory.java']
        dispatch[k]=['src/main/java/org/slavicmyths/military/GreyWolfStory.java','src/main/java/org/slavicmyths/military/WolfStoryDefinition.java']
    dispatch['tales_come_alive']+=['src/main/java/org/slavicmyths/yaga/YagaMenu.java','src/main/java/org/slavicmyths/bayun/BayunNetwork.java','src/main/java/org/slavicmyths/wolf/WolfProgress.java']
    rows=[]
    for k,n in tree.items():
        definition=nodes[k];d=definition['display'];icon=d['icon']['id'];ns,path=icon.split(':')
        assert ns=='slavicmyths' and (res/f'assets/{ns}/models/item/{path}.json').is_file(),icon
        for l,lang in [('ru_ru','ru'),('en_us','en')]:
            for field in ('title','description'):assert locales[l][d[field]['translate']]==n[field+'_'+lang] and n[field+'_'+lang]
        triggers={c['trigger'] for c in definition['criteria'].values()}
        if triggers=={'minecraft:impossible'}:assert dispatch.get(k),k+' missing callback'
        rows.append({'ID':'slavicmyths:'+k,'Parent':definition.get('parent',''),'RU':n['title_ru'],'EN':n['title_en'],
                     'Trigger':canonical(definition['criteria']),'Server source':'; '.join(sorted(set(dispatch.get(k,[])))) or 'Minecraft native criterion dispatcher',
                     'Existing/new':'EXISTING' if k in baseline else 'NEW','Toast':d['show_toast'],'Frame':d['frame'],
                     'Test':'save+graph+source+locale+icon contracts','Status':'OFFLINE_PASS / GAMEPLAY_NOT_RUN'})
    # Award helper never checks parents; book serializes completion independently and renders Done first.
    knowledge=sources['src/main/java/org/slavicmyths/progression/Knowledge.java'].split('public static void award',1)[1].split('public static boolean knows',1)[0]
    assert 'parent' not in knowledge.lower()
    book=sources['src/main/java/org/slavicmyths/tales/TaleService.java']
    assert 'n.putBoolean("Done",progress.isDone())'in book and '!progress.isDone()&&!parentDone'in book
    assert 'n.getBoolean("Done")?' in sources['src/main/java/org/slavicmyths/client/TaleScreen.java']
    # Strong source guards against inventory approximation, quest bypass, new tick loops and reward import.
    journey=sources['src/main/java/org/slavicmyths/progression/AdvancementJourney.java']
    assert not any(x in journey for x in ('getChunk(', 'getStructureWithPieceAt','locate','PlayerTick','getAllLevels'))
    ferm=sources['src/main/java/org/slavicmyths/brewing/Fermentation.java']
    assert 'JourneyStarter'in ferm and 'JourneyAwarded'in ferm and 'Arrays.asList(Brewing.BEERS).contains(beverage)'in ferm
    assert 'self_brewed_beer' not in sources['src/main/java/org/slavicmyths/brewing/BeverageItem.java']
    assert 'trust(p)>=85'in sources['src/main/java/org/slavicmyths/bayun/BayunEntity.java']
    assert 'if(id==2&&(!YagaServices.killed(p,"name_your_misfortune")||!YagaServices.killed(p,"steppe_champion")))'in sources['src/main/java/org/slavicmyths/yaga/YagaMenu.java']
    assert [q['id'] for q in read(res/'data/slavicmyths/wolf_story/quests.json')]==['grey_wolf_bandits','grey_wolf_kurgan','grey_wolf_likho','grey_wolf_volkolak_future']
    assert 'HistoricalAdvancementImport' not in sources['src/main/java/org/slavicmyths/tales/TaleEvents.java']
    # Generator replay must be byte stable for every resource, not only advancements.
    snapshot={p.relative_to(res).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in res.rglob('*') if p.is_file()}
    apply()
    assert snapshot=={p.relative_to(res).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in res.rglob('*') if p.is_file()},'generator replay changed resources'
    for name in ('advancements2_resources.py','tale_advancements.py'):
        subprocess.run(['python',str(ROOT/'tools'/name)],cwd=ROOT,check=True,capture_output=True)
        assert snapshot=={p.relative_to(res).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in res.rglob('*') if p.is_file()},name+' reverted v3'
    json_count=0;png_count=0
    for p in res.rglob('*.json'):read(p);json_count+=1
    for p in res.rglob('*.png'):
        with Image.open(p) as im:im.verify()
        png_count+=1
    jar_result=None
    if jar:
        with zipfile.ZipFile(jar) as z:
            names=z.namelist()
            for name in snapshot:
                if name!='META-INF/neoforge.mods.toml':
                    assert z.read(name)==(res/name).read_bytes(),'JAR stale resource '+name
            for p in (ROOT/'build/classes/java/main').rglob('*.class'):
                assert z.read(p.relative_to(ROOT/'build/classes/java/main').as_posix())==p.read_bytes(),'JAR stale class '+str(p)
            assert 'META-INF/neoforge.mods.toml'in names
            meta=z.read('META-INF/neoforge.mods.toml').decode()
            assert '1.9.7.5'in meta and '21.1.255'in meta and '[1.21.1]'in meta
            assert not any('GameTest' in n or n.startswith(('tools/','org/slavicmyths/verify/')) for n in names)
            for name in ('AdvancementJourney','JourneyZones'):
                assert 'org/slavicmyths/progression/'+name+'.class'in names
        jar_result={'path':str(jar),'sha256':hashlib.sha256(Path(jar).read_bytes()).hexdigest(),'bytes':Path(jar).stat().st_size,'resources':'PASS','test_code':'ABSENT'}
    with (OUT/'ACHIEVEMENTS.csv').open('w',encoding='utf8',newline='')as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
    (OUT/'ACHIEVEMENTS.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    matrix=[]
    for k,n in baseline.items():
        status='technical'if k.startswith('recipes/')else 'visible'if 'display'in n else 'data-only'
        matrix.append({'old_id':'slavicmyths:'+k,'old_status':status,'new_id':'slavicmyths:'+k,'new_parent':nodes[k].get('parent',''),
                       'event':','.join(sorted({c['trigger'] for c in n['criteria'].values()})),'criteria':canonical(n['criteria']),'requirements':canonical(n.get('requirements')),'rewards':canonical(n.get('rewards')),
                       'successor':{'first_brew':'self_brewed_beer','linen_craft':'linen_woven','follow_the_thread':'yaga_thread_earned','garden_harvest':'own_harvest','bad_people':'meet_bandits','meet_bayun':'talk_to_bayun'}.get(k,''),
                       'migration_policy':'UNCHANGED_TECHNICAL'if k.startswith('recipes/')else 'REUSED_ID'if k in tree else 'LEGACY_HOLDER_NO_IMPORT'})
    with (OUT/'COMPATIBILITY.csv').open('w',encoding='utf8',newline='')as f:
        w=csv.DictWriter(f,fieldnames=list(matrix[0]));w.writeheader();w.writerows(matrix)
    result={'status':'OFFLINE_PASS','visible':75,'branches':dict(zip(children['root'],counts)),'baseline_definitions':len(baseline),
            'preserved_recipe_unlocks':sum(k.startswith('recipes/')for k in baseline),'new_ids':sorted(set(nodes)-set(baseline)),
            'restored_display':sorted(k for k in tree if k in baseline and 'display'not in baseline[k]),
            'json_files':json_count,'png_files':png_count,'max_depth':max(depth.values()),'generator_replay':'PASS','production_jar':jar_result,'minecraft_launches':0,'runtime':'NOT_RUN'}
    (OUT/'CHECKS.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    print(json.dumps(result,ensure_ascii=False,indent=2))

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--jar',type=Path);args=p.parse_args();run(args.jar)
