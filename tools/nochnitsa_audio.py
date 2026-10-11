"""Original seeded formant/breath DSP. No recordings, third-party voices or borrowed cues."""
from pathlib import Path
import numpy as np
import json,csv,subprocess,wave,hashlib
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=A/'sounds/nochnitsa';O.mkdir(parents=True,exist_ok=True);tmp=R/'work/nochnitsa/audio';tmp.mkdir(parents=True,exist_ok=True)
spec=R/'work/nochnitsa-spec/Slavic_Myths_1.6.2_Nochnitsa_Codex_TZ/manifests/sounds.csv'
rows=list(csv.DictReader(spec.open(encoding='utf-8-sig')));sounds=json.loads((A/'sounds.json').read_text(encoding='utf8'));manifest=[]
def filtered(signal,sr,centers,widths):
 f=np.fft.rfftfreq(len(signal),1/sr);response=sum(np.exp(-.5*((f-c)/w)**2) for c,w in zip(centers,widths));return np.fft.irfft(np.fft.rfft(signal)*response,n=len(signal))
for idx,row in enumerate(rows):
 file=Path(row['file_expected']).name;name=row['sound_event_id'].split(':')[1];kind=name.split('.')[1];variant=int(file[-6:-4]);rng=np.random.default_rng(16400+idx);sr=44100;duration=float(row['duration_min_s'])+(float(row['duration_max_s'])-float(row['duration_min_s']))*(.32+(variant-1)*.15);n=int(sr*duration);t=np.arange(n)/sr;u=t/duration
 envelope=np.minimum(1,t/.035)*np.minimum(1,(duration-t)/.09);noise=rng.normal(0,1,n);air=filtered(noise,sr,[320,1350,3200],[170,500,750]);air/=max(abs(air))
 f0=165+variant*14+7*np.sin(t*2*np.pi*6.3)+4*np.sin(t*2*np.pi*19)
 if kind=='scream_release':f0+=(130*np.minimum(1,t/.2)-50*u);envelope*=np.minimum(1,t/.17)*np.maximum(0,1-u)**.45
 elif kind in ['death','sunrise_dissolve']:f0*=1-.35*u
 elif kind=='scream_inhale':f0*=.65+.4*u;envelope*=u**.45
 phase=2*np.pi*np.cumsum(f0)/sr;glottal=sum(np.sin(phase*h)/h for h in range(1,20));centers=[510+variant*25,1330+variant*35,2510-variant*45];voice=filtered(glottal+.18*noise,sr,centers,[100,180,250]);voice/=max(abs(voice));voice*=.72+.22*np.sin(t*2*np.pi*(9+variant))
 drone=.11*np.sin(phase*.7)+.055*np.sin(phase*.705+.7);cloth=filtered(noise,sr,[1800,4900],[900,1400]);cloth/=max(abs(cloth))
 if kind=='step':signal=(air*.5+cloth*.4)*np.exp(-u*7)+.17*np.sin(2*np.pi*(80+variant*9)*t)*np.exp(-u*18)
 elif kind=='robes_rustle':signal=cloth*(.5+.5*np.sin(t*2*np.pi*(6+variant)))+air*.2;envelope*=np.sin(np.pi*u)**.7
 elif kind=='claw_swing':signal=cloth*np.exp(-((u-.45)/.25)**2)+.2*air
 elif kind in ['breath','scream_inhale']:signal=air*.8+voice*.13+drone*.08;envelope*=np.sin(np.pi*u)**.8
 elif kind=='ambient':signal=voice*.34+air*.55+drone;envelope*=np.sin(np.pi*u)**1.2
 elif kind=='scream_release':signal=voice+air*.18+drone*.4
 elif kind=='scream_interrupt':signal=voice*.7*(1-u)+air*.45+cloth*.2;envelope*=np.sin(np.pi*u)**.6
 elif kind=='sunrise_dissolve':signal=air*.65+voice*.22+cloth*.15;envelope*=np.sin(np.pi*u)**.7
 elif kind=='death':signal=voice*.6+air*.4+drone*.2;envelope*=np.sin(np.pi*u)**.6
 else:signal=voice*.65+air*.3+cloth*.1;envelope*=np.sin(np.pi*u)**.7
 signal*=envelope;signal-=signal.mean();signal/=max(abs(signal));gain=.85*(.25 if kind=='ambient' else .50 if kind in ['step','robes_rustle','breath'] else 1);signal*=gain
 wav=tmp/(file[:-4]+'.wav');pcm=(signal*32767).astype('<i2')
 with wave.open(str(wav),'wb') as w:w.setparams((1,2,sr,n,'NONE','not compressed'));w.writeframes(pcm.tobytes())
 ogg=O/file;subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(ogg)],check=True)
 radius={'ambient':16,'breath':14,'step':12,'claw_swing':16,'hurt':16,'scream_inhale':24,'scream_release':32,'scream_interrupt':20,'target_spotted':20,'sunrise_dissolve':20,'death':20,'robes_rustle':10}[kind]
 if not any(x['event']==name for x in manifest):sounds[name]={'subtitle':'subtitles.slavicmyths.'+name,'sounds':[]}
 sounds[name]['sounds'].append({'name':'slavicmyths:nochnitsa/'+file[:-4],'attenuation_distance':radius})
 manifest.append({'event':name,'file':str(ogg.relative_to(A)),'duration':duration,'min':float(row['duration_min_s']),'max':float(row['duration_max_s']),'sample_rate':sr,'channels':1,'sha256':hashlib.sha256(ogg.read_bytes()).hexdigest(),'source':'Original seeded nonlinguistic formant/noise DSP, tools/nochnitsa_audio.py'})
(A/'sounds.json').write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf8');p=R/'docs/nochnitsa/audio-manifest.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(manifest,indent=2)+'\n');print(len(manifest),'original mono Vorbis variants')

