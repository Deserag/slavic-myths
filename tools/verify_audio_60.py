"""#60 narrow foley/JAR gate. No server/client execution and no owner acceptance."""
from pathlib import Path
import sys,json,hashlib,subprocess,zipfile
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'));import soundfile as sf,numpy as np
BASE='956f4645';A=R/'src/main/resources/assets/slavicmyths';P=R/'art/audio/pack-a-60';checks=0
def check(ok,msg):
 global checks
 checks+=1
 if not ok:raise AssertionError(msg)
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R)
def main():
 path='src/main/java/org/slavicmyths/hunt/TugarinCombat.java';before=old(path).decode().replace('\r\n','\n');after=(R/path).read_text();expected=before.replace('boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP,1,.55F);','boss.playSound(ModSounds.boss(time==16?"tugarin_spin_blade_1":time==28?"tugarin_spin_blade_2":"tugarin_spin_blade_3"),.8F,1);',1);check(after==expected,'Exact only sound replacement; full AI/timings/damage unchanged')
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));previous=json.loads(old('src/main/resources/assets/slavicmyths/sounds.json'));new={f'tugarin_spin_blade_{n}' for n in range(1,4)};check(set(events)-set(previous)==new,'Only 3 new event IDs')
 for k,v in previous.items():check(events[k]==v,'Every prior sound mapping preserved '+k)
 sourcehash={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in [P/'preview'/f'tugarin_spin_{n}.ogg' for n in range(1,4)]};subprocess.run([sys.executable,str(R/'tools/audio_60_foley.py')],cwd=R,check=True)
 for p,h in sourcehash.items():check(hashlib.sha256(p.read_bytes()).hexdigest()==h,'Deterministic actual recording edit')
 for lang in ['ru_ru','en_us']:
  prev=json.loads(old(f'src/main/resources/assets/slavicmyths/lang/{lang}.json'));actual=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));check(set(actual)-set(prev)=={'subtitles.slavicmyths.tugarin_spin_blade'},'Only own subtitle');check(all(actual[k]==v for k,v in prev.items()),'All previous translations unchanged')
 pcm=[]
 for n in range(1,4):
  name=f'tugarin_spin_blade_{n}';p=A/f'sounds/boss/tugarin/foley/spin_{n}.ogg';check(p.read_bytes()==(P/f'preview/tugarin_spin_{n}.ogg').read_bytes(),'Actual playable candidate = production');x,sr=sf.read(p);check(sr==44100 and x.ndim==1 and np.isfinite(x).all() and max(abs(x))<.6,'Valid spatial mono Vorbis');pcm.append(hashlib.sha256(x.tobytes()).hexdigest());check(events[name]['sounds'][0]['name']==f'boss/tugarin/foley/spin_{n}','Real event to actual file')
 check(len(set(pcm))==3,'3 separately recorded takes, not renamed/pitch variants')
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'JAR CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    k=p.relative_to(R/'src/main/resources').as_posix()
    if k!='META-INF/neoforge.mods.toml':check(z.read(k)==p.read_bytes(),'JAR resource parity '+k)
  for name in ['hunt/TugarinCombat','registry/ModSounds']:
   k='org/slavicmyths/'+name+'.class';check(z.read(k)==(R/'build/classes/java/main'/k).read_bytes(),'Actual compiled callsite/registry parity')
  check(not any('/verify/' in n for n in z.namelist()),'No fixtures in JAR')
 install=json.loads((R/'docs/implementation/weapon-grip-58/POLYMC_INSTALLATION.json').read_text());check(hashlib.sha256(Path(install['installed_jar']).read_bytes()).hexdigest()==install['sha256'],'Earlier owner requested model JAR still installed; new audio not silently delivered')
 report=dict(status='IN_PROGRESS',checks=checks,production_foley_events=3,preview_clips=8,game_launches=0,owner_accepted=False,package_a_complete=False,remaining='Ovinnik voice, Tugarin voice/step/transform, wolf distinct reactions, undead/Skim/Idolishche categories not completed',jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),installed_model_source=install['source_commit'],installed_model_sha256=install['sha256'],new_audio_installed=False)
 out=R/'docs/implementation/audio-60';out.mkdir(exist_ok=True);(out/'gates.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
