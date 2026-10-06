"""Record 0.9.6 gates separately, including legacy defects and manual checks still pending."""
from pathlib import Path
import argparse,collections,hashlib,json,re,shutil,subprocess,sys
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/verification/kurgan-0.9.6';OUT.mkdir(parents=True,exist_ok=True)
parser=argparse.ArgumentParser();parser.add_argument('--headless-boots',type=int,required=True);parser.add_argument('--failed-runs',type=int,required=True);args=parser.parse_args()
def require(ok,message):
 if not ok:raise AssertionError(message)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
logs={'clean-build.log':'kurgan-096-clean-build.log','runtime-core.log':'kurgan-096-runtime-core-final.log','runtime-companions.log':'kurgan-096-runtime-companions-final.log'}
for dst,src in logs.items():
 text=(ROOT/'work'/src).read_text(encoding='utf-8');require('BUILD SUCCESSFUL' in text,'Unsuccessful gate '+src)
 if dst.startswith('runtime'):
  for marker in ['All 10 required tests passed','KURGAN_RUNTIME_PLACEMENT_PASS tiers=3 containers=29','KURGAN_RUNTIME_TRAP_PASS initialAmmo=8 afterShot=7','KURGAN_RUNTIME_REGISTRY_TOTALS items=411 blocks=179','PORT_RUNTIME_DATA_PASS recipes=273 loot=260 advancements=154','PORT_RUNTIME_REGISTRIES_PASS checked=794 entities=47','PORT_RUNTIME_COMPONENTS_PASS','PORT_RUNTIME_EXISTING_CHECKS_PASS','NAVIGATION_RUNTIME_MODEL_AND_WIRE_PASS','NAVIGATION_RUNTIME_SERVER_AUTHORITY_PASS','NAVIGATION_RUNTIME_LIFECYCLE_AND_HOMES_PASS']:require(marker in text,'Absent gate marker '+marker+' in '+src)
  require(not re.search(r'/ERROR\]|/FATAL\]|\[(?:ERROR|FATAL)\]',text),'Runtime errors '+src)
 else:
  for marker in ['KURGAN_V2_HEADLESS_PASS plans=150 archetypes=14','PASS: 1200 seeded plans','900 tier/disturbance rosters','NAVIGATION_HEADLESS_PASS','PASS: immutable rune values','PASS: payload codecs','PASS: real ModelPart']:require(marker in text,'Missing regression '+marker)
 shutil.copy2(ROOT/'work'/src,OUT/dst)
subprocess.run([sys.executable,str(ROOT/'tools/verify_static_data_1211.py'),'--kurgan-rework'],cwd=ROOT,check=True)
subprocess.run([sys.executable,str(ROOT/'tools/verify_kurgan_096.py'),'--resources-only'],cwd=ROOT,check=True)
plans=list((ROOT/'build/reports/slavicmyths/kurgan').glob('*.json'));require(len(plans)==150,'Wrong layout report count');stats={}
for tier in range(3):
 rows=[json.loads(p.read_text(encoding='utf-8')) for p in plans if json.loads(p.read_text(encoding='utf-8'))['tier']==tier];require(len(rows)==50,'Wrong tier coverage')
 require(all(not p['validationErrors'] and 1<=p['attemptsUsed']<=12 for p in rows),'Rejected or unbounded plan')
 metrics={k:{'min':min(p['metrics'][k] for p in rows),'max':max(p['metrics'][k] for p in rows)} for k in rows[0]['metrics']}
 types=collections.Counter(r['archetype'] for p in rows for r in p['rooms'] if r.get('archetype'))
 shares=[]
 for p in rows:
  values=[(r,r['rewardPoints']*{'common_cache':3,'burial':3.5,'warrior':3.5,'ritual':3.5,'treasury':5.5,'secret':3,'great_special':4.5}.get(r['loot'],0)) for r in p['rooms']]
  shares.append(sum(v for r,v in values if not r['critical'])/sum(v for r,v in values))
 require(min(shares)>=[.40,.55,.60][tier],'Side branch reward roll allocation below target')
 stats[str(tier)]={'samples':50,'metrics':metrics,'archetypeOccurrences':dict(types),'maxAttemptsUsed':max(p['attemptsUsed'] for p in rows),'minimumSideBranchExpectedRollShare':min(shares),'rewardMeasurement':'Expected table rolls, not measured economic value or actual gameplay drops'}
report_dir=OUT/'plans';report_dir.mkdir(exist_ok=True)
for p in plans:shutil.copy2(p,report_dir/p.name)
(OUT/'statistics.json').write_text(json.dumps(stats,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
jar=ROOT/'build/libs/slavicmyths-0.9.6.jar';require(jar.is_file(),'No artifact');resources=json.loads((OUT/'resource-check.json').read_text(encoding='utf-8'))
acceptance={'version':'0.9.6','platform':{'minecraft':'1.21.1','neoforge':'21.1.255','java':21},'jar_sha256':sha(jar),'gates':{'cleanBuild':'PASS','layoutAndLegacyRegression':'PASS','registryDataLoaders':'PASS','placementInventoryTrap':'PASS','newAssetsAndReferences':'PASS'},'client_launches':0,'headless_server_boots':args.headless_boots,'headless_server_failed_runs':args.failed_runs,'final_tests_per_server':10,'manual_gameplay_verified':False,'manual_visual_qa_verified':False,'existing_asset_issues':resources['existingBaselineAssetIssues'],'manual_qa':'docs/MANUAL_QA_0.9.6_KURGAN.md','evidence_sha256':{n:sha(OUT/n) for n in [*logs,'statistics.json','static-data-audit.json','resource-check.json']}}
(OUT/'acceptance.json').write_text(json.dumps(acceptance,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
subprocess.run([sys.executable,str(ROOT/'tools/verify_kurgan_096.py')],cwd=ROOT,check=True)
print('PASS: 0.9.6 production gates recorded; visual/gameplay QA remains manual; baseline door references documented')
