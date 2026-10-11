"""28 actual mono Vorbis cues: licensed animal recordings, rough growl layer, original foley."""
from pathlib import Path
import subprocess,wave,array,math,random,json,hashlib
ROOT=Path(__file__).resolve().parents[1];SCRATCH=ROOT/'work/volkolak-143/audio';OUT=ROOT/'src/main/resources/assets/slavicmyths/sounds/volkolak';A=ROOT/'src/main/resources/assets/slavicmyths'
SPECS={'ambient':(3,1.7),'howl_short':(1,1.5),'alert':(1,.7),'snarl':(2,1.05),'claw_windup':(1,.35),'claw_swing':(2,.25),'bite':(1,.35),'pounce_grunt':(1,.6),'land':(1,.4),'stance_shift':(1,.6),'grab_prepare':(1,.85),'grab_bite':(1,.45),'grab_release':(1,.5),'hurt':(2,.6),'phase_roar':(1,2.05),'fake_fall':(1,1.2),'curse_revive':(1,1.85),'escape_howl':(1,1.5),'bad_blood_apply':(1,.4),'grave_rustle':(1,1.05),'grave_dig':(1,.7),'skull_unearth':(1,.9),'curse_cleared':(1,1.25)}
RATE=44100
def decode(source,name):
 target=SCRATCH/(name+'.wav');subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(source),'-ac','1','-ar',str(RATE),'-c:a','pcm_s16le',str(target)],check=True)
 with wave.open(str(target),'rb')as f:a=array.array('h',f.readframes(f.getnframes()))
 return [v/32768 for v in a]
def wav(path,samples):
 with wave.open(str(path),'wb')as f:f.setnchannels(1);f.setsampwidth(2);f.setframerate(RATE);f.writeframes(array.array('h',(round(max(-.98,min(.98,s))*32767)for s in samples)).tobytes())
def main():
 OUT.mkdir(parents=True,exist_ok=True);dog=decode(SCRATCH/'sources/coldvet-harvey-growl-preview.ogg','dog');rough=decode(SCRATCH/'sources/egomassive-growl-preview.ogg','rough');howl=decode(ROOT/'work/grey-wolf/audio/Wolf_howls.ogg','howl')
 # Choose audible windows of the real dog recording; silence never substitutes for an event.
 windows=[]
 for at in range(0,len(dog)-3*RATE,RATE//2):
  rms=math.sqrt(sum(v*v for v in dog[at:at+RATE:40])/(RATE/40));windows.append((rms,at))
 peaks=[at for rms,at in sorted(windows,reverse=True)[:60]]
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));report=[];index=0
 for event,(variants,duration)in SPECS.items():
  names=[]
  for variant in range(variants):
   name=event+(f'_{variant+1}'if variants>1 else '');names.append({'name':f'slavicmyths:volkolak/{name}','stream':False});rng=random.Random(14300+index);start=peaks[(index*7)%len(peaks)];rough_at=int((.15+((index*.19)%2.5))*RATE);noise=0;low=0;samples=[];nframes=round(duration*RATE)
   foley=event in ('claw_windup','claw_swing','land','stance_shift','grave_rustle','grave_dig','skull_unearth','curse_cleared','bad_blood_apply')
   for n in range(nframes):
    x=n/nframes;t=n/RATE;noise=.55*noise+.45*rng.uniform(-1,1);low=.965*low+.035*rng.uniform(-1,1);envelope=min(1,x*35)*(1-x)**(.7 if event in ('ambient','phase_roar','escape_howl') else 1.5)
    speed=.86+variant*.04;natural=dog[min(len(dog)-1,start+int(n*speed))];r=rough[(rough_at+int(n*.88))%len(rough)];harm=math.sin(2*math.pi*(75+13*math.sin(t*8))*t)*.09
    if event in ('escape_howl','howl_short'):natural=howl[min(len(howl)-1,round((11.2 if event=='escape_howl' else 14.2)*RATE)+n)]
    if foley:
     body=low*.8+noise*(.3 if event in ('claw_swing','grave_rustle','grave_dig')else .12)
     if event in ('land','stance_shift','skull_unearth'):body+=math.sin(t*2*math.pi*93)*math.exp(-t*17)*.35
     if event=='claw_swing':body*=math.sin(math.pi*x)**2
     if event in ('curse_cleared','bad_blood_apply'):body+=math.sin(t*2*math.pi*(240+80*x))*math.exp(-x*3)*.06
    else:body=.72*natural+.24*r+.04*harm
    if event=='curse_revive':body=.45*natural+.25*r+.25*low+.05*harm
    samples.append(body*envelope)
   peak=max(abs(s)for s in samples);samples=[s*.75/peak for s in samples];source=SCRATCH/(name+'.wav');wav(source,samples);target=OUT/(name+'.ogg');subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(source),'-ac','1','-ar',str(RATE),'-c:a','libvorbis','-q:a','5',str(target)],check=True)
   actual=decode(target,'verify-'+name);actual_peak=max(abs(v)for v in actual);assert actual_peak<=10**(-1/20),(name,actual_peak)
   provenance='original foley seed14300+'+str(index) if foley else ('FWS wolf howl public domain 72%, egomassive CC0 24%, original harmonic 4%' if event in ('escape_howl','howl_short') else 'Harvey growl CC0 72%, egomassive CC0 24%, original harmonic 4%')
   if event=='curse_revive':provenance='Harvey growl CC0 45%, egomassive CC0 25%, original noise 25%, original harmonic 5%'
   report.append({'event':event,'file':name+'.ogg','duration':len(actual)/RATE,'channels':1,'sample_rate':RATE,'peak_dbfs':20*math.log10(actual_peak),'source':provenance,'dog_offset_seconds':start/RATE,'sha256':hashlib.sha256(target.read_bytes()).hexdigest()});index+=1
  events['volkolak.'+event]={'subtitle':'subtitles.slavicmyths.volkolak.'+event,'sounds':names}
 (A/'sounds.json').write_text(json.dumps(events,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');(SCRATCH/'manifest.json').write_text(json.dumps(report,indent=2)+'\n');print(f'{len(report)} OGG files, {len(SPECS)} events, mono44100, all measured peaks <= -1dBFS')
if __name__=='__main__':main()
