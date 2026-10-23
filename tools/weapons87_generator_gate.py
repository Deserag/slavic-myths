"""#121: isolated canonical weapon-generator replay, never mutates actual source files.

The original historical manifests were captured *before* many approved patches and
are not valid immutable checksums for current production. Compare to fresh exact
source snapshots and record old drift rather than silently rewriting manifests.
"""
from __future__ import annotations
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess,sys,tempfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'src/main/resources'
DOC=ROOT/'docs/implementation/weapons87'
IDS=('serpent_crossbow','three_breath_crossbow')
STATES=('','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework')
PROTECTED=set()
for id in IDS:
    for suffix in STATES:
        PROTECTED.add('assets/slavicmyths/models/item/'+id+suffix+'.json')
for id in ('trophy_kalin_sabre','victor_spear','pool_spear'):
    PROTECTED.add('assets/slavicmyths/models/item/'+id+'.json')
PROTECTED.add('assets/slavicmyths/textures/item/kalin_sabre_87.png')
assert len(PROTECTED)==16
def sha(path:Path)->str:return hashlib.sha256(path.read_bytes()).hexdigest()
def snapshot(folder:Path)->dict[str,str]:
    return {p.relative_to(folder).as_posix():sha(p) for p in folder.rglob('*') if p.is_file()}
def check_assets(sandbox:Path)->int:
    asset=sandbox/'src/main/resources/assets/slavicmyths'
    weapon_lines=[line.split('\t') for line in (asset/'creative_catalog.tsv').read_text(encoding='utf-8').splitlines() if line and not line.startswith('#')]
    weapons=[n for n,group,family in weapon_lines if group=='weapons']
    assert len(weapons)==len(set(weapons))==68
    for name in weapons:
        path=asset/'models/item'/(name+'.json')
        assert path.is_file(),path
        obj=json.loads(path.read_text(encoding='utf-8'))
        for tex in obj.get('textures',{}).values():
            if tex.startswith('slavicmyths:'):
                expected=asset/'textures'/(tex.split(':',1)[1]+'.png')
                assert expected.is_file(),(name,tex)
    for name in IDS:
        for suffix in STATES:
            path=asset/'models/item'/(name+suffix+'.json')
            model=json.loads(path.read_text(encoding='utf-8'))
            for hand in ('thirdperson_righthand','thirdperson_lefthand'):
                transform=model['display'][hand]
                assert transform['rotation']==[0,0,0] and transform['translation']==[0,1.375,-2.475] and transform['scale']==[.55]*3,(name,suffix,hand)
    return len(weapons)
def equivalent(original:Path,generated:Path)->bool:
    if not original.is_file() or not generated.is_file():return False
    if original.suffix=='.json':
        try:return json.loads(original.read_text(encoding='utf-8'))==json.loads(generated.read_text(encoding='utf-8'))
        except (ValueError,UnicodeError):return False
    if original.suffix=='.png':
        with Image.open(original) as a,Image.open(generated) as b:
            return a.size==b.size and a.convert('RGBA').tobytes()==b.convert('RGBA').tobytes()
    return sha(original)==sha(generated)
def replay(workspace:Path,entrypoint:str):
    program=workspace/'tools'/entrypoint
    extra=['--apply'] if entrypoint=='weapons87_flails.py' else ['--weapon-only'] if entrypoint=='poloz_full_assets.py' else []
    p=subprocess.run([sys.executable,str(program),*extra],cwd=workspace,capture_output=True,text=True,timeout=170)
    return {'exit':p.returncode,'stdout':p.stdout[-4500:],'stderr':p.stderr[-4500:]}
def main(output:Path,entrypoint='weapons87_flails.py'):
    with tempfile.TemporaryDirectory(prefix='weapons87-canonical-',dir=None) as temp:
        workspace=Path(temp)
        shutil.copytree(SRC,workspace/'src/main/resources')
        # Replay consumes authored JSON drafts, not historical game captures,
        # frozen JARs or render archives (over 600 MB). Keep inputs explicit.
        for source in (DOC/'blueprints').rglob('*.json'):
            target=workspace/'docs/implementation/weapons87'/source.relative_to(DOC)
            target.parent.mkdir(parents=True,exist_ok=True)
            shutil.copy2(source,target)
        (workspace/'tools').mkdir(parents=True)
        for file in (ROOT/'tools').glob('*.py'):
            shutil.copy2(file,workspace/'tools'/file.name)
        shutil.copytree(ROOT/'tools/weapon-art-original',workspace/'tools/weapon-art-original')
        real_before=snapshot(SRC)
        snapshot_before=snapshot(workspace/'src/main/resources')
        assert snapshot_before==real_before
        first=replay(workspace,entrypoint)
        after_first=snapshot(workspace/'src/main/resources')
        diff=sorted(k for k in set(real_before)|set(after_first) if real_before.get(k)!=after_first.get(k))
        protected_mismatches=[k for k in sorted(PROTECTED) if not equivalent(SRC/k,workspace/'src/main/resources'/k)]
        semantic_drift=[k for k in diff if not equivalent(SRC/k,workspace/'src/main/resources'/k)]
        number=check_assets(workspace) if first['exit']==0 else 0
        second=replay(workspace,entrypoint) if first['exit']==0 else None
        after_second=snapshot(workspace/'src/main/resources')
        non_idempotent=sorted(k for k in set(after_first)|set(after_second) if after_first.get(k)!=after_second.get(k))
        actual_still_equal=snapshot(SRC)==real_before
        report={
            'scope':'#121 isolated current-source replay; full resource semantics and idempotence',
            'entrypoint':entrypoint,
            'source_head':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),
            'source_state':'working tree; exact resource hashes in baseline-resource-sha256.json',
            'source_mutated':not actual_still_equal,
            'catalogue_weapon_count':number,
            'protected_models_count':len(PROTECTED),
            'protected_mismatches':protected_mismatches,
            'protected_byte_drift':[k for k in sorted(PROTECTED) if after_first.get(k)!=real_before.get(k)],
            'semantic_or_png_pixel_drift_count':len(semantic_drift),'semantic_or_png_pixel_drift':semantic_drift,
            'first_pass':first,'second_pass':second,
            'changed_paths_count':len(diff),'changed_paths':diff,
            'idempotence_drift_count':len(non_idempotent),'idempotence_drift':non_idempotent,
            'historical_manifest_sha':'archived preproduction, not used for pass/fail of current production',
            'game':'NOT_RUN','polymc':'NOT_INSTALLED'
        }
    output.parent.mkdir(parents=True,exist_ok=True)
    (output.parent/'baseline-resource-sha256.json').write_text(json.dumps(real_before,indent=2)+'\n',encoding='utf-8')
    output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('P32 canonical generator source QA: 68 IDs',number,'protected',len(PROTECTED),'reverted',len(protected_mismatches),'all-source-byte-drift',len(diff),'actual-content-drift',len(semantic_drift),'idempotence',len(non_idempotent),'src_mutated',not actual_still_equal)
    print('DIAGNOSTIC current-source asset deltas:',diff[:40])
    print('REPORT',output,'Minecraft NOT_RUN')
    assert first['exit']==0,(first['stderr'],first['stdout'])
    assert second is not None and second['exit']==0,(second['stderr'] if second else 'missing second')
    assert number==68
    assert not protected_mismatches,protected_mismatches
    assert not semantic_drift,semantic_drift
    assert not non_idempotent,non_idempotent[:30]
    assert actual_still_equal
    print('PASS protected #115/#163/#184/#185/#189 asset geometry/material semantics and full canonical idempotence; changed JSON formatting/PNG encoding recorded separately; historical drift not normalized')
if __name__=='__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('--output',default='build/weapons87-generator-p32/report.json')
    parser.add_argument('--entrypoint',default='weapons87_flails.py',choices=['weapons87_flails.py','weapon_resources_1321.py','rare_weapon_resources_1322.py','weapon_patch_assets.py','weapon_feedback_assets.py','weapon_visual_repair_assets.py','poloz_full_assets.py'])
    arg=parser.parse_args()
    main(ROOT/arg.output,arg.entrypoint)
