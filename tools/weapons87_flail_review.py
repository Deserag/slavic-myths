"""Actual Java #164 kinematics + production split JSON/UV, source-only A/B."""
import collections,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp
import armor88_preview as painter
O=g.O;A=g.A if hasattr(g,'A') else g.R/'src/main/resources/assets/slavicmyths'
def align(direction):
 a=np.array([0.,-1,0]);b=np.array([direction[k] for k in ['x','y','z']]);v=np.cross(a,b);c=np.dot(a,b);k=np.array([[0,-v[2],v[1]],[v[2],0,-v[0]],[-v[1],v[0],0]])
 return np.eye(3)+k+k@k/(1+c)
def moved(faces,point,direction,alternate=False):
 rot=align(direction)@(g.rotation([0,90,0]) if alternate else np.eye(3));p=np.array([point[k] for k in ['x','y','z']]);return [(r,(v-8)@rot.T+p,uv) for r,v,uv in faces]
def geometry(id,sample):
 out=g.faces(g.model('slavicmyths:item/'+id+'_handle87'));link=g.faces(g.model('slavicmyths:item/flail_link87'));p=sample['pose']
 for i,point in enumerate(p['links']):out+=moved(link,point,p['directions'][i],i%2==0)
 return out+moved(g.faces(g.model('slavicmyths:item/'+id+'_ball87')),p['ball'],p['directions'][-1])
def main():
 rows=json.loads((O/'flail-motion-native.json').read_text());bodyrows=json.loads((O/'player-native.json').read_text());fp_rows=json.loads((O/'fp-native.json').read_text());archive=zipfile.ZipFile(O/'before-flail-animation.zip');folder=O/'flail-motion-source';folder.mkdir(exist_ok=True);metadata=[]
 def texture(ref):return g.texture(ref.replace(':textures/',':').removesuffix('.png'))
 painter.texture=texture
 for id in ['stone_flail','gold_flail','flail','silver_flail','diamond_flail','perunite_flail','netherite_flail','ataman_flail']:
  selection=[x for x in rows if x['heavy']==(id=='ataman_flail')];old=json.loads(archive.read('src/main/resources/assets/slavicmyths/models/item/'+id+'_held.json'));before=g.faces(old)
  sheet=Image.new('RGB',(1600,35+len(selection)*180),'#253039');d=ImageDraw.Draw(sheet);d.text((5,5),id+' / Java production chain + actual JSON/UV / same fixed wrist; SOURCE, NOT game',fill='white')
  for row,sample in enumerate(selection):
   after=geometry(id,sample)
   for left in [False,True]:
    native=next(x for x in fp_rows if x['id']==id and x['left']==left and x['after']);matrix=np.array(native['matrix']).reshape((4,4),order='F')
    for is_after,faces in [(False,before),(True,after)]:
     active=np.array(sample['fp_matrices']['LEFT' if left else 'RIGHT']).reshape((4,4),order='F') if is_after else matrix
     points=[(r,(np.column_stack(((v-8)/16,np.ones(4)))@active.T)[:,:3],uv) for r,v,uv in faces];im,pixels=fp.render(points,320,180);ball=sample['pose']['ball'];center=(active@np.array([(ball['x']-8)/16,(ball['y']-8)/16,(ball['z']-8)/16,1]))[:3];inside=bool(center[2]<-.04 and abs(center[1])<-center[2]*.7002075382 and abs(center[0])<-center[2]*.7002075382*16/9);col=int(left)*2+int(is_after);sheet.paste(im,(col*320,35+row*180));d.text((col*320+3,38+row*180),sample['phase']+' '+('L' if left else 'R')+' '+('AFTER' if is_after else 'BEFORE'),fill='white');metadata.append({'id':id,'phase':sample['phase'],'context':'fixed-native-FP-wrist','left':left,'after':is_after,'pixels':pixels,'ball_center':center.tolist() if is_after else None,'ball_center_inside_fixed_camera':inside if is_after else None})
   im=g.project(after,170,32,(15,35,0));sheet.paste(im,(1285,35+row*180),im);d.text((1285,38+row*180),'same-scale chain phase',fill='white')
  sheet.save(folder/(id+'-phases.png'))
  for body in ['Steve','Alex']:
   b=next(x for x in bodyrows if x['body']==body and x['pose']=='idle');board=Image.new('RGB',(800,35+len(selection)*280),'#253039');d=ImageDraw.Draw(board);d.text((5,5),id+' '+body+' / native fixed idle body; chain phases only, no simulated attack arms',fill='white')
   for row,sample in enumerate(selection):
    for left in [False,True]:
     ctx='thirdperson_'+('lefthand' if left else 'righthand');display=old['display'][ctx];rotation=display['rotation'].copy();translation=display['translation'].copy()
     if left:rotation[1]*=-1;rotation[2]*=-1;translation[0]*=-1
     matrix=np.array(b['hands']['LEFT' if left else 'RIGHT']).reshape((4,4),order='F')
     for is_after,faces in [(False,before),(True,geometry(id,sample))]:
      layers=collections.defaultdict(list)
      for ref,v,uv in faces:
       local=((v-8)*display['scale'])@g.rotation(rotation).T+translation;points=(np.column_stack((local/16,np.ones(4)))@matrix.T)[:,:3]*16;layers[ref].append(np.column_stack((points,uv)).tolist())
      scene={'layers':[{'id':'skin','texture':b['texture'],'faces':b['faces']}]+[{'id':id,'texture':ref,'faces':v} for ref,v in layers.items()]};col=int(left)*2+int(is_after);board.paste(painter.render(scene,90),(col*200,35+row*280));d.text((col*200+3,38+row*280),sample['phase']+' '+('L' if left else 'R')+' '+('AFTER' if is_after else 'BEFORE'),fill='white')
   board.save(folder/(id+'-'+body+'.png'))
 assert all(x['ball_center_inside_fixed_camera'] for x in metadata if x['after']), 'Ball leaves fixed-wrist source FOV'
 (folder/'matrix.json').write_text(json.dumps(metadata,indent=2)+'\n');print('Actual split geometry + Java motion: 24 A/B source sheets;',len(metadata),'FP records; fixed wrist, runtime NOT_RUN')
if __name__=='__main__':main()
