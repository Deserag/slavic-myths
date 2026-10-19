"""Recorded voice/foley batch. Delegated selection, no claimed hearing or runtime."""
from pathlib import Path
import sys,json,io,zipfile,urllib.request,hashlib,subprocess,shutil
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'));sys.path.insert(0,str(R/'tools'))
import numpy as np,soundfile as sf
from audio_60_foley import encode,RATE,stable_text
P=R/'art/audio/pack-a-60/voice-rework';A=R/'src/main/resources/assets/slavicmyths'
# Different performed takes, never one grunt recolored into multiple characters.
SPECS=[
 ('ovinnik_ambient','ovinnik',1,.055,.5,'Редкое сухое дыхание',1),
 ('ovinnik_hurt','ovinnik',3,.14,.40,'Короткая реакция на боль',1),
 ('ovinnik_death','ovinnik',4,.13,.8,'Последний хрип',1),
 ('ovinnik_angry','ovinnik',2,.16,.76,'Угрожающий выдох перед атакой',1),
 ('ovinnik_power','ovinnik',6,.18,1.27,'Яростный гортанный выброс',1),
 ('idolishche_poganoe.idle_growl_01','idolishche',5,.065,.94,'Низкое ворчание в ожидании',.84),
 ('idolishche_poganoe.idle_growl_02','idolishche',10,.065,1.15,'Второе ворчание',.84),
 ('idolishche_poganoe.hurt_01','idolishche',9,.15,1.25,'Реакция боли 1',.84),
 ('idolishche_poganoe.hurt_02','idolishche',12,.15,.83,'Реакция боли 2',.84),
 ('idolishche_poganoe.rage_01','idolishche',8,.19,1.67,'Переход в ярость',.84),
 ('idolishche_poganoe.roar_01','idolishche',11,.18,1.68,'Угроза при начале боя',.84),
 ('idolishche_poganoe.death_01','idolishche',16,.14,1.20,'Предсмертный хрип',.84),
 ('skim.hurt_1','skim',13,.15,.55,'Короткая реакция боли 1',1),
 ('skim.hurt_2','skim',14,.15,.55,'Короткая реакция боли 2',1)]
def restore_selected():
 manifest=json.loads((P/'sources.json').read_text(encoding='utf-8'));archive=None;report=[]
 for event,mob,take,target,end,action,pitch in SPECS:
  source=P/f'sources/starninjas_monster_{take}.ogg';member=f'monster.{take}.ogg'
  if not source.exists():
   if archive is None:
    b=urllib.request.urlopen('https://opengameart.org/sites/default/files/monster_-_starninjas.zip',timeout=30).read();assert hashlib.sha256(b).hexdigest()=='7542446ebc69892e4267d8b3abd3167193505833692da9624c01c8da392d9065';archive=zipfile.ZipFile(io.BytesIO(b))
   source.write_bytes(archive.read(member))
  h=hashlib.sha256(source.read_bytes()).hexdigest();entry=next((r for r in manifest if r.get('name')==f'starninjas_monster_{take}'),None)
  if entry:assert entry['sha256']==h
  else:manifest.append(dict(name=f'starninjas_monster_{take}',file=source.relative_to(P).as_posix(),author='StarNinjas',page='https://opengameart.org/content/16-monster-growls',license='CC0-1.0',license_url='https://creativecommons.org/publicdomain/zero/1.0/',download_url='https://opengameart.org/sites/default/files/monster_-_starninjas.zip',download_sha256='7542446ebc69892e4267d8b3abd3167193505833692da9624c01c8da392d9065',archive_member=member,sha256=h,source_description='Author states own mouth-performed monster growls/grunts',verified_date='2026-10-18'))
  # Pitch treatment only for the giant, using separate takes from every other mob.
  filters=f'rubberband=pitch={pitch}:formant=shifted,highpass=f=75,lowpass=f=4600' if pitch!=1 else 'highpass=f=100,lowpass=f=6000'
  raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-af',filters,'-f','f32le','-ar',str(RATE),'-ac','1','pipe:1'])
  x=np.frombuffer(raw,dtype='<f4').astype(float)[:int(end*RATE)].copy();x-=x.mean();fade=min(220,len(x)//8);x[:fade]*=np.linspace(0,1,fade);x[-fade:]*=np.linspace(1,0,fade);x*=min(target/max(np.sqrt(np.mean(x*x)),1e-12),.78/max(np.max(abs(x)),1e-12))
  file='voice/'+mob+'/'+event.replace('idolishche_poganoe.','').replace('ovinnik_','').replace('skim.','')+'.ogg';dest=A/'sounds'/file;dest.parent.mkdir(parents=True,exist_ok=True);encode(dest,x);y,sr=sf.read(dest);assert sr==RATE and y.ndim==1 and np.isfinite(y).all() and max(abs(y))<.95
  report.append(dict(event=event,mob=mob,role='Враждебный дух/босс',action=action,layer='recorded voice',source=source.relative_to(P).as_posix(),source_sha256=h,file=file,sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),seconds=len(y)/sr,processing=filters+f'; crop{end}s, DC removal, 5ms fade, calibrated gain',status='AGENT_SELECTED_UNDER_OWNER_DELEGATION',owner_approved=False,hearing='NOT_LISTENED'))
 # Recorded body/fur movement, not substitute voices. Vanilla hurt is appropriate
 # to a wolf; reserve growl for warning instead of reusing it for pain or movement.
 for event,sourcekey,cap in [('grey_wolf.pounce','cloth-03',.35),('grey_wolf.dash','cloth-05',.45),('grey_wolf.claw','cloth-01',.25)]:
  source=R/f'art/audio/pack-a-60/sources/{sourcekey}.flac'
  original=next(r for r in json.loads((R/'art/audio/pack-a-60/sources.json').read_text()) if r['file']==f'sources/{sourcekey}.flac');assert hashlib.sha256(source.read_bytes()).hexdigest()==original['sha256']
  x,sr=sf.read(source);assert sr==RATE;x=x.mean(axis=1) if x.ndim>1 else x;x=x[:int(cap*RATE)].copy();x-=x.mean();f=min(220,len(x)//8);x[:f]*=np.linspace(0,1,f);x[-f:]*=np.linspace(1,0,f);x*=min(.08/max(np.sqrt(np.mean(x*x)),1e-12),.6/max(abs(x)))
  file='voice/grey_wolf/'+event.split('.')[1]+'.ogg';dest=A/'sounds'/file;dest.parent.mkdir(parents=True,exist_ok=True);encode(dest,x)
  report.append(dict(event=event,mob='grey_wolf',role='Мифический хищник',action={'pounce':'Шорох тела при прыжке','dash':'Резкое движение шерсти','claw':'Движение лапы'}[event.split('.')[1]],layer='recorded cloth prop foley, not voice',source=source.relative_to(R).as_posix(),source_sha256=hashlib.sha256(source.read_bytes()).hexdigest(),file=file,sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),seconds=len(x)/RATE,processing='crop, mono, DC removal, 5ms fade, gain; no pitch shift',status='AGENT_SELECTED_UNDER_OWNER_DELEGATION',owner_approved=False,hearing='NOT_LISTENED'))
 path=A/'sounds.json';events=json.loads(path.read_text(encoding='utf-8'))
 for row in report:
  old=events[row['event']];entry={'name':row['file'][:-4],'attenuation_distance':28 if row['mob']!='grey_wolf' else 20}
  if row['mob']=='idolishche':entry['volume']=.85
  events[row['event']]={'subtitle':old['subtitle'],'sounds':[entry]}
 events['grey_wolf.hurt']={'subtitle':events['grey_wolf.hurt']['subtitle'],'sounds':[{'name':'minecraft:entity.wolf.hurt','type':'event','pitch':.9}]}
 stable_text(path,json.dumps(events,ensure_ascii=False,indent=2)+'\n');stable_text(P/'sources.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n');stable_text(P/'batch-selection.json',json.dumps(report,ensure_ascii=False,indent=2)+'\n')
 for lang,text in [('ru_ru','Овинник тяжело выдыхает'),('en_us','Ovinnik breathes harshly')]:
  path=A/f'lang/{lang}.json';d=json.loads(path.read_text(encoding='utf-8'));d['subtitles.slavicmyths.ovinnik_ambient']=text;stable_text(path,json.dumps(d,ensure_ascii=False,indent=2)+'\n')
 credits='StarNinjas — 16 Monster Growls, https://opengameart.org/content/16-monster-growls , CC0 1.0 https://creativecommons.org/publicdomain/zero/1.0/ . Own mouth-performed recordings, selected distinct takes, EQ/crop/gain/fades; giant uses pitch/formant shift.\nOwlishMedia — 202 More Sound Effects, https://opengameart.org/content/202-more-sound-effects , CC0 1.0. Recorded household cloth props used for wolf movement foley, not an animal vocal recording.\nSource/archive/member hashes and processing: art/audio/pack-a-60/voice-rework/sources.json and batch-selection.json. No original-author endorsement implied.\n'
  # Keep licenses with distributed assets; don't redistribute local vanilla cache.
 stable_text(A/'audio_licenses/creature-batch-60.txt',credits)
 names={'ovinnik':'Овинник','idolishche':'Идолище Поганое','skim':'Ским-зверь','grey_wolf':'Серый Волк'}
 text=['# Пакет существ #60 — новые выбранные файлы','', 'Подключено по разрешению автономного выбора. Не прослушано владельцем, не игровая приёмка. Голоса — отдельные исполненные take StarNinjas (CC0); движения Волка — записанные тканевые props OwlishMedia (CC0), не голос.','']
 for row in report:
  text += [f"## {names[row['mob']]} · {row['action']}",'',f"Слой: {row['layer']}. Event: `{row['event']}`. Длительность: {row['seconds']:.3f}s.",'',f"![{names[row['mob']]} — {row['action']}]({(A/'sounds'/row['file']).as_posix()})",'',f"Источник: `{row['source']}`. SHA256: `{row['sha256']}`.",'']
 text += ['Боль Волка: намеренный vanilla entity.wolf.hurt вместо growl, не новая запись. Его summon_howl и pack_answer не менялись.','', 'Источники/лицензии/обработка/хеши: sources.json, batch-selection.json, исходный art/audio/pack-a-60/sources.json; CC0 links и атрибуция входят в production JAR: audio_licenses/creature-batch-60.txt.','']
 stable_text(P/'batch-preview.md','\n'.join(text))
if __name__=='__main__':restore_selected()
