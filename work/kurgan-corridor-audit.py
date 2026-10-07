import json
from pathlib import Path
for file in sorted(Path('build/reports/slavicmyths/kurgan').glob('*.json'))[:3]:
 p=json.loads(file.read_text());links=p['links'];rooms=p['rooms'];hits=[]
 for a,l in enumerate(links):
  for b,k in enumerate(links[:a]):
   common={l['a'],l['b']}&{k['a'],k['b']}
   # Detect merged walking envelopes, not harmless adjacent stone surfaces.
   for x,y,z in l['steps']:
    for xx,yy,zz in k['steps']:
     if abs(x-xx)<=((l['width']+k['width'])//2) and abs(z-zz)<=((l['width']+k['width'])//2) and max(y,yy)+1<=min(y+l['height'],yy+k['height']):
      if not any(r['id'] in common and r['bounds'][0]-6<=x<=r['bounds'][3]+6 and r['bounds'][2]-6<=z<=r['bounds'][5]+6 and r['bounds'][1]-1<=y<=r['bounds'][4] for r in rooms):hits.append((a,b,[x,y,z],[xx,yy,zz]));break
    else:continue
    break
 print(file.name,'unplanned merges',hits[:20])
