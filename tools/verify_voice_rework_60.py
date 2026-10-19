"""#60 approved timbre subset; honest source/audio/JAR gates, no runtime launch."""
from pathlib import Path
import json,hashlib,subprocess,sys,zipfile
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'));import soundfile as sf,numpy as np
P=R/'art/audio/pack-a-60/voice-rework';A=R/'src/main/resources/assets/slavicmyths';BASE='037f80ac';checks=0
def check(ok,why):
 global checks
 checks+=1
 if not ok:raise AssertionError(why)
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R).decode('utf-8').replace('\r\n','\n')
def main():
 # New delegated mappings are independently checked against exact source/JAR contracts.
 from verify_audio_groups_61 import main as verify_project
 verify_project()
 from audio_groups_61 import EVENTS as PROJECT_EVENTS
 from audio_groups_61_plan import NEW as PROJECT_NEW
 acceptance=json.loads((P/'acceptance.json').read_text(encoding='utf-8'));specs=json.loads((P/'clips.json').read_text(encoding='utf-8'));keys=set(acceptance['timbre_approved_keys']);check(keys=={'tugarin_challenge','skim_roar','tugarin_breath','tugarin_attack_voice'} and not acceptance['owner_accepted'],'Four specific heard-file approvals, no invented whole issue acceptance')
 before={p.relative_to(P).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in P.rglob('*.ogg')};subprocess.run([sys.executable,str(R/'tools/voice_rework_60.py')],check=True,cwd=R);check(before=={p.relative_to(P).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in P.rglob('*.ogg')},'All 8 takes and EQ alternative byte reproducible')
 hashes=[]
 for spec in specs:
  check(all(spec.get(k) for k in ['mob','role','action','phase','intent','source','callsite']),'Every clip has concrete label/action/phase')
  path=P/'preview'/(spec['key']+'.ogg');x,sr=sf.read(path);check(sf.info(path).subtype=='VORBIS' and sr==44100 and x.ndim==1 and np.isfinite(x).all(),'Valid mono44100 Vorbis')
  check(.1<len(x)/sr<6 and np.max(abs(x))<.95 and abs(x[0])<.02 and abs(x[-1])<.02,'No clipping/edge clicks');hashes.append(hashlib.sha256(x.tobytes()).hexdigest())
  if spec['key'] in keys:check(hashlib.sha256((P/acceptance['approved_files'][spec['key']]).read_bytes()).hexdigest()==acceptance['approved_sha256'][spec['key']],'Exact owner-heard selected take frozen')
 check(len(set(hashes))==8,'Eight distinct decoded clips')
 for key in keys:
  selected=P/acceptance['approved_files'][key];x,sr=sf.read(selected)
  check(sf.info(selected).subtype=='VORBIS' and sr==44100 and x.ndim==1 and np.isfinite(x).all() and np.max(abs(x))<.95,'Selected runtime voice full decode/peak '+key)
  if key=='tugarin_attack_voice':check(len(x)/sr<24/20,'Attack voice fits 24-tick audio-only cooldown')
 src,sr=sf.read(P/'preview/tugarin_challenge.ogg');alt,sr2=sf.read(P/'alternatives/tugarin_challenge_bass.ogg');check(len(src)==len(alt) and sr==sr2,'EQ does not change duration/sample rate')
 def ratio(x):
  z=abs(np.fft.rfft(x*np.hanning(len(x))))**2;f=np.fft.rfftfreq(len(x),1/sr);return z[(f>=80)&(f<=300)].sum()/z[(f>=1000)&(f<=4000)].sum()
 check(ratio(alt)>ratio(src)*1.03,'Alternative has stronger lower midrange, not just renamed original')
 for source in json.loads((P/'sources.json').read_text(encoding='utf-8')):check(hashlib.sha256((P/source['file']).read_bytes()).hexdigest()==source['sha256'] and source['license'] in ['CC-BY-4.0','CC-BY-3.0','CC0-1.0'],'Selected source hashes/licenses')
 previous=__import__('audio_resource_names_61').qualified(json.loads(old('src/main/resources/assets/slavicmyths/sounds.json')));events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));changed={k for k in previous if previous[k]!=events[k]};check(changed==({'tugarin_phase1_aggro','skim.roar','tugarin_phase1_idle','tugarin_phase1_attack','skim.rage'}|{'idolishche_poganoe.rage_01', 'grey_wolf.dash', 'ovinnik_angry', 'idolishche_poganoe.hurt_01', 'ovinnik_power', 'grey_wolf.pounce', 'skim.hurt_1', 'ovinnik_death', 'grey_wolf.hurt', 'idolishche_poganoe.idle_growl_02', 'idolishche_poganoe.death_01', 'skim.hurt_2', 'grey_wolf.claw', 'idolishche_poganoe.roar_01', 'idolishche_poganoe.hurt_02', 'idolishche_poganoe.idle_growl_01', 'ovinnik_ambient', 'ovinnik_hurt'}|{k for k in PROJECT_EVENTS & set(previous) if previous[k]!=events[k]}|(__import__('frost_foley_60').EVENTS|(__import__('kurgan_audio_65').EVENTS-__import__('kurgan_audio_65').NEW))) and set(events)==set(previous)|{'tugarin_spin_prepare'}|__import__('kurgan_audio_65').NEW|PROJECT_NEW,'Only explicitly scoped selected voice/foley events changed; existing IDs preserved')
 for k,v in previous.items():
  if not v.get('sounds') and k not in PROJECT_EVENTS:check(events[k]==v,'Every quarantined event still unchanged '+k)
 for event,file in [('tugarin_phase1_aggro','voice/tugarin/challenge'),('skim.roar','voice/skim/roar'),('tugarin_phase1_idle','voice/tugarin/breath'),('tugarin_phase1_attack','voice/tugarin/attack')]:check(events[event]['sounds'][0]['name']=='slavicmyths:'+file and (A/'sounds'/(file+'.ogg')).exists(),'Actual event resolves real asset')
 check((A/'sounds/voice/tugarin/challenge.ogg').read_bytes()==(P/acceptance['approved_files']['tugarin_challenge']).read_bytes() and (A/'sounds/voice/skim/roar.ogg').read_bytes()==(P/acceptance['approved_files']['skim_roar']).read_bytes(),'Runtime assets are exact selected accepted takes, not EQ or other six')
 for key,file in [('tugarin_breath','breath'),('tugarin_attack_voice','attack')]:check((A/f'sounds/voice/tugarin/{file}.ogg').read_bytes()==(P/acceptance['approved_files'][key]).read_bytes(),'Newly approved voice is exact heard file '+key)
 spin=json.loads((P/'alternatives/spin_prepare_selection.json').read_text(encoding='utf-8'));path=A/'sounds/voice/tugarin/spin_prepare.ogg';x,sr=sf.read(path)
 check(hashlib.sha256(path.read_bytes()).hexdigest()==spin['sha256'] and path.read_bytes()==(P/spin['file']).read_bytes(),'Selected separate preparation exhale source -> production')
 check(sr==44100 and x.ndim==1 and np.isfinite(x).all() and np.max(abs(x))<.95 and len(x)/sr<.8,'Preparation voice ends before first hit16')
 check(spin['status']=='AGENT_SELECTED_UNDER_OWNER_DELEGATION' and not spin['owner_approved'],'No invented owner approval for delegated selection')
 check(events['tugarin_spin_prepare']['sounds'][0]['name']=='slavicmyths:voice/tugarin/spin_prepare','New event resolves selected exhale')
 registry='src/main/java/org/slavicmyths/registry/ModSounds.java';marker='        map.put("tugarin_phase1_idle",sound("tugarin_phase1_idle"));';expected_registry=old(registry).replace(marker,'        map.put("tugarin_spin_prepare",sound("tugarin_spin_prepare"));\n'+marker)
 expected_registry=expected_registry.replace('    public static final DeferredHolder<SoundEvent,SoundEvent> UPYR_AMBIENT=sound("upyr_ambient");', '    public static final DeferredHolder<SoundEvent,SoundEvent> UPYR_AMBIENT=sound("upyr_ambient");\n'+''.join('    public static final DeferredHolder<SoundEvent,SoundEvent> '+field+'=sound("'+event+'");\n' for field,event in [('UPYR_IMPACT','upyr_impact'),('NAV_IMPACT','nav_impact'),('DRUZHINNIK_IMPACT','kurgan_druzhinnik_impact'),('VOEVODA_IMPACT','kurgan_voevoda_impact'),('VOLKHV_IMPACT','buried_volkhv_impact'),('PRINCE_IMPACT','unresting_prince_impact')]).rstrip('\n'))
 expected_registry=expected_registry.replace('public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_HURT','public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_REFUSE = sound("domovoy_refuse");\n    public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_THANKS = sound("domovoy_thanks");\n    public static final DeferredHolder<SoundEvent,SoundEvent> DOMOVOY_HURT').replace('"alert","roar","attack","impact"','"alert","roar","attack","impact","step"')
 expected_registry=__import__('bandit_audio_contract_61').registry_edit(expected_registry)
 check((R/registry).read_text(encoding='utf-8')==expected_registry,'Exact SPIN and six kurgan contact registrations; all existing IDs preserved')
 rage=json.loads((P/'alternatives/skim_rage_selection.json').read_text(encoding='utf-8'));path=A/'sounds/voice/skim/rage.ogg';x,sr=sf.read(path)
 check(hashlib.sha256(path.read_bytes()).hexdigest()==rage['sha256'] and path.read_bytes()==(P/rage['file']).read_bytes(),'Distinct selected Skim rage take -> JAR source')
 check(sr==44100 and x.ndim==1 and np.isfinite(x).all() and np.max(abs(x))<.95 and len(x)/sr/.96<64/20,'Rage voice fits RAGE64 even at minimum pitch')
 check(path.read_bytes()!=(A/'sounds/voice/skim/roar.ogg').read_bytes() and not rage['owner_approved'],'Rage not a renamed approved roar or invented owner approval')
 check(events['skim.rage']['sounds'][0]['name']=='slavicmyths:voice/skim/rage' and 'RAGE(64)' in (R/'src/main/java/org/slavicmyths/skim/SkimAction.java').read_text(),'Actual rage event and source action duration')
 path='src/main/java/org/slavicmyths/hunt/TugarinCombat.java';expected=old(path).replace('boss.playSound(ModSounds.boss("tugarin_phase1_aggro"),1,.7F);','boss.playSound(ModSounds.boss(boss.tugarinState()==TugarinState.PHASE_ONE?"tugarin_phase1_aggro":"tugarin.phase1.aggro"),1,boss.tugarinState()==TugarinState.PHASE_ONE?1:.7F);');expected=expected.replace('    private Move lastSpecial=Move.IDLE;','    private Move lastSpecial=Move.IDLE;\n    private long nextAttackVoiceTick;\n    private void attackVoice(float volume){long now=boss.level().getGameTime();if(now>=nextAttackVoiceTick){boss.playSound(ModSounds.boss("tugarin_phase1_attack"),volume,1);nextAttackVoiceTick=now+24;}}').replace('boss.playSound(ModSounds.boss("tugarin_phase1_attack"),.8F,1);','attackVoice(.8F);').replace('        boss.playSound(switch(move){case BREATH,SMOKE_BURST','        var cue=switch(move){case BREATH,SMOKE_BURST').replace('},.7F,move==Move.BREATH?.7F:.8F);','};if(boss.tugarinState()==TugarinState.PHASE_ONE&&cue==ModSounds.boss("tugarin_phase1_attack"))attackVoice(.7F);else boss.playSound(cue,.7F,move==Move.BREATH?.7F:.8F);');expected=expected.replace('if(move==Move.SPIN)boss.playSound(SoundEvents.RAVAGER_ROAR,.9F,.6F);','if(move==Move.SPIN){boss.playSound(ModSounds.boss("tugarin_spin_prepare"),.8F,1);return;}');check((R/path).read_text(encoding='utf-8')==expected,'Exact only audio routing; all Tugarin AI/timings/damage/NBT unchanged, serpent legacy alias preserved')
 path='src/main/java/org/slavicmyths/skim/SkimActor.java';expected=old(path).replace('public void cue(String k){if(!level().isClientSide)playSound(SkimContent.sound(k),.8F,.96F+random.nextFloat()*.08F);}','public void cue(String k){if(!level().isClientSide){float pitch=.96F+random.nextFloat()*.08F;playSound(SkimContent.sound(k),.8F,k.equals("skim.roar")?1:pitch);}}');check((R/path).read_text(encoding='utf-8')==expected,'Exact only Skim roar pitch1; same random draw/server call, other sounds/gameplay unchanged')
 for lang in ['ru_ru','en_us']:
  prev=json.loads(old(f'src/main/resources/assets/slavicmyths/lang/{lang}.json'));actual=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'));check(set(actual)-set(prev)=={'subtitles.slavicmyths.tugarin_challenge','subtitles.slavicmyths.tugarin_breath','subtitles.slavicmyths.tugarin_attack_voice','subtitles.slavicmyths.tugarin_spin_prepare'}|{'subtitles.slavicmyths.'+k for k in __import__('kurgan_audio_65').NEW}|{events[e]['subtitle'] for e in PROJECT_EVENTS if events[e].get('subtitle') and events[e]['subtitle'] not in prev} and all(actual[k]==v for k,v in prev.items() if k not in {events[e].get('subtitle') for e in PROJECT_EVENTS} and k not in {'subtitles.slavicmyths.ovinnik_ambient','subtitles.slavicmyths.kurgan_druzhinnik_ambient'}),'Only own added subtitle; existing translations unchanged')
 version=next(line.split('=',1)[1].strip() for line in (R/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'JAR CRC')
  for p in (R/'src/main/resources').rglob('*'):
   if p.is_file():
    k=p.relative_to(R/'src/main/resources').as_posix()
    if k!='META-INF/neoforge.mods.toml':check(z.read(k)==p.read_bytes(),'Production resource parity '+k)
  for name in ['hunt/TugarinCombat','skim/SkimActor']:
   k='org/slavicmyths/'+name+'.class';check(z.read(k)==(R/'build/classes/java/main'/k).read_bytes(),'Actual compiled callsite parity')
  check(not any('/verify/' in n or '/alternatives/' in n or 'voice-rework/preview' in n for n in z.namelist()),'No fixtures or unapproved preview tree in JAR')
  check('assets/slavicmyths/audio_licenses/voice-rework-60.txt' in z.namelist(),'CC attribution actually packaged')
 receipt=R/'docs/implementation/audio-60/POLYMC_INSTALLATION.json';install=json.loads((receipt if receipt.exists() else R/'docs/implementation/weapon-grip-58/POLYMC_INSTALLATION.json').read_text(encoding='utf-8-sig'));check(hashlib.sha256(Path(install['installed_jar']).read_bytes()).hexdigest()==install['sha256'],'Installed profile matches its latest authorized receipt')
 report=dict(status='IN_PROGRESS',checks=checks,voice_candidates=8,timbre_approved_keys=sorted(keys),production_selected_files=23,owner_approved_files=4,agent_selected_files=19,other_candidates_and_eq_not_wired=True,package_a_complete=False,owner_accepted=False,runtime_accepted=False,runtime_launches=0,poly_mc_updated_this_iteration=hashlib.sha256(jar.read_bytes()).hexdigest()==install['sha256'],jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest())
 (P/'gates.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
