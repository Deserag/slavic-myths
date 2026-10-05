"""Stage the exact optional compile API from the existing pack lock; never install/launch mods."""
from pathlib import Path
import hashlib,json,urllib.request
ROOT=Path(__file__).resolve().parents[1]
pack=json.loads((ROOT/'packaging/test-pack-lock.json').read_text(encoding='utf-8'))
mod=next(m for m in pack['mods'] if m['id']=='xaerominimap')
if mod['version']!='26.5.0': raise SystemExit('Adapter targets Minimap 26.5.0; review API before changing pin')
target=ROOT/'.tools/test-pack-0.9.4'/mod['filename']
if not target.is_file():
    url=mod['downloadUrl']
    if not url.startswith('https://mediafilez.forgecdn.net/'): raise SystemExit('Unexpected artifact source')
    request=urllib.request.Request(url,headers={'User-Agent':'SlavicMyths-build-api/0.9.5'})
    with urllib.request.urlopen(request,timeout=60) as response: data=response.read()
    if hashlib.sha256(data).hexdigest()!=mod['sha256']: raise SystemExit('Artifact SHA256 mismatch')
    target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
if hashlib.sha256(target.read_bytes()).hexdigest()!=mod['sha256']: raise SystemExit('Staged API SHA256 mismatch')
print('PASS: exact optional Xaero compile API staged; no client launch')
