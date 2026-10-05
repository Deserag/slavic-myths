"""Port checks. --resources-only validates intermediate work, never final parity.

Default also requires migrated Java, a production Java-21 JAR and completed
gates. Static resource equivalence does not prove actual registry/codec loading.
"""
from pathlib import Path
import hashlib, json, re, sys, zipfile
from port_resources_1211 import target_path, normalize

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT/'src/main/resources'
partial = '--resources-only' in sys.argv
navigation = '--navigation' in sys.argv
OUT = ROOT/('docs/verification/navigation-0.9.5' if navigation else 'docs/port')
OUT.mkdir(parents=True,exist_ok=True)
errors = []
def check(ok, text):
    if not ok:
        errors.append(text)

baseline = json.loads((ROOT/'docs/port/baseline-0.9.3.json').read_text(encoding='utf-8'))
properties = dict(line.split('=',1) for line in (ROOT/'gradle.properties').read_text().splitlines()
                  if '=' in line and not line.startswith('#'))
check(properties['minecraft_version'] == '1.21.1','Minecraft target')
check(properties['mod_version']=='0.9.5' if navigation else properties['mod_version'].startswith('0.9.4'),'Mod version')
build = (ROOT/'build.gradle').read_text()
check('JavaLanguageVersion.of(21)' in build,'Java toolchain 21')
check('net.neoforged.moddev' in build,'ModDevGradle')
for name in ('build.gradle','settings.gradle','gradle.properties'):
    check(not re.search(r'ForgeGradle|net\.minecraftforge:forge|36\.2\.42|1\.16\.5',
                        (ROOT/name).read_text()),'Legacy dependency in '+name)
metadata = RES/'META-INF/neoforge.mods.toml'
check(metadata.exists(),'NeoForge metadata missing')
check(not (RES/'META-INF/mods.toml').exists(),'Legacy active mods.toml')
check('modId="slavicmyths"' in metadata.read_text(),'Mod ID changed')
check(json.loads((RES/'pack.mcmeta').read_text())['pack']['pack_format']==34,'Resource pack format')
obsolete = {'recipes','advancements','loot_tables','structures','predicates','item_modifiers'}
for p in (RES/'data').rglob('*'):
    if p.is_dir():
        parts = p.relative_to(RES/'data').parts
        check(not (len(parts)==2 and parts[1] in obsolete),'Obsolete folder '+str(p))
        check(not (len(parts)==3 and parts[1]=='tags' and parts[2] in
                   ('items','blocks','entity_types','fluids','game_events')),'Obsolete tag folder '+str(p))

entries=baseline['entries']
check(all(r['old_id']==r['target_id'] for r in entries if r['old_id'].startswith('slavicmyths:')),
      'Changed Slavic Myths public ID')
check(len({(r['kind'],r['old_id']) for r in entries})==len(entries),'Duplicate inventory rows')
backup=ROOT/'.tools/port-backups/slavicmyths-0.9.3-pre-port.zip'
checked_data=checked_binary=0
with zipfile.ZipFile(backup) as z:
    for old, digest in baseline['source_sha256'].items():
        if not old.startswith('src/main/resources/'):
            continue
        old_path=ROOT/old
        target=target_path(old_path)
        if old_path.suffix in ('.png','.ogg','.nbt'):
            check(target.is_file(),'Missing preserved asset '+str(target))
            if target.is_file():
                check(hashlib.sha256(target.read_bytes()).hexdigest()==digest,'Changed binary '+str(target))
            checked_binary+=1
        elif old_path.suffix=='.json' and '/data/' in old:
            check(target.is_file(),'Missing data ID '+old)
            if target.is_file():
                expected=normalize(target,json.loads(z.read(old)))
                actual=json.loads(target.read_text(encoding='utf-8'))
                check(actual==expected,'Data semantics changed '+str(target))
            checked_data+=1
    # Baseline counters must agree with actual item model/blockstate assets.
    check(baseline['counts']['item']==len([r for r in entries if r['kind']=='item']), 'Item count')
    for r in entries:
        if r['kind'] not in ('item','block'):
            continue
        name=r['old_id'].split(':',1)[1]
        p=RES/('assets/slavicmyths/models/item' if r['kind']=='item' else
               'assets/slavicmyths/blockstates')/(name+'.json')
        check(p.exists(),'Missing registered content resource '+str(p))
for p in RES.rglob('*.json'):
    try:
        json.loads(p.read_text(encoding='utf-8'))
    except (ValueError,UnicodeError) as exc:
        errors.append(str(p)+': '+str(exc))

if not partial:
    for p in (ROOT/'src/main/java').rglob('*.java'):
        source=p.read_text(encoding='utf-8')
        check('net.minecraftforge.' not in source,'Unported Forge API '+str(p.relative_to(ROOT)))
        check(not re.search(r'getOrCreateTag\(|CompoundNBT\b|SimpleChannel\b|WorldSavedData\b',source),
              'Unported NBT/network/SavedData '+str(p.relative_to(ROOT)))
    jar=ROOT/'build/libs'/('slavicmyths-'+properties['mod_version']+'.jar')
    check(jar.exists(),'Production target JAR absent')
    if jar.exists():
        with zipfile.ZipFile(jar) as z:
            check('META-INF/neoforge.mods.toml' in z.namelist(),'JAR metadata')
            for name in z.namelist():
                if name.endswith('.class'):
                    check(int.from_bytes(z.read(name)[6:8],'big')==65,'Non-Java21 class '+name)
            check(not any(n.startswith(('mezz/','top/theillusivec4/','org/slavicmyths/verify/'))
                          for n in z.namelist()),'Bundled dependency/test classes')
            check('data/slavicmyths/structure/port_empty.nbt' not in z.namelist(),'Bundled test template')
    acceptance=OUT/'acceptance.json'
    check(acceptance.exists(),'Final gate acceptance evidence absent')
    if acceptance.exists():
        result=json.loads(acceptance.read_text())
        check(all(result.get('gates',{}).get(i)=='PASS' for i in 'ABCDEF'),
              'Port gates incomplete')
        check(result.get('client_launches')==0,'Unexpected client launch')
        check(result.get('jar_sha256')==hashlib.sha256(jar.read_bytes()).hexdigest(),'Acceptance JAR hash drift')
        for name,digest in result.get('evidence_sha256',{}).items():
            check(hashlib.sha256((OUT/name).read_bytes()).hexdigest()==digest,
                  'Acceptance evidence changed '+name)
        for name in (('runtime-core.log','runtime-companions.log') if navigation else ('finalize-runtime-nojei.log','finalize-runtime-companions.log')):
            log=(OUT/name).read_text(encoding='utf-8')
            check(all(marker in log for marker in ('PORT_RUNTIME_REGISTRIES_PASS checked=794 entities=47',
                'PORT_RUNTIME_DATA_PASS recipes=213 loot=227 advancements=154','PORT_RUNTIME_COMPONENTS_PASS',
                'PORT_RUNTIME_EXISTING_CHECKS_PASS','All 7 required tests passed' if navigation else 'All 4 required tests passed','BUILD SUCCESSFUL')),
                'Missing actual loader evidence '+name)
            if navigation:
                check(all(marker in log for marker in ('NAVIGATION_RUNTIME_MODEL_AND_WIRE_PASS','NAVIGATION_RUNTIME_SERVER_AUTHORITY_PASS',
                    'NAVIGATION_RUNTIME_LIFECYCLE_AND_HOMES_PASS')),'Missing navigation loader evidence '+name)
            check(not re.search(r'\[(?:ERROR|FATAL)\]|/ERROR\]|/FATAL\]',log),'Loader errors '+name)
        check('BUILD SUCCESSFUL' in (OUT/('clean-build.log' if navigation else 'finalize-clean-build.log')).read_text(encoding='utf-8'),
              'Missing clean-build evidence')
report=dict(scope='resources-only' if partial else 'final-port',
            binary_assets_checked=checked_binary,data_files_checked=checked_data,
            errors=errors,runtime_verified=not partial and not errors)
(OUT/('resource-check.json' if partial else 'final-check.json')).write_text(
    json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if errors:
    print('\n'.join(errors[:25]))
    print(f'FAIL: {len(errors)} checks. Port incomplete.')
    raise SystemExit(1)
print(f'PASS ({report["scope"]}): {checked_data} data files preserve baseline semantics; '
      f'{checked_binary} binary assets unchanged; metadata/layout checked. '
      f'Runtime loading {"verified by loader tests" if report["runtime_verified"] else "not checked in this scope"}.')
