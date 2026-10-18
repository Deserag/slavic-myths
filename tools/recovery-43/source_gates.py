"""Check source/JAR parity, publication metadata and edited documentation offline."""
import ast,hashlib,io,json,re,struct,subprocess,zipfile
from pathlib import Path
from PIL import Image
R=Path(__file__).resolve().parents[2]
BASE='fb2994e958911169317523e1066b8a1ca7a155af'
def git(*args):return subprocess.check_output(['git',*args],cwd=R)
def need(ok,message):
    if not ok:raise AssertionError(message)

def main():
    properties=dict(re.findall(r'^([\w_]+)=(.*)$',(R/'gradle.properties').read_text(),re.M))
    version=properties['mod_version'];need(version=='1.9.7.5','source version preserved')
    jar=R/f'build/libs/slavicmyths-{version}.jar';checks={'json':0,'png':0,'resources':0,'classes':0,'links':0,'archived_tools':0}
    with zipfile.ZipFile(jar) as z:
        names=set(z.namelist());need(z.testzip() is None,'JAR CRC')
        metadata=z.read('META-INF/neoforge.mods.toml').decode()
        need(f'version="{version}"' in metadata or f'version = "{version}"' in metadata,'expanded mod metadata')
        need('${mod_version}' not in metadata,'no unexpanded version')
        need(version in z.read('META-INF/MANIFEST.MF').decode(),'manifest version')
        for p in (R/'src/main/resources').rglob('*'):
            if not p.is_file():continue
            rel=p.relative_to(R/'src/main/resources').as_posix();data=p.read_bytes()
            need(rel in names,'missing resource '+rel)
            if rel=='META-INF/neoforge.mods.toml':continue
            need(z.read(rel)==data,'resource parity '+rel);checks['resources']+=1
            if p.suffix=='.json':json.loads(data);checks['json']+=1
            if p.suffix=='.png':
                with Image.open(io.BytesIO(data)) as im:im.verify()
                checks['png']+=1
        output=R/'build/classes/java/main'
        expected={p.relative_to(output).as_posix() for p in output.rglob('*.class')}
        actual={n for n in names if n.endswith('.class')}
        need(actual==expected,'production-only classes; no extra fixtures/dependencies')
        for n in actual:
            need(struct.unpack('>H',z.read(n)[6:8])[0]==65,'Java21 '+n)
        checks['classes']=len(actual)
        need(not any(n.startswith(('tools/','org/slavicmyths/smoke/','org/slavicmyths/gametest/')) for n in names),'no test tools')
    for file,revision in [('src/main/java/org/slavicmyths/poloz/PolozEvents.java','69262a0545c92f069d8bfbbfd722761bd3e34dc5'),('src/main/java/org/slavicmyths/client/GuardianClient.java','224a320baad1f7a7d1e952f619cd7a847438f028')]:
        need((R/file).read_bytes().replace(b'\r\n',b'\n')==git('show',revision+':'+file).replace(b'\r\n',b'\n'),'accepted production source parity '+file)
    changed=git('diff','--name-only',BASE,'--','src/main/java').decode().splitlines()
    need(set(changed)=={'src/main/java/org/slavicmyths/poloz/PolozEvents.java','src/main/java/org/slavicmyths/client/GuardianClient.java'},'only two production Java changes')
    need(not git('diff','--name-only',BASE,'--','src/main/resources','art'),'resources/art unchanged')
    for path in ['README.md','CHANGELOG.md','docs/VERSIONING.md','docs/CONTENT_CATALOG.md','docs/maintenance/issue-43/HISTORICAL_ARTIFACTS.md']:
        p=R/path;body=p.read_text('utf8')
        for target in re.findall(r'\]\(([^)]+)\)',body):
            if '://' in target or target.startswith('#'):continue
            clean=target.split('#')[0];need((p.parent/clean).exists(),'broken link '+path+' -> '+target);checks['links']+=1
    need(not re.search(r'(?<![A-Za-z])[A-Za-z]:[/\\]|pavel|token=',(R/'README.md').read_text('utf8'),re.I),'no private absolute paths in README')
    manifest=json.loads((R/'docs/maintenance/issue-43/TRACKED_ARTIFACTS.json').read_text('utf8'))
    tracked=set(git('ls-files','-z').decode().split('\0'))
    for row in manifest['files']:
        if row['category']!='historical_tool':need(row['path'] not in tracked,'junk still tracked '+row['path']);continue
        p=R/row['path'];text=p.read_text('utf-8-sig')
        if p.suffix=='.py':
            tree=ast.parse(text);nodes=tree.body
            if isinstance(nodes[0],ast.Expr) and isinstance(nodes[0].value,ast.Constant):nodes=nodes[1:]
            while isinstance(nodes[0],ast.ImportFrom) and nodes[0].module=='__future__':nodes=nodes[1:]
            need(isinstance(nodes[0],ast.If) and isinstance(nodes[0].body[0],ast.Raise),'archive stop before IO '+str(p))
        else:need(text.startswith("throw 'ARCHIVED"),'PowerShell archive stop '+str(p))
        checks['archived_tools']+=1
    log=Path('C:/Users/pavel/.codex/recovery/slavic-myths-20261018/FINAL_CLEAN_BUILD.log').read_text('utf8',errors='replace')
    need('BUILD SUCCESSFUL' in log and '> Task :clean' in log,'actual clean build')
    need(not re.search(r'^> Task :\S*(?:runClient|runServer|gameTest|GameTest)',log,re.M),'no game task')
    report={'status':'PASS','source_version':version,'checks':checks,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'jar_bytes':jar.stat().st_size,'minecraft_launches':0,'server_launches':0,'gametest_launches':0,'runtime_acceptance':'NOT_RUN','build_compileJava':'FROM-CACHE' if '> Task :compileJava FROM-CACHE' in log else 'EXECUTED'}
    (R/'docs/maintenance/issue-43/FINAL_GATES.json').write_text(json.dumps(report,indent=2)+'\n','utf8');print(json.dumps(report))

if __name__=='__main__':main()
