"""Record separate 0.9.5 gate evidence without rewriting historical 0.9.4 acceptance."""
from pathlib import Path
import argparse,hashlib,json,re,shutil,subprocess,sys,zipfile
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/verification/navigation-0.9.5'
parser=argparse.ArgumentParser();parser.add_argument('--headless-boots',required=True,type=int)
parser.add_argument('--failed-runs',required=True,type=int);args=parser.parse_args()
def require(ok,message):
    if not ok:raise SystemExit(message)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
OUT.mkdir(parents=True,exist_ok=True)
logs={'clean-build.log':'navigation-clean-build.log','runtime-core.log':'navigation-runtime-core-release.log',
      'runtime-companions.log':'navigation-runtime-companions-release.log'}
for destination,source in logs.items():
    text=(ROOT/'work'/source).read_text(encoding='utf-8')
    require('BUILD SUCCESSFUL' in text,'Failed build: '+source)
    if destination.startswith('runtime'):
        require(all(s in text for s in ('All 7 required tests passed','NAVIGATION_RUNTIME_MODEL_AND_WIRE_PASS',
            'NAVIGATION_RUNTIME_SERVER_AUTHORITY_PASS','NAVIGATION_RUNTIME_LIFECYCLE_AND_HOMES_PASS',
            'PORT_RUNTIME_REGISTRIES_PASS checked=794 entities=47','PORT_RUNTIME_DATA_PASS recipes=213 loot=227 advancements=154',
            'PORT_RUNTIME_COMPONENTS_PASS','PORT_RUNTIME_EXISTING_CHECKS_PASS')),'Missing loader evidence: '+source)
        require(not re.search(r'\[(?:ERROR|FATAL)\]|/ERROR\]|/FATAL\]',text),'Loader errors: '+source)
    else:require('NAVIGATION_HEADLESS_PASS' in text,'Navigation logic test absent')
    shutil.copy2(ROOT/'work'/source,OUT/destination)
jar=ROOT/'build/libs/slavicmyths-0.9.5.jar';require(jar.is_file(),'Missing production JAR')
with zipfile.ZipFile(jar) as z:
    require(not any(n.startswith(('xaero/','org/slavicmyths/verify/','mezz/','top/theillusivec4/')) for n in z.namelist()),'Dependency/test classes bundled')
    require('data/slavicmyths/structure/port_empty.nbt' not in z.namelist(),'Test world template bundled')
    require('version="0.9.5"' in z.read('META-INF/neoforge.mods.toml').decode(),'Wrong artifact version')
    for n in z.namelist():
        if n.endswith('.class'):require(int.from_bytes(z.read(n)[6:8],'big')==65,'Non-Java21 class')
for locale in ('en_us','ru_ru'):
    lang=json.loads((ROOT/f'src/main/resources/assets/slavicmyths/lang/{locale}.json').read_text(encoding='utf-8'))
    for category in ('yaga','waystone','kurgan','bandit','boss','quest','special_location','event'):
        require('navigation.slavicmyths.category.'+category in lang,'Missing category '+locale+'/'+category)
subprocess.run([sys.executable,str(ROOT/'tools/verify_static_data_1211.py'),'--navigation'],cwd=ROOT,check=True)
subprocess.run([sys.executable,str(ROOT/'tools/verify_port_1211.py'),'--resources-only','--navigation'],cwd=ROOT,check=True)
stats=dict(samples=1000,radius={'min':180,'mean':391.032,'max':699},
    offset_fraction={'min':0.30140274471224837,'mean':0.6716825690190789,'max':0.949817881779704},
    inner25=0,middle25to50=110,outer50to85=757,edge85to95=133,outside=0,
    scope='Deterministic generator with accepting quality predicate; not real-world biome distribution')
# Match the actual emitted statistics instead of silently accepting a stale report.
line=(OUT/'clean-build.log').read_text(encoding='utf-8')
for text in ('samples=1000','meanRadius=391.032','inner25=0','middle25to50=110','outer50to85=757','edge85to95=133','outside=0'):
    require(text in line,'Statistics changed; update the report from actual measurements')
(OUT/'statistics.json').write_text(json.dumps(stats,indent=2)+'\n',encoding='utf-8')
acceptance=dict(schema_version=1,version='0.9.5',platform={'minecraft':'1.21.1','neoforge':'21.1.255','java':21},
    gates={k:'PASS' for k in 'ABCDEF'},jar_sha256=sha(jar),client_launches=0,
    headless_server_boots=args.headless_boots,headless_server_failed_runs=args.failed_runs,
    final_tests_per_server=7,manual_gameplay_verified=False,
    manual_qa='docs/MANUAL_QA_0.9.5_NAVIGATION.md',
    evidence_sha256={name:sha(OUT/name) for name in [*logs,'statistics.json','static-data-audit.json','resource-check.json']})
(OUT/'acceptance.json').write_text(json.dumps(acceptance,indent=2)+'\n',encoding='utf-8')
subprocess.run([sys.executable,str(ROOT/'tools/verify_port_1211.py'),'--navigation'],cwd=ROOT,check=True)
print('PASS: 0.9.5 clean build, navigation/legacy gates, two final real loader runs, resource parity, Java21 production artifact')
