"""No gameplay claim: exact preservation of production and armor registry/resource packaging."""
from pathlib import Path
import subprocess,json,hashlib,zipfile
R=Path(__file__).resolve().parents[1];OUT=R/'docs/implementation/armor287-20261026';checks=[]
def check(ok,name):
 if not ok:raise AssertionError(name)
 checks.append(name)
check(not subprocess.check_output(['git','diff','edc91530','--name-only','--','src/main'],cwd=R,text=True).strip(),'all production source/resources including stats/models/loot and #278/#284 unchanged')
rows=json.loads((OUT/'native.json').read_text(encoding='utf8'))['items'];check(len(rows)==34,'all34 native registry rows')
check(sum(row['class'].endswith('ClothingItem') for row in rows)==7,'seven clothing entries have no ArmorItem')
for row in rows:
 check(row['before']==row['removed'],'no residual native attrs '+row['id'])
jar=R/'build/libs/slavicmyths-2.0.1-HELL.jar'
with zipfile.ZipFile(jar) as z:
 for name in ['UniqueEquipment','UniqueEquipmentEvents','UniqueEquipmentRules']:check('org/slavicmyths/equipment/'+name+'.class' in z.namelist(),'packaged existing '+name)
 check(not any('Armor287' in name or name.startswith('org/slavicmyths/verify/') for name in z.namelist()),'test classes excluded')
report={'checks':checks,'count':len(checks),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'jar':str(jar.resolve()),'runtime':'separate GameTest logs; source/package checks only'};(OUT/'source-jar.json').write_text(json.dumps(report,indent=2)+'\n');print('ARMOR287_SOURCE_JAR PASS checks='+str(len(checks)))
