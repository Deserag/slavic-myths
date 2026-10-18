"""Edit licensed recordings; no synthesized/formant voice, no pitch shifting. #59."""
from pathlib import Path
import sys,json,hashlib,subprocess,re
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np
import soundfile as sf
RATE=44100;SRC=R/'art/audio/rusalka-59';DEST=R/'src/main/resources/assets/slavicmyths/sounds/water/rusalka'
def restore_events(assets=R/'src/main/resources/assets/slavicmyths'):
 canonical=json.loads((SRC/'events.json').read_text(encoding='utf-8'))
 acceptance=json.loads((SRC/'acceptance.json').read_text(encoding='utf-8'))
 for name,cue in canonical['events'].items():
  if not acceptance['owner_accepted'] or name not in acceptance['accepted_events']:cue['sounds']=[]
 for path,values in [(assets/'sounds.json',canonical['events'])]+[(assets/'lang'/f'{lang}.json',values) for lang,values in canonical['lang'].items()]:
  existing=json.loads(path.read_text(encoding='utf-8'));existing.update(values);path.write_text(json.dumps(existing,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def generate(destination=DEST,manifest=SRC/'outputs.json'):
 sources={}
 for spec in json.loads((SRC/'sources.json').read_text(encoding='utf-8')):
  p=SRC/spec['file'];assert hashlib.sha256(p.read_bytes()).hexdigest()==spec['sha256']
  x,sr=sf.read(p);x=x.mean(axis=1) if x.ndim>1 else x
  if sr!=RATE:x=np.interp(np.arange(round(len(x)*RATE/sr))*sr/RATE,np.arange(len(x)),x)
  sources[spec['name']]=x
 def clip(name,start,end):
  y=sources[name][int(start*RATE):int(end*RATE)].copy();y-=y.mean()
  n=min(int(.025*RATE),len(y)//4);y[:n]*=np.linspace(0,1,n);y[-n:]*=np.linspace(1,0,n)
  return y
 def phrase(parts,gap=.28,end=.6):return np.concatenate([item for p in parts for item in [p,np.zeros(int(gap*RATE))]]+[np.zeros(int(end*RATE))])
 # Four separately recorded notes C4 D4 A3 D4. Natural pitch and voice preserved.
 loop=phrase([clip('notes',2.3,3.7),clip('notes',24.2,25.6),clip('notes',44.2,45.6),clip('notes',30.2,31.6)])
 intro=phrase([clip('notes',8.3,9.2),clip('notes',20.2,21.1)],gap=.3,end=.6)
 intense=phrase([clip('notes',14.3,15.7),clip('notes',32.2,33.6),clip('notes',48.2,49.6),clip('notes',36.2,37.6)])
 outputs={'song_intro':intro,'song_loop':loop,'song_intense':intense,
  'ambient_1':clip('humming',.4,2.6),'ambient_2':clip('humming',7.5,9.5),'ambient_3':clip('humming',12.2,14.6),
  'angry':clip('gasp',0,1.1),'hurt_1':clip('hurt',1.45,2.75),'hurt_2':clip('hurt',9.35,10.8),
  'song_break':clip('death',2.3,3.7),'death':clip('death',5.0,8.2),'grab':clip('splash',0,1.08)}
 destination.mkdir(parents=True,exist_ok=True);report=[]
 for name,x in outputs.items():
  # Gentle low pass, not artificial low voice or chorus. Keep the recorded note frequencies.
  cutoff=1600 if name=='song_intense' else 2800
  n=np.arange(101)-50;kernel=2*cutoff/RATE*np.sinc(2*cutoff/RATE*n)*np.hanning(101);kernel/=kernel.sum();x=np.convolve(x,kernel,mode='same')
  rms=np.sqrt(np.mean(x*x));x*=min(.075/(rms+1e-12),.5/(np.max(abs(x))+1e-12))
  path=destination/(name+'.ogg');sf.write(path,x,RATE,format='OGG',subtype='VORBIS')
  decoded,sr=sf.read(path);assert sr==RATE and np.max(abs(decoded))<.7
  loud=subprocess.run(['ffmpeg','-hide_banner','-i',str(path),'-af','ebur128=peak=true','-f','null','-'],capture_output=True,text=True).stderr
  values=re.findall(r'I:\s+(-?[\d.]+) LUFS',loud)
  report.append(dict(file='water/rusalka/'+name+'.ogg',seconds=round(len(decoded)/sr,3),channels=1,peak_dbfs=round(20*np.log10(np.max(abs(decoded))+1e-12),2),integrated_lufs=float(values[-1]) if values else None,sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
 manifest.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
 return report
if __name__=='__main__':
 print(json.dumps(generate(),indent=2));restore_events()
