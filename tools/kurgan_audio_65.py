"""Complete six-kind kurgan sound policy, recorded voices/props, no synth fallback."""
from pathlib import Path
import sys,json,hashlib,subprocess
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np
import soundfile as sf
from audio_60_foley import encode,stable_text,RATE
A=R/'src/main/resources/assets/slavicmyths';P=R/'art/audio/kurgan-65'
KINDS=['upyr','nav','kurgan_druzhinnik','kurgan_voevoda','buried_volkhv','unresting_prince']
# Each list selects distinct original performances/prop takes. No pitch reskins.
PLAN={
 'upyr_ambient':['zombieYell1.wav','zombieYell2.wav'],
 'upyr_hurt':['zombieYell3.wav','zombieYell4.wav'],'upyr_death':['zombieDeath1.wav'],
 'upyr_attack':['quiver-leather-squeeze-01.wav','quiver-leather-squeeze-02.wav'],
 'upyr_leap':['boots-leather-jump-01.wav'],'upyr_bite':['quiver-leather-squeeze-03.wav'],
 'upyr_step':['boots-leather-step-01.wav','boots-leather-step-02.wav'],
 'nav_attack':['quiver-leather-squeeze-04.wav'],'nav_shift':['quiver-leather-squeeze-05.wav'],
 'nav_spectral_appear':['quiver-leather-squeeze-06.wav'],'nav_step':['quiver-leather-squeeze-07.wav','quiver-leather-squeeze-08.wav'],
 'kurgan_druzhinnik_ambient':['bone-0.ogg','bone-1.ogg'],'kurgan_druzhinnik_hurt':['bone-2.ogg','bone-3.ogg'],
 'kurgan_druzhinnik_death':['bone-4.ogg','bone-5.ogg'],
 'kurgan_druzhinnik_sword_attack':['sword.1.ogg','sword.2.ogg'],
 'kurgan_druzhinnik_shield_block':['sword_clash.1.ogg','sword_clash.2.ogg'],
 'kurgan_druzhinnik_shield_bash':['boots-leather-jump-02.wav'],
 'kurgan_druzhinnik_step':['belt-buckle-01.wav','belt-buckle-02.wav'],
 'kurgan_voevoda_ambient':['zombieYell5.wav'],'kurgan_voevoda_hurt':['zombieYell6.wav','zombieYell7.wav'],
 'kurgan_voevoda_death':['zombieDeath2.wav'],
 'kurgan_voevoda_axe_attack':['sword.3.ogg','sword.4.ogg'],
 'kurgan_voevoda_shield_charge':['boots-leather-jump-03.wav'],
 'kurgan_voevoda_shield_impact':['sword_clash.3.ogg','sword_clash.4.ogg'],
 'kurgan_voevoda_phase_shift':['sword-table-leg-scrape-01.wav'],
 'kurgan_voevoda_step':['boots-leather-step-03.wav','boots-leather-step-04.wav'],
 'buried_volkhv_cast_basic':['bone-6.ogg','bone-7.ogg'],
 'buried_volkhv_cast_clones':['quiver-leather-squeeze-09.wav'],
 'buried_volkhv_cast_ground_seal':['sword-table-leg-scrape-02.wav'],
 'buried_volkhv_summon':['bone-8.ogg'],'buried_volkhv_step':['bone-9.ogg'],
 'unresting_prince_ambient':['zombieYell8.wav'],
 'unresting_prince_hurt':['zombieYell9.wav','zombieYell10.wav'],
 'unresting_prince_death':['zombieDeath3.wav'],
 'unresting_prince_combo':['sword.5.ogg','sword.6.ogg'],
 'unresting_prince_summon':['sword-table-leg-scrape-03.wav'],
 'unresting_prince_phase_shift':['sword-table-leg-scrape-04.wav'],
 'unresting_prince_heavy_strike':['sword.7.ogg','sword.8.ogg'],
 'unresting_prince_grab':['sword-thermos-hit-01.wav'],
 'unresting_prince_shield_block':['sword_clash.5.ogg','sword_clash.6.ogg'],
 # Intentional common equipment step props, not shared character voice.
 'unresting_prince_step':['belt-buckle-03.wav','belt-buckle-04.wav'],
 'upyr_impact':['body-1.flac','body-2.flac'],'nav_impact':['body-3.flac'],
 'kurgan_druzhinnik_impact':['sword_clash.9.ogg'],'kurgan_voevoda_impact':['sword_clash.10.ogg'],
 'buried_volkhv_impact':['sword.9.ogg'],'unresting_prince_impact':['sword.10.ogg'],
}
KEEP={k+'_'+cue:'KEEP distinct existing '+('phantom ghost' if k=='nav' else 'evoker caster')+' vocal; owner hearing pending, not an approval' for k in ['nav','buried_volkhv'] for cue in ['ambient','hurt','death']}
NEW={k+'_impact' for k in KINDS}
EVENTS=set(PLAN)

def role(event):
 if event.endswith('impact'):return 'contact / successful damage or bolt collision'
 if event.endswith('ambient'):return 'idle, vanilla ambient scheduler'
 if event.endswith('hurt'):return 'hurt callback'
 if event.endswith('death'):return 'death callback'
 if event.endswith('step'):return 'footstep callback'
 if event.endswith('phase_shift'):return 'health threshold phase transition'
 if 'shield_block' in event or 'shield_impact' in event:return 'frontal blocking damage callback'
 if event.endswith('spectral_appear'):return 'SHIFT release, windup8'
 return 'attack begin telegraph, see move table'

def restore_selected():
 sources={Path(s['file']).name:s for s in json.loads((P/'sources.json').read_text(encoding='utf-8'))}
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));rows=[]
 for event,members in PLAN.items():
  suffix=next(event[len(k)+1:] for k in sorted(KINDS,key=len,reverse=True) if event.startswith(k+'_'))
  # Basic tells finish before shortest windup5 ticks, at lowest source pitch.9.
  cap=.20 if suffix in ['attack','sword_attack','axe_attack','combo'] else .24 if suffix=='impact' else .28 if suffix=='step' else .5 if suffix=='hurt' else .8 if suffix in ['death','phase_shift'] else .42
  target=.045 if suffix=='ambient' else .045 if suffix=='step' else .10 if suffix=='hurt' else .115 if suffix=='death' else .09
  values=[]
  for index,member in enumerate(members,1):
   spec=sources[member];source=P/spec['file'];assert hashlib.sha256(source.read_bytes()).hexdigest()==spec['sha256']
   raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-af','highpass=f=80,lowpass=f=9000','-f','f32le','-ar',str(RATE),'-ac','1','pipe:1'])
   x=np.frombuffer(raw,dtype='<f4').astype(float).copy();x-=x.mean();active=np.flatnonzero(abs(x)>max(abs(x))*.025);assert len(active)
   start=max(0,int(active[0])-220);x=x[start:start+int(cap*RATE)];f=min(220,len(x)//8);x[:f]*=np.linspace(0,1,f);x[-f:]*=np.linspace(1,0,f);x*=min(target/max(np.sqrt(np.mean(x*x)),1e-12),.6/max(abs(x)))
   x=np.pad(x,(220,220)) # Guard against Vorbis pre-echo at the first packet edge.
   name=f'kurgan/recorded/{event}_{index}';dest=A/'sounds'/(name+'.ogg');dest.parent.mkdir(parents=True,exist_ok=True);encode(dest,x);y,sr=sf.read(dest)
   values.append({'name':name,'attenuation_distance':20 if suffix in ['ambient','step'] else 28})
   rows.append(dict(event=event,file=name+'.ogg',sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),source=spec['file'],source_sha256=spec['sha256'],seconds=len(y)/sr,peak_dbfs=float(20*np.log10(max(abs(y)))),rms_dbfs=float(20*np.log10(np.sqrt(np.mean(y*y)))),trim_start_samples=start,role=role(event),layer='performed zombie voice' if member.startswith('zombie') else 'recorded shells/wood/leather/steel prop; not vocal',processing='mono44100; highpass80/lowpass9000; trim; cap;5ms fades;5ms zero guards; gain; no pitch shift',status='AGENT_SELECTED_OWNER_PENDING',owner_approved=False,hearing='NOT_LISTENED'))
  subtitle=events[event]['subtitle'] if event in events else 'subtitles.slavicmyths.'+event
  events[event]={'subtitle':subtitle,'sounds':values}
 stable_text(A/'sounds.json',json.dumps(events,ensure_ascii=False,indent=2)+'\n')
 for lang,label in [('ru_ru','удар достигает цели'),('en_us','attack makes contact')]:
  path=A/f'lang/{lang}.json';text=json.loads(path.read_text(encoding='utf-8'))
  for kind in KINDS:text['subtitles.slavicmyths.'+kind+'_impact']=text.get('entity.slavicmyths.'+kind,kind)+': '+label
  # Druzhinnik is skeletal: label prop bones honestly, not a vocal groan.
  text['subtitles.slavicmyths.kurgan_druzhinnik_ambient']='Дружинник: кости потрескивают' if lang=='ru_ru' else 'Buried warrior: bones rattle'
  stable_text(path,json.dumps(text,ensure_ascii=False,indent=2)+'\n')
 stable_text(P/'selection.json',json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
 stable_text(P/'kept.json',json.dumps(KEEP,ensure_ascii=False,indent=2)+'\n')
 credits='Recorded kurgan audio. ArcadeParty — Zombie / Skeleton / Monster Voice Effects, https://opengameart.org/content/zombie-skeleton-monster-voice-effects . StarNinjas — https://opengameart.org/content/20-sword-sound-effects-attacks-and-clashes . Vehicle — https://opengameart.org/content/fantasy-weapons-and-apparel-sfx-library . congusbongus / blukotek — https://opengameart.org/content/bones-rattle ; upstream https://freesound.org/people/blukotek/sounds/249319/ (recorded shells, not actual bones). All CC0-1.0 https://creativecommons.org/publicdomain/zero/1.0/ . Edits: mono/resample, EQ, crop, fade, gain; no pitch shift. Some weapon recordings are knife/household props, not authentic historical weapons. No endorsement. Exact source/archive/member hashes: art/audio/kurgan-65/sources.json; selection.json. Owner acceptance pending.\n'
 credits+='qubodup — wet towel/body impact props, https://opengameart.org/content/40-wet-towel-clubpoundhitattack-sounds , CC0-1.0; author page explicitly supersedes older archive text license note.\n'
 stable_text(A/'audio_licenses/kurgan-65.txt',credits)
 return rows

if __name__=='__main__':
 rows=restore_selected();print(f'{len(rows)} recorded variants; {len(PLAN)} replaced/new events; {len(KEEP)} retained distinct vanilla voice events. OWNER_PENDING.')
