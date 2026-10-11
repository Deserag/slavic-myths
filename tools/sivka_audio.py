"""Real CC0 equine sources plus original deterministic hoof/magic synthesis."""
from pathlib import Path
import subprocess,wave,array,math,random,csv,json,hashlib
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'work/sivka/audio-source';OUT=ROOT/'src/main/resources/assets/slavicmyths/sounds/entity/sivka';RATE=44100
def decode(p):
    target=SRC/(p.stem+'-decoded.wav');subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(p),'-ac','1','-ar',str(RATE),str(target)],check=True)
    with wave.open(str(target),'rb') as w:return [v/32768 for v in array.array('h',w.readframes(w.getnframes()))]
def write(p,values):
    with wave.open(str(p),'wb')as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(RATE);w.writeframes(array.array('h',(round(v*32767) for v in values)).tobytes())
def main():
    OUT.mkdir(parents=True,exist_ok=True);sources={n:decode(SRC/(n+'.ogg')) for n in ('0460','0286','1543')};manifest=[]
    specs=list(csv.DictReader((ROOT/'work/sivka-spec/Sivka_Burka_1.6_Codex_TZ/04_audio_fx/03_AUDIO_CUES_CSV.csv').read_text(encoding='utf8').splitlines()))
    for index,row in enumerate(specs):
        event=row['event'];lo,hi=map(float,row['duration_seconds'].split('-'))
        for variant in range(int(row['required_variants'])):
            duration=lo+(hi-lo)*(.55+variant*.08);samples=[];rng=random.Random(16000+index*7+variant);noise=low=0;source=None
            if event=='breath':source='1543'
            elif event=='snort_soft':source='0286'
            elif event in ('whinny_soft','whinny_alarm','summon_response','bond_complete'):source='0460'
            elif event=='bond_progress':source='0286'
            natural=sources[source] if source else [];nframes=int(duration*RATE)
            # Time-resample entire real vocal; trim/fade and gentle pitch variation, no looping fake neigh.
            for i in range(nframes):
                t=i/RATE;x=i/nframes;noise=.55*noise+.45*rng.uniform(-1,1);low=.965*low+.035*rng.uniform(-1,1)
                env=min(1,t/.01,(duration-t)/.02)
                value=0
                if source:
                    at=min(len(natural)-1,int(x*(len(natural)-1)));value=natural[at]
                    if event in ('bond_complete','summon_response'):value+=.025*math.sin(t*2*math.pi*(330+40*x))*math.sin(math.pi*x)
                    if event=='bond_progress':value*=.55
                elif event.startswith('hoof_') or event=='magic_land':
                    freq={'hoof_grass':95,'hoof_stone':240,'hoof_snow':75,'hoof_wood':155}.get(event,85)
                    value=(.6*math.sin(t*math.pi*2*freq)*math.exp(-t*30)+noise*(.25 if event=='hoof_snow' else .12)*math.exp(-t*18)+low*.3*math.exp(-t*20))
                elif event=='feed_chew':value=(noise*.25+low*.45)*(.3+.7*max(0,math.sin(t*math.pi*8)))
                elif event=='gallop_burst':
                    local=t%.15;value=(math.sin(local*math.pi*2*95)*.55+noise*.15)*math.exp(-local*35)*(1-x)
                elif event=='whistle_player':value=(math.sin(t*2*math.pi*(880+180*math.sin(x*math.pi*2)))*.4+math.sin(t*2*math.pi*1760)*.03)*math.sin(math.pi*x)
                elif event=='summon_reject':value=(low*.5+math.sin(t*2*math.pi*170)*.2)*math.exp(-t*25)
                else:
                    base=260 if event=='bond_expired' else 370;value=low*.35*math.sin(math.pi*x)+noise*.06*math.sin(math.pi*x)+sum(math.sin(t*2*math.pi*(base*k+(30 if event!='depart' else -30)*x))*.025/k for k in (1,1.5,2))*math.sin(math.pi*x)
                samples.append(value*max(0,env))
            peak=max(abs(v) for v in samples);samples=[v*.68/peak for v in samples];name=event+'_'+str(variant+1);wav=SRC/(name+'.wav');write(wav,samples);target=OUT/(name+'.ogg');subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(wav),'-ac','1','-ar',str(RATE),'-c:a','libvorbis','-q:a','5',str(target)],check=True)
            actual=decode(target);pk=max(abs(v) for v in actual);assert pk<.89
            manifest.append({'file':name+'.ogg','cue':event,'source':source or 'original synthesis','duration':len(actual)/RATE,'sample_rate':RATE,'channels':1,'peak_dbfs':20*math.log10(pk),'sha256':hashlib.sha256(target.read_bytes()).hexdigest()})
    (SRC/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');print('Audio PASS:',len(manifest),'mono 44100 Vorbis files; real CC0 equine vocal sources')
if __name__=='__main__':main()
