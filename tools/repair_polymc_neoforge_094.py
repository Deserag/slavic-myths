"""Repair stale PolyMC checksums using the official NeoForge Maven SHA-1 files.

Only the dedicated 0.9.4 instance receives a local component patch. No launcher
or Minecraft process is started and no existing worlds/mod JARs are changed.
"""
from pathlib import Path
import copy, hashlib, json, shutil, urllib.request, zipfile

ROOT=Path(__file__).resolve().parents[1]
lock_path=ROOT/'packaging/test-pack-lock.json'
lock=json.loads(lock_path.read_text(encoding='utf-8'))
launcher=Path(lock['polyMCPath']);instance=Path(lock['installedInstancePath'])
uid='net.neoforged.neoforge';version=lock['neoForge']
cache=launcher/'meta'/uid/(version+'.json')
original=json.loads(cache.read_text(encoding='utf-8'));patched=copy.deepcopy(original)
assert original['uid']==uid and original['version']==version=='21.1.255'
evidence=ROOT/'work/launcher-repair-094';evidence.mkdir(parents=True,exist_ok=True)
backup=evidence/'polymc-component-original.json'
if not backup.exists(): shutil.copy2(cache,backup)
repairs=[]
for classifier in ['installer','universal']:
    filename=f'neoforge-{version}-{classifier}.jar'
    url=f'https://maven.neoforged.net/releases/net/neoforged/neoforge/{version}/{filename}'
    data=urllib.request.urlopen(url,timeout=45).read()
    official=urllib.request.urlopen(url+'.sha1',timeout=30).read().decode('ascii').strip().split()[0]
    actual=hashlib.sha1(data).hexdigest()
    if actual!=official: raise SystemExit('Official Maven checksum mismatch; refusing repair')
    staged=evidence/filename;staged.write_bytes(data)
    with zipfile.ZipFile(staged) as archive:
        if archive.testzip() is not None: raise SystemExit('Corrupt official archive')
        if classifier=='installer':
            profile=json.loads(archive.read('install_profile.json'))
            if version not in profile['version']: raise SystemExit('Wrong installer version')
    replacements=0;old_hashes=set()
    for section in ['libraries','mavenFiles']:
        for library in patched.get(section,[]):
            artifact=library.get('downloads',{}).get('artifact',{})
            if artifact.get('url')==url:
                old_hashes.add(artifact['sha1']);artifact.update(sha1=actual,size=len(data));replacements+=1
    if not replacements: raise SystemExit('Expected component artifact absent')
    destination=launcher/'libraries/net/neoforged/neoforge'/version/filename
    destination.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(staged,destination)
    assert hashlib.sha1(destination.read_bytes()).hexdigest()==official
    repairs.append({'filename':filename,'url':url,'checksum_url':url+'.sha1',
        'old_metadata_sha1':sorted(old_hashes),'official_sha1':official,'size':len(data),
        'installed_path':destination.as_posix(),'sha256':hashlib.sha256(data).hexdigest()})
patch=instance/'patches'/f'{uid}.json';patch.parent.mkdir(exist_ok=True)
if patch.exists():
    old=json.loads(patch.read_text(encoding='utf-8'))
    if old!=patched: raise SystemExit('Existing custom component differs; refusing overwrite')
patch.write_text(json.dumps(patched,indent=2)+'\n',encoding='utf-8')
report={'status':'FILES_AND_CHECKSUMS_REPAIRED','cause':'PolyMC component SHA-1 differs from official Maven for installer and universal; launcher rejected downloads, leaving installer absent.',
    'instance_patch':patch.as_posix(),'minecraft_launched_by_agent':False,'artifacts':repairs,
    'launcher_cache_unchanged':True,'legacy_instances_unchanged':True}
(ROOT/'docs/port/launcher-repair-0.9.4.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
lock['launcherChecksumRepair']=report
lock_path.write_text(json.dumps(lock,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Repaired two official NeoForge artifacts and instance-local component checksums. No Minecraft launch.')
