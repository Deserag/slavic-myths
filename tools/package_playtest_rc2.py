"""Package the gated RC2 JAR; dependency versions stay pinned to the already verified player pack."""
from pathlib import Path
import json,hashlib,shutil,zipfile
R=Path(__file__).resolve().parents[1];V='0.9.10-rc2';out=R/'release'/V;out.mkdir(parents=True,exist_ok=True)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/'build/libs'/('slavicmyths-'+V+'.jar')
for log,n in [('playtest-rc2-workstations-final.log',9),('playtest-rc2-regressions.log',27)]:
 text=(R/'work'/log).read_text(encoding='utf-8-sig');assert f'All {n} required tests passed' in text and 'BUILD SUCCESSFUL' in text and '/ERROR]' not in text,log
assert 'BUILD SUCCESSFUL' in (R/'work/playtest-rc2-clean-build.log').read_text(encoding='utf-8-sig')
with zipfile.ZipFile(jar) as z:
 assert V in z.read('META-INF/neoforge.mods.toml').decode()
 assert not any('GameTests' in n or n.startswith(('org/slavicmyths/verify/','data/slavicmyths_hotfix/')) for n in z.namelist())
shutil.copy2(jar,out/jar.name);(out/'SHA256SUMS.txt').write_text(sha(jar)+'  '+jar.name+'\n')
dist=R/'work/rc2-distribution';dist.mkdir(exist_ok=True);(dist/'mods').mkdir(exist_ok=True);shutil.copy2(jar,dist/'mods'/jar.name)
previous=R/'work/rc1-distribution';manifest=json.loads((previous/'MODS_LOCK.json').read_text(encoding='utf-8'));manifest['slavicmyths']={'filename':jar.name,'sha256':sha(jar)}
(dist/'MODS_LOCK.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');shutil.copy2(previous/'DOWNLOAD_MODS.ps1',dist/'DOWNLOAD_MODS.ps1')
(dist/'README.md').write_text((previous/'README.md').read_text(encoding='utf-8').replace('0.9.10-rc1','0.9.10-rc2').replace('RC1','RC2'),encoding='utf-8')
for name in ['CHANGELOG_0.9.10-RC2.md','DEPENDENCIES.md','KNOWN_ISSUES.md']:shutil.copy2(out/name,dist/name)
remaining=(out/'KNOWN_ISSUES.md').read_text(encoding='utf-8')+'\n## Завершено в RC2\n\nСерверная приёмка: 9/9 hotfix/workstation tests; общая регрессия 27/27, включая сохранность кладки и доступный подход всех трёх курганов. Щит: реальное блокирование/износ/axe cooldown/repair/enchant support. Камень пути: menu/offering/XP/skill/navigation/NBT/range guard. Наковальня: forge/install/remove, materials/XP, validation, shift-click, return on close, component NBT. Кухня: все 12 рецептов/guards/consumption/tool wear/container remainder/shift-click/NBT. 16 missing door parents исправлены, asset reference issues: 0. Клиент не запускался; реальный multiplayer/перезаход и UI-визуал остаются ручными.\n'
(dist/'REMAINING_WORK.md').write_text(remaining,encoding='utf-8');(out/'REMAINING_WORK.md').write_text(remaining,encoding='utf-8');(dist/'SHA256SUMS.txt').write_text(sha(jar)+'  mods/'+jar.name+'\n')
archive=out/('slavicmyths-'+V+'-player-pack.zip')
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted(dist.rglob('*')):
  if p.is_file():z.write(p,'Slavic-Myths-0.9.10-RC2/'+p.relative_to(dist).as_posix())
files=[]
for mod in manifest['mods']:
 p=R/'.tools/test-pack-0.9.4'/mod['filename'];assert sha(p)==mod['sha256'];content=p.read_bytes()
 files.append({'path':'mods/'+mod['filename'],'hashes':{'sha1':hashlib.sha1(content).hexdigest(),'sha512':hashlib.sha512(content).hexdigest()},'env':{'client':'required','server':'required' if mod['required'] else 'unsupported'},'downloads':[mod['downloadUrl']],'fileSize':len(content)})
index={'formatVersion':1,'game':'minecraft','versionId':V,'name':'Slavic Myths 0.9.10 RC2','summary':'Public playtest; server gates passed; client visuals remain manual.','files':files,'dependencies':{'minecraft':'1.21.1','neoforge':'21.1.255'}}
with zipfile.ZipFile(out/('slavicmyths-'+V+'.mrpack'),'w',zipfile.ZIP_DEFLATED) as z:
 z.writestr('modrinth.index.json',json.dumps(index,indent=2)+'\n');z.write(jar,'overrides/mods/'+jar.name)
report={'version':V,'jar':str(jar),'sha256':sha(jar),'build':'PASS','resources':'PASS','missingAssetReferences':0,'hotfixAndWorkstationTests':'9/9 PASS','regressionTests':'27/27 PASS','serverLaunchesThisContinuation':3,'clientLaunches':0,'realMultiplayerAndReconnect':'MANUAL PENDING','visualAcceptance':'MANUAL PENDING','msptMeasured':False,'sourceBranch':'main','distributionBranch':'release/0.9.10-distribution'}
report_dir=R/'docs/verification/playtest-0.9.10-rc2';report_dir.mkdir(parents=True,exist_ok=True);(report_dir/'acceptance.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('RC2 package PASS SHA256='+sha(jar))
