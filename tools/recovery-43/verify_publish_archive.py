"""Read-only verification of the retired independent publish clone; preserve any delta."""
import hashlib,json,zipfile
from pathlib import Path
B=Path('C:/Users/pavel/.codex/recovery/slavic-myths-20261018')
R=Path('D:/slavic-myths/.tools/publish-1.9.4')
def sha(data):return hashlib.sha256(data).hexdigest()
archive=B/'historical-publish-1.9.4.zip'
assert sha(archive.read_bytes())=='11d80d896712ab52a6df5bd5ea40421f52c005d1d29e7564002f7d8b3ee62d7b'
rows=[];delta=[]
with zipfile.ZipFile(archive) as z, zipfile.ZipFile(B/'publish-final-delta.zip','w',zipfile.ZIP_DEFLATED,compresslevel=3) as d:
    entries={n.replace('\\','/'):n for n in z.namelist()}
    for p in R.rglob('*'):
        if not p.is_file():continue
        if p.is_symlink():raise RuntimeError('Symlink: '+str(p))
        rel=p.relative_to(R).as_posix(); data=p.read_bytes();digest=sha(data)
        matched=rel in entries and sha(z.read(entries[rel]))==digest
        if not matched:
            d.writestr(rel,data);assert sha(d.read(rel))==digest;delta.append(rel)
        rows.append({'path':rel,'bytes':len(data),'sha256':digest,'archive':'historical-publish-1.9.4.zip' if matched else 'publish-final-delta.zip'})
(B/'PUBLISH_FINAL_VERIFIED.json').write_text(json.dumps({'root':str(R),'files':rows,'delta':delta,'bytes':sum(x['bytes'] for x in rows)},indent=2),'utf8')
print('VERIFIED',len(rows),'files',sum(x['bytes'] for x in rows),'bytes; delta',len(delta))
