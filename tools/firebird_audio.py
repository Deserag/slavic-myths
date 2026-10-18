"""Original avian formant synthesis, modulated trills, filtered feather noise and chimes."""

# Rejected synthetic/formant creature vocals. Generate new OGG only from owner-approved sources.
raise RuntimeError("REJECTED_SYNTHETIC_VOICE (#60): legacy audio generator disabled pending owner acceptance")

from pathlib import Path
import math, random, wave, array, subprocess, json, hashlib
ROOT=Path(__file__).resolve().parents[1];assets=ROOT/'src/main/resources/assets/slavicmyths';out=assets/'sounds/firebird';out.mkdir(parents=True,exist_ok=True)
counts=[3,2,3,2,2,2,2,2,3,3,2,2];durations=[1.4,2.1,.55,1.3,1.7,.65,1.1,.85,.6,.85,1.85,2.1]
names=['ambient','call','wing_flap','takeoff','glide','perch','warning','retaliation','hurt','drop','depart','death']
sounds=json.loads((assets/'sounds.json').read_text(encoding='utf8'));manifest=[];tmp=ROOT/'work/firebird/audio';tmp.mkdir(parents=True,exist_ok=True)
for k,(name,count,duration) in enumerate(zip(names,counts,durations)):
 entries=[]
 for v in range(count):
  sr=44100;length=duration*(.96+v*.04);rng=random.Random(16100+k*10+v);values=[];phase=0;low=0
  for i in range(int(length*sr)):
   t=i/sr;u=t/length;envelope=math.sin(math.pi*u)**1.5;white=rng.uniform(-1,1);low=low*.93+white*.07;noise=white-low
   freq=(330+v*29)*(1+.16*math.sin(t*7)+.05*math.sin(t*38));phase+=math.tau*freq/sr
   vocal=sum(math.sin(phase*h+math.sin(t*(24+h))*.6)/h**1.35 for h in range(1,8))*.23
   trill=math.sin(math.tau*(2700+v*170)*t+2*math.sin(t*58))*(.5+.5*math.sin(t*28))*.055
   airy=low*.8+noise*.07
   chime=sum(math.sin(math.tau*f*(1+v*.018)*t)*math.exp(-t*(3+j)) for j,f in enumerate([880,1320,1764,2210]))*.11
   if name in ['wing_flap','glide','perch']:value=airy*(1+.4*math.sin(t*15))+chime*.08
   elif name in ['drop','takeoff','depart']:value=airy*.5+chime+vocal*.35+trill*.3
   else:value=vocal+trill+airy*.15+chime*.12
   values.append(value*envelope)
  peak=max(map(abs,values));pcm=array.array('h',(int(x/peak*26000) for x in values));wav=tmp/f'{name}_{v+1:02}.wav'
  with wave.open(str(wav),'wb') as w:w.setparams((1,2,sr,len(pcm),'NONE','not compressed'));w.writeframes(pcm.tobytes())
  ogg=out/f'{name}_{v+1:02}.ogg';subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(ogg)],check=True)
  entries.append({'name':f'slavicmyths:firebird/{name}_{v+1:02}','attenuation_distance':40 if name=='wing_flap' else 64})
  manifest.append({'file':str(ogg.relative_to(assets)),'duration':length,'channels':1,'sample_rate':sr,'sha256':hashlib.sha256(ogg.read_bytes()).hexdigest(),'source':'original deterministic synthesis; tools/firebird_audio.py'})
 sounds['firebird_'+name]={'sounds':entries}
(assets/'sounds.json').write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
p=ROOT/'docs/firebird/audio-manifest.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf8')
print(len(manifest),'original mono Vorbis variants')
