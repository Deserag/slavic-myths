"""Offline, scoped audio contracts for #61; never runtime or artistic acceptance."""
from pathlib import Path
import sys,json,hashlib,subprocess,zipfile
R=Path(__file__).resolve().parents[1];sys.path[:0]=[str(R/'.tools/audio-libs'),str(R/'tools')]
import soundfile as sf,numpy as np
from audio_groups_61 import A,P,EVENTS,DELEGATED_PREFIXES
from audio_groups_61_plan import PLAN,ALIASES,BLOCKED,NEW
BASE='ee4a40bec4f603e06dd69ee3b35ad4290dfc2389';checks=[]
def check(ok,msg):
 if not ok:raise AssertionError(msg)
 checks.append(msg)
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 rows=json.loads((P/'selection.json').read_text(encoding='utf-8'));sources={s['key']:s for s in json.loads((P/'sources.json').read_text(encoding='utf-8'))}
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));baseline=__import__('audio_resource_names_61').qualified(json.loads(old('src/main/resources/assets/slavicmyths/sounds.json')))
 check(set(events)==set(baseline)|NEW,'Existing event IDs preserved; exactly the planned audio IDs added')
 check(all(events[k]==v for k,v in baseline.items() if k not in EVENTS),'Every out-of-scope event including approved voices, Rusalka and Kuzya unchanged')
 for row in rows:
  p=A/'sounds'/row['file'];x,sr=sf.read(p);check(sr==44100 and x.ndim==1 and sf.info(p).subtype=='VORBIS' and np.isfinite(x).all(),'Decoded spatial Vorbis '+row['file'])
  check(sha(p)==row['sha256'] and max(abs(x))<.8 and abs(x[0])<.025 and abs(x[-1])<.025,'Output hash/peak/edges '+row['file'])
  check(len(x)/sr<=PLAN[row['event']]['cap']+.02,'Explicit role duration cap '+row['event'])
  check(not row['owner_approved'] and row['hearing']=='NOT_LISTENED','No invented hearing/acceptance '+row['event'])
  for ref in row['sources']:
   s=sources[ref['key']]
   public_domain=(s['key']=='stilgar__soft_whistle.ogg' and s['license']=='PD-author' and s['page']=='https://commons.wikimedia.org/wiki/File:Soft_whistle.ogg' and s['license_url']==s['page']+'#Licensing')
   check(sha(P/s['file'])==s['sha256']==ref['sha256'] and (s['license'] in ['CC0-1.0','CC-BY-4.0'] or public_domain) and s['page'].startswith('https://'),'Exact redistributable provenance '+ref['key'])
 asset_root=Path('C:/Users/pavel/AppData/Roaming/PolyMC/assets');objects=json.loads((asset_root/'indexes/17.json').read_text())['objects'];h=objects['minecraft/sounds.json']['hash'];vp=asset_root/'objects'/h[:2]/h;check(hashlib.sha1(vp.read_bytes()).hexdigest()==h,'Local vanilla sound index verified read-only');vanilla=json.loads(vp.read_bytes())
 for event,definition in events.items():
  for entry in definition.get('sounds',[]):
   entry={'name':entry} if isinstance(entry,str) else entry;name=entry['name'];namespace,path=name.split(':',1) if ':' in name else ('minecraft',name)
   if entry.get('type','sound')=='event':check(path in (vanilla if namespace=='minecraft' else events),'Actual namespace event resolves '+event+' -> '+name)
   else:check((A/'sounds'/(path+'.ogg')).exists() if namespace=='slavicmyths' else 'minecraft/sounds/'+path+'.ogg' in objects,'Actual namespace FILE resolves '+event+' -> '+name)
 with zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-sources.jar') as sourcejar:
  serializer=sourcejar.read('net/minecraft/client/resources/sounds/SoundEventRegistrationSerializer.java').decode();check('ResourceLocation.parse(GsonHelper.getAsString' in serializer,'Installed Minecraft serializer parses name independently of sound-event namespace')
 for e,c in PLAN.items():check(len(events[e]['sounds'])==len(c['sources']) and all(x['name'].startswith('slavicmyths:recorded61/') for x in events[e]['sounds']),'Exact planned variant mapping '+e)
 for e,c in ALIASES.items():check(events[e]['sounds']==[dict(name='minecraft:'+c['target'],type='event')],'Semantic vanilla reference '+e)
 for e in BLOCKED:
  if e not in ALIASES:check(events[e]['sounds']==[],'Unresolved source stays muted '+e)
 ledger=json.loads((A/'audio_licenses/delegated-recordings-61.json').read_text(encoding='utf-8'))
 check(set(ledger['events'])=={e for e in PLAN if e.startswith(DELEGATED_PREFIXES)},'Quarantine exceptions restricted to exact delegated recordings')
 for e,proof in ledger['events'].items():
  check(len(proof)==len(events[e]['sounds']) and all(p['name']==v['name'] and sha(A/'sounds'/(p['name'].removeprefix('slavicmyths:')+'.ogg'))==p['sha256'] for p,v in zip(proof,events[e]['sounds'])),'Quarantine ledger exact bytes '+e)
 for lang in ['ru_ru','en_us']:
  data=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));previous=json.loads(old(f'src/main/resources/assets/slavicmyths/lang/{lang}.json'))
  allowed={events[e]['subtitle'] for e in PLAN|ALIASES};check(all(data[k]==v for k,v in previous.items() if k not in allowed),'No unrelated translation changes '+lang)
  for e in PLAN|ALIASES:check(bool(data.get(events[e]['subtitle'])),'RU/EN subtitle '+lang+' '+e)
 # All pre-existing resources except exact maps/credits remain byte frozen.
 names=subprocess.check_output(['git','ls-tree','-r',BASE,'src/main/resources','src/main/java'],cwd=R).decode().splitlines()
 skip={'src/main/java/org/slavicmyths/entity/DomovoyEntity.java','src/main/java/org/slavicmyths/entity/WildlifeEntity.java','src/main/java/org/slavicmyths/registry/ModSounds.java','src/main/java/org/slavicmyths/bandit/BanditEntity.java','src/main/resources/assets/slavicmyths/sounds.json',*[f'src/main/resources/assets/slavicmyths/lang/{l}.json' for l in ['ru_ru','en_us']]}
 for entry in names:
  metadata,n=entry.split('\t');blob=metadata.split()[2]
  if n not in skip:
   data=(R/n).read_bytes()
   if b'\0' not in data:
    try:data=data.decode('utf-8').replace('\r\n','\n').encode('utf-8')
    except UnicodeDecodeError:pass
   actual=hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest();check(actual==blob,'Frozen existing resource '+n)
 for path in ['src/main/java/org/slavicmyths/entity/DomovoyEntity.java','src/main/java/org/slavicmyths/entity/WildlifeEntity.java','src/main/java/org/slavicmyths/registry/ModSounds.java']:
  expected=old(path).decode('utf-8').replace('\r\n','\n')
  if 'DomovoyEntity' in path:expected=expected.replace('message = "refused"; playSound(ModSounds.DOMOVOY_HURT.get(), 0.4F, 0.9F);','message = "refused"; playSound(ModSounds.DOMOVOY_REFUSE.get(), 0.4F, 1.0F);').replace('playSound(net.minecraft.sounds.SoundEvents.VILLAGER_YES, 0.4F, 1.3F);','playSound(ModSounds.DOMOVOY_THANKS.get(), 0.4F, 1.0F);')
  elif 'WildlifeEntity' in path:expected=expected.replace('if(!isInWater())voice("impact",kind==Kind.BEAR?.12F:.055F);','if(!isInWater())voice("step",kind==Kind.BEAR?.12F:.055F);')
  else:expected=expected.replace('public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_HURT','public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_REFUSE = sound("domovoy_refuse");\n    public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_THANKS = sound("domovoy_thanks");\n    public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_HURT').replace('"alert","roar","attack","impact"','"alert","roar","attack","impact","step"')
  if 'ModSounds' in path:expected=__import__('bandit_audio_contract_61').registry_edit(expected)
  check((R/path).read_text(encoding='utf-8')==expected,'Exact audio-only source diff '+path)
 path='src/main/java/org/slavicmyths/bandit/BanditEntity.java';expected=__import__('bandit_audio_contract_61').source_edit(old(path).decode('utf-8').replace('\r\n','\n'));check((R/path).read_text(encoding='utf-8')==expected,'Full bandit audio-only diff: unchanged attacks/damage/timers/AI/NBT')
 version=next(l.split('=',1)[1].strip() for l in (R/'gradle.properties').read_text().splitlines() if l.startswith('mod_version='))
 jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'JAR CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    n=p.relative_to(R/'src/main/resources').as_posix()
    if n!='META-INF/neoforge.mods.toml':check(z.read(n)==p.read_bytes(),'Packaged resource parity '+n)
  check(not any(n.startswith(('art/','tools/')) for n in z.namelist()),'No review sources or fixtures in production JAR')
 report=dict(status='IN_PROGRESS_OWNER_PENDING',baseline=BASE,checks=len(checks),recorded_variants=len(rows),recorded_events=len(PLAN),vanilla_aliases=len(ALIASES),source_gaps=BLOCKED,jar_sha256=sha(jar),runtime='NOT_RUN',hearing='NOT_LISTENED',owner_accepted=False,poly_mc_installed=False)
 (P/'gates.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({k:v for k,v in report.items() if k!='source_gaps'},indent=2))
if __name__=='__main__':main()
