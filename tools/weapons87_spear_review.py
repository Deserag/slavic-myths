"""#171 exact archived/current JSON/UV with native ItemTransform and native body matrices."""
import collections,json,zipfile,hashlib
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp
import armor88_preview as painter
from weapons87_head_clearance import triangle_box
O=g.O
FIX=['gold_spear','iron_spear','diamond_spear','perunite_spear','netherite_spear']
def main():
 rows=json.loads((O/'spear-native.json').read_text());bodies=json.loads((O/'player-native.json').read_text());folder=O/'spears-after';folder.mkdir(exist_ok=True);records=[]
 painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'))
 for id in dict.fromkeys(x['id'] for x in rows):
  group=[x for x in rows if x['id']==id];sheet=Image.new('RGB',(1600,1430),'#253039');draw=ImageDraw.Draw(sheet);draw.text((8,6),id+' / actual archived/current native source, fixed idle/walk poses; NOT Minecraft',fill='white')
  for left in [False,True]:
   for after in [False,True]:
    row=next(x for x in group if x['left']==left and x['after']==after and x['context'].startswith('firstperson'));matrix=np.array(row['matrix']).reshape((4,4),order='F');faces=g.faces(row['model']);projected=[(ref,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3],uv) for ref,v,uv in faces];im,pixels=fp.render(projected,400,225);col=int(left)*2+int(after);sheet.paste(im,(col*400,30));draw.text((col*400+3,34),'FP '+('L' if left else 'R')+' '+('AFTER' if after else 'BEFORE'),fill='white');records.append({'id':id,'context':'FP','left':left,'after':after,'pixels':pixels})
  for bindex,(body,pose) in enumerate([('Steve','idle'),('Alex','idle'),('Steve','walk_t10'),('Alex','walk_t10')]):
   bodyrow=next(x for x in bodies if x['body']==body and x['pose']==pose)
   for left in [False,True]:
    for after in [False,True]:
     row=next(x for x in group if x['left']==left and x['after']==after and x['context'].startswith('thirdperson'));matrix=np.array(bodyrow['hands']['LEFT' if left else 'RIGHT']).reshape((4,4),order='F')@np.array(row['matrix']).reshape((4,4),order='F');layers=collections.defaultdict(list);hits=0
     for ref,v,uv in g.faces(row['model']):
      points=(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3]*16;layers[ref].append(np.column_stack((points,uv)).tolist())
      if pose=='idle':
       for indices in [(0,1,2),(0,2,3)]:hits+=int(triangle_box(points[list(indices)],np.array([-4.,-8.,-4.]),np.array([4.,0.,4.])))
     scene={'layers':[{'id':'skin','texture':bodyrow['texture'],'faces':bodyrow['faces']}]+[{'id':id,'texture':ref,'faces':v} for ref,v in layers.items()]};col=int(left)*2+int(after)
     for view,yaw in enumerate([0,90]):
      image=painter.render(scene,yaw);x=col*400+view*200;y=280+bindex*285;sheet.paste(image,(x,y));draw.text((x+3,y+3),body+' '+pose+' '+('L' if left else 'R')+' '+('AFTER' if after else 'BEFORE'),fill='white')
     records.append({'id':id,'context':'TP','body':body,'pose':pose,'left':left,'after':after,'native_idle_head_triangle_candidates':hits if pose=='idle' else None,'limits':'Actual fixed native poses; triangles ignore alpha, not game clipping measurement'})
  sheet.save(folder/(id+'.png'))
 with zipfile.ZipFile(O/'before-spear-family.zip') as z:
  preserved=[]
  for name in z.namelist():
   path=g.R/name
   if name.endswith('.png') or not any('/'+id+'.json' in name for id in FIX):assert path.read_bytes()==z.read(name),'Unexpected spear family edit '+name;preserved.append(name)
  (folder/'preserved-files.json').write_text(json.dumps(preserved,indent=2)+'\n')
 assert not any(x.get('native_idle_head_triangle_candidates',0) for x in records if x['after'] and x.get('pose')=='idle'),'New source head candidate'
 (folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');print('9 spear A/B sheets;72 native context transforms;180 source cases; all PNG and4 KEEP sources identical; no AFTER native-idle head candidates; game NOT_RUN')
if __name__=='__main__':main()
