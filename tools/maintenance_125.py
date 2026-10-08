"""Read-only, repeatable maintenance inventory. No resource or world deletion."""
from pathlib import Path
from collections import defaultdict
import argparse, hashlib, json, os, subprocess, zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/maintenance'

def git(*args):
    p = subprocess.run(['git', *args], cwd=ROOT, capture_output=True, encoding='utf-8', errors='replace')
    return p.stdout + p.stderr

def inventory():
    files, dirs = [], defaultdict(int)
    for base, children, names in os.walk(ROOT, followlinks=False):
        children[:] = [n for n in children if not (Path(base)/n).is_symlink()]
        for name in names:
            p = Path(base)/name
            if p.is_symlink():
                continue
            size = p.stat().st_size
            rel = p.relative_to(ROOT)
            files.append((rel.as_posix(), size))
            for parent in rel.parents:
                dirs[parent.as_posix()] += size
    return files, dirs

def disk(phase):
    files, dirs = inventory()
    data = {'total_bytes': sum(s for _, s in files), 'directories': dict(dirs),
            'largest_files': sorted(files, key=lambda x:x[1], reverse=True)[:50]}
    (OUT/f'disk-{phase.lower()}-1.2.5.json').write_text(json.dumps(data, indent=2), encoding='utf-8')
    lines = [f'# Disk audit {phase} — 1.2.5', '',
             'Logical file bytes; junctions/symlinks are not traversed. External Gradle user cache excluded.', '',
             f'Total: {data["total_bytes"]:,} bytes ({data["total_bytes"]/1e9:.3f} GB).', '',
             '## 20 largest directories', '', '| Directory | Bytes |', '|---|---:|']
    lines += [f'| `{p}` | {s:,} |' for p,s in sorted(dirs.items(),key=lambda x:x[1],reverse=True) if p!='.'][:20]
    lines += ['', '## Categories', '', '| Directory | Bytes |', '|---|---:|']
    names = ['.git','.gradle','build','run','run/logs','run/crash-reports','run/saves','run/screenshots','run/mods','generated','src/generated/resources','.idea','.vscode','.cache','out','src/main/java','src/main/resources','docs','art','.tools','work','future','release']
    lines += [f'| `{p}` | {dirs.get(p,0):,} |' for p in names]
    lines += ['', '## Runtime directories', '', '| Directory | Bytes |', '|---|---:|']
    lines += [f'| `{p}` | {s:,} |' for p,s in sorted(dirs.items()) if '/' not in p and p.startswith('run')]
    lines += ['', '## 50 largest files', '', '| File | Bytes |', '|---|---:|']
    lines += [f'| `{p}` | {s:,} |' for p,s in data['largest_files']]
    lines += ['', '## Archives', '', '| File | Bytes |', '|---|---:|']
    lines += [f'| `{p}` | {s:,} |' for p,s in sorted(files,key=lambda x:x[1],reverse=True) if p.endswith(('.jar','.zip')) and not p.startswith(('.git/','build/'))]
    lines += ['', '## Git objects', '', '```text', git('count-objects','-vH'), '```']
    if phase=='AFTER':
        before=json.loads((OUT/'disk-before-1.2.5.json').read_text())
        delta=before['total_bytes']-data['total_bytes']
        lines += ['', f'Reduction: {delta:,} bytes ({delta/1e6:.2f} MB; {delta/before["total_bytes"]*100:.2f}%).',
                  f'Git before/after: {before["directories"].get(".git",0):,} / {dirs.get(".git",0):,} bytes.',
                  '', 'Worlds, screenshots, configs, source assets, toolchains and historical artifacts retained. See CLEANUP_1.2.5.md for exact deletions.']
    (OUT/f'DISK_AUDIT_{phase}_1.2.5.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    print(json.dumps({'phase':phase,'total_bytes':data['total_bytes'],'top_level':{p:s for p,s in dirs.items() if '/' not in p and p!='.'}},ensure_ascii=False))

def assets():
    groups=defaultdict(list)
    resources=ROOT/'src/main/resources'
    for p in resources.rglob('*'):
        if p.is_file():
            groups[hashlib.sha256(p.read_bytes()).hexdigest()].append(p)
    duplicate=[(h,ps) for h,ps in groups.items() if len(ps)>1]
    lines=['# Asset duplicates — 1.2.5','','SHA-256 byte identity across all runtime assets/data. Identity alone does not prove an ID can be removed.',
           '',f'{len(duplicate)} duplicate groups; potential raw duplicate bytes: {sum(ps[0].stat().st_size*(len(ps)-1) for _,ps in duplicate):,}.',
           '', 'No registry/resource IDs removed. Climate variants, family aliases and NBT references require preserving their paths.',
           'Existing production verifiers traverse model/blockstate texture links, catalogs, NBT palettes, pools, processors, recipes, loot and tags. This inventory does not prove any file orphaned; unresolved candidates are retained.', '']
    for h,ps in duplicate:
        lines += [f'## {h}', '',f'{ps[0].stat().st_size} bytes per file.', '']+[f'- `{p.relative_to(resources).as_posix()}`' for p in ps]+['']
    source_art=[p.relative_to(resources).as_posix() for p in resources.rglob('*') if p.suffix.lower() in {'.psd','.xcf','.kra','.pxo','.blend','.wav','.mp4','.zip','.jar'}]
    lines+=['## Runtime source-art/archive candidates','',json.dumps(source_art), '', 'Audio remains lossless/unchanged; no recompression or resolution reduction.']
    (OUT/'ASSET_DUPLICATES_1.2.5.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    print(f'Asset duplicate groups: {len(duplicate)}; source-art candidates: {len(source_art)}')

def jar_audit(path):
    with zipfile.ZipFile(path) as z:
        entries=z.infolist()
        forbidden=[e.filename for e in entries if e.filename.endswith(('.zip','.jar','.psd','.xcf','.kra','.blend','.log','.mp4')) or any(s in e.filename for s in ['/smoke/','/verify/','GameTests','screenshots/','saves/'])]
        names=[e.filename for e in entries]
        assert len(names)==len(set(names)), 'Duplicate ZIP entries'
        assert not forbidden, forbidden
        assert 'Implementation-Version: 1.2.5' in z.read('META-INF/MANIFEST.MF').decode()
        metadata=z.read('META-INF/neoforge.mods.toml').decode()
        assert 'version="1.2.5"' in metadata and 'modId="slavicmyths"' in metadata
        lines=['# Production JAR audit — 1.2.5','',f'Path: `{path}`',f'Size: {path.stat().st_size:,} bytes.',f'SHA-256: `{hashlib.sha256(path.read_bytes()).hexdigest()}`',f'Entries: {len(entries)}; duplicate entry names: 0; forbidden development/source-art entries: 0.',
               '', '## Largest entries', '', '| Entry | Raw bytes | ZIP bytes |','|---|---:|---:|']
        lines += [f'| `{e.filename}` | {e.file_size:,} | {e.compress_size:,} |' for e in sorted(entries,key=lambda e:e.file_size,reverse=True)[:30]]
        (OUT/'JAR_CONTENT_AUDIT_1.2.5.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    print(f'Production JAR audit PASS: {path.stat().st_size} bytes')

def backup_check():
    before=json.loads((OUT/'disk-before-1.2.5.json').read_text())
    moves=json.loads((OUT/'runtime-backup-1.2.5.json').read_text(encoding='utf-8-sig'))
    rows=[]
    for move in moves:
        dest=Path(move['destination'])
        actual=sum(p.stat().st_size for p in dest.rglob('*') if p.is_file())
        expected=before['directories'][dest.name]
        assert actual==expected,(dest,actual,expected)
        assert not Path(move['source']).exists(),move['source']
        rows.append({'directory':dest.name,'expected_bytes':expected,'backup_bytes':actual,'pass':True})
    (OUT/'backup-verification-1.2.5.json').write_text(json.dumps(rows,indent=2)+'\n',encoding='utf-8')
    print(f'Backup verification PASS: {len(rows)} directories, {sum(r["backup_bytes"] for r in rows)} bytes retained')

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--phase',choices=['BEFORE','AFTER']);parser.add_argument('--assets',action='store_true');parser.add_argument('--backup-check',action='store_true');parser.add_argument('--jar',type=Path);a=parser.parse_args()
    OUT.mkdir(parents=True,exist_ok=True)
    if a.phase:disk(a.phase)
    if a.assets:assets()
    if a.jar:jar_audit(a.jar)
    if a.backup_check:backup_check()
