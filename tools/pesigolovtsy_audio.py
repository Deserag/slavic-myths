"""Original procedural canine glottal/formant and material DSP. No sampled recordings."""
from pathlib import Path
import csv,json,wave,subprocess,hashlib
import numpy as np
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=A/'sounds/pesigolovtsy';O.mkdir(parents=True,exist_ok=True);W=R/'work/pesigolovtsy/audio';W.mkdir(parents=True,exist_ok=True)
rows=list(csv.DictReader((R/'work/pesigolovtsy-spec/Slavic_Myths_1.6.3_Pesigolovtsy_Codex_TZ/manifests/sound_production_manifest.csv').open(encoding='utf-8-sig')))
sounds=json.loads((A/'sounds.json').read_text(encoding='utf8'));report=[];sr=44100
def band(x,centers,widths):
 f=np.fft.rfftfreq(len(x),1/sr);response=sum(np.exp(-.5*((f-c)/w)**2) for c,w in zip(centers,widths));return np.fft.irfft(np.fft.rfft(x)*response,n=len(x))
for row_index,row in enumerate(rows):
 role=row['role'];event=row['event'];key='doghead.'+role+'.'+event;sounds[key]={'subtitle':'subtitles.slavicmyths.'+key,'sounds':[]};lo,hi=map(float,row['duration_sec_target'].split('-'))
 for i in range(1,int(row['variants'])+1):
  duration=lo+(hi-lo)*(.27+.1*i);rng=np.random.default_rng(16500+row_index*11+i);n=int(sr*duration);t=np.arange(n)/sr;u=t/duration;noise=rng.normal(0,1,n);voice_role=role if role!='shared' else ['hunter','scout','leader'][(i-1)//2] if event in ['hurt','death'] else 'hunter'
  f0={'hunter':115,'scout':155,'leader':85}[voice_role]+i*3+8*np.sin(t*2*np.pi*(7+i*.4));f0*=1.05-.2*u if event=='death' else 1+.15*np.sin(u*np.pi) if event=='howl_release' else 1
  phase=2*np.pi*np.cumsum(f0)/sr;glottal=sum(np.sin(phase*h)/h**.85 for h in range(1,24));voice=band(glottal,[380,950,1850],[110,210,300]);air=band(noise,[320,1300,4300],[160,600,1500]);cloth=band(noise,[1200,3900],[700,1400]);env=np.minimum(1,t/.012)*np.minimum(1,(duration-t)/.035)
  if 'whoosh' in event:signal=cloth*np.exp(-((u-.48)/.25)**2)+.15*air
  elif event=='bow_draw':signal=.45*cloth*(.55+.45*np.sin(2*np.pi*t*26))+.22*np.sin(2*np.pi*(1300-400*u)*t)*np.sin(np.pi*u)**2
  elif event=='bow_release':signal=cloth*np.exp(-u*14)+sum(np.sin(2*np.pi*(210+j*83+i*9)*t)*np.exp(-u*(11+j))*.12 for j in range(5))
  elif event=='axe_impact':signal=band(noise,[170,700,2500],[100,350,1100])*np.exp(-u*10)+.25*np.sin(2*np.pi*(90+i*7)*t)*np.exp(-u*19)
  elif event.endswith('step') or event=='claw_gear':signal=band(noise,[140,900 if event=='mud_step' else 2100],[90,700])*np.exp(-u*11)+.12*np.sin(2*np.pi*(80+i*15)*t)*np.exp(-u*22)
  elif event=='fur_cloth':signal=cloth*np.sin(np.pi*u)**1.5*(.7+.3*np.sin(t*2*np.pi*11))
  elif event=='howl_inhale':signal=air*np.sin(np.pi*u)**.7+.1*voice
  elif event=='howl_release':signal=voice*.8+air*.12;env*=np.sin(np.pi*u)**.55
  elif event in ['bark','spot_bark']:signal=voice*.9+air*.25;env*=np.exp(-((u-.35)/.25)**2)+.35*np.exp(-((u-.7)/.12)**2)
  elif event=='ambient':signal=voice*.45+air*.3;env*=np.sin(np.pi*u)**1.5*(.6+.4*np.sin(t*2*np.pi*8)**2)
  else:signal=voice*.6+air*.25;env*=np.sin(np.pi*u)**.7
  signal*=env;signal-=signal.mean();signal/=max(abs(signal));signal*=.65 if event=='ambient' else .72
  path=row['file_template'].format(index=i);wav=W/(Path(path).stem+'.wav');ogg=A/'sounds'/path
  with wave.open(str(wav),'wb') as w:w.setparams((1,2,sr,n,'NONE','not compressed'));w.writeframes((signal*32767).astype('<i2').tobytes())
  subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(ogg)],check=True)
  decoded=np.frombuffer(subprocess.check_output(['ffmpeg','-v','error','-i',str(ogg),'-f','f32le','-acodec','pcm_f32le','-']),dtype='<f4');peak=float(max(abs(decoded)));rms=float(np.sqrt(np.mean(decoded**2)));assert peak<=10**(-1/20) and rms>.005,(path,peak,rms)
  sounds[key]['sounds'].append({'name':'slavicmyths:'+path[:-4],'attenuation_distance':32 if event=='howl_release' else 24 if event in ['bark','spot_bark'] else 16})
  report.append({'event':key,'file':path,'duration':len(decoded)/sr,'peak_db':20*np.log10(peak),'rms_db':20*np.log10(rms),'sha256':hashlib.sha256(ogg.read_bytes()).hexdigest(),'original_seed':16500+row_index*11+i})
for role,index in [('hunter',0),('scout',2),('leader',4)]:
 for event in ['hurt','death']:
  sounds['doghead.'+role+'.'+event]={'subtitle':'subtitles.slavicmyths.doghead.shared.'+event,'sounds':sounds['doghead.shared.'+event]['sounds'][index:index+2]}
assert len(report)==70 and len({r['sha256'] for r in report})==70
(A/'sounds.json').write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
ru={'ambient':'Песиголовец рычит','bark':'Охотник лает','spot_bark':'Разведчик подаёт сигнал','thrust_whoosh':'Копьё рассекает воздух','sweep_whoosh':'Взмах копья','bow_draw':'Тетива натягивается','bow_release':'Лук стреляет','knife_whoosh':'Взмах ножа','howl_inhale':'Вожак вдыхает','howl_release':'Вожак воет','axe_whoosh':'Взмах топора','axe_impact':'Топор ударяет','hurt':'Песиголовец ранен','death':'Песиголовец погибает','claw_gear':'Снаряжение бряцает','leather_step':'Шаг по земле','mud_step':'Шаг по грязи','stone_step':'Шаг по камню','fur_cloth':'Мех и ткань шуршат'}
for lang in ['ru_ru','en_us']:
 p=A/'lang'/f'{lang}.json';v=json.loads(p.read_text(encoding='utf8'))
 for row in rows:v['subtitles.slavicmyths.doghead.'+row['role']+'.'+row['event']]=ru[row['event']] if lang=='ru_ru' else 'Dog-headed folk: '+row['event'].replace('_',' ')
 v['entity.slavicmyths.doghead_arrow']='Стрела песиголовца' if lang=='ru_ru' else 'Dog-headed Archer’s Arrow';p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
(R/'docs/pesigolovtsy/source/audio-manifest.json').write_text(json.dumps({'provenance':'Original seeded glottal/formant/noise DSP; no external samples. Game audition NOT RUN.','files':report},indent=2)+'\n')
print('70 original mono Vorbis cues validated: unique files, decoded peaks <= -1 dBFS, nonzero RMS.')
