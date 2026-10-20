"""Read-only #78 event/resource/callsite audit. Never synthesizes or changes audio."""
from pathlib import Path
import csv,json,re,hashlib,sys
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/bogatyr-78'
sys.path.insert(0,str(R/'.tools/audio-libs'))
import soundfile as sf
import numpy as np

def main():
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 langs={l:json.loads((A/f'lang/{l}.json').read_text(encoding='utf-8')) for l in ['en_us','ru_ru']}
 groups={'Ilya':('ilya_muromets.', ['bogatyr/IlyaEntity.java','bogatyr/CombatEntity.java','bogatyr/BogatyrEvents.java']),
 'Dobrynya':('dobrynya.',['marinka/DobrynyaEntity.java','marinka/WitchActor.java']),
 'Alyosha':('alyosha.',['skim/AlyoshaEntity.java','skim/SkimActor.java','skim/SkimEvents.java','skim/HeroCalls.java']),
 'Finist':('entity.finist.',['finist/FinistEntity.java','finist/FalconActor.java','finist/FinistEvents.java','skim/HeroCalls.java']),
 'Finist falcon':('entity.falcon.',['finist/FinistEntity.java','finist/FalconActor.java','client/FinistClient.java'])}
 rows=[]
 for who,(prefix,paths) in groups.items():
  sources={p:(R/'src/main/java/org/slavicmyths'/p).read_text(encoding='utf-8') for p in paths}
  for event,entry in events.items():
   if not event.startswith(prefix):continue
   short=event[len(prefix):];sites=[];confidence='NO_MATCH_REVIEW_UNUSED'
   for p,source in sources.items():
    for number,line in enumerate(source.splitlines(),1):
     if ('"'+event+'"' in line or '"'+short+'"' in line) and re.search(r'cue\(|playSound\(|getHurtSound|sound\(',line):
      sites.append(f'{p}:{number}');confidence='DIRECT_SOURCE_CALL'
     elif short.rsplit('_',1)[0]+'_' in line and re.search(r'cue\(|playSound\(|getHurtSound|sound\(',line):
      sites.append(f'{p}:{number} [dynamic family]');confidence='DYNAMIC_FAMILY_REQUIRES_RUNTIME'
   sub=entry.get('subtitle','');translation=bool(sub and all(sub in n for n in langs.values()))
   for value in entry.get('sounds',[]):
    value={'name':value} if isinstance(value,str) else value
    name=value['name'];file=A/'sounds'/(name.split(':',1)[-1]+'.ogg')
    decoded='NOT_LOCAL_EVENT' if value.get('type')=='event' else 'MISSING';duration=rms=peak=None
    if file.exists():
     audio,rate=sf.read(file,dtype='float32',always_2d=True)
     assert len(audio)>0 and np.isfinite(audio).all(),file
     decoded='PASS';duration=round(len(audio)/rate,4);rms=round(float(np.sqrt(np.mean(audio**2))),6);peak=round(float(np.abs(audio).max()),6)
    rows.append(dict(entity=who,action=short,java_call='; '.join(sites),callsite_status=confidence,event_id='slavicmyths:'+event,sounds_json='assets/slavicmyths/sounds.json#'+event,actual_ogg=file.relative_to(R).as_posix(),sha256=hashlib.sha256(file.read_bytes()).hexdigest() if file.exists() else '',decode=decoded,seconds=duration,rms=rms,peak=peak,event_volume=value.get('volume',1),event_pitch=value.get('pitch',1),subtitle_ru_en='PASS' if translation else 'FAIL',in_game='NOT_RUN',listened='NOT_RUN',verdict='OWNER_PENDING; no auditory defect inferred'))
 with (O/'AUDIO_EVENT_MAP.csv').open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
 # Registration and literal call resolution are source checks, never auditory approval.
 content={name:(R/'src/main/java/org/slavicmyths'/name).read_text(encoding='utf-8') for name in ['bogatyr/BogatyrContent.java','marinka/MarinkaContent.java','skim/SkimContent.java','finist/FinistContent.java']}
 registration=[]
 for row in rows:
  event=row['event_id'].split(':',1)[1];prefix,short=event.split('.',1)
  registered=any('"'+event+'"' in source for source in content.values()) or (prefix=='ilya_muromets' and '"'+short+'"' in content['bogatyr/BogatyrContent.java'])
  if not registered:registration.append(event)
 missing=[]
 for file,prefix in [('bogatyr/IlyaEntity.java','ilya_muromets.'),('marinka/DobrynyaEntity.java','dobrynya.'),('skim/AlyoshaEntity.java',''),('finist/FinistEntity.java','')]:
  source=(R/'src/main/java/org/slavicmyths'/file).read_text(encoding='utf-8')
  for cue in set(re.findall(r'cue\("([^"]+)"\)',source)):
   if prefix+cue not in events:missing.append([file,prefix+cue])
 result={'registration_failures':registration,'literal_call_failures':missing,'events':len({r['event_id'] for r in rows}),'files':len(rows),'decode_failures':[r['actual_ogg'] for r in rows if r['decode']!='PASS'],'subtitle_failures':[r['event_id'] for r in rows if r['subtitle_ru_en']!='PASS'],'unmatched_candidates':[r['event_id'] for r in rows if r['callsite_status']=='NO_MATCH_REVIEW_UNUSED'],'runtime_launches':0,'listened':False,'verdict':'OWNER_PENDING'}
 (O/'audio-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result))
if __name__=='__main__':main()
