"""Exact audio-only source contract, recordings, complete event policy and JAR."""
from pathlib import Path
import json,sys,hashlib,subprocess,zipfile
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np,soundfile as sf
from kurgan_audio_65 import restore_selected,A,P,PLAN,KEEP,NEW,KINDS
BASE='5c93408e';checks=[]
def check(ok,message):
 if not ok:raise AssertionError(message)
 checks.append(message)
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R).decode('utf-8').replace('\r\n','\n')
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 # New delegated mappings are independently checked against exact source/JAR contracts.
 from verify_audio_groups_61 import main as verify_project
 verify_project()
 from audio_groups_61 import EVENTS as PROJECT_EVENTS
 from audio_groups_61_plan import NEW as PROJECT_NEW
 rows=json.loads((P/'selection.json').read_text(encoding='utf-8'));before={r['file']:sha(A/'sounds'/r['file']) for r in rows};maps=(A/'sounds.json').read_bytes();restore_selected()
 check(maps==(A/'sounds.json').read_bytes() and all(sha(A/'sounds'/name)==h for name,h in before.items()),'Selected recordings and maps byte-idempotent')
 baseline=__import__('audio_resource_names_61').qualified(json.loads(old('src/main/resources/assets/slavicmyths/sounds.json')));events=json.loads(maps)
 family={k for k in baseline if any(k.startswith(kind+'_') for kind in KINDS)}
 check(set(PLAN)|set(KEEP)==family|NEW and set(PLAN)&set(KEEP)==set(),'All 47 original family events accounted:41 changed,6 KEEP,6 new contact events')
 check(set(events)==set(baseline)|NEW|PROJECT_NEW,'Only six audio event IDs added; no entity/item registry changes')
 for event,definition in baseline.items():
  if event not in PLAN and event not in PROJECT_EVENTS:check(events[event]==definition,'Unrelated / KEEP / quarantined mapping unchanged '+event)
 sources={r['file']:r for r in json.loads((P/'sources.json').read_text(encoding='utf-8'))};decoded=[]
 for row in rows:
  p=A/'sounds'/row['file'];x,sr=sf.read(p);check(sf.info(p).subtype=='VORBIS' and sr==44100 and x.ndim==1 and np.isfinite(x).all(),'Spatial Vorbis full decode '+row['file'])
  check(.025<len(x)/sr<.82 and max(abs(x))<.8 and abs(x[0])<.02 and abs(x[-1])<.02,'Duration / peak / faded edges '+row['file'])
  check(sha(p)==row['sha256'] and sha(P/row['source'])==row['source_sha256']==sources[row['source']]['sha256'],'Exact source and output SHA '+row['file'])
  check(sources[row['source']]['license']=='CC0-1.0' and sources[row['source']]['verified_date']=='2026-10-19','Source redistribution/derivative license '+row['source'])
  if row['event'].endswith(('_attack','_sword_attack','_axe_attack','_combo')):check(len(x)/sr/.9<5/20,'Short tell completes by earliest windup5 at lowest pitch.9')
  decoded.append(hashlib.sha256(x.tobytes()).hexdigest())
 check(len(decoded)==69 and len(set(decoded))==69,'69 distinct decoded clips, not byte/PCM duplicates')
 check(len({r['source'] for r in rows})==69,'69 separate source takes, no pitch clones')
 path='src/main/java/org/slavicmyths/kurgan/KurganCreature.java';expected=old(path)
 expected=expected.replace('    private int actionTick,basicWait,blocks,memory,cloneTime,alignWait;private boolean summoned,riposte,dashHit;','    private int actionTick,basicWait,blocks,memory,cloneTime,alignWait;private boolean summoned,riposte,dashHit;\n    private long nextImpactSoundTick;')
 expected=expected.replace('if(bash)target.knockback(.9F, -aim.x,-aim.z);return hit;', 'if(bash)target.knockback(.9F, -aim.x,-aim.z);if(hit&&level().getGameTime()>=nextImpactSoundTick){playSound(sound("impact"),.65F,1);nextImpactSoundTick=level().getGameTime()+2;}return hit;')
 expected=expected.replace('            case HEAVY:if(strike(target,damage+4,3.8,.65,true))cue("heavy_strike");break;','            // Contact cue now lives in strike; retain the former cue\'s pitch RNG draw.\n            case HEAVY:if(strike(target,damage+4,3.8,.65,true))random.nextFloat();break;')
 check((R/path).read_text(encoding='utf-8')==expected,'Full KurganCreature exact audio-only diff: unchanged damage, moves, timers, NBT, RNG draws;2tick audio anti-stack')
 path='src/main/java/org/slavicmyths/kurgan/KurganBolt.java';expected=old(path).replace('),6);discard();}}','),6);impactSound();discard();}}').replace('if(!level().isClientSide)discard();}', 'if(!level().isClientSide){impactSound();discard();}}\n    private void impactSound(){playSound(org.slavicmyths.registry.ModSounds.VOLKHV_IMPACT.get(),.55F,1);}')
 check((R/path).read_text(encoding='utf-8')==expected,'Projectile exact added collision audio only; damage6/lifetime50/ownership preserved')
 for path in ['src/main/java/org/slavicmyths/kurgan/KurganFighter.java','src/main/java/org/slavicmyths/registry/ModEntities.java','src/main/java/org/slavicmyths/kuzya/KuzyaEntity.java']:
  check((R/path).read_text(encoding='utf-8')==old(path),'Fixed combat/registry/protected Kuzya source unchanged '+path)
 sound_source=(R/'src/main/java/org/slavicmyths/registry/ModSounds.java').read_text(encoding='utf-8')
 for event in NEW:check('sound("'+event+'")' in sound_source,'Real sound registry '+event)
 for lang in ['ru_ru','en_us']:
  actual=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));previous=json.loads(old(f'src/main/resources/assets/slavicmyths/lang/{lang}.json'))
  check(set(actual)-set(previous)=={'subtitles.slavicmyths.'+e for e in NEW}|{events[e]['subtitle'] for e in PROJECT_EVENTS if events[e].get('subtitle') and events[e]['subtitle'] not in previous},'Only scoped new subtitle keys '+lang)
  check(all(actual[k]==v for k,v in previous.items() if k!='subtitles.slavicmyths.kurgan_druzhinnik_ambient' and k not in {events[e].get('subtitle') for e in PROJECT_EVENTS}),'Only intended skeleton ambient label changed '+lang)
  for event in set(PLAN)|set(KEEP):check(events[event]['subtitle'] in actual,'Actual subtitle '+lang+' '+event)
 version=next(s.split('=',1)[1].strip() for s in (R/'gradle.properties').read_text().splitlines() if s.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production JAR CRC')
  check(z.read('assets/slavicmyths/sounds.json')==maps,'Final actual event map parity')
  for row in rows:check(z.read('assets/slavicmyths/sounds/'+row['file'])==(A/'sounds'/row['file']).read_bytes(),'Packaged exact selected variant '+row['file'])
  for cls in ['kurgan/KurganCreature','kurgan/KurganBolt','registry/ModSounds']:
   entry='org/slavicmyths/'+cls+'.class';check(z.read(entry)==(R/'build/classes/java/main'/entry).read_bytes(),'Compiled real dispatch parity '+cls)
  check(z.read('assets/slavicmyths/audio_licenses/kurgan-65.txt')==(A/'audio_licenses/kurgan-65.txt').read_bytes(),'Source attribution packaged')
  check(not any(n.startswith('art/') or n.startswith('tools/smoke/') for n in z.namelist()),'Review sources / test fixtures excluded from runtime JAR')
 receipt=json.loads((R/'docs/implementation/audio-60/POLYMC_INSTALLATION.json').read_text());check(sha(Path(receipt['installed_jar']))==receipt['sha256'],'Previously installed PolyMC JAR preserved')
 report=dict(status='IN_PROGRESS_OWNER_PENDING',source_base=BASE,checks=len(checks),new_files=len(rows),existing_events_changed=41,existing_events_kept=6,new_contact_events=6,covered_types=KINDS,jar_sha256=sha(jar),runtime_launches=0,poly_mc_installed=False,hearing='NOT_LISTENED',owner_accepted=False,results=checks)
 (P/'gates.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({k:v for k,v in report.items() if k!='results'},indent=2))
if __name__=='__main__':main()
