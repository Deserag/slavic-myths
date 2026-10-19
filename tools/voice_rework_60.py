"""Labeled voice review; production restores only the exact owner-approved takes. No PolyMC."""
from pathlib import Path
import sys,json,hashlib,subprocess,shutil
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'));sys.path.insert(0,str(R/'tools'))
import numpy as np,soundfile as sf
from audio_60_foley import encode,RATE,stable_text
P=R/'art/audio/pack-a-60/voice-rework'
def restore_accepted():
 acceptance=json.loads((P/'acceptance.json').read_text(encoding='utf-8'));assets=R/'src/main/resources/assets/slavicmyths'
 targets={'tugarin_challenge':('tugarin_phase1_aggro','voice/tugarin/challenge','subtitles.slavicmyths.tugarin_challenge',32),'skim_roar':('skim.roar','voice/skim/roar','subtitles.slavicmyths.skim.roar',48)}
 targets.update({'tugarin_breath':('tugarin_phase1_idle','voice/tugarin/breath','subtitles.slavicmyths.tugarin_breath',20),'tugarin_attack_voice':('tugarin_phase1_attack','voice/tugarin/attack','subtitles.slavicmyths.tugarin_attack_voice',28)})
 events_path=assets/'sounds.json';events=json.loads(events_path.read_text(encoding='utf-8'))
 for key in acceptance['timbre_approved_keys']:
  assert key in targets;source=P/acceptance['approved_files'][key];assert hashlib.sha256(source.read_bytes()).hexdigest()==acceptance['approved_sha256'][key],'Do not replace approved take with unreviewed processing'
  event,dest,subtitle,distance=targets[key];path=assets/'sounds'/(dest+'.ogg');path.parent.mkdir(parents=True,exist_ok=True);
  if not path.exists() or path.read_bytes()!=source.read_bytes():shutil.copyfile(source,path)
  events[event]={'subtitle':subtitle,'sounds':[{'name':dest,'attenuation_distance':distance}]}
 stable_text(events_path,json.dumps(events,ensure_ascii=False,indent=2)+'\n')
 for lang,label in [('ru_ru','Тугарин бросает боевой вызов'),('en_us','Tugarin challenges his foe')]:
  path=assets/f'lang/{lang}.json';d=json.loads(path.read_text(encoding='utf-8'));d['subtitles.slavicmyths.tugarin_challenge']=label
  d.update({'subtitles.slavicmyths.tugarin_breath':'Тугарин тяжело дышит' if lang=='ru_ru' else 'Tugarin breathes heavily','subtitles.slavicmyths.tugarin_attack_voice':'Тугарин напрягается при ударе' if lang=='ru_ru' else 'Tugarin grunts with effort'})
  stable_text(path,json.dumps(d,ensure_ascii=False,indent=2)+'\n')
 credits=assets/'audio_licenses/voice-rework-60.txt';credits.parent.mkdir(exist_ok=True);stable_text(credits,(P/'LICENSES.md').read_text(encoding='utf-8')+'\nProduction uses the exact four owner-approved files: Alba MacKenna Tugarin challenge, breath and attack; Little Robot Sound Factory Skim roar. The separate SPIN preparation exhale is agent-selected under delegation, not owner-approved. Other candidates remain in art/audio.\n')
 from tugarin_spin_voice_60 import restore_selected
 restore_selected()
 from skim_rage_audio_60 import restore_selected as restore_skim_rage
 restore_skim_rage()
 from creature_audio_batch_60 import restore_selected as restore_creature_batch
 restore_creature_batch()
 from frost_foley_60 import restore_selected as restore_frost_foley
 restore_frost_foley()

def bass_alternative():
 source=P/'preview/tugarin_challenge.ogg';raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(source),'-af','equalizer=f=160:t=q:w=0.8:g=2','-f','f32le','-ar',str(RATE),'-ac','1','pipe:1']);x=np.frombuffer(raw,dtype='<f4').astype(float);x*=min(1,.88/max(np.max(abs(x)),1e-12))
 dest=P/'alternatives';dest.mkdir(exist_ok=True);encode(dest/'tugarin_challenge_bass.ogg',x)
 (dest/'README.md').write_text('Тугарин · босс, фаза1 · боевой вызов. Дополнительный EQ160Hz +2dB, без изменения высоты и темпа. В игру не подключён: выбранный владельцем исходный голос сохранён.\n\n![Тугарин — дополнительный басовый вариант](tugarin_challenge_bass.ogg)\n',encoding='utf-8')
 stable_text(dest/'README.md','Тугарин · босс, человеческая форма, фаза 1 · боевой вызов противнику.\n\nТекущий выбранный владельцем вариант: tugarin_challenge_rough.ogg. Он подключён в production, SHA закреплён в ../acceptance.json. Обработка: -5 полутонов, изменение формант, EQ и мягкая сатурация; темп сохранён.\n\nПрежний tugarin_challenge_bass.ogg — только EQ +2dB, в игре не используется. Игровая проверка не выполнялась; оценка тембра не означает приёмку всей озвучки.\n')

def main():
 sources={}
 for spec in json.loads((P/'sources.json').read_text(encoding='utf-8')):
  p=P/spec['file'];assert hashlib.sha256(p.read_bytes()).hexdigest()==spec['sha256'];raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(p),'-f','f32le','-ar',str(RATE),'-ac','1','pipe:1']);sources[spec['name']]=np.frombuffer(raw,dtype='<f4').astype(float)
 out=P/'preview';out.mkdir(exist_ok=True);report=[];readme=['# Озвучка угрозы — 4 файла одобрены, полная приёмка не завершена','', 'Голосовые примеры отделены от огня/оружия/ткани. Это рабочая подборка для оценки характера; качество угрозы не объявляется принятым по измерениям. Четыре прослушанных файла одобрены владельцем и подключаются; остальные кандидаты не подключены.','']
 for spec in json.loads((P/'clips.json').read_text(encoding='utf-8')):
  assert all(spec.get(k) for k in ['mob','role','action','intent','phase','event','callsite']);x=sources[spec['source']][int(spec['start']*RATE):None if spec['end'] is None else int(spec['end']*RATE)].copy();x-=x.mean();fade=min(220,len(x)//8);x[:fade]*=np.linspace(0,1,fade);x[-fade:]*=np.linspace(1,0,fade);gain=min(spec['target_rms']/max(np.sqrt(np.mean(x*x)),1e-12),.8/max(np.max(abs(x)),1e-12));x*=gain
  path=out/(spec['key']+'.ogg');encode(path,x);y,sr=sf.read(path);assert sr==RATE and y.ndim==1 and np.isfinite(y).all() and np.max(abs(y))<.95
  loud=subprocess.run(['ffmpeg','-v','info','-i',str(path),'-af','ebur128=peak=true','-f','null','-'],capture_output=True,text=True).stderr
  import re
  lu=re.findall(r'I:\s+(-?[\d.]+) LUFS',loud)
  row=dict(spec,seconds=round(len(y)/sr,4),gain=round(float(gain),6),peak_dbfs=round(float(20*np.log10(max(abs(y)))),2),rms_dbfs=round(float(20*np.log10(np.sqrt(np.mean(y*y)))),2),integrated_lufs=float(lu[-1]) if lu else None,sha256=hashlib.sha256(path.read_bytes()).hexdigest());report.append(row)
  readme += [f"## {spec['mob']} · {spec['role']} · {spec['action']}",'',f"Фаза: **{spec['phase']}**. Слой: **голос**. Назначение: {spec['intent']}.",'',f"![{spec['mob']} — {spec['action']}](preview/{spec['key']}.ogg)",'',f"Планируемый event: `{spec['event']}`. Точка: `{spec['callsite']}`. Статус: **{spec['status']}**, не игровая приёмка.",'']
 readme += ['## Происхождение и ограничения','', '[Авторы, лицензии и изменения](LICENSES.md). Источники и SHA256: sources.json. Генератор сохраняет естественную высоту голоса; редактура — mono/частота дискретизации/crop/gain/fade. Метрики не доказывают художественное качество.','', 'Владелец одобрил точные файлы: грубый вызов, дыхание и удар Тугарина, а также рёв Скима. Остальные остаются кандидатами. Игровой приёмки нет, карантин не снят, PolyMC эта итерация не меняет.','']
 bass_alternative();readme += ['## Необязательная корректировка Тугарина','', '[Более выраженная нижняя середина: EQ160Hz +2dB, темп и высота прежние](alternatives/README.md). В игру оставлен исходный одобренный вариант.','']
 subprocess.run([sys.executable,str(R/'tools/tugarin_rough_preview_60.py')],check=True)
 subprocess.run([sys.executable,str(R/'tools/audio_review_62_candidates.py')],check=True)
 readme += ['## Текущий выбранный Тугарин','', 'Владелец одобрил более грубый низкий вариант. В production используется именно [прослушанный rough-файл](alternatives/tugarin_challenge_rough.ogg), а не исходный высокий голос или EQ-only. Полная игровая приёмка не выполнена.','']
 (P/'outputs.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');(P/'README.md').write_text('\n'.join(readme)+'\n',encoding='utf-8');restore_accepted();print('8 labeled voices; 4 exact owner-approved takes restored to production.')
if __name__=='__main__':main()
