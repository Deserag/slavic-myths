from pathlib import Path
import json,hashlib,zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/2026-10-29-hell-remaining';base=Path.home()/'AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.8-HELL-Testing/.minecraft/mods'
installed=list(base.glob('slavicmyths*.jar'));current=R/'build/libs/slavicmyths-2.0.10-HELL.jar';report={'scope':'READ_ONLY actual installed JAR; no launch or replacement','installed_paths':[str(p.resolve()) for p in installed],'single_slavicmyths_jar':len(installed)==1,'candidate_installed':False,'runtime':'NOT_RUN','installed':[]}
for p in installed:
 with zipfile.ZipFile(p) as old,zipfile.ZipFile(current) as new:
  names=old.namelist();entry={'path':str(p.resolve()),'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'metadata':old.read('META-INF/neoforge.mods.toml').decode('utf-8'),'manifest':old.read('META-INF/MANIFEST.MF').decode('utf-8'),'boss_head_entry_parity':{}}
  for name in new.namelist():
   if name.endswith('.class') and any(token in name for token in ['/trophy/','/client/TrophyHeadsClient','/navya/client/Guardian','/losthalls/client/Posten','/blackcinder/client/Cinder','/burningislands/client/Islands']):entry['boss_head_entry_parity'][name]='SAME' if name in names and old.read(name)==new.read(name) else 'DIFFERENT_OR_MISSING'
  report['installed'].append(entry)
(O/'actual-installed-jar.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print({k:v for k,v in report.items() if k!='installed'});print([(x['sha256'],len(x['boss_head_entry_parity']),set(x['boss_head_entry_parity'].values())) for x in report['installed']])
