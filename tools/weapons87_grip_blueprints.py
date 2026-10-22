"""Before-production grip proposals: actual geometry + native player hand anchors."""
from pathlib import Path
import collections,hashlib,json
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as geo
import weapons87_grips as grips
import armor88_preview as painter
R=geo.R;O=geo.O
def scene(sample,id,arm,d):
 context='thirdperson_'+('righthand' if arm=='RIGHT' else 'lefthand');display=d.get('display',{});pose=display.get(context,display.get('thirdperson_righthand',{}));rot=list(pose.get('rotation',[0,0,0]));tr=list(pose.get('translation',[0,0,0]));scale=pose.get('scale',[1,1,1])
 if arm=='LEFT':rot[1]*=-1;rot[2]*=-1;tr[0]*=-1
 anchor=np.array(sample['hands'][arm]).reshape((4,4),order='F');layers=collections.defaultdict(list)
 for ref,v,uv in geo.faces(d):
  local=((v-8)*scale)@geo.rotation(rot).T+tr;points=(np.column_stack((local/16,np.ones(4)))@anchor.T)[:,:3]*16;layers[ref].append(np.column_stack((points,uv)).tolist())
 return {'layers':[{'id':'skin','texture':sample['texture'],'faces':sample['faces']}]+[{'id':id,'texture':ref,'faces':faces} for ref,faces in layers.items()]}
def main():
 folder=O/'blueprints/grips';folder.mkdir(parents=True,exist_ok=True);samples=[s for s in json.loads((O/'player-native.json').read_text()) if s['pose']=='idle'];plans=grips.plan();painter.texture=lambda ref:geo.texture(ref.replace(':textures/',':').removesuffix('.png'))
 for file in plans:
  existing=folder/(file+'.json')
  if existing.exists() and json.loads(existing.read_text(encoding='utf-8'))['before_sha256']!=hashlib.sha256((grips.A/(file+'.json')).read_bytes()).hexdigest():raise RuntimeError('Production has changed; preserve the existing before-production blueprint evidence.')
 for file,plan in plans.items():
  id=plan['id'];old=geo.model('slavicmyths:item/'+id,'thirdperson_righthand');new=dict(old);new['display']=plan['display'];sheet=Image.new('RGB',(1600,700),'#253039');draw=ImageDraw.Draw(sheet);draw.text((10,7),f'#192 / #115 / {id} / proposed grip revision; exact existing geometry, UV and textures KEEP',fill='white');draw.text((10,25),'Actual native player/hand matrices. BEFORE vs PROPOSED, same scale/camera; NOT game screenshots.',fill='white')
  for row,sample in enumerate(samples):
   for h,arm in enumerate(['RIGHT','LEFT']):
    for after in [False,True]:
     sample_scene=scene(sample,id,arm,new if after else old)
     for view,yaw in enumerate([0,90]):
      x=(h*4+int(after)*2+view)*200;y=55+row*285;sheet.paste(painter.render(sample_scene,yaw),(x,y));draw.text((x+3,y+2),sample['body']+' '+arm+' '+('PROPOSED' if after else 'BEFORE'),fill='white');draw.text((x+3,y+17),'FRONT' if view==0 else 'SIDE',fill='white')
  draw.text((12,638),'Authored grip XYZ '+str(plan['grip'])+'; player32 units, each model unit=1/16 block; exact transform JSON saved beside this sheet.',fill='white');draw.text((12,660),plan['reason']+'; custom scythe runtime helper handled separately.',fill='white');sheet.save(folder/(id+'.png'));data=plan|{'source_model':'src/main/resources/assets/slavicmyths/models/item/'+file+'.json','before_sha256':hashlib.sha256((grips.A/(file+'.json')).read_bytes()).hexdigest(),'design_state':'PROPOSAL_BEFORE_PRODUCTION, not owner runtime accepted'};(folder/(file+'.json')).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print('28 source-backed per-model grip proposals; no production mutations')
if __name__=='__main__':main()
