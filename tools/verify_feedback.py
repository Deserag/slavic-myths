"""Feedback gates: real files, deterministic resources, source/JAR parity and test exclusion."""
import hashlib,json,subprocess,sys,zipfile
from pathlib import Path
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources';O=R/'docs/implementation/feedback-20261024'
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def resources():return {p.relative_to(A).as_posix():digest(p) for p in A.rglob('*') if p.is_file()}
before=resources();subprocess.run([sys.executable,str(R/'tools/feedback_resources.py')],check=True);after=resources();assert before==after,'Scoped generator is not idempotent'
subprocess.run([sys.executable,str(R/'tools/audit_feedback_recipes.py')],check=True)
audit=json.loads((O/'recipe-audit.json').read_text());assert not audit['conflicts'] and not audit['process_overlap_candidates'] and not audit['unresolved_tags']
recipe=lambda name:json.loads((A/'data/slavicmyths/recipe'/f'{name}.json').read_text())
assert recipe('runic_anvil')['pattern']==['SSS',' I ','BBB']
assert recipe('armorer_table')['key']['P']=={'tag':'minecraft:planks'}
assert recipe('chekan')['type']=='slavicmyths:armorer_shaped'
plants=[]
for name in ['raspberry','blueberry','blackcurrant','lingonberry','cranberry']:
 d=recipe(name+'_sapling_from_berries');assert d['result']['id']=='slavicmyths:'+name+'_sapling'
 loot=json.loads((A/f'data/slavicmyths/loot_table/blocks/{name}_bush.json').read_text());assert name+'_sapling' in json.dumps(loot)
 plants.append({'plant':name,'planting':'berry_soil','growth':'PerennialBush; native stage checks','renewable':'berry+dirt recipe; shears; ripe right-click harvest','runtime':'NOT_RUN'})
for name in ['rye','barley','oat','turnip','cabbage','pea','flax','hops']:
 loot=json.loads((A/f'data/slavicmyths/loot_table/blocks/{name}_crop.json').read_text());serialized=json.dumps(loot)
 assert (name+'_seeds' in serialized or name+'_cutting' in serialized)
 plants.append({'plant':name,'planting':'farmland','growth':'FarmingCrop/CropBlock or HopsCrop; native stage checks','seed_loot':'present','runtime':'NOT_RUN'})
for name in ['apple','willow','rowan','pine','linden']:
 assert (A/f'data/slavicmyths/loot_table/blocks/{name}_sapling.json').is_file()
 assert (A/f'assets/slavicmyths/blockstates/{name}_sapling.json').is_file()
 plants.append({'plant':name,'growth':'vanilla SaplingBlock / configured tree; source resources present','runtime':'NOT_RUN'})
for lang in ['ru_ru','en_us']:
 d=json.loads((A/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
 for p in (A/'data/slavicmyths/tales').glob('guide_*.json'):
  entry=json.loads(p.read_text());assert entry['title'] in d
  for suffix in ['rumor','discovered','studied','verified']:assert d['tales.entry.'+p.stem+'.'+suffix]
 for name in ['yaga_search','chud_research_yaga','chud_research_bayun']:
  entry=json.loads((A/f'data/slavicmyths/tales/{name}.json').read_text());assert entry['initial']==0
props=dict(v.split('=',1) for v in (R/'gradle.properties').read_text().splitlines() if '=' in v and not v.startswith('#'))
jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar');assert jar.is_file()
with zipfile.ZipFile(jar) as z:
 for name,sha in after.items():
  if name.startswith('META-INF/'):continue
  assert hashlib.sha256(z.read(name)).hexdigest()==sha,name
 assert not any(n.startswith(('FeedbackChecks','tools/','feedback/')) for n in z.namelist())
 for name in ['compat/JeiBrewing','compat/JeiCraftProcesses','compat/JeiSpecialProcesses','chud/ChudResearch','chud/ChudResearchProgress','yaga/YagaDiscovery']:assert 'org/slavicmyths/'+name+'.class' in z.namelist()
report={'jar':str(jar),'sha256':digest(jar),'source_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),'source_state':'HEAD + preserved user changes + uncommitted feedback patch','resources_equal_to_jar':len(after),'generator_replay':'identical all resource bytes','minecraft_processes':0,'polymc':'NOT_INSTALLED_BY_REQUEST','owner_acceptance':'PENDING'}
(O/'artifact.json').write_text(json.dumps(report,indent=2));(O/'plants-audit.json').write_text(json.dumps(plants,indent=2));print(json.dumps(report,indent=2))
