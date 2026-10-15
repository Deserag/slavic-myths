"""Read-only design, current resource and production-JAR gates; no game launches."""
from pathlib import Path
import collections, hashlib, json, re, struct, zipfile, xml.etree.ElementTree as ET

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
RES=ROOT/'src/main/resources'
checks=0
def check(value,message):
    global checks
    checks+=1
    if not value: raise AssertionError(message)
def read(p): return json.loads(p.read_text(encoding='utf-8'))
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()

source=read(HERE/'SOURCE.json'); nodes=source['nodes']
decisions=read(HERE/'DECISIONS.json'); tree=read(HERE/'TREE.json')
old={n['id'].split(':',1)[1]:n for n in nodes if not n['recipe']}
recipes=[n for n in nodes if n['recipe']]
byid={n['id']:n for n in tree}
check(len(old)==129 and len(recipes)==77 and len(nodes)==206,'source inventory')
check(len(decisions)==len({n['id'] for n in decisions})==129,'unique decisions')
check({n['id'] for n in decisions}==set(old),'complete 129 decisions')
check(len(tree)==len(byid)==55,'exact 55-card proposal')
check(sum(n['id'].startswith('group_') for n in tree)==6,'six branches')
check(sum(n['id'] not in old for n in tree)==11,'eleven new milestones')
for n in decisions:
    check(n['decision'] in {'KEEP','MERGE','RETIRE','RELOCATE','REWORK'},'decision enum')
    check(n['successor'] is None or n['successor'] in byid,'successor exists')
    check(n['display']==(n['id'] in byid),'display decision consistent')
    check(n['migration']==('M0' if n['display'] else 'M1'),'migration exhaustive')
    check(n['id'] in (HERE/'AUDIT.md').read_text(encoding='utf-8'),'AUDIT full matrix')
for n in tree:
    check(n['parent'] is None if n['id']=='root' else n['parent'] in byid,'visible parent')
    seen=set(); cur=n
    while cur['parent'] is not None:
        check(cur['id'] not in seen,'no cycle'); seen.add(cur['id']); cur=byid[cur['parent']]
    check(cur['id']=='root','connected to root')
    for key in ('title_ru','title_en','description_ru','description_en'):
        check(bool(n[key].strip()),key)
    check(n['frame'] in {'task','goal','challenge'},'frame enum')
    check(not n['hidden'],'no permanently hidden bosses')
    check(n['show_toast']==(n['id']!='root' and not n['id'].startswith('group_')),'toast policy')
    check(n['announce_to_chat']==(n['frame']=='challenge'),'chat policy')
    ns,item=n['icon'].split(':')
    check(ns in {'minecraft','slavicmyths'},'icon namespace')
    if ns=='slavicmyths': check((RES/f'assets/{ns}/models/item/{item}.json').is_file(),'existing item icon '+item)
    check('| '+n['id']+' |' in (HERE/'TREE_TABLE.md').read_text(encoding='utf-8'),'full display table')
for n in nodes:
    check((ROOT/n['path']).exists(),'source still present')
    check(sha(ROOT/n['path'])==n['sha256'],'source JSON untouched '+n['id'])
    check(read(ROOT/n['path'])==n['resource'],'full criteria/rewards/requirements retained in snapshot')
    for r in n['dispatch']:
        check((ROOT/r['path']).is_file(),'dispatch source exists')
        if r['line'] is not None:
            check('"'+n['id'].split(':')[1]+'"' in (ROOT/r['path']).read_text(encoding='utf-8').splitlines()[r['line']-1],'actual main dispatch line')
for id in ['ancient_find','bad_people','traces_of_past','woodlands']:
    check(len(old[id]['resource']['requirements'])==1,'preserve OR '+id)
for id in ['all_timber','bog_explorer','full_yard']:
    req=old[id]['resource']['requirements']; check(len(req)>1 and all(len(r)==1 for r in req),'preserve AND '+id)
major={'defeat_leshy','without_ataman','defeat_nightingale','defeat_depth_master','earth_heart','name_your_misfortune','steppe_champion','endured_frost','endured_last_night','great_poloz_fall','defeat_gorynych','defeat_idolishche','defeat_marinka','defeat_skim','defeat_kartaus','defeat_kalin','claimed_fire_fragment','claimed_water_fragment','curse_resolved','kurgan_rest','depth_champions','night_watch'}
check(major<=set(byid),'all proposed major coverage present')
for id in major:
    parent=byid[id]['parent']
    while parent:
        check(parent not in major,'no unrelated boss prerequisite chain'); parent=byid[parent]['parent']
tales=list((RES/'data/slavicmyths/tales').rglob('*.json'))
check(len(tales)==234,'234 tales remain')
counts=collections.Counter()
for n in old.values():
    for t in {c['trigger'] for c in n['resource']['criteria'].values()}: counts[t]+=1
check(counts==dict([('minecraft:impossible',73),('minecraft:inventory_changed',30),('minecraft:location',12),('minecraft:tick',10),('minecraft:player_interacted_with_entity',2),('minecraft:player_killed_entity',1),('minecraft:bred_animals',1)]),'current criterion distribution')
ET.parse(HERE/'TREE.svg'); check(True,'valid SVG XML')

properties=dict(l.split('=',1) for l in (ROOT/'gradle.properties').read_text().splitlines() if '=' in l and not l.startswith('#'))
check(properties['minecraft_version']=='1.21.1','MC target')
check('21.1.255' in (ROOT/'gradle.properties').read_text(),'NeoForge pin')
jar=ROOT/'build/libs'/f"slavicmyths-{properties['mod_version']}.jar"
check(jar.is_file(),'production JAR exists')
resource_counts=collections.Counter()
with zipfile.ZipFile(jar) as z:
    names=set(z.namelist())
    check('META-INF/neoforge.mods.toml' in names,'NeoForge metadata')
    check(not any(re.search(r'(^|/)(smoke|gametest|testfixture|advancements_runtime|classes3_runtime)/',n,re.I) for n in names),'test fixture packages absent')
    for p in RES.rglob('*'):
        if not p.is_file(): continue
        rel=p.relative_to(RES).as_posix(); ext=p.suffix.lower()
        if ext in {'.json','.png','.ogg','.nbt'}:
            resource_counts[ext]+=1
            if ext=='.json': read(p)
            if ext=='.png': check(p.read_bytes()[:8]==b'\x89PNG\r\n\x1a\n','PNG signature '+rel)
            check(rel in names,'resource in JAR '+rel)
            check(z.read(rel)==p.read_bytes(),'exact resource JAR parity '+rel)
    classes=[n for n in names if n.endswith('.class') and n.startswith('org/slavicmyths/')]
    for name in classes:
        check(struct.unpack('>H',z.read(name)[6:8])[0]==65,'Java21 bytecode '+name)
        outer=name.split('$')[0].removesuffix('.class')+'.java'
        check((ROOT/'src/main/java'/outer).is_file(),'class has production source, not fixture '+name)
    check(bool(classes),'production mod classes')
    check(sum(n.startswith('data/slavicmyths/advancement/') and n.endswith('.json') for n in names)==206,'all206 packaged advancements')

cache=Path.home()/'.gradle/caches/neoformruntime/intermediate_results'
native=[]
for p in sorted(cache.glob('sourcesAndCompiledWithNeoForge*.jar')):
    with zipfile.ZipFile(p) as z:
        if 'net/minecraft/server/PlayerAdvancements.java' not in z.namelist(): continue
        a=z.read('net/minecraft/advancements/Advancement.java').decode()
        pa=z.read('net/minecraft/server/PlayerAdvancements.java').decode()
        ca=z.read('net/minecraft/client/multiplayer/ClientAdvancements.java').decode()
        check('optionalFieldOf("display")' in a,'native optional display')
        check('if (p_300725_.hasProgress())' in pa,'native persistent progress')
        check('!p_104400_.shouldReset() && advancementprogress.isDone()' in ca,'native initial-reset toast guard')
        native.append(dict(jar=p.name,sha256=sha(p),advancement_source_sha256=hashlib.sha256(a.encode()).hexdigest(),player_advancements_sha256=hashlib.sha256(pa.encode()).hexdigest(),client_advancements_sha256=hashlib.sha256(ca.encode()).hexdigest()))
        break
check(bool(native),'installed native source inspected')
report=dict(status='PASS',scope='Static design, source API and current unchanged production resource/JAR checks; no redesign runtime implementation',checks=checks,base=source['base'],gameplay=129,recipes=77,tales=234,visible_proposal=55,new_proposal=11,decisions=dict(collections.Counter(n['decision'] for n in decisions)),current_resources=dict(resource_counts),jar=jar.name,jar_sha256=sha(jar),java_classes=len(classes),native_source=native,server_launches=0,client_launches=0,gui='NOT_RUN',migration_runtime='NOT_RUN',coop='NOT_RUN',real_relog='NOT_RUN',mspt='NOT_MEASURED')
(HERE/'VERIFICATION.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
