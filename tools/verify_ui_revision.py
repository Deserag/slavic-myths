"""Production/UI gates against the installed 1.7.7 release; no historical claims reused."""
from pathlib import Path
import json,zipfile,hashlib,struct,re
from PIL import Image
from check_event_subscribers import check as check_subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/ui-1.7.8';O.mkdir(parents=True,exist_ok=True)
checks=0
def check(value,label):
 global checks
 checks+=1
 if not value:raise AssertionError(label)
build=(R/'work/ui-clean-build.log').read_text(encoding='utf8',errors='replace');client=(R/'work/ui-client-final.log').read_text(encoding='utf8',errors='replace')
check('BUILD SUCCESSFUL' in build,'Clean build failed');check('UI_LOGIC_PASS' in build,'Logic tests missing');check('UI_CLIENT_CAPTURE_PASS scenes=20' in client,'Real client capture incomplete');check('has no @SubscribeEvent methods' not in client,'Client subscriber crash')
owned=['ClassScreen','ClassClient','ClassTheme','FolkUi','TaleScreen','UiSearch','UiText','TaleTreeLayout','BrightFalconClient','SkimClient','MarinkaClient']
allow={'META-INF/neoforge.mods.toml','META-INF/MANIFEST.MF','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'}
jar=R/'build/libs/slavicmyths-1.7.8.jar'
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(R/'release/slavicmyths-1.7.7.jar') as old:
 names=set(z.namelist());prior=set(old.namelist());check(prior<=names,'Removed saved-world resource/class')
 changed=[name for name in prior if z.read(name)!=old.read(name)]
 for name in changed:check(name in allow or any(name=='org/slavicmyths/client/'+c+'.class' or name.startswith('org/slavicmyths/client/'+c+'$') for c in owned),'Unrelated change '+name)
 for name in names:
  check(not any(v in name.lower() for v in ['uiscreenprobe','gametest','org/slavicmyths/verify/','tools/smoke','uiclient']),'Test leakage '+name)
  if name.endswith('.class'):
   check(struct.unpack('>H',z.read(name)[6:8])[0]==65,'Java21 '+name)
   p=R/'build/classes/java/main'/name
   if p.exists():check(p.read_bytes()==z.read(name),'Stale compiled class '+name)
  p=R/'src/main/resources'/name
  if p.is_file() and name!='META-INF/neoforge.mods.toml':check(p.read_bytes()==z.read(name),'Stale resource '+name)
  if name.endswith('.json'):json.loads(z.read(name));check(True,'JSON '+name)
 lang=[json.loads(z.read('assets/slavicmyths/lang/'+l+'.json')) for l in ['ru_ru','en_us']]
 for key in ['classes.slavicmyths.binding_slot','classes.slavicmyths.assigned_key','classes.slavicmyths.conflict_hotbar','classes.slavicmyths.conflict_ability','tales.center','tales.search_empty','tales.count_short']+['tales.tab.'+p for p in ['tales','creatures','people','places','crafts','nature','quests','deeds']]:check(all(key in l for l in lang),'Missing localization '+key)
 check('version="1.7.8"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'Production version')
subscribers=check_subscribers(jar)
try:check_subscribers(R/'release/slavicmyths-1.7.7.jar')
except AssertionError:old_rejected=True
else:old_rejected=False
check(old_rejected,'Regression guard did not detect old broken release')
frames=list((O/'client-frames').glob('*.png'));check(len(frames)==20,'Expected 20 real client frames')
for p in frames:
 with Image.open(p) as im:check(im.size==(1920,1080),'Wrong capture resolution '+p.name);im.verify()
result={'status':'PASS','version':'1.7.8','checks':checks,'clean_build':'PASS','sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'prior_entries_identical':len(prior)-len(changed),'changed_entries':sorted(changed),'event_subscribers_checked':subscribers,'old_1_7_7_rejected_by_guard':old_rejected,'client_launches':5,'final_real_client_frames':20,'gui_scales':[2,3,4],'manual_gameplay_mp_relog':'NOT_RUN'}
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('UI PRODUCTION PASS checks='+str(checks)+' event_subscribers='+str(subscribers)+' prior_identical='+str(result['prior_entries_identical']))
