"""1.7.9 production/resource audit. No client or gameplay launch claims."""
from pathlib import Path
import hashlib,json,zipfile,struct,sys
from PIL import Image
from check_event_subscribers import check as subscribers
from feedback_resource_policy import EGGS,SOUNDS,apply
from vanilla_item_style import NAMES,sprite,FAMILIES
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/feedback-1.7.9';O.mkdir(parents=True,exist_ok=True)
checks=0
def check(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
log=(R/'work/feedback-1.7.9-build.log').read_text(encoding='utf8',errors='replace')
check('BUILD SUCCESSFUL' in log,'clean build');check('UI_LOGIC_PASS checks=71' in log,'existing UI helper checks')
jar=R/'build/libs/slavicmyths-1.7.9.jar'
vanilla=json.loads((R/'work/vanilla-sounds.json').read_text(encoding='utf8'))
allow={'META-INF/neoforge.mods.toml','META-INF/MANIFEST.MF','assets/slavicmyths/sounds.json',*('assets/slavicmyths/lang/'+l+'.json' for l in ['ru_ru','en_us']),*('assets/slavicmyths/models/item/'+e+'.json' for e in EGGS),*('assets/slavicmyths/textures/item/'+n+'.png' for n in NAMES)}
owned=['ClassScreen','ClassTheme','ClassSettingsScreen','ClassClient','TaleScreen','BrightFalconClient','SkimClient','KalinClient']
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(R/'release/slavicmyths-1.7.8.jar') as prior:
 names=set(z.namelist());old=set(prior.namelist());check(old<=names,'prior resource/class removed')
 changed=[n for n in old if z.read(n)!=prior.read(n)]
 for n in changed:check(n in allow or any(n=='org/slavicmyths/client/'+c+'.class' or n.startswith('org/slavicmyths/client/'+c+'$') for c in owned),'unexpected change '+n)
 for n in names:
  check(not any(v in n.lower() for v in ['uiscreenprobe','gametest','org/slavicmyths/verify/','tools/smoke','uiclient']),'fixture leakage '+n)
  if n.endswith('.class'):
   check(struct.unpack('>H',z.read(n)[6:8])[0]==65,'Java21 '+n)
   check((R/'build/classes/java/main'/n).read_bytes()==z.read(n),'stale class '+n)
  p=R/'src/main/resources'/n
  if p.is_file() and n!='META-INF/neoforge.mods.toml':check(p.read_bytes()==z.read(n),'stale resource '+n)
  if n.endswith('.json'):json.loads(z.read(n));check(True,'JSON '+n)
 check('version="1.7.9"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'metadata')
 for egg in EGGS:check(json.loads(z.read('assets/slavicmyths/models/item/'+egg+'.json'))=={'parent':'minecraft:item/template_spawn_egg'},'egg model '+egg)
 sounds=json.loads(z.read('assets/slavicmyths/sounds.json'));old_sounds=json.loads(prior.read('assets/slavicmyths/sounds.json'))
 check(sounds.keys()==old_sounds.keys(),'event IDs changed')
 for name,target in SOUNDS.items():
  check(target.split(':',1)[1] in vanilla,'missing vanilla event '+target)
  check(sounds[name]['sounds'][0]['name']==target and sounds[name]['sounds'][0]['type']=='event','wrong alias '+name)
  check(sounds[name].get('subtitle')==old_sounds[name].get('subtitle'),'subtitle changed '+name)
 for name in set(sounds)-set(SOUNDS):check(sounds[name]==old_sounds[name],'unrelated sound changed '+name)
 langs=[json.loads(z.read('assets/slavicmyths/lang/'+l+'.json')) for l in ['ru_ru','en_us']]
 for key in ['paths','show_skills','show_directions','assign_to','slot_occupied','clear_slot','automatic_skill','learn_then_assign','assign_instruction','binding_hint']:
  check(all('classes.slavicmyths.'+key in l for l in langs),'missing UI label '+key)
 for kind,items in FAMILIES.items():
  for name in items:
   with Image.open(R/'src/main/resources/assets/slavicmyths/textures/item'/f'{name}.png') as im:
    check(im.size==(16,16),'sprite dimensions '+name)
    check(im.convert('RGBA').tobytes()==sprite(kind,name).tobytes(),'generator drift '+name)
 # Gameplay, registry, held weapon geometry and mob rigs must be byte-identical.
 for name in old:
  if name.startswith('org/slavicmyths/') and not name.startswith('org/slavicmyths/client/') or name.startswith('data/') or '/models/item/' in name and name.endswith('_held.json') or '/textures/entity/' in name:
   check(prior.read(name)==z.read(name),'gameplay/held rig changed '+name)
count=subscribers(jar)
# Re-running the policy must reproduce the production resource bytes.
assets=[R/'src/main/resources'/n for n in allow if (R/'src/main/resources'/n).is_file() and n!='META-INF/neoforge.mods.toml']
before={p:p.read_bytes() for p in assets};apply()
for p,blob in before.items():check(p.read_bytes()==blob,'policy not idempotent '+str(p))
source=(R/'src/main/java/org/slavicmyths/client/ClassScreen.java').read_text(encoding='utf8')
check('renderTree(' not in source and 'ClassTreeLayout' not in source,'tree entry remains')
for screen in ['ClassScreen','TaleScreen','ClassSettingsScreen']:
 check('super.render(g,mx,my,delta)' not in (R/f'src/main/java/org/slavicmyths/client/{screen}.java').read_text(encoding='utf8'),'background repainted '+screen)
result=dict(version='1.7.9',status='PASS',checks=checks,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),event_subscribers=count,old_entries_identical=len(old)-len(changed),changed_entries=sorted(changed),eggs=len(EGGS),item_sprites=len(NAMES),vanilla_enemy_cues=len(SOUNDS),ui_helper_checks=71,client_launches=0,server_launches=0,gameplay='NOT_RUN',visual_client_qa='NOT_RUN',listening_qa='NOT_RUN')
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('PRODUCTION_RESOURCE_PASS checks='+str(checks)+' sha256='+result['sha256'])
