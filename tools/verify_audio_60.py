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
 path='src/main/java/org/slavicmyths/hunt/TugarinCombat.java';before=old(path).decode().replace('\r\n','\n');after=(R/path).read_text();expected=before.replace('boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP,1,.55F);','boss.playSound(ModSounds.boss(time==16?"tugarin_spin_blade_1":time==28?"tugarin_spin_blade_2":"tugarin_spin_blade_3"),.8F,1);',1);expected=expected.replace('boss.playSound(ModSounds.boss("tugarin_phase1_aggro"),1,.7F);','boss.playSound(ModSounds.boss(boss.tugarinState()==TugarinState.PHASE_ONE?"tugarin_phase1_aggro":"tugarin.phase1.aggro"),1,boss.tugarinState()==TugarinState.PHASE_ONE?1:.7F);');expected=expected.replace('    private Move lastSpecial=Move.IDLE;','    private Move lastSpecial=Move.IDLE;\n    private long nextAttackVoiceTick;\n    private void attackVoice(float volume){long now=boss.level().getGameTime();if(now>=nextAttackVoiceTick){boss.playSound(ModSounds.boss("tugarin_phase1_attack"),volume,1);nextAttackVoiceTick=now+24;}}').replace('boss.playSound(ModSounds.boss("tugarin_phase1_attack"),.8F,1);','attackVoice(.8F);').replace('        boss.playSound(switch(move){case BREATH,SMOKE_BURST','        var cue=switch(move){case BREATH,SMOKE_BURST').replace('},.7F,move==Move.BREATH?.7F:.8F);','};if(boss.tugarinState()==TugarinState.PHASE_ONE&&cue==ModSounds.boss("tugarin_phase1_attack"))attackVoice(.7F);else boss.playSound(cue,.7F,move==Move.BREATH?.7F:.8F);');expected=expected.replace('if(move==Move.SPIN)boss.playSound(SoundEvents.RAVAGER_ROAR,.9F,.6F);','if(move==Move.SPIN){boss.playSound(ModSounds.boss("tugarin_spin_prepare"),.8F,1);return;}');check(after==expected,'Exact only approved audio replacements; full AI/timings/damage unchanged')
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));previous=json.loads(old('src/main/resources/assets/slavicmyths/sounds.json'));new={f'tugarin_spin_blade_{n}' for n in range(1,4)}|{'tugarin_spin_prepare'};check(set(events)-set(previous)==new,'Only 3 new event IDs')
 for k,v in previous.items():
  if k not in ({'tugarin_phase1_aggro','skim.roar','tugarin_phase1_idle','tugarin_phase1_attack','skim.rage'}|{'idolishche_poganoe.rage_01', 'grey_wolf.dash', 'ovinnik_angry', 'idolishche_poganoe.hurt_01', 'ovinnik_power', 'grey_wolf.pounce', 'skim.hurt_1', 'ovinnik_death', 'grey_wolf.hurt', 'idolishche_poganoe.idle_growl_02', 'idolishche_poganoe.death_01', 'skim.hurt_2', 'grey_wolf.claw', 'idolishche_poganoe.roar_01', 'idolishche_poganoe.hurt_02', 'idolishche_poganoe.idle_growl_01', 'ovinnik_ambient', 'ovinnik_hurt'}|__import__('frost_foley_60').EVENTS):check(events[k]==v,'Every unrelated prior sound mapping preserved '+k)
 sourcehash={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in [P/'preview'/f'tugarin_spin_{n}.ogg' for n in range(1,4)]};subprocess.run([sys.executable,str(R/'tools/audio_60_foley.py')],cwd=R,check=True)
 for p,h in sourcehash.items():check(hashlib.sha256(p.read_bytes()).hexdigest()==h,'Deterministic actual recording edit')
 for lang in ['ru_ru','en_us']:
  prev=json.loads(old(f'src/main/resources/assets/slavicmyths/lang/{lang}.json'));actual=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));check(set(actual)-set(prev)=={'subtitles.slavicmyths.tugarin_spin_blade','subtitles.slavicmyths.tugarin_challenge','subtitles.slavicmyths.tugarin_breath','subtitles.slavicmyths.tugarin_attack_voice','subtitles.slavicmyths.tugarin_spin_prepare'},'Only own subtitles');check(all(actual[k]==v for k,v in prev.items() if k!='subtitles.slavicmyths.ovinnik_ambient'),'All previous translations unchanged')
 pcm=[]
 for n in range(1,4):
  name=f'tugarin_spin_blade_{n}';p=A/f'sounds/boss/tugarin/foley/spin_{n}.ogg';check(p.read_bytes()==(P/f'preview/tugarin_spin_{n}.ogg').read_bytes(),'Actual playable candidate = production');x,sr=sf.read(p);check(sr==44100 and x.ndim==1 and np.isfinite(x).all() and max(abs(x))<.6,'Valid spatial mono Vorbis');pcm.append(hashlib.sha256(x.tobytes()).hexdigest());check(events[name]['sounds'][0]['name']==f'boss/tugarin/foley/spin_{n}','Real event to actual file')
 check(len(set(pcm))==3,'3 separately recorded takes, not renamed/pitch variants')
 version=next(line.split('=',1)[1].strip() for line in (R/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'JAR CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    k=p.relative_to(R/'src/main/resources').as_posix()
    if k!='META-INF/neoforge.mods.toml':check(z.read(k)==p.read_bytes(),'JAR resource parity '+k)
  for name in ['hunt/TugarinCombat','registry/ModSounds']:
   k='org/slavicmyths/'+name+'.class';check(z.read(k)==(R/'build/classes/java/main'/k).read_bytes(),'Actual compiled callsite/registry parity')
  check(not any('/verify/' in n for n in z.namelist()),'No fixtures in JAR')
 receipt=R/'docs/implementation/audio-60/POLYMC_INSTALLATION.json';install=json.loads((receipt if receipt.exists() else R/'docs/implementation/weapon-grip-58/POLYMC_INSTALLATION.json').read_text(encoding='utf-8-sig'));check(hashlib.sha256(Path(install['installed_jar']).read_bytes()).hexdigest()==install['sha256'],'Installed JAR matches latest authorized receipt')
 report=dict(status='IN_PROGRESS',checks=checks,production_foley_events=3,preview_clips=8,game_launches=0,owner_accepted=False,package_a_complete=False,remaining='Kurgan attack/impact; remaining Tugarin phase/transform and Skim special cues; owner in-game hearing pending',jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),installed_model_source=install['source_commit'],installed_model_sha256=install['sha256'],new_audio_installed=hashlib.sha256(jar.read_bytes()).hexdigest()==install['sha256'])
 out=R/'docs/implementation/audio-60';out.mkdir(exist_ok=True);(out/'gates.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
