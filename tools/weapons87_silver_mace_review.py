"""Actual silver-mace archive/current geometry, connectivity, native hand source A/B."""
import collections,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp
import armor88_preview as painter
from weapons87_grip_blueprints import scene
from weapons87_head_clearance import triangle_box
O=g.O

def components(elements):
 left=set(range(len(elements)));groups=[]
 while left:
  start=left.pop();group={start};todo=[start]
  while todo:
   a=elements[todo.pop()]
   for i in list(left):
    b=elements[i];overlap=np.minimum(a['to'],b['to'])-np.maximum(a['from'],b['from'])
    if np.all(overlap>=-1e-8) and np.count_nonzero(overlap>1e-8)>=2:left.remove(i);group.add(i);todo.append(i)
  groups.append(sorted(group))
 return groups

def main():
 archive=zipfile.ZipFile(O/'before-silver-mace.zip');before=json.loads(archive.read('src/main/resources/assets/slavicmyths/models/item/silver_mace.json'));after=g.model('slavicmyths:item/silver_mace');oldgroups=components(before['elements']);newgroups=components(after['elements']);assert len(oldgroups)==3 and len(newgroups)==1,'Connected head regression'
 assert {k:v for k,v in before['display'].items() if k!='gui'}=={k:v for k,v in after['display'].items() if k!='gui'},'Hand transforms changed'
 folder=O/'silver-mace-after';folder.mkdir(exist_ok=True);bodies=json.loads((O/'player-native.json').read_text());fprows=[x for x in json.loads((O/'fp-native.json').read_text()) if x['id']=='silver_mace' and x['after']];records=[];board=Image.new('RGB',(1600,1430),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'silver_mace / actual archive/current JSON+UV, native body/wrist matrices; source only',fill='white');painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'))
 for left in [False,True]:
  row=next(x for x in fprows if x['left']==left);matrix=np.array(row['matrix']).reshape((4,4),order='F')
  for is_after,model in [(False,before),(True,after)]:
   faces=[(r,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3],uv) for r,v,uv in g.faces(model)];image,pixels=fp.render(faces,400,225);col=int(left)*2+int(is_after);board.paste(image,(col*400,30));draw.text((col*400+3,35),'FP '+('L' if left else 'R')+' '+('AFTER' if is_after else 'BEFORE'),fill='white');records.append({'context':'FP','left':left,'after':is_after,'pixels':pixels})
 for index,(body,pose) in enumerate([('Steve','idle'),('Alex','idle'),('Steve','walk_t10'),('Alex','walk_t10')]):
  b=next(x for x in bodies if x['body']==body and x['pose']==pose)
  for left in [False,True]:
   for is_after,model in [(False,before),(True,after)]:
    s=scene(b,'silver_mace','LEFT' if left else 'RIGHT',model);hits=0
    if pose=='idle':
     for layer in s['layers'][1:]:
      for face in layer['faces']:
       v=np.array(face)[:,:3]
       for inds in [(0,1,2),(0,2,3)]:hits+=int(triangle_box(v[list(inds)],np.array([-4.,-8.,-4.]),np.array([4.,0.,4.])))
    col=int(left)*2+int(is_after);y=280+index*285
    for view,yaw in enumerate([0,90]):
     x=col*400+view*200;board.paste(painter.render(s,yaw),(x,y));draw.text((x+3,y+3),body+' '+pose+' '+('L' if left else 'R')+' '+('AFTER' if is_after else 'BEFORE'),fill='white')
    records.append({'context':'TP','body':body,'pose':pose,'left':left,'after':is_after,'head_triangle_candidates':hits if pose=='idle' else None})
 assert not any(x.get('head_triangle_candidates',0) for x in records if x['after'] and x.get('pose')=='idle'),'Source head overlap'
 board.save(folder/'native-before-after.png');(folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');(folder/'geometry-gates.json').write_text(json.dumps({'before_connected_components':oldgroups,'after_connected_components':newgroups,'hand_displays_identical':True,'head_candidates_after_idle':0,'runtime':'NOT_RUN / OWNER_PENDING'},indent=2)+'\n');print('Silver mace source:3 old disconnected components ->1 connected weapon; unchanged TP/FP/ground/fixed;20 native cases; no AFTER idle head candidates')
if __name__=='__main__':main()
