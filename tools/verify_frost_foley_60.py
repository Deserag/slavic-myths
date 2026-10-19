"""Offline evidence for the recorded frost props. Never starts Java/game/launcher."""
from pathlib import Path
import sys,json,hashlib,subprocess,zipfile,tempfile,shutil
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'))
import numpy as np
import soundfile as sf
from frost_foley_60 import restore_selected,EVENTS,A,P
BASE='037f80ac';checks=[]
def check(ok,message):
 if not ok:raise AssertionError(message)
 checks.append(message)
def original(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R)
def digest(data):return hashlib.sha256(data).hexdigest()

def main():
 receipt_path=R/'docs/implementation/audio-60/POLYMC_INSTALLATION.json';receipt_before=receipt_path.read_bytes()
 rows=json.loads((P/'selection.json').read_text(encoding='utf-8'))
 check(len(rows)==11 and len(EVENTS)==9,'Eleven recorded clips, nine existing events')
 before={(A/'sounds'/r['file']):digest((A/'sounds'/r['file']).read_bytes()) for r in rows}
 maps=(A/'sounds.json').read_bytes();restore_selected()
 check(all(digest(p.read_bytes())==h for p,h in before.items()) and maps==(A/'sounds.json').read_bytes(),'Renderer byte-idempotent, frozen selected maps')
 baseline=json.loads(original('src/main/resources/assets/slavicmyths/sounds.json'));events=json.loads(maps)
 # Demonstrate recovery from old maps in a disposable sandbox, without legacy synths.
 import frost_foley_60 as renderer
 with tempfile.TemporaryDirectory(prefix='frost-restore-',dir=R/'.tools') as temporary:
  root=Path(temporary);temporary_assets=root/'assets';temporary_pack=root/'pack'
  temporary_assets.mkdir();(temporary_assets/'audio_licenses').mkdir()
  (temporary_assets/'sounds.json').write_text(json.dumps(baseline),encoding='utf-8')
  shutil.copytree(P/'sources',temporary_pack/'sources');shutil.copyfile(P/'sources.json',temporary_pack/'sources.json')
  try:
   renderer.A,renderer.P=temporary_assets,temporary_pack;renderer.restore_selected()
   recovered=json.loads((temporary_assets/'sounds.json').read_text(encoding='utf-8'))
   check(all(recovered[event]==events[event] for event in EVENTS),'Restoration replaces legacy event maps with current selected recordings')
   check(all((temporary_assets/'sounds'/r['file']).read_bytes()==(A/'sounds'/r['file']).read_bytes() for r in rows),'Restoration from sources recovers exact eleven output files')
  finally:renderer.A,renderer.P=A,P
 check(EVENTS<=set(baseline),'No new registry IDs; all nine events already existed')
 sources={s['file']:s for s in json.loads((P/'sources.json').read_text(encoding='utf-8'))};pcm=[]
 for row in rows:
  path=A/'sounds'/row['file'];x,sr=sf.read(path);info=sf.info(path)
  check(info.subtype=='VORBIS' and sr==44100 and x.ndim==1 and np.isfinite(x).all(),'Spatial mono Vorbis '+row['file'])
  check(.05<len(x)/sr<=.86 and max(abs(x))<.8 and abs(x[0])<.02 and abs(x[-1])<.02,'Duration, peak and faded edges '+row['file'])
  if row['event']=='karachun.scythe_windup':check(len(x)/sr/.94<10/20,'Windup finishes before earliest hit10 at lowest source pitch')
  check(digest(path.read_bytes())==row['sha256'],'Selected output SHA '+row['file'])
  source=sources[row['source']]
  check(source['license']=='CC0-1.0' and digest((P/row['source']).read_bytes())==source['sha256']==row['source_sha256'],'Legal source and exact recorded take '+row['source'])
  pcm.append(digest(x.tobytes()))
  check(any(e['name']==row['file'][:-4] for e in events[row['event']]['sounds']),'Actual event maps to installed resource '+row['event'])
  for lang in ['ru_ru','en_us']:
   d=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'))
   check(events[row['event']]['subtitle'] in d,'Existing subtitle '+lang+' '+row['event'])
 check(len(set(pcm))==11 and len({r['source'] for r in rows})==11,'Eleven distinct decoded clips and eleven separate source takes')
 for event in EVENTS:check(events[event]['subtitle']==baseline[event]['subtitle'],'Subtitle identity preserved '+event)
 for event,v in baseline.items():
  if not v.get('sounds'):check(events[event]==v,'Quarantine unchanged '+event)
 # Accepted visuals and complete frost/night mechanics must be byte-identical to main.
 tracked=subprocess.check_output(['git','ls-tree','-r','--name-only',BASE],cwd=R).decode().splitlines()
 protected=[]
 for name in tracked:
  low=name.lower()
  java=name.startswith('src/main/java/') and any(k in low for k in ['morozko','karachun','frost','nightcontent','nightblade','nightskills','nightcombat','nightraven'])
  visual=name.startswith('src/main/resources/assets/') and any(k in low for k in ['karachun','morozko','frost_mirror','heart_of_eternal_cold']) and '/sounds' not in name and name.endswith(('.png','.json'))
  if java or visual:
   path=R/name;data=path.read_bytes();old=original(name)
   textual=java or name.endswith('.json')
   check(data.replace(b'\r\n',b'\n')==old.replace(b'\r\n',b'\n') if textual else data==old,'Accepted visual / full mechanic unchanged (text EOL normalized) '+name)
   protected.append({'file':name,'sha256':digest(data)})
 check(len(protected)>40,'Protected accepted models/UV/textures/icons and full frost/night code enumerated')
 contracts={'karachun/Karachun.java':['"scythe_windup"','age==(a==3?14:10)','"scythe_hit"'], 'morozko/Morozko.java':['a==1&&age==10','"staff_hit"','a==2&&age==13','"ice_bolt"','"clone_break"'], 'morozko/FrostSkills.java':['"staff_shoot"'], 'morozko/FrostHazard.java':['kind()==1?"staff_icicles":kind()==2?"wave":"spike_break"'], 'morozko/FrostShard.java':['"spike_break"'], 'morozko/IceDouble.java':['"clone_break"'], 'morozko/PlayerDouble.java':['"mirror_break"']}
 for file,tokens in contracts.items():
  text=(R/'src/main/java/org/slavicmyths'/file).read_text(encoding='utf-8')
  check(all(token in text for token in tokens),'Real existing callsite contract '+file)
 for name in ['morozko_assets.py','karachun_assets.py']:
  code=(R/'tools'/name).read_text(encoding='utf-8');check("from frost_foley_60 import EVENTS" in code and 'in EVENTS:continue' in code and 'restore_selected()' in code,'Legacy generator skips selected synthetic outputs and restores recorded maps '+name)
 check('restore_frost_foley()' in (R/'tools/voice_rework_60.py').read_text(encoding='utf-8'),'Shared restoration pipeline includes frost batch')
 version=next(s.split('=',1)[1].strip() for s in (R/'gradle.properties').read_text(encoding='utf-8').splitlines() if s.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'Production JAR CRC')
  check(z.read('assets/slavicmyths/sounds.json')==maps,'Production event map byte parity')
  for row in rows:check(z.read('assets/slavicmyths/sounds/'+row['file'])==(A/'sounds'/row['file']).read_bytes(),'Production OGG byte parity '+row['file'])
  check(z.read('assets/slavicmyths/audio_licenses/frost-foley-60.txt')==(A/'audio_licenses/frost-foley-60.txt').read_bytes(),'Source license packaged')
  check(not any(n.startswith(('tools/smoke/','org/slavicmyths/smoke/')) for n in z.namelist()),'No smoke test package in production JAR (gameplay SmokeAging is legitimate)')
 receipt=json.loads(receipt_before)
 check(receipt_path.read_bytes()==receipt_before,'Installation receipt not rewritten')
 check(digest(Path(receipt['installed_jar']).read_bytes())==receipt['sha256'],'Previously installed JAR unchanged; current frost batch not installed')
 report=dict(status='IN_PROGRESS',source_base=BASE,source_revision_at_test=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),runtime_launches=0,owner_accepted=False,files=11,events=9,checks=len(checks),jar=str(jar),jar_sha256=digest(jar.read_bytes()),installed_receipt_unchanged=True,protected=protected,results=checks)
 (P/'gates.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps({k:v for k,v in report.items() if k not in ['protected','results']},indent=2))
if __name__=='__main__':main()
