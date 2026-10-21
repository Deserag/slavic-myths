"""Visible coplanar front-plane intersections in actual idle native meshes (offline only)."""
from pathlib import Path
import json,itertools
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor88-full'
def intersections(sample):
 faces=[]
 for f in sample['layers'][1]['faces']:
  if max(v[2] for v in f)-min(v[2] for v in f)>1e-5 or f[0][2]>=0:continue
  x0=min(v[0] for v in f);x1=max(v[0] for v in f);y0=min(v[1] for v in f);y1=max(v[1] for v in f)
  if x1-x0>1e-5 and y1-y0>1e-5:faces.append((x0,y0,x1,y1,f[0][2]))
 found=[]
 for i,j in itertools.combinations(range(len(faces)),2):
  a,b=faces[i],faces[j]
  if abs(a[4]-b[4])>1e-5:continue
  x0,y0,x1,y1=max(a[0],b[0]),max(a[1],b[1]),min(a[2],b[2]),min(a[3],b[3])
  if x1-x0<1e-5 or y1-y0<1e-5:continue
  x,y=(x0+x1)/2,(y0+y1)/2
  if any(c[0]+1e-5<x<c[2]-1e-5 and c[1]+1e-5<y<c[3]-1e-5 and c[4]<a[4]-1e-5 for c in faces):continue
  found.append(dict(face_indices=[i,j],intersection_xy=[x0,y0,x1,y1],z=a[4],area=(x1-x0)*(y1-y0)))
 return found
def main():
 before=json.loads((O/'before-native.json').read_text());after=json.loads((O/'after-native.json').read_text());rows=[]
 selected=['linen_ports','bast_shoes','indomitability_leggings','wind_boots','seven_league_boots']+[f+'_'+part for f in ['perunite','dragon','poloz'] for part in ['leggings','boots']]
 for id in selected:
  for body in ['Steve','Alex']:
   get=lambda data:next(s for s in data if s['id']==id and s['body']==body and s['pose']=='idle')
   old,new=intersections(get(before)),intersections(get(after));rows.append(dict(id=id,body=body,before=old,after=new))
 result=dict(scope='Actual native geometry/front planes at idle; source evidence, NOT runtime z-fighting test',rows=rows)
 (O/'coplanar-intersections.json').write_text(json.dumps(result,indent=2)+'\n')
 for row in rows:print(row['id'],row['body'],len(row['before']),'->',len(row['after']))
if __name__=='__main__':main()
