"""Scoped ordinary Volkolak dispatch/JAR verification. No Minecraft launch."""
from pathlib import Path
import sys,subprocess,json,hashlib,re,zipfile
R=Path(__file__).resolve().parents[1];sys.path[:0]=[str(R/'tools'),str(R/'.tools/audio-libs')]
import soundfile as sf
from volkolak_hunt_audio_61 import HUNT,VOLKOLAK,edit,registry_edit
BASE='c3a3a721'

def main():
 for path in [HUNT,VOLKOLAK,'src/main/java/org/slavicmyths/registry/ModSounds.java']:
  old=subprocess.check_output(['git','show',BASE+':'+path],cwd=R).decode('utf-8').replace('\r\n','\n')
  expected=registry_edit(old) if path.endswith('ModSounds.java') else edit(path,old)
  assert (R/path).read_text(encoding='utf-8')==expected,path
 # Preserve the separate cursed actor, its complete audio bank and every rule.
 assert not subprocess.check_output(['git','diff',BASE,'--','src/main/java/org/slavicmyths/volkolak','src/main/resources/assets/slavicmyths/sounds/volkolak','src/main/java/org/slavicmyths/hunt/HuntRules.java'],cwd=R).strip()
 rows=[r for r in json.loads((R/'art/audio/project-61/selection.json').read_text(encoding='utf-8')) if r['event'].startswith('volkolak_hunt_')]
 assert len(rows)==3 and {r['event'] for r in rows}=={'volkolak_hunt_rage','volkolak_hunt_howl'}
 pcm=[]
 for row in rows:
  x,rate=sf.read(R/'src/main/resources/assets/slavicmyths/sounds'/row['file']);assert x.ndim==1 and rate==44100
  assert len(x)/rate<1.4 and max(abs(x))<.8
  pcm.append(hashlib.sha256(x.tobytes()).hexdigest())
 assert len(set(pcm))==3,'Distinct recorded phrases, no renamed PCM copies'
 version=next(x.split('=',1)[1] for x in (R/'gradle.properties').read_text().splitlines() if x.startswith('mod_version='))
 jar=R/f'build/libs/slavicmyths-{version}.jar';javap=Path('C:/Program Files/Java/jdk-21.0.12/bin/javap.exe')
 def disasm(name):return subprocess.check_output([str(javap),'-c','-p','-classpath',str(jar),name],cwd=R).decode('utf-8',errors='replace')
 actor=disasm('org.slavicmyths.entity.VolkolakEntity')
 assert 'protected void huntVoice(boolean);' in actor and 'VOLKOLAK_HUNT_RAGE' in actor and 'VOLKOLAK_HUNT_HOWL' in actor
 assert 'RAVAGER_ROAR' not in actor
 hunt=disasm('org.slavicmyths.hunt.HuntMob')
 for signature in ['public void tick();','private void begin(']:
  body=hunt[hunt.index(signature):];body=re.split(r'\n  (?:public|private|protected) ',body,maxsplit=1)[0]
  assert 'huntVoice:(Z)V' in body and 'RAVAGER_ROAR' not in body,signature
 with zipfile.ZipFile(jar) as z:
  for path in [HUNT,VOLKOLAK]:
   name=path.removeprefix('src/main/java/').removesuffix('.java')+'.class'
   assert z.read(name)==(R/'build/classes/java/main'/name).read_bytes()
 report=dict(status='PASS_OFFLINE_OWNER_PENDING',baseline=BASE,version=version,events=2,recorded_variants=3,ordinary_override='COMPILED_JAR_VERIFIED',rage='actual dog growl',howl='actual FWS wolf howl',other_hunt_dispatch='PRESERVED_BY_EXACT_SOURCE_CONTRACT',cursed_volkolak='UNCHANGED',combat_rules='UNCHANGED',jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),runtime='NOT_RUN',hearing='NOT_LISTENED',owner_accepted=False)
 (R/'docs/audio-review/VOLKOLAK_HUNT_GATES.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(report,indent=2))

if __name__=='__main__':main()
