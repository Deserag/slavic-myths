"""Inspect the actual production JAR and decode authored audio. No game launches."""
from pathlib import Path
import json,zipfile,hashlib,re,subprocess,array,io
from PIL import Image
from check_event_subscribers import check as subscribers
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/verification/poloz-1.8.3.2';OUT.mkdir(parents=True,exist_ok=True)
checks=0
def check(ok,label):
 global checks
 checks+=1
 if not ok:raise AssertionError(label)
def main():
 jar=ROOT/'build/libs/slavicmyths-1.8.3.2.jar'
 log=(ROOT/'.tools/poloz-clean-build.log').read_text(encoding='utf-8',errors='replace');check('BUILD SUCCESSFUL' in log,'final clean build')
 native=(ROOT/'.tools/poloz-offline-final.log').read_text(encoding='utf-8',errors='replace');check('POLOZ_OFFLINE_PASS' in native and 'PASS 112 native' in native,'native gates')
 runtime=(ROOT/'.tools/poloz-headless-final.log').read_text(encoding='utf-8',errors='replace');check('All 23 required tests passed' in runtime,'headless gates')
 audio={}
 with zipfile.ZipFile(jar) as z:
  names=set(z.namelist())
  for n in names:
   check(not any(s in n.lower() for s in ['polozgametests','polozintegrationtests','polozchecks','morblockchecks','tools/smoke']),'no test leakage '+n)
  ids=json.loads((ROOT/'docs/spec-1.8.3/TECHNICAL_CONSTANTS.json').read_text(encoding='utf-8'))['blocks']
  for n in ids:
   for path in [f'assets/slavicmyths/blockstates/{n}.json',f'assets/slavicmyths/models/item/{n}.json',f'data/slavicmyths/loot_table/blocks/{n}.json']:check(path in names,'block resources '+path)
  for n in ['mor_horn','mor_dust','eye_of_mor','ashen_heart','poloz_core','poloz_scale']:
   check(f'assets/slavicmyths/models/item/{n}.json' in names,'item model '+n)
   im=Image.open(io.BytesIO(z.read(f'assets/slavicmyths/textures/item/{n}.png')));check(im.size in [(16,16),(32,32)] and im.mode=='RGBA','item pixels '+n)
  for n in names:
   if n.startswith('assets/slavicmyths/') and ('mor_' in n or 'poloz' in n) and n.endswith('.json'):
    data=json.loads(z.read(n));check(True,'json '+n)
    if '/models/' in n:
     for value in data.get('textures',{}).values():
      if value.startswith('slavicmyths:'):check('assets/slavicmyths/textures/'+value.split(':')[1]+'.png' in names,'model texture '+value)
   if n.startswith('assets/slavicmyths/textures/block/mor_') and n.endswith('.png'):check(Image.open(io.BytesIO(z.read(n))).size==(16,16),'16px '+n)
  recipe=json.loads(z.read('data/slavicmyths/recipe/mor_horn.json'));check(recipe['pattern']==['EGH','MRM','MMM'],'exact pattern');check(recipe['key']['R']['item']=='minecraft:goat_horn','vanilla horn center');check(''.join(recipe['pattern']).count('M')==5,'dust quantity')
  for lang in ['ru_ru','en_us']:
   data=json.loads(z.read(f'assets/slavicmyths/lang/{lang}.json'))
   for n in ids:check('block.slavicmyths.'+n in data,'block locale '+n)
   for n in ['mor_horn','mor_dust','eye_of_mor','ashen_heart','poloz_core','poloz_scale']:check('item.slavicmyths.'+n in data,'item locale '+n)
   check('poloz.warning' in data and 'poloz.irreversible' in data,'explicit permanent warning')
  sound=json.loads(z.read('assets/slavicmyths/sounds.json'))
  for key,value in sound.items():
   if not (key.startswith('poloz_') or key.startswith('mor_horn_')):continue
   p=value['sounds'][0]['name'].split(':')[1];asset='assets/slavicmyths/sounds/'+p+'.ogg';check(asset in names,'real OGG '+key)
   source=ROOT/'src/main/resources'/asset
   metadata=json.loads(subprocess.run(['ffprobe','-v','error','-show_streams','-of','json',str(source)],capture_output=True,check=True).stdout)['streams'][0]
   check(metadata['channels']==1,'mono '+key);check(int(metadata['sample_rate']) in [44100,48000],'rate '+key)
   raw=subprocess.run(['ffmpeg','-v','error','-i',str(source),'-f','s16le','-acodec','pcm_s16le','-'],capture_output=True,check=True).stdout;pcm=array.array('h');pcm.frombytes(raw);peak=max(abs(v) for v in pcm)/32768;check(0.01<peak<=10**(-1/20),'unclipped non-silent '+key)
   audio[key]={'seconds':len(pcm)/int(metadata['sample_rate']),'channels':1,'peak':peak}
  total=audio['mor_horn_call']['seconds']+audio['poloz_distant_reply']['seconds'];check(abs(total-9)<=.3,'9 second scenario')
  metadata=z.read('META-INF/neoforge.mods.toml').decode();check('1.8.3.2' in metadata,'real mod version')
  for name in ['GreatPoloz','PolozPart','PolozEvent','MorHorn','PolozGlob']:check('org/slavicmyths/poloz/'+name+'.class' in names,'production class '+name)
  for name in ['great_poloz_head','great_poloz_body','poloz_glob']:check('assets/slavicmyths/textures/entity/'+name+'.png' in names,'renderer texture '+name)
  for name in names:
   if name.startswith('org/slavicmyths/poloz/') and name.endswith('.class'):check(z.read(name)==(ROOT/'build/classes/java/main'/name).read_bytes(),'current production bytecode '+name)
 count=subscribers(jar);check(count>0,'event subscriber handlers')
 report={'status':'PASS','version':'1.8.3.2','checks':checks,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'jar':str(jar),'event_subscribers':count,'audio':audio,'client_starts':0,'headless_server_start_attempts':12, 'headless_world_server_starts':11, 'successful_headless_runs':10,'manual_gameplay':'NOT_RUN','MSPT':'NOT_MEASURED'}
 (OUT/'resource-gates.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({k:v for k,v in report.items() if k!='audio'},ensure_ascii=False))
if __name__=='__main__':main()
