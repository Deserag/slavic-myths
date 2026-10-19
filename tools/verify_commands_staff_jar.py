"""Current production JAR parity and scoped source gate. No game/audio generation."""
from pathlib import Path
import hashlib,json,re,subprocess,tomllib,zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/commands-74'
BASE='09d1d844'

def main():
    props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text().splitlines() if '=' in l)
    jar=R/f"build/libs/slavicmyths-{props['mod_version']}.jar"
    registrars=json.loads((O/'registrars-before.json').read_text())
    allowed={r['file'] for r in registrars}|{
        'src/main/java/org/slavicmyths/client/FrostClient.java',
        'src/main/java/org/slavicmyths/morozko/FrostSkills.java',
        'src/main/java/org/slavicmyths/morozko/FrostShard.java',
        'src/main/java/org/slavicmyths/morozko/FrostHazard.java',
        'src/main/java/org/slavicmyths/morozko/FrostEvents.java',
        'src/main/java/org/slavicmyths/morozko/FrostStaffTargeting.java',
        'src/main/java/org/slavicmyths/commands/CommandTree.java',
        'src/main/java/org/slavicmyths/commands/CommandHelp.java',
        'src/main/java/org/slavicmyths/commands/SlavicCommands.java',
        'src/main/resources/assets/slavicmyths/lang/ru_ru.json',
        'src/main/resources/assets/slavicmyths/lang/en_us.json'}
    changed=subprocess.check_output(['git','diff','--name-only',BASE,'--','src/main'],cwd=R,text=True).splitlines()
    changed+=subprocess.check_output(['git','ls-files','--others','--exclude-standard','src/main'],cwd=R,text=True).splitlines()
    assert set(changed)<=allowed,sorted(set(changed)-allowed)
    subscribers=[];contributors=[]
    for p in (R/'src/main/java').rglob('*.java'):
        s=p.read_text(encoding='utf-8')
        if re.search(r'@SubscribeEvent\s+public static void \w+\([^)]*RegisterCommandsEvent\s+\w+\)',s):subscribers.append(p.name)
        if 'org.slavicmyths.commands.CommandTree tree)' in s:contributors.append(p.relative_to(R).as_posix())
    assert subscribers==['SlavicCommands.java'],subscribers
    assert set(contributors)=={r['file'] for r in registrars}
    src=R/'src/main/java/org/slavicmyths/morozko'
    for name in ('FrostSkills.java','FrostShard.java','FrostHazard.java'):
        assert 'FrostStaffTargeting.canHit' in (src/name).read_text(encoding='utf-8'),name
    assert 'FrostStaffTargeting.inStream' in (src/'FrostSkills.java').read_text()
    assert 'staffInteractAt' in (src/'FrostEvents.java').read_text() and 'staffInteract(' in (src/'FrostEvents.java').read_text()
    log=(R/'.tools/issues-73-74-staff-final.log').read_text(errors='replace')
    assert all(x in log for x in ('BUILD SUCCESSFUL','COMMANDS_OFFLINE PASS','FROST_STAFF_OFFLINE PASS'))
    resources=classes=0
    with zipfile.ZipFile(jar) as z:
        assert z.testzip() is None
        metadata=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
        assert any(x['modId']=='slavicmyths' and x['version']==props['mod_version'] for x in metadata['mods'])
        assert not any('/verify/' in n or n.startswith(('tools/','smoke/')) for n in z.namelist())
        for p in (R/'src/main/resources').rglob('*'):
            if p.is_file():
                key=p.relative_to(R/'src/main/resources').as_posix()
                if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
                if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
                resources+=1
        for p in (R/'build/classes/java/main').rglob('*.class'):
            assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes()
            assert int.from_bytes(p.read_bytes()[6:8],'big')==65
            classes+=1
    inputs={p.relative_to(R).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'src/main').rglob('*') if p.is_file()}
    fingerprint=hashlib.sha256(json.dumps(inputs,sort_keys=True).encode()).hexdigest()
    report=dict(status='PASS_OFFLINE',baseline=BASE,version=props['mod_version'],jar=str(jar),jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),resources=resources,compiled_classes=classes,source_fingerprint=fingerprint,registrars=len(registrars),new_event_subscribers=subscribers,runtime_launches=0,polymc='NOT_UPDATED_BY_THIS_TASK',owner_acceptance='PENDING')
    (O/'jar-check.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report,indent=2))
if __name__=='__main__':main()
