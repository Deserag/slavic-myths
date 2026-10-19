"""#60 recorded foley, no oscillators/no pitch variation. Package A remains IN_PROGRESS."""
from pathlib import Path
import sys,json,hashlib,shutil,subprocess,tempfile,os
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np,soundfile as sf
A=R/'art/audio/pack-a-60';RATE=44100

def stable_text(path,text):
 if path.exists() and path.read_text(encoding='utf-8')==text:return
 temporary=path.with_name(path.name+'.voice60.tmp');assert not temporary.exists()
 try:
  temporary.write_text(text,encoding='utf-8');temporary.replace(path)
 finally:
  if temporary.exists():temporary.unlink()

def restore_events():
 assets=R/'src/main/resources/assets/slavicmyths';path=assets/'sounds.json';events=json.loads(path.read_text(encoding='utf-8'))
 for n in range(1,4):events[f'tugarin_spin_blade_{n}']={'subtitle':'subtitles.slavicmyths.tugarin_spin_blade','sounds':[{'name':f'slavicmyths:boss/tugarin/foley/spin_{n}','attenuation_distance':24}]}
 stable_text(path,json.dumps(events,ensure_ascii=False,indent=2)+'\n')
 for lang,label in [('en_us','Tugarin swings his blades'),('ru_ru','Тугарин взмахивает клинками')]:
  path=assets/f'lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'));data['subtitles.slavicmyths.tugarin_spin_blade']=label;stable_text(path,json.dumps(data,ensure_ascii=False,indent=2)+'\n')

def encode(path,x):
 fd,temporary=tempfile.mkstemp(prefix='.voice60-',suffix='.ogg',dir=path.parent);os.close(fd);temporary=Path(temporary)
 try:
  subprocess.run(['ffmpeg','-v','error','-y','-fflags','+bitexact','-f','f32le','-ar',str(RATE),'-ac','1','-i','pipe:0','-map_metadata','-1','-c:a','libvorbis','-q:a','4','-flags:a','+bitexact','-fflags','+bitexact',str(temporary)],input=x.astype('<f4').tobytes(),check=True)
  if not path.exists() or path.read_bytes()!=temporary.read_bytes():temporary.replace(path)
 finally:
  if temporary.exists():temporary.unlink()

def generate():
 source={}
 for spec in json.loads((A/'sources.json').read_text()):
  p=A/spec['file'];assert hashlib.sha256(p.read_bytes()).hexdigest()==spec['sha256'];x,sr=sf.read(p);assert sr==RATE;source[p.stem]=x.mean(axis=1) if x.ndim>1 else x
 clips=[('ovinnik_hearth','hearth',8,13),('ovinnik_ignition','ignition',0,1.169)]+[(f'tugarin_spin_{n}',f'swosh-0{n}',0,None) for n in range(1,4)]+[(f'cloth_{n}',f'cloth-0{n}',0,None) for n in [1,3,5]]
 preview=A/'preview';preview.mkdir(exist_ok=True);report=[];montage=[];offset=0
 for name,key,start,end in clips:
  x=source[key][int(start*RATE):None if end is None else int(end*RATE)].copy();x-=x.mean();fade=min(220,len(x)//8);x[:fade]*=np.linspace(0,1,fade);x[-fade:]*=np.linspace(1,0,fade);rms=np.sqrt(np.mean(x*x));gain=min(.075/max(rms,1e-12),.45/max(abs(x)));x*=gain
  p=preview/(name+'.ogg');encode(p,x);y,sr=sf.read(p);assert np.isfinite(y).all() and max(abs(y))<.6
  row=dict(name=name,source=key,start=start,end=end,gain=round(float(gain),6),duration=round(len(y)/RATE,5),rms_dbfs=round(float(20*np.log10(np.sqrt(np.mean(y*y)))),2),peak_dbfs=round(float(20*np.log10(max(abs(y)))),2),sha256=hashlib.sha256(p.read_bytes()).hexdigest(),montage_offset=round(offset,4),production=name.startswith('tugarin_spin_'))
  report.append(row);montage.extend([y,np.zeros(RATE//2)]);offset+=(len(y)+RATE//2)/RATE
  if row['production']:
   dest=R/'src/main/resources/assets/slavicmyths/sounds/boss/tugarin/foley'/('spin_'+name[-1]+'.ogg');dest.parent.mkdir(parents=True,exist_ok=True);
   if not dest.exists() or dest.read_bytes()!=p.read_bytes():shutil.copyfile(p,dest)
 encode(preview/'audition.ogg',np.concatenate(montage));(A/'outputs.json').write_text(json.dumps(report,indent=2)+'\n');restore_events();print('8 recorded foley previews; only 3 spin passes wired. Owner acceptance pending.')
if __name__=='__main__':generate()
