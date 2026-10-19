"""Labeled exact-file review, not a recording of Minecraft or owner acceptance."""
from pathlib import Path
import json,html,zipfile,io,sys,subprocess
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'tools'))
from audio_groups_61 import A,P
from audio_groups_61_plan import PLAN,ALIASES,BLOCKED,DEFERRED
from audio_60_foley import stable_text

def main():
 polymc=Path('C:/Users/pavel/AppData/Roaming/PolyMC/assets');objects=json.loads((polymc/'indexes/17.json').read_text())['objects']
 def obj(key):
  h=objects[key]['hash'];p=polymc/'objects'/h[:2]/h;assert __import__('hashlib').sha1(p.read_bytes()).hexdigest()==h;return p
 vanilla=json.loads(obj('minecraft/sounds.json').read_bytes())
 def resolve(entry,seen=()):
  if isinstance(entry,str):entry={'name':entry}
  name=entry['name'];namespace,path=name.split(':',1) if ':' in name else ('minecraft',name)
  if entry.get('type')=='event':
   if name in seen:return None
   values=(vanilla if namespace=='minecraft' else baseline).get(path,{}).get('sounds',[])
   return resolve(values[0],seen+(name,)) if values else None
  return obj('minecraft/sounds/'+path+'.ogg') if namespace=='minecraft' else A/'sounds'/(path+'.ogg')
 rows=json.loads((P/'selection.json').read_text(encoding='utf-8'));events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));baseline=json.loads(subprocess.check_output(['git','show','ee4a40be:src/main/resources/assets/slavicmyths/sounds.json'],cwd=R));cards=[];portable=[];md=['# Общий аудиопроект: точные записи нового этапа','', 'AGENT_SELECTED / OWNER_PENDING. Прослушивание владельцем NOT_LISTENED; Minecraft NOT_RUN. Это файлы, не игровая запись. Назначение каждого звука подписано. Vanilla в ZIP не распространяется.','']
 for group in dict.fromkeys(c['group'] for c in PLAN.values()):
  local=[f'<h2>{group}</h2>'];export=list(local);md+=['## '+group,'']
  for e,c in PLAN.items():
   if c['group']!=group:continue
   rr=[r for r in rows if r['event']==e];label=f'{c["ru"]} / {c["en"]}';body=f'<article><h3>{html.escape(label)}</h3><code>{e}</code><p>AGENT_SELECTED / OWNER_PENDING</p>'
   ls=body;ps=body
   for r in rr:
    path=A/'sounds'/r['file'];rel='new/'+r['file'];detail=html.escape('; '.join(x['author']+' — '+x['license'] for x in r['sources']))
    ls+=f'<p>{detail} · {r["seconds"]:.2f}s</p><audio controls preload="none" src="{path.as_uri()}"></audio>'
    ps+=f'<p>{detail} · {r["seconds"]:.2f}s</p><audio controls preload="none" src="{rel}"></audio>'
    md+=[f'**{label}** · `{e}`',f'![{label}]({path.as_posix()})','']
   old=baseline.get(e,{}).get('sounds',[])
   if e in {'cub_hurt','cub_death'}:
    old=[{'name':'minecraft:entity.polar_bear.'+('hurt' if e.endswith('hurt') else 'death'),'type':'event'}]
    ls+='<p>OLD actual1.9.7.15: adult vanilla polar-bear fallback.</p>'
   elif e=='karachun.voice_whisper':
    old=[]
    ls+='<p>OLD actual1.9.7.15: muted quarantine; no rejected tone playback. NEW real single male whisper.</p>'
   if e in {'volkolak_hunt_rage','volkolak_hunt_howl'}:
    old=[{'name':'minecraft:entity.ravager.roar','type':'event'}]
    ls+='<p>OLD direct HuntMob Java cue: volume0.85, pitch0.65. NEW ordinary Volkolak override: volume0.85, pitch1.0. Below OLD file plays without the old pitch/gain.</p>'
   previous=resolve(old[0]) if old else None
   if previous:ls+=f'<p>OLD — исходная первая вариация, без воспроизведения старого pitch/gain; не игровой захват</p><audio controls preload="none" src="{previous.as_uri()}"></audio>'
   ls+='<details><summary>OLD: прежнее назначение (JSON, не запись игры)</summary><pre>'+html.escape(json.dumps(old,ensure_ascii=False,indent=2))+'</pre></details>'
   local.append(ls+'</article>');export.append(ps+'</article>')
  cards+=local;portable+=export
 cards.append('<h2>Vanilla references — local playback only</h2>');portable.append('<h2>Vanilla references — not redistributed</h2>')
 translations=json.loads((A/'lang/ru_ru.json').read_text(encoding='utf-8'))
 for e,c in ALIASES.items():
  label=c.get('ru',translations.get(events[e].get('subtitle',''),e));path=resolve(events[e]['sounds'][0]);body=f'<article><h3>{html.escape(label)}</h3><code>{e}</code><p>{html.escape(c["target"])} — {html.escape(c["reason"])}</p>'
  cards.append(body+f'<audio controls preload="none" src="{path.as_uri()}"></audio></article>');portable.append(body+'<p>Built-in vanilla reference; use local board for audition.</p></article>')
 gaps='<h2>Реальные пробелы</h2>'+''.join(f'<p><code>{e}</code>: {html.escape(reason)}</p>' for e,reason in BLOCKED.items() if e not in DEFERRED)+ '<h2>Отложено владельцем, текущая озвучка сохранена</h2>'+''.join(f'<p><code>{e}</code>: {html.escape(reason)}</p>' for e,reason in DEFERRED.items())
 head='<!doctype html><meta charset="utf-8"><title>Slavic Myths audio #61</title><style>body{font:16px system-ui;background:#172126;color:#eee;max-width:1100px;margin:24px auto}article{padding:16px;border:1px solid #657;margin:12px}audio{display:block;width:95%}code{overflow-wrap:anywhere}</style><h1>Полная карта нового аудиоэтапа</h1><p>AGENT_SELECTED / OWNER_PENDING. Exact playable files; no game capture, no owner acceptance. Общие физические материалы могут повторяться; голосовая индивидуальность требует прослушивания.</p>'
 stable_text(P/'soundboard.html',head+''.join(cards)+gaps);stable_text(P/'PREVIEW.md','\n'.join(md))
 auditions=__import__('audio_state_auditions_61').render()
 buf=io.BytesIO()
 with zipfile.ZipFile(buf,'w',zipfile.ZIP_DEFLATED) as z:
  z.writestr('index.html',head+''.join(portable)+gaps);z.write(A/'audio_licenses/project-61.txt','LICENSES.txt')
  for r in rows:z.write(A/'sounds'/r['file'],'new/'+r['file'])
  for name in ['sources.json','selection.json','aliases.json','blocked.json','deferred.json']:z.write(P/name,name)
  for path in sorted(auditions.iterdir()):z.write(path,'auditions/'+path.name)
 (P/'review.zip').write_bytes(buf.getvalue());print(len(rows),'labeled playable variants; portable licensed review ZIP; runtime NOT_RUN')
if __name__=='__main__':main()
