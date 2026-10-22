"""#174 archived/current source5 IDs, actual native hands, duplicate-fork/connectivity gates."""
import json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp
import armor88_preview as painter
from weapons87_grip_blueprints import scene
from weapons87_head_clearance import triangle_box
from weapons87_silver_mace_review import components
O=g.O;IDS=['berdysh','noon_sickle','carved_staff','storm_staff','veles_staff']
def main():
 archive=zipfile.ZipFile(O/'before-polearms174.zip');overrides={}
 for name in archive.namelist():
  key='src/main/resources/assets/slavicmyths/models/'
  if name.startswith(key) and name.endswith('.json'):overrides['slavicmyths:'+name[len(key):-5]]=json.loads(archive.read(name))
 folder=O/'polearms174-after';folder.mkdir(exist_ok=True);records=[];bodies=json.loads((O/'player-native.json').read_text());fprows=json.loads((O/'fp-native.json').read_text());painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'))
 for id in IDS:
  before=g.model('slavicmyths:item/'+id,'thirdperson_righthand',overrides);after=g.model('slavicmyths:item/'+id,'thirdperson_righthand');board=Image.new('RGB',(1600,1430),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),id+' / actual archive/current JSON+UV/native body and wrist; fixed source poses, NOT Minecraft',fill='white')
  if id=='storm_staff':assert {k:v for k,v in before['display'].items() if k!='gui'}=={k:v for k,v in after['display'].items() if k!='gui'},'Staff hand transforms changed'
  for left in [False,True]:
   row=next(x for x in fprows if x['id']==id and x['left']==left and x['after']);matrix=np.array(row['matrix']).reshape((4,4),order='F')
   for stage,model in [(False,before),(True,after)]:
    faces=[(r,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3],uv) for r,v,uv in g.faces(model)];image,pixels=fp.render(faces,400,225);col=int(left)*2+int(stage);board.paste(image,(col*400,30));draw.text((col*400+3,34),'FP '+('L' if left else 'R')+' '+('AFTER' if stage else 'BEFORE'),fill='white');records.append({'id':id,'context':'FP','left':left,'after':stage,'pixels':pixels})
  for i,(body,pose) in enumerate([('Steve','idle'),('Alex','idle'),('Steve','walk_t10'),('Alex','walk_t10')]):
   sample=next(x for x in bodies if x['body']==body and x['pose']==pose)
   for left in [False,True]:
    for stage,model in [(False,before),(True,after)]:
     native=scene(sample,id,'LEFT' if left else 'RIGHT',model);hits=0
     if pose=='idle':
      for layer in native['layers'][1:]:
       for face in layer['faces']:
        vertices=np.array(face)[:,:3]
        for indices in [(0,1,2),(0,2,3)]:hits+=int(triangle_box(vertices[list(indices)],np.array([-4.,-8.,-4.]),np.array([4.,0.,4.])))
     col=int(left)*2+int(stage);y=280+i*285
     for view,yaw in enumerate([0,90]):
      x=col*400+view*200;board.paste(painter.render(native,yaw),(x,y));draw.text((x+3,y+3),body+' '+pose+' '+('L' if left else 'R')+' '+('AFTER' if stage else 'BEFORE'),fill='white')
     records.append({'id':id,'context':'TP','body':body,'pose':pose,'left':left,'after':stage,'head_triangle_candidates':hits if pose=='idle' else None})
  board.save(folder/(id+'.png'))
 oldstorm=overrides['slavicmyths:item/storm_staff'];carved=overrides['slavicmyths:item/carved_staff'];newstorm=g.model('slavicmyths:item/storm_staff');shape=lambda x:[{k:v for k,v in e.items() if k!='faces'} for e in x['elements']];assert shape(oldstorm)==shape(carved),'Original duplicate evidence changed';assert shape(newstorm)!=shape(carved),'New storm still duplicate';assert len(components(newstorm['elements']))==1,'Floating focus';assert not any(x.get('head_triangle_candidates',0) for x in records if x['after'] and x.get('pose')=='idle'),'Source head candidate'
 preserved=[]
 for name in archive.namelist():
  if name.endswith('/models/item/storm_staff.json'):continue
  assert (g.R/name).read_bytes()==archive.read(name),'Unexpected KEEP/shared texture edit '+name;preserved.append(name)
 (folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');(folder/'preserved-files.json').write_text(json.dumps(preserved,indent=2)+'\n');(folder/'geometry-gates.json').write_text(json.dumps({'old_storm_carved_same_shape':True,'new_storm_distinct_shape':True,'new_connected_components':components(newstorm['elements']),'hand_displays_unchanged':True,'after_idle_head_candidates':0,'runtime':'NOT_RUN / OWNER_PENDING'},indent=2)+'\n');print('5 actual source A/B sheets/100 native cases; storm new connected profile,4 KEEP/shared textures unchanged,0 AFTER fixed-idle head candidates; game NOT_RUN')
if __name__=='__main__':main()
