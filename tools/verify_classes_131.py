"""Verify the actual production JAR and all packaged resources, without claiming client QA."""
from pathlib import Path
import hashlib,json,re,zipfile
from PIL import Image
import class_resources_131 as art

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M).group(1)
jar=ROOT/f'build/libs/slavicmyths-{version}.jar';checks=0
def check(ok,message):
    global checks
    checks+=1
    if not ok:raise AssertionError(message)
before={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in RES.rglob('*') if p.is_file()}
art.main();after={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in RES.rglob('*') if p.is_file()}
check(before==after,'Class resource generator changed current resources')
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));checks+=1
definitions=(ROOT/'src/main/java/org/slavicmyths/rpg/classes/ClassDefinitions.java').read_text()
skills=set(re.findall(r'(?:add|evolution)\(s,"([a-z_]+)"',definitions));check(skills==set(art.SKILLS),'Definition/icon/translation catalog mismatch')
icons=RES/'assets/slavicmyths/textures/gui/classes';hashes=[]
for skill in skills:
    p=icons/f'{skill}.png'
    with Image.open(p) as im:check(im.size==(32,32) and im.getbbox() is not None,'Missing/empty icon '+skill)
    hashes.append(hashlib.sha256(p.read_bytes()).hexdigest())
check(len(set(hashes))==len(skills),'Identical skill silhouettes')
for locale in ('ru_ru','en_us'):
    data=json.loads((RES/f'assets/slavicmyths/lang/{locale}.json').read_text(encoding='utf-8'))
    for skill in skills:
        check(bool(data.get(f'classskill.slavicmyths.{skill}')),'Missing skill name '+skill)
        check(bool(data.get(f'classskill.slavicmyths.{skill}.description')),'Missing skill description '+skill)
    for cls in art.NAMES:check(bool(data.get(f'class.slavicmyths.{cls}')),'Missing class/branch name '+cls)
for source in (ROOT/'src/main/java/org/slavicmyths/rpg/classes').glob('*.java'):check('net.minecraft.client.' not in source.read_text(),'Common/client isolation '+source.name)
with zipfile.ZipFile(jar) as z:
    names=set(z.namelist());check(f'Implementation-Version: {version}' in z.read('META-INF/MANIFEST.MF').decode(),'Wrong manifest version')
    check('META-INF/neoforge.mods.toml' in names and b'1.3.1' in z.read('META-INF/neoforge.mods.toml'),'NeoForge production metadata')
    check(not any('/verify/' in n or '/smoke/' in n or 'GameTests' in n or '/slavicmyths_classes/' in n for n in names),'Test fixtures in production JAR')
    for p in RES.rglob('*'):
        if not p.is_file():continue
        name=p.relative_to(RES).as_posix();check(name in names,'Resource not bundled '+name)
        if name!='META-INF/neoforge.mods.toml':check(z.read(name)==p.read_bytes(),'Resource mismatch '+name)
    for cls in ('ClassState','ClassDefinitions','ClassRuntime','ClassEvents','ClassNetwork','ClassBalance','ClassControl','ClassCommands'):check(f'org/slavicmyths/rpg/classes/{cls}.class' in names,'Missing production module '+cls)
    for cls in ('ClassClient','ClassScreen'):check(f'org/slavicmyths/client/{cls}.class' in names,'Missing client module '+cls)
report={'version':version,'checks':checks,'skills':len(skills),'base_classes':3,'branches':18,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'size':jar.stat().st_size,'client_launches':0,'client_gameplay_verified':False,'jar':str(jar)}
(ROOT/'docs/verification/classes-1.3.1-resources.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
