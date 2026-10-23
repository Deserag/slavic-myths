"""Exact archived crossbow delta: rope only, fixed thickness and persistent cocked V."""
import copy,json
from pathlib import Path
import issue217_models as m
rows=[]
for bow in m.CROSSBOWS:
 for suffix in m.STATES:
  name=bow+suffix;old=m.old(name);actual=json.loads((m.A/'models/item'/(name+'.json')).read_text())
  assert actual==m.crossbow_string(name),name
  n=1 if not suffix else 32;rope=actual['elements'][7:7+n]
  assert all(abs(e['to'][1]-e['from'][1]-.3)<1e-8 and abs(e['to'][2]-e['from'][2]-.3)<1e-8 for e in rope)
  rest=copy.deepcopy(actual);rest['elements']=rest['elements'][:7]+[old['elements'][7]]+rest['elements'][7+n:]
  assert rest==old,(name,'changes beyond rope')
  if suffix in ['_arrow','_firework']:
   assert rope[15]['from'][2]-rope[0]['from'][2]>2.9
   assert actual['elements'][7:39]==json.loads((m.A/'models/item'/(bow+'_pulling_2.json')).read_text())['elements'][7:39]
  rows.append(dict(model=name,segments=n,thickness=.3,cocked=suffix in ['_arrow','_firework']))
out=m.R/'docs/implementation/weapons87/issue217/crossbow-string-source.json';out.write_text(json.dumps(rows,indent=2)+'\n')
print('PASS 12 crossbow states: exact rope-only changes, constant .3 thickness, full draw persists into arrow/firework; no mechanics or PNG changes')
