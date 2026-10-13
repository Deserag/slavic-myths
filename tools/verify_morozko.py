"""Production/resource gates; never boots Minecraft or opens a launcher."""
from pathlib import Path
from PIL import Image
import json, zipfile, hashlib, re, struct, subprocess
from check_event_subscribers import check as subscribers
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/morozko-1.9.1';A=R/'src/main/resources/assets/slavicmyths';J=R/'build/libs/slavicmyths-1.9.1.jar';checks=0
def require(ok,message):
 global checks
 assert ok,message
 checks+=1
def main():
 global checks
 O.mkdir(parents=True,exist_ok=True);log=(R/'.tools/morozko-clean-build.log').read_text(encoding='utf8',errors='replace');require('BUILD SUCCESSFUL' in log,'clean build missing');require('FROST_OFFLINE_PASS' in log,'frost checks missing')
 items=['morozko_staff','frost_mirror','frost_resistance_rune','frost_rune','heart_of_eternal_cold','morozko_trial_totem'];langs={locale:json.loads((A/f'lang/{locale}.json').read_text(encoding='utf-8-sig')) for locale in ['ru_ru','en_us']}
 with zipfile.ZipFile(J) as z:
  names=set(z.namelist());require(not any('/smoke/' in n or 'FrostChecks' in n or n.startswith('tools/') for n in names),'test code in production');require(b'1.9.1' in z.read('META-INF/neoforge.mods.toml'),'version');
  for p in (R/'build/classes/java/main').rglob('*.class'):
   n=p.relative_to(R/'build/classes/java/main').as_posix();require(z.read(n)==p.read_bytes(),'stale bytecode '+n);require(struct.unpack('>H',p.read_bytes()[6:8])[0]==65,'not Java21 '+n)
  for p in (R/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   n=p.relative_to(R/'src/main/resources').as_posix()
   if n!='META-INF/neoforge.mods.toml':require(z.read(n)==p.read_bytes(),'stale resource '+n)
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'));checks+=1
  for item in items:
   im=Image.open(A/f'textures/item/{item}.png').convert('RGBA');require(im.size==(32,32) and im.getbbox() is not None,'missing sprite '+item)
   for locale,n in langs.items():require('item.slavicmyths.'+item in n and 'item.slavicmyths.'+item+'.desc' in n,item+' localization '+locale)
   require('assets/slavicmyths/models/item/'+item+'.json' in names,'missing model '+item)
  for model in ['morozko_staff_held','heart_of_eternal_cold_held']:
   n=json.loads((A/f'models/item/{model}.json').read_text());require(len(n['elements'])>=10,'no held geometry '+model)
   for e in n['elements']:
    require(all(-16<=v<=32 for v in e['from']+e['to']),'baked model bounds '+model)
    for face in e['faces'].values():require(all(0<=v<=16 for v in face['uv']),'baked UV '+model)
  rig=json.loads((O/'morozko-rig.json').read_text());require(rig['height']==52 and len(rig['bones'])>=30,'incomplete native rig');require(len(rig['boxes'])>=180,'missing voxel details')
  staff=[b for b in rig['boxes'] if b['bone'].startswith('staff_root')];held=json.loads((A/'models/item/morozko_staff_held.json').read_text())['elements'];require(len(staff)==len(held),'different boss/player staff')
  for b,e in zip(staff,held):require(all(abs((hi-lo)-size*.5)<1e-6 for lo,hi,size in zip(e['from'],e['to'],b['size'])),'shared staff proportions')
  alpha=Image.open(A/'textures/entity/morozko_ice_double.png').getchannel('A');require(set(alpha.getdata())=={0,145},'clone opacity')
  icons=[Image.open(A/f'textures/item/{i}.png').getchannel('A').tobytes() for i in items];require(len(set(icons))==6,'recolored generic silhouette')
  for p in (R/'src/main/java/org/slavicmyths/morozko').glob('*.java'):
   s=p.read_text(encoding='utf8');require('net.minecraft.client' not in s,'client leak '+p.name);require('setBlock(' not in s and 'setBlockAndUpdate(' not in s,'terrain mutation '+p.name)
   for key in re.findall(r'FrostContent\.text\("([a-z0-9_.]+)"',s):
    for locale,n in langs.items():require('frost191.'+key in n,'raw key '+locale+' '+key)
  sounds=json.loads((A/'sounds.json').read_text(encoding='utf8'));authored={k:v for k,v in sounds.items() if k.startswith('morozko.')};require(len(authored)==21,'sound map')
  for k,v in authored.items():
   for locale,n in langs.items():require(v['subtitle'] in n,'missing subtitle '+k)
   for sound in v['sounds']:
    path=A/('sounds/'+sound['name'].split(':')[1]+'.ogg');require(path.exists() and path.read_bytes().startswith(b'OggS'),'missing original OGG '+k)
  baseline=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.8.5.jar')
  if not baseline.exists():baseline=next(p for p in (baseline.parents[2]/'.slavicmyths-backups').rglob('slavicmyths-1.8.5.jar') if hashlib.sha256(p.read_bytes()).hexdigest()=='f2b66c13d9034f9e94c47385750d843ac6838b2ccfb7fec26fef7c80c64741c0')
  changes=[]
  allowed={'assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json','data/curios/tags/item/necklace.json','data/slavicmyths/tags/item/rune_staff.json','data/minecraft/tags/damage_type/bypasses_cooldown.json','slavicmyths.equipment.mixins.json','META-INF/neoforge.mods.toml'}
  with zipfile.ZipFile(baseline) as old:
   for n in old.namelist():
    if n.endswith('/') or n.startswith('META-INF/') or n.endswith('.class'):continue
    require(n in names,'removed old resource '+n)
    if old.read(n)!=z.read(n):changes.append(n);require(n in allowed,'unrelated resource change '+n)
   for locale in langs:
    previous=json.loads(old.read('assets/slavicmyths/lang/'+locale+'.json'));approved={'poloz.call_ready','poloz.overworld','poloz.repeat','poloz.first','poloz.altar','poloz.warning','poloz.no_spawn','poloz.relic.mor_dust','poloz.busy'};require(all(langs[locale].get(k)==v or k in approved or k.startswith('tales.entry.mor_horn.') for k,v in previous.items()),'unapproved prior translation change '+locale)
  nsub=subscribers(J);require(nsub>0,'subscriber audit');sha=hashlib.sha256(J.read_bytes()).hexdigest()
  result={'status':'PASS','checks':checks,'version':'1.9.1','sha256':sha,'jar':str(J),'event_subscribers':nsub,'changed_existing_resources':changes,'client_starts':0,'server_starts':0,'worlds_started':0,'gameplay':'NOT_RUN','MSPT':'NOT_MEASURED'};(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
