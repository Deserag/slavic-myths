"""Local vanilla OLD playback, licensed NEW candidates. No mod/profile writes."""
from pathlib import Path
import json, subprocess, hashlib, html, csv
R=Path(__file__).resolve().parents[1];O=R/'docs/audio-review'
P=Path('C:/Users/pavel/AppData/Roaming/PolyMC');cache=R/'.tools/audio-review-62-local';cache.mkdir(exist_ok=True)
assets=json.loads((P/'assets/indexes/17.json').read_text())['objects']
def object_path(key):
 h=assets[key]['hash'];p=P/'assets/objects'/h[:2]/h
 assert hashlib.sha1(p.read_bytes()).hexdigest()==h
 return p
vanilla=json.loads(object_path('minecraft/sounds.json').read_text())
current=json.loads((R/'src/main/resources/assets/slavicmyths/sounds.json').read_text(encoding='utf-8'))
def resolve(event, pitch=1, seen=None):
 seen=set() if seen is None else seen
 assert event not in seen;seen.add(event)
 item=vanilla[event]['sounds'][0];item={'name':item} if isinstance(item,str) else item
 pitch*=item.get('pitch',1)
 if item.get('type')=='event':return resolve(item['name'].removeprefix('minecraft:'),pitch,seen)
 return object_path('minecraft/sounds/'+item['name'].removeprefix('minecraft:')+'.ogg'),pitch
rows=[
 ('Тугарин','Босс, фаза 1','Боевой вызов','tugarin_phase1_aggro','entity.ravager.roar',.7,'art/audio/pack-a-60/voice-rework/alternatives/tugarin_challenge_rough.ogg','APPROVED_EXACT_ROUGH'),
 ('Ским-зверь','Враждебный босс, фаза 2+','Атакующий рёв','skim.roar','entity.ravager.roar',1,'art/audio/pack-a-60/voice-rework/preview/skim_roar.ogg','APPROVED_TIMBRE'),
 ('Серый Волк','Мифический хищник, добровольное испытание','Предупреждающее рычание','grey_wolf.growl','entity.wolf.growl',.85,'art/audio/pack-a-60/voice-rework/preview/wolf_warning.ogg','CANDIDATE_DOMESTIC_DOG_NOT_WILD_WOLF'),
 ('Овинник','Враждебный дух овина','Яростный крик','ovinnik_angry','entity.ravager.roar',1,'','NO_VOICE_SOURCE_YET_FIRE_IS_SEPARATE_FOLEY'),
 ('Идолище Поганое','Босс','Ярость','idolishche_poganoe.rage_01','entity.ravager.roar',1,'','NO_CANDIDATE'),
 ('Упырь','Курганный противник','Удар','upyr_attack','entity.player.attack.sweep',1,'','NO_CANDIDATE'),
 ('Навь','Курганный противник','Атака','nav_attack','entity.player.attack.sweep',1,'','NO_CANDIDATE'),
 ('Курганный дружинник','Вооружённая нежить','Удар мечом','kurgan_druzhinnik_sword_attack','entity.player.attack.sweep',1,'','NO_CANDIDATE'),
 ('Курганный воевода','Элитная нежить','Смена фазы','kurgan_voevoda_phase_shift','entity.ravager.roar',1,'','NO_CANDIDATE'),
 ('Неупокоенный князь','Курганный босс','Смена фазы','unresting_prince_phase_shift','entity.ravager.roar',1,'','NO_CANDIDATE')]
index=['# Сравнения звуков #62 — не приёмка всей задачи','',
 'OLD: первая вариация соответствующего vanilla event, локально из Minecraft 1.21.1. Это воспроизведение исходника с указанным pitch, а не запись из игры. Для большинства строк динамический pitch Java ещё не доказан; см. таблицу. Никаких запусков. NEW — только названный конкретный кандидат.','',
 'Vanilla-примеры лежат только в игнорируемом .tools/audio-review-62-local; их нельзя включать в распространяемый пакет. Индекс зависит от локального кэша владельца; на другой машине запустить генератор с её легально установленными assets.','']
cards=[];manifest=[]
for mob,role,action,event,old,pitch,new,status in rows:
 source,internal=resolve(old);dest=cache/(event.replace('.','_')+'.ogg');factor=pitch*internal
 subprocess.run(['ffmpeg','-v','error','-y','-i',str(source),'-af',f'asetrate=44100*{factor},aresample=44100','-ac','1','-c:a','libvorbis','-q:a','4',str(dest)],check=True)
 assert event in current,event
 actual=current[event]['sounds']
 definition=json.dumps(actual,ensure_ascii=False)
 index += [f'## {mob} · {role} · {action}','',f'Event: `{event}`. Текущая definition: `{definition}`.','',f'OLD reference: `{old}`, illustrative pitch {factor:g}; случайная вариация/громкость Java не эмулируются.','',f'![{mob} — OLD {action}]({dest.as_posix()})','',f'NEW: **{status}**.']
 newpath=R/new if new else None
 if newpath:index += ['',f'![{mob} — NEW {action}]({newpath.as_posix()})']
 index += ['','Слуховая оценка этого сравнения: **NOT_LISTENED**; одобрение отдельных NEW отражено в acceptance.json.','']
 player=lambda path:f'<audio controls preload="none" src="{html.escape(path.as_uri())}"></audio>'
 cards.append(f'<section><h2>{html.escape(mob)} — {html.escape(action)}</h2><p>{html.escape(role)} · {html.escape(event)}</p><p>OLD reference · pitch {factor:g}</p>{player(dest)}<p>NEW · {html.escape(status)}</p>{player(newpath) if newpath else "<p>Нового голосового файла пока нет.</p>"}</section>')
 manifest.append(dict(mob=mob,event=event,old=old,old_sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),new=new,new_sha256=hashlib.sha256(newpath.read_bytes()).hexdigest() if newpath else '',status=status,hearing='NOT_LISTENED'))
for key,label in [('tugarin_breath_rough','Тяжёлое дыхание в ожидании боя'),('tugarin_attack_voice_rough','Голосовое усилие при ударе')]:
 path=R/'art/audio/pack-a-60/voice-rework/alternatives'/(key+'.ogg')
 if path.exists():
  index += [f'## Следующий кандидат: Тугарин · босс, фаза 1 · {label}','','CANDIDATE_NOT_WIRED. Отдельный исходный take, обработанный в выбранном направлении; одобрение боевого вызова не распространяется на этот файл.','',f'![Тугарин — {label}]({path.as_posix()})','']
  cards.append(f'<section><h2>Тугарин — {html.escape(label)}</h2><p>Босс, человеческая форма, фаза 1. CANDIDATE_NOT_WIRED.</p>{player(path)}</section>')
(O/'PREVIEW_INDEX.md').write_text('\n'.join(index),encoding='utf-8')
(O/'soundboard.html').write_text('<!doctype html><meta charset="utf-8"><title>Slavic Myths — audio #62</title><style>body{font:17px system-ui;background:#182027;color:#eee;max-width:1000px;margin:30px auto}section{padding:20px;border:1px solid #61727e;margin:15px 0}audio{width:100%}</style><h1>OLD / NEW — звуковые сравнения #62</h1><p>Локальные исходники, не запись из Minecraft. OLD: первая вариация vanilla; Java pitch/volume/state требуют проверки. NEW активируется только по отдельному одобрению. Нет autoplay или сетевых запросов.</p>'+''.join(cards),encoding='utf-8')
with (O/'PREVIEW_MANIFEST.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(manifest[0]));w.writeheader();w.writerows(manifest)
print('10 labeled OLD comparisons; 3 concrete NEW clips; 7 explicit missing candidates. No launches or production writes.')
