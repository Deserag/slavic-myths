from pathlib import Path
import json,zipfile
z=zipfile.ZipFile('.tools/port-backups/slavicmyths-0.9.5-pre-0.9.6.zip');print('backup entries',len(z.namelist()),z.namelist()[:3]);changed=[]
for n in z.namelist():
 if n.startswith('src/main/resources/') and not n.endswith('/'):
  p=Path(n)
  if not p.exists() or p.read_bytes()!=z.read(n):changed.append(n)
print('changed existing resources',len(changed));print('\n'.join(changed))
