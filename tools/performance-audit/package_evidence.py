from pathlib import Path
import gzip,shutil,json,hashlib,re,subprocess
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-audit'
def clean(s):
 for a,b in [(str(R),'<AUDIT_ROOT>'),(str(R).replace('\\','/'),'<AUDIT_ROOT>'),('C:\\Users\\pavel','<USER_HOME>'),('C:/Users/pavel','<USER_HOME>')]:s=s.replace(a,b)
 s=re.sub(r'\b[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\b','<UUID>',s)
 return '\n'.join(line.rstrip() for line in re.sub(r'\bDev\b','AuditPlayer',s).splitlines())+'\n'
for raw in [O/'raw',O/'extended/raw',O/'extended2/raw',O/'focused/raw']:
 if not raw.exists():continue
 if (raw/'client.log').exists():(raw/'client.sanitized.log').write_text(clean((raw/'client.log').read_text(encoding='utf-8-sig',errors='replace')),encoding='utf-8',newline='\n')
 p=raw/'frames.csv'
 if p.exists():
  with p.open('rb') as src,(raw/'frames.csv.gz').open('wb') as dst:
   with gzip.GzipFile(filename='',mode='wb',fileobj=dst,mtime=0) as compressed:shutil.copyfileobj(src,compressed)
for p in O.glob('*.log'):p.write_text(clean(p.read_text(encoding='utf-8-sig',errors='replace')),encoding='utf-8',newline='\n')
for p in O.glob('*.svg'):p.write_text('\n'.join(x.rstrip() for x in p.read_text(encoding='utf-8').splitlines())+'\n',encoding='utf-8',newline='\n')
entries=[]
for p in O.rglob('*'):
 if p.is_file() and p.suffix not in ['.jfr'] and p.name not in ['client.log','jfr-export.json','frames.csv','evidence-manifest.json'] and subprocess.run(['git','check-ignore','--no-index','-q',str(p)],cwd=R).returncode!=0:entries.append(dict(path=p.relative_to(O).as_posix(),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
(O/'evidence-manifest.json').write_text(json.dumps(entries,indent=2),encoding='utf-8',newline='\n')
