"""Labeled offline file sequences. Never game recordings or runtime evidence."""
from pathlib import Path
import sys,json,hashlib,html
R=Path(__file__).resolve().parents[1]
sys.path[:0]=[str(R/'.tools/audio-libs'),str(R/'tools')]
import numpy as np,soundfile as sf
from audio_60_foley import encode,RATE,stable_text
P=R/'art/audio/project-61';A=R/'src/main/resources/assets/slavicmyths/sounds'
SEQUENCES={
 'nightingale':[(0,'nightingale_phase1_idle_whistle',1),(3,'nightingale_phase1_aggro',1),(5,'nightingale_phase1_whistle_charge',1),(6,'nightingale_phase1_whistle_release',1),(9,'nightingale_phase1_slash',1),(11,'nightingale_phase1_hurt',1),(13,'nightingale.phase1.land',1),(15,'nightingale_phase2_laugh',1),(19,'nightingale_phase2_dash',1),(21,'nightingale_phase2_wave',1),(24,'nightingale_phase2_whirlwind',1),(27,'nightingale_phase2_death',1),(30,'nightingale_blade_wind_release',1)],
 'nochnitsa':[(0,'nochnitsa.ambient',1),(3,'nochnitsa.breath',1),(6,'nochnitsa.target_spotted',1),(9,'nochnitsa.scream_inhale',1),(10,'nochnitsa.scream_release',1),(13,'nochnitsa.scream_interrupt',1),(16,'nochnitsa.hurt',1),(19,'nochnitsa.death',1)],
 'ilya':[(0,'ilya_muromets.ambient_breath_01',1),(3,'ilya_muromets.mace_swing_01',1),(5,'ilya_muromets.mace_impact_01',1),(8,'ilya_muromets.pain_nonverbal_01',1),(11,'ilya_muromets.retreat_breath_01',1),(15,'ilya_muromets.recover_breath_01',1),(18,'ilya_muromets.shield_block_01',1)],
 'dobrynya':[(0,'dobrynya.breath_idle_01',1),(3,'dobrynya.armor_step_01',1),(5,'dobrynya.armor_step_02',1),(7,'dobrynya.spear_swing_01',1),(9,'dobrynya.spear_swing_02',1),(11,'dobrynya.spear_impact_01',1),(14,'dobrynya.grunt_hurt_01',1),(17,'dobrynya.elixir_drink_01',1)],
 'material_variants':[(0,'marinka.magpie_wings_01',1),(3,'marinka.magpie_wings_02',1),(6,'entity.finist.sword_swing_1',1),(9,'entity.finist.sword_swing_2',1),(12,'entity.finist.sword_contact',1),(15,'entity.finist.hurt_1',1),(18,'entity.finist.hurt_2',1)],
 'finist_magic':[(0,'entity.finist.summon_chime',1),(4,'entity.finist.mark_cast',1),(8,'entity.finist.inspire',1),(12,'entity.finist.sword_swing_1',1),(15,'entity.finist.sword_contact',1),(18,'entity.finist.victory',1)],
}

def render():
 rows=json.loads((P/'selection.json').read_text(encoding='utf-8'));folder=P/'auditions';folder.mkdir(exist_ok=True);cards=[]
 for name,sequence in SEQUENCES.items():
  duration=max(22,sequence[-1][0]+4);mix=np.zeros(duration*RATE);index=[]
  for start,event,variant in sequence:
   row=next(r for r in rows if r['event']==event and r['file'].endswith('_'+str(variant)+'.ogg'));source=A/row['file']
   assert hashlib.sha256(source.read_bytes()).hexdigest()==row['sha256']
   x,sr=sf.read(source);assert sr==RATE and x.ndim==1;offset=start*RATE;assert offset+len(x)<=len(mix)
   mix[offset:offset+len(x)]+=x;index.append(dict(time_s=start,event=event,label=row['ru'],file=row['file'],sha256=row['sha256']))
  assert max(abs(mix))<.8;dest=folder/(name+'.ogg');encode(dest,mix)
  report=dict(status='AGENT_SELECTED_OWNER_PENDING',kind='OFFLINE_FILE_AUDITION_NOT_GAMEPLAY',duration_s=duration,sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),runtime='NOT_RUN',hearing='NOT_LISTENED',events=index)
  stable_text(folder/(name+'.json'),json.dumps(report,ensure_ascii=False,indent=2)+'\n')
  cards.append('<section><h2>'+name+'</h2><audio controls preload="none" src="'+name+'.ogg"></audio><ul>'+''.join('<li>'+str(r['time_s'])+'s — '+html.escape(r['label'])+' <code>'+r['event']+'</code></li>' for r in index)+'</ul></section>')
 head='<!doctype html><meta charset="utf-8"><title>Подписанные аудиопоследовательности #61</title><style>body{font:17px system-ui;max-width:950px;margin:30px auto;background:#172126;color:#eee}audio{width:100%}section{border:1px solid #687;padding:16px;margin:20px 0}li{margin:8px}</style><h1>Подписанные последовательности записанных файлов</h1><p>AGENT_SELECTED / OWNER_PENDING. Это офлайн-подборка, не игровой захват и не доказательство слышимости или боевых таймингов. Варианты подписаны; авторы, URL, лицензии и SHA — sources.json/selection.json. Hearing NOT_LISTENED; runtime NOT_RUN.</p>'
 stable_text(folder/'index.html',head+''.join(cards))
 return folder

if __name__=='__main__':render();print(f'{len(SEQUENCES)} labeled offline auditions; no game launch or production write')
