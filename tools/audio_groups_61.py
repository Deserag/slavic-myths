"""Deterministic licensed source montage. No synthesis or artist voice cloning."""
from pathlib import Path
import sys,json,hashlib,subprocess
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np,soundfile as sf
from audio_60_foley import encode,stable_text,RATE
from audio_groups_61_plan import PLAN,ALIASES,BLOCKED,DEFERRED,NEW
A=R/'src/main/resources/assets/slavicmyths';P=R/'art/audio/project-61'
EVENTS=set(PLAN)|set(ALIASES)|set(BLOCKED)
DELEGATED_PREFIXES=('domovoy_','leshy_','kikimora_','polevik_','poludnitsa_','bannik_','igosha_','vodyanoy_','nochnitsa.','doghead.')
PROTECTED_PREFIXES=('rusalka','kuzya','kurgan/recorded')

def render_source(spec,source):
 path=P/source['file'];assert path.resolve().is_relative_to((R/'art/audio').resolve())
 assert hashlib.sha256(path.read_bytes()).hexdigest()==source['sha256']
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(path),'-af','highpass=f=65,lowpass=f=10500','-f','f32le','-ar',str(RATE),'-ac','1','pipe:1'])
 x=np.frombuffer(raw,dtype='<f4').astype(float).copy();start=int(spec.get('start',0)*RATE);end=int(spec['end']*RATE) if 'end' in spec else len(x);x=x[start:end]
 assert len(x)>220,source['file'];x-=x.mean();active=np.flatnonzero(abs(x)>max(abs(x))*.025)
 assert len(active),source['file'];trim=max(0,int(active[0])-220);x=x[trim:active[-1]+221]
 return x,start+trim

def restore_selected():
 assert not any(e.startswith(PROTECTED_PREFIXES) for e in EVENTS)
 catalog={s['key']:s for s in json.loads((P/'sources.json').read_text(encoding='utf-8'))}
 for choice in PLAN.values():
  for variant in choice['sources']:
   for layer in (variant if isinstance(variant,list) else [variant]):assert (layer if isinstance(layer,str) else layer['key']) in catalog,layer
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));rows=[];ledger={}
 cache={r['file']:r for r in json.loads((P/'selection.json').read_text(encoding='utf-8'))} if (P/'selection.json').exists() else {}
 # Frozen exact sounds/IDs of previous approved or curated work are not targets.
 assert not EVENTS&{'tugarin_phase1_idle','tugarin_phase1_aggro','tugarin_phase1_attack','skim.roar'}
 texts={l:json.loads((A/f'lang/{l}.json').read_text(encoding='utf-8')) for l in ['ru_ru','en_us']}
 for event,choice in PLAN.items():
  entries=[]
  for n,variant in enumerate(choice['sources'],1):
   layers=variant if isinstance(variant,list) else [variant];clips=[];provenance=[]
   name='recorded61/'+event.replace('.','/')+'_'+str(n);dest=A/'sounds'/(name+'.ogg')
   signature=hashlib.sha256(json.dumps(dict(renderer=1,choice=choice,variant=variant,sources=[catalog[x if isinstance(x,str) else x['key']]['sha256'] for x in layers]),sort_keys=True).encode()).hexdigest()
   previous=cache.get(name+'.ogg')
   if previous and previous.get('render_signature')==signature and dest.exists() and hashlib.sha256(dest.read_bytes()).hexdigest()==previous['sha256']:
    for layer in layers:
     source=catalog[layer if isinstance(layer,str) else layer['key']];assert hashlib.sha256((P/source['file']).read_bytes()).hexdigest()==source['sha256']
    entry={'name':'slavicmyths:'+name,'attenuation_distance':choice.get('radius',20 if any(t in event for t in ['ambient','idle','step']) else 28)}
    if choice.get('stream'):entry['stream']=True
    entries.append(entry);rows.append(previous)
    if event.startswith(DELEGATED_PREFIXES):ledger.setdefault(event,[]).append({'name':'slavicmyths:'+name,'sha256':previous['sha256'],'sources':previous['sources']})
    continue
   for layer in layers:
    spec={'key':layer} if isinstance(layer,str) else layer;source=catalog[spec['key']];x,start=render_source(spec,source)
    x=x[:int(choice['cap']*RATE)];x/=max(np.sqrt(np.mean(x*x)),1e-12);clips.append(x)
    provenance.append(dict(key=source['key'],file=source['file'],sha256=source['sha256'],license=source['license'],author=source['author'],page=source['page'],start_sample=start))
   x=np.zeros(max(len(c) for c in clips))
   for c in clips:x[:len(c)]+=c/len(clips)
   fade=min(int(.012*RATE),len(x)//6);x[:fade]*=np.linspace(0,1,fade);x[-fade:]*=np.linspace(1,0,fade)
   x*=min(choice['rms']/max(np.sqrt(np.mean(x*x)),1e-12),choice.get('peak',.55)/max(abs(x)));x=np.pad(x,(220,220))
   name='recorded61/'+event.replace('.','/')+'_'+str(n);dest=A/'sounds'/(name+'.ogg');dest.parent.mkdir(parents=True,exist_ok=True);encode(dest,x);decoded,sr=sf.read(dest)
   assert sr==RATE and decoded.ndim==1 and np.isfinite(decoded).all() and max(abs(decoded))<.8
   entry={'name':'slavicmyths:'+name,'attenuation_distance':choice.get('radius',20 if any(t in event for t in ['ambient','idle','step']) else 28)}
   if choice.get('stream'):entry['stream']=True
   entries.append(entry)
   row=dict(render_signature=signature,event=event,group=choice['group'],ru=choice['ru'],action=choice['en'],file=name+'.ogg',sha256=hashlib.sha256(dest.read_bytes()).hexdigest(),sources=provenance,seconds=len(decoded)/sr,peak_dbfs=float(20*np.log10(max(abs(decoded)))),rms_dbfs=float(20*np.log10(np.sqrt(np.mean(decoded*decoded)))),processing='decode mono44100, EQ65/10500, source crop/onset, duration cap,12ms fades,5ms guards, role-calibrated gain; no pitch shift or synthesis',status='AGENT_SELECTED_OWNER_PENDING',owner_approved=False,hearing='NOT_LISTENED',shared_foley_reason=choice.get('shared_foley_reason','Only physical material recordings may be shared across characters; never an identity voice'))
   rows.append(row)
   if event.startswith(DELEGATED_PREFIXES):ledger.setdefault(event,[]).append({'name':'slavicmyths:'+name,'sha256':row['sha256'],'sources':provenance})
  subtitle=events.get(event,{}).get('subtitle','subtitles.slavicmyths.'+event)
  events[event]={'subtitle':subtitle,'sounds':entries}
  for lang,key in [('ru_ru','ru'),('en_us','en')]:texts[lang][subtitle]=choice[key]
 for event,choice in ALIASES.items():
  subtitle=events.get(event,{}).get('subtitle','subtitles.slavicmyths.'+event)
  events[event]={'subtitle':subtitle,'sounds':[{'name':'minecraft:'+choice['target'],'type':'event'}]}
  if 'ru' in choice:
   texts['ru_ru'][subtitle]=choice['ru'];texts['en_us'][subtitle]=choice['en']
  elif event in NEW:
   texts['ru_ru'][subtitle]='Шаги зверя';texts['en_us'][subtitle]='Animal footsteps'
 # Only specifically rejected/unresolved cues are disabled, never silently restored.
 for event,reason in BLOCKED.items():
  if event not in ALIASES and event not in DEFERRED:events[event]['sounds']=[]
 from audio_resource_names_61 import qualified
 events=qualified(events)
 stable_text(A/'sounds.json',json.dumps(events,ensure_ascii=False,indent=2)+'\n')
 for lang,text in texts.items():stable_text(A/f'lang/{lang}.json',json.dumps(text,ensure_ascii=False,indent=2)+'\n')
 license_dir=A/'audio_licenses';license_dir.mkdir(exist_ok=True)
 stable_text(license_dir/'delegated-recordings-61.json',json.dumps(dict(status='AGENT_SELECTED_OWNER_PENDING',authorization='https://github.com/Deserag/slavic-myths/issues/61',owner_accepted=False,events=ledger),ensure_ascii=False,indent=2)+'\n')
 used={p['key'] for row in rows for p in row['sources']};credits=['Slavic Myths recorded audio project #61. AGENT_SELECTED / OWNER_PENDING.','Not listened by owner; no runtime acceptance. Real voice performances/physical props; no synthetic vocal tones.','Raw source/archive/member hashes and edits: art/audio/project-61/sources.json and selection.json.']
 for key in sorted(used):
  source=catalog[key];credits.append(f"{key}: {source['author']}; {source['page']}; {source['license']}; {source.get('license_url','see source manifest')}. Crop/resample/EQ/fade/gain/montage, no endorsement.")
 stable_text(license_dir/'project-61.txt','\n'.join(credits)+'\n')
 stable_text(P/'selection.json',json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
 stable_text(P/'aliases.json',json.dumps(ALIASES,ensure_ascii=False,indent=2)+'\n');stable_text(P/'blocked.json',json.dumps(BLOCKED,ensure_ascii=False,indent=2)+'\n')
 stable_text(P/'deferred.json',json.dumps(DEFERRED,ensure_ascii=False,indent=2)+'\n')
 return rows

if __name__=='__main__':
 rows=restore_selected();print(f'{len(rows)} recorded variants / {len(PLAN)} events; {len(ALIASES)} semantic vanilla references; {len(BLOCKED)-len(DEFERRED)} active source gaps / {len(DEFERRED)} owner-deferred. OWNER_PENDING.')
