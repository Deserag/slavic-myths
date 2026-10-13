"""Audit the exact production artifact, resource reproduction and previous release preservation."""
from pathlib import Path
from PIL import Image
import json,hashlib,zipfile,subprocess
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/winters-1.9.7';J=R/'build/libs/slavicmyths-1.9.7.jar';checks=0
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def need(value,message):
 global checks
 checks+=1
 if not value:raise AssertionError(message)
def main():
 log=(R/'.tools/winters-clean-build.log').read_text(encoding='utf-8',errors='replace')
 need('BUILD SUCCESSFUL' in log,'clean production build')
 for s in ['WINTERS_OFFLINE_PASS','GORYNYCH_OFFLINE_PASS','HILL_OFFLINE_PASS','GUARDIAN_OFFLINE_PASS','SANCTUARY_OFFLINE_PASS','NIGHT_OFFLINE_PASS','FROST_OFFLINE_PASS','QUEST185_OFFLINE_PASS','POLOZ_OFFLINE_PASS','120 native']:need(s in log,s)
 res=R/'src/main/resources';before={p.relative_to(res).as_posix():sha(p) for p in res.rglob('*') if p.is_file()}
 subprocess.run(['python',str(R/'tools/winters_assets.py')],check=True)
 need(before=={p.relative_to(res).as_posix():sha(p) for p in res.rglob('*') if p.is_file()},'asset generator reproducibility')
 old=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.9.6.jar')
 need(sha(old)=='e197311398f3634c3e2303dce37535d6dd457f1cf88e52e503452d32125a37aa','exact installed 1.9.6 baseline')
 changed=[];allowed={'assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','slavicmyths.equipment.mixins.json'}
 with zipfile.ZipFile(J) as z,zipfile.ZipFile(old) as b:
  names=set(z.namelist())
  for n in b.namelist():
   if n.startswith(('assets/','data/')) or n=='slavicmyths.equipment.mixins.json':
    need(n in names,'old resource retained '+n)
    if z.read(n)!=b.read(n):changed.append(n);need(n in allowed,'unexpected old resource change '+n)
   if n.startswith(('org/slavicmyths/morozko/','org/slavicmyths/karachun/','org/slavicmyths/gorynych/','org/slavicmyths/poloz/')) and n.endswith('.class'):need(z.read(n)==b.read(n),'old boss mechanics unchanged '+n)
  need(not any('WintersChecks' in n or n.startswith(('tools/','smoke/')) for n in names),'offline tests excluded from production')
  for n in names:
   if n.endswith('.json'):json.loads(z.read(n));need(True,'valid JSON '+n)
  for mode,ref in [('amulet_two_winters','04_amulet_neutral_32.png'),('amulet_two_winters_morozko','05_amulet_morozko_32.png'),('amulet_two_winters_karachun','06_amulet_karachun_32.png')]:
   n='assets/slavicmyths/textures/item/'+mode+'.png';need(z.read(n)==(R/'docs/specs/1.9.7/references'/ref).read_bytes(),'exact provided pixel sprite '+mode)
   im=Image.open(res/n);need(im.mode=='RGBA' and im.size==(32,32) and im.getextrema()[3][0]==0,'32px transparent sprite')
   need('assets/slavicmyths/models/item/'+mode+'.json' in names,'native generated model '+mode)
  for lang in ['ru_ru','en_us']:
   n='assets/slavicmyths/lang/'+lang+'.json';language=json.loads(z.read(n));old_language=json.loads(b.read(n))
   need(all(language.get(k)==v for k,v in old_language.items()),'existing localization untouched '+lang)
   for key in ['item.slavicmyths.amulet_two_winters','effect.slavicmyths.nether_aura','winters.slavicmyths.controls','winters.slavicmyths.mode','winters.slavicmyths.cooldown','winters.slavicmyths.aura_3']:need(key in language,'localized '+key)
  for recipe,cat in [('original','equipment'),('copy','misc')]:
   v=json.loads(z.read('data/slavicmyths/recipe/two_winters_'+recipe+'.json'));need(v=={'type':'slavicmyths:two_winters','category':cat},'disjoint recipe category')
 for p in (R/'src/main/java/org/slavicmyths/winters').glob('*.java'):need('net.minecraft.client.' not in p.read_text(encoding='utf-8'),'common/client isolation '+p.name)
 events=(R/'src/main/java/org/slavicmyths/winters/WintersEvents.java').read_text(encoding='utf-8')
 need('p.tickCount%20!=0' in events,'bounded periodic inventory checks')
 need(events.count('getEntitiesOfClass(')==1,'one local target search only on cast')
 need('WintersTemplates.get(s).contains' in (R/'src/main/java/org/slavicmyths/winters/WintersRecipe.java').read_text(encoding='utf-8'),'server receipt required')
 need('p.removeEffect(MobEffects.WEAKNESS)' not in events,'foreign Weakness not removed')
 result={'status':'PASS','version':'1.9.7','checks':checks,'sha256':sha(J),'baseline_sha256':sha(old),'changed_old_resources':changed,'event_subscribers':subscribers(J),'client_starts':0,'server_starts':0,'runtime':'NOT_RUN','offline':'ten suites; native recipe/metadata, receipt NBT and rules checks are not live game/relog/shift-click tests'}
 O.mkdir(parents=True,exist_ok=True);(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
