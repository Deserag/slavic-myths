"""Ensure full generators still reproduce untouched transformation/bird/prop assets."""
from pathlib import Path
import hashlib,subprocess,sys
R=Path(__file__).resolve().parents[1]
files=list((R/'src/main/resources').rglob('*'))+list((R/'src/main/java/org/slavicmyths/client').glob('*Mesh.java'))
before={p:p.read_bytes() for p in files if p.is_file()}
commands=[
 "import marinka_assets as m; m.bull(); m.bird(); m.focus()",
 "from pathlib import Path; scope={'__file__':str(Path('tools/finist_assets.py').resolve())}; exec(Path('tools/finist_assets.py').read_text(encoding='utf8').split('humanoid();humanoid(True);falcon();props();props(True)')[0],scope); scope['falcon'](); scope['props'](); scope['props'](True)",
]
for command in commands:subprocess.run([sys.executable,'-c',"import sys; sys.path.insert(0,'tools'); "+command],cwd=R,check=True)
changed=[str(p.relative_to(R)) for p,data in before.items() if p.read_bytes()!=data]
if changed:
 for p,data in before.items():
  if p.read_bytes()!=data:p.write_bytes(data)
 raise AssertionError('Unchanged asset generator drift (restored originals): '+str(changed))
print('UNTOUCHED GENERATOR PASS drift=0 files='+str(len(before)))
