"""Preserve reviewed historical artifacts, then remove them from the current index only."""
import ast, hashlib, json, re, subprocess, zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
BACKUP=Path('C:/Users/pavel/.codex/recovery/slavic-myths-20261018')
def sha(data): return hashlib.sha256(data).hexdigest()

def main():
    tracked=subprocess.check_output(['git','ls-files','-z'],cwd=ROOT).decode('utf8').split('\0')
    paths=[p for p in tracked if p and ((p.startswith('release/') and p.endswith('.jar'))
            or p.endswith('.log') or p.startswith('work/'))]
    historical=[]
    for p in (ROOT/'tools').glob('*'):
        if p.suffix not in ('.py','.ps1'): continue
        text=p.read_text('utf-8-sig')
        if re.search(r'release[/\\][^\n\r]*\.jar',text) and p.name.startswith(('install_','verify_')):
            historical.append(p.relative_to(ROOT).as_posix())
    archive=BACKUP/'historical-tracked-artifacts.zip'
    if archive.exists(): raise SystemExit('Archive exists: do not overwrite')
    rows=[]
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=3) as z:
        for rel in paths+historical:
            data=(ROOT/rel).read_bytes(); z.writestr(rel,data)
            rows.append({'path':rel,'bytes':len(data),'sha256':sha(data),
                         'category':'jar' if rel.startswith('release/') else 'scratch' if rel.startswith('work/') else 'log' if rel.endswith('.log') else 'historical_tool'})
        z.writestr('MANIFEST.json',json.dumps(rows,ensure_ascii=False,indent=2))
    with zipfile.ZipFile(archive) as z:
        for row in rows:
            assert sha(z.read(row['path']))==row['sha256'],row['path']
    manifest={'archive':archive.name,'archive_sha256':sha(archive.read_bytes()),'files':rows,
              'source_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT).decode().strip(),
              'removed_from_index':len(paths),'local_files':'kept; unique source/audio/art remain in verified archive and Git history'}
    (ROOT/'docs/maintenance/issue-43/TRACKED_ARTIFACTS.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n','utf8')
    for rel in historical:
        p=ROOT/rel; text=p.read_text('utf-8-sig')
        message='ARCHIVED historical tool. See docs/maintenance/issue-43/HISTORICAL_ARTIFACTS.md; restore its original revision and verified inputs in a separate historical checkout. Current source is not its baseline.'
        if p.suffix=='.py':
            tree=ast.parse(text); at=0
            if tree.body and isinstance(tree.body[0],ast.Expr) and isinstance(tree.body[0].value,ast.Constant) and isinstance(tree.body[0].value.value,str): at=tree.body[0].end_lineno
            while at<len(text.splitlines()) and text.splitlines()[at].startswith('from __future__'): at+=1
            lines=text.splitlines(keepends=True); lines.insert(at,'\nif __name__ == "__main__":\n    raise SystemExit('+repr(message)+')\n\n');text=''.join(lines)
            ast.parse(text)
        else: text="throw '"+message+"'\n\n"+text
        p.write_text(text,'utf8')
    for i in range(0,len(paths),100):
        subprocess.run(['git','rm','--cached','--quiet','--',*paths[i:i+100]],cwd=ROOT,check=True)
    print(json.dumps({'removed_from_index':len(paths),'categories':{c:sum(r['category']==c for r in rows) for c in ('jar','log','scratch','historical_tool')},'archive_bytes':archive.stat().st_size,'archive_sha256':manifest['archive_sha256']}))

if __name__=='__main__': main()
