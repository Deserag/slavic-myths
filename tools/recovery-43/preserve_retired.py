"""Archive unique ignored data and dirty source before ordinary worktree removal.

No deletion, no game launch, no reset. Run only for the reviewed issue-43 inventory.
"""
import hashlib, json, subprocess, zipfile
from pathlib import Path

BACKUP = Path('C:/Users/pavel/.codex/recovery/slavic-myths-20261018')
KEEP = {'d:/slavic-myths', 'd:/slavic-myths-advancements', 'd:/slavic-myths-advancements-v3'}

def git(root, *args):
    return subprocess.check_output(['git', '-C', str(root), *args]).decode('utf-8').strip()

def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as f:
        for b in iter(lambda: f.read(1024*1024), b''): h.update(b)
    return h.hexdigest()

def main():
    rows = json.loads((BACKUP/'WORKTREES.json').read_text('utf-8-sig'))
    with zipfile.ZipFile(BACKUP/'rebuildable-output-backup.zip') as z:
        cached = {x['sha256']: {'archive':'rebuildable-output-backup.zip','entry':x['entry']}
                  for x in json.loads(z.read('_files.json'))}
    receipts = []
    archive = BACKUP/'retired-unique-data-v2.zip'
    if archive.exists(): raise SystemExit('Archive already exists; inspect receipt instead of overwriting')
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED, compresslevel=3) as z:
        for i, row in enumerate(rows):
            root = Path(row['worktree'])
            if root.as_posix().lower() in KEEP: continue
            if not root.exists(): continue
            print('PRESERVING', root, flush=True)
            tracked = set(git(root, 'ls-files', '-z').split('\0'))
            files = []
            for p in root.rglob('*'):
                if not p.is_file() or p.relative_to(root).as_posix() == '.git': continue
                rel = p.relative_to(root).as_posix()
                if rel in tracked: continue
                if p.is_symlink(): raise RuntimeError('Symlink needs review: '+str(p))
                sha = digest(p)
                saved = cached.get(sha)
                if saved is None:
                    entry = 'objects/'+sha
                    z.writestr(entry, p.read_bytes())
                    if hashlib.sha256(z.read(entry)).hexdigest() != sha: raise RuntimeError('Archive mismatch')
                    saved = {'archive':archive.name, 'entry':entry}
                    cached[sha] = saved
                files.append({'path':rel,'bytes':p.stat().st_size,'sha256':sha,**saved})
            dirty = git(root, 'status', '--porcelain')
            branch = None
            if dirty:
                branch = f'archive/local-{i:02d}-20261018'
                git(root, 'switch', '-c', branch)
                git(root, 'add', '-A')
                if git(root,'diff','--cached','--name-only'):
                    git(root, 'commit', '-m', 'archive: preserve local work before issue-43 consolidation')
                if git(root,'status','--porcelain'): raise RuntimeError('Still dirty: '+str(root))
            receipts.append({'root':str(root),'head':git(root,'rev-parse','HEAD'),
                             'archive_branch':branch,'untracked_and_ignored':files})
            (BACKUP/'RETIRED_DATA.json').write_text(json.dumps(receipts,ensure_ascii=False,indent=2),'utf-8')
    with zipfile.ZipFile(archive) as z:
        if z.testzip() is not None: raise RuntimeError('Archive CRC mismatch')
    (BACKUP/'RETIRED_DATA_SHA256.txt').write_text(digest(archive)+'  '+archive.name+'\n','utf-8')
    print('VERIFIED', archive, archive.stat().st_size, 'bytes', len(receipts), 'worktrees', flush=True)

if __name__ == '__main__': main()
