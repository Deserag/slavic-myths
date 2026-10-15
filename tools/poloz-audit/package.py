"""Publish sanitised text/compressed frame evidence; retain original logs/JFR locally."""
from pathlib import Path
import json,gzip,hashlib,re,subprocess,sys
R=Path(__file__).resolve().parents[2];O=R/sys.argv[1]
def clean(text):
 for a,b in [(str(R),'<ROOT>'),(str(R).replace('\\','/'),'<ROOT>'),(str(Path.home()),'<USER_HOME>'),(str(Path.home()).replace('\\','/'),'<USER_HOME>')]:text=text.replace(a,b)
 text=re.sub(r'\b[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\b','<UUID>',text)
 return '\n'.join(x.rstrip() for x in re.sub(r'\bDev\b','AuditPlayer',text).splitlines())+'\n'
profiles=[]
for p in O.rglob('*.jfr'):profiles.append(dict(path=p.relative_to(O).as_posix(),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest(),publication='LOCAL_ONLY system/process metadata'))
(O/'profiles.json').write_bytes(json.dumps(profiles,indent=2).encode())
for p in O.rglob('frames.csv'):
 with p.open('rb') as src,p.with_suffix('.csv.gz').open('wb') as dst:
  with gzip.GzipFile(fileobj=dst,filename='',mode='wb',mtime=0) as z:
   import shutil;shutil.copyfileobj(src,z)
for p in O.rglob('client.log'):
 with p.with_name('client.sanitized.log.gz').open('wb') as dst:
  with gzip.GzipFile(fileobj=dst,filename='',mode='wb',mtime=0) as z:z.write(clean(p.read_text(encoding='utf-8-sig',errors='replace')).encode())
for p in O.glob('*.log'):p.write_bytes(clean(p.read_text(encoding='utf-8-sig',errors='replace')).encode())
manifest=[]
for p in O.rglob('*'):
 if not p.is_file() or p.name=='evidence-manifest.json' or subprocess.run(['git','check-ignore','--no-index','-q',str(p)],cwd=R).returncode==0:continue
 manifest.append(dict(path=p.relative_to(O).as_posix(),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
(O/'evidence-manifest.json').write_bytes(json.dumps(manifest,indent=2).encode())
print('Evidence files',len(manifest),'local profiles',len(profiles))
