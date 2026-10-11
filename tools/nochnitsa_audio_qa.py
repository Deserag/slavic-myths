"""Decode production Vorbis and measure it, without claiming an in-game audition."""
from pathlib import Path
import json,subprocess,hashlib,math
import numpy as np
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';out=R/'docs/verification/nochnitsa-1.6.4';out.mkdir(parents=True,exist_ok=True)
results=[];hashes=set();families={};decoded={}
for row in json.loads((R/'docs/nochnitsa/audio-manifest.json').read_text()):
 p=A/row['file'];probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(p)]));stream=probe['streams'][0]
 assert stream['codec_name']=='vorbis' and stream['channels']==1 and int(stream['sample_rate']) in [44100,48000]
 pcm=subprocess.check_output(['ffmpeg','-v','error','-i',str(p),'-f','f32le','-acodec','pcm_f32le','-']);a=np.frombuffer(pcm,dtype='<f4');assert len(a) and np.isfinite(a).all();duration=len(a)/int(stream['sample_rate']);peak=20*math.log10(float(np.max(np.abs(a))));rms=20*math.log10(float(np.sqrt(np.mean(a*a))));assert row['min']-.01<=duration<=row['max']+.01,(p,duration)
 assert peak<=-1 and rms>-48,(p,peak,rms);assert abs(float(np.mean(a)))<.005
 digest=hashlib.sha256(pcm).hexdigest();assert digest not in hashes;hashes.add(digest);families.setdefault(row['event'],[]).append(rms);decoded.setdefault(row['event'],[]).append(a)
 results.append({'file':row['file'],'duration':duration,'peak_dbfs':peak,'rms_dbfs':rms,'pcm_sha256':digest,'codec':'vorbis','channels':1,'sample_rate':int(stream['sample_rate'])})
correlations={}
for name,variants in decoded.items():
 assert max(families[name])-min(families[name])<=5
 maximum=0
 for i,a in enumerate(variants):
  for b in variants[:i]:
   n=min(len(a),len(b));value=abs(float(np.corrcoef(a[:n:8],b[:n:8])[0,1]));maximum=max(maximum,value);assert value<.95,(name,value)
 correlations[name]=maximum
assert len(results)==31 and len(families)==12
assert np.mean(families['nochnitsa.ambient'])<np.mean(families['nochnitsa.scream_release'])-10
report={'status':'PASS','files':results,'max_family_correlation':correlations,'game_audio_audition':'NOT TESTED','source':'Original synthetic formant/noise DSP; no external audio samples'}
(out/'audio-qa.json').write_text(json.dumps(report,indent=2)+'\n');print('AUDIO PASS: 31 decoded unique mono Vorbis, 12 families, duration/peak/RMS/correlation checked')
