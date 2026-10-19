"""Aligned waveform screening of envelope candidates; not a hearing verdict."""
from pathlib import Path
import csv, sys, json
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np
import soundfile as sf
O=R/'docs/audio-review'
pairs=[r['members'].split(';') for r in csv.DictReader((O/'DUPLICATES.csv').open(encoding='utf-8-sig')) if r['kind']=='ENVELOPE_SCREEN_ONLY']
cache={}
for name in sorted({p for pair in pairs for p in pair}):
 x,sr=sf.read(R/name,always_2d=True,dtype='float32');x=x.mean(axis=1).astype(float)
 # FFT band-limit resampling, preserving duration. This is analysis-only; never
 # writes processed audio back into the asset library.
 m=max(2,round(len(x)*8000/sr));spec=np.fft.rfft(x);new=np.zeros(m//2+1,complex);n=min(len(spec),len(new));new[:n]=spec[:n];x=np.fft.irfft(new,n=m)*(m/len(x))
 idx=np.flatnonzero(abs(x)>max(abs(x))*10**(-50/20))
 if len(idx):x=x[idx[0]:idx[-1]+1]
 x-=x.mean();norm=np.linalg.norm(x);cache[name]=x/max(norm,1e-12)
rows=[]
for a,b in pairs:
 x,y=cache[a],cache[b];n=1<<(len(x)+len(y)-1).bit_length();corr=np.fft.irfft(np.fft.rfft(x,n)*np.conj(np.fft.rfft(y,n)),n)
 # Evaluate only +/-200ms lag after leading-silence alignment. Correlation uses
 # whole-signal normalization: a tiny common fragment cannot score as a match.
 lag=min(1600,len(x)-1,len(y)-1);values=np.r_[corr[-lag:],corr[:lag+1]] if lag else corr[:1];k=int(np.argmax(abs(values)));score=float(abs(values[k]));shift=(k-lag)/8000
 rows.append(dict(a=a,b=b,normalized_cross_correlation=round(score,7),lag_s=round(shift,6),threshold=.98,result='HIGH_WAVEFORM_MATCH_REVIEW' if score>=.98 else 'ENVELOPE_MATCH_NOT_CONFIRMED_BY_WAVEFORM',hearing='NOT_LISTENED'))
with (O/'ALIGNED_CORRELATION.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
report=dict(pairs=len(rows),high_waveform_matches=sum(r['normalized_cross_correlation']>=.98 for r in rows),analysis_rate=8000,onset_threshold_relative_db=-50,max_lag_ms=200,threshold=.98,scope='Only envelope-screened pairs; misses candidates with different envelopes or pitch/timing changes. Not exhaustive perceptual analysis or a hearing verdict.',production_writes=False)
(O/'CORRELATION_METHOD.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
