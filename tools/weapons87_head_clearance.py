"""Triangle/box source-space head candidates, fixed native idle pose (not gameplay)."""
import copy,json
import numpy as np
import weapons87_geometry as g
from weapons87_grip_blueprints import scene
def triangle_box(triangle,lo,hi):
 center=(lo+hi)/2;half=(hi-lo)/2;t=triangle-center;edges=[t[1]-t[0],t[2]-t[1],t[0]-t[2]];axes=[np.eye(3)[i] for i in range(3)]+[np.cross(edges[0],edges[1])]+[np.cross(e,a) for e in edges for a in np.eye(3)]
 for axis in axes:
  if np.linalg.norm(axis)<1e-9:continue
  projection=t@axis;radius=np.abs(axis)@half
  if projection.min()>=radius-1e-5 or projection.max()<=-radius+1e-5:return False
 return True
def main():
 O=g.O;samples={s['body']:s for s in json.loads((O/'player-native.json').read_text()) if s['pose']=='idle'};before=json.loads((O/'grip-68/matrix.json').read_text());after=json.loads((O/'grip-68-after/matrix.json').read_text());report=[]
 for old,new in zip(before,after):
  for stage,row in [('before',old),('after',new)]:
   d=g.model('slavicmyths:item/'+row['id'],'thirdperson_'+('righthand' if row['hand']=='RIGHT' else 'lefthand'));d=copy.deepcopy(d);d.setdefault('display',{})['thirdperson_'+('righthand' if row['hand']=='RIGHT' else 'lefthand')]=row['display'];s=scene(samples[row['body']],row['id'],row['hand'],d);hits=0
   for layer in s['layers'][1:]:
    for face in layer['faces']:
     v=np.array(face)[:,:3]
     for indices in [(0,1,2),(0,2,3)]:hits+=int(triangle_box(v[list(indices)],np.array([-4.,-8.,-4.]),np.array([4.,0.,4.])))
   report.append({'id':row['id'],'body':row['body'],'hand':row['hand'],'stage':stage,'head_triangle_candidates':hits,'limits':'Exact triangle vs native idle head box; transparent texels not masked; fixed source pose, not runtime clipping validation','custom_path':row['custom_path']})
 (O/'head-clearance.json').write_text(json.dumps(report,indent=2)+'\n');ids=sorted({r['id'] for r in report if r['stage']=='after' and r['head_triangle_candidates']});print('AFTER native idle head candidates:',ids)
if __name__=='__main__':main()
