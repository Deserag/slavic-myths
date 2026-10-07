from pathlib import Path
import datetime, hashlib, json, re, shutil
root=Path('D:/slavic-myths')
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest()
logs={}
for name in ['thin','high','sequential','water','terrain','companions']:
    p=root/f'work/structure-priority-{name}-final.log'
    s=p.read_text(encoding='utf-8',errors='replace')
    assert 'BUILD SUCCESSFUL' in s and 'required tests passed' in s, name
    assert not re.search(r'/ERROR\]|BUILD FAILED|required tests failed',s), name
    logs[name]={'path':str(p),'sha256':digest(p),'pass':True}
    if name in ['thin','high']:
        assert 'actualCommands=10 realStructures=10 accessibleEntrances=10' in s,name
    if name=='sequential':
        assert s.count('MANUAL_SEQUENTIAL_COMMAND_PASS')==6,name
    if name=='companions': assert 'All 27 required tests passed' in s,name
for name in ['clean-build','jar-validation','static-data-final']:
    p=root/f'work/structure-priority-{name}.log'
    s=p.read_text(encoding='utf-8',errors='replace')
    assert ('BUILD SUCCESSFUL' if name=='clean-build' else 'PASS resource integrity' if name=='jar-validation' else '2676 references checked; 0 errors') in s,name
    logs[name]={'path':str(p),'sha256':digest(p),'pass':True}
jar=root/'build/libs/slavicmyths-0.9.9-dev.jar'
profile=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing')
mods=profile/'.minecraft/mods'
installed=mods/jar.name
assert installed.is_file()
other={p.name:digest(p) for p in mods.glob('*.jar') if not p.name.startswith('slavicmyths-')}
assert [p.name for p in mods.glob('slavicmyths-*.jar')]==[jar.name]
stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S')
backup=profile/'slavicmyths-backups'/stamp/installed.name
backup.parent.mkdir(parents=True,exist_ok=False)
old_hash=digest(installed)
shutil.copy2(installed,backup)
assert digest(backup)==old_hash
shutil.copy2(jar,installed)
assert digest(installed)==digest(jar)
assert other=={p.name:digest(p) for p in mods.glob('*.jar') if not p.name.startswith('slavicmyths-')}
receipt={'version':'0.9.9-dev','purpose':'Authorized structure-priority override testing','releaseAccepted':False,'clientLaunched':False,'profile':str(profile),'installedJar':str(installed),'installedSha256':digest(installed),'previousJarBackup':str(backup),'previousSha256':old_hash,'otherModsUnchanged':True,'otherModHashes':other,'worldsModified':False,'checks':logs,'manualPlacement':'PASS: thin/high 10 each; sequential 6 from one source; accessible structures and preserved earlier entrances','naturalNoiseCoverage':'NOT ACCEPTED: full ordinary-world coverage and compensation remain unfinished','cancelSemantics':'Already changed terrain/partial structures remain; tickets and active job are released','timeSlice':'2 ms target, bounded operations; cold calls/GC may exceed target; no measured client MSPT guarantee'}
out=root/'docs/verification/mob-worldgen-0.9.9/structure-priority-override.json'
out.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(root/'docs/verification/mob-worldgen-0.9.9/polymc-structure-priority-installation.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
status=f"Acceptance update: thin/high superflat each passed 10 actual placements; six sequential commands from one point passed; water/native-terrain gates and all 27 companion tests passed without ERROR logs. Clean build and production JAR audit passed. Current override JAR is installed in Slavic-Myths-1.21.1-Testing (SHA256 {digest(jar)}); previous dev JAR backed up outside mods. Other mods/worlds unchanged; client not launched. Evidence: docs/verification/mob-worldgen-0.9.9/structure-priority-override.json. Full ordinary-world natural coverage remains NOT ACCEPTED.\n\n"
for name in ['docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md','docs/worldgen/STRUCTURE_COVERAGE_AUDIT_0.9.9.md']:
    p=root/name;s=p.read_text(encoding='utf-8');p.write_text(status+s,encoding='utf-8')
p=root/'docs/worldgen/STRUCTURE_PRIORITY_OVERRIDE_0.9.9.md'
s=p.read_text(encoding='utf-8').replace('Evidence files are listed in `docs/verification/mob-worldgen-0.9.9/structure-priority-override.json` when the final gates finish.',status.strip())
p.write_text(s,encoding='utf-8')
p=root/'README.md';s=p.read_text(encoding='utf-8');s=s.replace('## Ручная генерация 0.9.9-dev','Текущий structure-priority JAR проверен и установлен в тестовый профиль PolyMC; предыдущий dev JAR сохранён вне mods. Подтверждены 10 сценариев на тонком и 10 на высоком superflat, шесть команд из одной точки и 27 общих headless-тестов. Полное естественное покрытие обычного мира пока не принято.\n\n## Ручная генерация 0.9.9-dev',1);p.write_text(s,encoding='utf-8')
print(json.dumps({'installedJar':str(installed),'sha256':digest(installed),'backup':str(backup),'checks':'PASS','clientLaunched':False},ensure_ascii=False))
