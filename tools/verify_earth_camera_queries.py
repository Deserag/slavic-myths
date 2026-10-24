"""Source calculation parity + current artifact isolation; no game launch."""
from pathlib import Path
import re,json,subprocess,zipfile,hashlib
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/performance-20261024'
before=(O/'before/EarthCamera.java').read_text();after=(R/'src/main/java/org/slavicmyths/client/EarthCamera.java').read_text()
def norm(s):return re.sub(r"\s+","",s)
checks={}
for var in ['actor','boss']:
 if var=='actor':
  b=before.split('for(var actor:')[1].split('){',1)[1].split('}for(var boss:')[0]
  a=after.split('var actor = actors.get(i);')[1].split('mc.level.getEntities(BOSS_TYPE')[0].rsplit('}',1)[0]
 else:
  b=before.split('for(var boss:')[1].split('){',1)[1].split('}var balance=')[0]
  a=after.split('var boss = bosses.get(i);')[1].split('} finally')[0].rsplit('}',1)[0]
 checks[var+'_formulas_unchanged']=norm(a)==norm(b)
checks['output_angles_unchanged']=norm(before.split('var balance=')[1].split('private EarthCamera')[0]).rstrip('}')==norm(after.split('var balance =')[1].split('private EarthCamera')[0]).rstrip('}')
checks['radius_16']=after.count('.inflate(16)')==1
checks['spectator_filter_both_queries']=after.count('EntitySelector.NO_SPECTATORS')==2
checks['clear_in_finally']='finally {\n            actors.clear();\n            bosses.clear();' in after
checks['no_resource_changes']=not subprocess.check_output(['git','diff','--name-only','main','--','src/main/resources'],cwd=R,text=True).strip()
props=dict(line.split('=',1)for line in(R/'gradle.properties').read_text().splitlines()if '='in line)
jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar')
with zipfile.ZipFile(jar)as z:
 checks['no_offline_harness_in_jar']=not any('CameraQueryChecks'in n for n in z.namelist())
checks['main_equals_origin']=subprocess.check_output(['git','rev-parse','main'],cwd=R,text=True).strip()==subprocess.check_output(['git','rev-parse','origin/main'],cwd=R,text=True).strip()
checks['installed_main_jar_unchanged']=hashlib.sha256(Path(json.loads((R/'docs/implementation/polymc-19762/installed.json').read_text())['installed_jar']).read_bytes()).hexdigest()==json.loads((R/'docs/implementation/polymc-19762/installed.json').read_text())['sha256']
(O/'source-checks.json').write_text(json.dumps(checks,indent=2)+'\n')
print(json.dumps(checks,indent=2));assert all(checks.values())
