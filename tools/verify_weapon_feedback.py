"""Offline artifact gates; does not assert in-game rendering or projectile behaviour."""
from pathlib import Path
import hashlib,io,json,zipfile
from PIL import Image
from check_event_subscribers import check
from weapon_feedback_assets import apply,GRIPS
from weapon_patch_assets import main as overlay
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapon-feedback';J=R/'build/libs/slavicmyths-1.9.7.2.jar'
checks=0
def need(value,label):
 global checks
 checks+=1
 if not value:raise AssertionError(label)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 log=(O/'BUILD.log').read_text(encoding='utf-8',errors='replace')
 need('BUILD SUCCESSFUL' in log,'final clean build')
 need('WEAPON_FEEDBACK_OFFLINE_PASS' in log,'native focused checks')
 need('NIGHT_OFFLINE_PASS' in log,'Karachun regression')
 root=R/'src/main/resources';before={p.relative_to(root).as_posix():digest(p) for p in root.rglob('*') if p.is_file()}
 apply();need(before=={p.relative_to(root).as_posix():digest(p) for p in root.rglob('*') if p.is_file()},'feedback overlay exactly reproducible')
 overlay();need(before=={p.relative_to(root).as_posix():digest(p) for p in root.rglob('*') if p.is_file()},'existing weapon overlay retains final resources')
 with zipfile.ZipFile(J) as z:
  names=set(z.namelist());need(z.testzip() is None,'JAR CRC')
  need('version="1.9.7.2"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'production version')
  need(not any(n.startswith(('tools/','smoke/')) or any(t in n for t in ['WeaponFeedbackChecks','CreatureFeedbackTests','NightChecks','GameTests']) for n in names),'offline/runtime fixtures excluded')
  for n,h in before.items():
   if n=='META-INF/neoforge.mods.toml':continue
   need(n in names and hashlib.sha256(z.read(n)).hexdigest()==h,'exact resource '+n)
  for p in (R/'build/classes/java/main').rglob('*.class'):
   n=p.relative_to(R/'build/classes/java/main').as_posix();need(n in names and z.read(n)==p.read_bytes(),'exact compiled class '+n)
  for n in names:
   if n.endswith('.json'):json.loads(z.read(n));need(True,'JSON '+n)
   if n.endswith('.png'):Image.open(io.BytesIO(z.read(n))).verify();need(True,'PNG '+n)
  for name,grip in GRIPS.items():
   d=json.loads(z.read('assets/slavicmyths/models/item/'+name+'.json'))
   for side in ['righthand','lefthand']:
    t=d['display']['thirdperson_'+side];need(t['rotation']==[90,0,0],'upright '+name+side);need(t['translation']==[0,0,(8-grip)*t['scale'][1]],'grip '+name+side)
  for name in ['WeaponHands','NightSwingPose']:
   need('org/slavicmyths/client/'+name+'.class' in names,'client class '+name)
  need('org/slavicmyths/gorynych/CrossbowVolley.class' in names,'bounded server queue')
  for lang in ['ru_ru','en_us']:
   d=json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json'));need(not any('\ufffd' in v for v in d.values() if isinstance(v,str)),'no replacement characters '+lang)
   for key in ['gorynych196.volley_fan','gorynych196.volley_burst']:need(key in d,'localized control '+key)
   for i in range(3):need('%s' not in d[f'gorynych196.mode_{i}'],'no obsolete charge counter')
 subscribers=check(J)
 result={'status':'PASS','version':'1.9.7.2','jar':str(J),'sha256':digest(J),'checks':checks,'event_subscribers':subscribers,'weapon_models_corrected':len(GRIPS),'native_tests':'state writes/copies, body damage policy, continuous motion, native transform directions','gameplay':'NOT_RUN','visual_acceptance':'MANUAL','client_starts':0,'server_starts':0,'gametest_starts':0}
 (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
