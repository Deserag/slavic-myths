"""Current #129 gate. Reads production; never regenerates it or launches Minecraft.

Historical #88 verifiers deliberately target frozen 46/47 snapshots. This gate
checks today's source against those accepted armor inputs without rejecting
unrelated, subsequently authorized shield/weapon changes.
"""
from pathlib import Path
import hashlib, json, math, subprocess, sys, zipfile
from PIL import Image

R = Path(__file__).resolve().parents[1]
OLD = R / 'docs/implementation/armor88-full'
OUT = R / 'docs/implementation/issue129-current'
EXCLUDED = ['invisibility_cap','woven_belt','hunter_belt','volchiy_poyas',
 'poyas_tugarina','gift_alyosha_belt','underground_guard_belt','perunite_shield',
 'retainer_shield','ward_shield','gift_ilya_shield','miners_goggles','finist_quiver']

def digest(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()

def read(p):
    return json.loads(p.read_text(encoding='utf-8-sig'))

def main():
    inventory = read(OLD/'inventory.json')
    ids = [row['id'] for row in inventory]
    assert len(ids) == len(set(ids)) == 29
    project = read(R/'work/project-after-acceptance.json')
    status = {x['content'].get('number'): x.get('status') for x in project['items']}
    assert all(status.get(n) == 'Готово' for n in range(122,129)), 'A1-A7 not accepted'
    catalog = (R/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf-8')
    armor = [line.split('\t')[0] for line in catalog.splitlines() if '\tarmor\t' in line]
    # Eight accessories were subsequently moved to their own catalog groups.
    # The old issue's 42-row armor tab is historical, not today's invariant.
    all_catalog_ids = {line.split('\t')[0] for line in catalog.splitlines() if line and not line.startswith('#')}
    assert set(ids) <= set(armor)
    assert set(armor) - set(ids) <= set(EXCLUDED)
    assert set(EXCLUDED) <= all_catalog_ids
    scope = read(OLD/'integration-scope.json')
    frozen = read(OLD/'build-source-sha256.json')
    for name in scope['changed'] + scope['added']:
        assert digest(R/name) == frozen[name], ('armor changed since source A/B',name)
    for name, expected in read(OLD/'preview-input-sha256.json').items():
        assert digest(R/name) == expected, ('stale preview input',name)
    for name, expected in read(OLD/'preview-output-sha256.json').items():
        assert digest(OLD/name) == expected, ('stale preview output',name)
    native = read(OUT/'native-current.json')
    assert native == read(OLD/'after-native.json'), 'fresh baked ModelPart differs from preview mesh'
    assert len(native) == 29*2*4
    for sample in native:
        assert sample['slot'] == next(x['slot'] for x in inventory if x['id']==sample['id'])
        texture = R/'src/main/resources/assets/slavicmyths'/sample['layers'][1]['texture']
        with Image.open(texture) as im:
            assert im.mode == 'RGBA'
        for face in sample['layers'][1]['faces']:
            assert len(face) == 4
            for v in face:
                assert len(v)==5 and all(math.isfinite(n) for n in v)
                assert 0 <= v[3] <= 1 and 0 <= v[4] <= 1
    overlap = read(OLD/'coplanar-intersections.json')
    assert all(not row['after'] for row in overlap['rows'])
    # Compare accessory files to the start of THIS task, not old43: the owner
    # explicitly accepted later shield fixes. Preserve that external work.
    start = read(OUT/'source-before.json')
    assert all(digest(R/name)==expected for name,expected in start.items()), 'source changed during QA'
    baseline = read(OLD/'baseline-sha256.json')
    accessory_rows = []
    for item in EXCLUDED:
        paths = {name:h for name,h in start.items() if item in Path(name).name}
        assert paths, ('missing accessory coverage',item)
        accessory_rows.append({'id':item,'unchanged_this_task':True,'files':paths,
          'historical_drift_before_this_task':[name for name,h in paths.items()
              if name in baseline and baseline[name]!=h]})
    version = next(line.split('=',1)[1] for line in (R/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
    jar = R/f'build/libs/slavicmyths-{version}.jar'
    with zipfile.ZipFile(jar) as z:
        assert z.testzip() is None
        for row in inventory:
            for f in row['files']:
                name = f['path']
                assert digest(R/name) == f['new_sha256'], ('inventory stale',name)
                if name.startswith('src/main/resources/'):
                    assert z.read(name.removeprefix('src/main/resources/')) == (R/name).read_bytes()
        assert not any('Armor88FullMeshes' in n for n in z.namelist())
    log = (R/'work/issue129-clean-build.log').read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in log and 'ARMOR88_FULL_NATIVE_PASS' in log
    rows = [{**row, 'current_files':{f['path']:digest(R/f['path']) for f in row['files']},
      'source_ab':str((OLD/row['preview']).relative_to(R)),
      'fresh_mesh_matches_source_ab':True} for row in inventory]
    (OUT/'inventory-current.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    result = {'status':'PASS_SOURCE_BUILD; OWNER_ONLY_RUNTIME_NOT_RUN','ids':29,
      'native_samples':len(native),'accessories':accessory_rows,'current_armor_tab_rows':len(armor),
      'accepted_prerequisites':{str(n):status[n] for n in range(122,129)},
      'jar':str(jar),'jar_sha256':digest(jar),'source_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),
      'source_state':'DIRTY; no commit/push','launches':0,'runtime':'OWNER_ONLY_NOT_RUN',
      'polymc':'No installation in this task; installed60 receipt remains separate',
      'historical_scope_preserved':True,'preview_inputs_and_outputs_match':True}
    (OUT/'verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('PASS #129: 29 IDs / 232 fresh native meshes / 13 accessories preserved / current JAR; runtime NOT_RUN')

if __name__ == '__main__':
    main()
