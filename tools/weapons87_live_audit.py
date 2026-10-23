"""#157/#192: audit all current 68 registered weapon IDs against frozen per-ID source evidence.

Read-only for production files. The archived #157 and flail manifests are *snapshots*;
report drift rather than rewriting them or claiming the old gate passed.
"""
from __future__ import annotations
from pathlib import Path
import argparse, csv, hashlib, json, zipfile

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
DOCS=ROOT/'docs/implementation/weapons87'
ART=RES/'assets/slavicmyths'

def digest(p: Path)->str:
    return hashlib.sha256(p.read_bytes()).hexdigest()

def model_paths(id: str):
    parent=ART/'models/item'
    paths=[parent/(id+'.json')]
    for suffix in ('_held','_icon','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework'):
        path=parent/(id+suffix+'.json')
        if path.exists():paths.append(path)
    return paths

def run(output: Path, check_jar: bool=False, native_path: Path|None=None):
    catalog={}
    for raw in (ART/'creative_catalog.tsv').read_text(encoding='utf-8').splitlines():
        if raw and not raw.startswith('#'):
            fields=raw.split('\t')
            assert len(fields)==3,raw
            if fields[1]=='weapons':
                assert fields[0] not in catalog,fields[0]
                catalog[fields[0]]=fields[2]
    old_rows=json.loads((DOCS/'audit-68.json').read_text(encoding='utf-8'))
    native_path=native_path or DOCS/'native-attributes-after.json'
    attributes=json.loads(native_path.read_text(encoding='utf-8'))
    archived={r['id']:r for r in old_rows}
    native={r['id']:r for r in attributes}
    assert len(catalog)==len(archived)==len(native)==68
    assert set(catalog)==set(archived)==set(native)
    jar_files=[]
    if check_jar:
        jar_files=list((ROOT/'build/libs').glob('slavicmyths-*.jar'))
        assert len(jar_files)==1,jar_files
    baseline_drift=[]
    current_rows=[]
    missing=[]
    for id,family in catalog.items():
        snapshot=archived[id]
        by_path={r['path']:r['sha256'] for group in ('models','textures') for r in snapshot.get(group,[])}
        model_files=model_paths(id)
        assets={}
        for path in model_files:
            assert path.is_file(),f'Missing active model for {id}: {path}'
            data=json.loads(path.read_text(encoding='utf-8'))
            relative=path.relative_to(ROOT).as_posix()
            assets[relative]=digest(path)
            for v in data.get('textures',{}).values():
                if v.startswith('slavicmyths:'):
                    tex=ART/'textures'/(v.split(':',1)[1]+'.png')
                    assert tex.is_file(),f'Unresolved texture {id}: {v}'
                    assets[tex.relative_to(ROOT).as_posix()]=digest(tex)
            for obj in data.get('overrides',[]):
                ref=obj.get('model','')
                if ref.startswith('slavicmyths:'):
                    target=ART/'models'/(ref.split(':',1)[1]+'.json')
                    assert target.is_file(),f'Unresolved predicate {id}: {target}'
        for path,old_digest in by_path.items():
            file=ROOT/path
            if not file.is_file():
                missing.append({'id':id,'path':path,'kind':'archived_source_no_longer_exists'})
            elif digest(file)!=old_digest:
                baseline_drift.append({'id':id,'path':path,'snapshot_sha256':old_digest,'live_sha256':digest(file)})
        attr=native[id]
        assert attr['id']==id and attr['max_damage']>=0
        assert attr['attack_speed_total']>0 and attr['reach_total']>0
        assert attr['class']
        current_rows.append({
            'id':id,'ru':snapshot.get('ru'),'family':family,
            'item_class':attr['class'],
            'attack_damage':attr['attack_damage_total'],
            'attack_speed':attr['attack_speed_total'],
            'reach':attr['reach_total'],
            'durability':attr['max_damage'],
            'native_analytical_dps_ceiling':attr['unarmored_full_charge_melee_dps_ceiling'],
            'actual_acquisition_note':snapshot.get('acquisition'),
            'current_recipe_outputs':[],
            'acquisition_status':'SOURCE_ONLY_NOT_TIMED',
            'effect_source':snapshot.get('effects_sources'),
            'decision_from_prior_audit':snapshot.get('decision'),
            'models':sorted(p.relative_to(ROOT).as_posix() for p in model_files),
            'active_asset_sha256':assets,
            'runtime':'NOT_RUN / OWNER_PENDING'
        })
    flail_snapshot=json.loads((DOCS/'blueprints/flails/applied-manifest.json').read_text(encoding='utf-8'))
    for recipe in (RES/'data/slavicmyths/recipe').glob('*.json'):
        data=json.loads(recipe.read_text(encoding='utf-8'));result=data.get('result',{})
        result_id=result.get('id',result.get('item')) if isinstance(result,dict) else result
        for row in current_rows:
            if result_id=='slavicmyths:'+row['id']:
                row['current_recipe_outputs'].append({'path':recipe.relative_to(ROOT).as_posix(),'sha256':digest(recipe),'recipe':data})
    flail_changed=[]
    flail_missing=[]
    for rel,recorded in flail_snapshot.items():
        live=ART/rel
        if not live.exists():
            flail_missing.append(rel)
        elif digest(live)!=recorded:
            flail_changed.append({'path':rel,'old_sha256':recorded,'live_sha256':digest(live)})
    report={
        'source':'CURRENT_RESOURCES_AND_PINNED_NATIVE_SNAPSHOT',
        'baseline':'old audit-68.json + native-attributes-after.json (source snapshot, NOT gameplay results)',
        'registry_ids':len(catalog),'current_rows':current_rows,
        'archived_sha256_drift':baseline_drift,
        'archived_deleted_references':missing,
        'flail_historical_manifest':{'count':len(flail_snapshot),'drift':flail_changed,'missing':flail_missing},
        'legacy_general_verifier':'NOT_CLAIMED_PASS: old baseline drift must be reconciled by owner review, not mutated',
        'native_snapshot_path':native_path.relative_to(ROOT).as_posix(),
        'native_snapshot_sha256':digest(native_path),
        'owner_approved_CD':'See continuation53/OWNER_APPROVAL_CD.json; use current native snapshot to assess implementation',
        'runtime':'NOT_RUN','polymc':'NOT_INSTALLED'
    }
    if jar_files:
        with zipfile.ZipFile(jar_files[0]) as archive:
            for row in current_rows:
                for asset,hashval in row['active_asset_sha256'].items():
                    if asset.startswith('src/main/resources/assets/slavicmyths/'):
                        key=asset.removeprefix('src/main/resources/')
                        assert hashlib.sha256(archive.read(key)).hexdigest()==hashval,('not in JAR',key)
        report['jar_packaged_assets_checked']=sum(len(row['active_asset_sha256']) for row in current_rows)
    output.parent.mkdir(parents=True,exist_ok=True)
    output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    csv_path=output.with_suffix('.csv')
    with csv_path.open('w',newline='',encoding='utf-8') as f:
        fields=('id','ru','family','item_class','attack_damage','attack_speed','reach','durability','native_analytical_dps_ceiling','acquisition_status','runtime')
        writer=csv.DictWriter(f,fieldnames=fields,extrasaction='ignore')
        writer.writeheader()
        writer.writerows(current_rows)
    print(f"PASS: {len(catalog)} exact catalog weapon IDs, native snapshot, source model/PNG refs"+(f", {report['jar_packaged_assets_checked']} JAR asset checks" if jar_files else ""))
    print(f"ARCHIVAL DRIFT (historical, NOT FIXED): audit68 {len(baseline_drift)} changed + {len(missing)} missing references, old flail manifest {len(flail_changed)} changed + {len(flail_missing)} missing")
    print('REPORT:',output,'CSV:',csv_path,'game/PolyMC NOT_RUN')
    return report

if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--output',default='build/weapons87-live-audit/current-68.json')
    p.add_argument('--jar',action='store_true')
    p.add_argument('--native-snapshot')
    args=p.parse_args()
    run(ROOT/args.output,args.jar,ROOT/args.native_snapshot if args.native_snapshot else None)
