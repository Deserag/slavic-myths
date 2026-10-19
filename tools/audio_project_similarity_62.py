"""All-asset acoustic screening; review candidates, never a hearing verdict."""
from pathlib import Path
import sys,json,csv
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np,soundfile as sf
from audio_project_61 import readcsv,write,O

def main():
 assets=readcsv('ASSETS.csv');vectors=[];pcm={};durations=[];paths=[]
 # Fixed mel bank on 8kHz PCM; 32 bands, 16 temporal bins. The cosine screen
 # is deliberately only a candidate generator, not "perceptually identical".
 hz=700*(np.exp(np.linspace(0,np.log(1+4000/700),34))-1);freq=np.fft.rfftfreq(256,1/8000)
 bank=np.array([np.maximum(0,np.minimum((freq-hz[i])/(hz[i+1]-hz[i]),(hz[i+2]-freq)/(hz[i+2]-hz[i+1]))) for i in range(32)])
 for row in assets:
  name=row['path'];x,sr=sf.read(R/name,always_2d=True,dtype='float32');x=x.mean(axis=1).astype(float)
  m=max(2,round(len(x)*8000/sr));spec=np.fft.rfft(x);new=np.zeros(m//2+1,complex);n=min(len(spec),len(new));new[:n]=spec[:n];x=np.fft.irfft(new,n=m)*(m/len(x))
  active=np.flatnonzero(abs(x)>max(abs(x))*10**(-50/20))
  if len(active):x=x[active[0]:active[-1]+1]
  x-=x.mean();x/=max(np.linalg.norm(x),1e-12);pcm[name]=x;durations.append(len(x)/8000);paths.append(name)
  padded=np.pad(x,(0,max(0,256-len(x))));frames=np.lib.stride_tricks.sliding_window_view(padded,256)[::128]*np.hanning(256)
  power=abs(np.fft.rfft(frames,axis=1))**2;mel=np.log1p((power@bank.T)*1e6)
  temporal=np.array([part.mean(axis=0) if len(part) else mel[-1] for part in np.array_split(mel,16)])
  v=temporal.ravel();v-=v.mean();v/=max(np.linalg.norm(v),1e-12);vectors.append(v)
 matrix=np.asarray(vectors);scores=matrix@matrix.T;rows=[];eligible=0
 for i,a in enumerate(paths):
  for j in range(i+1,len(paths)):
   b=paths[j];ratio=durations[i]/max(durations[j],1e-12)
   if not .5<=ratio<=2:continue
   eligible+=1;score=float(scores[i,j])
   if score<.985:continue
   x,y=pcm[a],pcm[b];n=1<<(len(x)+len(y)-1).bit_length();corr=np.fft.irfft(np.fft.rfft(x,n)*np.conj(np.fft.rfft(y,n)),n)
   lag=min(1600,len(x)-1,len(y)-1);values=np.r_[corr[-lag:],corr[:lag+1]] if lag else corr[:1];best=int(np.argmax(abs(values)));aligned=float(abs(values[best]))
   rows.append(dict(a=a,b=b,mel_temporal_cosine=round(score,7),aligned_waveform_correlation=round(aligned,7),lag_s=(best-lag)/8000,result='HIGH_WAVEFORM_MATCH_REVIEW' if aligned>=.98 else 'SIMILAR_SPECTRAL_ENVELOPE_REVIEW_FALSE_POSITIVE_POSSIBLE',hearing='NOT_LISTENED'))
 if rows:write('PROJECT_SIMILARITY.csv',rows)
 else:(O/'PROJECT_SIMILARITY.csv').write_text('a,b,mel_temporal_cosine,aligned_waveform_correlation,lag_s,result,hearing\n')
 report=dict(files=len(paths),all_pairs=len(paths)*(len(paths)-1)//2,duration_eligible_pairs=eligible,spectral_candidates=len(rows),high_waveform_matches=sum(r['aligned_waveform_correlation']>=.98 for r in rows),rate=8000,mel_bands=32,time_bins=16,window=256,hop=128,cosine_screen_threshold=.985,aligned_threshold=.98,max_lag_ms=200,limitations='Mel cosine is timbre/envelope screening with possible false positives. It misses large pitch/time transformations, phase cancellations and semantic misuse; hearing NOT_LISTENED. Waveform stage only on spectral candidates.',production_writes=False)
 (O/'PROJECT_SIMILARITY_METHOD.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
