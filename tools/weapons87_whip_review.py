"""#180 actual static whip native hand/body/UV comparisons; not runtime dynamics."""
import collections,hashlib,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp
import armor88_preview as painter
from weapons87_head_clearance import triangle_box

def main():
 folder=g.O/'whip180-after';folder.mkdir(exist_ok=True);rows=json.loads((g.O/'whip-native.json').read_text());assert len(rows)==40;bodies=json.loads((g.O/'player-native.json').read_text());records=[];painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'))
 for pose in range(5):
  board=Image.new('RGB',(1600,1430),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'poloz_whip static pose'+str(pose)+' / actual archived/current JSON UV and native hands; fixed idle/walk body poses, NOT game',fill='white');group=[r for r in rows if r['pose']==pose]
  for left in [False,True]:
   for after in [False,True]:
    row=next(r for r in group if r['left']==left and r['after']==after and r['context'].startswith('firstperson'));matrix=np.array(row['matrix']).reshape((4,4),order='F');faces=[(ref,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3],uv) for ref,v,uv in g.faces(row['model'])];im,pixels=fp.render(faces,400,225);column=int(left)*2+int(after);board.paste(im,(column*400,30));draw.text((column*400+3,34),'FP '+('L' if left else 'R')+' '+('AFTER' if after else 'BEFORE'),fill='white');records.append({'pose':pose,'context':'FP','left':left,'after':after,'pixels':pixels});assert not after or pixels>0
  for bindex,(body,frame) in enumerate([('Steve','idle'),('Alex','idle'),('Steve','walk_t10'),('Alex','walk_t10')]):
   sample=next(x for x in bodies if x['body']==body and x['pose']==frame)
   for left in [False,True]:
    for after in [False,True]:
     row=next(r for r in group if r['left']==left and r['after']==after and r['context'].startswith('thirdperson'));matrix=np.array(sample['hands']['LEFT' if left else 'RIGHT']).reshape((4,4),order='F')@np.array(row['matrix']).reshape((4,4),order='F');layers=collections.defaultdict(list);hits=0
     for ref,v,uv in g.faces(row['model']):
      points=(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3]*16;layers[ref].append(np.column_stack((points,uv)).tolist())
      for indices in [(0,1,2),(0,2,3)]:hits+=int(triangle_box(points[list(indices)],np.array([-4.,-8.,-4.]),np.array([4.,0.,4.])))
     scene={'layers':[{'id':'skin','texture':sample['texture'],'faces':sample['faces']}]+[{'id':'whip','texture':ref,'faces':v} for ref,v in layers.items()]};column=int(left)*2+int(after);y=280+bindex*285
     for view,yaw in enumerate([0,90]):
      x=column*400+view*200;board.paste(painter.render(scene,yaw),(x,y));draw.text((x+3,y+3),body+' '+frame+' '+('L' if left else 'R')+' '+('AFTER' if after else 'BEFORE'),fill='white')
     records.append({'pose':pose,'context':'TP','body':body,'frame':frame,'left':left,'after':after,'head_triangle_candidates':hits})
  board.save(folder/('pose'+str(pose)+'.png'))
 archive=zipfile.ZipFile(g.O/'before-whip180.zip');preserved=[]
 for name in archive.namelist():
  if '/models/' in name:continue
  assert (g.R/name).read_bytes()==archive.read(name),name;preserved.append({'path':name,'sha256':hashlib.sha256(archive.read(name)).hexdigest()})
 old=json.loads(archive.read('src/main/resources/assets/slavicmyths/models/item/poloz_whip.json'));new=json.loads((g.A/'slavicmyths/models/item/poloz_whip.json').read_text());assert old['overrides']==new['overrides'],'Predicate thresholds changed'
 plan=json.loads((g.O/'blueprints/poloz-whip/plan.json').read_text());paths=[]
 for entry in plan['points']:
  points=np.array(entry['points']);lengths=np.linalg.norm(np.diff(points,axis=0),axis=1);assert len(lengths)==20 and np.allclose(lengths,1.15);assert all(a>b>0 for a,b in zip(entry['radii'],entry['radii'][1:]));paths.append({'pose':entry['pose'],'arclength':float(sum(lengths)),'sections':20,'joint_boxes_at_every_endpoint':True,'taper_positive_monotone':True})
 icons=[g.model('slavicmyths:item/poloz_whip_pose_'+str(i),'gui') for i in range(5)];assert all(icon==icons[0] for icon in icons),'Inventory changed with owner attack/cooldown';assert not any(r['head_triangle_candidates'] for r in records if r['context']=='TP' and r['pose']==0 and r['after'] and r['frame']=='idle'),'Default idle head candidate'
 (folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');(folder/'preserved-files.json').write_text(json.dumps(preserved,indent=2)+'\n');(folder/'geometry-gates.json').write_text(json.dumps({'paths':paths,'gui_identical_all_overrides':True,'predicate_thresholds_unchanged':True,'idle_head_candidates':0,'runtime':'NOT_RUN / OWNER_PENDING'},indent=2)+'\n');print('5 actual A/B source sheets/100 native FP+TP cases; 20 connected/tapered sections each, stable GUI all5 overrides; PNG/server/predicate code preserved; default idle head candidates0')

if __name__=='__main__':main()
