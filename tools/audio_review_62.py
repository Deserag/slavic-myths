"""Read-only production audio inventory. Source matches are NOT reachability proofs."""
from pathlib import Path
import csv, json, hashlib, re, subprocess, sys, zipfile
from collections import defaultdict
R=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np
import soundfile as sf
A=R/'src/main/resources/assets/slavicmyths'; O=R/'docs/audio-review'; O.mkdir(exist_ok=True)
def sha(b): return hashlib.sha256(b).hexdigest()
def write(name, rows, fields):
 with (O/name).open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=fields,extrasaction='ignore'); w.writeheader();w.writerows(rows)
def main():
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 protected={p:sha(p.read_bytes()) for p in (R/'src/main').rglob('*') if p.is_file()}
 java={p.relative_to(R).as_posix():p.read_text(encoding='utf-8') for p in (R/'src/main/java').rglob('*.java')}
 classes={Path(p).stem:(p,s) for p,s in java.items()}
 sites=[]
 for p,s in java.items():
  for n,line in enumerate(s.splitlines(),1):
   if re.search(r'playSound\(|\bcue\(|\bvoice\(|getAmbientSound\(|getHurtSound\(|getDeathSound\(|SoundInstance|SoundEvents\.',line):
    sites.append({'path':p,'line':n,'source':line.strip(),'reachability':'UNVERIFIED_SOURCE_MATCH'})
 write('CALLSITES.csv',sites,['path','line','source','reachability'])
 langs={lang:json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8')) for lang in ['ru_ru','en_us']}
 eventrows=[];refs=defaultdict(list);aliases=defaultdict(list)
 for event,d in events.items():
  sources=d.get('sounds',[]) or [None]
  for item in sources:
   source={'name':item} if isinstance(item,str) else item or {}
   name=source.get('name','');kind=source.get('type','file') if item is not None else 'MUTED'
   ns,sep,key=name.partition(':');ns,key=(ns,key) if sep else ('slavicmyths',name)
   path=A/'sounds'/(key+'.ogg') if kind=='file' and ns=='slavicmyths' and name else None
   if path:refs[path.relative_to(R).as_posix()].append(event)
   if name:aliases[(kind,name)].append(event)
   subtitle=d.get('subtitle','')
   eventrows.append(dict(event=event,type=kind,name=name,weight=source.get('weight',1),pitch=source.get('pitch',1),volume=source.get('volume',1),stream=source.get('stream',False),subtitle=subtitle,ru=subtitle in langs['ru_ru'],en=subtitle in langs['en_us'],path=path.relative_to(R).as_posix() if path else '',exists=path.exists() if path else 'VANILLA_OR_EVENT' if name else 'MUTED',literal_mentions=';'.join(f'{p}:{s.count(chr(10),0,m.start())+1}' for p,s in java.items() for m in re.finditer(re.escape('"'+event+'"'),s)),reachability='UNVERIFIED_DYNAMIC_DISPATCH'))
 write('EVENT_MAP.csv',eventrows,list(eventrows[0]))
 metrics=[];bybyte=defaultdict(list);bypcm=defaultdict(list);signatures=[]
 jar=R/'build/libs/slavicmyths-1.9.7.8.jar'
 with zipfile.ZipFile(jar) as z:
  for path in sorted((A/'sounds').rglob('*.ogg')):
   rel=path.relative_to(R).as_posix();entry=path.relative_to(R/'src/main/resources').as_posix();b=path.read_bytes()
   row=dict(path=rel,sha256=sha(b),events=';'.join(refs[rel]),referenced=bool(refs[rel]),jar_parity=entry in z.namelist() and z.read(entry)==b,hearing='NOT_LISTENED')
   try:
    x,sr=sf.read(path,always_2d=True,dtype='float32');info=sf.info(path);mono=x.mean(axis=1);peak=float(np.max(abs(x)));rms=float(np.sqrt(np.mean(x.astype(float)**2)))
    audible=np.flatnonzero(np.max(abs(x),axis=1)>10**(-60/20))
    row.update(status='DECODE_PASS',format=info.format,subtype=info.subtype,channels=x.shape[1],rate=sr,seconds=len(x)/sr,decoded_peak_dbfs=20*np.log10(max(peak,1e-12)),rms_dbfs=20*np.log10(max(rms,1e-12)),trailing_silence_s=(len(x)-1-audible[-1])/sr if len(audible) else len(x)/sr,pcm_sha256=sha(x.tobytes()),clipping=peak>=1)
    bybyte[sha(b)].append(rel);bypcm[(sr,x.shape,sha(x.tobytes()))].append(rel)
    # Coarse amplitude envelope, 64 normalized time bins; screening only. No claim
    # of aligned perceptual similarity or a listened duplicate from this statistic.
    energy=np.array([np.sqrt(np.mean(c.astype(float)**2)) if len(c) else 0 for c in np.array_split(mono,64)])
    energy-=energy.mean();norm=np.linalg.norm(energy)
    if norm>1e-8:signatures.append((rel,len(x)/sr,energy/norm))
   except Exception as e:row.update(status='DECODE_FAIL',error=str(e))
   metrics.append(row)
 fields=list(dict.fromkeys(k for r in metrics for k in r));write('ASSETS.csv',metrics,fields)
 dup=[]
 for kind,groups in [('EXACT_BYTES',bybyte),('EXACT_DECODED_PCM',bypcm),('SHARED_EVENT_SOURCE',aliases)]:
  for key,paths in groups.items():
   if len(paths)>1:dup.append(dict(kind=kind,group=str(key)[:180],count=len(paths),members=';'.join(paths),interpretation='EXACT_IDENTITY' if kind!='SHARED_EVENT_SOURCE' else 'SHARED_SOURCE_NOT_AUTOMATIC_DEFECT',hearing='NOT_LISTENED'))
 for i,(p,d,s) in enumerate(signatures):
  for q,e,t in signatures[i+1:]:
   if abs(d-e)>.1*max(d,e):continue
   score=float(s@t)
   if score>=.995 and p!=q:
    dup.append(dict(kind='ENVELOPE_SCREEN_ONLY',group=f'{score:.6f}',count=2,members=p+';'+q,interpretation='POSSIBLE_FALSE_POSITIVE_NOT_PERCEPTUAL_PROOF',hearing='NOT_LISTENED'))
 write('DUPLICATES.csv',dup,['kind','group','count','members','interpretation','hearing'])
 # Re-scan literal + helper registrations, rather than assuming old CSV complete.
 registered={}
 for p,s in java.items():
  for m in re.finditer(r'(?:ModEntities\.)?ENTITIES\.register\(\s*"([a-z0-9_]+)"',s):
   decl=s[m.start():s.find(';',m.end())];
   if 'EntityType.Builder' in decl:registered['slavicmyths:'+m[1]]=f'{p}:{s.count(chr(10),0,m.start())+1}'
  if p.endswith('/ModEntities.java'):
   for m in re.finditer(r'=\s*wildlife\("([a-z_]+)"',s):registered['slavicmyths:'+m[1]]=f'{p}:{s.count(chr(10),0,m.start())+1}'
 old=list(csv.DictReader((R/'docs/audit/creatures-1.9.8.1/CREATURES.csv').open(encoding='utf-8-sig')))
 coverage=[]
 for entity in old:
  impl=entity['implementation'].split('.')[-1];chain=[];seen=set()
  while impl in classes and impl not in seen:
   seen.add(impl);p,s=classes[impl];chain.append(p)
   m=re.search(r'\bclass\s+\w+(?:<[^>]*>)?\s+extends\s+([\w.]+)',s);impl=m[1].split('.')[-1] if m else ''
  found=[f"{r['path']}:{r['line']}" for r in sites if r['path'] in chain]
  coverage.append(dict(id=entity['entity_type'],name=entity['name'],group=entity['group'],registry=registered.get(entity['entity_type'],'NOT_FOUND'),implementation=entity['implementation'],source_chain=';'.join(chain),sound_source_matches=';'.join(found),status='UNVERIFIED_REACHABILITY',reason='Source inheritance indexed; phase/kind routing, external goals and client loops require manual verification; no no-sound exemption inferred',hearing='NOT_LISTENED'))
 for ident in set(registered)-{r['id'] for r in coverage}:coverage.append(dict(id=ident,registry=registered[ident],status='MISSING_CLASSIFICATION',reason='Not in older creature CSV'))
 write('ENTITY_COVERAGE.csv',coverage,['id','name','group','registry','implementation','source_chain','sound_source_matches','status','reason','hearing'])
 licenses=[]
 for path in sorted((R/'art/audio').rglob('sources.json')):
  try:
   data=json.loads(path.read_text(encoding='utf-8'))
   for spec in data if isinstance(data,list) else []:
    licenses.append(dict(asset=str(spec.get('file','')),evidence=path.relative_to(R).as_posix(),author=spec.get('author','UNKNOWN'),license=spec.get('license','UNKNOWN'),url=spec.get('page',spec.get('url','UNKNOWN')),sha256=spec.get('sha256',''),status='RECORDED_LICENSE_METADATA_RECHECK_IF_DISTRIBUTING'))
  except Exception:pass
 known={
  'voice/tugarin/challenge.ogg':('Alba MacKenna','CC-BY-4.0','https://opengameart.org/content/voice-pack%E2%94%82universal-spurts','art/audio/pack-a-60/voice-rework/LICENSES.md'),
  'voice/tugarin/breath.ogg':('Alba MacKenna','CC-BY-4.0','https://opengameart.org/content/voice-pack%E2%94%82universal-spurts','art/audio/pack-a-60/voice-rework/LICENSES.md'),
  'voice/tugarin/attack.ogg':('Alba MacKenna','CC-BY-4.0','https://opengameart.org/content/voice-pack%E2%94%82universal-spurts','art/audio/pack-a-60/voice-rework/LICENSES.md'),
  'voice/skim/roar.ogg':('Little Robot Sound Factory','CC-BY-3.0','https://opengameart.org/content/horror-sound-effects-library','art/audio/pack-a-60/voice-rework/LICENSES.md')}
 for n in range(1,4):known[f'boss/tugarin/foley/spin_{n}.ogg']=('qubodup','CC0-1.0','https://opengameart.org/content/swish-bamboo-stick-weapon-swhoshes','art/audio/pack-a-60/sources.json')
 if (R/'art/audio/pack-a-60/voice-rework/batch-selection.json').exists():
  for row in json.loads((R/'art/audio/pack-a-60/voice-rework/batch-selection.json').read_text(encoding='utf-8')):
   if row['layer']=='recorded voice':known[row['file']]=('StarNinjas','CC0-1.0','https://opengameart.org/content/16-monster-growls','art/audio/pack-a-60/voice-rework/sources.json')
   else:known[row['file']]=('OwlishMedia','CC0-1.0','https://opengameart.org/content/202-more-sound-effects','art/audio/pack-a-60/sources.json')
 for row in metrics:
  key=row['path'].split('assets/slavicmyths/sounds/',1)[1];author,license,url,evidence=known.get(key,('UNKNOWN','UNKNOWN','','SOURCE_NOT_YET_TRACED_PER_ASSET'))
  licenses.append(dict(asset=row['path'],evidence=evidence,author=author,license=license,url=url,sha256=row['sha256'],status='RECORDED_SOURCE_CHAIN' if key in known else 'UNRESOLVED_NOT_A_CLAIM_OF_ILLEGALITY'))
 write('SOURCE_LICENSES.csv',licenses,['asset','evidence','author','license','url','sha256','status'])
 generators=[]
 for p in (R/'tools').glob('*.py'):
  s=p.read_text(encoding='utf-8')
  if re.search(r'ffmpeg|soundfile|\.ogg|sounds\.json',s):generators.append(dict(path=p.relative_to(R).as_posix(),mode='REVIEW_REQUIRED',note='Source text match; may be QA or writer, not executed by this inventory'))
 write('GENERATORS.csv',generators,['path','mode','note'])
 assert set(registered)=={r['id'] for r in coverage},'Registry/table drift requires manual fix'
 assert all(sha(p.read_bytes())==h for p,h in protected.items()),'Production changed during read-only inventory'
 report=dict(status='IN_PROGRESS',base_sha=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),working_tree='DIRTY_OWNER_APPROVED_AUDIO_SUBSET_AND_USER_DELETIONS_PRESERVED',events=len(events),muted=sum(not d.get('sounds') for d in events.values()),ogg=len(metrics),decode_fail=sum(r['status']!='DECODE_PASS' for r in metrics),jar_mismatch=sum(not r['jar_parity'] for r in metrics),registry_count=len(registered),entity_rows=len(coverage),independent=sum(r.get('group')!='auxiliary' for r in coverage),source_sites=len(sites),duplicate_groups=len(dup),jar_sha256=sha(jar.read_bytes()),production_unchanged=True,runtime_launches=0,manual_reachability='PENDING',perceptual_alignment='NOT_IMPLEMENTED_ENVELOPE_SCREEN_ONLY',hearing='NOT_LISTENED')
 (O/'gates.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
