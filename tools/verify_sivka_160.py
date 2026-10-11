"""Production, prior-feature preservation, resource and decoded audio gates. No Minecraft."""
from pathlib import Path
import json,zipfile,hashlib,subprocess,re
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';WORK=ROOT/'work/sivka';JAR=ROOT/'build/libs/slavicmyths-1.6.0.jar';checks=0
def check(ok,why):
    global checks;checks+=1
    if not ok:raise AssertionError(why)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    check(JAR.exists(),'production JAR');j=zipfile.ZipFile(JAR);base=zipfile.ZipFile(WORK/'baseline/slavicmyths-1.5.6.jar');names=set(j.namelist());oldclasses=oldassets=0
    exceptions=('org/slavicmyths/SlavicMyths.class','org/slavicmyths/registry/ModItemGroup.class')
    additive=('assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json')
    for name in base.namelist():
        if name.endswith('.class') and name not in exceptions:check(name in names and base.read(name)==j.read(name),'prior class preserved '+name);oldclasses+=1
        if name.startswith(('assets/','data/')):
            if name in additive:
                old=json.loads(base.read(name));new=json.loads(j.read(name));check(all(k in new and v==new[k] for k,v in old.items()),'prior keys preserved '+name)
            else:check(name in names and base.read(name)==j.read(name),'prior asset preserved '+name);oldassets+=1
    for p in (ROOT/'build/classes/java/main').rglob('*.class'):
        name=p.relative_to(ROOT/'build/classes/java/main').as_posix();check(j.read(name)==p.read_bytes(),'compiled artifact '+name);check(int.from_bytes(p.read_bytes()[6:8],'big')==65,'Java21 '+name)
    check(not any('SivkaChecks' in n or '/smoke/' in n or 'EarthChecks' in n for n in names),'test code excluded')
    metadata=j.read('META-INF/neoforge.mods.toml').decode();check('1.6.0' in metadata,'version metadata')
    for p in RES.rglob('*'):
        if not p.is_file():continue
        if p.suffix=='.json':json.loads(p.read_text(encoding='utf8'));check(True,'JSON')
        if p.suffix=='.png':
            with Image.open(p)as im:im.verify();check(True,'PNG')
        if 'sivka' in str(p).lower():check(p.relative_to(RES).as_posix() in names and j.read(p.relative_to(RES).as_posix())==p.read_bytes(),'Sivka packaged '+str(p))
    check(Image.open(A/'textures/entity/sivka_burka.png').size==(256,256),'atlas');check(Image.open(A/'textures/item/sivka_whistle.png').size==(64,64),'whistle')
    sounds=json.loads((A/'sounds.json').read_text());manifest=json.loads((WORK/'audio-source/manifest.json').read_text());check(len(manifest)==31,'31 OGG')
    for m in manifest:
        p=A/'sounds/entity/sivka'/m['file'];check(digest(p)==m['sha256'],'audio SHA '+m['file']);probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-of','json',str(p)]))['streams'][0];check(probe['codec_name']=='vorbis' and probe['sample_rate']=='44100' and probe['channels']==1,'mono44.1Vorbis '+m['file']);check(m['peak_dbfs']<=-1,'decoded peak '+m['file'])
    for key,value in sounds.items():
        if not key.startswith('sivka.'):continue
        for sound in value['sounds']:check((A/'sounds'/(sound['name'].split(':')[1]+'.ogg')).exists(),'sound resolved '+key)
        for lang in ('ru_ru','en_us'):check(value['subtitle'] in json.loads((A/'lang'/(lang+'.json')).read_text(encoding='utf8')),'subtitle '+key)
    check('BUILD SUCCESSFUL' in (WORK/'build.log').read_text(encoding='utf8'),'clean build log');check('SIVKA OFFLINE PASS' in (WORK/'build.log').read_text(encoding='utf8'),'offline checks')
    # Generator must reproduce both resources and actual compiled mesh/curves source.
    paths=list(RES.rglob('*'))+[ROOT/'src/main/java/org/slavicmyths/client/SivkaMesh.java',ROOT/'src/main/java/org/slavicmyths/sivka/SivkaPoses.java'];before={p:digest(p) for p in paths if p.is_file()};subprocess.run([__import__('sys').executable,str(ROOT/'tools/sivka_resources.py')],check=True);check(all(digest(p)==v for p,v in before.items()),'reproducible generator')
    report={'status':'PASS','checks':checks,'jar':str(JAR),'sha256':digest(JAR),'preserved_classes':oldclasses,'preserved_resources':oldassets,'audio_files':31,'minecraft_launches':0};out=ROOT/'docs/verification/sivka-1.6.0';out.mkdir(parents=True,exist_ok=True);(out/'resource-gates.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
