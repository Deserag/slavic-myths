from pathlib import Path
import json,hashlib,zipfile
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/reward-polymc-update'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
base=json.loads((O/'BASELINE_HASHES.json').read_text());checks=0
allowed={f'src/main/java/org/slavicmyths/{p}' for p in ['client/FrostClient.java','morozko/FrostStaff.java','morozko/FrostSkills.java','morozko/FrostNetwork.java','morozko/FrostRules.java','gorynych/GorynychContent.java','gorynych/GorynychEquipment.java','gorynych/SerpentCrossbow.java','gorynych/CrossbowVolley.java']}
allowed.update(['src/main/resources/slavicmyths.equipment.mixins.json',*[f'src/main/resources/assets/slavicmyths/lang/{l}.json' for l in ['ru_ru','en_us']]])
for p,h in base.items():
 if p not in allowed:assert sha(R/p)==h,'Unrelated source/resource changed: '+p;checks+=1
for p in (R/'src/main/resources').rglob('*.json'):json.loads(p.read_text(encoding='utf-8-sig'));checks+=1
for p in (R/'src/main/resources').rglob('*.png'):
 with Image.open(p) as im:im.verify()
 checks+=1
jar=R/'build/libs/slavicmyths-1.9.7.4.jar'
with zipfile.ZipFile(jar) as z:
 assert not any('IssueTests' in n or 'rewardChecks' in n or '/smoke/' in n for n in z.namelist())
 for p in (R/'src/main/resources').rglob('*'):
  if p.is_file() and p.name!='neoforge.mods.toml':assert z.read(p.relative_to(R/'src/main/resources').as_posix())==p.read_bytes(),p;checks+=1
 for p in (R/'build/classes/java/main').rglob('*.class'):assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes(),p;checks+=1
 for n in ['CrossbowDamage','StormCrossbow','FlameVfx','StormVfx','CrossbowVolley']:assert 'org/slavicmyths/gorynych/'+n+'.class' in z.namelist()
 meta=z.read('META-INF/neoforge.mods.toml').decode();assert '1.9.7.4' in meta and '21.1.255' in meta and '[1.21.1]' in meta
assert 'All 12 required tests passed' in (O/'FINAL_BUILD_RUNTIME.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in (O/'FINAL_BUILD_RUNTIME.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in (O/'OFFLINE_FINAL.log').read_text(encoding='utf-8')
result=dict(status='PASS',version='1.9.7.4',jar=str(jar),sha256=sha(jar),checks=checks,game_tests=12,client_starts=0,game_test_server_starts=1)
(O/'RESOURCE_JAR_CHECKS.json').write_text(json.dumps(result,indent=2));print(json.dumps(result))
