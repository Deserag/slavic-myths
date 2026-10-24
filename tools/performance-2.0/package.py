from pathlib import Path
import json,gzip,hashlib,re,subprocess,shutil
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-2.0'
def sanitize(s):
 for root in [str(R),str(R).replace('\\','/'),str(Path.home()),str(Path.home()).replace('\\','/')]:s=s.replace(root,'<WORKSPACE>' if root.startswith(str(R)) else '<USER_HOME>')
 s=re.sub(r'\b[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\b','<UUID>',s)
 return '\n'.join(v.rstrip() for v in re.sub(r'\bDev\b','AuditPlayer',s).splitlines()).rstrip()+'\n'
profiles=[]
for p in O.rglob('*.jfr'):profiles.append(dict(path=p.relative_to(O).as_posix(),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest(),publication='LOCAL_ONLY process/system/network metadata'))
(O/'profiles.json').write_bytes(json.dumps(profiles,indent=2).encode())
for p in O.rglob('frames.csv'):
 with p.open('rb') as src,p.with_suffix('.csv.gz').open('wb') as dst:
  with gzip.GzipFile(fileobj=dst,filename='',mode='wb',mtime=0) as z:shutil.copyfileobj(src,z)
for p in O.rglob('client.log'):
 with p.with_name('client.sanitized.log.gz').open('wb') as dst:
  with gzip.GzipFile(fileobj=dst,filename='',mode='wb',mtime=0) as z:z.write(sanitize(p.read_text(encoding='utf-8-sig',errors='replace')).encode())
for p in O.rglob('*'):
 if p.is_file() and p.name not in ['client.log','frames.csv','evidence-manifest.json'] and p.suffix in ['.md','.log','.txt','.tsv','.json','.csv']:p.write_bytes(sanitize(p.read_text(encoding='utf-8-sig')).encode())
manifest=[]
for p in O.rglob('*'):
 if not p.is_file() or p.name=='evidence-manifest.json' or subprocess.run(['git','check-ignore','--no-index','-q',str(p)],cwd=R).returncode==0:continue
 manifest.append(dict(path=p.relative_to(O).as_posix(),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
(O/'evidence-manifest.json').write_bytes(json.dumps(manifest,indent=2).encode())
print('Evidence',len(manifest),'local-only JFR',len(profiles))
