"""Agent-selected separate SPIN preparation exhale; never claims owner acceptance."""
from pathlib import Path
import sys, json, subprocess, hashlib, shutil
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'));sys.path.insert(0,str(R/'tools'))
import numpy as np
import soundfile as sf
from audio_60_foley import encode, RATE, stable_text
P=R/'art/audio/pack-a-60/voice-rework';A=R/'src/main/resources/assets/slavicmyths'
def restore_selected():
 source=P/'preview/tugarin_spin_prepare.ogg'
 filters=json.loads((P/'alternatives/tugarin_challenge_rough.json').read_text(encoding='utf-8'))['processing'].split(';',1)[0]
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-af',filters,'-f','f32le','-ar',str(RATE),'-ac','1','pipe:1'])
 x=np.frombuffer(raw,dtype='<f4').astype(float);x=np.tanh(x*2.2)/2.2;x-=x.mean();x*=min(.15/max(np.sqrt(np.mean(x*x)),1e-12),.8/max(np.max(abs(x)),1e-12))
 dest=P/'alternatives/tugarin_spin_prepare_rough.ogg';encode(dest,x);y,sr=sf.read(dest)
 assert sr==RATE and np.isfinite(y).all() and max(abs(y))<.95 and len(y)/sr<16/20
 target=A/'sounds/voice/tugarin/spin_prepare.ogg'
 if not target.exists() or target.read_bytes()!=dest.read_bytes():shutil.copyfile(dest,target)
 event='tugarin_spin_prepare';subtitle='subtitles.slavicmyths.tugarin_spin_prepare'
 path=A/'sounds.json';events=json.loads(path.read_text(encoding='utf-8'));events[event]={'subtitle':subtitle,'sounds':[{'name':'voice/tugarin/spin_prepare','attenuation_distance':28}]};stable_text(path,json.dumps(events,ensure_ascii=False,indent=2)+'\n')
 for lang,label in [('ru_ru','Тугарин выдыхает перед вращением'),('en_us','Tugarin exhales before spinning')]:
  path=A/f'lang/{lang}.json';d=json.loads(path.read_text(encoding='utf-8'));d[subtitle]=label;stable_text(path,json.dumps(d,ensure_ascii=False,indent=2)+'\n')
 path=R/'src/main/java/org/slavicmyths/registry/ModSounds.java';s=path.read_text(encoding='utf-8');line='        map.put("tugarin_spin_prepare",sound("tugarin_spin_prepare"));\n'
 if line not in s:
  marker='        map.put("tugarin_phase1_idle",sound("tugarin_phase1_idle"));';assert s.count(marker)==1;s=s.replace(marker,line+marker);stable_text(path,s)
 report=dict(mob='Тугарин',role='Босс, человеческая форма',action='Выдох перед вращением SPIN',source='preview/tugarin_spin_prepare.ogg',source_sha256=hashlib.sha256(source.read_bytes()).hexdigest(),file='alternatives/tugarin_spin_prepare_rough.ogg',sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),seconds=len(y)/sr,event=event,trigger='TugarinCombat.begin(SPIN), tick0; first hit remains tick16',status='AGENT_SELECTED_UNDER_OWNER_DELEGATION',owner_approved=False,runtime_accepted=False,processing=filters+'; mild saturation; gain',license='CC-BY-4.0, Alba MacKenna, existing documented distinct exhale source')
 stable_text(P/'alternatives/spin_prepare_selection.json',json.dumps(report,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':restore_selected()
