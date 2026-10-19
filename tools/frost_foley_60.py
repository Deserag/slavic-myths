"""Recorded prop foley for existing frost/night events; no voices or visual edits."""
from pathlib import Path
import sys,json,hashlib,subprocess
R=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np
import soundfile as sf
from audio_60_foley import encode,stable_text,RATE
A=R/'src/main/resources/assets/slavicmyths';P=R/'art/audio/pack-a-60/frost-foley'
# Each entry is a different recorded take, not a pitch-shifted copy.
# event, source, duration cap, RMS, label, exact source callsite
SPECS=[
 ('karachun.scythe_windup','swosh-04.flac',.42,.09,'Коса: первый взмах','karachun/Karachun.java action(1/2/3), hit10/10/14'),
 ('karachun.scythe_windup','swosh-05.flac',.42,.09,'Коса: второй взмах','karachun/Karachun.java action(1/2/3), hit10/10/14'),
 ('karachun.scythe_hit','metal_hit_01.ogg',.6,.11,'Коса: металлический удар 1','karachun/Karachun.java castTick age10/10/14'),
 ('karachun.scythe_hit','metal_hit_03.ogg',.6,.11,'Коса: металлический удар 2','karachun/Karachun.java castTick age10/10/14'),
 ('morozko.staff_hit','wood_hit_03.ogg',.38,.12,'Посох: деревянный удар','morozko/Morozko.java castTick a1 age10'),
 ('morozko.ice_bolt','bfh1_glass_hit_01.ogg',.35,.08,'Ледяной снаряд босса: звон при вылете','morozko/Morozko.java castTick a2 age13'),
 ('morozko.staff_shoot','bfh1_glass_hit_02.ogg',.35,.08,'Посох игрока: вылет осколка','morozko/FrostSkills.java shoot'),
 ('morozko.spike_break','bfh1_glass_breaking_01.ogg',.65,.10,'Лёд: треск при ударе снаряда или появлении шипа','morozko/FrostShard.java onHit; FrostHazard.java kind0 a0'),
 ('morozko.clone_break','bfh1_glass_breaking_02.ogg',.85,.10,'Ледяной двойник: разрушение','morozko/IceDouble.java dissolve; Morozko.java die'),
 ('morozko.mirror_break','bfh1_glass_breaking_03.ogg',.7,.09,'Зеркальная копия игрока: разрушение','morozko/PlayerDouble.java shatter'),
 ('morozko.staff_icicles','bfh1_glass_falling_01.ogg',.45,.065,'Сосулька: короткий звон при появлении','morozko/FrostHazard.java kind1 a0'),
]
EVENTS={row[0] for row in SPECS}

def restore_selected():
 sources={Path(s['file']).name:s for s in json.loads((P/'sources.json').read_text())}
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));rows=[];bindings={}
 for event,member,cap,target,label,callsite in SPECS:
  spec=sources[member];source=P/spec['file'];assert hashlib.sha256(source.read_bytes()).hexdigest()==spec['sha256']
  # No pitch change. Trim recording lead-in to keep foley at the existing hit tick.
  raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-af','highpass=f=90,lowpass=f=10000','-f','f32le','-ar',str(RATE),'-ac','1','pipe:1'])
  x=np.frombuffer(raw,dtype='<f4').astype(float).copy();x-=x.mean()
  active=np.flatnonzero(abs(x)>max(abs(x))*.025);assert len(active)
  start=max(0,int(active[0])-220);x=x[start:start+int(cap*RATE)]
  fade=min(220,len(x)//8);x[:fade]*=np.linspace(0,1,fade);x[-fade:]*=np.linspace(1,0,fade)
  x*=min(target/max(np.sqrt(np.mean(x*x)),1e-12),.6/max(abs(x)))
  mob=event.split('.')[0];name=f'foley/{mob}/{Path(member).stem}';dest=A/'sounds'/(name+'.ogg');dest.parent.mkdir(parents=True,exist_ok=True);encode(dest,x)
  y,sr=sf.read(dest);assert sr==RATE and y.ndim==1 and np.isfinite(y).all() and max(abs(y))<.8
  entry={'name':name,'attenuation_distance':24 if mob=='morozko' else 32}
  if event=='morozko.staff_icicles':entry['volume']=.55 # Five close emitters share a cast.
  bindings.setdefault(event,[]).append(entry)
  rows.append(dict(event=event,file=name+'.ogg',label=label,callsite=callsite,source=spec['file'],source_sha256=spec['sha256'],sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),seconds=len(y)/sr,rms_dbfs=float(20*np.log10(np.sqrt(np.mean(y*y)))),peak_dbfs=float(20*np.log10(max(abs(y)))),trim_start_samples=start,processing='90Hz highpass/10kHz lowpass, mono44100, leading silence trim, crop, 5ms fades, gain; no pitch shift',material='recorded bamboo/metal/wood/glass prop, glass represents fantasy ice; not voice',status='AGENT_SELECTED_UNDER_OWNER_DELEGATION',owner_approved=False,hearing='NOT_LISTENED'))
 for event,values in bindings.items():
  events[event]={'subtitle':events[event]['subtitle'],'sounds':values}
 stable_text(A/'sounds.json',json.dumps(events,ensure_ascii=False,indent=2)+'\n')
 stable_text(P/'selection.json',json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
 stable_text(A/'audio_licenses/frost-foley-60.txt','qubodup: recorded bamboo swishes, https://opengameart.org/content/swish-bamboo-stick-weapon-swhoshes\nrubberduck: metal/wood impacts, https://opengameart.org/content/100-cc0-metal-and-wood-sfx ; glass props, https://opengameart.org/content/75-cc0-breaking-falling-hit-sfx\nAll sources CC0-1.0 https://creativecommons.org/publicdomain/zero/1.0/ . Glass is prop foley for fantasy ice, not a claim of recorded real ice. Trim/EQ/gain/fades, no pitch shift. Separate recorded takes. Provenance, archive/member hashes: art/audio/pack-a-60/frost-foley/sources.json; exact edits: selection.json. No author endorsement implied.\n')
 preview=['# #60 / #64 — коса, посох и ледяные объекты','', 'Записанный предметный foley, не голоса. Стеклянные props изображают фантастический лёд. Выбрано по делегированию; прослушивание и игровая приёмка NOT_RUN. Геометрия #58 сохранена.','']
 for row in rows:
  mob,cue=row['event'].split('.');previous=f'{mob}/{cue}'+('_0' if mob=='karachun' else '')+'.ogg'
  preview += [f"## {row['label']}",'',f"Event `{row['event']}` → `{row['file']}` → `{row['callsite']}`. {row['seconds']:.3f}s.",'',f"A — прежний файл (только сравнение): ![Прежний {row['label']}]({(A/'sounds'/previous).as_posix()})",'',f"B — новая запись: ![{row['label']}]({(A/'sounds'/row['file']).as_posix()})",'']
 stable_text(P/'PREVIEW.md','\n'.join(preview))
 return rows

if __name__=='__main__':
 rows=restore_selected();print(f'{len(rows)} recorded files / {len(EVENTS)} existing events. IN_PROGRESS; no runtime or owner acceptance.')
