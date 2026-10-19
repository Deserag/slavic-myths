"""Two distinct Tugarin actions in the selected voice direction; preview only."""
from pathlib import Path
import sys, subprocess, json, hashlib
R=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(R/'.tools/audio-libs'));sys.path.insert(0,str(R/'tools'))
import numpy as np
import soundfile as sf
from audio_60_foley import encode, RATE
P=R/'art/audio/pack-a-60/voice-rework';report=[]
filters=json.loads((P/'alternatives/tugarin_challenge_rough.json').read_text(encoding='utf-8'))['processing'].split(';',1)[0]
for key,action,target in [('tugarin_breath','Тяжёлое дыхание в ожидании боя',.085),('tugarin_attack_voice','Голосовое усилие при ударе',.18)]:
 source=P/'preview'/(key+'.ogg');dest=P/'alternatives'/(key+'_rough.ogg')
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-af',filters,'-f','f32le','-ar',str(RATE),'-ac','1','pipe:1'])
 x=np.frombuffer(raw,dtype='<f4').astype(float);x=np.tanh(x*2.2)/2.2;x-=x.mean();x*=min(target/max(np.sqrt(np.mean(x*x)),1e-12),.82/max(np.max(abs(x)),1e-12))
 encode(dest,x);y,sr=sf.read(dest);original,osr=sf.read(source)
 assert np.isfinite(y).all() and np.max(abs(y))<.95 and abs(len(y)/sr-len(original)/osr)<.06
 report.append(dict(mob='Тугарин',role='Босс, человеческая форма',phase='Фаза 1',action=action,source=str(source.relative_to(R)),source_sha256=hashlib.sha256(source.read_bytes()).hexdigest(),file=str(dest.relative_to(R)),sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),seconds=len(y)/sr,processing=filters+'; mild saturation; calibrated gain',status='OWNER_APPROVED_SELECTED',owner_approved=True))
(P/'alternatives/next-review.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Two distinct-source Tugarin actions rendered; full decode and peak/duration checks PASS; no production writes.')
