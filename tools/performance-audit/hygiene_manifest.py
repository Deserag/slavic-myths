from pathlib import Path
import subprocess,json,collections,hashlib
R=Path.cwd();O=R/'docs/implementation/performance-audit';groups=collections.defaultdict(list);manifest=[]
for line in subprocess.check_output(['git','ls-tree','-rl','HEAD'],text=True).splitlines():
 meta,name=line.split('\t');parts=meta.split();size=int(parts[-1]);blob=parts[2];groups[(blob,size)].append(name)
 if name.startswith('release/') or name.startswith('work/') and size>=1024*1024:
  manifest.append(dict(path=name,bytes=size,git_blob=blob,sha256=hashlib.sha256((R/name).read_bytes()).hexdigest(),decision='KEEP; REVIEW archive/release-asset candidate only',restore='git show fb2994e958911169317523e1066b8a1ca7a155af:'+name,external_archive_verified=False,references_audited=False,source_commit_for_binary='NOT_ESTABLISHED',delete_authorized=False))
dups=[dict(blob=b,bytes_per_copy=size,copies=len(paths),paths=paths,logical_duplicate_bytes=size*(len(paths)-1)) for (b,size),paths in groups.items() if len(paths)>1 and size>=1024*1024]
(O/'hygiene-manifest.json').write_text(json.dumps(dict(warning='Logical repeated bytes are not Git pack savings or runtime memory. No files removed.',manifest=manifest,large_identical_blobs=sorted(dups,key=lambda x:x['logical_duplicate_bytes'],reverse=True)),indent=2),encoding='utf-8');print('manifest',len(manifest),'large duplicate groups',len(dups),'logical repeated bytes',sum(x['logical_duplicate_bytes'] for x in dups))
