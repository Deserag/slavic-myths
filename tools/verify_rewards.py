"""1.7.6 production gates, relative to the accepted 1.7.5 release."""
from pathlib import Path
import hashlib,json,zipfile,io,struct,re,subprocess,sys
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/rewards-1.7.6'
jar=R/'build/libs/slavicmyths-1.7.6.jar';baseline=R/'release/slavicmyths-1.7.5.jar'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(baseline)=='03c52508b6ae5332f201259895df3e6f7fad085e35d41d8b8fc34704eac93cf4'
checks=0
def check(value,message):
 global checks
 checks+=1
 if not value:raise AssertionError(message)
runtime=O/'runtime-final.log';log=runtime.read_text(encoding='utf-8',errors='replace')
check('BUILD SUCCESSFUL' in (O/'clean-build.log').read_text(encoding='utf-8',errors='replace'),'Clean build not successful')
m=re.search(r'REWARDS_CHECKS_PASSED (\d+)',log);check(m is not None and '1 required tests failed' not in log,'Dedicated test not passed')
owned=['item/FolkAccessoryItem','SlavicMyths','rpg/Runes','rpg/RuneDefinition','bogatyr/BogatyrLedger','marinka/WitchYard','skim/SkimValley','finist/KartausEncounter','finist/KartausEntity','kalin/KalinEncounter','tales/TaleService','equipment/mixin/WardBlockMixin','client/ClientSetup','client/ClearSightClient','client/FolkEquipmentLayer','client/TaleScreen','compat/JeiRunes']
allow={'META-INF/MANIFEST.MF','META-INF/neoforge.mods.toml','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json'}
allow.update('data/curios/tags/item/'+x+'.json' for x in ['belt','ring','necklace'])
allow.update('data/minecraft/tags/damage_type/'+x+'.json' for x in ['bypasses_armor','bypasses_effects','bypasses_enchantments','bypasses_shield'])
changed=[];identical=0
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(baseline) as old:
 names=set(z.namelist());prior=set(old.namelist());check(prior<=names,'Existing entries lost')
 for name in prior:
  if z.read(name)==old.read(name):identical+=1;continue
  changed.append(name)
  allowed=name in allow or any(name=='org/slavicmyths/'+c+'.class' or name.startswith('org/slavicmyths/'+c+'$') for c in owned)
  check(allowed,'Unrelated change '+name)
 for name in names:
  data=z.read(name)
  check(not any(x in name.lower() for x in ['org/slavicmyths/verify/','gametest','smoke/','slavicmyths_rewards','rewardsruntime']),'Test leakage '+name)
  if name.endswith('.class'):
   check(struct.unpack('>H',data[6:8])[0]==65,'Not Java21 '+name)
   compiled=R/'build/classes/java/main'/name
   if compiled.exists():check(data==compiled.read_bytes(),'Stale production class '+name)
   if name.startswith('org/slavicmyths/rewards/'):check(b'net/minecraft/client' not in data,'Common links client '+name)
  if name.endswith('.json'):json.loads(data)
  if name.endswith('.png'):Image.open(io.BytesIO(data)).verify()
 for p in (R/'src/main/resources').rglob('*'):
  if not p.is_file():continue
  name=p.relative_to(R/'src/main/resources').as_posix();check(name in names,'Missing '+name)
  expected=p.read_bytes()
  if name=='META-INF/neoforge.mods.toml':expected=expected.replace(b'${mod_version}',b'1.7.6').replace(b'${neo_version}',b'21.1.255')
  check(expected.replace(b'\r\n',b'\n')==z.read(name).replace(b'\r\n',b'\n'),'Stale resource '+name)
 for lang in ['ru_ru','en_us']:
  name='assets/slavicmyths/lang/'+lang+'.json';a=json.loads(old.read(name));b=json.loads(z.read(name))
  for key,value in a.items():check(b.get(key)==value,'Existing translation changed '+key)
 sounds=json.loads(z.read('assets/slavicmyths/sounds.json'))
 for key,value in json.loads(old.read('assets/slavicmyths/sounds.json')).items():check(sounds.get(key)==value,'Existing sound changed '+key)
 spec=R/'docs/specs/1.7.6';icons=list((spec/'07_Visual_References/Item_Textures_64').glob('*.png'));check(len(icons)==19,'Not nineteen supplied icons')
 for p in icons:
  im=Image.open(p);check(im.size==(64,64) and im.mode=='RGBA','Invalid icon '+p.name)
  check(z.read('assets/slavicmyths/textures/item/'+p.name)==p.read_bytes(),'Approved icon drift '+p.name)
  id=p.stem
  for lang in ['ru_ru','en_us']:
   d=json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json'));check('item.slavicmyths.'+id in d and 'item.slavicmyths.'+id+'.desc' in d,'Missing locale '+id)
 for p in (spec/'08_Audio/ogg').glob('*.ogg'):check(z.read('assets/slavicmyths/sounds/rewards176/'+p.name)==p.read_bytes(),'Audio altered '+p.name)
 for key,entry in json.loads((spec/'08_Audio/SOUND_MAP.json').read_text()).items():
  event=entry['registration'].split(':')[1];check(event in sounds,'Missing event '+event)
  for lang in ['ru_ru','en_us']:check(sounds[event]['subtitle'] in json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json')),'Missing subtitle '+event)
 for p in (R/'src/main/resources/data/slavicmyths/recipe').glob('trophy_*.json'):
  if p.stem not in ['trophy_idol_chest','trophy_marinka_cloak','trophy_skim_necklace','trophy_kartaus_mantle','trophy_kalin_sabre']:continue
  d=json.loads(p.read_text());check(all(isinstance(v,dict) and 'item' in v for v in d['key'].values()),'Not MC1.21.1 ingredient format')
result=dict(status='PASS',version='1.7.6',checks=checks,sha256=sha(jar),clean_build='PASS',dedicated_cases=1,dedicated_assertions=int(m.group(1)),prior_entries_identical=identical,changed_prior_entries=sorted(changed),client_launches=0,client_visual_audio_acceptance='NOT_RUN')
result['dedicated_server_launches']=sum('ModLauncher running:' in p.read_text(encoding='utf-8',errors='replace') for p in O.glob('runtime-*.log') if re.fullmatch(r'runtime-\d+\.log',p.name))+int('ModLauncher running:' in (O/'kalin-regression.log').read_text(encoding='utf-8',errors='replace'))
(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
