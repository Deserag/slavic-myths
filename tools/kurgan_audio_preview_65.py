"""Portable licensed NEW review + local-only vanilla OLD. No game capture claim."""
from pathlib import Path
import sys,json,html,hashlib,subprocess,zipfile,io
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np,soundfile as sf
from audio_60_foley import encode,stable_text,RATE
from kurgan_audio_65 import A,P,PLAN,KEEP,KINDS,role

def main():
 baseline=json.loads(subprocess.check_output(['git','show','5c93408e:src/main/resources/assets/slavicmyths/sounds.json'],cwd=R).decode('utf-8'));current=json.loads((A/'sounds.json').read_text(encoding='utf-8'));rows=json.loads((P/'selection.json').read_text(encoding='utf-8'))
 polymc=Path('C:/Users/pavel/AppData/Roaming/PolyMC');assets=json.loads((polymc/'assets/indexes/17.json').read_text())['objects']
 def obj(key):
  h=assets[key]['hash'];p=polymc/'assets/objects'/h[:2]/h;assert hashlib.sha1(p.read_bytes()).hexdigest()==h;return p
 vanilla=json.loads(obj('minecraft/sounds.json').read_text());cache=R/'.tools/kurgan65-old';cache.mkdir(exist_ok=True)
 def resolve(event):
  s=vanilla[event]['sounds'][0];s={'name':s} if isinstance(s,str) else s
  return resolve(s['name'].removeprefix('minecraft:')) if s.get('type')=='event' else obj('minecraft/sounds/'+s['name'].removeprefix('minecraft:')+'.ogg')
 old={};new={};index=['# Шесть курганных типов — полный sound policy','', 'AGENT_SELECTED / OWNER_PENDING. Новые записи не являются принятыми на слух. Все47 старых событий рассмотрены: 41 замена,6 KEEP;6 новых сигналов контакта. Игровые запуски0.','', 'OLD — локальная реконструкция первой vanilla-вариации, не запись Minecraft. Эти файлы не распространяются. NEW — лицензированные отдельные записи. Demo — офлайн-монтаж source tick schedule, не игровой тест.','']
 cards=[];portable=[];coverage=[]
 audio=lambda url:f'<audio controls preload="none" src="{html.escape(url,quote=True)}"></audio>'
 for kind in KINDS:
  section=[];portable_section=[];events=[e for e in current if e.startswith(kind+'_')]
  for event in events:
   definition=baseline.get(event,{});oldpath=None
   if definition.get('sounds'):
    first=definition['sounds'][0];first={'name':first} if isinstance(first,str) else first;source=resolve(first['name'].removeprefix('minecraft:'));dest=cache/(event+'.ogg');factor=first.get('pitch',1)*(.9 if event.endswith('_step') else .95)
    raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-af',f'aresample=44100,asetrate=44100*{factor},aresample=44100,volume={first.get("volume",1)}','-f','f32le','-ac','1','pipe:1']);x=np.frombuffer(raw,dtype='<f4').astype(float);encode(dest,x);old[event]=dest;oldpath=dest
   selected=[r for r in rows if r['event']==event]
   if selected:new[event]=A/'sounds'/selected[0]['file']
   decision='AGENT_SELECTED_OWNER_PENDING' if selected else KEEP[event]
   index.extend([f'## {event}','',f'{role(event)} · {decision}',''])
   if oldpath:index.extend([f'![OLD {event}]({oldpath.as_posix()})',''])
   for row in selected:index.extend([f"![NEW {event} · {Path(row['source']).name}]({(A/'sounds'/row['file']).as_posix()})",''])
   section.append(f'<article><h3>{event}</h3><p>{html.escape(role(event))} · {html.escape(decision)}</p><p>OLD — first vanilla variant, source pitch approximation</p>{audio(oldpath.as_uri()) if oldpath else "No prior contact event"}<p>NEW</p>'+''.join(audio((A/'sounds'/r['file']).as_uri()) for r in selected)+'</article>')
   portable_section.append(f'<article><h3>{event}</h3><p>{html.escape(role(event))} · {html.escape(decision)}</p>'+(''.join(audio('new/'+Path(r['file']).name) for r in selected) if selected else '<p>Vanilla reference kept; not redistributed.</p>')+'</article>')
   coverage.append(dict(mob=kind,event=event,role=role(event),old=json.dumps(definition,ensure_ascii=False),new=';'.join(r['file'] for r in selected),sha256=';'.join(r['sha256'] for r in selected),decision=decision,callsite='kurgan/KurganBolt.java onHitEntity/onHitBlock' if event=='buried_volkhv_impact' else 'kurgan/KurganCreature.java '+role(event),owner_hearing='NOT_RUN'))
  # Whole 22s sequence includes all available roles. Kept vanilla vocals are
  # deliberately absent from distributable NEW audio, available in local OLD.
  basic={'upyr':('attack',5),'nav':('attack',5),'kurgan_druzhinnik':('sword_attack',6),'kurgan_voevoda':('axe_attack',6),'buried_volkhv':('cast_basic',8),'unresting_prince':('combo',6)}[kind]
  special={'upyr':('leap',10),'nav':('shift',8),'kurgan_druzhinnik':('shield_bash',10),'kurgan_voevoda':('shield_charge',12),'buried_volkhv':('cast_ground_seal',28),'unresting_prince':('heavy_strike',18)}[kind]
  timeline=[(0,'ambient'),(3,'step'),(5,basic[0]),(5+basic[1]/20,'impact'),(9,special[0]),(9+special[1]/20,'spectral_appear' if kind=='nav' else 'impact'),(13,'hurt'),(16,'phase_shift' if kind in ['kurgan_voevoda','unresting_prince'] else 'step'),(19,'death')]
  montage=np.zeros(22*RATE);oldmontage=np.zeros_like(montage);schedule=[]
  for start,cue in timeline:
   event=kind+'_'+cue;path=new.get(event);previous=old.get(event)
   schedule.append(dict(seconds=start,event=event,new_present=path is not None,note='Collision time illustrative for projectile/charge; animation facing delays not simulated' if cue=='impact' else 'source callback/tell schedule illustration'))
   for sound,dest in [(path,montage),(previous,oldmontage)]:
    if sound:
     x,sr=sf.read(sound);assert sr==RATE;x=x.mean(axis=1) if x.ndim>1 else x;offset=int(start*RATE);n=min(len(x),len(dest)-offset);dest[offset:offset+n]+=x[:n]*.7
  assert max(abs(montage))<.95
  preview=P/'preview';preview.mkdir(exist_ok=True);encode(preview/(kind+'_sequence.ogg'),montage);encode(cache/(kind+'_sequence.ogg'),oldmontage)
  stable_text(preview/(kind+'_timeline.json'),json.dumps(schedule,indent=2)+'\n')
  index.extend([f'## {kind}: последовательность22s','','Сборка записей по source timing, не runtime. Для KEEP-вокала NEW-монтаж оставляет паузу; vanilla не включается в распространяемый архив.',f"![{kind} последовательность]({(preview/(kind+'_sequence.ogg')).as_posix()})",''])
  cards.append(f'<details open><summary>{kind}</summary><h3>22s source-timed demonstration (not runtime)</h3>{audio((preview/(kind+"_sequence.ogg")).as_uri())}'+''.join(section)+'</details>')
  portable.append(f'<details open><summary>{kind}</summary>{audio("demo/"+kind+"_sequence.ogg")}'+''.join(portable_section)+'</details>')
 style='<style>body{font:16px system-ui;background:#172126;color:#ece6ce;max-width:1080px;margin:24px auto}article,details{border:1px solid #607179;padding:16px;margin:14px 0}audio{width:100%;max-width:650px}summary{font-size:24px}</style>'
 head='<!doctype html><meta charset="utf-8"><title>Kurgan audio review</title>'+style+'<h1>Six kurgan characters — OWNER_PENDING</h1><p>Recorded voices/props; no game capture. Listen to exact variants and compare roles. Vanilla OLD remains local only. Nav/Volkhv retained vocals omitted from exported NEW demos.</p>'
 stable_text(P/'PREVIEW.md','\n'.join(index));stable_text(P/'soundboard.html',head+''.join(cards))
 from audio_project_61 import write
 write('KURGAN_EVENT_COVERAGE.csv',coverage)
 # Only licensed new clips + attribution enter the portable review archive.
 target=P/'kurgan-review.zip';buffer=io.BytesIO()
 with zipfile.ZipFile(buffer,'w',zipfile.ZIP_DEFLATED) as z:
  z.writestr('index.html',head+''.join(portable));z.writestr('LICENSES.txt',(A/'audio_licenses/kurgan-65.txt').read_text(encoding='utf-8'))
  for row in rows:z.write(A/'sounds'/row['file'],'new/'+Path(row['file']).name)
  for p in sorted(preview.iterdir()):z.write(p,'demo/'+p.name)
  z.write(P/'sources.json','sources.json');z.write(P/'selection.json','selection.json')
 target.write_bytes(buffer.getvalue());print('Six source-timed 22s NEW demos; 53-event A/B board; licensed portable ZIP. OLD cache private. No runtime.')
if __name__=='__main__':main()
